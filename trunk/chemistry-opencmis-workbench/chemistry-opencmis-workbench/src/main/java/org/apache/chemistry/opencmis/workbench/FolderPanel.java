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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.model.ClientModelEvent;
import org.apache.chemistry.opencmis.workbench.model.FolderListener;
import org.apache.chemistry.opencmis.workbench.model.ObjectListener;

public class FolderPanel extends JPanel implements FolderListener, ObjectListener {

    private static final long serialVersionUID = 1L;

    private final ClientModel model;

    private String parentId;

    private JButton upButton;
    private JTextField pathField;
    private JButton goButton;
    private FolderTable folderTable;

    public FolderPanel(ClientModel model) {
        super();

        this.model = model;
        model.addFolderListener(this);
        model.addObjectListener(this);
        createGUI();
    }

    public void folderLoaded(ClientModelEvent event) {
        Folder currentFolder = event.getClientModel().getCurrentFolder();

        if (currentFolder != null) {
            String path = currentFolder.getPath();
            pathField.setText(path);

            Folder parent = currentFolder.getFolderParent();
            if (parent == null) {
                parentId = null;
                upButton.setEnabled(false);
            } else {
                parentId = parent.getId();
                upButton.setEnabled(true);
            }
        } else {
            pathField.setText("???");
            parentId = null;
            upButton.setEnabled(false);
        }
    }

    public void objectLoaded(ClientModelEvent event) {
        if ((folderTable.getSelectedRow() > -1) && (event.getClientModel().getCurrentObject() != null)) {
            String selId = folderTable.getValueAt(folderTable.getSelectedRow(), FolderTable.ID_COLUMN).toString();
            String curId = event.getClientModel().getCurrentObject().getId();

            if (!curId.equals(selId)) {
                folderTable.clearSelection();
            }
        }
    }

    private void createGUI() {
        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));

        upButton = new JButton("up");
        upButton.setEnabled(false);
        upButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    ObjectId objectId = model.loadFolder(parentId, false);
                    model.loadObject(objectId.getId());
                } catch (Exception ex) {
                    ClientHelper.showError(null, ex);
                    return;
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        panel.add(upButton);

        pathField = new JTextField("");
        pathField.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    loadFolder();
                }
            }

            public void keyPressed(KeyEvent e) {
            }
        });
        panel.add(pathField);

        goButton = new JButton("go");
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadFolder();
            }
        });
        panel.add(goButton);

        add(panel, BorderLayout.PAGE_START);

        folderTable = new FolderTable(model);
        folderTable.setFillsViewportHeight(true);
        model.addFolderListener(folderTable);

        add(new JScrollPane(folderTable), BorderLayout.CENTER);
    }

    private void loadFolder() {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            String id = pathField.getText().trim();
            if (id.length() == 0) {
                id = "/";
            }
            ObjectId objectId = model.loadFolder(id, id.startsWith("/"));
            model.loadObject(objectId.getId());
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
            return;
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }
}
