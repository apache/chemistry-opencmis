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
package org.apache.opencmis.client.provider.framework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.DocumentTypeDefinition;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.CapabilityAcl;
import org.apache.opencmis.commons.enums.CapabilityChanges;
import org.apache.opencmis.commons.enums.CapabilityQuery;
import org.apache.opencmis.commons.enums.CapabilityRendition;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.CmisProvider;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectParentData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyDateTimeData;
import org.apache.opencmis.commons.provider.PropertyIdData;
import org.apache.opencmis.commons.provider.PropertyStringData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.commons.provider.RenditionData;
import org.apache.opencmis.commons.provider.RepositoryInfoData;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractCmisTestCase extends TestCase {

  public static final String DEFAULT_TESTS_ENABLED = "false";
  public static final String DEFAULT_USERNAME = "test";
  public static final String DEFAULT_PASSWORD = "test";
  public static final String DEFAULT_ATOMPUB_URL = "http://localhost:8080/cmis/atom";
  public static final String DEFAULT_WEBSERVICES_URLPREFIX = "http://localhost:8080/cmis/services/";
  public static final String DEFAULT_DOCTYPE = "cmis:document";
  public static final String DEFAULT_FOLDERTYPE = "cmis:folder";

  public static final String PROP_TESTS_ENABLED = "opencmis.test";
  public static final String PROP_USERNAME = "opencmis.test.username";
  public static final String PROP_PASSWORD = "opencmis.test.password";
  public static final String PROP_REPOSITORY = "opencmis.test.repository";
  public static final String PROP_TESTFOLDER = "opencmis.test.testfolder";
  public static final String PROP_DOCTYPE = "opencmis.test.documenttype";
  public static final String PROP_FOLDERTYPE = "opencmis.test.foldertype";

  public static final String PROP_ATOMPUB_URL = "opencmis.test.atompub.url";
  public static final String PROP_WEBSERVICES_URLPREFIX = "opencmis.test.webservices.url";

  private CmisProvider fProvider;
  private String fTestRepositoryId;
  private String fTestFolderId;

  private static Log log = LogFactory.getLog(AbstractCmisTestCase.class);

  /**
   * Returns the provider object or creates one if does not exist.
   */
  protected CmisProvider getProvider() {
    if (fProvider == null) {
      log.info("Creating provider...");
      fProvider = createProvider();
    }

    return fProvider;
  }

  /**
   * Creates a provider object.
   */
  protected abstract CmisProvider createProvider();

  /**
   * Returns a set of test names that enabled.
   */
  protected abstract Set<String> getEnabledTests();

  /**
   * Returns the test repository id.
   */
  protected String getTestRepositoryId() {
    if (fTestRepositoryId != null) {
      return fTestRepositoryId;
    }

    fTestRepositoryId = System.getProperty(PROP_REPOSITORY);
    if (fTestRepositoryId != null) {
      log.info("Test repository: " + fTestRepositoryId);
      return fTestRepositoryId;
    }

    fTestRepositoryId = getFirstRepositoryId();
    log.info("Test repository: " + fTestRepositoryId);

    return fTestRepositoryId;
  }

  /**
   * Returns the test root folder id.
   */
  protected String getTestRootFolder() {
    if (fTestFolderId != null) {
      return fTestFolderId;
    }

    fTestFolderId = System.getProperty(PROP_TESTFOLDER);
    if (fTestFolderId != null) {
      log.info("Test root folder: " + fTestFolderId);
      return fTestFolderId;
    }

    fTestFolderId = getRootFolderId();
    log.info("Test root folder: " + fTestFolderId);

    return fTestFolderId;
  }

  /**
   * Returns if the test is enabled.
   */
  protected boolean isEnabled(String name) {
    boolean testsEnabled = Boolean.parseBoolean(System.getProperty(PROP_TESTS_ENABLED,
        DEFAULT_TESTS_ENABLED));

    if (testsEnabled && getEnabledTests().contains(name)) {
      return true;
    }

    log.info("Skipping test '" + name + "'!");

    return false;
  }

  /**
   * Returns the test username.
   */
  protected String getUsername() {
    return System.getProperty(PROP_USERNAME, DEFAULT_USERNAME);
  }

  /**
   * Returns the test password.
   */
  protected String getPassword() {
    return System.getProperty(PROP_PASSWORD, DEFAULT_PASSWORD);
  }

  /**
   * Returns the default document type.
   */
  protected String getDefaultDocumentType() {
    return System.getProperty(PROP_DOCTYPE, DEFAULT_DOCTYPE);
  }

  /**
   * Returns the default folder type.
   */
  protected String getDefaultFolderType() {
    return System.getProperty(PROP_FOLDERTYPE, DEFAULT_FOLDERTYPE);
  }

  /**
   * Returns the AtomPub URL.
   */
  protected String getAtomPubURL() {
    return System.getProperty(PROP_ATOMPUB_URL, DEFAULT_ATOMPUB_URL);
  }

  /**
   * Returns the Web Services URL prefix.
   */
  protected String getWebServicesURL() {
    return System.getProperty(PROP_WEBSERVICES_URLPREFIX, DEFAULT_WEBSERVICES_URLPREFIX);
  }

  /**
   * Returns the object factory.
   */
  protected ProviderObjectFactory getObjectFactory() {
    return getProvider().getObjectFactory();
  }

  /**
   * Returns the id of the first repository.
   */
  protected String getFirstRepositoryId() {
    List<RepositoryInfoData> repositories = getProvider().getRepositoryService()
        .getRepositoryInfos(null);

    assertNotNull(repositories);
    assertFalse(repositories.isEmpty());
    assertNotNull(repositories.get(0).getRepositoryId());

    return repositories.get(0).getRepositoryId();
  }

  /**
   * Returns the info object of the test repository.
   */
  protected RepositoryInfoData getRepositoryInfo() {
    RepositoryInfoData repositoryInfo = getProvider().getRepositoryService().getRepositoryInfo(
        getTestRepositoryId(), null);

    assertNotNull(repositoryInfo);
    assertNotNull(repositoryInfo.getRepositoryId());
    assertEquals(getTestRepositoryId(), repositoryInfo.getRepositoryId());

    return repositoryInfo;
  }

  /**
   * Returns the root folder of the test repository.
   */
  protected String getRootFolderId() {
    RepositoryInfoData repository = getRepositoryInfo();

    assertNotNull(repository.getRootFolderId());

    return repository.getRootFolderId();
  }

  /**
   * Returns if the test repository supports reading ACLs.
   */
  protected boolean supportsDiscoverACLs() {
    RepositoryInfoData repository = getRepositoryInfo();

    assertNotNull(repository.getRepositoryCapabilities());

    return (repository.getRepositoryCapabilities().getCapabilityAcl() == CapabilityAcl.DISCOVER);
  }

  /**
   * Returns if the test repository supports setting ACLs.
   */
  protected boolean supportsManageACLs() {
    RepositoryInfoData repository = getRepositoryInfo();

    assertNotNull(repository.getRepositoryCapabilities());

    return (repository.getRepositoryCapabilities().getCapabilityAcl() == CapabilityAcl.MANAGE);
  }

  /**
   * Returns if the test repository supports renditions.
   */
  protected boolean supportsRenditions() {
    RepositoryInfoData repository = getRepositoryInfo();

    assertNotNull(repository.getRepositoryCapabilities());

    if (repository.getRepositoryCapabilities().getCapabilityRenditions() == null) {
      return false;
    }

    return (repository.getRepositoryCapabilities().getCapabilityRenditions() != CapabilityRendition.NONE);
  }

  /**
   * Returns if the test repository supports descendants.
   */
  protected boolean supportsDescendants() {
    RepositoryInfoData repository = getRepositoryInfo();

    assertNotNull(repository.getRepositoryCapabilities());

    if (repository.getRepositoryCapabilities().supportsGetDescendants() == null) {
      return false;
    }

    return repository.getRepositoryCapabilities().supportsGetDescendants().booleanValue();
  }

  /**
   * Returns if the test repository supports descendants.
   */
  protected boolean supportsFolderTree() {
    RepositoryInfoData repository = getRepositoryInfo();

    assertNotNull(repository.getRepositoryCapabilities());

    if (repository.getRepositoryCapabilities().supportsGetFolderTree() == null) {
      return false;
    }

    return repository.getRepositoryCapabilities().supportsGetFolderTree().booleanValue();
  }

  /**
   * Returns if the test repository supports content changes.
   */
  protected boolean supportsContentChanges() {
    RepositoryInfoData repository = getRepositoryInfo();

    assertNotNull(repository.getRepositoryCapabilities());

    if (repository.getRepositoryCapabilities().getCapabilityChanges() == null) {
      return false;
    }

    return (repository.getRepositoryCapabilities().getCapabilityChanges() != CapabilityChanges.NONE);
  }

  /**
   * Returns if the test repository supports query.
   */
  protected boolean supportsQuery() {
    RepositoryInfoData repository = getRepositoryInfo();

    assertNotNull(repository.getRepositoryCapabilities());

    if (repository.getRepositoryCapabilities().getCapabilityQuery() == null) {
      return false;
    }

    return (repository.getRepositoryCapabilities().getCapabilityQuery() != CapabilityQuery.NONE);
  }

  /**
   * Returns if the test repository supports relationships.
   */
  protected boolean supportsRelationships() {
    TypeDefinition relType = null;

    try {
      relType = getProvider().getRepositoryService().getTypeDefinition(getTestRepositoryId(),
          "cmis:relationship", null);
    }
    catch (CmisObjectNotFoundException e) {
      return false;
    }

    return (relType != null);
  }

  /**
   * Returns if the test repository supports policies.
   */
  protected boolean supportsPolicies() {
    TypeDefinition relType = null;

    try {
      relType = getProvider().getRepositoryService().getTypeDefinition(getTestRepositoryId(),
          "cmis:policy", null);
    }
    catch (CmisObjectNotFoundException e) {
      return false;
    }

    return (relType != null);
  }

  // ---- helpers ----

  /**
   * Prints a warning.
   */
  protected void warning(String message) {
    System.out.println("*** " + message);
  }

  /**
   * Creates a ContentStreamData object from a byte array.
   */
  protected ContentStreamData createContentStreamData(String mimeType, byte[] content) {
    assertNotNull(content);

    return getObjectFactory().createContentStream(BigInteger.valueOf(content.length), mimeType,
        "test", new ByteArrayInputStream(content));
  }

  /**
   * Extracts the path from a folder object.
   */
  protected String getPath(ObjectData folderObject) {
    assertNotNull(folderObject);
    assertNotNull(folderObject.getProperties());
    assertNotNull(folderObject.getProperties().getProperties());
    assertTrue(folderObject.getProperties().getProperties().get(PropertyIds.CMIS_PATH) instanceof PropertyStringData);

    PropertyStringData pathProperty = (PropertyStringData) folderObject.getProperties()
        .getProperties().get(PropertyIds.CMIS_PATH);

    assertNotNull(pathProperty.getValues());
    assertEquals(1, pathProperty.getValues().size());
    assertNotNull(pathProperty.getValues().get(0));

    return pathProperty.getValues().get(0);
  }

  // ---- short cuts ----

  /**
   * Retrieves an object.
   */
  protected ObjectData getObject(String objectId, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
      Boolean includeACL, ExtensionsData extension) {
    ObjectData object = getProvider().getObjectService().getObject(getTestRepositoryId(), objectId,
        filter, includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds,
        includeACL, extension);

    assertNotNull(object);

    return object;
  }

  /**
   * Retrieves a full blown object.
   */
  protected ObjectData getObject(String objectId) {
    ObjectData object = getObject(objectId, "*", Boolean.TRUE, IncludeRelationships.BOTH, null,
        Boolean.TRUE, Boolean.TRUE, null);

    assertBasicProperties(object.getProperties());

    return object;
  }

  /**
   * Retrieves an object by path.
   */
  protected ObjectData getObjectByPath(String path, String filter, Boolean includeAllowableActions,
      IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
      Boolean includeACL, ExtensionsData extension) {
    ObjectData object = getProvider().getObjectService().getObjectByPath(getTestRepositoryId(),
        path, filter, includeAllowableActions, includeRelationships, renditionFilter,
        includePolicyIds, includeACL, extension);

    assertNotNull(object);

    return object;
  }

  /**
   * Retrieves a full blown object by path.
   */
  protected ObjectData getObjectByPath(String path) {
    ObjectData object = getObjectByPath(path, "*", Boolean.TRUE, IncludeRelationships.BOTH, null,
        Boolean.TRUE, Boolean.TRUE, null);

    assertBasicProperties(object.getProperties());

    return object;
  }

  /**
   * Returns <code>true</code> if the object with the given id exists, <code>false</code> otherwise.
   */
  protected boolean existsObject(String objectId) {
    try {
      ObjectData object = getObject(objectId, PropertyIds.CMIS_OBJECT_ID, Boolean.FALSE,
          IncludeRelationships.NONE, null, Boolean.FALSE, Boolean.FALSE, null);

      assertNotNull(object);
      assertNotNull(object.getId());
    }
    catch (CmisObjectNotFoundException e) {
      return false;
    }

    return true;
  }

  /**
   * Returns the child of a folder.
   */
  protected ObjectInFolderData getChild(String folderId, String objectId) {
    boolean hasMore = true;

    while (hasMore) {
      ObjectInFolderList children = getProvider().getNavigationService().getChildren(
          getTestRepositoryId(), folderId, "*", null, Boolean.TRUE, IncludeRelationships.BOTH,
          null, Boolean.TRUE, null, null, null);

      assertNotNull(children);
      assertTrue(children.getObjects().size() > 0);

      hasMore = (children.hasMoreItems() == null ? false : children.hasMoreItems().booleanValue());

      for (ObjectInFolderData object : children.getObjects()) {
        assertNotNull(object);
        assertNotNull(object.getPathSegment());
        assertNotNull(object.getObject());
        assertNotNull(object.getObject().getId());

        assertBasicProperties(object.getObject().getProperties());

        if (object.getObject().getId().equals(objectId)) {
          return object;
        }
      }
    }

    fail("Child not found!");

    return null;
  }

  /**
   * Gets the version series id of an object.
   */
  protected String getVersionSeriesId(ObjectData object) {
    PropertyData<?> versionSeriesId = object.getProperties().getProperties().get(
        PropertyIds.CMIS_VERSION_SERIES_ID);
    assertNotNull(versionSeriesId);
    assertTrue(versionSeriesId instanceof PropertyIdData);

    return ((PropertyIdData) versionSeriesId).getFirstValue();
  }

  /**
   * Gets the version series id of an object.
   */
  protected String getVersionSeriesId(String docId) {
    return getVersionSeriesId(getObject(docId));
  }

  /**
   * Creates a folder.
   */
  protected String createFolder(PropertiesData properties, String folderId, List<String> policies,
      AccessControlList addACEs, AccessControlList removeACEs) {
    String objectId = getProvider().getObjectService().createFolder(getTestRepositoryId(),
        properties, folderId, policies, addACEs, removeACEs, null);
    assertNotNull(objectId);
    assertTrue(existsObject(objectId));

    ObjectInFolderData folderChild = getChild(folderId, objectId);

    // check name
    PropertyData<?> nameProp = properties.getProperties().get(PropertyIds.CMIS_NAME);
    if (nameProp != null) {
      assertPropertyValue(folderChild.getObject().getProperties(), PropertyIds.CMIS_NAME,
          PropertyStringData.class, nameProp.getFirstValue());
    }

    // check object type
    PropertyData<?> typeProp = properties.getProperties().get(PropertyIds.CMIS_OBJECT_TYPE_ID);
    assertNotNull(typeProp);
    assertPropertyValue(folderChild.getObject().getProperties(), PropertyIds.CMIS_OBJECT_TYPE_ID,
        PropertyIdData.class, typeProp.getFirstValue());

    // check parent
    ObjectData parent = getProvider().getNavigationService().getFolderParent(getTestRepositoryId(),
        objectId, null, null);
    assertNotNull(parent);
    assertNotNull(parent.getProperties());
    assertNotNull(parent.getProperties().getProperties());
    assertNotNull(parent.getProperties().getProperties().get(PropertyIds.CMIS_OBJECT_ID));
    assertEquals(folderId, parent.getProperties().getProperties().get(PropertyIds.CMIS_OBJECT_ID)
        .getFirstValue());

    return objectId;
  }

  /**
   * Creates a folder with the default type.
   */
  protected String createDefaultFolder(String folderId, String name) {
    List<PropertyData<?>> propList = new ArrayList<PropertyData<?>>();
    propList.add(getObjectFactory().createPropertyStringData(PropertyIds.CMIS_NAME, name));
    propList.add(getObjectFactory().createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID,
        getDefaultFolderType()));

    PropertiesData properties = getObjectFactory().createPropertiesData(propList);

    return createFolder(properties, folderId, null, null, null);
  }

  /**
   * Creates a document.
   */
  protected String createDocument(PropertiesData properties, String folderId,
      ContentStreamData contentStream, VersioningState versioningState, List<String> policies,
      AccessControlList addACEs, AccessControlList removeACEs) {
    String objectId = getProvider().getObjectService().createDocument(getTestRepositoryId(),
        properties, folderId, contentStream, versioningState, policies, addACEs, removeACEs, null);
    assertNotNull(objectId);
    assertTrue(existsObject(objectId));

    if (folderId != null) {
      ObjectInFolderData folderChild = getChild(folderId, objectId);

      // check name
      PropertyData<?> nameProp = properties.getProperties().get(PropertyIds.CMIS_NAME);
      if (nameProp != null) {
        assertPropertyValue(folderChild.getObject().getProperties(), PropertyIds.CMIS_NAME,
            PropertyStringData.class, nameProp.getFirstValue());
      }

      // check object type
      PropertyData<?> typeProp = properties.getProperties().get(PropertyIds.CMIS_OBJECT_TYPE_ID);
      assertNotNull(typeProp);
      assertPropertyValue(folderChild.getObject().getProperties(), PropertyIds.CMIS_OBJECT_TYPE_ID,
          PropertyIdData.class, typeProp.getFirstValue());

      // check parent
      List<ObjectParentData> parents = getProvider().getNavigationService().getObjectParents(
          getTestRepositoryId(), objectId, "*", Boolean.TRUE, IncludeRelationships.BOTH, null,
          Boolean.TRUE, null);
      assertNotNull(parents);
      assertEquals(1, parents.size());

      ObjectParentData parent = parents.get(0);
      assertNotNull(parent);
      assertNotNull(parent.getRelativePathSegment());
      assertNotNull(parent.getObject());
      assertNotNull(parent.getObject().getProperties().getProperties());
      assertNotNull(parent.getObject().getProperties().getProperties().get(
          PropertyIds.CMIS_OBJECT_ID));
      assertEquals(folderId, parent.getObject().getProperties().getProperties().get(
          PropertyIds.CMIS_OBJECT_ID).getFirstValue());

      // get document by path (check relative path segment)
      assertNotNull(parent.getObject().getProperties().getProperties().get(PropertyIds.CMIS_PATH));
      String parentPath = parent.getObject().getProperties().getProperties().get(
          PropertyIds.CMIS_PATH).getFirstValue().toString();

      ObjectData docByPath = getObjectByPath((parentPath.equals("/") ? "" : parentPath) + "/"
          + parent.getRelativePathSegment());

      PropertyData<?> idProp = docByPath.getProperties().getProperties().get(
          PropertyIds.CMIS_OBJECT_ID);
      assertNotNull(idProp);
      assertEquals(objectId, idProp.getFirstValue());
    }
    else {
      List<ObjectParentData> parents = getProvider().getNavigationService().getObjectParents(
          getTestRepositoryId(), objectId, null, Boolean.TRUE, IncludeRelationships.BOTH, null,
          Boolean.TRUE, null);
      assertNotNull(parents);
      assertEquals(0, parents.size());
    }

    return objectId;
  }

  /**
   * Creates a document with the default type.
   */
  protected String createDefaultDocument(String folderId, String name, String contentType,
      byte[] content) {
    VersioningState vs = (isVersionable(getDefaultDocumentType()) ? VersioningState.MAJOR
        : VersioningState.NONE);

    List<PropertyData<?>> propList = new ArrayList<PropertyData<?>>();
    propList.add(getObjectFactory().createPropertyStringData(PropertyIds.CMIS_NAME, name));
    propList.add(getObjectFactory().createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID,
        getDefaultDocumentType()));

    PropertiesData properties = getObjectFactory().createPropertiesData(propList);

    ContentStreamData contentStream = createContentStreamData(contentType, content);

    return createDocument(properties, folderId, contentStream, vs, null, null, null);
  }

  /**
   * Creates a document from source.
   */
  protected String createDocumentFromSource(String sourceId, PropertiesData properties,
      String folderId, VersioningState versioningState, List<String> policies,
      AccessControlList addACEs, AccessControlList removeACEs) {
    String objectId = getProvider().getObjectService().createDocumentFromSource(
        getTestRepositoryId(), sourceId, properties, folderId, versioningState, policies, addACEs,
        removeACEs, null);
    assertNotNull(objectId);
    assertTrue(existsObject(objectId));

    if (folderId != null) {
      ObjectInFolderData folderChild = getChild(folderId, objectId);

      // check name
      PropertyData<?> nameProp = properties.getProperties().get(PropertyIds.CMIS_NAME);
      if (nameProp != null) {
        assertPropertyValue(folderChild.getObject().getProperties(), PropertyIds.CMIS_NAME,
            PropertyStringData.class, nameProp.getValues().get(0));
      }

      // check parent
      List<ObjectParentData> parents = getProvider().getNavigationService().getObjectParents(
          getTestRepositoryId(), objectId, null, Boolean.TRUE, IncludeRelationships.BOTH, null,
          Boolean.TRUE, null);
      assertNotNull(parents);
      assertEquals(1, parents.size());

      ObjectParentData parent = parents.get(0);
      assertNotNull(parent);
      assertNotNull(parent.getRelativePathSegment());
      assertNotNull(parent.getObject());
      assertNotNull(parent.getObject().getProperties().getProperties());
      assertNotNull(parent.getObject().getProperties().getProperties().get(
          PropertyIds.CMIS_OBJECT_ID));
      assertEquals(folderId, parent.getObject().getProperties().getProperties().get(
          PropertyIds.CMIS_OBJECT_ID).getFirstValue());
    }

    return objectId;
  }

  /**
   * Deletes an object.
   */
  protected void delete(String objectId, boolean allVersions) {
    getProvider().getObjectService().deleteObject(getTestRepositoryId(), objectId, allVersions,
        null);
    assertFalse(existsObject(objectId));
  }

  /**
   * Deletes a tree.
   */
  protected void deleteTree(String folderId) {
    getProvider().getObjectService().deleteTree(getTestRepositoryId(), folderId, Boolean.TRUE,
        UnfileObjects.DELETE, Boolean.TRUE, null);
    assertFalse(existsObject(folderId));
  }

  /**
   * Gets a content stream.
   */
  protected ContentStreamData getContent(String objectId, String streamId) {
    ContentStreamData contentStream = getProvider().getObjectService().getContentStream(
        getTestRepositoryId(), objectId, streamId, null, null, null);
    assertNotNull(contentStream);
    assertNotNull(contentStream.getMimeType());
    assertNotNull(contentStream.getStream());

    return contentStream;
  }

  /**
   * Reads the content from a content stream into a byte array.
   */
  protected byte[] readContent(ContentStreamData contentStream) throws Exception {
    assertNotNull(contentStream);
    assertNotNull(contentStream.getStream());

    InputStream stream = contentStream.getStream();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();

    byte[] buffer = new byte[4096];
    int b;
    while ((b = stream.read(buffer)) > -1) {
      baos.write(buffer, 0, b);
    }

    return baos.toByteArray();
  }

  /**
   * Returns a type definition.
   */
  protected TypeDefinition getTypeDefinition(String typeName) {
    TypeDefinition typeDef = getProvider().getRepositoryService().getTypeDefinition(
        getTestRepositoryId(), typeName, null);

    assertNotNull(typeDef);
    assertNotNull(typeDef.getId());

    return typeDef;
  }

  /**
   * Returns if the type is versionable.
   */
  protected boolean isVersionable(String typeName) {
    TypeDefinition type = getTypeDefinition(typeName);

    assertTrue(type instanceof DocumentTypeDefinition);

    Boolean isVersionable = ((DocumentTypeDefinition) type).isVersionable();
    assertNotNull(isVersionable);

    return isVersionable.booleanValue();
  }

  // ---- asserts ----

  protected void assertEquals(TypeDefinition expected, TypeDefinition actual,
      boolean checkPropertyDefintions) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("Type definition is null!");
    }

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getLocalName(), actual.getLocalName());
    assertEquals(expected.getLocalNamespace(), actual.getLocalNamespace());
    assertEquals(expected.getDisplayName(), actual.getDisplayName());
    assertEquals(expected.getDescription(), actual.getDescription());
    assertEquals(expected.getQueryName(), actual.getQueryName());
    assertEquals(expected.getParentId(), actual.getParentId());
    assertEquals(expected.getBaseId(), actual.getBaseId());

    if (!checkPropertyDefintions) {
      return;
    }

    if ((expected.getPropertyDefinitions() == null) && (actual.getPropertyDefinitions() == null)) {
      return;
    }

    if ((expected.getPropertyDefinitions() == null) || (actual.getPropertyDefinitions() == null)) {
      fail("Property definition list is null!");
    }

    assertEquals(expected.getPropertyDefinitions().size(), actual.getPropertyDefinitions().size());

    for (PropertyDefinition<?> expectedPropDef : expected.getPropertyDefinitions().values()) {
      PropertyDefinition<?> actualPropDef = actual.getPropertyDefinitions().get(
          expectedPropDef.getId());

      assertEquals(expectedPropDef, actualPropDef);
    }
  }

  protected void assertEquals(PropertyDefinition<?> expected, PropertyDefinition<?> actual) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("Property definition is null!");
    }

    assertNotNull(expected.getId());
    assertNotNull(actual.getId());

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getLocalName(), actual.getLocalName());
    assertEquals(expected.getLocalNamespace(), actual.getLocalNamespace());
    assertEquals(expected.getQueryName(), actual.getQueryName());
    assertEquals(expected.getDisplayName(), actual.getDisplayName());
    assertEquals(expected.getDescription(), actual.getDescription());
    assertEquals(expected.getPropertyType(), actual.getPropertyType());
    assertEquals(expected.getCardinality(), actual.getCardinality());
    assertEquals(expected.getUpdatability(), actual.getUpdatability());
  }

  protected void assertEquals(PropertiesData expected, PropertiesData actual) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("Properties data is null!");
    }

    if ((expected.getProperties() == null) && (actual.getProperties() == null)) {
      return;
    }

    if ((expected.getProperties() == null) || (actual.getProperties() == null)) {
      fail("Properties are null!");
    }

    assertEquals(expected.getProperties().size(), actual.getProperties().size());

    for (String id : expected.getProperties().keySet()) {
      PropertyData<?> expectedProperty = expected.getProperties().get(id);
      assertNotNull(expectedProperty);
      assertEquals(id, expectedProperty.getId());

      PropertyData<?> actualProperty = actual.getProperties().get(id);
      assertNotNull(actualProperty);
      assertEquals(id, actualProperty.getId());

      assertEquals(expectedProperty, actualProperty);
    }
  }

  protected void assertEquals(PropertyData<?> expected, PropertyData<?> actual) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("Properties data is null!");
    }

    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getDisplayName(), actual.getDisplayName());
    assertEquals(expected.getLocalName(), actual.getLocalName());
    assertEquals(expected.getQueryName(), actual.getQueryName());

    assertEquals(expected.getValues().size(), actual.getValues().size());

    for (int i = 0; i < expected.getValues().size(); i++) {
      assertEquals(expected.getValues().get(i), actual.getValues().get(i));
    }
  }

  protected void assertBasicProperties(PropertiesData properties) {
    assertNotNull(properties);
    assertNotNull(properties.getProperties());

    assertProperty(properties.getProperties().get(PropertyIds.CMIS_OBJECT_ID),
        PropertyIds.CMIS_OBJECT_ID, PropertyIdData.class);
    assertProperty(properties.getProperties().get(PropertyIds.CMIS_OBJECT_TYPE_ID),
        PropertyIds.CMIS_OBJECT_TYPE_ID, PropertyIdData.class);
    assertProperty(properties.getProperties().get(PropertyIds.CMIS_BASE_TYPE_ID),
        PropertyIds.CMIS_BASE_TYPE_ID, PropertyIdData.class);
    assertProperty(properties.getProperties().get(PropertyIds.CMIS_NAME), PropertyIds.CMIS_NAME,
        PropertyStringData.class);
    assertProperty(properties.getProperties().get(PropertyIds.CMIS_CREATED_BY),
        PropertyIds.CMIS_CREATED_BY, PropertyStringData.class);
    assertProperty(properties.getProperties().get(PropertyIds.CMIS_CREATION_DATE),
        PropertyIds.CMIS_CREATION_DATE, PropertyDateTimeData.class);
    assertProperty(properties.getProperties().get(PropertyIds.CMIS_LAST_MODIFIED_BY),
        PropertyIds.CMIS_LAST_MODIFIED_BY, PropertyStringData.class);
    assertProperty(properties.getProperties().get(PropertyIds.CMIS_LAST_MODIFICATION_DATE),
        PropertyIds.CMIS_LAST_MODIFICATION_DATE, PropertyDateTimeData.class);
  }

  protected void assertProperty(PropertyData<?> property, String id, Class<?> clazz) {
    assertNotNull(property);
    assertNotNull(property.getId());
    assertEquals(id, property.getId());
    assertTrue(clazz.isAssignableFrom(property.getClass()));
    assertNotNull(property.getValues());
    assertFalse(property.getValues().isEmpty());
  }

  protected void assertPropertyValue(PropertyData<?> property, String id, Class<?> clazz,
      Object... values) {
    assertProperty(property, id, clazz);

    assertEquals(values.length, property.getValues().size());

    int i = 0;
    for (Object value : property.getValues()) {
      assertEquals(values[i], value);
    }
  }

  protected void assertPropertyValue(PropertiesData properties, String id, Class<?> clazz,
      Object... values) {
    assertNotNull(properties);
    assertNotNull(properties.getProperties());

    PropertyData<?> property = properties.getProperties().get(id);

    assertProperty(property, id, clazz);

    int i = 0;
    for (Object value : property.getValues()) {
      assertEquals(values[i], value);
    }
  }

  protected void assertEquals(AllowableActionsData expected, AllowableActionsData actual) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("Allowable action data is null!");
    }

    assertNotNull(expected.getAllowableActions());
    assertNotNull(actual.getAllowableActions());

    assertEquals(expected.getAllowableActions().size(), actual.getAllowableActions().size());

    for (String action : expected.getAllowableActions().keySet()) {
      Boolean expectedBoolean = expected.getAllowableActions().get(action);
      Boolean actualBoolean = actual.getAllowableActions().get(action);

      assertEquals(expectedBoolean, actualBoolean);
    }
  }

  protected void assertEquals(AccessControlList expected, AccessControlList actual) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("ACL data is null!");
    }

    if ((expected.getAces() == null) && (actual.getAces() == null)) {
      return;
    }

    if ((expected.getAces() == null) || (actual.getAces() == null)) {
      fail("ACE data is null!");
    }

    // assertEquals(expected.isExact(), actual.isExact());
    assertEquals(expected.getAces().size(), actual.getAces().size());

    for (int i = 0; i < expected.getAces().size(); i++) {
      assertEquals(expected.getAces().get(i), actual.getAces().get(i));
    }
  }

  protected void assertEquals(AccessControlEntry expected, AccessControlEntry actual) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("ACE data is null!");
    }

    assertNotNull(expected.getPrincipal());
    assertNotNull(expected.getPrincipal().getPrincipalId());
    assertNotNull(actual.getPrincipal());
    assertNotNull(actual.getPrincipal().getPrincipalId());
    assertEquals(expected.getPrincipal().getPrincipalId(), actual.getPrincipal().getPrincipalId());

    assertEqualLists(expected.getPermissions(), actual.getPermissions());
  }

  protected void assertEquals(RenditionData expected, RenditionData actual) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("Rendition is null!");
    }

    assertEquals(expected.getKind(), actual.getKind());
    assertEquals(expected.getMimeType(), actual.getMimeType());
    assertEquals(expected.getLength(), actual.getLength());
    assertEquals(expected.getStreamId(), actual.getStreamId());
    assertEquals(expected.getTitle(), actual.getTitle());
    assertEquals(expected.getHeight(), actual.getHeight());
    assertEquals(expected.getWidth(), actual.getWidth());
    assertEquals(expected.getRenditionDocumentId(), actual.getRenditionDocumentId());
  }

  protected void assertContent(byte[] expected, byte[] actual) {
    assertNotNull(expected);
    assertNotNull(actual);

    assertEquals(expected.length, actual.length);

    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }

  protected void assertEqualLists(List<?> expected, List<?> actual) {
    if ((expected == null) && (actual == null)) {
      return;
    }

    if ((expected == null) || (actual == null)) {
      fail("One list is null!");
    }

    assertEquals(expected.size(), actual.size());

    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actual.get(i));
    }
  }
}
