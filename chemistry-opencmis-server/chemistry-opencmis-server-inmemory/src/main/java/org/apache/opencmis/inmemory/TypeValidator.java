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
package org.apache.opencmis.inmemory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.commons.PropertyIds;
import org.apache.opencmis.commons.api.Choice;
import org.apache.opencmis.commons.api.DocumentTypeDefinition;
import org.apache.opencmis.commons.api.PropertyDecimalDefinition;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.api.PropertyIntegerDefinition;
import org.apache.opencmis.commons.api.PropertyStringDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.BaseObjectTypeIds;
import org.apache.opencmis.commons.enums.Cardinality;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.opencmis.commons.provider.PropertiesData;
import org.apache.opencmis.commons.provider.PropertyData;
import org.apache.opencmis.commons.provider.PropertyDecimalData;
import org.apache.opencmis.commons.provider.PropertyIntegerData;

/**
 * @author Jens
 */
public class TypeValidator {

  public static void validateRequiredSystemProperties(PropertiesData properties) {
    if (properties == null || properties.getProperties() == null)
      throw new RuntimeException("Cannot create object, no properties are given");

    if (!properties.getProperties().containsKey(PropertyIds.CMIS_OBJECT_TYPE_ID))
      throw new RuntimeException("Cannot create object, type id is missing");

  }
  
  private static boolean isMandatorySystemProperty(String propertyId) {
    // TODO Auto-generated method stub
    return propertyId.equals(PropertyIds.CMIS_OBJECT_TYPE_ID);
  }

  @SuppressWarnings("unchecked")
  static <T> PropertyValidator<T> createPropertyValidator(PropertyDefinition<?> propDef) {
    PropertyValidator<T> result = null;
    if (propDef instanceof PropertyIntegerDefinition) {
      result = (PropertyValidator<T>) new PropertyValidatorInteger();
    }
    else if (propDef instanceof PropertyDecimalDefinition) {
      result = (PropertyValidator<T>) new PropertyValidatorDecimal();
    }
    else if (propDef instanceof PropertyStringDefinition) {
      result = (PropertyValidator<T>) new PropertyValidatorString();
    }
    else {
      result = new PropertyValidator<T>();
    }
    return result;
  }

  /*
   * property validations: not readonly, all required are given, all are known in type
   * cardinality: no multi values for single value, def min max check for Integer and Decimal,
   * choices and in list Strings, max length set default value for omitted properties
   */
  static class PropertyValidator<T> {

    public void validate(PropertyDefinition<T> propDef, PropertyData<T> prop) {

      // check general constraints for all property types
      if (propDef.getCardinality() == Cardinality.SINGLE && prop.getValues().size() > 1)
        throw new CmisConstraintException("The property with id " + propDef.getId()
            + " is single valued, but multiple values are passed " + prop.getValues());

      if (propDef.getChoices() != null && propDef.getChoices().size() > 0) {
        validateChoices(propDef, prop);
      }
    }

    private void validateChoices(PropertyDefinition<T> propDef, PropertyData<T> prop) {
      boolean isAllowedValue = true;
      boolean hasMultiValueChoiceLists = false;
      for (Choice<?> allowedValue : propDef.getChoices()) {
        if (allowedValue.getValue() != null && allowedValue.getValue().size() > 1)
          hasMultiValueChoiceLists = true;
      }

      // check if value is in list
      if (hasMultiValueChoiceLists) {
        // do a complex check if this combination of actual values is allowed
        // check if value is in list
        isAllowedValue = false;
        List<?> actualValues = prop.getValues();
        for (Choice<?> allowedValue : propDef.getChoices()) {
          if (allowedValue.getValue().size() == actualValues.size()) {
            boolean listValuesAreEqual = true;
            Iterator<?> it = allowedValue.getValue().iterator();
            for (Object actualValue : actualValues) {
              if (!actualValue.equals(it.next())) {
                listValuesAreEqual = false;
                break;
              }
            }
            if (listValuesAreEqual) {
              isAllowedValue = true;
            }
          }

          if (isAllowedValue)
            break;
        }

      }
      else {
        List<T> allowedValues = getAllowedValues(propDef.getChoices());
        // do a simpler check if all values are choice elements

        for (Object actualValue : prop.getValues()) {
          if (!allowedValues.contains(actualValue)) {
            isAllowedValue = false;
            break;
          }
        }
      }

      if (!isAllowedValue)
        throw new CmisConstraintException("The property with id " + propDef.getId()
            + " has a fixed set of values. Value(s) " + prop.getValues() + " are not listed.");
    }
    
