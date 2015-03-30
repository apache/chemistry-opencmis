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
package org.apache.chemistry.opencmis.client.bindings.misc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.chemistry.opencmis.client.bindings.spi.cookies.CmisCookieManager;

public class CookiesTest extends TestCase {

    public void testCookies() {
        CmisCookieManager manager = new CmisCookieManager();
        String url;

        url = "https://www.example.com/s/test/abc?xyz";
        addCookie(manager, url, "cookie1", "c1-1111", "/s/");

        url = "https://www.example.com/s/test/abc?xyz";
        addCookie(manager, url, "cookie2", "c2-1111", "/s/");
        deleteCookie(manager, url, "cookie2", "/s/");

        url = "https://www.example.com/s/test/abc";
        addCookie(manager, url, "cookie1", "c1-2222", "/s/");

        url = "https://www.example.com/s/test/abc";
        addCookie(manager, url, "cookie1", "c1-3333", "/s/t");

        url = "https://www.example.com/s/test/abc?abc";
        addCookie(manager, url, "cookie1", "c1-4444", "/s/x");

        List<String> cookies = manager.get("https://www.example.com/s/test/abc/s", new HashMap<String, List<String>>())
                .get("Cookie");

        assertEquals(1, cookies.size());
        assertEquals(cookies.get(0), "cookie1=c1-3333; cookie1=c1-2222");
        //assertEquals(cookies.get(1), "cookie1=c1-2222");
    }

    private void addCookie(CmisCookieManager manager, String url, String name, String value, String path) {
        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();

        responseHeaders.put(null, Collections.singletonList("HTTP/1.1 200 OK"));
        responseHeaders
                .put("Set-Cookie", Collections.singletonList(name + "=" + value + "; Path=" + path + "; Secure"));

        manager.put(url, responseHeaders);
    }

    private void deleteCookie(CmisCookieManager manager, String url, String name, String path) {
        Map<String, List<String>> responseHeaders = new HashMap<String, List<String>>();

        responseHeaders.put(null, Collections.singletonList("HTTP/1.1 200 OK"));
        responseHeaders.put("Set-Cookie",
                Collections.singletonList(name + "=delete; Path=" + path + "; Secure; Max-Age=0"));

        manager.put(url, responseHeaders);
    }
}
