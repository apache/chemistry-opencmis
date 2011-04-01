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

/**
 * This result type of {@link EvaluatorXPath} provides means for partially evaluating
 * the underlying query's condition. This allows to determine whether there is a semantically
 * equivalent translation from the CMIS query's where clause to an XPath condition.
 * <br/>
 * Specifically <code>EvaluatorXPath</code> only supports a single folder predicate. That
 * is the original CMIS query must not contain more than one IN_TREE or IN_FOLDER
 * predicate respectively. Furthermore that single folder predicate must be affirmative.
 * A literal <code>p</code> in a boolean expression <code>X</code> is affirmative if there
 * exists a boolean expression <code>Y</code> such that <code>p &and; Y = X</code>.
 * <em>Note</em>: a single folder predicate is affirmative if any only if
 * {@link #eval(Boolean) <code>eval(false)</code>} return <code>false</code>.  
 * <br/>
 * Only if both conditions hold will the XPath translation provided the {@link #xPath()}
 * method be valid.
 */
public interface XPathBuilder {

    /**
     * Translation of the underlying CMIS query's where clause to a XPath condition.
     * The string is only valid if there is no more than one folder predicate and
     * the folder predicate is in affirmative position.
     */
    String xPath();

    /**
     * Evaluate the query condition for a given valuation of the folder predicate terms.
     *
     * @param folderPredicateValuation  valuation for the folder predicate terms. Use <code>null</code>
     *      for none.
     * @return  result of the partial evaluation. <code>null</code> means that the value of the
     *      query condition is not determined the value passed for <code>folderPredicateValuation</code>.
     */
    Boolean eval(Boolean folderPredicateValuation);

    /**
     * The folder predicates contained in this query's condition.
     */
    Iterable<XPathBuilder> folderPredicates();
}
