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

import groovy.ui.Console;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.apache.chemistry.opencmis.workbench.ClientHelper.FileEntry;
import org.apache.chemistry.opencmis.workbench.details.DetailsTabs;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.model.ClientSession;

public class ClientFrame extends JFrame implements WindowListener {

    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "CMIS Workbench";

    private static final int BUTTON_CONNECT = 0;
    private static final int BUTTON_REPOSITORY_INFO = 1;
    private static final int BUTTON_TYPES = 2;
    private static final int BUTTON_QUERY = 3;
    private static final int BUTTON_CHANGELOG = 4;
    private static final int BUTTON_CONSOLE = 5;
    private static final int BUTTON_TCK = 6;
    private static final int BUTTON_CREATE_DOCUMENT = 7;
    private static final int BUTTON_CREATE_FOLDER = 8;
    private static final int BUTTON_LOG = 9;
    private static final int BUTTON_INFO = 10;

    private static final String PREFS_X = "x";
    private static final String PREFS_Y = "y";
    private static final String PREFS_WIDTH = "width";
    private static final String PREFS_HEIGHT = "height";
    private static final String PREFS_DIV = "div";

    private static final String GROOVY_SCRIPT_FOLDER = "/scripts/";
    private static final String GROOVY_SCRIPT_LIBRARY = "script-library.properties";

    private final Preferences prefs = Preferences.userNodeForPackage(this.getClass());

    private LoginDialog loginDialog;
    private LogFrame logFrame;
    private InfoDialog infoDialog;

    private JToolBar toolBar;
    private JButton[] toolbarButton;
    private JPopupMenu toolbarConsolePopup;

    private JSplitPane split;
    private FolderPanel folderPanel;
    private DetailsTabs detailsTabs;

    private final ClientModel model;

    public ClientFrame() {
        super();

        model = new ClientModel();
        createGUI();
        showLoginForm();
    }

