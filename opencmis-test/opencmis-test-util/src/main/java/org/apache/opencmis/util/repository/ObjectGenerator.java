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
package org.apache.opencmis.util.repository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.impl.dataobjects.ContentStreamDataImpl;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectService;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;

/**
 * A simple helper class for the tests that generates a sample folder hierarchy and
 * optionally documents in it.
 * 
 * @author Jens
 *
 */
public class ObjectGenerator {
  
  private static Log log = LogFactory.getLog(ObjectGenerator.class);
  private ProviderObjectFactory fFactory;
  NavigationService fNavSvc;
  ObjectService fObjSvc;
  private String fRepositoryId;
  /**
   * Indicates if / how many documents are created in each folder
   */
  private int fNoDocumentsToCreate;
  
  /**
   * The type id of the document id that is created. 
   */
  private String fDocTypeId = BaseObjectTypeIds.CMIS_DOCUMENT.value();
  
  /**
   * The type id of the folder that is created.
   */
  private String fFolderTypeId = BaseObjectTypeIds.CMIS_FOLDER.value();
  
  /**
   * A list of property ids. For each element in this list a String property value
   * is created for each creation of a document. All ids must be valid string 
   * property id of the type fDocTypeId
   */
  private List<String> fStringPropertyIdsToSetForDocument;
  
  /**
   * A list of property ids. For each element in this list a String property value
   * is created for each creation of a folder. All ids must be valid string 
   * property id of the type fFolderTypeId
   */
  private List<String> fStringPropertyIdsToSetForFolder;
  
  /**
   * number of objects created in total
   */
  private int fObjectsInTotalCount = 0;
  
  /**
   * size of content in KB, if 0 create documents without content 
   */
  private int fContentSizeInK = 0;
  

  private static final String NAMEPROPVALPREFIXDOC = "My_Document-";
  private static final String NAMEPROPVALPREFIXFOLDER = "My_Folder-";
  private static final String STRINGPROPVALPREFIXDOC = "My Doc StringProperty ";
  private static final String STRINGPROPVALPREFIXFOLDER = "My Folder StringProperty ";
  private static int PROPVALCOUNTER_DOC_STRING_PROP = 0;
  private static int PROPVALCOUNTER_FOLDER_STRING_PROP = 0;
  /**
   * use UUIDs to generate folder and document names 
   */
  private boolean fUseUuids ;
  
  public ObjectGenerator(ProviderObjectFactory factory, NavigationService navSvc,
      ObjectService objSvc, String repositoryId) {
    super();
    fFactory = factory;
    fNavSvc = navSvc;
    fObjSvc = objSvc;
    fRepositoryId = repositoryId;
    // create an empty list of properties to generate by default for folder and document
    fStringPropertyIdsToSetForDocument = new ArrayList<String>();
    fStringPropertyIdsToSetForFolder = new ArrayList<String>();
    fNoDocumentsToCreate = 0;
    fUseUuids = false;
  }
  
  public void setNumberOfDocumentsToCreatePerFolder(int noDocumentsToCreate) {
    fNoDocumentsToCreate = noDocumentsToCreate;
  }
  
  public void setFolderTypeId(String folderTypeId) {
    fFolderTypeId = folderTypeId;
  }
  
  public void setDocumentTypeId(String docTypeId) {
    fDocTypeId = docTypeId;
  }

  public void setDocumentPropertiesToGenerate(List<String> propertyIds) {
    fStringPropertyIdsToSetForDocument = propertyIds;
  }

  public void setFolderPropertiesToGenerate(List<String> propertyIds) {
    fStringPropertyIdsToSetForFolder = propertyIds;
  }
  
  public void setContentSizeInKB(int sizeInK) {
    fContentSizeInK = sizeInK;
  }
  
  public void createFolderHierachy(int levels, int childrenPerLevel, String rootFolderId) {
    createFolderHierachy(rootFolderId, 0, levels, childrenPerLevel);
  }
  
  public void setUseUuidsForNames(boolean useUuids) {
    /**
     * use UUIDs to generate folder and document names 
     */
    fUseUuids = useUuids;
  }
  
  /**
   * retrieve the index-th folder from given level of the hierarchy
   * starting at rootId
   * @param rootId
   * @param level
   * @param index
   * @return
   */
  public String getFolderId(String rootId, int level, int index) {
    String objectId = rootId;
    final String requiredProperties = PropertyIds.CMIS_OBJECT_ID + "," + PropertyIds.CMIS_OBJECT_TYPE_ID +
      "," + PropertyIds.CMIS_BASE_TYPE_ID;
    // Note: This works because first folders are created then documents
    for (int i=0; i<level; i++) {
      ObjectInFolderList result = fNavSvc.getChildren(fRepositoryId, objectId, requiredProperties, PropertyIds.CMIS_OBJECT_TYPE_ID, false,
          IncludeRelationships.NONE, null, true, BigInteger.valueOf(-1), BigInteger.valueOf(-1), null);
      List<ObjectInFolderData> children = result.getObjects();
      ObjectData child = children.get(index).getObject();
      objectId = (String) child.getProperties().getProperties().get(PropertyIds.CMIS_OBJECT_ID).getFirstValue();        
    }
    return objectId;    
  }
  
