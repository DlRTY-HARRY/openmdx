/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Jmi1ClassInvocationHandler.java,v 1.10 2008/05/06 14:53:48 wfro Exp $
 * Description: Jmi1PackageInvocationHandler 
 * Revision:    $Revision: 1.10 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/05/06 14:53:48 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2007-2008, OMEX AG, Switzerland
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
package org.openmdx.base.accessor.jmi.spi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

import javax.jmi.reflect.JmiException;

import org.openmdx.base.accessor.jmi.cci.RefClass_1_0;
import org.openmdx.base.accessor.jmi.cci.RefPackage_1_0;

/**
 * Jmi1ClassInvocationHandler
 *
 */
public class Jmi1ClassInvocationHandler implements InvocationHandler {
    
    //-----------------------------------------------------------------------
    public static class RefClass_1Proxy extends RefClass_1 {
        
        public RefClass_1Proxy(
            String qualifiedClassName,
            RefPackage_1_0 immediatePackage        
        ) {
            super(
                immediatePackage
            );
            this.qualifiedClassName = qualifiedClassName;
        }
        
        private static final long serialVersionUID = -1410998279197958164L;
        private final String qualifiedClassName;

        public String refMofId(
        ) throws JmiException {
            return this.qualifiedClassName;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            return 
                obj instanceof RefClass_1Proxy && 
                this.qualifiedClassName.equals(
                    ((RefClass_1Proxy)obj).qualifiedClassName
                );
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return this.qualifiedClassName.hashCode();
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "RefClass " + this.qualifiedClassName;
        }
    
    }
    
    //-----------------------------------------------------------------------
    public Jmi1ClassInvocationHandler(
        String qualifiedClassName,
        RefPackage_1_0 immediatePackage
    ) {
        this.delegation = new RefClass_1Proxy(
            qualifiedClassName,
            immediatePackage
        );
    }

    //-----------------------------------------------------------------------
    /* (non-Javadoc)
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
     */
    public Object invoke(
        Object proxy, 
        Method method, 
        Object[] args
    ) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        String methodName = method.getName().intern();
        if(Object.class == declaringClass) {
            //
            // Object methods
            //
            if("toString" == methodName) {
                return proxy.getClass().getName() + " delegating to " + this.delegation;
            } else if ("hashCode" == methodName) {
                return this.delegation.hashCode();
            } else if ("equals" == methodName) {
                if(Proxy.isProxyClass(args[0].getClass())) {
                    InvocationHandler invocationHandler = Proxy.getInvocationHandler(args[0]);
                    if(invocationHandler instanceof Jmi1ClassInvocationHandler) {
                        return this.delegation.equals(
                            ((Jmi1ClassInvocationHandler)invocationHandler).delegation
                        );
                    }
                }
                return false;
            }
        } else if("refCreateInstance" == methodName && args.length == 1) {
            return this.delegation.refCreateInstance(
                (List<?>)args[0], // arguments
                (RefClass_1_0)proxy
            );
        } else if(
            declaringClass == Jmi1Class.class || (    
                methodName.startsWith("ref") && 
                methodName.length() > 3 &&
                Character.isUpperCase(method.getName().charAt(3))
            )
        ) {
            //
            // RefObject API
            //
            try {
                return method.invoke(
                    this.delegation, 
                    args
                );
            } catch(InvocationTargetException e) {
                throw e.getTargetException();
            }
        } else if(
            args == null || 
            args.length == 0 ||
            methodName.startsWith("create")
        ) {
            //
            // Creators
            //
            return this.delegation.refCreateInstance(
                null, // arguments
                (RefClass_1_0)proxy
            );            
        }        
        throw new UnsupportedOperationException(method.getName());
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    protected final RefClass_1 delegation;
    
}
