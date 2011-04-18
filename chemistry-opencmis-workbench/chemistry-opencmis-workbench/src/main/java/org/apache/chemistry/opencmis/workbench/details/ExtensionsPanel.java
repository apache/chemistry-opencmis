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
package org.apache.chemistry.opencmis.workbench.details;

import java.awt.Color;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.model.ClientModelEvent;
import org.apache.chemistry.opencmis.workbench.model.ObjectListener;

public class ExtensionsPanel extends JPanel implements ObjectListener {

    private static final long serialVersionUID = 1L;

    private final ClientModel model;

    private JTree extensionsTree;

    public ExtensionsPanel(ClientModel model) {
        super();

        this.model = model;
        model.addObjectListener(this);

        createGUI();
    }

    @Override
    public void objectLoaded(ClientModelEvent event) {
        CmisObject object = model.getCurrentObject();

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();

        if (object != null) {
            List<CmisExtensionElement> extensions;

            // object extensions
            extensions = object.getExtensions(ExtensionLevel.OBJECT);
            if ((extensions != null) && (!extensions.isEmpty())) {
                DefaultMutableTreeNode objectRootNode = new DefaultMutableTreeNode("Object");
                addExtension(objectRootNode, extensions);
                rootNode.add(objectRootNode);
            }

            // property extensions
            extensions = object.getExtensions(ExtensionLevel.PROPERTIES);
            if ((extensions != null) && (!extensions.isEmpty())) {
                DefaultMutableTreeNode propertiesRootNode = new DefaultMutableTreeNode("Properties");
                addExtension(propertiesRootNode, extensions);
                rootNode.add(propertiesRootNode);
            }

            // allowable actions extensions
            extensions = object.getExtensions(ExtensionLevel.ALLOWABLE_ACTIONS);
            if ((extensions != null) && (!extensions.isEmpty())) {
                DefaultMutableTreeNode allowableActionsRootNode = new DefaultMutableTreeNode("Allowable Actions");
                addExtension(allowableActionsRootNode, extensions);
                rootNode.add(allowableActionsRootNode);
            }

            // ACL extensions
            extensions = object.getExtensions(ExtensionLevel.ACL);
            if ((extensions != null) && (!extensions.isEmpty())) {
                DefaultMutableTreeNode aclRootNode = new DefaultMutableTreeNode("ACL");
                addExtension(aclRootNode, extensions);
                rootNode.add(aclRootNode);
            }

            // policies extensions
            extensions = object.getExtensions(ExtensionLevel.POLICIES);
            if ((extensions != null) && (!extensions.isEmpty())) {
                DefaultMutableTreeNode policiesRootNode = new DefaultMutableTreeNode("Policies");
                addExtension(policiesRootNode, extensions);
                rootNode.add(policiesRootNode);
            }
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(rootNode);

        extensionsTree.setModel(treeModel);
    }

    private void addExtension(DefaultMutableTreeNode parent, List<CmisExtensionElement> extensions) {
        if ((extensions == null) || (extensions.isEmpty())) {
            return;
        }

        for (CmisExtensionElement ext : extensions) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(new ExtensionNode(ext));
            parent.add(node);

            if (ext.getChildren().size() > 0) {
                addExtension(node, ext.getChildren());
            }
        }
    }

    private void createGUI() {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBorder(BorderFactory.createEmptyBorder());
        setBackground(Color.WHITE);

        extensionsTree = new JTree();
        extensionsTree.setRootVisible(false);
        extensionsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        extensionsTree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        extensionsTree.setModel(new DefaultTreeModel(null));

        JScrollPane pane = new JScrollPane(extensionsTree);
        pane.setBorder(BorderFactory.createEmptyBorder());

        add(pane);
    }

    static class ExtensionNode {
        private final CmisExtensionElement extension;

        public ExtensionNode(CmisExtensionElement extension) {
            this.extension = extension;
        }

        @Override
        public String toString() {
            return (extension.getNamespace() == null ? "" : "{" + extension.getNamespace() + "}") + extension.getName()
                    + (!extension.getAttributes().isEmpty() ? " " + extension.getAttributes() : "")
                    + (extension.getChildren().isEmpty() ? ": " + extension.getValue() : "");
        }
    }
}
