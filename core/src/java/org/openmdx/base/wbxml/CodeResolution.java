/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: CodeResolution.java,v 1.2 2010/03/02 10:01:05 hburger Exp $
 * Description: Code Resolution 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/03/02 10:01:05 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
package org.openmdx.base.wbxml;


/**
 * Code Resolution
 */
public class CodeResolution {

    /**
     * Tag Constructor 
     *
     * @param namespaceURI
     * @param localName
     * @param name
     */
    public CodeResolution(
        String namespaceURI, 
        String localName,
        String name
    ){
        this.namespaceURI = namespaceURI == null ? "" : namespaceURI;
        this.localName = localName == null ? "" : localName;
        this.name = name == null ?  "" : name;
        this.valueStart = null;
    }

    /**
     * Attribute Constructor 
     *
     * @param namespaceURI
     * @param elementName <code>null</code> or <code>""</code> for attributes in the namespace's global scope
     * @param name
     * @param valueStart
     */
    public CodeResolution(
        String namespaceURI, 
        String elementName,
        String name,
        String valueStart
    ){
        this.namespaceURI = namespaceURI == null ? "" : namespaceURI;
        this.localName = elementName == null ? "" : elementName;
        this.name = name == null ?  "" : name;
        this.valueStart = valueStart == null ? "" : valueStart;
    }

    
    /**
     * The Namespace URI, or the empty string if the element has no namespace 
     * URI or if Namespace processing is not being performed
     */
    public final String namespaceURI;
    
    /**
     * The local name (without prefix), or the empty string if<ul>
     * <li>namespace processing is not being performed
     * <li>the code represents an attribute in the namespace's global scopet
     * </ul>
     */
    public final String localName;
    
    /**
     * The name
     */
    public final String name;
    
    /**
     * The beginning of the value
     */
    public final String valueStart;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String text = "".equals(this.name) ? this.localName : this.name;
        if(this.valueStart != null && this.valueStart.length() > 0) text += '=' + this.valueStart + '\u2026';
        return text;
    }
    
}
