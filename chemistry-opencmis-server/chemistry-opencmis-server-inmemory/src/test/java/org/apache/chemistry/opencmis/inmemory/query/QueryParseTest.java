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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictLexer;
import org.apache.chemistry.opencmis.server.support.query.CmisQueryWalker;
import org.apache.chemistry.opencmis.server.support.query.CmisSelector;
import org.apache.chemistry.opencmis.server.support.query.ColumnReference;
import org.apache.chemistry.opencmis.server.support.query.FunctionReference;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.chemistry.opencmis.server.support.query.QueryObject.SortSpec;
import org.apache.chemistry.opencmis.server.support.query.TextSearchLexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;

public class QueryParseTest extends AbstractQueryTest {

    private static final Logger LOG = LoggerFactory.getLogger(QueryParseTest.class);

    @Before
    public void setUp() {
        // initialize query object, we do not need a type manager for just testing parsing
        super.setUp(new QueryObject(null), null);
    }

    @Test
    public void simpleFailTest() {
        String statement = "SELECT * TO MyType ORDER BY abc.def ASC";
//        String statement = "SELECT dsfj disfj dsifj dsoijf�039fi ";
        try {
            traverseStatement(statement);
            fail("Errornous statement should throw exception.");
        } catch (Exception e) {
            LOG.debug("Exception in simpleFailTest: " + e);
        }
    }

    public void simpleSelectTest1() {
        String statement = "SELECT SCORE() FROM cmis:document";

        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        assertTrue(selects.get(0) instanceof FunctionReference);

        FunctionReference funcRef = ((FunctionReference)selects.get(0));
        assertTrue(FunctionReference.CmisQlFunction.SCORE == funcRef.getFunction());
    }

    @Test
    public void simpleSelectTest2() {
        String statement = "SELECT abc FROM cmis:document";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        // nothing should be in where references
        assertTrue(0 == queryObj.getWhereReferences().size());

        ColumnReference colRef = ((ColumnReference)selects.get(0));
        assertTrue(selects.get(0) instanceof ColumnReference);
        assertEquals("abc", colRef.getPropertyQueryName());
    }

    @Test
    public void simpleSelectTest3() {
        String statement = "SELECT t1.abc FROM cmis:document";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        // nothing should be in where references
        assertTrue(0 == queryObj.getWhereReferences().size());
        assertTrue(selects.get(0) instanceof ColumnReference);

        ColumnReference colRef = ((ColumnReference)selects.get(0));
        assertEquals("t1", colRef.getQualifier());
        assertEquals("abc", colRef.getPropertyQueryName());
    }

    @Test
    public void simpleSelectTest4() {
        String statement = "SELECT * FROM cmis:document";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        // nothing should be in where references
        assertTrue(0 == queryObj.getWhereReferences().size());

        ColumnReference colRef = ((ColumnReference)selects.get(0));
        assertTrue(selects.get(0) instanceof ColumnReference);
        assertEquals(null, colRef.getQualifier());
        assertEquals("*", colRef.getPropertyQueryName());
    }

    @Test
    public void simpleSelectTest5() {
        String statement = "SELECT t1.* FROM cmis:document";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        // nothing should be in where references
        assertTrue(0 == queryObj.getWhereReferences().size());
        assertTrue(selects.get(0) instanceof ColumnReference);

        ColumnReference colRef = ((ColumnReference)selects.get(0));
        assertEquals("t1", colRef.getQualifier());
        assertEquals("*", colRef.getPropertyQueryName());
    }

    @Test
    public void simpleSelectTest6() {
        String statement = "SELECT t2.aaa myalias FROM cmis:document";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        // nothing should be in where references
        assertTrue(0 == queryObj.getWhereReferences().size());
        assertTrue(selects.get(0) instanceof ColumnReference);

        ColumnReference colRef = ((ColumnReference)selects.get(0));
        assertEquals("t2", colRef.getQualifier());
        assertEquals("aaa", colRef.getPropertyQueryName());
    }

    @Test
    public void simpleSelectTest7() {
        // error processing
        String statement = "SELECTXXX t2.aaa myalias FROM cmis:document WHERE a < t1";
        try {
            CmisQueryWalker walker = traverseStatement(statement);
            fail("Walking of statement should with RecognitionException but succeeded");
        } catch (Exception e) {
            assertTrue(e instanceof CmisInvalidArgumentException);
        }
    }

