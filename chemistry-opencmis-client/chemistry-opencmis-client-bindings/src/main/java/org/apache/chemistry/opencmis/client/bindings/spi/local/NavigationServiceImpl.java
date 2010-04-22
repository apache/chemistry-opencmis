package org.apache.chemistry.opencmis.client.bindings.spi.local;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.NavigationService;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.api.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.api.ObjectList;
import org.apache.chemistry.opencmis.commons.api.ObjectParentData;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

public class NavigationServiceImpl extends AbstractLocalService implements NavigationService {

    /**
     * Constructor.
     */
    public NavigationServiceImpl(Session session, CmisServiceFactory factory) {
        setSession(session);
        setServiceFactory(factory);
    }

    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getCheckedOutDocs(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                    includeRelationships, renditionFilter, maxItems, skipCount, extension);
        } finally {
            service.close();
        }
    }

    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getChildren(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                    includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension);
        } finally {
            service.close();
        }
    }

    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions,
                    includeRelationships, renditionFilter, includePathSegment, extension);
        } finally {
            service.close();
        }
    }

    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getFolderParent(repositoryId, folderId, filter, extension);
        } finally {
            service.close();
        }
    }

    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getFolderTree(repositoryId, folderId, depth, filter, includeAllowableActions,
                    includeRelationships, renditionFilter, includePathSegment, extension);
        } finally {
            service.close();
        }
    }

    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getObjectParents(repositoryId, objectId, filter, includeAllowableActions,
                    includeRelationships, renditionFilter, includeRelativePathSegment, extension);
        } finally {
            service.close();
        }
    }
}