    /**
     * Calculate the list of allowed values for this property definition by recursively
     * collecting all choice values from property definition
     * 
     * @param propDef
     *          property definition
     * @return
     *    list of possible values in complete hierarchy
     */
    private List<T> getAllowedValues (List<Choice<T>> choices) {
      List<T> allowedValues = new ArrayList<T>(choices.size());
      for (Choice<T> choice : choices) {
        if (choice.getValue() != null)
          allowedValues.add(choice.getValue().get(0));
        if (choice.getChoice() != null) {
          List<Choice<T>> x = choice.getChoice();
          allowedValues.addAll(getAllowedValues(x));
        }
      }  
      return allowedValues;
    }
  }

  static class PropertyValidatorInteger extends PropertyValidator<BigInteger> {

    public void validate(PropertyDefinition<BigInteger> propDef, PropertyData<BigInteger> property) {

      super.validate(propDef, property);

      BigInteger propVal = ((PropertyIntegerData) property).getFirstValue();
      BigInteger minVal = ((PropertyIntegerDefinition) propDef).getMinValue();
      BigInteger maxVal = ((PropertyIntegerDefinition) propDef).getMaxValue();

      // check min and max
      if (minVal != null && propVal != null && propVal.compareTo(minVal) == -1) {
        throw new CmisConstraintException("For property with id " + propDef.getId() + " the value "
            + propVal + " is less than the minimum value " + minVal);
      }
      if (maxVal != null && propVal != null && propVal.compareTo(maxVal) == 1) {
        throw new CmisConstraintException("For property with id " + propDef.getId() + " the value "
            + propVal + " is bigger than the maximum value " + maxVal);
      }
    }
  }

  static class PropertyValidatorDecimal extends PropertyValidator<BigDecimal> {

    public void validate(PropertyDefinition<BigDecimal> propDef, PropertyData<BigDecimal> property) {

      super.validate(propDef, property);

      BigDecimal propVal = ((PropertyDecimalData) property).getFirstValue();
      BigDecimal minVal = ((PropertyDecimalDefinition) propDef).getMinValue();
      BigDecimal maxVal = ((PropertyDecimalDefinition) propDef).getMaxValue();

      // check min and max
      if (minVal != null && propVal != null && propVal.compareTo(minVal) == -1) {
        throw new CmisConstraintException("For property with id " + propDef.getId() + " the value "
            + propVal + " is less than the minimum value " + minVal);
      }
      if (maxVal != null && propVal != null && propVal.compareTo(maxVal) == 1) {
        throw new CmisConstraintException("For property with id " + propDef.getId() + " the value "
            + propVal + " is bigger than the maximum value " + maxVal);
      }
    }
  }

  static class PropertyValidatorString extends PropertyValidator<String> {

