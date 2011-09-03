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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "CMIS Client Logging";

    private JTextArea logTextArea;

    public LogFrame() {
        super();
        createGUI();
    }

    private void createGUI() {
        setTitle(WINDOW_TITLE);
        setPreferredSize(new Dimension(800, 600));
        setMinimumSize(new Dimension(200, 60));

        setLayout(new BorderLayout());

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTextArea.setLineWrap(true);
        logTextArea.setWrapStyleWord(true);
        add(new JScrollPane(logTextArea), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new FlowLayout());
        add(inputPanel, BorderLayout.PAGE_END);

        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTextArea.setText("");
            }
        });
        inputPanel.add(clearButton);

        String[] levels = new String[] { Level.ALL.toString(), Level.TRACE.toString(), Level.DEBUG.toString(),
                Level.INFO.toString(), Level.WARN.toString(), Level.ERROR.toString(), Level.FATAL.toString(),
                Level.OFF.toString() };

        final JComboBox levelBox = new JComboBox(levels);
        levelBox.setSelectedItem(Logger.getRootLogger().getLevel().toString());
        levelBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Logger.getRootLogger().setLevel(Level.toLevel(levelBox.getSelectedItem().toString()));
            }
        });
        inputPanel.add(levelBox);

        ClientHelper.installEscapeBinding(this, getRootPane(), false);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        pack();

        setLocationRelativeTo(null);
        setVisible(false);

        ClientWriterAppender.setTextArea(logTextArea);
    }

    public void showFrame() {
        setVisible(true);
    }
}
