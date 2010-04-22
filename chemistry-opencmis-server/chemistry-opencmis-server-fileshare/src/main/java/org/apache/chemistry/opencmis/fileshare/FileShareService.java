package org.apache.chemistry.opencmis.fileshare;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.apache.chemistry.opencmis.commons.api.RenditionData;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.api.TypeDefinition;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.api.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;

/**
 * FileShare service implementation.
 */
public class FileShareService extends AbstractCmisService {

    private RepositoryMap repositoryMap;
    private CallContext context;

    /**
     * Constructor.
     */
    public FileShareService(RepositoryMap repositoryMap) {
        this.repositoryMap = repositoryMap;
    }

    // --- context ---

    public void setCallContext(CallContext context) {
        this.context = context;
    }

    public CallContext getCallContext() {
        return context;
    }

    public FileShareRepository getRepository() {
        return repositoryMap.getRepository(getCallContext().getRepositoryId());
    }

    // --- repository service ---

    @Override
    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        return getRepository().getRepositoryInfo(getCallContext());
    }

    @Override
    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        List<RepositoryInfo> result = new ArrayList<RepositoryInfo>();

        for (FileShareRepository fsr : repositoryMap.getRepositories()) {
            result.add(fsr.getRepositoryInfo(getCallContext()));
        }

        return result;
    }

    @Override
    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return getRepository().getTypesChildren(getCallContext(), typeId, includePropertyDefinitions, maxItems,
                skipCount);
    }

    @Override
    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return getRepository().getTypeDefinition(getCallContext(), typeId);
    }

    @Override
    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        return getRepository().getTypesDescendants(getCallContext(), typeId, depth, includePropertyDefinitions);
    }

    // --- navigation service ---

    @Override
    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return getRepository().getChildren(getCallContext(), folderId, filter, includeAllowableActions,
                includePathSegment, maxItems, skipCount, null);
    }

    @Override
    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        return getRepository().getDescendants(getCallContext(), folderId, depth, filter, includeAllowableActions,
                includePathSegment, null, false);
    }

    @Override
    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        return getRepository().getFolderParent(getCallContext(), folderId, filter, null);
    }

    @Override
    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        return getRepository().getDescendants(getCallContext(), folderId, depth, filter, includeAllowableActions,
                includePathSegment, null, true);
    }

    @Override
    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {
        return getRepository().getObjectParents(getCallContext(), objectId, filter, includeAllowableActions,
                includeRelativePathSegment, null);
    }

    @Override
    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        ObjectListImpl result = new ObjectListImpl();
        result.setHasMoreItems(false);
        result.setNumItems(BigInteger.ZERO);
        List<ObjectData> emptyList = Collections.emptyList();
        result.setObjects(emptyList);

        return result;
    }

    // --- object service ---

    @Override
    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies, ExtensionsData extension) {
        ObjectData object = getRepository().create(getCallContext(), properties, folderId, contentStream,
                versioningState, null);

        return object.getId();
    }

    @Override
    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        return getRepository().createDocument(getCallContext(), properties, folderId, contentStream, versioningState);
    }

    @Override
    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        return getRepository().createDocumentFromSource(getCallContext(), sourceId, properties, folderId,
                versioningState);
    }

    @Override
    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        return getRepository().createFolder(getCallContext(), properties, folderId);
    }

    @Override
    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        getRepository().setContentStream(getCallContext(), objectId, true, null);
    }

    @Override
    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {
        getRepository().deleteObject(getCallContext(), objectId);
    }

    @Override
    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        return getRepository().deleteTree(getCallContext(), folderId, continueOnFailure);
    }

    @Override
    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        return getRepository().getAllowableActions(getCallContext(), objectId);
    }

    @Override
    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        return getRepository().getContentStream(getCallContext(), objectId, offset, length);
    }

    @Override
    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        return getRepository().getObject(getCallContext(), objectId, filter, includeAllowableActions, includeAcl, null);
    }

    @Override
    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        return getRepository().getObjectByPath(getCallContext(), path, filter, includeAllowableActions, includeAcl,
                null);
    }

    @Override
    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        ObjectData object = getRepository().getObject(getCallContext(), objectId, filter, false, false, null);
        return object.getProperties();
    }

    @Override
    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return Collections.emptyList();
    }

    @Override
    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        getRepository().moveObject(getCallContext(), objectId, targetFolderId, null);
    }

    @Override
    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        getRepository().setContentStream(getCallContext(), objectId, overwriteFlag, contentStream);
    }

    @Override
    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        getRepository().updateProperties(getCallContext(), objectId, properties, null);
    }

    // --- versioning service ---

    @Override
    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        ObjectData theVersion = getRepository().getObject(getCallContext(), versionSeriesId, filter,
                includeAllowableActions, false, null);

        return Collections.singletonList(theVersion);
    }

    @Override
    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        return getRepository().getObject(getCallContext(), versionSeriesId, filter, includeAllowableActions,
                includeAcl, null);
    }

    @Override
    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        ObjectData object = getRepository().getObject(getCallContext(), versionSeriesId, filter, false, false, null);

        return object.getProperties();
    }

    // --- ACL service ---

    @Override
    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        return getRepository().getAcl(getCallContext(), objectId);
    }
}
