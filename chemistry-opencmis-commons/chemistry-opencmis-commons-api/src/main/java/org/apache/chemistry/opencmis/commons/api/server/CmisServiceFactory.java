package org.apache.chemistry.opencmis.commons.api.server;

public interface CmisServiceFactory {

	CmisService getService(CallContext context);
}
