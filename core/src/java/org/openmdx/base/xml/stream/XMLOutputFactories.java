/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: XMLOutputFactories.java,v 1.5 2010/03/19 12:32:54 hburger Exp $
 * Description: XML Output Factory Builder
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/19 12:32:54 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010, OMEX AG, Switzerland
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
package org.openmdx.base.xml.stream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.stream.XMLOutputFactory;

import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.loading.Classes;
import org.openmdx.kernel.log.SysLog;

/**
 * XML Output Factory Builder
 */
public class XMLOutputFactories {

    /**
     * Constructor 
     */
    private XMLOutputFactories() {
        // Avoid instantiation
    }

    /**
     * The MIME type property
     */
    public static final String MIME_TYPE = "org.openmdx.xml.stream.mimeType";
    
    /**
     * The configuration
     */
    private static final Properties configuration = new Properties();
    
    /**
     * The lazily fetched classes
     */
    private static final ConcurrentMap<String,Class<? extends XMLOutputFactory>> classes = new ConcurrentHashMap<String,Class<? extends XMLOutputFactory>>();
    
    /**
     * Tells whether the given content type is supported
     * 
     * @return <code>true</code> if given content type is supported
     */
    public static boolean isSupported(
        String mimeType
    ){
        return classes.containsKey(mimeType);
    }
        
    /**
     * Create a an XML Output Factory instance
     * 
     * @param mimeType
     * 
     * @return a new XML Output Factory
     */
    public static XMLOutputFactory newInstance(
        String mimeType
    ){
        Class<? extends XMLOutputFactory> factoryClass = classes.get(mimeType);
        if(factoryClass == null) {
            String factoryName = configuration.getProperty(mimeType);
            if(factoryName == null) {
                throw BasicException.initHolder(
                    new IllegalArgumentException(
                        "No XMLOutputFactory configured for the given MIME type",
                        BasicException.newEmbeddedExceptionStack(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("mime-type", mimeType)
                        )
                   )
               );
            }
            try {
                classes.put(
                    mimeType,
                    factoryClass = Classes.getApplicationClass(factoryName)
                );
            } catch (ClassNotFoundException exception) {
                throw BasicException.initHolder(
                    new IllegalArgumentException(
                        "XMLOutputFactory class for the given MIME type not found",
                        BasicException.newEmbeddedExceptionStack(
                            exception,
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.BAD_PARAMETER,
                            new BasicException.Parameter("mime-type", mimeType),
                            new BasicException.Parameter("class", factoryName)
                        )
                   )
               );
            }
        }
        try {
            XMLOutputFactory factory = factoryClass.newInstance();
            if(factory.isPropertySupported(MIME_TYPE)) {
                factory.setProperty(MIME_TYPE, mimeType);
            }
            return factory;
        } catch (Exception exception) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "XMLOutputFactory class for the given MIME type could not be instantiated",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("mime-type", mimeType),
                        new BasicException.Parameter("class", factoryClass.getName())
                    )
               )
           );
        }
    }

    static {
        ClassLoader classLoader = XMLOutputFactories.class.getClassLoader();
        try {
            for(
                Enumeration<URL> urls = classLoader.getResources("META-INF/openmdx-xml-outputfactory.properties");
                urls.hasMoreElements();
            ) {
                URL url = urls.nextElement();
                try {
                    InputStream source = url.openStream();       
                    configuration.load(source);
                    source.close();
                } catch (IOException exception) {
                    SysLog.warning("XML output factory configuration failure: " + url, exception);
                }
            }
        } catch (IOException exception) {
            SysLog.error("XML output factory configuration failure", exception);
        }
    }

}