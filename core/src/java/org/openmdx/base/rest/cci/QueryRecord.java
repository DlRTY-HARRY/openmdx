/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: QueryRecord.java,v 1.1 2010/03/19 12:32:54 hburger Exp $
 * Description: Query Record
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/19 12:32:54 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009-2010, OMEX AG, Switzerland
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
package org.openmdx.base.rest.cci;

import java.util.Set;

import javax.resource.cci.MappedRecord;
import javax.resource.cci.Record;

import org.openmdx.base.naming.Path;

/**
 * <code>org::openmdx::kernel::Query</code>
 */
public interface QueryRecord extends MappedRecord {
    
    /**
     * An object record's name
     */
    String NAME = "org:openmdx:kernel:Query";
    
    /**
     * Retrieve group.
     *
     * @return Returns the group.
     */
    Set<String> getGroup();
    
    /**
     * Set group.
     * 
     * @param group The group to set.
     */
    void setGroup(Set<String> group);

    
    /**
     * Retrieve groups.
     *
     * @return Returns the groups.
     */
    Set<String> getGroups();
    
    /**
     * Set groups.
     * 
     * @param groups The groups to set.
     */
    void setGroups(Set<String> groups);
    
    /**
     * Retrieve path.
     *
     * @return Returns the path.
     */
    Path getPath();
    
    /**
     * Set path.
     * 
     * @param path The path to set.
     */
    void setPath(Path path);
    
    /**
     * Retrieve position.
     *
     * @return Returns the position.
     */
    Long getPosition();
    
    /**
     * Set position.
     * 
     * @param position The position to set.
     */
    void setPosition(Long position);
    
    /**
     * Retrieve query.
     *
     * @return Returns the query.
     */
    String getQuery();
    
    /**
     * Set query.
     * 
     * @param query The query to set.
     */
    void setQuery(String query);
    
    /**
     * Retrieve queryType.
     *
     * @return Returns the queryType.
     */
    String getQueryType();

    /**
     * Set queryType.
     * 
     * @param queryType The queryType to set.
     */
    void setQueryType(String queryType);
    
    /**
     * Retrieve parameters.
     *
     * @return Returns the parameters.
     */
    Record getParameters();
    
    /**
     * Set parameters.
     * 
     * @param parameters The parameters to set.
     */
    void setParameters(Record parameters);
    
    /**
     * Retrieve size.
     *
     * @return Returns the size.
     */
    Long getSize();
    
    /**
     * Set size.
     * 
     * @param size The size to set.
     */
    void setSize(Long size);

}
