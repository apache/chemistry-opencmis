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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomAllowableActions;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomElement;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomEntry;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomFeed;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomLink;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConnectionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.MimeHelper;
import org.apache.chemistry.opencmis.commons.impl.ReturnVersion;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.commons.spi.ObjectService;

/**
 * Object Service AtomPub client.
 */
public class ObjectServiceImpl extends AbstractAtomPubService implements ObjectService {

    /**
     * Constructor.
     */
    public ObjectServiceImpl(BindingSession session) {
        setSession(session);
    }

    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        checkCreateProperties(properties);

        // find the link
        String link = null;

        if (folderId == null) {
            // Creation of unfiled objects via AtomPub is not defined in the
            // CMIS 1.0 specification. This implementation follow the CMIS 1.1
            // draft and POSTs the document to the Unfiled collection.

            link = loadCollection(repositoryId, Constants.COLLECTION_UNFILED);

            if (link == null) {
                throw new CmisObjectNotFoundException("Unknown repository or unfiling not supported!");
            }
        } else {
            link = loadLink(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);

            if (link == null) {
                throwLinkException(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
            }
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_VERSIONIG_STATE, versioningState);

        // set up object and writer
        ObjectDataImpl object = new ObjectDataImpl();
        object.setProperties(properties);
        // TODO
        // object.setPolicyIds(convertPolicyIds(policies));

        String mediaType = null;
        InputStream stream = null;

        if (contentStream != null) {
            mediaType = contentStream.getMimeType();
            stream = contentStream.getStream();
        }

        final AtomEntryWriter entryWriter = new AtomEntryWriter(object, mediaType, stream);

        // post the new folder object
        HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                entryWriter.write(out);
            }
        });

        // parse the response
        AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

        // handle ACL modifications
        handleAclModifications(repositoryId, entry, addAces, removeAces);

        return entry.getId();
    }

    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addACEs, Acl removeACEs,
            ExtensionsData extension) {
        throw new CmisNotSupportedException("createDocumentFromSource is not supported by the AtomPub binding!");
    }

    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        checkCreateProperties(properties);

        // find the link
        String link = loadLink(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);

        if (link == null) {
            throwLinkException(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
        }

        UrlBuilder url = new UrlBuilder(link);

        // set up object and writer
        ObjectDataImpl object = new ObjectDataImpl();
        object.setProperties(properties);
        // TODO
        // object.setPolicyIds(convertPolicyIds(policies));

        final AtomEntryWriter entryWriter = new AtomEntryWriter(object);

        // post the new folder object
        HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                entryWriter.write(out);
            }
        });

        // parse the response
        AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

        // handle ACL modifications
        handleAclModifications(repositoryId, entry, addAces, removeAces);

        return entry.getId();
    }

    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        checkCreateProperties(properties);

        // find the link
        String link = null;

        if (folderId == null) {
            // Creation of unfiled objects via AtomPub is not defined in the
            // CMIS 1.0 specification. This implementation follow the CMIS 1.1
            // draft and POSTs the policy to the Unfiled collection.

            link = loadCollection(repositoryId, Constants.COLLECTION_UNFILED);

            if (link == null) {
                throw new CmisObjectNotFoundException("Unknown repository or unfiling not supported!");
            }
        } else {
            link = loadLink(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);

            if (link == null) {
                throwLinkException(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
            }
        }

        UrlBuilder url = new UrlBuilder(link);

        // set up object and writer
        ObjectDataImpl object = new ObjectDataImpl();
        object.setProperties(properties);
        // TODO
        // object.setPolicyIds(convertPolicyIds(policies));

        final AtomEntryWriter entryWriter = new AtomEntryWriter(object);

        // post the new folder object
        HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                entryWriter.write(out);
            }
        });

        // parse the response
        AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

        // handle ACL modifications
        handleAclModifications(repositoryId, entry, addAces, removeAces);

        return entry.getId();
    }

    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        checkCreateProperties(properties);

        // find source id
        PropertyData<?> sourceIdProperty = properties.getProperties().get(PropertyIds.SOURCE_ID);
        if (!(sourceIdProperty instanceof PropertyId)) {
            throw new CmisInvalidArgumentException("Source Id is not set!");
        }

        String sourceId = ((PropertyId) sourceIdProperty).getFirstValue();
        if (sourceId == null) {
            throw new CmisInvalidArgumentException("Source Id is not set!");
        }

        // find the link
        String link = loadLink(repositoryId, sourceId, Constants.REL_RELATIONSHIPS, Constants.MEDIATYPE_FEED);

        if (link == null) {
            throwLinkException(repositoryId, sourceId, Constants.REL_RELATIONSHIPS, Constants.MEDIATYPE_FEED);
        }

        UrlBuilder url = new UrlBuilder(link);

        // set up object and writer
        ObjectDataImpl object = new ObjectDataImpl();
        object.setProperties(properties);
        // TODO
        // object.setPolicyIds(convertPolicyIds(policies));
        final AtomEntryWriter entryWriter = new AtomEntryWriter(object);

        // post the new folder object
        HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                entryWriter.write(out);
            }
        });

        // parse the response
        AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

        // handle ACL modifications
        handleAclModifications(repositoryId, entry, addAces, removeAces);

        return entry.getId();
    }

    @SuppressWarnings("rawtypes")
    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        // we need an object id
        if ((objectId == null) || (objectId.getValue() == null) || (objectId.getValue().length() == 0)) {
            throw new CmisInvalidArgumentException("Object id must be set!");
        }

        // find the link
        String link = loadLink(repositoryId, objectId.getValue(), Constants.REL_SELF, Constants.MEDIATYPE_ENTRY);

        if (link == null) {
            throwLinkException(repositoryId, objectId.getValue(), Constants.REL_SELF, Constants.MEDIATYPE_ENTRY);
        }

        UrlBuilder url = new UrlBuilder(link);
        if (changeToken != null) {
            url.addParameter(Constants.PARAM_CHANGE_TOKEN, changeToken.getValue());
        }

        // set up object and writer
        ObjectDataImpl object = new ObjectDataImpl();
        object.setProperties(properties);
        // TODO
        // object.setPolicyIds(convertPolicyIds(policies));

        final AtomEntryWriter entryWriter = new AtomEntryWriter(object);

        // update
        HttpUtils.Response resp = put(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                entryWriter.write(out);
            }
        });

        // parse new entry
        AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

        // we expect a CMIS entry
        if (entry.getId() == null) {
            throw new CmisConnectionException("Received Atom entry is not a CMIS entry!");
        }

        // set object id
        objectId.setValue(entry.getId());

        if (changeToken != null) {
            changeToken.setValue(null); // just in case
        }

        lockLinks();
        try {
            // clean up cache
            removeLinks(repositoryId, entry.getId());

            // walk through the entry
            for (AtomElement element : entry.getElements()) {
                if (element.getObject() instanceof AtomLink) {
                    addLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
                } else if (element.getObject() instanceof ObjectData) {
                    // extract new change token
                    if (changeToken != null) {
                        object = (ObjectDataImpl) element.getObject();

                        if (object.getProperties() != null) {
                            for (PropertyData property : object.getProperties().getPropertyList()) {
                                if (PropertyIds.CHANGE_TOKEN.equals(property.getId())
                                        && (property instanceof PropertyString)) {

                                    PropertyString changeTokenProperty = (PropertyString) property;
                                    if (changeTokenProperty.getFirstValue() != null && changeTokenProperty.getFirstValue().length() > 0) {
                                        changeToken.setValue(changeTokenProperty.getFirstValue());
                                    }

                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            unlockLinks();
        }
    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {

        // find the link
        String link = loadLink(repositoryId, objectId, Constants.REL_SELF, Constants.MEDIATYPE_ENTRY);

        if (link == null) {
            throwLinkException(repositoryId, objectId, Constants.REL_SELF, Constants.MEDIATYPE_ENTRY);
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_ALL_VERSIONS, allVersions);

        delete(url);
    }

    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {

        // find the link
        String link = loadLink(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_DESCENDANTS);

        if (link == null) {
            link = loadLink(repositoryId, folderId, Constants.REL_FOLDERTREE, Constants.MEDIATYPE_DESCENDANTS);
        }

        if (link == null) {
            throwLinkException(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_DESCENDANTS);
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_ALL_VERSIONS, allVersions);
        url.addParameter(Constants.PARAM_UNFILE_OBJECTS, unfileObjects);
        url.addParameter(Constants.PARAM_CONTINUE_ON_FAILURE, continueOnFailure);

        // make the call
        HttpUtils.Response resp = HttpUtils.invokeDELETE(url, getSession());

        // check response code
        if (resp.getResponseCode() == 200 || resp.getResponseCode() == 202 || resp.getResponseCode() == 204) {
            return new FailedToDeleteDataImpl();
        }

        // If the server returned an internal server error, get the remaining
        // children of the folder. We only retrieve the first level, since
        // getDescendants() is not supported by all repositories.
        if (resp.getResponseCode() == 500) {
            link = loadLink(repositoryId, folderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);

            if (link != null) {
                url = new UrlBuilder(link);
                // we only want the object ids
                url.addParameter(Constants.PARAM_FILTER, "cmis:objectId");
                url.addParameter(Constants.PARAM_ALLOWABLE_ACTIONS, false);
                url.addParameter(Constants.PARAM_RELATIONSHIPS, IncludeRelationships.NONE);
                url.addParameter(Constants.PARAM_RENDITION_FILTER, "cmis:none");
                url.addParameter(Constants.PARAM_PATH_SEGMENT, false);
                // 1000 children should be enough to indicate a problem
                url.addParameter(Constants.PARAM_MAX_ITEMS, 1000);
                url.addParameter(Constants.PARAM_SKIP_COUNT, 0);

                // read and parse
                resp = read(url);
                AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

                // prepare result
                FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
                List<String> ids = new ArrayList<String>();
                result.setIds(ids);

                // get the children ids
                for (AtomEntry entry : feed.getEntries()) {
                    ids.add(entry.getId());
                }

                return result;
            }
        }

        throw convertStatusCode(resp.getResponseCode(), resp.getResponseMessage(), resp.getErrorContent(), null);
    }

    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        // find the link
        String link = loadLink(repositoryId, objectId, Constants.REL_ALLOWABLEACTIONS,
                Constants.MEDIATYPE_ALLOWABLEACTION);

        if (link == null) {
            throwLinkException(repositoryId, objectId, Constants.REL_ALLOWABLEACTIONS,
                    Constants.MEDIATYPE_ALLOWABLEACTION);
        }

        UrlBuilder url = new UrlBuilder(link);

        // read and parse
        HttpUtils.Response resp = read(url);
        AtomAllowableActions allowableActions = parse(resp.getStream(), AtomAllowableActions.class);

        return allowableActions.getAllowableActions();
    }

    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        ContentStreamImpl result = new ContentStreamImpl();

        // find the link
        String link = null;
        if (streamId != null) {
            // use the alternate link per spec
            link = loadLink(repositoryId, objectId, Constants.REL_ALTERNATE, streamId);
            if (link != null) {
                streamId = null; // we have a full URL now
            }
        }
        if (link == null) {
            link = loadLink(repositoryId, objectId, AtomPubParser.LINK_REL_CONTENT, null);
        }

        if (link == null) {
            throw new CmisConstraintException("No content stream");
        }

        UrlBuilder url = new UrlBuilder(link);
        // using the content URL and adding a streamId param
        // is not spec-compliant
        url.addParameter(Constants.PARAM_STREAM_ID, streamId);

        // get the content
        HttpUtils.Response resp = HttpUtils.invokeGET(url, getSession(), offset, length);

        // check response code
        if ((resp.getResponseCode() != 200) && (resp.getResponseCode() != 206)) {
            throw convertStatusCode(resp.getResponseCode(), resp.getResponseMessage(), resp.getErrorContent(), null);
        }

        result.setFileName(null);
        result.setLength(resp.getContentLength());
        result.setMimeType(resp.getContentTypeHeader());
        result.setStream(resp.getStream());

        return result;
    }

    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeACL, ExtensionsData extension) {

        return getObjectInternal(repositoryId, IdentifierType.ID, objectId, ReturnVersion.THIS, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeACL, extension);
    }

    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeACL, ExtensionsData extension) {

        return getObjectInternal(repositoryId, IdentifierType.PATH, path, ReturnVersion.THIS, filter,
                includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeACL, extension);
    }

    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        ObjectData object = getObjectInternal(repositoryId, IdentifierType.ID, objectId, ReturnVersion.THIS, filter,
                Boolean.FALSE, IncludeRelationships.NONE, "cmis:none", Boolean.FALSE, Boolean.FALSE, extension);

        return object.getProperties();
    }

    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        ObjectData object = getObjectInternal(repositoryId, IdentifierType.ID, objectId, ReturnVersion.THIS,
                PropertyIds.OBJECT_ID, Boolean.FALSE, IncludeRelationships.NONE, renditionFilter, Boolean.FALSE,
                Boolean.FALSE, extension);

        List<RenditionData> result = object.getRenditions();
        if (result == null) {
            result = Collections.emptyList();
        }

        return result;
    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        if ((objectId == null) || (objectId.getValue() == null) || (objectId.getValue().length() == 0)) {
            throw new CmisInvalidArgumentException("Object id must be set!");
        }

        if ((targetFolderId == null) || (targetFolderId.length() == 0) || (sourceFolderId == null)
                || (sourceFolderId.length() == 0)) {
            throw new CmisInvalidArgumentException("Source and target folder must be set!");
        }

        // find the link
        String link = loadLink(repositoryId, targetFolderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);

        if (link == null) {
            throwLinkException(repositoryId, targetFolderId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
        }

        UrlBuilder url = new UrlBuilder(link);
        url.addParameter(Constants.PARAM_SOURCE_FOLDER_ID, sourceFolderId);

        // set up object and writer
        final AtomEntryWriter entryWriter = new AtomEntryWriter(createIdObject(objectId.getValue()));

        // post move request
        HttpUtils.Response resp = post(url, Constants.MEDIATYPE_ENTRY, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                entryWriter.write(out);
            }
        });

        // parse the response
        AtomEntry entry = parse(resp.getStream(), AtomEntry.class);

        objectId.setValue(entry.getId());
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        // we need an object id
        if ((objectId == null) || (objectId.getValue() == null)) {
            throw new CmisInvalidArgumentException("Object ID must be set!");
        }

        // we need content
        if ((contentStream == null) || (contentStream.getStream() == null) || (contentStream.getMimeType() == null)) {
            throw new CmisInvalidArgumentException("Content must be set!");
        }

        // find the link
        String link = loadLink(repositoryId, objectId.getValue(), Constants.REL_EDITMEDIA, null);

        if (link == null) {
            throwLinkException(repositoryId, objectId.getValue(), Constants.REL_EDITMEDIA, null);
        }

        UrlBuilder url = new UrlBuilder(link);
        if (changeToken != null) {
            url.addParameter(Constants.PARAM_CHANGE_TOKEN, changeToken.getValue());
        }
        url.addParameter(Constants.PARAM_OVERWRITE_FLAG, overwriteFlag);

        final InputStream stream = contentStream.getStream();

        // Content-Disposition header for the filename
        Map<String, String> headers = null;
        if (contentStream.getFileName() != null) {
            headers = Collections
                    .singletonMap(
                            MimeHelper.CONTENT_DISPOSITION,
                            MimeHelper.encodeContentDisposition(MimeHelper.DISPOSITION_ATTACHMENT,
                                    contentStream.getFileName()));
        }

        // send content
        HttpUtils.Response resp = put(url, contentStream.getMimeType(), headers, new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                int b;
                byte[] buffer = new byte[4096];

                while ((b = stream.read(buffer)) > -1) {
                    out.write(buffer, 0, b);
                }

                stream.close();
            }
        });

        // check response code further
        if ((resp.getResponseCode() != 200) && (resp.getResponseCode() != 201) && (resp.getResponseCode() != 204)) {
            throw convertStatusCode(resp.getResponseCode(), resp.getResponseMessage(), resp.getErrorContent(), null);
        }

        objectId.setValue(null);
        if (changeToken != null) {
            changeToken.setValue(null);
        }
    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        // we need an object id
        if ((objectId == null) || (objectId.getValue() == null)) {
            throw new CmisInvalidArgumentException("Object ID must be set!");
        }

        // find the link
        String link = loadLink(repositoryId, objectId.getValue(), Constants.REL_EDITMEDIA, null);

        if (link == null) {
            throwLinkException(repositoryId, objectId.getValue(), Constants.REL_EDITMEDIA, null);
        }

        UrlBuilder url = new UrlBuilder(link);
        if (changeToken != null) {
            url.addParameter(Constants.PARAM_CHANGE_TOKEN, changeToken.getValue());
        }

        delete(url);

        objectId.setValue(null);
        if (changeToken != null) {
            changeToken.setValue(null);
        }
    }

    // ---- internal ----

    private static void checkCreateProperties(Properties properties) {
        if ((properties == null) || (properties.getProperties() == null)) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        if (!properties.getProperties().containsKey(PropertyIds.OBJECT_TYPE_ID)) {
            throw new CmisInvalidArgumentException("Property " + PropertyIds.OBJECT_TYPE_ID + " must be set!");
        }

        if (properties.getProperties().containsKey(PropertyIds.OBJECT_ID)) {
            throw new CmisInvalidArgumentException("Property " + PropertyIds.OBJECT_ID + " must not be set!");
        }
    }

    /**
     * Handles ACL modifications of newly created objects.
     */
    private void handleAclModifications(String repositoryId, AtomEntry entry, Acl addAces, Acl removeAces) {
        if (!isAclMergeRequired(addAces, removeAces)) {
            return;
        }

        Acl originalAces = getAclInternal(repositoryId, entry.getId(), Boolean.FALSE, null);

        if (originalAces != null) {
            // merge and update ACL
            Acl newACL = mergeAcls(originalAces, addAces, removeAces);
            if (newACL != null) {
                updateAcl(repositoryId, entry.getId(), newACL, null);
            }
        }
    }
}
