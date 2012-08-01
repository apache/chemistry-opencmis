package org.apache.chemistry.opencmis.inmemory.storedobj.impl;
/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.FilterParser;
import org.apache.chemistry.opencmis.inmemory.NameValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Filing;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FolderImpl extends AbstractSingleFilingImpl implements Folder {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSingleFilingImpl.class.getName());

    FolderImpl(ObjectStoreImpl objStore) {
        super(objStore);
    }

    public FolderImpl(ObjectStoreImpl objStore, String name, Folder parent) {
        super(objStore);
        init(name, parent);
    }

    public void addChildFolder(Folder folder) {
        try {
            fObjStore.lock();
            boolean hasChild;
            String name = folder.getName();
            hasChild = hasChild(name);
            if (hasChild) {
                throw new CmisNameConstraintViolationException("Cannot create folder " + name + ". Name already exists in parent folder");
            }
            folder.setParent(this);
        } finally {
            fObjStore.unlock();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.opencmis.client.provider.spi.inmemory.IFolder#addChildDocument(org
     * .opencmis.client.provider .spi.inmemory.storedobj.impl.DocumentImpl)
     */
    public void addChildDocument(Document doc) {
        addChildObject(doc);
    }

    public void addChildDocument(VersionedDocument doc) {
        addChildObject(doc);
    }

    private void addChildObject(StoredObject so) {
        try {
            fObjStore.lock();
            String name = so.getName();

            boolean hasChild;
            hasChild = hasChild(name);
            if (hasChild) {
                throw new CmisNameConstraintViolationException(
                        "Cannot create object: " + name + ". Name already exists in parent folder");
            }

            if (so instanceof SingleFiling) {
                ((SingleFiling) so).setParent(this);
            } else if (so instanceof MultiFiling) {
                ((MultiFiling) so).addParent(this);
            } else {
                throw new CmisInvalidArgumentException("Cannot create document, object is not fileable.");
            }

        } finally {
            fObjStore.unlock();
        }
    }

    public ChildrenResult getChildren(int maxItems, int skipCount, String user) {
        List<StoredObject> result = new ArrayList<StoredObject>();
        for (String id : fObjStore.getIds()) {
            StoredObject obj = fObjStore.getObject(id);
            Filing pathObj = (Filing) obj;
            if (fObjStore.hasReadAccess(user, obj) && pathObj.getParents(user).contains(this)) {
                if (pathObj instanceof VersionedDocument) {
                    DocumentVersion ver = ((VersionedDocument) pathObj).getLatestVersion(false);
                    result.add(ver);
                } else if (pathObj instanceof DocumentVersion) {
                    // ignore
                } else {
                    result.add(obj);
                }
            }
        }
        sortFolderList(result);

        if (maxItems < 0) {
            maxItems = result.size();
        }
        if (skipCount < 0) {
            skipCount = 0;
        }
        
        int from = Math.min(skipCount, result.size());
        int to = Math.min(maxItems + from, result.size());
        int noItems = result.size();
        
        result = result.subList(from, to);
        return new ChildrenResult(result, noItems);
    }

    public ChildrenResult getFolderChildren(int maxItems, int skipCount, String user) {
        List<Folder> result = new ArrayList<Folder>();
        for (String id : fObjStore.getIds()) {
            StoredObject obj = fObjStore.getObject(id);
            if (fObjStore.hasReadAccess(user, obj) && obj instanceof SingleFiling) {
                SingleFiling pathObj = (SingleFiling) obj;
                if (pathObj.getParent() == this && pathObj instanceof Folder) {
                    result.add((Folder) obj);
                }
            }
        }
        sortFolderList(result);
        int from = Math.min(skipCount, result.size());
        int to = Math.min(maxItems + from, result.size());
        int noItems = result.size();

        result = result.subList(from, to);
        return new ChildrenResult(result, noItems);
    }

    public boolean hasChild(String name) {
        for (String id : fObjStore.getIds()) {
            StoredObject obj = fObjStore.getObject(id);
            if (obj instanceof Filing) {
                Filing pathObj = (Filing) obj;
                if (pathObj.getParents(null).contains(this) && obj.getName().equals(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void fillProperties(Map<String, PropertyData<?>> properties, BindingsObjectFactory objFactory,
            List<String> requestedIds) {

        super.fillProperties(properties, objFactory, requestedIds);

        // add folder specific properties

        if (FilterParser.isContainedInFilter(PropertyIds.PARENT_ID, requestedIds)) {
            String parentId = getParent() == null ? null : getParent().getId();
            properties.put(PropertyIds.PARENT_ID, objFactory.createPropertyIdData(PropertyIds.PARENT_ID,
                    parentId));
        }

        if (FilterParser.isContainedInFilter(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, requestedIds)) {
            String allowedChildObjects = "*"; // TODO: not yet supported
            properties.put(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, objFactory.createPropertyIdData(
                    PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, allowedChildObjects));
        }

        if (FilterParser.isContainedInFilter(PropertyIds.PATH, requestedIds)) {
            String path = getPath();
            properties.put(PropertyIds.PATH, objFactory.createPropertyStringData(PropertyIds.PATH, path));
        }
    }

    // Helper functions
    private void init(String name, Folder parent) {
        if (!NameValidator.isValidName(name)) {
            throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
        }
        setName(name);
        setParent(parent);
    }

    private static void sortFolderList(List<? extends StoredObject> list) {
        // TODO evaluate orderBy, for now sort by path segment
        class FolderComparator implements Comparator<StoredObject> {

            public int compare(StoredObject f1, StoredObject f2) {
                String segment1 = f1.getName();
                String segment2 = f2.getName();

                return segment1.compareTo(segment2);
            }
        }

        Collections.sort(list, new FolderComparator());
    }

    public void moveChildDocument(StoredObject so, Folder oldParent, Folder newParent) {
        try {
            fObjStore.lock();
            if (newParent.hasChild(so.getName())) {
                throw new IllegalArgumentException("Cannot move object, this name already exists in target.");
            }
            if (!(so instanceof Filing)) {
                throw new IllegalArgumentException("Cannot move object, object does not have a path.");
            }

            if (so instanceof SingleFiling) {
                SingleFiling pathObj = (SingleFiling) so;
                pathObj.setParent(newParent);
            } else if (so instanceof MultiFiling) {
                MultiFiling pathObj = (MultiFiling) so;
                pathObj.addParent(newParent);
                pathObj.removeParent(oldParent);
            }
        } finally {
            fObjStore.unlock();
        }
    }

    public List<String> getAllowedChildObjectTypeIds() {
        // TODO implement this.
        return null;
    }

}
