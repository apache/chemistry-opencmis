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
package org.apache.chemistry.opencmis.commons.impl;

import java.io.File;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MimeTypes {

    private static final Map<String, String> EXT2MIME = new HashMap<String, String>();
    private static final Map<String, String> MIME2EXT = new HashMap<String, String>();

    private MimeTypes() {
    }

    static {
        // extension to MIME type
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
        EXT2MIME.put("class", "application/java-vm");
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
        EXT2MIME.put("epub", "application/epub+zip");
        EXT2MIME.put("etx", "text/x-setext");
        EXT2MIME.put("evy", "application/envoy");
        EXT2MIME.put("flac", "audio/flac");
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
        EXT2MIME.put("jar", "application/java-archive");
        EXT2MIME.put("jfif", "image/pipeg");
        EXT2MIME.put("jpe", "image/jpeg");
        EXT2MIME.put("jpeg", "image/jpeg");
        EXT2MIME.put("jpg", "image/jpeg");
        EXT2MIME.put("js", "application/x-javascript");
        EXT2MIME.put("json", "application/json");
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
        EXT2MIME.put("odb", "application/vnd.oasis.opendocument.database");
        EXT2MIME.put("odc", "application/vnd.oasis.opendocument.chart");
        EXT2MIME.put("odf", "application/vnd.oasis.opendocument.formula");
        EXT2MIME.put("odft", "application/vnd.oasis.opendocument.formula-template");
        EXT2MIME.put("odg", "application/vnd.oasis.opendocument.graphics");
        EXT2MIME.put("odi", "application/vnd.oasis.opendocument.image");
        EXT2MIME.put("odm", "application/vnd.oasis.opendocument.text-master");
        EXT2MIME.put("odp", "application/vnd.oasis.opendocument.presentation");
        EXT2MIME.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
        EXT2MIME.put("odt", "application/vnd.oasis.opendocument.text");
        EXT2MIME.put("ogg", "audio/ogg");
        EXT2MIME.put("ogv", "video/ogg");
        EXT2MIME.put("otc", "application/vnd.oasis.opendocument.chart-template");
        EXT2MIME.put("otg", "application/vnd.oasis.opendocument.graphics-template");
        EXT2MIME.put("oth", "application/vnd.oasis.opendocument.text-web");
        EXT2MIME.put("oti", "application/vnd.oasis.opendocument.image-template");
        EXT2MIME.put("otp", "application/vnd.oasis.opendocument.presentation-template");
        EXT2MIME.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
        EXT2MIME.put("ott", "application/vnd.oasis.opendocument.text-template");
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
        EXT2MIME.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
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
        EXT2MIME.put("ser", "application/java-serialized-object");
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

        // MIME type to extension
        MIME2EXT.put("application/octet-stream", "");
        MIME2EXT.put("application/envoy", "evy");
        MIME2EXT.put("application/epub+zip", "epub");
        MIME2EXT.put("application/fractals", "fif");
        MIME2EXT.put("application/futuresplash", "spl");
        MIME2EXT.put("application/hta", "hta");
        MIME2EXT.put("application/java-archive", "jar");
        MIME2EXT.put("application/java-serialized-object", "ser");
        MIME2EXT.put("application/java-vm", "class");
        MIME2EXT.put("application/javascript", "js");
        MIME2EXT.put("application/json", "json");
        MIME2EXT.put("application/mac-binhex40", "hqx");
        MIME2EXT.put("application/msword", "doc");
        MIME2EXT.put("application/oda", "oda");
        MIME2EXT.put("application/olescript", "axs");
        MIME2EXT.put("application/pdf", "pdf");
        MIME2EXT.put("application/pics-rules", "prf");
        MIME2EXT.put("application/pkcs10", "p10");
        MIME2EXT.put("application/pkix-crl", "crl");
        MIME2EXT.put("application/postscript", "ps");
        MIME2EXT.put("application/rtf", "rtf");
        MIME2EXT.put("application/vnd.ms-excel", "xls");
        MIME2EXT.put("application/vnd.ms-pkicertstore", "sst");
        MIME2EXT.put("application/vnd.ms-pkiseccat", "cat");
        MIME2EXT.put("application/vnd.ms-pkistl", "stl");
        MIME2EXT.put("application/vnd.ms-powerpoint", "ppt");
        MIME2EXT.put("application/vnd.ms-project", "mpp");
        MIME2EXT.put("application/vnd.ms-works", "wps");
        MIME2EXT.put("application/vnd.oasis.opendocument.chart", "odc");
        MIME2EXT.put("application/vnd.oasis.opendocument.chart-template", "otc");
        MIME2EXT.put("application/vnd.oasis.opendocument.database", "odb");
        MIME2EXT.put("application/vnd.oasis.opendocument.formula", "odf");
        MIME2EXT.put("application/vnd.oasis.opendocument.formula-template", "odft");
        MIME2EXT.put("application/vnd.oasis.opendocument.graphics", "odg");
        MIME2EXT.put("application/vnd.oasis.opendocument.graphics-template", "otg");
        MIME2EXT.put("application/vnd.oasis.opendocument.image", "odi");
        MIME2EXT.put("application/vnd.oasis.opendocument.image-template", "oti");
        MIME2EXT.put("application/vnd.oasis.opendocument.presentation", "odp");
        MIME2EXT.put("application/vnd.oasis.opendocument.presentation-template", "otp");
        MIME2EXT.put("application/vnd.oasis.opendocument.spreadsheet", "ods");
        MIME2EXT.put("application/vnd.oasis.opendocument.spreadsheet-template", "ots");
        MIME2EXT.put("application/vnd.oasis.opendocument.text", "odt");
        MIME2EXT.put("application/vnd.oasis.opendocument.text-master", "odm");
        MIME2EXT.put("application/vnd.oasis.opendocument.text-template", "ott");
        MIME2EXT.put("application/vnd.oasis.opendocument.text-web", "oth");
        MIME2EXT.put("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
        MIME2EXT.put("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx");
        MIME2EXT.put("application/vnd.openxmlformats-officedocument.presentationml.template", "potx");
        MIME2EXT.put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
        MIME2EXT.put("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx");
        MIME2EXT.put("application/winhlp", "hlp");
        MIME2EXT.put("application/x-cdf", "cdf");
        MIME2EXT.put("application/x-compress", "z");
        MIME2EXT.put("application/x-compressed", "tgz");
        MIME2EXT.put("application/x-cpio", "cpio");
        MIME2EXT.put("application/x-csh", "csh");
        MIME2EXT.put("application/x-director", "dxr");
        MIME2EXT.put("application/x-dvi", "dvi");
        MIME2EXT.put("application/x-gtar", "gtar");
        MIME2EXT.put("application/x-gzip", "gz");
        MIME2EXT.put("application/x-hdf", "hdf");
        MIME2EXT.put("application/x-internet-signup", "isp");
        MIME2EXT.put("application/x-iphone", "iii");
        MIME2EXT.put("application/x-javascript", "js");
        MIME2EXT.put("application/x-latex", "latex");
        MIME2EXT.put("application/x-msaccess", "mdb");
        MIME2EXT.put("application/x-mscardfile", "crd");
        MIME2EXT.put("application/x-msclip", "clp");
        MIME2EXT.put("application/x-msdownload", "dll");
        MIME2EXT.put("application/x-msmediaview", "mvb");
        MIME2EXT.put("application/x-msmetafile", "wmf");
        MIME2EXT.put("application/x-mspublisher", "pub");
        MIME2EXT.put("application/x-msschedule", "scd");
        MIME2EXT.put("application/x-msterminal", "trm");
        MIME2EXT.put("application/x-mswrite", "wri");
        MIME2EXT.put("application/x-perfmon", "pmw");
        MIME2EXT.put("application/x-pkcs12", "pfx");
        MIME2EXT.put("application/x-pkcs12v", "p12");
        MIME2EXT.put("application/x-pkcs7-certificates", "p7b");
        MIME2EXT.put("application/x-pkcs7-certificates", "spc");
        MIME2EXT.put("application/x-pkcs7-certreqresp", "p7r");
        MIME2EXT.put("application/x-pkcs7-mime", "p7m");
        MIME2EXT.put("application/x-pkcs7-signature", "p7s");
        MIME2EXT.put("application/x-sh", "sh");
        MIME2EXT.put("application/x-shar", "shar");
        MIME2EXT.put("application/x-shockwave-flash", "swf");
        MIME2EXT.put("application/x-stuffit", "sit");
        MIME2EXT.put("application/x-tar", "tar");
        MIME2EXT.put("application/x-tcl", "tcl");
        MIME2EXT.put("application/x-tex", "tex");
        MIME2EXT.put("application/x-texinfo", "texinfo");
        MIME2EXT.put("application/x-troff-man", "man");
        MIME2EXT.put("application/x-troff-me", "me");
        MIME2EXT.put("application/x-troff-ms", "ms");
        MIME2EXT.put("application/x-troff", "tr");
        MIME2EXT.put("application/x-ustar", "ustar");
        MIME2EXT.put("application/x-wais-source", "src");
        MIME2EXT.put("application/x-x509-ca-cert", "cer");
        MIME2EXT.put("application/ynd.ms-pkipko", "vpko");
        MIME2EXT.put("application/zip", "zip");
        MIME2EXT.put("audio/basic", "snd");
        MIME2EXT.put("audio/flac", "flac");
        MIME2EXT.put("audio/mid", "mid");
        MIME2EXT.put("audio/mpeg", "mp3");
        MIME2EXT.put("audio/ogg", "ogg");
        MIME2EXT.put("audio/x-aiff", "aif");
        MIME2EXT.put("audio/x-mpegurl", "m3u");
        MIME2EXT.put("audio/x-pn-realaudio", "ram");
        MIME2EXT.put("audio/x-wav", "wav");
        MIME2EXT.put("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "doct");
        MIME2EXT.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        MIME2EXT.put("image/bmp", "bmp");
        MIME2EXT.put("image/cis-cod", "cod");
        MIME2EXT.put("image/gif", "gif");
        MIME2EXT.put("image/ief", "ief");
        MIME2EXT.put("image/jpeg", "jpeg");
        MIME2EXT.put("image/pipeg", "jfif");
        MIME2EXT.put("image/png", "png");
        MIME2EXT.put("image/svg+xml", "svg");
        MIME2EXT.put("image/tiff", "tiff");
        MIME2EXT.put("image/x-cmu-raster", "ras");
        MIME2EXT.put("image/x-cmx", "cmx");
        MIME2EXT.put("image/x-icon", "ico");
        MIME2EXT.put("image/x-portable-anymap", "pnm");
        MIME2EXT.put("image/x-portable-bitmap", "pbm");
        MIME2EXT.put("image/x-portable-graymap", "pgm");
        MIME2EXT.put("image/x-portable-pixmap", "ppm");
        MIME2EXT.put("image/x-rgb", "rgb");
        MIME2EXT.put("image/x-xbitmap", "xbm");
        MIME2EXT.put("image/x-xpixmap", "xpm");
        MIME2EXT.put("image/x-xwindowdump", "xwd");
        MIME2EXT.put("message/rfc822", "mhtml");
        MIME2EXT.put("text/css", "css");
        MIME2EXT.put("text/html", "html");
        MIME2EXT.put("text/iuls", "uls");
        MIME2EXT.put("text/plain", "txt");
        MIME2EXT.put("text/richtext", "rtx");
        MIME2EXT.put("text/scriptlet", "sct");
        MIME2EXT.put("text/tab-separated-values", "tsv");
        MIME2EXT.put("text/webviewhtml", "htt");
        MIME2EXT.put("text/x-component", "htc");
        MIME2EXT.put("text/x-setext", "etx");
        MIME2EXT.put("text/x-vcard", "vcf");
        MIME2EXT.put("text/xml", "xml");
        MIME2EXT.put("video/mpeg", "mpeg");
        MIME2EXT.put("video/mpegv", "mpe");
        MIME2EXT.put("video/ogg", "ogv");
        MIME2EXT.put("video/quicktime", "mov");
        MIME2EXT.put("video/quicktime", "qt");
        MIME2EXT.put("video/x-la-asf", "lsf");
        MIME2EXT.put("video/x-la-asf", "lsx");
        MIME2EXT.put("video/x-ms-asf", "asf");
        MIME2EXT.put("video/x-msvideo", "avi");
        MIME2EXT.put("video/x-sgi-movie", "movie");
        MIME2EXT.put("x-world/x-vrml", "vrml");
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
            mime = URLConnection.getFileNameMap().getContentTypeFor("x." + ext);
        }
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

    /**
     * Guesses a extension from a MIME type.
     */
    public static String getExtension(String mimeType) {
        if (mimeType == null) {
            return "";
        }

        int x = mimeType.indexOf(';');
        if (x > -1) {
            mimeType = mimeType.substring(0, x);
        }
        mimeType = mimeType.trim().toLowerCase(Locale.ENGLISH);

        String extension = MIME2EXT.get(mimeType);
        return ((extension == null || extension.length() == 0) ? "" : "." + extension);
    }
}
