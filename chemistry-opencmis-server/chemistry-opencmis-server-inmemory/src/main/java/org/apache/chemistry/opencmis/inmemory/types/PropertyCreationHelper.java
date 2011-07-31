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
package org.apache.chemistry.opencmis.inmemory.types;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyInteger;
import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AbstractPropertyDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BindingsObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChoiceImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriDefinitionImpl;
import org.apache.chemistry.opencmis.commons.spi.BindingsObjectFactory;
import org.apache.chemistry.opencmis.inmemory.DataObjectCreator;
import org.apache.chemistry.opencmis.inmemory.FilterParser;
import org.apache.chemistry.opencmis.inmemory.NameValidator;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jens
 *
 */
public class PropertyCreationHelper {

    private static final Log log = LogFactory.getLog(PropertyCreationHelper.class);

    private PropertyCreationHelper() {
    }

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

    public static <T> void addElemToPicklist(AbstractPropertyDefinition<T> prop, T value) {
        List<Choice<T>> choiceList = prop.getChoices();
        if (choiceList == null) {
            choiceList = new ArrayList<Choice<T>>();
            prop.setChoices(choiceList);
        }

        ChoiceImpl<T> elem = new ChoiceImpl<T>();
        elem.setValue(Collections.singletonList(value));
        choiceList.add(elem);
    }

    public static <T> void setDefaultValue(AbstractPropertyDefinition<T> prop, T defVal) {
        prop.setDefaultValue(Collections.singletonList(defVal));
    }

    public static Properties getPropertiesFromObject(StoredObject so, TypeDefinition td, List<String> requestedIds, boolean fillOptionalPropertyData) {
        // build properties collection

        BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();
        Map<String, PropertyData<?>> properties = new HashMap<String, PropertyData<?>>();
        so.fillProperties(properties, objectFactory, requestedIds);

        String typeId = so.getTypeId();
        if (FilterParser.isContainedInFilter(PropertyIds.BASE_TYPE_ID, requestedIds)) {
            if (td == null) {
                log.warn("getPropertiesFromObject(), cannot get type definition, a type with id " + typeId
                        + " is unknown");
            } else {
                String baseTypeId = td.getBaseTypeId().value();
                properties.put(PropertyIds.BASE_TYPE_ID, objectFactory.createPropertyIdData(PropertyIds.BASE_TYPE_ID,
                        baseTypeId));
            }
        }
        List<PropertyData<?>> propertiesList = new ArrayList<PropertyData<?>>(properties.values());

        if (fillOptionalPropertyData) {  // add query name, local name, display name
            fillOptionalPropertyData(td, propertiesList);
        }

        Properties props = objectFactory.createPropertiesData(propertiesList);
        return props;
    }

    public static Properties getPropertiesFromObject(StoredObject so, TypeDefinition td,
            Map<String, String> requestedIds, Map<String, String> requestedFuncs) {
        // build properties collection

        List<String> idList = new ArrayList<String>(requestedIds.keySet());
        BindingsObjectFactory objectFactory = new BindingsObjectFactoryImpl();
        Map<String, PropertyData<?>> properties = new HashMap<String, PropertyData<?>>();
        so.fillProperties(properties, objectFactory, idList);

        String typeId = so.getTypeId();
        if (FilterParser.isContainedInFilter(PropertyIds.BASE_TYPE_ID, idList)) {
            if (td == null) {
                log.warn("getPropertiesFromObject(), cannot get type definition, a type with id " + typeId
                        + " is unknown");
            } else {
                String baseTypeId = td.getBaseTypeId().value();
                properties.put(PropertyIds.BASE_TYPE_ID, objectFactory.createPropertyIdData(PropertyIds.BASE_TYPE_ID,
                        baseTypeId));
            }
        }


        Map<String, PropertyData<?>> mappedProperties = new HashMap<String, PropertyData<?>>();
        if (requestedIds.containsKey("*")) {
            for (Map.Entry<String, PropertyData<?>> prop : properties.entrySet()) {
                // map property id to property query name
                String queryName = td.getPropertyDefinitions().get(prop.getKey()).getQueryName();
                String localName = td.getPropertyDefinitions().get(prop.getKey()).getLocalName();
                String displayName = td.getPropertyDefinitions().get(prop.getKey()).getDisplayName();
                AbstractPropertyData<?> ad = (AbstractPropertyData<?>) prop.getValue(); // a bit dirty
                ad.setQueryName(queryName);
                ad.setLocalName(localName);
                ad.setDisplayName(displayName);
                mappedProperties.put(queryName, prop.getValue());
            }
        } else {
            // replace all ids with query names or alias:
            for (Map.Entry<String, PropertyData<?>> prop : properties.entrySet()) {
                String queryNameOrAlias = requestedIds.get(prop.getKey());
                String localName = td.getPropertyDefinitions().get(prop.getKey()).getLocalName();
                String displayName = td.getPropertyDefinitions().get(prop.getKey()).getDisplayName();
                AbstractPropertyData<?> ad = (AbstractPropertyData<?>) prop.getValue(); // a bit dirty
                ad.setQueryName(queryNameOrAlias);
                ad.setLocalName(localName);
                ad.setDisplayName(displayName);
                mappedProperties.put(queryNameOrAlias, prop.getValue());
            }
        }
        // add functions:
        BindingsObjectFactory objFactory = new BindingsObjectFactoryImpl();
        for (Entry<String, String> funcEntry : requestedFuncs.entrySet()) {
            PropertyInteger pi = objFactory.createPropertyIntegerData(funcEntry.getKey(), BigInteger.valueOf(100));
              // fixed dummy value
            mappedProperties.put(funcEntry.getValue(), pi);
        }

        Properties props = new PropertiesImpl(mappedProperties.values());
        return props;
    }