    private void createGUI() {
        setTitle(WINDOW_TITLE);

        ImageIcon icon = ClientHelper.getIcon("icon.png");
        if (icon != null) {
            setIconImage(icon.getImage());
        }

        setLayout(new BorderLayout());

        final ClientFrame thisFrame = this;
        loginDialog = new LoginDialog(this);
        logFrame = new LogFrame();
        infoDialog = new InfoDialog(this);

        Container pane = getContentPane();

        toolBar = new JToolBar("CMIS Toolbar", JToolBar.HORIZONTAL);

        toolbarButton = new JButton[11];

        toolbarButton[BUTTON_CONNECT] = new JButton("Connection", ClientHelper.getIcon("connect.png"));
        toolbarButton[BUTTON_CONNECT].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLoginForm();
            }
        });

        toolBar.add(toolbarButton[BUTTON_CONNECT]);

        toolBar.addSeparator();

        toolbarButton[BUTTON_REPOSITORY_INFO] = new JButton("Repository Info",
                ClientHelper.getIcon("repository-info.png"));
        toolbarButton[BUTTON_REPOSITORY_INFO].setEnabled(false);
        toolbarButton[BUTTON_REPOSITORY_INFO].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new RepositoryInfoFrame(model);
            }
        });

        toolBar.add(toolbarButton[BUTTON_REPOSITORY_INFO]);

        toolbarButton[BUTTON_TYPES] = new JButton("Types", ClientHelper.getIcon("types.png"));
        toolbarButton[BUTTON_TYPES].setEnabled(false);
        toolbarButton[BUTTON_TYPES].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new TypesFrame(model);
            }
        });

        toolBar.add(toolbarButton[BUTTON_TYPES]);

        toolbarButton[BUTTON_QUERY] = new JButton("Query", ClientHelper.getIcon("query.png"));
        toolbarButton[BUTTON_QUERY].setEnabled(false);
        toolbarButton[BUTTON_QUERY].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new QueryFrame(model);
            }
        });

        toolBar.add(toolbarButton[BUTTON_QUERY]);

        toolbarButton[BUTTON_CHANGELOG] = new JButton("Change Log", ClientHelper.getIcon("changelog.png"));
        toolbarButton[BUTTON_CHANGELOG].setEnabled(false);
        toolbarButton[BUTTON_CHANGELOG].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ChangeLogFrame(model);
            }
        });

        toolBar.add(toolbarButton[BUTTON_CHANGELOG]);

        toolbarButton[BUTTON_CONSOLE] = new JButton("Console", ClientHelper.getIcon("console.png"));
        toolbarButton[BUTTON_CONSOLE].setEnabled(false);
        toolbarButton[BUTTON_CONSOLE].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                toolbarConsolePopup.show(toolbarButton[BUTTON_CONSOLE], 0, toolbarButton[BUTTON_CONSOLE].getHeight());
            }
        });

        toolBar.add(toolbarButton[BUTTON_CONSOLE]);

        toolbarConsolePopup = new JPopupMenu();
        for (FileEntry fe : readScriptLibrary()) {
            JMenuItem menuItem = new JMenuItem(fe.getName());
            final String file = fe.getFile();
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    openConsole(file);
                }
            });
            toolbarConsolePopup.add(menuItem);
        }

        toolbarButton[BUTTON_TCK] = new JButton("TCK", ClientHelper.getIcon("tck.png"));
        toolbarButton[BUTTON_TCK].setEnabled(false);
        toolbarButton[BUTTON_TCK].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new TckDialog(thisFrame, model);
            }
        });

        toolBar.add(toolbarButton[BUTTON_TCK]);

        toolBar.addSeparator();

        toolbarButton[BUTTON_CREATE_DOCUMENT] = new JButton("Create Document", ClientHelper.getIcon("newdocument.png"));
        toolbarButton[BUTTON_CREATE_DOCUMENT].setEnabled(false);
        toolbarButton[BUTTON_CREATE_DOCUMENT].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CreateDocumentDialog(thisFrame, model);
            }
        });

        toolBar.add(toolbarButton[BUTTON_CREATE_DOCUMENT]);

        toolbarButton[BUTTON_CREATE_FOLDER] = new JButton("Create Folder", ClientHelper.getIcon("newfolder.png"));
        toolbarButton[BUTTON_CREATE_FOLDER].setEnabled(false);
        toolbarButton[BUTTON_CREATE_FOLDER].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new CreateFolderDialog(thisFrame, model);
            }
        });

        toolBar.add(toolbarButton[BUTTON_CREATE_FOLDER]);

        toolBar.addSeparator();

        toolbarButton[BUTTON_LOG] = new JButton("Log", ClientHelper.getIcon("log.png"));
        toolbarButton[BUTTON_LOG].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logFrame.showFrame();
            }
        });

        toolBar.add(toolbarButton[BUTTON_LOG]);

        toolbarButton[BUTTON_INFO] = new JButton("Info", ClientHelper.getIcon("info.png"));
        toolbarButton[BUTTON_INFO].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                infoDialog.showDialog();
            }
        });

        toolBar.add(toolbarButton[BUTTON_INFO]);

        pane.add(toolBar, BorderLayout.PAGE_START);

        folderPanel = new FolderPanel(model);
        detailsTabs = new DetailsTabs(model);

        split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, folderPanel, new JScrollPane(detailsTabs));

        pane.add(split, BorderLayout.CENTER);

        addWindowListener(this);

        setPreferredSize(new Dimension(prefs.getInt(PREFS_WIDTH, 1000), prefs.getInt(PREFS_HEIGHT, 600)));
        setMinimumSize(new Dimension(200, 60));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();

        split.setDividerLocation(prefs.getInt(PREFS_DIV, 500));

        if (prefs.getInt(PREFS_X, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
            setLocationRelativeTo(null);
        } else {
            setLocation(prefs.getInt(PREFS_X, 0), prefs.getInt(PREFS_Y, 0));
        }

        setVisible(true);
    }

    private void showLoginForm() {
        loginDialog.showDialog();
        if (!loginDialog.isCanceled()) {
            ClientSession clientSession = loginDialog.getClientSession();

            model.setClientSession(clientSession);

            try {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                model.loadFolder(clientSession.getSession().getRepositoryInfo().getRootFolderId(), false);
                model.loadObject(clientSession.getSession().getRepositoryInfo().getRootFolderId());

                toolbarButton[BUTTON_REPOSITORY_INFO].setEnabled(true);
                toolbarButton[BUTTON_TYPES].setEnabled(true);
                toolbarButton[BUTTON_QUERY].setEnabled(model.supportsQuery());
                toolbarButton[BUTTON_CHANGELOG].setEnabled(model.supportsChangeLog());
                toolbarButton[BUTTON_CONSOLE].setEnabled(true);
                toolbarButton[BUTTON_TCK].setEnabled(true);
                toolbarButton[BUTTON_CREATE_DOCUMENT].setEnabled(true);
                toolbarButton[BUTTON_CREATE_FOLDER].setEnabled(true);

                setTitle(WINDOW_TITLE + " - " + clientSession.getSession().getRepositoryInfo().getName());
            } catch (Exception ex) {
                toolbarButton[BUTTON_REPOSITORY_INFO].setEnabled(false);
                toolbarButton[BUTTON_TYPES].setEnabled(false);
                toolbarButton[BUTTON_QUERY].setEnabled(false);
                toolbarButton[BUTTON_CHANGELOG].setEnabled(false);
                toolbarButton[BUTTON_CONSOLE].setEnabled(false);
                toolbarButton[BUTTON_TCK].setEnabled(false);
                toolbarButton[BUTTON_CREATE_DOCUMENT].setEnabled(false);
                toolbarButton[BUTTON_CREATE_FOLDER].setEnabled(false);

                ClientHelper.showError(null, ex);

                setTitle(WINDOW_TITLE);
            } finally {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    private void openConsole(String file) {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            Console console = new Console(this.getClass().getClassLoader());
            console.setVariable("session", model.getClientSession().getSession());
            console.setVariable("binding", model.getClientSession().getSession().getBinding());

            console.run();

            JMenu cmisMenu = new JMenu("CMIS");
            console.getFrame().getRootPane().getJMenuBar().add(cmisMenu);

            addConsoleMenu(cmisMenu, "CMIS 1.0 Specification", new URI(
                    "http://docs.oasis-open.org/cmis/CMIS/v1.0/os/cmis-spec-v1.0.html"));
            addConsoleMenu(cmisMenu, "OpenCMIS Documentation", new URI(
                    "http://incubator.apache.org/chemistry/opencmis.html"));
            addConsoleMenu(
                    cmisMenu,
                    "OpenCMIS Client API JavaDoc",
                    new URI(
                            "http://incubator.apache.org/chemistry/javadoc/org/apache/chemistry/opencmis/client/api/package-summary.html"));

            console.getInputArea().setText(ClientHelper.readFileAndRemoveHeader(file));
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void addConsoleMenu(JMenu menu, String title, final URI url) {
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

    private List<FileEntry> readScriptLibrary() {
        List<FileEntry> result = ClientHelper.readFileProperties(GROOVY_SCRIPT_FOLDER + GROOVY_SCRIPT_LIBRARY,
                GROOVY_SCRIPT_FOLDER);
        if (result == null) {
            result = Collections.singletonList(new FileEntry("Groovy Console", null));
        }

        return result;
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        Point p = getLocation();
        prefs.putInt(PREFS_X, p.x);
        prefs.putInt(PREFS_Y, p.y);
        prefs.putInt(PREFS_WIDTH, getWidth());
        prefs.putInt(PREFS_HEIGHT, getHeight());
        prefs.putInt(PREFS_DIV, split.getDividerLocation());
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowDeactivated(WindowEvent e) {
    }
}
