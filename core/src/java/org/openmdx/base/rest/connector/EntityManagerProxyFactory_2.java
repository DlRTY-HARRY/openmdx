/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Description: Entity Manager Proxy Factory
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2012, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in
 *   the documentation and/or other materials provided with the
 *   distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * ------------------
 * 
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.base.rest.connector;

import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import javax.jdo.Constants;
import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalDataStoreException;
import javax.jdo.PersistenceManagerFactory;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.Interaction;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ResourceAllocationException;

import org.openmdx.application.configuration.Configuration;
import org.openmdx.application.spi.PropertiesConfigurationProvider;
import org.openmdx.base.Version;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.rest.DataObjectManager_1;
import org.openmdx.base.accessor.rest.spi.BasicCache_2;
import org.openmdx.base.accessor.rest.spi.Switch_2;
import org.openmdx.base.aop0.PlugIn_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.cci.ConfigurableProperty;
import org.openmdx.base.persistence.spi.AbstractPersistenceManagerFactory;
import org.openmdx.base.persistence.spi.PersistenceManagers;
import org.openmdx.base.resource.cci.ConnectionFactory;
import org.openmdx.base.resource.spi.Port;
import org.openmdx.base.resource.spi.ResourceExceptions;
import org.openmdx.base.resource.spi.RestInteractionSpec;
import org.openmdx.base.rest.spi.ConnectionAdapter;
import org.openmdx.base.rest.cci.RestConnectionSpec;
import org.openmdx.base.transaction.TransactionAttributeType;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.BeanFactory;
import org.openmdx.kernel.loading.Factory;
import org.w3c.cci2.SparseArray;

/**
 * Entity Manager Proxy Factory
 */
public class EntityManagerProxyFactory_2 extends AbstractPersistenceManagerFactory<DataObjectManager_1_0> {

