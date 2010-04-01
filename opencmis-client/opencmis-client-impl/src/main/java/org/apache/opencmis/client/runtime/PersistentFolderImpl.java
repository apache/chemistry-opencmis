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
package org.apache.opencmis.client.runtime;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.FileableCmisObject;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.client.api.ObjectId;
import org.apache.opencmis.client.api.OperationContext;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.client.runtime.util.AbstractPagingList;
import org.apache.opencmis.client.runtime.util.ContainerImpl;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.Updatability;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.ObjectInFolderData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.ObjectList;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyStringData;

public class PersistentFolderImpl extends AbstractPersistentFilableCmisObject implements Folder {

  private static final Set<Updatability> CREATE_UPDATABILITY = new HashSet<Updatability>();
  static {
    CREATE_UPDATABILITY.add(Updatability.ONCREATE);
    CREATE_UPDATABILITY.add(Updatability.READWRITE);
  }

  /**
   * Constructor.
   */
  public PersistentFolderImpl(PersistentSessionImpl session, ObjectType objectType,
      ObjectData objectData, OperationContext context) {
    initialize(session, objectType, objectData, context);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#createDocument(java.util.Map,
   * org.apache.opencmis.client.api.ContentStream,
   * org.apache.opencmis.commons.enums.VersioningState, java.util.List, java.util.List,
   * java.util.List, org.apache.opencmis.client.api.OperationContext)
   */
  public Document createDocument(Map<String, ?> properties, ContentStream contentStream,
      VersioningState versioningState, List<Policy> policies, List<Ace> addAces,
      List<Ace> removeAces, OperationContext context) {
    String objectId = getObjectId();

    ObjectFactory of = getObjectFactory();

    String newId = getProvider().getObjectService().createDocument(getRepositoryId(),
        of.convertProperties(properties, null, CREATE_UPDATABILITY), objectId,
        of.convertContentStream(contentStream), versioningState, of.convertPolicies(policies),
        of.convertAces(addAces), of.convertAces(removeAces), null);

    // if no context is provided the object will not be fetched
    if ((context == null) || (newId == null)) {
      return null;
    }

    // get the new object
    CmisObject object = getSession().getObject(getSession().createObjectId(newId), context);
    if (!(object instanceof Document)) {
      throw new CmisRuntimeException("Newly created object is not a document! New id: " + newId);
    }

    return (Document) object;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.Folder#createDocumentFromSource(org.apache.opencmis.client.api
   * .ObjectId, java.util.Map, org.apache.opencmis.commons.enums.VersioningState, java.util.List,
   * java.util.List, java.util.List, org.apache.opencmis.client.api.OperationContext)
   */
  public Document createDocumentFromSource(ObjectId source, Map<String, ?> properties,
      VersioningState versioningState, List<Policy> policies, List<Ace> addAces,
      List<Ace> removeAces, OperationContext context) {
    if ((source == null) || (source.getId() == null)) {
      throw new IllegalArgumentException("Source must be set!");
    }

    String objectId = getObjectId();

    // get the type of the source document
    ObjectType type = null;
    if (source instanceof CmisObject) {
      type = ((CmisObject) source).getBaseType();
    }
    else {
      CmisObject sourceObj = getSession().getObject(source);
      type = sourceObj.getType();
    }

    if (type.getBaseTypeId() != BaseObjectTypeIds.CMIS_DOCUMENT) {
      throw new IllegalArgumentException("Source object must be a document!");
    }

    ObjectFactory of = getObjectFactory();

    Set<Updatability> updatebility = new HashSet<Updatability>();
    updatebility.add(Updatability.READWRITE);

    String newId = getProvider().getObjectService().createDocumentFromSource(getRepositoryId(),
        source.getId(), of.convertProperties(properties, type, updatebility), objectId,
        versioningState, of.convertPolicies(policies), of.convertAces(addAces),
        of.convertAces(removeAces), null);

    // if no context is provided the object will not be fetched
    if ((context == null) || (newId == null)) {
      return null;
    }

    // get the new object
    CmisObject object = getSession().getObject(getSession().createObjectId(newId), context);
    if (!(object instanceof Document)) {
      throw new CmisRuntimeException("Newly created object is not a document! New id: " + newId);
    }

    return (Document) object;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#createFolder(java.util.Map, java.util.List,
   * java.util.List, java.util.List, org.apache.opencmis.client.api.OperationContext)
   */
  public Folder createFolder(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
      List<Ace> removeAces, OperationContext context) {
    String objectId = getObjectId();

    ObjectFactory of = getObjectFactory();

    String newId = getProvider().getObjectService().createFolder(getRepositoryId(),
        of.convertProperties(properties, null, CREATE_UPDATABILITY), objectId,
        of.convertPolicies(policies), of.convertAces(addAces), of.convertAces(removeAces), null);

    // if no context is provided the object will not be fetched
    if ((context == null) || (newId == null)) {
      return null;
    }

    // get the new object
    CmisObject object = getSession().getObject(getSession().createObjectId(newId), context);
    if (!(object instanceof Folder)) {
      throw new CmisRuntimeException("Newly created object is not a folder! New id: " + newId);
    }

    return (Folder) object;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#createPolicy(java.util.Map, java.util.List,
   * java.util.List, java.util.List, org.apache.opencmis.client.api.OperationContext)
   */
  public Policy createPolicy(Map<String, ?> properties, List<Policy> policies, List<Ace> addAces,
      List<Ace> removeAces, OperationContext context) {
    String objectId = getObjectId();

    ObjectFactory of = getObjectFactory();

    String newId = getProvider().getObjectService().createPolicy(getRepositoryId(),
        of.convertProperties(properties, null, CREATE_UPDATABILITY), objectId,
        of.convertPolicies(policies), of.convertAces(addAces), of.convertAces(removeAces), null);

    // if no context is provided the object will not be fetched
    if ((context == null) || (newId == null)) {
      return null;
    }

    // get the new object
    CmisObject object = getSession().getObject(getSession().createObjectId(newId), context);
    if (!(object instanceof Policy)) {
      throw new CmisRuntimeException("Newly created object is not a policy! New id: " + newId);
    }

    return (Policy) object;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#deleteTree(boolean,
   * org.apache.opencmis.commons.enums.UnfileObjects, boolean)
   */
  public List<String> deleteTree(boolean allVersions, UnfileObjects unfile,
      boolean continueOnFailure) {
    String repositoryId = getRepositoryId();
    String objectId = getObjectId();

    FailedToDeleteData failed = getProvider().getObjectService().deleteTree(repositoryId, objectId,
        allVersions, unfile, continueOnFailure, null);

    return failed.getIds();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getAllowedChildObjectTypes()
   */
  public List<ObjectType> getAllowedChildObjectTypes() {
    List<ObjectType> result = new ArrayList<ObjectType>();

    readLock();
    try {
      List<String> otids = getPropertyMultivalue(PropertyIds.CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS);
      if (otids == null) {
        return result;
      }

      for (String otid : otids) {
        result.add(getSession().getTypeDefinition(otid));
      }
    }
    finally {
      readUnlock();
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getCheckedOutDocs(int)
   */
  public PagingList<Document> getCheckedOutDocs(int itemsPerPage) {
    return getCheckedOutDocs(getSession().getDefaultContext(), itemsPerPage);
  }

  /*
   * (non-Javadoc)
   * 
   * @seeorg.apache.opencmis.client.api.Folder#getCheckedOutDocs(org.apache.opencmis.client.api.
   * OperationContext, int)
   */
  public PagingList<Document> getCheckedOutDocs(OperationContext context, final int itemsPerPage) {
    if (itemsPerPage < 1) {
      throw new IllegalArgumentException("itemsPerPage must be > 0!");
    }

    final String objectId = getObjectId();
    final NavigationService nagivationService = getProvider().getNavigationService();
    final ObjectFactory objectFactory = getSession().getObjectFactory();
    final OperationContext ctxt = new OperationContextImpl(context);

    return new AbstractPagingList<Document>() {

      @Override
      protected FetchResult fetchPage(int pageNumber) {
        int skipCount = pageNumber * getMaxItemsPerPage();

        // get checked out documents for this folder
        ObjectList checkedOutDocs = nagivationService.getCheckedOutDocs(getRepositoryId(),
            objectId, ctxt.getFilterString(), ctxt.getOrderBy(), ctxt.isIncludeAllowableActions(),
            ctxt.getIncludeRelationships(), ctxt.getRenditionFilterString(), BigInteger
                .valueOf(getMaxItemsPerPage()), BigInteger.valueOf(skipCount), null);

        // convert objects
        List<Document> page = new ArrayList<Document>();
        if (checkedOutDocs.getObjects() != null) {
          for (ObjectData objectData : checkedOutDocs.getObjects()) {
            CmisObject doc = objectFactory.convertObject(objectData, ctxt);
            if (!(doc instanceof Document)) {
              // should not happen...
              continue;
            }

            page.add((Document) doc);
          }
        }

        return new FetchResult(page, checkedOutDocs.getNumItems(), checkedOutDocs.hasMoreItems());
      }

      @Override
      public int getMaxItemsPerPage() {
        return itemsPerPage;
      }
    };
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getChildren(int)
   */
  public PagingList<CmisObject> getChildren(int itemsPerPage) {
    return getChildren(getSession().getDefaultContext(), itemsPerPage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.api.Folder#getChildren(org.apache.opencmis.client.api.OperationContext
   * , int)
   */
  public PagingList<CmisObject> getChildren(OperationContext context, final int itemsPerPage) {
    if (itemsPerPage < 1) {
      throw new IllegalArgumentException("itemsPerPage must be > 0!");
    }

    final String objectId = getObjectId();
    final NavigationService navigationService = getProvider().getNavigationService();
    final ObjectFactory objectFactory = getSession().getObjectFactory();
    final OperationContext ctxt = new OperationContextImpl(context);

    return new AbstractPagingList<CmisObject>() {

      @Override
      protected FetchResult fetchPage(int pageNumber) {
        int skipCount = pageNumber * getMaxItemsPerPage();

        // get the children
        ObjectInFolderList children = navigationService.getChildren(getRepositoryId(), objectId,
            ctxt.getFilterString(), ctxt.getOrderBy(), ctxt.isIncludeAllowableActions(), ctxt
                .getIncludeRelationships(), ctxt.getRenditionFilterString(), ctxt
                .isIncludePathSegments(), BigInteger.valueOf(getMaxItemsPerPage()), BigInteger
                .valueOf(skipCount), null);

        // convert objects
        List<CmisObject> page = new ArrayList<CmisObject>();
        List<ObjectInFolderData> childObjects = children.getObjects();
        if (childObjects != null) {
          for (ObjectInFolderData objectData : childObjects) {
            if (objectData.getObject() != null) {
              page.add(objectFactory.convertObject(objectData.getObject(), ctxt));
            }
          }
        }

        return new FetchResult(page, children.getNumItems(), children.hasMoreItems());
      }

      @Override
      public int getMaxItemsPerPage() {
        return itemsPerPage;
      }
    };
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getDescendants(int)
   */
  public List<Container<FileableCmisObject>> getDescendants(int depth) {
    return getDescendants(depth, getSession().getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getDescendants(int,
   * org.apache.opencmis.client.api.OperationContext)
   */
  public List<Container<FileableCmisObject>> getDescendants(int depth, OperationContext context) {
    String objectId = getObjectId();

    // get the descendants
    List<ObjectInFolderContainer> providerContainerList = getProvider().getNavigationService()
        .getDescendants(getRepositoryId(), objectId, BigInteger.valueOf(depth),
            context.getFilterString(), context.isIncludeAllowableActions(),
            context.getIncludeRelationships(), context.getRenditionFilterString(),
            context.isIncludePathSegments(), null);

    return convertProviderContainer(providerContainerList, context);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getFolderTree(int)
   */
  public List<Container<FileableCmisObject>> getFolderTree(int depth) {
    return getFolderTree(depth, getSession().getDefaultContext());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getFolderTree(int,
   * org.apache.opencmis.client.api.OperationContext)
   */
  public List<Container<FileableCmisObject>> getFolderTree(int depth, OperationContext context) {
    String objectId = getObjectId();

    // get the folder tree
    List<ObjectInFolderContainer> providerContainerList = getProvider().getNavigationService()
        .getFolderTree(getRepositoryId(), objectId, BigInteger.valueOf(depth),
            context.getFilterString(), context.isIncludeAllowableActions(),
            context.getIncludeRelationships(), context.getRenditionFilterString(),
            context.isIncludePathSegments(), null);

    return convertProviderContainer(providerContainerList, context);
  }

  /**
   * Converts a provider container into an API container.
   */
  private List<Container<FileableCmisObject>> convertProviderContainer(
      List<ObjectInFolderContainer> providerContainerList, OperationContext context) {
    if (providerContainerList == null) {
      return null;
    }

    ObjectFactory of = getSession().getObjectFactory();

    List<Container<FileableCmisObject>> result = new ArrayList<Container<FileableCmisObject>>();
    for (ObjectInFolderContainer oifc : providerContainerList) {
      if ((oifc.getObject() == null) || (oifc.getObject().getObject() == null)) {
        // shouldn't happen ...
        continue;
      }

      // convert the object
      CmisObject object = of.convertObject(oifc.getObject().getObject(), context);
      if (!(object instanceof FileableCmisObject)) {
        // the repository must not return objects that are not fileable, but you never know...
        continue;
      }

      // convert the children
      List<Container<FileableCmisObject>> children = convertProviderContainer(oifc.getChildren(),
          context);

      // add both to current container
      result.add(new ContainerImpl<FileableCmisObject>((FileableCmisObject) object, children));
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#isRootFolder()
   */
  public boolean isRootFolder() {
    String objectId = getObjectId();
    String rootFolderId = getSession().getRepositoryInfo().getRootFolderId();

    return objectId.equals(rootFolderId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getFolderParent()
   */
  public Folder getFolderParent() {
    if (isRootFolder()) {
      return null;
    }

    List<Folder> parents = super.getParents();
    if ((parents == null) || (parents.isEmpty())) {
      return null;
    }

    return parents.get(0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getPath()
   */
  public String getPath() {
    String path;

    readLock();
    try {
      // get the path property
      path = getPropertyValue(PropertyIds.CMIS_PATH);

      // if the path property isn't set, get it
      if (path == null) {
        String objectId = getObjectId();
        ObjectData objectData = getProvider().getObjectService().getObject(getRepositoryId(),
            objectId, PropertyIds.CMIS_PATH, false, IncludeRelationships.NONE, "cmis:none", false,
            false, null);

        if ((objectData.getProperties() != null)
            && (objectData.getProperties().getProperties() != null)) {
          PropertyData<?> pathProperty = objectData.getProperties().getProperties().get(
              PropertyIds.CMIS_PATH);

          if (pathProperty instanceof PropertyStringData) {
            path = ((PropertyStringData) pathProperty).getFirstValue();
          }
        }
      }
    }
    finally {
      readUnlock();
    }

    // we still don't know the path ... it's not a CMIS compliant repository
    if (path == null) {
      throw new CmisRuntimeException("Repository didn't return " + PropertyIds.CMIS_PATH + "!");
    }

    return path;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.runtime.AbstractPersistentFilableCmisObject#getPaths()
   */
  @Override
  public List<String> getPaths() {
    return Collections.singletonList(getPath());
  }
}
