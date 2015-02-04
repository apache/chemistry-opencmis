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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
 * OAuth 2.0 Authentication Provider.
 * <p>
 * This authentication provider implements OAuth 2.0 (RFC 6749) Bearer Tokens
 * (RFC 6750).
 * <p>
 * The provider can be either configured with an authorization code or with an
 * existing bearer token. Token endpoint and client ID are always required. If a
 * client secret is required depends on the authorization server.
 * <p>
 * Configuration with authorization code:
 * 
 * <pre>
 * SessionFactory factory = ...
 * 
 * Map&lt;String, String> parameter = new HashMap&lt;String, String>();
 * 
 * parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost/cmis/atom");
 * parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
 * parameter.put(SessionParameter.REPOSITORY_ID, "myRepository");
 * 
 * parameter.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, "org.apache.chemistry.opencmis.client.bindings.spi.OAuthAuthenticationProvider");
 * 
 * parameter.put(SessionParameter.OAUTH_TOKEN_ENDPOINT, "https://example.com/auth/oauth/token");
 * parameter.put(SessionParameter.OAUTH_CLIENT_ID, "s6BhdRkqt3");
 * parameter.put(SessionParameter.OAUTH_CLIENT_SECRET, "7Fjfp0ZBr1KtDRbnfVdmIw");
 * 
 * parameter.put(SessionParameter.OAUTH_CODE, "abc");
 * 
 * ...
 * Session session = factory.createSession(parameter);
 * </pre>
 * 
 * <p>
 * Configuration with existing bearer token:
 * 
 * <pre>
 * SessionFactory factory = ...
 * 
 * Map&lt;String, String> parameter = new HashMap&lt;String, String>();
 * 
 * parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost/cmis/atom");
 * parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
 * parameter.put(SessionParameter.REPOSITORY_ID, "myRepository");
 * 
 * parameter.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS, "org.apache.chemistry.opencmis.client.bindings.spi.OAuthAuthenticationProvider");
 *  
 * parameter.put(SessionParameter.OAUTH_TOKEN_ENDPOINT, "https://example.com/auth/oauth/token");
 * parameter.put(SessionParameter.OAUTH_CLIENT_ID, "s6BhdRkqt3");
 * parameter.put(SessionParameter.OAUTH_CLIENT_SECRET, "7Fjfp0ZBr1KtDRbnfVdmIw");
 * 
 * parameter.put(SessionParameter.OAUTH_ACCESS_TOKEN, "2YotnFZFEjr1zCsicMWpAA");
 * parameter.put(SessionParameter.OAUTH_REFRESH_TOKEN, "tGzv3JOkF0XG5Qx2TlKWIA");
 * parameter.put(SessionParameter.OAUTH_EXPIRATION_TIMESTAMP, "1388237075127");
 * 
 * ...
 * Session session = factory.createSession(parameter);
 * </pre>
 */
