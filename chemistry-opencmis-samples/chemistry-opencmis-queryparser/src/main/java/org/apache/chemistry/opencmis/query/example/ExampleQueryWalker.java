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
package org.apache.chemistry.opencmis.query.example;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.server.support.query.StringUtil;
import org.apache.chemistry.opencmis.server.support.query.TextSearchLexer;

public class ExampleQueryWalker extends ExtendedAbstractPredicateWalker {

    private StringBuffer result = new StringBuffer();
    
    public String getResult() {
        return result.toString();
    }

    @Override
    public Boolean walkPredicate(Tree whereNode) {
        if (null != whereNode) {
            onStartProcessing(whereNode);
            super.walkPredicate(whereNode);
            onStopProcessing();
        }
        return null; // unused
    }

    @Override
    public Boolean walkNot(Tree opNode, Tree node) {
        onNot(opNode, node);
        super.walkPredicate(node);
        onPostNot(opNode, node);
        return false;
    }

    @Override
    public Boolean walkAnd(Tree opNode, Tree leftNode, Tree rightNode) {
        onPreAnd(opNode, leftNode, rightNode);
        super.walkPredicate(leftNode);
        onAnd(opNode, leftNode, rightNode);
        super.walkPredicate(rightNode);
        onPostAnd(opNode, leftNode, rightNode);
        return false;
    }

    @Override
    public Boolean walkOr(Tree opNode, Tree leftNode, Tree rightNode) {
        onPreOr(opNode, leftNode, rightNode);
        super.walkPredicate(leftNode);
        onOr(opNode, leftNode, rightNode);
        super.walkPredicate(rightNode);
        onPostOr(opNode, leftNode, rightNode);
        return false;
    }

    @Override
    public Boolean walkEquals(Tree opNode, Tree leftNode, Tree rightNode) {
        onPreEquals(opNode, leftNode, rightNode);
        super.walkPredicate(leftNode);
        onEquals(opNode, leftNode, rightNode);
        super.walkPredicate(rightNode);
        onPostEquals(opNode, leftNode, rightNode);
        return false;
    }

    @Override
    public Boolean walkNotEquals(Tree opNode, Tree leftNode, Tree rightNode) {
        onPreNotEquals(opNode, leftNode, rightNode);
        super.walkPredicate(leftNode);
        onNotEquals(opNode, leftNode, rightNode);
        super.walkPredicate(rightNode);
        onPostNotEquals(opNode, leftNode, rightNode);
        return false;
    }

    @Override
    public Boolean walkGreaterThan(Tree opNode, Tree leftNode, Tree rightNode) {
        onPreGreaterThan(opNode, leftNode, rightNode);
        super.walkPredicate(leftNode);
        onGreaterThan(opNode, leftNode, rightNode);
        super.walkPredicate(rightNode);
        onPostGreaterThan(opNode, leftNode, rightNode);
        return false;
    }

