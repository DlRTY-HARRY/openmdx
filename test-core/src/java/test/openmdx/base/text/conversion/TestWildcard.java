/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestWildcard.java,v 1.1 2009/03/06 14:46:45 hburger Exp $
 * Description: Wildcard Test
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/06 14:46:45 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2006-2009, OMEX AG, Switzerland
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
package test.openmdx.base.text.conversion;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openmdx.base.text.conversion.SQLWildcards;

/**
 * Wildcard Test
 */
public class TestWildcard {

    /**
     * 
     */
    protected final static SQLWildcards sqlWildcards = new SQLWildcards('\\');
    
    @Test
    public void testFromJDO(
    ){
        assertEquals("Convert .* Wildcard", "%X%Y%", sqlWildcards.fromJDO(".*X.*Y.*"));
        assertEquals("Convert . Wildcard", "_X_Y_", sqlWildcards.fromJDO(".X.Y."));
        assertEquals("Escape %", "\\%X\\%Y\\%", sqlWildcards.fromJDO("%X%Y%"));
        assertEquals("Escape _", "\\_X\\_Y\\_", sqlWildcards.fromJDO("_X_Y_"));
        assertEquals("Convert .* Escape", ".*X.*Y.*", sqlWildcards.fromJDO("\\.\\*X\\.\\*Y\\.\\*"));
        assertEquals("Convert . Escape", ".X.Y.", sqlWildcards.fromJDO("\\.X\\.Y\\."));
        assertEquals(
            "All",
            "%X%Y%" + "_X_Y_" + "\\%X\\%Y\\%" + "\\_X\\_Y\\_" + ".*X.*Y.*" + ".X.Y.",
            sqlWildcards.fromJDO(
                ".*X.*Y.*" + ".X.Y." + "%X%Y%" + "_X_Y_" + "\\.\\*X\\.\\*Y\\.\\*" + "\\.X\\.Y\\."
            )
        );
    }
    
    @Test
    public void testToJDO(
    ){
        assertEquals("Convert % Wildcard", ".*X.*Y.*", sqlWildcards.toJDO("%X%Y%"));
        assertEquals("Convert _ Wildcard", ".X.Y.", sqlWildcards.toJDO("_X_Y_"));
        assertEquals("Escape %", "%X%Y%", sqlWildcards.toJDO("\\%X\\%Y\\%"));
        assertEquals("Escape _", "_X_Y_", sqlWildcards.toJDO("\\_X\\_Y\\_"));
        assertEquals("Convert .* Escape", "\\.*X\\.*Y\\.*", sqlWildcards.toJDO(".*X.*Y.*"));
        assertEquals("Convert . Escape", "\\.X\\.Y\\.", sqlWildcards.toJDO(".X.Y."));
        assertEquals(
            "All",
            ".*X.*Y.*" + ".X.Y." + "%X%Y%" + "_X_Y_" + "\\.*X\\.*Y\\.*" + "\\.X\\.Y\\.",
            sqlWildcards.toJDO(
                "%X%Y%" + "_X_Y_" + "\\%X\\%Y\\%" + "\\_X\\_Y\\_" + ".*X.*Y.*" + ".X.Y."
            )
        );
    }

}