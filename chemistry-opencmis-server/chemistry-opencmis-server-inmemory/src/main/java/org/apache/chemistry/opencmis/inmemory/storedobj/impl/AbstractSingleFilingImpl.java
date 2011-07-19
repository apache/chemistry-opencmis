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
package org.apache.chemistry.opencmis.inmemory.storedobj.impl;

import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.inmemory.NameValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;

/**
 * AbstractPathImpl is the common superclass of all objects hold in the
 * repository that have a single parent, these are: Folders.
 *
 * @author Jens
 */
public abstract class AbstractSingleFilingImpl extends StoredObjectImpl implements SingleFiling {

    protected FolderImpl fParent;

    protected AbstractSingleFilingImpl(ObjectStoreImpl objStore) {
        super(objStore);
    }

    public String getPath() {
        StringBuffer path = new StringBuffer(getName());
        if (null == getParent()) {
            path.replace(0, path.length(), PATH_SEPARATOR);
        } else {
            // root folder-->
            Folder f = getParent();
            while (f.getParent() != null) {
                path.insert(0, PATH_SEPARATOR);
                path.insert(0, f.getName());
                f = f.getParent();
            }
            path.insert(0, PATH_SEPARATOR);
        }
        // if (LOG.isDebugEnabled())
        // LOG.debug("getPath() returns: " + path.toString());
        return path.toString();
    }

    public Folder getParent() {
        return fParent;
    }

    public boolean hasParent() {
      return null != fParent;
    }

    public List<Folder> getParents() {
        if (null == fParent) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList((Folder) fParent);
        }
    }

    public List<Folder> getParents(String user) {
        return getParents();
    }
    
    public void setParent(Folder parent) {
        try {
            fObjStore.lock();
            fParent = (FolderImpl) parent;
        } finally {
          fObjStore.unlock();
        }
    }

    @Override
    public void rename(String newName) {
        if (!NameValidator.isValidId(newName)) {
            throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
        }
        try {
            fObjStore.lock();
            if (getParent() == null) {
                throw new CmisInvalidArgumentException("Root folder cannot be renamed.");
            }
            if (getParent().hasChild(newName)) {
                throw new CmisNameConstraintViolationException("Cannot rename object to " + newName
                        + ". This path already exists.");
            }

            setName(newName);
        } finally {
          fObjStore.unlock();
        }
    }

    public void move(Folder oldParent, Folder newParent) {
        try {
            fObjStore.lock();
            if (this instanceof Document || this instanceof VersionedDocument) {
                fParent.moveChildDocument(this, oldParent, newParent);
            } else {// it must be a folder
                if (getParent() == null) {
                    throw new IllegalArgumentException("Root folder cannot be moved.");
                }
                if (newParent == null) {
                    throw new IllegalArgumentException("null is not a valid move target.");
                }
                if (newParent.hasChild(getName())) {
                    throw new IllegalArgumentException("Cannot move folder, this name already exists in target.");
                }

                setParent(newParent);
            }
        } finally {
          fObjStore.unlock();
        }
    }

}
