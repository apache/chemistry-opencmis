package org.apache.chemistry.opencmis.client.bindings.spi.local;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.MultiFilingService;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.CmisServiceFactory;

public class MultiFilingServiceImpl extends AbstractLocalService implements MultiFilingService {

	/**
	 * Constructor.
	 */
	public MultiFilingServiceImpl(Session session, CmisServiceFactory factory) {
		setSession(session);
		setServiceFactory(factory);
	}

	public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
			ExtensionsData extension) {
		CmisService service = getService(repositoryId);

		try {
			service.addObjectToFolder(repositoryId, objectId, folderId, allVersions, extension);
		} finally {
			service.close();
		}
	}

	public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
		CmisService service = getService(repositoryId);

		try {
			service.removeObjectFromFolder(repositoryId, objectId, folderId, extension);
		} finally {
			service.close();
		}
	}
}
