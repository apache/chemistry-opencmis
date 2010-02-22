package org.apache.opencmis.client.runtime;

import java.io.Serializable;
import java.util.List;

import org.apache.opencmis.client.api.repository.RepositoryAclCapabilities;
import org.apache.opencmis.client.api.repository.RepositoryCapabilities;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.RepositoryInfoData;

public class RepositoryInfoImpl implements RepositoryInfo, Serializable {

	/**
	 * serialization
	 */
	private static final long serialVersionUID = -1297274972722405445L;

	/*
	 * provider data (serializable)
	 */
	private RepositoryInfoData riData;

	/*
	 * session (serializable)
	 */
	private PersistentSessionImpl session;

	public RepositoryInfoImpl(PersistentSessionImpl session,
			RepositoryInfoData riData) {
		this.riData = riData;
		this.session = session;
	}

	public boolean changesIncomplete() {
		return this.riData.changesIncomplete();
	}

	public RepositoryAclCapabilities getAclCapabilities() {
		throw new CmisRuntimeException("not implemented");
	}

	public RepositoryCapabilities getCapabilities() {
		throw new CmisRuntimeException("not implemented");
	}

	public List<BaseObjectTypeIds> getChangesOnType() {
		return this.riData.getChangesOnType();
	}

	public String getCmisVersionSupported() {
		return this.riData.getCmisVersionSupported();
	}

	public String getDescription() {
		return this.riData.getRepositoryDescription();
	}

	public String getId() {
		return this.riData.getRepositoryId();
	}

	public String getLatestChangeLogToken() {
		return this.riData.getLatestChangeLogToken();
	}

	public String getName() {
		return this.riData.getRepositoryName();
	}

	public String getPrincipalIdAnonymous() {
		return this.riData.getPrincipalAnonymous();
	}

	public String getPrincipalIdAnyone() {
		return this.riData.getPrincipalAnyone();
	}

	public String getProductName() {
		return this.riData.getProductName();

	}

	public String getProductVersion() {
		return this.getProductVersion();
	}

	public String getRootFolderId() {
		return this.riData.getRootFolderId();
	}

	public String getThinClientUri() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getVendorName() {
		return this.riData.getThinClientUri();
	}

}
