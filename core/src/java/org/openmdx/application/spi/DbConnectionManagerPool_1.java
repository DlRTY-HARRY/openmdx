/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: DbConnectionManagerPool_1.java,v 1.1 2009/01/05 13:44:50 wfro Exp $
 * Description: Pooling DB connection manager
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:50 $
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
package org.openmdx.application.spi;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.openmdx.application.cci.DbConnectionManager_1_0;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;

public class DbConnectionManagerPool_1 
    implements DbConnectionManager_1_0 {

    //-----------------------------------------------------------------------
    /**
     * Creates a new connection manager. 
     * Using a pool of connections to the database.
     *
     * @param poolName JDBC Connection Pool name
     *
     * @param exDomain the exception domain name
     */
    public DbConnectionManagerPool_1(
        String jndiName
    ) throws ServiceException {
        this.pool = null; 
        this.jndiName = jndiName;
    }

    //-----------------------------------------------------------------------
    /**
     * Get a data source
     * 
     * @return the DataSource corresponding to the JNDI name
     * @throws ServiceException
     * 
     * @throws ServiceException
     */
    protected DataSource getDataSource(
    ) throws SQLException {
        try {
            return (DataSource)new InitialContext().lookup(this.jndiName);
        } 
        catch(Exception e) {
            new ServiceException(e).log();
            throw new SQLException("Could not lookup connection pool '" + this.jndiName + "'");
        }
    }

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
        if (this.pool == null){
            SysLog.detail(
                "Acquire Connection Pool",
                this.jndiName
            );
            this.pool = this.getDataSource();
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
            if(conn == null) {
                throw new SQLException("No connection available from '" + this.jndiName + "'");
            }
            return conn;
        } 
        catch(Exception ex) {
            new ServiceException(ex).log();
            throw new SQLException("Failure when getting a connection from '" + this.jndiName + "'"); 
        }    
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
    public void activate(
    ) throws java.lang.Exception {
    }

    //-----------------------------------------------------------------------
    /**
     * Closes the connection manager. 
     *
     * Does not close any open connections that have been obtained from the manager.
     */
    public void deactivate(
    ) throws java.lang.Exception {
        this.pool = null;
        SysLog.detail(
            "Connection pool discarded",
            this.jndiName
        );
    }

    //-----------------------------------------------------------------------
    /**
     * Get the connection pool's JNDI name
     * 
     * @return the connection pool's JNDI name
     */
    protected final String getName(
    ){
        return this.jndiName;
    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private DataSource pool;
    private final String jndiName;

}
