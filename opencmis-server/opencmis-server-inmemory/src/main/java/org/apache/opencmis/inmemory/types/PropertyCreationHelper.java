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
package org.apache.opencmis.inmemory.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.Choice;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.Cardinality;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.PropertyType;
import org.apache.opencmis.commons.enums.Updatability;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.opencmis.commons.provider.AllowableActionsData;
import org.apache.opencmis.commons.provider.ObjectData;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.ProviderObjectFactory;
import org.apache.opencmis.inmemory.DataObjectCreator;
import org.apache.opencmis.inmemory.FilterParser;
import org.apache.opencmis.inmemory.NameValidator;
import org.apache.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.opencmis.inmemory.storedobj.api.StoredObject;

/**
 * @author Jens
 * 
 */

public class PropertyCreationHelper {
  private static Log log = LogFactory.getLog(PropertyCreationHelper.class);
  
  public static PropertyBooleanDefinitionImpl createBooleanDefinition(String id, String displayName) {    
    PropertyBooleanDefinitionImpl prop = new PropertyBooleanDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.BOOLEAN, displayName, Cardinality.SINGLE);    
    return prop;
  }

  public static PropertyBooleanDefinitionImpl createBooleanMultiDefinition(String id, String displayName) {
    PropertyBooleanDefinitionImpl prop = new PropertyBooleanDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.BOOLEAN, displayName, Cardinality.MULTI);
    return prop;
  }
  
  public static PropertyDateTimeDefinitionImpl createDateTimeDefinition(String id, String displayName) {    
    PropertyDateTimeDefinitionImpl prop = new PropertyDateTimeDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.DATETIME, displayName, Cardinality.SINGLE);    
    return prop;
  }

  public static PropertyDateTimeDefinitionImpl createDateTimeMultiDefinition(String id, String displayName) {
    PropertyDateTimeDefinitionImpl prop = new PropertyDateTimeDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.DATETIME, displayName, Cardinality.MULTI);
    return prop;
  }
  
  public static PropertyDecimalDefinitionImpl createDecimalDefinition(String id, String displayName) {    
    PropertyDecimalDefinitionImpl prop = new PropertyDecimalDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.DECIMAL, displayName, Cardinality.SINGLE);    
    return prop;
  }

  public static PropertyDecimalDefinitionImpl createDecimalMultiDefinition(String id, String displayName) {
    PropertyDecimalDefinitionImpl prop = new PropertyDecimalDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.DECIMAL, displayName, Cardinality.MULTI);
    return prop;
  }
  
  public static PropertyHtmlDefinitionImpl createHtmlDefinition(String id, String displayName) {    
    PropertyHtmlDefinitionImpl prop = new PropertyHtmlDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.HTML, displayName, Cardinality.SINGLE);    
    return prop;
  }

  public static PropertyHtmlDefinitionImpl createHtmlMultiDefinition(String id, String displayName) {
    PropertyHtmlDefinitionImpl prop = new PropertyHtmlDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.HTML, displayName, Cardinality.MULTI);
    return prop;
  }

  public static PropertyIdDefinitionImpl createIdDefinition(String id, String displayName) {    
    PropertyIdDefinitionImpl prop = new PropertyIdDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.ID, displayName, Cardinality.SINGLE);    
    return prop;
  }

  public static PropertyIdDefinitionImpl createIdMultiDefinition(String id, String displayName) {
    PropertyIdDefinitionImpl prop = new PropertyIdDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.ID, displayName, Cardinality.MULTI);
    return prop;
  }

  public static PropertyIntegerDefinitionImpl createIntegerDefinition(String id, String displayName) {    
    PropertyIntegerDefinitionImpl prop = new PropertyIntegerDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.INTEGER, displayName, Cardinality.SINGLE);    
    return prop;
  }

  public static PropertyIntegerDefinitionImpl createIntegerMultiDefinition(String id, String displayName) {
    PropertyIntegerDefinitionImpl prop = new PropertyIntegerDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.INTEGER, displayName, Cardinality.MULTI);
    return prop;
  }
  
  public static PropertyStringDefinitionImpl createStringDefinition(String id, String displayName) {    
    PropertyStringDefinitionImpl prop = new PropertyStringDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.STRING, displayName, Cardinality.SINGLE);    
    return prop;
  }

  public static PropertyStringDefinitionImpl createStringMultiDefinition(String id, String displayName) {
    PropertyStringDefinitionImpl prop = new PropertyStringDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.STRING, displayName, Cardinality.MULTI);
    return prop;
  }

  public static PropertyUriDefinitionImpl createUriDefinition(String id, String displayName) {    
    PropertyUriDefinitionImpl prop = new PropertyUriDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.URI, displayName, Cardinality.SINGLE);    
    return prop;
  }

  public static PropertyUriDefinitionImpl createUriMultiDefinition(String id, String displayName) {
    PropertyUriDefinitionImpl prop = new PropertyUriDefinitionImpl();
    createStandardDefinition(prop, id, PropertyType.URI, displayName, Cardinality.MULTI);
    return prop;
  }
  
  
    public static<T> void  addElemToPicklist(AbstractPropertyDefinition<T> prop, T value) {
      List<Choice<T>> choiceList = prop.getChoices();
      if (choiceList == null)
        choiceList = new ArrayList<Choice<T>>();

      ChoiceImpl<T> elem = new ChoiceImpl<T>();
      elem.setValue( Collections.singletonList(value));
      choiceList.add(elem);
    }    
    
    public static<T> void setDefaultValue(AbstractPropertyDefinition<T> prop , T defVal) {
      prop.setDefaultValue(Collections.singletonList(defVal));
    }    

  
  // internal helpers
  private static void createStandardDefinition(AbstractPropertyDefinition<?> prop,
      String id, PropertyType propType, String displayName, Cardinality card) {

    if (!NameValidator.isValidId(id))
      if (!NameValidator.isValidId(id))
        throw new IllegalArgumentException(NameValidator.ERROR_ILLEGAL_NAME);

    prop.setId(id);
    if (displayName == null)
      prop.setDisplayName("Sample " + prop.getId() + " boolean property");
    else
      prop.setDisplayName(displayName);
    prop.setLocalName(id);
    prop.setLocalNamespace("local");
    prop.setQueryName(id);
    prop.setIsInherited(false);
    prop.setCardinality(card);
    prop.setIsOpenChoice(false);
    prop.setIsQueryable(true);
    prop.setIsRequired(false);
    prop.setPropertyType(propType);
    prop.setUpdatability(Updatability.READWRITE);
  }
  
  public static PropertiesData getPropertiesFromObject(String repositoryId, StoredObject so,
      StoreManager storeManager, List<String> requestedIds) {
    // build properties collection 
    
    ProviderObjectFactory objectFactory = storeManager.getObjectFactory();
    List<PropertyData<?>> properties = new ArrayList<PropertyData<?>>();
    so.fillProperties(properties, objectFactory, requestedIds);

    String typeId = so.getTypeId();
      // (String) props.getProperties().get(PropertyIds.CMIS_OBJECT_TYPE_ID).getFirstValue();
    if (FilterParser.isContainedInFilter(PropertyIds.CMIS_BASE_TYPE_ID, requestedIds)) {
      TypeDefinitionContainer typeDefC = storeManager.getTypeById(repositoryId, typeId);
      if (typeDefC == null) {
        log.warn("getPropertiesFromObject(), cannot get type definition, a type with id " + typeId
            + " is unknown");
      } else {
        TypeDefinition typeDef = typeDefC.getTypeDefinition();
        String baseTypeId = typeDef.getBaseId().value();
        properties.add(objectFactory.createPropertyIdData(PropertyIds.CMIS_BASE_TYPE_ID, baseTypeId));
      }
    }    
    PropertiesData props = objectFactory.createPropertiesData(properties);
    return props;    
  }
  
  public static ObjectData getObjectData(StoreManager sm, StoredObject so, String filter,
      Boolean includeAllowableActions, IncludeRelationships includeRelationships,
      String renditionFilter, Boolean includePolicyIds, Boolean includeACL, ExtensionsData extension) {

    ObjectDataImpl od = new ObjectDataImpl();

    if (so == null)
      throw new CmisObjectNotFoundException("Illegal object id: null");

    // build properties collection
    List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);
    PropertiesData props = getPropertiesFromObject(so.getRepositoryId(), so,
        sm, requestedIds);

    // fill output object
    if (null != includeAllowableActions && includeAllowableActions) {
      AllowableActionsData allowableActions = DataObjectCreator.fillAllowableActions(so);
      od.setAllowableActions(allowableActions);
    }
    if (null != includeACL && includeACL)
      od.setAcl(null);
    od.setIsExactAcl(true);

    if (null != includePolicyIds && includePolicyIds)
      od.setPolicyIds(DataObjectCreator.fillPolicyIds(so));

    if (null != includeRelationships && includeRelationships != IncludeRelationships.NONE)
      od.setRelationships(DataObjectCreator.fillRelationships(includeRelationships, so));

    if (renditionFilter != null && renditionFilter.length() > 0)
      od.setRenditions(DataObjectCreator.fillRenditions(so));

    od.setProperties(props);

    // Note: do not set change event info for this call
    log.debug("stop getObject()");
    return od;
  }
  
}
