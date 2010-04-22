package org.apache.chemistry.opencmis.client.bindings.spi.local;

import java.math.BigInteger;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.api.DiscoveryService;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.Holder;
import org.apache.chemistry.opencmis.commons.api.ObjectList;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

public class DiscoveryServiceImpl extends AbstractLocalService implements DiscoveryService {

    /**
     * Constructor.
     */
    public DiscoveryServiceImpl(Session session, CmisServiceFactory factory) {
        setSession(session);
        setServiceFactory(factory);
    }

    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getContentChanges(repositoryId, changeLogToken, includeProperties, filter, includePolicyIds,
                    includeAcl, maxItems, extension);
        } finally {
            service.close();
        }
    }

    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.query(repositoryId, statement, searchAllVersions, includeAllowableActions,
                    includeRelationships, renditionFilter, maxItems, skipCount, extension);
        } finally {
            service.close();
        }
    }
}
