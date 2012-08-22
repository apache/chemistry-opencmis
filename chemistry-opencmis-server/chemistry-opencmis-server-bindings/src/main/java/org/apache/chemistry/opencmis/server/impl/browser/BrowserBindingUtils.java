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
package org.apache.chemistry.opencmis.server.impl.browser;

import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;
import org.apache.chemistry.opencmis.commons.impl.json.JSONStreamAware;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;
import org.apache.chemistry.opencmis.server.shared.Dispatcher;
import org.apache.chemistry.opencmis.server.shared.HttpUtils;

public class BrowserBindingUtils {

    public static final String JSON_MIME_TYPE = "application/json";
    public static final String HTML_MIME_TYPE = "text/html";

    public static final String ROOT_PATH_FRAGMENT = "root";

    public static final String CONTEXT_OBJECT_ID = "org.apache.chemistry.opencmis.browserbinding.objectId";
    public static final String CONTEXT_OBJECT_TYPE_ID = "org.apache.chemistry.opencmis.browserbinding.objectTypeId";
    public static final String CONTEXT_BASETYPE_ID = "org.apache.chemistry.opencmis.browserbinding.basetypeId";
    public static final String CONTEXT_TOKEN = "org.apache.chemistry.opencmis.browserbinding.token";

    public enum CallUrl {
        SERVICE, REPOSITORY, ROOT
    }

    // Utility class.
    private BrowserBindingUtils() {
    }

    /**
     * Compiles the base URL for links, collections and templates.
     */
    public static UrlBuilder compileBaseUrl(HttpServletRequest request) {
        String baseUrl = (String) request.getAttribute(Dispatcher.BASE_URL_ATTRIBUTE);
        if (baseUrl != null) {
            return new UrlBuilder(baseUrl);
        }

        UrlBuilder url = new UrlBuilder(request.getScheme(), request.getServerName(), request.getServerPort(), null);

        url.addPath(request.getContextPath());
        url.addPath(request.getServletPath());

        return url;
    }

    public static UrlBuilder compileRepositoryUrl(HttpServletRequest request, String repositoryId) {
        return compileBaseUrl(request).addPathSegment(repositoryId);
    }

    public static UrlBuilder compileRootUrl(HttpServletRequest request, String repositoryId) {
        return compileRepositoryUrl(request, repositoryId).addPathSegment(ROOT_PATH_FRAGMENT);
    }

    /**
     * Returns the current CMIS path.
     */
    public static String getPath(HttpServletRequest request) {
        String[] pathFragments = HttpUtils.splitPath(request);
        if (pathFragments.length < 2) {
            return null;
        }
        if (pathFragments.length == 2) {
            return "/";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < pathFragments.length; i++) {
            if (pathFragments[i].length() == 0) {
                continue;
            }

            sb.append("/");
            sb.append(pathFragments[i]);
        }

        return sb.toString();
    }

    /**
     * Returns the object id of the current request.
     */
    public static void prepareContext(CallContext context, CallUrl callUrl, CmisService service, String repositoryId,
            String objectId, String token, HttpServletRequest request) {
        CallContextImpl contextImpl = null;
        if (context instanceof CallContextImpl) {
            contextImpl = (CallContextImpl) context;
            contextImpl.put(CONTEXT_TOKEN, token);
        }

        if (callUrl != CallUrl.ROOT) {
            return;
        }

        ObjectData object = null;

        if (objectId != null) {
            object = service.getObject(repositoryId, objectId, "cmis:objectId,cmis:objectTypeId,cmis:baseTypeId",
                    false, IncludeRelationships.NONE, "cmis:none", false, false, null);
        } else {
            object = service.getObjectByPath(repositoryId, getPath(request),
                    "cmis:objectId,cmis:objectTypeId,cmis:baseTypeId", false, IncludeRelationships.NONE, "cmis:none",
                    false, false, null);
        }

        if (contextImpl != null) {
            contextImpl.put(CONTEXT_OBJECT_ID, object.getId());
            contextImpl.put(CONTEXT_OBJECT_TYPE_ID, getProperty(object, PropertyIds.OBJECT_TYPE_ID, String.class));
            contextImpl.put(CONTEXT_BASETYPE_ID, getProperty(object, PropertyIds.BASE_TYPE_ID, String.class));
        }
    }

