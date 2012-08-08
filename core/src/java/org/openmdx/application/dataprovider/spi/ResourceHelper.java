/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: ResourceHelper.java,v 1.2 2011/11/26 01:34:57 hburger Exp $
 * Description: Abstract persistence layer
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2010-2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.spi;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;

/**
 * Resource Helper
 * 
 * @deprecated use {@link Facades}
 */
@Deprecated
public class ResourceHelper {

    /**
     * Constructor 
     */
    private ResourceHelper(
    ) {
        // Avoid instantiation
    }

    /**
     * Create an object facade for a given mapped record 
     * 
     * @param object
     * 
     * @return an object facade for the given record
     * 
     * @throws ServiceException
     * 
     * @deprecated use {@link Facades#asObject(MappedRecord)}
     */
    @Deprecated
    public static Object_2Facade getObjectFacade(
        MappedRecord object
    ) throws ServiceException{
    	return Facades.asObject(object);
    }
    
}