    /**
     * Constructor 
     *
     * @param configuration
     */
    protected EntityManagerProxyFactory_2(
        Map<?,?> configuration
    ){
        super(configuration);
        //
        // Data Manager Properties
        // 
        try {
            Properties properties = PropertiesConfigurationProvider.toProperties(configuration);
            Configuration dataManagerConfiguration = PropertiesConfigurationProvider.getConfiguration(
                properties,
                "org", "openmdx", "jdo", "DataManager"
            );
            this.optimalFetchSize = (Integer) dataManagerConfiguration.values(
                "optimalFetchSize"
            ).get(0);
            this.cacheThreshold = (Integer) dataManagerConfiguration.values(
                "cacheThreshold"
            ).get(0);
            SparseArray<?> plugIns = dataManagerConfiguration.values(
                "plugIn"
            );
            if(plugIns.isEmpty()) {
                this.plugIns = DEFAULT_PLUG_INS;
            } else {
                this.plugIns = new PlugIn_1_0[plugIns.size()];
                ListIterator<?> p = plugIns.populationIterator();
                for(
                    int i = 0;
                    i < this.plugIns.length;
                    i++
                ){
                    this.plugIns[i] = new BeanFactory<PlugIn_1_0>(
                        "class",
                        PropertiesConfigurationProvider.getConfiguration(
                            properties,
                            toSection(p.next())
                        ).entries()
                    ).instantiate();
                }
            }
            
            
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new JDOFatalDataStoreException(
                    "Data object manager factory set up failure",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.INVALID_CONFIGURATION
                    )
                )
            );
        }
        //
        // Connection Factory
        // 
        try {
            Object connectionFactory = super.getConnectionFactory();
            if(connectionFactory == null) {
                String connectionFactoryName = super.getConnectionFactoryName();
                if(connectionFactoryName == null) { 
                    String connectionURL = super.getConnectionURL();
                    if(connectionURL == null) {
                        throw new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CONFIGURATION,
                            "Neither connection factory nor connection factory name nor connection URL have been specified",
                            new BasicException.Parameter(
                                "expected",
                                ConfigurableProperty.ConnectionFactory.qualifiedName(), 
                                ConfigurableProperty.ConnectionFactoryName.qualifiedName(),
                                ConfigurableProperty.ConnectionURL.qualifiedName()
                             )
                        );
                    } else {
                        connectionFactory = getConnectionFactoryByURL(configuration, connectionURL);
                    }
                } else {
                    connectionFactory = getConnectionFactoryByName(connectionFactoryName);
                }
            }
            this.destinations = Collections.singletonMap(
                PROXY_PATTERN,
                newPort(connectionFactory)
            );
        } catch (ServiceException exception) {
            throw BasicException.initHolder(
                new JDOFatalDataStoreException(
                    "Data manager proxy factory set up failure",
                    BasicException.newEmbeddedExceptionStack(exception)
                )
            );
        }
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 7461507288357096266L;

    /**
     * The aop0 plug-ins
     */
    private final PlugIn_1_0[] plugIns;

    /**
     * The optimal fetch size
     */
    private final Integer optimalFetchSize;

    /**
     * Collections smaller than this value are cached before being evaluated
     */
    private final Integer cacheThreshold;
    
    /**
     * The destinations
     */
    private final Map<Path,Port> destinations;

    /**
     * The standard plug-ins
     */
    private static final PlugIn_1_0[] DEFAULT_PLUG_INS = {
        new ProxyPlugIn_1()
    };
    
    /**
     * Catch all proxied objects
     */
    private static final Path PROXY_PATTERN = new Path("%").lock();
    
    /**
     * The resource adapter's metadata
     */
    private final static ResourceAdapterMetaData RESOURCE_ADAPTER_META_DATA = new ResourceAdapterMetaData(){

        public String getAdapterName() {
            return "openMDX/REST";
        }

        public String getAdapterShortDescription() {
            return "openMDX/2 REST Resource Adapter";
        }

        public String getAdapterVendorName() {
            return "openMDX";
        }

        public String getAdapterVersion() {
            return Version.getSpecificationVersion();
        }

        public String[] getInteractionSpecsSupported() {
            return new String[]{RestInteractionSpec.class.getName()};
        }

        /**
         * Retrieve the JCA specification version
         * 
         * @return the JCA specification version
         */
        public String getSpecVersion() {
            return "1.5.";
        }

        public boolean supportsExecuteWithInputAndOutputRecord() {
            return true; 
        }

        public boolean supportsExecuteWithInputRecordOnly() {
            return true;
        }

        public boolean supportsLocalTransactionDemarcation(
        ) {
            return true;
        }
        
    };

        
    /**
     * Acquire the connection factory by its URL and driver name
     * 
     * @param configuration
     * @param connectionURL
     * 
     * @return the connection factory
     * 
     * @throws ServiceException
     */
    protected Object getConnectionFactoryByURL(
        Map<?,?> configuration,
        String connectionURL
    ) throws ServiceException{
        String connectionDriverName = super.getConnectionDriverName();
        if(connectionDriverName == null) {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Together with a connection URL you have to specify a connection driver",
                new BasicException.Parameter(
                    "connection-url-property", 
                    ConfigurableProperty.ConnectionURL.qualifiedName()
                ),
                new BasicException.Parameter(
                    "required-connection-driver-properties", 
                    ConfigurableProperty.ConnectionDriverName.qualifiedName()
                ),
                new BasicException.Parameter(
                    "optional-connection-driver-properties", 
                    ConfigurableProperty.ConnectionUserName.qualifiedName(), 
                    ConfigurableProperty.ConnectionPassword.qualifiedName()
                ),
                new BasicException.Parameter(
                    "connection-driver-interface", 
                    Port.class.getName()
                )
            );
        } else {
            Map<String,Object> connectionDriverProperties = new HashMap<String,Object>();
            //
            // Standard Properties
            //
            connectionDriverProperties.put(
                "ConnectionURL",
                connectionURL
            );
            String userName = super.getConnectionUserName();
            if(userName != null) {
                connectionDriverProperties.put(
                    "UserName",
                    userName
                );
            }
            String password = super.getConnectionPassword();
            if(password != null) {
                connectionDriverProperties.put(
                    "Password",
                    password
                );
            }
            //
            // Specific Properties
            //
            try {
                Configuration connectionDriverConfiguration = PropertiesConfigurationProvider.getConfiguration(
                    PropertiesConfigurationProvider.toProperties(configuration),
                    "org", "openmdx", "jdo", "ConnectionDriver"
                );
                connectionDriverProperties.putAll(connectionDriverConfiguration.entries());
            } catch (ServiceException exception) {
                throw BasicException.initHolder(
                    new JDOFatalDataStoreException(
                        "Data object manager factory set up failure",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.INVALID_CONFIGURATION
                        )
                    )
                );
            }
            return BeanFactory.newInstance(
                connectionDriverName,
                connectionDriverProperties
            );
        }
    }
    
    /**
     * Create a new Port
     * 
     * @param connectionFactory
     * 
     * @return a new <code>Port</code> for the given connection factory
     * 
     * @throws ServiceException
     */
    protected Port newPort(
        final Object connectionFactory
    ) throws ServiceException{
        if(connectionFactory instanceof ConnectionFactory) {
            return new Port(){
                
                /* (non-Javadoc)
                 * @see org.openmdx.base.resource.spi.Port#getInteraction(javax.resource.cci.Connection)
                 */
                public Interaction getInteraction(
                    Connection connection
                ) throws ResourceException {
                    if(connection instanceof ConnectionAdapter) {
                        return ((ConnectionFactory)connectionFactory).getConnection(
                            ((ConnectionAdapter)connection).getConnectionSpec()
                        ).createInteraction(
                        );
                    }
                    throw ResourceExceptions.initHolder(
                        new ResourceAllocationException(
                            "Connection can't be established",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("expected", ConnectionAdapter.class.getName()),
                                new BasicException.Parameter("actual", connection == null ? null : connection.getClass().getName())
                            )
                        )
                    );
                }
                
            };
        } else if(connectionFactory instanceof Factory<?>) {
            Factory<?> portFactory = (Factory<?>)connectionFactory; 
            Object port = portFactory.instantiate();
            if(port instanceof Port) {
                return (Port) port;
            } else {
                throw new ServiceException(
                    new ResourceAllocationException(
                        "Inapporopriate connection driver, can't create port",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter(
                                "expected", 
                                Factory.class.getName() + "<" + Port.class.getName() + ">"
                            ),
                            new BasicException.Parameter(
                                "actual", 
                                Factory.class.getName() + "<" + (port == null ? null : port.getClass().getName()) + ">"
                            )
                        )
                    )
                );
            }
        } else {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.ACTIVATION_FAILURE,
                "Inapporopriate connection factory, can't create port",
                new BasicException.Parameter(
                    "actual", 
                    connectionFactory == null ? null : connectionFactory.getClass().getName()
                )
            );
        }
    }
    
    /**
     * The default configuration
     */
    protected static final Map<String, Object> DEFAULT_CONFIGURATION = new HashMap<String, Object>(
        AbstractPersistenceManagerFactory.DEFAULT_CONFIGURATION
    );

    /**
     * The method is used by JDOHelper to construct an instance of 
     * <code>PersistenceManagerFactory</code> based on user-specified 
     * properties.
     * 
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map<?,?> props
    ){
        return getPersistenceManagerFactory(
            Collections.EMPTY_MAP,
            props
        );
    }

    /**
     * The method is used by JDOHelper to construct an instance of 
     * <code>PersistenceManagerFactory</code> based on user-specified 
     * properties.
     * 
     * @param overrides
     * @param props
     * 
     * @return a new <code>PersistenceManagerFactory</code>
     */
    public static PersistenceManagerFactory getPersistenceManagerFactory (
        Map<?,?> overrides, 
        Map<?,?> props
    ){
        Map<Object,Object> configuration = new HashMap<Object,Object>(DEFAULT_CONFIGURATION);
        configuration.putAll(props);
        configuration.putAll(overrides);
        return new EntityManagerProxyFactory_2(configuration);
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    protected DataObjectManager_1_0 newPersistenceManager(
        String userid,
        String password
    ) {
        try {
            RestConnectionSpec connectionSpec = new RestConnectionSpec(userid, password);
            return new DataObjectManager_1(
                this,
                true, // proxy
                userid == null ? null : PersistenceManagers.toPrincipalChain(userid),
                ConnectionAdapter.newInstance(
                    RESOURCE_ADAPTER_META_DATA,
                    connectionSpec,     
                    TransactionAttributeType.SUPPORTS, 
                    new Switch_2(
                        new BasicCache_2(), 
                        this.destinations
                    )
                ), 
                null, // connection2
                this.plugIns, 
                this.optimalFetchSize, 
                this.cacheThreshold, 
                connectionSpec  
            );
        } catch (ResourceException exception) {
            throw BasicException.initHolder(
                new JDODataStoreException(
                    "The data object manager proxy factory is unable to establish connection(s)",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.MEDIA_ACCESS_FAILURE
                    )
                )
            );
        }
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.AbstractPersistenceManagerFactory_1#newPersistenceManager(java.lang.String, java.lang.String)
     */
    @Override
    protected DataObjectManager_1_0 newPersistenceManager(
    ) {
        return newPersistenceManager(
            System.getProperty("user.name"), 
            null
        );
    }

    /**
     * Provide a configuration entry's section
     * 
     * @param name the configuration entry
     * 
     * @return the configuration entry's section
     */
    private static String[] toSection(
        Object name
    ){
        return ((String)name).split("\\.");
    }
    
    static {
        EntityManagerProxyFactory_2.DEFAULT_CONFIGURATION.put(
            ConfigurableProperty.TransactionType.qualifiedName(),
            Constants.RESOURCE_LOCAL
        );    
    }

}
