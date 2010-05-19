package org.apache.chemistry.opencmis.client.bindings.spi.local;

import java.math.BigInteger;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.spi.RelationshipService;

public class RelationshipServiceImpl extends AbstractLocalService implements RelationshipService {

    /**
     * Constructor.
     */
    public RelationshipServiceImpl(Session session, CmisServiceFactory factory) {
        setSession(session);
        setServiceFactory(factory);
    }

    public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes,
                    relationshipDirection, typeId, filter, includeAllowableActions, maxItems, skipCount, extension);
        } finally {
            service.close();
        }
    }
}