public class OAuthAuthenticationProvider extends StandardAuthenticationProvider {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAuthenticationProvider.class);

    private static final long serialVersionUID = 1L;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Token token = null;
    private long defaultTokenLifetime = 3600;
    private List<TokenListener> tokenListeners;

    @Override
    public void setSession(BindingSession session) {
        super.setSession(session);

        if (token == null) {
            // get predefined access token
            String accessToken = null;
            if (session.get(SessionParameter.OAUTH_ACCESS_TOKEN) instanceof String) {
                accessToken = (String) session.get(SessionParameter.OAUTH_ACCESS_TOKEN);
            }

            // get predefined refresh token
            String refreshToken = null;
            if (session.get(SessionParameter.OAUTH_REFRESH_TOKEN) instanceof String) {
                refreshToken = (String) session.get(SessionParameter.OAUTH_REFRESH_TOKEN);
            }

            // get predefined expiration timestamp
            long expirationTimestamp = 0;
            if (session.get(SessionParameter.OAUTH_EXPIRATION_TIMESTAMP) instanceof String) {
                try {
                    expirationTimestamp = Long.parseLong((String) session
                            .get(SessionParameter.OAUTH_EXPIRATION_TIMESTAMP));
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            } else if (session.get(SessionParameter.OAUTH_EXPIRATION_TIMESTAMP) instanceof Number) {
                expirationTimestamp = ((Number) session.get(SessionParameter.OAUTH_EXPIRATION_TIMESTAMP)).longValue();
            }

            // get default token lifetime
            if (session.get(SessionParameter.OAUTH_DEFAULT_TOKEN_LIFETIME) instanceof String) {
                try {
                    defaultTokenLifetime = Long.parseLong((String) session
                            .get(SessionParameter.OAUTH_DEFAULT_TOKEN_LIFETIME));
                } catch (NumberFormatException nfe) {
                    // ignore
                }
            } else if (session.get(SessionParameter.OAUTH_DEFAULT_TOKEN_LIFETIME) instanceof Number) {
                defaultTokenLifetime = ((Number) session.get(SessionParameter.OAUTH_DEFAULT_TOKEN_LIFETIME))
                        .longValue();
            }

            token = new Token(accessToken, refreshToken, expirationTimestamp);
            fireTokenListner(token);
        }
    }

    @Override
    public Map<String, List<String>> getHTTPHeaders(String url) {
        Map<String, List<String>> headers = super.getHTTPHeaders(url);
        if (headers == null) {
            headers = new HashMap<String, List<String>>();
        }

        headers.put("Authorization", Collections.singletonList("Bearer " + getAccessToken()));

        return headers;
    }

    /**
     * Returns the current token.
     * 
     * @return the current token
     */
    public Token getToken() {
        lock.readLock().lock();
        try {
            return token;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Adds a token listener.
     * 
     * @param listner
     *            the listener object
     */
    public void addTokenListener(TokenListener listner) {
        if (listner == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            if (tokenListeners == null) {
                tokenListeners = new ArrayList<OAuthAuthenticationProvider.TokenListener>();
            }

            tokenListeners.add(listner);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes a token listener.
     * 
     * @param listner
     *            the listener object
     */
    public void removeTokenListener(TokenListener listner) {
        if (listner == null) {
            return;
        }

        lock.writeLock().lock();
        try {
            if (tokenListeners != null) {
                tokenListeners.remove(listner);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Lets all token listeners know that there is a new token.
     */
    protected void fireTokenListner(Token token) {
        if (tokenListeners == null) {
            return;
        }

        for (TokenListener listner : tokenListeners) {
            listner.tokenRefreshed(token);
        }
    }

    @Override
    protected boolean getSendBearerToken() {
        // the super class should not handle bearer tokens
        return false;
    }

    /**
     * Gets the access token. If no access token is present or the access token
     * is expired, a new token is requested.
     * 
     * @return the access token
     */
    protected String getAccessToken() {
        lock.writeLock().lock();
        try {
            if (token.getAccessToken() == null) {
                if (token.getRefreshToken() == null) {
                    requestToken();
                } else {
                    refreshToken();
                }
            } else if (token.isExpired()) {
                refreshToken();
            }

            return token.getAccessToken();
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
            LOG.debug("Requesting new OAuth access token.");
        }

        makeRequest(false);

        if (LOG.isTraceEnabled()) {
            LOG.trace(token.toString());
        }
    }

    private void refreshToken() throws IOException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Refreshing OAuth access token.");
        }

        makeRequest(true);

        if (LOG.isTraceEnabled()) {
            LOG.trace(token.toString());
        }
    }

    private void makeRequest(boolean isRefresh) throws IOException {
        Object tokenEndpoint = getSession().get(SessionParameter.OAUTH_TOKEN_ENDPOINT);
        if (!(tokenEndpoint instanceof String)) {
            throw new CmisConnectionException("Token endpoint not set!");
        }

        if (isRefresh && token.getRefreshToken() == null) {
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
            writer.write(IOUtils.encodeURL(token.getRefreshToken()));
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
            throw new CmisConnectionException("Invalid OAuth access_token!");
        }

        Object jsonRefreshToken = jsonResponse.get("refresh_token");
        if (jsonRefreshToken != null && !(jsonRefreshToken instanceof String)) {
            throw new CmisConnectionException("Invalid OAuth refresh_token!");
        }

        long expiresIn = defaultTokenLifetime;
        Object jsonExpiresIn = jsonResponse.get("expires_in");
        if (jsonExpiresIn != null) {
            if (jsonExpiresIn instanceof Number) {
                expiresIn = ((Number) jsonExpiresIn).longValue();
            } else if (jsonExpiresIn instanceof String) {
                try {
                    expiresIn = Long.parseLong((String) jsonExpiresIn);
                } catch (NumberFormatException nfe) {
                    throw new CmisConnectionException("Invalid OAuth expires_in value!");
                }
            } else {
                throw new CmisConnectionException("Invalid OAuth expires_in value!");
            }
        }

        token = new Token(jsonAccessToken.toString(), (jsonRefreshToken == null ? null : jsonRefreshToken.toString()),
                expiresIn * 1000 + System.currentTimeMillis());
        fireTokenListner(token);
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
                String part = parts[i].trim().toLowerCase(Locale.ENGLISH);
                if (part.startsWith("charset")) {
                    int x = part.indexOf('=');
                    charset = part.substring(x + 1).trim();
                    break;
                }
            }
        }

        return charset;
    }

    /**
     * Token holder class.
     */
    public static class Token {
        private String accessToken;
        private String refreshToken;
        private long expirationTimestamp;

        public Token(String accessToken, String refreshToken, long expirationTimestamp) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expirationTimestamp = expirationTimestamp;
        }

        /**
         * Returns the access token.
         * 
         * @return the access token
         */
        public String getAccessToken() {
            return accessToken;
        }

        /**
         * Returns the refresh token.
         * 
         * @return the refresh token
         */
        public String getRefreshToken() {
            return refreshToken;
        }

        /**
         * Returns the timestamp when the access expires.
         * 
         * @return the timestamp in milliseconds since midnight, January 1, 1970
         *         UTC.
         */
        public long getExpirationTimestamp() {
            return expirationTimestamp;
        }

        /**
         * Returns whether the access token is expired or not.
         * 
         * @return {@code true} if the access token is expired, {@code false}
         *         otherwise
         */
        public boolean isExpired() {
            return System.currentTimeMillis() >= expirationTimestamp;
        }

        @Override
        public String toString() {
            return "Access token: " + accessToken + " / Refresh token: " + refreshToken + " / Expires : "
                    + expirationTimestamp;
        }
    }

    /**
     * Listener for OAuth token events.
     */
    public interface TokenListener {

        /**
         * Called when a token is requested of refreshed.
         * 
         * @param token
         *            the new token
         */
        void tokenRefreshed(Token token);
    }
}
