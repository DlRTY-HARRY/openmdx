/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: Uml2Mapper_1.java,v 1.9 2008/04/02 17:38:40 wfro Exp $
 * Description: XMIExternalizer_1
 * Revision:    $Revision: 1.9 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/02 17:38:40 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2007, OMEX AG, Switzerland
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

package org.openmdx.model1.mapping.xmi;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.dataprovider.cci.SystemAttributes;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.ModelAttributes;
import org.openmdx.model1.mapping.AbstractMapper_1;

//---------------------------------------------------------------------------
@SuppressWarnings("unchecked")
public class Uml2Mapper_1
    extends AbstractMapper_1 {

    //---------------------------------------------------------------------------
    public Uml2Mapper_1() {
        super("xmi");
        this.packages = new HashMap();
        this.subpackages = new HashMap();
    }

    //---------------------------------------------------------------------------
    public void externalize(
        String qualifiedPackageName,
        Model_1_3 model,
        ZipOutputStream zip)
        throws ServiceException {

        SysLog.trace("externalize");

        this.model = model;

        long start = System.currentTimeMillis();

        Set topLevelPackageNames = this.determinePackageContainment();

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(bs);

        // create XMI stream (Poseidon for UML specific)
        this.writeUMLModel(topLevelPackageNames, pw, Uml2ModelMapper.XMI_FORMAT_RSM6);
        pw.flush();

        this.addToZip(zip, bs, null, "rsm6.xmi");

        long end = System.currentTimeMillis();
        SysLog.info("Uml2Mapper: time(ms) used to externalize: " + (end - start));

    }

    //---------------------------------------------------------------------------
    private Set determinePackageContainment()
        throws ServiceException {

        // Note:
        // The XMI/UML mapping requires that all packages are traversed in their
        // containment order. Since our MOF packages are all toplevel model
        // elements (i.e. not nested), this package hierarchy must be reconstructed
        // based on the qualified package names.

        // set of all toplevel package names, e.g. ch, org, net
        Set topLevelPackageNames = new HashSet();

        // maps qualified package names to the designated package model element
        this.packages.clear();
        // lists the qualified name of all subpackages for a given package
        this.subpackages.clear();

        for(
            Iterator i = this.model.getContent().iterator(); 
            i.hasNext();
        ) {
            ModelElement_1_0 elementDef = (ModelElement_1_0) i.next();
            if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PACKAGE)) {
                String qualifiedName = ((String) elementDef.values("qualifiedName").get(0)).substring(
                    0, 
                    ((String) elementDef.values("qualifiedName").get(0)).lastIndexOf(":")
                );
                packages.put(qualifiedName, elementDef);

                // check whether this package is a toplevel package
                if(qualifiedName.indexOf(":") == -1) {
                    // found a toplevel package
                    topLevelPackageNames.add(qualifiedName);
                } 
                else {
                    // found no toplevel package, get parent package for this package
                    String parentPackageName = qualifiedName.substring(
                        0,
                        qualifiedName.lastIndexOf(":"));

                    // add new subpackage for this parent package
                    if (!this.subpackages.containsKey(parentPackageName)) {
                        this.subpackages.put(parentPackageName, new HashSet());
                    }
                    ((Set)this.subpackages.get(parentPackageName)).add(qualifiedName);
                }
            }
        }
        return topLevelPackageNames;
    }

    //---------------------------------------------------------------------------
    private void writeUMLModel(
        Set topLevelPackageNames,
        PrintWriter outputStream,
        short xmiFormat
    ) throws ServiceException {

        SysLog.trace("writeUMLModel");

        Uml2ModelMapper writer = new Uml2ModelMapper(outputStream, xmiFormat);

        // write XMI and UML:Model header
        writer.mapModelBegin();

        for (Iterator it = topLevelPackageNames.iterator(); it.hasNext();) {
            this.mapPackage(writer, (String) it.next());
        }

        // write XMI and UML:Model footer
        writer.mapModelEnd();
    }

    //---------------------------------------------------------------------------
    private void mapPackage(
        Uml2ModelMapper writer,
        String qualifiedPackageName)
        throws ServiceException {

        SysLog.trace("writePackage ", qualifiedPackageName);

        ModelElement_1_0 packageDef = (ModelElement_1_0) this.packages.get(qualifiedPackageName);

        // write package header
        writer.mapPackageBegin(packageDef);

        // write all model elements in this package
        for(
            Iterator i = packageDef.values("content").iterator(); 
            i.hasNext();
        ) {
            this.mapModelElement(writer, this.model.getElement(i.next()));
        }

        // write all subpackages within this package
        if (this.subpackages.containsKey(qualifiedPackageName)) {
            for(
                Iterator i = ((Set)this.subpackages.get(qualifiedPackageName)).iterator(); 
                i.hasNext();
            ) {
                this.mapPackage(writer, (String) i.next());
            }
        }

        // write package footer
        writer.mapPackageEnd(packageDef);
    }

    //---------------------------------------------------------------------------
    private void mapAssociationEnd(
        Uml2ModelMapper mapper,
        ModelElement_1_0 association,
        ModelElement_1_0 associationEndDef,
        boolean mapAsAttribute
    ) throws ServiceException {
        ModelElement_1_0 associationEnd1TypeDef = this.model.getElement(associationEndDef.values("type").get(0));
        List associationEnd1QualifierTypes = new ArrayList();
        for(
            Iterator i = associationEndDef.values("qualifierType").iterator(); 
            i.hasNext();
        ) {
            associationEnd1QualifierTypes.add(
                this.model.getElement(i.next())
            );
        }
        mapper.mapAssociationEnd(
            association,
            associationEndDef,
            associationEnd1TypeDef,
            associationEnd1QualifierTypes,
            mapAsAttribute
        );        
    }
    
    //---------------------------------------------------------------------------
    private void mapModelElement(
        Uml2ModelMapper mapper,
        ModelElement_1_0 elementDef
    ) throws ServiceException {

        // Primitive
        if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(
            ModelAttributes.PRIMITIVE_TYPE)) {
            mapper.mapPrimitiveType(elementDef);
        }
        // Alias
        else if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(
            ModelAttributes.ALIAS_TYPE)) {
            ModelElement_1_0 typeDef = this.model.getElement(elementDef.values("type").get(0));
            boolean refTypeIsPrimitive = typeDef.values(
                SystemAttributes.OBJECT_CLASS).contains(
                ModelAttributes.PRIMITIVE_TYPE);
            mapper.mapAliasType(elementDef, typeDef, refTypeIsPrimitive);
        }
        // Class
        else if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.CLASS)) {

            // determine the direct supertypes
            List supertypePaths = elementDef.values("supertype");
            List supertypes = new ArrayList();
            for(Iterator it = supertypePaths.iterator(); it.hasNext();) {
                supertypes.add(this.model.getElement(it.next()));
            }

            // check whether this class has structural features
            boolean hasFeatures = hasStructuralFeatures(elementDef.values("content"));

            // write class header with direct supertypes
            mapper.mapClassBegin(
                elementDef,
                supertypes,
                hasFeatures,
                false
            );
            // Map features
            for(
                Iterator iterator = elementDef.values("content").iterator(); iterator.hasNext();) {
                this.mapModelElement(
                    mapper, 
                    this.model.getElement(iterator.next())
                );
            }
            // Map navigable association ends as class members
            for(Iterator i = this.model.getContent().iterator(); i.hasNext(); ) {
                ModelElement_1_0 candidate = (ModelElement_1_0)i.next();
                if(candidate.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ASSOCIATION)) {
                    ModelElement_1_0 associationEnd1Def = this.model.getElement(candidate.values("content").get(0));
                    ModelElement_1_0 associationEnd2Def = this.model.getElement(candidate.values("content").get(1));
                    // End 1
                    if(
                        this.model.getElement(associationEnd2Def.values("type").get(0)).equals(elementDef) &&
                        ((Boolean)associationEnd1Def.values("isNavigable").get(0)).booleanValue()
                    ) {
                        this.mapAssociationEnd(
                            mapper, 
                            candidate, 
                            associationEnd1Def, 
                            true
                        );
                    }
                    // End 2
                    if(
                        this.model.getElement(associationEnd1Def.values("type").get(0)).equals(elementDef) &&
                        ((Boolean)associationEnd2Def.values("isNavigable").get(0)).booleanValue()
                    ) {
                        this.mapAssociationEnd(
                            mapper, 
                            candidate, 
                            associationEnd2Def, 
                            true
                        );
                    }
                }
            }
            mapper.mapClassEnd(elementDef, hasFeatures);
        }
        // Attribute
        else if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ATTRIBUTE)) {
            ModelElement_1_0 typeDef = this.model.getElement(elementDef.values("type").get(0));
            boolean refTypeIsPrimitive = typeDef.values(
                SystemAttributes.OBJECT_CLASS).contains(
                ModelAttributes.PRIMITIVE_TYPE);
            boolean isDerived = ((Boolean) elementDef
                .values("isDerived")
                .get(0)).booleanValue();
            boolean isChangeable = ((Boolean) elementDef
                .values("isChangeable")
                .get(0)).booleanValue();
            mapper.mapAttribute(
                elementDef,
                isDerived,
                isChangeable,
                typeDef,
                refTypeIsPrimitive);
        } 
        // Association
        else if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.ASSOCIATION)) {
            ModelElement_1_0 associationEnd1Def = this.model.getElement(elementDef.values("content").get(0));
            ModelElement_1_0 associationEnd2Def = this.model.getElement(elementDef.values("content").get(1));
            mapper.mapAssociationBegin(
                elementDef,
                associationEnd1Def,
                associationEnd2Def
            );
            // End 1
            if(!((Boolean)associationEnd1Def.values("isNavigable").get(0)).booleanValue()) {
                this.mapAssociationEnd(
                    mapper, 
                    elementDef, 
                    associationEnd1Def, 
                    false
                );
            }
            // End 2
            if(!((Boolean)associationEnd2Def.values("isNavigable").get(0)).booleanValue()) {
                this.mapAssociationEnd(
                    mapper, 
                    elementDef, 
                    associationEnd2Def, 
                    false
                );
            }
            mapper.mapAssociationEnd(
                elementDef
            );
        }
        // Operation
        else if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.OPERATION)) {
            ModelElement_1_0 returnType = null;
            for(
                Iterator i = elementDef.values("content").iterator(); 
                i.hasNext();
            ) {
                ModelElement_1_0 parameter = this.model.getElement(i.next());
                if("return".equals(parameter.values("direction").get(0))) {
                    returnType = this.model.getElement(parameter.values("type"));
                    break;
                }
            }
            mapper.mapOperationBegin(
                elementDef,
                returnType
            );
            // Map parameters
            for(
                Iterator i = elementDef.values("content").iterator(); 
                i.hasNext();
            ) {
                this.mapModelElement(mapper, this.model.getElement(i.next()));
            }
            mapper.mapOperationEnd(elementDef);
        }
        // Exception
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.EXCEPTION)) {
            mapper.mapOperationBegin(
                elementDef, 
                null
            );
            for(
                Iterator i = elementDef.values("content").iterator(); 
                i.hasNext();
            ) {
                this.mapModelElement(
                    mapper, 
                    this.model.getElement(i.next())
                );
            }
            mapper.mapOperationEnd(elementDef);
        }
        // Parameter
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.PARAMETER)) {
            ModelElement_1_0 typeDef = this.model.getElement(elementDef.values("type").get(0));
            mapper.mapParameter(
                elementDef, 
                typeDef
            );
        } 
        // Structure type 
        else if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_TYPE)) {
            boolean hasFields = hasStructureFields(elementDef.values("content"));
            mapper.mapClassBegin(
                elementDef, 
                Collections.EMPTY_LIST,
                hasFields,
                true
            );
            // Map fields
            for(
                Iterator i = elementDef.values("content").iterator(); 
                i.hasNext();
            ) {
                this.mapModelElement(mapper, this.model.getElement(i.next()));
            }
            mapper.mapClassEnd(
                elementDef, 
                hasFields
            );
        }
        // Structure field
        else if(elementDef.values(SystemAttributes.OBJECT_CLASS).contains(ModelAttributes.STRUCTURE_FIELD)) {
            ModelElement_1_0 typeDef = this.model.getElement(elementDef.values("type").get(0));
            boolean refTypeIsPrimitive = typeDef.values(
                SystemAttributes.OBJECT_CLASS).contains(
                ModelAttributes.PRIMITIVE_TYPE);
            mapper.mapAttribute(
                elementDef, 
                false, 
                false, 
                typeDef, 
                refTypeIsPrimitive
            );
        }
    }

    //---------------------------------------------------------------------------
    private boolean hasStructureFields(SparseList content)
        throws ServiceException {
        if (!content.isEmpty()) {
            for (Iterator iterator = content.iterator(); iterator.hasNext();) {
                ModelElement_1_0 elementDef = this.model.getElement(iterator
                    .next());
                if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(
                    ModelAttributes.STRUCTURE_FIELD)) { return true; }
            }
        }
        return false;
    }

    //---------------------------------------------------------------------------
    private boolean hasStructuralFeatures(SparseList content)
        throws ServiceException {
        if (!content.isEmpty()) {
            for (Iterator iterator = content.iterator(); iterator.hasNext();) {
                ModelElement_1_0 elementDef = this.model.getElement(iterator
                    .next());
                if (elementDef.values(SystemAttributes.OBJECT_CLASS).contains(
                    ModelAttributes.ATTRIBUTE)) {
                    return true;
                } else if (elementDef
                    .values(SystemAttributes.OBJECT_CLASS)
                    .contains(ModelAttributes.OPERATION)) {
                    return true;
                }
                // in openMDX exceptions are modeled as operations and are therefore 
                // structural features as well in the broadest sense
                else if (elementDef
                    .values(SystemAttributes.OBJECT_CLASS)
                    .contains(ModelAttributes.EXCEPTION)) { return true; }
            }
        }
        return false;
    }

    //---------------------------------------------------------------------------
    // Variables
    //---------------------------------------------------------------------------
    private final Map subpackages;
    private final Map packages;
}
