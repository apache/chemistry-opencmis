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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.chemistry.opencmis.workbench.ClientHelper;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

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
        return addLine(label, bold, new JTextField());
    }

    protected JTextField addLine(final String label, final boolean bold, JTextField textField) {
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
        return addLine(label, false, new IdTextField());
    }

    protected JTextField addLink(final String label) {
        return addLine(label, false, new UrlTextField());
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

    private abstract class ClickableTextField extends JTextField {
        private static final long serialVersionUID = 1L;

        private String link;
        private boolean updated = false;
        private final JPopupMenu popup;

        public ClickableTextField() {
            popup = new JPopupMenu();
            final JMenuItem menuItem = new JMenuItem("Copy to clipboard");
            popup.add(menuItem);

            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    Transferable transferable = new StringSelection(link);
                    clipboard.setContents(transferable, null);
                }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (link != null) {
                        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                            try {
                                linkAction(link);
                            } catch (Exception ex) {
                                ClientHelper.showError(InfoPanel.this, ex);
                            }
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    maybeShowPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    maybeShowPopup(e);
                }

                private void maybeShowPopup(MouseEvent e) {
                    if (e.isPopupTrigger()) {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        }

        public JPopupMenu getPopupMenu() {
            return popup;
        }

        public abstract boolean isLink(String link);

        public abstract Color getLinkColor(String link);

        public abstract void linkAction(String link);

        @Override
        public void setText(String text) {
            if (!isLink(text)) {
                setForeground(UIManager.getColor("textForeground"));
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                popup.setEnabled(false);
                link = null;
            } else {
                setForeground(getLinkColor(text));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                popup.setEnabled(true);
                link = text;
            }

            updated = true;

            super.setText(text);
        }

        @Override
        public void validate() {
        }

        @Override
        public void revalidate() {
        }

        @Override
        public void repaint(long tm, int x, int y, int width, int height) {
            if (updated) {
                super.repaint(tm, x, y, width, height);
                updated = false;
            }
        }

        @Override
        public void repaint(Rectangle r) {
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (link != null) {
                FontMetrics fm = getFontMetrics(getFont());

                int y1 = fm.getHeight() - 2;
                int x2 = fm.stringWidth(link);
                g.setColor(getLinkColor(link));
                g.drawLine(0, y1, x2, y1);
            }
        }
    }

    private class IdTextField extends ClickableTextField {
        private static final long serialVersionUID = 1L;

        public IdTextField() {
            super();
        }

        @Override
        public boolean isLink(String link) {
            return link != null && link.length() > 0 && link.charAt(0) != '(';
        }

        @Override
        public Color getLinkColor(String link) {
            return ClientHelper.LINK_COLOR;
        }

        @Override
        public void linkAction(String link) {
            try {
                getClientModel().loadObject(link);
            } catch (Exception ex) {
                ClientHelper.showError(InfoPanel.this, ex);
            }
        }
    }

    private class UrlTextField extends ClickableTextField {
        private static final long serialVersionUID = 1L;

        private JMenuItem qrCodeItem;

        public UrlTextField() {
            super();

            qrCodeItem = new JMenuItem();
            qrCodeItem.setVerticalTextPosition(AbstractButton.BOTTOM);
            qrCodeItem.setHorizontalTextPosition(AbstractButton.CENTER);

            getPopupMenu().addSeparator();
            getPopupMenu().add(qrCodeItem);
        }

        @Override
        public void setText(String text) {
            if (isLink(text)) {
                try {
                    Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
                    hints.put(EncodeHintType.MARGIN, 0);

                    BitMatrix bitMatrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 200, 200, hints);
                    qrCodeItem.setIcon(new ImageIcon(MatrixToImageWriter.toBufferedImage(bitMatrix)));
                    qrCodeItem.setVisible(true);
                } catch (WriterException e) {
                    qrCodeItem.setVisible(false);
                }
            } else {
                qrCodeItem.setVisible(false);
                getPopupMenu().setEnabled(false);
            }

            super.setText(text);
        }

        @Override
        public boolean isLink(String link) {
            if (link == null || link.length() == 0) {
                return false;
            }

            String lower = link.toLowerCase(Locale.ENGLISH);
            return lower.startsWith("http://") || lower.startsWith("https://");
        }

        @Override
        public Color getLinkColor(String link) {
            return Color.BLUE;
        }

        @Override
        public void linkAction(String link) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI(link));
                } catch (Exception ex) {
                    ClientHelper.showError(InfoPanel.this, ex);
                }
            }
        }
    }
}
