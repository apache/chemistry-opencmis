package org.apache.chemistry.opencmis.workbench.types;

import java.util.List;

import javax.swing.JTextField;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.BaseTypeLabel;
import org.apache.chemistry.opencmis.workbench.swing.ExtensionsTree;
import org.apache.chemistry.opencmis.workbench.swing.InfoPanel;
import org.apache.chemistry.opencmis.workbench.swing.InfoTreePane;
import org.apache.chemistry.opencmis.workbench.swing.YesNoLabel;

public class TypeDefinitionInfoPanel extends InfoPanel {

    private static final long serialVersionUID = 1L;

    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField idField;
    private JTextField localNamespaceField;
    private JTextField localNameField;
    private JTextField queryNameField;
    private BaseTypeLabel baseTypeField;
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
    private JTextField typeMutabilityField;
    private InfoTreePane<List<CmisExtensionElement>> extensionsTree;

    public TypeDefinitionInfoPanel(ClientModel model) {
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
            baseTypeField.setValue(type.getBaseTypeId());
            creatableLabel.setValue(is(type.isCreatable()));
            fileableLabel.setValue(is(type.isFileable()));
            queryableLabel.setValue(is(type.isQueryable()));
            includeInSuperTypeLabel.setValue(is(type.isIncludedInSupertypeQuery()));
            fulltextIndexedLabel.setValue(is(type.isFulltextIndexed()));
            aclLabel.setValue(is(type.isControllableAcl()));
            policyLabel.setValue(is(type.isControllablePolicy()));

            if (type.getTypeMutability() != null) {
                StringBuilder sb = new StringBuilder(64);

                if (Boolean.TRUE.equals(type.getTypeMutability().canCreate())) {
                    sb.append("create");
                }

                if (Boolean.TRUE.equals(type.getTypeMutability().canUpdate())) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append("update");
                }

                if (Boolean.TRUE.equals(type.getTypeMutability().canDelete())) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append("delete");
                }

                typeMutabilityField.setText(sb.toString());
            } else {
                typeMutabilityField.setText("");
            }

            if (type instanceof DocumentTypeDefinition) {
                DocumentTypeDefinition docType = (DocumentTypeDefinition) type;
                versionableLabel.setVisible(true);
                versionableLabel.setValue(is(docType.isVersionable()));
                contentStreamAllowedField.setVisible(true);
                contentStreamAllowedField.setText(docType.getContentStreamAllowed() == null ? "???" : docType
                        .getContentStreamAllowed().value());
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

            extensionsTree.setData(type.getExtensions());
        } else {
            nameField.setText("");
            descriptionField.setText("");
            idField.setText("");
            localNamespaceField.setText("");
            localNameField.setText("");
            queryNameField.setText("");
            baseTypeField.setValue(null);
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
            typeMutabilityField.setText("");
            extensionsTree.setData(null);
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
        baseTypeField = addBaseTypeLabel("Base Type:");
        creatableLabel = addYesNoLabel("Creatable:");
        fileableLabel = addYesNoLabel("Fileable:");
        queryableLabel = addYesNoLabel("Queryable:");
        includeInSuperTypeLabel = addYesNoLabel("In Super Type Queries:");
        fulltextIndexedLabel = addYesNoLabel("Full Text Indexed:");
        aclLabel = addYesNoLabel("ACL Controlable:");
        policyLabel = addYesNoLabel("Policy Controlable:");
        typeMutabilityField = addLine("Type Mutability:");
        versionableLabel = addYesNoLabel("Versionable:");
        contentStreamAllowedField = addLine("Content Stream Allowed:");
        allowedSourceTypesField = addLine("Allowed Source Types:");
        allowedTargetTypesField = addLine("Allowed Target Types:");
        extensionsTree = addComponent("Extensions:", new InfoTreePane<List<CmisExtensionElement>>(new ExtensionsTree()));
    }

    private boolean is(Boolean b) {
        if (b == null) {
            return false;
        }

        return b.booleanValue();
    }
}