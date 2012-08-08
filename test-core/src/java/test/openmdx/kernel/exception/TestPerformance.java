/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: TestPerformance.java,v 1.3 2009/03/05 17:51:35 hburger Exp $
 * Description: TestPerformance 
 * Revision:    $Revision: 1.3 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/03/05 17:51:35 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2009, OMEX AG, Switzerland
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

package test.openmdx.kernel.exception;

import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.junit.Test;
import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.kernel.exception.BasicException;

/**
 * TestPerformance
 */
public class TestPerformance {

    private static final int THROW_COUNT = 1000;
    
    private static final int ACCEPTABLE_FACTOR = 2;
    
    @Test
    public void comparePerformance(){
        throwAndCatch(false);
    }

    @Test
    public void testRuntimeServiceException(){
        org.junit.Assert.assertTrue(
            "Overhead Excess", 
            throwAndCatch(false) * ACCEPTABLE_FACTOR > throwAndCatch(true)
        );
    }
    
    private long throwAndCatch(
        boolean service
    ){
        long ns = 0L;
        for(
           int i = 0;
           i <= THROW_COUNT;
           i++
        ){
            if(i == 1) {
                ns = - System.nanoTime();
            }
            try {
                parse("XYZ", service);
                fail("The value shoud be unparsable");
            } catch (RuntimeException expected) {
                if(i == 0) {
                    System.out.println("Summary: " + expected);
                    System.out.print("Detail: ");
                    expected.printStackTrace(System.out);
                }
            }
        }
        ns += System.nanoTime();
        ns /= THROW_COUNT;
        BigDecimal ms = BigDecimal.valueOf(ns, 3);
        System.out.println(
            (service ? "RuntimeServiceException" : "RuntimeException") +
            " requires " + ms.toPlainString() + " ms per double try/catch"
        );
        return ns;
    }
    
    protected Number parse(
        String text,
        boolean service
    ){
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException exception) {
            throw service ? new RuntimeServiceException(
                exception,
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.BAD_PARAMETER,
                "Parse failure",
                new BasicException.Parameter("text", text)
            ) : new RuntimeException(
                "Parse failure: " + text,
                exception
            );
        }
    }
    
}
