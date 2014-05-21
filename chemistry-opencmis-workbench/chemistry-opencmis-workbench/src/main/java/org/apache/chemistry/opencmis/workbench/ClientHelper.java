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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultEditorKit;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientHelper {

    public static final Color LINK_COLOR = new Color(105, 29, 21);
    public static final Color LINK_SELECTED_COLOR = new Color(255, 255, 255);

    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);
    private static final int BUFFER_SIZE = 64 * 1024;

    private ClientHelper() {
    }

    public static void logError(Exception ex) {
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

                if (LOG.isDebugEnabled() && cex.getCause() != null) {
                    LOG.debug("Cause: " + cex.getCause().toString(), cex.getCause());
                }
            }
        }
    }

    public static void showError(Component parent, Exception ex) {
        logError(ex);

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
        if (!clientTempDir.exists() && !clientTempDir.mkdirs()) {
            throw new CmisRuntimeException("Could not create directory for temp file!");
        }
        clientTempDir.deleteOnExit();

        File tempFile = new File(clientTempDir, filename);
        tempFile.deleteOnExit();

        return tempFile;
    }

    public static File createTempFileFromDocument(CmisObject object, String streamId) throws IOException {
        ContentStream content = getContentStream(object, streamId);
        if (content == null) {
            throw new IllegalArgumentException("No content!");
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
            out = new FileOutputStream(file);
            IOUtils.copy(new LoggingInputStream(in, file.getName()), out, BUFFER_SIZE);
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
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
                sb.append(',');
            }

            sb.append(formatCSVValue(table.getModel().getColumnName(col)));
        }

        sb.append(newline);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (col > 0) {
                    sb.append(',');
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

            StringBuilder sb = new StringBuilder();
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
            StringBuilder sb = new StringBuilder();
            sb.append('[');

            for (Object v : (Collection<?>) value) {
                if (sb.length() > 1) {
                    sb.append(',');
                }
                sb.append(formatCSVValue(v));
            }

            sb.append(']');

            return sb.toString();
        } else if (value instanceof ObjectId) {
            return formatCSVValue(((ObjectId) value).getId());
        } else if (value instanceof ImageIcon) {
            return "<icon>";
        }

        return value.toString();
    }

    public static URI getClasspathURI(String path) {
        try {
            return ClientHelper.class.getResource(path).toURI();
        } catch (URISyntaxException e) {
            // not very likely
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static String readFileAndRemoveHeader(final URI file) {
        if (file == null) {
            return "";
        }

        final InputStream stream;
        try {
            stream = file.toURL().openStream();
        } catch (Exception e) {
            return "";
        }

        final String result = readStreamAndRemoveHeader(stream);

        IOUtils.closeQuietly(stream);

        return result;
    }

    public static String readStreamAndRemoveHeader(final InputStream stream) {
        if (stream == null) {
            return "";
        }

        try {
            return IOUtils.readAllLinesAndRemoveHeader(stream);
        } catch (IOException e1) {
            return "";
        }
    }

    public static List<FileEntry> readFileProperties(URI propertiesFile) {

        final InputStream stream;
        try {
            stream = propertiesFile.toURL().openStream();
            if (stream == null) {
                return null;
            }
        } catch (Exception e) {
            LOG.error("Cannot open library file: " + propertiesFile, e);
            return null;
        }

        String classpathParent = null;
        if ("classpath".equalsIgnoreCase(propertiesFile.getScheme())) {
            String path = propertiesFile.getSchemeSpecificPart();
            int x = path.lastIndexOf('/');
            if (x > -1) {
                classpathParent = path.substring(0, x);
            }
        }

        if ("jar".equalsIgnoreCase(propertiesFile.getScheme())) {
            String path = propertiesFile.getSchemeSpecificPart();
            int x = path.lastIndexOf('/');
            if (x > -1) {
                path = path.substring(0, x);
                x = path.indexOf("!/");
                if (x > -1) {
                    classpathParent = path.substring(x + 1);
                }
            }
        }

        String fileParent = null;
        if ("file".equalsIgnoreCase(propertiesFile.getScheme())) {
            fileParent = (new File(propertiesFile)).getParent();
        }

        try {
            Properties properties = new Properties();
            properties.load(stream);
            stream.close();

            final List<FileEntry> result = new ArrayList<FileEntry>();
            for (String file : properties.stringPropertyNames()) {

                try {
                    URI uri = null;

                    if (classpathParent != null) {
                        URL url = ClientHelper.class.getResource(classpathParent + "/" + file);
                        if (url != null) {
                            uri = url.toURI();
                        }
                    }

                    if (fileParent != null) {
                        uri = (new File(fileParent, file)).toURI();
                    }

                    if (uri != null) {
                        result.add(new FileEntry(properties.getProperty(file), uri));
                    } else {
                        LOG.error("Cannot find library entry: " + file);
                    }
                } catch (URISyntaxException e) {
                    // ignore entry
                }
            }
            Collections.sort(result);

            return result;
        } catch (IOException e) {
            LOG.error("Cannot read library file: " + propertiesFile);
            return null;
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    public static Console openConsole(final Component parent, final ClientModel model, final URI file) {
        try {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            final Session groovySession = model.getClientSession().getSession();
            final String user = model.getClientSession().getSessionParameters().get(SessionParameter.USER);
            final String title = "GroovyConsole - Repsository: " + groovySession.getRepositoryInfo().getId();

            final Console console = new Console(parent.getClass().getClassLoader()) {
                @Override
                public void updateTitle() {
                    JFrame frame = (JFrame) getFrame();

                    if (getScriptFile() != null) {
                        frame.setTitle(((File) getScriptFile()).getName() + (getDirty() ? " * " : "") + " - " + title);
                    } else {
                        frame.setTitle(title);
                    }
                }
            };

            console.setVariable("session", groovySession);
            console.setVariable("binding", groovySession.getBinding());

            console.run();

            JMenu cmisMenu = new JMenu("CMIS");
            console.getFrame().getRootPane().getJMenuBar().add(cmisMenu);

            addConsoleMenu(cmisMenu, "CMIS 1.0 Specification", new URI(
                    "http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.html"));
            addConsoleMenu(cmisMenu, "CMIS 1.1 Specification", new URI(
                    "http://docs.oasis-open.org/cmis/CMIS/v1.1/CMIS-v1.1.html"));
            addConsoleMenu(cmisMenu, "OpenCMIS Documentation",
                    new URI("http://chemistry.apache.org/java/opencmis.html"));
            addConsoleMenu(cmisMenu, "OpenCMIS Client API JavaDoc", new URI(
                    "http://chemistry.apache.org/java/0.11.0/maven/apidocs/"));
            cmisMenu.addSeparator();
            JMenuItem menuItem = new JMenuItem("CMIS Session Details");
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AttributeSet style = console.getOutputStyle();
                    console.clearOutput();
                    console.appendOutputNl("Session ID:      " + groovySession.getBinding().getSessionId(), style);
                    console.appendOutputNl("Repository ID:   " + groovySession.getRepositoryInfo().getId(), style);
                    console.appendOutputNl("Repository name: " + groovySession.getRepositoryInfo().getName(), style);
                    console.appendOutputNl("Binding:         " + groovySession.getBinding().getBindingType(), style);
                    console.appendOutputNl("User:            " + user, style);
                }
            });
            cmisMenu.add(menuItem);

            console.getInputArea().setText(readFileAndRemoveHeader(file));

            return console;
        } catch (Exception ex) {
            showError(null, ex);
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
                } catch (IOException ex) {
                    showError(null, ex);
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
            engine.eval(new InputStreamReader(new FileInputStream(file), IOUtils.UTF8));
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
        } finally {
            parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public static class FileEntry implements Comparable<FileEntry> {
        private final String name;
        private final URI file;

        public FileEntry(String name, URI file) {
            this.name = name;
            this.file = file;
        }

        public String getName() {
            return name;
        }

        public URI getFile() {
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
            if (this == obj) {
                return true;
            }

            if (!(obj instanceof FileEntry)) {
                return false;
            }

            return name.equals(((FileEntry) obj).getName());
        }
    }
}
