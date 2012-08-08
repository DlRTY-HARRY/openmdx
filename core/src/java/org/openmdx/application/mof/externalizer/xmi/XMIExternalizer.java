/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: XMIExternalizer.java,v 1.4 2010/04/13 14:05:44 wfro Exp $
 * Description: RoseExporterMain command-line tool
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2010/04/13 14:05:44 $
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.mof.externalizer.xmi;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmdx.application.mof.externalizer.spi.Model_1Accessor;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.uses.gnu.getopt.Getopt;
import org.openmdx.uses.gnu.getopt.LongOpt;

/**
 * XMI Externalizer
 */
public class XMIExternalizer {

    /**
     * Main
     * 
     * @param args
     */
    public static void main(
        String[] args
    ) {
        try {
            Getopt g = new Getopt(
                XMIExternalizer.class.getName(),
                args,
                "",
                new LongOpt[]{
                    new LongOpt("pathMapSymbol", LongOpt.OPTIONAL_ARGUMENT, null, 's'),
                    new LongOpt("pathMapPath", LongOpt.OPTIONAL_ARGUMENT, null, 'p'),
                    new LongOpt("url", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
                    new LongOpt("xmi", LongOpt.REQUIRED_ARGUMENT, null, 'x'),
                    new LongOpt("out", LongOpt.OPTIONAL_ARGUMENT, null, 'o'),
                    new LongOpt("format", LongOpt.OPTIONAL_ARGUMENT, null, 't'),
                    new LongOpt("openmdxjdo", LongOpt.OPTIONAL_ARGUMENT, null, 'j')
                }
            );

            List<String> modelNames = new ArrayList<String>();
            List<String> formats = new ArrayList<String>();
            String url = null;
            String openmdxjdo = null;
            String stXMIDialect = "poseidon";
            String outFileName = "./out.jar";
            List<String> pathMapSymbols = new ArrayList<String>();
            List<String> pathMapPaths = new ArrayList<String>();

            int c;
            while ((c = g.getopt()) != -1) {
                switch(c) {
                    case 's':
                        pathMapSymbols.add(
                            g.getOptarg()
                        );
                        break;
                    case 't':
                        formats.add(
                            g.getOptarg()
                        );
                        break;
                    case 'p':
                        pathMapPaths.add(
                            g.getOptarg()
                        );
                        break;
                    case 'u':
                        url = g.getOptarg();
                        break;
                    case 'j':
                        openmdxjdo = g.getOptarg();
                        break;
                    case 'x':
                        stXMIDialect = g.getOptarg();
                        break;
                    case 'o':
                        outFileName = g.getOptarg();
                        break;
                }
            }

            // modelNames      
            for(
                    int i = g.getOptind(); 
                    i < args.length ; 
                    i++
            ) {
                modelNames.add(args[i]);
            }
            if(modelNames.size() != 1) {
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.INVALID_CONFIGURATION,
                    "number of model names must be 1",
                    new BasicException.Parameter("model names", modelNames)
                );
            }

            Model_1Accessor modelExternalizer = new Model_1Accessor("Mof", openmdxjdo);

            short xmiFormat = 0;
            if("poseidon".equals(stXMIDialect)) {
                System.out.println("INFO:    Gentleware Poseidon XMI 1");
                xmiFormat = XMIImporter_1.XMI_FORMAT_POSEIDON;
            } else if("magicdraw".equals(stXMIDialect)) {
                System.out.println("INFO:    No Magic MagicDraw XMI 1");
                xmiFormat = XMIImporter_1.XMI_FORMAT_MAGICDRAW;
            } else if("rsm".equals(stXMIDialect)) {
                System.out.println("INFO:    IBM Rational Software Modeler XMI 2");          
                xmiFormat = XMIImporter_1.XMI_FORMAT_RSM;
            } else if("emf".equals(stXMIDialect)) {
                System.out.println("INFO:    Eclipse UML2 Tools");          
                xmiFormat = XMIImporter_1.XMI_FORMAT_EMF;
            }

            // pathMap
            Map<String,String> pathMap = new HashMap<String,String>();
            for(int i = 0; i < pathMapSymbols.size(); i++) {
                pathMap.put(
                    pathMapSymbols.get(i),
                    pathMapPaths.get(i)
                );
            }
            System.out.println("INFO:    pathMap=" + pathMap);
            modelExternalizer.importModel(
                new XMIImporter_1(
                    new URL(url),
                    xmiFormat,
                    pathMap,
                    System.out,
                    System.err,
                    System.err
                )
            );

            FileOutputStream fos = new FileOutputStream(
                new File(outFileName)
            );
            fos.write(
                modelExternalizer.externalizePackageAsJar(
                    modelNames.get(0),
                    formats
                )
            );  
            fos.close();
        } catch(ServiceException e) {
        	e.log().printStackTrace();
            System.exit(-1);
        } catch(Exception e) {
        	new ServiceException(e).log().printStackTrace();
            System.exit(-1);
        }
    }

}
