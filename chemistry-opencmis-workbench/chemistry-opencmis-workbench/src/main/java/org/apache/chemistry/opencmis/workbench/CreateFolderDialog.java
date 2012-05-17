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
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.CreateDialog;

public class CreateFolderDialog extends CreateDialog {

    private static final long serialVersionUID = 1L;

    private JTextField nameField;
    private JComboBox typeBox;

    public CreateFolderDialog(Frame owner, ClientModel model) {
        super(owner, "Create Folder", model);
        createGUI();
    }

    private void createGUI() {
        final CreateFolderDialog thisDialog = this;

        nameField = new JTextField(60);
        createRow("Name:", nameField, 0);

        Object[] types = getTypes(BaseTypeId.CMIS_FOLDER.value());
        if (types.length == 0) {
            JOptionPane.showMessageDialog(this, "No creatable type!", "Creatable Types", JOptionPane.ERROR_MESSAGE);
            thisDialog.dispose();
            return;
        }

        typeBox = new JComboBox(types);
        typeBox.setSelectedIndex(0);
        createRow("Type:", typeBox, 1);

        JButton createButton = new JButton("Create Folder", ClientHelper.getIcon("newfolder.png"));
        createButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                String name = nameField.getText();
                String type = ((ObjectTypeItem) typeBox.getSelectedItem()).getObjectType().getId();

                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    ObjectId objectId = getClientModel().createFolder(name, type);

                    if (objectId != null) {
                        getClientModel().loadObject(objectId.getId());
                    }

                    thisDialog.setVisible(false);
                    thisDialog.dispose();
                } catch (Exception e) {
                    ClientHelper.showError(null, e);
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                    try {
                        getClientModel().reloadFolder();
                    } catch (Exception e) {
                        ClientHelper.showError(null, e);
                    }
                }
            }
        });
        createRow("", createButton, 3);

        getRootPane().setDefaultButton(createButton);

        showDialog();
    }
}
