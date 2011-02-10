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

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.workbench.ClientHelper;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;

public class VersionTable extends AbstractDetailsTable {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = { "Name", "Label", "Latest", "Major", "Latest Major", "Id", "Filename", "MIME Type", "Length" };
    private static final int[] COLUMN_WIDTHS = { 200, 200, 50, 50, 50, 400, 200, 100, 100 };

    private static final int OLD = 60 * 1000;

    private List<Document> versions;
    private String lastId;
    private long lastTimestamp;

    public VersionTable(ClientModel model) {
        super();

        versions = Collections.emptyList();
        lastId = null;
        lastTimestamp = System.currentTimeMillis();
        init(model, COLUMN_NAMES, COLUMN_WIDTHS);
    }

    @Override
    public void doubleClickAction(MouseEvent e, int rowIndex) {
        if (e.isShiftDown()) {
            ClientHelper.download(this.getParent(), getVersions().get(rowIndex), null);
        } else {
            ClientHelper.open(this.getParent(), getVersions().get(rowIndex), null);
        }
    }

    @Override
    public int getDetailRowCount() {
        if (!(getObject() instanceof Document)) {
            return 0;
        }

        return getVersions().size();
    }

    @Override
    public Object getDetailValueAt(int rowIndex, int columnIndex) {
        Document version = getVersions().get(rowIndex);

        switch (columnIndex) {
        case 0:
            return version.getName();
        case 1:
            return version.getVersionLabel();
        case 2:
            return version.isLatestVersion();
        case 3:
            return version.isMajorVersion();
        case 4:
            return version.isLatestMajorVersion();
        case 5:
            return version.getId();
        case 6:
            return version.getContentStreamFileName();
        case 7:
            return version.getContentStreamMimeType();
        case 8:
            return version.getContentStreamLength() == -1 ? null : version.getContentStreamLength();
        }

        return null;
    }

    @Override
    public Class<?> getDetailColumClass(int columnIndex) {
        if((columnIndex == 2) || (columnIndex == 3) || (columnIndex == 4)) {
            return Boolean.class;            
        }
        else if (columnIndex == 8) {
            return Long.class;
        }

        return super.getDetailColumClass(columnIndex);
    }

    private List<Document> getVersions() {
        // not a document -> no versions
        if (!(getObject() instanceof Document)) {
            versions = Collections.emptyList();
            lastId = null;

            return versions;
        }

        // if the versions have been fetched recently, don't reload
        Document doc = (Document) getObject();
        if (doc.getId().equals(lastId)) {
            if (lastTimestamp + OLD > System.currentTimeMillis()) {
                return versions;
            }
        }

        // reset everything
        lastId = doc.getId();
        lastTimestamp = System.currentTimeMillis();
        versions = Collections.emptyList();

        // get versions
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            versions = doc.getAllVersions();
        } catch (Exception ex) {
            if (!(ex instanceof CmisNotSupportedException)) {
                ClientHelper.showError(null, ex);
            }
        } finally {
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }

        return versions;
    }
}
