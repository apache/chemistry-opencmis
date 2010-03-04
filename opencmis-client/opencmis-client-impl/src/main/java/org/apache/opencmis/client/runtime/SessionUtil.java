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
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.Ace;
import org.apache.opencmis.client.api.Acl;
import org.apache.opencmis.client.api.AllowableActions;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.QueryProperty;
import org.apache.opencmis.client.api.Rendition;
import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.objecttype.ObjectType;
import org.apache.opencmis.client.api.repository.ObjectFactory;
import org.apache.opencmis.client.runtime.objecttype.DocumentTypeImpl;
import org.apache.opencmis.client.runtime.objecttype.FolderTypeImpl;
import org.apache.opencmis.client.runtime.objecttype.PolicyTypeImpl;
import org.apache.opencmis.client.runtime.objecttype.RelationshipTypeImpl;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.DocumentTypeDefinition;
import org.apache.opencmis.commons.api.FolderTypeDefinition;
import org.apache.opencmis.commons.api.PolicyTypeDefinition;
import org.apache.opencmis.commons.api.PropertyBooleanDefinition;
import org.apache.opencmis.commons.api.PropertyDateTimeDefinition;
import org.apache.opencmis.commons.api.PropertyDecimalDefinition;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.PropertyHtmlDefinition;
import org.apache.opencmis.commons.api.PropertyIdDefinition;
import org.apache.opencmis.commons.api.PropertyIntegerDefinition;
import org.apache.opencmis.commons.api.PropertyStringDefinition;
import org.apache.opencmis.commons.api.PropertyUriDefinition;
import org.apache.opencmis.commons.api.RelationshipTypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyIdData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.commons.provider.RenditionData;

/**
 * Utility methods.
 */
public final class SessionUtil {

  private SessionUtil() {
  }

  /**
   * Converts a type definition.
   */
  public static ObjectType convertTypeDefinition(Session session, TypeDefinition typeDefinition) {
    if (typeDefinition instanceof DocumentTypeDefinition) {
      return new DocumentTypeImpl(session, typeDefinition);
    }
    else if (typeDefinition instanceof FolderTypeDefinition) {
      return new FolderTypeImpl(session, typeDefinition);
    }
    else if (typeDefinition instanceof RelationshipTypeDefinition) {
      return new RelationshipTypeImpl(session, typeDefinition);
    }
    else if (typeDefinition instanceof PolicyTypeDefinition) {
      return new PolicyTypeImpl(session, typeDefinition);
    }
    else {
      throw new CmisRuntimeException("Unknown base type!");
    }
  }

  /**
   * Converts properties.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Property<?>> convertProperties(Session session, ObjectType objectType,
      PropertiesData properties) {
    // check input
    if (objectType == null) {
      throw new IllegalArgumentException("Object type must set!");
    }

    if (objectType.getPropertyDefintions() == null) {
      throw new IllegalArgumentException("Object type has no property defintions!");
    }

    if ((properties == null) || (properties.getProperties() == null)) {
      throw new IllegalArgumentException("Properties must be set!");
    }

    // iterate through properties and convert them
    Map<String, Property<?>> result = new LinkedHashMap<String, Property<?>>();
    for (Map.Entry<String, PropertyData<?>> property : properties.getProperties().entrySet()) {
      // find property definition
      PropertyDefinition<?> definition = objectType.getPropertyDefintions().get(property.getKey());
      if (definition == null) {
        // property without definition
        throw new CmisRuntimeException("Property '" + property.getKey() + "' doesn't exist!");
      }

      Property<?> apiProperty = null;

      if (definition instanceof PropertyStringDefinition) {
        apiProperty = session.getPropertyFactory().createPropertyMultivalue(
            (PropertyStringDefinition) definition, (List<String>) property.getValue().getValues());
      }
      else if (definition instanceof PropertyIdDefinition) {
        apiProperty = session.getPropertyFactory().createPropertyMultivalue(
            (PropertyIdDefinition) definition, (List<String>) property.getValue().getValues());
      }
      else if (definition instanceof PropertyHtmlDefinition) {
        apiProperty = session.getPropertyFactory().createPropertyMultivalue(
            (PropertyHtmlDefinition) definition, (List<String>) property.getValue().getValues());
      }
      else if (definition instanceof PropertyUriDefinition) {
        apiProperty = session.getPropertyFactory().createPropertyMultivalue(
            (PropertyUriDefinition) definition, (List<String>) property.getValue().getValues());
      }
      else if (definition instanceof PropertyIntegerDefinition) {
        apiProperty = session.getPropertyFactory().createPropertyMultivalue(
            (PropertyIntegerDefinition) definition,
            (List<BigInteger>) property.getValue().getValues());
      }
      else if (definition instanceof PropertyBooleanDefinition) {
        apiProperty = session.getPropertyFactory()
            .createPropertyMultivalue((PropertyBooleanDefinition) definition,
                (List<Boolean>) property.getValue().getValues());
      }
      else if (definition instanceof PropertyDecimalDefinition) {
        apiProperty = session.getPropertyFactory().createPropertyMultivalue(
            (PropertyDecimalDefinition) definition,
            (List<BigDecimal>) property.getValue().getValues());
      }
      else if (definition instanceof PropertyDateTimeDefinition) {
        apiProperty = session.getPropertyFactory().createPropertyMultivalue(
            (PropertyDateTimeDefinition) definition,
            (List<GregorianCalendar>) property.getValue().getValues());
      }

      result.put(property.getKey(), apiProperty);
    }

    return result;
  }

  /**
   * Converts properties.
   */
  @SuppressWarnings("unchecked")
  public static PropertiesData convertProperties(Session session, Collection<Property<?>> properties) {
    // check input
    if (properties == null) {
      throw new IllegalArgumentException("Properties must be set!");
    }

    ProviderObjectFactory pof = session.getProvider().getObjectFactory();

    // iterate through properties and convert them
    List<PropertyData<?>> propertyList = new ArrayList<PropertyData<?>>();
    for (Property<?> property : properties) {

      PropertyDefinition<?> definition = property.getDefinition();
      if (definition instanceof PropertyStringDefinition) {
        propertyList.add(pof.createPropertyStringData(property.getId(), (List<String>) property
            .getValues()));
      }
      else if (definition instanceof PropertyIdDefinition) {
        propertyList.add(pof.createPropertyIdData(property.getId(), (List<String>) property
            .getValues()));
      }
      else if (definition instanceof PropertyHtmlDefinition) {
        propertyList.add(pof.createPropertyHtmlData(property.getId(), (List<String>) property
            .getValues()));
      }
      else if (definition instanceof PropertyUriDefinition) {
        propertyList.add(pof.createPropertyUriData(property.getId(), (List<String>) property
            .getValues()));
      }
      else if (definition instanceof PropertyIntegerDefinition) {
        propertyList.add(pof.createPropertyIntegerData(property.getId(),
            (List<BigInteger>) property.getValues()));
      }
      else if (definition instanceof PropertyBooleanDefinition) {
        propertyList.add(pof.createPropertyBooleanData(property.getId(), (List<Boolean>) property
            .getValues()));
      }
      else if (definition instanceof PropertyDecimalDefinition) {
        propertyList.add(pof.createPropertyDecimalData(property.getId(),
            (List<BigDecimal>) property.getValues()));
      }
      else if (definition instanceof PropertyDateTimeDefinition) {
        propertyList.add(pof.createPropertyDateTimeData(property.getId(),
            (List<GregorianCalendar>) property.getValues()));
      }
    }

    return pof.createPropertiesData(propertyList);
  }

