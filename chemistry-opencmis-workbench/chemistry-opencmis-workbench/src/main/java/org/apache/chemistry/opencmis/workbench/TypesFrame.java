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

import java.awt.Cursor;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

public class TypesFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "CMIS Types";

    private final ClientModel model;

    private JTree typesTree;
    private TypeSplitPane typePanel;

    public TypesFrame(ClientModel model) {
        super();

        this.model = model;
        createGUI();
        loadData();
    }

    private void createGUI() {
        setTitle(WINDOW_TITLE + " - " + model.getRepositoryName());
        setPreferredSize(new Dimension(1000, 700));
        setMinimumSize(new Dimension(200, 60));

        typesTree = new JTree();
        typesTree.setRootVisible(false);
        typesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        typesTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) ((JTree) e.getSource())
                        .getLastSelectedPathComponent();

                if (node == null) {
                    return;
                }

                ObjectType type = ((TypeNode) node.getUserObject()).getType();
                typePanel.setType(type);
            }
        });

        typePanel = new TypeSplitPane(model);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(typesTree), typePanel);
        splitPane.setDividerLocation(300);

        add(splitPane);

        ClientHelper.installEscapeBinding(this, getRootPane(), true);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
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

    static class TypeNode {
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
}
