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
package org.apache.opencmis.inmemory.storedobj.impl;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;

import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.inmemory.FilterParser;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;

/**
 * InMemory Stored Object
 * 
 * @author Jens
 * 
 */

/**
 * StoredObject is the common superclass of all objects hold in the repository Documents, Folders,
 * Relationships and Policies
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
  
  StoredObjectImpl() {  // visibility should be package    
    GregorianCalendar now = getNow();
    now.setTime(new Date());
    fCreatedAt = now;
    fModifiedAt = now;
  }
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#getId()
   */
  public String getId() {
    return fId;
  }
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#getName()
   */
  public String getName() {
    return fName;
  }
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#setName(java.lang.String)
   */
  public void setName(String name) {
    fName = name;
  }
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#getTypeId()
   */
  public String getTypeId() {
    return fTypeId;
  }
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#setTypeId(java.lang.String)
   */
  public void setTypeId(String type) {
    fTypeId = type;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#getCreatedBy()
   */
  public String getCreatedBy() {
    return fCreatedBy;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#setCreatedBy(java.lang.String)
   */
  public void setCreatedBy(String createdBy) {
    this.fCreatedBy = createdBy;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#getModifiedBy()
   */
  public String getModifiedBy() {
    return fModifiedBy;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#setModifiedBy(java.lang.String)
   */
  public void setModifiedBy(String modifiedBy) {
    this.fModifiedBy = modifiedBy;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#getCreatedAt()
   */
  public GregorianCalendar getCreatedAt() {
    return fCreatedAt;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#setCreatedAt(java.util.GregorianCalendar)
   */
  public void setCreatedAt(GregorianCalendar createdAt) {
    this.fCreatedAt = createdAt;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#getModifiedAt()
   */
  public GregorianCalendar getModifiedAt() {
    return fModifiedAt;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#setModifiedAtNow()
   */
  public void setModifiedAtNow() {
    this.fModifiedAt = getNow();
  }
  
  public void setRepositoryId(String repositoryId) {
	  fRepositoryId = repositoryId;
  }
  
  public String getRepositoryId() {
	  return fRepositoryId;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#setProperties(java.util.Map)
   */
  public void setProperties(Map<String, PropertyData<?>> props) {
    fProperties = props;
  }
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#getProperties()
   */
  public Map<String, PropertyData<?>> getProperties() {
    return fProperties;
  }
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.storedobj.api.StoredObject#getChangeToken()
   */
  public String getChangeToken() {
    GregorianCalendar lastModified = getModifiedAt();
    String token = Long.valueOf(lastModified.getTimeInMillis()).toString();
    return token;
  }
  
  public void rename(String newName) {
    setName(newName);
    persist();
  }
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#createSystemBasePropertiesWhenCreated(java.util.Map, java.lang.String)
   */
  public void createSystemBasePropertiesWhenCreated(
      Map<String, PropertyData<?>> properties,
      String user) {
    addSystemBaseProperties(properties, user, true);
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#updateSystemBasePropertiesWhenModified(java.util.Map, java.lang.String)
   */
  public void updateSystemBasePropertiesWhenModified(
      Map<String, PropertyData<?>> properties,
      String user) {
    addSystemBaseProperties(properties, user, false);
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#fillProperties(java.util.List, org.apache.opencmis.client.provider.ProviderObjectFactory, java.util.List)
   */
  public void fillProperties(List<PropertyData<?>> properties,
      ProviderObjectFactory objFactory, List<String> requestedIds) {
    
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_NAME, requestedIds)) {
      properties.add(objFactory.createPropertyStringData(PropertyIds.CMIS_NAME, getName()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_OBJECT_ID, requestedIds)) {
      properties.add(objFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_ID, getId()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_OBJECT_TYPE_ID, requestedIds)) {
      properties.add(objFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID, getTypeId()));
    }
    // set the base type id outside becaus it requires the type definition
//    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_BASE_TYPE_ID, requestedIds)) {
//      properties.add(objFactory.createPropertyIdData(PropertyIds.CMIS_BASE_TYPE_ID, getBaseTypeId()));
//    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_CREATED_BY, requestedIds)) {
      properties.add(objFactory.createPropertyStringData(PropertyIds.CMIS_CREATED_BY, getCreatedBy()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_CREATION_DATE, requestedIds)) {
      properties.add(objFactory.createPropertyDateTimeData(PropertyIds.CMIS_CREATION_DATE, getCreatedAt()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_LAST_MODIFIED_BY, requestedIds)) {
      properties.add(objFactory.createPropertyStringData(PropertyIds.CMIS_LAST_MODIFIED_BY, getModifiedBy()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_LAST_MODIFICATION_DATE, requestedIds)) {
      properties.add(objFactory.createPropertyDateTimeData(PropertyIds.CMIS_LAST_MODIFICATION_DATE, getModifiedAt()));
    }
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_CHANGE_TOKEN, requestedIds)) {
      String token = getChangeToken();
      properties.add(objFactory.createPropertyStringData(PropertyIds.CMIS_CHANGE_TOKEN, token));
    }

    // add custom properties of type definition to the collection
    if (null != fProperties) {
      for (Entry<String, PropertyData<?>> prop: fProperties.entrySet()) {
        properties.add(prop.getValue());      
      }
    }
  }


  /////////////////////////////////////////////
  // private helper methods
  
  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.spi.inmemory.IStoredObject#setCustomProperties(java.util.Map)
   */
  public void setCustomProperties(Map<String, PropertyData<?>> properties) {
    properties = new HashMap<String, PropertyData<?>>(properties); // get a writable collection
    removeAllSystemProperties(properties);    
    setProperties(properties);        
  }

  private GregorianCalendar getNow() {
    GregorianCalendar now = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    return now; 
  }
  
  /**
   * Add CMIS_CREATED_BY, CMIS_CREATION_DATE, CMIS_LAST_MODIFIED_BY, CMIS_LAST_MODIFICATION_DATE,
   * CMIS_CHANGE_TOKEN system properties to the list of properties with current values
   */
  private void addSystemBaseProperties(Map<String, PropertyData<?>> properties, String user, boolean isCreated) {
    if (user == null)
      user = "unknown";

    // Note that initial creation and modification date is set in constructor.
    setModifiedBy(user);
    if (isCreated) {
      setCreatedBy(user);
      setName((String) properties.get(PropertyIds.CMIS_NAME).getFirstValue());
      setTypeId((String) properties.get(PropertyIds.CMIS_OBJECT_TYPE_ID).getFirstValue());
    } else {
      setModifiedAtNow();
    }      
  }
  
  /**
   * Add CMIS_CREATED_BY, CMIS_CREATION_DATE, CMIS_LAST_MODIFIED_BY, CMIS_LAST_MODIFICATION_DATE,
   * CMIS_CHANGE_TOKEN system properties to the list of properties with current values
   */
  protected void setSystemBasePropertiesWhenCreatedDirect(String name, String typeId, String user) {
    // Note that initial creation and modification date is set in constructor.
    setModifiedBy(user);
    setCreatedBy(user);
    setName(name);
    setTypeId(typeId);    
  }
  
/*  
  CMIS_NAME
  CMIS_OBJECT_ID
  CMIS_OBJECT_TYPE_ID
  CMIS_BASE_TYPE_ID
  CMIS_CREATED_BY
  CMIS_CREATION_DATE
  CMIS_LAST_MODIFIED_BY
  CMIS_LAST_MODIFICATION_DATE
  CMIS_CHANGE_TOKEN 

  // ---- document ----
  CMIS_IS_IMMUTABLE
  CMIS_IS_LATEST_VERSION
  CMIS_IS_MAJOR_VERSION
  CMIS_IS_LATEST_MAJOR_VERSION
  CMIS_VERSION_LABEL
  CMIS_VERSION_SERIES_ID
  CMIS_IS_VERSION_SERIES_CHECKED_OUT
  CMIS_VERSION_SERIES_CHECKED_OUT_BY
  CMIS_VERSION_SERIES_CHECKED_OUT_ID
  CMIS_CHECKIN_COMMENT
  CMIS_CONTENT_STREAM_LENGTH
  CMIS_CONTENT_STREAM_MIME_TYPE
  CMIS_CONTENT_STREAM_FILE_NAME
  CMIS_CONTENT_STREAM_ID

   // ---- folder ---- 
  CMIS_PARENT_ID
  CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS
  CMIS_PATH

    // ---- relationship ----
  CMIS_SOURCE_ID
  CMIS_TARGET_ID

    // ---- policy ----
  CMIS_POLICY_TEXT
  */
  private void removeAllSystemProperties(Map<String, PropertyData<?>> properties) {
    // ---- base ----
    if (properties.containsKey(PropertyIds.CMIS_NAME))
      properties.remove(PropertyIds.CMIS_NAME);
    if (properties.containsKey(PropertyIds.CMIS_OBJECT_ID))
      properties.remove(PropertyIds.CMIS_OBJECT_ID);
    if (properties.containsKey(PropertyIds.CMIS_OBJECT_TYPE_ID))
      properties.remove(PropertyIds.CMIS_OBJECT_TYPE_ID);
    if (properties.containsKey(PropertyIds.CMIS_BASE_TYPE_ID))
      properties.remove(PropertyIds.CMIS_BASE_TYPE_ID);
    if (properties.containsKey(PropertyIds.CMIS_CREATED_BY))
      properties.remove(PropertyIds.CMIS_CREATED_BY);
    if (properties.containsKey(PropertyIds.CMIS_CREATION_DATE))
      properties.remove(PropertyIds.CMIS_CREATION_DATE);
    if (properties.containsKey(PropertyIds.CMIS_LAST_MODIFIED_BY))
      properties.remove(PropertyIds.CMIS_LAST_MODIFIED_BY);
    if (properties.containsKey(PropertyIds.CMIS_LAST_MODIFICATION_DATE))
      properties.remove(PropertyIds.CMIS_LAST_MODIFICATION_DATE);
    if (properties.containsKey(PropertyIds.CMIS_CHANGE_TOKEN))
      properties.remove(PropertyIds.CMIS_CHANGE_TOKEN);
    // ---- document ----
    if (properties.containsKey(PropertyIds.CMIS_IS_IMMUTABLE))
      properties.remove(PropertyIds.CMIS_IS_IMMUTABLE);
    if (properties.containsKey(PropertyIds.CMIS_IS_LATEST_VERSION))
      properties.remove(PropertyIds.CMIS_IS_LATEST_VERSION);
    if (properties.containsKey(PropertyIds.CMIS_IS_MAJOR_VERSION))
      properties.remove(PropertyIds.CMIS_IS_MAJOR_VERSION);
    if (properties.containsKey(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION))
      properties.remove(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION);
    if (properties.containsKey(PropertyIds.CMIS_VERSION_LABEL))
      properties.remove(PropertyIds.CMIS_VERSION_LABEL);
    if (properties.containsKey(PropertyIds.CMIS_VERSION_SERIES_ID))
      properties.remove(PropertyIds.CMIS_VERSION_SERIES_ID);
    if (properties.containsKey(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT))
      properties.remove(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT);
    if (properties.containsKey(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY))
      properties.remove(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY);
    if (properties.containsKey(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID))
      properties.remove(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID);
    if (properties.containsKey(PropertyIds.CMIS_CHECKIN_COMMENT))
      properties.remove(PropertyIds.CMIS_CHECKIN_COMMENT);
    if (properties.containsKey(PropertyIds.CMIS_CONTENT_STREAM_LENGTH))
      properties.remove(PropertyIds.CMIS_CONTENT_STREAM_LENGTH);
    if (properties.containsKey(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE))
      properties.remove(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE);
    if (properties.containsKey(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME))
      properties.remove(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME);
    if (properties.containsKey(PropertyIds.CMIS_CONTENT_STREAM_ID))
      properties.remove(PropertyIds.CMIS_CONTENT_STREAM_ID);
    // ---- folder ----     
    if (properties.containsKey(PropertyIds.CMIS_PARENT_ID))
      properties.remove(PropertyIds.CMIS_PARENT_ID);
    if (properties.containsKey(PropertyIds.CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS))
      properties.remove(PropertyIds.CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS);
    if (properties.containsKey(PropertyIds.CMIS_PATH))
      properties.remove(PropertyIds.CMIS_PATH);
    // ---- relationship ----
    if (properties.containsKey(PropertyIds.CMIS_SOURCE_ID))
      properties.remove(PropertyIds.CMIS_SOURCE_ID);
    if (properties.containsKey(PropertyIds.CMIS_TARGET_ID))
      properties.remove(PropertyIds.CMIS_TARGET_ID);
    // ---- policy ----
    if (properties.containsKey(PropertyIds.CMIS_POLICY_TEXT))
      properties.remove(PropertyIds.CMIS_POLICY_TEXT);
  }

public void persist() {
  // in-memory implementation does not need to to anything to persist,
  // but after this call the id should be set.
}


}
