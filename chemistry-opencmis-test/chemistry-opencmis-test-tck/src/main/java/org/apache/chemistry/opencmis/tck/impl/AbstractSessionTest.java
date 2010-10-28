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
package org.apache.chemistry.opencmis.tck.impl;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.OK;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.UNEXPECTED_EXCEPTION;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;

/**
 * Base class for tests that require an OpenCMIS session.
 */
public abstract class AbstractSessionTest extends AbstractCmisTest {

    private SessionFactory factory = SessionFactoryImpl.newInstance();

    public BindingType getBinding() {
        if (getParameters() == null) {
            return null;
        }

        try {
            return BindingType.fromValue(getParameters().get(SessionParameter.BINDING_TYPE));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return super.getName() + " (" + getBinding() + ")";
    }

    @Override
    public void run() throws Exception {
        Session session;

        Map<String, String> parameters = getParameters();
        if (parameters.containsKey(SessionParameter.REPOSITORY_ID)) {
            session = factory.createSession(parameters);
        } else {
            session = factory.getRepositories(parameters).get(0).createSession();
        }

        try {
            run(session);
        } catch (Exception e) {
            if (!(e instanceof FatalTestException)) {
                addResult(createResult(UNEXPECTED_EXCEPTION, "Exception: " + e.getMessage(), e, true));
            }
        }
    }

    public abstract void run(Session session) throws Exception;

    protected RepositoryInfo getRepositoryInfo(Session session) {
        RepositoryInfo ri = session.getRepositoryInfo();

        CmisTestResult failure = createResult(FAILURE, "Repository info is null!", true);
        addResult(assertNotNull(ri, null, failure));

        return ri;
    }

    // --- reusable checks ----

    protected CmisTestResult checkObject(CmisObject object, String[] properties, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        f = createResult(FAILURE, "Object is null!", true);
        addResult(results, assertNotNull(object, null, f));

        if (object != null) {
            f = createResult(FAILURE, "Object id is not set!");
            addResult(results, assertStringNotEmpty(object.getId(), null, f));

            // properties
            for (String propId : properties) {
                Property<?> prop = object.getProperty(propId);

                // values of non-spec properties are not checked here
                PropertyCheckEnum propertyCheck = PropertyCheckEnum.NO_VALUE_CHECK;

                // known properties that are strings and must be set
                if (PropertyIds.OBJECT_ID.equals(propId) || PropertyIds.BASE_TYPE_ID.equals(propId)
                        || PropertyIds.OBJECT_TYPE_ID.equals(propId) || PropertyIds.CREATED_BY.equals(propId)
                        || PropertyIds.LAST_MODIFIED_BY.equals(propId) || PropertyIds.CHANGE_TOKEN.equals(propId)
                        || PropertyIds.PATH.equals(propId)) {
                    propertyCheck = PropertyCheckEnum.STRING_MUST_NOT_BE_EMPTY;
                }

                // known properties that are strings and should be set
                if (PropertyIds.NAME.equals(propId) || PropertyIds.SOURCE_ID.equals(propId)
                        || PropertyIds.TARGET_ID.equals(propId)) {
                    propertyCheck = PropertyCheckEnum.STRING_SHOULD_NOT_BE_EMPTY;
                }

                // known properties that are not strings and must be set
                if (PropertyIds.CREATION_DATE.equals(propId) || PropertyIds.LAST_MODIFICATION_DATE.equals(propId)
                        || PropertyIds.IS_IMMUTABLE.equals(propId)) {
                    propertyCheck = PropertyCheckEnum.MUST_BE_SET;
                }

                // special cases
                if (PropertyIds.PARENT_ID.equals(propId)) {
                    if (object instanceof Folder) {
                        if (((Folder) object).isRootFolder()) {
                            propertyCheck = PropertyCheckEnum.MUST_NOT_BE_SET;
                        } else {
                            propertyCheck = PropertyCheckEnum.STRING_MUST_NOT_BE_EMPTY;
                        }
                    } else {
                        addResult(
                                results,
                                createResult(FAILURE, "Property " + PropertyIds.PARENT_ID
                                        + " is only defined for folders!"));
                    }
                }

                // check property
                addResult(results, checkProperty(prop, "Property " + propId, propertyCheck));
            }

            // allowable actions
            f = createResult(FAILURE, "Object has no CAN_GET_PROPERTIES allowable action!");
            addResult(results, assertNotAllowableAction(object, Action.CAN_GET_PROPERTIES, null, f));
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected CmisTestResult assertAllowableAction(CmisObject object, Action action, CmisTestResult success,
            CmisTestResult failure) {
        if ((object.getAllowableActions() != null) && (object.getAllowableActions().getAllowableActions() != null)) {
            if (object.getAllowableActions().getAllowableActions().contains(action)) {
                return success;
            }
        }

        return failure;
    }

    protected CmisTestResult assertNotAllowableAction(CmisObject object, Action action, CmisTestResult success,
            CmisTestResult failure) {
        if ((object.getAllowableActions() != null) && (object.getAllowableActions().getAllowableActions() != null)) {
            if (!object.getAllowableActions().getAllowableActions().contains(action)) {
                return success;
            }
        }

        return failure;
    }

    protected CmisTestResult checkProperty(Property<?> property, String message, PropertyCheckEnum propertyCheck) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        f = createResult(FAILURE, "Property is null!");
        addResult(results, assertNotNull(property, null, f));

        if (property != null) {
            f = createResult(FAILURE, "Property id is not set or empty!");
            addResult(results, assertStringNotEmpty(property.getId(), null, f));

            f = createResult(WARNING, "Display name is not set!");
            addResult(results, assertNotNull(property.getDisplayName(), null, f));

            f = createResult(WARNING, "Query name is not set!");
            addResult(results, assertNotNull(property.getQueryName(), null, f));

            f = createResult(WARNING, "Local name is not set!");
            addResult(results, assertNotNull(property.getLocalName(), null, f));

            if ((propertyCheck == PropertyCheckEnum.MUST_BE_SET)
                    || (propertyCheck == PropertyCheckEnum.STRING_MUST_NOT_BE_EMPTY)
                    || (propertyCheck == PropertyCheckEnum.STRING_SHOULD_NOT_BE_EMPTY)) {
                f = createResult(FAILURE, "Property has no value!");
                addResult(results, assertIsTrue(property.getValues().size() > 0, null, f));
            } else if (propertyCheck == PropertyCheckEnum.MUST_NOT_BE_SET) {
                f = createResult(FAILURE, "Property has a value!");
                addResult(results, assertIsTrue(property.getValues().size() == 0, null, f));
            }

            boolean isString = ((property.getDefinition().getPropertyType() == PropertyType.STRING)
                    || (property.getDefinition().getPropertyType() == PropertyType.ID)
                    || (property.getDefinition().getPropertyType() == PropertyType.URI) || (property.getDefinition()
                    .getPropertyType() == PropertyType.HTML));
            for (Object value : property.getValues()) {
                if (value == null) {
                    addResult(results, createResult(FAILURE, "Property values contain a null value!"));
                    break;
                } else if (isString) {
                    if (propertyCheck == PropertyCheckEnum.STRING_MUST_NOT_BE_EMPTY) {
                        f = createResult(FAILURE, "Property values contain an empty string!");
                        addResult(results, assertStringNotEmpty(value.toString(), null, f));
                    } else if (propertyCheck == PropertyCheckEnum.STRING_SHOULD_NOT_BE_EMPTY) {
                        f = createResult(WARNING, "Property values contain an empty string!");
                        addResult(results, assertStringNotEmpty(value.toString(), null, f));
                    }
                }
            }

            if (property.getDefinition().getCardinality() == Cardinality.SINGLE) {
                f = createResult(FAILURE, "Property cardinality is SINGLE but property has more than one value!");
                addResult(results, assertIsTrue(property.getValues().size() <= 1, null, f));
            }

            if (property.getDefinition().isRequired() == null) {
                addResult(results, createResult(FAILURE, "Property definition doesn't contain the required flag!!"));
            } else {
                if (property.getDefinition().isRequired().booleanValue()) {
                    f = createResult(FAILURE, "Property is required but has no value!");
                    addResult(results, assertIsTrue(property.getValues().size() > 0, null, f));
                }
            }
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected CmisTestResult checkChildren(Folder folder, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        f = createResult(FAILURE, "Folder is null!");
        addResult(results, assertNotNull(folder, null, f));

        if (folder != null) {
            OperationContext oc = new OperationContextImpl();
            oc.setFilterString("*");
            oc.setCacheEnabled(false);
            oc.setIncludeAllowableActions(true);

            for (CmisObject child : folder.getChildren(oc)) {
                if (child == null) {
                    addResult(results, createResult(FAILURE, "Folder contains a null child!"));
                } else {
                    String[] propertiesToCheck = new String[child.getType().getPropertyDefinitions().size()];

                    int i = 0;
                    for (String propId : child.getType().getPropertyDefinitions().keySet()) {
                        propertiesToCheck[i++] = propId;
                    }

                    addResult(results, checkObject(folder, propertiesToCheck, "Child check: " + child.getId()));

                    f = createResult(FAILURE, "Child is not fileable! Id: " + child.getId() + " / Type: "
                            + child.getType().getId());
                    addResult(results, assertIsTrue(child instanceof FileableCmisObject, null, f));

                    if (child instanceof FileableCmisObject) {
                        FileableCmisObject fileableChild = (FileableCmisObject) child;
                        List<Folder> parents = fileableChild.getParents();

                        f = createResult(FAILURE, "Child has no parents! Id: " + child.getId());
                        addResult(results, assertIsTrue(parents.size() > 0, null, f));

                        boolean foundParent = false;
                        for (Folder parent : parents) {
                            if (parent == null) {
                                f = createResult(FAILURE, "One of childs parents is null! Id: " + child.getId());
                                addResult(results, assertIsTrue(parents.size() > 0, null, f));
                            }

                            if (folder.getId().equals(parent.getId())) {
                                foundParent = true;
                                break;
                            }
                        }

                        if (!foundParent) {
                            f = createResult(FAILURE, "Folder is not found in childs parents! Id: " + child.getId());
                            addResult(results, assertIsTrue(parents.size() > 0, null, f));
                        }
                    }

                    f = createResult(FAILURE,
                            "Child has no CAN_GET_FOLDER_PARENT allowable action! Id: " + child.getId());
                    addResult(results, assertAllowableAction(folder, Action.CAN_GET_FOLDER_PARENT, null, f));
                }
            }
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected CmisTestResult checkTypeDefinition(TypeDefinition type, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        f = createResult(FAILURE, "Type is null!");
        addResult(results, assertNotNull(type, null, f));

        if (type != null) {
            f = createResult(FAILURE, "Type id is not set!");
            addResult(results, assertStringNotEmpty(type.getId(), null, f));

            f = createResult(FAILURE, "Base type id is not set!");
            addResult(results, assertNotNull(type.getBaseTypeId(), null, f));

            f = createResult(FAILURE, "Local name is not set!");
            addResult(results, assertStringNotEmpty(type.getLocalName(), null, f));

            f = createResult(FAILURE, "Local namespace is not set!");
            addResult(results, assertStringNotEmpty(type.getLocalNamespace(), null, f));

            f = createResult(FAILURE, "Query name is not set!");
            addResult(results, assertStringNotEmpty(type.getQueryName(), null, f));

            if ((type.getId() != null) && (type.getBaseTypeId() != null)) {
                if (type.getBaseTypeId().value().equals(type.getId())) {
                    f = createResult(FAILURE, "Base type has parent type!");
                    addResult(results, assertNull(type.getParentTypeId(), null, f));
                } else {
                    f = createResult(FAILURE, "Parent type is not set!");
                    addResult(results, assertStringNotEmpty(type.getParentTypeId(), null, f));
                }
            }

            f = createResult(FAILURE, "Creatable flag is not set!");
            addResult(results, assertNotNull(type.isCreatable(), null, f));

            f = createResult(FAILURE, "Fileable flag is not set!");
            addResult(results, assertNotNull(type.isFileable(), null, f));

            f = createResult(FAILURE, "Controllable ACL flag is not set!");
            addResult(results, assertNotNull(type.isControllableAcl(), null, f));

            f = createResult(FAILURE, "Controllable Policy flag is not set!");
            addResult(results, assertNotNull(type.isControllablePolicy(), null, f));

            f = createResult(FAILURE, "Fulltext indexed flag is not set!");
            addResult(results, assertNotNull(type.isFulltextIndexed(), null, f));

            f = createResult(FAILURE, "Included in super type flag is not set!");
            addResult(results, assertNotNull(type.isIncludedInSupertypeQuery(), null, f));

            f = createResult(FAILURE, "Queryable flag is not set!");
            addResult(results, assertNotNull(type.isQueryable(), null, f));

            f = createResult(WARNING, "Type display name is not set!");
            addResult(results, assertStringNotEmpty(type.getDisplayName(), null, f));

            f = createResult(WARNING, "Type description is not set!");
            addResult(results, assertStringNotEmpty(type.getDescription(), null, f));

            if (BaseTypeId.CMIS_DOCUMENT.equals(type.getBaseTypeId())) {
                // TODO
            } else if (BaseTypeId.CMIS_FOLDER.equals(type.getBaseTypeId())) {
                // TODO
            } else if (BaseTypeId.CMIS_RELATIONSHIP.equals(type.getBaseTypeId())) {
                // TODO
            } else if (BaseTypeId.CMIS_POLICY.equals(type.getBaseTypeId())) {
                // TODO
            }

            // TODO check base property definitions

            f = createResult(FAILURE, "Type has no property definitions!");
            addResult(results, assertNotNull(type.getPropertyDefinitions(), null, f));

            if (type.getPropertyDefinitions() != null) {
                for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
                    if (propDef == null) {
                        addResult(results, createResult(FAILURE, "A property definition is null!"));
                    } else if (propDef.getId() == null) {
                        addResult(results, createResult(FAILURE, "A property definition id is null!"));
                    } else {
                        addResult(results, checkPropertyDefinition(propDef, "Property definition: " + propDef.getId()));
                    }
                }
            }
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected CmisTestResult checkPropertyDefinition(PropertyDefinition<?> propDef, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        f = createResult(FAILURE, "Property definition is null!");
        addResult(results, assertNotNull(propDef, null, f));

        if (propDef != null) {
            // TODO
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    private void addResult(List<CmisTestResult> results, CmisTestResult result) {
        if (result != null) {
            if (result instanceof CmisTestResultImpl) {
                ((CmisTestResultImpl) result).setStackTrace(getStackTrace());
            }

            results.add(result);
            if (result.isFatal()) {
                throw new FatalTestException();
            }
        }
    }

    private CmisTestResultStatus getWorst(List<CmisTestResult> results) {
        if ((results == null) || (results.isEmpty())) {
            return CmisTestResultStatus.OK;
        }

        int max = 0;

        for (CmisTestResult result : results) {
            if (max < result.getStatus().getLevel()) {
                max = result.getStatus().getLevel();
            }
        }

        return CmisTestResultStatus.fromLevel(max);
    }
}