    @Test
    public void simpleFromTest1() {
        String statement = "SELECT * FROM MyType MyAlias";

        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        Map<String,String> types = queryObj.getTypes();
        assertTrue(1 == types.size());

        String key = types.keySet().iterator().next();
        assertEquals("MyAlias", key);
        assertEquals("MyType", types.get(key));
    }

    @Test
    public void simpleFromTest2() {
        String statement = "SELECT * FROM MyType";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        Map<String,String> types = queryObj.getTypes();
        assertTrue(1 == types.size());

        String key = types.keySet().iterator().next();
        assertEquals("MyType", key);
        assertEquals("MyType", types.get(key));
    }

    @Test
    public void simpleFromTest3() {
        String statement = "SELECT t2.aaa FROM MyType abc123";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        Map<String,String> types = queryObj.getTypes();
        assertTrue(1 == types.size());

        String key = types.keySet().iterator().next();
        assertEquals("abc123", key);
        assertEquals("MyType", types.get(key));
    }

    @Test
    public void simpleFromTest4() {
        String statement = "SELECT X.aaa FROM MyType AS X WHERE 10 = ANY X.aaa ";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        Map<String,String> types = queryObj.getTypes();
        assertTrue(1 == types.size());

        String key = types.keySet().iterator().next();
        assertEquals("X", key);
        assertEquals("MyType", types.get(key));
    }

    @Test
    public void simpleWhereTest() {
        String statement = "SELECT * FROM MyType WHERE MyProp1=123";

        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<CmisSelector> whereRefs = queryObj.getWhereReferences();
        Map<Integer, CmisSelector> colRefs = queryObj.getColumnReferences();
        assertTrue(1 == whereRefs.size());

        CmisSelector value = whereRefs.iterator().next();
        assertTrue(value instanceof ColumnReference);
        assertEquals("MyProp1", ((ColumnReference)value).getPropertyQueryName());
        // only "*" should be in select references
        assertTrue(1 == queryObj.getSelectReferences().size());

        CommonTree tree = (CommonTree) walker.getTreeNodeStream().getTreeSource();

        // System.out.println("simpleWhereTest printing Tree ...");
        // System.out.println("id in map: " + System.identityHashCode(whereRefs.keySet().iterator().next()));
//        assertTrue(traverseTreeAndFindNodeInColumnMap(tree, colRefs));
        traverseTreeAndFindNodeInColumnMap2(tree, colRefs);
        // System.out.println("... simpleWhereTest printing Tree done.");
    }

    // check if the map containing all column references in the where clause has an existing node as key
    private boolean traverseTreeAndFindNodeInColumnMap(Tree node, Map<Object, CmisSelector> colRefs) {
        boolean found = false;
//        System.out.println("cmp to: " + System.identityHashCode(node) + " is: " + node.toString());
        if (null != colRefs.get(node)) {
            return true;
        }

        int count = node.getChildCount();
        for (int i=0; i<count && !found; i++) {
            Tree child = node.getChild(i);
            found = traverseTreeAndFindNodeInColumnMap(child, colRefs);
        }
        return found;
    }

    private boolean traverseTreeAndFindNodeInColumnMap2(Tree node, Object colRef) {
        int count = node.getChildCount();
        LOG.debug("  checking with: " + node + " identity hash code: " + System.identityHashCode(node));
        if (node==colRef) {
            return true;
        }
        boolean found = false;
        for (int i=0; i<count && !found; i++) {
            Tree child = node.getChild(i);
            found = traverseTreeAndFindNodeInColumnMap2(child, colRef);
        }
        return found;
    }

    @Test
    public void simpleSortTest1() {
        String statement = "SELECT * FROM MyType ORDER BY abc.def ASC";

        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<SortSpec> orderBys = queryObj.getOrderBys();
        assertTrue(1 == orderBys.size());

        SortSpec sp = orderBys.get(0);
        assertTrue(sp.isAscending());

        CmisSelector sortSpec = sp.getSelector();
        assert(sortSpec instanceof ColumnReference);
        assertEquals("abc", ((ColumnReference)sortSpec).getQualifier());
        assertEquals("def", ((ColumnReference)sortSpec).getPropertyQueryName());
    }

