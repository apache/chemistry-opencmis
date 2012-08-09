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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.LinkAccess;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoBrowserBindingImpl;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.parser.ContainerFactory;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;

/**
 * Base class for all Browser Binding client services.
 */
public abstract class AbstractBrowserBindingService implements LinkAccess {

    protected static final ContainerFactory SIMPLE_CONTAINER_FACTORY = new ContainerFactory() {
        public Map<String, Object> createObjectContainer() {
            return new LinkedHashMap<String, Object>();
        }

        public List<Object> creatArrayContainer() {
            return new ArrayList<Object>();
        }
    };

    private BindingSession session;
    private boolean succint;

    /**
     * Sets the current session.
     */
    protected void setSession(BindingSession session) {
        this.session = session;

        Object succintObj = session.get(SessionParameter.BROWSER_SUCCINCT);
        this.succint = (succintObj == null ? true : Boolean.parseBoolean(succintObj.toString()));
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
    protected String getServiceUrl() {
        Object url = session.get(SessionParameter.BROWSER_URL);
        if (url instanceof String) {
            return (String) url;
        }

        return null;
    }

    protected UrlBuilder getRepositoryUrl(String repositoryId, String selector) {
        UrlBuilder result = getRepositoryUrlCache().getRepositoryUrl(repositoryId, selector);

        if (result == null) {
            getRepositoriesInternal(repositoryId);
            result = getRepositoryUrlCache().getRepositoryUrl(repositoryId, selector);
        }

        if (result == null) {
            throw new CmisObjectNotFoundException("Unknown repository!");
        }

        return result;
    }

    protected UrlBuilder getRepositoryUrl(String repositoryId) {
        UrlBuilder result = getRepositoryUrlCache().getRepositoryUrl(repositoryId);

        if (result == null) {
            getRepositoriesInternal(repositoryId);
            result = getRepositoryUrlCache().getRepositoryUrl(repositoryId);
        }

        if (result == null) {
            throw new CmisObjectNotFoundException("Unknown repository!");
        }

        return result;
    }

    protected UrlBuilder getObjectUrl(String repositoryId, String objectId, String selector) {
        UrlBuilder result = getRepositoryUrlCache().getObjectUrl(repositoryId, objectId, selector);

        if (result == null) {
            getRepositoriesInternal(repositoryId);
            result = getRepositoryUrlCache().getObjectUrl(repositoryId, objectId, selector);
        }

        if (result == null) {
            throw new CmisObjectNotFoundException("Unknown repository!");
        }

        return result;
    }

    protected UrlBuilder getObjectUrl(String repositoryId, String objectId) {
        UrlBuilder result = getRepositoryUrlCache().getObjectUrl(repositoryId, objectId);

        if (result == null) {
            getRepositoriesInternal(repositoryId);
            result = getRepositoryUrlCache().getObjectUrl(repositoryId, objectId);
        }

        if (result == null) {
            throw new CmisObjectNotFoundException("Unknown repository!");
        }

        return result;
    }

    protected UrlBuilder getPathUrl(String repositoryId, String objectId, String selector) {
        UrlBuilder result = getRepositoryUrlCache().getPathUrl(repositoryId, objectId, selector);

        if (result == null) {
            getRepositoriesInternal(repositoryId);
            result = getRepositoryUrlCache().getPathUrl(repositoryId, objectId, selector);
        }

        if (result == null) {
            throw new CmisObjectNotFoundException("Unknown repository!");
        }

        return result;
    }

    protected boolean getSuccinct() {
        return succint;
    }

    protected String getSuccinctParameter() {
        return succint ? "true" : null;
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
     * Parses an object from an input stream.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> parseObject(InputStream stream, String charset) {
        Object obj = parse(stream, charset, SIMPLE_CONTAINER_FACTORY);

        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }

        throw new CmisConnectionException("Unexpected object!");
    }

    /**
     * Parses an array from an input stream.
     */
    @SuppressWarnings("unchecked")
    protected List<Object> parseArray(InputStream stream, String charset) {
        Object obj = parse(stream, charset, SIMPLE_CONTAINER_FACTORY);

        if (obj instanceof List) {
            return (List<Object>) obj;
        }

        throw new CmisConnectionException("Unexpected object!");
    }

    /**
     * Parses an input stream.
     */
    protected Object parse(InputStream stream, String charset, ContainerFactory containerFactory) {

        InputStreamReader reader = null;

        Object obj = null;
        try {
            reader = new InputStreamReader(stream, charset);
            JSONParser parser = new JSONParser();
            obj = parser.parse(reader, containerFactory);
        } catch (Exception e) {
            throw new CmisConnectionException("Parsing exception!", e);
        } finally {
            try {
                char[] buffer = new char[4096];
                while (reader.read(buffer) > -1) {
                }
            } catch (Exception e) {
            }
            try {
                if (reader == null) {
                    stream.close();
                } else {
                    reader.close();
                }
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

    /**
     * Performs a POST on an URL, checks the response code and returns the
     * result.
     */
    protected void postAndConsume(UrlBuilder url, String contentType, HttpUtils.Output writer) {
        HttpUtils.Response resp = post(url, contentType, writer);

        InputStream stream = resp.getStream();
        try {
            byte[] buffer = new byte[4096];
            while (stream.read(buffer) > -1) {
            }
        } catch (Exception e) {
            // ignore
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }
    }

    // ---- URL ----

    /**
     * Returns the repository URL cache or creates a new cache if it doesn't
     * exist.
     */
    protected RepositoryUrlCache getRepositoryUrlCache() {
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

        UrlBuilder url = null;

        if (repositoryId == null) {
            // no repository id provided -> get all
            url = new UrlBuilder(getServiceUrl());
        } else {
            // use URL of the specified repository
            url = getRepositoryUrlCache().getRepositoryUrl(repositoryId, Constants.SELECTOR_REPOSITORY_INFO);
            if (url == null) {
                // repository infos haven't been fetched yet -> get them all
                url = new UrlBuilder(getServiceUrl());
            }
        }

        // read and parse
        HttpUtils.Response resp = read(url);
        Map<String, Object> json = parseObject(resp.getStream(), resp.getCharset());

        List<RepositoryInfo> repInfos = new ArrayList<RepositoryInfo>();

        for (Object jri : json.values()) {
            if (jri instanceof Map) {
                @SuppressWarnings("unchecked")
                RepositoryInfo ri = JSONConverter.convertRepositoryInfo((Map<String, Object>) jri);
                String id = ri.getId();

                if (ri instanceof RepositoryInfoBrowserBindingImpl) {
                    String repositoryUrl = ((RepositoryInfoBrowserBindingImpl) ri).getRepositoryUrl();
                    String rootUrl = ((RepositoryInfoBrowserBindingImpl) ri).getRootUrl();

                    if (id == null || repositoryUrl == null || rootUrl == null) {
                        throw new CmisConnectionException("Found invalid Repository Info! (id: " + id + ")");
                    }

                    getRepositoryUrlCache().addRepository(id, repositoryUrl, rootUrl);
                }

                repInfos.add(ri);
            } else {
                throw new CmisConnectionException("Found invalid Repository Info!");
            }
        }

        return repInfos;
    }

    /**
     * Retrieves a type definition.
     */
    protected TypeDefinition getTypeDefinitionInternal(String repositoryId, String typeId) {
        // build URL
        UrlBuilder url = getRepositoryUrl(repositoryId, Constants.SELECTOR_TYPE_DEFINITION);
        url.addParameter(Constants.PARAM_TYPE_ID, typeId);

        // read and parse
        HttpUtils.Response resp = read(url);
        Map<String, Object> json = parseObject(resp.getStream(), resp.getCharset());

        return JSONConverter.convertTypeDefinition(json);
    }

    // ---- LinkAccess interface ----

    public String loadLink(String repositoryId, String objectId, String rel, String type) {
        // AtomPub specific -> return null
        return null;
    }

    public String loadContentLink(String repositoryId, String documentId) {
        UrlBuilder result = getRepositoryUrlCache().getObjectUrl(repositoryId, documentId, Constants.SELECTOR_CONTENT);
        return result == null ? null : result.toString();
    }
}
