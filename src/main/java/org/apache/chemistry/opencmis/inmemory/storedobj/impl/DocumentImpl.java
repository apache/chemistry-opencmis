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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.ConfigConstants;
import org.apache.chemistry.opencmis.inmemory.ConfigurationSettings;
import org.apache.chemistry.opencmis.inmemory.FilterParser;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * InMemory Stored Document A document is a stored object that has a path and
 * (optional) content
 *
 * @author Jens
 *
 */

public class DocumentImpl extends AbstractMultiFilingImpl implements Document {
    private ContentStreamDataImpl fContent;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSingleFilingImpl.class.getName());
    private final Long MAX_CONTENT_SIZE_KB = ConfigurationSettings.getConfigurationValueAsLong(ConfigConstants.MAX_CONTENT_SIZE_KB);

    public static final int IMG_HEIGHT = 100;
    public static final int IMG_WIDTH = 100;
    public static final String RENDITION_MIME_TYPE = "image/jpeg";
    public static final String RENDITION_SUFFIX = "-rendition"
            ;

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
            fContent.setLastModified(getModifiedAt());
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
        if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_FILE_NAME, requestedIds)) {
            properties.put(PropertyIds.CONTENT_STREAM_FILE_NAME, objFactory.createPropertyStringData(
                    PropertyIds.CONTENT_STREAM_FILE_NAME, null != fContent ? fContent.getFileName() : (String)null) );
        }
        if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_ID, requestedIds)) {
            properties.put(PropertyIds.CONTENT_STREAM_ID, objFactory.createPropertyStringData(
                    PropertyIds.CONTENT_STREAM_ID, (String) null));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_LENGTH, requestedIds)) {
            properties.put(PropertyIds.CONTENT_STREAM_LENGTH, objFactory.createPropertyIntegerData(
                    PropertyIds.CONTENT_STREAM_LENGTH, null != fContent ? fContent.getBigLength() : null));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.CONTENT_STREAM_MIME_TYPE, requestedIds)) {
            properties.put(PropertyIds.CONTENT_STREAM_MIME_TYPE, objFactory.createPropertyStringData(
                    PropertyIds.CONTENT_STREAM_MIME_TYPE, null != fContent ? fContent.getMimeType() : (String)null) );
        }
        
        // Spec requires versioning properties even for unversioned documents
        // overwrite the version related properties
        if (FilterParser.isContainedInFilter(PropertyIds.VERSION_SERIES_ID, requestedIds)) {
            properties.put(PropertyIds.VERSION_SERIES_ID, objFactory.createPropertyIdData(
                    PropertyIds.VERSION_SERIES_ID, (String)null));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, requestedIds)) {
            properties.put(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, objFactory.createPropertyBooleanData(
                    PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, requestedIds)) {
            properties.put(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, objFactory.createPropertyStringData(
                    PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, (String)null));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, requestedIds)) {
            properties.put(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, objFactory.createPropertyIdData(
                    PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, (String)null));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.IS_LATEST_VERSION, requestedIds)) {
            properties.put(PropertyIds.IS_LATEST_VERSION, objFactory.createPropertyBooleanData(
                    PropertyIds.IS_LATEST_VERSION, true));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.IS_MAJOR_VERSION, requestedIds)) {
            properties.put(PropertyIds.IS_MAJOR_VERSION, objFactory.createPropertyBooleanData(
                    PropertyIds.IS_MAJOR_VERSION, true));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.IS_LATEST_MAJOR_VERSION, requestedIds)) {
            properties.put(PropertyIds.IS_LATEST_MAJOR_VERSION, objFactory.createPropertyBooleanData(
                    PropertyIds.IS_LATEST_MAJOR_VERSION, true));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.CHECKIN_COMMENT, requestedIds)) {
            properties.put(PropertyIds.CHECKIN_COMMENT, objFactory.createPropertyStringData(
                    PropertyIds.CHECKIN_COMMENT, (String )null));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.VERSION_LABEL, requestedIds)) {
            properties.put(PropertyIds.VERSION_LABEL, objFactory.createPropertyStringData(PropertyIds.VERSION_LABEL,
                    (String) null));
        }
        
    }

    public boolean hasContent() {
        return null != fContent;
    }

    @Override
    public List<RenditionData> getRenditions(String renditionFilter,
            long maxItems, long skipCount) {

        String tokenizer = "[\\s;]";
        if (null==renditionFilter)
            renditionFilter = "*";
        String[] formats = renditionFilter.split(tokenizer);
        boolean isImageRendition = testRenditionFilterForImage(formats);
        if (isImageRendition && fContent != null && hasRendition(null)) {
            List<RenditionData> renditions = new ArrayList<RenditionData>(1);
            RenditionDataImpl rendition = new RenditionDataImpl();
            rendition.setBigHeight(BigInteger.valueOf(IMG_HEIGHT));
            rendition.setBigWidth(BigInteger.valueOf(IMG_WIDTH));
            rendition.setKind("cmis:thumbnail");
            rendition.setMimeType(RENDITION_MIME_TYPE);
            rendition.setRenditionDocumentId(getId());
            rendition.setStreamId(getId() + RENDITION_SUFFIX);
            rendition.setBigLength(BigInteger.valueOf(-1L));
            renditions.add(rendition);
            return renditions;
        } else {
            return null;
        }
    }

    @Override
    public ContentStream getRenditionContent(String streamId, long offset, long length) {        
        ImageThumbnailGenerator generator = new ImageThumbnailGenerator(getContent(0L, -1L).getStream());
        return generator.getRendition(IMG_WIDTH, IMG_HEIGHT);
    }
    
    @Override
    public boolean hasRendition(String user) {
        return null != fContent && fContent.getMimeType().startsWith("image/");
    }


    protected boolean testRenditionFilterForImage(String[] formats) {
        if (formats.length == 1 && null != formats[0] && formats[0].equals("cmis:none"))
            return false;
        else
            return arrayContainsString(formats, "*")  || arrayContainsString(formats, "image/*") 
                || arrayContainsString(formats, "image/jpeg") ;
    }

    private boolean arrayContainsString(String[] formats, String val) {
        for (String s : formats) {
            if (val.equals(s))
                return true;            
        }
        return false;
    }


}
