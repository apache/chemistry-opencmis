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
package org.apache.chemistry.opencmis.server.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.server.impl.browser.MultipartParser;
import org.apache.chemistry.opencmis.server.shared.TempStoreOutputStreamFactory;
import org.junit.Test;

/**
 * Tests the multipart parser.
 */
public class MultipartParserTest {

    private static final int THRESHOLD = 4 * 1024 * 1024;
    private static final int MAX_SIZE = -1;

    @Test
    public void testMultipartParser() throws Exception {
        String boundary = "---- next ----";
        byte[] content = "This is content!".getBytes();
        byte[] formdata = ("\r\n--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"field1\"\r\n"
                + "\r\n" + "value1\r\n" + "--" + boundary + "\r\n"
                + "content-disposition: form-data; name=\"field2\"\r\n" + "\r\n" + "value2\r\n" + "--" + boundary
                + "\r\n" + "content-disposition: form-data; name=\"field3\"\r\n" + "\r\n" + "value3\r\n" + "--"
                + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"content\"; filename=test-filename.txt\r\n"
                + "Content-Type: text/plain\r\n" + "Content-Transfer-Encoding: binary\r\n" + "\r\n"
                + new String(content) + "\r\n" + "--" + boundary + "--").getBytes("ISO-8859-1");

        MultipartParser parser = prepareParser(boundary, formdata);

        Map<String, String> values = new HashMap<String, String>();
        values.put("field1", "value1");
        values.put("field2", "value2");
        values.put("field3", "value3");

        assertMultipartBasics(parser, 4, values, true, "test-filename.txt", "text/plain", content);
    }

    @Test
    public void testMultipartParser2() throws Exception {
        String boundary = "-----------------------------1294919323195";
        byte[] content = "Test content!".getBytes("ISO-8859-1");
        byte[] formdata = ("\r\n--"
                + boundary
                + "\r\nContent-Disposition: form-data; name=\"fileUploader\"; filename=\"Ä.txt\"\r\nContent-Type: text/plain\r\n\r\n"
                + new String(content) + "\r\n--" + boundary
                + "\r\nContent-Disposition: form-data; name=\"fileUploader-data\"\r\n\r\n\r\n--" + boundary
                + "\r\nContent-Disposition: form-data; name=\"objectid\"\r\n\r\nf6bad54b4696bf2ac9249805\r\n--"
                + boundary + "\r\nContent-Disposition: form-data; name=\"cmisaction\"\r\n\r\ncreateDocument\r\n--"
                + boundary + "\r\nContent-Disposition: form-data; name=\"propertyId[0]\"\r\n\r\ncmis:name\r\n--"
                + boundary + "\r\nContent-Disposition: form-data; name=\"propertyValue[0]\"\r\n\r\nÄ.txt\r\n--"
                + boundary
                + "\r\nContent-Disposition: form-data; name=\"propertyId[1]\"\r\n\r\ncmis:objectTypeId\r\n--"
                + boundary + "\r\nContent-Disposition: form-data; name=\"propertyValue[1]\"\r\n\r\ncmis:document\r\n--"
                + boundary
                + "\r\nContent-Disposition: form-data; name=\"token\"\r\n\r\n855475d8a6169b5f57111f5921f56136\r\n--"
                + boundary + "--").getBytes("ISO-8859-1");

        MultipartParser parser = prepareParser(boundary, formdata);

        Map<String, String> values = new HashMap<String, String>();
        values.put("fileUploader-data", "");
        values.put("objectid", "f6bad54b4696bf2ac9249805");
        values.put("cmisaction", "createDocument");
        values.put("propertyId[0]", "cmis:name");
        values.put("propertyValue[0]", "Ä.txt");
        values.put("propertyId[1]", "cmis:objectTypeId");
        values.put("propertyValue[1]", "cmis:document");
        values.put("token", "855475d8a6169b5f57111f5921f56136");

        assertMultipartBasics(parser, 9, values, true, "Ä.txt", "text/plain", content);
    }

    @Test
    public void testNoPreamble() throws Exception {
        String boundary = "BoUnDaRy--987654320";
        byte[] formdata = ("--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"field1\"\r\n" + "\r\n"
                + "value1\r\n" + "--" + boundary + "--").getBytes();

        MultipartParser parser = prepareParser(boundary, formdata);

        Map<String, String> values = new HashMap<String, String>();
        values.put("field1", "value1");

        assertMultipartBasics(parser, 1, values, false, null, null, null);
    }