    /**
     * Extracts a property from an object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProperty(ObjectData object, String name, Class<T> clazz) {
        if (object == null) {
            return null;
        }

        Properties propData = object.getProperties();
        if (propData == null) {
            return null;
        }

        Map<String, PropertyData<?>> properties = propData.getProperties();
        if (properties == null) {
            return null;
        }

        PropertyData<?> property = properties.get(name);
        if (property == null) {
            return null;
        }

        Object value = property.getFirstValue();
        if (!clazz.isInstance(value)) {
            return null;
        }

        return (T) value;
    }

    public static Properties createProperties(ControlParser controlParser, String typeId, TypeCache typeCache) {
        List<String> propertyIds = controlParser.getValues(Constants.CONTROL_PROP_ID);
        if (propertyIds == null) {
            return null;
        }

        Map<Integer, String> singleValuePropertyMap = controlParser.getOneDimMap(Constants.CONTROL_PROP_VALUE);
        Map<Integer, Map<Integer, String>> multiValuePropertyMap = controlParser
                .getTwoDimMap(Constants.CONTROL_PROP_VALUE);

        if (typeId == null) {
            // it's a create call -> find type id in properties
            int i = 0;
            for (String propertId : propertyIds) {
                if (PropertyIds.OBJECT_TYPE_ID.equals(propertId)) {
                    typeId = singleValuePropertyMap.get(i);
                    break;
                }

                i++;
            }

            if (typeId == null) {
                throw new CmisInvalidArgumentException(PropertyIds.OBJECT_TYPE_ID + " not set!");
            }
        }

        TypeDefinition typeDef = typeCache.getTypeDefinition(typeId);
        if (typeDef == null) {
            throw new CmisInvalidArgumentException("Invalid type: " + typeId);
        }

        PropertiesImpl result = new PropertiesImpl();

        int i = 0;
        for (String propertyId : propertyIds) {
            PropertyDefinition<?> propDef = typeDef.getPropertyDefinitions().get(propertyId);
            if (propDef == null) {
                throw new CmisInvalidArgumentException(propertyId + " is unknown!");
            }

            PropertyData<?> propertyData = null;

            if (singleValuePropertyMap != null && singleValuePropertyMap.containsKey(i)) {
                propertyData = createPropertyData(propDef, singleValuePropertyMap.get(i));
            } else if (multiValuePropertyMap != null && multiValuePropertyMap.containsKey(i)) {
                propertyData = createPropertyData(propDef, controlParser.getValues(Constants.CONTROL_PROP_VALUE, i));
            } else {
                propertyData = createPropertyData(propDef, null);
            }

            result.addProperty(propertyData);

            i++;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static PropertyData<?> createPropertyData(PropertyDefinition<?> propDef, Object value) {

        List<String> strValues;
        if (value == null) {
            strValues = Collections.emptyList();
        } else if (value instanceof String) {
            strValues = new ArrayList<String>();
            strValues.add((String) value);
        } else {
            strValues = (List<String>) value;
        }

        PropertyData<?> propertyData = null;
        switch (propDef.getPropertyType()) {
        case STRING:
            propertyData = new PropertyStringImpl(propDef.getId(), strValues);
            break;
        case ID:
            propertyData = new PropertyIdImpl(propDef.getId(), strValues);
            break;
        case BOOLEAN:
            List<Boolean> boolValues = new ArrayList<Boolean>(strValues.size());
            try {
                for (String s : strValues) {
                    boolValues.add(Boolean.valueOf(s));
                }
            } catch (NumberFormatException e) {
                throw new CmisInvalidArgumentException(propDef.getId() + " value is not a boolean value!");
            }
            propertyData = new PropertyBooleanImpl(propDef.getId(), boolValues);
            break;
        case INTEGER:
            List<BigInteger> intValues = new ArrayList<BigInteger>(strValues.size());
            try {
                for (String s : strValues) {
                    intValues.add(new BigInteger(s));
                }
            } catch (NumberFormatException e) {
                throw new CmisInvalidArgumentException(propDef.getId() + " value is not an integer value!");
            }
            propertyData = new PropertyIntegerImpl(propDef.getId(), intValues);
            break;
        case DECIMAL:
            List<BigDecimal> decValues = new ArrayList<BigDecimal>(strValues.size());
            try {
                for (String s : strValues) {
                    decValues.add(new BigDecimal(s));
                }
            } catch (NumberFormatException e) {
                throw new CmisInvalidArgumentException(propDef.getId() + " value is not an integer value!");
            }
            propertyData = new PropertyDecimalImpl(propDef.getId(), decValues);
            break;
        case DATETIME:
            List<GregorianCalendar> calValues = new ArrayList<GregorianCalendar>(strValues.size());
            try {
                for (String s : strValues) {
                    GregorianCalendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(Long.parseLong(s));
                    calValues.add(cal);
                }
            } catch (NumberFormatException e) {
                throw new CmisInvalidArgumentException(propDef.getId() + " value is not an datetime value!");
            }
            propertyData = new PropertyDateTimeImpl(propDef.getId(), calValues);
            break;
        case HTML:
            propertyData = new PropertyHtmlImpl(propDef.getId(), strValues);
            break;
        case URI:
            propertyData = new PropertyUriImpl(propDef.getId(), strValues);
            break;
        }

        return propertyData;
    }

    public static List<String> createPolicies(ControlParser controlParser) {
        return controlParser.getValues(Constants.CONTROL_POLICY);
    }

    public static Acl createAddAcl(ControlParser controlParser) {
        List<String> principals = controlParser.getValues(Constants.CONTROL_ADD_ACE_PRINCIPAL);
        if (principals == null) {
            return null;
        }

        List<Ace> aces = new ArrayList<Ace>();

        int i = 0;
        for (String principalId : principals) {
            aces.add(new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(principalId), controlParser
                    .getValues(Constants.CONTROL_ADD_ACE_PERMISSION, i)));
            i++;
        }

        return new AccessControlListImpl(aces);
    }

    public static Acl createRemoveAcl(ControlParser controlParser) {
        List<String> principals = controlParser.getValues(Constants.CONTROL_REMOVE_ACE_PRINCIPAL);
        if (principals == null) {
            return null;
        }

        List<Ace> aces = new ArrayList<Ace>();

        int i = 0;
        for (String principalId : principals) {
            aces.add(new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(principalId), controlParser
                    .getValues(Constants.CONTROL_REMOVE_ACE_PERMISSION, i)));
            i++;
        }

        return new AccessControlListImpl(aces);
    }

    public static ContentStream createContentStream(HttpServletRequest request) {
        ContentStreamImpl result = null;

        if (request instanceof POSTHttpServletRequestWrapper) {
            POSTHttpServletRequestWrapper post = (POSTHttpServletRequestWrapper) request;
            if (post.getStream() != null) {
                result = new ContentStreamImpl(post.getFilename(), post.getSize(), post.getContentType(),
                        post.getStream());
            }
        }

        return result;
    }

    protected static ObjectData getSimpleObject(CmisService service, String repositoryId, String objectId) {
        return service.getObject(repositoryId, objectId, null, false, IncludeRelationships.NONE, "cmis:none", false,
                false, null);
    }

    /**
     * Sets the given HTTP status code if the surpessResponseCodes parameter is
     * not set to true; otherwise sets HTTP status code 200 (OK).
     */
    public static void setStatus(HttpServletRequest request, HttpServletResponse response, int statusCode) {
        if (getBooleanParameter(request, Constants.PARAM_SUPPRESS_RESPONSE_CODES, false)) {
            statusCode = HttpServletResponse.SC_OK;
        }

        response.setStatus(statusCode);
    }

