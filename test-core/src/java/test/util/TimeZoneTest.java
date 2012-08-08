/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: TimeZoneTest.java,v 1.2 2009/09/18 12:44:37 hburger Exp $
 * Description: TestTimeZone 
 * Revision:    $Revision: 1.2 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/09/18 12:44:37 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2007, OMEX AG, Switzerland
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

package test.util;

import java.util.GregorianCalendar;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

/**
 * TestTimeZone
 *
 */
public class TimeZoneTest
{

    /**
     * @param args
     */
    public static void main(String[] args) {
        displayTimestamp();
    }

    private static void displayTimestamp(){
        try {
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            GregorianCalendar calendar = datatypeFactory.newXMLGregorianCalendar(
                new GregorianCalendar()
            ).toGregorianCalendar(
                null, // zone 
                null, // locale 
                null // defaults
            );
            System.out.println(
                "Now: " + new StringBuilder(
                    64
                ).append(
                    "TIMESTAMP '"
                ).append(
                    calendar.get(GregorianCalendar.YEAR)
                ).append(
                    '-'                            
                ).append(
                    calendar.get(GregorianCalendar.MONTH)
                ).append(
                    '-'                            
                ).append(
                    calendar.get(GregorianCalendar.DAY_OF_MONTH)
                ).append(
                    ' '
                ).append(
                    calendar.get(GregorianCalendar.HOUR_OF_DAY)
                ).append(
                    ':'
                ).append(
                    calendar.get(GregorianCalendar.MINUTE)
                ).append(
                    ':'
                ).append(
                    calendar.get(GregorianCalendar.SECOND)
                ).append(
                    '.'
                ).append(
                    String.valueOf(
                        1000 + calendar.get(GregorianCalendar.MILLISECOND)
                    ).substring(1)
                ).append(
                    ' '
                ).append(
                    TimeZone.getDefault().getID()
                ).append(
                    "'"
                )
            );
        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    final static String PATTERN = "yyyy-MM-dd HH:mm:ss.SSS ";

}