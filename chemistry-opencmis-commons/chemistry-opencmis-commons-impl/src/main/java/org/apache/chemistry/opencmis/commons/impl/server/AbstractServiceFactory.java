package org.apache.chemistry.opencmis.commons.impl.server;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.CmisServiceFactory;

public abstract class AbstractServiceFactory implements CmisServiceFactory {

	public void init(Map<String, String> parameters) {
	}

	public void destroy() {
	}

	public abstract CmisService getService(CallContext context);
}
