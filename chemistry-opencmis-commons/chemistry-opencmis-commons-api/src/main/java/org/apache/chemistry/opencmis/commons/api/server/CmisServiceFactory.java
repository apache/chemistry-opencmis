package org.apache.chemistry.opencmis.commons.api.server;

import java.util.Map;

public interface CmisServiceFactory {

    /**
     * Initializes the factory instance.
     */
    void init(Map<String, String> parameters);

    /**
     * Cleans up the the factory instance.
     */
    void destroy();

    /**
     * Returns a {@link CmisService} object for the given {@link CallContext}.
     * 
     * When the {@link CmisService} object is not longer needed
     * {@link CmisService#close()} will be called.
     */
    CmisService getService(CallContext context);
}
