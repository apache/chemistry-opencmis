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

import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.DataObjectCreator;
import org.apache.chemistry.opencmis.inmemory.FilterParser;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;

/**
 * StoredObject is the common superclass of all objects hold in the repository
 * Documents, Folders, Relationships and Policies
 *
 * @author Jens
 */
public class StoredObjectImpl implements StoredObject {

    protected String fId;
    protected String fName;
    protected String fTypeId;
    protected String fCreatedBy;
    protected String fModifiedBy;
    protected GregorianCalendar fCreatedAt;
    protected GregorianCalendar fModifiedAt;
    protected String fRepositoryId;
    protected Map<String, PropertyData<?>> fProperties;
    protected final ObjectStoreImpl fObjStore;
    protected int fAclId;

    StoredObjectImpl(ObjectStoreImpl objStore) { // visibility should be package
        GregorianCalendar now = getNow();
        now.setTime(new Date());
        fCreatedAt = now;
        fModifiedAt = now;
        fObjStore = objStore;
    }

    public String getId() {
        return fId;
    }

    public String getName() {
        return fName;
    }

    public void setName(String name) {
        fName = name;
    }

    public String getTypeId() {
        return fTypeId;
    }

    public void setTypeId(String type) {
        fTypeId = type;
    }

    public String getCreatedBy() {
        return fCreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        this.fCreatedBy = createdBy;
    }

    public String getModifiedBy() {
        return fModifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.fModifiedBy = modifiedBy;
    }

    public GregorianCalendar getCreatedAt() {
        return fCreatedAt;
    }

    public void setCreatedAt(GregorianCalendar createdAt) {
        this.fCreatedAt = createdAt;
    }

    public GregorianCalendar getModifiedAt() {
        return fModifiedAt;
    }

    public void setModifiedAtNow() {
        this.fModifiedAt = getNow();
    }

    public void setRepositoryId(String repositoryId) {
        fRepositoryId = repositoryId;
    }

    public String getRepositoryId() {
        return fRepositoryId;
    }

    public void setProperties(Map<String, PropertyData<?>> props) {
        fProperties = props;
    }

    public Map<String, PropertyData<?>> getProperties() {
        return fProperties;
    }

    public String getChangeToken() {
        GregorianCalendar lastModified = getModifiedAt();
        String token = Long.valueOf(lastModified.getTimeInMillis()).toString();
        return token;
    }

    public void rename(String newName) {
        setName(newName);
    }

    public void createSystemBasePropertiesWhenCreated(Map<String, PropertyData<?>> properties, String user) {
        addSystemBaseProperties(properties, user, true);
    }

    public void updateSystemBasePropertiesWhenModified(Map<String, PropertyData<?>> properties, String user) {
        addSystemBaseProperties(properties, user, false);
    }

