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

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.LinkAccess;
import org.apache.chemistry.opencmis.workbench.ClientHelper;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.model.ClientModelEvent;
import org.apache.chemistry.opencmis.workbench.model.ObjectListener;
import org.apache.chemistry.opencmis.workbench.swing.InfoPanel;

public class ObjectPanel extends InfoPanel implements ObjectListener {

    private static final long serialVersionUID = 1L;

    private final ClientModel model;

    private JTextField nameField;
    private JTextField idField;
    private JTextField typeField;
    private JTextField basetypeField;
    private JTextField contentUrlField;
    private InfoList paths;
    private InfoList allowableActionsList;
    private JButton refreshButton;

    public ObjectPanel(ClientModel model) {
        super();

        this.model = model;
        model.addObjectListener(this);

        createGUI();
    }

    public void objectLoaded(ClientModelEvent event) {
        CmisObject object = model.getCurrentObject();

        if (object == null) {
            nameField.setText("");
            idField.setText("");
            typeField.setText("");
            basetypeField.setText("");
            paths.removeAll();
            contentUrlField.setText("");
            allowableActionsList.removeAll();
            refreshButton.setEnabled(false);
        } else {
            try {
                nameField.setText(object.getName());
                idField.setText(object.getId());
                typeField.setText(object.getType().getId());
                basetypeField.setText(object.getBaseTypeId().toString());

                if (object instanceof FileableCmisObject) {
                    if (object instanceof Folder) {
                        paths.setList(Collections.singletonList(((Folder) object).getPath()));
                    } else {
                        paths.setList(Collections.singletonList(""));
                        final FileableCmisObject pathObject = (FileableCmisObject) object;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    List<String> pathsList = (pathObject).getPaths();
                                    if ((pathsList == null) || (pathsList.size() == 0)) {
                                        paths.setList(Collections.singletonList("(unfiled)"));
                                    } else {
                                        paths.setList(pathsList);
                                    }
                                } catch (Exception e) {
                                    paths.setList(Collections.singletonList("(???)"));
                                }
                                ObjectPanel.this.revalidate();
                            }
                        });
                    }
                } else {
                    paths.setList(Collections.singletonList("(not filable)"));
                }

                String docUrl = getDocumentURL(object, model.getClientSession().getSession());
                if (docUrl != null) {
                    contentUrlField.setText(docUrl);
                } else {
                    contentUrlField.setText("(not available)");
                }

                if (object.getAllowableActions() != null) {
                    allowableActionsList.setList(object.getAllowableActions().getAllowableActions());
                } else {
                    allowableActionsList.setList(Collections.singletonList("(missing)"));
                }

                refreshButton.setEnabled(true);
            } catch (Exception e) {
                ClientHelper.showError(this, e);
            }
        }

        revalidate();
    }

    private void createGUI() {
        setupGUI();

        nameField = addLine("Name:", true);
        idField = addLine("Id:");
        typeField = addLine("Type:");
        basetypeField = addLine("Base Type:");
        paths = addComponent("Paths:", new InfoList());
        contentUrlField = addLink("Content URL:");
        allowableActionsList = addComponent("Allowable Actions:", new InfoList());
        refreshButton = addComponent("", new JButton("Refresh"));
        refreshButton.setEnabled(false);

        refreshButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    model.reloadObject();
                } catch (Exception ex) {
                    ClientHelper.showError(null, ex);
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
    }

    public String getDocumentURL(final CmisObject document, final Session session) {
        if (session.getBinding().getObjectService() instanceof LinkAccess) {
            return ((LinkAccess) session.getBinding().getObjectService()).loadContentLink(session.getRepositoryInfo()
                    .getId(), document.getId());
        }

        return null;
    }
}