   public static ObjectData getObjectData(TypeDefinition typeDef, StoredObject so, String filter, String user,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePolicyIds, Boolean includeACL, ExtensionsData extension) {

        ObjectDataImpl od = new ObjectDataImpl();

        if (so == null) {
            throw new CmisObjectNotFoundException("Illegal object id: null");
        }

        // build properties collection
        List<String> requestedIds = FilterParser.getRequestedIdsFromFilter(filter);
        Properties props = getPropertiesFromObject(so, typeDef, requestedIds, true);

        // fill output object
        if (null != includeAllowableActions && includeAllowableActions) {
        	AllowableActions allowableActions = so.getAllowableActions(user);
            od.setAllowableActions(allowableActions);
        }
        
        if (null != includeACL && includeACL) {
            od.setAcl(so.getAcl());
        }
        od.setIsExactAcl(true);

        if (null != includePolicyIds && includePolicyIds) {
            od.setPolicyIds(DataObjectCreator.fillPolicyIds(so));
        }

        if (null != includeRelationships && includeRelationships != IncludeRelationships.NONE) {
            od.setRelationships(DataObjectCreator.fillRelationships(includeRelationships, so, user));
        }

        if (renditionFilter != null && renditionFilter.length() > 0) {
            od.setRenditions(DataObjectCreator.fillRenditions(so));
        }

        od.setProperties(props);

        // Note: do not set change event info for this call
        return od;
    }

    public static ObjectData getObjectDataQueryResult(TypeDefinition typeDef, StoredObject so, String user,
            Map<String, String> requestedProperties, Map<String, String> requestedFuncs,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter) {

        ObjectDataImpl od = new ObjectDataImpl();

        // build properties collection
        Properties props = getPropertiesFromObject(so, typeDef, requestedProperties, requestedFuncs);

        // fill output object
        if (null != includeAllowableActions && includeAllowableActions) {
        	 //     AllowableActions allowableActions = DataObjectCreator.fillAllowableActions(so, user);
        	AllowableActions allowableActions = so.getAllowableActions(user);
            od.setAllowableActions(allowableActions);
        }

        if (null != includeRelationships && includeRelationships != IncludeRelationships.NONE) {
            od.setRelationships(DataObjectCreator.fillRelationships(includeRelationships, so, user));
        }

        if (renditionFilter != null && renditionFilter.length() > 0) {
            od.setRenditions(DataObjectCreator.fillRenditions(so));
        }

        od.setProperties(props);

        return od;
    }

    // internal helpers
    private static void createStandardDefinition(AbstractPropertyDefinition<?> prop, String id, PropertyType propType,
            String displayName, Cardinality card) {

        if (!NameValidator.isValidId(id)) {
            if (!NameValidator.isValidId(id)) {
                throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
            }
        }

        prop.setId(id);
        if (displayName == null) {
            prop.setDisplayName("Sample " + prop.getId() + " boolean property");
        } else {
            prop.setDisplayName(displayName);
        }
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

    private static void fillOptionalPropertyData(TypeDefinition td, List<PropertyData<?>> properties) {
        for (PropertyData<?> pd : properties) {
            fillOptionalPropertyData(td, (AbstractPropertyData<?>) pd);
        }
    }

    private static void fillOptionalPropertyData(TypeDefinition td, AbstractPropertyData<?> property) {
        PropertyDefinition<?> pd = td.getPropertyDefinitions().get(property.getId());
        if (null != pd) {
            String displayName = pd.getDisplayName();
            String queryName = pd.getQueryName();
            String localName = pd.getLocalName();
            property.setDisplayName(displayName);
            property.setLocalName(localName);
            property.setQueryName(queryName);
        }
    }

}