    @Test
    public void testPreamble() throws Exception {
        String boundary = "BoUnDaRy--987654320";
        byte[] formdata = ("This is a preamble.\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"field1\"\r\n" + "\r\n" + "value1\r\n" + "--" + boundary + "--")
                .getBytes();

        MultipartParser parser = prepareParser(boundary, formdata);

        Map<String, String> values = new HashMap<String, String>();
        values.put("field1", "value1");

        assertMultipartBasics(parser, 1, values, false, null, null, null);
    }

    @Test
    public void testEpilogue() throws Exception {
        String boundary = "BoUnDaRy--987654320";
        byte[] formdata = ("\r\n--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"field1\"\r\n"
                + "\r\n" + "value1\r\n" + "--" + boundary + "--This is an epilogue.").getBytes();

        MultipartParser parser = prepareParser(boundary, formdata);

        Map<String, String> values = new HashMap<String, String>();
        values.put("field1", "value1");

        assertMultipartBasics(parser, 1, values, false, null, null, null);
    }

    @Test
    public void testEmpty() throws Exception {
        String boundary = "BoUnDaRy--987654320";
        byte[] formdata = ("\r\n--" + boundary + "--").getBytes();

        MultipartParser parser = prepareParser(boundary, formdata);

        Map<String, String> values = new HashMap<String, String>();

        assertMultipartBasics(parser, 0, values, false, null, null, null);
    }

    @Test
    public void testContentOnly() throws Exception {
        String boundary = "ABCD-1234";
        byte[] content = "abcäöü".getBytes();
        byte[] formdata = ("\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"content\"; filename=\"a new file\"\r\n"
                + "Content-Type: application/something\r\n" + "Content-Transfer-Encoding: binary\r\n" + "\r\n"
                + new String(content) + "\r\n" + "--" + boundary + "--").getBytes();

        MultipartParser parser = prepareParser(boundary, formdata);

        Map<String, String> values = new HashMap<String, String>();

        assertMultipartBasics(parser, 1, values, true, "a new file", "application/something", content);
    }

