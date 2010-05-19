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

import java.math.BigInteger;
import java.util.List;

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

/**
 * Object Service interface.
 * 
 * <p>
 * <em>
 * See CMIS 1.0 specification for details on the operations, parameters,
 * exceptions and the domain model.
 * </em>
 * </p>
 */
public interface ObjectService {

    /**
     * Creates a document object of the specified type (given by the
     * cmis:objectTypeId property) in the (optionally) specified location.
     * 
     * @param repositoryId
     *            the identifier for the repository
     * @param properties
     *            the property values that MUST be applied to the newly created
     *            document object
     * @param folderId
     *            <em>(optional)</em> if specified, the identifier for the
     *            folder that MUST be the parent folder for the newly created
     *            document object
     * @param contentStream
     *            <em>(optional)</em> the content stream that MUST be stored for
     *            the newly created document object
     * @param versioningState
     *            <em>(optional)</em> specifies what the versioning state of the
     *            newly created object MUST be (default is
     *            {@link VersioningState#MAJOR})
     * @param policies
     *            <em>(optional)</em> a list of policy IDs that MUST be applied
     *            to the newly created document object
     * @param addAces
     *            <em>(optional)</em> a list of ACEs that MUST be added to the
     *            newly created document object, either using the ACL from
     *            <code>folderId</code> if specified, or being applied if no
     *            <code>folderId</code> is specified
     * @param removeAces
     *            <em>(optional)</em> a list of ACEs that MUST be removed from
     *            the newly created document object, either using the ACL from
     *            <code>folderId</code> if specified, or being ignored if no
     *            <code>folderId</code> is specified
     */
    String createDocument(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension);

    /**
     * Creates a document object as a copy of the given source document in the
     * (optionally) specified location.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    String createDocumentFromSource(String repositoryId, String sourceId, Properties properties, String folderId,
            VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension);

    /**
     * Creates a folder object of the specified type (given by the
     * cmis:objectTypeId property) in the specified location.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension);

    /**
     * Creates a relationship object of the specified type (given by the
     * cmis:objectTypeId property).
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension);

    /**
     * Creates a policy object of the specified type (given by the
     * cmis:objectTypeId property).
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension);

    /**
     * Gets the list of allowable actions for an object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension);

    /**
     * Gets the specified information for the object specified by id.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension);

    /**
     * Gets the list of properties for an object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension);

    /**
     * Gets the list of associated renditions for the specified object. Only
     * rendition attributes are returned, not rendition stream.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension);

    /**
     * Gets the specified information for the object specified by path.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension);

    /**
     * Gets the content stream for the specified document object, or gets a
     * rendition stream for a specified rendition of a document or folder
     * object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension);

    /**
     * Updates properties of the specified object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension);

    /**
     * Moves the specified file-able object from one folder to another.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension);

    /**
     * Deletes the specified object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     * @param objectId
     *            the identifier for the object
     * @param allVersions
     *            <em>(optional)</em> If <code>true</code> then delete all
     *            versions of the document, otherwise delete only the document
     *            object specified (default is <code>true</code>)
     */
    void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension);

    /**
     * Deletes the specified folder object and all of its child- and
     * descendant-objects.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension);

    /**
     * Sets the content stream for the specified document object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension);

    /**
     * Deletes the content stream for the specified document object.
     * 
     * @param repositoryId
     *            the identifier for the repository
     */
    void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension);
}
