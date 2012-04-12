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

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer;
import org.apache.chemistry.opencmis.server.support.query.PredicateWalkerBase;
import org.apache.chemistry.opencmis.server.support.query.TextSearchLexer;

import java.util.ArrayList;
import java.util.List;

/**
 * This implementation of {@link PredicateWalkerBase} traverses the parse tree of a CMIS query.
 * It uses an {@link Evaluator} to accumulate the result of the traversal. <code>Evaluator</code>
 * has a corresponding method for each {@link Tree#getType() node type} in the parse tree.
 * <code>ParseTreeWalker</code> calls these methods while traversing the parse tree passing an
 * <code>Evaluator</code> for each of the corresponding operation's arguments.
 * </br>
 * The {@link #walkPredicate(Tree)} serves as entry point for traversing a parse tree. After
 * successful traversal, the result is obtained from the {@link #getResult()} method.
 *
 * @param <T>  type of the result determined by the <code>Evaluator</code> used.
 */
public class ParseTreeWalker<T> implements PredicateWalkerBase {

    private final Evaluator<T> evaluator;
    private T result;

    /**
     * Create a new instance for traversing CMIS query parse trees.
     *
     * @param evaluator  <code>Evaluator</code> for evaluating the nodes of the parse tree
     */
    public ParseTreeWalker(Evaluator<T> evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Retrieve the result of a successful traversal.
     *
     * @return  result of traversal or <code>null</code> if either not yet traversed, an error occurred
     *      on traversal or the query has an empty where clause. 
     */
    public T getResult() {
        return result;
    }

    //------------------------------------------< PredicateWalkerBase >---
    
    public Boolean walkPredicate(Tree node) {
        result = null;
        result = walkPredicate(evaluator, node);
        return false; // Return value is ignored by caller
    }

    //------------------------------------------< protected >---

    /** For extensibility. */
    protected T walkOtherExpr(Evaluator<?> evaluator, Tree node) {
        throw new CmisRuntimeException("Unknown node type: " + node.getType() + " (" + node.getText() + ")");
    }

    /** For extensibility. */
    protected T walkOtherPredicate(Evaluator<?> evaluator, Tree node) {
        throw new CmisRuntimeException("Unknown node type: " + node.getType() + " (" + node.getText() + ")");
    }

    //------------------------------------------< private >---

    private T walkPredicate(Evaluator<T> evaluator, Tree node) {
        switch (node.getType()) {
            case CmisQlStrictLexer.NOT:
                return evaluator.not(walkPredicate(evaluator.op(), node.getChild(0)));
            case CmisQlStrictLexer.AND:
                return evaluator.and(
                        walkPredicate(evaluator.op(), node.getChild(0)),
                        walkPredicate(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.OR:
                return evaluator.or(
                        walkPredicate(evaluator.op(), node.getChild(0)),
                        walkPredicate(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.EQ:
                return evaluator.eq(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.NEQ:
                return evaluator.neq(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.GT:
                return evaluator.gt(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.GTEQ:
                return evaluator.gteq(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.LT:
                return evaluator.lt(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.LTEQ:
                return evaluator.lteq(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.IN:
                return evaluator.in(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.NOT_IN:
                return evaluator.notIn(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.IN_ANY:
                return evaluator.inAny(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.NOT_IN_ANY:
                return evaluator.notInAny(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.EQ_ANY:
                return evaluator.eqAny(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.IS_NULL:
                return evaluator.isNull(walkExpr(evaluator.op(), node.getChild(0)));
            case CmisQlStrictLexer.IS_NOT_NULL:
                return evaluator.notIsNull(walkExpr(evaluator.op(), node.getChild(0)));
            case CmisQlStrictLexer.LIKE:
                return evaluator.like(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.NOT_LIKE:
                return evaluator.notLike(
                        walkExpr(evaluator.op(), node.getChild(0)),
                        walkExpr(evaluator.op(), node.getChild(1)));
            case CmisQlStrictLexer.CONTAINS:
                if (node.getChildCount() == 1) {
                    return evaluator.contains(
                            null,
                            walkExprTextSearch(evaluator.op(), node.getChild(0)));
                }
                else {
                    return evaluator.contains(
                            walkExpr(evaluator.op(), node.getChild(0)),
                            walkExpr(evaluator.op(), node.getChild(1)));
                }
            case CmisQlStrictLexer.IN_FOLDER:
                if (node.getChildCount() == 1) {
                    return evaluator.inFolder(
                            null,
                            walkExpr(evaluator.op(), node.getChild(0)));
                }
                else {
                    return evaluator.inFolder(
                            walkExpr(evaluator.op(), node.getChild(0)),
                            walkExpr(evaluator.op(), node.getChild(1)));
                }
            case CmisQlStrictLexer.IN_TREE:
                if (node.getChildCount() == 1) {
                    return evaluator.inTree(
                            null,
                            walkExpr(evaluator.op(), node.getChild(0)));
                }
                else {
                    return evaluator.inTree(
                            walkExpr(evaluator.op(), node.getChild(0)),
                            walkExpr(evaluator.op(), node.getChild(1)));
                }
            default:
                return walkOtherPredicate(evaluator, node);
        }
    }

    private T walkExpr(Evaluator<T> evaluator, Tree node) {
        switch (node.getType()) {
            case CmisQlStrictLexer.BOOL_LIT:
                return walkBoolean(evaluator, node);
            case CmisQlStrictLexer.NUM_LIT:
                return walkNumber(evaluator, node);
            case CmisQlStrictLexer.STRING_LIT:
                return walkString(evaluator, node);
            case CmisQlStrictLexer.TIME_LIT:
                return walkTimestamp(evaluator, node);
            case CmisQlStrictLexer.IN_LIST:
                return evaluator.list(walkList(evaluator, node));
            case CmisQlStrictLexer.COL:
                return walkCol(evaluator, node);
            default:
                return walkOtherExpr(evaluator, node);
        }
    }
    
    private T walkExprTextSearch(Evaluator<T> evaluator, Tree node) {
        switch (node.getType()) {
            case TextSearchLexer.TEXT_AND:
                return walkTextAnd(evaluator, node);
            case TextSearchLexer.TEXT_OR:
                return walkTextOr(evaluator, node);
            case TextSearchLexer.TEXT_MINUS:
                return walkTextMinus(evaluator, node);
            case TextSearchLexer.TEXT_SEARCH_WORD_LIT:
                return walkTextWord(evaluator, node);
            case TextSearchLexer.TEXT_SEARCH_PHRASE_STRING_LIT:
                return walkTextPhrase(evaluator, node);
            default:
                return walkOtherExpr(evaluator, node);
        }
    }

    private List<T> walkList(Evaluator<T> evaluator, Tree node) {
        int n = node.getChildCount();
        List<T> result = new ArrayList<T>(n);
        for (int i = 0; i < n; i++) {
            result.add(walkExpr(evaluator.op(), node.getChild(i)));
        }
        return result;
    }

    private T walkBoolean(Evaluator<T> evaluator, Tree node) {
        String s = node.getText();

        if ("true".equalsIgnoreCase(s)) {
            return evaluator.value(true);
        }
        else if ("false".equalsIgnoreCase(s)) {
            return evaluator.value(false);
        }
        else {
            throw new CmisInvalidArgumentException("Not a boolean: " + s);
        }
    }

    private T walkNumber(Evaluator<T> evaluator, Tree node) {
        String s = node.getText();
        try {
            return s.contains(".") || s.contains("e") || s.contains("E")
                    ? evaluator.value(Double.valueOf(s))
                    : evaluator.value(Long.valueOf(s));
        }
        catch (NumberFormatException e) {
            throw new CmisInvalidArgumentException("Not a number: " + s);
        }
    }

    private T walkString(Evaluator<T> evaluator, Tree node) {
        String s = node.getText();
        s = s.substring(1, s.length() - 1);
        return evaluator.value(s.replace("''", "'"));  // un-escape quotes
    }

    private T walkTimestamp(Evaluator<T> evaluator, Tree node) {
        String s = node.getText();
        s = s.substring(s.indexOf('\'') + 1, s.length() - 1);
        try {
            return evaluator.value(CalendarHelper.fromString(s));
        }
        catch (IllegalArgumentException e) {
            throw new CmisInvalidArgumentException("Not a date time value: " + s);
        }
    }

    private T walkCol(Evaluator<T> evaluator, Tree node) {
        return evaluator.col(node.getChild(0).getText());
    }

    private T walkTextAnd(Evaluator<T> evaluator, Tree node) {
        List<T> terms = new ArrayList<T>();
        for (Tree term: getChildrenAsList(node)) {
            terms.add(walkExprTextSearch(evaluator, term));
        }

        return evaluator.textAnd(terms);
    }
    
    private T walkTextOr(Evaluator<T> evaluator, Tree node) {
        List<T> terms = new ArrayList<T>();
        for (Tree term: getChildrenAsList(node)) {
            terms.add(walkExprTextSearch(evaluator, term));
        }

        return evaluator.textOr(terms);
    }
    
    private T walkTextMinus(Evaluator<T> evaluator, Tree node) {
        return evaluator.textMinus(node.getChild(0).getText());
    }
    
    private T walkTextWord(Evaluator<T> evaluator, Tree node) {
        return evaluator.textWord(node.getText());
    }
    
    private T walkTextPhrase(Evaluator<T> evaluator, Tree node) {
        return evaluator.textPhrase(node.getText());
    }

    private static List<Tree> getChildrenAsList(Tree node) {
        List<Tree> res = new ArrayList<Tree>(node.getChildCount());
        for (int i=0; i<node.getChildCount(); i++) {
            Tree childNode =  node.getChild(i);
            res.add(childNode);
        }
        return res;
    }

}
