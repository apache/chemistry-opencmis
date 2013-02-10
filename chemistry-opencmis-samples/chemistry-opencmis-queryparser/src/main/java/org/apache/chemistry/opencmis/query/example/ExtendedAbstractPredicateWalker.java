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

import java.util.List;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.server.support.query.AbstractPredicateWalker;

public abstract class ExtendedAbstractPredicateWalker extends AbstractPredicateWalker {

    protected abstract void onStartProcessing(Tree whereNode);
    protected abstract void onStopProcessing();

    // Compare operators
    protected void onPreEquals(Tree eqNode, Tree leftNode, Tree rightNode) {        
    }
    protected abstract void onEquals(Tree eqNode, Tree leftNode, Tree rightNode);
    protected void onPostEquals(Tree eqNode, Tree leftNode, Tree rightNode) {        
    }

    protected void onPreNotEquals(Tree neNode, Tree leftNode, Tree rightNode) {        
    }
    protected abstract void onNotEquals(Tree neNode, Tree leftNode, Tree rightNode);
    protected void onPostNotEquals(Tree neNode, Tree leftNode, Tree rightNode) {        
    }
 
    protected void onPreGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode) {        
    }
    protected abstract void onGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode);
    protected void onPostGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode) {        
    }

    protected void onPreGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode) {        
    }
    protected abstract void onGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode);
    protected void onPostGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode) {        
    }

    protected void onPreLessThan(Tree ltNode, Tree leftNode, Tree rightNode)  {        
    }
    protected abstract void onLessThan(Tree ltNode, Tree leftNode, Tree rightNode);
    protected void onPostLessThan(Tree ltNode, Tree leftNode, Tree rightNode) {        
    }

    protected void onPreLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode) {        
    }
    protected abstract void onLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode);
    protected void onPostLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode) {        
    }

    // Boolean operators
    protected abstract void onNot(Tree opNode, Tree leftNode);
    protected void onPostNot(Tree opNode, Tree leftNode) {
    }
    protected void onPreAnd(Tree opNode, Tree leftNode, Tree rightNode) {
    }
    protected abstract void onAnd(Tree opNode, Tree leftNode, Tree rightNode);
    protected void onPostAnd(Tree opNode, Tree leftNode, Tree rightNode) {
    }
    protected void onPreOr(Tree opNode, Tree leftNode, Tree rightNode) {
    }
    protected abstract void onOr(Tree opNode, Tree leftNode, Tree rightNode);
    protected void onPostOr(Tree opNode, Tree leftNode, Tree rightNode) {
    }

    // Multi-value:
    protected void onPreIn(Tree node, Tree colNode, Tree listNode) {        
    }
    protected abstract void onIn(Tree node, Tree colNode, Tree listNode);
    protected void onPostIn(Tree node, Tree colNode, Tree listNode) {        
    }

    protected void onPreNotIn(Tree node, Tree colNode, Tree listNode) {        
    }
    protected abstract void onNotIn(Tree node, Tree colNode, Tree listNode);
    protected void onPostNotIn(Tree node, Tree colNode, Tree listNode) {        
    }

    protected void onPreInAny(Tree node, Tree colNode, Tree listNode) {        
    }
    protected abstract void onInAny(Tree node, Tree colNode, Tree listNode);
    protected void onPostInAny(Tree node, Tree colNode, Tree listNode) {        
    }

    protected void onPreNotInAny(Tree node, Tree colNode, Tree listNode) {        
    }
    protected abstract void onNotInAny(Tree node, Tree literalNode, Tree colNode);
    protected void onPostNotInAny(Tree node, Tree colNode, Tree listNode) {        
    }
    
    protected void onPreEqAny(Tree node, Tree literalNode, Tree colNode) {
    }
    protected abstract void onEqAny(Tree node, Tree literalNode, Tree colNode);
    protected void onPostEqAny(Tree node, Tree literalNode, Tree colNode) {        
    }

    // Null comparisons:
    protected abstract void onIsNull(Tree nullNode, Tree colNode);
    protected void onPostIsNull(Tree nullNode, Tree colNode) {
    }

    protected abstract void onIsNotNull(Tree notNullNode, Tree colNode);
    protected void onPostIsNotNull(Tree notNullNode, Tree colNode) {
    }
    
    // String matching:
    protected void onPreIsLike(Tree node, Tree colNode, Tree stringNode) {
    }
    protected abstract void onIsLike(Tree node, Tree colNode, Tree stringNode);
    protected void onPostIsLike(Tree node, Tree colNode, Tree stringNode) {
    }

    protected void onPreIsNotLike(Tree node, Tree colNode, Tree stringNode) {
    }
    protected abstract void onIsNotLike(Tree node, Tree colNode, Tree stringNode);
    protected void onPostIsNotLike(Tree node, Tree colNode, Tree stringNode) {
    }

    protected abstract void onInFolder(Tree node, Tree colNode, Tree paramNode);
    protected void onBetweenInFolder(Tree node, Tree colNode, Tree paramNode) {
    }
    protected void onPostInFolder(Tree node, Tree colNode, Tree paramNode) {
    }

    protected abstract void onInTree(Tree node, Tree colNode, Tree paramNode);
    protected void onBetweenInTree(Tree node, Tree colNode, Tree paramNode) {
    }
    protected void onPostInTree(Tree node, Tree colNode, Tree paramNode) {
    }
    
    protected abstract void onScore(Tree node);
    protected abstract void onColNode(Tree node);
    
    protected void onPreTextAnd(Tree node, List<Tree> conjunctionNodes) {
    }
    protected abstract void onTextAnd(Tree node, List<Tree> conjunctionNodes, int index);
    protected void onPostTextAnd(Tree node, List<Tree> conjunctionNodes) {
    }
    protected void onPreTextOr(Tree node, List<Tree> termNodes) {
    }
    protected abstract void onTextOr(Tree node, List<Tree> termNodes, int index);
    protected void onPostTextOr(Tree node, List<Tree> termNodes) {
    }
    protected abstract void onTextMinus(Tree node, Tree notNode);
    protected void onPostTextMinus(Tree node, Tree notNode) {        
    }
    protected abstract void onTextWord(String word);
    protected abstract void onTextPhrase(String phrase);
    
    protected void onPreContains(Tree node, Tree typeNode, Tree searchExprNode) {        
    }
    
    protected abstract void onContains(Tree node, Tree typeNode, Tree searchExprNode);
    
    protected void onBetweenContains(Tree node, Tree typeNode, Tree searchExprNode) {        
    }

}
