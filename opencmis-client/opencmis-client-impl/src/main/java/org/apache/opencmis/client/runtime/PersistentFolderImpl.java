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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.ContentStream;
import org.apache.opencmis.client.api.Document;
import org.apache.opencmis.client.api.FileableCmisObject;
import org.apache.opencmis.client.api.Folder;
import org.apache.opencmis.client.api.Policy;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.SessionContext;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.client.api.util.Container;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.client.runtime.util.AbstractPagingList;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.FailedToDeleteData;
import org.apache.opencmis.commons.provider.NavigationService;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderData;
import org.apache.opencmis.commons.provider.ObjectInFolderList;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;

public class PersistentFolderImpl extends AbstractPersistentFilableCmisObject implements Folder {

  /**
   * Constructor.
   */
  public PersistentFolderImpl(PersistentSessionImpl session, ObjectType objectType,
      ObjectData objectData) {
    initialize(session, objectType, objectData);
  }

  public PersistentFolderImpl(PersistentSessionImpl session) {
    initialize(session, null, null);
  }

  public Document createDocument(String name) {
    throw new CmisRuntimeException("not implemented");
  }

  public Document createDocument(String name, String typeId) {
    throw new CmisRuntimeException("not implemented");
  }

  public Document createDocument(List<Property<?>> properties, ContentStream contentstream,
      VersioningState versioningState, List<Policy> policies, List<Ace> addACEs,
      List<Ace> removeACEs) {
    throw new CmisRuntimeException("not implemented");
  }

  public Document createDocumentFromSource(Document source, List<Property<?>> properties,
      VersioningState versioningState, List<Policy> policies, List<Ace> addACEs,
      List<Ace> removeACEs) {
    throw new CmisRuntimeException("not implemented");
  }

  public Folder createFolder(List<Property<?>> properties, List<Policy> policies,
      List<Ace> addACEs, List<Ace> removeACEs) {

    Folder f = getSession().getObjectFactory().createFolder(this, properties, policies, addACEs,
        removeACEs);
    return f;
  }

  public Policy createPolicy(List<Property<?>> properties, List<Policy> policies,
      List<Ace> addACEs, List<Ace> removeACEs) {
    throw new CmisRuntimeException("not implemented");
  }

  public List<String> deleteTree(boolean allVersions, UnfileObjects unfile,
      boolean continueOnFailure) {
    String repositoryId = getRepositoryId();
    String objectId = getObjectId();

    FailedToDeleteData failed = getProvider().getObjectService().deleteTree(repositoryId, objectId,
        allVersions, unfile, continueOnFailure, null);

    return failed.getIds();
  }

  public List<ObjectType> getAllowedChildObjectTypes() {
    throw new CmisRuntimeException("not implemented");
  }

