/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: LayerConfigurationEntries.java,v 1.4 2009/12/14 14:47:52 wfro Exp $
 * Description: TYPE layer configuration entries
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/12/14 14:47:52 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2009, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.type;


/**
 * The <code>TypeLayerConfigurationEntries</code> class contains
 * constants identifying the configuration entries of the
 * dataprovider's type layer.
 */
public class LayerConfigurationEntries extends org.openmdx.application.dataprovider.cci.SharedConfigurationEntries {

  protected LayerConfigurationEntries() {
   // Avoid instantiation
  }

  /**
   * GENERIC_TYPE_PATH allows to specify a list of paths. Objects at one
   * of these paths are generic. This means that no type checking occurs. 
   * The objects may be of any type and may have any attributes. The 
   * Application Layer is responsible for handling these objects. 
   */
  static public final String GENERIC_TYPE_PATH = "genericTypePath";

  /**
   * org::openmdx::state2::StateCapable::validTimeUnique value
   * <p><em>
   * Note:<br>
   * This value is not stored in the database but configured for the provider.
   * </em>
   */
  static public final String VALID_TIME_UNIQUE = "validTimeUnique";
  
  /**
   * org::openmdx::state2::StateCapable::transactionTimeUnique value
   * <p><em>
   * Note:<br>
   * This value is not stored in the database but configured for the provider.
   * </em>
   */
  static public final String TRANSACTION_TIME_UNIQUE = "transactionTimeUnique";

}