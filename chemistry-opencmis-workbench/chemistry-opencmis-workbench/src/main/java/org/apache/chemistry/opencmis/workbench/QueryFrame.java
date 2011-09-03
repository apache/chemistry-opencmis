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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.NumberFormatter;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.IdRenderer;

public class QueryFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Cursor HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

    private static final String WINDOW_TITLE = "CMIS Query";
    private static final String DEFAULT_QUERY = "SELECT * FROM cmis:document";

    private final ClientModel model;

    private JTextArea queryText;
    private JFormattedTextField maxHitsField;
    private JCheckBox searchAllVersionsCheckBox;
    private JTable resultsTable;
    private JLabel queryTimeLabel;

    public QueryFrame(ClientModel model) {
        super();

        this.model = model;
        createGUI();
    }

    private void createGUI() {
        setTitle(WINDOW_TITLE + " - " + model.getRepositoryName());
        setPreferredSize(new Dimension(800, 700));
        setMinimumSize(new Dimension(200, 60));

        setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));

        // input
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.LINE_AXIS));

        queryText = new JTextArea(DEFAULT_QUERY, 5, 60);
        inputPanel.add(queryText);

        JPanel inputPanel2 = new JPanel();
        inputPanel2.setPreferredSize(new Dimension(160, 100));
        inputPanel2.setMaximumSize(inputPanel.getPreferredSize());
        inputPanel2.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        JButton queryButton = new JButton("Query");
        queryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        inputPanel2.add(queryButton, c);

        maxHitsField = new JFormattedTextField(new NumberFormatter());
        maxHitsField.setValue(Integer.valueOf(100));
        maxHitsField.setColumns(5);

        JLabel maxHitsLabel = new JLabel("Max hits:");
        maxHitsLabel.setLabelFor(maxHitsField);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        inputPanel2.add(maxHitsLabel, c);
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        inputPanel2.add(maxHitsField, c);

        searchAllVersionsCheckBox = new JCheckBox("search all versions", false);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        inputPanel2.add(searchAllVersionsCheckBox, c);

        queryTimeLabel = new JLabel("(-- hits in -- seconds)");
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 2;
        inputPanel2.add(queryTimeLabel, c);

        inputPanel.add(inputPanel2);

        // table
        resultsTable = new JTable();
        resultsTable.setDefaultRenderer(ObjectIdImpl.class, new IdRenderer());
        resultsTable.setFillsViewportHeight(true);
        resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        final JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Copy to clipboard");
        popup.add(menuItem);

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ClientHelper.copyTableToClipboard(resultsTable);
            }
        });

        resultsTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = resultsTable.rowAtPoint(e.getPoint());
                int column = resultsTable.columnAtPoint(e.getPoint());
                if (row > -1 && resultsTable.getColumnClass(column) == ObjectIdImpl.class) {
                    try {
                        model.loadObject(((ObjectId) resultsTable.getValueAt(row, column)).getId());
                    } catch (Exception ex) {
                        ClientHelper.showError(QueryFrame.this, ex);
                    }
                }
            }

            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        resultsTable.addMouseMotionListener(new MouseMotionListener() {
            public void mouseMoved(MouseEvent e) {
                int row = resultsTable.rowAtPoint(e.getPoint());
                int column = resultsTable.columnAtPoint(e.getPoint());
                if (row > -1 && resultsTable.getColumnClass(column) == ObjectIdImpl.class) {
                    resultsTable.setCursor(HAND_CURSOR);
                } else {
                    resultsTable.setCursor(DEFAULT_CURSOR);
                }
            }

            public void mouseDragged(MouseEvent e) {
            }
        });

        add(new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, new JScrollPane(resultsTable)));

        getRootPane().setDefaultButton(queryButton);
        
        ClientHelper.installEscapeBinding(this, getRootPane(), true);
        
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private synchronized void doQuery() {
        String text = queryText.getText();
        text = text.replace('\n', ' ');

        ItemIterable<QueryResult> results = null;

        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            int maxHits = 1000;
            try {
                maxHitsField.commitEdit();
                maxHits = ((Number) maxHitsField.getValue()).intValue();
            } catch (Exception e) {
            }

            results = model.query(text, searchAllVersionsCheckBox.isSelected(), maxHits);

            ResultTableModel rtm = new ResultTableModel();

            long startTime = System.currentTimeMillis();

            int row = 0;
            for (QueryResult qr : results.getPage(maxHits)) {
                rtm.setColumnCount(Math.max(rtm.getColumnCount(), qr.getProperties().size()));

                for (PropertyData<?> prop : qr.getProperties()) {
                    if (PropertyIds.OBJECT_ID.equals(prop.getId()) && (prop.getFirstValue() != null)) {
                        rtm.setValue(row, prop.getQueryName(), new ObjectIdImpl(prop.getFirstValue().toString()));
                    } else {
                        rtm.setValue(row, prop.getQueryName(), prop.getFirstValue());
                    }
                }

                row++;
            }
            rtm.setRowCount(row);

            long stopTime = System.currentTimeMillis();
            float time = ((float) (stopTime - startTime)) / 1000f;
            queryTimeLabel.setText("(" + row + " hits in " + time + " seconds)");

            resultsTable.setModel(rtm);
        } catch (Exception ex) {
            ClientHelper.showError(null, ex);
            return;
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    static class ResultTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        private int columnCount = 0;
        private int rowCount = 0;
        private final Map<String, Integer> columnMapping = new HashMap<String, Integer>();
        private final Map<Integer, Map<Integer, Object>> data = new HashMap<Integer, Map<Integer, Object>>();
        private final Map<Integer, Class<?>> columnClass = new HashMap<Integer, Class<?>>();

        public ResultTableModel() {
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }

        public int getRowCount() {
            return rowCount;
        }

        public void setValue(int rowIndex, String queryName, Object value) {
            Integer col = columnMapping.get(queryName);
            if (col == null) {
                col = columnMapping.size();
                columnMapping.put(queryName, columnMapping.size());
            }

            if (value == null) {
                return;
            }

            if (value instanceof GregorianCalendar) {
                value = ClientHelper.getDateString((GregorianCalendar) value);
            }

            columnClass.put(col, value.getClass());

            Map<Integer, Object> row = data.get(rowIndex);
            if (row == null) {
                row = new HashMap<Integer, Object>();
                data.put(rowIndex, row);
            }

            row.put(col, value);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Map<Integer, Object> row = data.get(rowIndex);
            if (row == null) {
                return null;
            }

            return row.get(columnIndex);
        }

        @Override
        public String getColumnName(int column) {
            for (Map.Entry<String, Integer> e : columnMapping.entrySet()) {
                if (e.getValue().equals(column)) {
                    return e.getKey();
                }
            }

            return "?";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            Class<?> clazz = columnClass.get(columnIndex);
            if (clazz != null) {
                return clazz;
            }

            return String.class;
        }
    }
}
