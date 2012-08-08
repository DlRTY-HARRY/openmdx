/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: ViewManager_1.java,v 1.40 2010/04/15 10:25:06 hburger Exp $
 * Description: View Manager
 * Revision:    $Revision: 1.40 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/15 10:25:06 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jdo.Extent;
import javax.jdo.FetchGroup;
import javax.jdo.FetchPlan;
import javax.jdo.JDOException;
import javax.jdo.JDOFatalUserException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.JDOUserException;
import javax.jdo.ObjectState;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.datastore.Sequence;
import javax.jdo.listener.InstanceLifecycleListener;
import javax.resource.cci.InteractionSpec;

import org.openmdx.application.mof.cci.ModelAttributes;
import org.openmdx.base.accessor.cci.DataObjectManager_1_0;
import org.openmdx.base.accessor.cci.DataObject_1_0;
import org.openmdx.base.accessor.spi.AbstractTransaction_1;
import org.openmdx.base.accessor.spi.ExceptionHelper;
import org.openmdx.base.aop1.PlugIn_1_0;
import org.openmdx.base.collection.ConcurrentWeakRegistry;
import org.openmdx.base.collection.MarshallingList;
import org.openmdx.base.collection.MarshallingMap;
import org.openmdx.base.collection.MarshallingSet;
import org.openmdx.base.collection.MarshallingSparseArray;
import org.openmdx.base.collection.Unmarshalling;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.marshalling.Marshaller;
import org.openmdx.base.mof.cci.AggregationKind;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.Model_1_0;
import org.openmdx.base.mof.cci.Multiplicities;
import org.openmdx.base.mof.spi.ModelUtils;
import org.openmdx.base.mof.spi.Model_1Factory;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.MarshallingInstanceLifecycleListener;
import org.openmdx.base.resource.InteractionSpecs;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.SortedMaps;

/**
 * View Manager
 * <p>
 * The manager returns the same object for a given object id as long as it is not 
 * garbage collected.
 */
