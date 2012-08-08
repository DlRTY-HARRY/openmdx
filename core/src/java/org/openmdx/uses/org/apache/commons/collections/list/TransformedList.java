/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openmdx.uses.org.apache.commons.collections.list;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.openmdx.uses.org.apache.commons.collections.Transformer;
import org.openmdx.uses.org.apache.commons.collections.collection.TransformedCollection;
import org.openmdx.uses.org.apache.commons.collections.iterators.AbstractListIteratorDecorator;

/**
 * Decorates another <code>List</code> to transform objects that are added.
 * <p>
 * The add and set methods are affected by this class.
 * Thus objects must be removed or searched for using their transformed form.
 * For example, if the transformation converts Strings to Integers, you must
 * use the Integer form to remove objects.
 * <p>
 * This class is Serializable from Commons Collections 3.1.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 1.6 $ $Date: 2008/06/28 00:21:22 $
 * 
 * @author Stephen Colebourne
 */
@SuppressWarnings("unchecked")
public class TransformedList extends TransformedCollection implements List {

    /** Serialization version */
    private static final long serialVersionUID = 1077193035000013141L;

    /**
     * Factory method to create a transforming list.
     * <p>
     * If there are any elements already in the list being decorated, they
     * are NOT transformed.
     * 
     * @param list  the list to decorate, must not be null
     * @param transformer  the transformer to use for conversion, must not be null
     * @throws IllegalArgumentException if list or transformer is null
     */
    public static List decorate(List list, Transformer transformer) {
        return new TransformedList(list, transformer);
    }
    
    //-----------------------------------------------------------------------
    /**
     * Constructor that wraps (not copies).
     * <p>
     * If there are any elements already in the list being decorated, they
     * are NOT transformed.
     * 
     * @param list  the list to decorate, must not be null
     * @param transformer  the transformer to use for conversion, must not be null
     * @throws IllegalArgumentException if list or transformer is null
     */
    protected TransformedList(List list, Transformer transformer) {
        super(list, transformer);
    }

    /**
     * Gets the decorated list.
     * 
     * @return the decorated list
     */
    protected List getList() {
        return (List) collection;
    }

    //-----------------------------------------------------------------------
    public Object get(int index) {
        return getList().get(index);
    }

    public int indexOf(Object object) {
        return getList().indexOf(object);
    }

    public int lastIndexOf(Object object) {
        return getList().lastIndexOf(object);
    }

    public Object remove(int index) {
        return getList().remove(index);
    }

    //-----------------------------------------------------------------------
    public void add(int index, Object _object) {
        Object object = transform(_object);
        getList().add(index, object);
    }

    public boolean addAll(int index, Collection _coll) {
        Collection coll = transform(_coll);
        return getList().addAll(index, coll);
    }

    public ListIterator listIterator() {
        return listIterator(0);
    }

    public ListIterator listIterator(int i) {
        return new TransformedListIterator(getList().listIterator(i));
    }

    public Object set(int index, Object _object) {
        Object object = transform(_object);
        return getList().set(index, object);
    }

    public List subList(int fromIndex, int toIndex) {
        List sub = getList().subList(fromIndex, toIndex);
        return new TransformedList(sub, transformer);
    }

    /**
     * Inner class Iterator for the TransformedList
     */
    protected class TransformedListIterator extends AbstractListIteratorDecorator {
        
        protected TransformedListIterator(ListIterator iterator) {
            super(iterator);
        }
        
        @SuppressWarnings("synthetic-access")
        public void add(Object _object) {
            Object object = transform(_object);
            iterator.add(object);
        }
        
        @SuppressWarnings("synthetic-access")
        public void set(Object _object) {
            Object object = transform(_object);
            iterator.set(object);
        }
    }

}
