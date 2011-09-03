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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.CreateDialog;

public class CreateDocumentDialog extends CreateDialog {

    private static final long serialVersionUID = 1L;

    private JTextField nameField;
    private JComboBox typeBox;
    private JTextField filenameField;
    private JComboBox versioningStateBox;

    public CreateDocumentDialog(Frame owner, ClientModel model) {
        this(owner, model, null);
    }

    public CreateDocumentDialog(Frame owner, ClientModel model, File file) {
        super(owner, "Create Document", model);
        createGUI(file);
    }

    private void createGUI(File file) {
        final CreateDocumentDialog thisDialog = this;

        nameField = new JTextField(60);
        createRow("Name:", nameField, 0);

        typeBox = new JComboBox(getTypes(BaseTypeId.CMIS_DOCUMENT.value()));
        typeBox.setSelectedIndex(0);
        typeBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                DocumentTypeDefinition type = (DocumentTypeDefinition) ((ObjectTypeItem) typeBox.getSelectedItem())
                        .getObjectType();
                if (type.isVersionable()) {
                    versioningStateBox.setSelectedItem(VersioningState.MAJOR);
                } else {
                    versioningStateBox.setSelectedItem(VersioningState.NONE);
                }
            }
        });

        createRow("Type:", typeBox, 1);

        JPanel filePanel = new JPanel(new BorderLayout());

        filenameField = new JTextField(30);
        filePanel.add(filenameField, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                int chooseResult = fileChooser.showDialog(filenameField, "Select");
                if (chooseResult == JFileChooser.APPROVE_OPTION) {
                    if (fileChooser.getSelectedFile().isFile()) {
                        setFile(fileChooser.getSelectedFile());
                    }
                }
            }
        });
        filePanel.add(browseButton, BorderLayout.LINE_END);

        createRow("File:", filePanel, 2);

        versioningStateBox = new JComboBox(new Object[] { VersioningState.NONE, VersioningState.MAJOR,
                VersioningState.MINOR, VersioningState.CHECKEDOUT });
        if (((DocumentTypeDefinition) ((ObjectTypeItem) typeBox.getSelectedItem()).getObjectType()).isVersionable()) {
            versioningStateBox.setSelectedItem(VersioningState.MAJOR);
        } else {
            versioningStateBox.setSelectedItem(VersioningState.NONE);
        }
        createRow("Versioning State:", versioningStateBox, 3);

        JButton createButton = new JButton("Create Document");
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String name = nameField.getText();
                String type = ((ObjectTypeItem) typeBox.getSelectedItem()).getObjectType().getId();
                String filename = filenameField.getText();

                try {
                    getClientModel().createDocument(name, type, filename,
                            (VersioningState) versioningStateBox.getSelectedItem());
                    getClientModel().reloadFolder();

                    thisDialog.setVisible(false);
                    thisDialog.dispose();
                } catch (Exception e) {
                    ClientHelper.showError(null, e);
                }
            }
        });
        createRow("", createButton, 4);

        if (file != null) {
            setFile(file);
        }

        getRootPane().setDefaultButton(createButton);
        
        showDialog();
    }

    private void setFile(File file) {
        filenameField.setText(file.getAbsolutePath());
        if (nameField.getText().trim().length() == 0) {
            nameField.setText(file.getName());
        }
    }
}
