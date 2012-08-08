/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: ClassDef.java,v 1.13 2008/03/21 18:40:15 hburger Exp $
 * Description: VelocityClassDef class
 * Revision:    $Revision: 1.13 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/03/21 18:40:15 $
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
package org.openmdx.model1.mapping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.compatibility.base.collection.SparseList;
import org.openmdx.compatibility.base.naming.Path;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_0;
import org.openmdx.model1.code.Stereotypes;

@SuppressWarnings("unchecked")
public class ClassDef
  extends ClassifierDef {
  
  //-------------------------------------------------------------------------
  public ClassDef(
    ModelElement_1_0 classDef,
    Model_1_0 model
  ) throws ServiceException {
    this( 
      classDef,
      model,
      false, // lazySuperTypes
      null // metaData
    );
  }

  public ClassDef(
      ModelElement_1_0 classDef,
      Model_1_0 model,
      MetaData_1_0 metaData
    ) throws ServiceException {
      this( 
        classDef,
        model,
        false, // lazySuperTypes
        metaData
      );
    }
  
  private ClassDef(
      ModelElement_1_0 classDef,
      Model_1_0 model,
      boolean lazySuperTypes, 
      MetaData_1_0 metaData
  ) throws ServiceException {
      this( 
        (String)classDef.values("name").get(0),
        (String)classDef.values("qualifiedName").get(0),
        (String)classDef.values("annotation").get(0),
        new HashSet(classDef.values("stereotype")),
        ((Boolean)classDef.values("isAbstract").get(0)).booleanValue(),
        lazySuperTypes ? null : getSuperTypes(classDef, model, metaData), 
        classDef.values("allSupertype"),    
        lazySuperTypes ? classDef : null, 
        lazySuperTypes ? model : null, 
        metaData
      );      
  }

  
  //-------------------------------------------------------------------------
  public boolean isInstanceOf(
      String qualifiedName
  ){
      for(
          Iterator i = this.allSupertypes.populationIterator();
          i.hasNext();
      ){
          if (((Path) i.next()).getBase().equals(qualifiedName)) {
              return true;
          }
      }
      return false;
  }

  //-------------------------------------------------------------------------
  private static List getSuperTypes(
    ModelElement_1_0 classDef,
    Model_1_0 model, 
    MetaData_1_0 metaData
  ) throws ServiceException {
    List supertypes = new ArrayList();
    for(
      Iterator it = classDef.values("supertype").iterator();
      it.hasNext();
    ) {
      ModelElement_1_0 supertype = model.getDereferencedType(it.next());
      supertypes.add(
        new ClassDef(
            supertype,
            model,
            true, // lazySuperTypes
            metaData
        )
      );
    }
    return supertypes;
  }
  
  //-------------------------------------------------------------------------
  /**
   * @deprecated Use {@link #ClassDef(String,String,String,Set,boolean,boolean,List,ModelElement_1_0, Model_1_0, MetaData_1_0)} instead
   */
  public ClassDef(
    String name,
    String qualifiedName,
    String annotation,
    Set stereotype,
    boolean isAbstract,
    List supertypes
  ) {
    this(name, qualifiedName, annotation, stereotype, isAbstract, supertypes, null, null, null, null);
  }

//-------------------------------------------------------------------------
  private ClassDef(
    String name,
    String qualifiedName,
    String annotation,
    Set stereotype,
    boolean isAbstract,
    List supertypes, 
    SparseList allSupertypes,
    ModelElement_1_0 classDef, 
    Model_1_0 model, 
    MetaData_1_0 metaData
  ) {
    super(
      name,
      qualifiedName,
      annotation,
      stereotype,
      isAbstract,
      supertypes
    );
    this.allSupertypes = allSupertypes;
    this.classDef = classDef;
    this.model = model;
    this.metaData = metaData;
    this.lazyMetaData = metaData != null;
  } 

  /* (non-Javadoc)
   * @see org.openmdx.model1.mapping.ClassifierDef#getSupertypes()
   */
  public List getSupertypes() {
      if(
          super.supertypes == null &&
          this.classDef != null 
      ) try {
            super.supertypes = getSuperTypes(this.classDef, this.model, this.metaData);
      } catch (ServiceException exception) {
            // return null;
      }
      return super.supertypes;
  }
  
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object that) {
    return 
        that instanceof ClassDef && 
        this.classDef.equals(((ClassDef)that).classDef);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
 public int hashCode() {
    return this.classDef.hashCode();
  }

  public Object getClassMetaData(){
      if(this.metaData == null) {
          System.out.println("WARNING: meta data for class " + this.getQualifiedName()+ " is null");
      }
      if(this.lazyMetaData) {
          this.lazyMetaData = false;
          this.classMetaData = this.metaData.getClassMetaData(getQualifiedName());
      }
      return this.classMetaData;
  }

  public boolean isMixIn(
  ) {
    return getStereotype().contains(Stereotypes.ROOT);
  }
  
  protected static ClassDef getSuperClassDef(
      ClassDef classDef
  ){
      for(
          Iterator i = classDef.getSupertypes().iterator();
          i.hasNext();
       ){
           ClassDef c = (ClassDef) i.next();
           if(!c.isMixIn()) {
               return c;
           }
      }
      return null;
  }
  
  /**
   * Find the class to be extended
   * 
   * @param immediate skip abstract non-base super-classes if <code>false</code>.
   * 
   * @return the super-class
   */
  public ClassDef getSuperClassDef(
      boolean immediate
  ){
      ClassDef c = getSuperClassDef(this);
      if(c != null && !immediate) {
          while(c.isAbstract()) {
              ClassDef t = getSuperClassDef(c);
              if(t == null) {
                  return c;
              } 
              c = t;
          }
      }
      return c;
  }

  public ClassDef getBaseClassDef(){
      ClassDef c = this;
      for(
          ClassDef s = getSuperClassDef(c);
          s != null;
          s = getSuperClassDef(c)
      ) {
          c = s;
      }
      return c;
  }
  

  private final ModelElement_1_0 classDef;
  private final Model_1_0 model;
  
  private final MetaData_1_0 metaData;
  private boolean lazyMetaData;
  private Object classMetaData;
  private final SparseList allSupertypes;

}
