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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.chemistry.opencmis.workbench.ClientHelper;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

public abstract class InfoPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final ClientModel model;

    private JPanel gridPanel;
    private GridBagConstraints gbc;
    private Font boldFont;

    public InfoPanel(ClientModel model) {
        this.model = model;
    }

    protected ClientModel getClientModel() {
        return model;
    }

    protected void setupGUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBackground(Color.WHITE);

        gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(Color.WHITE);
        add(gridPanel);

        gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.insets = new Insets(3, 3, 3, 3);

        Font labelFont = UIManager.getFont("Label.font");
        boldFont = labelFont.deriveFont(Font.BOLD, labelFont.getSize2D() * 1.2f);
    }

    protected JTextField addLine(final String label) {
        return addLine(label, false);
    }

    protected JTextField addLine(final String label, final boolean bold) {
        JTextField textField = new JTextField();
        textField.setEditable(false);
        textField.setBorder(BorderFactory.createEmptyBorder());
        if (bold) {
            textField.setFont(boldFont);
        }

        JLabel textLable = new JLabel(label);
        textLable.setLabelFor(textField);
        if (bold) {
            textLable.setFont(boldFont);
        }

        gbc.gridy++;

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        gridPanel.add(textLable, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gridPanel.add(textField, gbc);

        return textField;
    }

    protected JTextField addId(final String label) {
        final JTextField textField = addLine(label, false);

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                String id = textField.getText();
                if (id.length() > 0 && !id.startsWith("(")) {
                    textField.setForeground(ClientHelper.LINK_COLOR);
                    textField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    textField.setForeground(UIManager.getColor("textForeground"));
                    textField.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        textField.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    String id = textField.getText();
                    if (id.length() > 0 && !id.startsWith("(")) {
                        try {
                            getClientModel().loadObject(id);
                        } catch (Exception ex) {
                            ClientHelper.showError(InfoPanel.this, ex);
                        }
                    }
                }
            }
        });

        return textField;
    }

    protected JTextField addLink(final String label) {
        final JTextField textField = addLine(label, false);

        if (Desktop.isDesktopSupported()) {
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void removeUpdate(DocumentEvent e) {

                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    String uri = textField.getText().toLowerCase();
                    if (uri.startsWith("http://") || uri.startsWith("https://")) {
                        textField.setForeground(Color.BLUE);
                        textField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        textField.setForeground(UIManager.getColor("textForeground"));
                        textField.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                }
            });

            textField.addMouseListener(new MouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {
                }

                @Override
                public void mousePressed(MouseEvent e) {
                }

                @Override
                public void mouseExited(MouseEvent e) {
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                        String uri = textField.getText().toLowerCase();
                        if (uri.startsWith("http://") || uri.startsWith("https://")) {
                            try {
                                Desktop.getDesktop().browse(new URI(textField.getText()));
                            } catch (Exception ex) {
                                ClientHelper.showError(InfoPanel.this, ex);
                            }
                        }
                    }
                }
            });
        }

        return textField;
    }

    protected YesNoLabel addYesNoLabel(String label) {
        YesNoLabel ynl = new YesNoLabel();

        JLabel textLable = new JLabel(label);
        textLable.setLabelFor(ynl);

        gbc.gridy++;

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        gridPanel.add(textLable, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gridPanel.add(ynl, gbc);

        return ynl;
    }

    protected <T extends JComponent> T addComponent(String label, T comp) {
        JLabel textLable = new JLabel(label);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setOpaque(false);
        panel.add(comp);
        textLable.setLabelFor(panel);

        gbc.gridy++;

        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
        gridPanel.add(textLable, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.BASELINE_LEADING;
        gridPanel.add(panel, gbc);

        return comp;
    }

    public static class InfoList extends JPanel {
        private static final long serialVersionUID = 1L;

        public InfoList() {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setBackground(Color.WHITE);
        }

        public void clear() {
            removeAll();
        }

        public void setList(Collection<?> list) {
            clear();

            if (list == null || list.size() == 0) {
                return;
            }

            for (Object o : list) {
                JTextField textField = new JTextField(o == null ? "" : o.toString());
                textField.setEditable(false);
                textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
                add(textField);
            }
        }
    }
}
