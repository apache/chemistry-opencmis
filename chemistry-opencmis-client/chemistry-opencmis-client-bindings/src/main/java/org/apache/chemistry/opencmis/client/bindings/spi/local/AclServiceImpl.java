package org.apache.chemistry.opencmis.client.bindings.spi.local;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.api.Acl;
import org.apache.chemistry.opencmis.commons.api.AclService;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;

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
