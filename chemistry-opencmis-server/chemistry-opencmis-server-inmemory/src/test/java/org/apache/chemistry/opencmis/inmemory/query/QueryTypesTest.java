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

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.inmemory.TypeManagerImpl;
import org.apache.chemistry.opencmis.server.support.query.CmisQueryWalker;
import org.apache.chemistry.opencmis.server.support.query.CmisSelector;
import org.apache.chemistry.opencmis.server.support.query.ColumnReference;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.chemistry.opencmis.server.support.query.QueryObject.SortSpec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class QueryTypesTest extends AbstractQueryTest {

    private static final Log LOG = LogFactory.getLog(QueryTypesTest.class);
    private TypeManagerImpl tm;

    @Before
    public void setUp() throws Exception {
        tm = new TypeManagerImpl();
        tm.initTypeSystem(null); // create CMIS default types

        // create some types for testing
        List<TypeDefinition> typeDefs = super.createTypes();
        for (TypeDefinition typeDef : typeDefs)
            tm.addTypeDefinition(typeDef);

        // initialize query object with type manager
        super.setUp(new QueryObject(tm), null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void resolveTypesTest1() throws Exception {
        String statement = "SELECT " + TITLE_PROP + ", " + AUTHOR_PROP + " FROM " + BOOK_TYPE + " AS BooksAlias WHERE " + ISBN_PROP + " = '100'";
        verifyResolveSelect(statement);
    }

    @Test
    public void resolveTypesTest2() throws Exception {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = '100'";
        verifyResolveSelect(statement);
    }

    @Test
    public void resolveTypesTest3() throws Exception {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType AS BooksAlias WHERE ISBN = '100'";
        verifyResolveSelect(statement);
    }

    @Test
    public void resolveTypesTest4() throws Exception {
        String statement = "SELECT BooksAlias.Title, BooksAlias.Author FROM BookType AS BooksAlias WHERE ISBN = '100'";
        verifyResolveSelect(statement);
    }

    @Test
    public void resolveTypesTest5() throws Exception {
        String statement = "SELECT BooksAlias.Title AS abc, BooksAlias.Author def FROM BookType AS BooksAlias WHERE ISBN = '100'";
        verifyResolveSelect(statement);
    }

    @Test
    public void resolveTypesTest6() throws Exception {
        String statement = "SELECT BookType.UnknownProperty FROM BookType WHERE ISBN = '100'";
        try {
            verifyResolveSelect(statement);
            fail("Select of unknown property in type should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof RecognitionException);
            LOG.debug("resolveTypesTest6(), e: " + e.getMessage());
            assertTrue(e.toString().contains("is not a valid property query name in"));
        }
    }

    @Test
    public void resolveTypesTest7() throws Exception {
        String statement = "SELECT UnknownProperty FROM BookType WHERE ISBN = '100'";
        try {
            verifyResolveSelect(statement);
            fail("Select of unknown property in type should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof RecognitionException);
            assertTrue(e.toString().contains("is not a property query name in any"));
        }
    }

    @Test
    public void resolveTypesTest8() throws Exception {
        String statement = "SELECT BookType.Title, BookType.Author FROM BookType WHERE ISBN = '100'";
        verifyResolveSelect(statement);
    }

    @Test
    public void resolveTypesTest9() throws Exception {
        String statement = "SELECT BookType.Author, Title TitleAlias FROM BookType WHERE TitleAlias <> 'Harry Potter'";
        verifyResolveSelect(statement);
    }

    @Test
    public void resolveTypesTest10() throws Exception {
        String statement = "SELECT BookType.Author, BookType.Title TitleAlias FROM BookType WHERE TitleAlias <> 'Harry Potter'";
        verifyResolveSelect(statement);
    }

    private void verifyResolveSelect(String statement) throws Exception {
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        Map<String,String> types = queryObj.getTypes();
        assertTrue(1 == types.size());
        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(2 == selects.size());
        for (CmisSelector select : selects) {
            assertTrue(select instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) select);
            assertEquals(bookType, colRef.getTypeDefinition());
            assertTrue(colRef.getPropertyQueryName().equals(TITLE_PROP) || colRef.getPropertyQueryName().equals(AUTHOR_PROP));
        }
    }

    @Test
    public void resolveTypesWithTwoFromsQualified() throws Exception {
        String statement = "SELECT BookType.Title, MyDocType.MyStringProp FROM BookType JOIN MyDocType WHERE BookType.ISBN = '100'";

        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        Map<String,String> types = queryObj.getTypes();
        assertTrue(2 == types.size());
        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(2 == selects.size());

        ColumnReference colRef = ((ColumnReference) selects.get(0));
        assertEquals(colRef.getTypeDefinition(), bookType);
        assertTrue(colRef.getPropertyQueryName().equals(TITLE_PROP));

        colRef = ((ColumnReference) selects.get(1));
        assertEquals(colRef.getTypeDefinition(), myType);
        assertTrue(colRef.getPropertyQueryName().equals(STRING_PROP));
    }

    @Test
    public void resolveTypesWithTwoFromsUnqualified() throws Exception {
        String statement = "SELECT Title, MyStringProp FROM BookType JOIN MyDocType AS MyDocAlias WHERE BookType.ISBN = '100'";

        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
       Map<String,String> types = queryObj.getTypes();
        assertTrue(2 == types.size());
        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(2 == selects.size());

        ColumnReference colRef = ((ColumnReference) selects.get(0));
        assertEquals(colRef.getTypeDefinition(), bookType);
        assertTrue(colRef.getPropertyQueryName().equals(TITLE_PROP));

        colRef = ((ColumnReference) selects.get(1));
        assertEquals(colRef.getTypeDefinition(), myType);
        assertTrue(colRef.getPropertyQueryName().equals(STRING_PROP));
    }

    @Test
    public void resolveTypesWithTwoFromsNotUnique() throws Exception {
        String statement = "SELECT MyStringProp FROM MyDocTypeCopy JOIN MyDocType";

        try {
            traverseStatement(statement);
            fail("Select with an unqualified property that is not unique should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof RecognitionException);
            assertTrue(e.toString().contains("is not a unique property query name within the types in from"));
        }
    }

    @Test
    public void resolveTypesWithTwoFromsUniqueByQualifying() throws Exception {
        String statement = "SELECT MyDocType.MyStringProp FROM MyDocTypeCopy JOIN MyDocType";

        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        Map<String,String> types = queryObj.getTypes();
        assertTrue(2 == types.size());
        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        ColumnReference colRef = ((ColumnReference) selects.get(0));
        assertEquals(myType, colRef.getTypeDefinition());
        assertTrue(colRef.getPropertyQueryName().equals(STRING_PROP));
    }

    @Test
    public void resolveTypesTest11() throws Exception {
        String statement = "SELECT BookType.* FROM BookType WHERE ISBN = '100'";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        Map<String,String> types = queryObj.getTypes();
        assertTrue(1 == types.size());
        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        ColumnReference colRef = ((ColumnReference) selects.get(0));
        assertTrue(colRef.getPropertyQueryName().equals("*"));
        assertEquals(bookType, colRef.getTypeDefinition());
    }

    @Test
    public void resolveTypesTest12() throws Exception {
        String statement = "SELECT * FROM MyDocTypeCopy JOIN MyDocType";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        Map<String,String> types = queryObj.getTypes();
        assertTrue(2 == types.size());
        List<CmisSelector> selects = queryObj.getSelectReferences();
        assertTrue(1 == selects.size());
        ColumnReference colRef = ((ColumnReference) selects.get(0));
        assertTrue(colRef.getPropertyQueryName().equals("*"));
        assertEquals(null, colRef.getTypeDefinition());
    }

    @Test
    public void resolveTypesWhere1() throws Exception {
        String statement = "SELECT * FROM BookType WHERE ISBN = '100'";
        verifyResolveWhere(statement);
    }

    @Test
    public void resolveTypesWhere2() throws Exception {
        String statement = "SELECT * FROM BookType WHERE BookType.ISBN = '100'";
        verifyResolveWhere(statement);
    }

    @Test
    public void resolveTypesWhere3() throws Exception {
        String statement = "SELECT * FROM BookType As BookAlias WHERE BookAlias.ISBN = '100'";
        verifyResolveWhere(statement);
    }

    @Test
    public void resolveTypesWhere4() throws Exception {
        String statement = "SELECT BookType.ISBN IsbnAlias FROM BookType WHERE IsbnAlias < '100'";
        verifyResolveWhere(statement);
    }

    @Test
    public void resolveTypesWhereWithTwoFromsUnqualified() throws Exception {
        String statement = "SELECT * FROM BookType JOIN MyDocType WHERE ISBN = '100'";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<CmisSelector> wheres = queryObj.getWhereReferences();
        assertTrue(1 == wheres.size());
        for (CmisSelector where : wheres) {
            assertTrue(where instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) where);
            assertEquals(colRef.getTypeDefinition(), bookType);
            assertTrue(colRef.getPropertyQueryName().equals(ISBN_PROP));
        }
    }

    @Test
    public void resolveTypesWhereWithTwoFromsQualified() throws Exception {
        String statement = "SELECT * FROM BookType JOIN MyDocType AS MyDocAlias WHERE BookType.ISBN = '100'";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<CmisSelector> wheres = queryObj.getWhereReferences();
        assertTrue(1 == wheres.size());
        for (CmisSelector where : wheres) {
            assertTrue(where instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) where);
            assertEquals(colRef.getTypeDefinition(), bookType);
            assertTrue(colRef.getPropertyQueryName().equals(ISBN_PROP));
        }
    }


    @Test
    public void resolveTypesWhereWithTwoFromsQualifiedWithAlias() throws Exception {
        String statement = "SELECT * FROM BookType AS MyBookAlias JOIN MyDocType  WHERE MyBookAlias.ISBN = '100'";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<CmisSelector> wheres = queryObj.getWhereReferences();
        assertTrue(1 == wheres.size());
        for (CmisSelector where : wheres) {
            assertTrue(where instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) where);
            assertEquals(colRef.getTypeDefinition(), bookType);
            assertTrue(colRef.getPropertyQueryName().equals(ISBN_PROP));
        }
    }

    @Test
    public void resolveTypesWhereWithTwoFromsQualifiedWithAlias2() throws Exception {
//        String statement = "SELECT X.aaa FROM MyType AS X WHERE 10 = ANY X.aaa ";
        String statement = "SELECT MyBookAlias.Title FROM BookType AS MyBookAlias WHERE MyBookAlias.ISBN = '100'";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<CmisSelector> wheres = queryObj.getWhereReferences();
        assertTrue(1 == wheres.size());
        for (CmisSelector where : wheres) {
            assertTrue(where instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) where);
            assertEquals(colRef.getTypeDefinition(), bookType);
        }
    }

    private void verifyResolveWhere(String statement) throws Exception {
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        Map<String,String> types = queryObj.getTypes();
        assertTrue(1 == types.size());
        List<CmisSelector> wheres = queryObj.getWhereReferences();
        assertTrue(1 == wheres.size());
        for (CmisSelector where : wheres) {
            assertTrue(where instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) where);
            assertEquals(bookType, colRef.getTypeDefinition());
            assertTrue(colRef.getPropertyQueryName().equals(ISBN_PROP));
        }
    }

    @Test
    public void resolveTypesWhereWithTwoFromsNotUnique() throws Exception {
        String statement = "SELECT * FROM MyDocTypeCopy JOIN MyDocType WHERE MyStringProp = '100'";

        try {
            traverseStatement(statement);
            fail("Select with an unqualified property that is not unique should fail.");
        } catch (Exception e) {
            assertTrue(e instanceof RecognitionException);
            assertTrue(e.toString().contains("is not a unique property query name within the types in from"));
        }
    }

    @Test
    public void resolveTypesWhereWithTwoFromsUniqueByQualifying() throws Exception {
        String statement = "SELECT * FROM MyDocTypeCopy JOIN MyDocType WHERE MyDocType.MyStringProp = '100'";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<CmisSelector> wheres = queryObj.getWhereReferences();
        assertTrue(1 == wheres.size());
        for (CmisSelector where : wheres) {
            assertTrue(where instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) where);
            assertEquals(colRef.getTypeDefinition(), myType);
            assertTrue(colRef.getPropertyQueryName().equals(STRING_PROP));
        }
    }

    @Test
    public void resolveTypesOrderBy() throws Exception {
        String statement = "SELECT Title AS TitleAlias FROM BookType WHERE Author = 'Jim' ORDER BY TitleAlias";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<SortSpec> sorts = queryObj.getOrderBys();
        assertTrue(1 == sorts.size());
        for (SortSpec sort : sorts) {
            assertTrue(sort.getSelector() instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) sort.getSelector());
            assertEquals(colRef.getTypeDefinition(), bookType);
            assertTrue(colRef.getPropertyQueryName().equals(TITLE_PROP));
        }
    }

    @Test
    public void resolveTypesOrderBy2() throws Exception {
        String statement = "SELECT Title AS TitleAlias FROM BookType WHERE Author = 'Jim' ORDER BY BookType.Author";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<SortSpec> sorts = queryObj.getOrderBys();
        assertTrue(1 == sorts.size());
        for (SortSpec sort : sorts) {
            assertTrue(sort.getSelector() instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) sort.getSelector());
            assertEquals(colRef.getTypeDefinition(), bookType);
            assertTrue(colRef.getPropertyQueryName().equals(AUTHOR_PROP));
        }
    }

    @Test
    public void resolveTypesOrderBy3() throws Exception {
        String statement = "SELECT Title FROM BookType WHERE ISBN < '100' ORDER BY Author";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<SortSpec> sorts = queryObj.getOrderBys();
        assertTrue(1 == sorts.size());
        for (SortSpec sort : sorts) {
            assertTrue(sort.getSelector() instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) sort.getSelector());
            assertEquals(colRef.getTypeDefinition(), bookType);
            assertTrue(colRef.getPropertyQueryName().equals(AUTHOR_PROP));
        }
    }

    @Test
    public void resolveJoinTypesSimple() throws Exception {
        String statement = "SELECT * FROM MyDocType JOIN BookType ON MyDocType.MyStringProp = BookType.Title";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<CmisSelector> joins = queryObj.getJoinReferences();
        assertTrue(2 == joins.size());
        for (CmisSelector join : joins) {
            assertTrue(join instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) join);
            if (myType.equals(colRef.getTypeDefinition())) {
                assertTrue(colRef.getPropertyQueryName().equals(STRING_PROP));
            } else if (bookType.equals(colRef.getTypeDefinition())) {
                assertTrue(colRef.getPropertyQueryName().equals(TITLE_PROP));
            } else
                fail("Unexpected type in JOIN reference");
        }
    }

    @Test
    public void resolveJoinTypesWithAlias() throws Exception {
        String statement = "SELECT Y.ISBN, X.MyBooleanProp, Y.Author FROM (MyDocType AS X JOIN BookType AS Y ON X.MyStringProp = Y.Title) "+
                           "WHERE ('Joe' = ANY Y.Author)";
//        "SELECT    Y.CLAIM_NUM, X.PROPERTY_ADDRESS, Y.DAMAGE_ESTIMATES " +
//        "FROM   ( POLICY AS X JOIN CLAIMS AS Y ON X.POLICY_NUM = Y.POLICY_NUM ) " +
//        "WHERE ( 100000 = ANY Y.DAMAGE_ESTIMATES )";
        CmisQueryWalker walker = traverseStatement(statement);
        assertNotNull(walker);
        List<CmisSelector> joins = queryObj.getJoinReferences();
        assertTrue(2 == joins.size());
        for (CmisSelector join : joins) {
            assertTrue(join instanceof ColumnReference);
            ColumnReference colRef = ((ColumnReference) join);
            if (myType.equals(colRef.getTypeDefinition())) {
                assertTrue(colRef.getPropertyQueryName().equals(STRING_PROP));
            } else if (bookType.equals(colRef.getTypeDefinition())) {
                assertTrue(colRef.getPropertyQueryName().equals(TITLE_PROP));
            } else
                fail("Unexpected type in JOIN reference");
        }
    }
}
