package org.apache.chemistry.opencmis.commons.impl.server;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
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
import org.apache.chemistry.opencmis.commons.api.PropertyBoolean;
import org.apache.chemistry.opencmis.commons.api.PropertyData;
import org.apache.chemistry.opencmis.commons.api.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.api.PropertyId;
import org.apache.chemistry.opencmis.commons.api.PropertyInteger;
import org.apache.chemistry.opencmis.commons.api.PropertyString;
import org.apache.chemistry.opencmis.commons.api.RenditionData;
import org.apache.chemistry.opencmis.commons.api.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.api.TypeDefinition;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.api.server.RenditionInfo;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
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
    private boolean addObjectInfos = true;

    // --- repository service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is required. Convenience implementation is present.</li>
     * </ul>
     */
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

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is required.</li>
     * </ul>
     */
    public abstract List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension);

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is required.</li>
     * </ul>
     */
    public abstract TypeDefinitionList getTypeChildren(String repositoryId, String typeId,
            Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is required.</li>
     * </ul>
     */
    public abstract TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension);

    // --- navigation service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is required.</li>
     * <li>Object infos should contain the folder and all returned children.</li>
     * </ul>
     */
    public abstract ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the folder and all returned descendants.</li>
     * </ul>
     */
    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the folder and all returned descendants.</li>
     * </ul>
     */
    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is required.</li>
     * <li>Object infos should contain the object and all returned parents.</li>
     * </ul>
     */
    public abstract List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension);

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the returned parent folder.</li>
     * </ul>
     */
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the folder and the returned objects.</li>
     * </ul>
     */
    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    // --- object service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub</li>
     * <li>Implementation is optional. Convenience implementation is present.</li>
     * <li>Object infos should contain the newly created object.</li>
     * </ul>
     */
    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
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
        case CMIS_POLICY:
            newId = createPolicy(repositoryId, properties, folderId, policies, null, null, extension);
            break;
        }

        // check new object id
        if (newId == null) {
            throw new CmisRuntimeException("Creation failed!");
        }

        // return the new object id
        return newId;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the newly created object.</li>
     * </ul>
     */
    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is required.</li>
     * <li>Object infos should contain the returned object.</li>
     * </ul>
     */
    public abstract ObjectData getObject(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension);

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the returned object.</li>
     * </ul>
     */
    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the updated object.</li>
     * </ul>
     */
    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the moved object.</li>
     * </ul>
     */
    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional. Convenience implementation is present.</li>
     * </ul>
     */
    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
        deleteObjectOrCancelCheckOut(repositoryId, objectId, allVersions, extension);
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    // --- versioning service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the checked out object.</li>
     * </ul>
     */
    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the checked in object.</li>
     * </ul>
     */
    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the returned object.</li>
     * </ul>
     */
    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the returned objects.</li>
     * </ul>
     */
    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    // --- discovery service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the returned objects.</li>
     * </ul>
     */
    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    // --- multi filing service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the added object.</li>
     * </ul>
     */
    public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
            ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the removed object.</li>
     * </ul>
     */
    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    // --- relationship service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the object and the returned relationship
     * objects.</li>
     * </ul>
     */
    public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    // --- ACL service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    // --- policy service ---

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the applied policy object.</li>
     * </ul>
     */
    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * <li>Object infos should contain the returned policy objects.</li>
     * </ul>
     */
    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub, Web Services, Local</li>
     * <li>Implementation is optional.</li>
     * </ul>
     */
    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        throw new CmisNotSupportedException("Not supported!");
    }

    // --- server specific ---

    /**
     * Returns the object info map.
     */
    private Map<String, ObjectInfo> getObjectInfoMap() {
        if (objectInfoMap == null) {
            objectInfoMap = new HashMap<String, ObjectInfo>();
        }

        return objectInfoMap;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * <b>Implementation Hints:</b>
     * <ul>
     * <li>Bindings: AtomPub</li>
     * <li>If the object info is not found, the object info will be assembled.
     * To do that the repository info, the object, the object parent, the object
     * history and the base type definitions will be fetched. If you want to
     * change this behavior, override this method.</li>
     * </ul>
     */
    public ObjectInfo getObjectInfo(String repositoryId, String objectId) {
        objectInfoMap = getObjectInfoMap();

        ObjectInfo info = objectInfoMap.get(objectId);
        if (info == null) {
            // object info has not been found -> create one
            ObjectInfoImpl infoImpl = new ObjectInfoImpl();

            try {
                // switch off object info collection to avoid side effects
                addObjectInfos = false;

                // get the object
                ObjectData object = getObject(repositoryId, objectId, null, Boolean.TRUE, IncludeRelationships.BOTH,
                        "*", Boolean.TRUE, Boolean.FALSE, null);

                // if the object has no properties, stop here
                if (object.getProperties() == null || object.getProperties().getProperties() == null) {
                    throw new Exception("No properties!");
                }

                // get the repository info
                RepositoryInfo repositoryInfo = getRepositoryInfo(repositoryId, null);

                // general properties
                infoImpl.setObject(object);
                infoImpl.setId(object.getId());
                infoImpl.setName(getStringProperty(object, PropertyIds.NAME));
                infoImpl.setCreatedBy(getStringProperty(object, PropertyIds.CREATED_BY));
                infoImpl.setCreationDate(getDateTimeProperty(object, PropertyIds.CREATED_BY));
                infoImpl.setLastModificationDate(getDateTimeProperty(object, PropertyIds.LAST_MODIFICATION_DATE));
                infoImpl.setTypeId(getIdProperty(object, PropertyIds.OBJECT_TYPE_ID));
                infoImpl.setBaseType(object.getBaseTypeId());

                // versioning
                infoImpl.setIsCurrentVersion(object.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT);
                infoImpl.setWorkingCopyId(null);
                infoImpl.setWorkingCopyOriginalId(null);

                infoImpl.setVersionSeriesId(getStringProperty(object, PropertyIds.VERSION_SERIES_ID));
                if (infoImpl.getVersionSeriesId() != null) {
                    Boolean isLatest = getBooleanProperty(object, PropertyIds.IS_LATEST_VERSION);
                    infoImpl.setIsCurrentVersion(isLatest == null ? true : isLatest.booleanValue());

                    Boolean isCheckedOut = getBooleanProperty(object, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
                    if (isCheckedOut != null && isCheckedOut.booleanValue()) {
                        infoImpl.setWorkingCopyId(getIdProperty(object, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID));

                        // get latest version
                        List<ObjectData> versions = getAllVersions(repositoryId, objectId, infoImpl
                                .getVersionSeriesId(), PropertyIds.OBJECT_ID, Boolean.FALSE, null);
                        if (versions != null && versions.size() > 0) {
                            infoImpl.setWorkingCopyOriginalId(versions.get(0).getId());
                        }
                    }
                }

                // content
                String fileName = getStringProperty(object, PropertyIds.CONTENT_STREAM_FILE_NAME);
                String mimeType = getStringProperty(object, PropertyIds.CONTENT_STREAM_MIME_TYPE);
                String streamId = getStringProperty(object, PropertyIds.CONTENT_STREAM_ID);
                BigInteger length = getIntegerProperty(object, PropertyIds.CONTENT_STREAM_LENGTH);
                boolean hasContent = fileName != null || mimeType != null || streamId != null || length != null;
                if (hasContent) {
                    infoImpl.setHasContent(hasContent);
                    infoImpl.setContentType(mimeType);
                    infoImpl.setFileName(fileName);
                } else {
                    infoImpl.setHasContent(false);
                    infoImpl.setContentType(null);
                    infoImpl.setFileName(null);
                }

                // parent
                List<ObjectParentData> parents = getObjectParents(repositoryId, objectId, PropertyIds.OBJECT_ID,
                        Boolean.FALSE, IncludeRelationships.NONE, "cmis:none", Boolean.FALSE, null);
                infoImpl.setHasParent(parents.size() > 0);

                // policies and relationships
                infoImpl.setSupportsRelationships(false);
                infoImpl.setSupportsPolicies(false);

                TypeDefinitionList baseTypesList = getTypeChildren(repositoryId, null, Boolean.FALSE, BigInteger
                        .valueOf(4), BigInteger.ZERO, null);
                for (TypeDefinition type : baseTypesList.getList()) {
                    if (BaseTypeId.CMIS_RELATIONSHIP.value().equals(type.getId())) {
                        infoImpl.setSupportsRelationships(true);
                    } else if (BaseTypeId.CMIS_POLICY.value().equals(type.getId())) {
                        infoImpl.setSupportsPolicies(true);
                    }
                }

                // renditions
                infoImpl.setRenditionInfos(null);

                List<RenditionData> renditions = object.getRenditions();
                if (renditions != null && renditions.size() > 0) {
                    List<RenditionInfo> renditionInfos = new ArrayList<RenditionInfo>();

                    for (RenditionData rendition : renditions) {
                        RenditionInfoImpl renditionInfo = new RenditionInfoImpl();
                        renditionInfo.setId(rendition.getStreamId());
                        renditionInfo.setKind(rendition.getKind());
                        renditionInfo.setContentType(rendition.getMimeType());
                        renditionInfo.setTitle(rendition.getTitle());
                        renditionInfo.setLength(rendition.getBigLength());

                        renditionInfos.add(renditionInfo);
                    }

                    infoImpl.setRenditionInfos(renditionInfos);
                }

                // relationships
                infoImpl.setRelationshipSourceIds(null);
                infoImpl.setRelationshipTargetIds(null);

                List<ObjectData> relationships = object.getRelationships();
                if (relationships != null && relationships.size() > 0) {
                    List<String> sourceIds = new ArrayList<String>();
                    List<String> targetIds = new ArrayList<String>();

                    for (ObjectData relationship : relationships) {
                        String sourceId = getIdProperty(relationship, PropertyIds.SOURCE_ID);
                        String targetId = getIdProperty(relationship, PropertyIds.TARGET_ID);

                        if (object.getId().equals(sourceId)) {
                            sourceIds.add(relationship.getId());
                        }
                        if (object.getId().equals(targetId)) {
                            targetIds.add(relationship.getId());
                        }
                    }

                    if (sourceIds.size() > 0) {
                        infoImpl.setRelationshipSourceIds(sourceIds);
                    }
                    if (targetIds.size() > 0) {
                        infoImpl.setRelationshipTargetIds(targetIds);
                    }
                }

                // global settings
                infoImpl.setHasAcl(false);
                infoImpl.setSupportsDescendants(false);
                infoImpl.setSupportsFolderTree(false);

                RepositoryCapabilities capabilities = repositoryInfo.getCapabilities();
                if (capabilities != null) {
                    infoImpl.setHasAcl(capabilities.getAclCapability() == CapabilityAcl.DISCOVER
                            || capabilities.getAclCapability() == CapabilityAcl.MANAGE);
                    if (object.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
                        infoImpl.setSupportsDescendants(Boolean.TRUE.equals(capabilities.isGetDescendantsSupported()));
                        infoImpl.setSupportsFolderTree(Boolean.TRUE.equals(capabilities.isGetFolderTreeSupported()));
                    }
                }

                // switch on object info collection
                addObjectInfos = true;

                // add object info
                addObjectInfo(infoImpl);
                info = infoImpl;
            } catch (Exception e) {
                info = null;
            } finally {
                addObjectInfos = true;
            }
        }

        return info;
    }

    /**
     * Adds an object info.
     */
    public void addObjectInfo(ObjectInfo objectInfo) {
        if (!addObjectInfos) {
            return;
        }

        if (objectInfo != null && objectInfo.getId() != null) {
            getObjectInfoMap().put(objectInfo.getId(), objectInfo);
        }
    }

    /**
     * Clears the object info map.
     */
    public void clearObjectInfos() {
        objectInfoMap = null;
    }

    public void close() {
        clearObjectInfos();
    }

    // --- helpers ---

    private String getStringProperty(ObjectData object, String name) {
        PropertyData<?> property = object.getProperties().getProperties().get(name);
        if (property instanceof PropertyString) {
            return ((PropertyString) property).getFirstValue();
        }
        return null;
    }

    private String getIdProperty(ObjectData object, String name) {
        PropertyData<?> property = object.getProperties().getProperties().get(name);
        if (property instanceof PropertyId) {
            return ((PropertyId) property).getFirstValue();
        }
        return null;
    }

    private GregorianCalendar getDateTimeProperty(ObjectData object, String name) {
        PropertyData<?> property = object.getProperties().getProperties().get(name);
        if (property instanceof PropertyDateTime) {
            return ((PropertyDateTime) property).getFirstValue();
        }
        return null;
    }

    private Boolean getBooleanProperty(ObjectData object, String name) {
        PropertyData<?> property = object.getProperties().getProperties().get(name);
        if (property instanceof PropertyBoolean) {
            return ((PropertyBoolean) property).getFirstValue();
        }
        return null;
    }

    private BigInteger getIntegerProperty(ObjectData object, String name) {
        PropertyData<?> property = object.getProperties().getProperties().get(name);
        if (property instanceof PropertyInteger) {
            return ((PropertyInteger) property).getFirstValue();
        }
        return null;
    }
}
