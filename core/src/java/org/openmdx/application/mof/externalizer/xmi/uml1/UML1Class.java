/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: UML1Class.java,v 1.1 2009/01/13 02:10:39 wfro Exp $
 * Description: lab client
 * Revision:    $Revision: 1.1 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/01/13 02:10:39 $
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

package org.openmdx.application.mof.externalizer.xmi.uml1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class UML1Class
  extends UML1Classifier
{
  public UML1Class(
      String id,
      String name,
      String qualifiedName,
      UML1VisibilityKind visibility,
      boolean isSpecification,
      boolean isRoot,
      boolean isLeaf,
      boolean isAbstract,
      boolean isActive
  ) {
      super(id, name, qualifiedName, visibility, isSpecification, isRoot, isLeaf, isAbstract);
      this.setActive(isActive);
      superclasses = new HashSet();
  }
  
  public boolean isActive(
  ) {
    return this.isActive;
  }
  
  public void setActive(
    boolean isActive
  ) {
    this.isActive = isActive;
  }
  
  // openMDX extension
  public List getAttributes() {
    List attributes = new ArrayList();
    for(
      Iterator it = this.getFeature().iterator();
      it.hasNext();
    ) {
      UML1Feature feature = (UML1Feature)it.next();
      if (feature instanceof UML1Attribute)
      {
        attributes.add(feature);
      }
    }
    
    return attributes;
  }

  // openMDX extension
  public List getOperations() {
    List operations = new ArrayList();
    for(
      Iterator it = this.getFeature().iterator();
      it.hasNext();
    ) {
      UML1Feature feature = (UML1Feature)it.next();
      if (feature instanceof UML1Operation)
      {
        operations.add(feature);
      }
    }
    
    return operations;
  }


  // openMDX extension
  public Set getSuperclasses() {
    return superclasses;
  }

  private Set superclasses = null;
  private boolean isActive = false;
}