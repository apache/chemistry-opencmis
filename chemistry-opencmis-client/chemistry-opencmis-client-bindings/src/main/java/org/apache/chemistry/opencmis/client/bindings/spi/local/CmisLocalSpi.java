package org.apache.chemistry.opencmis.client.bindings.spi.local;

import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpi;
import org.apache.chemistry.opencmis.client.bindings.spi.CmisSpiFactory;
import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.api.AclService;
import org.apache.chemistry.opencmis.commons.api.DiscoveryService;
import org.apache.chemistry.opencmis.commons.api.MultiFilingService;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectService;
import org.apache.chemistry.opencmis.commons.api.PolicyService;
import org.apache.chemistry.opencmis.commons.api.RelationshipService;
import org.apache.chemistry.opencmis.commons.api.RepositoryService;
import org.apache.chemistry.opencmis.commons.api.VersioningService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CmisLocalSpi implements CmisSpiFactory, CmisSpi {

	private static Log log = LogFactory.getLog(CmisLocalSpi.class);

	private Session session;

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

		return this;
	}

	public AclService getAclService() {
		// TODO Auto-generated method stub
		return null;
	}

	public DiscoveryService getDiscoveryService() {
		// TODO Auto-generated method stub
		return null;
	}

	public MultiFilingService getMultiFilingService() {
		// TODO Auto-generated method stub
		return null;
	}

	public NavigationService getNavigationService() {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectService getObjectService() {
		// TODO Auto-generated method stub
		return null;
	}

	public PolicyService getPolicyService() {
		// TODO Auto-generated method stub
		return null;
	}

	public RelationshipService getRelationshipService() {
		// TODO Auto-generated method stub
		return null;
	}

	public RepositoryService getRepositoryService() {
		// TODO Auto-generated method stub
		return null;
	}

	public VersioningService getVersioningService() {
		// TODO Auto-generated method stub
		return null;
	}

	public void clearAllCaches() {
		// TODO Auto-generated method stub
	}

	public void clearRepositoryCache(String repositoryId) {
		// TODO Auto-generated method stub
	}

	public void close() {
		// TODO Auto-generated method stub
	}
}
