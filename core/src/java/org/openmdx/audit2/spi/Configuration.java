/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Configuration.java,v 1.1 2009/11/27 19:13:19 hburger Exp $
 * Description: Configuration 
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/11/27 19:13:19 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.audit2.spi;

import java.util.Map;

import org.openmdx.base.naming.Path;


/**
 * Configuration
 */
public interface Configuration {

    /**
     * Retrieve the data to audit path mapping
     * 
     * @return the data to audit path mapping
     */
    Map<Path,Path>  getMapping();

    /**
     * Retrieve the audit segment's object id.
     * 
     * @return the audit segment's object id
     */
    Path getAuditSegmentId();

    /**
     * Tells whether audit1 persistence is used.
     * <p>
     * <em>
     * <ul>
     * <li>If <code>true</code>, then<ul>
     * <li>newly created objects are not considered being involved in the 
     * current unit of work
     * </ul>
     * <li>
     * <li>If <code>false</code>, then<ul>
     * <li>newly created objects are considered being involved in the current 
     * unit of work
     * </ul>
     * <li>
     * </ul>
     * </em>
     * @return return <code>true</code> if audit is persistence is used.
     */
    boolean isAudit1Persistence();
    
    /**
     * Tells whether the modified feature set is persistent or re-calculated
     * 
     * @return <code>true</code> if the modified feature set is persistent
     */
    boolean isModifiedFeaturePersistent();
    
}