    public void validate(PropertyDefinition<String> propDef, PropertyData<String> property) {

      super.validate(propDef, property);

      long maxLen = ((PropertyStringDefinition) propDef).getMaxLength() == null ? -1
          : ((PropertyStringDefinition) propDef).getMaxLength().longValue();
      long len = ((PropertyData<String>) property).getFirstValue() == null ? -1
          : ((PropertyData<String>) property).getFirstValue().length();

      // check max length
      if (maxLen >= 0 && len >= 0 && maxLen < len) {
        throw new CmisConstraintException("For property with id " + propDef.getId()
            + " the length of " + len + "is bigger than the maximum allowed length  " + maxLen);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> void validateProperties(TypeDefinition typeDef, PropertiesData properties,
      boolean checkMandatory) {

    List<String> propDefsRequired = getMandatoryPropDefs(typeDef.getPropertyDefinitions());

    for (PropertyData<?> prop : properties.getProperties().values()) {
      String propertyId = prop.getId();
      BaseObjectTypeIds baseTypeId = typeDef.getBaseId();

      if (isSystemProperty(baseTypeId, propertyId))
        continue; // ignore system properties for validation

      // Check if all properties are known in the type
      if (!typeContainsProperty(typeDef, propertyId)) {
        throw new CmisConstraintException("Unknown property " + propertyId + " in type "
            + typeDef.getId());
      }

      // check that all mandatory attributes are present
      if (checkMandatory && propDefsRequired.contains(propertyId))
        propDefsRequired.remove(propertyId);

      // check all type specific constraints:
      PropertyDefinition<T> propDef = getPropertyDefinition(typeDef, propertyId);
      PropertyValidator<T> validator = createPropertyValidator(propDef);
      validator.validate(propDef, (PropertyData<T>) prop);
    }

    if (checkMandatory && !propDefsRequired.isEmpty())
      throw new CmisConstraintException("The following mandatory properties are missing: "
          + propDefsRequired);
  }
  
  public static void validateVersionStateForCreate(DocumentTypeDefinition typeDef, VersioningState verState) {
    if (null==verState)
      return;
    if (typeDef.isVersionable() && verState.equals(VersioningState.NONE) || 
        ! typeDef.isVersionable() && !verState.equals(VersioningState.NONE)) {
      throw new CmisConstraintException("The versioning state flag is imcompatible to the type definition.");
    }

  }
  public static void validateAllowedChildObjectTypes(TypeDefinition childTypeDef, List<String> allowedChildTypes) {
    
    if (null == allowedChildTypes)
      return; // all types are allowed
    
    for (String allowedChildType : allowedChildTypes ) {
      if (allowedChildType.equals(childTypeDef.getId()))
        return;
    }
    throw new RuntimeException("The requested type " + childTypeDef.getId() + " is not allowed in this folder");    
  }

  private static List<String> getMandatoryPropDefs(Map<String, PropertyDefinition<?>> propDefs) {
    List<String> res = new ArrayList<String>();
    if (null != propDefs) {
      for (PropertyDefinition<?> propDef : propDefs.values()) {
        if (propDef.isRequired() && !isMandatorySystemProperty(propDef.getId()) )
          res.add(propDef.getId());
      }
    }
    return res;
  }

  public static boolean typeContainsProperty(TypeDefinition typeDef, String propertyId) {

    Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
    if (null == propDefs)
      return false;
    
    PropertyDefinition<?> propDef = propDefs.get(propertyId);
    
    if (null == propDef)
      return false; // unknown property id in this type
    else
      return true;
  }

  @SuppressWarnings("unchecked")
  private static<T> PropertyDefinition<T> getPropertyDefinition(TypeDefinition typeDef,
      String propertyId) {

    Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
    if (null == propDefs)
      return null;
    
    PropertyDefinition<?> propDef = propDefs.get(propertyId);
    
    if (null == propDef)
      return null; // not found
    else
      return (PropertyDefinition<T>)propDef;
  }

  private static boolean isSystemProperty(BaseObjectTypeIds baseTypeId, String propertyId) {

    if (propertyId.equals(PropertyIds.CMIS_NAME)) {
      return true;
    }
    else if (propertyId.equals(PropertyIds.CMIS_OBJECT_ID)) {
      return true;
    }
    else if (propertyId.equals(PropertyIds.CMIS_OBJECT_TYPE_ID)) {
      return true;
    }
    else if (propertyId.equals(PropertyIds.CMIS_BASE_TYPE_ID)) {
      return true;
    }
    else if (propertyId.equals(PropertyIds.CMIS_CREATED_BY)) {
      return true;
    }
    else if (propertyId.equals(PropertyIds.CMIS_CREATION_DATE)) {
      return true;
    }
    else if (propertyId.equals(PropertyIds.CMIS_LAST_MODIFIED_BY)) {
      return true;
    }
    else if (propertyId.equals(PropertyIds.CMIS_LAST_MODIFICATION_DATE)) {
      return true;
    }
    else if (propertyId.equals(PropertyIds.CMIS_CHANGE_TOKEN)) {
      return true;
    }

    if (baseTypeId.equals(BaseObjectTypeIds.CMIS_DOCUMENT)) {
      if (propertyId.equals(PropertyIds.CMIS_IS_IMMUTABLE)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_IS_LATEST_VERSION)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_IS_MAJOR_VERSION)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_VERSION_SERIES_ID)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_IS_LATEST_MAJOR_VERSION)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_VERSION_LABEL)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_VERSION_SERIES_ID)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_IS_VERSION_SERIES_CHECKED_OUT)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_BY)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_VERSION_SERIES_CHECKED_OUT_ID)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_CHECKIN_COMMENT)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_CONTENT_STREAM_LENGTH)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_CONTENT_STREAM_MIME_TYPE)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_CONTENT_STREAM_FILE_NAME)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_CONTENT_STREAM_ID)) {
        return true;
      }
      else {
        return false;
      }
    }
    else if (baseTypeId.equals(BaseObjectTypeIds.CMIS_FOLDER)) {
      if (propertyId.equals(PropertyIds.CMIS_PARENT_ID)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_ALLOWED_CHILD_OBJECT_TYPE_IDS)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_PATH)) {
        return true;
      }
      else {
        return false;
      }
    }
    else if (baseTypeId.equals(BaseObjectTypeIds.CMIS_POLICY)) {
      if (propertyId.equals(PropertyIds.CMIS_SOURCE_ID)) {
        return true;
      }
      else if (propertyId.equals(PropertyIds.CMIS_TARGET_ID)) {
        return true;
      }
      else {
        return false;
      }
    }
    else { // relationship
      if (propertyId.equals(PropertyIds.CMIS_POLICY_TEXT)) {
        return true;
      }
      else {
        return false;
      }
    }
  }
}
