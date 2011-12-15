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
/*
 * This class has been taken from Apache Harmony (http://harmony.apache.org/) 
 * and has been modified to work with OpenCMIS.
 */
package org.apache.chemistry.opencmis.client.bindings.spi.cookies;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides an in-memory cookie store.
 */
class CmisCookieStoreImpl implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<URI, ArrayList<CmisHttpCookie>> storeMap;

    public CmisCookieStoreImpl() {
        this(1000);
    }

    public CmisCookieStoreImpl(final int maxUrls) {
        storeMap = new LinkedHashMap<URI, ArrayList<CmisHttpCookie>>(maxUrls + 1, 0.70f, true) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean removeEldestEntry(Map.Entry<URI, ArrayList<CmisHttpCookie>> eldest) {
                return size() > maxUrls;
            }
        };
    }

    public void add(URI uri, CmisHttpCookie cookie) {
        if (uri == null || cookie == null) {
            throw new NullPointerException();
        }

        ArrayList<CmisHttpCookie> cookies = null;
        if (storeMap.containsKey(uri)) {
            cookies = storeMap.get(uri);
            cookies.remove(cookie);
            cookies.add(cookie);

            // eliminate expired cookies
            if (cookies.size() > 1) {
                cleanCookieList(cookies);
            }
        } else {
            cookies = new ArrayList<CmisHttpCookie>();
            cookies.add(cookie);
            storeMap.put(uri, cookies);
        }
    }

    public List<CmisHttpCookie> get(URI uri) {
        if (uri == null) {
            throw new NullPointerException("URI is null!");
        }

        // get cookies associated with given URI. If none, returns an empty list
        List<CmisHttpCookie> cookies = storeMap.get(uri);
        if (cookies == null) {
            cookies = new ArrayList<CmisHttpCookie>();
        } else {
            // eliminate expired cookies
            cleanCookieList(cookies);
        }

        // get cookies whose domain matches the given URI
        List<URI> uris = new ArrayList<URI>(storeMap.keySet());
        for (URI u : uris) {
            // exclude the given URI
            if (!u.equals(uri)) {
                List<CmisHttpCookie> listCookie = storeMap.get(u);
                for (CmisHttpCookie cookie : listCookie) {
                    if (CmisHttpCookie.domainMatches(cookie.getDomain(), uri.getHost())) {
                        if (cookie.hasExpired()) {
                            listCookie.remove(cookie);
                            if (listCookie.isEmpty()) {
                                storeMap.remove(u);
                            }
                        } else if (!(cookie.hasExpired() || cookies.contains(cookie))) {
                            cookies.add(cookie);
                        }
                    }
                }
            }
        }

        return cookies;
    }

    private void cleanCookieList(List<CmisHttpCookie> cookies) {
        for (CmisHttpCookie cookie : cookies) {
            if (cookie.hasExpired()) {
                cookies.remove(cookie);
            }
        }
    }

    public List<CmisHttpCookie> getCookies() {
        List<CmisHttpCookie> cookies = new ArrayList<CmisHttpCookie>();
        Collection<ArrayList<CmisHttpCookie>> values = storeMap.values();
        for (ArrayList<CmisHttpCookie> list : values) {
            for (CmisHttpCookie cookie : list) {
                if (cookie.hasExpired()) {
                    list.remove(cookie); // eliminate expired cookies
                } else if (!cookies.contains(cookie)) {
                    cookies.add(cookie);
                }
            }
        }

        return Collections.unmodifiableList(cookies);
    }

    public List<URI> getURIs() {
        return new ArrayList<URI>(storeMap.keySet());
    }

    public boolean remove(URI uri, CmisHttpCookie cookie) {
        if (cookie == null) {
            throw new NullPointerException("Cookie is null!");
        }

        boolean success = false;
        Collection<ArrayList<CmisHttpCookie>> values = storeMap.values();
        for (ArrayList<CmisHttpCookie> list : values) {
            if (list.remove(cookie)) {
                success = true;
            }
        }

        return success;
    }

    public boolean removeAll() {
        if (!storeMap.isEmpty()) {
            storeMap.clear();
        }

        return true;
    }
}
