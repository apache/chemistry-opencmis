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
package org.apache.chemistry.opencmis.client.runtime;

import static org.junit.Assert.assertEquals;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.junit.Test;

public class QueryStatementTest {

    @Test
    public void testStaticQueries() {
        Session session = new SessionImpl(new HashMap<String, String>(), null, null, null);
        String query;
        QueryStatement st;

        query = "SELECT cmis:name FROM cmis:folder";
        st = new QueryStatementImpl(session, query);
        assertEquals(query, st.toQueryString());

        query = "SELECT * FROM cmis:document WHERE cmis:createdBy = \'admin\' AND abc:int = 42";
        st = new QueryStatementImpl(session, query);
        assertEquals(query, st.toQueryString());

        query = "SELECT * FROM cmis:document WHERE abc:test = 'x?z'";
        st = new QueryStatementImpl(session, query);
        st.setString(1, "y");
        assertEquals(query, st.toQueryString());
    }

    @Test
    public void testWherePlacholder() {
        Session session = new SessionImpl(new HashMap<String, String>(), null, null, null);
        String query;
        QueryStatement st;

        // strings
        query = "SELECT * FROM cmis:document WHERE abc:string = ?";
        st = new QueryStatementImpl(session, query);
        st.setString(1, "test");
        assertEquals("SELECT * FROM cmis:document WHERE abc:string = 'test'", st.toQueryString());

        query = "SELECT * FROM cmis:document WHERE abc:string = ?";
        st = new QueryStatementImpl(session, query);
        st.setString(1, "te'st");
        assertEquals("SELECT * FROM cmis:document WHERE abc:string = 'te\\'st'", st.toQueryString());

        // likes
        query = "SELECT * FROM cmis:document WHERE abc:string LIKE ?";
        st = new QueryStatementImpl(session, query);
        st.setStringLike(1, "%test%");
        assertEquals("SELECT * FROM cmis:document WHERE abc:string LIKE '%test%'", st.toQueryString());

        query = "SELECT * FROM cmis:document WHERE abc:string LIKE ?";
        st = new QueryStatementImpl(session, query);
        st.setStringLike(1, "\\_test\\%blah\\\\blah");
        assertEquals("SELECT * FROM cmis:document WHERE abc:string LIKE '\\_test\\%blah\\\\\\\\blah'",
                st.toQueryString());

        // contains

        // *, ? and - are treated as text search operators: 1st level escaping: none, 2nd level escaping: none
        // \*, \? and \- are used as literals, 1st level escaping: none, 2nd level escaping: \\*, \\?, \\-
        // ' and " are used as literals, 1st level escaping: \', \", 2nd level escaping:  \\\', \\\",
        // \ plus any other character, 1st level escaping \\ plus character, 2nd level: \\\\ plus character

        query = "SELECT * FROM cmis:document WHERE CONTAINS(?)";
        st = new QueryStatementImpl(session, query);
        st.setStringContains(1, "John's");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('John\\\\\\'s')",
                st.toQueryString());
        st.setStringContains(1, "foo -bar");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('foo -bar')",
                st.toQueryString());
        st.setStringContains(1, "foo*");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('foo*')",
                st.toQueryString());
        st.setStringContains(1, "foo?");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('foo?')",
                st.toQueryString());
        st.setStringContains(1, "foo\\-bar");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('foo\\\\-bar')",
                st.toQueryString());
        st.setStringContains(1, "foo\\*");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('foo\\\\*')",
                st.toQueryString());
        st.setStringContains(1, "foo\\?");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('foo\\\\?')",
                st.toQueryString());
        st.setStringContains(1, "\"Cool\"");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('\\\\\\\"Cool\\\\\\\"')",
                st.toQueryString());
        st.setStringContains(1, "c:\\MyDcuments");
        assertEquals("SELECT * FROM cmis:document WHERE CONTAINS('c:\\\\MyDcuments')",
                st.toQueryString());
       
        // ids
        query = "SELECT * FROM cmis:document WHERE abc:id = ?";
        st = new QueryStatementImpl(session, query);
        st.setId(1, new ObjectIdImpl("123"));
        assertEquals("SELECT * FROM cmis:document WHERE abc:id = '123'", st.toQueryString());

        // booleans
        query = "SELECT * FROM cmis:document WHERE abc:bool = ?";
        st = new QueryStatementImpl(session, query);
        st.setBoolean(1, true);
        assertEquals("SELECT * FROM cmis:document WHERE abc:bool = TRUE", st.toQueryString());

        // numbers
        query = "SELECT * FROM cmis:document WHERE abc:int = ? AND abc:int2 = 123";
        st = new QueryStatementImpl(session, query);
        st.setNumber(1, 42);
        assertEquals("SELECT * FROM cmis:document WHERE abc:int = 42 AND abc:int2 = 123", st.toQueryString());

        // dateTime
        query = "SELECT * FROM cmis:document WHERE abc:dateTime = TIMESTAMP ?";
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        cal.clear();
        cal.set(2012, 1, 2, 3, 4, 5);

        st = new QueryStatementImpl(session, query);
        st.setDateTime(1, cal);
        assertEquals("SELECT * FROM cmis:document WHERE abc:dateTime = TIMESTAMP '2012-02-02T03:04:05.000Z'",
                st.toQueryString());

        st = new QueryStatementImpl(session, query);
        st.setDateTime(1, cal.getTimeInMillis());
        assertEquals("SELECT * FROM cmis:document WHERE abc:dateTime = TIMESTAMP '2012-02-02T03:04:05.000Z'",
                st.toQueryString());

        // dateTime Timestamp
        query = "SELECT * FROM cmis:document WHERE abc:dateTime = ?";

        st = new QueryStatementImpl(session, query);
        st.setDateTimeTimestamp(1, cal);
        assertEquals("SELECT * FROM cmis:document WHERE abc:dateTime = TIMESTAMP '2012-02-02T03:04:05.000Z'",
                st.toQueryString());

        st = new QueryStatementImpl(session, query);
        st.setDateTimeTimestamp(1, cal.getTimeInMillis());
        assertEquals("SELECT * FROM cmis:document WHERE abc:dateTime = TIMESTAMP '2012-02-02T03:04:05.000Z'",
                st.toQueryString());

        query = "SELECT * FROM cmis:document WHERE abc:dateTime IN (?)";

        st = new QueryStatementImpl(session, query);
        st.setDateTimeTimestamp(1, cal.getTime(), cal.getTime());
        assertEquals("SELECT * FROM cmis:document WHERE abc:dateTime "
                + "IN (TIMESTAMP '2012-02-02T03:04:05.000Z',TIMESTAMP '2012-02-02T03:04:05.000Z')", st.toQueryString());
    }
    
//    @Test
    public void testQueryApiEscaping() {
        // contains
        Session session = new SessionImpl(new HashMap<String, String>(), null, null, null);

        String query = "SELECT * FROM cmis:document WHERE CONTAINS(?)";
        String ss = "a\\xc";
        assertEquals(4, ss.length());
        System.out.println(ss);
        QueryStatement st = new QueryStatementImpl(session, query);
        st.setStringContains(1, "John's");
        System.out.println("setStringContains: "+ st.toQueryString());
        String expected = "SELECT * FROM cmis:document WHERE CONTAINS('John\\\'s')";
        System.out.println("Expected: " + expected);
        
        assertEquals(expected, st.toQueryString());

    }

}
