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
package org.apache.opencmis.client.provider.spi.atompub;

import static org.apache.opencmis.commons.impl.Converter.convert;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomElement;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomEntry;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomFeed;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomLink;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.dataobjects.TypeDefinitionContainerImpl;
import org.apache.opencmis.commons.impl.dataobjects.TypeDefinitionListImpl;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.opencmis.commons.provider.RepositoryInfoData;
import org.apache.opencmis.commons.provider.RepositoryService;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryServiceImpl extends AbstractAtomPubService implements RepositoryService {

  /**
   * Constructor.
   */
  public RepositoryServiceImpl(Session session) {
    setSession(session);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfos(org.apache.opencmis.client.provider
   * .ExtensionsData)
   */
  public List<RepositoryInfoData> getRepositoryInfos(ExtensionsData extension) {
    return getRepositoriesInternal(null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getRepositoryInfo(java.lang.String,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public RepositoryInfoData getRepositoryInfo(String repositoryId, ExtensionsData extension) {
    List<RepositoryInfoData> repositoryInfos = getRepositoriesInternal(repositoryId);

    // find the repository
    for (RepositoryInfoData info : repositoryInfos) {
      if (info.getRepositoryId() == null) {
        continue;
      }

      if (info.getRepositoryId().equals(repositoryId)) {
        return info;
      }
    }

    throw new CmisObjectNotFoundException("Repository not found!");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDefinition(java.lang.String,
   * java.lang.String, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinition getTypeDefinition(String repositoryId, String typeId,
      ExtensionsData extension) {
    return getTypeDefinitionInternal(repositoryId, typeId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeChildren(java.lang.String,
   * java.lang.String, java.lang.Boolean, java.math.BigInteger, java.math.BigInteger,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public TypeDefinitionList getTypeChildren(String repositoryId, String typeId,
      Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension) {
    TypeDefinitionListImpl result = new TypeDefinitionListImpl();

    // find the link
    String link = null;
    if (typeId == null) {
      link = loadCollection(repositoryId, Constants.COLLECTION_TYPES);
    }
    else {
      link = loadTypeLink(repositoryId, typeId, Constants.REL_DOWN, Constants.MEDIATYPE_CHILDREN);
    }

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or type!");
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_TYPE_ID, typeId);
    url.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);
    url.addParameter(Constants.PARAM_MAX_ITEMS, maxItems);
    url.addParameter(Constants.PARAM_SKIP_COUNT, skipCount);

    // read and parse
    HttpUtils.Response resp = read(url);
    AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

    // handle top level
    for (AtomElement element : feed.getElements()) {
      if (element.getObject() instanceof AtomLink) {
        if (isNextLink(element)) {
          result.setHasMoreItems(Boolean.TRUE);
        }
      }
      else if (isInt(NAME_NUM_ITEMS, element)) {
        result.setNumItems((BigInteger) element.getObject());
      }
    }

    // get the children
    if (!feed.getEntries().isEmpty()) {
      result.setList(new ArrayList<TypeDefinition>(feed.getEntries().size()));

      for (AtomEntry entry : feed.getEntries()) {
        TypeDefinition child = null;

        // walk through the entry
        for (AtomElement element : entry.getElements()) {
          if (element.getObject() instanceof AtomLink) {
            addTypeLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
          }
          else if (element.getObject() instanceof CmisTypeDefinitionType) {
            child = convert((CmisTypeDefinitionType) element.getObject());
          }
        }

        if (child != null) {
          result.getList().add(child);
        }
      }
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.RepositoryService#getTypeDescendants(java.lang.String,
   * java.lang.String, java.math.BigInteger, java.lang.Boolean,
   * org.apache.opencmis.client.provider.ExtensionsData)
   */
  public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId,
      BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension) {
    List<TypeDefinitionContainer> result = new ArrayList<TypeDefinitionContainer>();

    // find the link
    String link = null;
    if (typeId == null) {
      link = loadRepositoryLink(repositoryId, Constants.REP_REL_TYPEDESC);
    }
    else {
      link = loadTypeLink(repositoryId, typeId, Constants.REL_DOWN, Constants.MEDIATYPE_DESCENDANTS);
    }

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or type!");
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_TYPE_ID, typeId);
    url.addParameter(Constants.PARAM_DEPTH, depth);
    url.addParameter(Constants.PARAM_PROPERTY_DEFINITIONS, includePropertyDefinitions);

    // read and parse
    HttpUtils.Response resp = read(url);
    AtomFeed feed = parse(resp.getStream(), AtomFeed.class);

    // process tree
    addTypeDescendantsLevel(repositoryId, feed, result);

    return result;
  }

  /**
   * Adds type descendants level recursively.
   */
  private void addTypeDescendantsLevel(String repositoryId, AtomFeed feed,
      List<TypeDefinitionContainer> containerList) {
    if ((feed == null) || (feed.getEntries().isEmpty())) {
      return;
    }

    // walk through the feed
    for (AtomEntry entry : feed.getEntries()) {
      TypeDefinitionContainerImpl childContainer = null;
      List<TypeDefinitionContainer> childContainerList = new ArrayList<TypeDefinitionContainer>();

      // walk through the entry
      for (AtomElement element : entry.getElements()) {
        if (element.getObject() instanceof AtomLink) {
          addTypeLink(repositoryId, entry.getId(), (AtomLink) element.getObject());
        }
        else if (element.getObject() instanceof CmisTypeDefinitionType) {
          childContainer = new TypeDefinitionContainerImpl(convert((CmisTypeDefinitionType) element
              .getObject()));
        }
        else if (element.getObject() instanceof AtomFeed) {
          addTypeDescendantsLevel(repositoryId, (AtomFeed) element.getObject(), childContainerList);
        }
      }

      if (childContainer != null) {
        childContainer.setChildren(childContainerList);
        containerList.add(childContainer);
      }
    }
  }
}
