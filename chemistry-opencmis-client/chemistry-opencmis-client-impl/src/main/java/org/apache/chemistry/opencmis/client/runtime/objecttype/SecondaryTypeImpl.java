package org.apache.chemistry.opencmis.client.runtime.objecttype;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.definitions.SecondaryTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.SecondaryTypeDefinitionImpl;

public class SecondaryTypeImpl extends SecondaryTypeDefinitionImpl implements SecondaryType {

    private static final long serialVersionUID = 1L;

    private final ObjectTypeHelper helper;

    public SecondaryTypeImpl(Session session, SecondaryTypeDefinition typeDefinition) {
        assert session != null;
        assert typeDefinition != null;

        initialize(typeDefinition);
        helper = new ObjectTypeHelper(session, this);
    }

    @Override
    public ObjectType getBaseType() {
        return helper.getBaseType();
    }

    @Override
    public ItemIterable<ObjectType> getChildren() {
        return helper.getChildren();
    }

    @Override
    public List<Tree<ObjectType>> getDescendants(int depth) {
        return helper.getDescendants(depth);
    }

    @Override
    public ObjectType getParentType() {
        return helper.getParentType();
    }

    @Override
    public boolean isBaseType() {
        return helper.isBaseType();
    }
}
