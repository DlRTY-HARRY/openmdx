/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LocalHomeHandler.java,v 1.5 2010/04/16 12:36:39 hburger Exp $
 * Description: Local Home Handler
 * Revision:    $Revision: 1.5 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/16 12:36:39 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
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
package org.openmdx.kernel.application.container.spi.ejb;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;

import org.openmdx.kernel.loading.Classes;

/**
 * Local Home Invocation Handler
 * <p>
 * This method handles stateless session beans only at the moment.
 */
public class LocalHomeHandler<H extends EJBLocalHome>
    extends AbstractHomeHandler
{

    /**
     * Constructor
     * 
     * @param ejbLocalObjectClass
     * 
     * @throws ClassNotFoundException
     */
    public LocalHomeHandler (
        Class<H> ejbLocalHomeClass,
        Class<EJBLocalObject> ejbLocalObjectClass
    ){
        this.ejbLocalObjectClass = ejbLocalObjectClass;
        this.ejbLocalHome = Classes.<H>newProxyInstance (
            this,
            EJBLocalHome.class, ejbLocalHomeClass
        );
        this.localObjectHandler = new LocalObjectHandler<H>(this);
    }

    /**
     * 
     */
    private final H ejbLocalHome;
    
    /**
     * 
     */
    private final InvocationHandler localObjectHandler;
    
    /**
     * 
     */
    private final Class<EJBLocalObject> ejbLocalObjectClass;

    
    //------------------------------------------------------------------------
    // Implements HomeConfiguration
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.HomeConfiguration#getHome()
     */
    public EJBHome getHome() {
        return null;
    }


    /* (non-Javadoc)
     * @see org.openmdx.kernel.application.container.spi.ejb.HomeConfiguration#getLocalHome()
     */
    public H getLocalHome() {
        return this.ejbLocalHome;
    }

    
    //------------------------------------------------------------------------
    // Extends AbstractInvocationHandler
    //------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] arguments
    ) throws Throwable {
        if (
            "create".equals(method.getName()) &&
            method.getParameterTypes().length == 0
        ) {
            //
            // EJBLocalHome.create()
            //
            return Classes.newProxyInstance(
                this.localObjectHandler,
                EJBLocalObject.class, this.ejbLocalObjectClass
            ); 
        } else {
            return super.invoke(proxy, method, arguments);
        }
    }

}
