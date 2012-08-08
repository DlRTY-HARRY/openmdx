/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ControlObjects_2.java,v 1.2 2010/01/08 18:49:40 hburger Exp $
 * Description: ControlObjects_2 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/01/08 18:49:40 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
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

package org.openmdx.base.accessor.rest.spi;

import org.openmdx.base.naming.Path;


/**
 * ControlObjects_2
 *
 */
public class ControlObjects_2 {

    /**
     * Constructor 
     */
    private ControlObjects_2() {
        // Avoid instantiation
    }

    /**
     * Virtual Transaction Object Id Pattern
     */
    private static final Path TRANSACTION_OBJECT_PATTERN = new Path(
        "xri://@openmdx*org.openmdx.kernel/transaction/($..)"
    );

    /**
     * Virtual Transaction Object Id Pattern
     */
    private static final Path TRANSACTION_COMMIT_PATTERN = new Path(
        "xri://@openmdx*org.openmdx.kernel/transaction/($..)/commit/($..)"
    );

    /**
     * Virtual Connection Object Id Pattern
     */
    protected static final Path CONNECTION_OBJECT_PATTERN = new Path(
        "xri://@openmdx*org.openmdx.kernel/connection/($..)"
    );
    
    /**
     * Tests whether the candidate is a control object 
     * 
     * @param recordName the candidate's type
     * 
     * @return <code>true</code> if the candidate is a control object 
     */
    public static boolean isControlObjectType(
        String recordName
    ){
        return recordName.startsWith("org:openmdx:kernel:");
    }
    
    /**
     * Tests whether the candidate matches 
     * 
     * @param candidate
     * @param pattern
     * 
     * @return <code>true</code> in case of a match
     */
    private static boolean matches(
        Path candidate,
        Path pattern
    ){
        return candidate.size() % 2 == 1 ? 
            candidate.isLike(pattern) :
            candidate.isLike(pattern.getParent());
    }

    /**
     * Tells whether the XRI represents a transaction object
     * 
     * @param candidate
     * 
     * @return <code>true</code> if the XRI represents a transaction object
     */
    public static boolean isTransactionObjectIdentifier(
        Path candidate
    ){
        return matches(candidate, TRANSACTION_OBJECT_PATTERN);
    }
     
    /**
     * Tells whether the XRI represents a transaction commit operation
     * 
     * @param candidate
     * 
     * @return <code>true</code> if the XRI represents a transaction commit operation
     */
    public static boolean isTransactionCommitIdentifier(
        Path candidate
    ){
        return matches(candidate, TRANSACTION_COMMIT_PATTERN);
    }

    /**
     * Tells whether the XRI represents a connection object
     * 
     * @param candidate
     * 
     * @return <code>true</code> if the XRI represents a connection object
     */
    public static boolean isConnectionObjectIdentifier(
        Path candidate
    ){
        return matches(candidate, CONNECTION_OBJECT_PATTERN);
    }
     
}
