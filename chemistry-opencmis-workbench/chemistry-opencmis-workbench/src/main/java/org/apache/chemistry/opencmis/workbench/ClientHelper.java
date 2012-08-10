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

import groovy.lang.Binding;
import groovy.ui.Console;
import groovy.util.GroovyScriptEngine;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHelper {

    public static final Color LINK_COLOR = new Color(105, 29, 21);
    public static final Color LINK_SELECTED_COLOR = new Color(255, 255, 255);

    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);
    private static final int BUFFER_SIZE = 64 * 1024;

    private ClientHelper() {
    }

    public static void showError(Component parent, Exception ex) {
        if (LOG.isErrorEnabled()) {
            LOG.error(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);

            if (ex instanceof CmisBaseException) {
                CmisBaseException cex = (CmisBaseException) ex;

                if (cex.getCode() != null) {
                    LOG.error("Error code: " + cex.getCode());
                }

                if (cex.getErrorContent() != null) {
                    LOG.error("Error content: " + cex.getErrorContent());
                }
            }
        }

        String exceptionName = ex.getClass().getSimpleName();
        if (ex instanceof CmisBaseException) {
            exceptionName = ((CmisBaseException) ex).getExceptionName();
        }

        StringBuilder sb = new StringBuilder(ex.getMessage() == null ? "null" : ex.getMessage());

        int width = 80;
        while (sb.length() > width) {
            int p = width;

            int x = sb.indexOf(" ", p);
            if (x < 0 || x > p + 10) {
                x = sb.indexOf("/", p);
            }
            if (x < 0 || x > p + 10) {
                x = sb.indexOf(":", p);
            }
            if (x < 0 || x > p + 10) {
                x = p;
            }

            sb.insert(x, '\n');
            width = x + 80;
        }

        JOptionPane.showMessageDialog(parent, exceptionName + ":\n" + sb, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean isMacOSX() {
        return System.getProperty("os.name").startsWith("Mac OS X");
    }

    public static void installKeyBindings() {
        if (isMacOSX()) {
            final KeyStroke copyKeyStroke = KeyStroke.getKeyStroke("meta pressed C");
            final KeyStroke pasteKeyStroke = KeyStroke.getKeyStroke("meta pressed V");
            final KeyStroke cutKeyStroke = KeyStroke.getKeyStroke("meta pressed X");
            final KeyStroke allKeyStroke = KeyStroke.getKeyStroke("meta pressed A");

            InputMap textFieldMap = (InputMap) UIManager.get("TextField.focusInputMap");
            textFieldMap.put(copyKeyStroke, DefaultEditorKit.copyAction);
            textFieldMap.put(pasteKeyStroke, DefaultEditorKit.pasteAction);
            textFieldMap.put(cutKeyStroke, DefaultEditorKit.cutAction);
            textFieldMap.put(allKeyStroke, DefaultEditorKit.selectAllAction);

            InputMap formattedTextFieldMap = (InputMap) UIManager.get("FormattedTextField.focusInputMap");
            formattedTextFieldMap.put(copyKeyStroke, DefaultEditorKit.copyAction);
            formattedTextFieldMap.put(pasteKeyStroke, DefaultEditorKit.pasteAction);
            formattedTextFieldMap.put(cutKeyStroke, DefaultEditorKit.cutAction);
            formattedTextFieldMap.put(allKeyStroke, DefaultEditorKit.selectAllAction);

            InputMap textAreaMap = (InputMap) UIManager.get("TextArea.focusInputMap");
            textAreaMap.put(copyKeyStroke, DefaultEditorKit.copyAction);
            textAreaMap.put(pasteKeyStroke, DefaultEditorKit.pasteAction);
            textAreaMap.put(cutKeyStroke, DefaultEditorKit.cutAction);
            textAreaMap.put(allKeyStroke, DefaultEditorKit.selectAllAction);

            InputMap passwordFieldMap = (InputMap) UIManager.get("PasswordField.focusInputMap");
            passwordFieldMap.put(pasteKeyStroke, DefaultEditorKit.pasteAction);
        }
    }

    public static void installEscapeBinding(final Window window, final JRootPane rootPane, final boolean dispose) {
        final KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        final InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (dispose) {
                    window.dispose();
                } else {
                    window.setVisible(false);
                }
            }
        });
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZ");
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
            return;
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

        String ext = MimeTypes.getExtension(content.getMimeType());
        if (ext.length() > 0 && !filename.endsWith(ext)) {
            filename = filename + ext;
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

    public static void copyTableToClipboard(JTable table) {
        final String newline = System.getProperty("line.separator");

        final StringBuilder sb = new StringBuilder();
        final int rows = table.getModel().getRowCount();
        final int cols = table.getModel().getColumnCount();

        for (int col = 0; col < cols; col++) {
            if (col > 0) {
                sb.append(",");
            }

            sb.append(formatCSVValue(table.getModel().getColumnName(col)));
        }

        sb.append(newline);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (col > 0) {
                    sb.append(",");
                }

                Object value = table.getModel().getValueAt(row, col);
                sb.append(formatCSVValue(value));
            }
            sb.append(newline);
        }

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = new StringSelection(sb.toString());
        clipboard.setContents(transferable, null);
    }

    private static String formatCSVValue(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof GregorianCalendar) {
            return getDateString((GregorianCalendar) value);
        } else if (value instanceof String) {
            String s = value.toString();

            StringBuffer sb = new StringBuffer();
            sb.append('\"');

            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                sb.append(c);
                if (c == '\"') {
                    sb.append('\"');
                }
            }

            sb.append('\"');

            return sb.toString();
        } else if (value instanceof Collection<?>) {
            StringBuffer sb = new StringBuffer();
            sb.append("[");

            for (Object v : (Collection<?>) value) {
                if (sb.length() > 1) {
                    sb.append(",");
                }
                sb.append(formatCSVValue(v));
            }

            sb.append("]");

            return sb.toString();
        } else if (value instanceof ObjectId) {
            return formatCSVValue(((ObjectId) value).getId());
        } else if (value instanceof ImageIcon) {
            return "<icon>";
        }

        return value.toString();
    }

    public static String readFileAndRemoveHeader(String file) {
        if (file == null) {
            return "";
        }

        InputStream stream = ClientHelper.class.getResourceAsStream(file);
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
        InputStream stream = ClientHelper.class.getResourceAsStream(propertiesFile);
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

    public static Console openConsole(final Component parent, final ClientModel model, final String file) {
        try {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Console console = new Console(parent.getClass().getClassLoader());
            console.setVariable("session", model.getClientSession().getSession());
            console.setVariable("binding", model.getClientSession().getSession().getBinding());

            console.run();

            JMenu cmisMenu = new JMenu("CMIS");
            console.getFrame().getRootPane().getJMenuBar().add(cmisMenu);

            addConsoleMenu(cmisMenu, "CMIS 1.0 Specification", new URI(
                    "http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.html"));
            addConsoleMenu(cmisMenu, "OpenCMIS Documentation",
                    new URI("http://chemistry.apache.org/java/opencmis.html"));
            addConsoleMenu(cmisMenu, "OpenCMIS Client API JavaDoc", new URI(
                    "http://chemistry.apache.org/java/0.7.0/maven/apidocs/"));

            console.getInputArea().setText(ClientHelper.readFileAndRemoveHeader(file));

            return console;
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
            return null;
        } finally {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private static void addConsoleMenu(JMenu menu, String title, final URI url) {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Action.BROWSE)) {
            return;
        }

        JMenuItem menuItem = new JMenuItem(title);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Desktop.getDesktop().browse(url);
                } catch (IOException e1) {
                }
            }
        });

        menu.add(menuItem);
    }

    public static void runGroovyScript(final Component parent, final ClientModel model, final File file,
            final Writer out) {
        try {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            String[] roots = new String[] { file.getParentFile().getAbsolutePath() };
            GroovyScriptEngine gse = new GroovyScriptEngine(roots, parent.getClass().getClassLoader());
            Binding binding = new Binding();
            binding.setVariable("session", model.getClientSession().getSession());
            binding.setVariable("binding", model.getClientSession().getSession().getBinding());
            binding.setVariable("out", out);
            gse.run(file.getName(), binding);
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
        } finally {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public static void runJSR223Script(final Component parent, final ClientModel model, final File file,
            final String ext, final Writer out) {
        try {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByExtension(ext);
            engine.getContext().setWriter(out);
            engine.getContext().setErrorWriter(out);
            engine.put("session", model.getClientSession().getSession());
            engine.put("binding", model.getClientSession().getSession().getBinding());
            engine.put("out", new PrintWriter(out));
            engine.eval(new FileReader(file));
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
        } finally {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public static class FileEntry implements Comparable<FileEntry> {
        private final String name;
        private final String file;

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
