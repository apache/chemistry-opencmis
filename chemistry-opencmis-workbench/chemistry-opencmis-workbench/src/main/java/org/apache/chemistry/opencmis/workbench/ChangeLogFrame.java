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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.chemistry.opencmis.client.api.ChangeEvent;
import org.apache.chemistry.opencmis.client.api.ChangeEvents;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.CollectionRenderer;
import org.apache.chemistry.opencmis.workbench.swing.GregorianCalendarRenderer;

public class ChangeLogFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String WINDOW_TITLE = "CMIS Change Logger";

    private final ClientModel model;

    private JTextField changeLogTokenField;
    private ChangeLogTable changeLogTable;

    public ChangeLogFrame(ClientModel model) {
        super();

        this.model = model;
        createGUI();
    }

    private void createGUI() {
        setTitle(WINDOW_TITLE + " - " + model.getRepositoryName());
        setPreferredSize(new Dimension(700, 700));
        setMinimumSize(new Dimension(200, 60));

        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

        inputPanel.add(new JLabel("Change Logger Token: "), BorderLayout.LINE_START);

        changeLogTokenField = new JTextField();
        try {
            changeLogTokenField.setText(model.getRepositoryInfo().getLatestChangeLogToken());
        } catch (Exception e1) {
        }
        inputPanel.add(changeLogTokenField, BorderLayout.CENTER);

        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String changeLogToken = changeLogTokenField.getText();
                if (changeLogToken.trim().length() == 0) {
                    changeLogToken = null;
                }

                ChangeEvents events = null;
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    events = model.getClientSession().getSession().getContentChanges(changeLogToken, true, 1000);
                } catch (Exception ex) {
                    ClientHelper.showError(null, ex);
                    return;
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }

                changeLogTable.setChangeEvents(events.getChangeEvents());
                changeLogTokenField.setText(events.getLatestChangeLogToken() == null ? "" : events
                        .getLatestChangeLogToken());
            }
        });
        inputPanel.add(loadButton, BorderLayout.LINE_END);
        getRootPane().setDefaultButton(loadButton);

        add(inputPanel, BorderLayout.PAGE_START);

        changeLogTable = new ChangeLogTable();
        add(new JScrollPane(changeLogTable), BorderLayout.CENTER);

        ClientHelper.installEscapeBinding(this, getRootPane(), true);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    static class ChangeLogTable extends JTable {

        private static final long serialVersionUID = 1L;

        private static final String[] COLUMN_NAMES = { "Change Type", "Object Id", "Change Time", "Properties" };
        private static final int[] COLUMN_WIDTHS = { 100, 200, 200, 400 };

        private List<ChangeEvent> changeEvents;

        public ChangeLogTable() {
            setDefaultRenderer(GregorianCalendar.class, new GregorianCalendarRenderer());
            setDefaultRenderer(Collection.class, new CollectionRenderer());
            setModel(new ChangeLogTableModel(this));

            setAutoResizeMode(AUTO_RESIZE_OFF);
            setAutoCreateRowSorter(true);

            for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
                TableColumn column = getColumnModel().getColumn(i);
                column.setPreferredWidth(COLUMN_WIDTHS[i]);
            }

            setFillsViewportHeight(true);
        }

        public void setChangeEvents(List<ChangeEvent> changeEvents) {
            this.changeEvents = changeEvents;
            ((AbstractTableModel) getModel()).fireTableDataChanged();
        }

        public List<ChangeEvent> getChangeEvents() {
            return changeEvents;
        }

        static class ChangeLogTableModel extends AbstractTableModel {

            private static final long serialVersionUID = 1L;

            private final ChangeLogTable table;

            public ChangeLogTableModel(ChangeLogTable table) {
                this.table = table;
            }

            public String getColumnName(int columnIndex) {
                return COLUMN_NAMES[columnIndex];
            }

            public int getColumnCount() {
                return COLUMN_NAMES.length;
            }

            public int getRowCount() {
                if (table.getChangeEvents() == null) {
                    return 0;
                }

                return table.getChangeEvents().size();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                ChangeEvent event = table.getChangeEvents().get(rowIndex);

                switch (columnIndex) {
                case 0:
                    return (event.getChangeType() == null ? "?" : event.getChangeType().value());
                case 1:
                    return (event.getObjectId() == null ? "?" : event.getObjectId());
                case 2:
                    return event.getChangeTime();
                case 3:
                    return event.getProperties().entrySet();
                }

                return null;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 2) {
                    return GregorianCalendar.class;
                } else if (columnIndex == 3) {
                    return Collection.class;
                }

                return super.getColumnClass(columnIndex);
            }
        }
    }
}
