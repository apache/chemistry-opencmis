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

  byte[] fContent;

  public void setContent (InputStream in) throws IOException {
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
    }
    in.close();    
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
    else
      return new ByteArrayInputStream(fContent);
  }

  public List<Object> getExtensions() {
    return null;
  }

  public void setExtensions(List<Object> extensions) {
    // not implemented
  }
}
