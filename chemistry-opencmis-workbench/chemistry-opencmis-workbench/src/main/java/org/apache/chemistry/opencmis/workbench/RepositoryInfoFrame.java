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

import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.InfoPanel;

public class RepositoryInfoFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "CMIS Repository Info";

    private final ClientModel model;

    public RepositoryInfoFrame(ClientModel model) {
        super();

        this.model = model;
        createGUI();
    }

    private void createGUI() {
        setTitle(WINDOW_TITLE + " - " + model.getRepositoryName());
        setPreferredSize(new Dimension(700, 700));
        setMinimumSize(new Dimension(200, 60));

        RepositoryInfo repInfo = null;
        try {
            repInfo = model.getRepositoryInfo();
        } catch (Exception e) {
            ClientHelper.showError(this, e);
            dispose();
            return;
        }

        add(new JScrollPane(new RepositoryInfoPanel(model, repInfo)));

        ClientHelper.installEscapeBinding(this, getRootPane(), true);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    static class RepositoryInfoPanel extends InfoPanel {

        private static final long serialVersionUID = 1L;

        private final RepositoryInfo repInfo;

        public RepositoryInfoPanel(ClientModel model, RepositoryInfo repInfo) {
            super(model);

            this.repInfo = repInfo;
            createGUI();
        }

        private void createGUI() {
            setupGUI();

            addLine("Name:", true).setText(repInfo.getName());
            addLine("Id:").setText(repInfo.getId());
            addLine("Description:").setText(repInfo.getDescription());
            addLine("Vendor:").setText(repInfo.getVendorName());
            addLine("Product:").setText(repInfo.getProductName() + " " + repInfo.getProductVersion());
            addLine("CMIS Version:").setText(repInfo.getCmisVersionSupported());
            addId("Root folder Id:").setText(repInfo.getRootFolderId());
            addLine("Latest change token:").setText(repInfo.getLatestChangeLogToken());
            addLink("Thin client URI:").setText(repInfo.getThinClientUri());
            addLine("Principal id anonymous:").setText(repInfo.getPrincipalIdAnonymous());
            addLine("Principal id anyone:").setText(repInfo.getPrincipalIdAnyone());
            addYesNoLabel("Changes incomplete:").setValue(is(repInfo.getChangesIncomplete()));
            addLine("Changes on type:").setText(
                    repInfo.getChangesOnType() == null ? "" : repInfo.getChangesOnType().toString());

            if (repInfo.getCapabilities() != null) {
                RepositoryCapabilities cap = repInfo.getCapabilities();

                addLine("Capabilities:", true).setText("");

                addYesNoLabel("Get descendants supported:").setValue(is(cap.isGetDescendantsSupported()));
                addYesNoLabel("Get folder tree supported:").setValue(is(cap.isGetFolderTreeSupported()));
                addYesNoLabel("Unfiling supported:").setValue(is(cap.isUnfilingSupported()));
                addYesNoLabel("Multifiling supported:").setValue(is(cap.isMultifilingSupported()));
                addYesNoLabel("Version specific filing supported:")
                        .setValue(is(cap.isVersionSpecificFilingSupported()));
                addLine("Query:").setText(str(cap.getQueryCapability()));
                addLine("Joins:").setText(str(cap.getJoinCapability()));
                addYesNoLabel("All versions searchable:").setValue(is(cap.isAllVersionsSearchableSupported()));
                addYesNoLabel("PWC searchable:").setValue(is(cap.isPwcSearchableSupported()));
                addYesNoLabel("PWC updatable:").setValue(is(cap.isPwcUpdatableSupported()));
                addLine("Content stream updates:").setText(str(cap.getContentStreamUpdatesCapability()));
                addLine("Renditions:").setText(str(cap.getRenditionsCapability()));
                addLine("Changes:").setText(str(cap.getChangesCapability()));
                addLine("ACLs:").setText(str(cap.getAclCapability()));
            }

            if (repInfo.getAclCapabilities() != null) {
                AclCapabilities cap = repInfo.getAclCapabilities();

                addLine("ACL Capabilities:", true).setText("");

                addLine("Supported permissions:").setText(str(cap.getSupportedPermissions()));
                addLine("ACL propagation:").setText(str(cap.getAclPropagation()));

                if (cap.getPermissions() != null) {
                    String[][] data = new String[cap.getPermissions().size()][2];

                    int i = 0;
                    for (PermissionDefinition pd : cap.getPermissions()) {
                        data[i][0] = pd.getId();
                        data[i][1] = pd.getDescription();
                        i++;
                    }

                    JTable permTable = new JTable(data, new String[] { "Permission", "Description" });
                    permTable.setFillsViewportHeight(true);
                    addComponent("Permissions:", new JScrollPane(permTable));
                }

                if (cap.getPermissionMapping() != null) {
                    String[][] data = new String[cap.getPermissionMapping().size()][2];

                    int i = 0;
                    for (PermissionMapping pm : cap.getPermissionMapping().values()) {
                        data[i][0] = pm.getKey();
                        data[i][1] = (pm.getPermissions() == null ? "" : pm.getPermissions().toString());
                        i++;
                    }

                    JTable permMapTable = new JTable(data, new String[] { "Key", "Permissions" });
                    permMapTable.setFillsViewportHeight(true);
                    addComponent("Permission mapping:", new JScrollPane(permMapTable));
                }
            }

            if (repInfo.getExtensions() != null && !repInfo.getExtensions().isEmpty()) {
                JTree extensionsTree = new JTree();
                extensionsTree.setRootVisible(false);
                extensionsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

                DefaultMutableTreeNode extRootNode = new DefaultMutableTreeNode("Extensions");
                addExtension(extRootNode, repInfo.getExtensions());

                extensionsTree.setModel(new DefaultTreeModel(extRootNode));

                addComponent("Extensions:", new JScrollPane(extensionsTree));
            }
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

        private boolean is(Boolean b) {
            if (b == null) {
                return false;
            }

            return b.booleanValue();
        }

        private String str(Object o) {
            if (o == null) {
                return "?";
            }

            return o.toString();
        }

        static class ExtensionNode {
            private final CmisExtensionElement extension;

            public ExtensionNode(CmisExtensionElement extension) {
                this.extension = extension;
            }

            @Override
            public String toString() {
                return (extension.getNamespace() == null ? "" : "{" + extension.getNamespace() + "}")
                        + extension.getName()
                        + (!extension.getAttributes().isEmpty() ? " " + extension.getAttributes() : "")
                        + (extension.getChildren().isEmpty() ? ": " + extension.getValue() : "");
            }
        }
    }
}
