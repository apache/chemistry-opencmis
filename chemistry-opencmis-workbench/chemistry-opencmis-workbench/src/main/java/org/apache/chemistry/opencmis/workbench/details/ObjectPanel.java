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
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JTextField;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.AbstractAtomPubService;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.AtomPubParser;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.ObjectServiceImpl;
import org.apache.chemistry.opencmis.workbench.ClientHelper;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.model.ClientModelEvent;
import org.apache.chemistry.opencmis.workbench.model.ObjectListener;
import org.apache.chemistry.opencmis.workbench.swing.InfoPanel;

public class ObjectPanel extends InfoPanel implements ObjectListener {

    private static final long serialVersionUID = 1L;

    private ClientModel model;

    private JTextField nameField;
    private JTextField idField;
    private JTextField typeField;
    private JTextField basetypeField;
    // private JTextField contentUrlField;
    private JList allowableActionsList;
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
            // contentUrlField.setText("");
            allowableActionsList.removeAll();
            refreshButton.setEnabled(false);
        } else {
            try {
                nameField.setText(object.getName());
                idField.setText(object.getId());
                typeField.setText(object.getType().getId());
                basetypeField.setText(object.getBaseTypeId().toString());
                // String docUrl = getDocumentURL(object,
                // model.getClientSession().getSession());
                // contentUrlField.setText(docUrl == null ? "" : docUrl);
                if (object.getAllowableActions() != null) {
                    allowableActionsList.setListData(object.getAllowableActions().getAllowableActions().toArray());
                } else {
                    allowableActionsList.setListData(new String[] { "(missing)" });
                }
                refreshButton.setEnabled(true);
            } catch (Exception e) {
                ClientHelper.showError(this, e);
            }
        }
    }

    private void createGUI() {
        setupGUI();

        nameField = addLine("Name:", true);
        idField = addLine("Id:");
        typeField = addLine("Type:");
        basetypeField = addLine("Base Type:");
        // contentUrlField = addLine("Content URL:");
        allowableActionsList = addComponent("Allowable Actions:", new JList());
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
        String link = null;

        if (!(session.getBinding().getObjectService() instanceof ObjectServiceImpl)) {
            return null;
        }

        try {
            Method loadLink = AbstractAtomPubService.class.getDeclaredMethod("loadLink", new Class[] { String.class,
                    String.class, String.class, String.class });
            loadLink.setAccessible(true);
            link = (String) loadLink.invoke(session.getBinding().getObjectService(), session.getRepositoryInfo()
                    .getId(), document.getId(), AtomPubParser.LINK_REL_CONTENT, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return link;
    }
}
