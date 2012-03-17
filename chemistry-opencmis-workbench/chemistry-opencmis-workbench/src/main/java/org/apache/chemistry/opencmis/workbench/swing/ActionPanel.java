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
package org.apache.chemistry.opencmis.workbench.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.workbench.ClientHelper;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

public abstract class ActionPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final ClientModel model;
    private CmisObject object;

    private JPanel centerPanel;

    public ActionPanel(String title, String buttonLabel, ClientModel model) {
        super();
        this.model = model;
        createGUI(title, buttonLabel);
    }

    public ClientModel getClientModel() {
        return model;
    }

    public void setObject(CmisObject object) {
        this.object = object;
    }

    public CmisObject getObject() {
        return object;
    }

    protected void createGUI(String title, String buttonLabel) {
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(3);
        setLayout(borderLayout);

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 5, 5, 5),
                BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 2),
                        BorderFactory.createEmptyBorder(5, 5, 5, 5))));

        Font labelFont = UIManager.getFont("Label.font");
        Font boldFont = labelFont.deriveFont(Font.BOLD, labelFont.getSize2D() * 1.2f);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(boldFont);
        add(titleLabel, BorderLayout.PAGE_START);

        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
        centerPanel.setBackground(Color.WHITE);
        add(centerPanel, BorderLayout.CENTER);

        createActionComponents();

        JButton deleteButton = new JButton(buttonLabel);
        deleteButton.addActionListener(this);
        add(deleteButton, BorderLayout.PAGE_END);

        setMaximumSize(new Dimension(Short.MAX_VALUE, getPreferredSize().height));
    }

    protected void addActionComponent(JComponent comp) {
        comp.setAlignmentX(LEFT_ALIGNMENT);
        centerPanel.add(comp);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            if (doAction()) {
                model.reloadObject();
            }
            model.reloadFolder();
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    protected abstract void createActionComponents();

    public abstract boolean isAllowed();

    /**
     * @return <code>true</code> if object should be reloaded.
     */
    public abstract boolean doAction() throws Exception;

    protected JPanel createFilenamePanel(final JTextField filenameField) {
        JPanel filePanel = new JPanel(new BorderLayout());
        filePanel.setBackground(Color.WHITE);

        filePanel.add(new JLabel("File:"), BorderLayout.LINE_START);

        filePanel.add(filenameField, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                int chooseResult = fileChooser.showDialog(filenameField, "Select");
                if (chooseResult == JFileChooser.APPROVE_OPTION) {
                    if (fileChooser.getSelectedFile().isFile()) {
                        filenameField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    }
                }
            }
        });
        filePanel.add(browseButton, BorderLayout.LINE_END);

        return filePanel;
    }
}
