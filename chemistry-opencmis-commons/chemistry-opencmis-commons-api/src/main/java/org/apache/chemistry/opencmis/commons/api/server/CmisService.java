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

/**
 * OpenCMIS server interface.
 */
public interface CmisService extends RepositoryService, NavigationService, ObjectService, VersioningService,
		DiscoveryService, MultiFilingService, RelationshipService, AclService, PolicyService {

	/**
	 * Creates a new document, folder or policy.
	 * 
	 * The property "cmis:objectTypeId" defines the type and implicitly the base
	 * type.
	 */
	ObjectData create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
			VersioningState versioningState, List<String> policies, ExtensionsData extension);

	/**
	 * Deletes an object or cancels a check out.
	 * 
	 * For the Web Services binding this is always an object deletion. For the
	 * AtomPub it depends on the referenced object. If it is a checked out
	 * document then the check out must be canceled. If the object is not a
	 * checked out document then the object must be deleted.
	 */
	void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
			ExtensionsData extension);

	/**
	 * Applies a new ACL to an object.
	 * 
	 * Since it is not possible to transmit an "add ACL" and a "remove ACL" via
	 * AtomPub, the merging has to be done the client side. The ACEs provided
	 * here is supposed to the new complete ACL.
	 */
	Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation);

	/**
	 * Returns the {@link ObjectInfo} of the given object id or
	 * <code>null</code> if no object info exists.
	 * 
	 * Only AtomPub requests will require object infos.
	 */
	ObjectInfo getObjectInfo(String objectId);

	/**
	 * Signals that this object will not be used anymore and resources can
	 * released.
	 */
	void close();
}
