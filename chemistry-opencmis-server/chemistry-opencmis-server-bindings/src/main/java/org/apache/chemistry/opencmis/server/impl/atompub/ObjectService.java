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
package org.apache.chemistry.opencmis.server.impl.atompub;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.MimeHelper;
import org.apache.chemistry.opencmis.commons.impl.ReturnVersion;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.server.shared.ThresholdOutputStreamFactory;

/**
 * Object Service operations.
 */
public class ObjectService {

    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * Create.
     */
    public static class Create extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String folderId = getStringParameter(request, Constants.PARAM_ID);
            String sourceFolderId = getStringParameter(request, Constants.PARAM_SOURCE_FOLDER_ID);
            VersioningState versioningState = getEnumParameter(request, Constants.PARAM_VERSIONIG_STATE,
                    VersioningState.class);

            ThresholdOutputStreamFactory streamFactory = (ThresholdOutputStreamFactory) context
                    .get(CallContext.STREAM_FACTORY);
            AtomEntryParser parser = new AtomEntryParser(streamFactory);
            parser.setIgnoreAtomContentSrc(true); // needed for some clients
            parser.parse(request.getInputStream());

            String objectId = parser.getId();

            // execute
            String newObjectId = null;

            if (objectId == null) {
                // create
                ContentStream contentStream = parser.getContentStream();
                try {
                    newObjectId = service.create(repositoryId, parser.getProperties(), folderId, contentStream,
                            versioningState, parser.getPolicyIds(), null);
                } finally {
                    closeContentStream(contentStream);
                }
            } else {
                if ((sourceFolderId == null) || (sourceFolderId.trim().length() == 0)) {
                    // addObjectToFolder
                    service.addObjectToFolder(repositoryId, objectId, folderId, null, null);
                    newObjectId = objectId;
                } else {
                    // move
                    Holder<String> objectIdHolder = new Holder<String>(objectId);
                    service.moveObject(repositoryId, objectIdHolder, folderId, sourceFolderId, null);
                    newObjectId = objectIdHolder.getValue();
                }
            }

            ObjectInfo objectInfo = service.getObjectInfo(repositoryId, newObjectId);
            if (objectInfo == null) {
                throw new CmisRuntimeException("Object Info is missing!");
            }

            ObjectData object = objectInfo.getObject();
            if (object == null) {
                throw new CmisRuntimeException("Object is null!");
            }

            // set headers
            UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType(Constants.MEDIATYPE_ENTRY);
            response.setHeader("Location", compileUrl(baseUrl, RESOURCE_ENTRY, newObjectId));

