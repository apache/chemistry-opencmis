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
package org.apache.opencmis.fileshare;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MIMETypes {

  private static Map<String, String> EXT2MIME = new HashMap<String, String>();

  static {
    EXT2MIME.put("", "application/octet-stream");
    EXT2MIME.put("ai", "application/postscript");
    EXT2MIME.put("aif", "audio/x-aiff");
    EXT2MIME.put("aifc", "audio/x-aiff");
    EXT2MIME.put("aiff", "audio/x-aiff");
    EXT2MIME.put("asf", "video/x-ms-asf");
    EXT2MIME.put("asr", "video/x-ms-asf");
    EXT2MIME.put("asx", "video/x-ms-asf");
    EXT2MIME.put("au", "audio/basic");
    EXT2MIME.put("avi", "video/x-msvideo");
    EXT2MIME.put("axs", "application/olescript");
    EXT2MIME.put("bas", "text/plain");
    EXT2MIME.put("bmp", "image/bmp");
    EXT2MIME.put("c", "text/plain");
    EXT2MIME.put("cat", "application/vnd.ms-pkiseccat");
    EXT2MIME.put("cdf", "application/x-cdf");
    EXT2MIME.put("cer", "application/x-x509-ca-cert");
    EXT2MIME.put("clp", "application/x-msclip");
    EXT2MIME.put("cmx", "image/x-cmx");
    EXT2MIME.put("cod", "image/cis-cod");
    EXT2MIME.put("cpio", "application/x-cpio");
    EXT2MIME.put("crd", "application/x-mscardfile");
    EXT2MIME.put("crl", "application/pkix-crl");
    EXT2MIME.put("crt", "application/x-x509-ca-cert");
    EXT2MIME.put("csh", "application/x-csh");
    EXT2MIME.put("css", "text/css");
    EXT2MIME.put("dll", "application/x-msdownload");
    EXT2MIME.put("doc", "application/msword");
    EXT2MIME.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    EXT2MIME.put("doct", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
    EXT2MIME.put("dot", "application/msword");
    EXT2MIME.put("dvi", "application/x-dvi");
    EXT2MIME.put("dxr", "application/x-director");
    EXT2MIME.put("eps", "application/postscript");
    EXT2MIME.put("etx", "text/x-setext");
    EXT2MIME.put("evy", "application/envoy");
    EXT2MIME.put("fif", "application/fractals");
    EXT2MIME.put("flr", "x-world/x-vrml");
    EXT2MIME.put("gif", "image/gif");
    EXT2MIME.put("gtar", "application/x-gtar");
    EXT2MIME.put("gz", "application/x-gzip");
    EXT2MIME.put("h", "text/plain");
    EXT2MIME.put("hdf", "application/x-hdf");
    EXT2MIME.put("hlp", "application/winhlp");
    EXT2MIME.put("hqx", "application/mac-binhex40");
    EXT2MIME.put("hta", "application/hta");
    EXT2MIME.put("htc", "text/x-component");
    EXT2MIME.put("htm", "text/html");
    EXT2MIME.put("html", "text/html");
    EXT2MIME.put("htt", "text/webviewhtml");
    EXT2MIME.put("ico", "image/x-icon");
    EXT2MIME.put("ief", "image/ief");
    EXT2MIME.put("iii", "application/x-iphone");
    EXT2MIME.put("isp", "application/x-internet-signup");
    EXT2MIME.put("jfif", "image/pipeg");
    EXT2MIME.put("jpe", "image/jpeg");
    EXT2MIME.put("jpeg", "image/jpeg");
    EXT2MIME.put("jpg", "image/jpeg");
    EXT2MIME.put("js", "application/x-javascript");
    EXT2MIME.put("latex", "application/x-latex");
    EXT2MIME.put("lsf", "video/x-la-asf");
    EXT2MIME.put("lsx", "video/x-la-asf");
    EXT2MIME.put("m3u", "audio/x-mpegurl");
    EXT2MIME.put("man", "application/x-troff-man");
    EXT2MIME.put("mdb", "application/x-msaccess");
    EXT2MIME.put("me", "application/x-troff-me");
    EXT2MIME.put("mhtv", "message/rfc822");
    EXT2MIME.put("mhtml", "message/rfc822");
    EXT2MIME.put("mid", "audio/mid");
    EXT2MIME.put("mov", "video/quicktime");
    EXT2MIME.put("movie", "video/x-sgi-movie");
    EXT2MIME.put("mp2", "video/mpeg");
    EXT2MIME.put("mp3", "audio/mpeg");
    EXT2MIME.put("mpa", "video/mpeg");
    EXT2MIME.put("mpe", "video/mpegv");
    EXT2MIME.put("mpeg", "video/mpeg");
    EXT2MIME.put("mpg", "video/mpegv");
    EXT2MIME.put("mpp", "application/vnd.ms-project");
    EXT2MIME.put("mpv2", "video/mpeg");
    EXT2MIME.put("ms", "application/x-troff-ms");
    EXT2MIME.put("mvb", "application/x-msmediaview");
    EXT2MIME.put("nws", "message/rfc822");
    EXT2MIME.put("oda", "application/oda");
    EXT2MIME.put("p10", "application/pkcs10");
    EXT2MIME.put("p12", "application/x-pkcs12v");
    EXT2MIME.put("p7b", "application/x-pkcs7-certificates");
    EXT2MIME.put("p7c", "application/x-pkcs7-mime");
    EXT2MIME.put("p7m", "application/x-pkcs7-mime");
    EXT2MIME.put("p7r", "application/x-pkcs7-certreqresp");
    EXT2MIME.put("p7s", "application/x-pkcs7-signature");
    EXT2MIME.put("pbm", "image/x-portable-bitmap");
    EXT2MIME.put("pdf", "application/pdf");
    EXT2MIME.put("pfx", "application/x-pkcs12");
    EXT2MIME.put("pgm", "image/x-portable-graymap");
    EXT2MIME.put("vpko", "application/ynd.ms-pkipko");
    EXT2MIME.put("pma", "application/x-perfmon");
    EXT2MIME.put("pmc", "application/x-perfmon");
    EXT2MIME.put("pml", "application/x-perfmon");
    EXT2MIME.put("pmr", "application/x-perfmon");
    EXT2MIME.put("pmw", "application/x-perfmon");
    EXT2MIME.put("png", "image/png");
    EXT2MIME.put("pnm", "image/x-portable-anymap");
    EXT2MIME.put("pot", "application/vnd.ms-powerpoint");
    EXT2MIME.put("ppm", "image/x-portable-pixmap");
    EXT2MIME.put("pps", "application/vnd.ms-powerpoint");
    EXT2MIME.put("ppt", "application/vnd.ms-powerpoint");
    EXT2MIME.put("pptx",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    EXT2MIME.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
    EXT2MIME.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
    EXT2MIME.put("prf", "application/pics-rules");
    EXT2MIME.put("ps", "application/postscript");
    EXT2MIME.put("pub", "application/x-mspublisher");
    EXT2MIME.put("qt", "video/quicktime");
    EXT2MIME.put("ra", "audio/x-pn-realaudio");
    EXT2MIME.put("ram", "audio/x-pn-realaudio");
    EXT2MIME.put("ras", "image/x-cmu-raster");
    EXT2MIME.put("rgb", "image/x-rgb");
    EXT2MIME.put("rmi", "audio/mid");
    EXT2MIME.put("roff", "application/x-troff");
    EXT2MIME.put("rtf", "application/rtf");
    EXT2MIME.put("rtx", "text/richtext");
    EXT2MIME.put("scd", "application/x-msschedule");
    EXT2MIME.put("sct", "text/scriptlet");
    EXT2MIME.put("sh", "application/x-sh");
    EXT2MIME.put("shar", "application/x-shar");
    EXT2MIME.put("sit", "application/x-stuffit");
    EXT2MIME.put("snd", "audio/basic");
    EXT2MIME.put("spc", "application/x-pkcs7-certificates");
    EXT2MIME.put("spl", "application/futuresplash");
    EXT2MIME.put("src", "application/x-wais-source");
    EXT2MIME.put("sst", "application/vnd.ms-pkicertstore");
    EXT2MIME.put("stl", "application/vnd.ms-pkistl");
    EXT2MIME.put("stm", "text/html");
    EXT2MIME.put("svg", "image/svg+xml");
    EXT2MIME.put("swf", "application/x-shockwave-flash");
    EXT2MIME.put("t", "application/x-troff");
    EXT2MIME.put("tar", "application/x-tar");
    EXT2MIME.put("tcl", "application/x-tcl");
    EXT2MIME.put("tex", "application/x-tex");
    EXT2MIME.put("texi", "application/x-texinfo");
    EXT2MIME.put("texinfo", "application/x-texinfo");
    EXT2MIME.put("tgz", "application/x-compressed");
    EXT2MIME.put("tif", "image/tiff");
    EXT2MIME.put("tiff", "image/tiff");
    EXT2MIME.put("tr", "application/x-troff");
    EXT2MIME.put("trm", "application/x-msterminal");
    EXT2MIME.put("tsv", "text/tab-separated-values");
    EXT2MIME.put("txt", "text/plain");
    EXT2MIME.put("uls", "text/iuls");
    EXT2MIME.put("ustar", "application/x-ustar");
    EXT2MIME.put("vcf", "text/x-vcard");
    EXT2MIME.put("vrml", "x-world/x-vrml");
    EXT2MIME.put("wav", "audio/x-wav");
    EXT2MIME.put("wcm", "application/vnd.ms-works");
    EXT2MIME.put("wdb", "application/vnd.ms-works");
    EXT2MIME.put("wmf", "application/x-msmetafile");
    EXT2MIME.put("wps", "application/vnd.ms-works");
    EXT2MIME.put("wri", "application/x-mswrite");
    EXT2MIME.put("wrl", "x-world/x-vrml");
    EXT2MIME.put("wrz", "x-world/x-vrml");
    EXT2MIME.put("xaf", "x-world/x-vrml");
    EXT2MIME.put("xbm", "image/x-xbitmap");
    EXT2MIME.put("xla", "application/vnd.ms-excel");
    EXT2MIME.put("xlc", "application/vnd.ms-excel");
    EXT2MIME.put("xlm", "application/vnd.ms-excel");
    EXT2MIME.put("xls", "application/vnd.ms-excel");
    EXT2MIME.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    EXT2MIME.put("xlt", "application/vnd.ms-excel");
    EXT2MIME.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
    EXT2MIME.put("xlw", "application/vnd.ms-excel");
    EXT2MIME.put("xml", "text/xml");
    EXT2MIME.put("xof", "x-world/x-vrml");
    EXT2MIME.put("xpm", "image/x-xpixmap");
    EXT2MIME.put("xwd", "image/x-xwindowdump");
    EXT2MIME.put("z", "application/x-compress");
    EXT2MIME.put("zip", "application/zip");
  }

  /**
   * Returns the MIME type for file extension.
   */
  public static String getMIMEType(String ext) {
    if (ext == null) {
      return EXT2MIME.get("");
    }

    int x = ext.lastIndexOf('.');
    if (x > -1) {
      ext = ext.substring(x + 1);
    }

    String mime = EXT2MIME.get(ext.toLowerCase());
    if (mime == null) {
      mime = EXT2MIME.get("");
    }

    return mime;
  }

  /**
   * Returns the MIME type for a file.
   */
  public static String getMIMEType(File file) {
    if (file == null) {
      return getMIMEType("");
    }

    return getMIMEType(file.getName());
  }
}
