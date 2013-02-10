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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ExampleQueryProcessorTest {
    
    private ExampleQueryProcessor queryProcessor;
    private String queryPrefix = "SELECT cmis:name, cmis:objectId FROM cmis:document WHERE ";
    private String expectedPrefix = "SELECT cmis:name, cmis:objectId FROM cmis:document WHERE ";
    
    @Before
    public void setUp() {
        queryProcessor = new ExampleQueryProcessor();
    }
    
    @Test 
    public void testEquals() {
        String queryString = queryPrefix + "cmis:name = 'MyDocument'";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "(cmis:name = 'MyDocument')";
        assertEquals(expected, response);
        
        queryString = queryPrefix + "SCORE() = 100";
        response = queryProcessor.parseQuery(queryString);
        expected = expectedPrefix + "(SCORE() = 100)";
        assertEquals(expected, response);
    }
    
    @Test
    public void testNotEquals() {
        String queryString = queryPrefix + "cmis:name <> 'MyDocument'";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:name <> 'MyDocument'";
        assertEquals(expected, response);        
    }
    
    @Test
    public void testLessThan() {
        String queryString = queryPrefix + "cmis:contentStreamLength < 1048576";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:contentStreamLength < 1048576";
        assertEquals(expected, response);        
    }

    @Test
    public void testGreaterThan() {
        String queryString = queryPrefix + "cmis:contentStreamLength > 1048576";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:contentStreamLength > 1048576";
        assertEquals(expected, response);        
    }

    @Test
    public void testLessOrEquals() {
        String queryString = queryPrefix + "cmis:contentStreamLength <= 1048576";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:contentStreamLength <= 1048576";
        assertEquals(expected, response);        
    }

    @Test
    public void testGreaterOrEquals() {
        String queryString = queryPrefix + "cmis:contentStreamLength >= 1048576";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:contentStreamLength >= 1048576";
        assertEquals(expected, response);        
    }

    @Test
    public void testIn() {
        String queryString = queryPrefix + "cmis:name IN ('MyDocument', 'YourDocument')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:name IN ('MyDocument', 'YourDocument')";
        assertEquals(expected, response);        
    }
    
    @Test
    public void testNotIn() {
        String queryString = queryPrefix + "cmis:name NOT IN ('MyDocument', 'YourDocument')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:name NOT IN ('MyDocument', 'YourDocument')";
        assertEquals(expected, response);        
    }

    @Test
    public void testAnyIn() {
        String queryString = queryPrefix + "ANY cmis:secondaryObjectTypeIds IN ('MySecondaryType', 'YourSecondaryType')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "ANY cmis:secondaryObjectTypeIds IN ('MySecondaryType', 'YourSecondaryType')";
        assertEquals(expected, response);        
    }

    @Test
    public void testAnyNotIn() {
        String queryString = queryPrefix + "ANY cmis:secondaryObjectTypeIds NOT IN ('MySecondaryType', 'YourSecondaryType')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "ANY cmis:secondaryObjectTypeIds NOT IN ('MySecondaryType', 'YourSecondaryType')";
        assertEquals(expected, response);        
    }

    @Test
    public void testEqAny() {
        String queryString = queryPrefix + "'MySecondaryType' = ANY cmis:secondaryObjectTypeIds";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "'MySecondaryType' = ANY cmis:secondaryObjectTypeIds";
        assertEquals(expected, response);        
    }

    @Test
    public void testIsNull() {
        String queryString = queryPrefix + "cmis:description IS NULL";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:description IS NULL";
        assertEquals(expected, response);        
    }

    @Test
    public void testIsNotNull() {
        String queryString = queryPrefix + "cmis:description IS NOT NULL";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:description IS NOT NULL";
        assertEquals(expected, response);        
    }
    
    @Test 
    public void testLike() {
        String queryString = "SELECT cmis:name AS name, cmis:objectTypeId, SCORE() FROM cmis:document WHERE cmis:name LIKE 'My%'";
        String response = queryProcessor.parseQuery(queryString);
        String expected = "SELECT cmis:name AS name, cmis:objectTypeId, SCORE() FROM cmis:document WHERE (cmis:name LIKE 'My%')";
        assertEquals(expected, response);
    }

    @Test
    public void testNotLike() {
        String queryString = queryPrefix + "cmis:name NOT LIKE 'My%'";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "cmis:name NOT LIKE 'My%'";
        assertEquals(expected, response);        
    }

    @Test
    public void testInFolder() {
        String queryString = queryPrefix + "IN_FOLDER('100')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "IN_FOLDER('100')";
        assertEquals(expected, response);        

        queryString = queryPrefix + "IN_FOLDER(cmis:document, '100')";
        response = queryProcessor.parseQuery(queryString);
        expected = expectedPrefix + "IN_FOLDER(cmis:document, '100')";
        assertEquals(expected, response);        
    }

    @Test
    public void testInTree() {
        String queryString = queryPrefix + "IN_TREE('100')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "IN_TREE('100')";
        assertEquals(expected, response);        

        queryString = queryPrefix + "IN_TREE(cmis:document, '100')";
        response = queryProcessor.parseQuery(queryString);
        expected = expectedPrefix + "IN_TREE(cmis:document, '100')";
        assertEquals(expected, response);        
    }

    @Test
    public void testAnd() {
        String queryString = queryPrefix + "cmis:name NOT LIKE 'My*' AND cmis:description IS NULL";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "(cmis:name NOT LIKE 'My*' AND cmis:description IS NULL)";
        assertEquals(expected, response);        
    }

    @Test
    public void testOr() {
        String queryString = queryPrefix + "cmis:name NOT LIKE 'My*' OR cmis:description IS NULL";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "(cmis:name NOT LIKE 'My*' OR cmis:description IS NULL)";
        assertEquals(expected, response);        
    }

    @Test
    public void testNot() {
        String queryString = queryPrefix + "NOT IN_FOLDER('100')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "NOT (IN_FOLDER('100'))";
        assertEquals(expected, response);        
    }

    @Test
    public void testContains() {
        String queryString = queryPrefix + "CONTAINS('Foo')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "CONTAINS('Foo')";
        assertEquals(expected, response);
        
        queryString = queryPrefix + "CONTAINS(cmis:document, 'Foo')";
        response = queryProcessor.parseQuery(queryString);
        expected = expectedPrefix + "CONTAINS(cmis:document, 'Foo')";
        assertEquals(expected, response);
    }

    @Test
    public void testTextAnd() {
        String queryString = queryPrefix + "CONTAINS('abc def')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "CONTAINS('abc def')";
        assertEquals(expected, response);        
    }

    @Test
    public void testTextOr() {
        String queryString = queryPrefix + "CONTAINS('abc OR def')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "CONTAINS('abc OR def')";
        assertEquals(expected, response);        
    }

    @Test
    public void testTextMinus() {
        String queryString = queryPrefix + "CONTAINS('abc -def')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "CONTAINS('abc -def')";
        assertEquals(expected, response);        
    }

    @Test
    public void testPhrase() {
        String queryString = queryPrefix + "CONTAINS('\\'abc\\'')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "CONTAINS('\\'abc\\'')";
        assertEquals(expected, response);        
    }

    @Test
    public void testWord() {
        String queryString = queryPrefix + "CONTAINS('Foo')";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "CONTAINS('Foo')";
        assertEquals(expected, response);        
    }

    @Test 
    public void testOrderBy() {
        String queryString = queryPrefix + "cmis:name = 'MyDocument' ORDER BY cmis:objectId";
        String response = queryProcessor.parseQuery(queryString);
        String expected = expectedPrefix + "(cmis:name = 'MyDocument') ORDER BY cmis:objectId";
        assertEquals(expected, response);
        
        queryString = queryPrefix + "cmis:name = 'MyDocument' ORDER BY cmis:name ASC, cmis:objectId DESC";
        response = queryProcessor.parseQuery(queryString);
        expected = expectedPrefix + "(cmis:name = 'MyDocument') ORDER BY cmis:name, cmis:objectId DESC";
        assertEquals(expected, response);
    }
}
