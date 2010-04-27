package org.apache.chemistry.opencmis.client.bindings.spi.local;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.CmisServiceFactory;

/**
 * Base class for all local clients.
 */
public abstract class AbstractLocalService {

    private Session session;
    private CmisServiceFactory factory;

    private String user;
    private String password;

    /**
     * Sets the current session.
     */
    protected void setSession(Session session) {
        this.session = session;

        Object userObj = session.get(SessionParameter.USER);
        user = userObj instanceof String ? userObj.toString() : null;

        Object passwordObj = session.get(SessionParameter.PASSWORD);
        password = passwordObj instanceof String ? passwordObj.toString() : null;
    }

    /**
     * Gets the current session.
     */
    protected Session getSession() {
        return session;
    }

    /**
     * Sets the service factory.
     */
    protected void setServiceFactory(CmisServiceFactory factory) {
        this.factory = factory;
    }

    /**
     * Gets the service factory.
     */
    protected CmisServiceFactory getServiceFactory() {
        return factory;
    }

    /**
     * creates a local call context.
     */
    protected CallContext createCallContext(String repositoryId) {
        return new LocalCallContext(repositoryId, user, password);
    }

    protected CmisService getService(String repositoryId) {
        return factory.getService(createCallContext(repositoryId));
    }

    // ------------------------------------------------------------------

    /**
     * Simple {@link CallContext} implementation.
     */
    static class LocalCallContext implements CallContext {

        private Map<String, String> contextMap = new HashMap<String, String>();

        public LocalCallContext(String repositoryId, String user, String password) {
            contextMap.put(REPOSITORY_ID, repositoryId);
            contextMap.put(USERNAME, user);
            contextMap.put(PASSWORD, password);
        }

        public String getBinding() {
            return BINDING_LOCAL;
        }

        public String get(String key) {
            return contextMap.get(key);
        }

        public String getRepositoryId() {
            return get(REPOSITORY_ID);
        }

        public String getUsername() {
            return get(USERNAME);
        }

        public String getPassword() {
            return get(PASSWORD);
        }

        public String getLocale() {
            return null;
        }

        public boolean isObjectInfoRequired() {
            return false;
        }
    }
}
