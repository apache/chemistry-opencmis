/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.chemistry.opencmis.jcr.query;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.jcr.JcrTypeManager;
import org.apache.chemistry.opencmis.jcr.PathManager;
import org.apache.chemistry.opencmis.jcr.impl.DefaultDocumentTypeHandler;
import org.apache.chemistry.opencmis.jcr.impl.DefaultFolderTypeHandler;
import org.apache.chemistry.opencmis.jcr.impl.DefaultUnversionedDocumentTypeHandler;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;
import org.apache.chemistry.opencmis.jcr.util.ISO8601;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.chemistry.opencmis.server.support.query.QueryUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class XPathBuilderTest {

    private JcrTypeManager typeManager;

    @Before
    public void setUp() throws Exception {
        typeManager = new JcrTypeManager();
        PathManager pathManager = new PathManager(PathManager.CMIS_ROOT_PATH);
        JcrTypeHandlerManager typeHandlerManager = new JcrTypeHandlerManager(pathManager, typeManager);
        typeHandlerManager.addHandler(new DefaultFolderTypeHandler());
        typeHandlerManager.addHandler(new DefaultDocumentTypeHandler());
        typeHandlerManager.addHandler(new DefaultUnversionedDocumentTypeHandler());
    }

    @Test
    public void testValidQuery() {
        check("select * from cmis:document",
                null,
                list(),
                null);

        check("select * from cmis:document where cmis:isLatestVersion='foo'",
                "cmis:isLatestVersion = 'foo'",
                list(),
                null);

        check("select * from cmis:document where cmis:isLatestVersion LIKE 'foo'",
                "jcr:like(cmis:isLatestVersion, 'foo')",
                list(),
                null);

        check("select * from cmis:document where cmis:isLatestVersion NOT LIKE 'foo'",
                "not(jcr:like(cmis:isLatestVersion, 'foo'))",
                list(),
                null);

        check("select * from cmis:document where cmis:isLatestVersion='foo' AND cmis:name<>'baz'",
                "cmis:isLatestVersion = 'foo' and cmis:name != 'baz'",
                list(),
                null);

        check("select * from cmis:document where NOT (cmis:isLatestVersion>'foo' OR cmis:name< 1.0)",
                "not((cmis:isLatestVersion > 'foo' or cmis:name < 1.0))",
                list(),
                null);

        check("select * from cmis:document where cmis:name = 'foo' or cmis:objectId = 'baz' and cmis:createdBy = 'bar'",
                "(cmis:name = 'foo' or cmis:objectId = 'baz' and cmis:createdBy = 'bar')",
                list(),
                null);

        check("select * from cmis:document where cmis:name = 'foo' and cmis:objectId = 'baz' or cmis:createdBy = 'bar'",
                "(cmis:name = 'foo' and cmis:objectId = 'baz' or cmis:createdBy = 'bar')",
                list(),
                null);

        check("select * from cmis:document where cmis:name = 'foo' and (cmis:objectId = 'baz' or cmis:createdBy = 'bar')",
                "cmis:name = 'foo' and (cmis:objectId = 'baz' or cmis:createdBy = 'bar')",
                list(),
                null);

        check("select * from cmis:document where IN_FOLDER('folderId')",
                "folderId/",
                list("folderId/"),
                false);

        check("select * from cmis:document where not(not(IN_FOLDER('folderId')))",
                "true()",
                list("folderId/"),
                false);

        check("select * from cmis:document where IN_TREE('folderId')",
                "folderId//",
                list("folderId//"),
                false);

        check("select * from cmis:document where not(not(IN_TREE('folderId')))",
                "true()",
                list("folderId//"),
                false);

        check("select * from cmis:document where IN_FOLDER('folderId') AND cmis:name <= 1",
                "cmis:name <= 1",
                list("folderId/"),
                false);

        check("select * from cmis:document where IN_TREE('folderId') AND cmis:name >= 'name' AND cmis:name = TRUE",
                "cmis:name >= 'name' and cmis:name = true",
                list("folderId//"),
                false);

        GregorianCalendar date = new GregorianCalendar();
        check("select * from cmis:document where NOT(NOT IN_FOLDER('folderId') OR cmis:name = TIMESTAMP '" +
                CalendarHelper.toString(date) + "')",
                "not(cmis:name = xs:dateTime('" + ISO8601.format(date) + "'))",
                list("folderId/"),
                false);

        check("select * from cmis:document where cmis:name IS NULL",
                "cmis:name",
                list(),
                null);

        check("select * from cmis:document where cmis:name IS NOT NULL",
                "not(cmis:name)",
                list(),
                null);

        check("select * from cmis:document where CONTAINS('foo')",
                "jcr:contains(jcr:content, 'foo')",
                list(),
                null);

        check("select * from cmis:document where CONTAINS('foo AND bar')",
                "jcr:contains(jcr:content, 'foo bar')",
                list(),
                null);

        check("select * from cmis:document where CONTAINS('foo OR bar')",
                "jcr:contains(jcr:content, 'foo OR bar')",
                list(),
                null);

        check("select * from cmis:document where CONTAINS('foo -bar')",
                "jcr:contains(jcr:content, 'foo -bar')",
                list(),
                null);

        check("select * from cmis:document where CONTAINS('foo AND \"bar phrase\"')",
                "jcr:contains(jcr:content, 'foo \"bar phrase\"')",
                list(),
                null);

        check("select * from cmis:document where CONTAINS('foo AND -\"bar phrase\"')",
                "jcr:contains(jcr:content, 'foo -\"bar phrase\"')",
                list(),
                null);
    }

    @Test
    public void testTooSpecificQuery() {
        check("select * from cmis:document where NOT IN_FOLDER('folderId')",
                "false()",
                list("folderId/"),
                true);

        check("select * from cmis:document where NOT(NOT IN_FOLDER('folderId') AND cmis:name = 'name')",
                "true()",
                list("folderId/"),
                null);

        check("select * from cmis:document where IN_FOLDER('folderId') OR cmis:name = 'name'",
                "true()",
                list("folderId/"),
                null);

        check("select * from cmis:document where NOT(IN_FOLDER('folderId') AND cmis:name = 'name')",
                "not(cmis:name = 'name')",
                list("folderId/"),
                true);

        check("select * from cmis:document where IN_FOLDER('folder1Id') OR IN_TREE('folder2Id')",
                "true()",
                list("folder1Id/", "folder2Id//"),
                false);

        check("select * from cmis:document where IN_FOLDER('folder1Id') AND NOT IN_TREE('folder2Id')",
                "false()",
                list("folder1Id/", "folder2Id//"),
                false);
    }

    @Test
    public void testNotImplemented() {
        try {
            execute("select * from cmis:document where cmis:name in (1,2,3)");
            fail();
        }
        catch (CmisNotSupportedException expected) {}

        try {
            execute("select * from cmis:document where 'foo' = ANY cmis:name");
            fail();
        }
        catch (CmisNotSupportedException expected) {}
    }

    @Test
    public void testInvalidQuery() {
        try {
            execute("");
            fail();
        }
        catch (CmisInvalidArgumentException expected) {}

        try {
            execute("select * from cmis:something");
            fail();
        }
        catch (CmisInvalidArgumentException expected) {}

        try {
            execute("select * from cmis:document WHERE");
            fail();
        }
        catch (CmisInvalidArgumentException expected) {}

        try {
            execute("select * from cmis:document WHERE cmis:something = 'foo'");
            fail();
        }
        catch (CmisInvalidArgumentException expected) {}
    }

    //------------------------------------------< private >---

    private static List<String> list(String... elements) {
        return Arrays.asList(elements);
    }

    private XPathBuilder execute(String statement) {
        QueryUtil queryUtil = new QueryUtil();
        QueryObject queryObject = new QueryObject(typeManager);
        ParseTreeWalker<XPathBuilder> parseTreeWalker = new ParseTreeWalker<XPathBuilder>(new EvaluatorXPath());
        queryUtil.traverseStatementAndCatchExc(statement, queryObject, parseTreeWalker);
        return parseTreeWalker.getResult();
    }

    private void check(String query, String result, List<String> folderPredicates, Boolean evaluation) {
        XPathBuilder queryBuilder = execute(query);
        if (result == null) {
            assertEquals(null, queryBuilder);
        } else {
            assertEquals(result, queryBuilder.xPath());

            Iterator<XPathBuilder> folderPredicatesBuilder = queryBuilder.folderPredicates().iterator();
            for (String folderPredicate : folderPredicates) {
                assertTrue(folderPredicatesBuilder.hasNext());
                assertEquals(folderPredicate, folderPredicatesBuilder.next().xPath());
            }
            assertFalse(folderPredicatesBuilder.hasNext());

            assertEquals(evaluation, queryBuilder.eval(false));
        }
    }

}
