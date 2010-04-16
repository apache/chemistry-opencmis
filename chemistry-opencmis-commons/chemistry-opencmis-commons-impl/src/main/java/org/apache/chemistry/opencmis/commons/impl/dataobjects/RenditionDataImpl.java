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
package org.apache.chemistry.opencmis.commons.impl.dataobjects;

import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.api.RenditionData;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RenditionDataImpl extends AbstractExtensionData implements RenditionData {

	protected String fStreamId;
	private String fMimeType;
	protected BigInteger fLength;
	private String fKind;
	private String fTitle;
	protected BigInteger fWidth;
	protected BigInteger fHeight;
	protected String fRenditionDocumentId;

	public RenditionDataImpl() {
	}

	public RenditionDataImpl(String streamId, String mimeType, BigInteger length, String kind, String title,
			BigInteger width, BigInteger height, String renditionDocumentId) {
		setStreamId(streamId);
		setMimeType(mimeType);
		setBigLength(length);
		setKind(kind);
		setTitle(title);
		setBigWidth(width);
		setBigHeight(height);
		setRenditionDocumentId(renditionDocumentId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.RenditionData#getStreamId()
	 */
	public String getStreamId() {
		return fStreamId;
	}

	public void setStreamId(String streamId) {
		fStreamId = streamId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.RenditionData#getMimeType()
	 */
	public String getMimeType() {
		return fMimeType;
	}

	public void setMimeType(String mimeType) {
		fMimeType = mimeType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.RenditionData#getLength()
	 */
	public BigInteger getBigLength() {
		return fLength;
	}

	public void setBigLength(BigInteger length) {
		fLength = length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.RenditionData#getKind()
	 */
	public String getKind() {
		return fKind;
	}

	public void setKind(String kind) {
		fKind = kind;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.RenditionData#getTitle()
	 */
	public String getTitle() {
		return fTitle;
	}

	public void setTitle(String title) {
		fTitle = title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.RenditionData#getHeight()
	 */
	public BigInteger getBigHeight() {
		return fHeight;
	}

	public void setBigHeight(BigInteger height) {
		fHeight = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.RenditionData#getWidth()
	 */
	public BigInteger getBigWidth() {
		return fWidth;
	}

	public void setBigWidth(BigInteger width) {
		fWidth = width;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.opencmis.client.provider.RenditionData#getRenditionDocumentId
	 * ()
	 */
	public String getRenditionDocumentId() {
		return fRenditionDocumentId;
	}

	public void setRenditionDocumentId(String renditionDocumentId) {
		fRenditionDocumentId = renditionDocumentId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RenditionDataImpl [, kind=" + fKind + ", title=" + fTitle + ", MIME type=" + fMimeType + ", length="
				+ fLength + ", rendition document id=" + fRenditionDocumentId + ", stream id=" + fStreamId + " height="
				+ fHeight + ", width=" + fWidth + "]" + super.toString();
	}

}