            // write XML
            AtomEntry entry = new AtomEntry();
            entry.startDocument(response.getOutputStream(), getNamespaces(service));
            writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true,
                    context.getCmisVersion());
            entry.endDocument();
        }
    }

    /**
     * Create relationship.
     */
    public static class CreateRelationship extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            ThresholdOutputStreamFactory streamFactory = (ThresholdOutputStreamFactory) context
                    .get(CallContext.STREAM_FACTORY);
            AtomEntryParser parser = new AtomEntryParser(request.getInputStream(), streamFactory);

            // execute
            String newObjectId = service.createRelationship(repositoryId, parser.getProperties(),
                    parser.getPolicyIds(), null, null, null);

            ObjectInfo objectInfo = service.getObjectInfo(repositoryId, newObjectId);
            if (objectInfo == null) {
                throw new CmisRuntimeException("Object Info is missing!");
            }

            ObjectData object = objectInfo.getObject();
            if (object == null) {
                throw new CmisRuntimeException("Object is null!");
            }

            // set headers
            UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType(Constants.MEDIATYPE_ENTRY);
            response.setHeader("Location", compileUrl(baseUrl, RESOURCE_ENTRY, newObjectId));

            // write XML
            AtomEntry entry = new AtomEntry();
            entry.startDocument(response.getOutputStream(), getNamespaces(service));
            writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true,
                    context.getCmisVersion());
            entry.endDocument();
        }
    }

    /**
     * Delete object.
     */
    public static class DeleteObject extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String objectId = getStringParameter(request, Constants.PARAM_ID);
            Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);

            // execute
            service.deleteObject(repositoryId, objectId, allVersions, null);

            // set headers
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    /**
     * Delete content stream.
     */
    public static class DeleteContentStream extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String objectId = getStringParameter(request, Constants.PARAM_ID);
            String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);

            // execute
            service.deleteContentStream(repositoryId, new Holder<String>(objectId), changeToken == null ? null
                    : new Holder<String>(changeToken), null);

            // set headers
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    /**
     * Set or append content stream.
     */
    public static class SetOrAppendContentStream extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String objectId = getStringParameter(request, Constants.PARAM_ID);
            String changeToken = getStringParameter(request, Constants.PARAM_CHANGE_TOKEN);
            Boolean appendFlag = getBooleanParameter(request, Constants.PARAM_APPEND);
            Boolean overwriteFlag = getBooleanParameter(request, Constants.PARAM_OVERWRITE_FLAG);
            Boolean isLastChunk = getBooleanParameter(request, Constants.PARAM_IS_LAST_CHUNK);

            ContentStreamImpl contentStream = new ContentStreamImpl();
            contentStream.setStream(request.getInputStream());
            contentStream.setMimeType(request.getHeader("Content-Type"));
            String lengthStr = request.getHeader("Content-Length");
            if (lengthStr != null) {
                try {
                    contentStream.setLength(new BigInteger(lengthStr));
                } catch (NumberFormatException e) {
                }
            }
            String contentDisposition = request.getHeader(MimeHelper.CONTENT_DISPOSITION);
            if (contentDisposition != null) {
                contentStream.setFileName(MimeHelper.decodeContentDispositionFilename(contentDisposition));
            }

            // execute
            Holder<String> objectIdHolder = new Holder<String>(objectId);
            if (Boolean.TRUE.equals(appendFlag)) {
                service.appendContentStream(repositoryId, objectIdHolder, changeToken == null ? null
                        : new Holder<String>(changeToken), contentStream, (Boolean.TRUE.equals(isLastChunk) ? true
                        : false), null);
            } else {
                service.setContentStream(repositoryId, objectIdHolder, overwriteFlag, changeToken == null ? null
                        : new Holder<String>(changeToken), contentStream, null);
            }

            // set headers
            String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());
            String location = compileUrl(compileBaseUrl(request, repositoryId), RESOURCE_CONTENT, newObjectId);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setHeader("Content-Location", location);
            response.setHeader("Location", location);
        }
    }

    /**
     * Delete tree.
     */
    public static class DeleteTree extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String folderId = getStringParameter(request, Constants.PARAM_ID);
            Boolean allVersions = getBooleanParameter(request, Constants.PARAM_ALL_VERSIONS);
            UnfileObject unfileObjects = getEnumParameter(request, Constants.PARAM_UNFILE_OBJECTS, UnfileObject.class);
            Boolean continueOnFailure = getBooleanParameter(request, Constants.PARAM_CONTINUE_ON_FAILURE);

            // execute
            FailedToDeleteData ftd = service.deleteTree(repositoryId, folderId, allVersions, unfileObjects,
                    continueOnFailure, null);

            if ((ftd != null) && (ftd.getIds() != null) && (ftd.getIds().size() > 0)) {
                // print ids that could not be deleted
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("text/plain");

                PrintWriter pw = response.getWriter();

                pw.println("Failed to delete the following objects:");
                for (String id : ftd.getIds()) {
                    pw.println(id);
                }

                pw.flush();

                return;
            }

            // set headers
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
    }

    /**
     * getObject.
     */
    public static class GetObject extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String objectId = getStringParameter(request, Constants.PARAM_ID);
            ReturnVersion returnVersion = getEnumParameter(request, Constants.PARAM_RETURN_VERSION, ReturnVersion.class);
            String filter = getStringParameter(request, Constants.PARAM_FILTER);
            Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
            IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                    IncludeRelationships.class);
            String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
            Boolean includePolicyIds = getBooleanParameter(request, Constants.PARAM_POLICY_IDS);
            Boolean includeAcl = getBooleanParameter(request, Constants.PARAM_ACL);

            // execute
            ObjectData object = null;

            if ((returnVersion == ReturnVersion.LATEST) || (returnVersion == ReturnVersion.LASTESTMAJOR)) {
                object = service.getObjectOfLatestVersion(repositoryId, objectId, null,
                        returnVersion == ReturnVersion.LASTESTMAJOR, filter, includeAllowableActions,
                        includeRelationships, renditionFilter, includePolicyIds, includeAcl, null);
            } else {
                object = service.getObject(repositoryId, objectId, filter, includeAllowableActions,
                        includeRelationships, renditionFilter, includePolicyIds, includeAcl, null);
            }

            if (object == null) {
                throw new CmisRuntimeException("Object is null!");
            }

            ObjectInfo objectInfo = service.getObjectInfo(repositoryId, objectId);
            if (objectInfo == null) {
                throw new CmisRuntimeException("Object Info is missing!");
            }

            // set headers
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(Constants.MEDIATYPE_ENTRY);

            // write XML
            UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

            AtomEntry entry = new AtomEntry();
            entry.startDocument(response.getOutputStream(), getNamespaces(service));
            writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true,
                    context.getCmisVersion());
            entry.endDocument();
        }
    }

    /**
     * objectByPath URI template.
     */
    public static class GetObjectByPath extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String path = getStringParameter(request, Constants.PARAM_PATH);
            String filter = getStringParameter(request, Constants.PARAM_FILTER);
            Boolean includeAllowableActions = getBooleanParameter(request, Constants.PARAM_ALLOWABLE_ACTIONS);
            IncludeRelationships includeRelationships = getEnumParameter(request, Constants.PARAM_RELATIONSHIPS,
                    IncludeRelationships.class);
            String renditionFilter = getStringParameter(request, Constants.PARAM_RENDITION_FILTER);
            Boolean includePolicyIds = getBooleanParameter(request, Constants.PARAM_POLICY_IDS);
            Boolean includeAcl = getBooleanParameter(request, Constants.PARAM_ACL);

            // execute
            ObjectData object = service.getObjectByPath(repositoryId, path, filter, includeAllowableActions,
                    includeRelationships, renditionFilter, includePolicyIds, includeAcl, null);

            if (object == null) {
                throw new CmisRuntimeException("Object is null!");
            }

            ObjectInfo objectInfo = service.getObjectInfo(repositoryId, object.getId());
            if (objectInfo == null) {
                throw new CmisRuntimeException("Object Info is missing!");
            }

            // set headers
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(Constants.MEDIATYPE_ENTRY);

            // write XML
            UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

            AtomEntry entry = new AtomEntry();
            entry.startDocument(response.getOutputStream(), getNamespaces(service));
            writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true,
                    context.getCmisVersion());
            entry.endDocument();
        }
    }

    /**
     * Allowable Actions.
     */
    public static class GetAllowableActions extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String objectId = getStringParameter(request, Constants.PARAM_ID);

            // execute
            AllowableActions allowableActions = service.getAllowableActions(repositoryId, objectId, null);

            if (allowableActions == null) {
                throw new CmisRuntimeException("Allowable Actions is null!");
            }

            // set headers
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(Constants.MEDIATYPE_ALLOWABLEACTION);

            // write XML
            XMLStreamWriter writer = XMLUtils.createWriter(response.getOutputStream());
            XMLUtils.startXmlDocument(writer);
            XMLConverter.writeAllowableActions(writer, context.getCmisVersion(), true, allowableActions);
            XMLUtils.endXmlDocument(writer);
        }
    }

    /**
     * getContentStream.
     */
    public static class GetContentStream extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String objectId = getStringParameter(request, Constants.PARAM_ID);
            String streamId = getStringParameter(request, Constants.PARAM_STREAM_ID);

            BigInteger offset = context.getOffset();
            BigInteger length = context.getLength();

            // execute
            ContentStream content = service.getContentStream(repositoryId, objectId, streamId, offset, length, null);

            if ((content == null) || (content.getStream() == null)) {
                throw new CmisRuntimeException("Content stream is null!");
            }

            // set HTTP headers, if requested by the server implementation
            if (sendContentStreamHeaders(content, request, response)) {
                return;
            }

            String contentType = content.getMimeType();
            if (contentType == null) {
                contentType = Constants.MEDIATYPE_OCTETSTREAM;
            }

            // set headers
            if ((offset == null || offset.signum() == 0) && (length == null)) {
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            }
            response.setContentType(contentType);

            if (content.getFileName() != null) {
                response.setHeader(MimeHelper.CONTENT_DISPOSITION,
                        MimeHelper.encodeContentDisposition(MimeHelper.DISPOSITION_ATTACHMENT, content.getFileName()));
            }

            // send content
            InputStream in = new BufferedInputStream(content.getStream(), BUFFER_SIZE);
            OutputStream out = new BufferedOutputStream(response.getOutputStream());

            byte[] buffer = new byte[BUFFER_SIZE];
            int b;
            while ((b = in.read(buffer)) > -1) {
                out.write(buffer, 0, b);
            }

            in.close();
            out.flush();
        }
    }

    /**
     * UpdateProperties.
     */
    public static class UpdateProperties extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            // get parameters
            String objectId = getStringParameter(request, Constants.PARAM_ID);
            Boolean checkin = getBooleanParameter(request, Constants.PARAM_CHECK_IN);
            String checkinComment = getStringParameter(request, Constants.PARAM_CHECKIN_COMMENT);
            Boolean major = getBooleanParameter(request, Constants.PARAM_MAJOR);

            ThresholdOutputStreamFactory streamFactory = (ThresholdOutputStreamFactory) context
                    .get(CallContext.STREAM_FACTORY);
            AtomEntryParser parser = new AtomEntryParser(request.getInputStream(), streamFactory);

            // execute
            Holder<String> objectIdHolder = new Holder<String>(objectId);

            if ((checkin != null) && (checkin.booleanValue())) {
                ContentStream contentStream = parser.getContentStream();
                try {
                    service.checkIn(repositoryId, objectIdHolder, major, parser.getProperties(), contentStream,
                            checkinComment, parser.getPolicyIds(), null, null, null);
                } finally {
                    closeContentStream(contentStream);
                }
            } else {
                String changeToken = extractChangeToken(parser.getProperties());

                service.updateProperties(repositoryId, objectIdHolder, changeToken == null ? null : new Holder<String>(
                        changeToken), parser.getProperties(), null);
            }

            ObjectInfo objectInfo = service.getObjectInfo(repositoryId, objectIdHolder.getValue());
            if (objectInfo == null) {
                throw new CmisRuntimeException("Object Info is missing!");
            }

            ObjectData object = objectInfo.getObject();
            if (object == null) {
                throw new CmisRuntimeException("Object is null!");
            }

            // set headers
            UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);
            String location = compileUrl(baseUrl, RESOURCE_ENTRY, objectIdHolder.getValue());

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType(Constants.MEDIATYPE_ENTRY);
            response.setHeader("Content-Location", location);
            response.setHeader("Location", location);

            // write XML
            AtomEntry entry = new AtomEntry();
            entry.startDocument(response.getOutputStream(), getNamespaces(service));
            writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, true,
                    context.getCmisVersion());
            entry.endDocument();
        }

        /**
         * Gets the change token from a property set.
         */
        private String extractChangeToken(Properties properties) {
            if (properties == null) {
                return null;
            }

            Map<String, PropertyData<?>> propertiesMap = properties.getProperties();
            if (propertiesMap == null) {
                return null;
            }

            PropertyData<?> changeLogProperty = propertiesMap.get(PropertyIds.CHANGE_TOKEN);
            if (!(changeLogProperty instanceof PropertyString)) {
                return null;
            }

            return ((PropertyString) changeLogProperty).getFirstValue();
        }
    }

    /**
     * BulkUpdateProperties.
     */
    public static class BulkUpdateProperties extends AbstractAtomPubServiceCall {
        public void serve(CallContext context, CmisService service, String repositoryId, HttpServletRequest request,
                HttpServletResponse response) throws Exception {
            ThresholdOutputStreamFactory streamFactory = (ThresholdOutputStreamFactory) context
                    .get(CallContext.STREAM_FACTORY);
            AtomEntryParser parser = new AtomEntryParser(streamFactory);
            parser.parse(request.getInputStream());

            BulkUpdateImpl bulkUpdate = parser.getBulkUpdate();
            if (bulkUpdate == null) {
                throw new CmisInvalidArgumentException("Bulk update data is missing!");
            }

            List<BulkUpdateObjectIdAndChangeToken> result = service.bulkUpdateProperties(repositoryId,
                    bulkUpdate.getObjectIdAndChangeToken(), bulkUpdate.getProperties(),
                    bulkUpdate.getAddSecondaryTypeIds(), bulkUpdate.getRemoveSecondaryTypeIds(), null);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType(Constants.MEDIATYPE_FEED);

            // write XML
            AtomFeed feed = new AtomFeed();
            feed.startDocument(response.getOutputStream(), getNamespaces(service));
            feed.startFeed(true);

            // write basic Atom feed elements
            feed.writeFeedElements(null, null, null, "Bulk Update Properties",
                    new GregorianCalendar(TimeZone.getTimeZone("GMT")), null,
                    (result == null ? null : BigInteger.valueOf(result.size())));

            // write links
            UrlBuilder baseUrl = compileBaseUrl(request, repositoryId);

            feed.writeServiceLink(baseUrl.toString(), repositoryId);

            UrlBuilder selfLink = compileUrlBuilder(baseUrl, RESOURCE_BULK_UPDATE, null);
            feed.writeSelfLink(selfLink.toString(), null);

            // write entries
            if (result != null) {
                AtomEntry entry = new AtomEntry(feed.getWriter());
                for (BulkUpdateObjectIdAndChangeToken idAndToken : result) {
                    if ((idAndToken == null) || (idAndToken.getId() == null)) {
                        continue;
                    }

                    ObjectDataImpl object = new ObjectDataImpl();
                    PropertiesImpl properties = new PropertiesImpl();
                    object.setProperties(properties);

                    properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_ID, idAndToken.getId()));

                    if (idAndToken.getChangeToken() != null) {
                        properties.addProperty(new PropertyStringImpl(PropertyIds.CHANGE_TOKEN, idAndToken
                                .getChangeToken()));
                    }

                    writeObjectEntry(service, entry, object, null, repositoryId, null, null, baseUrl, false,
                            context.getCmisVersion());
                }
            }

            // we are done
            feed.endFeed();
            feed.endDocument();
        }
    }

}
