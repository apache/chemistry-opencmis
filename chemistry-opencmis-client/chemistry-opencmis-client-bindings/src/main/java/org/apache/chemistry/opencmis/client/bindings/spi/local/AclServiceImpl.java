package org.apache.chemistry.opencmis.client.bindings.spi.local;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.spi.AclService;

public class AclServiceImpl extends AbstractLocalService implements AclService {

    /**
     * Constructor.
     */
    public AclServiceImpl(Session session, CmisServiceFactory factory) {
        setSession(session);
        setServiceFactory(factory);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.applyAcl(repositoryId, objectId, addAces, removeAces, aclPropagation, extension);
        } finally {
            service.close();
        }
    }

    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getAcl(repositoryId, objectId, onlyBasicPermissions, extension);
        } finally {
            service.close();
        }
    }
}
