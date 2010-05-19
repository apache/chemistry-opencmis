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
package org.apache.chemistry.opencmis.client.runtime;

import java.math.BigInteger;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RenditionDataImpl;

/**
 * Implementation of <code>Rendition</code>.
 */
public class RenditionImpl extends RenditionDataImpl implements Rendition {

    private Session session;
    private String objectId;

    /**
     * Constructor.
     */
    public RenditionImpl(Session session, String objectId, String streamId, String renditionDocumentId, String kind,
            long length, String mimeType, String title, int height, int width) {
        super(streamId, mimeType, BigInteger.valueOf(length), kind, title, BigInteger.valueOf(width), BigInteger
                .valueOf(height), renditionDocumentId);
        this.session = session;
        this.objectId = objectId;
    }

    public long getLength() {
        return fLength == null ? -1 : fLength.longValue();
    }

    public long getHeight() {
        return fHeight == null ? -1 : fHeight.longValue();
    }

    public long getWidth() {
        return fWidth == null ? -1 : fWidth.longValue();
    }

    public Document getRenditionDocument() {
        return getRenditionDocument(session.getDefaultContext());
    }

    public Document getRenditionDocument(OperationContext context) {
        if (fRenditionDocumentId == null) {
            return null;
        }
        CmisObject rendDoc = session.getObject(session.createObjectId(fRenditionDocumentId), context);
        if (!(rendDoc instanceof Document)) {
            return null;
        }

        return (Document) rendDoc;
    }

    public ContentStream getContentStream() {
        if ((objectId == null) || (fStreamId == null)) {
            return null;
        }

        ContentStream contentStream = session.getBinding().getObjectService().getContentStream(
                session.getRepositoryInfo().getId(), objectId, fStreamId, null, null, null);
        if (contentStream == null) {
            return null;
        }

        long length = contentStream.getBigLength() == null ? -1 : contentStream.getBigLength().longValue();

        return session.getObjectFactory().createContentStream(contentStream.getFileName(), length,
                contentStream.getMimeType(), contentStream.getStream());
    }

}
