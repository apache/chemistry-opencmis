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
package org.apache.opencmis.inmemory.storedobj.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.inmemory.FilterParser;
import org.apache.opencmis.inmemory.storedobj.api.Document;

/**
 * InMemory Stored Document A document is a stored object that has a path and (optional) content
 * 
 * @author Jens
 * 
 */

public class DocumentImpl extends AbstractMultiFilingImpl implements Document {
  private ContentStreamDataImpl fContent;

  private static final Log LOG = LogFactory.getLog(AbstractSingleFilingImpl.class.getName());

  DocumentImpl(ObjectStoreImpl objStore) { // visibility should be package
    super(objStore);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.opencmis.client.provider.spi.inmemory.IDocument#getContent()
   */
  public ContentStreamData getContent(long offset, long length) {
    if (null == fContent)
      return null;
    else if (offset<=0 && length<0)
      return fContent;
    else
      return fContent.getCloneWithLimits(offset, length);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.opencmis.client.provider.spi.inmemory.IDocument#setContent(org.opencmis.client.provider
   * .ContentStreamData)
   */
  public void setContent(ContentStreamData content, boolean mustPersist) {
    
    if (null == content) {
      fContent = null;
    } else {     
      fContent = new ContentStreamDataImpl();
      fContent.setFileName(content.getFilename());
      fContent.setMimeType(content.getMimeType());
      try {
        fContent.setContent(content.getStream());
      }
      catch (IOException e) {
        e.printStackTrace();
        throw new RuntimeException("Failed to get content from InputStream" , e);
      }
    }
  }

  public void fillProperties(Map<String, PropertyData<?>> properties, ProviderObjectFactory objFactory,
      List<String> requestedIds) {

    super.fillProperties(properties, objFactory, requestedIds);

    // fill the version related properties (versions should override this but the spec requires some
    // properties always to be set
    
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_IMMUTABLE, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_IMMUTABLE, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_IMMUTABLE, false));
    }
        
    // Set the content related properties
    if (null != fContent) {
      if (FilterParser.isContainedInFilter(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME, requestedIds)) {
        properties.put(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME, objFactory
            .createPropertyStringData(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME, fContent
                .getFileName()));
      }
      // omit: PropertyIds.CMIS_CONTENT_STREAM_ID
      if (FilterParser.isContainedInFilter(PropertyIds.CMIS_CONTENT_STREAM_LENGTH, requestedIds)) {
        properties.put(PropertyIds.CMIS_CONTENT_STREAM_LENGTH, objFactory
            .createPropertyIntegerData(PropertyIds.CMIS_CONTENT_STREAM_LENGTH, fContent.getLength()));
      }
      if (FilterParser.isContainedInFilter(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE, requestedIds)) {
        properties.put(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE, objFactory
            .createPropertyStringData(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE, fContent.getMimeType()));
      }      
    }
  }

  public boolean hasContent() {
    return null != fContent;
  }


}
