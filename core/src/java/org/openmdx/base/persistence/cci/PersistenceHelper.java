/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: PersistenceHelper.java,v 1.18 2010/03/23 15:34:26 hburger Exp $
 * Description: PersistenceHelper 
 * Revision:    $Revision: 1.18 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/23 15:34:26 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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
package org.openmdx.base.persistence.cci;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.Query;
import javax.jmi.reflect.RefObject;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.accessor.jmi.cci.RefQuery_1_0;
import org.openmdx.base.accessor.spi.PersistenceManager_1_0;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.persistence.spi.Container;
import org.openmdx.base.persistence.spi.ExtentCollection;
import org.openmdx.base.persistence.spi.TransientContainerId;
import org.openmdx.base.query.FilterOperators;
import org.openmdx.base.query.Quantifier;
import org.openmdx.kernel.exception.BasicException;
import org.w3c.cci2.AnyTypePredicate;

/**
 * PersistenceHelper
 */
public class PersistenceHelper {

    /**
     * Constructor 
     */
    private PersistenceHelper() {
        // Avoid instantiation
    }

    /**
     * Return a clone of the object
     * 
     * @param object
     * 
     * @return a clone
     */
    @SuppressWarnings("unchecked")
    public static <T> T clone(
        T object
    ) {
        if(object instanceof org.openmdx.base.persistence.spi.Cloneable) {
            return ((org.openmdx.base.persistence.spi.Cloneable<T>)object).openmdxjdoClone();
        }
        if(object instanceof java.lang.Cloneable) try {
            return (T) object.getClass(
            ).getMethod(
                "clone"
            ).invoke(
                object
            );
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RuntimeServiceException(
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                "A class declared as Cloneable can't be cloned",
                new BasicException.Parameter("interface", java.lang.Cloneable.class.getName()),
                new BasicException.Parameter("class", object.getClass().getName())

            );
        }
        return null;
    }

    /**
     * Tells whether a container is persistent
     * 
     * @param object a container
     * 
     * @return <code>true</code> if the container is persistent
     */
    public static boolean isPersistent(
        Object object
    ){
        return object instanceof Container ? 
            ((Container)object).openmdxjdoIsPersistent() :
            JDOHelper.isPersistent(object);
    }
        
    /**
     * Return the container's id
     * 
     * @param container a container
     * 
     * @return the container id, or <code>null</code> if the container's owner is <em>transient</em>.
     */
    public static Path getContainerId(
        Object container
    ){
        return container instanceof Container ? ((Container)container).openmdxjdoGetContainerId() : null;
    }

    /**
     * Return a container's transient id
     * 
     * @param container a container
     * 
     * @return the transient container id, or <code>null</code> if the container's owner is <em>persistent</em>.
     */
    public static TransientContainerId getTransientContainerId(
        Object container
    ){
        return container instanceof Container ? ((Container)container).openmdxjdoGetTransientContainerId() : null;
    }
    
    /**
     * A way to avoid fetching an object just to retrieve its object id
     * 
     * @param pc a persistence capable object
     * @param featureName
     * 
     * @return the value where each object is replaced by its id
     */
    public static Object getFeatureReplacingObjectById(
        Object pc,
        String featureName
    ){  
        PersistenceManager_1_0 pm = (PersistenceManager_1_0) JDOHelper.getPersistenceManager(pc);
        return pm.getFeatureReplacingObjectById(
            (UUID) JDOHelper.getTransactionalObjectId(pc), 
            featureName
        );
    }

    /**
     * Retrieve a candidate collection
     * 
     * @param extent the extent
     * @param pattern the object id pattern either as a Path or XRI string representation
     * 
     * @return the candidate collection
     */
    public static Query newQuery(
        Extent<?> extent,
        Object xriPattern
    ){
        Query query = extent.getPersistenceManager().newQuery(extent);
        query.setCandidates(getCandidates(extent, xriPattern));
        return query;
    }

    /**
     * Retrieve a candidate collection
     * 
     * @param extent the extent
     * @param xriPattern the object id pattern either as a Path or XRI string representation
     * 
     * @return the candidate collection
     */
    public static <E> Collection<E> getCandidates(
        Extent<E> extent,
        Object xriPattern
    ){
        return new ExtentCollection<E>(
            extent, 
            xriPattern instanceof Path ? (Path)xriPattern : new Path((String)xriPattern)
        );
    }

    public static void setClasses(
        AnyTypePredicate query,
        Class<? extends RefObject>... classes 
    ){
        ((RefQuery_1_0)query).refAddValue(
            SystemAttributes.OBJECT_INSTANCE_OF,
            Quantifier.THERE_EXISTS.code(),
            FilterOperators.IS_IN,
            Arrays.asList(classes)
        );
    }
    
}