    public void fillProperties(Map<String, PropertyData<?>> properties, BindingsObjectFactory objFactory,
            List<String> requestedIds) {

        if (FilterParser.isContainedInFilter(PropertyIds.NAME, requestedIds)) {
            properties.put(PropertyIds.NAME, objFactory.createPropertyStringData(PropertyIds.NAME, getName()));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.OBJECT_ID, requestedIds)) {
            properties.put(PropertyIds.OBJECT_ID, objFactory.createPropertyIdData(PropertyIds.OBJECT_ID, getId()));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.OBJECT_TYPE_ID, requestedIds)) {
            properties.put(PropertyIds.OBJECT_TYPE_ID, objFactory.createPropertyIdData(PropertyIds.OBJECT_TYPE_ID,
                    getTypeId()));
        }
        // set the base type id outside becaus it requires the type definition
        // if (FilterParser.isContainedInFilter(PropertyIds.CMIS_BASE_TYPE_ID,
        // requestedIds)) {
        // properties.add(objFactory.createPropertyIdData(PropertyIds.
        // CMIS_BASE_TYPE_ID, getBaseTypeId()));
        // }
        if (FilterParser.isContainedInFilter(PropertyIds.CREATED_BY, requestedIds)) {
            properties.put(PropertyIds.CREATED_BY, objFactory.createPropertyStringData(PropertyIds.CREATED_BY,
                    getCreatedBy()));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.CREATION_DATE, requestedIds)) {
            properties.put(PropertyIds.CREATION_DATE, objFactory.createPropertyDateTimeData(PropertyIds.CREATION_DATE,
                    getCreatedAt()));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.LAST_MODIFIED_BY, requestedIds)) {
            properties.put(PropertyIds.LAST_MODIFIED_BY, objFactory.createPropertyStringData(
                    PropertyIds.LAST_MODIFIED_BY, getModifiedBy()));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.LAST_MODIFICATION_DATE, requestedIds)) {
            properties.put(PropertyIds.LAST_MODIFICATION_DATE, objFactory.createPropertyDateTimeData(
                    PropertyIds.LAST_MODIFICATION_DATE, getModifiedAt()));
        }
        if (FilterParser.isContainedInFilter(PropertyIds.CHANGE_TOKEN, requestedIds)) {
            String token = getChangeToken();
            properties.put(PropertyIds.CHANGE_TOKEN, objFactory.createPropertyStringData(PropertyIds.CHANGE_TOKEN,
                    token));
        }

        // add custom properties of type definition to the collection
        if (null != fProperties) {
            for (Entry<String, PropertyData<?>> prop : fProperties.entrySet()) {
                if (FilterParser.isContainedInFilter(prop.getKey(), requestedIds)) {
                    properties.put(prop.getKey(), prop.getValue());
                }
            }
        }
        
    }

    // ///////////////////////////////////////////
    // private helper methods

    public void setCustomProperties(Map<String, PropertyData<?>> properties) {
        properties = new HashMap<String, PropertyData<?>>(properties); // get a
        // writable
        // collection
        removeAllSystemProperties(properties);
        setProperties(properties);
    }

    private static GregorianCalendar getNow() {
        GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        return now;
    }

    /**
     * Add CMIS_CREATED_BY, CMIS_CREATION_DATE, CMIS_LAST_MODIFIED_BY,
     * CMIS_LAST_MODIFICATION_DATE, CMIS_CHANGE_TOKEN system properties to the
     * list of properties with current values
     */
    private void addSystemBaseProperties(Map<String, PropertyData<?>> properties, String user, boolean isCreated) {
        if (user == null) {
            user = "unknown";
        }

        // Note that initial creation and modification date is set in
        // constructor.
        setModifiedBy(user);
        if (isCreated) {
            setCreatedBy(user);
            setName((String) properties.get(PropertyIds.NAME).getFirstValue());
            setTypeId((String) properties.get(PropertyIds.OBJECT_TYPE_ID).getFirstValue());
        } else {
            setModifiedAtNow();
        }
    }

    /**
     * Add CMIS_CREATED_BY, CMIS_CREATION_DATE, CMIS_LAST_MODIFIED_BY,
     * CMIS_LAST_MODIFICATION_DATE, CMIS_CHANGE_TOKEN system properties to the
     * list of properties with current values
     */
    protected void setSystemBasePropertiesWhenCreatedDirect(String name, String typeId, String user) {
        // Note that initial creation and modification date is set in
        // constructor.
        setModifiedBy(user);
        setCreatedBy(user);
        setName(name);
        setTypeId(typeId);
    }

    /*
     * CMIS_NAME CMIS_OBJECT_ID CMIS_OBJECT_TYPE_ID CMIS_BASE_TYPE_ID
     * CMIS_CREATED_BY CMIS_CREATION_DATE CMIS_LAST_MODIFIED_BY
     * CMIS_LAST_MODIFICATION_DATE CMIS_CHANGE_TOKEN
     *
     * // ---- document ---- CMIS_IS_IMMUTABLE CMIS_IS_LATEST_VERSION
     * CMIS_IS_MAJOR_VERSION CMIS_IS_LATEST_MAJOR_VERSION CMIS_VERSION_LABEL
     * CMIS_VERSION_SERIES_ID CMIS_IS_VERSION_SERIES_CHECKED_OUT
     * CMIS_VERSION_SERIES_CHECKED_OUT_BY CMIS_VERSION_SERIES_CHECKED_OUT_ID
     * CMIS_CHECKIN_COMMENT CMIS_CONTENT_STREAM_LENGTH
     * CMIS_CONTENT_STREAM_MIME_TYPE CMIS_CONTENT_STREAM_FILE_NAME
     * CMIS_CONTENT_STREAM_ID
     *
     * // ---- folder ---- CMIS_PARENT_ID CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS
     * CMIS_PATH
     *
     * // ---- relationship ---- CMIS_SOURCE_ID CMIS_TARGET_ID
     *
     * // ---- policy ---- CMIS_POLICY_TEXT
     */
    private static void removeAllSystemProperties(Map<String, PropertyData<?>> properties) {
        // ---- base ----
        if (properties.containsKey(PropertyIds.NAME)) {
            properties.remove(PropertyIds.NAME);
        }
        if (properties.containsKey(PropertyIds.OBJECT_ID)) {
            properties.remove(PropertyIds.OBJECT_ID);
        }
        if (properties.containsKey(PropertyIds.OBJECT_TYPE_ID)) {
            properties.remove(PropertyIds.OBJECT_TYPE_ID);
        }
        if (properties.containsKey(PropertyIds.BASE_TYPE_ID)) {
            properties.remove(PropertyIds.BASE_TYPE_ID);
        }
        if (properties.containsKey(PropertyIds.CREATED_BY)) {
            properties.remove(PropertyIds.CREATED_BY);
        }
        if (properties.containsKey(PropertyIds.CREATION_DATE)) {
            properties.remove(PropertyIds.CREATION_DATE);
        }
        if (properties.containsKey(PropertyIds.LAST_MODIFIED_BY)) {
            properties.remove(PropertyIds.LAST_MODIFIED_BY);
        }
        if (properties.containsKey(PropertyIds.LAST_MODIFICATION_DATE)) {
            properties.remove(PropertyIds.LAST_MODIFICATION_DATE);
        }
        if (properties.containsKey(PropertyIds.CHANGE_TOKEN)) {
            properties.remove(PropertyIds.CHANGE_TOKEN);
        }
        // ---- document ----
        if (properties.containsKey(PropertyIds.IS_IMMUTABLE)) {
            properties.remove(PropertyIds.IS_IMMUTABLE);
        }
        if (properties.containsKey(PropertyIds.IS_LATEST_VERSION)) {
            properties.remove(PropertyIds.IS_LATEST_VERSION);
        }
        if (properties.containsKey(PropertyIds.IS_MAJOR_VERSION)) {
            properties.remove(PropertyIds.IS_MAJOR_VERSION);
        }
        if (properties.containsKey(PropertyIds.IS_LATEST_MAJOR_VERSION)) {
            properties.remove(PropertyIds.IS_LATEST_MAJOR_VERSION);
        }
        if (properties.containsKey(PropertyIds.VERSION_LABEL)) {
            properties.remove(PropertyIds.VERSION_LABEL);
        }
        if (properties.containsKey(PropertyIds.VERSION_SERIES_ID)) {
            properties.remove(PropertyIds.VERSION_SERIES_ID);
        }
        if (properties.containsKey(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT)) {
            properties.remove(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
        }
        if (properties.containsKey(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY)) {
            properties.remove(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY);
        }
        if (properties.containsKey(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID)) {
            properties.remove(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID);
        }
        if (properties.containsKey(PropertyIds.CHECKIN_COMMENT)) {
            properties.remove(PropertyIds.CHECKIN_COMMENT);
        }
        if (properties.containsKey(PropertyIds.CONTENT_STREAM_LENGTH)) {
            properties.remove(PropertyIds.CONTENT_STREAM_LENGTH);
        }
        if (properties.containsKey(PropertyIds.CONTENT_STREAM_MIME_TYPE)) {
            properties.remove(PropertyIds.CONTENT_STREAM_MIME_TYPE);
        }
        if (properties.containsKey(PropertyIds.CONTENT_STREAM_FILE_NAME)) {
            properties.remove(PropertyIds.CONTENT_STREAM_FILE_NAME);
        }
        if (properties.containsKey(PropertyIds.CONTENT_STREAM_ID)) {
            properties.remove(PropertyIds.CONTENT_STREAM_ID);
        }
        // ---- folder ----
        if (properties.containsKey(PropertyIds.PARENT_ID)) {
            properties.remove(PropertyIds.PARENT_ID);
        }
        if (properties.containsKey(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS)) {
            properties.remove(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
        }
        if (properties.containsKey(PropertyIds.PATH)) {
            properties.remove(PropertyIds.PATH);
        }
        // ---- relationship ----
        if (properties.containsKey(PropertyIds.SOURCE_ID)) {
            properties.remove(PropertyIds.SOURCE_ID);
        }
        if (properties.containsKey(PropertyIds.TARGET_ID)) {
            properties.remove(PropertyIds.TARGET_ID);
        }
        // ---- policy ----
        if (properties.containsKey(PropertyIds.POLICY_TEXT)) {
            properties.remove(PropertyIds.POLICY_TEXT);
        }
    }

    public void persist() {
        // in-memory implementation does not need to to anything to persist,
        // but after this call the id should be set.
        fId = fObjStore.storeObject(this);
    }

	public Acl getAcl() {
	    return fObjStore.getAcl(fAclId);
	}

	public int getAclId() {
	    return fAclId;
	}
	
	public void setAclId(int aclId) {
	    fAclId = aclId;
	}
	
	public ObjectList getObjectRelationships(
			Boolean includeSubRelationshipTypes,
			RelationshipDirection relationshipDirection, String typeId,
			String filter, Boolean includeAllowableActions,
			BigInteger maxItems, BigInteger skipCount,
			ExtensionsData extension, String user) {
		return null;
	}

	public AllowableActions getAllowableActions(String user) {
		AllowableActions actions = DataObjectCreator.fillAllowableActions(this, user);
		return actions;
	}

    public List<RenditionData> getRenditions(String renditionFilter, long maxItems, long skipCount) {
        return null;
    }

    public ContentStream getRenditionContent(String streamId, long offset, long length) {
        return null;
    }

    public boolean hasRendition(String user) {
        return false;
    }
}
