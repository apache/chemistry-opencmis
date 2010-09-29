/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 * Contributors:
 *     Florent Guillaume, Nuxeo
 */
package org.apache.chemistry.opencmis.server.support.query;

import org.antlr.runtime.tree.Tree;

/**
 * Interface for a tree walker of a WHERE clause.
 * <p>
 * Can be used to build another datastructure, or for direct value evaluation
 * (thus the boolean return values for clauses, and Object for values).
 * <p>
 * The method {@link walkClause} is the entry point.
 */
public interface PredicateWalker {

    boolean walkPredicate(Tree node);

    boolean walkNot(Tree opNode, Tree leftNode);

    boolean walkAnd(Tree opNode, Tree leftNode, Tree rightNode);

    boolean walkOr(Tree opNode, Tree leftNode, Tree rightNode);

    Object walkExpr(Tree node);

    boolean walkEquals(Tree eqNode, Tree leftNode, Tree rightNode);

    boolean walkNotEquals(Tree neNode, Tree leftNode, Tree rightNode);

    boolean walkGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode);

    boolean walkGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode);

    boolean walkLessThan(Tree ltNode, Tree leftNode, Tree rightNode);

    boolean walkLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode);

    boolean walkIn(Tree node, Tree colNode, Tree listNode);

    boolean walkNotIn(Tree node, Tree colNode, Tree listNode);

    boolean walkInAny(Tree node, Tree colNode, Tree listNode);

    boolean walkNotInAny(Tree node, Tree colNode, Tree listNode);

    boolean walkEqAny(Tree node, Tree literalNode, Tree colNode);

    boolean walkIsNull(Tree nullNode, Tree colNode);

    boolean walkIsNotNull(Tree notNullNode, Tree colNode);

    boolean walkLike(Tree node, Tree colNode, Tree stringNode);

    boolean walkNotLike(Tree node, Tree colNode, Tree stringNode);

    boolean walkContains(Tree node, Tree qualNode, Tree paramNode);

    boolean walkInFolder(Tree node, Tree qualNode, Tree paramNode);

    boolean walkInTree(Tree node, Tree qualNode, Tree paramNode);

    Object walkList(Tree node);

    Object walkBoolean(Tree node);

    Object walkNumber(Tree node);

    Object walkString(Tree node);

    Object walkTimestamp(Tree node);

    Object walkCol(Tree node);

}
