/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: DatatypeFactories.java,v 1.3 2009/10/19 12:40:35 hburger Exp $
 * Description: Datatype Factories 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/10/19 12:40:35 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008-2009, OMEX AG, Switzerland
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
package org.w3c.spi;


import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * DatatypeFactories
 */
public class DatatypeFactories {

    /**
     * Constructor 
     */
    private DatatypeFactories() {
        // Avoid instantiation
    }

    /**
     * The XML Datatype Factory is lazily initialized
     */
    private static DatatypeFactory xmlFactory;

    /**
     * The XML Datatype Factory is lazily initialized
     */
    private static ImmutableDatatypeFactory immutableFactory;
    
    /**
     * Retrieve an XML Datatype Factory
     * 
     * @return an XML Datatype Factory instance
     */
    public static DatatypeFactory xmlDatatypeFactory(
    ){
        if(xmlFactory == null) try {
            xmlFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException exception) {
            throw new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.INVALID_CONFIGURATION,
                "Datatype factory acquisition failure"
            ).log();
        }
        return xmlFactory;
    }

    /**
     * Retrieve an Immutable Datatype Factory
     * 
     * @return an Immutable Datatype Factory instance
     */
    public static ImmutableDatatypeFactory immutableDatatypeFactory(
    ){  
        if(immutableFactory == null) {
            immutableFactory = new AlternativeDatatypeFactory();
        }
        return immutableFactory;
    }

}