    @Test
    public void testBigContent() throws Exception {
        String boundary = "---- next ----";
        byte[] content = new byte[2 * 1024 * 1024];

        Random rnd = new Random();
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) ('a' + rnd.nextInt('z' - 'a'));
        }

        byte[] formdata = ("\r\n--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"field1\"\r\n"
                + "\r\n" + "value1\r\n" + "--" + boundary + "\r\n"
                + "content-disposition: form-data; name=\"field2\"\r\n" + "\r\n" + "value2\r\n" + "--" + boundary
                + "\r\n" + "content-disposition: form-data; name=\"field3\"\r\n" + "\r\n" + "value3\r\n" + "--"
                + boundary + "\r\n" + "Content-Disposition: form-data; name=\"content\"; filename=bigtest.txt\r\n"
                + "Content-Type: text/plain\r\n" + "Content-Transfer-Encoding: binary\r\n" + "\r\n"
                + new String(content) + "\r\n" + "--" + boundary + "--").getBytes();

        MultipartParser parser = prepareParser(boundary, formdata);

        Map<String, String> values = new HashMap<String, String>();
        values.put("field1", "value1");
        values.put("field2", "value2");
        values.put("field3", "value3");

        assertMultipartBasics(parser, 4, values, true, "bigtest.txt", "text/plain", content);
    }

    @Test
    public void testManyFields() throws Exception {
        String boundary = "============";

        StringBuilder sb = new StringBuilder("\r\n");

        Map<String, String> values = new HashMap<String, String>();

        for (int i = 0; i < 10000; i++) {
            String name = "field" + i;
            String value = "value " + i * i;

            values.put(name, value);

            sb.append("\r\n--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + name + "\"\r\n"
                    + "\r\n" + value);
        }

        sb.append("\r\n" + "--" + boundary + "--");

        MultipartParser parser = prepareParser(boundary, sb.toString().getBytes(IOUtils.ISO_8859_1));

        assertMultipartBasics(parser, values.size(), values, false, null, null, null);
    }

    @Test
    public void testLargeFields() throws Exception {
        testLargeFields(64 * 1024);
        testLargeFields(128 * 1024);
        testLargeFields(256 * 1024);
        testLargeFields(512 * 1024);
        testLargeFields(1024 * 1024);
    }

    private void testLargeFields(int size) throws Exception {
        String boundary = "============";

        StringBuilder sb = new StringBuilder("\r\n");

        Random rnd = new Random();
        Map<String, String> values = new HashMap<String, String>();

        for (int i = 0; i < 5; i++) {
            String name = "field" + i;

            StringBuilder valueBuffer = new StringBuilder();
            for (int j = 0; j < size; j++) {
                valueBuffer.append((char) ('a' + rnd.nextInt(26)));
            }
            String value = valueBuffer.toString();

            values.put(name, value);

            sb.append("\r\n--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
            sb.append(value);
        }

        sb.append("\r\n" + "--" + boundary + "--");

        MultipartParser parser = prepareParser(boundary, sb.toString().getBytes(IOUtils.ISO_8859_1));

        assertMultipartBasics(parser, values.size(), values, false, null, null, null);
    }

    @Test
    public void testCharsetsInContentType() throws Exception {
        String[] charsets = new String[] { "utf-8", "iso-8859-1", "utf-16" };

        String boundary = "ldchqeriuvoqeirbvxipu  eckqnqklwjcnqwklcqwncqewlciqecqwecevoipooei cqwcoewcq";
        StringBuilder value = new StringBuilder();

        for (int i = 1; i < 255; i++) {
            value.append((char) i);
        }

        Map<String, String> values = new HashMap<String, String>();
        values.put("field1", value.toString());

        for (String charset : charsets) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(("\r\n--" + boundary + "\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Disposition: form-data; name=\"field1\"\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Type: text/plain; charset=" + charset + "\r\n\r\n").getBytes("ISO-8859-1"));
            bos.write(value.toString().getBytes(charset));
            bos.write(("\r\n--" + boundary + "--\r\n").getBytes("ISO-8859-1"));
            bos.write(("This is an epilogue.").getBytes("ISO-8859-1"));

            MultipartParser parser = prepareParser(boundary, bos.toByteArray());

            assertMultipartBasics(parser, 1, values, false, null, null, null);
        }
    }

    @Test
    public void testCharsetsAsExtraField() throws Exception {
        String[] charsets = new String[] { "utf-8", "iso-8859-1", "utf-16" };

        String boundary = "ldchqeriuvoqeirbvxipu  eckqnqklwjcnqwklcqwncqewlciqecqwecevoipooei cqwcoewcq";
        StringBuilder value = new StringBuilder();

        for (int i = 1; i < 255; i++) {
            value.append((char) i);
        }

        Map<String, String> values = new HashMap<String, String>();
        values.put("field1", value.toString());

        for (String charset : charsets) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(("\r\n--" + boundary + "\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Disposition: form-data; name=\"field1\"\r\n\r\n").getBytes("ISO-8859-1"));
            bos.write(value.toString().getBytes(charset));
            bos.write(("\r\n--" + boundary + "\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Disposition: form-data; name=\"_charset_\"\r\n\r\n").getBytes("ISO-8859-1"));
            bos.write(charset.getBytes("ISO-8859-1"));
            bos.write(("\r\n--" + boundary + "--\r\n").getBytes("ISO-8859-1"));
            bos.write(("This is an epilogue.").getBytes("ISO-8859-1"));

            MultipartParser parser = prepareParser(boundary, bos.toByteArray());

            assertMultipartBasics(parser, 1, values, false, null, null, null);
        }
    }

    @Test
    public void testCharsetsMixed() throws Exception {
        String[] charsets = new String[] { "utf-8", "iso-8859-1", "utf-16" };

        String boundary = "ldchqeriuvoqeirbvxipu  eckqnqklwjcnqwklcqwncqewlciqecqwecevoipooei cqwcoewcq";
        StringBuilder value = new StringBuilder();

        for (int i = 1; i < 255; i++) {
            value.append((char) i);
        }

        Map<String, String> values = new HashMap<String, String>();
        values.put("field1", value.toString());
        values.put("field2", value.toString());
        values.put("field3", value.toString());

        for (String charset : charsets) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bos.write(("\r\n--" + boundary + "\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Disposition: form-data; name=\"field1\"\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Type: text/plain; charset=\"utf-8\"\r\n\r\n").getBytes("ISO-8859-1"));
            bos.write(value.toString().getBytes("utf-8"));
            bos.write(("\r\n--" + boundary + "\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Disposition: form-data; name=\"field2\"\r\n\r\n").getBytes("ISO-8859-1"));
            bos.write(value.toString().getBytes(charset));
            bos.write(("\r\n--" + boundary + "\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Disposition: form-data; name=\"_charset_\"\r\n\r\n").getBytes("ISO-8859-1"));
            bos.write(charset.getBytes("ISO-8859-1"));
            bos.write(("\r\n--" + boundary + "\r\n").getBytes("ISO-8859-1"));
            bos.write(("Content-Disposition: form-data; name=\"field3\"\r\n\r\n").getBytes("ISO-8859-1"));
            bos.write(value.toString().getBytes(charset));
            bos.write(("\r\n--" + boundary + "--\r\n").getBytes("ISO-8859-1"));

            MultipartParser parser = prepareParser(boundary, bos.toByteArray());

            assertMultipartBasics(parser, 3, values, false, null, null, null);
        }
    }

    @Test(expected = CmisInvalidArgumentException.class)
    public void testNoBoundary() throws Exception {
        String boundary = "";
        byte[] formdata = ("\r\n--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"field1\"\r\n"
                + "\r\n" + "value1\r\n" + "--" + boundary + "--").getBytes();

        prepareParser(boundary, formdata);
    }

    @Test(expected = CmisInvalidArgumentException.class)
    public void testInvalidCharset() throws Exception {
        String boundary = "15983409582340582340";
        byte[] formdata = ("\r\n--" + boundary + "\r\n" + "Content-Disposition: form-data; name=\"field1\"\r\n"
                + "Content-Type: text/plain; charset=xyz\r\n" + "\r\n" + "value1\r\n" + "--" + boundary + "--")
                .getBytes();

        MultipartParser parser = prepareParser(boundary, formdata);

        assertMultipartBasics(parser, 1, null, false, null, null, null);
    }

    @Test(expected = CmisInvalidArgumentException.class)
    public void testTwoContentParts() throws Exception {
        String boundary = "-?-";
        byte[] content = "abc������".getBytes();
        byte[] formdata = ("\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"content1\"; filename=\"file1\"\r\n"
                + "Content-Type: application/something\r\n" + "Content-Transfer-Encoding: binary\r\n" + "\r\n"
                + new String(content) + "\r\n--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"content2\"; filename=\"file2\"\r\n"
                + "Content-Type: application/something\r\n" + "Content-Transfer-Encoding: binary\r\n" + "\r\n"
                + new String(content) + "\r\n" + "--" + boundary + "--").getBytes();

        MultipartParser parser = prepareParser(boundary, formdata);

        assertMultipartBasics(parser, 2, null, true, "file1", "application/something", content);
    }

    // ---- helpers ----

    private MultipartParser prepareParser(String boundary, byte[] content) throws Exception {
        HttpServletRequest request = HttpRequestMockHelper.createMultipartRequest(boundary, content);

        TempStoreOutputStreamFactory streamFactory = TempStoreOutputStreamFactory.newInstance(null, THRESHOLD,
                MAX_SIZE, false);
        return new MultipartParser(request, streamFactory);
    }

    private byte[] readBytesFromStream(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int b;
        while ((b = is.read(buffer)) > -1) {
            bos.write(buffer, 0, b);
        }

        is.close();

        return bos.toByteArray();
    }

    private void assertMultipartBasics(MultipartParser parser, int count, Map<String, String> values,
            boolean hasContent, String filename, String contentType, byte[] content) throws Exception {
        int counter = 0;

        parser.parse();

        if (parser.hasContent()) {
            counter++;
            assertTrue(hasContent);
            assertEquals(filename, parser.getFilename());
            assertEquals(contentType, parser.getContentType());
            assertEquals(content.length, parser.getSize().intValue());
            assertArrayEquals(content, readBytesFromStream(parser.getStream()));
        } else {
            assertFalse(hasContent);
        }

        Map<String, String[]> fields = parser.getFields();
        for (Map.Entry<String, String[]> e : fields.entrySet()) {
            assertNotNull(e.getValue());
            assertEquals(1, e.getValue().length);

            String fieldName = e.getKey();
            String fieldValue = e.getValue()[0];

            assertEquals(fieldValue, values.get(fieldName));

            counter++;
        }

        assertEquals(count, counter);
        assertEquals(counter - (hasContent ? 1 : 0), fields.size());
    }
}
