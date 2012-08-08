/*
 * ====================================================================
 * Project:     openMDX, http://www.openmdx.org/
 * Name:        $Id: AbstractFilter.java,v 1.6 2011/11/26 01:34:57 hburger Exp $
 * Description: Abstract Filter Class
 * Revision:    $Revision: 1.6 $
 * Owner:       OMEX AG, Switzerland, http://www.omex.ch
 * Date:        $Date: 2011/11/26 01:34:57 $
 * ====================================================================
 *
 * This software is published under the BSD license as listed below.
 * 
 * Copyright (c) 2004-2011, OMEX AG, Switzerland
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
package org.openmdx.application.dataprovider.spi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.openmdx.application.dataprovider.cci.FilterProperty;
import org.openmdx.base.naming.Path;
import org.openmdx.base.query.ConditionType;
import org.openmdx.base.query.Filter;
import org.openmdx.base.query.LenientPathComparator;
import org.openmdx.base.query.Quantifier;
import org.openmdx.base.query.Selector;
import org.openmdx.base.resource.Records;
import org.openmdx.kernel.exception.BasicException;
import org.openmdx.kernel.exception.Throwables;
import org.openmdx.kernel.url.protocol.XRI_1Protocols;


/**
 * FilterProperty based Filter
 */
public abstract class AbstractFilter implements Selector {

    /**
     * Constructor
     * 
     * @param filter
     * 
     * @exception   IllegalArgumentException
     *              in case of an invalid filter property set
     * @exception   NullPointerException
     *              if the filter is <code>null</code>
     */
    protected AbstractFilter(
        FilterProperty[] filter
    ){
        this.filter = filter;
        for (
            int i = 0;
            i < this.filter.length;
            i++
        ) try {
            short operator = filter[i].operator();
            if(
                operator == ConditionType.IS_LIKE.code() ||
                operator == ConditionType.IS_UNLIKE.code()
            ){
                if(this.pattern == null) {
                    this.pattern = new Pattern_1_0[filter.length];
                }
                Object value = filter[i].getValue(0);
                this.pattern[i] = value instanceof Path ?
                    new PathPattern((Path)value) :
                    ((String)value).startsWith(XRI_1Protocols.OPENMDX_PREFIX) ?
                        new PathPattern(new Path((String)value)) :
                        new RegularExpressionPattern((String)value);                
            }
        } 
        catch (IllegalArgumentException exception) {
            throw BasicException.initHolder(
                new IllegalArgumentException(
                    "Invalid filter property",
                    BasicException.newEmbeddedExceptionStack(
                        exception,
                        BasicException.Code.DEFAULT_DOMAIN,
                        BasicException.Code.BAD_PARAMETER,
                        new BasicException.Parameter("filterProperty",filter[i])
                    )
                )
            );
        }
    }

    /**
     * 
     * @param candidate
     * @param attribute
     * @return an iterator for the values, never <code>null</code>
     * 
     * @exception   Exception
     *              in case of failure
     */
    protected abstract Iterator<?> getValuesIterator(
        Object candidate,
        String attribute
    ) throws Exception;

    /**
     * 
     * @param candidate
     * @param attribute
     * @return an iterator for the values, never <code>null</code>
     * 
     * @exception   Exception
     *              in case of failure
     */
    protected abstract Iterator<?> getObjectIterator(
        Object candidate,
        String attribute
    ) throws Exception;
    
    /**
     * 
     */
    protected FilterProperty[] filter;

    /**
     * 
     */
    private Pattern_1_0[] pattern;

    /**
     * Test two values for equality in the context of a filter
     * 
     * @param candidate the candidate to be compared with the filter value
     * @param filterValue the filter value
     * 
     * @return <code>true</code> if the two values are considered to be equal 
     * in the context of a filter
     */
    protected boolean equal(
        Object candidate,
        Object filterValue
    ){
        return 
            LenientPathComparator.isComparable(candidate) ? compare(candidate,filterValue) == 0 :
            candidate.equals(filterValue);
    }

    /**
     * Compare two values in the context of a filter
     * 
     * @param candidate the candidate to be compared with the filter value
     * @param filterValue the filter value
     * 
     * @return the result of the comparisom
     */
    protected int compare(
        Object candidate,
        Object filterValue
    ){
        return LenientPathComparator.getInstance().compare(candidate, filterValue);
    }
    
