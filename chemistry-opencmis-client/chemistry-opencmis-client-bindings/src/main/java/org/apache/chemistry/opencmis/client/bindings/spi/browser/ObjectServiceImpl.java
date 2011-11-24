/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.client.bindings.spi.browser;

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
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;

/**
 * Object Service Browser Binding client.
 */
public class ObjectServiceImpl extends AbstractBrowserBindingService implements ObjectService {

    /**
     * Constructor.
     */
    public ObjectServiceImpl(BindingSession session) {
        setSession(session);
    }
    
    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        // TODO Auto-generated method stub

    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        // TODO Auto-generated method stub

    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
        // TODO Auto-generated method stub

    }

    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        // TODO Auto-generated method stub

    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        // TODO Auto-generated method stub

    }

}
