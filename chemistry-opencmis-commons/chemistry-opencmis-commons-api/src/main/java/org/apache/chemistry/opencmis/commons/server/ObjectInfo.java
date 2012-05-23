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
package org.apache.chemistry.opencmis.commons.server;

import java.util.GregorianCalendar;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * This class contains information about an object. This data is used to
 * generate the appropriate links in AtomPub entries and feeds.
 */
public interface ObjectInfo {

    /**
     * Returns the object id.
     */
    String getId();

    /**
     * Returns an id for the atom:id tag. This id must comply with the Atom
     * specification. If this method returns <code>null</code>, OpenCMIS
     * generates a valid id.
     */
    String getAtomId();

    /**
     * Returns the object name.
     */
    String getName();

    /**
     * Returns the creator.
     */
    String getCreatedBy();

    /**
     * Returns the creation date.
     */
    GregorianCalendar getCreationDate();

    /**
     * Returns the last modification date.
     */
    GregorianCalendar getLastModificationDate();

    /**
     * Returns the type id.
     */
    String getTypeId();

    /**
     * Returns the base type.
     */
    BaseTypeId getBaseType();

    /**
     * Returns <code>true</code> if the object is a document and if it is the
     * current version or it is not versionable, <code>false</code> otherwise.
     */
    boolean isCurrentVersion();

    /**
     * Returns the version series id if the object is a document and it is
     * versionable, <code>null</code> otherwise.
     */
    String getVersionSeriesId();

    /**
     * Returns the working copy id if the object is a document and a working
     * copy exists, <code>null</code> otherwise.
     */
    String getWorkingCopyId();

    /**
     * Returns the original id of the working copy if the object is a document
     * and a working copy, <code>null</code> otherwise.
     */
    String getWorkingCopyOriginalId();

    /**
     * Returns <code>true</code> if the object is a document and has content,
     * <code>false</code> otherwise.
     */
    boolean hasContent();

    /**
     * Returns the content type of the content if the object is a document and
     * has content, <code>null</code> otherwise.
     */
    String getContentType();

    /**
     * Returns the file name of the content if the object is a document and has
     * content, <code>null</code> otherwise.
     */
    String getFileName();

    /**
     * Returns rendition information if the object has renditions,
     * <code>null</code> otherwise.
     */
    List<RenditionInfo> getRenditionInfos();

    /**
     * Returns <code>true</code> if the object supports relationships even if no
     * relationships exist, <code>false</code> otherwise.
     */
    boolean supportsRelationships();

    /**
     * Returns <code>true</code> if the object supports policies even if no
     * policies are applied, <code>false</code> otherwise.
     */
    boolean supportsPolicies();

    /**
     * Returns <code>true</code> if the object has an ACL, <code>false</code>
     * otherwise.
     */
    boolean hasAcl();

    /**
     * Returns <code>true</code> if the object has at least one parent,
     * <code>false</code> otherwise.
     */
    boolean hasParent();

    /**
     * Returns <code>true</code> if the object is a folder and supports
     * <code>getDescendants</code>, <code>false</code> otherwise.
     */
    boolean supportsDescendants();

    /**
     * Returns <code>true</code> if the object is a folder and supports
     * <code>getFolderTree</code>, <code>false</code> otherwise.
     */
    boolean supportsFolderTree();

    /**
     * Returns the list of ids of the relationships that originate from this
     * object, <code>null</code> is no such relationships exist.
     */
    List<String> getRelationshipSourceIds();

    /**
     * Returns the list of ids of the relationships that point to this object,
     * <code>null</code> is no such relationships exist.
     */
    List<String> getRelationshipTargetIds();

    /**
     * Returns the full object.
     */
    ObjectData getObject();
}
