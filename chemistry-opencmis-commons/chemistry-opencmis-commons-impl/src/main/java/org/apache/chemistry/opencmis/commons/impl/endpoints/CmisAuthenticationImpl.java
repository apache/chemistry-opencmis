package org.apache.chemistry.opencmis.commons.impl.endpoints;

import java.util.LinkedHashMap;

import org.apache.chemistry.opencmis.commons.endpoints.CmisAuthentication;
import org.apache.chemistry.opencmis.commons.endpoints.CmisEndpoint;

public class CmisAuthenticationImpl extends LinkedHashMap<String, Object> implements CmisAuthentication {

    private static final long serialVersionUID = 1L;

    private CmisEndpoint endpoint;

    public CmisAuthenticationImpl(CmisEndpoint endpoint) {
        if (endpoint == null) {
            throw new IllegalArgumentException("Endpoitn must be set!");
        }

        this.endpoint = endpoint;
    }

    public CmisAuthenticationImpl(CmisEndpoint endpoint, String type) {
        this(endpoint);
        put(KEY_TYPE, type);
    }

    @Override
    public String getType() {
        return getString(KEY_TYPE);
    }

    @Override
    public String getDisplayName() {
        return getString(KEY_DISPLAY_NAME);
    }

    @Override
    public String getDocumentationUrl() {
        return getString(KEY_DOCUMENTATION_URL);
    }

    @Override
    public Integer getPreference() {
        Object value = get(KEY_PREFERENCE);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        return null;
    }

    @Override
    public CmisEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public boolean requiresCookies() {
        if (CmisEndpoint.COOKIES_REQUIRED.equals(endpoint.getCookies())) {
            return true;
        }

        String type = getType();
        if (AUTH_FORM.equals(type) || AUTH_SAML.equals(type) || AUTH_LTPA.equals(type)) {
            return true;
        }

        if (endpoint.getCsrfHeader() != null || endpoint.getCsrfParameter() == null) {
            return true;
        }

        return false;
    }

    protected String getString(String key) {
        Object value = get(key);
        if (value instanceof String) {
            return (String) value;
        }

        return null;
    }
}