  /**
   * Converts query properties.
   */
  @SuppressWarnings("unchecked")
  public static List<QueryProperty<?>> convertQueryProperties(Session session,
      PropertiesData properties) {
    // check input
    if ((properties == null) || (properties.getProperties() == null)) {
      throw new IllegalArgumentException("Properties must be set!");
    }

    // iterate through properties and convert them
    List<QueryProperty<?>> result = new ArrayList<QueryProperty<?>>();
    for (PropertyData<?> property : properties.getProperties().values()) {
      result.add(new QueryPropertyImpl(property.getId(), property.getQueryName(), property
          .getValues()));
    }

    return result;
  }

  /**
   * Converts allowable actions.
   */
  public static AllowableActions convertAllowableActions(Session session,
      AllowableActionsData allowableActions) {
    if ((allowableActions == null) || (allowableActions.getAllowableActions() == null)) {
      throw new IllegalArgumentException("Allowable actions must be set!");
    }

    return session.getObjectFactory().createAllowableAction(allowableActions.getAllowableActions());
  }

  /**
   * Converts ACL.
   */
  public static AccessControlList convertAces(Session session, List<Ace> aces) {
    if (aces == null) {
      throw new IllegalArgumentException("ACEs must be set!");
    }

    ProviderObjectFactory pof = session.getProvider().getObjectFactory();

    List<AccessControlEntry> providerAces = new ArrayList<AccessControlEntry>();
    for (Ace ace : aces) {
      providerAces.add(pof.createAccessControlEntry(ace.getPrincipalId(), ace.getPermissions()));
    }

    return pof.createAccessControlList(providerAces);
  }

  /**
   * Converts ACL.
   */
  public static Acl convertAcl(Session session, AccessControlList acl) {
    if (acl == null) {
      throw new IllegalArgumentException("ACL must be set!");
    }

    ObjectFactory of = session.getObjectFactory();
    List<Ace> aces = new ArrayList<Ace>();
    if (acl.getAces() != null) {
      for (AccessControlEntry ace : acl.getAces()) {
        if (ace.getPrincipal() == null) {
          continue;
        }
        aces.add(of.createAce(ace.getPrincipal().getPrincipalId(), ace.getPermissions(), ace
            .isDirect()));
      }
    }

    return of.createAcl(aces, acl.isExact());
  }

  /**
   * Converts rendition.
   */
  public static Rendition convertRendition(Session session, String objectId, RenditionData rendition) {
    if (rendition == null) {
      throw new IllegalArgumentException("Rendition must be set!");
    }

    // TODO: what should happen if the length is not set?
    long length = (rendition.getLength() == null ? -1 : rendition.getLength().longValue());
    int height = (rendition.getHeight() == null ? -1 : rendition.getHeight().intValue());
    int width = (rendition.getWidth() == null ? -1 : rendition.getWidth().intValue());

    return new RenditionImpl(session, objectId, rendition.getStreamId(), rendition
        .getRenditionDocumentId(), rendition.getKind(), length, rendition.getMimeType(), rendition
        .getTitle(), height, width);
  }

  /**
   * Extracts the type information from the given object data and returns the object type or
   * <code>null</code> if there is no type information.
   */
  public static ObjectType getTypeFromObjectData(Session session, ObjectData objectData) {
    if ((objectData == null) || (objectData.getProperties() == null)
        || (objectData.getProperties().getProperties() == null)) {
      return null;
    }

    PropertyData<?> typeProperty = objectData.getProperties().getProperties().get(
        PropertyIds.CMIS_OBJECT_TYPE_ID);
    if (!(typeProperty instanceof PropertyIdData)) {
      return null;
    }

    return session.getTypeDefinition((String) typeProperty.getFirstValue());
  }
}
