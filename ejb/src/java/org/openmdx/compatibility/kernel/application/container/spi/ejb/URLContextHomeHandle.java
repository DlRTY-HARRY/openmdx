/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: URLContextHomeHandle.java,v 1.1 2009/01/12 12:49:24 wfro Exp $
 * Description: URL Context based HomeHandle
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/12 12:49:24 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.compatibility.kernel.application.container.spi.ejb;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.openmdx.kernel.naming.initial.ContextFactory;

/**
 * URL Context based HomeHandle
 */
class URLContextHomeHandle implements HomeHandle {

	/**
	 * Constructor
	 * 
	 * @param jndiName
	 * @param providerURL, may be null
	 */
    URLContextHomeHandle(
        String jndiName,
		String providerURL
    ){
        this.jndiName = jndiName;
        this.environment.put(Context.INITIAL_CONTEXT_FACTORY, ContextFactory.class.getName());
        if(providerURL != null) this.environment.put(Context.PROVIDER_URL, providerURL);
    }

    /**
     * The JNDI Name
     */
    private final String jndiName;
    
    /**
     * The Provider URL
     */
    private final Properties environment = new Properties();
    
    
    //------------------------------------------------------------------------
    // Implements Serializable
    //------------------------------------------------------------------------

    /**
     * 
     */
    static final long serialVersionUID = 6785058433668886616L;
    
    
    //------------------------------------------------------------------------
    // Implements HomeHandle
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see javax.ejb.HomeHandle#getEJBHome()
     */
    public EJBHome getEJBHome(
    ) throws RemoteException {
        try {
            Context initialContext = new InitialContext(this.environment);
            try {
                return (EJBHome) initialContext.lookup(this.jndiName);
            } finally {
                initialContext.close();
            }
        } catch (NamingException exception) {
            throw new RemoteException(
                "EJB Home lookup failed: " + jndiName,
                exception
            );
        }
    }
    
    
    //------------------------------------------------------------------------
    // Extends Object
    //------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
    	if(!(object instanceof URLContextHomeHandle)) return false;
    	URLContextHomeHandle that = (URLContextHomeHandle) object;
    	return 
			this.jndiName.equals(that.jndiName) && 
			this.environment.equals(that.environment);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.jndiName.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getClass().getName() + ": " + this.jndiName;
    }

}