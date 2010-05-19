package org.apache.chemistry.opencmis.client.bindings.spi.local;

import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;

public class VersioningServiceImpl extends AbstractLocalService implements VersioningService {

    /**
     * Constructor.
     */
    public VersioningServiceImpl(Session session, CmisServiceFactory factory) {
        setSession(session);
        setServiceFactory(factory);
    }

    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.cancelCheckOut(repositoryId, objectId, extension);
        } finally {
            service.close();
        }
    }

    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.checkIn(repositoryId, objectId, major, properties, contentStream, checkinComment, policies,
                    addAces, removeAces, extension);
        } finally {
            service.close();
        }
    }

    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {
        CmisService service = getService(repositoryId);

        try {
            service.checkOut(repositoryId, objectId, extension, contentCopied);
        } finally {
            service.close();
        }
    }

    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getAllVersions(repositoryId, objectId, versionSeriesId, filter, includeAllowableActions,
                    extension);
        } finally {
            service.close();
        }
    }

    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter,
                    includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl,
                    extension);
        } finally {
            service.close();
        }
    }

    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getPropertiesOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter,
                    extension);
        } finally {
            service.close();
        }
    }
}
