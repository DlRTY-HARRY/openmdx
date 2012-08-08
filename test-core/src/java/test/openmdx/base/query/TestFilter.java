/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestFilter.java,v 1.1 2009/09/17 17:12:59 hburger Exp $
 * Description: Test Filter
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/17 17:12:59 $
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
 * This product includes or is based on software developed by other 
 * organizations as listed in the NOTICE file.
 */
package test.openmdx.base.query;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openmdx.base.query.Condition;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.IsBetweenCondition;
import org.openmdx.base.query.IsGreaterCondition;
import org.openmdx.base.query.IsGreaterOrEqualCondition;
import org.openmdx.base.query.IsInCondition;
import org.openmdx.base.query.IsLikeCondition;
import org.openmdx.base.query.OrderSpecifier;
import org.openmdx.base.query.Quantors;
import org.openmdx.base.query.SoundsLikeCondition;

/**
 * Test Filter
 */
public class TestFilter {

    @Test
    public void testEncode() throws IOException {
      try {
        File scratchFile = File.createTempFile(getClass().getName(), null);
        System.out.println("writing to file " + scratchFile);
        OutputStream os = new FileOutputStream(scratchFile);
        Filter filter = new Filter(
            new Condition[]{
                new IsBetweenCondition(
                    Quantors.FOR_ALL,
                    "fBetween",
                    true,
                    "Lower", "Upper"
                ),
                new IsBetweenCondition(
                    Quantors.FOR_ALL,
                    "fBetween",
                    false,
                    "Lower", "Upper"
                ),
                new IsGreaterCondition(
                    Quantors.FOR_ALL,
                    "fGreater",
                    true,
                    Integer.valueOf(-1000), 
                    Integer.valueOf(1000)
                ),
                new IsGreaterCondition(
                    Quantors.FOR_ALL,
                    "fGreater",
                    false,
                    Integer.valueOf(-1000), 
                    Integer.valueOf(1000)
                ),
                new IsGreaterOrEqualCondition(
                    Quantors.FOR_ALL,
                    "fGreaterOrEqual",
                    true,
                    Double.parseDouble("-9999999999999.123"), 
                    Double.parseDouble("99999999999.123")
                ),
                new IsGreaterOrEqualCondition(
                    Quantors.FOR_ALL,
                    "fGreaterOrEqual",
                    false,
                    Double.parseDouble("-9999999999999.123"), 
                    Double.parseDouble("99999999999.123")
                ),
                new IsInCondition(
                    Quantors.FOR_ALL,
                    "fIsIn",
                    true,
                    Boolean.FALSE, 
                    Boolean.TRUE
                ),
                new IsInCondition(
                    Quantors.FOR_ALL,
                    "fIsIn",
                    false,
                    Boolean.FALSE, 
                    Boolean.TRUE
                ),
                new IsLikeCondition(
                    Quantors.FOR_ALL,
                    "fIsLike",
                    true,
                    "IsLikeCondition", 
                    "IsLikeCondition"
                ),
                new IsLikeCondition(
                    Quantors.FOR_ALL,
                    "fIsLike",
                    false,
                    "IsLikeCondition", 
                    "IsLikeCondition"
                ),
                new SoundsLikeCondition(
                    Quantors.FOR_ALL,
                    "fSoundsLike",
                    true,
                    "uri:@openmdx:a.b.c/:*/This/%", 
                    "uri:@openmdx:a.b.c/:*/That/%"
                ),
                new SoundsLikeCondition(
                    Quantors.FOR_ALL,
                    "fSoundsLike",
                    false,
                    "uri:@openmdx:a.b.c/:*/This/%", 
                    "uri:@openmdx:a.b.c/:*/That/%"
                )
            },
            new OrderSpecifier[]{
                new OrderSpecifier("order_ASC", Directions.ASCENDING),
                new OrderSpecifier("order_DESC", Directions.DESCENDING)
            }
        );
        XMLEncoder encoder = new XMLEncoder(os);
        encoder.writeObject(filter);
        encoder.close();

        InputStream is = new FileInputStream(scratchFile);
        XMLDecoder decoder = new XMLDecoder(is);
        //... Filter filter2 = (Filter)
            decoder.readObject();
        decoder.close();

      }
      catch(FileNotFoundException e) {
        Assert.fail("can not open scratch file");
      }
    }

}