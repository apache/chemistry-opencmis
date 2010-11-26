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
package org.apache.chemistry.opencmis.workbench;

import java.awt.Component;
import java.awt.Desktop;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.workbench.model.MIMETypes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientHelper {

    private static Log log = LogFactory.getLog(ClientHelper.class);
    private static int BUFFER_SIZE = 64 * 1024;

    public static void showError(Component parent, Exception ex) {
        if (log.isErrorEnabled()) {
            log.error(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);

            if (ex instanceof CmisBaseException) {
                CmisBaseException cex = (CmisBaseException) ex;

                if (cex.getCode() != null) {
                    log.error("Error code: " + cex.getCode());
                }

                if (cex.getErrorContent() != null) {
                    log.error("Error content: " + cex.getErrorContent());
                }
            }
        }

        JOptionPane.showMessageDialog(parent, ex.getClass().getSimpleName() + "\n" + ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static ImageIcon getIcon(String name) {
        URL imageURL = ClientHelper.class.getResource("/images/" + name);
        if (imageURL != null) {
            return new ImageIcon(imageURL);
        }

        return null;
    }

    public static String getDateString(GregorianCalendar cal) {
        if (cal == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss ZZZ");
        return sdf.format(cal.getTime());
    }

    public static void download(Component component, CmisObject object, String streamId) {
        ContentStream content = getContentStream(object, streamId);
        if (content == null) {
            return;
        }

        String filename = content.getFileName();
        if (filename == null) {
            if (object instanceof Document) {
                filename = ((Document) object).getContentStreamFileName();
            } else {
                filename = object.getName();
            }
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(filename));

        int chooseResult = fileChooser.showDialog(component, "Download");
        if (chooseResult == JFileChooser.APPROVE_OPTION) {
            try {
                storeStream(content.getStream(), fileChooser.getSelectedFile());
            } catch (Exception e) {
                showError(component, e);
            }
        }
    }

    public static void copy(Component component, File file) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(file.getName()));

        int chooseResult = fileChooser.showDialog(component, "Download");
        if (chooseResult == JFileChooser.APPROVE_OPTION) {
            try {
                storeStream(new FileInputStream(file), fileChooser.getSelectedFile());
            } catch (Exception e) {
                showError(component, e);
            }
        }
    }

    public static void open(Component component, CmisObject object, String streamId) {
        if (!Desktop.isDesktopSupported()) {
            download(component, object, streamId);
            return;
        }

        Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.OPEN)) {
            download(component, object, streamId);
            return;
        }

        File file = null;

        try {
            file = createTempFileFromDocument(object, streamId);
        } catch (Exception e) {
            showError(component, e);
        }

        try {
            desktop.open(file);
        } catch (Exception e) {
            if (e instanceof IOException) {
                copy(component, file);
            } else {
                showError(component, e);
            }
        }
    }

    public static File createTempFile(String filename) {
        String tempDir = System.getProperty("java.io.tmpdir");
        File clientTempDir = new File(tempDir, "cmisworkbench");
        if (!clientTempDir.exists()) {
            clientTempDir.mkdirs();
        }
        clientTempDir.deleteOnExit();

        File tempFile = new File(clientTempDir, filename);
        tempFile.deleteOnExit();

        return tempFile;
    }

    public static File createTempFileFromDocument(CmisObject object, String streamId) throws Exception {
        ContentStream content = getContentStream(object, streamId);
        if (content == null) {
            throw new Exception("No content!");
        }

        String filename = content.getFileName();
        if ((filename == null) || (filename.length() == 0)) {
            if (object instanceof Document) {
                filename = ((Document) object).getContentStreamFileName();
            }
        }
        if ((filename == null) || (filename.length() == 0)) {
            filename = object.getName();
        }
        if ((filename == null) || (filename.length() == 0)) {
            filename = "content";
        }

        if (filename.indexOf('.') == -1) {
            filename = filename + MIMETypes.getExtension(content.getMimeType());
        }

        File tempFile = ClientHelper.createTempFile(filename);
        try {
            storeStream(content.getStream(), tempFile);
        } catch (CmisConstraintException e) {
            // there is no content - leave the temp file empty
        }

        return tempFile;
    }

    private static void storeStream(InputStream in, File file) throws IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(file), BUFFER_SIZE);

            byte[] buffer = new byte[BUFFER_SIZE];
            int b;
            while ((b = in.read(buffer)) > -1) {
                out.write(buffer, 0, b);
            }

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private static ContentStream getContentStream(CmisObject object, String streamId) {
        if (object == null) {
            return null;
        }

        if (object instanceof Document) {
            return ((Document) object).getContentStream(streamId);
        } else {
            if (streamId == null) {
                return null;
            }

            List<Rendition> renditions = object.getRenditions();
            if (renditions == null) {
                return null;
            }

            for (Rendition rendition : renditions) {
                if (streamId.equals(rendition.getStreamId())) {
                    return rendition.getContentStream();
                }
            }
        }

        return null;
    }

    public static String readFileAndRemoveHeader(String file) {
        if (file == null) {
            return "";
        }

        InputStream stream = file.getClass().getResourceAsStream(file);
        if (stream == null) {
            return "";
        } else {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String s;
                boolean header = true;

                while ((s = reader.readLine()) != null) {
                    // remove header
                    if (header) {
                        String st = s.trim();
                        if (st.length() == 0) {
                            header = false;
                            continue;
                        }

                        char c = st.charAt(0);
                        header = (c == '/') || (c == '*') || (c == '#');
                        if (header) {
                            continue;
                        }
                    }

                    sb.append(s);
                    sb.append("\n");
                }

                reader.close();

                return sb.toString();
            } catch (Exception e) {
                return "";
            }
        }
    }

    public static List<FileEntry> readFileProperties(String propertiesFile, String path) {
        InputStream stream = propertiesFile.getClass().getResourceAsStream(propertiesFile);
        if (stream == null) {
            return null;
        }

        try {
            Properties properties = new Properties();
            properties.load(stream);
            stream.close();

            List<FileEntry> result = new ArrayList<FileEntry>();
            for (String file : properties.stringPropertyNames()) {
                result.add(new FileEntry(properties.getProperty(file), path + file));
            }
            Collections.sort(result);

            return result;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                stream.close();
            } catch (IOException ioe) {
            }
        }
    }

    public static class FileEntry implements Comparable<FileEntry> {
        private String name;
        private String file;

        public FileEntry(String name, String file) {
            this.name = name;
            this.file = file;
        }

        public String getName() {
            return name;
        }

        public String getFile() {
            return file;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public int compareTo(FileEntry o) {
            return name.compareToIgnoreCase(o.getName());
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FileEntry)) {
                return false;
            }

            return name.equals(((FileEntry) obj).getName());
        }
    }
}
