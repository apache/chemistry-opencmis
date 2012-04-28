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
 */
package org.apache.chemistry.opencmis.inmemory.query;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictParser;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictParser_CmisBaseGrammar.query_return;
import org.apache.chemistry.opencmis.server.support.query.CmisQueryWalker;
import org.apache.chemistry.opencmis.server.support.query.StringUtil;
import org.apache.chemistry.opencmis.server.support.query.TextSearchLexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractQueryConditionProcessor implements QueryConditionProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessQueryTest.class);
    
    public abstract void onStartProcessing(Tree whereNode);
    public abstract void onStopProcessing();

    // Compare operators
    public abstract void onEquals(Tree eqNode, Tree leftNode, Tree rightNode);
    public abstract void onNotEquals(Tree neNode, Tree leftNode, Tree rightNode);
    public abstract void onGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode);
    public abstract void onGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode);
    public abstract void onLessThan(Tree ltNode, Tree leftNode, Tree rightNode);
    public abstract void onLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode);

    // Boolean operators
    public void onPreNot(Tree opNode, Tree leftNode) {
    }
    public abstract void onNot(Tree opNode, Tree leftNode);
    public void onPostNot(Tree opNode, Tree leftNode) {
    }
    public void onPreAnd(Tree opNode, Tree leftNode, Tree rightNode) {
    }
    public abstract void onAnd(Tree opNode, Tree leftNode, Tree rightNode);
    public void onPostAnd(Tree opNode, Tree leftNode, Tree rightNode) {
    }
    public void onPreOr(Tree opNode, Tree leftNode, Tree rightNode) {
    }
    public abstract void onOr(Tree opNode, Tree leftNode, Tree rightNode);
    public void onPostOr(Tree opNode, Tree leftNode, Tree rightNode) {
    }

    // Multi-value:
    public abstract void onIn(Tree node, Tree colNode, Tree listNode);
    public abstract void onNotIn(Tree node, Tree colNode, Tree listNode);
    public abstract void onInAny(Tree node, Tree colNode, Tree listNode);
    public abstract void onNotInAny(Tree node, Tree colNode, Tree listNode);
    public abstract void onEqAny(Tree node, Tree literalNode, Tree colNode);

    // Null comparisons:
    public abstract void onIsNull(Tree nullNode, Tree colNode);
    public abstract void onIsNotNull(Tree notNullNode, Tree colNode);

    // String matching:
    public abstract void onIsLike(Tree node, Tree colNode, Tree stringNode);
    public abstract void onIsNotLike(Tree node, Tree colNode, Tree stringNode);

    // Functions:
    public abstract void onInFolder(Tree node, Tree colNode, Tree paramNode);
    public abstract void onInTree(Tree node, Tree colNode, Tree paramNode);
    public abstract void onScore(Tree node);
    
    public void onPreTextAnd(Tree node, List<Tree> conjunctionNodes) {
    }
    public abstract void onTextAnd(Tree node, List<Tree> conjunctionNodes);
    public void onPostTextAnd(Tree node, List<Tree> conjunctionNodes) {
    }
    public void onPreTextOr(Tree node, List<Tree> termNodes) {
    }
    public abstract void onTextOr(Tree node, List<Tree> termNodes);
    public void onPostTextOr(Tree node, List<Tree> termNodes) {
    }
    public abstract void onTextMinus(Tree node, Tree notNode);
    public abstract void onTextWord(String word);
    public abstract void onTextPhrase(String phrase);

    // convenience method because everybody needs this piece of code
    public static CmisQueryWalker getWalker(String statement) throws UnsupportedEncodingException, IOException, RecognitionException {
        CharStream input = new ANTLRInputStream(new ByteArrayInputStream(statement.getBytes("UTF-8")));
        TokenSource lexer = new CmisQlStrictLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        CmisQlStrictParser parser = new CmisQlStrictParser(tokens);
        CommonTree parserTree; // the ANTLR tree after parsing phase

        query_return parsedStatement = parser.query();
//        if (parser.errorMessage != null) {
//            throw new RuntimeException("Cannot parse query: " + statement + " (" + parser.errorMessage + ")");
//        }
        parserTree = (CommonTree) parsedStatement.getTree();

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(parserTree);
        nodes.setTokenStream(tokens);
        CmisQueryWalker walker = new CmisQueryWalker(nodes);
        return walker;
    }


    // Base interface called from query parser
    public Boolean walkPredicate(Tree whereNode) {
        if (null != whereNode) {
            onStartProcessing(whereNode);
            evalWhereNode(whereNode);
            onStopProcessing();
        }
        return null; // unused
    }

    // ///////////////////////////////////////////////////////
    // Processing the WHERE clause

    // default implementation for ^ains
    public void onContains(Tree node, Tree typeNode, Tree searchExprNode) {
        LOG.debug("evaluating text search node: " + searchExprNode);
        evalTextSearchNode(searchExprNode);        
    }

    protected void evalWhereNode(Tree node) {
        // Ensure that we receive only valid tokens and nodes in the where
        // clause:
        LOG.debug("evaluating node: " + node.toString());
        switch (node.getType()) {
        case CmisQlStrictLexer.WHERE:
            break; // ignore
        case CmisQlStrictLexer.EQ:
            evalWhereNode(node.getChild(0));
            onEquals(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.NEQ:
            evalWhereNode(node.getChild(0));
            onNotEquals(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.GT:
            evalWhereNode(node.getChild(0));
            onGreaterThan(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.GTEQ:
            evalWhereNode(node.getChild(0));
            onGreaterOrEquals(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.LT:
            evalWhereNode(node.getChild(0));
            onLessThan(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.LTEQ:
            evalWhereNode(node.getChild(0));
            onLessOrEquals(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;

        case CmisQlStrictLexer.NOT:
            onPreNot(node, node.getChild(0));
            onNot(node, node.getChild(0));
            evalWhereNode(node.getChild(0));
            onPostNot(node, node.getChild(0));
            break;
        case CmisQlStrictLexer.AND:
            onPreAnd(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(0));
            onAnd(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            onPostAnd(node, node.getChild(0), node.getChild(1));
            break;
        case CmisQlStrictLexer.OR:
            onPreOr(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(0));
            onOr(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            onPostOr(node, node.getChild(0), node.getChild(1));
            break;

        // Multi-value:
        case CmisQlStrictLexer.IN:
            evalWhereNode(node.getChild(0));
            onIn(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.NOT_IN:
            evalWhereNode(node.getChild(0));
            onNotIn(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.IN_ANY:
            evalWhereNode(node.getChild(0));
            onInAny(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.NOT_IN_ANY:
            evalWhereNode(node.getChild(0));
            onNotInAny(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.EQ_ANY:
            evalWhereNode(node.getChild(0));
            onEqAny(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;

        // Null comparisons:
        case CmisQlStrictLexer.IS_NULL:
            onIsNull(node, node.getChild(0));
            evalWhereNode(node.getChild(0));
            break;
        case CmisQlStrictLexer.IS_NOT_NULL:
            onIsNotNull(node, node.getChild(0));
            evalWhereNode(node.getChild(0));
            break;

        // String matching
        case CmisQlStrictLexer.LIKE:
            evalWhereNode(node.getChild(0));
            onIsLike(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;
        case CmisQlStrictLexer.NOT_LIKE:
            evalWhereNode(node.getChild(0));
            onIsNotLike(node, node.getChild(0), node.getChild(1));
            evalWhereNode(node.getChild(1));
            break;

        // Functions
        case CmisQlStrictLexer.CONTAINS:
            onContains(node, null, node.getChild(0));
            break;
        case CmisQlStrictLexer.IN_FOLDER:
            if (node.getChildCount() == 1) {
                onInFolder(node, null, node.getChild(0));
                evalWhereNode(node.getChild(0));
            } else {
                evalWhereNode(node.getChild(0));
                onInFolder(node, node.getChild(0), node.getChild(1));
                evalWhereNode(node.getChild(1));
            }
            break;
        case CmisQlStrictLexer.IN_TREE:
            if (node.getChildCount() == 1) {
                onInTree(node, null, node.getChild(0));
                evalWhereNode(node.getChild(0));
            } else {
                evalWhereNode(node.getChild(0));
                onInTree(node, node.getChild(0), node.getChild(1));
                evalWhereNode(node.getChild(1));
            }
            break;
        case CmisQlStrictLexer.SCORE:
            onScore(node);
            break;

        default:
            // do nothing;
        }
    }

    protected void evalTextSearchNode(Tree node) {
        // Ensure that we receive only valid tokens and nodes in the where
        // clause:
        LOG.debug("evaluating node: " + node.toString());
        switch (node.getType()) {
        case TextSearchLexer.TEXT_AND:
            List<Tree> children = getChildrenAsList(node);
            onPreTextAnd(node, children);
            for (Tree child : children)
                evalTextSearchNode(child);
            onTextAnd(node, children);
            onPostTextAnd(node, children);
            break;
        case TextSearchLexer.TEXT_OR:
            children = getChildrenAsList(node);
            onPreTextOr(node, children);
            for (Tree child : children)
                evalTextSearchNode(child);
            onTextOr(node, children);
            onPostTextOr(node, children);
            break;
        case TextSearchLexer.TEXT_MINUS:
            onTextMinus(node, node.getChild(0));
            break;
        case TextSearchLexer.TEXT_SEARCH_PHRASE_STRING_LIT:
            onTextPhrase(onTextLiteral(node));
            break;
        case TextSearchLexer.TEXT_SEARCH_WORD_LIT:
            onTextWord(onTextLiteral(node));
            break;
        }
    }
        
    // helper functions that are needed by most query tree walkers

    protected Object onLiteral(Tree node) {
        int type = node.getType();
        String text = node.getText();
        switch (type) {
        case CmisQlStrictLexer.BOOL_LIT:
            return Boolean.parseBoolean(node.getText());
        case CmisQlStrictLexer.NUM_LIT:
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                return Double.parseDouble(text);
            } else {
                return Long.parseLong(text);
            }
        case CmisQlStrictLexer.STRING_LIT:
            return text.substring(1, text.length()-1);
        case CmisQlStrictLexer.TIME_LIT:
            GregorianCalendar gc = CalendarHelper.fromString(text.substring(text.indexOf('\'')+1, text.lastIndexOf('\'')));
            return gc;
        default:
            throw new RuntimeException("Unknown literal. " + node);
        }
    }
    
    protected String onTextLiteral(Tree node) {
        int type = node.getType();
        String text = node.getText();
        switch (type) {
        case TextSearchLexer.TEXT_SEARCH_PHRASE_STRING_LIT:
            return StringUtil.unescape(text.substring(1, text.length()-1), null);
        case TextSearchLexer.TEXT_SEARCH_WORD_LIT:
            return StringUtil.unescape(text, null);
        default:
            throw new RuntimeException("Unknown text literal. " + node);
        }

    }

    protected List<Object> onLiteralList(Tree node) {
        List<Object> res = new ArrayList<Object>(node.getChildCount());
        for (int i=0; i<node.getChildCount(); i++) {
            Tree literal =  node.getChild(i);
            res.add(onLiteral(literal));
        }
        return res;
    }
    
    protected List<Tree> getChildrenAsList(Tree node) {
        List<Tree> res = new ArrayList<Tree>(node.getChildCount());
        for (int i=0; i<node.getChildCount(); i++) {
            Tree childNnode =  node.getChild(i);
            res.add(childNnode);
        }
        return res;
    }
}
