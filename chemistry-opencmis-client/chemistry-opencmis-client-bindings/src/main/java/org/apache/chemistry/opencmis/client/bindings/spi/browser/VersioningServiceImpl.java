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

import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.VersioningService;

/**
 * Versioning Service Browser Binding client.
 */
public class VersioningServiceImpl extends AbstractBrowserBindingService implements VersioningService {

    /**
     * Constructor.
     */
    public VersioningServiceImpl(BindingSession session) {
        setSession(session);
    }

    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {
        // TODO Auto-generated method stub

    }

    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        // TODO Auto-generated method stub

    }

    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        // TODO Auto-generated method stub

    }

    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        // TODO Auto-generated method stub
        return null;
    }

}
