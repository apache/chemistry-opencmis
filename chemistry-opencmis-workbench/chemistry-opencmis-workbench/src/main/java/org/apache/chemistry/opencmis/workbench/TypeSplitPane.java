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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.CollectionRenderer;
import org.apache.chemistry.opencmis.workbench.swing.InfoPanel;
import org.apache.chemistry.opencmis.workbench.swing.YesNoLabel;

public class TypeSplitPane extends JSplitPane {

    private static final long serialVersionUID = 1L;

    private final ClientModel model;

    private TypeInfoPanel typePanel;
    private PropertyDefinitionTable propertyDefinitionTable;

    public TypeSplitPane(ClientModel model) {
        super(JSplitPane.VERTICAL_SPLIT);

        this.model = model;

        createGUI();
    }

    protected ClientModel getClientModel() {
        return model;
    }

    private void createGUI() {
        typePanel = new TypeInfoPanel(model);
        propertyDefinitionTable = new PropertyDefinitionTable();

        setLeftComponent(new JScrollPane(typePanel));
        setRightComponent(new JScrollPane(propertyDefinitionTable));

        setDividerLocation(300);
    }

    public void setType(ObjectType type) {
        typePanel.setType(type);
        propertyDefinitionTable.setType(type);
    }

    static class TypeInfoPanel extends InfoPanel {

        private static final long serialVersionUID = 1L;

        private JTextField nameField;
        private JTextField descriptionField;
        private JTextField idField;
        private JTextField localNamespaceField;
        private JTextField localNameField;
        private JTextField queryNameField;
        private JTextField baseTypeField;
        private YesNoLabel creatableLabel;
        private YesNoLabel fileableLabel;
        private YesNoLabel queryableLabel;
        private YesNoLabel includeInSuperTypeLabel;
        private YesNoLabel fulltextIndexedLabel;
        private YesNoLabel aclLabel;
        private YesNoLabel policyLabel;
        private YesNoLabel versionableLabel;
        private JTextField contentStreamAllowedField;
        private JTextField allowedSourceTypesField;
        private JTextField allowedTargetTypesField;

        public TypeInfoPanel(ClientModel model) {
            super(model);
            createGUI();
        }

        public void setType(ObjectType type) {
            if (type != null) {
                nameField.setText(type.getDisplayName());
                descriptionField.setText(type.getDescription());
                idField.setText(type.getId());
                localNamespaceField.setText(type.getLocalNamespace());
                localNameField.setText(type.getLocalName());
                queryNameField.setText(type.getQueryName());
                baseTypeField.setText(type.getBaseTypeId().value());
                creatableLabel.setValue(is(type.isCreatable()));
                fileableLabel.setValue(is(type.isFileable()));
                queryableLabel.setValue(is(type.isQueryable()));
                includeInSuperTypeLabel.setValue(is(type.isIncludedInSupertypeQuery()));
                fulltextIndexedLabel.setValue(is(type.isFulltextIndexed()));
                aclLabel.setValue(is(type.isControllableAcl()));
                policyLabel.setValue(is(type.isControllablePolicy()));

                if (type instanceof DocumentTypeDefinition) {
                    DocumentTypeDefinition docType = (DocumentTypeDefinition) type;
                    versionableLabel.setVisible(true);
                    versionableLabel.setValue(is(docType.isVersionable()));
                    contentStreamAllowedField.setVisible(true);
                    contentStreamAllowedField.setText(docType.getContentStreamAllowed() == null ? "???" : docType
                            .getContentStreamAllowed().toString());
                } else {
                    versionableLabel.setVisible(false);
                    contentStreamAllowedField.setVisible(false);
                }

                if (type instanceof RelationshipTypeDefinition) {
                    RelationshipTypeDefinition relationshipType = (RelationshipTypeDefinition) type;
                    allowedSourceTypesField.setVisible(true);
                    allowedSourceTypesField.setText(relationshipType.getAllowedSourceTypeIds() == null ? "???"
                            : relationshipType.getAllowedSourceTypeIds().toString());
                    allowedTargetTypesField.setVisible(true);
                    allowedTargetTypesField.setText(relationshipType.getAllowedTargetTypeIds() == null ? "???"
                            : relationshipType.getAllowedTargetTypeIds().toString());
                } else {
                    allowedSourceTypesField.setVisible(false);
                    allowedTargetTypesField.setVisible(false);
                }
            } else {
                nameField.setText("");
                descriptionField.setText("");
                idField.setText("");
                localNamespaceField.setText("");
                localNameField.setText("");
                queryNameField.setText("");
                baseTypeField.setText("");
                creatableLabel.setValue(false);
                fileableLabel.setValue(false);
                queryableLabel.setValue(false);
                includeInSuperTypeLabel.setValue(false);
                fulltextIndexedLabel.setValue(false);
                aclLabel.setValue(false);
                policyLabel.setValue(false);
                versionableLabel.setVisible(false);
                contentStreamAllowedField.setVisible(false);
                allowedSourceTypesField.setVisible(false);
                allowedTargetTypesField.setVisible(false);
            }

            revalidate();
        }

