package org.apache.chemistry.opencmis.client.bindings.spi.local;

import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpi;
import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpiFactory;
import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.api.AclService;
import org.apache.chemistry.opencmis.commons.api.DiscoveryService;
import org.apache.chemistry.opencmis.commons.api.MultiFilingService;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectService;
import org.apache.chemistry.opencmis.commons.api.PolicyService;
import org.apache.chemistry.opencmis.commons.api.RelationshipService;
import org.apache.chemistry.opencmis.commons.api.RepositoryService;
import org.apache.chemistry.opencmis.commons.api.VersioningService;
import org.apache.chemistry.opencmis.commons.api.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * * CMIS local SPI implementation.
 */
public class CmisLocalSpi implements CmisSpiFactory, CmisSpi {

	private static Log log = LogFactory.getLog(CmisLocalSpi.class);

	private Session session;
	private CmisServiceFactory serviceFactory;

	private RepositoryService repositoryService;
	private NavigationService navigationService;
	private ObjectService objectService;
	private VersioningService versioningService;
	private DiscoveryService discoveryService;
	private MultiFilingService multiFilingService;
	private RelationshipService relationshipService;
	private PolicyService policyService;
	private AclService aclService;

	/**
	 * Constructor.
	 */
	public CmisLocalSpi() {
	}

	public CmisSpi getSpiInstance(Session session) {
		if (log.isDebugEnabled()) {
			log.debug("Initializing local SPI...");
		}

		this.session = session;

		String serviceFactoryClassname = (String) session.get(SessionParameter.LOCAL_FACTORY);
		if (serviceFactoryClassname == null) {
			throw new CmisConnectionException("Factory class not set!");
		}

		try {
			serviceFactory = (CmisServiceFactory) Class.forName(serviceFactoryClassname).newInstance();
		} catch (Exception e) {
			throw new CmisConnectionException("Factory cannot be created!", e);
		}

		return this;
	}

	public RepositoryService getRepositoryService() {
		return repositoryService;
	}

	public NavigationService getNavigationService() {
		return navigationService;
	}

	public ObjectService getObjectService() {
		return objectService;
	}

	public DiscoveryService getDiscoveryService() {
		return discoveryService;
	}

	public VersioningService getVersioningService() {
		return versioningService;
	}

	public MultiFilingService getMultiFilingService() {
		return multiFilingService;
	}

	public RelationshipService getRelationshipService() {
		return relationshipService;
	}

	public PolicyService getPolicyService() {
		return policyService;
	}

	public AclService getAclService() {
		return aclService;
	}

	public void clearAllCaches() {
	}

	public void clearRepositoryCache(String repositoryId) {
	}

	public void close() {
		serviceFactory.destroy();
	}
}
