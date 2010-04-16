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

import java.io.InputStream;
import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.api.ContentStream;

/**
 * Content stream data implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ContentStreamImpl extends AbstractExtensionData implements ContentStream {

	private String fFilename;
	private BigInteger fLength;
	private String fMimeType;
	private InputStream fStream;

	/**
	 * Constructor.
	 */
	public ContentStreamImpl() {
	}

	/**
	 * Constructor.
	 */
	public ContentStreamImpl(String filename, BigInteger length, String mimetype, InputStream stream) {
		setLength(length);
		setMimeType(mimetype);
		setFileName(filename);
		setStream(stream);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.ContentStreamData#getFilename()
	 */
	public String getFileName() {
		return fFilename;
	}

	public void setFileName(String filename) {
		fFilename = filename;
	}

	public long getLength() {
		return fLength == null ? -1 : fLength.longValue();
	}

	public BigInteger getBigLength() {
		return fLength;
	}

	public void setLength(BigInteger length) {
		fLength = length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.ContentStreamData#getMimeType()
	 */
	public String getMimeType() {
		return fMimeType;
	}

	public void setMimeType(String mimetype) {
		fMimeType = mimetype;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.opencmis.client.provider.ContentStreamData#getStream()
	 */
	public InputStream getStream() {
		return fStream;
	}

	public void setStream(InputStream stream) {
		fStream = stream;
	}

	@Override
	public String toString() {
		return "ContentStream [filename=" + fFilename + ", length=" + fLength + ", MIME type=" + fMimeType
				+ ", has stream=" + (fStream != null) + "]" + super.toString();
	}
}
