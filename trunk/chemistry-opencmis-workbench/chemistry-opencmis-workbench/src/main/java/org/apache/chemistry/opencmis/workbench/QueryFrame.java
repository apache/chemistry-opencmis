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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
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
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
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

    private static final String[] QUERY_SNIPPETS = new String[] { //
    "SELECT * FROM cmis:document", //
            "SELECT * FROM cmis:folder", //
            "SELECT cmis:objectId, cmis:name, SCORE() AS score FROM cmis:document WHERE CONTAINS('?')", //
            "WHERE cmis:name LIKE '%'", //
            "WHERE ? IN (?, ?, ?)", //
            "WHERE IN_FOLDER('?')", //
            "WHERE IN_TREE('?')", //
            "WHERE ? = TIMESTAMP 'YYYY-MM-DDThh:mm:ss.sss[Z|+hh:mm|-hh:mm]'", //
            "WHERE '?' = ANY ?", //
            "ORDER BY cmis:name", //
            "ORDER BY cmis:creationDate" };

    private final ClientModel model;

    private JTextArea queryText;
    private JFormattedTextField maxHitsField;
    private JCheckBox searchAllVersionsCheckBox;
    private ResultTable resultsTable;
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
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.PAGE_AXIS));

        // query text area
        queryText = new JTextArea(DEFAULT_QUERY, 5, 60);
        queryText.setLineWrap(true);
        queryText.setPreferredSize(new Dimension(Short.MAX_VALUE, queryText.getPreferredSize().height));
        inputPanel.add(queryText);

        JPanel inputPanel2 = new JPanel();
        inputPanel2.setLayout(new BorderLayout());

        // buttons
        JPanel buttonPanel = new JPanel();

        maxHitsField = new JFormattedTextField(new NumberFormatter());
        maxHitsField.setValue(Integer.valueOf(100));
        maxHitsField.setColumns(5);

        JLabel maxHitsLabel = new JLabel("Max hits:");
        maxHitsLabel.setLabelFor(maxHitsField);

        buttonPanel.add(maxHitsLabel);
        buttonPanel.add(maxHitsField);

        searchAllVersionsCheckBox = new JCheckBox("search all versions", false);
        buttonPanel.add(searchAllVersionsCheckBox);

        JButton queryButton = new JButton("Query", ClientHelper.getIcon("query.png"));
        queryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doQuery();
            }
        });

        buttonPanel.add(queryButton);

        inputPanel2.add(buttonPanel, BorderLayout.LINE_END);

        // snippets
        final JPopupMenu snippetsPopup = new JPopupMenu();
        for (final String s : QUERY_SNIPPETS) {
            JMenuItem menuItem = new JMenuItem(s);
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    queryText.insert(s, queryText.getCaretPosition());
                }
            });
            snippetsPopup.add(menuItem);
        }

        final JButton snippetButton = new JButton("Query Snippets", ClientHelper.getIcon("paste.png"));
        snippetButton.setFocusPainted(true);
        snippetButton.setBorderPainted(false);
        snippetButton.setContentAreaFilled(false);
        snippetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                snippetsPopup.show(snippetButton, 0, snippetButton.getHeight());
            }
        });

        inputPanel2.add(snippetButton, BorderLayout.LINE_START);

        // query time label
        queryTimeLabel = new JLabel("");
        queryTimeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel2.add(queryTimeLabel, BorderLayout.CENTER);

        inputPanel2.setMaximumSize(new Dimension(Short.MAX_VALUE, inputPanel2.getPreferredSize().height));
        inputPanel.add(inputPanel2);

        // table
        resultsTable = new ResultTable();

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
                        rtm.setValue(row, prop.getQueryName(), prop.getValues());
                    }
                }

                row++;
            }
            rtm.setRowCount(row);

            long stopTime = System.currentTimeMillis();
            float time = ((float) (stopTime - startTime)) / 1000f;
            queryTimeLabel.setText(" " + row + " hits (" + time + " seconds)");

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
        private final Map<Integer, Map<Integer, List<?>>> multivalue = new HashMap<Integer, Map<Integer, List<?>>>();
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

        public void setValue(final int rowIndex, final String queryName, Object value) {
            Integer col = columnMapping.get(queryName);
            if (col == null) {
                col = columnMapping.size();
                columnMapping.put(queryName, columnMapping.size());
            }

            if (value == null) {
                return;
            }

            if (value instanceof List<?>) {
                List<?> values = (List<?>) value;
                if (values.size() == 0) {
                    return;
                }

                value = values.get(0);

                if (values.size() > 1) {
                    Map<Integer, List<?>> mvrow = multivalue.get(rowIndex);
                    if (mvrow == null) {
                        mvrow = new HashMap<Integer, List<?>>();
                        multivalue.put(rowIndex, mvrow);
                    }
                    mvrow.put(col, values);
                }
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

        public List<?> getMultiValueAt(int rowIndex, int columnIndex) {
            Map<Integer, List<?>> row = multivalue.get(rowIndex);
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

    static class ResultTable extends JTable {

        private static final long serialVersionUID = 1L;

        public ResultTable() {
            super();

            setDefaultRenderer(ObjectIdImpl.class, new IdRenderer());
            setFillsViewportHeight(true);
            setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        }

        @Override
        public String getToolTipText(final MouseEvent e) {
            String result = null;

            Point p = e.getPoint();
            int rowIndex = rowAtPoint(p);
            int columnIndex = convertColumnIndexToModel(columnAtPoint(p));

            final ResultTableModel model = (ResultTableModel) getModel();

            final List<?> values = model.getMultiValueAt(rowIndex, columnIndex);
            if (values != null) {
                StringBuilder sb = new StringBuilder();

                for (Object value : values) {
                    if (sb.length() == 0) {
                        sb.append("<html>");
                    } else {
                        sb.append("<br>");
                    }
                    sb.append(value.toString());
                }

                result = sb.toString();
            } else {
                final Object value = model.getValueAt(rowIndex, columnIndex);
                if (value != null) {
                    result = value.toString();
                }
            }

            return result;
        }

        @Override
        public Component prepareRenderer(final TableCellRenderer renderer, final int rowIndex, final int columnIndex) {
            final Component prepareRenderer = super.prepareRenderer(renderer, rowIndex, columnIndex);
            final TableColumn column = getColumnModel().getColumn(columnIndex);

            final int currentWidth = column.getPreferredWidth();
            if (currentWidth < 200) {
                int width = prepareRenderer.getPreferredSize().width;
                if (currentWidth < width) {
                    if (width < 50) {
                        width = 50;
                    } else if (width > 200) {
                        width = 200;
                    }

                    if (width != currentWidth) {
                        column.setPreferredWidth(width);
                    }
                }
            }

            return prepareRenderer;
        }
    }
}
