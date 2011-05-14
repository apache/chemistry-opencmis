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
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.SKIPPED;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.UNEXPECTED_EXCEPTION;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.RelationshipTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;

/**
 * Base class for tests that require an OpenCMIS session.
 */
public abstract class AbstractSessionTest extends AbstractCmisTest {

    public static final OperationContext SELECT_ALL_NO_CACHE_OC = new OperationContextImpl();
    static {
        SELECT_ALL_NO_CACHE_OC.setFilterString("*");
        SELECT_ALL_NO_CACHE_OC.setCacheEnabled(false);
        SELECT_ALL_NO_CACHE_OC.setIncludeAllowableActions(true);
        SELECT_ALL_NO_CACHE_OC.setIncludeAcls(true);
        SELECT_ALL_NO_CACHE_OC.setIncludePathSegments(true);
        SELECT_ALL_NO_CACHE_OC.setIncludePolicies(true);
        SELECT_ALL_NO_CACHE_OC.setRenditionFilterString("*");
    }

    private final SessionFactory factory = SessionFactoryImpl.newInstance();
    private Folder testFolder;

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

        // switch off the cache
        session.getDefaultContext().setCacheEnabled(false);

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

    // --- handy create and delete methods ---

    /**
     * Creates a folder.
     */
    protected Folder createFolder(Folder parent, String name) {
        String objectTypeId = getParameters().get(TestParameters.DEFAULT_FOLDER_TYPE);
        if (objectTypeId == null) {
            objectTypeId = TestParameters.DEFAULT_FOLDER_TYPE_VALUE;
        }

        return createFolder(parent, name, objectTypeId);
    }

    /**
     * Creates a folder.
     */
    protected Folder createFolder(Folder parent, String name, String objectTypeId) {
        Folder result = null;

        CmisTestResult f;

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);

        try {
            // create the folder
            result = parent.createFolder(properties);
        } catch (CmisBaseException e) {
            addResult(createResult(UNEXPECTED_EXCEPTION, "Folder could not be created! Exception: " + e.getMessage(),
                    e, true));
        }

        try {
            // check the new folder
            String[] propertiesToCheck = new String[result.getType().getPropertyDefinitions().size()];

            int i = 0;
            for (String propId : result.getType().getPropertyDefinitions().keySet()) {
                propertiesToCheck[i++] = propId;
            }

            addResult(checkObject(result, propertiesToCheck, "New folder object spec compliance"));

            // check object parents
            List<Folder> objectParents = result.getParents();

            f = createResult(FAILURE, "Newly created folder has no or more than one parent! Id: " + result.getId(),
                    true);
            addResult(assertEquals(1, objectParents.size(), null, f));

            f = createResult(FAILURE, "First object parent of the newly created folder does not match parent! Id: "
                    + result.getId(), true);
            assertShallowEquals(parent, objectParents.get(0), null, f);

            // check folder parent
            Folder folderParent = result.getFolderParent();
            f = createResult(FAILURE, "Newly created folder has no folder parent! Id: " + result.getId(), true);
            addResult(assertNotNull(folderParent, null, f));

            f = createResult(FAILURE,
                    "Folder parent of the newly created folder does not match parent! Id: " + result.getId(), true);
            assertShallowEquals(parent, folderParent, null, f);

            // check children of parent
            boolean found = false;
            for (CmisObject child : parent.getChildren()) {
                if (child == null) {
                    addResult(createResult(FAILURE, "Parent folder contains a null child!", true));
                } else {
                    if (result.getId().equals(child.getId())) {
                        found = true;

                        f = createResult(FAILURE, "Folder and parent child don't match! Id: " + result.getId(), true);
                        assertShallowEquals(result, child, null, f);
                        break;
                    }
                }
            }

            if (!found) {
                addResult(createResult(FAILURE, "Folder is not a child of the parent folder! Id: " + result.getId(),
                        true));
            }
        } catch (CmisBaseException e) {
            addResult(createResult(UNEXPECTED_EXCEPTION,
                    "Newly created folder is invalid! Exception: " + e.getMessage(), e, true));
        }

