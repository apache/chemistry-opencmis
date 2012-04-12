/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.chemistry.opencmis.jcr.query;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * Evaluator for CMIS query parse trees.
 *
 * @param <T>  The result type of the evaluation of the parse tree.
 * @see ParseTreeWalker
 */
public interface Evaluator<T> {

    /** Create a new instance of this <code>Evaluator</code>. */
    Evaluator<T> op();

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#NOT} nodes */
    T not(T op);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#AND} nodes */
    T and(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#OR} nodes */
    T or(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#EQ} nodes */
    T eq(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#NEQ} nodes */
    T neq(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#GT} nodes */
    T gt(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#GTEQ} nodes */
    T gteq(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#LT} nodes */
    T lt(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#LTEQ} nodes */
    T lteq(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#IN} nodes */
    T in(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#NOT_IN} nodes */
    T notIn(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#IN_ANY} nodes */
    T inAny(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#NOT_IN_ANY} nodes */
    T notInAny(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#EQ_ANY} nodes */
    T eqAny(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#IS_NULL} nodes */
    T isNull(T op);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#IS_NOT_NULL} nodes */
    T notIsNull(T op);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#LIKE} nodes */
    T like(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#NOT_LIKE} nodes */
    T notLike(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#CONTAINS} nodes */
    T contains(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#IN_FOLDER} nodes */
    T inFolder(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#IN_TREE} nodes */
    T inTree(T op1, T op2);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#IN_LIST} nodes */
    T list(List<T> ops);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#BOOL_LIT} nodes */
    T value(boolean value);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#NUM_LIT} nodes */
    T value(double value);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#NUM_LIT} nodes */
    T value(long value);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#STRING_LIT} nodes */
    T value(String value);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#TIME_LIT} nodes */
    T value(GregorianCalendar value);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer#COL} nodes */
    T col(String name);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.TextSearchLexer#TEXT_AND} */
    T textAnd(List<T> ops);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.TextSearchLexer#TEXT_OR} */
    T textOr(List<T> ops);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.TextSearchLexer#TEXT_MINUS} */
    T textMinus(String text);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.TextSearchLexer#TEXT_SEARCH_WORD_LIT} */
    T textWord(String word);

    /** Handle {@link org.apache.chemistry.opencmis.server.support.query.TextSearchLexer#TEXT_SEARCH_PHRASE_STRING_LIT} */
    T textPhrase(String phrase);
}
