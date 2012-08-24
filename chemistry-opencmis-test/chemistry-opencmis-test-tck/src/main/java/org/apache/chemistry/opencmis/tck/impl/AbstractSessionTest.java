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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
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
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;

/**
 * Base class for tests that require an OpenCMIS session.
 */
public abstract class AbstractSessionTest extends AbstractCmisTest {

    public static final OperationContext SELECT_ALL_NO_CACHE_OC = new OperationContextImpl();
    public static final OperationContext SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME;
    static {
        SELECT_ALL_NO_CACHE_OC.setFilterString("*");
        SELECT_ALL_NO_CACHE_OC.setCacheEnabled(false);
        SELECT_ALL_NO_CACHE_OC.setIncludeAllowableActions(true);
        SELECT_ALL_NO_CACHE_OC.setIncludeAcls(true);
        SELECT_ALL_NO_CACHE_OC.setIncludePathSegments(true);
        SELECT_ALL_NO_CACHE_OC.setIncludePolicies(true);
        SELECT_ALL_NO_CACHE_OC.setIncludeRelationships(IncludeRelationships.BOTH);
        SELECT_ALL_NO_CACHE_OC.setRenditionFilterString("*");

        SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME = new OperationContextImpl(SELECT_ALL_NO_CACHE_OC);
        SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME.setOrderBy("cmis:name");
    }

    private final SessionFactory factory = SessionFactoryImpl.newInstance();
    private Folder testFolder;