  public PagingList<Document> getCheckedOutDocs(String orderby, int itemsPerPage) {
    throw new CmisRuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.Folder#getChildren(java.lang.String, int)
   */
  public PagingList<CmisObject> getChildren(final String orderBy, final int itemsPerPage) {
    if (itemsPerPage < 1) {
      throw new IllegalArgumentException("itemsPerPage must be > 0!");
    }

    final String objectId = getObjectId();
    final SessionContext context = getSession().getContext();
    final NavigationService navigationService = getProvider().getNavigationService();
    final ObjectFactory objectFactory = getSession().getObjectFactory();

    return new AbstractPagingList<CmisObject>() {

      @Override
      protected FetchResult fetchPage(int pageNumber) {
        int skipCount = pageNumber * getMaxItemsPerPage();

        // get the children
        ObjectInFolderList children = navigationService.getChildren(getRepositoryId(), objectId,
            context.getIncludeProperties(), orderBy, context.getIncludeAllowableActions(), context
                .getIncludeRelationships(), context.getIncludeRenditions(), context
                .getIncludePathSegments(), BigInteger.valueOf(getMaxItemsPerPage()), BigInteger
                .valueOf(skipCount), null);

        // convert objects
        List<CmisObject> page = new ArrayList<CmisObject>();
        for (ObjectInFolderData objectData : children.getObjects()) {
          if (objectData.getObject() != null) {
            page.add(objectFactory.convertObject(objectData.getObject()));
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

  public List<Container<FileableCmisObject>> getFolderTree(int depth) {
    throw new CmisRuntimeException("not implemented");
  }

  public List<Container<FileableCmisObject>> getDescendants(int depth) {
    throw new CmisRuntimeException("not implemented");
  }

  public Folder getFolderParent() {
    List<Folder> parents = getParents();

    if ((parents == null) || (parents.isEmpty())) {
      return null;
    }

    return parents.get(0);
  }

  public String getPath() {
    List<String> paths = getPaths();
    if ((paths == null) || (paths.isEmpty())) {
      return null;
    }

    return paths.get(0);
  }

  /**
   * Create folder in backend
   * 
   * @param parent
   * @param properties
   * @param policies
   * @param addACEs
   * @param removeACEs
   */
  public void create(Folder parent, List<Property<?>> properties, List<Policy> policies,
      List<Ace> addAce, List<Ace> removeAce) {

    String repositoryId = getRepositoryId();
    String parentFolderId = parent.getId();
    PropertiesData pd = this.convertToPropertiesData(properties);
    List<String> pol = this.convertToPoliciesData(policies);
    AccessControlList addAcl = SessionUtil.convertAces(getSession(), addAce);
    AccessControlList removeAcl = SessionUtil.convertAces(getSession(), removeAce);

    String objectId = getProvider().getObjectService().createFolder(repositoryId, pd,
        parentFolderId, pol, addAcl, removeAcl, null);
    ObjectData newObjectData = getProvider().getObjectService().getObject(repositoryId, objectId,
        null, false, IncludeRelationships.NONE, null, true, true, null);

    getSession().getCache().put(this);
  }

  private List<String> convertToPoliciesData(List<Policy> policies) {
    List<String> pList = null;

    if (policies != null) {
      pList = new ArrayList<String>();
      for (Policy pol : policies) {
        pList.add(pol.getId());
      }
    }
    return pList;
  }

  @SuppressWarnings("unchecked")
  private PropertiesData convertToPropertiesData(List<Property<?>> origProperties) {
    ProviderObjectFactory of = getProvider().getObjectFactory();

    List<PropertyData<?>> convProperties = new ArrayList<PropertyData<?>>();
    PropertyData<?> convProperty = null;

    convProperties.add(of.createPropertyStringData(PropertyIds.CMIS_NAME, "testfolder"));
    convProperties.add(of.createPropertyIdData(PropertyIds.CMIS_OBJECT_TYPE_ID, "cmis_Folder"));

    for (Property<?> origProperty : origProperties) {

      switch (origProperty.getType()) {
      case BOOLEAN:
        Property<Boolean> pb = (Property<Boolean>) origProperty;
        convProperty = of.createPropertyBooleanData(pb.getId(), pb.getValue());
        break;
      case DATETIME:
        Property<GregorianCalendar> pg = (Property<GregorianCalendar>) origProperty;
        convProperty = of.createPropertyDateTimeData(pg.getId(), pg.getValue());
        break;
      case DECIMAL:
        Property<BigDecimal> pd = (Property<BigDecimal>) origProperty;
        convProperty = of.createPropertyDecimalData(pd.getId(), pd.getValue());
        break;
      case HTML:
        Property<String> ph = (Property<String>) origProperty;
        convProperty = of.createPropertyHtmlData(ph.getId(), ph.getValue());
        break;
      case ID:
        Property<String> pi = (Property<String>) origProperty;
        convProperty = of.createPropertyIdData(pi.getId(), pi.getValue());
        break;
      case INTEGER:
        Property<BigInteger> pn = (Property<BigInteger>) origProperty;
        convProperty = of.createPropertyIntegerData(pn.getId(), pn.getValue());
        break;
      case STRING:
        Property<String> ps = (Property<String>) origProperty;
        convProperty = of.createPropertyStringData(ps.getId(), ps.getValue());
        break;
      case URI:
        Property<String> pu = (Property<String>) origProperty;
        convProperty = of.createPropertyUriData(pu.getId(), pu.getValue());
        break;
      default:
        throw new CmisRuntimeException("unsupported property type" + origProperty.getType());
      }
      convProperties.add(convProperty);
    }

    PropertiesData pd = of.createPropertiesData(convProperties);

    return pd;
  }

}
