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
package org.apache.opencmis.server.impl.atompub;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.ReturnVersion;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.ObjectInFolderContainer;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.server.spi.ObjectInfo;
import org.apache.opencmis.server.spi.ObjectInfoHolder;
import org.apache.opencmis.server.spi.RenditionInfo;

/**
 * This class contains operations used by all services.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class AtomPubUtils {

  public static final String RESOURCE_CHILDREN = "children";
  public static final String RESOURCE_DESCENDANTS = "descendants";
  public static final String RESOURCE_FOLDERTREE = "foldertree";
  public static final String RESOURCE_TYPE = "type";
  public static final String RESOURCE_TYPES = "types";
  public static final String RESOURCE_TYPESDESC = "typedesc";
  public static final String RESOURCE_ENTRY = "entry";
  public static final String RESOURCE_PARENTS = "parents";
  public static final String RESOURCE_VERSIONS = "versions";
  public static final String RESOURCE_ALLOWABLEACIONS = "allowableactions";
  public static final String RESOURCE_ACL = "acl";
  public static final String RESOURCE_POLICIES = "policies";
  public static final String RESOURCE_RELATIONSHIPS = "relationships";
  public static final String RESOURCE_OBJECTBYID = "id";
  public static final String RESOURCE_OBJECTBYPATH = "path";
  public static final String RESOURCE_QUERY = "query";
  public static final String RESOURCE_CHECKEDOUT = "checkedout";
  public static final String RESOURCE_UNFILED = "unfiled";
  public static final String RESOURCE_CHANGES = "changes";
  public static final String RESOURCE_CONTENT = "content";

  public static final BigInteger PAGE_SIZE = BigInteger.valueOf(100);

  public static final String TYPE_AUTHOR = "unknown";

  /**
   * Private constructor.
   */
  private AtomPubUtils() {
  }

  /**
   * Compiles the base URL for links, collections and templates.
   */
  public static UrlBuilder compileBaseUrl(HttpServletRequest request, String repositoryId) {
    UrlBuilder url = new UrlBuilder(request.getScheme(), request.getServerName(), request
        .getServerPort(), null);

    url.addPath(request.getContextPath());
    url.addPath(request.getServletPath());

    if (repositoryId != null) {
      url.addPath(repositoryId);
    }

    return url;
  }

  /**
   * Compiles a URL for links, collections and templates.
   */
  public static String compileUrl(UrlBuilder baseUrl, String resource, String id) {
    return compileUrlBuilder(baseUrl, resource, id).toString();
  }

  /**
   * Compiles a URL for links, collections and templates.
   */
  public static UrlBuilder compileUrlBuilder(UrlBuilder baseUrl, String resource, String id) {
    UrlBuilder url = new UrlBuilder(baseUrl);
    url.addPath(resource);

    if (id != null) {
      url.addParameter("id", id);
    }

    return url;
  }

  // -------------------------------------------------------------------------
  // --- parameters ---
  // -------------------------------------------------------------------------

  /**
   * Extracts a string parameter.
   */
  public static String getStringParameter(HttpServletRequest request, String name) {
    return request.getParameter(name);
  }

  /**
   * Extracts a boolean parameter (with default).
   */
  public static boolean getBooleanParameter(HttpServletRequest request, String name, boolean def) {
    String value = request.getParameter(name);
    if (value == null) {
      return def;
    }

    return Boolean.valueOf(value);
  }

  /**
   * Extracts a boolean parameter.
   */
  public static Boolean getBooleanParameter(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    if (value == null) {
      return null;
    }

    return Boolean.valueOf(value);
  }

  /**
   * Extracts an integer parameter (with default).
   */
  public static BigInteger getBigIntegerParameter(HttpServletRequest request, String name, long def) {
    BigInteger result = getBigIntegerParameter(request, name);
    if (result == null) {
      result = BigInteger.valueOf(def);
    }

    return result;
  }

  /**
   * Extracts an integer parameter.
   */
  public static BigInteger getBigIntegerParameter(HttpServletRequest request, String name) {
    String value = request.getParameter(name);
    if (value == null) {
      return null;
    }

    try {
      return new BigInteger(value);
    }
    catch (Exception e) {
      throw new CmisInvalidArgumentException("Invalid parameter '" + name + "'!");
    }
  }

  /**
   * Extracts an enum parameter.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getEnumParameter(HttpServletRequest request, String name, Class<T> clazz) {
    String value = request.getParameter(name);
    if (value == null) {
      return null;
    }

    try {
      Method m = clazz.getMethod("fromValue", new Class[] { String.class });
      return (T) m.invoke(null, new Object[] { value });
    }
    catch (IllegalArgumentException iae) {
      throw new CmisInvalidArgumentException("Invalid parameter '" + name + "'!");
    }
    catch (Exception e) {
      throw new CmisRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Extracts a property from an object.
   */
  @SuppressWarnings("unchecked")
  public static <T> T getProperty(ObjectData object, String name, Class<T> clazz) {
    if (object == null) {
      return null;
    }

    PropertiesData propData = object.getProperties();
    if (propData == null) {
      return null;
    }

    Map<String, PropertyData<?>> properties = propData.getProperties();
    if (properties == null) {
      return null;
    }

    PropertyData<?> property = properties.get(name);
    if (property == null) {
      return null;
    }

    Object value = property.getFirstValue();
    if (!clazz.isInstance(value)) {
      return null;
    }

    return (T) value;
  }

  // -------------------------------------------------------------------------
  // --- entry builder ---
  // -------------------------------------------------------------------------

  /**
   * Writes the a object entry.
   */
  public static void writeObjectEntry(AtomEntry entry, ObjectData object,
      ObjectInfoHolder infoHolder, List<ObjectInFolderContainer> children, String repositoryId,
      String pathSegment, String relativePathSegment, UrlBuilder baseUrl, boolean isRoot)
      throws XMLStreamException, JAXBException {
    if ((object == null) || (infoHolder == null)) {
      throw new CmisRuntimeException("Object or Object Info not set!");
    }

    ObjectInfo info = infoHolder.getObjectInfo(object.getId());
    if (info == null) {
      throw new CmisRuntimeException("Object Info not found!");
    }

    // start
    entry.startEntry(isRoot);

    // write object
    String contentSrc = null;

    if (info.hasContent()) {
      UrlBuilder contentSrcBuilder = compileUrlBuilder(baseUrl, RESOURCE_CONTENT, info.getId());
      if (info.getFileName() != null) {
        contentSrcBuilder.addPath(info.getFileName());
      }

      contentSrc = contentSrcBuilder.toString();
    }

    entry.writeObject(object, info, contentSrc, info.getContentType(), pathSegment,
        relativePathSegment);

    // write links
    entry.writeServiceLink(baseUrl.toString(), repositoryId);

    entry.writeSelfLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getId()), info.getId());
    entry.writeEnclosureLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getId()));
    entry.writeEditLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getId()));
    entry.writeDescribedByLink(compileUrl(baseUrl, RESOURCE_TYPE, info.getTypeId()));
    entry.writeAllowableActionsLink(compileUrl(baseUrl, RESOURCE_ALLOWABLEACIONS, info.getId()));

    if (info.hasParent()) {
      entry.writeUpLink(compileUrl(baseUrl, RESOURCE_PARENTS, info.getId()),
          Constants.MEDIATYPE_FEED);
    }

    if (info.getBaseType() == BaseObjectTypeIds.CMIS_FOLDER) {
      entry.writeDownLink(compileUrl(baseUrl, RESOURCE_CHILDREN, info.getId()),
          Constants.MEDIATYPE_FEED);

      if (info.supportsDescendants()) {
        entry.writeDownLink(compileUrl(baseUrl, RESOURCE_DESCENDANTS, info.getId()),
            Constants.MEDIATYPE_DESCENDANTS);
      }

      if (info.supportsFolderTree()) {
        entry.writeFolderTreeLink(compileUrl(baseUrl, RESOURCE_FOLDERTREE, info.getId()));
      }
    }

    if (info.hasVersionHistory()) {
      entry.writeVersionHistoryLink(compileUrl(baseUrl, RESOURCE_VERSIONS, info.getId()));
    }

    if (!info.isCurrentVersion()) {
      UrlBuilder cvUrl = compileUrlBuilder(baseUrl, RESOURCE_ENTRY, info.getId());
      cvUrl.addParameter(Constants.PARAM_RETURN_VERSION, ReturnVersion.LATEST);
      entry.writeEditLink(cvUrl.toString());
    }

    if (contentSrc != null) {
      entry.writeEditMediaLink(contentSrc, info.getContentType());
    }

    if (info.getWorkingCopyId() != null) {
      entry.writeWorkingCopyLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getWorkingCopyId()));
    }

    if (info.getWorkingCopyOriginalId() != null) {
      entry.writeViaLink(compileUrl(baseUrl, RESOURCE_ENTRY, info.getWorkingCopyOriginalId()));
    }

    if (info.getRenditionInfos() != null) {
      for (RenditionInfo ri : info.getRenditionInfos()) {
        entry.writeAlternateLink(compileUrl(baseUrl, RESOURCE_CONTENT, ri.getId()), ri
            .getContenType(), ri.getKind(), ri.getTitle(), ri.getLength());
      }
    }

    if (info.hasAcl()) {
      entry.writeAclLink(compileUrl(baseUrl, RESOURCE_ACL, info.getId()));
    }

    if (info.supportsPolicies()) {
      entry.writeAclLink(compileUrl(baseUrl, RESOURCE_POLICIES, info.getId()));
    }

    if (info.supportsRelationships()) {
      entry.writeRelationshipsLink(compileUrl(baseUrl, RESOURCE_RELATIONSHIPS, info.getId()));
    }

    if (info.getRelationshipSourceIds() != null) {
      for (String id : info.getRelationshipSourceIds()) {
        entry.writeRelationshipSourceLink(compileUrl(baseUrl, RESOURCE_ENTRY, id));
      }
    }

    if (info.getRelationshipTargetIds() != null) {
      for (String id : info.getRelationshipTargetIds()) {
        entry.writeRelationshipTargetLink(compileUrl(baseUrl, RESOURCE_ENTRY, id));
      }
    }

    // write children
    if ((children != null) && (children.size() > 0)) {
      writeObjectChildren(entry, info, children, infoHolder, repositoryId, baseUrl);
    }

    // we are done
    entry.endEntry();
  }

  /**
   * Writes an objects entry children feed.
   */
  public static void writeObjectChildren(AtomEntry entry, ObjectInfo folderInfo,
      List<ObjectInFolderContainer> children, ObjectInfoHolder infoHolder, String repositoryId,
      UrlBuilder baseUrl) throws XMLStreamException, JAXBException {

    // start
    AtomFeed feed = new AtomFeed(entry.getWriter());
    feed.startChildren();
    feed.startFeed(false);

    // write basic Atom feed elements
    feed.writeFeedElements(folderInfo.getId(), folderInfo.getCreatedBy(), folderInfo.getName(),
        folderInfo.getLastModificationDate(), null, null);

    // write links
    feed.writeServiceLink(baseUrl.toString(), repositoryId);

    feed.writeSelfLink(compileUrl(baseUrl, RESOURCE_DESCENDANTS, folderInfo.getId()), null);

    feed.writeViaLink(compileUrl(baseUrl, RESOURCE_ENTRY, folderInfo.getId()));

    feed.writeDownLink(compileUrl(baseUrl, RESOURCE_CHILDREN, folderInfo.getId()),
        Constants.MEDIATYPE_FEED);

    feed.writeDownLink(compileUrl(baseUrl, RESOURCE_FOLDERTREE, folderInfo.getId()),
        Constants.MEDIATYPE_DESCENDANTS);

    feed.writeUpLink(compileUrl(baseUrl, RESOURCE_PARENTS, folderInfo.getId()),
        Constants.MEDIATYPE_FEED);

    for (ObjectInFolderContainer container : children) {
      if ((container != null) && (container.getObject() != null)) {
        writeObjectEntry(entry, container.getObject().getObject(), infoHolder, container
            .getChildren(), repositoryId, container.getObject().getPathSegment(), null, baseUrl,
            false);
      }
    }

    // we are done
    feed.endFeed();
    feed.endChildren();
  }

  /**
   * Writes the a type entry.
   */
  public static void writeTypeEntry(AtomEntry entry, TypeDefinition type,
      List<TypeDefinitionContainer> children, String repositoryId, UrlBuilder baseUrl,
      boolean isRoot) throws XMLStreamException, JAXBException {

    // start
    entry.startEntry(isRoot);

    // write type
    entry.writeType(type);

    // write links
    entry.writeServiceLink(baseUrl.toString(), repositoryId);

    entry.writeSelfLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getId()), type.getId());
    entry.writeEnclosureLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getId()));
    if (type.getParentId() != null) {
      entry.writeUpLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getParentId()),
          Constants.MEDIATYPE_ENTRY);
    }
    UrlBuilder downLink = compileUrlBuilder(baseUrl, RESOURCE_TYPES, null);
    downLink.addParameter(Constants.PARAM_TYPE_ID, type.getId());
    entry.writeDownLink(downLink.toString(), Constants.MEDIATYPE_CHILDREN);
    entry.writeDescribedByLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getBaseId().value()));

    // write children
    if ((children != null) && (children.size() > 0)) {
      writeTypeChildren(entry, type, children, repositoryId, baseUrl);
    }

    // we are done
    entry.endEntry();
  }

  /**
   * Writes the a type entry children feed.
   */
  private static void writeTypeChildren(AtomEntry entry, TypeDefinition type,
      List<TypeDefinitionContainer> children, String repositoryId, UrlBuilder baseUrl)
      throws XMLStreamException, JAXBException {

    // start
    AtomFeed feed = new AtomFeed(entry.getWriter());
    feed.startChildren();
    feed.startFeed(false);

    // write basic Atom feed elements
    feed.writeFeedElements(type.getId(), TYPE_AUTHOR, type.getDisplayName(),
        new GregorianCalendar(), null, null);

    feed.writeServiceLink(baseUrl.toString(), repositoryId);

    UrlBuilder selfLink = compileUrlBuilder(baseUrl, RESOURCE_TYPESDESC, null);
    selfLink.addParameter(Constants.PARAM_TYPE_ID, type.getId());
    feed.writeSelfLink(selfLink.toString(), type.getId());

    feed.writeViaLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getId()));

    UrlBuilder downLink = compileUrlBuilder(baseUrl, RESOURCE_TYPES, null);
    downLink.addParameter(Constants.PARAM_TYPE_ID, type.getId());
    feed.writeDownLink(downLink.toString(), Constants.MEDIATYPE_FEED);

    if (type.getParentId() != null) {
      feed.writeUpLink(compileUrl(baseUrl, RESOURCE_TYPE, type.getParentId()),
          Constants.MEDIATYPE_ENTRY);
    }

    // write tree
    for (TypeDefinitionContainer container : children) {
      if ((container != null) && (container.getTypeDefinition() != null)) {
        writeTypeEntry(entry, container.getTypeDefinition(), container.getChildren(), repositoryId,
            baseUrl, false);
      }
    }

    // we are done
    feed.endFeed();
    feed.endChildren();
  }
}