    /**
     * Transforms the transaction into a cookie name.
     */
    public static String getCookieName(String token) {
        if (token == null || token.length() == 0) {
            return "cmis%";
        }

        return "cmis_" + Base64.encodeBytes(token.getBytes()).replace('=', '%');
    }

    /**
     * Sets a transaction cookie.
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String repositoryId,
            String token, String value) {
        setCookie(request, response, repositoryId, token, value, 3600);
    }

    /**
     * Deletes a transaction cookie.
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String repositoryId,
            String token) {
        setCookie(request, response, repositoryId, token, "", 0);
    }

    /**
     * Sets a transaction cookie.
     */
    public static void setCookie(HttpServletRequest request, HttpServletResponse response, String repositoryId,
            String token, String value, int expiry) {
        if (token != null && token.length() > 0) {
            String cookieValue = value;
            try {
                cookieValue = URLEncoder.encode(value, "UTF-8");
            } catch (UnsupportedEncodingException e) {
            }

            Cookie transactionCookie = new Cookie(getCookieName(token), cookieValue);
            transactionCookie.setMaxAge(expiry);
            transactionCookie.setPath(request.getContextPath() + request.getServletPath() + "/" + repositoryId);
            response.addCookie(transactionCookie);
        }
    }

    public static String createCookieValue(int code, String objectId, String ex, String message) {
        JSONObject result = new JSONObject();

        result.put("code", code);
        result.put("objectId", objectId == null ? "" : objectId);
        result.put("exception", ex == null ? "" : ex);
        result.put("message", message == null ? "" : message);

        return result.toJSONString();
    }

    /**
     * Writes JSON to the servlet response and adds a callback wrapper if
     * requested.
     */
    public static void writeJSON(JSONStreamAware json, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String token = getStringParameter(request, Constants.PARAM_TOKEN);

        if (token != null && "POST".equals(request.getMethod())) {
            response.setContentType(HTML_MIME_TYPE);
            response.setContentLength(0);
        } else {
            response.setContentType(JSON_MIME_TYPE);
            response.setCharacterEncoding("UTF-8");

            String callback = getStringParameter(request, Constants.PARAM_CALLBACK);
            if (callback != null) {
                if (!callback.matches("[A-Za-z0-9._\\[\\]]*")) {
                    throw new CmisInvalidArgumentException("Invalid callback name!");
                }
                response.getWriter().print(callback + "(");
            }

            json.writeJSONString(response.getWriter());

            if (callback != null) {
                response.getWriter().print(");");
            }
        }

        response.getWriter().flush();
    }

    public static void writeEmpty(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentLength(0);
        response.setContentType(HTML_MIME_TYPE);
        response.getWriter().flush();
    }
}
