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
package org.apache.chemistry.opencmis.inmemory.storedobj.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.ConfigConstants;
import org.apache.chemistry.opencmis.inmemory.ConfigurationSettings;
import org.apache.chemistry.opencmis.inmemory.FilterParser;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * InMemory Stored Document A document is a stored object that has a path and
 * (optional) content
 *
 * @author Jens
 *
 */

public class DocumentImpl extends AbstractMultiFilingImpl implements Document {
    private ContentStreamDataImpl fContent;

    private static final Log LOG = LogFactory.getLog(AbstractSingleFilingImpl.class.getName());
    private final Long MAX_CONTENT_SIZE_KB = ConfigurationSettings.getConfigurationValueAsLong(ConfigConstants.MAX_CONTENT_SIZE_KB);

    DocumentImpl(ObjectStoreImpl objStore) { // visibility should be package
        super(objStore);
    }

    public ContentStream getContent(long offset, long length) {
        if (null == fContent) {
            return null;
        } else if (offset <= 0 && length < 0) {
            return fContent;
        } else {
            return fContent.getCloneWithLimits(offset, length);
        }
    }

    public void setContent(ContentStream content, boolean mustPersist) {
        if (null == content) {
            fContent = null;
        } else {
            fContent = new ContentStreamDataImpl(MAX_CONTENT_SIZE_KB == null ? 0 : MAX_CONTENT_SIZE_KB);
            String fileName = content.getFileName();
            if (null == fileName || fileName.length() <= 0) {
                fileName = getName(); // use name of document as fallback
            }
            fContent.setFileName(fileName);
            String mimeType = content.getMimeType();
            if (null == mimeType || mimeType.length() <= 0) {
                mimeType = "application/octet-stream"; // use as fallback
            }
            fContent.setMimeType(mimeType);
            try {
                fContent.setContent(content.getStream());
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to get content from InputStream", e);
            }
        }
    }

    @Override
    public void fillProperties(Map<String, PropertyData<?>> properties, BindingsObjectFactory objFactory,
            List<String> requestedIds) {

        super.fillProperties(properties, objFactory, requestedIds);

        // fill the version related properties (versions should override this
        // but the spec requires some
        // properties always to be set

        if (FilterParser.isContainedInFilter(PropertyIds.IS_IMMUTABLE, requestedIds)) {
            properties.put(PropertyIds.IS_IMMUTABLE, objFactory.createPropertyBooleanData(PropertyIds.IS_IMMUTABLE,
                    false));
        }

        // Set the content related properties
        if (null != fContent) {
            if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_FILE_NAME, requestedIds)) {
                properties.put(PropertyIds.CONTENT_STREAM_FILE_NAME, objFactory.createPropertyStringData(
                        PropertyIds.CONTENT_STREAM_FILE_NAME, fContent.getFileName()));
            }
            // omit: PropertyIds.CMIS_CONTENT_STREAM_ID
            if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_LENGTH, requestedIds)) {
                properties.put(PropertyIds.CONTENT_STREAM_LENGTH, objFactory.createPropertyIntegerData(
                        PropertyIds.CONTENT_STREAM_LENGTH, fContent.getBigLength()));
            }
            if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_MIME_TYPE, requestedIds)) {
                properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, objFactory.createPropertyStringData(
                        PropertyIds.CONTENT_STREAM_MIME_TYPE, fContent.getMimeType()));
            }
        }
    }

    public boolean hasContent() {
        return null != fContent;
    }

}
