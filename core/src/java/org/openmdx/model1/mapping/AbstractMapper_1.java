/*
 * ====================================================================
 * Project:     openmdx, http://www.openmdx.org/
 * Name:        $Id: AbstractMapper_1.java,v 1.8 2008/04/02 17:38:40 wfro Exp $
 * Description: PackageExternalizer_1
 * Revision:    $Revision: 1.8 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2008/04/02 17:38:40 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2006, OMEX AG, Switzerland
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
package org.openmdx.model1.mapping;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.kernel.log.SysLog;
import org.openmdx.model1.accessor.basic.cci.ModelElement_1_0;
import org.openmdx.model1.accessor.basic.cci.Model_1_3;

//---------------------------------------------------------------------------

/**
 * This class provides utility functions useful for the implementation of
 * PackageExternalizers. This class is abstract, hence the externalize()
 * method must be implemented by a concreted subclass.
 */

public abstract class AbstractMapper_1
  implements Mapper_1_0 {

  //---------------------------------------------------------------------------  
  protected AbstractMapper_1(
    String packageSuffix
  ) {
    this.packageSuffix = packageSuffix;
  }
  
  //---------------------------------------------------------------------------  
  protected void addToZip(
    ZipOutputStream zip,
    ByteArrayOutputStream os,
    ModelElement_1_0 element,
    String entryName,
    String suffix,
    boolean dereferenceType, 
    String packageSuffix
  ) throws ServiceException {

    try {
      String zipEntryName = null;
      if(element == null) {
        zipEntryName = entryName + suffix;
      }
      else {
        String packageName =
          this.model.toJavaPackageName(
            element.path(),
            packageSuffix,
            dereferenceType
          ).replace('.', '/');
        if(packageName.startsWith("/")) {
          packageName = packageName.substring(1);
        }
        zipEntryName = packageName + "/" + entryName + suffix;
      }
      SysLog.trace("adding to jar. element " + zipEntryName);
      ZipEntry zipEntry = new JarEntry(
          zipEntryName
      );
      zipEntry.setSize(os.size());
      zip.putNextEntry(zipEntry);
      os.writeTo(zip);
    }
    catch(IOException e) {
      throw new ServiceException(e);
    }
  }

  //---------------------------------------------------------------------------  
  protected void addToZip(
    ZipOutputStream zip,
    ByteArrayOutputStream os,
    ModelElement_1_0 element,
    String entryName,
    String suffix
  ) throws ServiceException {
    this.addToZip(
      zip,
      os,
      element,
      entryName,
      suffix,
      true, 
      this.packageSuffix
    );
  }
  
  //---------------------------------------------------------------------------  
  public void addToZip(
      ZipOutputStream zip,
      ByteArrayOutputStream os,
      ModelElement_1_0 element,
      String suffix
    ) throws ServiceException {
      this.addToZip(
        zip,
        os,
        element,
        element == null ? "" : (String)element.values("name").get(0),
        suffix
      );
    }
    
  //---------------------------------------------------------------------------  
  public void addToZip(
    ZipOutputStream zip,
    ByteArrayOutputStream os,
    ModelElement_1_0 element,
    String suffix,
    boolean dereferenceType
  ) throws ServiceException {
    this.addToZip(
        zip,
        os,
        element,
        (String)element.values("name").get(0),
        suffix,
        dereferenceType, 
        this.packageSuffix
    );
  }
  
  //--------------------------------------------------------------------------------
  String toJavaObjectType(
    String attributeType
  ) throws ServiceException {

    if("short".equals(attributeType)) {
      return "java.lang.Short";
    }
    else if("long".equals(attributeType)) {
      return "java.lang.Long";
    }
    else if("int".equals(attributeType)) {
      return "java.lang.Integer";
    }
    else if("boolean".equals(attributeType)) {
      return "java.lang.Boolean";
    }
    else {
      return attributeType;
    }
  }

  //--------------------------------------------------------------------------------
  /**
   * Return model packages which match the qualified package name. 
   * qualifiedPackageName may contain % as last character serving as wildcard.
   */
  @SuppressWarnings("unchecked")
protected List<ModelElement_1_0> getMatchingPackages(
    String qualifiedPackageName
  ) throws ServiceException {
    List<ModelElement_1_0> modelPackages = new ArrayList<ModelElement_1_0>();
    for(
      Iterator<ModelElement_1_0> i = this.model.getContent().iterator(); 
      i.hasNext();
    ) {
      ModelElement_1_0 modelElement = i.next();
      if(this.model.isPackageType(modelElement)) {
        boolean wildcard = qualifiedPackageName.indexOf("%") >= 0;
        String pattern = wildcard 
          ? qualifiedPackageName.substring(0, qualifiedPackageName.indexOf("%"))
          : qualifiedPackageName;
        String qualifiedName = (String)modelElement.values("qualifiedName").get(0);
        if(
          (!wildcard && qualifiedName.equals(qualifiedPackageName)) ||
          (wildcard && qualifiedName.startsWith(pattern))
        ) {
          modelPackages.add(modelElement);
        }
      }
    }
    return modelPackages;
  }
  
  //--------------------------------------------------------------------------------
  // Variables
  //--------------------------------------------------------------------------------
  
  /**
   * 
   */
  protected Model_1_3 model = null;
  
  /**
   * 
   */
  protected final String packageSuffix;

}

//--- End of File -----------------------------------------------------------
