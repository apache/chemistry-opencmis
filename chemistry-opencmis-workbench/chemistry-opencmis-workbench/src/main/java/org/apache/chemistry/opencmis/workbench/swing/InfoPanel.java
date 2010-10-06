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
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

public abstract class InfoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JPanel gridPanel;
	private GridBagConstraints gbc;
	private Font boldFont;

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
		boldFont = labelFont
				.deriveFont(Font.BOLD, labelFont.getSize2D() * 1.2f);
	}

	protected JTextField addLine(String label) {
		return addLine(label, false);
	}

	protected JTextField addLine(String label, boolean bold) {
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

	protected JCheckBox addCheckBox(String label) {
		JCheckBox checkBox = new JCheckBox();
		checkBox.setEnabled(false);

		JLabel textLable = new JLabel(label);
		textLable.setLabelFor(checkBox);

		gbc.gridy++;

		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		gridPanel.add(textLable, gbc);

		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gridPanel.add(checkBox, gbc);

		return checkBox;
	}

	protected <T extends JComponent> T addComponent(String label, T comp) {
		JLabel textLable = new JLabel(label);
		textLable.setLabelFor(comp);

		gbc.gridy++;

		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		gridPanel.add(textLable, gbc);

		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gridPanel.add(comp, gbc);

		return comp;
	}
}