  /**
   * retrieve the index-th document from given folder
   * @param folderId
   *    folder to retrieve document from
   * @param index
   *    index of document to retrieve from this folder
   * @return
   */
  public String getDocumentId(String folderId, int index) {
    String docId = null;
    final String requiredProperties = PropertyIds.CMIS_OBJECT_ID + "," + PropertyIds.CMIS_OBJECT_TYPE_ID +
      "," + PropertyIds.CMIS_BASE_TYPE_ID;
    ObjectInFolderList result = fNavSvc.getChildren(fRepositoryId, folderId, requiredProperties,
        PropertyIds.CMIS_OBJECT_TYPE_ID, false, IncludeRelationships.NONE, null, true, BigInteger
            .valueOf(-1), BigInteger.valueOf(-1), null);
    List<ObjectInFolderData> children = result.getObjects();
    int numDocsFound = 0;
    for (int i=0; i<children.size(); i++) {
      ObjectData child = children.get(i).getObject();
      docId = (String) child.getProperties().getProperties().get(PropertyIds.CMIS_OBJECT_ID).getFirstValue();        
      if (child.getBaseTypeId().equals(BaseObjectTypeIds.CMIS_DOCUMENT)) {
        if (numDocsFound == index)
          return docId;
        else
          numDocsFound++;
      }
    }
    return docId;    
  }
  
  /**
   * return the total number of objects created
   * @return
   */
  public int getObjectsInTotal() {
    return fObjectsInTotalCount;
  }

  public void createSingleDocument(String folderId) {
	  createDocument(folderId, 0, 0);      
  }
	  
  private void createFolderHierachy(String parentId, int level, int levels, int childrenPerLevel) {
    
    if (level>=levels)
      return;
    log.debug(" create folder for parent id: " + parentId + ", in level " + level 
         + ", max levels " + levels);
    
    for (int i = 0; i < childrenPerLevel; i++) {
      PropertiesData props = createFolderProperties(i, level);      
      String id = fObjSvc.createFolder(fRepositoryId, props, parentId, null, null, null, null);
      if (id != null) {
        ++fObjectsInTotalCount;
        createFolderHierachy(id, level+1, levels, childrenPerLevel);
      }
    }
    for (int j=0; j<fNoDocumentsToCreate; j++) {
      createDocument(parentId, j, level);
    }
  }

  private String createDocument(String folderId, int no, int level) {
    ContentStreamData contentStream = null;
    VersioningState versioningState = VersioningState.NONE;
    List<String> policies = null;
    AccessControlList addACEs = null;
    AccessControlList removeACEs = null;
    ExtensionsData extension = null;

    // log.info("create document in folder " + folderId);
    PropertiesData props = createDocumentProperties(no, level);
    String id = null;
    if (fContentSizeInK > 0)
      contentStream = createContent();
    id = fObjSvc.createDocument(fRepositoryId, props, folderId, contentStream, versioningState,
        policies, addACEs, removeACEs, extension);
    if (null == id)
      throw new RuntimeException("createDocument failed.");
    ++fObjectsInTotalCount;
    return id;
  }
  
  private ContentStreamData createContent() {
    ContentStreamDataImpl content = new ContentStreamDataImpl();
    content.setFilename("data.txt");
    content.setMimeType("text/plain");
    int len = fContentSizeInK * 1024; // size of document in K
    byte[] b = {0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
                0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a,     
                0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
                0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x0c, 0x0a
                }; // 32 Bytes
    ByteArrayOutputStream ba = new ByteArrayOutputStream(len);
    try {
      for (int j=0; j<fContentSizeInK; j++) {
        // write 1K of data
        for (int i=0; i<32; i++)
          ba.write(b);
      }
    } catch (IOException e) {
        throw new RuntimeException("Failed to fill content stream with data", e) ;
    }
    content.setStream(new ByteArrayInputStream(ba.toByteArray()));
    return content;
  }

