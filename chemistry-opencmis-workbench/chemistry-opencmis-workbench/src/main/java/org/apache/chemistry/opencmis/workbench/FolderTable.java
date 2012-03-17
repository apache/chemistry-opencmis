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
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.model.ClientModelEvent;
import org.apache.chemistry.opencmis.workbench.model.FolderListener;
import org.apache.chemistry.opencmis.workbench.swing.GregorianCalendarRenderer;

public class FolderTable extends JTable implements FolderListener {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = { "", "Name", "Type", "Content Type", "Size", "Creation Date",
            "Created by", "Modification Date", "Modified by", "Id" };
    private static final int[] COLUMN_WIDTHS = { 24, 200, 150, 150, 80, 180, 100, 180, 100, 300 };
    public static final int ID_COLUMN = 9;

    private final ClientModel model;

    private Map<BaseTypeId, ImageIcon> icons;
    private ImageIcon checkedOutIcon;
    private ImageIcon pwcIcon;

    public FolderTable(final ClientModel model) {
        super();

        this.model = model;

        setModel(new FolderTableModel());

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setAutoCreateRowSorter(true);

        setDefaultRenderer(GregorianCalendar.class, new GregorianCalendarRenderer());
        setTransferHandler(new FolderTransferHandler());
        setDragEnabled(true);
        setDropMode(DropMode.INSERT);

        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            TableColumn column = getColumnModel().getColumn(i);
            column.setPreferredWidth(COLUMN_WIDTHS[i]);
        }

        final JPopupMenu popup = new JPopupMenu();
        JMenuItem menuItem = new JMenuItem("Copy to clipboard");
        popup.add(menuItem);

        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ClientHelper.copyTableToClipboard(FolderTable.this);
            }
        });

        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                int row = getSelectedRow();
                if (row > -1) {
                    String id = getModel().getValueAt(getRowSorter().convertRowIndexToModel(row), ID_COLUMN).toString();

                    try {
                        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                        model.loadObject(id);
                    } catch (Exception ex) {
                        ClientHelper.showError(null, ex);
                        return;
                    } finally {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doAction(e.isShiftDown());
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

        addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    doAction(e.isShiftDown());
                }
            }

            public void keyPressed(KeyEvent e) {
            }
        });

        loadIcons();
    }

    private void loadIcons() {
        icons = new HashMap<BaseTypeId, ImageIcon>();
        icons.put(BaseTypeId.CMIS_DOCUMENT, ClientHelper.getIcon("document.png"));
        icons.put(BaseTypeId.CMIS_FOLDER, ClientHelper.getIcon("folder.png"));
        icons.put(BaseTypeId.CMIS_RELATIONSHIP, ClientHelper.getIcon("relationship.png"));
        icons.put(BaseTypeId.CMIS_POLICY, ClientHelper.getIcon("policy.png"));

        checkedOutIcon = ClientHelper.getIcon("checkedout.png");
        pwcIcon = ClientHelper.getIcon("pwc.png");
    }

    public void folderLoaded(ClientModelEvent event) {
        event.getClientModel().getCurrentChildren();

        ((FolderTableModel) getModel()).fireTableDataChanged();
    }

    private void doAction(boolean alternate) {
        int row = getSelectedRow();
        if ((row > -1) && (row < model.getCurrentChildren().size())) {
            String id = getModel().getValueAt(getRowSorter().convertRowIndexToModel(row), ID_COLUMN).toString();
            CmisObject object = model.getFromCurrentChildren(id);

            if (object instanceof Document) {
                if (alternate) {
                    ClientHelper.download(this.getParent(), (Document) object, null);
                } else {
                    ClientHelper.open(this.getParent(), (Document) object, null);
                }
            } else if (object instanceof Folder) {
                try {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    model.loadFolder(object.getId(), false);
                } catch (Exception ex) {
                    ClientHelper.showError(null, ex);
                    return;
                } finally {
                    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
            }
        }
    }

    class FolderTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 1L;

        public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        public int getRowCount() {
            return model.getCurrentChildren().size();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            CmisObject obj = model.getCurrentChildren().get(rowIndex);

            switch (columnIndex) {
            case 0:
                if (obj instanceof Document) {
                    Document doc = (Document) obj;
                    if (Boolean.TRUE.equals(doc.isVersionSeriesCheckedOut())) {
                        if (doc.getId().equals(doc.getVersionSeriesCheckedOutId())) {
                            return pwcIcon;
                        } else {
                            return checkedOutIcon;
                        }
                    } else {
                        return icons.get(BaseTypeId.CMIS_DOCUMENT);
                    }
                }
                return icons.get(obj.getBaseTypeId());
            case 1:
                return obj.getName();
            case 2:
                return obj.getType().getId();
            case 3:
                if (obj instanceof Document) {
                    return ((Document) obj).getContentStreamMimeType();
                } else {
                    return null;
                }
            case 4:
                if (obj instanceof Document) {
                    return ((Document) obj).getContentStreamLength();
                } else {
                    return null;
                }
            case 5:
                return obj.getCreationDate();
            case 6:
                return obj.getCreatedBy();
            case 7:
                return obj.getLastModificationDate();
            case 8:
                return obj.getLastModifiedBy();
            case ID_COLUMN:
                return obj.getId();
            }

            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
            case 0:
                return ImageIcon.class;
            case 4:
                return Long.class;
            case 5:
            case 7:
                return GregorianCalendar.class;
            }

            return String.class;
        }
    }

    class FolderTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 1L;

        public FolderTransferHandler() {
            super();
        }

        @Override
        public boolean canImport(TransferSupport support) {
            if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                return false;
            }

            if (!support.isDrop()) {
                return false;
            }

            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }

            File file = null;
            try {
                List<File> fileList = (List<File>) support.getTransferable().getTransferData(
                        DataFlavor.javaFileListFlavor);

                if ((fileList == null) || (fileList.size() != 1) || (fileList.get(0) == null)
                        || !fileList.get(0).isFile()) {
                    return false;
                }

                file = fileList.get(0);
            } catch (Exception ex) {
                ClientHelper.showError(null, ex);
                return false;
            }

            new CreateDocumentDialog(null, model, file);

            return true;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            int row = getSelectedRow();
            if ((row > -1) && (row < model.getCurrentChildren().size())) {
                String id = getValueAt(row, ID_COLUMN).toString();
                CmisObject object = model.getFromCurrentChildren(id);

                if (object instanceof Document) {
                    Document doc = (Document) object;

                    File tempFile = null;
                    try {
                        tempFile = ClientHelper.createTempFileFromDocument(doc, null);
                    } catch (Exception e) {
                        ClientHelper.showError(null, e);
                    }

                    final File tempTransFile = tempFile;

                    return new Transferable() {
                        public boolean isDataFlavorSupported(DataFlavor flavor) {
                            return flavor == DataFlavor.javaFileListFlavor;
                        }

                        public DataFlavor[] getTransferDataFlavors() {
                            return new DataFlavor[] { DataFlavor.javaFileListFlavor };
                        }

                        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                            return (List<File>) Collections.singletonList(tempTransFile);
                        }
                    };
                }
            }

            return null;
        }
    }
}
