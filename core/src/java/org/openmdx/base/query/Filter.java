/*
 * ====================================================================
 * Project:     openMDX/Core, http://www.openmdx.org/
 * Name:        $Id: Filter.java,v 1.17 2009/12/27 23:39:43 wfro Exp $
 * Description: Filter
 * Revision:    $Revision: 1.17 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/12/27 23:39:43 $
 * ====================================================================
 *
 * This software is published under the BSD license
 * as listed below.
 * 
 * Copyright (c) 2004-2008, OMEX AG, Switzerland
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
package org.openmdx.base.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.resource.ResourceException;

import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.resource.Records;
import org.w3c.cci2.AnyTypePredicate;

/**
 * A filter allows to retrieve subsets of filterable maps and to sort
 * its content. A filter consists of a set of (ANDed) filter conditions 
 * and a (ordered) list of order clauses.
 * 
 * The Filter class is bean-compliant. Hence, it can be externalized
 * with the XMLDecoder.
 * 
 * @see java.beans.XMLDecoder
 * @see java.beans.XMLEncoder
 */
public class Filter
    implements Serializable, AnyTypePredicate {

    //-------------------------------------------------------------------------
    /**
     * Constructs an empty filter
     */
    public Filter(
    ) {
        this.conditions = new ArrayList<Condition>();
        this.orderSpecifiers = new ArrayList<OrderSpecifier>();
    }

    //-------------------------------------------------------------------------
    /**
     * Constructs a filter with the specified conditions
     * @param conditions
     */
    public Filter(
        Condition[] conditions
    ) {
        this();
        this.setCondition(conditions);
    }

    //-------------------------------------------------------------------------
    /**
     * Constructs a filter with the specified conditions and
     * order specifiers.
     */
    public Filter(
        Condition[] conditions,
        OrderSpecifier[] orderSpecifiers
    ) {
        this();
        this.setCondition(conditions);
        this.setOrderSpecifier(orderSpecifiers);
    }

    //-------------------------------------------------------------------------
    public Filter(
         FilterProperty[] filterProperties,
         AttributeSpecifier[] attributeSpecifiers
    ) {
        this();
        if(filterProperties != null) {
            for(FilterProperty p: filterProperties) {
                this.conditions.add(
                    new AnyTypeCondition(p)
                );
            }
        }
        if(attributeSpecifiers != null) {
            for(AttributeSpecifier a: attributeSpecifiers) {
                this.orderSpecifiers.add(
                    new OrderSpecifier(
                        a.name(),
                        a.order()
                    )
                );
            }
        }
    }
    
    //-------------------------------------------------------------------------
    public void addCondition(
        Condition condition
    ) {
        this.conditions.add(condition);
    }
    
    //-------------------------------------------------------------------------
    /**
     * Returns the Condition at the specified position.
     * @param index - index of element to return. 
     * @return the element at the specified position in this list. 
     * @throws 
     *    IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size()).
     */
    public Condition getCondition(
        int index
    ) {
        return this.conditions.get(index);
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the array of filter conditions.
     */
    public Condition[] getCondition(
    ) {
        return this.conditions.toArray(
            new Condition[this.conditions.size()]
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Replaces the condition at the specified position with the specified condition.
     */
    public void setCondition(
        int index,
        Condition condition
    ) {
        this.conditions.set(
            index,
            condition
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Sets the filter conditions to the specified array.
     */
    public void setCondition(
        Condition[] conditions
    ) {
        this.conditions.clear();
        this.conditions.addAll(
            Arrays.asList(conditions)
        );
    }

    //-------------------------------------------------------------------------
    public void addOrderSpecifier(
        OrderSpecifier orderSpecifier
    ) {
        this.orderSpecifiers.add(orderSpecifier);
    }
    
    //-------------------------------------------------------------------------
    /**
     * Returns the order specifier at position index.
     * @param index - index of element to return. 
     * @return the element at the specified position in this list. 
     * @exception IndexOutOfBoundsException - if the index is out of range (index < 0 || index >= size()).
     */
    public OrderSpecifier getOrderSpecifier(
        int index
    ) {
        return this.orderSpecifiers.get(index);
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the list of order specifiers.
     */
    public OrderSpecifier[] getOrderSpecifier(
    ) {
        return this.orderSpecifiers.toArray(
            new OrderSpecifier[this.orderSpecifiers.size()]
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Replaces the order specifier at the specified position.
     */
    public void setOrderSpecifier(
        int index,
        OrderSpecifier orderSpecifier
    ) {
        this.orderSpecifiers.set(
            index,
            orderSpecifier
        );
    }

    //-------------------------------------------------------------------------
    /**
     * Sets the order specifier to the specified array.
     */
    public void setOrderSpecifier(
        OrderSpecifier[] orderSpecifiers
    ) {
        this.orderSpecifiers.clear();
        this.orderSpecifiers.addAll(
            Arrays.asList(orderSpecifiers)
        );
    }
     
    //-------------------------------------------------------------------------
    public String toString(
    ) {
        try {
            return Records.getRecordFactory().asMappedRecord(
                getClass().getName(),
                null,
                TO_STRING_FIELDS, 
                new Object[]{
                    Records.getRecordFactory().asIndexedRecord(
                        Condition.class.getName(),
                        null,
                        this.conditions
                    ),
                    Records.getRecordFactory().asIndexedRecord(
                        OrderSpecifier.class.getName(),
                        null,
                        this.orderSpecifiers
                    )
                }
            ).toString();
        } catch (ResourceException exception) {
            return super.toString();
        }
    }

    //-------------------------------------------------------------------------
    // Implements AnyTypePredicate
    //-------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(java.lang.Object[])
     */
    public void elementOf(Object... operands) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#elementOf(java.util.Collection)
     */
    public void elementOf(Collection<?> operands) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#equalTo(java.lang.Object)
     */
    public void equalTo(Object operand) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(java.lang.Object[])
     */
    public void notAnElementOf(Object... operands) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notAnElementOf(java.util.Collection)
     */
    public void notAnElementOf(Collection<?> operands) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see org.w3c.cci2.AnyTypePredicate#notEqualTo(java.lang.Object)
     */
    public void notEqualTo(Object operand) {
        throw new UnsupportedOperationException();
    }

    //-------------------------------------------------------------------------
    public static List<FilterProperty> getFilterProperties(
        Filter filter
    ) throws ServiceException {
        if(filter != null) {
            List<FilterProperty> filterProperties = new ArrayList<FilterProperty>();
            for(Condition condition : filter.getCondition()) {
                filterProperties.add(
                    new FilterProperty(
                        condition.getQuantor(),
                        condition.getFeature(),
                        FilterOperators.fromString(condition.getName()),
                        condition.getValue()
                    )
                );
            }
            return filterProperties;
        }
        return Collections.emptyList();
    }
    
    //-------------------------------------------------------------------------
    // Variables
    //-------------------------------------------------------------------------
    private static final long serialVersionUID = 3257285842266371888L;
    
    private final List<Condition> conditions;
    private final List<OrderSpecifier> orderSpecifiers;
    private static final String[] TO_STRING_FIELDS = {
        "condition",
        "orderSpecifier"
    };
  
}

//--- End of File -----------------------------------------------------------