  private PropertiesData createFolderProperties(int no, int level) {
    List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, generateFolderNameValue(no, level)));
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID, fFolderTypeId));
    // Generate some property values for custom attributes
    for (String stringPropId : fStringPropertyIdsToSetForFolder) {
      properties.add(fFactory.createPropertyStringData(stringPropId, generateStringPropValueFolder()));      
    }
    PropertiesData props = fFactory.createPropertiesData(properties);
    return props;
  }

  private PropertiesData createDocumentProperties(int no, int level) {
    List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_NAME, generateDocNameValue(no, level)));
    properties.add(fFactory.createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID, fDocTypeId));
    // Generate some property values for custom attributes
    for (String stringPropId : fStringPropertyIdsToSetForDocument) {
      properties.add(fFactory.createPropertyStringData(stringPropId, generateStringPropValueDoc()));      
    }
    PropertiesData props = fFactory.createPropertiesData(properties);
    return props;
  }
  
  private static synchronized int incrementPropCounterDocStringProp() {
    return PROPVALCOUNTER_DOC_STRING_PROP++;
  }
  
  private static synchronized int incrementPropCounterFolderStringProp() {
    return PROPVALCOUNTER_FOLDER_STRING_PROP++;
  }

  private String generateDocNameValue(int no, int level) {
    if (fUseUuids)
      return UUID.randomUUID().toString();
    else
      return NAMEPROPVALPREFIXDOC + level + "-" + no;
  }
  
  private String generateFolderNameValue(int no, int level) {
    if (fUseUuids)
      return UUID.randomUUID().toString();
    else
      return NAMEPROPVALPREFIXFOLDER + level + "-" + no;
  }

  private static String generateStringPropValueDoc() {
    return STRINGPROPVALPREFIXDOC + incrementPropCounterDocStringProp();
  }
  
  private static String generateStringPropValueFolder() {
    return STRINGPROPVALPREFIXFOLDER + incrementPropCounterFolderStringProp();
  }
  
  public void dumpFolder(String folderId, String propertyFilter) {
    log.info("starting dumpFolder() id " + folderId + " ...");
    boolean allRequiredPropertiesArePresent =  propertyFilter!= null && propertyFilter.equals("*"); // can be optimized
    final String requiredProperties = allRequiredPropertiesArePresent ? propertyFilter :
       PropertyIds.CMIS_OBJECT_ID+","+PropertyIds.CMIS_NAME + ","+PropertyIds.CMIS_OBJECT_TYPE_ID +
       ","+PropertyIds.CMIS_BASE_TYPE_ID;
    // if all required properties are contained in the filter use we use the filter otherwise
    // we use our own set and get those from the filter later in an extra call
    String propertyFilterIntern = allRequiredPropertiesArePresent ? propertyFilter : requiredProperties;
    dumpFolder(folderId, propertyFilterIntern, 0);
  }

  private void dumpFolder(String folderId, String propertyFilter, int depth) {
    boolean allRequiredPropertiesArePresent = propertyFilter.equals("*"); // can be optimized
    StringBuilder prefix = new StringBuilder();
    for (int i=0; i<depth; i++)
      prefix.append("   ");
    
    ObjectInFolderList result = fNavSvc.getChildren(fRepositoryId, folderId, propertyFilter, null, false,
        IncludeRelationships.NONE, null, true, BigInteger.valueOf(-1), BigInteger.valueOf(-1), null);
    List<ObjectInFolderData> folders = result.getObjects();
    if (null != folders) {
      log.info(prefix + "found " + folders.size() + " children in folder " + folderId);
      int no=0;
      for (ObjectInFolderData folder : folders) {
        log.info(prefix.toString() + ++no + ": found object with id: " + folder.getObject().getId() + " and path segment: "
            + folder.getPathSegment());
        dumpObjectProperties(folder.getObject(), depth, propertyFilter, !allRequiredPropertiesArePresent);        
        String objectTypeBaseId = folder.getObject().getBaseTypeId().value();
        if (objectTypeBaseId.equals(BaseObjectTypeIds.CMIS_FOLDER.value())) {
          dumpFolder(folder.getObject().getId(), propertyFilter, depth+1);
        }
        else if (objectTypeBaseId.equals(BaseObjectTypeIds.CMIS_DOCUMENT.value())) {
          dumpObjectProperties(folder.getObject(), depth+1, propertyFilter, !allRequiredPropertiesArePresent);        
        }
      }
    }
    log.info(""); // add empty line
  }

  private void dumpObjectProperties(ObjectData object, int depth, String propertyFilter, boolean mustFetchProperties) {
    final SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    StringBuilder prefix = new StringBuilder();
    for (int i=0; i<depth; i++)
      prefix.append("   ");

    log.info(prefix + "found object id " + object.getId());
    Map<String, PropertyData<?>> propMap;
    if (mustFetchProperties) {
      String objId = (String) object.getProperties().getProperties().get(PropertyIds.CMIS_OBJECT_ID).getFirstValue();
      PropertiesData props = fObjSvc.getProperties(fRepositoryId, objId, propertyFilter, null);
      propMap = props.getProperties();
    } else {
      propMap = object.getProperties().getProperties();
    }
    StringBuilder valueStr = new StringBuilder("[");
    for (Map.Entry <String, PropertyData<?>> entry : propMap.entrySet()) {
        if (entry.getValue().getValues().size() > 1) {
          if (entry.getValue().getFirstValue() instanceof GregorianCalendar) {
            for ( Object obj : entry.getValue().getValues()) {
              GregorianCalendar cal = (GregorianCalendar) obj;
              valueStr.append(df.format(cal.getTime()) + ", ");              
            }
            valueStr.append("]");
          } else {
            valueStr = new StringBuilder(entry.getValue().getValues().toString());
          }
        } else {
          Object value = entry.getValue().getFirstValue();
          if (null != value) {
            valueStr = new StringBuilder(value.toString());
            if (value instanceof GregorianCalendar) {
              valueStr = new StringBuilder(df.format(((GregorianCalendar) entry.getValue().getFirstValue()).getTime()));   
            }
          }
        }
        log.info(prefix + entry.getKey() + ": " + valueStr);          
    }
    log.info(""); // add empty line
  }

}
