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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class InfoDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    public InfoDialog(Frame owner) {
        super(owner, "Info", true);
        createGUI();
    }

    private void createGUI() {
        setPreferredSize(new Dimension(800, 500));
        setMinimumSize(new Dimension(600, 400));

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        JPanel topPanel = new JPanel(new FlowLayout());

        JLabel cmisLogo = new JLabel(ClientHelper.getIcon("icon.png"));
        topPanel.add(cmisLogo);

        Font labelFont = UIManager.getFont("Label.font");
        Font titleFont = labelFont.deriveFont(Font.BOLD, labelFont.getSize2D() * 2f);

        JLabel titleLabel = new JLabel("CMIS Workbench");
        titleLabel.setFont(titleFont);
        topPanel.add(titleLabel);

        add(topPanel);

        StringBuilder readme = new StringBuilder();

        readme.append(loadText("/META-INF/README", "CMIS Workbench"));
        readme.append("\n---------------------------------------------------------\n");

        readme.append("\nCurrent System Properties:\n\n");

        Properties sysProps = System.getProperties();
        for (Object key : new TreeSet<Object>(sysProps.keySet())) {
            readme.append(key).append(" = ").append(sysProps.get(key)).append("\n");
        }

        readme.append("\n---------------------------------------------------------\n");
        readme.append(loadText("/META-INF/build-timestamp.txt", ""));

        JTextArea ta = new JTextArea(readme.toString());
        ta.setEditable(false);
        ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane readmePane = new JScrollPane(ta);
        readmePane.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));

        add(readmePane);

        ClientHelper.installEscapeBinding(this, getRootPane(), false);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    public void showDialog() {
        setVisible(true);
    }

    public void hideDialog() {
        setVisible(false);
    }

    private String loadText(String file, String defaultText) {
        StringBuilder result = new StringBuilder();

        InputStream stream = getClass().getResourceAsStream(file);
        if (stream != null) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));

                String s = null;
                while ((s = br.readLine()) != null) {
                    result.append(s);
                    result.append('\n');
                }

                br.close();
            } catch (Exception e) {
            }
        } else {
            result.append(defaultText);
        }

        return result.toString();
    }
}
