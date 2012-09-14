package org.apache.chemistry.opencmis.inmemory.storedobj.impl;

/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.LastModifiedContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentStreamDataImpl implements LastModifiedContentStream {

    private static final Logger LOG = LoggerFactory.getLogger(ContentStreamDataImpl.class.getName());

    private static long TOTAL_LENGTH  = 0L;
    private static long TOTAL_CALLS  = 0L;
    
    private int fLength;

    private String fMimeType;

    private String fFileName;

    private byte[] fContent;
    
    private GregorianCalendar fLastModified;

    private long fStreamLimitOffset;

    private long fStreamLimitLength;

    private final long sizeLimitKB;

    public ContentStreamDataImpl(long maxAllowedContentSizeKB) {
        sizeLimitKB = maxAllowedContentSizeKB;
    }

    public void setContent(InputStream in) throws IOException {
        fStreamLimitOffset = fStreamLimitLength = -1;
        if (null == in) {
            fContent = null; // delete content
            fLength = 0;
        } else {
            byte[] buffer = new byte[0xFFFF];
            ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
            for (int len = 0; (len = in.read(buffer)) != -1;) {
                contentStream.write(buffer, 0, len);
                fLength += len;
                if (sizeLimitKB > 0 && fLength > sizeLimitKB * 1024) {
                    throw new CmisInvalidArgumentException("Content size exceeds max. allowed size of " + sizeLimitKB
                            + "KB.");
                }
            }
            fContent = contentStream.toByteArray();
            fLength = contentStream.size();
            contentStream.close();
            in.close();
        }
        TOTAL_LENGTH += fLength;
        LOG.debug("setting content stream, total no calls " + ++TOTAL_CALLS + ".");
        LOG.debug("setting content stream, new size total " + (TOTAL_LENGTH / (1024 * 1024)) + "MB.");

    }

    public long getLength() {
        return fLength;
    }

    public BigInteger getBigLength() {
        return BigInteger.valueOf(fLength);
    }

    public String getMimeType() {
        return fMimeType;
    }

    public void setMimeType(String fMimeType) {
        this.fMimeType = fMimeType;
    }

    public String getFileName() {
        return fFileName;
    }

    public void setFileName(String fileName) {
        this.fFileName = fileName;
    }

    public String getFilename() {
        return fFileName;
    }

    public InputStream getStream() {
        if (null == fContent) {
            return null;
        } else if (fStreamLimitOffset <= 0 && fStreamLimitLength < 0) {
            return new ByteArrayInputStream(fContent);
        } else {
            return new ByteArrayInputStream(fContent, (int) (fStreamLimitOffset < 0 ? 0 : fStreamLimitOffset),
                    (int) (fStreamLimitLength < 0 ? fLength : fStreamLimitLength));
        }
    }

    public void setLastModified(GregorianCalendar lastModified) {
        this.fLastModified = lastModified;
    }
    
    public GregorianCalendar getLastModified() {
        return fLastModified;
    }
    
    public ContentStream getCloneWithLimits(long offset, long length) {
        ContentStreamDataImpl clone = new ContentStreamDataImpl(0);
        clone.fFileName = fFileName;
        clone.fLength = fLength;
        clone.fContent = fContent;
        clone.fMimeType = fMimeType;
        clone.fStreamLimitOffset = offset;
        clone.fStreamLimitLength = length;
        clone.fLastModified = fLastModified;
        return clone;
    }

    public final byte[] getBytes() {
        return fContent;
    }

    public List<CmisExtensionElement> getExtensions() {
        return null;
    }

    public void setExtensions(List<CmisExtensionElement> extensions) {
        // not implemented
    }
}
