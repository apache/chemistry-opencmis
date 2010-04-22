package org.apache.chemistry.opencmis.inmemory.clientprovider;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.api.server.CallContext;

public class DummyCallContext implements CallContext {
    private Map<String, String> fParameter = new HashMap<String, String>();

    public DummyCallContext() {
        fParameter.put(USERNAME, "TestUser");
        fParameter.put(PASSWORD, "secret");
        fParameter.put(LOCALE, "en");
    }

    public boolean isObjectInfoRequired() {
        return false;
    }

    public String get(String key) {
        return fParameter.get(key);
    }

    public String getBinding() {
        return BINDING_ATOMPUB;
    }

    public String getRepositoryId() {
        return get(REPOSITORY_ID);
    }

    public String getLocale() {
        return get(LOCALE);
    }

    public String getPassword() {
        return get(PASSWORD);
    }

    public String getUsername() {
        return get(USERNAME);
    }

    public void put(String key, String value) {
        fParameter.put(key, value);
    }
}
