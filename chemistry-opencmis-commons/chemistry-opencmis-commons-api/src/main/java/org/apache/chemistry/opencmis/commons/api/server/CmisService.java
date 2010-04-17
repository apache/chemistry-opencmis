package org.apache.chemistry.opencmis.commons.api.server;

import java.util.List;

import org.apache.chemistry.opencmis.commons.api.Acl;
import org.apache.chemistry.opencmis.commons.api.AclService;
import org.apache.chemistry.opencmis.commons.api.ContentStream;
import org.apache.chemistry.opencmis.commons.api.DiscoveryService;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.MultiFilingService;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.ObjectService;
import org.apache.chemistry.opencmis.commons.api.PolicyService;
import org.apache.chemistry.opencmis.commons.api.Properties;
import org.apache.chemistry.opencmis.commons.api.RelationshipService;
import org.apache.chemistry.opencmis.commons.api.RepositoryService;
import org.apache.chemistry.opencmis.commons.api.VersioningService;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;

public interface CmisService extends RepositoryService, NavigationService, ObjectService, VersioningService,
		DiscoveryService, MultiFilingService, RelationshipService, AclService, PolicyService {

	ObjectData create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
			VersioningState versioningState, List<String> policies, ExtensionsData extension);

	void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
			ExtensionsData extension);

	Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation);

	ObjectInfo getObjectInfo(String objectId);

	void close();
}
