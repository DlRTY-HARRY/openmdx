/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: StandardXMLOutputFactory.java,v 1.7 2010/04/08 17:28:31 wfro Exp $
 * Description: Standard XML Output Factory 
 * Revision:    $Revision: 1.7 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/08 17:28:31 $
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
package org.openmdx.base.xml.stream;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * The Standard XML Output Factory creates non-escaping XMLStreamWriters 
 */
public class StandardXMLOutputFactory extends AbstractXMLOutputFactory {

    /**
     * Constructor 
     */
    public StandardXMLOutputFactory(
    ) {
        super("text/xml");
    }

    /**
     * Tells whether white space characters shall be included or not
     * 
     * @return <code>true</code> if white space characters shall be included
     */
    protected boolean isPretty(
    ){
        return getMimeType().startsWith("text/");
    }
    
    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.Writer)
     */
    @Override
    public XMLStreamWriter createXMLStreamWriter(
        Writer characterStream
    ) throws XMLStreamException {
        return new StandardXMLStreamWriter(
            null, // encoding
            isPretty(), 
            characterStream
        );
    }

    /* (non-Javadoc)
     * @see javax.xml.stream.XMLOutputFactory#createXMLStreamWriter(java.io.OutputStream, java.lang.String)
     */
    @Override
    public XMLStreamWriter createXMLStreamWriter(
        OutputStream byteStream, 
        String encoding
    ) throws XMLStreamException {
        try {
            return new StandardXMLStreamWriter(
                encoding, 
                isPretty(), 
                byteStream
            );
        } catch (UnsupportedEncodingException exception) {
            throw AbstractXMLStreamWriter.toXMLStreamException(exception);
        }
    }

}