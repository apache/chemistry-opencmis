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
package org.apache.chemistry.opencmis.client.bindings.spi.browser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.LinkAccess;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisProxyAuthenticationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUnauthorizedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JSONConstants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Base class for all Browser Binding client services.
 */
public abstract class AbstractBrowserBindingService implements LinkAccess {

    private BindingSession session;

    /**
     * Sets the current session.
     */
    protected void setSession(BindingSession session) {
        this.session = session;
    }

    /**
     * Gets the current session.
     */
    protected BindingSession getSession() {
        return session;
    }

    /**
     * Returns the service URL of this session.
     */
    protected String getServiceURL() {
        Object url = session.get(SessionParameter.BROWSER_URL);
        if (url instanceof String) {
            return (String) url;
        }

        return null;
    }

    // ---- exceptions ----

    /**
     * Converts an error message or a HTTP status code into an Exception.
     */
    protected CmisBaseException convertStatusCode(int code, String message, String errorContent, Throwable t) {

        Object obj = null;
        try {
            JSONParser parser = new JSONParser();
            obj = parser.parse(errorContent);
        } catch (Exception pe) {
        }

        if (obj instanceof JSONObject) {
            JSONObject json = (JSONObject) obj;
            Object jsonError = json.get(JSONConstants.ERROR_EXCEPTION);
            if (jsonError instanceof String) {
                Object jsonMessage = json.get(JSONConstants.ERROR_MESSAGE);
                if (jsonMessage != null) {
                    message = jsonMessage.toString();
                }

                if (CmisConstraintException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisConstraintException(message, errorContent, t);
                } else if (CmisContentAlreadyExistsException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisContentAlreadyExistsException(message, errorContent, t);
                } else if (CmisFilterNotValidException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisFilterNotValidException(message, errorContent, t);
                } else if (CmisInvalidArgumentException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisInvalidArgumentException(message, errorContent, t);
                } else if (CmisNameConstraintViolationException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisNameConstraintViolationException(message, errorContent, t);
                } else if (CmisNotSupportedException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisNotSupportedException(message, errorContent, t);
                } else if (CmisObjectNotFoundException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisObjectNotFoundException(message, errorContent, t);
                } else if (CmisPermissionDeniedException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisPermissionDeniedException(message, errorContent, t);
                } else if (CmisStorageException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisStorageException(message, errorContent, t);
                } else if (CmisStreamNotSupportedException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisStreamNotSupportedException(message, errorContent, t);
                } else if (CmisUpdateConflictException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisUpdateConflictException(message, errorContent, t);
                } else if (CmisVersioningException.EXCEPTION_NAME.equalsIgnoreCase((String) jsonError)) {
                    return new CmisVersioningException(message, errorContent, t);
                }
            }
        }

        // fall back to status code
        switch (code) {
        case 400:
            return new CmisInvalidArgumentException(message, errorContent, t);
        case 401:
            return new CmisUnauthorizedException(message, errorContent, t);
        case 403:
            return new CmisPermissionDeniedException(message, errorContent, t);
        case 404:
            return new CmisObjectNotFoundException(message, errorContent, t);
        case 405:
            return new CmisNotSupportedException(message, errorContent, t);
        case 407:
            return new CmisProxyAuthenticationException(message, errorContent, t);
        case 409:
            return new CmisConstraintException(message, errorContent, t);
        default:
            return new CmisRuntimeException(message, errorContent, t);
        }
    }

    // ---- helpers ----

    /**
     * Parses an input stream.
     */
    protected Object parse(InputStream stream, String charset) {
        Object obj = null;
        try {
            JSONParser parser = new JSONParser();
            obj = parser.parse(new InputStreamReader(stream, charset));
        } catch (Exception e) {
            throw new CmisConnectionException("Parsing exception!", e);
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }

        return obj;
    }

    /**
     * Performs a GET on an URL, checks the response code and returns the
     * result.
     */
    protected HttpUtils.Response read(UrlBuilder url) {
        // make the call
        HttpUtils.Response resp = HttpUtils.invokeGET(url, session);

        // check response code
        if (resp.getResponseCode() != 200) {
            throw convertStatusCode(resp.getResponseCode(), resp.getResponseMessage(), resp.getErrorContent(), null);
        }

        return resp;
    }

    /**
     * Performs a POST on an URL, checks the response code and returns the
     * result.
     */
    protected HttpUtils.Response post(UrlBuilder url, String contentType, HttpUtils.Output writer) {
        // make the call
        HttpUtils.Response resp = HttpUtils.invokePOST(url, contentType, writer, session);

        // check response code
        if (resp.getResponseCode() != 200 && resp.getResponseCode() != 201) {
            throw convertStatusCode(resp.getResponseCode(), resp.getResponseMessage(), resp.getErrorContent(), null);
        }

        return resp;
    }

    // ---- URL ----

    /**
     * Returns the repository URL cache or creates a new cache if it doesn't
     * exist.
     */
    protected RepositoryUrlCache geRepositoryUrlCache() {
        RepositoryUrlCache repositoryUrlCache = (RepositoryUrlCache) getSession().get(
                SpiSessionParameter.REPOSITORY_URL_CACHE);
        if (repositoryUrlCache == null) {
            repositoryUrlCache = new RepositoryUrlCache();
            getSession().put(SpiSessionParameter.REPOSITORY_URL_CACHE, repositoryUrlCache);
        }

        return repositoryUrlCache;
    }

    /**
     * Retrieves the the repository info objects.
     */
    protected List<RepositoryInfo> getRepositoriesInternal(String repositoryId) {
        // retrieve service doc
        UrlBuilder url = new UrlBuilder(getServiceURL());
        url.addParameter(Constants.PARAM_REPOSITORY_ID, repositoryId);

        // read and parse
        HttpUtils.Response resp = read(url);

        Object json = parse(resp.getStream(), resp.getCharset());

        if (json instanceof JSONObject) {
            return Collections.singletonList(JSONConverter.convertRepositoryInfo((JSONObject) json));
        }

        if (json instanceof JSONArray) {
            List<RepositoryInfo> repInfos = new ArrayList<RepositoryInfo>();

            for (Object ri : ((JSONArray) json)) {
                if (ri instanceof JSONObject) {
                    repInfos.add(JSONConverter.convertRepositoryInfo((JSONObject) json));
                }
            }

            return repInfos;
        }

        throw new CmisConnectionException("Repository Infos could not be read!");
    }

    // ---- LinkAccess interface ----

    public String loadLink(String repositoryId, String objectId, String rel, String type) {
        // AtomPub specific -> return null
        return null;
    }

    public String loadContentLink(String repositoryId, String documentId) {
        UrlBuilder result = geRepositoryUrlCache().getObjectUrl(repositoryId, documentId, Constants.SELECTOR_CONTENT);
        return result == null ? null : result.toString();
    }
}
