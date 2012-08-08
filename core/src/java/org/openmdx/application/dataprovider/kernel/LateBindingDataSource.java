/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: LateBindingDataSource.java,v 1.4 2010/01/09 00:09:14 wfro Exp $
 * Description: Late Binding Data Source
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/09 00:09:14 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 
 * * Neither the name of the openMDX team nor the names of its
 * contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission.
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
 * This product includes software developed by the Apache Software
 * Foundation (http://www.apache.org/).
 */
package org.openmdx.application.dataprovider.kernel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.log.SysLog;

/**
 * Late Binding <code>DataSource</code>
 */
class LateBindingDataSource implements DataSource {

    /**
     * Creates a new connection manager. 
     * Using a pool of connections to the database.
     *
     * @param jndiName JDBC Connection Pool name
     */
    LateBindingDataSource(
        String jndiName
    ){
        this.pool = null; 
        this.jndiName = jndiName;
    }

    /**
     * The <code>DataSource</code> is retrieved lazily
     */
    private DataSource pool;
    
    /**
     * The JNDI name refers usually to an entry in the <code>java:comp/env/jdbc</code> context 
     */
    private final String jndiName;
    
    //-----------------------------------------------------------------------
    /**
     * Get a database connection pool
     * 
     * @return the DataSource corresponding to the JNDI name
     * 
     * @throws ServiceException
     */
    private synchronized DataSource getPool(
    ) throws SQLException {
        if (this.pool == null) try {
            SysLog.detail(
                "Acquire Connection Pool",
                this.jndiName
            );
            this.pool = (DataSource)new InitialContext().lookup(this.jndiName);
        } catch(Exception exception) {
            throw Throwables.initCause(
                new SQLException("Could not lookup connection pool"),
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE,
                new BasicException.Parameter("jndiName", this.jndiName)
            );
        }
        return this.pool;	    
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a connection. 
     * <p>
     * The connection must be closed when it is not needed any more, so it is 
     * available again. Otherwise the connection pool runs out of available 
     * connections!
     */
    public java.sql.Connection getConnection(
    ) throws SQLException {	
        try {
            Connection conn = getPool().getConnection();    
            if(conn != null) {
            	return conn;
            }
        } catch(Exception ex) {
            throw Throwables.initCause(
            	new SQLException("Connection acquisition failed"),
            	ex,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.GENERIC,
                new BasicException.Parameter("jndiName", this.jndiName)
            ); 
        }    
        throw new SQLException("No connection available from '" + this.jndiName + "'");
    }

    //-----------------------------------------------------------------------
    public Connection getConnection(
        String username, 
        String password
    ) throws SQLException {
        return this.getPool().getConnection(username, password);
    }
    
    //-----------------------------------------------------------------------
    public int getLoginTimeout(
    ) throws SQLException {
        return this.getPool().getLoginTimeout();
    }
    
    //-----------------------------------------------------------------------
    public PrintWriter getLogWriter(
    ) throws SQLException {
        return this.getPool().getLogWriter();
    }
    
    //-----------------------------------------------------------------------
    public void setLoginTimeout(
        int seconds
    ) throws SQLException {
        this.getPool().setLoginTimeout(seconds);
    }
    
    //-----------------------------------------------------------------------
    public void setLogWriter(
        PrintWriter out
    ) throws SQLException {
        this.getPool().setLogWriter(out);
    }

    //-----------------------------------------------------------------------
    // Since JRE 6
    //-----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
     */
    public boolean isWrapperFor(Class<?> iface)
        throws SQLException {
        return this.getPool().isWrapperFor(iface);
    }

    /* (non-Javadoc)
     * @see java.sql.Wrapper#unwrap(java.lang.Class)
     */
    public <T> T unwrap(Class<T> arg0)
        throws SQLException {
        return this.getPool().unwrap(arg0);
    }

    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    
}