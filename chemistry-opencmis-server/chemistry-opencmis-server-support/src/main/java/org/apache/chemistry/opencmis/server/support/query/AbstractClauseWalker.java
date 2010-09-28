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
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

/**
 * Basic implementation walking a WHERE clause in lexical order.
 * <p>
 * The {@code walkXYZ} methods can be overridden to change the walking order.
 */
public abstract class AbstractClauseWalker implements ClauseWalker {

    public boolean walkClause(Tree node) {
        switch (node.getType()) {
        case CmisQlStrictLexer.NOT:
            return walkNot(node, node.getChild(0));
        case CmisQlStrictLexer.AND:
            return walkAnd(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.OR:
            return walkOr(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.EQ:
            return walkEquals(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.NEQ:
            return walkNotEquals(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.GT:
            return walkGreaterThan(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.GTEQ:
            return walkGreaterOrEquals(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.LT:
            return walkLessThan(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.LTEQ:
            return walkLessOrEquals(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.IN:
            return walkIn(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.NOT_IN:
            return walkNotIn(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.IN_ANY:
            return walkInAny(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.NOT_IN_ANY:
            return walkNotInAny(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.EQ_ANY:
            return walkEqAny(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.IS_NULL:
            return walkIsNull(node, node.getChild(0));
        case CmisQlStrictLexer.IS_NOT_NULL:
            return walkIsNotNull(node, node.getChild(0));
        case CmisQlStrictLexer.LIKE:
            return walkLike(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.NOT_LIKE:
            return walkNotLike(node, node.getChild(0), node.getChild(1));
        case CmisQlStrictLexer.CONTAINS:
            if (node.getChildCount() == 1) {
                return walkContains(node, null, node.getChild(0));
            } else {
                return walkContains(node, node.getChild(0), node.getChild(1));
            }
        case CmisQlStrictLexer.IN_FOLDER:
            if (node.getChildCount() == 1) {
                return walkInFolder(node, null, node.getChild(0));
            } else {
                return walkInFolder(node, node.getChild(0), node.getChild(1));
            }
        case CmisQlStrictLexer.IN_TREE:
            if (node.getChildCount() == 1) {
                return walkInTree(node, null, node.getChild(0));
            } else {
                return walkInTree(node, node.getChild(0), node.getChild(1));
            }
        default:
            return walkOtherClause(node);
        }
    }

    /** For extensibility. */
    protected boolean walkOtherClause(Tree node) {
        throw new CmisRuntimeException("Unknown node type: " + node.getType() + " (" + node.getText() + ")");
    }

    public boolean walkNot(Tree opNode, Tree node) {
        walkClause(node);
        return false;
    }

    public boolean walkAnd(Tree opNode, Tree leftNode, Tree rightNode) {
        walkClause(leftNode);
        walkClause(rightNode);
        return false;
    }

    public boolean walkOr(Tree opNode, Tree leftNode, Tree rightNode) {
        walkClause(leftNode);
        walkClause(rightNode);
        return false;
    }

    public Object walkValue(Tree node) {
        switch (node.getType()) {
        case CmisQlStrictLexer.BOOL_LIT:
            return walkBoolean(node);
        case CmisQlStrictLexer.NUM_LIT:
            return walkNumber(node);
        case CmisQlStrictLexer.STRING_LIT:
            return walkString(node);
        case CmisQlStrictLexer.TIME_LIT:
            return walkTimestamp(node);
        case CmisQlStrictLexer.IN_LIST:
            return walkInList(node);
        case CmisQlStrictLexer.COL:
            return walkCol(node);
        default:
            return walkOtherValue(node);
        }
    }

    /** For extensibility. */
    protected Object walkOtherValue(Tree node) {
        throw new CmisRuntimeException("Unknown node type: " + node.getType() + " (" + node.getText() + ")");
    }

    public boolean walkEquals(Tree opNode, Tree leftNode, Tree rightNode) {
        walkValue(leftNode);
        walkValue(rightNode);
        return false;
    }

    public boolean walkNotEquals(Tree opNode, Tree leftNode, Tree rightNode) {
        walkValue(leftNode);
        walkValue(rightNode);
        return false;
    }

    public boolean walkGreaterThan(Tree opNode, Tree leftNode, Tree rightNode) {
        walkValue(leftNode);
        walkValue(rightNode);
        return false;
    }

    public boolean walkGreaterOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
        walkValue(leftNode);
        walkValue(rightNode);
        return false;
    }

    public boolean walkLessThan(Tree opNode, Tree leftNode, Tree rightNode) {
        walkValue(leftNode);
        walkValue(rightNode);
        return false;
    }

    public boolean walkLessOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
        walkValue(leftNode);
        walkValue(rightNode);
        return false;
    }

    public boolean walkIn(Tree opNode, Tree colNode, Tree listNode) {
        walkValue(colNode);
        walkValue(listNode);
        return false;
    }

    public boolean walkNotIn(Tree opNode, Tree colNode, Tree listNode) {
        walkValue(colNode);
        walkValue(listNode);
        return false;
    }

    public boolean walkInAny(Tree opNode, Tree colNode, Tree listNode) {
        walkValue(colNode);
        walkValue(listNode);
        return false;
    }

    public boolean walkNotInAny(Tree opNode, Tree colNode, Tree listNode) {
        walkValue(colNode);
        walkValue(listNode);
        return false;
    }

    public boolean walkEqAny(Tree opNode, Tree literalNode, Tree colNode) {
        walkValue(literalNode);
        walkValue(colNode);
        return false;
    }

    public boolean walkIsNull(Tree opNode, Tree colNode) {
        walkValue(colNode);
        return false;
    }

    public boolean walkIsNotNull(Tree opNode, Tree colNode) {
        walkValue(colNode);
        return false;
    }

    public boolean walkLike(Tree opNode, Tree colNode, Tree stringNode) {
        walkValue(colNode);
        walkValue(stringNode);
        return false;
    }

    public boolean walkNotLike(Tree opNode, Tree colNode, Tree stringNode) {
        walkValue(colNode);
        walkValue(stringNode);
        return false;
    }

    public boolean walkContains(Tree opNode, Tree colNode, Tree queryNode) {
        walkValue(colNode);
        walkValue(queryNode);
        return false;
    }

    public boolean walkInFolder(Tree opNode, Tree colNode, Tree paramNode) {
        walkValue(colNode);
        walkValue(paramNode);
        return false;
    }

    public boolean walkInTree(Tree opNode, Tree colNode, Tree paramNode) {
        walkValue(colNode);
        walkValue(paramNode);
        return false;
    }

    public Object walkBoolean(Tree node) {
        return null;
    }

    public Object walkNumber(Tree node) {
        return null;
    }

    public Object walkString(Tree node) {
        return null;
    }

    public Object walkTimestamp(Tree node) {
        return null;
    }

    public Object walkInList(Tree node) {
        return null;
    }

    public Object walkCol(Tree node) {
        return null;
    }

}
