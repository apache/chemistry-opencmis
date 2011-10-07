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
package org.apache.chemistry.opencmis.server.support.filter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LoggingFilter implements Filter {

    private static final Log log = LogFactory.getLog(LoggingFilter.class);
    private static int REQUEST_NO = 0;
    private static String OVERRIDE_NAME;
    private static int OVERRIDE_INDENT = -1;
    private static String OVERRIDE_LOG_DIR;
    private static Boolean OVERRIDE_PRETTY_PRINT;
    private String logDir;
    private boolean prettyPrint = true;
    private int indent = -1;

    public void init(FilterConfig cfg) throws ServletException {
        
        String val; 
        if (null == OVERRIDE_LOG_DIR) {
            logDir = cfg.getInitParameter("LogDir");
            if (null == logDir)
                logDir = System.getProperty("java.io.tmpdir");
            if (null == logDir)
                logDir = "." + File.separator;
        } else
            logDir = OVERRIDE_LOG_DIR;
       
        if (!logDir.endsWith(File.separator))
            logDir += File.separator;
        
        if (OVERRIDE_INDENT < 0) {
            val = cfg.getInitParameter("Indent");
            if (null != val)
                indent = Integer.parseInt(val);
            if (indent < 0)
                indent = 4;
        } else
            indent = OVERRIDE_INDENT;
        
        if (null == OVERRIDE_PRETTY_PRINT) {
            val = cfg.getInitParameter("PrettyPrint");
            if (null != val)
                prettyPrint = Boolean.parseBoolean(val);
        } else 
            prettyPrint = OVERRIDE_PRETTY_PRINT;
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        log.debug("Logging filter doFilter");

        if (resp instanceof HttpServletResponse && req instanceof HttpServletRequest) {
            LoggingRequestWrapper logReq = new LoggingRequestWrapper((HttpServletRequest)req);
            LoggingResponseWrapper logResponse = new LoggingResponseWrapper((HttpServletResponse)resp);
            
            int reqNo = getNextRequestNumber();
            String requestFileName = getRequestFileName(reqNo);
            String cType = logReq.getContentType();
            String xmlRequest = logReq.getPayload();
            if (xmlRequest != null && xmlRequest.length() > 0) {
                if (prettyPrint)
                    xmlRequest = prettyPrint(xmlRequest, indent);
            } else
                xmlRequest = "";

            log.debug("Found request: " + requestFileName + ": " + xmlRequest);
            writeTextToFile(requestFileName, xmlRequest);
            
            chain.doFilter(logReq, logResponse);

            cType = logResponse.getContentType();
            String xmlResponse;
            String responseFileName = getResponseFileName(reqNo);
            if(cType != null && cType.endsWith("xml")) {
                if (prettyPrint)
                    xmlResponse = prettyPrint(logResponse.getPayload(), indent);
                else
                    xmlResponse = logResponse.getPayload();
                
                log.debug("Found response: " + responseFileName  + ": " + xmlResponse);
                writeTextToFile(responseFileName, xmlResponse);
            }
        } else {            
            chain.doFilter(req, resp);
        }
    }
    
    private void writeTextToFile(String filename, String content) {
        PrintWriter pw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(filename);
            pw = new PrintWriter(fw);

            Scanner scanner = new Scanner(content);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                pw.println(line);
            }

            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pw != null)
                pw.close();
            if (fw != null)
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    
    private static String prettyPrint(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer(); 
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e); // simple exception handling, please review it
        }
    }

    private String getRequestFileName(int no) {
        if (OVERRIDE_NAME == null)
            return logDir + String.format("%05d-request.log", no);
        else
            return OVERRIDE_NAME + "-request";
    }
    
    private String getResponseFileName(int no) {
        if (OVERRIDE_NAME == null)
            return logDir + String.format("%05d-response.log", no);
        else
            return OVERRIDE_NAME + "-request";
    }
    
    private static synchronized int getNextRequestNumber() {
        return REQUEST_NO++;
    }
    
    public static void setFileName(String name) {
        OVERRIDE_NAME = name;
    }
    
    public static void setLogDir(String dir) {
        OVERRIDE_LOG_DIR = dir;
    }

    public static void setIndent(int indent) {
        OVERRIDE_INDENT = indent;
    }

    public static void setPrettyPrint(boolean pp) {
        OVERRIDE_PRETTY_PRINT = pp;
    }

    private class LoggingRequestWrapper extends HttpServletRequestWrapper {
        
        private LoggingInputStream is;
   
        public LoggingRequestWrapper(HttpServletRequest request) throws IOException {
           super(request);
           this.is = new LoggingInputStream(request.getInputStream());
        }
   
        @Override
        public ServletInputStream getInputStream() throws IOException {
           return is;
        }
   
        public String getPayload() {
           return is.getPayload();
        }
     }
   
     private class LoggingInputStream extends ServletInputStream {
   
        private ByteArrayOutputStream baous = new ByteArrayOutputStream();
        private ServletInputStream is;
   
        public LoggingInputStream(ServletInputStream is) {
           super();
           this.is = is;
        }
   
        // Since we are not sure which method is used just overwrite all 4 of them:
        @Override
        public int read() throws IOException {
           int ch = is.read();
           if (ch != -1) {
              baous.write(ch);
           }
           return ch;
        }
   
        @Override
        public int read(byte[] b) throws IOException {
           int ch = is.read(b);
           if (ch != -1) {
              baous.write(b);
           }
           return ch;
        }
   
        @Override
        public int read(byte[] b, int o, int l) throws IOException {
           int ch = is.read(b,o,l);
           if (ch != -1) {
              baous.write(b);
           }
           return ch;
        }
        
        @Override
        public int readLine(byte[] b, int o, int l) throws IOException {
           int ch = is.readLine(b,o,l);
           if (ch != -1) {
               baous.write(b, o, l);
           }
           return ch;
        }
   
        public String getPayload() {
           return baous.toString();
        }
     }
     
     private class LoggingResponseWrapper extends HttpServletResponseWrapper {
         
         private LoggingOutputStream os;
    
         public LoggingResponseWrapper(HttpServletResponse response) throws IOException {
            super(response);
            this.os = new LoggingOutputStream(response.getOutputStream());
         }
    
         @Override
         public ServletOutputStream getOutputStream() throws IOException {
            return os;
         }
    
         public String getPayload() {
            return os.getPayload();
         }
      }
    
      private class LoggingOutputStream extends ServletOutputStream {
          private ByteArrayOutputStream baous = new ByteArrayOutputStream();
          private ServletOutputStream os;
     
          public LoggingOutputStream(ServletOutputStream os) {
             super();
             this.os = os;
          }
          
          public String getPayload() {
              return new String(baous.toString());
           }

        @Override
        public void write(byte[] b, int off, int len) {
            try {
                baous.write(b, off, len);
                os.write(b, off, len);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        @Override
        public
        void write(byte[] b) {
            try {
                baous.write(b);
                os.write(b);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
         
        @Override
        public void write(int ch) throws IOException {
            baous.write(ch);
            os.write(ch);
        }
      }
}