    @Test
    public void simpleSortTest2() {
        String statement = "SELECT * FROM MyType ORDER BY def DESC";
        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        List<SortSpec> orderBys = queryObj.getOrderBys();
        assertTrue(1 == orderBys.size());

        SortSpec sp = orderBys.get(0);
        assertFalse(sp.isAscending());

        CmisSelector sortSpec = sp.getSelector();
        assert(sortSpec instanceof ColumnReference);
        assertNull(((ColumnReference)sortSpec).getQualifier());
        assertEquals("def", ((ColumnReference)sortSpec).getPropertyQueryName());
    }

    @Test
    public void printTreeTest() {
        // System.out.println("printTreeTest():");
        String statement = "SELECT p1, p2, p3.t3 mycol FROM MyType AS MyAlias WHERE p1='abc' and p2=123 ORDER BY abc.def ASC";
        try {
            getWalker(statement);
            Tree parserTree = (Tree) walker.getTreeNodeStream().getTreeSource();
            printTree(parserTree, statement);

        } catch (Exception e) {
            fail("Cannot parse query: " + statement + " (" + e + ")");
        }
    }

    @Test
    public void extractWhereTreeTest() {
        // System.out.println("extractWhereTreeTest():");
        String statement = "SELECT p1, p2, p3.t3 mycol FROM MyType AS MyAlias WHERE p1='abc' and p2=123 ORDER BY abc.def ASC";

        try {
            traverseStatementAndCatchExc(statement);
            Tree whereTree = walker.getWherePredicateTree(); // getWhereTree(parserTree);
            printTree(whereTree);
            LOG.info("Evaluate WHERE subtree: ...");
            evalWhereTree(whereTree);
        } catch (Exception e) {
            fail("Cannot parse query: " + statement + " (" + e + ")");
        }
    }

