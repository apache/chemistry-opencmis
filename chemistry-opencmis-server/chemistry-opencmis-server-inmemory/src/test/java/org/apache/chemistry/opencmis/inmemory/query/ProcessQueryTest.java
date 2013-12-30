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
import org.apache.chemistry.opencmis.server.support.query.TextSearchLexer;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessQueryTest extends AbstractQueryTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProcessQueryTest.class);

    private static class TestQueryProcessor extends AbstractQueryConditionProcessor {

        private static final String ON_START = "onStartWasCalled";
        private static final String ON_STOP = "onStopWasCalled";
        private static final String ON_EQUALS = "onEqualsWasCalled";
        private static final String ON_NOT_EQUALS = "onNotEqualsWasCalled";
        private static final String ON_GREATER_THAN = "onGreaterThanWasCalled";
        private static final String ON_GREATER_OR_EQUALS = "onGreaterOrEqualsWasCalled";
        private static final String ON_LESS_THAN = "onLessThanWasCalled";
        private static final String ON_LESS_OR_EQUALS = "onLessOrEqualsWasCalled";
        private static final String ON_NOT = "onNotWasCalled";
        private static final String ON_AND = "onAndWasCalled";
        private static final String ON_OR = "onOrWasCalled";
        private static final String ON_IN = "onInWasCalled";
        private static final String ON_NOT_IN = "onNotInWasCalled";
        private static final String ON_IN_ANY = "onInAnyWasCalled";
        private static final String ON_NOT_IN_ANY = "onNotInAnyWasCalled";
        private static final String ON_EQ_ANY = "onEqAnyWasCalled";
        private static final String ON_IS_NULL = "onIsNullWasCalled";
        private static final String ON_IS_NOT_NULL = "onIsNotNullWasCalled";
        private static final String ON_IS_LIKE = "onIsLikeWasCalled";
        private static final String ON_IS_NOT_LIKE = "onIsNotLikeWasCalled";
        private static final String ON_CONTAINS = "onContainsWasCalled";
        private static final String ON_IN_FOLDER = "onInFolderWasCalled";
        private static final String ON_IN_TREE = "onInTreeWasCalled";
        private static final String ON_SCORE = "onScoreWasCalled";
        private static final String ON_TEXT_AND = "onTextAndWasCalled";
        private static final String ON_TEXT_OR = "onTextOrWasCalled";
        private static final String ON_TEXT_MINUS = "onTextMinusWasCalled";
        private static final String ON_TEXT_PHRASE = "onTextPhraseWasCalled";
        private static final String ON_TEXT_WORD = "onTextWordWasCalled";

        final Map<String, Integer> rulesTrackerMap = new HashMap<String, Integer>() {
            private static final long serialVersionUID = 1L;
            {
                put(ON_START, 0);
                put(ON_STOP, 0);
                put(ON_EQUALS, 0);
                put(ON_NOT_EQUALS, 0);
                put(ON_GREATER_THAN, 0);
                put(ON_GREATER_OR_EQUALS, 0);
                put(ON_LESS_THAN, 0);
                put(ON_LESS_OR_EQUALS, 0);
                put(ON_NOT, 0);
                put(ON_AND, 0);
                put(ON_OR, 0);
                put(ON_IN, 0);
                put(ON_NOT_IN, 0);
                put(ON_IN_ANY, 0);
                put(ON_NOT_IN_ANY, 0);
                put(ON_EQ_ANY, 0);
                put(ON_IS_NULL, 0);
                put(ON_IS_NOT_NULL, 0);
                put(ON_IS_LIKE, 0);
                put(ON_IS_NOT_LIKE, 0);
                put(ON_CONTAINS, 0);
                put(ON_IN_FOLDER, 0);
                put(ON_IN_TREE, 0);
                put(ON_SCORE, 0);
                put(ON_TEXT_AND, 0);
                put(ON_TEXT_OR, 0);
                put(ON_TEXT_MINUS, 0);
                put(ON_TEXT_PHRASE, 0);
                put(ON_TEXT_WORD, 0);
            }
        };

        private int counter;

        public TestQueryProcessor() {
            counter = 1;
        }

        @Override
        public void onStartProcessing(Tree node) {
            LOG.debug("TestQueryProcessor:onStartProcessing()");
            rulesTrackerMap.put(ON_START, counter++);
            assertEquals(CmisQlStrictLexer.WHERE, node.getParent().getType());
        }

        @Override
        public void onStopProcessing() {
            LOG.debug("TestQueryProcessor:onStopProcessing()");
            rulesTrackerMap.put(ON_STOP, counter++);
        }

        @Override
        public void onEquals(Tree eqNode, Tree leftNode, Tree rightNode) {
            rulesTrackerMap.put(ON_EQUALS, counter++);
            assertEquals(CmisQlStrictLexer.EQ, eqNode.getType());
            assertTrue(CmisQlStrictLexer.COL == leftNode.getType() || CmisQlStrictLexer.SCORE == leftNode.getType());
            assertTrue(isLiteral(rightNode));
        }

        @Override
        public void onNotEquals(Tree neNode, Tree leftNode, Tree rightNode) {
            rulesTrackerMap.put(ON_NOT_EQUALS, counter++);
            assertEquals(CmisQlStrictLexer.NEQ, neNode.getType());
            assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
            assertTrue(isLiteral(rightNode));
            Object value = onLiteral(rightNode, Integer.class);
            assertEquals(100, value);
        }

        @Override
        public void onLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode) {
            rulesTrackerMap.put(ON_LESS_OR_EQUALS, counter++);
            assertEquals(CmisQlStrictLexer.LTEQ, leqNode.getType());
            assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
            assertTrue(isLiteral(rightNode));
            Object value = onLiteral(rightNode, Integer.class);
            assertEquals(100, value);
        }

        @Override
        public void onLessThan(Tree ltNode, Tree leftNode, Tree rightNode) {
            rulesTrackerMap.put(ON_LESS_THAN, counter++);
            assertEquals(CmisQlStrictLexer.LT, ltNode.getType());
            assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
            assertTrue(isLiteral(rightNode));
            Object value = onLiteral(rightNode, Integer.class);
            assertEquals(100, value);
        }

        @Override
        public void onGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode) {
            rulesTrackerMap.put(ON_GREATER_OR_EQUALS, counter++);
            assertEquals(CmisQlStrictLexer.GTEQ, geNode.getType());
            assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
            assertTrue(isLiteral(rightNode));
            Object value = onLiteral(rightNode, Integer.class);
            assertEquals(100, value);
        }

        @Override
        public void onGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode) {
            rulesTrackerMap.put(ON_GREATER_THAN, counter++);
            assertEquals(CmisQlStrictLexer.GT, gtNode.getType());
            assertEquals(CmisQlStrictLexer.COL, leftNode.getType());
            assertTrue(isLiteral(rightNode));
            Object value = onLiteral(rightNode, Integer.class);
            assertEquals(100, value);
        }

        @Override
        public void onNot(Tree opNode, Tree leftNode) {
            rulesTrackerMap.put(ON_NOT, counter++);
            assertEquals(CmisQlStrictLexer.NOT, opNode.getType());
        }

        @Override
        public void onAnd(Tree opNode, Tree leftNode, Tree rightNode) {
            assertEquals(CmisQlStrictLexer.AND, opNode.getType());
            rulesTrackerMap.put(ON_AND, counter++);
        }

        @Override
        public void onOr(Tree opNode, Tree leftNode, Tree rightNode) {
            assertEquals(CmisQlStrictLexer.OR, opNode.getType());
            rulesTrackerMap.put(ON_OR, counter++);
        }

        @Override
        public void onIn(Tree opNode, Tree colNode, Tree listNode) {
            assertEquals(CmisQlStrictLexer.IN, opNode.getType());
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertEquals(CmisQlStrictLexer.IN_LIST, listNode.getType());
            Object value = onLiteral(listNode.getChild(0), String.class);
            assertEquals("'Joe'", value);
            value = onLiteral(listNode.getChild(1), String.class);
            assertEquals("'Jim'", value);
            rulesTrackerMap.put(ON_IN, counter++);
        }

        @Override
        public void onNotIn(Tree node, Tree colNode, Tree listNode) {
            assertEquals(CmisQlStrictLexer.NOT_IN, node.getType());
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertEquals(CmisQlStrictLexer.IN_LIST, listNode.getType());
            Object value = onLiteral(listNode.getChild(0), String.class);
            assertEquals("'Joe'", value);
            value = onLiteral(listNode.getChild(1), String.class);
            assertEquals("'Jim'", value);
            rulesTrackerMap.put(ON_NOT_IN, counter++);
        }

        @Override
        public void onEqAny(Tree node, Tree literalNode, Tree colNode) {
            assertEquals(CmisQlStrictLexer.EQ_ANY, node.getType());
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertTrue(isLiteral(literalNode));
            Object value = onLiteral(literalNode, String.class);
            assertEquals("'Joe'", value);
            rulesTrackerMap.put(ON_EQ_ANY, counter++);
        }

        @Override
        public void onInAny(Tree node, Tree colNode, Tree listNode) {
            assertEquals(CmisQlStrictLexer.IN_ANY, node.getType());
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertEquals(CmisQlStrictLexer.IN_LIST, listNode.getType());
            Object value = onLiteral(listNode.getChild(0), String.class);
            assertEquals("'Joe'", value);
            value = onLiteral(listNode.getChild(1), String.class);
            assertEquals("'Jim'", value);
            rulesTrackerMap.put(ON_IN_ANY, counter++);
        }

        @Override
        public void onNotInAny(Tree node, Tree colNode, Tree listNode) {
            assertEquals(CmisQlStrictLexer.NOT_IN_ANY, node.getType());
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertEquals(CmisQlStrictLexer.IN_LIST, listNode.getType());
            Object value = onLiteral(listNode.getChild(0), String.class);
            assertEquals("'Joe'", value);
            value = onLiteral(listNode.getChild(1), String.class);
            assertEquals("'Jim'", value);
            rulesTrackerMap.put(ON_NOT_IN_ANY, counter++);
        }

        @Override
        public void onIsNull(Tree nullNode, Tree colNode) {
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertEquals(CmisQlStrictLexer.IS_NULL, nullNode.getType());
            rulesTrackerMap.put(ON_IS_NULL, counter++);
        }

        @Override
        public void onIsNotNull(Tree notNullNode, Tree colNode) {
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertEquals(CmisQlStrictLexer.IS_NOT_NULL, notNullNode.getType());
            rulesTrackerMap.put(ON_IS_NOT_NULL, counter++);
        }

        @Override
        public void onIsLike(Tree node, Tree colNode, Tree stringNode) {
            assertEquals(CmisQlStrictLexer.LIKE, node.getType());
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertEquals(CmisQlStrictLexer.STRING_LIT, stringNode.getType());
            Object value = onLiteral(stringNode, String.class);
            assertEquals("'Harry%'", value);
            rulesTrackerMap.put(ON_IS_LIKE, counter++);
        }

        @Override
        public void onIsNotLike(Tree node, Tree colNode, Tree stringNode) {
            assertEquals(CmisQlStrictLexer.NOT_LIKE, node.getType());
            assertEquals(CmisQlStrictLexer.COL, colNode.getType());
            assertEquals(CmisQlStrictLexer.STRING_LIT, stringNode.getType());
            Object value = onLiteral(stringNode, String.class);
            assertEquals("'Harry%'", value);
            rulesTrackerMap.put(ON_IS_NOT_LIKE, counter++);
        }

        @Override
        public void onContains(Tree node, Tree typeNode, Tree searchExprNode) {
            assertEquals(CmisQlStrictLexer.CONTAINS, node.getType());
            assertTrue(null != searchExprNode);
            rulesTrackerMap.put(ON_CONTAINS, counter++);
            super.onContains(node, typeNode, searchExprNode);
        }

        @Override
        public void onInFolder(Tree node, Tree colNode, Tree paramNode) {
            assertEquals(CmisQlStrictLexer.IN_FOLDER, node.getType());
            assertTrue(colNode == null || CmisQlStrictLexer.STRING_LIT == paramNode.getType());
            assertEquals(CmisQlStrictLexer.STRING_LIT, paramNode.getType());
            rulesTrackerMap.put(ON_IN_FOLDER, counter++);
        }

        @Override
        public void onInTree(Tree node, Tree colNode, Tree paramNode) {
            assertEquals(CmisQlStrictLexer.IN_TREE, node.getType());
            assertTrue(colNode == null || CmisQlStrictLexer.STRING_LIT == paramNode.getType());
            assertEquals(CmisQlStrictLexer.STRING_LIT, paramNode.getType());
            rulesTrackerMap.put(ON_IN_TREE, counter++);
        }

        @Override
        public void onScore(Tree node) {
            assertEquals(CmisQlStrictLexer.SCORE, node.getType());
            rulesTrackerMap.put(ON_SCORE, counter++);
        }

        @Override
        public void onTextAnd(Tree node, List<Tree> conjunctionNodes, int index) {
            assertEquals(TextSearchLexer.TEXT_AND, node.getType());
            assertTrue(conjunctionNodes.size() >= 2);
            rulesTrackerMap.put(ON_TEXT_AND, counter++);
        }

        @Override
        public void onTextOr(Tree node, List<Tree> termNodes, int index) {
            assertEquals(TextSearchLexer.TEXT_OR, node.getType());
            assertTrue(termNodes.size() >= 2);
            rulesTrackerMap.put(ON_TEXT_OR, counter++);
        }

        @Override
        public void onTextMinus(Tree node, Tree notNode) {
            assertEquals(TextSearchLexer.TEXT_MINUS, node.getType());
            assertTrue(notNode.getType() == TextSearchLexer.TEXT_SEARCH_PHRASE_STRING_LIT
                    || notNode.getType() == TextSearchLexer.TEXT_SEARCH_WORD_LIT);
            rulesTrackerMap.put(ON_TEXT_MINUS, counter++);
        }

        @Override
        public void onTextWord(String word) {
            assertTrue(word != null && word.length() > 0);
            rulesTrackerMap.put(ON_TEXT_WORD, counter++);
        }

        @Override
        public void onTextPhrase(String phrase) {
            assertTrue(phrase != null && phrase.length() > 0);
            rulesTrackerMap.put(ON_TEXT_PHRASE, counter++);
        }

        // private helper functions:

        private static boolean isLiteral(Tree node) {
            int type = node.getType();
            return type == CmisQlStrictLexer.BOOL_LIT || type == CmisQlStrictLexer.NUM_LIT
                    || type == CmisQlStrictLexer.STRING_LIT || type == CmisQlStrictLexer.TIME_LIT;
        }

        private static Object onLiteral(Tree node, Class<?> clazz) {
            int type = node.getType();
            switch (type) {
            case CmisQlStrictLexer.BOOL_LIT:
                return clazz == Boolean.class ? Boolean.parseBoolean(node.getText()) : null;
            case CmisQlStrictLexer.NUM_LIT:
                if (clazz == Integer.class) {
                    return Integer.parseInt(node.getText());
                } else if (clazz == Long.class) {
                    return Long.parseLong(node.getText());
                } else if (clazz == Short.class) {
                    return Short.parseShort(node.getText());
                } else if (clazz == Double.class) {
                    return Double.parseDouble(node.getText());
                } else if (clazz == Float.class) {
                    return Float.parseFloat(node.getText());
                } else {
                    return null;
                }
            case CmisQlStrictLexer.STRING_LIT:
                return clazz == String.class ? node.getText() : null;
            case CmisQlStrictLexer.TIME_LIT:
                return clazz == GregorianCalendar.class ? CalendarHelper.fromString(node.getText()) : null;
            default:
                LOG.error("Unknown literal. " + node);
                return null;
            }
        }

        @Override
        public void onColNode(Tree node) {
        }

    }

    private TypeManagerImpl tm;
    private TestQueryProcessor queryProcessor;

    @Before
    public void setUp() {
        tm = new TypeManagerImpl();
        tm.initTypeSystem(null, true); // create CMIS default types

        // create some types for testing
        List<TypeDefinition> typeDefs = super.createTypes();
        for (TypeDefinition typeDef : typeDefs) {
            tm.addTypeDefinition(typeDef, true);
        }

        // initialize query object with type manager
        queryProcessor = new TestQueryProcessor();
        super.setUp(tm, queryProcessor);
    }

    @Test
    public void testStartStopProcessing() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = 100";
        traverseStatementAndCatchExc(statement); // calls query processor
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_START) > 0);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_STOP) > 0);
    }

    @Test
    public void testEq() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = 100";
        testStatement(statement, TestQueryProcessor.ON_EQUALS);
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
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = 100 AND Title LIKE 'Harry%'";
        testStatementMultiRule(statement, TestQueryProcessor.ON_AND);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_START) == 1);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_EQUALS) == 2);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_AND) == 3);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_IS_LIKE) == 4);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_STOP) == 5);
    }

    @Test
    public void testOr() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = 100 OR Title LIKE 'Harry%'";
        testStatementMultiRule(statement, TestQueryProcessor.ON_OR);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_START) == 1);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_EQUALS) == 2);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_OR) == 3);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_IS_LIKE) == 4);
        assertTrue(queryProcessor.rulesTrackerMap.get(TestQueryProcessor.ON_STOP) == 5);
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
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE Author LIKE 'Harry%'";
        testStatement(statement, TestQueryProcessor.ON_IS_LIKE);
    }

    @Test
    public void testOnNotLikeWasCalled() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE Author NOT LIKE 'Harry%'";
        testStatement(statement, TestQueryProcessor.ON_IS_NOT_LIKE);
    }

    @Test
    public void testOnContainsWasCalled1() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE CONTAINS('Hello')";
        testStatementMultiRule(statement, TestQueryProcessor.ON_CONTAINS);
    }

    @Test
    public void testOnContainsWasCalled2() {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE CONTAINS(BookType, 'Harry')";
        testStatementMultiRule(statement, TestQueryProcessor.ON_CONTAINS);
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

    @Test
    public void testOnTextWordLiteral() {
        String statement = "SELECT * FROM BookType WHERE CONTAINS('abc')";
        testStatementMultiRule(statement, TestQueryProcessor.ON_TEXT_WORD);
    }

    @Test
    public void testOnTextPhraseLiteral() {
        String statement = "SELECT * FROM BookType WHERE CONTAINS('\"abc\"')";
        testStatementMultiRule(statement, TestQueryProcessor.ON_TEXT_PHRASE);
    }

    @Test
    public void testOnTextAnd() {
        String statement = "SELECT * FROM BookType WHERE CONTAINS('abc def')";
        testStatementMultiRule(statement, TestQueryProcessor.ON_TEXT_AND);
    }

    @Test
    public void testOnTextOr() {
        String statement = "SELECT * FROM BookType WHERE CONTAINS('abc OR def')";
        testStatementMultiRule(statement, TestQueryProcessor.ON_TEXT_OR);
    }

    @Test
    public void testOnTextMinus() {
        String statement = "SELECT * FROM BookType WHERE CONTAINS('abc -def')";
        testStatementMultiRule(statement, TestQueryProcessor.ON_TEXT_MINUS);
    }

    // private helper functions

    private void testStatementMultiRule(String statement, String ruleAssertion) {
        traverseStatementAndCatchExc(statement); // calls query processor
        assertTrue(queryProcessor.rulesTrackerMap.get(ruleAssertion) > 0);
    }

    private void testStatement(String statement, String ruleAssertion) {
        testStatementMultiRule(statement, ruleAssertion);
        checkOtherRulesNotCalled(ruleAssertion);
    }

    private void checkOtherRulesNotCalled(String ruleAssertion) {
        for (Entry<String, Integer> e : queryProcessor.rulesTrackerMap.entrySet()) {
            if (!e.getKey().equals(ruleAssertion) && !e.getKey().equals("onPropertyValueWasCalled")
                    && !e.getKey().equals(TestQueryProcessor.ON_START)
                    && !e.getKey().equals(TestQueryProcessor.ON_STOP) && !e.getKey().contains("Literal")) {
                assertFalse("Rule " + e.getKey() + " was expected not to be executed, but was executed.",
                        queryProcessor.rulesTrackerMap.get(e.getKey()) > 0);
            }
        }
    }

}
