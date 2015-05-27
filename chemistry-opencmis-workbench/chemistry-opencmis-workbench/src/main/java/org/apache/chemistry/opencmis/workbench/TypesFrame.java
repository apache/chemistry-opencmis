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

import static org.apache.chemistry.opencmis.commons.impl.CollectionsHelper.isNotEmpty;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.util.TypeUtils;
import org.apache.chemistry.opencmis.client.util.TypeUtils.ValidationError;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.workbench.icons.BaseTypeIcon;
import org.apache.chemistry.opencmis.workbench.icons.CreateTypeIcon;
import org.apache.chemistry.opencmis.workbench.icons.DeleteTypeIcon;
import org.apache.chemistry.opencmis.workbench.icons.SaveTypeIcon;
import org.apache.chemistry.opencmis.workbench.icons.TypeIcon;
import org.apache.chemistry.opencmis.workbench.icons.UpdateTypeIcon;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

public class TypesFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "CMIS Types";

    private static final int BUTTON_SAVE = 0;
    private static final int BUTTON_UPDATE = 1;
    private static final int BUTTON_DELETE = 2;
    private static final int BUTTON_CREATE = 3;

    private final ClientModel model;
    private RepositoryInfo repInfo;
    private ObjectType currentType;

    private JToolBar toolBar;
    private JButton[] toolbarButton;
    private JTree typesTree;
    private TypeSplitPane typePanel;

    public TypesFrame(ClientModel model) {
        super();

        this.model = model;
        repInfo = model.getRepositoryInfo();

        createGUI();
        loadData();
    }

    private void createGUI() {
        setTitle(WINDOW_TITLE + " - " + model.getRepositoryName());
        setIconImage(ClientHelper.getCmisIcon().getImage());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setPreferredSize(new Dimension((int) (screenSize.getWidth() / 1.5), (int) (screenSize.getHeight() / 1.5)));
        setMinimumSize(new Dimension(200, 60));
        setLayout(new BorderLayout());

        toolBar = new JToolBar("CMIS Types Toolbar", JToolBar.HORIZONTAL);

        toolbarButton = new JButton[4];

        JMenuItem menuItem;

        // -- save ---
        final JPopupMenu savePopup = new JPopupMenu();

        menuItem = new JMenuItem("Save Type Definition to XML");
        savePopup.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = createXmlFileChooser();
                fileChooser.setSelectedFile(new File(getFilename() + ".xml"));

                int chooseResult = fileChooser.showDialog(getRootPane(), "Save XML");
                if (chooseResult == JFileChooser.APPROVE_OPTION) {
                    OutputStream out = null;
                    try {
                        out = new BufferedOutputStream(new FileOutputStream(fileChooser.getSelectedFile()));
                        TypeUtils.writeToXML(currentType, out);
                        out.flush();
                    } catch (Exception e) {
                        ClientHelper.showError(getRootPane(), e);
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                }
            }
        });

        menuItem = new JMenuItem("Save Type Definition to JSON");
        savePopup.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = createJsonFileChooser();
                fileChooser.setSelectedFile(new File(getFilename() + ".json"));

                int chooseResult = fileChooser.showDialog(getRootPane(), "Save JSON");
                if (chooseResult == JFileChooser.APPROVE_OPTION) {
                    OutputStream out = null;
                    try {
                        out = new BufferedOutputStream(new FileOutputStream(fileChooser.getSelectedFile()));
                        TypeUtils.writeToJSON(currentType, out);
                        out.flush();
                    } catch (Exception e) {
                        ClientHelper.showError(getRootPane(), e);
                    } finally {
                        IOUtils.closeQuietly(out);
                    }
                }
            }
        });

        toolbarButton[BUTTON_SAVE] = new JButton("Save Type Definition", new SaveTypeIcon(
                ClientHelper.TOOLBAR_ICON_SIZE, ClientHelper.TOOLBAR_ICON_SIZE));
        toolbarButton[BUTTON_SAVE].setDisabledIcon(new SaveTypeIcon(ClientHelper.TOOLBAR_ICON_SIZE,
                ClientHelper.TOOLBAR_ICON_SIZE, false));
        toolbarButton[BUTTON_SAVE].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                savePopup.show(toolbarButton[BUTTON_SAVE], 0, toolbarButton[BUTTON_SAVE].getHeight());
            }
        });
        toolBar.add(toolbarButton[BUTTON_SAVE]);

        toolBar.addSeparator();

        // -- update --
        final JPopupMenu updatePopup = new JPopupMenu();
        menuItem = new JMenuItem("Load Type Definition from XML");
        updatePopup.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = createXmlFileChooser();

                int chooseResult = fileChooser.showDialog(getRootPane(), "Load XML");
                if (chooseResult == JFileChooser.APPROVE_OPTION) {
                    InputStream in = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()), 64 * 1024);
                        TypeDefinition type = TypeUtils.readFromXML(in);

                        if (checkTypeDefinition(type)) {
                            model.getClientSession().getSession().updateType(type);
                        }

                        loadData();
                    } catch (Exception e) {
                        ClientHelper.showError(getRootPane(), e);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                }
            }
        });

        menuItem = new JMenuItem("Load Type Definition from JSON");
        updatePopup.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = createJsonFileChooser();

                int chooseResult = fileChooser.showDialog(getRootPane(), "Load JSON");
                if (chooseResult == JFileChooser.APPROVE_OPTION) {
                    InputStream in = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()), 64 * 1024);
                        TypeDefinition type = TypeUtils.readFromJSON(in);

                        if (checkTypeDefinition(type)) {
                            model.getClientSession().getSession().updateType(type);
                        }

                        loadData();
                    } catch (Exception e) {
                        ClientHelper.showError(getRootPane(), e);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                }
            }
        });

        toolbarButton[BUTTON_UPDATE] = new JButton("Update Type", new UpdateTypeIcon(ClientHelper.TOOLBAR_ICON_SIZE,
                ClientHelper.TOOLBAR_ICON_SIZE));
        toolbarButton[BUTTON_UPDATE].setDisabledIcon(new UpdateTypeIcon(ClientHelper.TOOLBAR_ICON_SIZE,
                ClientHelper.TOOLBAR_ICON_SIZE, false));
        toolbarButton[BUTTON_UPDATE].setEnabled(repInfo.getCmisVersion() != CmisVersion.CMIS_1_0);
        toolbarButton[BUTTON_UPDATE].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                updatePopup.show(toolbarButton[BUTTON_UPDATE], 0, toolbarButton[BUTTON_UPDATE].getHeight());
            }
        });
        toolBar.add(toolbarButton[BUTTON_UPDATE]);

        // -- delete --
        toolbarButton[BUTTON_DELETE] = new JButton("Delete Type", new DeleteTypeIcon(ClientHelper.TOOLBAR_ICON_SIZE,
                ClientHelper.TOOLBAR_ICON_SIZE));
        toolbarButton[BUTTON_DELETE].setDisabledIcon(new DeleteTypeIcon(ClientHelper.TOOLBAR_ICON_SIZE,
                ClientHelper.TOOLBAR_ICON_SIZE, false));
        toolbarButton[BUTTON_DELETE].setEnabled(repInfo.getCmisVersion() != CmisVersion.CMIS_1_0);
        toolbarButton[BUTTON_DELETE].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                int answer = JOptionPane.showConfirmDialog(getOwner(), "Do you really want to delete the type "
                        + currentType.getId() + "?", "Delete Type", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);

                if (answer == JOptionPane.YES_OPTION) {
                    try {
                        model.getClientSession().getSession().deleteType(currentType.getId());
                    } catch (Exception e) {
                        ClientHelper.showError(getRootPane(), e);
                    }

                    loadData();
                }
            }
        });
        toolBar.add(toolbarButton[BUTTON_DELETE]);

        toolBar.addSeparator();

        // -- create --
        final JPopupMenu createPopup = new JPopupMenu();
        menuItem = new JMenuItem("Load Type Definition from XML");
        createPopup.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = createXmlFileChooser();

                int chooseResult = fileChooser.showDialog(getRootPane(), "Load XML");
                if (chooseResult == JFileChooser.APPROVE_OPTION) {
                    InputStream in = null;
                    try {
                        in = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()), 64 * 1024);
                        TypeDefinition type = TypeUtils.readFromXML(in);
                        if (checkTypeDefinition(type)) {
                            model.getClientSession().getSession().createType(type);
                        }

                        loadData();
                    } catch (Exception e) {
                        ClientHelper.showError(getRootPane(), e);
                    } finally {
                        IOUtils.closeQuietly(in);
                    }
                }
            }
        });

        menuItem = new JMenuItem("Load Type Definition from JSON");
        createPopup.add(menuItem);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = createJsonFileChooser();

                int chooseResult = fileChooser.showDialog(getRootPane(), "Load JSON");
                if (chooseResult == JFileChooser.APPROVE_OPTION) {
                    try {
                        InputStream in = new BufferedInputStream(new FileInputStream(fileChooser.getSelectedFile()),
                                64 * 1024);
                        TypeDefinition type = TypeUtils.readFromJSON(in);
                        in.close();

                        if (checkTypeDefinition(type)) {
                            model.getClientSession().getSession().createType(type);
                        }

                        loadData();
                    } catch (Exception e) {
                        ClientHelper.showError(getRootPane(), e);
                    }
                }
            }
        });

        toolbarButton[BUTTON_CREATE] = new JButton("Create Type", new CreateTypeIcon(ClientHelper.TOOLBAR_ICON_SIZE,
                ClientHelper.TOOLBAR_ICON_SIZE));
        toolbarButton[BUTTON_CREATE].setDisabledIcon(new CreateTypeIcon(ClientHelper.TOOLBAR_ICON_SIZE,
                ClientHelper.TOOLBAR_ICON_SIZE, false));
        toolbarButton[BUTTON_CREATE].setEnabled(repInfo.getCmisVersion() != CmisVersion.CMIS_1_0);
        toolbarButton[BUTTON_CREATE].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                createPopup.show(toolbarButton[BUTTON_CREATE], 0, toolbarButton[BUTTON_CREATE].getHeight());
            }
        });
        toolBar.add(toolbarButton[BUTTON_CREATE]);

        add(toolBar, BorderLayout.PAGE_START);

        typesTree = new JTree();
        typesTree.setRootVisible(false);
        typesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        typesTree.setCellRenderer(new TreeCellRenderer());

        typesTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((JTree) e.getSource())
                        .getLastSelectedPathComponent();

                if (node == null) {
                    return;
                }

                currentType = ((TypeNode) node.getUserObject()).getType();

                if (repInfo.getCmisVersion() != CmisVersion.CMIS_1_0) {
                    toolbarButton[BUTTON_UPDATE].setEnabled(currentType.getTypeMutability() != null
                            && Boolean.TRUE.equals(currentType.getTypeMutability().canUpdate()));
                    toolbarButton[BUTTON_DELETE].setEnabled(currentType.getTypeMutability() != null
                            && Boolean.TRUE.equals(currentType.getTypeMutability().canDelete()));
                }

                typePanel.setType(currentType);
            }
        });

        typePanel = new TypeSplitPane(model);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(typesTree), typePanel);
        splitPane.setDividerLocation(300);

        add(splitPane, BorderLayout.CENTER);

        ClientHelper.installEscapeBinding(this, getRootPane(), true);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JFileChooser createXmlFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return "XML CMIS Type Definition File";
            }

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".xml");
            }
        });

        return fileChooser;
    }

    private JFileChooser createJsonFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileFilter() {

            @Override
            public String getDescription() {
                return "JSON CMIS Type Definition File";
            }

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().endsWith(".json");
            }
        });

        return fileChooser;
    }

    private String getFilename() {
        if (currentType != null) {
            String filename = currentType.getId();
            filename = filename.replace(':', '_');
            filename = filename.replace('/', '_');
            filename = filename.replace('\\', '_');

            return filename;
        }

        return "type";
    }

    private boolean checkTypeDefinition(TypeDefinition type) {
        StringBuilder sb = new StringBuilder(128);

        List<ValidationError> typeResult = TypeUtils.validateTypeDefinition(type);

        if (isNotEmpty(typeResult)) {
            sb.append("\nType Definition:\n");

            for (ValidationError error : typeResult) {
                sb.append("- ");
                sb.append(error.toString());
                sb.append('\n');
            }
        }

        if (type.getPropertyDefinitions() != null) {
            for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
                List<ValidationError> propResult = TypeUtils.validatePropertyDefinition(propDef);

                if (isNotEmpty(propResult)) {
                    sb.append("\nProperty Definition '" + propDef.getId() + "':\n");

                    for (ValidationError error : propResult) {
                        sb.append("- ");
                        sb.append(error.toString());
                        sb.append('\n');
                    }
                }
            }
        }

        if (sb.length() == 0) {
            return true;
        }

        int answer = JOptionPane
                .showConfirmDialog(this, "The type defintion has the following issues.\n" + sb.toString()
                        + "\n\nDo you want to proceed anyway?", "Type Definition Validation",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        return answer == JOptionPane.YES_OPTION;
    }

    private void loadData() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

            List<Tree<ObjectType>> types = model.getTypeDescendants();
            for (Tree<ObjectType> tt : types) {
                addLevel(rootNode, tt);
            }

            DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);
            typesTree.setModel(treeModel);

            typesTree.setSelectionRow(0);
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
            return;
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    private void addLevel(DefaultMutableTreeNode parent, Tree<ObjectType> tree) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new TypeNode(tree.getItem()));
        parent.add(node);

        if (tree.getChildren() != null) {
            for (Tree<ObjectType> tt : tree.getChildren()) {
                addLevel(node, tt);
            }
        }
    }

    private static class TypeNode {
        private final ObjectType type;

        public TypeNode(ObjectType type) {
            this.type = type;
        }

        public ObjectType getType() {
            return type;
        }

        @Override
        public String toString() {
            return type.getDisplayName() + " (" + type.getId() + ")";
        }
    }

    private static class TreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;

        private static final Icon BASETYPE_ICON = new BaseTypeIcon(ClientHelper.OBJECT_ICON_SIZE,
                ClientHelper.OBJECT_ICON_SIZE);
        private static final Icon TYPE_ICON = new TypeIcon(ClientHelper.OBJECT_ICON_SIZE, ClientHelper.OBJECT_ICON_SIZE);

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            Object node = ((DefaultMutableTreeNode) value).getUserObject();
            if (node instanceof TypeNode) {
                if (((TypeNode) node).getType().isBaseType()) {
                    setIcon(BASETYPE_ICON);
                } else {
                    setIcon(TYPE_ICON);
                }
            }

            return comp;
        }
    }
}
