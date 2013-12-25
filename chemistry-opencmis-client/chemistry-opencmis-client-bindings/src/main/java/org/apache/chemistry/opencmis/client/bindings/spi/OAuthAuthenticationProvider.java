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
package org.apache.chemistry.opencmis.client.bindings.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.chemistry.opencmis.client.bindings.impl.ClientVersion;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth Authentication Provider.
 */
public class OAuthAuthenticationProvider extends StandardAuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticationProvider.class);

    private static final long serialVersionUID = 1L;

    private String accessToken;
    private String refreshToken;
    private long expiresTimestamp;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Map<String, List<String>> getHTTPHeaders(String url) {
        Map<String, List<String>> headers = super.getHTTPHeaders(url);
        if (headers == null) {
            headers = new HashMap<String, List<String>>();
        }

        headers.put("Authorization", Collections.singletonList("Bearer " + getToken()));

        return headers;
    }

    /**
     * Gets the access token. If no access token is present or the access token
     * expired, a new token is requested.
     * 
     * @return the access token
     */
    protected String getToken() {
        lock.writeLock().lock();
        try {
            if (accessToken == null) {
                requestToken();
            } else if (System.currentTimeMillis() >= expiresTimestamp) {
                refreshToken();
            }
            return accessToken;
        } catch (CmisConnectionException ce) {
            throw ce;
        } catch (Exception e) {
            throw new CmisConnectionException("Cannot get OAuth access token: " + e.getMessage(), e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void requestToken() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Request new OAuth access token.");
        }

        makeRequest(false);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Access token: {} / Refresh token: {}", accessToken, refreshToken);
        }
    }

    private void refreshToken() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Refresh new OAuth access token.");
        }

        makeRequest(true);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Access token: {} / Refresh token: {}", accessToken, refreshToken);
        }
    }

    private void makeRequest(boolean isRefresh) throws IOException {
        Object tokenEndpoint = getSession().get(SessionParameter.OAUTH_TOKEN_ENDPOINT);
        if (!(tokenEndpoint instanceof String)) {
            throw new CmisConnectionException("Token endpoint not set!");
        }

        if (isRefresh && refreshToken == null) {
            throw new CmisConnectionException("No refresh token!");
        }

        // request token
        HttpURLConnection conn = (HttpURLConnection) (new URL(tokenEndpoint.toString())).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setAllowUserInteraction(false);
        conn.setUseCaches(false);
        conn.setRequestProperty("User-Agent", ClientVersion.OPENCMIS_CLIENT);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        // compile request
        Writer writer = new OutputStreamWriter(conn.getOutputStream(), IOUtils.UTF8);

        if (isRefresh) {
            writer.write("grant_type=refresh_token");

            writer.write("&refresh_token=");
            writer.write(IOUtils.encodeURL(refreshToken));
        } else {
            writer.write("grant_type=authorization_code");

            Object code = getSession().get(SessionParameter.OAUTH_CODE);
            if (code != null) {
                writer.write("&code=");
                writer.write(IOUtils.encodeURL(code.toString()));
            }

            Object redirectUri = getSession().get(SessionParameter.OAUTH_REDIRECT_URI);
            if (redirectUri != null) {
                writer.write("&redirect_uri=");
                writer.write(IOUtils.encodeURL(redirectUri.toString()));
            }
        }

        Object clientId = getSession().get(SessionParameter.OAUTH_CLIENT_ID);
        if (clientId != null) {
            writer.write("&client_id=");
            writer.write(IOUtils.encodeURL(clientId.toString()));
        }

        Object clientSecret = getSession().get(SessionParameter.OAUTH_CLIENT_SECRET);
        if (clientSecret != null) {
            writer.write("&client_secret=");
            writer.write(IOUtils.encodeURL(clientSecret.toString()));
        }

        writer.flush();

        // connect
        conn.connect();

        // check success
        if (conn.getResponseCode() != 200) {
            JSONObject jsonResponse = parseResponse(conn);
            Object error = jsonResponse.get("error");
            Object description = jsonResponse.get("error_description");

            if (LOG.isDebugEnabled()) {
                LOG.debug("OAuth token request failed: {}", jsonResponse.toJSONString());
            }

            throw new CmisConnectionException("OAuth token request failed" + (error == null ? "" : ": " + error)
                    + (description == null ? "" : ": " + description));
        }

        // parse response
        JSONObject jsonResponse = parseResponse(conn);

        Object tokenType = jsonResponse.get("token_type");
        if (!(tokenType instanceof String) || !"bearer".equalsIgnoreCase((String) tokenType)) {
            throw new CmisConnectionException("Unsupported OAuth token type: " + tokenType);
        }

        Object jsonAccessToken = jsonResponse.get("access_token");
        if (!(jsonAccessToken instanceof String)) {
            throw new CmisConnectionException("Invalid OAuth access token!");
        }

        Object jsonRefreshToken = jsonResponse.get("refresh_token");
        if (jsonRefreshToken != null && !(jsonRefreshToken instanceof String)) {
            throw new CmisConnectionException("Invalid OAuth refresh token!");
        }

        // default expiration time: 1 hour
        long expiresIn = 3600;
        Object jsonExpiresIn = jsonResponse.get("expires_in");
        if (jsonExpiresIn != null) {
            if (jsonExpiresIn instanceof Number) {
                expiresIn = ((Number) jsonExpiresIn).longValue();
            } else if (jsonExpiresIn instanceof String) {
                try {
                    expiresIn = Long.parseLong((String) jsonExpiresIn);
                } catch (NumberFormatException nfe) {
                    throw new CmisConnectionException("Invalid OAuth expires in value!");
                }
            } else {
                throw new CmisConnectionException("Invalid OAuth expires in value!");
            }
        }

        accessToken = jsonAccessToken.toString();
        refreshToken = (jsonRefreshToken == null ? null : jsonRefreshToken.toString());
        expiresTimestamp = expiresIn * 1000 + System.currentTimeMillis();
    }

    private JSONObject parseResponse(HttpURLConnection conn) {
        Reader reader = null;
        try {
            InputStream stream = null;

            int respCode = conn.getResponseCode();
            if (respCode >= 200 && respCode < 300) {
                stream = conn.getInputStream();
            } else {
                stream = conn.getErrorStream();
            }
            if (stream == null) {
                throw new CmisConnectionException("Invalid OAuth token response!");
            }

            reader = new InputStreamReader(stream, extractCharset(conn));
            JSONParser parser = new JSONParser();
            Object response = parser.parse(reader);

            if (!(response instanceof JSONObject)) {
                throw new CmisConnectionException("Invalid OAuth token response!");
            }

            return (JSONObject) response;
        } catch (CmisConnectionException ce) {
            throw ce;
        } catch (Exception pe) {
            throw new CmisConnectionException("Parsing the OAuth token response failed: " + pe.getMessage(), pe);
        } finally {
            IOUtils.consumeAndClose(reader);
        }
    }

    private String extractCharset(HttpURLConnection conn) {
        String charset = IOUtils.UTF8;

        String contentType = conn.getContentType();
        if (contentType != null) {
            String[] parts = contentType.split(";");
            for (int i = 1; i < parts.length; i++) {
                String part = parts[i].trim().toLowerCase();
                if (part.startsWith("charset")) {
                    int x = part.indexOf('=');
                    charset = part.substring(x + 1).trim();
                    break;
                }
            }
        }

        return charset;
    }
}
