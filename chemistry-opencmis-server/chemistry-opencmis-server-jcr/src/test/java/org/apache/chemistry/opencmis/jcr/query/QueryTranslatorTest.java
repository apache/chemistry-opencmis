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

import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
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
import org.junit.Before;
import org.junit.Test;

import java.util.GregorianCalendar;

import static org.junit.Assert.*;

public class QueryTranslatorTest {
    private String jcrTypeCondition;

    private QueryTranslator queryTranslator;

    @Before
    public void setUp() throws Exception {
        JcrTypeManager typeManager = new JcrTypeManager();
        PathManager pathManager = new PathManager(PathManager.CMIS_ROOT_PATH);
        JcrTypeHandlerManager typeHandlerManager = new JcrTypeHandlerManager(pathManager, typeManager);
        typeHandlerManager.addHandler(new DefaultFolderTypeHandler());
        typeHandlerManager.addHandler(new DefaultDocumentTypeHandler());
        typeHandlerManager.addHandler(new DefaultUnversionedDocumentTypeHandler());

        queryTranslator = new QueryTranslator(typeManager) {

            @Override
            protected String jcrPathFromId(String id) {
                assertNotNull(id);
                return "/jcr:" + id;
            }

            @Override
            protected String jcrPathFromCol(TypeDefinition fromType, String name) {
                assertNotNull(fromType);
                assertNotNull(name);
                return name.replace("cmis:", "@jcr:");
            }

            @Override
            protected String jcrTypeName(TypeDefinition fromType) {
                assertNotNull(fromType);
                return fromType.getQueryName().replace("cmis:", "jcr:");
            }

            @Override
            protected String jcrTypeCondition(TypeDefinition fromType) {
                assertNotNull(fromType);
                return jcrTypeCondition;
            }
        };
    }