public class ViewManager_1  
    implements Marshaller, Serializable, DataObjectManager_1_0  
{

    /**
     * Constructor 
     *
     * @param factory
     * @param connection
     * @param plugIns
     * @param objectFactories
     * @param interactionSpec
     * @param transaction
     */
    private ViewManager_1(
        PersistenceManagerFactory factory, 
        DataObjectManager_1_0 connection,
        PlugIn_1_0[] plugIns,
        ConcurrentMap<InteractionSpec, ViewManager_1> objectFactories,
        InteractionSpec interactionSpec,
        Transaction transaction
    ){
        this.factory = factory;
        this.connection = connection;
        this.plugIns = plugIns;
        this.objectFactories = objectFactories;
        this.interactionSpec = interactionSpec;
        this.transaction =  transaction == null ? new AbstractTransaction_1(){

            @Override
            protected Transaction getDelegate() {
                return ViewManager_1.this.connection.currentTransaction();
            }

            @Override
            public PersistenceManager getPersistenceManager() {
                return ViewManager_1.this;
            }
            
        } : transaction;
        this.registry = new ConcurrentWeakRegistry<DataObject_1_0,ObjectView_1>();
        this.instanceLifecycleListener = new MarshallingInstanceLifecycleListener(
            this.registry,
            this.interactionSpec == null ? this : null
        );
        connection.addInstanceLifecycleListener(
            this.instanceLifecycleListener, 
            (Class<?>[])null
        );
    }
    
    /**
     * Constructor 
     *
     * @param factory
     * @param connection
     * @param plugIns
     */
    ViewManager_1(
        PersistenceManagerFactory factory, 
        DataObjectManager_1_0 connection,
        PlugIn_1_0[] plugIns
    ){
        this(
            factory,
            connection,
            plugIns,
            new ConcurrentHashMap<InteractionSpec, ViewManager_1>(), // objectFactories
            null, // interactionSpec
            null // transaction
        );
        this.objectFactories.put(InteractionSpecs.NULL, this);
    }

    /**
     * Dispatches<ol>
     * <li>to its registered children
     * <li>to the instance acquired from the manager by its object id
     * </ol>
     */
    private MarshallingInstanceLifecycleListener instanceLifecycleListener; 

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = 4121130329538180151L;

    /**
     * The plug-ins
     */
    private final PlugIn_1_0[] plugIns;
    
    /**
     *  
     */
    DataObjectManager_1_0 connection;

    /**
     *  
     */
    private final InteractionSpec interactionSpec;

    /**
     * 
     */
    private ConcurrentMap<InteractionSpec, ViewManager_1> objectFactories;
    
    /**
     * The transaction view
     */
    private final Transaction transaction;
    
    /**
     * 
     */
    private PersistenceManagerFactory factory;
    
    /**
     * Maps data objects to object views
     */
    private ConcurrentWeakRegistry<DataObject_1_0,ObjectView_1> registry;

    /**
     * Unregister an object view
     * 
     * @param key
     */
    void unregister(
        DataObject_1_0 key
    ){
        this.registry.remove(key);
    }

    /**
     * Register an object view
     * 
     * @param key
     * @param value
     */
    void register(
        DataObject_1_0 key,
        ObjectView_1 value
    ){
        this.registry.put(key, value);
    }
    
    /**
     * Return connection assigned to this manager.
     */
    private DataObjectManager_1_0 getConnection(
    ) {
    	try {
	        validateState();
        } catch (ServiceException exception) {
        	throw BasicException.initHolder(
	        	new JDOFatalUserException(
	        		"Unable to retrieve a closed persistence manager's connection",
	        		BasicException.newEmbeddedExceptionStack(exception)
	        	)
	        );
        }
        return this.connection;
    }

    /**
     * Retrieve the interaction spec associated with this object factory.
     * 
     * @return the interaction spec associated with this object factory
     */
    InteractionSpec getInteractionSpec(){
        return this.interactionSpec;
    }

    /**
     * Provides the <code>plugIn</code> array
     * 
     * @return the <code>plugIn</code> array
     */
    protected PlugIn_1_0[] getPlugIn(){
        return this.plugIns;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_4#getObjectFactory(javax.resource.cci.InteractionSpec)
     */
    @Override
    public ViewManager_1 getPersistenceManager(
        InteractionSpec interactionSpec
    ) {
        InteractionSpec key = interactionSpec == null ? InteractionSpecs.NULL : interactionSpec;
        ViewManager_1 objectFactory = this.objectFactories.get(key);
        if(objectFactory == null) {
            ViewManager_1 concurrent = this.objectFactories.putIfAbsent(
                key,
                objectFactory = new ViewManager_1(
                    null, // factory
                    this.connection,
                    this.plugIns,
                    this.objectFactories,
                    interactionSpec,
                    this.transaction
                )
            );
            if(concurrent != null) {
                objectFactory = concurrent;
            }
        }
        return objectFactory;
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.spi.PersistenceManager_1_0#getFeatureReplacingObjectById(java.lang.Object, java.lang.String)
     */
    @Override
    public Object getFeatureReplacingObjectById(
        UUID transientObjectId,
        String featureName
    ) {
        DataObject_1_0 source = (DataObject_1_0) getObjectById(transientObjectId);
        try {
            if(source == null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_FOUND,
                    "Object not found",
                    new BasicException.Parameter("xri", transientObjectId),
                    new BasicException.Parameter("feature", featureName)
                ); 
            }
            Model_1_0 model = Model_1Factory.getModel();
            ModelElement_1_0 featureDef;
            if (featureName.indexOf(':') >= 0) {
                //
                // Fully qualified feature name. Lookup in model
                //
                featureDef = model.getElement(featureName);
            } else {
                //
                // Get all features of class and find feature with featureName
                //
                ModelElement_1_0 classifierDef = model.getElement(source.objGetClass());
                featureDef = classifierDef== null ? null  : model.getFeatureDef(
                    classifierDef,
                    featureName,
                    false
                );
            }
            if(featureDef == null) { 
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_MEMBER_NAME,
                    "feature not found",
                    new BasicException.Parameter("xri", transientObjectId),
                    new BasicException.Parameter("class", source.objGetClass()),
                    new BasicException.Parameter("feature", featureName)
                ); 
            }
            String cciFeatureName = (String) featureDef.objGetValue("name"); 
            if (!model.isReferenceType(featureDef)) throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_MEMBER_NAME,
                "model element not of type " + ModelAttributes.REFERENCE,
                new BasicException.Parameter("model element", featureDef)
            ); 
            if(model.referenceIsStoredAsAttribute(featureDef)) {
                //
                // Reference Stored As Attribute
                //
                String multiplicity = ModelUtils.getMultiplicity(featureDef);
                if(
                    Multiplicities.SINGLE_VALUE.equals(multiplicity) || 
                    Multiplicities.OPTIONAL_VALUE.equals(multiplicity) 
                ){
                    Object pc = source.objGetValue(cciFeatureName); 
                    return JDOHelper.isPersistent(pc) ?
                        JDOHelper.getObjectId(pc) :
                        JDOHelper.getTransactionalObjectId(pc);
                } else if (Multiplicities.LIST.equals(multiplicity)) { 
                    return new MarshallingList<Object>(
                        ObjectIdMarshaller.INSTANCE,
                        source.objGetList(cciFeatureName),
                        Unmarshalling.RELUCTANT
                    );
                } else if (Multiplicities.SET.equals(multiplicity)) { 
                    return new MarshallingSet<Object>(
                        ObjectIdMarshaller.INSTANCE,
                        source.objGetSet(cciFeatureName),
                        Unmarshalling.RELUCTANT
                    );
                } else if (Multiplicities.SPARSEARRAY.equals(multiplicity)) { 
                    return new MarshallingSparseArray(
                        ObjectIdMarshaller.INSTANCE,
                        SortedMaps.asSparseArray(source.objGetSparseArray(cciFeatureName)),
                        Unmarshalling.RELUCTANT
                    );
                } else throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    "Unsupported multiplicity",
                    ExceptionHelper.newObjectIdParameter("id", source),
                    new BasicException.Parameter("feature", cciFeatureName),
                    new BasicException.Parameter("multiplicity", multiplicity)
                 );
            } else {
                //
                // Aggregation
                //
                ModelElement_1_0 exposedEnd = model.getElement(
                    featureDef.objGetValue("exposedEnd")
                );
                // navigation to parent object is performed locally by removing
                // the last to object path components
                if(
                    AggregationKind.SHARED.equals(exposedEnd.objGetValue("aggregation")) || 
                    AggregationKind.COMPOSITE.equals(exposedEnd.objGetValue("aggregation"))
                ) {
                    Path childId = source.jdoGetObjectId();
                    return childId.getPrefix(childId.size() - 2);
                }  else {
                    return new MarshallingMap<String, DataObject_1_0>(
                        ObjectIdMarshaller.INSTANCE,
                        source.objGetContainer(cciFeatureName),
                        Unmarshalling.RELUCTANT
                    );
                }
            }
        }  catch (ServiceException exception) {
            throw BasicException.initHolder(
                new JDOUserException(
                    "Could not retrieve a feature while replacing objects by their id",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        exception.getExceptionDomain(),
                        exception.getExceptionCode(),
                        new BasicException.Parameter("transientObjectId", transientObjectId),
                        new BasicException.Parameter("feature", featureName)
                    ),
                    source
               )
           );
        }  catch (RuntimeServiceException exception) {
            throw BasicException.initHolder(
                new JDOUserException(
                    "Could not retrieve a feature while replacing objects by their id",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        exception.getExceptionDomain(),
                        exception.getExceptionCode(),
                        new BasicException.Parameter("transientObjectId", transientObjectId),
                        new BasicException.Parameter("feature", featureName)
                    ),
                    source
               )
           );
        }
    }

    
    //------------------------------------------------------------------------
    // Implements PersistenceManager_1_0
    //------------------------------------------------------------------------

    /**
     * Close the basic accessor.
     * <p>
     * After the close method completes, all methods on the ObjectFactory_1_0
     * instance except isClosed throw a ILLEGAL_STATE RuntimeServiceException.
     */
    @Override
    public void close(
    ) {
        if (!isClosed()) {
            this.instanceLifecycleListener = null;
            this.connection.close();
            this.connection = null;
            this.registry.close();
            this.registry = null;
        }
    }

    /**
     * Tells whether the object factory has been closed.
     * 
     * @return <code>true</code> if the object factory has been closed
     */
    @Override
    public boolean isClosed(
    ){
        return this.connection == null;
    }

    /**
     * 
     * @throws ServiceException
     */
    private void validateState(
    ) throws ServiceException{
        if(isClosed()) throw new ServiceException(
            BasicException.Code.DEFAULT_DOMAIN,
            BasicException.Code.ILLEGAL_STATE,
            "The manager is closed"
        ); 
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object)
     */
    @Override
    public Object getObjectById(Object oid) {
        return getObjectById(oid, true);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Object, boolean)
     */
    @Override
    public Object getObjectById(
        Object oid, 
        boolean validate
    ) {
        try {
            if(oid instanceof UUID) {
                return marshal(
                    this.connection.getObjectById(oid, validate)
                );
            } else if(oid instanceof Path) {
                Path path = (Path)oid; 
                boolean odd = path.size() % 2 == 1;
                Path objectId = odd ? path : path.getParent();
                DataObject_1_0 object =  (DataObject_1_0) marshal(
                    this.connection.getObjectById(objectId, validate)
                );
                return odd ? object : object.objGetContainer(path.getBase());
            }
        } catch(ServiceException exception) {
            throw exception.getExceptionCode() == BasicException.Code.NOT_FOUND ? new JDOObjectNotFoundException(
                "Object not found",
                exception
            ) : new JDOUserException(
                "Unable to get object",
                exception
            );
        }
        throw oid == null ? BasicException.initHolder(
            new JDOFatalUserException(
                "Null object id",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.BAD_PARAMETER,
                    new BasicException.Parameter("expected", Path.class.getName())
                )
            )
        ) : BasicException.initHolder(
            new JDOFatalUserException(
                "Unsupported object id class",
                BasicException.newEmbeddedExceptionStack(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.NOT_SUPPORTED,
                    new BasicException.Parameter("expected", Path.class.getName()),
                    new BasicException.Parameter("actual", oid.getClass().getName())
                )
            )
        );
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObjectManager_1_0#getOptimalFetchSize(int)
     */
    @Override
    public int getOptimalFetchSize() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.cci.DataObjectManager_1_0#getCacheThreshold()
     */
    @Override
    public int getCacheThreshold() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addInstanceLifecycleListener(
        InstanceLifecycleListener listener,
        Class... classes
    ) {
        if(classes == null || classes.length == 0) {
            this.instanceLifecycleListener.addInstanceLifecycleListener(listener);
        } else {
            throw new UnsupportedOperationException("The view manager expects the classes argument to be null");
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#checkConsistency()
     */
    @Override
    public void checkConsistency() {
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#currentTransaction()
     */
    @Override
    public Transaction currentTransaction(
    ) {
        return this.transaction;
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistent(java.lang.Object)
     */
    @Override
    public void deletePersistent(
        Object pc
    ) {
        try {
            ((ObjectView_1_0)pc).objDelete();
        }
        catch(Exception e) {
            throw new JDOUserException(
                "Unable to delete object",
                e,
                pc
            );
        }
    }
    
    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.lang.Object[])
     */
    @Override
    public void deletePersistentAll(Object... pcs) {
    	for(Object pc : pcs){
    		deletePersistent(pc);
    	}
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#deletePersistentAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void deletePersistentAll(Collection pcs) {
    	for(Object pc : pcs){
    		deletePersistent(pc);
    	}
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopy(java.lang.Object)
     */
    @Override
    public <T> T detachCopy(T pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.util.Collection)
     */
    @Override
    public <T> Collection<T> detachCopyAll(Collection<T> pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#detachCopyAll(java.lang.Object[])
     */
    @Override
    public <T> T[] detachCopyAll(T... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evict(java.lang.Object)
     */
    @Override
    public void evict(Object pc) {
    	getConnection().evict(toDataObject(pc));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.lang.Object[])
     */
    @Override
    public void evictAll(Object... pcs) {
    	getConnection().evictAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void evictAll(Collection pcs) {
    	getConnection().evictAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#flush()
     */
    @Override
    public void flush() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#evictAll(boolean, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void evictAll(boolean subclasses, Class pcClass) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getCopyOnAttach()
     */
    @Override
    public boolean getCopyOnAttach() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchGroup(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public FetchGroup getFetchGroup(Class type, String name) {
        return getConnection().getFetchGroup(type, name);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects()
     */
    @Override
    public Set<?> getManagedObjects() {
        return this.registry.values();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<?> getManagedObjects(EnumSet<ObjectState> states) {
        if(this.interactionSpec == null) {
            return new MarshallingSet(
                this,
                getConnection().getManagedObjects(states)
            );
        } else {
            throw new UnsupportedOperationException("The view manager does not support selection by state unless its interaction spec is null");
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set getManagedObjects(Class... classes) {
        throw new UnsupportedOperationException("Unsupported because all objects are instances of the ViewObject_1_0");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getManagedObjects(java.util.EnumSet, java.lang.Class[])
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set getManagedObjects(EnumSet<ObjectState> states, Class... classes) {
        throw new UnsupportedOperationException("Unsupported because all objects are instances of the ViewObject_1_0");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(boolean, java.lang.Object[])
     */
    @Override
    public Object[] getObjectsById(boolean validate, Object... oids) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getServerDate()
     */
    @Override
    public Date getServerDate() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(boolean, java.lang.Object[])
     */
    @Override
    public void makeTransientAll(boolean useFetchPlan, Object... pcs) {
    	for(Object pc : pcs){
    		makeTransient(pc, useFetchPlan);
    	}
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(boolean, java.lang.Object[])
     */
    @Override
    public void retrieveAll(boolean useFetchPlan, Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setCopyOnAttach(boolean)
     */
    @Override
    public void setCopyOnAttach(boolean flag) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDataStoreConnection()
     */
    @Override
    public JDOConnection getDataStoreConnection() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getDetachAllOnCommit()
     */
    @Override
    public boolean getDetachAllOnCommit() {
        return this.connection.getDetachAllOnCommit();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class)
     */
    @Override
    public <T> Extent<T> getExtent(Class<T> persistenceCapableClass) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getExtent(java.lang.Class, boolean)
     */
    @Override
    public <T> Extent<T> getExtent(
        Class<T> persistenceCapableClass,
        boolean subclasses
    ) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getFetchPlan()
     */
    @Override
    public FetchPlan getFetchPlan() {
        return this.connection.getFetchPlan();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getIgnoreCache()
     */
    @Override
    public boolean getIgnoreCache() {
        return connection.getIgnoreCache();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectById(java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> T getObjectById(Class<T> cls, Object key) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectId(java.lang.Object)
     */
    @Override
    public Object getObjectId(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectIdClass(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Class getObjectIdClass(Class cls) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection getObjectsById(Collection oids) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[])
     */
    @Override
    public Object[] getObjectsById(Object... oids) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection getObjectsById(Collection oids, boolean validate) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getObjectsById(java.lang.Object[], boolean)
     */
    @Override
    public Object[] getObjectsById(Object[] oids, boolean validate) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getPersistenceManagerFactory()
     */
    @Override
    public PersistenceManagerFactory getPersistenceManagerFactory(
    ) {
        if(this.interactionSpec == null) { 
            if(this.factory == null) {
                this.factory = new ViewManagerFactory_1(this.connection.getPersistenceManagerFactory());
            }
            return this.factory;
        } else {
            return getPersistenceManager(null).getPersistenceManagerFactory();
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getSequence(java.lang.String)
     */
    @Override
    public Sequence getSequence(String name) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getTransactionalObjectId(java.lang.Object)
     */
    @Override
    public Object getTransactionalObjectId(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject()
     */
    @Override
    public Object getUserObject() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#getUserObject(java.lang.Object)
     */
    @Override
    public Object getUserObject(Object key) {
        return this.connection.getUserObject(key);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactional(java.lang.Object)
     */
    @Override
    public void makeNontransactional(Object pc) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.lang.Object[])
     */
    @Override
    public void makeNontransactionalAll(Object... pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeNontransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void makeNontransactionalAll(Collection pcs) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistent(java.lang.Object)
     */
    @Override
    public <T> T makePersistent(T pc) {
        return this.connection.makePersistent(pc);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.lang.Object[])
     */
    @Override
    public <T> T[] makePersistentAll(T... pcs) {
        return this.connection.makePersistentAll(pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makePersistentAll(java.util.Collection)
     */
    @Override
    public <T> Collection<T> makePersistentAll(Collection<T> pcs) {
        return this.connection.makePersistentAll(pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactional(java.lang.Object)
     */
    @Override
    public void makeTransactional(Object pc) {
    	getConnection().makeTransactional(toDataObject(pc));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.lang.Object[])
     */
    @Override
    public void makeTransactionalAll(Object... pcs) {
    	getConnection().makeTransactionalAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransactionalAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void makeTransactionalAll(Collection pcs) {
    	getConnection().makeTransactionalAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object)
     */
    @Override
    public void makeTransient(Object pc) {
    	getConnection().makeTransient(toDataObject(pc));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransient(java.lang.Object, boolean)
     */
    @Override
    public void makeTransient(Object pc, boolean useFetchPlan) {
    	getConnection().makeTransient(toDataObject(pc),useFetchPlan);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[])
     */
    @Override
    public void makeTransientAll(Object... pcs) {
    	getConnection().makeTransientAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void makeTransientAll(Collection pcs) {
    	getConnection().makeTransientAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.lang.Object[], boolean)
     */
    @Override
    public void makeTransientAll(Object[] pcs, boolean useFetchPlan) {
    	getConnection().makeTransientAll(useFetchPlan, toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#makeTransientAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void makeTransientAll(Collection pcs, boolean useFetchPlan) {
    	getConnection().makeTransientAll(toDataObjects(pcs), useFetchPlan);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newInstance(java.lang.Class)
     */
    @Override
    public <T> T newInstance(Class<T> pcClass) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newNamedQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Query newNamedQuery(Class cls, String queryName) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newObjectIdInstance(java.lang.Class, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object newObjectIdInstance(Class pcClass, Object key) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery()
     */
    @Override
    public Query newQuery() {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Object)
     */
    @Override
    public Query newQuery(Object compiled) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String)
     */
    @Override
    public Query newQuery(String query) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Query newQuery(Class cls) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Query newQuery(Extent cln) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.String, java.lang.Object)
     */
    @Override
    public Query newQuery(String language, Object query) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Query newQuery(Class cls, Collection cln) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Query newQuery(Class cls, String filter) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(javax.jdo.Extent, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Query newQuery(Extent cln, String filter) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#newQuery(java.lang.Class, java.util.Collection, java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Query newQuery(Class cls, Collection cln, String filter) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#putUserObject(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object putUserObject(Object key, Object val) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refresh(java.lang.Object)
     */
    @Override
    public void refresh(
        Object pc
    ) {
        try {
            if(pc instanceof ObjectView_1_0) {
                ((ObjectView_1_0)pc).objRefresh();
            }
        }
        catch(ServiceException e) {
            throw new JDOUserException(
                "Unable to refresh object",
                e,
                pc
            );
        }
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll()
     */
    @Override
    public void refreshAll() {
        getConnection().refreshAll();
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.lang.Object[])
     */
    @Override
    public void refreshAll(Object... pcs) {
        getConnection().refreshAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void refreshAll(Collection pcs) {
        getConnection().refreshAll(toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#refreshAll(javax.jdo.JDOException)
     */
    @Override
    public void refreshAll(JDOException jdoe) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeInstanceLifecycleListener(javax.jdo.listener.InstanceLifecycleListener)
     */
    @Override
    public void removeInstanceLifecycleListener(
        InstanceLifecycleListener listener
    ) {
        this.instanceLifecycleListener.removeInstanceLifecycleListener(listener);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#removeUserObject(java.lang.Object)
     */
    @Override
    public Object removeUserObject(Object key) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object)
     */
    @Override
    public void retrieve(Object pc) {
        retrieve(pc, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieve(java.lang.Object, boolean)
     */
    @Override
    public void retrieve(Object pc, boolean useFetchPlan) {
        getConnection().retrieve(toDataObject(pc), useFetchPlan);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void retrieveAll(Collection pcs) {
        retrieveAll(pcs, false);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[])
     */
    @Override
    public void retrieveAll(Object... pcs) {
    	retrieveAll(false,pcs);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.util.Collection, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void retrieveAll(Collection pcs, boolean useFetchPlan) {
        getConnection().retrieveAll(toDataObjects(pcs), useFetchPlan);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#retrieveAll(java.lang.Object[], boolean)
     */
    @Override
    public void retrieveAll(Object[] pcs, boolean useFetchPlan) {
    	getConnection().retrieveAll(useFetchPlan, toDataObjects(pcs));
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setDetachAllOnCommit(boolean)
     */
    @Override
    public void setDetachAllOnCommit(boolean flag) {
        this.connection.setDetachAllOnCommit(flag);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setIgnoreCache(boolean)
     */
    @Override
    public void setIgnoreCache(boolean flag) {
        this.connection.setIgnoreCache(flag);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setMultithreaded(boolean)
     */
    @Override
    public void setMultithreaded(boolean flag) {
        this.connection.setMultithreaded(flag);
    }

    /* (non-Javadoc)
     * @see javax.jdo.PersistenceManager#setUserObject(java.lang.Object)
     */
    @Override
    public void setUserObject(Object o) {
        throw new UnsupportedOperationException("Unsupported operation by manager");
    }
    
    /**
     * Create a transient object
     * 
     * @param       objectClass
     *              The model class of the object to be created
     *
      * @return      an object
     */
    @Override
    public DataObject_1_0 newInstance(
        String objectClass, 
        UUID transientObjectId
    ) throws ServiceException{
        validateState();
        return (DataObject_1_0)marshal(
            this.connection.newInstance(objectClass, transientObjectId)
        );
    }

    /**
     * Tells whether the persistence manager represented by this connection is multithreaded or not
     * 
     * @return <code> true</code> if the the persistence manager is multithreaded 
     */
    @Override
    public boolean getMultithreaded(
    ) {
        return this.connection.getMultithreaded();
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.base.accessor.generic.cci.ObjectFactory_1_3#evict()
     */
    @Override
    public void evictAll(
    ) {
        this.connection.evictAll();
    }

    //------------------------------------------------------------------------
    // Extends CachingMarshaller
    //------------------------------------------------------------------------

    /**
     * Marshals an object
     *
     * @param  source    The object to be marshalled
     * 
     * @return           The marshalled object
     * 
     * @exception        ServiceException 
     *                   Object can't be marshalled
     */
    @Override
    public Object marshal (
        Object source
    ) throws ServiceException{
        validateState();
        if(source instanceof ObjectView_1_0){
            return source;
        } else if (source instanceof DataObject_1_0) {
            DataObject_1_0 dataObject = (DataObject_1_0) source;
            ObjectView_1 target = this.registry.get(dataObject);
            if(target == null) {
                ObjectView_1 concurrent = this.registry.putIfAbsent(
                    dataObject, 
                    target = new ObjectView_1(this, dataObject)
                );
                return concurrent == null ? target : concurrent;
            } else {
                return this.interactionSpec != null && !target.isHollow() && target.jdoIsDeleted() ? null : target;
            }
        } else {
            return source;
        }
    }

    /**
     * Unmarshals an object
     *
     * @param  source   The marshalled object
     * 
     * @return          The unmarshalled object
     * 
     * @exception       ServiceException
     *                  Object can't be unmarshalled
     */
    @Override
    public Object unmarshal (
        Object source
    ) throws ServiceException {
        return source instanceof ObjectView_1 ? ((ObjectView_1)source).objGetDelegate() : source;
    }
    
    /**
     * Retrieve an ObjectView_1's DataObject_1_0 delegate
     * 
     * @param pc the ObjectView_1 instance
     * 
     * @return its DataObject_1_0 delegate
     */
    private static Object toDataObject(
    	Object pc
    ){
        if(pc instanceof ObjectView_1_0) try {
    		return ((ObjectView_1_0)pc).objGetDelegate();
    	} catch (ServiceException exception) {
    		throw BasicException.initHolder(
	        	new JDOFatalUserException(
	        		"Unable to retrieve the persistence capable's DataObject_1_0 delegate",
	        		BasicException.newEmbeddedExceptionStack(exception)
	        	)
        	);    		
    	} else {
    		throw BasicException.initHolder(
	        	new JDOFatalUserException(
	        		"Unable to retrieve the persistence capable's DataObject_1_0 delegate",
	        		BasicException.newEmbeddedExceptionStack(
	            		BasicException.Code.DEFAULT_DOMAIN,
	            		BasicException.Code.BAD_PARAMETER,
	            		new BasicException.Parameter("class", pc == null ? null : pc.getClass().getName()),
                        new BasicException.Parameter("acceptable", ObjectView_1_0.class.getName())
	        		)
	        	)
        	);    		
    	}
    }
    
    private static Collection<Object> toDataObjects(Collection<?> pcs) {
    	Collection<Object> dos = new ArrayList<Object>();
    	for(Object pc : pcs) {
    		dos.add(toDataObject(pc));
    	}
    	return dos;
    }

    private static Object[] toDataObjects(Object[] pcs) {
    	Object[] dos = new Object[pcs.length];
    	int i = 0;
    	for(Object pc : pcs) {
    		dos[i++] = toDataObject(pc);
    	}
    	return dos;
    }
    

    // -----------------------------------------------------------------------
    // Class ObjectIdMarshaller
    // -----------------------------------------------------------------------

    /**
     * Object Id Marshaller
     */
    static class ObjectIdMarshaller implements Marshaller {

        /**
         * Constructor 
         */
        private ObjectIdMarshaller(
        ){
            // Avoid external instantiation
        }
            
        /**
         * The singleton
         */
        static final Marshaller INSTANCE = new ObjectIdMarshaller();

        /* (non-Javadoc)
         * @see org.openmdx.base.marshalling.Marshaller#marshal(java.lang.Object)
         */
        public Object marshal(
            Object source
        ) throws ServiceException {
            return JDOHelper.getObjectId(source);
        }

        /* (non-Javadoc)
         * @see org.openmdx.base.marshalling.Marshaller#unmarshal(java.lang.Object)
         */
        public Object unmarshal(
            Object source
        ) throws ServiceException {
            throw new ServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.NOT_SUPPORTED,
                "Object id collections are unmodifiable"
            );
        }
        
    }


}