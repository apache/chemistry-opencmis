package org.apache.chemistry.opencmis.client.bindings.spi.local;

import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.api.ObjectData;
import org.apache.chemistry.opencmis.commons.api.PolicyService;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.api.server.CmisServiceFactory;

public class PolicyServiceImpl extends AbstractLocalService implements PolicyService {

    /**
     * Constructor.
     */
    public PolicyServiceImpl(Session session, CmisServiceFactory factory) {
        setSession(session);
        setServiceFactory(factory);
    }

    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.applyPolicy(repositoryId, policyId, objectId, extension);
        } finally {
            service.close();
        }
    }

    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getAppliedPolicies(repositoryId, objectId, filter, extension);
        } finally {
            service.close();
        }
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.removePolicy(repositoryId, policyId, objectId, extension);
        } finally {
            service.close();
        }
    }
}
