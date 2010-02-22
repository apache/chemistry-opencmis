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

public class DocumentImpl extends AbstractPathImpl implements Document{
  private ContentStreamDataImpl fContent;

  private static final Log LOG = LogFactory.getLog(AbstractPathImpl.class.getName());

  DocumentImpl(ObjectStoreImpl objStore) { // visibility should be package
    super(objStore);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.opencmis.client.provider.spi.inmemory.IDocument#getContent()
   */
  public ContentStreamData getContent(long offset, long length) {
    if (offset<=0 && length<0)
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
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_LATEST_VERSION, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_LATEST_VERSION, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_LATEST_VERSION, true));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_MAJOR_VERSION, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_MAJOR_VERSION, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_MAJOR_VERSION, true));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION, true));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_VERSION_SERIES_ID, requestedIds)) {
      // return id of document itself
      properties.put(PropertyIds.CMIS_VERSION_SERIES_ID, objFactory.createPropertyIdData(PropertyIds.CMIS_VERSION_SERIES_ID, getId()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT, requestedIds)) {
      properties.put(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT, objFactory.createPropertyBooleanData(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT, false));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY, requestedIds)) {
      properties.put(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY, objFactory.createPropertyStringData(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY, (String)null));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID, requestedIds)) {
      properties.put(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID, objFactory.createPropertyIdData(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID, (String)null));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_CHECKIN_COMMENT, requestedIds)) {
      properties.put(PropertyIds.CMIS_CHECKIN_COMMENT, objFactory.createPropertyStringData(PropertyIds.CMIS_CHECKIN_COMMENT, (String)null));
    }
    
    // optional:
//    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_VERSION_LABEL, requestedIds)) {
//      properties.add(objFactory.createPropertyStringData(PropertyIds.CMIS_VERSION_LABEL, ""));
//    }
    
    
  }

}