    /**
     * 
     */
    public FilterProperty[] getDelegate(
    ){
        return this.filter;
    }


    //------------------------------------------------------------------------
    // Implements Selector 
    //------------------------------------------------------------------------

    private boolean isComplex(
        FilterProperty predicate
    ){
        List<?> values = predicate.values();
        return !values.isEmpty() && values.get(0) instanceof Filter;
    }
    
    /* (non-Javadoc)
     * @see org.openmdx.compatibility.base.query.Selector#accept(java.lang.Object)
     */
    public boolean accept(
        Object candidate
    ){
        Properties: for (
            int propertyIndex = 0;
            propertyIndex < this.filter.length;
            propertyIndex++
        ){
            FilterProperty property = this.filter[propertyIndex];
            Quantifier quantifier = Quantifier.valueOf(property.quantor());
            Iterator<?> iterator;
            try {                
                iterator = isComplex(property) ? getObjectIterator(candidate, property.name()) : getValuesIterator(candidate, property.name());
            } catch (Exception exception) {
                Throwables.log(exception);
                return false;
            }
            while (iterator.hasNext()){
                Object raw = iterator.next();
                switch(ConditionType.valueOf(property.operator())){

                    case IS_UNLIKE: {
                        if (!matches(propertyIndex, raw)){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_LIKE: {
                        if (matches(propertyIndex, raw)){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_OUTSIDE: {
                        if(
                            compare(raw,property.getValue(0)) < 0 ||
                            compare(raw,property.getValue(1)) > 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_BETWEEN: {
                        if(
                            compare(raw,property.getValue(0)) >= 0 &&
                            compare(raw,property.getValue(1)) <= 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_LESS_OR_EQUAL: {                
                        if(
                            compare(raw,property.getValue(0)) <= 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_GREATER: {
                        if(
                            compare(raw,property.getValue(0)) > 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_LESS: {
                        if(
                            compare(raw,property.getValue(0)) < 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_GREATER_OR_EQUAL: {
                        if(
                            compare(raw,property.getValue(0)) >= 0
                        ){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_NOT_IN: {
                        boolean test = true;
                        IsNotIn: for(
                            int setIndex = 0, setSize = property.getValues().length;
                            setIndex < setSize;
                            setIndex++
                        ) {
                            if(equal(raw, property.getValue(setIndex))) {
                                test = false;
                                break IsNotIn;
                            }
                        }
                        if(test){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case IS_IN: {
                        boolean test = false;
                        IsIn: for(
                            int setIndex = 0, setSize = property.getValues().length;
                            setIndex < setSize;
                            setIndex++
                        ) {
                            if(equal(raw, property.getValue(setIndex))) {
                                test = true;
                                break IsIn;
                            }
                        }
                        if(test){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case SOUNDS_LIKE: {
                        boolean test = false;
                        String encoded = Soundex.getInstance().encode((String)raw);
                        SoundsLike: for(
                            int setIndex = 0, setSize = property.getValues().length;
                            setIndex < setSize;
                            setIndex++
                        ){
                            if(encoded.equals(Soundex.getInstance().encode((String)property.getValue(setIndex)))) {
                                test = true;
                                break SoundsLike;
                            }
                        } 
                        if(test){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    case SOUNDS_UNLIKE: {
                        boolean test = true;
                        String encoded = Soundex.getInstance().encode((String)raw);
                        SoundsUnlike: for(
                            int setIndex = 0, setSize = property.getValues().length;
                            setIndex < setSize;
                            setIndex++
                        ) {
                            if(encoded.equals(Soundex.getInstance().encode((String)property.getValue(setIndex)))) {
                                test = false;
                                break SoundsUnlike;
                            }
                        }
                        if(test){
                            if(quantifier == Quantifier.THERE_EXISTS) continue Properties;
                        } else {
                            if(quantifier == Quantifier.FOR_ALL) return false;
                        }
                        break;
                    }

                    default: throw BasicException.initHolder( 
                        new IllegalArgumentException(
                            "Unsupported operator",
                            BasicException.newEmbeddedExceptionStack(
                                BasicException.Code.DEFAULT_DOMAIN,
                                BasicException.Code.BAD_PARAMETER,
                                new BasicException.Parameter("operator", ConditionType.valueOf(property.operator()))
                            )
                        )
                    );                
                }
            }
            if(quantifier == Quantifier.THERE_EXISTS) return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Records.getRecordFactory().asIndexedRecord(
		    getClass().getName(), 
		    null, 
		    this.filter
		).toString();
    }

    public int size(){
        return this.filter.length;
    }

    /**
     * PathPattern aware match method
     * 
     * @param index
     * @param value
     * 
     * @return
     */
    private boolean matches (
        int index,
        Object value
    ){        
        if(value instanceof Path){
            if(!(this.pattern[index] instanceof PathPattern)) try {
                this.pattern[index] = new PathPattern(new Path(this.pattern[index].pattern()));
            } catch (Exception exception) {
                return false;
            }
            return ((PathPattern)this.pattern[index]).matches((Path)value);
        } else {
            return this.pattern[index].matches(value.toString());
        }
    }

    //------------------------------------------------------------------------
    // Class Soundex 
    //------------------------------------------------------------------------

    /**
     * Encodes words using the soundex phonetic algorithm.
     * The primary method to call is Soundex.encode(String).<p>
     * The main method encodes arguments to System.out.
     * @author Aaron Hansen
     */
    final static class Soundex {

        /**
         * 
         */
        public static Soundex getInstance(
        ) {
            if(Soundex.instance == null) {
                Soundex.instance = new Soundex();
            }
            return instance;
        }

        /**
         * @param _word
         */    
        public String encode(
            String _word
        ) {
            String word = _word.trim();
            if (DropFinalSBoolean) {
                //we're not dropping double s as in guess
                if ( (word.length() > 1) 
                        && (word.endsWith("S") || word.endsWith("s")))
                    word = word.substring(0, (word.length() - 1));
            }
            if(word.length() == 0) return "";
            word = reduce(word);
            int wordLength = word.length(); //original word size
            int sofar = 0; //how many codes have been created
            int max = LengthInt - 1; //max codes to create (less the first char)
            if (LengthInt < 0) //if NO_MAX
                max = wordLength; //wordLength was the max possible size.
            int code = 0; 
            StringBuilder buf = new StringBuilder(
                max
            ).append(
                Character.toLowerCase(word.charAt(0))
            );
            for (int i = 1;(i < wordLength) && (sofar < max); i++) {
                code = getCode(word.charAt(i));
                if (code > 0) {
                    buf.append(code);
                    sofar++;
                }
            }
            if (PadBoolean && (LengthInt > 0)) {
                for (;sofar < max; sofar++)
                    buf.append('0');
            }
            return buf.toString();
        }

        /**
         * Returns the Soundex code for the specified character.
         * @param ch Should be between A-Z or a-z
         * @return -1 if the character has no phonetic code.
         */
        public int getCode(
            char ch
        ) {
            int arrayidx = -1;
            if (('a' <= ch) && (ch <= 'z'))
                arrayidx = ch - 'a';
            else if (('A' <= ch) && (ch <= 'Z'))
                arrayidx = ch - 'A';
            if ((arrayidx >= 0) && (arrayidx < SoundexInts.length))
                return SoundexInts[arrayidx];
            else
                return -1;
        }

        /**
         * If true, a final char of 's' or 'S' of the word being encoded will be 
         * dropped. By dropping the last s, lady and ladies for example,
         * will encode the same. False by default.
         */
        public boolean getDropFinalS(
        ) {
            return DropFinalSBoolean;
        }

        /**
         * The length of code strings to build, 4 by default.
         * If negative, length is unlimited.
         * @see #NO_MAX
         */
        public int getLength(
        ) {
            return LengthInt;
        }

        /**
         * If true, appends zeros to a soundex code if the code is less than
         * Soundex.getLength().  True by default.
         */
        public boolean getPad(
        ) {
            return PadBoolean;
        }

        /**
         * Allows you to modify the default code table
         * @param ch The character to specify the code for.
         * @param code The code to represent ch with, must be -1, or 1 thru 9
         */
        public void setCode(
            char ch, 
            int code
        ) {
            int arrayidx = -1;
            if (('a' <= ch) || (ch <= 'z'))
                arrayidx = ch - 'a';
            else if (('A' <= ch) || (ch <= 'Z'))
                arrayidx = ch - 'A';
            if ((0 <= arrayidx) && (arrayidx < SoundexInts.length))
                SoundexInts[arrayidx] = code;
        }

        /**
         * If true, a final char of 's' or 'S' of the word being encoded will be 
         * dropped.
         */
        public void setDropFinalS(
            boolean bool
        ) {
            DropFinalSBoolean = bool;
        }

        /**
         * Sets the length of code strings to build. 4 by default.
         * @param Length of code to produce, must be &gt;= 1
         */
        public void setLength(
            int length
        ) {
            LengthInt = length;
        }

        /**
         * If true, appends zeros to a soundex code if the code is less than
         * Soundex.getLength().  True by default.
         */
        public void setPad(
            boolean bool
        ) {
            PadBoolean = bool;
        }

        /**
         * Creates the Soundex code table.
         */
        protected static int[] createArray(
        ) {
            return new int[] {
                -1, //a 
                1, //b
                2, //c 
                3, //d
                -1, //e 
                1, //f
                2, //g 
                -1, //h
                -1, //i 
                2, //j
                2, //k
                4, //l
                5, //m
                5, //n
                -1, //o
                1, //p
                2, //q
                6, //r
                2, //s
                3, //t
                -1, //u
                1, //v
                -1, //w
                2, //x
                -1, //y
                2  //z
            };
        }

        /**
         * Removes adjacent sounds.
         */
        protected String reduce(
            String word
        ) {
            int len = word.length();
            StringBuilder buf = new StringBuilder(len);
            char ch = word.charAt(0);
            int currentCode = getCode(ch);
            buf.append(ch);
            int lastCode = currentCode;
            for (int i = 1; i < len; i++) {
                ch = word.charAt(i);
                currentCode = getCode(ch);
                if ((currentCode != lastCode) && (currentCode >= 0)) {
                    buf.append(ch);
                    lastCode = currentCode;
                }
            }
            return buf.toString();
        }

        /**
         * 
         */
        private static transient Soundex instance = null;

        /**
         * 
         */
        public static final int NO_MAX = -1;

        /**
         * If true, the final 's' of the word being encoded is dropped.
         */
        protected boolean DropFinalSBoolean = false;

        /**
         * Length of code to build.
         */
        protected int LengthInt = 4;

        /**
         * If true, codes are padded to the LengthInt with zeros.
         */
        protected boolean PadBoolean = true;

        /**
         * Soundex code table.
         */
        protected int[] SoundexInts = createArray();

    }

    /**
     * Pattern interface
     * <p>
     * Instances of this class are immutable and are safe for use by 
     * multiple concurrent threads. Instances of Matcher_1_0 class are 
     * not safe for such use. 
     */
    interface Pattern_1_0 
        extends Serializable
    {

        /**
         * Attempts to match the given input against the pattern
         * <p>
         * An invocation of this convenience method of the form
         * <pre>
         *      matches(input)
         * </pre>
         * behaves in exactly the same way as the expression
         * <pre>
         *      matcher(input).matches()
         * </pre>
         *  
         * @param input
         */
        boolean matches (
            String input
        );

        /**
         * Returns the expression from which this pattern was compiled. 
         *  
         * @return The source of this pattern
         */
        public String pattern();
            
    }

    /**
     * PathPattern_1
     */
    final static class PathPattern implements Pattern_1_0 {

        private static final long serialVersionUID = 3256441391432086579L;
        private final Path pathPattern;
        
        public PathPattern(
             Path pathPattern
        ) {
            this.pathPattern = pathPattern;
        }

        public boolean matches(String input) {
            try {
                return this.matches(new Path(input));
            } catch (Exception exception){
                return false;
            }
        }

        public boolean matches(Path input) {
            return input.isLike(this.pathPattern);
        }

        @SuppressWarnings("deprecation")
        public String pattern() {
            return this.pathPattern.toUri();
        }

    }
        
    /**
     * Pattern implementation 
     */
    final static class RegularExpressionPattern implements Pattern_1_0 {

        private static final long serialVersionUID = 1340205266230718256L;
        private final Pattern compiledExpression;
        private final String rawExpression;

        /**
         * Compiles a regular expression
         * 
         * @param pattern
         *
         * @exception IllegalArgumentException
         *            if the pattern can't be compiled
         */    
        RegularExpressionPattern(
            String pattern,
            String rawExpression
        ) {
            this.rawExpression = rawExpression;
            this.compiledExpression = Pattern.compile(pattern);
        }

        RegularExpressionPattern(
            String pattern
        ) {
            this(pattern, pattern);
        }

        public boolean matches(String source) {
            return this.compiledExpression.matcher(source).matches();
        }

        public String pattern() {
            return this.rawExpression;
        }

    }
    
}