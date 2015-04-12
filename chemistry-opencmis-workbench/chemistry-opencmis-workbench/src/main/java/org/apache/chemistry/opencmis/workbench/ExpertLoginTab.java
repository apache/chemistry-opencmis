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
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.chemistry.opencmis.client.SessionParameterMap;
import org.apache.chemistry.opencmis.workbench.ClientHelper.FileEntry;
import org.apache.chemistry.opencmis.workbench.model.ClientSession;

public class ExpertLoginTab extends AbstractLoginTab {

    private static final long serialVersionUID = 1L;

    public static final String SYSPROP_CONFIGS = ClientSession.WORKBENCH_PREFIX + "configs";

    private static final String CONFIGS_FOLDER = "/configs/";
    private static final String CONFIGS_LIBRARY = "config-library.properties";

    private JComboBox configs;
    private JTextArea sessionParameterTextArea;
    private List<FileEntry> sessionConfigurations;

    public ExpertLoginTab() {
        super();
        createGUI();
    }

    private void createGUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        URI propFile = null;

        String externalConfigs = System.getProperty(SYSPROP_CONFIGS);
        if (externalConfigs == null) {
            propFile = ClientHelper.getClasspathURI(CONFIGS_FOLDER + CONFIGS_LIBRARY);
        } else {
            propFile = (new File(externalConfigs)).toURI();
        }

        sessionConfigurations = ClientHelper.readFileProperties(propFile);

        configs = new JComboBox();
        configs.setMaximumRowCount(20);

        configs.addItem(new FileEntry("", null));
        if (sessionConfigurations != null) {
            for (FileEntry fe : sessionConfigurations) {
                configs.addItem(fe);
            }
        }

        configs.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    FileEntry fe = (FileEntry) e.getItem();

                    sessionParameterTextArea.setText(ClientHelper.readFileAndRemoveHeader(fe.getFile()));
                    sessionParameterTextArea.setCaretPosition(0);
                }
            }
        });

        add(configs, BorderLayout.PAGE_START);

        sessionParameterTextArea = new JTextArea();
        sessionParameterTextArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, sessionParameterTextArea.getFont()
                .getSize()));
        add(new JScrollPane(sessionParameterTextArea), BorderLayout.CENTER);
    }

    public void setSessionParameters(Map<String, String> parameters) {
        configs.setSelectedIndex(0);

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            sb.append(parameter.getKey());
            sb.append('=');
            sb.append(parameter.getValue());
            sb.append('\n');
        }

        sessionParameterTextArea.setText(sb.toString());
        sessionParameterTextArea.setCaretPosition(0);
    }

    @Override
    public String getTabTitle() {
        return "Expert";
    }

    @Override
    public Map<String, String> getSessionParameters() {
        SessionParameterMap result = new SessionParameterMap();
        result.parse(sessionParameterTextArea.getText());

        return result;
    }

    @Override
    public boolean transferSessionParametersToExpertTab() {
        return false;
    }
}
