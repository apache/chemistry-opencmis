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
import static org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator.PROP_ID_ID;
import static org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator.PROP_ID_INT;
import static org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator.SECONDARY_TYPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.antlr.runtime.RecognitionException;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.inmemory.AbstractServiceTest;
import org.apache.chemistry.opencmis.inmemory.UnitTestTypeSystemCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvalQueryTest extends AbstractServiceTest {

    private static final Logger log = LoggerFactory.getLogger(EvalQueryTest.class);
    private QueryTestDataCreator dataCreator;
    static int COUNT = 0;

    @Override
    @Before
    public void setUp() {
    	setUp(false);
    }

    protected void setUp(boolean parserMode) {
        // initialize query object with type manager
        super.setTypeCreatorClass(UnitTestTypeSystemCreator.class.getName());
        super.setUp(parserMode);
        // create test data
        dataCreator = new QueryTestDataCreator(fRepositoryId, fRootFolderId, fObjSvc, fVerSvc);
        dataCreator.createBasicTestData();
    }
    
    @Override
    @After
    public void tearDown() {
        log.debug("tearDown started.");
        super.tearDown();
        log.debug("tearDown done.");
    }

    @Test
    public void testAll() {
        log.debug("Start testAll...");
        String statement = "SELECT * FROM cmis:document";
        ObjectList res = doQuery(statement);
        assertEquals(6, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertFalse(resultContains("jens", res));
        log.debug("...Stop testAll.");
    }

    // ////////////////////////////////////////////////////////////////////
    // Boolean tests

    @Test
    public void testBooleanEquals() {
        log.debug("Start testBooleanEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_BOOLEAN + "= true";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testBooleanEquals.");
    }

    @Test
    public void testBooleanNotEquals() {
        log.debug("Start testBooleanNotEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_BOOLEAN + "= false";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testBooleanNotEquals.");
    }

    // ////////////////////////////////////////////////////////////////////
    // Integer tests

    @Test
    public void testIntegerEquals() {
        log.debug("Start testIntegerEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= 100";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testIntegerEquals.");
    }

    @Test
    public void testIntegerNotEquals() {
        log.debug("Start testIntegerNotEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "<> 100";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testIntegerNotEquals.");
    }

    @Test
    public void testIntegerLess() {
        log.debug("Start testIntegerLess...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "< 0";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        log.debug("...Stop testIntegerLess.");
    }

    @Test
    public void testIntegerLessOrEqual() {
        log.debug("Start testIntegerLessOrEqual...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "<= 0";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        log.debug("...Stop testIntegerLessOrEqual.");
    }

    @Test
    public void testIntegerGreater() {
        log.debug("Start testIntegerGreater...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "> 0";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testIntegerGreater.");
    }

    @Test
    public void testIntegerGreaterOrEqual() {
        log.debug("Start testIntegerGreaterOrEqual...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + ">= 0";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testIntegerGreaterOrEqual.");
    }

    // ////////////////////////////////////////////////////////////////////
    // Decimal tests

    @Test
    public void testDecimalEquals() {
        log.debug("Start testDecimalEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "= 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testDecimalEquals.");
    }

    @Test
    public void testDecimalNotEquals() {
        log.debug("Start testDecimalNotEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "<> 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testDecimalNotEquals.");
    }

    @Test
    public void testDecimalLess() {
        log.debug("Start testDecimalLess...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "< 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        log.debug("...Stop testDecimalLess.");
    }

    @Test
    public void testDecimalLessOrEqual() {
        log.debug("Start testDecimalLessOrEqual...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "<= 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testDecimalLessOrEqual.");
    }

    @Test
    public void testDecimalGreater() {
        log.debug("Start testDecimalGreater...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + "> 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testDecimalGreater.");
    }

    @Test
    public void testDecimalGreaterOrEqual() {
        log.debug("Start testDecimalGreaterOrEqual...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DECIMAL + ">= 1.23456E-6";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testDecimalGreaterOrEqual.");
    }

    // ////////////////////////////////////////////////////////////////////
    // DateTime tests

    @Test
    public void testDateEquals() {
        log.debug("Start testDateEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME
                + "= TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testDateEquals.");
    }

    @Test
    public void testDateNotEquals() {
        log.debug("Start testDateNotEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME
                + "<> TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testDateNotEquals.");
    }

    @Test
    public void testDateLess() {
        log.debug("Start testDateLess...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME
                + "< TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        log.debug("...Stop testDateLess.");
    }

    @Test
    public void testDateLessOrEquals() {
        log.debug("Start testDateLessOrEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME
                + "<= TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testDateLessOrEquals.");
    }

    @Test
    public void testDategreater() {
        log.debug("Start testDateGreaterOrEqual...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME
                + "> TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testDateGreaterOrEqual.");
    }

    // @Test
    public void testDateGreaterOrEqual() {
        log.debug("Start testDateGreaterOrEqual...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_DATETIME
                + ">= TIMESTAMP '2038-01-20T00:00:00.000Z'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testDateGreaterOrEqual.");
    }

    // //////////////////////////////////////////////////////////////////
    // String tests

    @Test
    public void testStringEquals() {
        log.debug("Start testStringEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + "= 'Alpha'";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        log.debug("...Stop testStringEquals.");
    }

    @Test
    public void testStringNotEquals() {
        log.debug("Start testStringNotEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + "<> 'Gamma'";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testStringNotEquals.");
    }

    @Test
    public void testStringLess() {
        log.debug("Start testStringLess...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + "< 'Delta'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        log.debug("...Stop testStringLess.");
    }

    @Test
    public void testStringLessOrEquals() {
        log.debug("Start testStringLessOrEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + "<= 'Delta'";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testStringLessOrEquals.");
    }

    @Test
    public void testStringGreater() {
        log.debug("Start testStringGreater...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + "> 'Delta'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testStringGreater.");
    }

    @Test
    public void testStringGreaterOrEquals() {
        log.debug("Start testStringGreaterOrEquals...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + ">= 'Delta'";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testStringGreaterOrEquals.");
    }

    @Test
    public void testStringEscape() {
        log.debug("Start testStringEscape...");
        String statement = "SELECT * FROM " + BaseTypeId.CMIS_DOCUMENT.value() + " WHERE " + PropertyIds.NAME
                + "='John\\'s Document'";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains( "John's Document", res));
        log.debug("...Stop testStringEscape.");
    }
    // //////////////////////////////////////////////////////////////////
    // Boolean condition tests

    @Test
    public void testAnd() {
        log.debug("Start testAnd...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= 50 AND " + PROP_ID_BOOLEAN
                + "= true";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("delta", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= 50 AND " + PROP_ID_BOOLEAN
                + "= false";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());
        log.debug("...Stop testAnd.");
    }

    @Test
    public void testOr() {
        log.debug("Start testOr...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= -50 OR " + PROP_ID_BOOLEAN
                + "= false";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testOr.");
    }

    @Test
    public void testNot() {
        log.debug("Start testNot...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE NOT " + PROP_ID_INT + "= 50";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testNot.");
    }

    @Test
    public void testOrderByString() {
        log.debug("Start testOrderByString...");
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
        log.debug("...Stop testOrderByString.");
    }

    @Test
    public void testOrderByInteger() {
        log.debug("Start testOrderByInteger...");
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
        log.debug("...Stop testOrderByInteger.");
    }

    @Test
    public void testOrderByDecimal() {
        log.debug("Start testOrderByDecimal...");
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
        log.debug("...Stop testOrderByDecimal.");
    }

    @Test
    public void testOrderByDate() {
        log.debug("Start testOrderByDate...");
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
        log.debug("...Stop testOrderByDate.");
    }

    @Test
    public void testOrderByBool() {
        log.debug("Start testOrderByBool...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_BOOLEAN;
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("beta", 0, res) || resultContainsAtPos("beta", 1, res));
        assertTrue(resultContainsAtPos("epsilon", 0, res) || resultContainsAtPos("epsilon", 1, res));
        assertTrue(resultContainsAtPos("alpha", 2, res) || resultContainsAtPos("alpha", 3, res)
                || resultContainsAtPos("alpha", 4, res));
        assertTrue(resultContainsAtPos("gamma", 2, res) || resultContainsAtPos("gamma", 3, res)
                || resultContainsAtPos("gamma", 4, res));
        assertTrue(resultContainsAtPos("delta", 2, res) || resultContainsAtPos("delta", 3, res)
                || resultContainsAtPos("delta", 4, res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " ORDER BY " + PROP_ID_BOOLEAN + " DESC";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContainsAtPos("beta", 3, res) || resultContainsAtPos("beta", 4, res));
        assertTrue(resultContainsAtPos("epsilon", 3, res) || resultContainsAtPos("epsilon", 4, res));
        assertTrue(resultContainsAtPos("alpha", 2, res) || resultContainsAtPos("alpha", 1, res)
                || resultContainsAtPos("alpha", 0, res));
        assertTrue(resultContainsAtPos("gamma", 2, res) || resultContainsAtPos("gamma", 1, res)
                || resultContainsAtPos("gamma", 0, res));
        assertTrue(resultContainsAtPos("delta", 2, res) || resultContainsAtPos("delta", 1, res)
                || resultContainsAtPos("delta", 0, res));
        log.debug("...Stop testOrderByBool.");
    }

    // reported JIRA issue CMIS-510
    @Test
    public void testOrderBySystemProperties() {
        log.debug("Start testOrderBySystemProperties...");
        String statement = "SELECT * from cmis:document ORDER BY " + PropertyIds.NAME;
        ObjectList res = doQuery(statement);
        assertEquals(6, res.getObjects().size());
        statement = "SELECT * from cmis:document ORDER BY " + PropertyIds.CREATION_DATE + " ASC";
        assertEquals(6, res.getObjects().size());
        statement = "SELECT * from cmis:document ORDER BY " + PropertyIds.LAST_MODIFICATION_DATE + " DESC";
        assertEquals(6, res.getObjects().size());
        log.debug("...Stop testOrderBySystemProperties.");
    }

    @Test
    public void testIsNull() {
        log.debug("Start testIsNull...");

        dataCreator.createNullTestDocument();
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + " IS NULL";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("nulldoc", res));
        log.debug("...Stop testIsNull.");
    }

    @Test
    public void testIsNotNull() {
        log.debug("Start testIsNotNull...");
        dataCreator.createNullTestDocument();
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + " IS NOT NULL";
        ObjectList res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testIsNotNull.");
    }

    @Test
    public void patternTest() {
        log.debug("Start patternTest...");
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
        log.debug("...Stop patternTest.");
    }

    @Test
    public void testLike() {
        log.debug("Start testLike...");
        dataCreator.createLikeTestDocuments(fRootFolderId);
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " LIKE 'ABC%'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc2", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " LIKE '%ABC'";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("likedoc3", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " LIKE '%ABC%'";
        res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc2", res));
        assertTrue(resultContains("likedoc3", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " LIKE 'AB_DEF'";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
        log.debug("...Stop testLike.");
    }

    @Test
    public void testNotLike() {
        log.debug("Start testNotLike...");
        dataCreator.createLikeTestDocuments(fRootFolderId);
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " NOT LIKE 'ABC%'";
        ObjectList res = doQuery(statement);
        assertEquals(6, res.getObjects().size());
        assertTrue(resultContains("likedoc3", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " NOT LIKE '%a'";
        res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc1", res));
        assertTrue(resultContains("likedoc3", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testNotLike.");
    }

    @Test
    public void testInFolder() {
        log.debug("Start testInFolder...");
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

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_FOLDER(UnknownType, '" + dataCreator.getFolder2()
                + "')";
        try {
            res = doQuery(statement);
            fail("Unknown type in folder should throw exception");
        } catch (Exception e) {
            assertTrue(e.toString().contains("is neither a type query name nor an alias"));
            log.debug("expected Exception: " + e);
        }
        log.debug("...Stop testInFolder.");
    }

    @Test
    public void testInTree() {
        log.debug("Start testInTree...");
        dataCreator.createLikeTestDocuments(dataCreator.getFolder11());

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE IN_TREE(" + COMPLEX_TYPE + ", '"
                + dataCreator.getFolder1() + "')";
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
        log.debug("...Stop testInTree.");
    }

    @Test
    public void testIn() {
        log.debug("Start testNotIn...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " IN ('Alpha', 'Beta', 'Gamma')";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " IN ('Theta', 'Pi', 'Rho')";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());
        log.debug("...Stop testNotIn.");
    }

    @Test
    public void testNotIn() {
        log.debug("Start testNotIn...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " NOT IN ('Alpha', 'Beta', 'Gamma')";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + " NOT IN ('Theta', 'Pi', 'Rho')";
        res = doQuery(statement);
        assertEquals(5, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testNotIn.");
    }

    @Test
    public void testMultiValueInAny() {
        log.debug("Start testMultiValueNotInAny...");
        dataCreator.createMultiValueDocuments();

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " IN ('red', 'black', 'grey')";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("mv-alpha", res));
        assertTrue(resultContains("mv-beta", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " IN ('green', 'black', 'grey')";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("mv-alpha", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " IN ('white', 'black', 'grey')";
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());
        log.debug("...Stop testMultiValueNotInAny.");
    }

    @Test
    public void testMultiValueNotInAny() {
        log.debug("Start testMultiValueNotInAny...");
        dataCreator.createMultiValueDocuments();

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " NOT IN ('red', 'black', 'grey')";
        ObjectList res = doQuery(statement);
        assertEquals(0, res.getObjects().size());

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " NOT IN ('green', 'black', 'grey')";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("mv-beta", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE + " NOT IN ('white', 'black', 'grey')";
        res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("mv-alpha", res));
        assertTrue(resultContains("mv-beta", res));
        log.debug("...Stop testMultiValueNotInAny.");
    }

    @Test
    public void testMultiValueEqAny() {
        log.debug("Start testMultiValueEqAny...");
        dataCreator.createMultiValueDocuments();

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE 'red' = ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE;
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("mv-alpha", res));
        assertTrue(resultContains("mv-beta", res));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE 'black' = ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING_MULTI_VALUE;
        res = doQuery(statement);
        assertEquals(0, res.getObjects().size());

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE 'black' = ANY "
                + UnitTestTypeSystemCreator.PROP_ID_STRING;
        try {
            doQuery(statement);
            fail("Unknown = ANY with single value prop should throw exception");
        } catch (Exception e) {
            assertTrue(e.toString().contains("only is allowed on multi-value properties"));
            log.debug("expected Exception: " + e);
        }
        log.debug("...Stop testMultiValueEqAny.");
    }

    @Test
    public void testVersionsWithQuery() {
        log.debug("Start testLastestVersionsWithQuery...");
        String id = dataCreator.createVersionedDocument();
        assertNotNull(id);
        String statement = "SELECT * FROM " + UnitTestTypeSystemCreator.VERSIONED_TYPE;
        ObjectList res = doQueryAllVersions(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("ver123", UnitTestTypeSystemCreator.VERSION_PROPERTY_ID, res));
        assertTrue(resultContains("ver456", UnitTestTypeSystemCreator.VERSION_PROPERTY_ID, res));
        assertTrue(resultContains("1.0", PropertyIds.VERSION_LABEL, res));
        assertTrue(resultContains("2.0", PropertyIds.VERSION_LABEL, res));

        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertFalse(resultContains("1.0", PropertyIds.VERSION_LABEL, res));
        assertTrue(resultContains("2.0", PropertyIds.VERSION_LABEL, res));
        log.debug("...Stop testLastestVersionsWithQuery.");
    }

    @Test
    public void testLastestVersionsWithQuery() {
        log.debug("Start testLastestVersionsWithQuery...");
        String id = dataCreator.createVersionedDocument();
        assertNotNull(id);
        String statement = "SELECT * FROM " + UnitTestTypeSystemCreator.VERSIONED_TYPE;
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("ver456", UnitTestTypeSystemCreator.VERSION_PROPERTY_ID, res));
        assertTrue(resultContains("2.0", PropertyIds.VERSION_LABEL, res));

        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertFalse(resultContains("1.0", PropertyIds.VERSION_LABEL, res));
        assertTrue(resultContains("2.0", PropertyIds.VERSION_LABEL, res));
        log.debug("...Stop testLastestVersionsWithQuery.");
    }

    @Test
    public void testContainsWord() {
        log.debug("Start testContainsWord...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('cat')";
        ObjectList res = doQuery(statement);
        assertEquals(3, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testContainsWord.");
    }

    @Test
    public void testContainsPhrase() {
        log.debug("Start testContainsPhrase...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('\"Kitty Katty\"')";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("beta", res));
        log.debug("...Stop testContainsPhrase.");
    }

    @Test
    public void testContainsNot() {
        log.debug("Start testContainsNot...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('-cat')";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("epsilon", res));
        log.debug("...Stop testContainsNot.");
    }

    @Test
    public void testContainsOr() {
        log.debug("Start testContainsOr...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('cat OR dog')";
        ObjectList res = doQuery(statement);
        assertEquals(4, res.getObjects().size());
        assertTrue(resultContains("alpha", res));
        assertTrue(resultContains("beta", res));
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testContainsOr.");
    }

    @Test
    public void testContainsAnd() {
        log.debug("Start testContainsAnd...");
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('cat dog')";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("delta", res));
        log.debug("...Stop testContainsAnd.");
    }

    @Test
    public void testContainsAndScore() {
        log.debug("Start testContainsAndScore...");
        String statement = "SELECT cmis:objectId,cmis:name,SCORE() FROM " + COMPLEX_TYPE + " WHERE CONTAINS('dog')";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContains("gamma", res));
        assertTrue(resultContains("delta", res));
        assertTrue(resultContains(1.0, "SEARCH_SCORE", res));
        log.debug("...Stop testContainsAndScore.");
    }

    @Test
    public void testContainsSyntaxError() {
        log.debug("Start testContainsSyntaxError...");
        String statement = "SELECT cmis:objectId FROM " + COMPLEX_TYPE + " WHERE CONTAINS('')";
        try {
        	doQuery(statement);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("line 1:0 no viable alternative at input '<EOF>'"));
            assertTrue(e instanceof CmisInvalidArgumentException);                   	
            assertTrue(e.getCause() instanceof RuntimeException);                   	
            assertTrue(e.getCause().getCause() instanceof RecognitionException);                   	
        }
        log.debug("...Stop testContainsSyntaxError.");
    }
    @Test
    public void testNotSetProperties() {
        log.debug("Start testNotSetProperties...");
        // PROP_ID_ID is not set property
        String statement = "SELECT cmis:name, " + PROP_ID_ID + " FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT
                + "= 100";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("epsilon", res));
        assertTrue(resultContainsNotSetPropertyValue(PROP_ID_ID, res.getObjects().get(0)));

        statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + PROP_ID_INT + "= 100";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("epsilon", res));
        assertTrue(resultContainsNotSetPropertyValue(PROP_ID_ID, res.getObjects().get(0)));
        log.debug("...Stop testNotSetProperties.");
    }

    @Test
    public void testSecondaryTypes() {
        log.debug("Start testSecondaryTypes...");
        // create documents with secondary types in addition
        dataCreator.createSecondaryTestDocuments();

        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE " + UnitTestTypeSystemCreator.PROP_ID_STRING
                + "= 'Secondary'";
        ObjectList res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContains("docwithsecondary", res));
        assertFalse(resultContains(UnitTestTypeSystemCreator.SECONDARY_STRING_PROP, res));

        statement = "SELECT * FROM " + SECONDARY_TYPE + " WHERE " + UnitTestTypeSystemCreator.SECONDARY_INTEGER_PROP
                + "= 100";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertFalse(resultContains("docwithsecondary", res));
        assertTrue(resultContains("Secondary Property Value", UnitTestTypeSystemCreator.SECONDARY_STRING_PROP, res));
        assertTrue(resultContains(BigInteger.valueOf(100), UnitTestTypeSystemCreator.SECONDARY_INTEGER_PROP, res));
        assertFalse(resultContainsProperty(UnitTestTypeSystemCreator.PROP_ID_STRING, res));
        log.debug("...Stop testSecondaryTypes.");
    }

    @Test
    public void testSecondaryJoin() {
        log.debug("Start testSecondaryJoin...");
        // create documents with secondary types in addition
        dataCreator.createSecondaryTestDocuments();
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " JOIN " + SECONDARY_TYPE + " ON " + COMPLEX_TYPE
                + ".cmis:objectId = " + SECONDARY_TYPE + ".cmis:objectId WHERE cmis:name LIKE 'docwithsecondary%'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContainsProperty(UnitTestTypeSystemCreator.PROP_ID_STRING, res));
        assertTrue(resultContainsProperty(PropertyIds.NAME, res));
        assertTrue(resultContainsProperty(PropertyIds.OBJECT_ID, res));
        assertTrue(resultContainsProperty(PropertyIds.OBJECT_TYPE_ID, res));
        assertTrue(resultContainsProperty(UnitTestTypeSystemCreator.SECONDARY_STRING_PROP, res));
        assertTrue(resultContainsProperty(UnitTestTypeSystemCreator.SECONDARY_INTEGER_PROP, res));

        // Test a query with secondary types matching only one document not
        // having this secondary type
        statement = "SELECT * FROM " + COMPLEX_TYPE + " JOIN " + SECONDARY_TYPE + " ON " + COMPLEX_TYPE
                + ".cmis:objectId = " + SECONDARY_TYPE + ".cmis:objectId WHERE cmis:name = 'alpha'";
        res = doQuery(statement);
        assertEquals(1, res.getObjects().size());
        assertTrue(resultContainsProperty(UnitTestTypeSystemCreator.PROP_ID_STRING, res));
        assertTrue(resultContainsProperty(PropertyIds.NAME, res));
        assertTrue(resultContainsProperty(PropertyIds.OBJECT_ID, res));
        assertTrue(resultContainsProperty(PropertyIds.OBJECT_TYPE_ID, res));
        assertTrue(resultContainsProperty(UnitTestTypeSystemCreator.SECONDARY_STRING_PROP, res));
        assertTrue(resultContainsProperty(UnitTestTypeSystemCreator.SECONDARY_INTEGER_PROP, res));

        log.debug("...Stop testSecondaryJoin.");
    }
    
    @Test
    public void testAskForSecondaryPropertyOnSimpleQuery() {
        log.debug("Start testAskForSecondaryPropertyOnSimpleQuery...");
        setUp(true); // force relaxed parser mode
        dataCreator.createSecondaryTestDocuments();
        String statement = "SELECT cmis:name, cmis:objectId, " + UnitTestTypeSystemCreator.SECONDARY_INTEGER_PROP
        		+ " AS SecInt, " + UnitTestTypeSystemCreator.SECONDARY_STRING_PROP + " FROM " + COMPLEX_TYPE + 
        		" WHERE cmis:name LIKE 'docwithsecondary%'";
        ObjectList res = doQuery(statement);
        assertEquals(2, res.getObjects().size());
        assertTrue(resultContainsProperty(PropertyIds.NAME, res));
        assertTrue(resultContainsProperty(PropertyIds.OBJECT_ID, res));
        assertTrue(resultContainsProperty(UnitTestTypeSystemCreator.SECONDARY_STRING_PROP, res));
        assertTrue(resultContainsQueryName("SecInt", res));
        log.debug("...Stop testAskForSecondaryPropertyOnSimpleQuery.");
    }
    
    @Test
    public void testMultipleContains() {
        log.debug("Start testMultipleContains...");
        dataCreator.createSecondaryTestDocuments();
        String statement = "SELECT * FROM " + COMPLEX_TYPE + " WHERE CONTAINS('abc') AND CONTAINS('123')";
        try {
            doQuery(statement);
            fail("Multiple CONTAINS clauses should throw CmisInvalidArgumentException");
        } catch (CmisInvalidArgumentException e) {
            assertTrue(e.getMessage().contains("More than one CONTAINS"));
        }
        log.debug("...Stop testMultipleContains.");
    }

    @Test
    public void testPredfinedQueryName() {
        log.debug("Start testPredfinedQueryName...");
        String statement = "SELECT cmis:name as abc, SCORE() FROM " + COMPLEX_TYPE + " ORDER BY SEARCH_SCORE";
        try {
            doQuery(statement);
        } catch (Exception e) {
            fail("SEARCH_SCORE in ORDER_BY must be supported.");
        }
        log.debug("...Stop testPredfinedQueryName.");
    }

    private ObjectList doQuery(String queryString) {
        log.debug("\nExecuting query: " + queryString);
        ObjectList res = fDiscSvc.query(fRepositoryId, queryString, false, false, IncludeRelationships.NONE, null,
                null, null, null);
        log.debug("Query result, number of matching objects: " + res.getNumItems());
        for (ObjectData od : res.getObjects()) {
            PropertyData<?> propData = od.getProperties().getProperties().get(PropertyIds.NAME);
            if (null != propData) {
                log.debug("Found matching object: " + propData.getFirstValue());
            } else {
                log.debug("Found matching object: (unknown, no name)");
            }
        }
        return res;
    }

    private ObjectList doQueryAllVersions(String queryString) {
        log.debug("\nExecuting query: " + queryString);
        ObjectList res = fDiscSvc.query(fRepositoryId, queryString, true, false, IncludeRelationships.NONE, null, null,
                null, null);
        log.debug("Query result, number of matching objects: " + res.getNumItems());
        for (ObjectData od : res.getObjects()) {
            log.debug("Found matching object: "
                    + od.getProperties().getProperties().get(PropertyIds.NAME).getFirstValue());
        }
        return res;
    }

    private static boolean resultContains(Object value, String propId, ObjectList results) {
        for (ObjectData od : results.getObjects()) {
            PropertyData<?> propData = od.getProperties().getProperties().get(propId);
            if (null != propData) {
                Object propVal = propData.getFirstValue();
                if (value.equals(propVal)) {
                    return true;
                }
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
        String nameProp = (String) results.getObjects().get(index).getProperties().getProperties()
                .get(PropertyIds.NAME).getFirstValue();
        return name.equals(nameProp);
    }

    private boolean resultContainsNotSetPropertyValue(String propId, ObjectData od) {
        PropertyData<?> propData = od.getProperties().getProperties().get(propId);
        return propData != null && propId.equals(propData.getId()) && propData.getValues().isEmpty();
    }

    private static boolean resultContainsProperty(String propId, ObjectList results) {
        for (ObjectData od : results.getObjects()) {
            PropertyData<?> propData = od.getProperties().getProperties().get(propId);
            if (null == propData) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean resultContainsQueryName(String queryName, ObjectList results) {
        for (ObjectData od : results.getObjects()) {
        	for (PropertyData<?> propData : od.getProperties().getProperties().values()) {
        		if (queryName.equals(propData.getQueryName())) {
        			return true;
        		}
        	}
        }
        return false;
    }

}
