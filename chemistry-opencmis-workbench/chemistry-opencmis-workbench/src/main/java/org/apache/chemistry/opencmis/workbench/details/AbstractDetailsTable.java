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
package org.apache.chemistry.opencmis.workbench.details;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.model.ClientModelEvent;
import org.apache.chemistry.opencmis.workbench.model.ObjectListener;
import org.apache.chemistry.opencmis.workbench.swing.CollectionRenderer;

public abstract class AbstractDetailsTable extends JTable implements ObjectListener {

    private static final long serialVersionUID = 1L;

    private ClientModel model;
    private String[] columnNames;

    public void init(ClientModel model, String[] columnNames, int[] colummnWidths) {
        this.model = model;
        model.addObjectListener(this);

        this.columnNames = columnNames;

        setModel(new DetailsTableModel(this));

        setDefaultRenderer(Collection.class, new CollectionRenderer());
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setAutoCreateRowSorter(true);

        for (int i = 0; i < colummnWidths.length; i++) {
            TableColumn column = getColumnModel().getColumn(i);
            column.setPreferredWidth(colummnWidths[i]);
        }

        setFillsViewportHeight(true);

        final JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Copy to clipboard");
        popup.add(menuItem);

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringBuilder sb = new StringBuilder();
                int rows = getDetailRowCount();
                for (int row = 0; row < rows; row++) {
                    int cols = getColumnNames().length;
                    for (int col = 0; col < cols; col++) {
                        sb.append(getDetailValueAt(row, col));
                        sb.append("|");
                    }
                    sb.append("\n");
                }

                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                Transferable transferable = new StringSelection(sb.toString());
                clipboard.setContents(transferable, null);
            }
        });

        addMouseListener(new MouseListener() {
            public void mouseExited(MouseEvent e) {
            }

            public void mouseEntered(MouseEvent e) {
            }

            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = getSelectedRow();
                    if ((row > -1) && (row < getModel().getRowCount())) {
                        doubleClickAction(e, row);
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
    }

    public void objectLoaded(ClientModelEvent event) {
        ((DetailsTableModel) getModel()).fireTableDataChanged();
    }

    public CmisObject getObject() {
        return model.getCurrentObject();
    }

    public ClientModel getClientModel() {
        return model;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public abstract int getDetailRowCount();

    public abstract Object getDetailValueAt(int rowIndex, int columnIndex);

    public Class<?> getDetailColumClass(int columnIndex) {
        return String.class;
    }

    public void doubleClickAction(MouseEvent e, int rowIndex) {
    }

    static class DetailsTableModel extends AbstractTableModel {

        private final AbstractDetailsTable table;

        public DetailsTableModel(AbstractDetailsTable table) {
            this.table = table;
        }

        private static final long serialVersionUID = 1L;

        public String getColumnName(int columnIndex) {
            return table.getColumnNames()[columnIndex];
        }

        public int getColumnCount() {
            return table.getColumnNames().length;
        }

        public int getRowCount() {
            if (table.getObject() == null) {
                return 0;
            }

            return table.getDetailRowCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (table.getObject() == null) {
                return null;
            }

            return table.getDetailValueAt(rowIndex, columnIndex);
        }

        public Class<?> getColumnClass(int columnIndex) {
            return table.getDetailColumClass(columnIndex);
        }
    }
}
