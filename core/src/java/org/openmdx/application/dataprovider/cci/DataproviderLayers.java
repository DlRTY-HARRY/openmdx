/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: DataproviderLayers.java,v 1.1 2009/01/05 13:44:50 wfro Exp $
 * Description: Generated constants for DataproviderLayers
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/05 13:44:50 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.cci;


/**
 * The <code>DataproviderLayers</code> class contains constants 
 * identifying the dataprovider layers.
 */




public class DataproviderLayers {

  
  protected DataproviderLayers() {
      // Avoid instantiation
  }


  /**
   * The INTERCEPTION layer id
   */
  static public final short INTERCEPTION = 4;



  /**
   * The TYPE layer id
   */
  static public final short TYPE = 3;



  /**
   * The APPLICATION layer id
   */
  static public final short APPLICATION = 2;



  /**
   * The MODEL layer id
   */
  static public final short MODEL = 1;



  /**
   * The PERSISTENCE layer id
   */
  static public final short PERSISTENCE = 0;



  /**
   * Returns the smallest defined integer constant or
   * Integer.MAX_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int min()
  {
    return PERSISTENCE;
  }



  /**
   * Returns the biggest defined integer constant or
   * Integer.MIN_VALUE if no integer constant is defined.
   *
   * @return an int
   */
  static public int max()
  {
    return INTERCEPTION;
  }



  /**
   * Returns a string representation of the passed code
   *
   * @param code  a code to be stringified
   * @return a stringified code
   */
  static public String toString(int code)
  {
    switch(code) {
      case INTERCEPTION: return "INTERCEPTION";
      case TYPE: return "TYPE";
      case APPLICATION: return "APPLICATION";
      case MODEL: return "MODEL";
      case PERSISTENCE: return "PERSISTENCE";
      default:
        return String.valueOf(code);
    }
  }



  /**
   * Returns the code of the passed code's string representation.
   * The string representation is case insensitive.
   *
   * @exception  throws an <code>IllegalArgumentException</code> 
   *             if the stringified code cannot be resolved
   * @param code a stringified code
   * @return a code
   */
  static public int fromString(String code)
  {  
    if (code.equalsIgnoreCase("INTERCEPTION")) return INTERCEPTION;
    if (code.equalsIgnoreCase("TYPE")) return TYPE;
    if (code.equalsIgnoreCase("APPLICATION")) return APPLICATION;
    if (code.equalsIgnoreCase("MODEL")) return MODEL;
    if (code.equalsIgnoreCase("PERSISTENCE")) return PERSISTENCE;

    // Not found
    throw new IllegalArgumentException(
          "The code '" + code + "' is unkown to the class DataproviderLayers");
  }



}

// end-of-file