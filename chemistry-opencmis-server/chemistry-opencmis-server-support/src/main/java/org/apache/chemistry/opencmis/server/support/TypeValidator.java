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
package org.apache.chemistry.opencmis.server.support;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.Choice;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDecimalDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyIntegerDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

/**
 * @author Jens
 */
public class TypeValidator {

    public static void validateRequiredSystemProperties(Properties properties) {
        if (properties == null || properties.getProperties() == null) {
            throw new CmisInvalidArgumentException("Cannot create object, no properties are given");
        }

        if (!properties.getProperties().containsKey(PropertyIds.OBJECT_TYPE_ID)) {
            throw new CmisInvalidArgumentException("Cannot create object, type id is missing");
        }

    }

    private static boolean isMandatorySystemProperty(String propertyId) {
        // TODO Auto-generated method stub
        return propertyId.equals(PropertyIds.OBJECT_TYPE_ID);
    }

    @SuppressWarnings("unchecked")
    static <T> PropertyValidator<T> createPropertyValidator(PropertyDefinition<?> propDef) {
        PropertyValidator<T> result = null;
        if (propDef instanceof PropertyIntegerDefinition) {
            result = (PropertyValidator<T>) new PropertyValidatorInteger();
        } else if (propDef instanceof PropertyDecimalDefinition) {
            result = (PropertyValidator<T>) new PropertyValidatorDecimal();
        } else if (propDef instanceof PropertyStringDefinition) {
            result = (PropertyValidator<T>) new PropertyValidatorString();
        } else {
            result = new PropertyValidator<T>();
        }
        return result;
    }

    /*
     * property validations: not readonly, all required are given, all are known
     * in type cardinality: no multi values for single value, def min max check
     * for Integer and Decimal, choices and in list Strings, max length set
     * default value for omitted properties
     */
    static class PropertyValidator<T> {

        public void validate(PropertyDefinition<T> propDef, PropertyData<T> prop) {

            // check general constraints for all property types
            if (propDef.getCardinality() == Cardinality.SINGLE && prop.getValues().size() > 1) {
                throw new CmisConstraintException("The property with id " + propDef.getId()
                        + " is single valued, but multiple values are passed " + prop.getValues());
            }

            if (propDef.getChoices() != null && propDef.getChoices().size() > 0) {
                validateChoices(propDef, prop);
            }
        }

        private void validateChoices(PropertyDefinition<T> propDef, PropertyData<T> prop) {
            boolean isAllowedValue = true;
            boolean hasMultiValueChoiceLists = false;
            for (Choice<?> allowedValue : propDef.getChoices()) {
                if (allowedValue.getValue() != null && allowedValue.getValue().size() > 1) {
                    hasMultiValueChoiceLists = true;
                }
            }

            // check if value is in list
            if (hasMultiValueChoiceLists) {
                // do a complex check if this combination of actual values is
                // allowed
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

                    if (isAllowedValue) {
                        break;
                    }
                }

            } else {
                List<T> allowedValues = getAllowedValues(propDef.getChoices());
                // do a simpler check if all values are choice elements

                for (Object actualValue : prop.getValues()) {
                    if (!allowedValues.contains(actualValue)) {
                        isAllowedValue = false;
                        break;
                    }
                }
            }

            if (!isAllowedValue) {
                throw new CmisConstraintException("The property with id " + propDef.getId()
                        + " has a fixed set of values. Value(s) " + prop.getValues() + " are not listed.");
            }
        }

