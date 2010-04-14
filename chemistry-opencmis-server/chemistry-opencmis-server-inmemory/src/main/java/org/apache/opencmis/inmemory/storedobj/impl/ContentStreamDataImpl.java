package org.apache.opencmis.inmemory.storedobj.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.provider.ContentStreamData;

public class ContentStreamDataImpl implements ContentStreamData {

  private int fLength;

  private String fMimeType;

  private String fFileName;

  private byte[] fContent;
  
  private long fStreamLimitOffset;
  
  private long fStreamLimitLength;

  public void setContent (InputStream in) throws IOException {
    fStreamLimitOffset = fStreamLimitLength = -1;
    if (null == in) {
      fContent = null; // delete content
      fLength = 0;
    } else {
      byte[] buffer = new byte[ 0xFFFF ]; 
      ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
      for ( int len=0; (len = in.read(buffer)) != -1; )  {
        contentStream.write( buffer, 0, len ); 
        fLength += len;
      }
      fContent = contentStream.toByteArray();
      fLength = contentStream.size();
      contentStream.close();
      in.close();    
    }
  }

  public BigInteger getLength() {
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
    if (null == fContent)
      return null;
    else if (fStreamLimitOffset <= 0 && fStreamLimitLength < 0)
      return new ByteArrayInputStream(fContent);
    else 
      return new ByteArrayInputStream(fContent, (int)(fStreamLimitOffset<0 ? 0 : fStreamLimitOffset),
          (int)(fStreamLimitLength<0 ? fLength : fStreamLimitLength));      
  }

  public ContentStreamData getCloneWithLimits(long offset, long length) {
    ContentStreamDataImpl clone = new ContentStreamDataImpl();
    clone.fFileName = fFileName;
    clone.fLength = fLength;
    clone.fContent = fContent;
    clone.fMimeType = fMimeType;
    clone.fStreamLimitOffset = offset;
    clone.fStreamLimitLength = length;
    return clone;
  }
  
  
  public List<Object> getExtensions() {
    return null;
  }

  public void setExtensions(List<Object> extensions) {
    // not implemented
  }
}
