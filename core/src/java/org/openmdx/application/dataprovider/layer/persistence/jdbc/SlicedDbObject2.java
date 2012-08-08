/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: SlicedDbObject2.java,v 1.11 2011/11/26 01:34:56 hburger Exp $
 * Description: Sliced DB Object Indexed
 * Revision:    $Revision: 1.11 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:56 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.layer.persistence.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;

import javax.resource.cci.MappedRecord;

import org.openmdx.base.accessor.cci.SystemAttributes;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.mof.cci.ModelElement_1_0;
import org.openmdx.base.mof.cci.ModelHelper;
import org.openmdx.base.naming.Path;
import org.openmdx.base.rest.spi.Facades;
import org.openmdx.base.rest.spi.Object_2Facade;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;
import org.w3c.cci2.SparseArray;

/**
 * Sliced DB Object eliminating Non-used multi-value slices
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class SlicedDbObject2 extends SlicedDbObject {

    /**
     * Constructor 
     *
     * @param database
     * @param conn
     * @param dbObjectConfiguration
     * @param accessPath
     * @param isExtent
     * @param isQuery
     * @throws ServiceException
     */
    public SlicedDbObject2(
        AbstractDatabase_1 database,
        Connection conn,
        DbObjectConfiguration dbObjectConfiguration,
        Path accessPath,
        boolean isExtent,
        boolean isQuery
    ) throws ServiceException {
        super(database, conn, dbObjectConfiguration, accessPath, isExtent, isQuery);
    }

    /* (non-Javadoc)
     * @see org.openmdx.application.dataprovider.layer.persistence.jdbc.StandardDbObject#createReferenceClause()
     */
    @Override
    protected String createReferenceClause(
        List<String> referenceColumns, 
        List<Object> referenceValues, 
        boolean extentQuery
    ) throws ServiceException {
        String original = super.createReferenceClause(referenceColumns, referenceValues, extentQuery);
        if(this.database.configuration.useViewsForRedundantColumns()) {
            String removableReferenceIdPrefix = this.dbObjectConfiguration.getRemovableReferenceIdPrefix();
            if(
                removableReferenceIdPrefix != null &&
                referenceColumns.size() == 1 &&
                referenceValues.size() == 1
            ){
                Object rid = referenceValues.get(0);
                if(rid instanceof String) {
                    String referenceColumn = this.database.toRsx(this.database.getPrivateAttributesPrefix() + "object");
                    String referenceValue = (String) rid;
                    if(referenceValue.startsWith(removableReferenceIdPrefix)) {
                        referenceValue = referenceValue.substring(removableReferenceIdPrefix.length());
                        referenceColumns.set(0, referenceColumn);
                        referenceValues.set(0, referenceValue);
                        return this.referenceClause = "(v." + referenceColumn + " = ? )";
                    }
                }
                
            }
        }
        return original; 
    }

    /**
     * Constructor 
     *
     * @param database
     * @param conn
     * @param typeConfiguration

     * @throws ServiceException 
     */
    public SlicedDbObject2(
        AbstractDatabase_1 database,
        Connection conn,
        DbObjectConfiguration typeConfiguration
    ) throws ServiceException {
        super(database, conn, typeConfiguration);
    }

    /**
     * Implements <code>Serializable</code>
     */
    private static final long serialVersionUID = -211489817333529686L;

    /**
     * Create
     * 
     * @param index
     * @param object
     * @param objectClass
     * @param referenceIdColumns
     * @param referenceIdValues
     * @param objectIdColumns
     * @param objectIdValues
     * @param excludeAttributes
     * @throws ServiceException
     */
    @Override
    protected void createObject(
        int index,
        MappedRecord object,
        String objectClass,
        List referenceIdColumns,
        List referenceIdValues,
        List objectIdColumns,
        List objectIdValues,
        Set excludeAttributes
    ) throws ServiceException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        String currentStatement = null;
        List statementParameters = new ArrayList();
        List statementParameterTypes = new ArrayList();
        List processedColumns = new ArrayList();
        Set processedAttributes = new HashSet();
        if(excludeAttributes != null) {
            processedAttributes.addAll(excludeAttributes);
        }
        Object_2Facade facade= Facades.asObject(object);
        try {        
            // Single-valued attributes at index 0 are stored in primary dbObject, multi-valued
            // attributes in secondary dbObjects. Multi-valued attributes (index > 0) are always
            // stored in secondary dbObject.
            List dbObjects = new ArrayList();
            boolean processAsSecondary;
            if(index > 0) {
                dbObjects.add(
                    this.getConfiguration().getDbObjectForUpdate2() == null ? 
                        this.getConfiguration().getDbObjectForUpdate1() : 
                        this.getConfiguration().getDbObjectForUpdate2()
                );
                processAsSecondary = true;
            }
            else {
                dbObjects.add(
                    this.getConfiguration().getDbObjectForUpdate1()
                );
                if(this.getConfiguration().getDbObjectForUpdate2() != null) {
                    dbObjects.add(
                        this.getConfiguration().getDbObjectForUpdate2()
                    );
                }
                processAsSecondary = false;
            }
            // Process all db objects
            for(
                Iterator d = dbObjects.iterator();
                d.hasNext();
                processAsSecondary = true
            ) {
                String dbObject = (String)d.next();            
                Set dbObjectColumns = this.getDbObjectColumns(dbObject);
                String statement = "INSERT INTO " + dbObject + " (";
                statementParameters.clear();
                int k = 0;      
                // All attributes for dbObject
                for(
                    Iterator i = facade.getValue().keySet().iterator(); 
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    if((excludeAttributes == null || !excludeAttributes.contains(attributeName)) && (attributeName.indexOf(':') < 0)) {
                        Object attributeValues = facade.attributeValues(attributeName);
                        if(
                            (attributeValues instanceof Collection && ((Collection)attributeValues).isEmpty()) ||
                            (attributeValues instanceof SparseArray && ((SparseArray)attributeValues).isEmpty())
                        ) {
                            String columnName = this.database.getColumnName(
                                this.conn, 
                                attributeName, 
                                0, 
                                false, 
                                true, 
                                false // markAsPrivate
                            );
                            if(dbObjectColumns.contains(columnName)) {
                                processedAttributes.add(
                                    attributeName
                                );
                            }                      
                        }
                        else {
                            ListIterator j = attributeValues instanceof SparseArray ?
                                ((SparseArray)attributeValues).populationIterator() :
                                    ((List)attributeValues).listIterator();
                            while(j.hasNext()) {
                                int valIndex = j.nextIndex();
                                Object value = j.next();
                                String columnName = this.database.getColumnName(
                                    this.conn, 
                                    attributeName, 
                                    valIndex, 
                                    false, 
                                    true, 
                                    false // markAsPrivate
                                );
                                if(dbObjectColumns.contains(columnName)) {
                                    if(!columnName.equals(this.database.OBJECT_IDX)) {
                                        if(k > 0) statement += ", ";
                                        statement += this.database.getColumnName(
                                            this.conn, 
                                            attributeName, 
                                            valIndex, 
                                            false, 
                                            false, // escape reserved words
                                            false // markAsPrivate
                                        );
                                        statementParameters.add(
                                            this.database.externalizeStringValue(columnName, value)
                                        );
                                        statementParameterTypes.add(
                                            value.getClass().getName()
                                        );
                                        processedColumns.add(
                                            columnName
                                        );
                                        k++;
                                    }
                                    processedAttributes.add(attributeName);
                                }
                            }
                        }
                    }
                    else {
                        processedAttributes.add(
                            attributeName
                        );                    
                    }
                }
                // Add autonum columns for slice 0
                List autonumColumns = null;
                List autonumValues = null;
                boolean hasParameters = !statementParameters.isEmpty();
                if(index == 0 && !processAsSecondary) {
                    for(
                        Iterator i = this.getConfiguration().getAutonumColumns().iterator();
                        i.hasNext();
                    ) {
                        String autonumColumn = (String)i.next();  
                        String autonumColumnName = autonumColumn;
                        // USETYPE allows to include type in sequence name
                        int posTyped = autonumColumn.indexOf(" TYPED ");
                        // AS <format> allows to cast/format the sequence number
                        int posAs = autonumColumn.indexOf(" AS ");
                        autonumColumnName = autonumColumnName.indexOf(" ") > 0 ?
                            autonumColumnName.substring(0, autonumColumnName.indexOf(" ")).trim() :
                            autonumColumnName.trim();
                        // Only add if not supplied explicitly as attribute
                        if(!processedColumns.contains(autonumColumnName)) {
                            if(autonumColumns == null) {
                                autonumColumns = new ArrayList();
                            }
                            autonumColumns.add(
                                autonumColumnName
                            );
                            String sequenceName = this.database.namespaceId + "_" + autonumColumnName;
                            if(posTyped > 0) {
                                sequenceName += "_" + this.getConfiguration().getTypeName();
                            }
                            String autonumValue = this.database.getAutonumValue(
                                this.conn,
                                sequenceName,
                                posAs > 0 ? 
                                    autonumColumn.substring(posAs) : 
                                    null
                            );
                            // Emulate SQL sequence
                            if(autonumValue == null) {
                                String normalizedSequenceName = sequenceName.toUpperCase() + "_SEQ";                                
                                ps = this.database.prepareStatement(
                                    this.conn,
                                    currentStatement = "SELECT nextval FROM " + normalizedSequenceName
                                );
                                rs = ps.executeQuery();
                                if(rs.next()) {
                                    autonumValue = rs.getString("nextval");
                                    rs.close();
                                    ps.close();
                                    ps = this.database.prepareStatement(
                                        this.conn,
                                        currentStatement = "UPDATE " + normalizedSequenceName + " SET nextval = nextval + 1"
                                    );
                                    this.database.executeUpdate(ps, currentStatement, Collections.EMPTY_LIST);
                                    ps.close();
                                }
                                else {
                                    autonumValue = "0";
                                    ps = this.database.prepareStatement(
                                        this.conn,
                                        currentStatement = "INSERT INTO " + normalizedSequenceName + " (nextval) VALUES (0)"
                                    );                          
                                    this.database.executeUpdate(ps, currentStatement, Collections.EMPTY_LIST);
                                    ps.close();
                                }
                            }
                            if(autonumValues == null) {
                                autonumValues = new ArrayList();                   
                            }
                            autonumValues.add(autonumValue);
                        }
                    }
                }  
                if(hasParameters || !processAsSecondary) {
                    // typeName
                    String columnNameTypeName = this.database.getPrivateAttributesPrefix() + COLUMN_TYPE_NAME;
                    if(dbObjectColumns.contains(columnNameTypeName)) {
                        if(k > 0) statement += ", ";
                        statement += columnNameTypeName;
                        statementParameters.add(
                            this.database.getObjectId(
                                this.getReference().getBase()
                            )
                        );
                        k++;
                    }
                    // objectClass
                    if(
                        !processedAttributes.contains(SystemAttributes.OBJECT_CLASS) && (
                                index == 0 || this.getConfiguration().getDbObjectForUpdate2() != null
                        )
                    ){
                        String columnNameObjectClass = this.database.getColumnName(
                            this.conn, 
                            SystemAttributes.OBJECT_CLASS, 
                            0, 
                            false, 
                            true, 
                            false // markAsPrivate
                        );
                        if(dbObjectColumns.contains(columnNameObjectClass)) {
                            if(k > 0) statement += ", ";
                            statement += columnNameObjectClass;
                            statementParameters.add(objectClass);
                            k++;
                            processedAttributes.add(
                                SystemAttributes.OBJECT_CLASS
                            );                    
                        }                
                    }
                    // rid
                    if(referenceIdColumns != null) {
                        for(Iterator i = referenceIdColumns.iterator(); i.hasNext(); ) {
                            if(k > 0) statement += ", ";
                            statement += i.next();
                            k++;
                        }
                        if(!referenceIdColumns.isEmpty()) {
                            statementParameters.addAll(referenceIdValues);
                        }
                    }      
                    // oid
                    if(objectIdColumns != null) {
                        for(Iterator i = objectIdColumns.iterator(); i.hasNext(); ) {
                            if(k > 0) statement += ", ";
                            statement += i.next();
                            k++;
                        }
                        if(!objectIdColumns.isEmpty()) {
                            statementParameters.addAll(objectIdValues);
                        }
                    }
                    // idx
                    if(this.getIndexColumn() != null) {
                        if(k > 0) statement += ", ";
                        statement += this.getIndexColumn();
                        statementParameters.add(Integer.valueOf(index));
                        k++;
                    }
                    // secondary tables require index
                    else if(processAsSecondary) {
                        if(k > 0) statement += ", ";
                        statement += this.database.OBJECT_IDX;
                        statementParameters.add(Integer.valueOf(index));
                        k++;
                    }
                    // autonum columns
                    if(autonumColumns != null) {
                        for(Iterator i = autonumColumns.iterator(); i.hasNext(); ) {
                            statement += ", ";
                            statement += i.next();
                        }
                    }          
                    statement += ")";
    
                    // VALUE placeholders
                    statement += " VALUES (";
                    for(int i = 0; i < k; i++) {
                        if(i > 0) {
                            statement += ", ";
                        }
                        statement += this.database.getPlaceHolder(conn, statementParameters.get(i));
                    }
                    // autonum values
                    if(autonumValues != null) {
                        for(Iterator i = autonumValues.iterator(); i.hasNext(); ) {
                            statement += ", ";
                            statement += i.next();
                        }          
                    }
                    statement += ")";          
                    // prepare
                    ps = this.database.prepareStatement(
                        this.conn, 
                        currentStatement = statement.toString()
                    );
    
                    // fill in values
                    for(int i = 0; i < statementParameters.size(); i++) {
                        Object value = statementParameters.get(i);
                        this.database.setPreparedStatementValue(
                            this.conn,
                            ps,
                            i+1,
                            value
                        );    
                    }    
                    this.database.executeUpdate(ps, currentStatement, statementParameters);
                    ps.close(); ps = null;
                }
            }
            if(!processedAttributes.containsAll(facade.getValue().keySet())) {
                Set nonProcessedAttributes = new HashSet(facade.getValue().keySet());
                nonProcessedAttributes.removeAll(processedAttributes);
                throw new ServiceException(
                    BasicException.Code.DEFAULT_DOMAIN,
                    BasicException.Code.MEDIA_ACCESS_FAILURE, 
                    "Attributes can not be stored. Missing columns in db objects",
                    new BasicException.Parameter("object", object),
                    new BasicException.Parameter("processed attributes", processedAttributes),
                    new BasicException.Parameter("non-processed attributes", nonProcessedAttributes),
                    new BasicException.Parameter("db objects", dbObjects)
                );                
            }                              
        }
        catch(SQLException ex) {
            String sqlState = ex.getSQLState();
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                "23000".equals(sqlState) || "23505".equals(sqlState) ? BasicException.Code.DUPLICATE : BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", facade.getPath()),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("values", statementParameters),
                new BasicException.Parameter("types", statementParameterTypes),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                new BasicException.Parameter("sqlState", sqlState)
            );
        }
        finally {
            try {
                if(rs != null) rs.close();
                if(ps != null) ps.close();
            } catch(Throwable ex) {
                // ignore
            }
        }
    }

    /**
     * Replace
     * 
     * @param index
     * @param newObject
     * @param oldObject
     * @param referenceIdColumns
     * @param referenceIdValues
     * @param objectIdColumns
     * @param objectIdValues
     * @param excludeAttributes
     * @param lockAssertion
     * @throws ServiceException
     */
    @Override
    protected void replaceObjectSlice(
        int index,
        MappedRecord newObject,
        MappedRecord oldObject,
        List referenceIdColumns,
        List referenceIdValues,
        List objectIdColumns,
        List objectIdValues,
        Set excludeAttributes, 
        String lockAssertion
    ) throws ServiceException {
        PreparedStatement ps = null;
        String currentStatement = null;
        List statementParameters = new ArrayList();
        Set statementColumns = new HashSet();
        Set processedAttributes = new HashSet();
        if(excludeAttributes != null) {
            processedAttributes.addAll(excludeAttributes);
        }
        if(this.excludeAttributes != null) {
            processedAttributes.addAll(this.excludeAttributes);
        }            
        SysLog.detail("Processed attributes", processedAttributes);
        Object_2Facade newObjectFacade = Facades.asObject(newObject);
        Object_2Facade oldObjectFacade = Facades.asObject(oldObject);
        try {
            // Single-valued attributes at index 0 are stored in primary dbObject, multi-valued
            // attributes in secondary dbObjects. Multi-valued attributes (index > 0) are always
            // stored in secondary dbObject.
            List dbObjects = new ArrayList();
            boolean processAsSecondary;
            if(index > 0) {
                dbObjects.add(
                    this.getConfiguration().getDbObjectForUpdate2() == null
                    ? this.getConfiguration().getDbObjectForUpdate1()
                        : this.getConfiguration().getDbObjectForUpdate2()
                );
                processAsSecondary = true;
            }
            else {
                dbObjects.add(
                    this.getConfiguration().getDbObjectForUpdate1()
                );
                if(this.getConfiguration().getDbObjectForUpdate2() != null) {
                    dbObjects.add(
                        this.getConfiguration().getDbObjectForUpdate2()
                    );
                }
                processAsSecondary = false;
            }
            // Process all db objects
            for(
                Iterator d = dbObjects.iterator();
                d.hasNext();
                processAsSecondary = true
            ) {
                String dbObject = (String)d.next();
                Set dbObjectColumns = this.getDbObjectColumns(dbObject);
                String statement = "UPDATE " + dbObject + " SET ";
                String statement2 = processAsSecondary ? "INSERT INTO " + dbObject + " (" : null;
                statementParameters.clear();
                boolean hasParameters = false;
                // newValues
                for(
                    Iterator i = newObjectFacade.getValue().keySet().iterator(); 
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    if(((excludeAttributes == null) || !excludeAttributes.contains(attributeName)) && attributeName.indexOf(':') < 0) {
                        List attributeValues = newObjectFacade.attributeValuesAsList(attributeName);   
                        if(attributeValues.isEmpty()) {
                            String columnName = this.database.getColumnName(this.conn, attributeName, 0, false, true, false);
                            if(dbObjectColumns.contains(columnName)) {
                                processedAttributes.add(attributeName);
                            }                      
                        }
                        else {
                            for(
                                ListIterator j = attributeValues.listIterator(); 
                                j.hasNext(); 
                            ) {
                                int valIndex = j.nextIndex();
                                Object value = j.next();
                                String columnName = this.database.getColumnName(this.conn, attributeName, valIndex, false, true, false);
                                if(dbObjectColumns.contains(columnName)) {
                                    if(!columnName.equals(this.database.OBJECT_IDX)) {
                                        String clause = hasParameters ? ", " : "";
                                        clause += this.database.getColumnName(this.conn, attributeName, valIndex, false, false, false);
                                        statement += clause;
                                        statement += " = ";
                                        statement += this.database.getPlaceHolder(this.conn, value); 
                                        if(processAsSecondary) statement2 += clause;
                                        statementColumns.add(columnName);
                                        statementParameters.add(
                                            this.database.externalizeStringValue(columnName, value)
                                        );
                                        hasParameters = true;
                                    }
                                    processedAttributes.add(
                                        attributeName
                                    );                            
                                }
                            }
                        }
                    }
                    else {
                        processedAttributes.add(
                            attributeName
                        );                    
                    }
                }
                // NULL oldValues
                for(
                    Iterator i = oldObjectFacade.getValue().keySet().iterator(); 
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    if(((excludeAttributes == null) || !excludeAttributes.contains(attributeName)) && attributeName.indexOf(':') < 0) {
                        List attributeValues = oldObjectFacade.attributeValuesAsList(attributeName);
                        if(attributeValues.isEmpty()) {
                            String columnName = this.database.getColumnName(conn, attributeName, 0, false, true, false);
                            if(dbObjectColumns.contains(columnName)) {
                                processedAttributes.add(attributeName);
                            }                      
                        }
                        else {
                            for(
                                ListIterator j = attributeValues.listIterator(); 
                                j.hasNext(); 
                            ) {
                                int valIndex = j.nextIndex();
                                String columnName = this.database.getColumnName(conn, attributeName, valIndex, false, true, false);
                                if(dbObjectColumns.contains(columnName)) {
                                    if(
                                        !columnName.equals(this.database.OBJECT_IDX) && 
                                        !statementColumns.contains(columnName)
                                    ) {
                                        statement += hasParameters ? ", " : "";
                                        statement += columnName + " = NULL";
                                        statementColumns.add(columnName);
                                        hasParameters = true;
                                    }
                                    processedAttributes.add(
                                        attributeName
                                    );                            
                                }
                                j.next();
                            }
                        }
                    }
                    else {
                        processedAttributes.add(
                            attributeName
                        );                    
                    }
                }
                if(hasParameters) {

                    // WHERE
                    statement += " WHERE ";
                    boolean hasClause = false;

                    // rid
                    if(referenceIdColumns != null) {
                        for(Object referenceIdColumn : referenceIdColumns){
                            if(hasClause) statement += " AND ";
                            statement += "(" + referenceIdColumn + " = ?)";
                            if(processAsSecondary) statement2 += ", " + referenceIdColumn;
                            hasClause = true;
                        }
                        if(!referenceIdColumns.isEmpty()) {
                            statementParameters.addAll(referenceIdValues);
                        }
                    }

                    // oid
                    if(objectIdColumns != null) {
                        for(Object objectIdColumn : objectIdColumns) {
                            if(hasClause) statement += " AND ";
                            statement += "(" + objectIdColumn + " = ?)";
                            if(processAsSecondary) statement2 += ", " + objectIdColumn;
                            hasClause = true;
                        }
                        if(!objectIdColumns.isEmpty()) {
                            statementParameters.addAll(objectIdValues);
                        }
                    }                
                    // idx
                    String indexColumn = this.getIndexColumn();
                    if(indexColumn != null) {
                        if(hasClause) statement += " AND ";
                        statement += "(" + indexColumn + " = ?)";
                        if(processAsSecondary) statement2 += ", " + indexColumn;
                        statementParameters.add(Integer.valueOf(index));
                        hasClause = true;
                    }
                    // secondary tables require index
                    else if(processAsSecondary) {
                        if(hasClause) statement += " AND ";
                        statement += "(" + this.database.OBJECT_IDX + " = ?)";
                        statement2 += ", " + this.database.OBJECT_IDX;
                        statementParameters.add(Integer.valueOf(index));
                        hasClause = true;                    
                    }
                    boolean lock = !processAsSecondary && lockAssertion != null;
                    if(lock) {
                        String versionField = AbstractDatabase_1.getVersionField(lockAssertion);
                        Object versionValue = AbstractDatabase_1.getVersionValue(lockAssertion, versionField);
                        statement += getVersionClause(versionField, versionValue);
                        if(versionValue != null) {
                            statementParameters.add(versionValue);
                        }
                    }
                    ps = this.database.prepareStatement(
                        this.conn, 
                        currentStatement = statement
                    );            
                    // fill in values
                    {
                        int ii = 1;
                        for(Object statementParameter : statementParameters) {
                            this.database.setPreparedStatementValue(
                                this.conn,
                                ps,
                                ii++,
                                statementParameter
                            );               
                        }
                    }
                    int rowCount = this.database.executeUpdate(ps, currentStatement, statementParameters);
                    switch(rowCount) {
                        case 0:
                            if(lock) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.CONCURRENT_ACCESS_FAILURE,
                                    "The object has been modified since it was read",
                                    new BasicException.Parameter("path", newObjectFacade.getPath()),
                                    new BasicException.Parameter("assertion", lockAssertion), 
                                    new BasicException.Parameter("sqlStatement", currentStatement),
                                    new BasicException.Parameter("parameters", statementParameters),
                                    new BasicException.Parameter("sqlRowCount", rowCount)
                                );
                            } else if(processAsSecondary)  {
                                ps.close(); 
                                String separator = ") VALUES (";
                                for(Object statementParameter : statementParameters) {
                                    statement2 += separator;
                                    statement2 += this.database.getPlaceHolder(conn, statementParameter); 
                                    separator = ", ";
                                }
                                statement2 += ")";
                                ps = this.database.prepareStatement(
                                    this.conn, 
                                    currentStatement = statement2
                                );
                                int ii = 1;
                                for(Object statementParameter : statementParameters) {
                                    this.database.setPreparedStatementValue(
                                        this.conn,
                                        ps,
                                        ii++,
                                        statementParameter
                                    );               
                                }
                                this.database.executeUpdate(ps, currentStatement, statementParameters);
                            } else {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.NOT_FOUND,
                                    "The object to be updated could not be found",
                                    new BasicException.Parameter("path", newObjectFacade.getPath()),
                                    new BasicException.Parameter("assertion", (String)null), 
                                    new BasicException.Parameter("sqlStatement", currentStatement),
                                    new BasicException.Parameter("parameters", statementParameters),
                                    new BasicException.Parameter("sqlRowCount", rowCount)
                                );
                            }
                            break;
                        case 1:
                            // O.k.
                            break;
                        default:
                            throw new ServiceException(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.ASSERTION_FAILURE,
                                "More than one row has been updated at once",
                                new BasicException.Parameter("path", newObjectFacade.getPath()),
                                new BasicException.Parameter("assertion", lock ? lockAssertion : null), 
                                new BasicException.Parameter("sqlStatement", currentStatement),
                                new BasicException.Parameter("parameters", statementParameters),
                                new BasicException.Parameter("sqlRowCount", rowCount)
                            );
                    }
                    ps.close(); ps = null;
                }
            }
            Set nonProcessedAttributes = new HashSet(newObjectFacade.getValue().keySet());
            nonProcessedAttributes.removeAll(processedAttributes);
            if(true/*!nonProcessedAttributes.isEmpty()*/) {
                ModelElement_1_0 classifierDef = this.getModel().findElement(newObjectFacade.getObjectClass());
                if(classifierDef == null) {
                    SysLog.warning("No classifier definition found", newObject);                    
                }
                for(Iterator<String> i = nonProcessedAttributes.iterator(); i.hasNext(); ) {
                    String attributeName = i.next();
                    ModelElement_1_0 featureDef = classifierDef == null ? 
                        null : 
                            classifierDef.getModel().getFeatureDef(
                            classifierDef, 
                            attributeName, 
                            true // includeSubtypes
                        ); 
                    if(featureDef == null) {
                        SysLog.log(Level.WARNING, "Sys|No feature definition found|{0}#{1}", newObject.getRecordName(), attributeName);
                    }
                    if(featureDef != null && !ModelHelper.isChangeable(featureDef)) {
                        i.remove();
                    }                    
                }
                if(!nonProcessedAttributes.isEmpty()) {
                    throw new ServiceException(
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.MEDIA_ACCESS_FAILURE, 
                        "Some attributes can not be stored. Check for missing columns.",
                        new BasicException.Parameter("object", newObject),
                        new BasicException.Parameter("processed attributes", processedAttributes),
                        new BasicException.Parameter("non-processed attributes", nonProcessedAttributes),
                        new BasicException.Parameter("db objects", dbObjects)
                    );
                }
            }
        }
        catch(SQLException ex) {
            throw new ServiceException(
                ex, 
                BasicException.Code.DEFAULT_DOMAIN,
                BasicException.Code.MEDIA_ACCESS_FAILURE, 
                null,
                new BasicException.Parameter("path", newObjectFacade.getPath()),
                new BasicException.Parameter("statement", currentStatement),
                new BasicException.Parameter("values", statementParameters),
                new BasicException.Parameter("sqlErrorCode", ex.getErrorCode()), 
                new BasicException.Parameter("sqlState", ex.getSQLState())
            );
        }
        finally {
            try {
                if(ps != null) ps.close();
            } catch(Throwable ex) {
                // ignore
            }
        }
    }

    /**
     * Normalize Objetc Paths
     * 
     * @param facade
     * @param normalizedObjectFacade
     * @param removeValuesProvidedByView 
     * @param pathBormalizationLevel
     * 
     * @throws ServiceException  
     */
    @Override
    protected void normalizeObjectPaths(
        Object_2Facade facade,
        int pathNormalizeLevel,
        Object_2Facade normalizedObjectFacade, 
        boolean removeValuesProvidedByView
    ) throws ServiceException {
        // Add (rid,oid) object parent
        if(pathNormalizeLevel > 0) {  
            Path parentObjectPath = facade.getPath().getPrefix(facade.getPath().size()-2);    
            if(parentObjectPath.size() >= 5) {
                String name = this.database.getPrivateAttributesPrefix() + "objectParent";
                normalizedObjectFacade.attributeValuesAsList(this.database.toRid(name)).add(
                    this.database.getReferenceId(
                        conn, 
                        parentObjectPath, 
                        true 
                    )
                );
                normalizedObjectFacade.attributeValuesAsList(this.database.toOid(name)).add(
                    parentObjectPath.getBase()
                );
            }
            // add (rid, oid) for all attributes with values of type path
            if(pathNormalizeLevel > 1) {    
                for(
                    Iterator i = facade.getValue().keySet().iterator();
                    i.hasNext();
                ) {
                    String attributeName = (String)i.next();
                    Object values = facade.attributeValues(attributeName);
                    Object firstValue = null;
                    ListIterator valuesIterator = null;
                    if(values instanceof SparseArray) {
                        SparseArray v = (SparseArray)values;
                        firstValue = v.isEmpty() ? null : v.get(v.firstKey());
                        valuesIterator = v.populationIterator();
                    }
                    else if(values instanceof List) {
                        List v = (List)values;
                        firstValue = v.isEmpty() ? null : v.get(0);     
                        valuesIterator = v.listIterator();
                    }
                    if(firstValue instanceof Path) {
                        while(valuesIterator.hasNext()) {
                            Object v = valuesIterator.next();
                            if(!(v instanceof Path)) {
                                throw new ServiceException(
                                    BasicException.Code.DEFAULT_DOMAIN,
                                    BasicException.Code.ASSERTION_FAILURE, 
                                    "value of attribute expected to be instance of path",
                                    new BasicException.Parameter("attribute", attributeName),
                                    new BasicException.Parameter("value class", (v == null ? "null" : v.getClass().getName())),
                                    new BasicException.Parameter("value", v)
                                );
                            }
                            Path objectPath = (Path)v;
                            String name = this.database.getPrivateAttributesPrefix() + attributeName;
                            if(
                                "core".equals(attributeName) &&
                                this.database.configuration.useViewsForRedundantColumns() &&
                                this.getModel().isSubtypeOf(facade.getObjectClass(), "org:openmdx:base:Aspect")
                            ){
                                normalizedObjectFacade.attributeValuesAsList(this.database.toOid(name)).add(
                                    objectPath.getBase()
                                );
                                if(removeValuesProvidedByView) {
                                    i.remove();
                                }
                            } else {
                                normalizedObjectFacade.attributeValuesAsList(this.database.toRid(name)).add(
                                    this.database.getReferenceId(
                                        this.conn, 
                                        objectPath, 
                                        true
                                    )
                                );
                                normalizedObjectFacade.attributeValuesAsList(this.database.toOid(name)).add(
                                    objectPath.getBase()
                                );
                                // add parent of path value
                                if(pathNormalizeLevel > 2) {
                                    Path parentPath = objectPath.getPrefix(objectPath.size()-2);
                                    if(parentPath.size() >= 5) {
                                        normalizedObjectFacade.attributeValuesAsList(this.database.toRid(name + "Parent")).add(
                                            this.database.getReferenceId(
                                                conn, 
                                                parentPath, 
                                                true
                                            )
                                        );
                                        normalizedObjectFacade.attributeValuesAsList(this.database.toOid(name + "Parent")).add(
                                            parentPath.getBase()
                                        );
                                    }
                                }
                            }
                        }
                    }
                } 
            }
        }
    }
    
}