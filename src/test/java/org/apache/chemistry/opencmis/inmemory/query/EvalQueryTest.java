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

import static org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator.COMPLEX_TYPE;
import static org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator.PROP_ID_BOOLEAN;
import static org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator.PROP_ID_DATETIME;
import static org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator.PROP_ID_DECIMAL;
import static org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator.PROP_ID_INT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.inmemory.AbstractServiceTest;
import org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EvalQueryTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(EvalQueryTest.class);
    private QueryTestDataCreator dataCreator;

    @Override
    @Before
    public void setUp() {

        // initialize query object with type manager
        super.setTypeCreatorClass(UnitTestTypeSystemCreator.class.getName());
        super.setUp();
        //create test data
        dataCreator = new QueryTestDataCreator(fRepositoryId, fRootFolderId, fObjSvc, fVerSvc);
        dataCreator.createBasicTestData();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testAll() {
        String statement = "SELECT * FROM cmis:document";
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertFalse(resultContains("jens", res));
    }

    //////////////////////////////////////////////////////////////////////
    // Boolean tests

    @Test
    public void testBooleanEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_BOOLEAN + "= true";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testBooleanNotEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_BOOLEAN + "= false";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("epsilon", res));
    }

    //////////////////////////////////////////////////////////////////////
    // Integer tests

    @Test
    public void testIntegerEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= 100";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testIntegerNotEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "<> 100";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testIntegerLess() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "< 0";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
    }

    @Test
    public void testIntegerLessOrEqual() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "<= 0";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
    }

    @Test
    public void testIntegerGreater() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "> 0";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testIntegerGreaterOrEqual() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + ">= 0";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
    }

    //////////////////////////////////////////////////////////////////////
    // Decimal tests

    @Test
    public void testDecimalEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "= 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testDecimalNotEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "<> 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testDecimalLess() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "< 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
    }

    @Test
    public void testDecimalLessOrEqual() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "<= 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testDecimalGreater() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "> 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testDecimalGreaterOrEqual() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + ">= 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
    }

    //////////////////////////////////////////////////////////////////////
    // DateTime tests

    @Test
    public void testDateEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME + "= TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testDateNotEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME + "<> TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testDateLess() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME + "< TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
    }

    @Test
    public void testDateLessOrEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME + "<= TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testDategreater() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME + "> TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("epsilon", res));
    }

    //    @Test
    public void testDateGreaterOrEqual() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME + ">= TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
    }

    ////////////////////////////////////////////////////////////////////
    // String tests

    @Test
    public void testStringEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + "= 'Alpha'";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
    }

    @Test
    public void testStringNotEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + "<> 'Gamma'";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testStringLess() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + "< 'Delta'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
    }

    @Test
    public void testStringLessOrEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + "<= 'Delta'";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testStringGreater() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + "> 'Delta'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testStringGreaterOrEquals() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + ">= 'Delta'";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
    }

    ////////////////////////////////////////////////////////////////////
    // Boolean condition tests

    @Test
    public void testAnd() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= 50 AND " + PROP_ID_BOOLEAN + "= true";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("delta", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= 50 AND " + PROP_ID_BOOLEAN + "= false";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());
    }

    @Test
    public void testOr() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= -50 OR " + PROP_ID_BOOLEAN + "= false";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testNot() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE NOT " + PROP_ID_INT + "= 50";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testOrderByString() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + UnitTestTypeSystemCreator.PROP_ID_STRING;
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("alpha", 0, res));
        assertTrue(resultContainsAtPos("beta", 1, res));
        assertTrue(resultContainsAtPos("delta", 2, res));
        assertTrue(resultContainsAtPos("epsilon", 3, res));
        assertTrue(resultContainsAtPos("gamma", 4, res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + UnitTestTypeSystemCreator.PROP_ID_STRING + " DESC";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("alpha", 4, res));
        assertTrue(resultContainsAtPos("beta", 3, res));
        assertTrue(resultContainsAtPos("delta", 2, res));
        assertTrue(resultContainsAtPos("epsilon", 1, res));
        assertTrue(resultContainsAtPos("gamma", 0, res));
}

    @Test
    public void testOrderByInteger() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_INT;
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("alpha", 0, res));
        assertTrue(resultContainsAtPos("beta", 1, res));
        assertTrue(resultContainsAtPos("gamma", 2, res));
        assertTrue(resultContainsAtPos("delta", 3, res));
        assertTrue(resultContainsAtPos("epsilon", 4, res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_INT + " DESC";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("alpha", 4, res));
        assertTrue(resultContainsAtPos("beta", 3, res));
        assertTrue(resultContainsAtPos("gamma", 2, res));
        assertTrue(resultContainsAtPos("delta", 1, res));
        assertTrue(resultContainsAtPos("epsilon", 0, res));
}

    @Test
    public void testOrderByDecimal() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_DECIMAL;
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("alpha", 0, res));
        assertTrue(resultContainsAtPos("beta", 1, res));
        assertTrue(resultContainsAtPos("delta", 2, res));
        assertTrue(resultContainsAtPos("gamma", 3, res));
        assertTrue(resultContainsAtPos("epsilon", 4, res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_DECIMAL + " DESC";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("alpha", 4, res));
        assertTrue(resultContainsAtPos("beta", 3, res));
        assertTrue(resultContainsAtPos("delta", 2, res));
        assertTrue(resultContainsAtPos("gamma", 1, res));
        assertTrue(resultContainsAtPos("epsilon", 0, res));
    }

    @Test
    public void testOrderByDate() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_DATETIME;
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("beta", 0, res));
        assertTrue(resultContainsAtPos("alpha", 1, res));
        assertTrue(resultContainsAtPos("gamma", 2, res));
        assertTrue(resultContainsAtPos("delta", 3, res));
        assertTrue(resultContainsAtPos("epsilon", 4, res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_DATETIME + " DESC";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("beta", 4, res));
        assertTrue(resultContainsAtPos("alpha", 3, res));
        assertTrue(resultContainsAtPos("gamma", 2, res));
        assertTrue(resultContainsAtPos("delta", 1, res));
        assertTrue(resultContainsAtPos("epsilon", 0, res));
    }

    @Test
    public void testOrderByBool() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_BOOLEAN;
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("beta", 0, res) || resultContainsAtPos("beta", 1, res));
        assertTrue(resultContainsAtPos("epsilon", 0, res) || resultContainsAtPos("epsilon", 1, res));
        assertTrue(resultContainsAtPos("alpha", 2, res) || resultContainsAtPos("alpha", 3, res) || resultContainsAtPos("alpha", 4, res));
        assertTrue(resultContainsAtPos("gamma", 2, res) || resultContainsAtPos("gamma", 3, res) || resultContainsAtPos("gamma", 4, res));
        assertTrue(resultContainsAtPos("delta", 2, res) || resultContainsAtPos("delta", 3, res) || resultContainsAtPos("delta", 4, res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_BOOLEAN + " DESC";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("beta", 3, res) || resultContainsAtPos("beta", 4, res));
        assertTrue(resultContainsAtPos("epsilon", 3, res) || resultContainsAtPos("epsilon", 4, res));
        assertTrue(resultContainsAtPos("alpha", 2, res) || resultContainsAtPos("alpha", 1, res) || resultContainsAtPos("alpha", 0, res));
        assertTrue(resultContainsAtPos("gamma", 2, res) || resultContainsAtPos("gamma", 1, res) || resultContainsAtPos("gamma", 0, res));
        assertTrue(resultContainsAtPos("delta", 2, res) || resultContainsAtPos("delta", 1, res) || resultContainsAtPos("delta", 0, res));
    }
    
    // reported JIRA issue CMIS-510
    @Test
    public void testOrderBySystemProperties() {
        String statement = "SELECT * from cmis:document ORDER BY "+ PropertyIds.NAME;
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        statement = "SELECT * from cmis:document ORDER BY " + PropertyIds.CREATION_DATE + " ASC";
        assertEquals(5, res.getObjects().size());
        statement = "SELECT * from cmis:document ORDER BY " + PropertyIds.LAST_MODIFICATION_DATE + " DESC";
        assertEquals(5, res.getObjects().size());
    }

    @Test
    public void testIsNull() {
        dataCreator.createNullTestDocument();
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + " IS NULL";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("nulldoc", res));
    }

    @Test
    public void testIsNotNull() {
        dataCreator.createNullTestDocument();
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + " IS NOT NULL";
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void patternTest() {
        String res = InMemoryQueryProcessor.translatePattern("ABC%def");
        assertEquals("ABC.*def", res);
        res = InMemoryQueryProcessor.translatePattern("%abc");
        assertEquals(".*abc", res);
        res = InMemoryQueryProcessor.translatePattern("abc%");
        assertEquals("abc.*", res);
        res = InMemoryQueryProcessor.translatePattern("ABC\\%def");
        assertEquals("ABC\\%def", res);
        res = InMemoryQueryProcessor.translatePattern("\\%abc");
        assertEquals("\\%abc", res);
        res = InMemoryQueryProcessor.translatePattern("abc%def%ghi");
        assertEquals("abc.*def.*ghi", res);
        res = InMemoryQueryProcessor.translatePattern("abc%def\\%ghi%jkl");
        assertEquals("abc.*def\\%ghi.*jkl", res);

        res = InMemoryQueryProcessor.translatePattern("ABC_def");
        assertEquals("ABC.def", res);
        res = InMemoryQueryProcessor.translatePattern("_abc");
        assertEquals(".abc", res);
        res = InMemoryQueryProcessor.translatePattern("abc_");
        assertEquals("abc.", res);
        res = InMemoryQueryProcessor.translatePattern("ABC\\_def");
        assertEquals("ABC\\_def", res);
        res = InMemoryQueryProcessor.translatePattern("\\_abc");
        assertEquals("\\_abc", res);
        res = InMemoryQueryProcessor.translatePattern("abc_def_ghi");
        assertEquals("abc.def.ghi", res);
        res = InMemoryQueryProcessor.translatePattern("abc_def\\_ghi_jkl");
        assertEquals("abc.def\\_ghi.jkl", res);
    }

    @Test
    public void testLike() {
        dataCreator.createLikeTestDocuments(fRootFolderId);
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " LIKE 'ABC%'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc2", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " LIKE '%ABC'";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("likedoc3", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " LIKE '%ABC%'";
        res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc2", res));
        assertTrue(resultContains("likedoc3", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " LIKE 'AB_DEF'";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
    }

    @Test
    public void testNotLike() {
        dataCreator.createLikeTestDocuments(fRootFolderId);
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " NOT LIKE 'ABC%'";
        ObjectList res = doQuery(statement);
        assertEquals(6, res.getObjects().size());
        assertTrue(resultContains("likedoc3", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " NOT LIKE '%a'";
        res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc3", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testInFolder() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_FOLDER('" + fRootFolderId + "')";
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_FOLDER('" + dataCreator.getFolder1() + "')";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_FOLDER(" + COMPLEX_TYPE + ", '" + fRootFolderId + "')";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_FOLDER(UnknownType, '" + dataCreator.getFolder2() + "')";
        try {
            res = doQuery(statement);
            fail("Unknown type in folder should throw exception");
        } catch (Exception e) {
            assertTrue(e.toString().contains("is neither a type query name nor an alias"));
            log.debug("expected Exception: " + e);
        }
    }

    @Test
    public void testInTree() {
        dataCreator.createLikeTestDocuments(dataCreator.getFolder11());

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_TREE(" + COMPLEX_TYPE + ", '" + dataCreator.getFolder1() + "')";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc2", res));
        assertTrue(resultContains("likedoc3", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_FOLDER('" + dataCreator.getFolder1() + "')";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_TREE('" + dataCreator.getFolder2() + "')";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_TREE(UnknownType, '" + dataCreator.getFolder2() + "')";
        try {
            res = doQuery(statement);
            fail("Unknown type in folder should throw exception");
        } catch (Exception e) {
            assertTrue(e.toString().contains("is neither a type query name nor an alias"));
            log.debug("expected Exception: " + e);
        }
    }

    @Test
    public void testIn() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " IN ('Alpha', 'Beta', 'Gamma')";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " IN ('Theta', 'Pi', 'Rho')";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());
    }

    @Test
    public void testNotIn() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " NOT IN ('Alpha', 'Beta', 'Gamma')";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING + " NOT IN ('Theta', 'Pi', 'Rho')";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testMultiValueInAny() {
        dataCreator.createMultiValueDocuments();

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " IN ('red', 'black', 'grey')";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("mv-alpha", res));
        assertTrue(resultContains("mv-beta", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " IN ('green', 'black', 'grey')";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("mv-alpha", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " IN ('white', 'black', 'grey')";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());
    }

    @Test
    public void testMultiValueNotInAny() {
        dataCreator.createMultiValueDocuments();

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " NOT IN ('red', 'black', 'grey')";
        ObjectList res = doQuery(statement);
        assertEquals(0, res.getObjects().size());

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " NOT IN ('green', 'black', 'grey')";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("mv-beta", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " NOT IN ('white', 'black', 'grey')";
        res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("mv-alpha", res));
        assertTrue(resultContains("mv-beta", res));
    }

    @Test
    public void testMultiValueEqAny() {
        dataCreator.createMultiValueDocuments();

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE 'red' = ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE ;
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("mv-alpha", res));
        assertTrue(resultContains("mv-beta", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE 'black' = ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE ;
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE 'black' = ANY " + UnitTestTypeSystemCreator.PROP_ID_STRING;
        try {
            doQuery(statement);
            fail("Unknown = ANY with single value prop should throw exception");
        } catch (Exception e) {
            assertTrue(e.toString().contains("only is allowed on multi-value properties"));
            log.debug("expected Exception: " + e);
        }
    }

    @Test
    public void testVersionsWithQuery() {
        String id = dataCreator.createVersionedDocument();
        assertNotNull(id);
        String statement = "SELECT * FROM " + UnitTestTypeSystemCreator.VERSIONED_TYPE;
        ObjectList res = doQueryAllVersions(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("ver123", UnitTestTypeSystemCreator.VERSION_PROPERTY_ID, res));
        assertTrue(resultContains("ver456", UnitTestTypeSystemCreator.VERSION_PROPERTY_ID, res));
        assertTrue(resultContains("V 1.0", PropertyIds.VERSION_LABEL, res));
        assertTrue(resultContains("V 2.0", PropertyIds.VERSION_LABEL, res));

        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertFalse(resultContains("V 1.0", PropertyIds.VERSION_LABEL, res));
        assertTrue(resultContains("V 2.0", PropertyIds.VERSION_LABEL, res));
    }

    @Test
    public void testLastestVersionsWithQuery() {
        String id = dataCreator.createVersionedDocument();
        assertNotNull(id);
        String statement = "SELECT * FROM " + UnitTestTypeSystemCreator.VERSIONED_TYPE;
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("ver456", UnitTestTypeSystemCreator.VERSION_PROPERTY_ID, res));
        assertTrue(resultContains("V 2.0", PropertyIds.VERSION_LABEL, res));

        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertFalse(resultContains("V 1.0", PropertyIds.VERSION_LABEL, res));
        assertTrue(resultContains("V 2.0", PropertyIds.VERSION_LABEL, res));
    }
    
    @Test
    public void testContainsWord() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('cat')";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testContainsPhrase() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('\\'Kitty Katty\\'')";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("beta", res));
    }

    @Test
    public void testContainsNot() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('-cat')";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
    }

    @Test
    public void testContainsOr() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('cat OR dog')";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testContainsAnd() {
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('cat dog')";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("delta", res));
    }

    @Test
    public void testContainsAndScore() {
        String statement = "SELECT cmis:objectId,cmis:name,SCORE() FROM " + COMPLEX_TYPE + " WHERE CONTAINS('dog')";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains(1.0, "SEARCH_SCORE", res));
    }

    private ObjectList doQuery(String queryString) {
        log.debug("\nExecuting query: " + queryString);
        ObjectList res = fDiscSvc.query(fRepositoryId, queryString, false, false,
                IncludeRelationships.NONE, null, null, null, null);
        log.debug("Query result, number of matching objects: " + res.getNumItems());
        for (ObjectData od : res.getObjects()) {
            log.debug("Found matching object: " + od.getProperties().getProperties().get(PropertyIds.NAME).getFirstValue());
        }
        return res;
    }

    private ObjectList doQueryAllVersions(String queryString) {
        log.debug("\nExecuting query: " + queryString);
        ObjectList res = fDiscSvc.query(fRepositoryId, queryString, true, false,
                IncludeRelationships.NONE, null, null, null, null);
        log.debug("Query result, number of matching objects: " + res.getNumItems());
        for (ObjectData od : res.getObjects()) {
            log.debug("Found matching object: " + od.getProperties().getProperties().get(PropertyIds.NAME).getFirstValue());
        }
        return res;
    }

    private static boolean resultContains(String name, String propId, ObjectList results) {
        for (ObjectData od : results.getObjects()) {
            String nameProp = (String) od.getProperties().getProperties().get(propId).getFirstValue();
            if (name.equals(nameProp)) {
                return true;
            }
        }
        return false;
    }

    private static boolean resultContains(Double val, String propId, ObjectList results) {
        for (ObjectData od : results.getObjects()) {
            BigDecimal bd = (BigDecimal) od.getProperties().getProperties().get(propId).getFirstValue();
            if (val.equals(bd.doubleValue())) {
                return true;
            }
        }
        return false;
    }

    private static boolean resultContains(String name, ObjectList results) {
        return resultContains(name, PropertyIds.NAME, results);
    }

    private static boolean resultContainsAtPos(String name, int index, ObjectList results) {
        String nameProp = (String) results.getObjects().get(index).getProperties().getProperties().get(PropertyIds.NAME).getFirstValue();
        return name.equals(nameProp);
    }
}
