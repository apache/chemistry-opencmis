package org.apache.chemistry.opencmis.commons.impl.server;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.api.Acl;
import org.apache.chemistry.opencmis.commons.api.AllowableActions;
import org.apache.chemistry.opencmis.commons.api.ContentStream;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.api.Holder;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.api.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.api.ObjectList;
import org.apache.chemistry.opencmis.commons.api.ObjectParentData;
import org.apache.chemistry.opencmis.commons.api.Properties;
import org.apache.chemistry.opencmis.commons.api.PropertyData;
import org.apache.chemistry.opencmis.commons.api.RenditionData;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.api.TypeDefinition;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;

public abstract class AbstractCmisService implements CmisService {

	private Map<String, ObjectInfo> objectInfoMap;

	public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
		RepositoryInfo result = null;

		List<RepositoryInfo> repositories = getRepositoryInfos(extension);
		if (repositories != null) {
			for (RepositoryInfo ri : repositories) {
				if (ri.getId().equals(repositoryId)) {
					result = ri;
					break;
				}
			}
		}

		if (result == null) {
			throw new CmisObjectNotFoundException("Repository '" + repositoryId + "' does not exist!");
		}

		return result;
	}

	public abstract List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension);

	public abstract TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension);

	public abstract TypeDefinitionList getTypeChildren(String repositoryId, String typeId,
			Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

	public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
			Boolean includePropertyDefinitions, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public abstract ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

	public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
			String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
			String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public abstract List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			Boolean includeRelativePathSegment, ExtensionsData extension);

	public ObjectData create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
			VersioningState versioningState, List<String> policies, ExtensionsData extension) {
		// check properties
		if (properties == null || properties.getProperties() == null) {
			throw new CmisInvalidArgumentException("Properties must be set!");
		}

		// check object type id
		PropertyData<?> baseTypeIdProperty = properties.getProperties().get(PropertyIds.OBJECT_TYPE_ID);
		if (baseTypeIdProperty == null || !(baseTypeIdProperty.getFirstValue() instanceof String)) {
			throw new CmisInvalidArgumentException("Property '" + PropertyIds.OBJECT_TYPE_ID + "' must be set!");
		}

		// get the type
		String baseTypeId = baseTypeIdProperty.getFirstValue().toString();
		TypeDefinition baseType = getTypeDefinition(repositoryId, baseTypeId, null);

		// create object
		String newId = null;
		switch (baseType.getBaseTypeId()) {
		case CMIS_DOCUMENT:
			newId = createDocument(repositoryId, properties, folderId, contentStream, versioningState, policies, null,
					null, extension);
			break;
		case CMIS_FOLDER:
			newId = createFolder(repositoryId, properties, folderId, policies, null, null, extension);
			break;
		case CMIS_RELATIONSHIP:
			newId = createRelationship(repositoryId, properties, policies, null, null, extension);
			break;
		case CMIS_POLICY:
			newId = createPolicy(repositoryId, properties, folderId, policies, null, null, extension);
			break;
		}

		// check new object id
		if (newId == null) {
			throw new CmisRuntimeException("Creation failed!");
		}

		// return the new object
		return getObject(repositoryId, newId, null, Boolean.TRUE, IncludeRelationships.BOTH, null, Boolean.TRUE,
				Boolean.TRUE, null);
	}

	public String createDocument(String repositoryId, Properties properties, String folderId,
			ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
			Acl removeAces, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
			String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
			ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
			Acl addAces, Acl removeAces, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
			Acl addAces, Acl removeAces, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
			Acl removeAces, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
			ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
			ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
			UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
			BigInteger length, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public abstract ObjectData getObject(String repositoryId, String objectId, String filter,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension);

	public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
			IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
			Boolean includeAcl, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
			BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
			ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
			Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
			Properties properties, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
			ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
			ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
			Holder<Boolean> contentCopied) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
			Boolean includeAllowableActions, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public List<ObjectData> getAllVersions(String repositoryId, String versionSeriesId, String filter,
			Boolean includeAllowableActions, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public ObjectData getObjectOfLatestVersion(String repositoryId, String versionSeriesId, Boolean major,
			String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
			String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public Properties getPropertiesOfLatestVersion(String repositoryId, String versionSeriesId, Boolean major,
			String filter, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
			String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
			Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
			BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
			ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
			RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
			BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
			AclPropagation aclPropagation, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
			ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
		throw new CmisNotSupportedException("Not supported!");
	}

	public ObjectInfo getObjectInfo(String objectId) {
		return objectInfoMap.get(objectId);
	}

	public void addObjectInfo(ObjectInfo objectInfo) {
		if (objectInfoMap == null) {
			objectInfoMap = new HashMap<String, ObjectInfo>();
		}

		if (objectInfo != null && objectInfo.getId() != null) {
			objectInfoMap.put(objectInfo.getId(), objectInfo);
		}
	}

	public void close() {
	}
}