        private void createGUI() {
            setupGUI();

            nameField = addLine("Name:", true);
            descriptionField = addLine("Description:");
            idField = addLine("Id:");
            localNamespaceField = addLine("Local Namespace:");
            localNameField = addLine("Local Name:");
            queryNameField = addLine("Query Name:");
            baseTypeField = addLine("Base Type:");
            creatableLabel = addYesNoLabel("Creatable:");
            fileableLabel = addYesNoLabel("Fileable:");
            queryableLabel = addYesNoLabel("Queryable:");
            includeInSuperTypeLabel = addYesNoLabel("Included in super type queries:");
            fulltextIndexedLabel = addYesNoLabel("Full text indexed:");
            aclLabel = addYesNoLabel("ACL controlable:");
            policyLabel = addYesNoLabel("Policy controlable:");
            versionableLabel = addYesNoLabel("Versionable:");
            contentStreamAllowedField = addLine("Content stream allowed:");
            allowedSourceTypesField = addLine("Allowed source types:");
            allowedTargetTypesField = addLine("Allowed target types:");
        }

        private boolean is(Boolean b) {
            if (b == null) {
                return false;
            }

            return b.booleanValue();
        }
    }

    static class PropertyDefinitionTable extends JTable {

        private static final long serialVersionUID = 1L;

        private static final String[] COLUMN_NAMES = { "Name", "Id", "Description", "Local Namespace", "Local Name",
                "Query Name", "Type", "Cardinality", "Updatability", "Queryable", "Orderable", "Required", "Inherited",
                "Default Value", "Choices" };
        private static final int[] COLUMN_WIDTHS = { 200, 200, 200, 200, 200, 200, 80, 80, 80, 50, 50, 50, 50, 200, 200 };

        private ObjectType type;
        private List<PropertyDefinition<?>> propertyDefintions;

        public PropertyDefinitionTable() {
            setDefaultRenderer(Collection.class, new CollectionRenderer());
            setModel(new PropertyDefinitionTableModel(this));

            setAutoResizeMode(AUTO_RESIZE_OFF);

            for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
                TableColumn column = getColumnModel().getColumn(i);
                column.setPreferredWidth(COLUMN_WIDTHS[i]);
            }

            final JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem("Copy to clipboard");
            popup.add(menuItem);

            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ClientHelper.copyTableToClipboard(PropertyDefinitionTable.this);
                }
            });

            addMouseListener(new MouseAdapter() {
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

            setFillsViewportHeight(true);
        }

        public void setType(ObjectType type) {
            this.type = type;

            if ((type != null) && (type.getPropertyDefinitions() != null)) {
                propertyDefintions = new ArrayList<PropertyDefinition<?>>();
                for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
                    propertyDefintions.add(propDef);
                }

                Collections.sort(propertyDefintions, new Comparator<PropertyDefinition<?>>() {
                    public int compare(PropertyDefinition<?> pd1, PropertyDefinition<?> pd2) {
                        return pd1.getId().compareTo(pd2.getId());
                    }
                });
            } else {
                propertyDefintions = null;
            }

            ((AbstractTableModel) getModel()).fireTableDataChanged();
        }

        public ObjectType getType() {
            return type;
        }

        public List<PropertyDefinition<?>> getPropertyDefinitions() {
            return propertyDefintions;
        }

        static class PropertyDefinitionTableModel extends AbstractTableModel {

            private static final long serialVersionUID = 1L;

            private final PropertyDefinitionTable table;

            public PropertyDefinitionTableModel(PropertyDefinitionTable table) {
                this.table = table;
            }

            public String getColumnName(int columnIndex) {
                return COLUMN_NAMES[columnIndex];
            }

            public int getColumnCount() {
                return COLUMN_NAMES.length;
            }

            public int getRowCount() {
                if (table.getPropertyDefinitions() == null) {
                    return 0;
                }

                return table.getPropertyDefinitions().size();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                PropertyDefinition<?> propDef = table.getPropertyDefinitions().get(rowIndex);

                switch (columnIndex) {
                case 0:
                    return propDef.getDisplayName();
                case 1:
                    return propDef.getId();
                case 2:
                    return propDef.getDescription();
                case 3:
                    return propDef.getLocalNamespace();
                case 4:
                    return propDef.getLocalName();
                case 5:
                    return propDef.getQueryName();
                case 6:
                    return propDef.getPropertyType();
                case 7:
                    return propDef.getCardinality();
                case 8:
                    return propDef.getUpdatability();
                case 9:
                    return propDef.isQueryable();
                case 10:
                    return propDef.isOrderable();
                case 11:
                    return propDef.isRequired();
                case 12:
                    return propDef.isInherited();
                case 13:
                    return propDef.getDefaultValue();
                case 14:
                    return propDef.getChoices();
                }

                return null;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if ((columnIndex == 13) || (columnIndex == 14)) {
                    return Collection.class;
                }

                return super.getColumnClass(columnIndex);
            }
        }
    }
}
