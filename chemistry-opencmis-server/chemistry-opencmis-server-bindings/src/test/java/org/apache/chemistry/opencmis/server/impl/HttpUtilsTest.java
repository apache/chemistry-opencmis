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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;
import org.junit.Test;
import org.mockito.Mockito;

public class HttpUtilsTest {

    @Test
    public void testSplitPath() throws Exception {
        String[] fragments;

        fragments = HttpUtils.splitPath(createRequest("f1/f2/f3/f4"));
        assertNotNull(fragments);
        assertEquals(4, fragments.length);
        assertEquals("f1", fragments[0]);
        assertEquals("f2", fragments[1]);
        assertEquals("f3", fragments[2]);
        assertEquals("f4", fragments[3]);

        fragments = HttpUtils.splitPath(createRequest("f1/" + URLEncoder.encode(" !§$%&/()?@", "UTF-8") + "/f3"));
        assertNotNull(fragments);
        assertEquals(3, fragments.length);
        assertEquals("f1", fragments[0]);
        assertEquals(" !§$%&/()?@", fragments[1]);
        assertEquals("f3", fragments[2]);

        try {
            HttpUtils.splitPath(createRequest("f1/" + URLEncoder.encode("xxx\nxxx", "UTF-8") + "/f3"));
            fail("CmisInvalidArgumentException expected!");
        } catch (CmisInvalidArgumentException iae) {
            // expected
        }

        try {
            HttpUtils.splitPath(createRequest("f1/" + URLEncoder.encode("xxx\rxxx", "UTF-8") + "/f3"));
            fail("CmisInvalidArgumentException expected!");
        } catch (CmisInvalidArgumentException iae) {
            // expected
        }

        try {
            HttpUtils.splitPath(createRequest("f1/" + URLEncoder.encode("xxx\bxxx", "UTF-8") + "/f3"));
            fail("CmisInvalidArgumentException expected!");
        } catch (CmisInvalidArgumentException iae) {
            // expected
        }

        try {
            HttpUtils.splitPath(createRequest("f1/" + URLEncoder.encode("xxx\u0000xxx", "UTF-8") + "/f3"));
            fail("CmisInvalidArgumentException expected!");
        } catch (CmisInvalidArgumentException iae) {
            // expected
        }
    }

    private HttpServletRequest createRequest(String path) {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(request.getContextPath()).thenReturn("/context");
        Mockito.when(request.getServletPath()).thenReturn("/servlet");
        Mockito.when(request.getRequestURI()).thenReturn("/context/servlet/" + path);

        return request;
    }
}
