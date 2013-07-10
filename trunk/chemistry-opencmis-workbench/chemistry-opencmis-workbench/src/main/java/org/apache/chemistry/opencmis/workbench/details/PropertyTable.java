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

import java.awt.event.MouseEvent;
import java.util.Collection;

import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.workbench.PropertyEditorFrame;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

public class PropertyTable extends AbstractDetailsTable {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = { "Name", "Id", "Type", "Value" };
    private static final int[] COLUMN_WIDTHS = { 200, 200, 80, 300 };

    public PropertyTable(ClientModel model) {
        super();
        init(model, COLUMN_NAMES, COLUMN_WIDTHS);
    }

    @Override
    public void doubleClickAction(MouseEvent e, int rowIndex) {
        AllowableActions aa = getObject().getAllowableActions();

        if ((aa == null) || (aa.getAllowableActions() == null)
                || aa.getAllowableActions().contains(Action.CAN_UPDATE_PROPERTIES)) {
            new PropertyEditorFrame(getClientModel(), getObject());
        }
    }

    public int getDetailRowCount() {
        return getObject().getProperties().size();
    }

    public Object getDetailValueAt(int rowIndex, int columnIndex) {
        Property<?> property = getObject().getProperties().get(rowIndex);

        switch (columnIndex) {
        case 0:
            return property.getDefinition().getDisplayName();
        case 1:
            return property.getId();
        case 2:
            return property.getDefinition().getPropertyType().value();
        case 3:
            return property.getValues();
        }

        return null;
    }

    @Override
    public Class<?> getDetailColumClass(int columnIndex) {
        if (columnIndex == 3) {
            return Collection.class;
        }

        return super.getDetailColumClass(columnIndex);
    }
}