        /**
         * Calculate the list of allowed values for this property definition by
         * recursively collecting all choice values from property definition
         *
         * @param propDef
         *            property definition
         * @return list of possible values in complete hierarchy
         */
        private List<T> getAllowedValues(List<Choice<T>> choices) {
            List<T> allowedValues = new ArrayList<T>(choices.size());
            for (Choice<T> choice : choices) {
                if (choice.getValue() != null) {
                    allowedValues.add(choice.getValue().get(0));
                }
                if (choice.getChoice() != null) {
                    List<Choice<T>> x = choice.getChoice();
                    allowedValues.addAll(getAllowedValues(x));
                }
            }
            return allowedValues;
        }
    }

    static class PropertyValidatorInteger extends PropertyValidator<BigInteger> {

        @Override
        public void validate(PropertyDefinition<BigInteger> propDef, PropertyData<BigInteger> property) {

            super.validate(propDef, property);

            BigInteger propVal = property.getFirstValue();
            BigInteger minVal = ((PropertyIntegerDefinition) propDef).getMinValue();
            BigInteger maxVal = ((PropertyIntegerDefinition) propDef).getMaxValue();

            // check min and max
            if (minVal != null && propVal != null && propVal.compareTo(minVal) == -1) {
                throw new CmisConstraintException("For property with id " + propDef.getId() + " the value " + propVal
                        + " is less than the minimum value " + minVal);
            }
            if (maxVal != null && propVal != null && propVal.compareTo(maxVal) == 1) {
                throw new CmisConstraintException("For property with id " + propDef.getId() + " the value " + propVal
                        + " is bigger than the maximum value " + maxVal);
            }
        }
    }

    static class PropertyValidatorDecimal extends PropertyValidator<BigDecimal> {

        @Override
        public void validate(PropertyDefinition<BigDecimal> propDef, PropertyData<BigDecimal> property) {

            super.validate(propDef, property);

            BigDecimal propVal = property.getFirstValue();
            BigDecimal minVal = ((PropertyDecimalDefinition) propDef).getMinValue();
            BigDecimal maxVal = ((PropertyDecimalDefinition) propDef).getMaxValue();

            // check min and max
            if (minVal != null && propVal != null && propVal.compareTo(minVal) == -1) {
                throw new CmisConstraintException("For property with id " + propDef.getId() + " the value " + propVal
                        + " is less than the minimum value " + minVal);
            }
            if (maxVal != null && propVal != null && propVal.compareTo(maxVal) == 1) {
                throw new CmisConstraintException("For property with id " + propDef.getId() + " the value " + propVal
                        + " is bigger than the maximum value " + maxVal);
            }
        }
    }

    static class PropertyValidatorString extends PropertyValidator<String> {

        @Override
        public void validate(PropertyDefinition<String> propDef, PropertyData<String> property) {

            super.validate(propDef, property);

            long maxLen = ((PropertyStringDefinition) propDef).getMaxLength() == null ? -1
                    : ((PropertyStringDefinition) propDef).getMaxLength().longValue();
            long len = property.getFirstValue() == null ? -1
                    : property.getFirstValue().length();

            // check max length
            if (maxLen >= 0 && len >= 0 && maxLen < len) {
                throw new CmisConstraintException("For property with id " + propDef.getId() + " the length of " + len
                        + "is bigger than the maximum allowed length  " + maxLen);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> void validateProperties(TypeDefinition typeDef, Properties properties, boolean checkMandatory) {

        List<String> propDefsRequired = getMandatoryPropDefs(typeDef.getPropertyDefinitions());

        if(properties != null) {
			for (PropertyData<?> prop : properties.getProperties().values()) {
				String propertyId = prop.getId();
				BaseTypeId baseTypeId = typeDef.getBaseTypeId();

				// check that all mandatory attributes are present
				if (checkMandatory && propDefsRequired.contains(propertyId)) {
					propDefsRequired.remove(propertyId);
				}

				if (isSystemProperty(baseTypeId, propertyId)) {
					continue; // ignore system properties for validation
				}

				// Check if all properties are known in the type
				if (!typeContainsProperty(typeDef, propertyId)) {
					throw new CmisConstraintException("Unknown property "
							+ propertyId + " in type " + typeDef.getId());
				}

				// check all type specific constraints:
				PropertyDefinition<T> propDef = getPropertyDefinition(typeDef,
						propertyId);
				PropertyValidator<T> validator = createPropertyValidator(propDef);
				validator.validate(propDef, (PropertyData<T>) prop);
			}
        }

        if (checkMandatory && !propDefsRequired.isEmpty()) {
            throw new CmisConstraintException("The following mandatory properties are missing: " + propDefsRequired);
        }
    }

    public static void validateVersionStateForCreate(DocumentTypeDefinition typeDef, VersioningState verState) {
        if (null == verState) {
            return;
        }
        if (typeDef.isVersionable() && verState.equals(VersioningState.NONE) || !typeDef.isVersionable()
                && !verState.equals(VersioningState.NONE)) {
            throw new CmisConstraintException("The versioning state flag is imcompatible to the type definition.");
        }

    }

    public static void validateAllowedChildObjectTypes(TypeDefinition childTypeDef, List<String> allowedChildTypes) {

     	validateAllowedTypes(childTypeDef, allowedChildTypes, "in this folder");
    }
    
    public static void validateAllowedRelationshipTypes (RelationshipTypeDefinition relationshipTypeDef, TypeDefinition sourceTypeDef,
 		   TypeDefinition targetTypeDef)
    {
    	List<String> allowedSourceTypes = relationshipTypeDef.getAllowedSourceTypeIds();	
    	validateAllowedTypes(sourceTypeDef, allowedSourceTypes, " as source type in this relationship");
      	List<String> allowedTargetTypes = relationshipTypeDef.getAllowedTargetTypeIds();
        validateAllowedTypes(targetTypeDef, allowedTargetTypes, " as target type in this relationship");	
    }
    
    protected static void validateAllowedTypes(TypeDefinition typeDef, List<String> allowedTypes, String description)
    {
    	 if (null == allowedTypes || allowedTypes.size() == 0)
             return; // all types are allowed

         for (String allowedType : allowedTypes) {
             if (allowedType.equals(typeDef.getId()))
                 return;
         }
         throw new CmisConstraintException("The requested type " + typeDef.getId() + " is not allowed " + description);
     }
    	 
    public static void  validateAcl(TypeDefinition typeDef, Acl addACEs, Acl removeACEs)
    {
    	if (!typeDef.isControllableAcl() && (addACEs != null || removeACEs != null))
    	{
    		throw new CmisConstraintException("acl set for type: " + typeDef.getDisplayName() + " that is not controllableACL");
    	}
    }
    
    public static void validateContentAllowed(DocumentTypeDefinition typeDef, boolean hasContent) {
        ContentStreamAllowed contentAllowed = typeDef.getContentStreamAllowed();
        if (ContentStreamAllowed.REQUIRED == contentAllowed && !hasContent) {
            throw new CmisConstraintException("Type " + typeDef.getId() + " requires content but document has no content.");
        } else if (ContentStreamAllowed.NOTALLOWED == contentAllowed && hasContent) {
            throw new CmisConstraintException("Type " + typeDef.getId() + " does not allow content but document has content.");
        }
    }

    private static List<String> getMandatoryPropDefs(Map<String, PropertyDefinition<?>> propDefs) {
        List<String> res = new ArrayList<String>();
        if (null != propDefs) {
            for (PropertyDefinition<?> propDef : propDefs.values()) {
                if (propDef.isRequired() && !isMandatorySystemProperty(propDef.getId())) {
                    res.add(propDef.getId());
                }
            }
        }
        return res;
    }

    public static boolean typeContainsProperty(TypeDefinition typeDef, String propertyId) {

        Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
        if (null == propDefs) {
            return false;
        }

        PropertyDefinition<?> propDef = propDefs.get(propertyId);

        if (null == propDef) {
            return false; // unknown property id in this type
        } else {
            return true;
        }
    }

    public static boolean typeContainsPropertyWithQueryName(TypeDefinition typeDef, String propertyQueryName) {

        Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
        if (null == propDefs) {
            return false;
        }

        for (PropertyDefinition<?> propDef : propDefs.values()) {
            if (propDef.getQueryName().toLowerCase().equals(propertyQueryName.toLowerCase())) {
                return true;
            }
        }

        return false; // unknown property query name in this type
    }

    @SuppressWarnings("unchecked")
    private static <T> PropertyDefinition<T> getPropertyDefinition(TypeDefinition typeDef, String propertyId) {

        Map<String, PropertyDefinition<?>> propDefs = typeDef.getPropertyDefinitions();
        if (null == propDefs) {
            return null;
        }

        PropertyDefinition<?> propDef = propDefs.get(propertyId);

        if (null == propDef) {
            return null; // not found
        } else {
            return (PropertyDefinition<T>) propDef;
        }
    }

    private static boolean isSystemProperty(BaseTypeId baseTypeId, String propertyId) {

        if (propertyId.equals(PropertyIds.NAME)) {
            return true;
        } else if (propertyId.equals(PropertyIds.OBJECT_ID)) {
            return true;
        } else if (propertyId.equals(PropertyIds.OBJECT_TYPE_ID)) {
            return true;
        } else if (propertyId.equals(PropertyIds.BASE_TYPE_ID)) {
            return true;
        } else if (propertyId.equals(PropertyIds.CREATED_BY)) {
            return true;
        } else if (propertyId.equals(PropertyIds.CREATION_DATE)) {
            return true;
        } else if (propertyId.equals(PropertyIds.LAST_MODIFIED_BY)) {
            return true;
        } else if (propertyId.equals(PropertyIds.LAST_MODIFICATION_DATE)) {
            return true;
        } else if (propertyId.equals(PropertyIds.CHANGE_TOKEN)) {
            return true;
        }

        if (baseTypeId.equals(BaseTypeId.CMIS_DOCUMENT)) {
            if (propertyId.equals(PropertyIds.IS_IMMUTABLE)) {
                return true;
            } else if (propertyId.equals(PropertyIds.IS_LATEST_VERSION)) {
                return true;
            } else if (propertyId.equals(PropertyIds.IS_MAJOR_VERSION)) {
                return true;
            } else if (propertyId.equals(PropertyIds.VERSION_SERIES_ID)) {
                return true;
            } else if (propertyId.equals(PropertyIds.IS_LATEST_MAJOR_VERSION)) {
                return true;
            } else if (propertyId.equals(PropertyIds.VERSION_LABEL)) {
                return true;
            } else if (propertyId.equals(PropertyIds.VERSION_SERIES_ID)) {
                return true;
            } else if (propertyId.equals(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT)) {
                return true;
            } else if (propertyId.equals(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY)) {
                return true;
            } else if (propertyId.equals(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID)) {
                return true;
            } else if (propertyId.equals(PropertyIds.CHECKIN_COMMENT)) {
                return true;
            } else if (propertyId.equals(PropertyIds.CONTENT_STREAM_LENGTH)) {
                return true;
            } else if (propertyId.equals(PropertyIds.CONTENT_STREAM_MIME_TYPE)) {
                return true;
            } else if (propertyId.equals(PropertyIds.CONTENT_STREAM_FILE_NAME)) {
                return true;
            } else if (propertyId.equals(PropertyIds.CONTENT_STREAM_ID)) {
                return true;
            } else {
                return false;
            }
        } else if (baseTypeId.equals(BaseTypeId.CMIS_FOLDER)) {
            if (propertyId.equals(PropertyIds.PARENT_ID)) {
                return true;
            } else if (propertyId.equals(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS)) {
                return true;
            } else if (propertyId.equals(PropertyIds.PATH)) {
                return true;
            } else {
                return false;
            }
        } else if (baseTypeId.equals(BaseTypeId.CMIS_POLICY)) {
            if (propertyId.equals(PropertyIds.SOURCE_ID)) {
                return true;
            } else if (propertyId.equals(PropertyIds.TARGET_ID)) {
                return true;
            } else {
                return false;
            }
        } else { // relationship
            if (propertyId.equals(PropertyIds.POLICY_TEXT)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