    private Boolean supportsRelationships;
    private Boolean supportsPolicies;

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
        String repId = parameters.get(SessionParameter.REPOSITORY_ID);
        if ((repId != null) && (repId.length() > 0)) {
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
                addResult(createResult(UNEXPECTED_EXCEPTION, "Exception: " + e, e, true));
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

    protected String getFolderTestTypeId() {
        String objectTypeId = getParameters().get(TestParameters.DEFAULT_FOLDER_TYPE);
        if (objectTypeId == null) {
            objectTypeId = TestParameters.DEFAULT_FOLDER_TYPE_VALUE;
        }

        return objectTypeId;
    }

    protected String getDocumentTestTypeId() {
        String objectTypeId = getParameters().get(TestParameters.DEFAULT_DOCUMENT_TYPE);
        if (objectTypeId == null) {
            objectTypeId = TestParameters.DEFAULT_DOCUMENT_TYPE_VALUE;
        }

        return objectTypeId;
    }

    // --- helpers ---

    protected String[] getAllProperties(CmisObject object) {
        String[] propertiesk = new String[object.getType().getPropertyDefinitions().size()];

        int i = 0;
        for (String propId : object.getType().getPropertyDefinitions().keySet()) {
            propertiesk[i++] = propId;
        }

        return propertiesk;
    }

    protected String getStringFromContentStream(ContentStream contentStream) throws IOException {
        if (contentStream == null || contentStream.getStream() == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        Reader reader = new InputStreamReader(contentStream.getStream(), "UTF-8");

        try {
            final char[] buffer = new char[64 * 1024];
            int b;
            while (true) {
                b = reader.read(buffer, 0, buffer.length);
                if (b > 0) {
                    sb.append(buffer, 0, b);
                } else if (b == -1) {
                    break;
                }
            }
        } finally {
            reader.close();
        }

        return sb.toString();
    }

    // --- handy create and delete methods ---

    /**
     * Creates a folder.
     */
    protected Folder createFolder(Session session, Folder parent, String name) {
        return createFolder(session, parent, name, getFolderTestTypeId());
    }

    /**
     * Creates a folder.
     */
    protected Folder createFolder(Session session, Folder parent, String name, String objectTypeId) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);

        Folder result = null;
        try {
            // create the folder
            result = parent.createFolder(properties, null, null, null, SELECT_ALL_NO_CACHE_OC);
        } catch (CmisBaseException e) {
            addResult(createResult(UNEXPECTED_EXCEPTION, "Folder could not be created! Exception: " + e.getMessage(),
                    e, true));
        }

        try {
            CmisTestResult f;

            // check document name
            f = createResult(FAILURE, "Folder name does not match!", false);
            addResult(assertEquals(name, result.getName(), null, f));

            // check the new folder
            String[] propertiesToCheck = new String[result.getType().getPropertyDefinitions().size()];

            int i = 0;
            for (String propId : result.getType().getPropertyDefinitions().keySet()) {
                propertiesToCheck[i++] = propId;
            }

            addResult(checkObject(session, result, propertiesToCheck, "New folder object spec compliance"));

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
            for (CmisObject child : parent.getChildren(SELECT_ALL_NO_CACHE_OC)) {
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
     * Counts the children in a folder.
     */
    protected int countFolderChildren(Folder folder) {
        int count = 0;

        for (@SuppressWarnings("unused")
        CmisObject object : folder.getChildren()) {
            count++;
        }

        return count;
    }

    /**
     * Creates a document.
     */
    protected Document createDocument(Session session, Folder parent, String name, String content) {
        return createDocument(session, parent, name, getDocumentTestTypeId(), content);
    }

    /**
     * Creates a document.
     */
    protected Document createDocument(Session session, Folder parent, String name, String objectTypeId, String content) {
        if (content == null) {
            content = "";
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);

        TypeDefinition type = session.getTypeDefinition(objectTypeId);
        if (!(type instanceof DocumentTypeDefinition)) {
            addResult(createResult(FAILURE, "Type is not a document type! Type: " + objectTypeId, true));
            return null;
        }

        DocumentTypeDefinition docType = (DocumentTypeDefinition) type;
        VersioningState versioningState = (Boolean.TRUE.equals(docType.isVersionable()) ? VersioningState.MAJOR
                : VersioningState.NONE);

        byte[] contentBytes = null;
        Document result = null;
        try {
            contentBytes = content.getBytes("UTF-8");
            ContentStream contentStream = new ContentStreamImpl(name, BigInteger.valueOf(contentBytes.length),
                    "text/plain", new ByteArrayInputStream(contentBytes));

            // create the document
            result = parent.createDocument(properties, contentStream, versioningState, null, null, null,
                    SELECT_ALL_NO_CACHE_OC);

            contentStream.getStream().close();
        } catch (Exception e) {
            addResult(createResult(UNEXPECTED_EXCEPTION, "Document could not be created! Exception: " + e.getMessage(),
                    e, true));
        }

        try {
            CmisTestResult f;

            // check document name
            f = createResult(FAILURE, "Document name does not match!", false);
            addResult(assertEquals(name, result.getName(), null, f));

            // check the new document
            addResult(checkObject(session, result, getAllProperties(result), "New document object spec compliance"));

            // check content
            try {
                ContentStream contentStream = result.getContentStream();

                f = createResult(WARNING, "Document filename does not match!", false);
                addResult(assertEquals(name, contentStream.getFileName(), null, f));

                f = createResult(WARNING,
                        "cmis:contentStreamFileName and the filename of the content stream do not match!", false);
                addResult(assertEquals(result.getContentStreamFileName(), contentStream.getFileName(), null, f));

                String fetchedContent = getStringFromContentStream(result.getContentStream());
                if (!content.equals(fetchedContent)) {
                    addResult(createResult(FAILURE,
                            "Content of newly created document doesn't match the orign content!"));
                }
            } catch (IOException e) {
                addResult(createResult(UNEXPECTED_EXCEPTION,
                        "Content of newly created document couldn't be read! Exception: " + e.getMessage(), e, true));
            }
        } catch (CmisBaseException e) {
            addResult(createResult(UNEXPECTED_EXCEPTION,
                    "Newly created document is invalid! Exception: " + e.getMessage(), e, true));
        }

        if (parent != null) {
            List<Folder> parents = result.getParents(SELECT_ALL_NO_CACHE_OC);
            boolean found = false;
            for (Folder folder : parents) {
                if (parent.getId().equals(folder.getId())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                addResult(createResult(FAILURE,
                        "The folder the document has been created in is not in the list of the document parents!"));
            }
        }

        return result;
    }

    /**
     * Creates a relationship.
     */
    protected Relationship createRelationship(Session session, String name, ObjectId source, ObjectId target) {
        String objectTypeId = getParameters().get(TestParameters.DEFAULT_RELATIONSHIP_TYPE);
        if (objectTypeId == null) {
            objectTypeId = TestParameters.DEFAULT_RELATIONSHIP_TYPE_VALUE;
        }

        return createRelationship(session, name, source, target, objectTypeId);
    }

    /**
     * Creates a relationship.
     */
    protected Relationship createRelationship(Session session, String name, ObjectId source, ObjectId target,
            String objectTypeId) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, objectTypeId);
        properties.put(PropertyIds.SOURCE_ID, source.getId());
        properties.put(PropertyIds.TARGET_ID, target.getId());

        ObjectId relId;
        Relationship result = null;

        try {
            relId = session.createRelationship(properties);
            result = (Relationship) session.getObject(relId, SELECT_ALL_NO_CACHE_OC);
        } catch (Exception e) {
            addResult(createResult(UNEXPECTED_EXCEPTION,
                    "Relationship could not be created! Exception: " + e.getMessage(), e, true));
        }

        try {
            // check the new relationship
            addResult(checkObject(session, result, getAllProperties(result), "New document object spec compliance"));
        } catch (CmisBaseException e) {
            addResult(createResult(UNEXPECTED_EXCEPTION,
                    "Newly created document is invalid! Exception: " + e.getMessage(), e, true));
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
            CmisObject parentObject = session.getObjectByPath(testFolderParentPath, SELECT_ALL_NO_CACHE_OC);
            if (!(parentObject instanceof Folder)) {
                addResult(createResult(FAILURE, "Parent folder of the test folder is actually not a folder! Path: "
                        + testFolderParentPath, true));
            }

            parent = (Folder) parentObject;
        } catch (CmisBaseException e) {
            addResult(createResult(UNEXPECTED_EXCEPTION,
                    "Test folder could not be created! Exception: " + e.getMessage(), e, true));
        }

        if (parent != null) {
            testFolder = createFolder(session, parent, name);
        }

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

    protected boolean hasRelationships(Session session) {
        if (supportsRelationships == null) {
            supportsRelationships = Boolean.FALSE;
            for (ObjectType type : session.getTypeChildren(null, false)) {
                if (BaseTypeId.CMIS_RELATIONSHIP.value().equals(type.getId())) {
                    supportsRelationships = Boolean.TRUE;
                    break;
                }
            }
        }

        return supportsRelationships.booleanValue();
    }

    protected boolean hasPolicies(Session session) {
        if (supportsPolicies == null) {
            supportsPolicies = Boolean.FALSE;
            for (ObjectType type : session.getTypeChildren(null, false)) {
                if (BaseTypeId.CMIS_POLICY.value().equals(type.getId())) {
                    supportsPolicies = Boolean.TRUE;
                    break;
                }
            }
        }

        return supportsPolicies.booleanValue();
    }

    protected CmisTestResult checkObject(Session session, CmisObject object, String[] properties, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        f = createResult(FAILURE, "Object is null!", true);
        addResult(results, assertNotNull(object, null, f));

        if (object != null) {
            f = createResult(FAILURE, "Object id is not set!");
            addResult(results, assertStringNotEmpty(object.getId(), null, f));

            GregorianCalendar creationDate = null;
            GregorianCalendar lastModificationDate = null;

            // properties
            for (String propId : properties) {
                Property<?> prop = object.getProperty(propId);

                // values of non-spec properties are not checked here
                PropertyCheckEnum propertyCheck = PropertyCheckEnum.NO_VALUE_CHECK;

                // known properties that are strings and must be set
                if (PropertyIds.OBJECT_ID.equals(propId) || PropertyIds.BASE_TYPE_ID.equals(propId)
                        || PropertyIds.OBJECT_TYPE_ID.equals(propId) || PropertyIds.PATH.equals(propId)
                        || PropertyIds.SOURCE_ID.equals(propId) || PropertyIds.TARGET_ID.equals(propId)
                        || PropertyIds.POLICY_TEXT.equals(propId)) {
                    propertyCheck = PropertyCheckEnum.STRING_MUST_NOT_BE_EMPTY;
                }

                if (!(object instanceof Relationship)) {
                    if (PropertyIds.CREATED_BY.equals(propId) || PropertyIds.LAST_MODIFIED_BY.equals(propId)) {
                        propertyCheck = PropertyCheckEnum.STRING_MUST_NOT_BE_EMPTY;
                    }
                }

                // known properties that are strings and should be set
                if (PropertyIds.NAME.equals(propId)) {
                    propertyCheck = PropertyCheckEnum.STRING_SHOULD_NOT_BE_EMPTY;
                }

                // known properties that are not strings and must be set
                if (PropertyIds.IS_IMMUTABLE.equals(propId)) {
                    propertyCheck = PropertyCheckEnum.MUST_BE_SET;
                }

                if (!(object instanceof Relationship)) {
                    if (PropertyIds.CREATION_DATE.equals(propId) || PropertyIds.LAST_MODIFICATION_DATE.equals(propId)) {
                        propertyCheck = PropertyCheckEnum.MUST_BE_SET;
                    }
                }

                // special case: parent
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

                // catch creationDate and lastModificationDate
                if (PropertyIds.CREATION_DATE.equals(propId)) {
                    creationDate = (GregorianCalendar) prop.getFirstValue();
                } else if (PropertyIds.LAST_MODIFICATION_DATE.equals(propId)) {
                    lastModificationDate = (GregorianCalendar) prop.getFirstValue();
                }
            }

            // check creationDate <= lastModificationDate
            if (creationDate != null && lastModificationDate != null) {
                f = createResult(FAILURE, "Last modification date precedes creation date!");
                addResult(results,
                        assertIsTrue(creationDate.getTimeInMillis() <= lastModificationDate.getTimeInMillis(), null, f));
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
                    DocumentTypeDefinition docType = (DocumentTypeDefinition) doc.getType();
                    if (doc.isVersionSeriesCheckedOut() != null) {
                        if (doc.isVersionSeriesCheckedOut()) {
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

                            // versionable check
                            if (docType.isVersionable()) {
                                if (Boolean.TRUE.equals(doc.isLatestVersion())) {
                                    f = createResult(WARNING,
                                            "Document is versionable and not checked but has no CAN_CHECK_OUT allowable action!");
                                    addResult(results, assertAllowableAction(object, Action.CAN_CHECK_OUT, null, f));
                                }
                            } else {
                                f = createResult(FAILURE,
                                        "Document is not versionable but has CAN_CHECK_OUT allowable action!");
                                addResult(results, assertNotAllowableAction(object, Action.CAN_CHECK_OUT, null, f));
                            }
                        }
                    } else {
                        addResult(results, createResult(WARNING, "Property cmis:isVersionSeriesCheckedOut is not set!"));
                    }

                    // immutable check
                    if (Boolean.TRUE.equals(doc.isImmutable())) {
                        f = createResult(FAILURE,
                                "Document is immutable and has CAN_UPDATE_PROPERTIES allowable action!");
                        addResult(results, assertNotAllowableAction(object, Action.CAN_UPDATE_PROPERTIES, null, f));

                        f = createResult(FAILURE, "Document is immutable and has CAN_DELETE_OBJECT allowable action!");
                        addResult(results, assertNotAllowableAction(object, Action.CAN_DELETE_OBJECT, null, f));
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

                        f = createResult(FAILURE, "Root folder has CAN_MOVE_OBJECT allowable action!");
                        addResult(results, assertNotAllowableAction(object, Action.CAN_MOVE_OBJECT, null, f));
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

                // get allowable actions again
                AllowableActions allowableActions = session.getBinding().getObjectService()
                        .getAllowableActions(session.getRepositoryInfo().getId(), object.getId(), null);

                if (allowableActions.getAllowableActions() == null) {
                    addResult(results,
                            createResult(FAILURE, "getAllowableActions() didn't returned allowable actions!"));
                } else {
                    f = createResult(FAILURE,
                            "Object allowable actions don't match the allowable actions returned by getAllowableActions()!");
                    addResult(
                            results,
                            assertEqualSet(object.getAllowableActions().getAllowableActions(),
                                    allowableActions.getAllowableActions(), null, f));
                }
            }

            // check ACL
            if (object.getAcl() != null && object.getAcl().getAces() != null) {
                addResult(results, checkACL(session, object.getAcl(), "ACL"));
            }

            // check policies
            if (hasPolicies(session)) {
                try {
                    List<ObjectData> appliedPolicies = session.getBinding().getPolicyService()
                            .getAppliedPolicies(session.getRepositoryInfo().getId(), object.getId(), "*", null);

                    if (appliedPolicies == null) {
                        appliedPolicies = Collections.emptyList();
                    }

                    List<Policy> objectPolicies = object.getPolicies();
                    if (objectPolicies == null) {
                        objectPolicies = Collections.emptyList();
                    }

                    f = createResult(FAILURE,
                            "The number of policies returned by getAppliedPolicies() and the number of object policies don't match!");
                    addResult(results, assertEquals(appliedPolicies.size(), objectPolicies.size(), null, f));
                } catch (CmisNotSupportedException e) {
                    addResult(results,
                            createResult(WARNING, "getAppliedPolicies() not supported for object: " + object.getId()));
                }
            }

            // check relationships
            checkRelationships(session, results, object);

            // check document content
            checkDocumentContent(session, results, object);

            // check renditions
            if (object.getRenditions() != null) {
                addResult(results, checkRenditions(session, object, "Rendition check"));
            }
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected CmisTestResult checkACL(Session session, Acl acl, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        f = createResult(FAILURE, "ACL is null!");
        addResult(results, assertNotNull(acl, null, f));

        if (acl != null) {

            f = createResult(FAILURE, "List of ACEs is null!");
            addResult(results, assertNotNull(acl.getAces(), null, f));

            if (acl.getAces() != null) {
                for (Ace ace : acl.getAces()) {
                    f = createResult(FAILURE, "ACE with empty principal id!");
                    addResult(results, assertStringNotEmpty(ace.getPrincipalId(), null, f));

                    f = createResult(FAILURE, "ACE with empty permission list!");
                    addResult(results, assertListNotEmpty(ace.getPermissions(), null, f));

                    if (ace.getPermissions() != null) {
                        for (String permission : ace.getPermissions()) {
                            f = createResult(FAILURE, "ACE with empty permission entry!");
                            addResult(results, assertStringNotEmpty(permission, null, f));
                        }
                    }
                }
            }
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    private void checkRelationships(Session session, List<CmisTestResult> results, CmisObject object) {
        if (object instanceof Relationship) {
            if (object.getRelationships() != null && !object.getRelationships().isEmpty()) {
                addResult(results, createResult(FAILURE, "A relationship has relationships!"));
                return;
            }
        }

        if (object.getRelationships() != null) {
            for (Relationship relationship : object.getRelationships()) {
                if (relationship == null) {
                    addResult(results, createResult(FAILURE, "A relationship in the relationship list is null!"));
                    continue;
                }

                CmisObject fullRelationshipObject = session.getObject(relationship, SELECT_ALL_NO_CACHE_OC);
                addResult(
                        results,
                        checkObject(session, fullRelationshipObject, getAllProperties(fullRelationshipObject),
                                "Relationship check: " + fullRelationshipObject.getId()));
            }
        }
    }

    private void checkDocumentContent(Session session, List<CmisTestResult> results, CmisObject object) {
        if (!(object instanceof Document)) {
            // only documents have content
            return;
        }

        CmisTestResult f;

        Document doc = (Document) object;
        DocumentTypeDefinition type = (DocumentTypeDefinition) doc.getType();

        // check ContentStreamAllowed flag
        boolean hasContentProperties = (doc.getContentStreamFileName() != null) || (doc.getContentStreamId() != null)
                || (doc.getContentStreamLength() > -1) || (doc.getContentStreamMimeType() != null);

        if (hasContentProperties) {
            if (type.getContentStreamAllowed() == ContentStreamAllowed.NOTALLOWED) {
                addResult(
                        results,
                        createResult(FAILURE,
                                "Content properties have values but the document type doesn't allow content!"));
            }
        } else {
            if (type.getContentStreamAllowed() == ContentStreamAllowed.REQUIRED) {
                addResult(results,
                        createResult(FAILURE, "Content properties are not set but the document type demands content!"));
            }
        }

        // get the content stream
        ContentStream contentStream = doc.getContentStream();

        if (contentStream == null) {
            if (hasContentProperties && doc.getContentStreamLength() > 0) {
                addResult(results,
                        createResult(FAILURE, "Content properties have values but the document has no content!"));
            }

            if (type.getContentStreamAllowed() == ContentStreamAllowed.REQUIRED) {
                addResult(results,
                        createResult(FAILURE, "The document type demands content but the document has no content!"));
            }

            return;
        }

        if (type.getContentStreamAllowed() == ContentStreamAllowed.NOTALLOWED) {
            addResult(results, createResult(FAILURE, "Document type doesn't allow content but document has content!"));
        }

        // file name check
        f = createResult(FAILURE, "Content file names don't match!");
        addResult(results, assertEquals(doc.getContentStreamFileName(), contentStream.getFileName(), null, f));

        if (doc.getContentStreamLength() > -1 && contentStream.getLength() > -1) {
            f = createResult(FAILURE, "Content lengths don't match!");
            addResult(results, assertEquals(doc.getContentStreamLength(), contentStream.getLength(), null, f));
        }

        // MIME type check
        String docMimeType = doc.getContentStreamMimeType();
        if (docMimeType != null) {
            int x = docMimeType.indexOf(';');
            if (x > -1) {
                docMimeType = docMimeType.substring(0, x);
            }
            docMimeType = docMimeType.trim();
        }

        String contentMimeType = contentStream.getMimeType();
        if (contentMimeType != null) {
            int x = contentMimeType.indexOf(';');
            if (x > -1) {
                contentMimeType = contentMimeType.substring(0, x);
            }
            contentMimeType = contentMimeType.trim();
        }

        f = createResult(FAILURE, "Content MIME types don't match!");
        addResult(results, assertEquals(contentMimeType, docMimeType, null, f));

        if (contentStream.getMimeType() != null) {
            if (contentMimeType.equals(docMimeType)) {
                f = createResult(WARNING, "Content MIME types don't match!");
                addResult(results, assertEquals(doc.getContentStreamMimeType(), contentStream.getMimeType(), null, f));
            }

            f = createResult(FAILURE, "Content MIME types is invalid: " + contentStream.getMimeType());
            addResult(
                    results,
                    assertIsTrue(contentStream.getMimeType().length() > 2
                            && contentStream.getMimeType().indexOf('/') > 0, null, f));
        }

        // check stream
        InputStream stream = contentStream.getStream();
        if (stream == null) {
            addResult(results, createResult(FAILURE, "Docuemnt has no content stream!"));
            return;
        }

        try {
            long bytes = 0;
            byte[] buffer = new byte[64 * 1024];
            int b = stream.read(buffer);
            while (b > -1) {
                bytes += b;
                b = stream.read(buffer);
            }
            stream.close();

            // check content length
            if (doc.getContentStreamLength() > -1) {
                f = createResult(FAILURE,
                        "Content stream length property value doesn't match the actual content length!");
                addResult(results, assertEquals(doc.getContentStreamLength(), bytes, null, f));
            }

            if (contentStream.getLength() > -1) {
                f = createResult(FAILURE, "Content length value doesn't match the actual content length!");
                addResult(results, assertEquals(contentStream.getLength(), bytes, null, f));
            }
        } catch (Exception e) {
            addResult(results, createResult(FAILURE, "Reading content failed: " + e, e, false));
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
            }
        }
    }

    protected CmisTestResult checkRenditions(Session session, CmisObject object, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if (object.getRenditions() != null) {

            for (Rendition rend : object.getRenditions()) {
                f = createResult(FAILURE, "A rendition in the list of renditions is null!");
                addResult(results, assertNotNull(rend, null, f));

                if (rend != null) {
                    f = createResult(FAILURE, "A rendition has an empty stream id!");
                    addResult(results, assertStringNotEmpty(rend.getStreamId(), null, f));

                    f = createResult(FAILURE, "A rendition has an empty kind! Stream id: " + rend.getStreamId());
                    addResult(results, assertStringNotEmpty(rend.getKind(), null, f));

                    f = createResult(FAILURE, "A rendition has an empty MIME type! Stream id: " + rend.getStreamId());
                    addResult(results, assertStringNotEmpty(rend.getMimeType(), null, f));

                    if ("cmis:thumbnail".equals(rend.getKind())) {
                        f = createResult(WARNING,
                                "A rendition is of kind 'cmis:thumbnail' but the height is not set or has an invalid value! Stream id: "
                                        + rend.getStreamId());
                        addResult(results, assertIsTrue(rend.getHeight() > 0, null, f));

                        f = createResult(WARNING,
                                "A rendition is of kind 'cmis:thumbnail' but the width is not set or has an invalid value! Stream id: "
                                        + rend.getStreamId());
                        addResult(results, assertIsTrue(rend.getWidth() > 0, null, f));
                    }

                    // check the content
                    ContentStream contentStream = rend.getContentStream();
                    f = createResult(FAILURE, "A rendition has no content stream! Stream id: " + rend.getStreamId());
                    addResult(results, assertNotNull(contentStream, null, f));

                    if (contentStream != null) {
                        InputStream stream = contentStream.getStream();

                        f = createResult(FAILURE, "A rendition has no stream! Stream id: " + rend.getStreamId());
                        addResult(results, assertNotNull(stream, null, f));

                        if (stream != null) {
                            try {
                                long bytes = 0;
                                byte[] buffer = new byte[64 * 1024];
                                int b = stream.read(buffer);
                                while (b > -1) {
                                    bytes += b;
                                    b = stream.read(buffer);
                                }
                                stream.close();

                                // check content length
                                if (rend.getLength() > -1) {
                                    f = createResult(FAILURE,
                                            "Rendition content stream lengthÏ value doesn't match the actual content length!");
                                    addResult(results, assertEquals(rend.getLength(), bytes, null, f));
                                }
                            } catch (Exception e) {
                                addResult(results, createResult(FAILURE, "Reading content failed: " + e, e, false));
                            } finally {
                                try {
                                    stream.close();
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                }
            }
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected CmisTestResult checkVersionHistory(Session session, CmisObject object, String[] properties, String message) {
        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if (object.getBaseTypeId() != BaseTypeId.CMIS_DOCUMENT) {
            // skip non-document objects
            return null;
        }

        if (!Boolean.TRUE.equals(((DocumentTypeDefinition) object.getType()).isVersionable())) {
            // skip non-versionable types
            return null;
        }

        Document doc = (Document) object;

        // check version series id
        String versionSeriesId = doc.getVersionSeriesId();

        f = createResult(FAILURE, "Versionable document has no version series id property!");
        addResult(results, assertStringNotEmpty(versionSeriesId, null, f));
        if (versionSeriesId == null) {
            CmisTestResultImpl result = createResult(getWorst(results), message);
            result.getChildren().addAll(results);
            return result;
        }

        // get version history
        List<Document> versions = doc.getAllVersions(SELECT_ALL_NO_CACHE_OC);

        f = createResult(FAILURE, "Version history is null!");
        addResult(results, assertNotNull(versions, null, f));
        if (versions == null) {
            CmisTestResultImpl result = createResult(getWorst(results), message);
            result.getChildren().addAll(results);
            return result;
        }

        f = createResult(FAILURE, "Version history must have at least one version!");
        addResult(results, assertIsTrue(versions.size() > 0, null, f));

        if (versions.size() > 0) {
            // get latest version
            Document lastestVersion = doc.getObjectOfLatestVersion(false, SELECT_ALL_NO_CACHE_OC);
            addResult(results,
                    checkObject(session, lastestVersion, properties, "Latest version check: " + lastestVersion.getId()));

            f = createResult(FAILURE, "Latest version is not flagged as latest version! Id: " + lastestVersion.getId());
            addResult(results, assertIsTrue(lastestVersion.isLatestVersion(), null, f));

            // get latest major version
            Document lastestMajorVersion = null;
            try {
                lastestMajorVersion = doc.getObjectOfLatestVersion(true, SELECT_ALL_NO_CACHE_OC);

                f = createResult(FAILURE, "getObjectOfLatestVersion returned an invalid object!");
                addResult(results, assertNotNull(lastestMajorVersion, null, f));
            } catch (CmisObjectNotFoundException e) {
                // no latest major version
            }
            if (lastestMajorVersion != null) {
                addResult(
                        results,
                        checkObject(session, lastestMajorVersion, properties, "Latest major version check: "
                                + lastestMajorVersion.getId()));

                f = createResult(FAILURE, "Latest major version is not flagged as latest major version! Id: "
                        + lastestMajorVersion.getId());
                addResult(results, assertIsTrue(lastestMajorVersion.isLatestMajorVersion(), null, f));
            }

            // iterate through the version history and test each version
            // document
            long creatationDate = Long.MAX_VALUE;
            int latestVersion = 0;
            int latestMajorVersion = 0;
            long latestModificationDate = Long.MAX_VALUE;
            int latestModifictaionIndex = Integer.MIN_VALUE;
            Set<String> versionLabels = new HashSet<String>();
            boolean found = false;
            boolean foundLastestVersion = false;
            boolean foundLastestMajorVersion = false;
            for (int i = 0; i < versions.size(); i++) {
                Document version = versions.get(i);

                f = createResult(FAILURE, "Version " + i + " is null!");
                addResult(results, assertNotNull(version, null, f));
                if (version == null) {
                    continue;
                }

                addResult(results, checkObject(session, version, properties, "Version check: " + version.getId()));

                // check first entry
                if (i == 0) {
                    if (version.isVersionSeriesCheckedOut()) {
                        f = createResult(
                                WARNING,
                                "Version series is checked-out and the PWC is not the latest version! Id: "
                                        + version.getId()
                                        + " (Note: The words of the CMIS specification define that the PWC is the latest version."
                                        + " But that is not the intention of the spec and will be changed in CMIS 1.1."
                                        + " Thus this a warning, not an error.)");
                        addResult(results, assertIsTrue(version.isLatestVersion(), null, f));
                    } else {
                        f = createResult(FAILURE,
                                "Version series is not checked-out and first version history entry is not the latest version! Id: "
                                        + version.getId());
                        addResult(results, assertIsTrue(version.isLatestVersion(), null, f));
                    }
                }

                // check version id
                f = createResult(FAILURE, "Version series id does not match! Id: " + version.getId());
                addResult(results, assertEquals(versionSeriesId, version.getVersionSeriesId(), null, f));

                // check creation date
                if (creatationDate == version.getCreationDate().getTimeInMillis()) {
                    addResult(results, createResult(WARNING, "Two or more versions have the same creation date!"));
                } else {
                    f = createResult(FAILURE, "Version history order incorrect! Must be sorted bei creation date!");
                    addResult(results,
                            assertIsTrue(version.getCreationDate().getTimeInMillis() <= creatationDate, null, f));
                }
                version.getCreationDate().getTimeInMillis();

                // count latest versions and latest major versions
                if (version.isLatestVersion()) {
                    latestVersion++;
                }

                if (version.isLatestMajorVersion()) {
                    latestMajorVersion++;
                }

                // find latest modification date
                if (latestModificationDate == version.getLastModificationDate().getTimeInMillis()) {
                    addResult(results,
                            createResult(WARNING, "Two or more versions have the same last modification date!"));
                } else if (latestModificationDate < version.getLastModificationDate().getTimeInMillis()) {
                    latestModificationDate = version.getLastModificationDate().getTimeInMillis();
                    latestModifictaionIndex = i;
                }

                // check for version label duplicates
                String versionLabel = version.getVersionLabel();
                f = createResult(WARNING, "More than one version have this version label: " + versionLabel);
                addResult(results, assertIsFalse(versionLabels.contains(versionLabel), null, f));

                versionLabels.add(versionLabel);

                // check PWC
                if (version.getId().equals(version.getVersionSeriesCheckedOutId())) {
                    f = createResult(FAILURE, "PWC must not be flagged as latest major version! Id: " + version.getId());
                    addResult(results, assertIsFalse(version.isLatestMajorVersion(), null, f));
                }

                // check checked out
                if (Boolean.TRUE.equals(doc.isVersionSeriesCheckedOut())) {
                    f = createResult(WARNING,
                            "Version series is marked as checked out but cmis:versionSeriesCheckedOutId is not set! Id: "
                                    + version.getId());
                    addResult(results, assertStringNotEmpty(doc.getVersionSeriesCheckedOutId(), null, f));

                    f = createResult(WARNING,
                            "Version series is marked as checked out but cmis:versionSeriesCheckedOutBy is not set! Id: "
                                    + version.getId());
                    addResult(results, assertStringNotEmpty(doc.getVersionSeriesCheckedOutBy(), null, f));
                } else if (Boolean.FALSE.equals(doc.isVersionSeriesCheckedOut())) {
                    f = createResult(FAILURE,
                            "Version series is not marked as checked out but cmis:versionSeriesCheckedOutId is set! Id: "
                                    + version.getId());
                    addResult(results, assertNull(doc.getVersionSeriesCheckedOutId(), null, f));

                    f = createResult(FAILURE,
                            "Version series is not marked as checked out but cmis:versionSeriesCheckedOutIdBy is set! Id: "
                                    + version.getId());
                    addResult(results, assertNull(doc.getVersionSeriesCheckedOutBy(), null, f));
                }

                // found origin object?
                if (version.getId().equals(object.getId())) {
                    found = true;
                }

                // found latest version?
                if (version.getId().equals(lastestVersion.getId())) {
                    foundLastestVersion = true;
                }

                // found latest major version?
                if (lastestMajorVersion != null && version.getId().equals(lastestMajorVersion.getId())) {
                    foundLastestMajorVersion = true;
                }
            }

            // check latest versions
            f = createResult(FAILURE, "Version series id has " + latestVersion
                    + " latest versions! There must be only one!");
            addResult(results, assertEquals(1, latestVersion, null, f));

            if (!foundLastestVersion) {
                addResult(results, createResult(FAILURE, "Latest version not found in version history!"));
            }

            // check latest major versions
            if (lastestMajorVersion == null) {
                f = createResult(FAILURE, "Version series id has " + latestMajorVersion
                        + " latest major version(s) but getObjectOfLatestVersion() didn't return a major version!");
                addResult(results, assertEquals(0, latestMajorVersion, null, f));
            } else {
                f = createResult(FAILURE, "Version series id has " + latestMajorVersion
                        + " latest major versions but there should be exactly one!");
                addResult(results, assertEquals(1, latestMajorVersion, null, f));

                if (!foundLastestMajorVersion) {
                    addResult(results, createResult(FAILURE, "Latest major version not found in version history!"));
                }
            }

            // check latest version
            if (latestModifictaionIndex >= 0) {
                f = createResult(
                        FAILURE,
                        "Version with the latest modification date is not flagged as latest version! Id: "
                                + versions.get(latestModifictaionIndex));
                addResult(results, assertIsTrue(versions.get(latestModifictaionIndex).isLatestVersion(), null, f));
            }

            // check if the origin object was found
            if (!found) {
                addResult(results, createResult(FAILURE, "Document not found in its version history!"));
            }
        }

        CmisTestResultImpl result = createResult(getWorst(results), message);
        result.getChildren().addAll(results);

        return (result.getStatus().getLevel() <= OK.getLevel() ? null : result);
    }

    protected CmisTestResult assertAllowableAction(CmisObject object, Action action, CmisTestResult success,
            CmisTestResult failure) {
        AllowableActions allowableActions = object.getAllowableActions();
        if (allowableActions != null && allowableActions.getAllowableActions() != null) {
            if (allowableActions.getAllowableActions().contains(action)) {
                return success;
            }
        }

        return failure;
    }

    protected CmisTestResult assertNotAllowableAction(CmisObject object, Action action, CmisTestResult success,
            CmisTestResult failure) {
        AllowableActions allowableActions = object.getAllowableActions();
        if (allowableActions != null && allowableActions.getAllowableActions() != null) {
            if (!allowableActions.getAllowableActions().contains(action)) {
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
        ItemIterable<CmisObject> children = folder.getChildren(SELECT_ALL_NO_CACHE_OC_ORDER_BY_NAME);

        int orderByNameIssues = 0;
        String lastName = null;

        for (CmisObject child : children) {
            childrenCount++;
            if (child instanceof Folder) {
                childrenFolderCount++;
            }

            checkChild(session, results, folder, child);

            if (lastName != null && child.getName() != null) {
                if (child.getName().compareToIgnoreCase(lastName) < 0) {
                    orderByNameIssues++;
                }
            }

            lastName = child.getName();
        }

        if (children.getTotalNumItems() >= 0) {
            f = createResult(WARNING, "Number of children doesn't match the reported total number of items!");
            addResult(results, assertEquals(childrenCount, children.getTotalNumItems(), null, f));
        } else {
            addResult(results, createResult(WARNING, "getChildren did not report the total number of items!"));
        }

        f = createResult(WARNING,
                "Children should be ordered by cmis:name, but they are not! (It might be a collation mismtach.)");
        addResult(results, assertEquals(0, orderByNameIssues, null, f));

        // getDescendants

        if (isGetDescendantsSupported(session)) {
            long descendantsCount = 0;
            List<Tree<FileableCmisObject>> descendants = folder.getDescendants(1, SELECT_ALL_NO_CACHE_OC);

            for (Tree<FileableCmisObject> child : descendants) {
                descendantsCount++;

                if (child == null) {
                    addResult(results, createResult(FAILURE, "Folder descendants contain a null tree!"));
                } else {
                    checkChild(session, results, folder, child.getItem());
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
            List<Tree<FileableCmisObject>> folderTree = folder.getFolderTree(1, SELECT_ALL_NO_CACHE_OC);

            for (Tree<FileableCmisObject> child : folderTree) {
                folderTreeCount++;

                if (child == null) {
                    addResult(results, createResult(FAILURE, "Folder tree contains a null tree!"));
                } else {
                    checkChild(session, results, folder, child.getItem());
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

    private void checkChild(Session session, List<CmisTestResult> results, Folder folder, CmisObject child) {
        CmisTestResult f;

        if (child == null) {
            addResult(results, createResult(FAILURE, "Folder contains a null child!"));
        } else {
            String[] propertiesToCheck = new String[child.getType().getPropertyDefinitions().size()];

            int i = 0;
            for (String propId : child.getType().getPropertyDefinitions().keySet()) {
                propertiesToCheck[i++] = propId;
            }

            addResult(results, checkObject(session, child, propertiesToCheck, "Child check: " + child.getId()));
            addResult(
                    results,
                    checkVersionHistory(session, child, propertiesToCheck,
                            "Child version history check: " + child.getId()));

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
                    } else if (folder.getId().equals(parent.getId())) {
                        foundParent = true;
                        break;
                    }
                }

                if (!foundParent) {
                    f = createResult(FAILURE, "Folder is not found in childs parents! Id: " + child.getId());
                    addResult(results, assertIsTrue(parents.size() > 0, null, f));
                }

                // get object by id and compare
                CmisObject objectById = session.getObject(child.getId(), SELECT_ALL_NO_CACHE_OC);

                f = createResult(FAILURE, "Child and object fetched by id don't match! Id: " + child.getId());
                addResult(results, assertEquals(child, objectById, null, f, false, false));

                // get object by path and compare
                List<String> paths = ((FileableCmisObject) child).getPaths();

                if (paths == null || paths.isEmpty()) {
                    addResult(results, createResult(FAILURE, "Child has no path! " + child.getId()));
                } else {
                    for (String path : paths) {
                        CmisObject objectByPath = session.getObjectByPath(path, SELECT_ALL_NO_CACHE_OC);

                        f = createResult(FAILURE, "Child and object fetched by path don't match! Id: " + child.getId()
                                + " / Path: " + path);
                        addResult(results, assertEquals(child, objectByPath, null, f, false, false));

                        f = createResult(FAILURE, "Object fetched by id and object fetched by path don't match! Id: "
                                + child.getId() + " / Path: " + path);
                        addResult(results, assertEquals(objectById, objectByPath, null, f, true, true));
                    }
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
                    addResult(results, assertStringNullOrEmpty(type.getParentTypeId(), null, f));
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
                        } catch (CmisInvalidArgumentException e) {
                            addResult(
                                    results,
                                    createResult(WARNING,
                                            "Allowed Source Type Ids contain a type id that doesn't exist: " + typeId));
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
                        } catch (CmisInvalidArgumentException e) {
                            addResult(
                                    results,
                                    createResult(WARNING,
                                            "Allowed Target Type Ids contain a type id that doesn't exist: " + typeId));
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

    protected CmisTestResult assertEquals(CmisObject expected, CmisObject actual, CmisTestResult success,
            CmisTestResult failure, boolean checkAcls, boolean checkPolicies) {

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

        if (expected.getProperties().size() != actual.getProperties().size()) {
            f = createResult(FAILURE, "Number of properties don't match");
            addResult(results, assertEquals(expected.getProperties().size(), actual.getProperties().size(), null, f));
        } else {
            for (Property<?> expectedProperty : expected.getProperties()) {
                Property<?> actualProperty = actual.getProperty(expectedProperty.getId());

                f = createResult(FAILURE, "Properties don't match! Property: " + expectedProperty.getId());
                addResult(results, assertEquals(expectedProperty, actualProperty, null, f));
            }
        }

        f = createResult(FAILURE, "Allowable actions don't match!");
        addResult(results, assertEquals(expected.getAllowableActions(), actual.getAllowableActions(), null, f));

        if (checkAcls) {
            f = createResult(FAILURE, "ACLs don't match!");
            addResult(results, assertEquals(expected.getAcl(), actual.getAcl(), null, f));
        }

        if (checkPolicies) {
            f = createResult(FAILURE, "Policies don't match!");
            addResult(results, assertEqualObjectList(expected.getPolicies(), actual.getPolicies(), null, f));
        }

        f = createResult(FAILURE, "Relationships don't match!");
        addResult(results, assertEqualObjectList(expected.getRelationships(), actual.getRelationships(), null, f));

        f = createResult(FAILURE, "Renditions don't match!");
        addResult(results, assertEqualRenditionLists(expected.getRenditions(), actual.getRenditions(), null, f));

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

    protected CmisTestResult assertEqualObjectList(List<? extends CmisObject> expected,
            List<? extends CmisObject> actual, CmisTestResult success, CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected list of CMIS objects is null, but actual list of CMIS objects is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual list of CMIS objects is null, but expected list of CMIS objects is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (expected.size() != actual.size()) {
            addResult(
                    results,
                    createResult(
                            CmisTestResultStatus.INFO,
                            "Object list sizes don't match! expected: " + expected.size() + " / actual: "
                                    + actual.size()));
        } else {
            for (int i = 0; i < expected.size(); i++) {
                f = createResult(FAILURE, "Objects at position " + i + "  dont't match!");
                addResult(results, assertEquals(expected.get(i), actual.get(i), null, f, true, false));
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

    protected CmisTestResult assertEquals(Property<?> expected, Property<?> actual, CmisTestResult success,
            CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected property is null, but actual property is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual property is null, but expected property is not!");
            addResultChild(failure, f);

            return failure;
        }

        f = createResult(FAILURE, "Property definitions don't match!");
        addResult(results, assertEquals(expected.getDefinition(), actual.getDefinition(), null, f));

        f = createResult(FAILURE, "Property values don't match!");
        addResult(results, assertEqualLists(expected.getValues(), actual.getValues(), null, f));

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

    protected CmisTestResult assertEquals(AllowableActions expected, AllowableActions actual, CmisTestResult success,
            CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected allowable actions are null, but actual allowable actions are not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual allowable actions are null, but expected allowable actions are not!");
            addResultChild(failure, f);

            return failure;
        }

        f = createResult(FAILURE, "Allowable action sets don't match!");
        addResult(results, assertEqualSet(expected.getAllowableActions(), actual.getAllowableActions(), null, f));

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

    protected CmisTestResult assertEquals(Acl expected, Acl actual, CmisTestResult success, CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected ACL is null, but actual ACL is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual ACL is null, but expected ACL is not!");
            addResultChild(failure, f);

            return failure;
        }

        f = createResult(FAILURE, "ACEs don't match!");
        addResult(results, assertEqualAceLists(expected.getAces(), actual.getAces(), null, f));

        f = createResult(FAILURE, "Exact flags dont't match!");
        addResult(results, assertEquals(expected.isExact(), actual.isExact(), null, f));

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

    protected CmisTestResult assertEqualAceLists(List<Ace> expected, List<Ace> actual, CmisTestResult success,
            CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if (expected == null && actual == null) {
            return success;
        }

        if (expected == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Expected ACE list is null!"));
        }

        if (actual == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Actual ACE list is null!"));
        }

        if (expected.size() != actual.size()) {
            addResult(
                    results,
                    createResult(CmisTestResultStatus.INFO, "ACE list sizes don't match! expected: " + expected.size()
                            + " / actual: " + actual.size()));
        } else {
            for (int i = 0; i < expected.size(); i++) {
                f = createResult(FAILURE, "ACEs at position " + i + "  dont't match!");
                addResult(results, assertEquals(expected.get(i), actual.get(i), null, f));
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

    protected CmisTestResult assertEquals(Ace expected, Ace actual, CmisTestResult success, CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected ACE is null, but actual ACE is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual ACE is null, but expected ACE is not!");
            addResultChild(failure, f);

            return failure;
        }

        f = createResult(FAILURE, "Principal ids dont't match!");
        addResult(results, assertEquals(expected.getPrincipalId(), actual.getPrincipalId(), null, f));

        f = createResult(FAILURE, "Permissions dont't match!");
        addResult(results, assertEqualLists(expected.getPermissions(), actual.getPermissions(), null, f));

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

    protected CmisTestResult assertEqualRenditionLists(List<Rendition> expected, List<Rendition> actual,
            CmisTestResult success, CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if (expected == null && actual == null) {
            return success;
        }

        if (expected == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Expected rendition list is null!"));
        }

        if (actual == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Actual rendition list is null!"));
        }

        if (expected.size() != actual.size()) {
            addResult(
                    results,
                    createResult(
                            CmisTestResultStatus.INFO,
                            "Rendition list sizes don't match! expected: " + expected.size() + " / actual: "
                                    + actual.size()));
        } else {
            for (int i = 0; i < expected.size(); i++) {
                f = createResult(FAILURE, "Renditions at position " + i + "  dont't match!");
                addResult(results, assertEquals(expected.get(i), actual.get(i), null, f));
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

    protected CmisTestResult assertEquals(Rendition expected, Rendition actual, CmisTestResult success,
            CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected rendition is null, but actual rendition is not!");
            addResultChild(failure, f);

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual rendition is null, but expected rendition is not!");
            addResultChild(failure, f);

            return failure;
        }

        f = createResult(FAILURE, "Stream ids dont't match!");
        addResult(results, assertEquals(expected.getStreamId(), actual.getStreamId(), null, f));

        f = createResult(FAILURE, "Kinds dont't match!");
        addResult(results, assertEquals(expected.getKind(), actual.getKind(), null, f));

        f = createResult(FAILURE, "MIME types dont't match!");
        addResult(results, assertEquals(expected.getMimeType(), actual.getMimeType(), null, f));

        f = createResult(FAILURE, "Titles dont't match!");
        addResult(results, assertEquals(expected.getTitle(), actual.getTitle(), null, f));

        f = createResult(FAILURE, "Lengths dont't match!");
        addResult(results, assertEquals(expected.getLength(), actual.getLength(), null, f));

        f = createResult(FAILURE, "Heights dont't match!");
        addResult(results, assertEquals(expected.getBigHeight(), actual.getBigHeight(), null, f));

        f = createResult(FAILURE, "Widths dont't match!");
        addResult(results, assertEquals(expected.getBigWidth(), actual.getBigWidth(), null, f));

        f = createResult(FAILURE, "Rendition document ids dont't match!");
        addResult(results, assertEquals(expected.getRenditionDocumentId(), actual.getRenditionDocumentId(), null, f));

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

    protected CmisTestResult assertEquals(ContentStream expected, ContentStream actual, CmisTestResult success,
            CmisTestResult failure) {

        List<CmisTestResult> results = new ArrayList<CmisTestResult>();

        CmisTestResult f;

        if ((expected == null) && (actual == null)) {
            return success;
        }

        if (expected == null) {
            f = createResult(FAILURE, "Expected stream is null, but actual stream is not!");
            addResultChild(failure, f);

            try {
                actual.getStream().close();
            } catch (Exception e) {
            }

            return failure;
        }

        if (actual == null) {
            f = createResult(FAILURE, "Actual object is null, but expected object is not!");
            addResultChild(failure, f);

            try {
                expected.getStream().close();
            } catch (Exception e) {
            }

            return failure;
        }

        f = createResult(WARNING, "Filenames don't match!");
        addResult(results, assertEquals(expected.getFileName(), actual.getFileName(), null, f));

        f = createResult(FAILURE, "MIME types don't match!");
        addResult(results, assertEquals(expected.getMimeType(), actual.getMimeType(), null, f));

        f = createResult(WARNING, "Lengths don't match!");
        addResult(results, assertEquals(expected.getBigLength(), actual.getBigLength(), null, f));

        boolean match = true;

        BufferedInputStream as = new BufferedInputStream(actual.getStream());
        BufferedInputStream es = new BufferedInputStream(expected.getStream());

        try {
            int ab = 0;
            int eb = 0;

            while (true) {
                if (ab > -1) {
                    ab = as.read();
                }

                if (eb > -1) {
                    eb = es.read();
                }

                if (ab == -1 && eb == -1) {
                    break;
                }

                if (ab != eb) {
                    match = false;
                }
            }
        } catch (Exception e) {
            f = createResult(UNEXPECTED_EXCEPTION, e.getMessage(), e, false);
            addResultChild(failure, f);
        }

        if (!match) {
            f = createResult(FAILURE, "Content streams don't match!");
            addResultChild(failure, f);
        }

        try {
            actual.getStream().close();
        } catch (Exception e) {
        }

        try {
            expected.getStream().close();
        } catch (Exception e) {
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

    // --- helpers ---

    protected void addResult(List<CmisTestResult> results, CmisTestResult result) {
        if (result != null) {
            if (result instanceof CmisTestResultImpl) {
                ((CmisTestResultImpl) result).setStackTrace(getStackTrace());
            }

            results.add(result);
            if (result.isFatal()) {
                throw new FatalTestException(result.getMessage());
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