    @Test
    public void testQueryTranslator() {
        assertEquals(
                "/jcr:root//element(*,jcr:document)",
                queryTranslator.translateToXPath("select * from cmis:document"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[@jcr:isLatestVersion = 'foo']",
                queryTranslator.translateToXPath("select * from cmis:document where cmis:isLatestVersion='foo'"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[jcr:like(@jcr:isLatestVersion, 'foo')]",
                queryTranslator.translateToXPath("select * from cmis:document where cmis:isLatestVersion LIKE 'foo'"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[not(jcr:like(@jcr:isLatestVersion, 'foo'))]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where cmis:isLatestVersion NOT LIKE 'foo'"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[@jcr:isLatestVersion = 'foo' and @jcr:name != 'baz']",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where cmis:isLatestVersion='foo' AND cmis:name<>'baz'"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[not((@jcr:isLatestVersion > 'foo' or @jcr:name < 1.0))]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where NOT (cmis:isLatestVersion>'foo' OR cmis:name< 1.0)"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[(@jcr:name = 'foo' or @jcr:objectId = 'baz' and @jcr:createdBy = 'bar')]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where cmis:name = 'foo' or cmis:objectId = 'baz' " +
                                "and cmis:createdBy = 'bar'"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[(@jcr:name = 'foo' and @jcr:objectId = 'baz' or @jcr:createdBy = 'bar')]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where cmis:name = 'foo' and cmis:objectId = 'baz' " +
                                "or cmis:createdBy = 'bar'"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[@jcr:name = 'foo' and (@jcr:objectId = 'baz' or @jcr:createdBy = 'bar')]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where cmis:name = 'foo' and (cmis:objectId = 'baz' " +
                                "or cmis:createdBy = 'bar')"));

        assertEquals(
                "/jcr:root/jcr:folderId/element(*,jcr:document)",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where IN_FOLDER('folderId')"));

        assertEquals(
                "/jcr:root/jcr:folderId/element(*,jcr:document)",
                queryTranslator.translateToXPath("select * from cmis:document where not(not(IN_FOLDER('folderId')))"));

        assertEquals(
                "/jcr:root/jcr:folderId//element(*,jcr:document)",
                queryTranslator.translateToXPath("select * from cmis:document where IN_TREE('folderId')"));

        assertEquals(
                "/jcr:root/jcr:folderId//element(*,jcr:document)",
                queryTranslator.translateToXPath("select * from cmis:document where not(not(IN_TREE('folderId')))"));

        assertEquals(
                "/jcr:root/jcr:folderId/element(*,jcr:document)[@jcr:name <= 1]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where IN_FOLDER('folderId') AND cmis:name <= 1"));

        assertEquals(
                "/jcr:root/jcr:folderId//element(*,jcr:document)[@jcr:name >= 'name' and @jcr:name = true]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where IN_TREE('folderId') AND cmis:name >= 'name' " +
                                "AND cmis:name = TRUE"));

        GregorianCalendar date = new GregorianCalendar();
        assertEquals(
                "/jcr:root/jcr:folderId/element(*,jcr:document)[not(@jcr:creationDate = xs:dateTime('" +
                        ISO8601.format(date) + "'))]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where NOT(NOT IN_FOLDER('folderId') OR cmis:creationDate = TIMESTAMP '" +
                                CalendarHelper.toString(date) + "')"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[jcr:contains(jcr:content, '\u4E2D\u6587')]",
                queryTranslator.translateToXPath("select * from cmis:document where contains('\u4E2D\u6587')"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[jcr:contains(jcr:content, 'foo bar')]",
                queryTranslator.translateToXPath("select * from cmis:document where contains('foo AND bar')"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[jcr:contains(jcr:content, 'foo OR bar')]",
                queryTranslator.translateToXPath("select * from cmis:document where contains('foo OR bar')"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[jcr:contains(jcr:content, 'foo -bar')]",
                queryTranslator.translateToXPath("select * from cmis:document where contains('foo -bar')"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[jcr:contains(jcr:content, 'foo \"bar phrase\"')]",
                queryTranslator.translateToXPath("select * from cmis:document where contains('foo \"bar phrase\"')"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[jcr:contains(jcr:content, 'foo -\"bar phrase\"')]",
                queryTranslator.translateToXPath("select * from cmis:document where contains('foo -\"bar phrase\"')"));

    }

    @Test
    public void testQueryWithOrderBy() {
        assertEquals(
                "/jcr:root//element(*,jcr:document)order by @jcr:name ascending",
                queryTranslator.translateToXPath("select * from cmis:document order by cmis:name"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[@jcr:isLatestVersion = 'foo']order by @jcr:name descending",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where cmis:isLatestVersion='foo' order by cmis:name desc"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[jcr:like(@jcr:isLatestVersion, 'foo')]order by @jcr:name ascending," +
                        "@jcr:objectId descending",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where cmis:isLatestVersion LIKE 'foo' order by cmis:name asc, cmis:objectId desc"));
    }

    @Test
    public void testQueryTranslatorWithTypeCondition() {
        jcrTypeCondition = "@jcr:primaryType = nt:base";

        assertEquals(
                "/jcr:root//element(*,jcr:document)[@jcr:primaryType = nt:base]",
                queryTranslator.translateToXPath("select * from cmis:document"));

        assertEquals(
                "/jcr:root//element(*,jcr:document)[@jcr:primaryType = nt:base and @jcr:isLatestVersion = 'foo']",
                queryTranslator.translateToXPath("select * from cmis:document where cmis:isLatestVersion='foo'"));

        assertEquals(
                "/jcr:root/jcr:folderId/element(*,jcr:document)[@jcr:primaryType = nt:base]",
                queryTranslator.translateToXPath("select * from cmis:document where IN_FOLDER('folderId')"));

        assertEquals(
                "/jcr:root/jcr:folderId/element(*,jcr:document)[@jcr:primaryType = nt:base]",
                queryTranslator.translateToXPath("select * from cmis:document where not(not(IN_FOLDER('folderId')))"));

        assertEquals(
                "/jcr:root/jcr:folderId//element(*,jcr:document)[@jcr:primaryType = nt:base]",
                queryTranslator.translateToXPath("select * from cmis:document where IN_TREE('folderId')"));

        assertEquals(
                "/jcr:root/jcr:folderId//element(*,jcr:document)[@jcr:primaryType = nt:base]",
                queryTranslator.translateToXPath("select * from cmis:document where not(not(IN_TREE('folderId')))"));

        assertEquals(
                "/jcr:root/jcr:folderId/element(*,jcr:document)[@jcr:primaryType = nt:base and @jcr:name <= 1]",
                queryTranslator.translateToXPath(
                        "select * from cmis:document where IN_FOLDER('folderId') AND cmis:name <= 1"));
    }

    @Test
    public void testQueryTranslatorQueryTooSpecific() {
        try {
            queryTranslator.translateToXPath(
                    "select * from cmis:document where NOT IN_FOLDER('folderId')");
            fail();
        }
        catch (CmisInvalidArgumentException expected) { }

        try {
            queryTranslator.translateToXPath(
                    "select * from cmis:document where NOT(NOT IN_FOLDER('folderId') AND cmis:name = 'name')");
            fail();
        }
        catch (CmisInvalidArgumentException expected) { }

        try {
            queryTranslator.translateToXPath(
                    "select * from cmis:document where IN_FOLDER('folderId') OR cmis:name = 'name'");
            fail();
        }
        catch (CmisInvalidArgumentException expected) { }

        try {
            queryTranslator.translateToXPath(
                    "select * from cmis:document where NOT(IN_FOLDER('folderId') AND cmis:name = 'name')");
            fail();
        }
        catch (CmisInvalidArgumentException expected) { }

        try {
            queryTranslator.translateToXPath(
                    "select * from cmis:document where IN_FOLDER('folder1Id') OR IN_TREE('folder2Id')");
            fail();
        }
        catch (CmisInvalidArgumentException expected) { }

        try {
            queryTranslator.translateToXPath(
                    "select * from cmis:document where IN_FOLDER('folder1Id') AND NOT IN_TREE('folder2Id')");
            fail();
        }
        catch (CmisInvalidArgumentException expected) { }
    }

    @Test
    public void testNotImplemented() {
        try {
            queryTranslator.translateToXPath("select * from cmis:document where cmis:name in (1,2,3)");
            fail();
        }
        catch (CmisNotSupportedException expected) {}

        try {
            queryTranslator.translateToXPath("select * from cmis:document where 'foo' = ANY cmis:name");
            fail();
        }
        catch (CmisNotSupportedException expected) {}
    }

    @Test
    public void testInvalidQuery() {
        try {
            queryTranslator.translateToXPath("");
            fail();
        } catch (CmisInvalidArgumentException expected) {
        }

        try {
            queryTranslator.translateToXPath("select * from cmis:something");
            fail();
        } catch (CmisInvalidArgumentException expected) {
        }

        try {
            queryTranslator.translateToXPath("select * from cmis:document WHERE");
            fail();
        } catch (CmisInvalidArgumentException expected) {
        }

        try {
            queryTranslator.translateToXPath("select * from cmis:document WHERE cmis:something = 'foo'");
            fail();
        } catch (CmisInvalidArgumentException expected) {
        }
    }

}
