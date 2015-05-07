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

import java.awt.Component;
import java.awt.Container;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Spring;
import javax.swing.SpringLayout;

/**
 * Convenience methods for spring layout tabs.
 */
public abstract class AbstractSpringLoginTab extends AbstractLoginTab {

    private static final long serialVersionUID = 1L;

    protected JTextField createTextField(Container pane, String label) {
        JTextField textField = new JTextField(60);
        JLabel textLabel = new JLabel(label, JLabel.TRAILING);
        textLabel.setLabelFor(textField);

        pane.add(textLabel);
        pane.add(textField);

        return textField;
    }

    protected JFormattedTextField createIntegerField(Container pane, String label) {
        NumberFormat format = NumberFormat.getIntegerInstance();
        JFormattedTextField intField = new JFormattedTextField(format);
        JLabel intLabel = new JLabel(label, JLabel.TRAILING);
        intLabel.setLabelFor(intField);

        pane.add(intLabel);
        pane.add(intField);

        return intField;
    }

    protected JPasswordField createPasswordField(Container pane, String label) {
        JPasswordField textField = new JPasswordField(60);
        JLabel textLabel = new JLabel(label, JLabel.TRAILING);
        textLabel.setLabelFor(textField);

        pane.add(textLabel);
        pane.add(textField);

        return textField;
    }

    private SpringLayout.Constraints getConstraintsForCell(int row, int col, Container parent, int cols) {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    protected void makeCompactGrid(Container parent, int rows, int cols, int initialX, int initialY, int xPad, int yPad) {
        SpringLayout layout = (SpringLayout) parent.getLayout();

        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++) {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++) {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++) {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++) {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++) {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        layout.getConstraints(parent).setConstraint(SpringLayout.EAST, x);
    }
}