    @Override
    public Boolean walkGreaterOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
        onPreGreaterOrEquals(opNode, leftNode, rightNode);
        super.walkPredicate(leftNode);
        onGreaterOrEquals(opNode, leftNode, rightNode);
        super.walkPredicate(rightNode);
        onPostGreaterOrEquals(opNode, leftNode, rightNode);
        return false;
    }

    @Override
    public Boolean walkLessThan(Tree opNode, Tree leftNode, Tree rightNode) {
        onPreLessThan(opNode, leftNode, rightNode);
        super.walkPredicate(leftNode);
        onLessThan(opNode, leftNode, rightNode);
        super.walkPredicate(rightNode);
        onPostLessThan(opNode, leftNode, rightNode);
        return false;
    }

    @Override
    public Boolean walkLessOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
        onPreLessOrEquals(opNode, leftNode, rightNode);
        super.walkPredicate(leftNode);
        onLessOrEquals(opNode, leftNode, rightNode);
        super.walkPredicate(rightNode);
        onPostLessOrEquals(opNode, leftNode, rightNode);
        return false;
    }

    @Override
    public Boolean walkIn(Tree opNode, Tree colNode, Tree listNode) {
        onPreIn(opNode, colNode, listNode);
        super.walkPredicate(colNode);
        onIn(opNode, colNode, listNode);
        super.walkPredicate(listNode);
        onPostIn(opNode, colNode, listNode);
        return false;
    }

    @Override
    public Boolean walkNotIn(Tree opNode, Tree colNode, Tree listNode) {
        onPreNotIn(opNode, colNode, listNode);
        super.walkPredicate(colNode);
        onNotIn(opNode, colNode, listNode);
        super.walkPredicate(listNode);
        onPostNotIn(opNode, colNode, listNode);
        return false;
    }

    @Override
    public Boolean walkInAny(Tree opNode, Tree colNode, Tree listNode) {
        onPreInAny(opNode, colNode, listNode);
        super.walkPredicate(colNode);
        onInAny(opNode, colNode, listNode);
        super.walkPredicate(listNode);
        onPostInAny(opNode, colNode, listNode);
        return false;
    }

    @Override
    public Boolean walkNotInAny(Tree opNode, Tree colNode, Tree listNode) {
        onPreNotInAny(opNode, colNode, listNode);
        super.walkPredicate(colNode);
        onNotInAny(opNode, colNode, listNode);
        super.walkPredicate(listNode);
        onPostNotInAny(opNode, colNode, listNode);
        return false;
    }

    @Override
    public Boolean walkEqAny(Tree opNode, Tree literalNode, Tree colNode) {
        onPreEqAny(opNode, literalNode, colNode);
        super.walkPredicate(literalNode);
        onEqAny(opNode, literalNode, colNode);
        super.walkPredicate(colNode);
        onPostEqAny(opNode, literalNode, colNode);
        return false;
    }

    @Override
    public Boolean walkIsNull(Tree opNode, Tree colNode) {
        onIsNull(opNode, colNode);
        super.walkPredicate(colNode);
        onPostIsNull(opNode, colNode);
        return false;
    }

    @Override
    public Boolean walkIsNotNull(Tree opNode, Tree colNode) {
        onIsNotNull(opNode, colNode);
        super.walkPredicate(colNode);
        onPostIsNotNull(opNode, colNode);
        return false;
    }

    @Override
    public Boolean walkLike(Tree opNode, Tree colNode, Tree stringNode) {
        onPreIsLike(opNode, colNode, stringNode);
        super.walkPredicate(colNode);
        onIsLike(opNode, colNode, stringNode);
        super.walkPredicate(stringNode);
        onPostIsLike(opNode, colNode, stringNode);
        return false;
    }

    @Override
    public Boolean walkNotLike(Tree opNode, Tree colNode, Tree stringNode) {
        onPreIsNotLike(opNode, colNode, stringNode);
        super.walkPredicate(colNode);
        onIsNotLike(opNode, colNode, stringNode);
        super.walkPredicate(stringNode);
        onPostIsNotLike(opNode, colNode, stringNode);
        return false;
    }

    @Override
    public Boolean walkContains(Tree opNode, Tree typeNode, Tree textSearchNode) {

        onPreContains(opNode, typeNode, textSearchNode);
        if (opNode.getChildCount() > 1) {
            super.walkPredicate(typeNode);
            onBetweenContains(opNode, typeNode, textSearchNode);
        } else
            result.append("'");
        super.walkSearchExpr(textSearchNode);
        onContains(opNode, typeNode, textSearchNode);
        return false;                
    }

    @Override
    public Boolean walkInFolder(Tree opNode, Tree typeNode, Tree paramNode) {
        if (opNode.getChildCount() == 1) {
            onInFolder(opNode, paramNode, null);
            super.walkPredicate(paramNode);
            onPostInFolder(opNode, paramNode, null);
        } else {
            onInFolder(opNode, typeNode, paramNode);
            super.walkPredicate(typeNode);
            onBetweenInFolder(opNode, typeNode, paramNode);
            super.walkPredicate(paramNode);
            onPostInFolder(opNode, typeNode, paramNode);
        }
        return false;
    }

    @Override
    public Boolean walkInTree(Tree opNode, Tree typeNode, Tree paramNode) {
        if (opNode.getChildCount() == 1) {
            onInTree(opNode, paramNode, null);
            super.walkPredicate(paramNode);
            onPostInTree(opNode, paramNode, null);
        } else {
            onInTree(opNode, typeNode, paramNode);
            super.walkPredicate(typeNode);
            onBetweenInTree(opNode, typeNode, paramNode);
            super.walkPredicate(paramNode);
            onPostInTree(opNode, typeNode, paramNode);
        }
        return false;
    }

    @Override
    public Object walkCol(Tree node) {
        onColNode(node);
        return null;
    }

    @Override
    public Object walkId(Tree node) {
        return onId(node);
    }
    
    @Override
    public Boolean walkScore(Tree node) {
        onScore(node);
        return false;
    }
    
    @Override
    protected Boolean walkTextAnd(Tree node) {
        List<Tree> children = getChildrenAsList(node);
        onPreTextAnd(node, children);
        int i=0;
        for (Tree child : children) {
            walkSearchExpr(child);
            onTextAnd(node, children, i++);
        }
        onPostTextAnd(node, children);
        return false;
    }
    
    @Override
    protected Boolean walkTextOr(Tree node) {
        List<Tree> children = getChildrenAsList(node);
        onPreTextOr(node, children);
        int j=0;
        for (Tree child : children) {
            walkSearchExpr(child);
            onTextOr(node, children, j++);
        }
        onPostTextOr(node, children);
        return false;
    }
    
    @Override
    protected Boolean walkTextMinus(Tree node) {
        onTextMinus(node, node.getChild(0));
        walkSearchExpr(node.getChild(0));
        onPostTextMinus(node, node.getChild(0));
        return false;
    }
    
    @Override
    protected Boolean walkTextWord(Tree node) {
        onTextWord(onTextLiteral(node));
        return false;
    }
    
    @Override
    protected Boolean walkTextPhrase(Tree node) {
        onTextPhrase(onTextLiteral(node));
        return false;
    }
    
    @Override
    public Object walkBoolean(Tree node) {
        Object obj = super.walkNumber(node);
        result.append(obj);
        return obj;
    }

    @Override
    public Object walkNumber(Tree node) {
        Object obj = super.walkNumber(node);
        result.append(obj);
        return obj;
    }

    @Override
    public Object walkString(Tree node) {
        String val =  "'" + super.walkString(node) + "'";
        result.append(val);
        return val;
    }

    @Override
    public Object walkTimestamp(Tree node) {
        String s = (String) super.walkTimestamp(node);
        result.append(s);
        return s;
    }

    @Override
    public Object walkList(Tree node) {
        result.append("(");
        int n = node.getChildCount();
        boolean first = true;
        List<Object> res = new ArrayList<Object>(n);
        for (int i = 0; i < n; i++) {
            Object o = node.getChild(i);
            if (first)
                first = false;
            else
                result.append(", ");
            result.append(o.toString());
        }
        
        result.append(")");

        return res;
    }
    
    private void endSubExpr() {
        result.append(")");        
    }

    private void startSubExpr() {
        result.append("(");        
    }

    @Override
    protected void onStartProcessing(Tree whereNode) {
        result.append("WHERE ");        
    }

    @Override
    protected void onStopProcessing() {
    }

    @Override
    protected void onPreEquals(Tree eqNode, Tree leftNode, Tree rightNode) {
        startSubExpr();
    }
    
    @Override
    protected void onEquals(Tree eqNode, Tree leftNode, Tree rightNode) {
        result.append(" = ");
    }

    @Override
    protected void onPostEquals(Tree eqNode, Tree leftNode, Tree rightNode) {
        endSubExpr();
    }

    @Override
    protected void onNotEquals(Tree neNode, Tree leftNode, Tree rightNode) {
        result.append(" <> ");
    }

    @Override
    protected void onGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode) {
        result.append(" > ");
    }

    @Override
    protected void onGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode) {
        result.append(" >= ");
    }

    @Override
    protected void onLessThan(Tree ltNode, Tree leftNode, Tree rightNode) {
        result.append(" < ");
    }

    @Override
    protected void onLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode) {
        result.append(" <= ");
    }

    @Override
    protected void onNot(Tree opNode, Tree leftNode) {
        result.append("NOT (");
    }

    @Override
    protected void onPostNot(Tree opNode, Tree leftNode) {
        endSubExpr();
    }

    @Override
    protected void onPreAnd(Tree opNode, Tree leftNode, Tree rightNode) {
        startSubExpr();
    }
    
    @Override
    protected void onAnd(Tree opNode, Tree leftNode, Tree rightNode) {
        result.append(" AND ");
    }
    
    @Override
    protected void onPostAnd(Tree opNode, Tree leftNode, Tree rightNode) {
        endSubExpr();
    }

    @Override
    protected void onPreOr(Tree opNode, Tree leftNode, Tree rightNode) {
        startSubExpr();
    }
    
    @Override
    protected void onOr(Tree opNode, Tree leftNode, Tree rightNode) {
        result.append(" OR ");
    }

    @Override
    protected void onPostOr(Tree opNode, Tree leftNode, Tree rightNode) {
        endSubExpr();
    }
    
    @Override
    protected void onIn(Tree node, Tree colNode, Tree listNode) {
        result.append(" IN ");
    }

    @Override
    protected void onNotIn(Tree node, Tree colNode, Tree listNode) {
        result.append(" NOT IN ");
    }

    @Override
    protected void onPreInAny(Tree node, Tree colNode, Tree listNode) {
        result.append("ANY ");
    }
    
    @Override
    protected void onInAny(Tree node, Tree colNode, Tree listNode) {
        result.append(" IN ");
    }

    @Override
    protected void onPreNotInAny(Tree node, Tree colNode, Tree listNode) {
        result.append("ANY ");
    }

    @Override
    protected void onNotInAny(Tree node, Tree colNode, Tree listNode) {
        result.append(" NOT IN ");
    }

    @Override
    protected void onEqAny(Tree node, Tree literalNode, Tree colNode) {
        result.append(" = ANY ");
    }

    @Override
    protected void onIsNull(Tree nullNode, Tree colNode) {
    }

    @Override
    protected void onPostIsNull(Tree nullNode, Tree colNode) {
        result.append(" IS NULL");
    }

    @Override
    protected void onIsNotNull(Tree notNullNode, Tree colNode) {
    }

    @Override
    protected void onPostIsNotNull(Tree notNullNode, Tree colNode) {
        result.append(" IS NOT NULL");
    }
    
    @Override
    protected void onPreIsLike(Tree node, Tree colNode, Tree stringNode) {
        startSubExpr();
    }

    @Override
    protected void onIsLike(Tree node, Tree colNode, Tree stringNode) {
        result.append(" LIKE ");
    }

    @Override
    protected void onPostIsLike(Tree node, Tree colNode, Tree stringNode) {
        endSubExpr();
    }

    @Override
    protected void onIsNotLike(Tree node, Tree colNode, Tree stringNode) {
        result.append(" NOT LIKE ");
    }

    @Override
    protected void onInFolder(Tree node, Tree colNode, Tree paramNode) {
        result.append("IN_FOLDER(");
    }
    
    protected void onBetweenInFolder(Tree node, Tree colNode, Tree paramNode) {
        result.append(", ");
    }

    @Override
    protected void onPostInFolder(Tree node, Tree colNode, Tree paramNode) {
        result.append(")");
    }

    @Override
    protected void onInTree(Tree node, Tree colNode, Tree paramNode) {
        result.append("IN_TREE(");
    }

    @Override
    protected void onBetweenInTree(Tree node, Tree colNode, Tree paramNode) {
        result.append(", ");
    }
    
    @Override
    protected void onPostInTree(Tree node, Tree colNode, Tree paramNode) {
        result.append(")");
    }
    
    @Override
    protected void onScore(Tree node) {
        result.append("SCORE()");
    }

    @Override
    protected void onColNode(Tree node) {
        result.append(node.getChild(0).getText());
    }

    @Override
    protected void onPreContains(Tree node, Tree typeNode, Tree searchExprNode) {
        result.append("CONTAINS(");
    }
    
    @Override
    protected void onBetweenContains(Tree node, Tree typeNode, Tree searchExprNode) {
        result.append(", '");
    }
   
    @Override
    protected void onContains(Tree node, Tree typeNode, Tree searchExprNode) {
        result.append("')");
    }

    @Override
    protected void onTextAnd(Tree node, List<Tree> conjunctionNodes, int index) {
        if (index < conjunctionNodes.size()-1)
            result.append(" ");        
    }

    @Override
    protected void onTextOr(Tree node, List<Tree> termNodes, int index) {
        if (index < termNodes.size()-1)
            result.append(" OR ");        
    }

    @Override
    protected void onTextMinus(Tree node, Tree notNode) {
        result.append("-");        
    }

    @Override
    protected void onTextWord(String word) {
        result.append(word);        
    }

    @Override
    protected void onTextPhrase(String phrase) {
       result.append("\\'" + phrase + "\\'");
    } 

    private String onId(Tree node) {
        String id = node.getText();
        result.append(id);
        return id;
    }
    
    private List<Tree> getChildrenAsList(Tree node) {
        List<Tree> res = new ArrayList<Tree>(node.getChildCount());
        for (int i=0; i<node.getChildCount(); i++) {
            Tree childNnode =  node.getChild(i);
            res.add(childNnode);
        }
        return res;
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

}
