/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.chemistry.opencmis.client.bindings.spi.local;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;

public class ObjectServiceImpl extends AbstractLocalService implements ObjectService {

    /**
     * Constructor.
     */
    public ObjectServiceImpl(BindingSession session, CmisServiceFactory factory) {
        setSession(session);
        setServiceFactory(factory);
    }

    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.createDocument(repositoryId, properties, folderId, contentStream, versioningState, policies,
                    addAces, removeAces, extension);
        } finally {
            service.close();
        }
    }

    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.createDocumentFromSource(repositoryId, sourceId, properties, folderId, versioningState,
                    policies, addAces, removeAces, extension);
        } finally {
            service.close();
        }
    }

    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.createFolder(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
        } finally {
            service.close();
        }
    }

    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.createPolicy(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
        } finally {
            service.close();
        }
    }

    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.createRelationship(repositoryId, properties, policies, addAces, removeAces, extension);
        } finally {
            service.close();
        }
    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.deleteContentStream(repositoryId, objectId, changeToken, extension);
        } finally {
            service.close();
        }
    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.deleteObject(repositoryId, objectId, allVersions, extension);
        } finally {
            service.close();
        }
    }

    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.deleteTree(repositoryId, folderId, allVersions, unfileObjects, continueOnFailure, extension);
        } finally {
            service.close();
        }
    }

    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getAllowableActions(repositoryId, objectId, extension);
        } finally {
            service.close();
        }
    }

    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getContentStream(repositoryId, objectId, streamId, offset, length, extension);
        } finally {
            service.close();
        }
    }

    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getObject(repositoryId, objectId, filter, includeAllowableActions, includeRelationships,
                    renditionFilter, includePolicyIds, includeAcl, extension);
        } finally {
            service.close();
        }
    }

    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getObjectByPath(repositoryId, path, filter, includeAllowableActions, includeRelationships,
                    renditionFilter, includePolicyIds, includeAcl, extension);
        } finally {
            service.close();
        }
    }

    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getProperties(repositoryId, objectId, filter, extension);
        } finally {
            service.close();
        }
    }

    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            return service.getRenditions(repositoryId, objectId, renditionFilter, maxItems, skipCount, extension);
        } finally {
            service.close();
        }
    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.moveObject(repositoryId, objectId, targetFolderId, sourceFolderId, extension);
        } finally {
            service.close();
        }
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.setContentStream(repositoryId, objectId, overwriteFlag, changeToken, contentStream, extension);
        } finally {
            service.close();
        }
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        CmisService service = getService(repositoryId);

        try {
            service.updateProperties(repositoryId, objectId, changeToken, properties, extension);
        } finally {
            service.close();
        }
    }
}