    @Test
    public void whereTestIn() {
        // System.out.println("extractWhereTestIN():");
        String statement = "SELECT p1 FROM MyType WHERE p1 IN ('Red', 'Green', 'Blue', 'Black')";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestEq() {
        String statement = "SELECT p1 FROM MyType WHERE p1='abc'";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestNotEq() {
        String statement = "SELECT p1 FROM MyType WHERE p1 <> 123";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestLT() {
        String statement = "SELECT p1 FROM MyType WHERE p1 < 123";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestGT() {
        String statement = "SELECT p1 FROM MyType WHERE p1 > 123";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestLTEQ() {
        String statement = "SELECT p1 FROM MyType WHERE p1 <= 123";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestGTEQ() {
        String statement = "SELECT p1 FROM MyType WHERE p1 >= 123";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestAnd() {
        String statement = "SELECT p1 FROM MyType WHERE p1=1 AND p2=2";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestOr() {
        String statement = "SELECT p1 FROM MyType WHERE p1='abc' OR p2=123";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestNot() {
        String statement = "SELECT p1 FROM MyType WHERE NOT p1 = 123";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestInFolder() {
        String statement = "SELECT p1 FROM MyType WHERE IN_FOLDER('myfolder')";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestInTree() {
        String statement = "SELECT p1 FROM MyType WHERE IN_TREE('myfolder')";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestAny() {
        String statement = "SELECT p1 FROM MyType WHERE 'Smith' = ANY Authors ";
        checkTreeWhere(statement);
    }


    @Test
    public void whereTestAnyIn() {
        String statement = "SELECT p1 FROM MyType WHERE ANY Colors IN ('Red', 'Green', 'Blue')";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestLike() {
        String statement = "SELECT p1 FROM MyType WHERE p1 LIKE 'abc*' ";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestTokensAfterStatement() {
    	String statement = "SELECT p1 FROM MyType WHERE p1 LIKE 'abc*' IN_FOLDER('def')";
    	try {
            checkTreeWhere(statement);    		
    	} catch (Exception e) {
    		LOG.debug("Exception is: " + e);
    		assertTrue(e instanceof CmisInvalidArgumentException);
    		assertTrue(e.getMessage().contains("illegal tokens after end"));
    	}
    }

    @Test
    public void whereTestNotLike() {
        String statement = "SELECT p1 FROM MyType WHERE p1 NOT LIKE 'abc*'";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestNull() {
        String statement = "SELECT p1 FROM MyType WHERE p1 IS NULL";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestNotNull() {
        String statement = "SELECT p1 FROM MyType WHERE p1 IS NOT NULL";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestContains() {
        String statement = "SELECT p1 FROM MyType WHERE CONTAINS('Beethoven')";
        checkTreeWhere(statement);
        Tree tree = findSearchExpression(statement);
        printSearchTree(tree, statement);
        assertEquals("Beethoven", tree.getChild(0).getText());
    }

    @Test
    public void whereTestContainsNoFulltextParse() throws Exception {
        String statement = "SELECT p1 FROM MyType WHERE CONTAINS('Beethoven')";
        walker = getWalker(statement);
        walker.setDoFullTextParse(false);
        walker.query(queryObj, predicateWalker);
        Tree whereTree = walker.getWherePredicateTree();
        Tree tree = findTextSearchNode(whereTree);
        printSearchTree(tree, statement);
        // unparsed, still with quotes
        assertEquals("'Beethoven'", tree.getChild(0).getText());
    }

    @Test
    public void whereTestScore() {
        String statement = "SELECT p1 FROM MyType WHERE SCORE() = 100";
        checkTreeWhere(statement);
    }

    @Test
    public void whereTestParentheses() {
        String statement = "SELECT p1 FROM MyType WHERE (p1 IS NULL OR SCORE()=100) AND (p2=123 OR p3=456)";
        checkTreeWhere(statement);
    }

    @Test
    public void doubleFromTest() {
        String statement = "SELECT * FROM MyType JOIN YourType WHERE a='1'";

        CmisQueryWalker walker = traverseStatementAndCatchExc(statement);
        assertNotNull(walker);

        QueryObject from = queryObj;
        Map<String,String> types = from.getTypes();
        assertTrue(2 == types.size());
    }

    @Test
    public void duplicatedAliasTestSelect() {
        String statement = "SELECT p1.T1 MyAlias, p2.T1 AS MyAlias FROM T1";
        try {
            traverseStatement(statement);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("more than once as alias in a select"));
        }
    }

    @Test
    public void duplicatedAliasTestFrom() {
        String statement = "SELECT * FROM T1 MyAlias JOIN T2 AS MyAlias";
        try {
            traverseStatement(statement);
            fail("Parsing statement " + statement + " should fail.");
        } catch (RecognitionException e) {
            assertTrue(e instanceof FailedPredicateException);
            LOG.debug("duplicatedAliasTestFrom(), exception: " + e);
            // walker.reportError(e);
            String errorMessage = queryUtil.getErrorMessage(e);
            LOG.debug("");
            LOG.debug("duplicatedAliasTestFrom(), error message: " + errorMessage);
            assertTrue(e.toString().contains("more than once as alias in a from"));
            assertTrue(errorMessage.contains("more than once as alias in a from"));
        } catch (Exception e) {
            fail("Parsing statement " + statement + " should fail with RecognitionException, but was: " + e.getClass());
        }
    }
    
    @Test
    public void whereTestContains2() {
        String statement = "SELECT p1 FROM MyType WHERE CONTAINS('Beethoven OR \\'Johann Sebastian\\' Mozart -Cage AND Orff')";
        checkTreeWhere(statement);
        Tree tree = findSearchExpression(statement);
        printSearchTree(tree, statement);
    }
    
    private void checkTreeWhere(String statement) {
        LOG.info("\ncheckTreeWhere: " + statement);
        traverseStatementAndCatchExc(statement);
        Tree whereTree = walker.getWherePredicateTree();
        evalWhereTree(whereTree);
    }
    
    private Tree findSearchExpression(String statement) {
        traverseStatementAndCatchExc(statement);
        Tree whereTree = walker.getWherePredicateTree(); 
        return findTextSearchNode(whereTree);
    }
    
    private Tree findTextSearchNode(Tree node) {
        int count = node.getChildCount();
        if (node.getType() == CmisQlStrictLexer.CONTAINS) {
            return node;
        } else {
            for (int i=0; i<count; i++) {
                Tree child = node.getChild(i);
                node = findTextSearchNode(child); // recursive descent
                if (null != node)
                    return node;
            }
            return null;
        }        
    }

    private void evalWhereTree(Tree root) {
        int count = root.getChildCount();
        if (root.getType() == CmisQlStrictLexer.CONTAINS) {
            evalSearchExprTree(root);
        } else {
            for (int i=0; i<count; i++) {
                Tree child = root.getChild(i);
                evaluateWhereNode(child);
                evalWhereTree(child); // recursive descent
            }
        }
    }
    
    private void evalSearchExprTree(Tree root) {
        int count = root.getChildCount();
        for (int i=0; i<count; i++) {
            Tree child = root.getChild(i);
            evaluateSearchExprNode(child);
            evalSearchExprTree(child); // recursive descent
        }
    }
    
    private void printTree(Tree tree, String statement) {
        LOG.info("Printing the abstract syntax tree for statement:");
        LOG.info("  " + statement);
        printTree(tree);
    }

    private int indent = 1;

    private String indentString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; ++i) {
            sb.append("  ");
        }
        return sb.toString();
    }

    private void printTree(Tree node) {
        LOG.info(indentString() + printNode(node));
        ++indent;
        int count = node.getChildCount();
        for (int i=0;i<count;i++) {
            Tree child = node.getChild(i);
            printTree(child);
        }
        --indent;
    }

    private static String printNode(Tree node) {
        switch (node.getType()) {
        case CmisQlStrictLexer.TABLE:
            return "#TABLE";
        case CmisQlStrictLexer.COL:
            return "#COL";
        case CmisQlStrictLexer.IN_LIST:
            return "#IN_LIST";
        case CmisQlStrictLexer.SEL_LIST:
            return "#SEL_LIST";
        case CmisQlStrictLexer.EQ_ANY:
            return "#EQ_ANY";
        case CmisQlStrictLexer.NOT_LIKE:
            return "#NOT_LIKE";
        case CmisQlStrictLexer.NOT_IN:
            return "#NOT_IN";
        case CmisQlStrictLexer.IN_ANY:
            return "#IN_ANY";
        case CmisQlStrictLexer.NOT_IN_ANY:
            return "#NOT_IN_ANY";
        case CmisQlStrictLexer.IS_NULL:
            return "#IS_NULL";
        case CmisQlStrictLexer.IS_NOT_NULL:
            return "#IS_NOT_NULL";
        case CmisQlStrictLexer.ORDER_BY:
            return "#ORDER_BY";

        case CmisQlStrictLexer.WHERE:
        case CmisQlStrictLexer.LT:
        case CmisQlStrictLexer.STAR:
        case CmisQlStrictLexer.BOOL_LIT:
        case CmisQlStrictLexer.INNER:
        case CmisQlStrictLexer.TIME_LIT:
        case CmisQlStrictLexer.ORDER:
        case CmisQlStrictLexer.STRING_LIT:
        case CmisQlStrictLexer.CONTAINS:
        case CmisQlStrictLexer.ExactNumLit:
        case CmisQlStrictLexer.LTEQ:
        case CmisQlStrictLexer.NOT:
        case CmisQlStrictLexer.ID:
        case CmisQlStrictLexer.AND:
        case CmisQlStrictLexer.EOF:
        case CmisQlStrictLexer.AS:
        case CmisQlStrictLexer.IN:
        case CmisQlStrictLexer.LPAR:
        case CmisQlStrictLexer.Digits:
        case CmisQlStrictLexer.COMMA:
        case CmisQlStrictLexer.IS:
        case CmisQlStrictLexer.LEFT:
        case CmisQlStrictLexer.Sign:
        case CmisQlStrictLexer.EQ:
        case CmisQlStrictLexer.DOT:
        case CmisQlStrictLexer.NUM_LIT:
        case CmisQlStrictLexer.SELECT:
        case CmisQlStrictLexer.LIKE:
        case CmisQlStrictLexer.OUTER:
        case CmisQlStrictLexer.BY:
        case CmisQlStrictLexer.ASC:
        case CmisQlStrictLexer.NULL:
        case CmisQlStrictLexer.ON:
        case CmisQlStrictLexer.RIGHT:
        case CmisQlStrictLexer.GTEQ:
        case CmisQlStrictLexer.ApproxNumLit:
        case CmisQlStrictLexer.JOIN:
        case CmisQlStrictLexer.IN_FOLDER:
        case CmisQlStrictLexer.WS:
        case CmisQlStrictLexer.NEQ:
        case CmisQlStrictLexer.ANY:
        case CmisQlStrictLexer.SCORE:
        case CmisQlStrictLexer.IN_TREE:
        case CmisQlStrictLexer.OR:
        case CmisQlStrictLexer.GT:
        case CmisQlStrictLexer.RPAR:
        case CmisQlStrictLexer.DESC:
        case CmisQlStrictLexer.FROM:
        case CmisQlStrictLexer.TIMESTAMP:
            return node.toString();
        default:
            return "[Unknown token: " + node.toString() + "]";
        }
    }

    private void printSearchTree(Tree tree, String searchExpr) {
        LOG.info("Printhing the abstract syntax tree for the search expression in CONTAINS :");
        LOG.info(searchExpr);
        printSearchTree(tree);
    }

    private void printSearchTree(Tree node) {
        LOG.info(indentString() + printSearchNode(node));
        ++indent;
        int count = node.getChildCount();
        for (int i=0;i<count;i++) {
            Tree child = node.getChild(i);
            printSearchTree(child);
        }
        --indent;
    }

    private static String printSearchNode(Tree node) {
        switch (node.getType()) {
        case TextSearchLexer.TEXT_AND:
        case TextSearchLexer.TEXT_OR:
        case TextSearchLexer.TEXT_SEARCH_PHRASE_STRING_LIT:
        case TextSearchLexer.TEXT_SEARCH_WORD_LIT:            
            return node.toString();
        case TextSearchLexer.TEXT_MINUS:
            return "MINUS";
         default:
             return "Unknown token: " +  node.toString();
        }
    }
    
    // Ensure that we receive only valid tokens and nodes in the where clause:
    private void evaluateWhereNode(Tree node) {
        LOG.info("evaluating node: " + node.toString());
        switch (node.getType()) {
        case CmisQlStrictLexer.WHERE:
            break; // ignore
        case CmisQlStrictLexer.COL:
            evalColumn(node);
            break;
        case CmisQlStrictLexer.IN_LIST:
            evalInList(node);
            break;
        case CmisQlStrictLexer.IN_ANY:
            evalInAny(node);
            break;
        case CmisQlStrictLexer.EQ_ANY:
            evalEqAny(node);
            break;
        case CmisQlStrictLexer.NOT_LIKE:
            evalNotLike(node);
            break;
        case CmisQlStrictLexer.NOT_IN:
            evalNotIn(node);
            break;
        case CmisQlStrictLexer.IS_NULL:
            evalIsNull(node);
            break;
        case CmisQlStrictLexer.IS_NOT_NULL:
            evalIsNotNull(node);
            break;
        case CmisQlStrictLexer.LT:
            evalLessThan(node);
            break;
        case CmisQlStrictLexer.BOOL_LIT:
            evalBooleanLiteral(node);
            break;
        case CmisQlStrictLexer.TIME_LIT:
            evalTimeLiteral(node);
            break;
        case CmisQlStrictLexer.STRING_LIT:
            evalStringLiteral(node);
            break;
        case CmisQlStrictLexer.CONTAINS:
            evalContains(node);
            break;
        case CmisQlStrictLexer.ExactNumLit:
            evalExactNumLiteral(node);
            break;
        case CmisQlStrictLexer.LTEQ:
            evalLessOrEqual(node);
            break;
        case CmisQlStrictLexer.NOT:
            evalNot(node);
            break;
        case CmisQlStrictLexer.ID:
            evalId(node);
            break;
        case CmisQlStrictLexer.AND:
            evalAnd(node);
            break;
        case CmisQlStrictLexer.IN:
            evalIn(node);
            break;
        case CmisQlStrictLexer.EQ:
            evalEquals(node);
            break;
        case CmisQlStrictLexer.NUM_LIT:
            evalNumLiteral(node);
            break;
        case CmisQlStrictLexer.LIKE:
            evalLike(node);
            break;
        case CmisQlStrictLexer.NULL:
            evalNull(node);
            break;
        case CmisQlStrictLexer.GTEQ:
            evalGreaterThan(node);
            break;
        case CmisQlStrictLexer.ApproxNumLit:
            evalApproxNumLiteral(node);
            break;
        case CmisQlStrictLexer.IN_FOLDER:
            evalInFolder(node);
            break;
        case CmisQlStrictLexer.NEQ:
            evalNotEquals(node);
            break;
        case CmisQlStrictLexer.SCORE:
            evalScore(node);
            break;
        case CmisQlStrictLexer.IN_TREE:
            evalInTree(node);
            break;
        case CmisQlStrictLexer.OR:
            evalOr(node);
            break;
        case CmisQlStrictLexer.GT:
            evalGreaterThan(node);
            break;
        case CmisQlStrictLexer.TIMESTAMP:
            evalTimeLiteral(node);
            break;
        default:
            fail("[Unexpected node in WHERE clause: " + node.toString() + "]");
        }
    }

    // Ensure that we receive only valid tokens and nodes in the where clause:
    private void evaluateSearchExprNode(Tree node) {
        LOG.info("evaluating text search expression node: " + node.toString());
        switch (node.getType()) {
        case TextSearchLexer.TEXT_AND:
        case TextSearchLexer.TEXT_OR:
            assertTrue(node.getChildCount() >= 2 );
            break;
        case TextSearchLexer.TEXT_MINUS:
            assertEquals(1, node.getChildCount());
            break;
        case TextSearchLexer.TEXT_SEARCH_PHRASE_STRING_LIT:
        case TextSearchLexer.TEXT_SEARCH_WORD_LIT:
            evalStringLiteral(node);
            break;
        default:
            fail("[Unexpected node in text search expression: " + node.toString() + "]");         
        }
    }
        
    private void evalInAny(Tree node) {
    }

    private static void evalColumn(Tree node) {
        assertEquals(1, node.getChildCount());
        assertEquals(CmisQlStrictLexer.ID, node.getChild(0).getType());
    }

    private static void evalEquals(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private static void evalInFolder(Tree node) {
        assertEquals(1, node.getChildCount());
    }

    private static void evalApproxNumLiteral(Tree node) {
    }

    private static void evalNull(Tree node) {
        assertEquals(1, node.getChildCount());
    }

    private static void evalLike(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private static void evalNumLiteral(Tree node) {
        assertEquals(0, node.getChildCount());
    }

    private static void evalInList(Tree node) {
    }

    private static void evalEqAny(Tree node) {
    }

    private static void evalNotLike(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private static void evalNotIn(Tree node) {
    }

    private static void evalIsNull(Tree node) {
        assertEquals(1, node.getChildCount());
    }

    private static void evalIsNotNull(Tree node) {
        assertEquals(1, node.getChildCount());
    }

    private static void evalLessThan(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private static void evalBooleanLiteral(Tree node) {
        assertEquals(0, node.getChildCount());
    }

    private static void evalStringLiteral(Tree node) {
        assertEquals(0, node.getChildCount());
    }

    private static void evalContains(Tree node) {
        assertEquals(1, node.getChildCount());
    }

    private static void evalExactNumLiteral(Tree node) {
        assertEquals(0, node.getChildCount());
    }

    private static void evalLessOrEqual(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private static void evalNot(Tree node) {
        assertEquals(1, node.getChildCount());
    }

    private static void evalId(Tree node) {
        assertEquals(0, node.getChildCount());
    }

    private static void evalAnd(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private void evalIn(Tree node) {
    }

    private static void evalNotEquals(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private static void evalScore(Tree node) {
        assertEquals(0, node.getChildCount());
    }

    private static void evalInTree(Tree node) {
        assertEquals(1, node.getChildCount());
    }

    private static void evalOr(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private static void evalGreaterThan(Tree node) {
        assertEquals(2, node.getChildCount());
    }

    private static void evalTimeLiteral(Tree node) {
        assertEquals(0, node.getChildCount());
    }

}