        return result;
    }

    /**
     * Deletes an object and checks if it is deleted.
     */
    protected void deleteObject(CmisObject object) {
        if (object != null) {
            if (object instanceof Folder) {
                try {
                    ((Folder) object).deleteTree(true, null, true);
                } catch (CmisBaseException e) {
                    addResult(createResult(UNEXPECTED_EXCEPTION,
                            "Folder could not be deleted! Exception: " + e.getMessage(), e, true));
                }
            } else {
                try {
                    object.delete(true);
                } catch (CmisBaseException e) {
                    addResult(createResult(UNEXPECTED_EXCEPTION,
                            "Object could not be deleted! Exception: " + e.getMessage(), e, true));
                }
            }

            CmisTestResult f = createResult(FAILURE, "Object should not exist anymore but it is still there! Id: "
                    + object.getId(), true);
            addResult(assertIsFalse(exists(object), null, f));
        }
    }

    /**
     * Tests if an object exists by refreshing it.
     */
    protected boolean exists(CmisObject object) {
        try {
            object.refresh();
            return true;
        } catch (CmisObjectNotFoundException e) {
            return false;
        }
    }

    // --- test folder methods ---

    /**
     * Creates a test folder.
     */
    protected Folder createTestFolder(Session session) {

        String testFolderParentPath = getParameters().get(TestParameters.DEFAULT_TEST_FOLDER_PARENT);
        if (testFolderParentPath == null) {
            testFolderParentPath = TestParameters.DEFAULT_TEST_FOLDER_PARENT_VALUE;
        }

        String name = "cmistck" + System.currentTimeMillis() + session.getRepositoryInfo().hashCode();

        Folder parent = null;
        try {
            CmisObject parentObject = session.getObjectByPath(testFolderParentPath);
            if (!(parentObject instanceof Folder)) {
                addResult(createResult(FAILURE, "Parent folder of the test folder is actually not a folder! Path: "
                        + testFolderParentPath, true));
            }

            parent = (Folder) parentObject;
        } catch (CmisBaseException e) {
            addResult(createResult(UNEXPECTED_EXCEPTION,
                    "Test folder could not be created! Exception: " + e.getMessage(), e, true));
        }

        testFolder = createFolder(parent, name);

        return testFolder;
    }

    /**
     * Get the test folder.
     */
    protected Folder getTestFolder() {
        return testFolder;
    }

    /**
     * Delete the test folder.
     */
    protected void deleteTestFolder() {
        deleteObject(testFolder);
    }

    // --- reusable checks ----

    protected boolean isGetDescendantsSupported(Session session) {
        RepositoryCapabilities cap = session.getRepositoryInfo().getCapabilities();

        if (cap == null) {
            return false;
        }

        if (cap.isGetDescendantsSupported() == null) {
            return false;
        }

        return cap.isGetDescendantsSupported().booleanValue();
    }

    protected boolean isGetFolderTreeSupported(Session session) {
        RepositoryCapabilities cap = session.getRepositoryInfo().getCapabilities();

        if (cap == null) {
            return false;
        }

        if (cap.isGetFolderTreeSupported() == null) {
            return false;
        }

        return cap.isGetFolderTreeSupported().booleanValue();
    }

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
                        || PropertyIds.LAST_MODIFIED_BY.equals(propId) || PropertyIds.PATH.equals(propId)
                        || PropertyIds.SOURCE_ID.equals(propId) || PropertyIds.TARGET_ID.equals(propId)
                        || PropertyIds.POLICY_TEXT.equals(propId)) {
                    propertyCheck = PropertyCheckEnum.STRING_MUST_NOT_BE_EMPTY;
                }

                // known properties that are strings and should be set
                if (PropertyIds.NAME.equals(propId)) {
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
            if ((object.getAllowableActions() == null) || (object.getAllowableActions().getAllowableActions() == null)) {
                addResult(results, createResult(FAILURE, "Object has no allowable actions!"));
            } else {
                Set<Action> actions = object.getAllowableActions().getAllowableActions();

                f = createResult(FAILURE, "Object has no CAN_GET_PROPERTIES allowable action!");
                addResult(results, assertAllowableAction(object, Action.CAN_GET_PROPERTIES, null, f));

                if (object instanceof Document) {
                    if (actions.contains(Action.CAN_CHECK_OUT) && actions.contains(Action.CAN_CHECK_IN)) {
                        addResult(
                                results,
                                createResult(FAILURE,
                                        "Document object has CAN_CHECK_OUT and CAN_CHECK_IN allowable actions!"));
                    }

                    if (actions.contains(Action.CAN_CHECK_OUT) && actions.contains(Action.CAN_CANCEL_CHECK_OUT)) {
                        addResult(
                                results,
                                createResult(FAILURE,
                                        "Document object has CAN_CHECK_OUT and CAN_CANCEL_CHECK_OUT allowable actions!"));
                    }

                    Document doc = (Document) object;
                    if (doc.isVersionSeriesCheckedOut() != null) {
                        if (doc.isVersionSeriesCheckedOut().booleanValue()) {
                            f = createResult(WARNING, "Document is checked out and has CAN_CHECK_OUT allowable action!");
                            addResult(results, assertNotAllowableAction(object, Action.CAN_CHECK_OUT, null, f));

                            if (doc.getVersionSeriesCheckedOutId() == null) {
                                addResult(
                                        results,
                                        createResult(WARNING,
                                                "Document is checked out and but the property cmis:versionSeriesCheckedOutId is not set!"));
                            } else {
                                if (doc.getVersionSeriesCheckedOutId().equals(object.getId())) {
                                    // object is PWC
                                    f = createResult(FAILURE, "PWC doesn't have CAN_CHECK_IN allowable action!");
                                    addResult(results, assertAllowableAction(object, Action.CAN_CHECK_IN, null, f));

                                    f = createResult(FAILURE, "PWC doesn't have CAN_CANCEL_CHECK_OUT allowable action!");
                                    addResult(results,
                                            assertAllowableAction(object, Action.CAN_CANCEL_CHECK_OUT, null, f));
                                } else {
                                    // object is not PWC
                                    f = createResult(WARNING, "Non-PWC has CAN_CHECK_IN allowable action!");
                                    addResult(results, assertNotAllowableAction(object, Action.CAN_CHECK_IN, null, f));

                                    f = createResult(WARNING, "Non-PWC has CAN_CANCEL_CHECK_OUT allowable action!");
                                    addResult(results,
                                            assertNotAllowableAction(object, Action.CAN_CANCEL_CHECK_OUT, null, f));
                                }
                            }
                        } else {
                            f = createResult(FAILURE,
                                    "Document is not checked out and has CAN_CHECK_IN allowable action!");
                            addResult(results, assertNotAllowableAction(object, Action.CAN_CHECK_IN, null, f));

                            f = createResult(FAILURE,
                                    "Document is not checked out and has CAN_CANCEL_CHECK_OUT allowable action!");
                            addResult(results, assertNotAllowableAction(object, Action.CAN_CANCEL_CHECK_OUT, null, f));
                        }
                    } else {
                        addResult(results, createResult(FAILURE, "Property cmis:isVersionSeriesCheckedOut is not set!"));
                    }
                } else {
                    f = createResult(FAILURE, "Non-Document object has CAN_CHECK_IN allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_CHECK_IN, null, f));

                    f = createResult(FAILURE, "Non-Document object has CAN_CHECK_OUT allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_CHECK_OUT, null, f));

                    f = createResult(FAILURE, "Non-Document object has CAN_CANCEL_CHECK_OUT allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_CANCEL_CHECK_OUT, null, f));

                    f = createResult(FAILURE, "Non-Document object has CAN_GET_CONTENT_STREAM allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_GET_CONTENT_STREAM, null, f));

                    f = createResult(FAILURE, "Non-Document object has CAN_DELETE_CONTENT_STREAM allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_DELETE_CONTENT_STREAM, null, f));

                    f = createResult(FAILURE, "Non-Document object has CAN_GET_ALL_VERSIONS allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_GET_ALL_VERSIONS, null, f));
                }

                if (object instanceof Folder) {
                    Folder folder = (Folder) object;
                    if (folder.isRootFolder()) {
                        f = createResult(FAILURE, "Root folder has CAN_DELETE_OBJECT allowable action!");
                        addResult(results, assertNotAllowableAction(object, Action.CAN_DELETE_OBJECT, null, f));

                        f = createResult(FAILURE, "Root folder has CAN_GET_FOLDER_PARENT allowable action!");
                        addResult(results, assertNotAllowableAction(object, Action.CAN_GET_FOLDER_PARENT, null, f));
                    }
                } else {
                    f = createResult(FAILURE, "Non-Folder object has CAN_GET_DESCENDANTS allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_GET_DESCENDANTS, null, f));

                    f = createResult(FAILURE, "Non-Folder object has CAN_GET_FOLDER_PARENT allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_GET_FOLDER_PARENT, null, f));

                    f = createResult(FAILURE, "Non-Folder object has CAN_GET_CHILDREN allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_GET_CHILDREN, null, f));

                    f = createResult(FAILURE, "Non-Folder object has CAN_DELETE_TREE allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_DELETE_TREE, null, f));

                    f = createResult(FAILURE, "Non-Folder object has CAN_GET_FOLDER_PARENT allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_GET_FOLDER_PARENT, null, f));

                    f = createResult(FAILURE, "Non-Folder object has CAN_CREATE_DOCUMENT allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_CREATE_DOCUMENT, null, f));

                    f = createResult(FAILURE, "Non-Folder object has CAN_CREATE_FOLDER allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_CREATE_FOLDER, null, f));
                }

                if (!(object instanceof Document) && !(object instanceof Policy)) {
                    f = createResult(FAILURE,
                            "Non-Document/Policy object has CAN_ADD_OBJECT_TO_FOLDER allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_ADD_OBJECT_TO_FOLDER, null, f));

                    f = createResult(FAILURE,
                            "Non-Document/Policy object has CAN_REMOVE_OBJECT_FROM_FOLDER allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_REMOVE_OBJECT_FROM_FOLDER, null, f));
                }

                if (!(object instanceof FileableCmisObject)) {
                    f = createResult(FAILURE, "Non-Fileable object has CAN_MOVE_OBJECT allowable action!");
                    addResult(results, assertNotAllowableAction(object, Action.CAN_MOVE_OBJECT, null, f));
                }
            }
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

        f = createResult(FAILURE, "Property is not included in response!");
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

    protected CmisTestResult checkChildren(Session session, Folder folder, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if (folder == null) {
            return createResult(FAILURE, "Folder is null!");
        }

        // getChildren

        long childrenCount = 0;
        long childrenFolderCount = 0;
        ItemIterable<CmisObject> children = folder.getChildren(SELECT_ALL_NO_CACHE_OC);

        for (CmisObject child : children) {
            childrenCount++;
            if (child instanceof Folder) {
                childrenFolderCount++;
            }

            checkChild(results, folder, child);
        }

        f = createResult(WARNING, "Number of children doesn't match the reported total number of items!");
        addResult(results, assertEquals(childrenCount, children.getTotalNumItems(), null, f));

        // getDescendants

        if (isGetDescendantsSupported(session)) {
            long descendantsCount = 0;
            List<Tree<FileableCmisObject>> descendants = folder.getDescendants(1);

            for (Tree<FileableCmisObject> child : descendants) {
                descendantsCount++;

                if (child == null) {
                    addResult(results, createResult(FAILURE, "Folder descendants contain a null tree!"));
                } else {
                    checkChild(results, folder, child.getItem());
                }
            }

            f = createResult(FAILURE,
                    "Number of descendants doesn't match the number of children returned by getChildren!");
            addResult(results, assertEquals(childrenCount, descendantsCount, null, f));
        } else {
            addResult(results, createResult(SKIPPED, "getDescendants is not supported."));
        }

        // getFolderTree

        if (isGetFolderTreeSupported(session)) {
            long folderTreeCount = 0;
            List<Tree<FileableCmisObject>> folderTree = folder.getFolderTree(1);

            for (Tree<FileableCmisObject> child : folderTree) {
                folderTreeCount++;

                if (child == null) {
                    addResult(results, createResult(FAILURE, "Folder tree contains a null tree!"));
                } else {
                    checkChild(results, folder, child.getItem());
                }
            }

            f = createResult(FAILURE, "Number of folders doesn't match the number of folders returned by getChildren!");
            addResult(results, assertEquals(childrenFolderCount, folderTreeCount, null, f));
        } else {
            addResult(results, createResult(SKIPPED, "getFolderTree is not supported."));
        }

        // --- wrap up ---

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    private void checkChild(List<CmisTestResult> results, Folder folder, CmisObject child) {
        CmisTestResult f;

        if (child == null) {
            addResult(results, createResult(FAILURE, "Folder contains a null child!"));
        } else {
            String[] propertiesToCheck = new String[child.getType().getPropertyDefinitions().size()];

            int i = 0;
            for (String propId : child.getType().getPropertyDefinitions().keySet()) {
                propertiesToCheck[i++] = propId;
            }

            addResult(results, checkObject(child, propertiesToCheck, "Child check: " + child.getId()));

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

            f = createResult(FAILURE, "Child has no CAN_GET_OBJECT_PARENTS allowable action! Id: " + child.getId());
            addResult(results, assertAllowableAction(child, Action.CAN_GET_OBJECT_PARENTS, null, f));

            if (child instanceof Folder) {
                f = createResult(FAILURE, "Child has no CAN_GET_FOLDER_PARENT allowable action! Id: " + child.getId());
                addResult(results, assertAllowableAction(child, Action.CAN_GET_FOLDER_PARENT, null, f));
            }
        }
    }

    protected CmisTestResult assertShallowEquals(CmisObject expected, CmisObject actual, CmisTestResult success,
            CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected object is null, but actual object is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual object is null, but expected object is not!");
            addResultChild(failure, f);

            return failure;
        }

        f = createResult(FAILURE, "Ids don't match!");
        addResult(results, assertEquals(expected.getId(), actual.getId(), null, f));

        f = createResult(FAILURE, "Base types don't match!");
        addResult(results, assertEquals(expected.getBaseTypeId(), actual.getBaseTypeId(), null, f));

        f = createResult(FAILURE, "Types don't match!");
        addResult(results, assertEquals(expected.getType().getId(), actual.getType().getId(), null, f));

        if (getWorst(results).getLevel() <= OK.getLevel()) {
            for (CmisTestResult result : results) {
                addResultChild(success, result);
            }

            return success;
        } else {
            for (CmisTestResult result : results) {
                addResultChild(failure, result);
            }

            return failure;
        }
    }

    // --- type checks ---

    protected CmisTestResult checkTypeDefinition(Session session, TypeDefinition type, String message) {
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

            // f = createResult(FAILURE, "Local namespace is not set!");
            // addResult(results, assertStringNotEmpty(type.(), null, f));

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
                DocumentTypeDefinition docType = (DocumentTypeDefinition) type;

                f = createResult(FAILURE, "Versionable flag is not set!");
                addResult(results, assertNotNull(docType.isVersionable(), null, f));

                f = createResult(FAILURE, "Content stream allowed flag is not set!");
                addResult(results, assertNotNull(docType.getContentStreamAllowed(), null, f));
            } else if (BaseTypeId.CMIS_FOLDER.equals(type.getBaseTypeId())) {
                if (type.isFileable() != null) {
                    f = createResult(FAILURE, "Folder types must be fileable!");
                    addResult(results, assertIsTrue(type.isFileable(), null, f));
                }
            } else if (BaseTypeId.CMIS_RELATIONSHIP.equals(type.getBaseTypeId())) {
                RelationshipTypeDefinition relType = (RelationshipTypeDefinition) type;

                f = createResult(FAILURE, "Allowed Source Type Ids are not set!");
                addResult(results, assertNotNull(relType.getAllowedSourceTypeIds(), null, f));

                if (relType.getAllowedSourceTypeIds() != null) {
                    for (String typeId : relType.getAllowedSourceTypeIds()) {
                        try {
                            session.getTypeDefinition(typeId);
                        } catch (CmisObjectNotFoundException e) {
                            addResult(
                                    results,
                                    createResult(WARNING,
                                            "Allowed Source Type Ids contain a type id that doesn't exist: " + typeId));
                        }
                    }
                }

                f = createResult(FAILURE, "Allowed Target Type Ids are not set!");
                addResult(results, assertNotNull(relType.getAllowedTargetTypeIds(), null, f));

                if (relType.getAllowedTargetTypeIds() != null) {
                    for (String typeId : relType.getAllowedTargetTypeIds()) {
                        try {
                            session.getTypeDefinition(typeId);
                        } catch (CmisObjectNotFoundException e) {
                            addResult(
                                    results,
                                    createResult(WARNING,
                                            "Allowed Target Type Ids contain a type id that doesn't exist: " + typeId));
                        }
                    }
                }

                if (type.isFileable() != null) {
                    f = createResult(FAILURE, "Relationship types must not be fileable!");
                    addResult(results, assertIsFalse(type.isFileable(), null, f));
                }
            } else if (BaseTypeId.CMIS_POLICY.equals(type.getBaseTypeId())) {
                // nothing to do
            }

            // check properties
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

            CmisPropertyDefintion cpd;

            // cmis:name
            cpd = new CmisPropertyDefintion(PropertyIds.NAME, null, PropertyType.STRING, Cardinality.SINGLE, null,
                    null, null);
            addResult(results, cpd.check(type));

            // cmis:objectId
            cpd = new CmisPropertyDefintion(PropertyIds.OBJECT_ID, false, PropertyType.ID, Cardinality.SINGLE,
                    Updatability.READONLY, null, null);
            addResult(results, cpd.check(type));

            // cmis:baseTypeId
            cpd = new CmisPropertyDefintion(PropertyIds.BASE_TYPE_ID, false, PropertyType.ID, Cardinality.SINGLE,
                    Updatability.READONLY, null, null);
            addResult(results, cpd.check(type));

            // cmis:objectTypeId
            cpd = new CmisPropertyDefintion(PropertyIds.OBJECT_TYPE_ID, true, PropertyType.ID, Cardinality.SINGLE,
                    Updatability.ONCREATE, null, null);
            addResult(results, cpd.check(type));

            // cmis:createdBy
            cpd = new CmisPropertyDefintion(PropertyIds.CREATED_BY, false, PropertyType.STRING, Cardinality.SINGLE,
                    Updatability.READONLY, true, true);
            addResult(results, cpd.check(type));

            // cmis:creationDate
            cpd = new CmisPropertyDefintion(PropertyIds.CREATION_DATE, false, PropertyType.DATETIME,
                    Cardinality.SINGLE, Updatability.READONLY, true, true);
            addResult(results, cpd.check(type));

            // cmis:lastModifiedBy
            cpd = new CmisPropertyDefintion(PropertyIds.LAST_MODIFIED_BY, false, PropertyType.STRING,
                    Cardinality.SINGLE, Updatability.READONLY, true, true);
            addResult(results, cpd.check(type));

            // cmis:lastModificationDate
            cpd = new CmisPropertyDefintion(PropertyIds.LAST_MODIFICATION_DATE, false, PropertyType.DATETIME,
                    Cardinality.SINGLE, Updatability.READONLY, true, true);
            addResult(results, cpd.check(type));

            // cmis:changeToken
            cpd = new CmisPropertyDefintion(PropertyIds.CHANGE_TOKEN, false, PropertyType.STRING, Cardinality.SINGLE,
                    Updatability.READONLY, null, null);
            addResult(results, cpd.check(type));

            if (BaseTypeId.CMIS_DOCUMENT.equals(type.getBaseTypeId())) {
                // cmis:isImmutable
                cpd = new CmisPropertyDefintion(PropertyIds.IS_IMMUTABLE, false, PropertyType.BOOLEAN,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:isLatestVersion
                cpd = new CmisPropertyDefintion(PropertyIds.IS_LATEST_VERSION, false, PropertyType.BOOLEAN,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:isMajorVersion
                cpd = new CmisPropertyDefintion(PropertyIds.IS_MAJOR_VERSION, false, PropertyType.BOOLEAN,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:isLatestMajorVersion
                cpd = new CmisPropertyDefintion(PropertyIds.IS_LATEST_MAJOR_VERSION, false, PropertyType.BOOLEAN,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:versionLabel
                cpd = new CmisPropertyDefintion(PropertyIds.VERSION_LABEL, false, PropertyType.STRING,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:versionSeriesId
                cpd = new CmisPropertyDefintion(PropertyIds.VERSION_SERIES_ID, false, PropertyType.ID,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:isVersionSeriesCheckedOut
                cpd = new CmisPropertyDefintion(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false, PropertyType.BOOLEAN,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:versionSeriesCheckedOutBy
                cpd = new CmisPropertyDefintion(PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, false, PropertyType.STRING,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:versionSeriesCheckedOutId
                cpd = new CmisPropertyDefintion(PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, false, PropertyType.ID,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:checkinComment
                cpd = new CmisPropertyDefintion(PropertyIds.CHECKIN_COMMENT, false, PropertyType.STRING,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:contentStreamLength
                cpd = new CmisPropertyDefintion(PropertyIds.CONTENT_STREAM_LENGTH, false, PropertyType.INTEGER,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:contentStreamMimeType
                cpd = new CmisPropertyDefintion(PropertyIds.CONTENT_STREAM_MIME_TYPE, false, PropertyType.STRING,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:contentStreamFileName
                cpd = new CmisPropertyDefintion(PropertyIds.CONTENT_STREAM_FILE_NAME, false, PropertyType.STRING,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:contentStreamId
                cpd = new CmisPropertyDefintion(PropertyIds.CONTENT_STREAM_ID, false, PropertyType.ID,
                        Cardinality.SINGLE, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));
            } else if (BaseTypeId.CMIS_FOLDER.equals(type.getBaseTypeId())) {
                // cmis:parentId
                cpd = new CmisPropertyDefintion(PropertyIds.PARENT_ID, false, PropertyType.ID, Cardinality.SINGLE,
                        Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:path
                cpd = new CmisPropertyDefintion(PropertyIds.PATH, false, PropertyType.STRING, Cardinality.SINGLE,
                        Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));

                // cmis:allowedChildObjectTypeIds
                cpd = new CmisPropertyDefintion(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, false, PropertyType.ID,
                        Cardinality.MULTI, Updatability.READONLY, null, null);
                addResult(results, cpd.check(type));
            } else if (BaseTypeId.CMIS_RELATIONSHIP.equals(type.getBaseTypeId())) {
                // cmis:sourceId
                cpd = new CmisPropertyDefintion(PropertyIds.SOURCE_ID, true, PropertyType.ID, Cardinality.SINGLE, null,
                        null, null);
                addResult(results, cpd.check(type));

                // cmis:targetId
                cpd = new CmisPropertyDefintion(PropertyIds.TARGET_ID, true, PropertyType.ID, Cardinality.SINGLE, null,
                        null, null);
                addResult(results, cpd.check(type));
            } else if (BaseTypeId.CMIS_POLICY.equals(type.getBaseTypeId())) {
                // cmis:policyText
                cpd = new CmisPropertyDefintion(PropertyIds.POLICY_TEXT, true, PropertyType.STRING, Cardinality.SINGLE,
                        null, null, null);
                addResult(results, cpd.check(type));
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
            f = createResult(FAILURE, "Property id is not set!");
            addResult(results, assertStringNotEmpty(propDef.getId(), null, f));

            f = createResult(WARNING, "Local name is not set!");
            addResult(results, assertStringNotEmpty(propDef.getLocalName(), null, f));

            // f = createResult(WARNING, "Local namespace is not set!");
            // addResult(results,
            // assertStringNotEmpty(propDef.getLocalNamespace(), null, f));

            f = createResult(FAILURE, "Query name is not set!");
            addResult(results, assertStringNotEmpty(propDef.getQueryName(), null, f));

            f = createResult(WARNING, "Display name is not set!");
            addResult(results, assertStringNotEmpty(propDef.getDisplayName(), null, f));

            f = createResult(WARNING, "Description is not set!");
            addResult(results, assertStringNotEmpty(propDef.getDescription(), null, f));

            f = createResult(FAILURE, "Property type is not set!");
            addResult(results, assertNotNull(propDef.getPropertyType(), null, f));

            f = createResult(FAILURE, "Cardinality is not set!");
            addResult(results, assertNotNull(propDef.getCardinality(), null, f));

            f = createResult(FAILURE, "Updatability is not set!");
            addResult(results, assertNotNull(propDef.getUpdatability(), null, f));

            f = createResult(FAILURE, "Inherited flag is not set!");
            addResult(results, assertNotNull(propDef.isInherited(), null, f));

            f = createResult(FAILURE, "Required flag is not set!");
            addResult(results, assertNotNull(propDef.isRequired(), null, f));

            f = createResult(FAILURE, "Queryable flag is not set!");
            addResult(results, assertNotNull(propDef.isQueryable(), null, f));

            f = createResult(FAILURE, "Orderable flag is not set!");
            addResult(results, assertNotNull(propDef.isOrderable(), null, f));
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected CmisTestResult assertEquals(TypeDefinition expected, TypeDefinition actual, CmisTestResult success,
            CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected type defintion is null, but actual type defintion is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual type defintion is null, but expected type defintion is not!");
            addResultChild(failure, f);

            return failure;
        }

        f = createResult(FAILURE, "Type ids don't match!");
        addResult(results, assertEquals(expected.getId(), actual.getId(), null, f));

        f = createResult(FAILURE, "Base type ids don't match!");
        addResult(results, assertEquals(expected.getBaseTypeId(), actual.getBaseTypeId(), null, f));

        f = createResult(FAILURE, "Parent type ids don't match!");
        addResult(results, assertEquals(expected.getParentTypeId(), actual.getParentTypeId(), null, f));

        f = createResult(FAILURE, "Query names don't match!");
        addResult(results, assertEquals(expected.getQueryName(), actual.getQueryName(), null, f));

        f = createResult(FAILURE, "Local names don't match!");
        addResult(results, assertEquals(expected.getLocalName(), actual.getLocalName(), null, f));

        f = createResult(FAILURE, "Local namespaces don't match!");
        addResult(results, assertEquals(expected.getLocalNamespace(), actual.getLocalNamespace(), null, f));

        f = createResult(FAILURE, "Display names don't match!");
        addResult(results, assertEquals(expected.getDisplayName(), actual.getDisplayName(), null, f));

        f = createResult(FAILURE, "Descriptions don't match!");
        addResult(results, assertEquals(expected.getDescription(), actual.getDescription(), null, f));

        f = createResult(FAILURE, "Controllable ACl flags don't match!");
        addResult(results, assertEquals(expected.isControllableAcl(), actual.isControllableAcl(), null, f));

        f = createResult(FAILURE, "Controllable Policy flags don't match!");
        addResult(results, assertEquals(expected.isControllablePolicy(), actual.isControllablePolicy(), null, f));

        f = createResult(FAILURE, "Creatable flags don't match!");
        addResult(results, assertEquals(expected.isCreatable(), actual.isCreatable(), null, f));

        f = createResult(FAILURE, "Fileable flags don't match!");
        addResult(results, assertEquals(expected.isFileable(), actual.isFileable(), null, f));

        f = createResult(FAILURE, "Fulltext indexed flags don't match!");
        addResult(results, assertEquals(expected.isFulltextIndexed(), actual.isFulltextIndexed(), null, f));

        f = createResult(FAILURE, "Queryable flags don't match!");
        addResult(results, assertEquals(expected.isQueryable(), actual.isQueryable(), null, f));

        f = createResult(FAILURE, "Included in supertype query flags don't match!");
        addResult(results,
                assertEquals(expected.isIncludedInSupertypeQuery(), actual.isIncludedInSupertypeQuery(), null, f));

        if ((expected.getPropertyDefinitions() != null) && (actual.getPropertyDefinitions() != null)) {
            Map<String, PropertyDefinition<?>> epd = expected.getPropertyDefinitions();
            Map<String, PropertyDefinition<?>> apd = actual.getPropertyDefinitions();

            f = createResult(FAILURE, "Different number of property defintions!");
            addResult(results, assertEquals(epd.size(), apd.size(), null, f));

            for (PropertyDefinition<?> pd : epd.values()) {
                f = createResult(FAILURE, "Property definition mismatch: " + pd.getId());
                addResult(results, assertEquals(pd, apd.get(pd.getId()), null, f));
            }
        }

        if (getWorst(results).getLevel() <= OK.getLevel()) {
            for (CmisTestResult result : results) {
                addResultChild(success, result);
            }

            return success;
        } else {
            for (CmisTestResult result : results) {
                addResultChild(failure, result);
            }

            return failure;
        }
    }

    protected CmisTestResult assertEquals(PropertyDefinition<?> expected, PropertyDefinition<?> actual,
            CmisTestResult success, CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected property defintion is null, but actual property defintion is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual property defintion is null, but expected property defintion is not!");
            addResultChild(failure, f);

            return failure;
        }

        f = createResult(FAILURE, "Property ids don't match!");
        addResult(results, assertEquals(expected.getId(), actual.getId(), null, f));

        f = createResult(FAILURE, "Local names don't match!");
        addResult(results, assertEquals(expected.getLocalName(), actual.getLocalName(), null, f));

        f = createResult(FAILURE, "Local namespaces don't match!");
        addResult(results, assertEquals(expected.getLocalNamespace(), actual.getLocalNamespace(), null, f));

        f = createResult(FAILURE, "Display names don't match!");
        addResult(results, assertEquals(expected.getDisplayName(), actual.getDisplayName(), null, f));

        f = createResult(FAILURE, "Query names don't match!");
        addResult(results, assertEquals(expected.getQueryName(), actual.getQueryName(), null, f));

        f = createResult(FAILURE, "Property types don't match!");
        addResult(results, assertEquals(expected.getPropertyType(), actual.getPropertyType(), null, f));

        f = createResult(FAILURE, "Cardinalities don't match!");
        addResult(results, assertEquals(expected.getCardinality(), actual.getCardinality(), null, f));

        f = createResult(FAILURE, "Descriptions don't match!");
        addResult(results, assertEquals(expected.getDescription(), actual.getDescription(), null, f));

        f = createResult(FAILURE, "Updatability flags don't match!");
        addResult(results, assertEquals(expected.getUpdatability(), actual.getUpdatability(), null, f));

        f = createResult(FAILURE, "Default values don't match!");
        addResult(results, assertEqualLists(expected.getDefaultValue(), actual.getDefaultValue(), null, f));

        f = createResult(FAILURE, "Inherited flags don't match!");
        addResult(results, assertEquals(expected.isInherited(), actual.isInherited(), null, f));

        f = createResult(FAILURE, "Required flags don't match!");
        addResult(results, assertEquals(expected.isRequired(), actual.isRequired(), null, f));

        f = createResult(FAILURE, "Queryable flags don't match!");
        addResult(results, assertEquals(expected.isQueryable(), actual.isQueryable(), null, f));

        f = createResult(FAILURE, "Orderable flags don't match!");
        addResult(results, assertEquals(expected.isOrderable(), actual.isOrderable(), null, f));

        f = createResult(FAILURE, "Open choice flags don't match!");
        addResult(results, assertEquals(expected.isOpenChoice(), actual.isOpenChoice(), null, f));

        if (getWorst(results).getLevel() <= OK.getLevel()) {
            for (CmisTestResult result : results) {
                addResultChild(success, result);
            }

            return success;
        } else {
            for (CmisTestResult result : results) {
                addResultChild(failure, result);
            }

            return failure;
        }
    }

    // --- helpers ---

    protected void addResult(List<CmisTestResult> results, CmisTestResult result) {
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

    protected CmisTestResultStatus getWorst(List<CmisTestResult> results) {
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

    // --- helper classes ---

    public class CmisPropertyDefintion {
        private final String id;
        private final Boolean required;
        private final PropertyType propertyType;
        private final Cardinality cardinality;
        private final Updatability updatability;
        private final Boolean queryable;
        private final Boolean orderable;

        public CmisPropertyDefintion(String id, Boolean required, PropertyType propertyType, Cardinality cardinality,
                Updatability updatability, Boolean queryable, Boolean orderable) {
            this.id = id;
            this.required = required;
            this.propertyType = propertyType;
            this.cardinality = cardinality;
            this.updatability = updatability;
            this.queryable = queryable;
            this.orderable = orderable;
        }

        public CmisTestResult check(TypeDefinition type) {
            List<CmisTestResult> results = new ArrayList<CmisTestResult>();

            CmisTestResult f;

            Map<String, PropertyDefinition<?>> propDefs = type.getPropertyDefinitions();
            if (propDefs == null) {
                addResult(results, createResult(FAILURE, "Property definitions are missing!"));
            } else {
                PropertyDefinition<?> propDef = propDefs.get(id);
                if (propDef == null) {
                    addResult(results, createResult(FAILURE, "Property definition is missing!"));
                } else {
                    if ((required != null) && !required.equals(propDef.isRequired())) {
                        f = createResult(FAILURE,
                                "Required flag: expected: " + required + " / actual: " + propDef.isRequired());
                        addResult(results, f);
                    }

                    if (!propertyType.equals(propDef.getPropertyType())) {
                        f = createResult(FAILURE,
                                "Property type: expected: " + propertyType + " / actual: " + propDef.getPropertyType());
                        addResult(results, f);
                    }

                    if (!cardinality.equals(propDef.getCardinality())) {
                        f = createResult(FAILURE,
                                "Cardinality: expected: " + cardinality + " / actual: " + propDef.getCardinality());
                        addResult(results, f);
                    }

                    if ((updatability != null) && !updatability.equals(propDef.getUpdatability())) {
                        f = createResult(FAILURE,
                                "Updatability: expected: " + updatability + " / actual: " + propDef.getUpdatability());
                        addResult(results, f);
                    }

                    if ((queryable != null) && !queryable.equals(propDef.isQueryable())) {
                        f = createResult(FAILURE,
                                "Queryable: expected: " + queryable + " / actual: " + propDef.isQueryable());
                        addResult(results, f);
                    }

                    if ((orderable != null) && !orderable.equals(propDef.isOrderable())) {
                        f = createResult(FAILURE,
                                "Orderable: expected: " + orderable + " / actual: " + propDef.isOrderable());
                        addResult(results, f);
                    }

                    if (type.getBaseTypeId() != null) {
                        Boolean inherited = !type.getBaseTypeId().value().equals(type.getId());
                        if (!inherited.equals(propDef.isInherited())) {
                            f = createResult(FAILURE,
                                    "Inhertited: expected: " + inherited + " / actual: " + propDef.isInherited());
                            addResult(results, f);
                        }
                    }
                }
            }

            CmisTestResultImpl result = createResult(getWorst(results), "Property definition: " + id);
            result.getChildren().addAll(results);

            return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
        }
    }
}
