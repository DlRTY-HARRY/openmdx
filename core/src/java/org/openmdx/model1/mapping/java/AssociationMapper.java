/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AssociationMapper.java,v 1.4 2008/06/16 13:30:26 hburger Exp $
 * Description: AssociationMapper 
 * Revision:    $Revision: 1.4 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/06/16 13:30:26 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2008, OMEX AG, Switzerland
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
package org.openmdx.model1.mapping.java;

import java.io.Writer;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;
import org.openmdx.model1.code.AggregationKind;
import org.openmdx.model1.mapping.AssociationDef;
import org.openmdx.model1.mapping.AssociationEndDef;
import org.openmdx.model1.mapping.MapperUtils;
import org.openmdx.model1.mapping.MetaData_1_0;
import org.openmdx.model1.mapping.ReferenceDef;

/**
 * AssociationMapper
 */
public class AssociationMapper
    extends AbstractMapper
{

    /**
     * Constructor 
     *
     * @param writer
     * @param model
     * @param format
     * @param packageSuffix
     * @param metaData
     * @throws ServiceException 
     */
    public AssociationMapper(
        ModelElement_1_0 element,
        Writer writer,
        Model_1_3 model,
        Format format,
        String packageSuffix,
        MetaData_1_0 metaData
    ) throws ServiceException {
        super(writer, model, format, packageSuffix, metaData);
        this.associationName = Identifier.CLASS_PROXY_NAME.toIdentifier(
            (String)element.values("name").get(0),
            null, // removablePrefix
            null, // prependablePrefix
            null, // removableSuffix
            null //appendableSuffix
        );
        this.associationDef = new AssociationDef(
            element,
            model
        );
    }

    final String associationName;
    
    final AssociationDef associationDef;

    static final String QUALIFIED_CONTAINER_CLASS_NAME = "org.w3c.cci2.Container";
    static final String QUALIFIED_COLLECTION_CLASS_NAME = "java.util.Collection";
    
    /**
     * Begin
     * 
     * @throws ServiceException
     */
    protected void mapBegin(
    ) throws ServiceException {
        this.trace("Association/Begin");
        this.fileHeader();
        this.pw.println(
            "package " + this.getNamespace(
                MapperUtils.getNameComponents(
                    MapperUtils.getPackageName(
                        this.associationDef.getQualifiedName()
                    )
                )
            ) + ';'
        );
        this.pw.println();
        this.pw.println("/**");
        this.pw.println(" * Association Interface <code>" + this.associationDef.getName() + "</code>"); 
        if (this.associationDef.getAnnotation() != null) {
            this.pw.println(" *<p>");
            this.pw.println(MapperUtils.wrapText(" * ", this.associationDef.getAnnotation()));
        }
        this.pw.println(" */");
        this.pw.println("public interface " + this.associationName + " {"); 
    }

    /**
     * End
     * 
     * @throws ServiceException
     */
    protected void mapEnd(
    ) throws ServiceException {
        this.pw.println("}"); 
        this.trace("Association/End");
    }
    
    /**
     * End
     * 
     * @throws ServiceException
     */
    protected void mapAssociationEnd(
        AssociationEndDef associationEnd
    ) throws ServiceException {
        this.trace("AssociationEnd/Begin");
        String name = Identifier.CLASS_PROXY_NAME.toIdentifier(associationEnd.getName());            
        String qualifierValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(associationEnd.getQualifierName());
        String qualifierTypeName = qualifierValueName + InstanceMapper.QUALIFIER_TYPE_SUFFIX;
        String qualifierValueType = getType(associationEnd.getQualifierType());
        String objectValueName = Identifier.ATTRIBUTE_NAME.toIdentifier(associationEnd.getName());
        if(objectValueName.equals(qualifierValueName)) {
            objectValueName = '_' + objectValueName;
        }
        this.pw.println();
        this.pw.println("  /**");
        this.pw.println("   * Association End Interface <code>" + associationEnd.getName() + "</code>"); 
        if (associationEnd.getAnnotation() != null) {
            this.pw.println(" *<p>");
            this.pw.println(MapperUtils.wrapText(" * ", associationEnd.getAnnotation()));
        }
        this.pw.println("   */");
        this.pw.println(
            "  interface " + name + "<E> extends " + (
                    true ? QUALIFIED_CONTAINER_CLASS_NAME : QUALIFIED_COLLECTION_CLASS_NAME
        )+ "<E> {"); 
        this.pw.println();            
        this.pw.println("     E get(");
        this.pw.println("       " + InstanceMapper.QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeName + ",");
        this.pw.println("       " + qualifierValueType + " " + qualifierValueName);
        this.pw.println("     );");
        this.pw.println();
        ReferenceDef referenceDef = associationEnd.getReference(); 
        if(referenceDef != null && referenceDef.isChangeable()) {
            this.pw.println("     void add(");
            this.pw.println("       " + InstanceMapper.QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeName + ",");
            this.pw.println("       " + qualifierValueType + " " + qualifierValueName + ",");
            this.pw.println("       E " + objectValueName);
            this.pw.println("     );");
            this.pw.println();            
            this.pw.println("     void remove(");
            this.pw.println("       " + InstanceMapper.QUALIFIER_TYPE_CLASS_NAME + " " + qualifierTypeName + ",");
            this.pw.println("       " + qualifierValueType + " " + qualifierValueName);
            this.pw.println("     );");
            this.pw.println();            
        }
        this.pw.println("  }"); 
        this.pw.println();                        
        this.trace("AssociationEnd/End");
    }
    
    /**
     * Map Association
     * 
     * @throws ServiceException 
     */
    public boolean mapAssociation(
    ) throws ServiceException {
        for(AssociationEndDef associationEnd : this.associationDef.getEnds()) {
            if(!AggregationKind.NONE.equals(associationEnd.getAggregation())) {
                mapBegin();
                mapAssociationEnd(associationEnd);
                mapEnd();
                return true;
            }
        }
        return false;
    }
    
}
