/*
 * ====================================================================
 * Project:     openMDX http://www.openmdx.org/
 * Name:        $Id: RequestedList.java,v 1.6 2009/12/14 15:21:32 wfro Exp $
 * Description: RequestedList class
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2009/12/14 15:21:32 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
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
 * This product includes software developed by other organizations as
 * listed in the NOTICE file.
 */
package org.openmdx.application.dataprovider.cci;

import java.io.Serializable;
import java.util.AbstractSequentialList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.openmdx.base.exception.RuntimeServiceException;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.AttributeSpecifier;
import org.openmdx.base.query.Directions;
import org.openmdx.base.query.FilterProperty;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.log.SysLog;

/**
 * Note that the requested list's size return Integer.MAX_VALUE as long as
 * its actual size can't be determined.
 */
public class RequestedList 
    extends AbstractSequentialList<Object>
    implements Serializable, DataproviderReplyListener
{

    /**
     * Constructor
     *
     * @param       iterationProcessor
     *              To get the next bunch of elements
     */ 
    public RequestedList(
        DataproviderRequestProcessor iterationProcessor,
        Path referenceFilter,
        FilterProperty[] attributeFilter,
        short attributeSelector,
        AttributeSpecifier[] attributeSpecifiers
    ) {
        this.iterationProcessor = iterationProcessor;
        this.referenceFilter = referenceFilter;
        this.attributeFilter = attributeFilter;
        this.attributeSelector = attributeSelector;
        this.attributeSpecifiers = attributeSpecifiers;
        this.capacity = Integer.MAX_VALUE;
    }

    protected void onReplyInterceptor(
        DataproviderReply reply
    ){
        //
    }

    //------------------------------------------------------------------------
    // Implements DataproviderReplyListener
    //------------------------------------------------------------------------

    /**
     * Called if the work unit has been processed successfully
     */
    public void onReply(
        DataproviderReply reply
    ){
        onReplyInterceptor(reply);
        this.exception = null;
        // Extract capacity from reply
        this.capacity = reply.getObjects().length;
        // Extract size from reply
        this.size = reply.getTotal() != null ? 
            total(reply) : 
                Integer.MAX_VALUE;
        // Save initial reply for iterators
        this.initialReply = reply;
    }

    /**
     * Called if the work unit processing failed
     */
    public void onException(
        ServiceException exception
    ){
        this.initialReply = null;
        this.exception = exception;
    }

    //------------------------------------------------------------------------
    // Class Methods
    //------------------------------------------------------------------------

    static boolean hasMore(
        DataproviderReply reply
    ){        
        return reply.getHasMore().booleanValue();
    }

    static int total(
        DataproviderReply reply
    ){
        Number value = reply.getTotal();
        if(value == null) SysLog.warning(
            "'total' context without value",
            reply
        );
        return value == null ? Integer.MAX_VALUE : value.intValue();
    }

    //------------------------------------------------------------------------
    // Instance Members
    //------------------------------------------------------------------------

    /**
     * The exception if the request fails; null otherwise.
     */
    transient protected ServiceException exception;

    /**
     *
     */
    protected DataproviderRequestProcessor iterationProcessor;

    /**
     * The initial reply if the request succeeds; null otherwise.
     */
    transient protected DataproviderReply initialReply;

    /**
     * The capacity to be used.
     */ 
    protected int capacity; 

    /**
     * The list's size
     */
    protected int size;

    protected final Path referenceFilter;
    protected final FilterProperty[] attributeFilter;
    protected final short attributeSelector;
    protected final AttributeSpecifier[] attributeSpecifiers;

    //------------------------------------------------------------------------
    // Extends AbstractSequentialList
    //------------------------------------------------------------------------

    /**
     * Returns the number of elements in this list. If this list contains more
     * than Integer.MAX_VALUE elements, returns Integer.MAX_VALUE.
     *
     * @return      the number of elements in this list;
     *              or -1 if it is unknown
     *
     * @exception   RuntimeServiceException
     *              if size is not available
     */
    public int size(
    ){
        if(this.exception != null) throw new RuntimeServiceException(
            this.exception
        );
        return this.size;
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list. The
     * specified index indicates the first element that would be returned
     * by an initial call to the next method. An initial call to the
     * previous method would return the element with the specified index
     * minus one.
     *
     * @param       index
     *              index of first element to be returned from the list
     *              iterator (by a call to the next method).
     *
     * @return      a list iterator of the elements in this list (in
     *              proper sequence), starting at the specified position
     *              in this list.
     *
     * @exception   IndexOutOfBoundsException
     *              if the index is out of range (index < 0 || index >
     *              size()).
     */
    public ListIterator<Object> listIterator(
        int index
    ){
        return new BufferingIterator(
            index
        );
    }

    //------------------------------------------------------------------------
    // BufferingIterator
    //------------------------------------------------------------------------
    protected class BufferingIterator 
        implements Serializable, ListIterator<Object>, DataproviderReplyListener
    {

        /**
         * 
         */
        private static final long serialVersionUID = 3257850995454523702L;
        public BufferingIterator(
            int index
        ){
            this.reply = RequestedList.this.initialReply;
            this.previousIndex = index - 1;
            this.currentIndex = -1;
            this.nextIndex = index;
            this.iterator = // throw IndexOutOfBoundsException if necessary
                this.reply == null || (
                    index >= this.reply.getObjects().length && 
                    hasMore(this.reply)
                ) ? 
                    cache(this.nextIndex, Directions.ASCENDING) :
                        Arrays.asList((Object[])this.reply.getObjects()).listIterator(index);
        }

        /**
         * Called if the work unit has been processed successfully
         */
        public void onReply(
            DataproviderReply reply
        ){
            onReplyInterceptor(reply);
            RequestedList.this.exception = null;
            this.reply = reply;
            if(reply.getTotal() != null){
                int oldValue = size;
                int newValue = total(reply);
                if(newValue != Integer.MAX_VALUE) {
                    if (oldValue != Integer.MAX_VALUE && oldValue != newValue){
                        ServiceException exception = new ServiceException(
                            BasicException.Code.DEFAULT_DOMAIN,
                            BasicException.Code.ASSERTION_FAILURE,
                            "Dataprovider changed the list's size",
                            new BasicException.Parameter("oldSize", oldValue),
                            new BasicException.Parameter("newSize", size)
                        );
                        SysLog.error(
                            exception.getMessage(),
                            exception.getCause()
                        ); // This pattern should be supported by Assertions
                    }
                    size = newValue;
                }
            }
        }

        /**
         * Called if the work unit processing failed
         */
        public void onException(
            ServiceException exception
        ){
            this.reply = null;
            RequestedList.this.exception = exception;
        }

        private ListIterator<Object> cache(
            int position,
            short direction
        ){
            try {
                RequestedList.this.iterationProcessor.addFindRequest(
                    RequestedList.this.referenceFilter,
                    RequestedList.this.attributeFilter,
                    RequestedList.this.attributeSelector,
                    RequestedList.this.attributeSpecifiers,
                    position,
                    RequestedList.this.capacity,
                    direction,
                    this
                );
                return Arrays.asList(
                    (Object[])reply.getObjects()
                ).listIterator(
                    direction == Directions.ASCENDING ? 0 : reply.getObjects().length
                );
            } catch (ServiceException exception) {
                throw new RuntimeServiceException(exception);
            }
        }

        /**
         * Returns true if this list iterator has more elements when
         * traversing the list in the forward direction. (In other words,
         * returns true if next would return an element rather than throwing
         * an exception.)
         *
         * @return      true if the list iterator has more elements when
         *              traversing the list in the forward direction.
         */
        public boolean hasNext(
        ){
            return this.iterator.hasNext() || hasMore(this.reply);
        }

        /**
         * Returns the next element in the list. This method may be called
         * repeatedly to iterate through the list, or intermixed with calls to
         * previous to go back and forth. (Note that alternating calls to next
         * and previous will return the same element repeatedly.)
         *
         * @return      the next element in the list.
         *
         * @exception   NoSuchElementException
         *              if the iteration has no next element.
         */
        public Object next(
        ){
            if (! this.iterator.hasNext()) this.iterator = cache(
                this.nextIndex,
                Directions.ASCENDING
            );
            this.currentIndex = this.previousIndex = this.nextIndex;
            this.nextIndex++; 
            return this.iterator.next();
        }

        /**
         * Returns true if this list iterator has more elements when
         * traversing the list in the reverse direction. (In other words,
         * returns true if previous would return an element rather than
         * throwing an exception.)
         *
         * @return      true if the list iterator has more elements when
         *              traversing the list in the reverse direction.
         */
        public boolean hasPrevious(
        ){
            return this.previousIndex >= 0;
        }

        /**
         * Returns the previous element in the list. This method may be called
         * repeatedly to iterate through the list backwards, or intermixed
         * with calls to next to go back and forth. (Note that alternating
         * calls to next and previous will return the same element
         * repeatedly.)
         *
         * @return      the previous element in the list.
         *
         * @exception   NoSuchElementException
         *              if the iteration has no previous element.
         */
        public Object previous(
        ){
            if (! this.iterator.hasPrevious()) this.iterator = cache(
                this.previousIndex,
                Directions.DESCENDING
            );
            this.currentIndex = this.nextIndex = this.previousIndex;
            this.previousIndex--; 
            return this.iterator.previous();
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to next. (Returns list size if the list iterator is
         * at the end of the list.)
         *
         * @return      the index of the element that would be returned by a
         *              subsequent call to next, or list size if list iterator
         *              is at end of list.
         */
        public int nextIndex(
        ){
            return this.nextIndex;
        }

        /**
         * Returns the index of the element that would be returned by a
         * subsequent call to previous. (Returns -1 if the list iterator is at
         * the beginning of the list.)
         *
         * @return      the index of the element that would be returned by a
         *              subsequent call to previous, or -1 if list iterator is
         *              at beginning of list.
         */
        public int previousIndex(
        ){
            return this.previousIndex;
        }

        /**
         * Removes from the list the last element that was returned by next or
         * previous (optional operation). This call can only be made once per
         * call to next or previous. It can be made only if ListIterator.add
         * has not been called after the last call to next or previous.
         *
         * @exception   UnsupportedOperationException
         *              if the remove operation is not supported by this list
         *              iterator.
         * @exception   IllegalStateException
         *              neither next nor previous have been called, or remove
         *              or add have been called after the last call to
         *              next or previous.
         */
        public void remove(
        ){
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * Replaces the last element returned by next or previous with the
         * specified element (optional operation). This call can be made only
         * if neither ListIterator.remove nor ListIterator.add have been
         * called after the last call to next or previous.
         *
         * @param       o
         *              the element with which to replace the last element
         *              returned by next or previous.
         *
         * @exception   UnsupportedOperationException
         *              if the set operation is not supported by this list
         *              iterator.
         * @exception   ClassCastException
         *              if the class of the specified element prevents it from
         *              being added to this list.
         * @exception   IllegalArgumentException
         *              if some aspect of the specified element prevents it
         *              from being added to this list.
         * @exception   IllegalStateException
         *              if neither next nor previous have been called, or
         *              remove or add have been called after the last call to
         *              next or previous.
         */
        public void set(
            Object o
        ){
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        /**
         * Inserts the specified element into the list (optional operation).
         * The element is inserted immediately before the next element that
         * would be returned by next, if any, and after the next element that
         * would be returned by previous, if any. (If the list contains no
         * elements, the new element becomes the sole element on the list.)
         * The new element is inserted before the implicit currentIndex: a
         * subsequent call to next would be unaffected, and a subsequent call
         * to previous would return the new element. (This call increases by
         * one the value that would be returned by a call to nextIndex or
         * previousIndex.)
         *
         * @param       o
         *              the element to insert.
         *
         * @exception   UnsupportedOperationException
         *              if the add method is not supported by this list
         *              iterator.
         * @exception   ClassCastException
         *              if the class of the specified element prevents it from
         *              being added to this Set.
         * @exception   IllegalArgumentException
         *              if some aspect of this element prevents it from being
         *              added to this Collection.
         */
        public void add(
            Object o
        ){
            throw new UnsupportedOperationException(UNMODIFIABLE);
        }

        final static String UNMODIFIABLE = "This list is unmodifiable";

        DataproviderReply reply;  
        ListIterator<Object> iterator;
        int previousIndex;
        int currentIndex;
        int nextIndex;

    }
    
    //-----------------------------------------------------------------------
    // Members
    //-----------------------------------------------------------------------
    private static final long serialVersionUID = 3257290248869721908L;

}