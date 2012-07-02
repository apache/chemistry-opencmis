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
package org.apache.chemistry.opencmis.commons.spi;

import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;

/**
 * Versioning Service interface.
 * 
 * <p>
 * <em>
 * See CMIS 1.0 specification for details on the operations, parameters,
 * exceptions and the domain model.
 * </em>
 * </p>
 */
public interface VersioningService {
    /**
     * Create a private working copy of the document.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension, Holder<Boolean> contentCopied);

    /**
     * Reverses the effect of a check-out.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension);

    /**
     * Checks-in the private working copy (PWC) document.
     * 
     * The stream in <code>contentStream</code> is consumed but not closed by
     * this method.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension);

    /**
     * Get the latest document object in the version series.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId, Boolean major,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension);

    /**
     * Get a subset of the properties for the latest document object in the
     * version series.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension);

    /**
     * Returns the list of all document objects in the specified version series,
     * sorted by the property "cmis:creationDate" descending.
     * 
     * Either the <code>objectId</code> or the <code>versionSeriesId</code>
     * parameter must be set.
     * 
     * @param repositoryId
     *            the identifier for the repository
     * @param objectId
     *            the identifier for the object
     * @param versionSeriesId
     *            the identifier for the object
     * @param filter
     *            <em>(optional)</em> a comma-separated list of query names that
     *            defines which properties must be returned by the repository
     *            (default is repository specific)
     * @param includeAllowableActions
     *            <em>(optional)</em> if <code>true</code>, then the repository
     *            must return the allowable actions for the objects (default is
     *            <code>false</code>)
     * 
     * @return the complete version history of the version series
     */
    List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension);
}
