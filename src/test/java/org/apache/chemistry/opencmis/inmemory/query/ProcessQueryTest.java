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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.inmemory.TypeManagerImpl;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer;
import org.apache.chemistry.opencmis.server.support.query.QueryConditionProcessor;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProcessQueryTest extends AbstractQueryTest {
    
    static private class TestQueryProcessor implements QueryConditionProcessor {

        private static Log log = LogFactory.getLog(ProcessQueryTest.class);
        
        private static final String ON_START = "onStartWasCalled";
        private static final String ON_STOP = "onStopWasCalled";
        private static final String ON_EQUALS_WASCALLED = "onEqualsWasCalled";
        private static final String ON_NOT_EQUALS = "onNotEqualsWasCalled";
        private static final String ON_GREATER_THAN = "onGreaterThanWasCalled";
        private static final String ON_GREATER_OR_EQUALS ="onGreaterOrEqualsWasCalled";
        private static final String ON_LESS_THAN = "onLessThanWasCalled";
        private static final String ON_LESS_OR_EQUALS = "onLessOrEqualsWasCalled";
        private static final String ON_NOT = "onNotWasCalled";
        private static final String ON_AND = "onAndWasCalled";
        private static final String ON_OR = "onOrWasCalled";
        private static final String ON_IN = "onInWasCalled";
        private static final String ON_NOT_IN = "onNotInWasCalled";
        private static final String ON_IN_ANY = "onInAnyWasCalled";
        private static final String ON_NOT_IN_ANY ="onNotInAnyWasCalled";
        private static final String ON_EQ_ANY = "onEqAnyWasCalled";
        private static final String ON_IS_NULL = "onIsNullWasCalled";
        private static final String ON_IS_NOT_NULL ="onIsNotNullWasCalled";
        private static final String ON_IS_LIKE = "onIsLikeWasCalled";
        private static final String ON_IS_NOT_LIKE = "onIsNotLikeWasCalled";
        private static final String ON_CONTAINS  = "onContainsWasCalled";
        private static final String ON_IN_FOLDER  = "onInFolderWasCalled";
        private static final String ON_IN_TREE  = "onInTreeWasCalled";
        private static final String ON_SCORE = "onScoreWasCalled";

        
        final Map<String, Boolean> rulesTrackerMap = 
            new HashMap<String, Boolean>() {
                private static final long serialVersionUID = 1L;
            { 
                put(ON_START, false);
                put(ON_STOP, false);
                put(ON_EQUALS_WASCALLED, false);
                put(ON_NOT_EQUALS, false);
                put(ON_GREATER_THAN, false);
                put(ON_GREATER_OR_EQUALS, false);
                put(ON_LESS_THAN, false);
                put(ON_LESS_OR_EQUALS, false);
                put(ON_NOT, false);
                put(ON_AND, false);
                put(ON_OR, false);
                put(ON_IN, false);
                put(ON_NOT_IN, false);
                put(ON_IN_ANY, false);
                put(ON_NOT_IN_ANY, false);
                put(ON_EQ_ANY, false);
                put(ON_IS_NULL, false);
                put(ON_IS_NOT_NULL, false);
                put(ON_IS_LIKE, false);
                put(ON_IS_NOT_LIKE, false);
                put(ON_CONTAINS, false);
                put(ON_IN_FOLDER, false);
                put(ON_IN_TREE, false);
                put(ON_SCORE, false);
           }};
        
       public TestQueryProcessor() {
        }
        

       public void onStartProcessing(Tree node) {
            log.debug("TestQueryProcessor:onStartProcessing()");
            rulesTrackerMap.put(ON_START, true);
            assertEquals(CmisQlStrictLexer.WHERE, node.getType());
       }

       public void onStopProcessing() {
           log.debug("TestQueryProcessor:onStopProcessing()");
           rulesTrackerMap.put(ON_STOP, true);
       }


       public void onEquals(Tree eqNode, Tree leftNode, Tree rightNode) {
           rulesTrackerMap.put(ON_EQUALS_WASCALLED, true);
           assertEquals(CmisQlStrictLexer.EQ, eqNode.getType());
           assertTrue(CmisQlStrictLexer.COL==leftNode.getType() || CmisQlStrictLexer.SCORE==leftNode.getType());
           assertTrue(isLiteral(rightNode));
       }

       public void onNotEquals(Tree neNode, Tree leftNode, Tree rightNode) {
           rulesTrackerMap.put(ON_NOT_EQUALS, true);
           assertEquals(CmisQlStrictLexer.NEQ, neNode.getType());
           assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
           assertTrue(isLiteral(rightNode));
           Object value=onLiteral(rightNode, Integer.class);
           assertEquals(100, value);            
       }

       public void onLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode) {
           rulesTrackerMap.put(ON_LESS_OR_EQUALS, true);
           assertEquals(CmisQlStrictLexer.LTEQ, leqNode.getType());
           assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
           assertTrue(isLiteral(rightNode));
           Object value=onLiteral(rightNode, Integer.class);
           assertEquals(100, value);            
       }

       public void onLessThan(Tree ltNode, Tree leftNode, Tree rightNode) {
           rulesTrackerMap.put(ON_LESS_THAN, true);
           assertEquals(CmisQlStrictLexer.LT, ltNode.getType());
           assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
           assertTrue(isLiteral(rightNode));
           Object value=onLiteral(rightNode, Integer.class);
           assertEquals(100, value);            
       }

       public void onGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode) {
           rulesTrackerMap.put(ON_GREATER_OR_EQUALS, true);
           assertEquals(CmisQlStrictLexer.GTEQ, geNode.getType());
           assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
           assertTrue(isLiteral(rightNode));
           Object value=onLiteral(rightNode, Integer.class);
           assertEquals(100, value);            
       }

       public void onGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode) {
           rulesTrackerMap.put(ON_GREATER_THAN, true);
           assertEquals(CmisQlStrictLexer.GT, gtNode.getType());
           assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
           assertTrue(isLiteral(rightNode));
           Object value=onLiteral(rightNode, Integer.class);
           assertEquals(100, value);            
       }

       public void onNot(Tree opNode, Tree leftNode) {
           rulesTrackerMap.put(ON_NOT, true);
           assertEquals(CmisQlStrictLexer.NOT, opNode.getType());
       }

       public void onAnd(Tree opNode, Tree leftNode, Tree rightNode) {
           assertEquals(CmisQlStrictLexer.AND, opNode.getType());
           rulesTrackerMap.put(ON_AND, true);
       }

       public void onOr(Tree opNode, Tree leftNode, Tree rightNode) {
           assertEquals(CmisQlStrictLexer.OR, opNode.getType());
           rulesTrackerMap.put(ON_OR, true);
       }

       public void onIn(Tree opNode, Tree colNode, Tree listNode) {
           assertEquals(CmisQlStrictLexer.IN, opNode.getType());
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertEquals(CmisQlStrictLexer.IN_LIST, listNode.getType());
           Object value=onLiteral(listNode.getChild(0), String.class);
           assertEquals("'Joe'", value);            
           value=onLiteral(listNode.getChild(1), String.class);
           assertEquals("'Jim'", value);            
           rulesTrackerMap.put(ON_IN, true);
       }

       public void onNotIn(Tree node, Tree colNode, Tree listNode) {
           assertEquals(CmisQlStrictLexer.NOT_IN, node.getType());
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertEquals(CmisQlStrictLexer.IN_LIST, listNode.getType());
           Object value=onLiteral(listNode.getChild(0), String.class);
           assertEquals("'Joe'", value);            
           value=onLiteral(listNode.getChild(1), String.class);
           assertEquals("'Jim'", value);            
           rulesTrackerMap.put(ON_NOT_IN, true);
       }

       public void onEqAny(Tree node, Tree literalNode, Tree colNode) {
           assertEquals(CmisQlStrictLexer.EQ_ANY, node.getType());
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertTrue(isLiteral(literalNode));
           Object value=onLiteral(literalNode, String.class);
           assertEquals("'Joe'", value);            
           rulesTrackerMap.put(ON_EQ_ANY, true);
       }

       public void onInAny(Tree node, Tree colNode, Tree listNode) {
           assertEquals(CmisQlStrictLexer.IN_ANY, node.getType());
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertEquals(CmisQlStrictLexer.IN_LIST, listNode.getType());
           Object value=onLiteral(listNode.getChild(0), String.class);
           assertEquals("'Joe'", value);            
           value=onLiteral(listNode.getChild(1), String.class);
           assertEquals("'Jim'", value);            
           rulesTrackerMap.put(ON_IN_ANY, true);
       }

       public void onNotInAny(Tree node, Tree colNode, Tree listNode) {
           assertEquals(CmisQlStrictLexer.NOT_IN_ANY, node.getType());
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertEquals(CmisQlStrictLexer.IN_LIST, listNode.getType());
           Object value=onLiteral(listNode.getChild(0), String.class);
           assertEquals("'Joe'", value);            
           value=onLiteral(listNode.getChild(1), String.class);
           assertEquals("'Jim'", value);            
           rulesTrackerMap.put(ON_NOT_IN_ANY, true);
       }

       public void onIsNull(Tree nullNode, Tree colNode) {
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertEquals(CmisQlStrictLexer.IS_NULL, nullNode.getType());
           rulesTrackerMap.put(ON_IS_NULL, true);
       }

       public void onIsNotNull(Tree notNullNode, Tree colNode) {
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertEquals(CmisQlStrictLexer.IS_NOT_NULL, notNullNode.getType());
           rulesTrackerMap.put(ON_IS_NOT_NULL, true);
       }

       public void onIsLike(Tree node, Tree colNode, Tree stringNode) {
           assertEquals(CmisQlStrictLexer.LIKE, node.getType());
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertEquals(CmisQlStrictLexer.STRING_LIT, stringNode.getType());
           Object value=onLiteral(stringNode, String.class);
           assertEquals("'Harry*'", value);            
           rulesTrackerMap.put(ON_IS_LIKE, true);
       }

       public void onIsNotLike(Tree node, Tree colNode, Tree stringNode) {
           assertEquals(CmisQlStrictLexer.NOT_LIKE, node.getType());
           assertEquals(CmisQlStrictLexer.COL, colNode.getType());
           assertEquals(CmisQlStrictLexer.STRING_LIT, stringNode.getType());
           Object value=onLiteral(stringNode, String.class);
           assertEquals("'Harry*'", value);            
           rulesTrackerMap.put(ON_IS_NOT_LIKE, true);
       }

       public void onContains(Tree node, Tree colNode, Tree paramNode) {
           assertEquals(CmisQlStrictLexer.CONTAINS, node.getType());
           assertTrue(colNode==null || CmisQlStrictLexer.STRING_LIT == paramNode.getType());
           assertEquals(CmisQlStrictLexer.STRING_LIT, paramNode.getType());
           rulesTrackerMap.put(ON_CONTAINS, true);
       }

       public void onInFolder(Tree node, Tree colNode, Tree paramNode) {
           assertEquals(CmisQlStrictLexer.IN_FOLDER, node.getType());
           assertTrue(colNode==null || CmisQlStrictLexer.STRING_LIT == paramNode.getType());
           assertEquals(CmisQlStrictLexer.STRING_LIT, paramNode.getType());
           rulesTrackerMap.put(ON_IN_FOLDER, true);
       }

       public void onInTree(Tree node, Tree colNode, Tree paramNode) {
           assertEquals(CmisQlStrictLexer.IN_TREE, node.getType());
           assertTrue(colNode==null || CmisQlStrictLexer.STRING_LIT == paramNode.getType());
           assertEquals(CmisQlStrictLexer.STRING_LIT, paramNode.getType());
           rulesTrackerMap.put(ON_IN_TREE, true);
       }

       public void onScore(Tree node, Tree paramNode) {
           assertEquals(CmisQlStrictLexer.SCORE, node.getType());
           rulesTrackerMap.put(ON_SCORE, true);
       }


       // private helper functions:

       private boolean isLiteral(Tree node) {
           int type = node.getType();
           return type==CmisQlStrictLexer.BOOL_LIT || type==CmisQlStrictLexer.NUM_LIT ||
           type==CmisQlStrictLexer.STRING_LIT || type==CmisQlStrictLexer.TIME_LIT;
       }

       private Object onLiteral(Tree node, Class<?> clazz) {
           int type = node.getType();
           switch (type) {
           case CmisQlStrictLexer.BOOL_LIT:
               return clazz==Boolean.class ? Boolean.parseBoolean(node.getText()) : null;
           case CmisQlStrictLexer.NUM_LIT:
               if (clazz == Integer.class)
                   return Integer.parseInt(node.getText());
               else if (clazz == Long.class)
                   return Long.parseLong(node.getText());
               else if (clazz == Short.class)
                   return Short.parseShort(node.getText());
               else if (clazz == Double.class)
                   return Double.parseDouble(node.getText());
               else if (clazz == Float.class)
                   return Float.parseFloat(node.getText());
               else return null;
           case CmisQlStrictLexer.STRING_LIT:
               return clazz==String.class ? node.getText() : null;
           case CmisQlStrictLexer.TIME_LIT:
               return clazz==GregorianCalendar.class ?  CalendarHelper.fromString(node.getText()) : null; 
           default:
               log.error("Unknown literal. " + node);
               return null;
           }
       }

    }

    private TypeManagerImpl tm;
    private TestQueryProcessor queryProcessor;

    @Before
    public void setUp() throws Exception {
        tm = new TypeManagerImpl();
        tm.initTypeSystem(null); // create CMIS default types

        // create some types for testing
        List<TypeDefinition> typeDefs = super.createTypes();
        for (TypeDefinition typeDef : typeDefs)
            tm.addTypeDefinition(typeDef);

        // initialize query object with type manager
        queryProcessor = new TestQueryProcessor();
        QueryObject qo = new QueryObject(tm, queryProcessor);
        super.setUp(qo);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testStartStopProcessing() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = 100"; 
        traverseStatementAndCatchExc(statement); // calls query processor
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_START));
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_STOP));
    }

    @Test
    public void testEq() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = 100"; 
        testStatement(statement, TestQueryProcessor.ON_EQUALS_WASCALLED);
    }

    @Test
    public void testNeq() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN <> 100"; 
        testStatement(statement, TestQueryProcessor.ON_NOT_EQUALS);
    }

    @Test
    public void testLt() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN < 100"; 
        testStatement(statement, TestQueryProcessor.ON_LESS_THAN);
    }

    @Test
    public void testLteq() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN <= 100"; 
        testStatement(statement, TestQueryProcessor.ON_LESS_OR_EQUALS);
    }

    @Test
    public void testGt() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN > 100"; 
        testStatement(statement, TestQueryProcessor.ON_GREATER_THAN);
    }

    @Test
    public void testGteq() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN >= 100"; 
        testStatement(statement, TestQueryProcessor.ON_GREATER_OR_EQUALS);
    }

    @Test
    public void testNot() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE NOT ISBN = 100"; 
        testStatementMultiRule(statement, TestQueryProcessor.ON_NOT);
    }

    @Test
    public void testAnd() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = 100 AND Title='Harry'"; 
        testStatementMultiRule(statement, TestQueryProcessor.ON_AND);
    }

    @Test
    public void testOr() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = 100 OR Title='Harry'"; 
        testStatementMultiRule(statement,TestQueryProcessor.ON_OR);
    }

    @Test
    public void testIn() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE Author IN ('Joe', 'Jim')"; 
        testStatement(statement, TestQueryProcessor.ON_IN);
    }

    @Test
    public void testNotIn() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE Author NOT IN ('Joe', 'Jim')"; 
        testStatement(statement, TestQueryProcessor.ON_NOT_IN);
    }

    @Test
    public void testEqAny() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE 'Joe' = ANY Author"; 
        testStatement(statement, TestQueryProcessor.ON_EQ_ANY);
    }

    @Test
    public void testInAny() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ANY Author IN ('Joe', 'Jim')"; 
        testStatement(statement, TestQueryProcessor.ON_IN_ANY);
    }

    @Test
    public void testNotInAny() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ANY Author NOT IN ('Joe', 'Jim')"; 
        testStatement(statement, TestQueryProcessor.ON_NOT_IN_ANY);
    }

    @Test
    public void testOnIsNullWasCalled() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE Author IS NULL"; 
        testStatement(statement, TestQueryProcessor.ON_IS_NULL);        
    }

    @Test
    public void testOnIsNotNullWasCalled() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE Author IS NOT NULL"; 
        testStatement(statement, TestQueryProcessor.ON_IS_NOT_NULL);        
    }

    @Test
    public void testOnLikeWasCalled() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE Author LIKE 'Harry*'"; 
        testStatement(statement, TestQueryProcessor.ON_IS_LIKE);        
    }

    @Test
    public void testOnNotLikeWasCalled() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE Author NOT LIKE 'Harry*'"; 
        testStatement(statement, TestQueryProcessor.ON_IS_NOT_LIKE);        
    }

    @Test
    public void testOnContainsWasCalled1() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE CONTAINS('Hello')"; 
        testStatement(statement, TestQueryProcessor.ON_CONTAINS);        
    }

    @Test
    public void testOnContainsWasCalled2() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE CONTAINS(BookType, 'Harry')";
        testStatement(statement, TestQueryProcessor.ON_CONTAINS);        
    }

    @Test
    public void testOnInFolderWasCalled1() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE IN_FOLDER('ID1234')"; 
        testStatement(statement, TestQueryProcessor.ON_IN_FOLDER);        
    }

    @Test
    public void testOnInFolderWasCalled2() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE IN_FOLDER(BookType, 'ID1234')";
        testStatement(statement, TestQueryProcessor.ON_IN_FOLDER);        
    }

    @Test
    public void testOnInTreeWasCalled1() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE IN_Tree('ID1234')"; 
        testStatement(statement, TestQueryProcessor.ON_IN_TREE);        
    }

    @Test
    public void testOnInTreeWasCalled2() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE IN_Tree(BookType, 'ID1234')"; 
        testStatement(statement, TestQueryProcessor.ON_IN_TREE);        
    }

    @Test
    public void testOnScoreCalled() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE SCORE()=100"; 
        testStatementMultiRule(statement, TestQueryProcessor.ON_SCORE);        
    }

    // private helper functions

    private void testStatementMultiRule(String statement, String ruleAssertion) {
        traverseStatementAndCatchExc(statement); // calls query processor
        Tree whereRoot = getWhereTree(parserTree);
        assertTrue(queryProcessor.rulesTrackerMap.get(ruleAssertion));
    }

    private void testStatement(String statement, String ruleAssertion) {
        testStatementMultiRule(statement, ruleAssertion);
        checkOtherRulesNotCalled(ruleAssertion);
    }

    private void checkOtherRulesNotCalled(String ruleAssertion) {
        for (Entry<String, Boolean> e : queryProcessor.rulesTrackerMap.entrySet()) {
            if (!e.getKey().equals(ruleAssertion) && !e.getKey().equals("onPropertyValueWasCalled")
                    && !e.getKey().equals(TestQueryProcessor.ON_START) && !e.getKey().equals(TestQueryProcessor.ON_STOP)
                    && !e.getKey().contains("Literal"))
                assertFalse("Rule " + e.getKey() + " was expected not to be executed, but was executed.", 
                        queryProcessor.rulesTrackerMap.get(e.getKey()));
        }
    }

}
