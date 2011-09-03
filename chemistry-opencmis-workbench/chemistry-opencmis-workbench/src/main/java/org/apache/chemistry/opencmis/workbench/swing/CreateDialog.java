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
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.workbench.ClientHelper;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

public abstract class CreateDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final ClientModel model;
    private final JPanel panel;

    public CreateDialog(Frame owner, String title, ClientModel model) {
        super(owner, title, true);
        this.model = model;

        setLayout(new BorderLayout());
        panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel, BorderLayout.CENTER);
    }

    protected ClientModel getClientModel() {
        return model;
    }

    protected void createRow(String label, JComponent comp, int row) {
        JLabel textLabel = new JLabel(label);
        textLabel.setLabelFor(comp);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = row;
        panel.add(textLabel, c);
        c.gridx = 1;
        panel.add(comp, c);
    }

    public void showDialog() {
        panel.invalidate();

        ClientHelper.installEscapeBinding(this, getRootPane(), true);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    protected Object[] getTypes(String rootTypeId) {
        List<ObjectType> types = model.getCreateableTypes(rootTypeId);

        Object[] result = new Object[types.size()];

        int i = 0;
        for (final ObjectType type : types) {
            result[i] = new ObjectTypeItem() {
                public ObjectType getObjectType() {
                    return type;
                }

                @Override
                public String toString() {
                    return type.getDisplayName() + " (" + type.getId() + ")";
                }
            };

            i++;
        }

        return result;
    }

    public interface ObjectTypeItem {
        ObjectType getObjectType();
    }
}
