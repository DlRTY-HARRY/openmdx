/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: NonConfigurableProperty.java,v 1.4 2009/10/12 10:34:10 hburger Exp $
 * Description: JDO 2.0 Non-Configurable Properties
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/12 10:34:10 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2005-2008, OMEX AG, Switzerland
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
package org.openmdx.kernel.persistence.cci;

import javax.jdo.Constants;



/**
 * JDO 2.0 Non-Configurable Properties
 */
public enum NonConfigurableProperty {

    //------------------------------------------------------------------------
    // JDO Standard Properties
    //------------------------------------------------------------------------

    VendorName(Constants.NONCONFIGURABLE_PROPERTY_VENDOR_NAME),
    VersionNumber(Constants.NONCONFIGURABLE_PROPERTY_VERSION_NUMBER);
    //------------------------------------------------------------------------
    // Vendor Specific Properties
    //------------------------------------------------------------------------

    // none yet


    /**
     * Constructor
     * 
     * @param qualifiedName the property's qualifiedName
     */
    private NonConfigurableProperty(
        String qualifiedName
    ){
        this.qualifiedName = qualifiedName;
    }

    /**
     * The property name
     */
    private final String qualifiedName;

    /**
     * Retrieve the property's qualified name
     * 
     * @return the qualified property name
     */
    public final String qualifiedName(
    ){
        return this.qualifiedName;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return qualifiedName();
    }

}