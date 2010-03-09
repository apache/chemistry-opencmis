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
package org.apache.opencmis.client.runtime;

import java.io.Serializable;

import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.client.api.Rendition;
import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.commons.provider.ContentStreamData;

/**
 * Implementation of <code>Rendition</code>.
 */
public class RenditionImpl implements Rendition, Serializable {

  private static final long serialVersionUID = 1L;

  private Session session;
  private String objectId;
  private String streamId;
  private String renditionDocumentId;
  private String kind;
  private long length;
  private String mimetype;
  private String title;
  private int height;
  private int width;

  /**
   * Constructor.
   */
  public RenditionImpl(Session session, String objectId, String streamId,
      String renditionDocumentId, String kind, long length, String mimetype, String title,
      int height, int width) {
    this.session = session;
    this.objectId = objectId;
    this.streamId = streamId;
    this.renditionDocumentId = renditionDocumentId;
    this.kind = kind;
    this.mimetype = mimetype;
    this.title = title;
    this.height = height;
    this.width = width;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Rendition#getKind()
   */
  public String getKind() {
    return this.kind;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Rendition#getLength()
   */
  public long getLength() {
    return this.length;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Rendition#getMimeType()
   */
  public String getMimeType() {
    return this.mimetype;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Rendition#getTitle()
   */
  public String getTitle() {
    return this.title;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Rendition#getHeight()
   */
  public int getHeight() {
    return this.height;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Rendition#getWidth()
   */
  public int getWidth() {
    return this.width;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Rendition#getRenditionDocument()
   */
  public Document getRenditionDocument() {
    return getRenditionDocument(session.getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.Rendition#getRenditionDocument(org.apache.opencmis.client.api
   * .OperationContext)
   */
  public Document getRenditionDocument(OperationContext context) {
    if (this.renditionDocumentId == null) {
      return null;
    }

    CmisObject rendDoc = session.getObject(session.createObjectId(this.renditionDocumentId),
        context);
    if (!(rendDoc instanceof Document)) {
      return null;
    }

    return (Document) rendDoc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Rendition#getContentStream()
   */
  public ContentStream getContentStream() {
    if ((objectId == null) || (streamId == null)) {
      return null;
    }

    ContentStreamData contentStream = session.getProvider().getObjectService().getContentStream(
        session.getRepositoryInfo().getId(), objectId, streamId, null, null, null);
    if (contentStream == null) {
      return null;
    }

    // TODO: what should happen if the length is not set?
    long length = (contentStream.getLength() == null ? -1 : contentStream.getLength().longValue());

    return session.getObjectFactory().createContentStream(contentStream.getFilename(), length,
        contentStream.getMimeType(), contentStream.getStream());
  }

}
