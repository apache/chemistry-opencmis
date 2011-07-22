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
package org.apache.chemistry.opencmis.doc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.RelationshipType;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.AclCapabilities;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

public class GettingStarted {
    public static void main(String args[]) {

        System.out.println("Getting Started...");
        System.out.println("------------------");

        // Create a SessionFactory and set up the SessionParameter map
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        // user credentials - using the standard admin/admin

        parameter.put(SessionParameter.USER, "admin");
        parameter.put(SessionParameter.PASSWORD, "admin");

        // connection settings - we're connecting to a public cmis repo,
        // using the AtomPUB binding, but there are other options here,
        // or you can substitute your own URL
        parameter.put(SessionParameter.ATOMPUB_URL, 
        "http://repo.opencmis.org/inmemory/atom/");
        // "http://cmis.alfresco.com/cmisatom");
        // "http://cmis.alfresco.com/service/cmis");
        // "http://localhost:8080/alfresco/service/api/cmis");
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

        System.out.println("Accessing ATOMPUB_URL: " + parameter.get(SessionParameter.ATOMPUB_URL)
                + " userid: " + parameter.get(SessionParameter.USER) + " password: "
                + parameter.get(SessionParameter.PASSWORD));

        // find all the repositories at this URL - there should only be one.
        List<Repository> repositories = new ArrayList<Repository>();
        repositories = sessionFactory.getRepositories(parameter);
        for (Repository r : repositories) {
            System.out.println("Found repository: " + r.getName());
        }

        // create session with the first (and only) repository
        Repository repository = repositories.get(0);
        parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
        Session session = sessionFactory.createSession(parameter);

        System.out.println("Got a connection to repository: " + repository.getName()
                + ", with id: " + repository.getId());

        // // An example of creating a session with a known repository id.
        // parameter.put(SessionParameter.REPOSITORY_ID, "A1");
        // Session session = sessionFactory.createSession(parameter);

        // Remove anything that was created by a previous run of this program
        cleanup(session);

        // Get everything in the root folder and print the names of the objects
        Folder root = session.getRootFolder();
        ItemIterable<CmisObject> children = root.getChildren();
        System.out.println("Found the following objects in the root folder:-");
        for (CmisObject o : children) {
            System.out.println(o.getName() + " which is of type " + o.getType().getDisplayName());
        }

        System.out.println("\nFile and Folders...");
        System.out.println("-------------------");

        // Add a new folder to the root folder
        System.out.println("Creating 'ADGNewFolder' in the root folder");
        Map<String, String> newFolderProps = new HashMap<String, String>();
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, "ADGNewFolder");
        Folder newFolder = root.createFolder(newFolderProps);

        // Did it work?
        children = root.getChildren();
        System.out.println("Now finding the following objects in the root folder:-");
        for (CmisObject o : children) {
            System.out.println(o.getName());
        }

        // Create a simple text document in the new folder
        // First, create the content stream
        final String textFileName = "test.txt";
        System.out.println("creating a simple text document, " + textFileName);
        String mimetype = "text/plain; charset=UTF-8";
        String content = "This is some test content.";
        String filename = textFileName;

        byte[] buf = null;
        try {
            buf = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ByteArrayInputStream input = new ByteArrayInputStream(buf);
        ContentStream contentStream = session.getObjectFactory().createContentStream(filename,
                buf.length, mimetype, input);

        // Create the Document Object
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, filename);
        ObjectId id = newFolder.createDocument(properties, contentStream, VersioningState.NONE);

        // Did it work?
        // Get the contents of the document by id
        Document doc = (Document) session.getObject(id);
        try {
            content = getContentAsString(doc.getContentStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get the contents of the document by path
        String path = newFolder.getPath() + "/" + textFileName;
        System.out.println("Getting object by path " + path);
        doc = (Document) session.getObjectByPath(path);
        try {
            content = getContentAsString(doc.getContentStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Contents of " + doc.getName() + " are: " + content);

        // Create Document Object with no content stream
        System.out.println("creating a document  called testNoContent with no ContentStream");
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, "testNoContent");
        newFolder.createDocument(properties, null, VersioningState.NONE);

        // Create a new document and then update its name
        final String textFileName2 = "test2.txt";
        System.out.println("creating a simple text document, " + textFileName2);
        mimetype = "text/plain; charset=UTF-8";
        content = "This is some test content for our second document.";
        filename = textFileName2;

        buf = null;
        try {
            buf = content.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        input = new ByteArrayInputStream(buf);
        contentStream = session.getObjectFactory().createContentStream(filename, buf.length,
                mimetype, input);
        properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, filename);
        ObjectId id2 = newFolder.createDocument(properties, contentStream, VersioningState.NONE);

        Document doc2 = (Document) session.getObject(id2);
        System.out.println("renaming " + doc2.getName() + " to test3.txt");
        properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, "test3.txt");
        id2 = doc2.updateProperties(properties);
        System.out.println("renamed to " + doc2.getName());

        // Update the content stream
        if (!session.getRepositoryInfo().getCapabilities().getContentStreamUpdatesCapability()
                .equals(CapabilityContentStreamUpdates.ANYTIME)) {
            System.out.println("update without checkout not supported in this repository");
        } else {
            System.out.println("updating content stream");
            content = "This is some updated test content for our renamed second document.";
            buf = null;
            try {
                buf = content.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            input = new ByteArrayInputStream(buf);
            contentStream = session.getObjectFactory().createContentStream("test3.txt", buf.length,
                    mimetype, input);
            properties = new HashMap<String, Object>();
            properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            properties.put(PropertyIds.NAME, "test3.txt");
            doc2.setContentStream(contentStream, true);

            // did it work?
            try {
                content = getContentAsString(doc2.getContentStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("Contents of " + doc2.getName() + " are: " + content);
        }

        // Force and handle a CmisInvalidArgumentException exception
        System.out.println("\nExceptions...");
        System.out.println("-------------");
        System.out.println("Forcing and handling a CmisInvalidArgumentException");
        try {
            doc2.setContentStream(null, false);
        } catch (CmisInvalidArgumentException e1) {
            System.out.println("caught an " + e1.getClass().getName() + " exception with message "
                    + e1.getMessage());
        }

        // delete a document
        System.out.println("\nMore files and folders...");
        System.out.println("-------------------------");
        children = newFolder.getChildren();
        System.out.println("Now finding the following objects in our folder:-");
        for (CmisObject o : children) {
            System.out.println(o.getName());
        }
        System.out.println("Deleting document " + doc2.getName());
        doc2.delete(true);
        System.out.println("Now finding the following objects in our folder:-");
        for (CmisObject o : children) {
            System.out.println(o.getName());
        }

        // Create a new folder tree, and delete it
        System.out.println("Creating 'ADGFolder1' in the root folder");
        newFolderProps = new HashMap<String, String>();
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, "ADGFolder1");
        Folder folder1 = root.createFolder(newFolderProps);
        newFolderProps.put(PropertyIds.NAME, "ADGFolder11");
        Folder folder11 = folder1.createFolder(newFolderProps);
        newFolderProps.put(PropertyIds.NAME, "ADGFolder12");
        Folder folder12 = folder1.createFolder(newFolderProps);
        System.out.println("delete the 'ADGFolder1' tree");
        folder1.deleteTree(true, UnfileObject.DELETE, true);

        // Create a folder tree to navigate through
        System.out.println("Creating folder tree for navigation");
        newFolderProps = new HashMap<String, String>();
        HashMap<String, String> newFileProps = new HashMap<String, String>();

        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, "ADGFolder1");
        folder1 = root.createFolder(newFolderProps);

        newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newFileProps.put(PropertyIds.NAME, "ADGFile1f1");
        folder1.createDocument(newFileProps, contentStream, VersioningState.NONE);

        newFolderProps.put(PropertyIds.NAME, "ADGFolder11");
        folder11 = folder1.createFolder(newFolderProps);

        newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newFileProps.put(PropertyIds.NAME, "ADGFile11f1");
        folder11.createDocument(newFileProps, contentStream, VersioningState.NONE);

        newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newFileProps.put(PropertyIds.NAME, "ADGFile11f2");
        folder11.createDocument(newFileProps, contentStream, VersioningState.NONE);

        newFolderProps.put(PropertyIds.NAME, "ADGFolder111");
        folder11.createFolder(newFolderProps);

        newFolderProps.put(PropertyIds.NAME, "ADGFolder112");
        folder11.createFolder(newFolderProps);

        newFolderProps.put(PropertyIds.NAME, "ADGFolder12");
        folder12 = folder1.createFolder(newFolderProps);

        newFolderProps.put(PropertyIds.NAME, "ADGFolder121");
        Folder folder121 = folder12.createFolder(newFolderProps);

        newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        newFileProps.put(PropertyIds.NAME, "ADGFile121f1");
        folder121.createDocument(newFileProps, contentStream, VersioningState.NONE);

        newFolderProps.put(PropertyIds.NAME, "ADGFolder122");
        folder12.createFolder(newFolderProps);

        // Navigating the object tree
        System.out.println("\nNavigating the object tree...");
        System.out.println("-----------------------------");

        // Get the children of folder1
        children = folder1.getChildren();
        System.out.println("Children of " + folder1.getName() + ":-");
        for (CmisObject o : children) {
            System.out.println(o.getName());
        }

        // Get the descendants of folder1
        if (!session.getRepositoryInfo().getCapabilities().isGetDescendantsSupported()) {
            System.out.println("getDescendants not supported in this repository");
        } else {
            System.out.println("Descendants of " + folder1.getName() + ":-");
            for (Tree<FileableCmisObject> t : folder1.getDescendants(-1)) {
                printTree(t);
            }
        }

        // Get the foldertree of folder1
        if (!session.getRepositoryInfo().getCapabilities().isGetFolderTreeSupported()) {
            System.out.println("getFolderTree not supported in this repository");
        } else {
            System.out.println("Foldertree for " + folder1.getName() + ":-");
            for (Tree<FileableCmisObject> t : folder1.getFolderTree(-1)) {
                printFolderTree(t);
            }
        }

        // Paging
        System.out.println("\nPaging...");
        System.out.println("--------");
        System.out.println("Creating folders for paging example");
        newFolderProps = new HashMap<String, String>();
        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        newFolderProps.put(PropertyIds.NAME, "ADGFolderPaging");
        Folder folderPaging = root.createFolder(newFolderProps);
        createFolders(folderPaging, 10);

        System.out.println("Getting page of length 3 from item 5");
        OperationContext operationContext = new OperationContextImpl();
        operationContext.setMaxItemsPerPage(3);
        ItemIterable<CmisObject> children1 = folderPaging.getChildren(operationContext);
        int count = 0;
        for (CmisObject child : children1.skipTo(5).getPage()) {
            System.out.println("object " + count + " in page of " + children1.getPageNumItems()
                    + " is " + child.getName());
            count++;
        }

        System.out.println("Getting complete result set in pages of 3");
        operationContext = new OperationContextImpl();
        operationContext.setMaxItemsPerPage(3);
        children1 = folderPaging.getChildren(operationContext);
        int pageNumber = 0;
        boolean finished = false;
        count= 0;
        while (!finished) {
            ItemIterable<CmisObject> currentPage = children1.skipTo(count).getPage();
            System.out.println("page " + pageNumber + " has " + currentPage.getPageNumItems() + " items");
            for (CmisObject item : currentPage) {
                System.out.println("object " + count +  " is " + item.getName());
                count++;
            }
            pageNumber++;
            if (!currentPage.getHasMoreItems())
                finished = true;
        }

        // Types
        System.out.println("\nTypes...");
        System.out.println("--------");
        // Look at the type definition
        System.out.println("Getting type definition for doc");
        ObjectType objectType = session.getTypeDefinition(doc.getType().getId());
        System.out.println("doc is of type " + objectType.getDisplayName());
        System.out.println("isBaseType() returns " + (objectType.isBaseType() ? "true" : "false"));
        ObjectType baseType = objectType.getBaseType();
        if (baseType == null) {
            System.out.println("getBaseType() returns null");
        } else {
            System.out.println("getBaseType() returns " + baseType.getDisplayName());
        }
        ObjectType parentType = objectType.getParentType();
        if (parentType == null) {
            System.out.println("getParentType() returns null");
        } else {
            System.out.println("getParentType() returns " + parentType.getDisplayName());
        }
        System.out.println("Listing child types of " + objectType.getDisplayName());
        for (ObjectType o : objectType.getChildren()) {
            System.out.println("\t" + o.getDisplayName());
        }
        System.out.println("Getting immediate descendant types of " + objectType.getDisplayName());
        for (Tree<ObjectType> o : objectType.getDescendants(1)) {
            System.out.println("\t" + o.getItem().getDisplayName());
        }

        System.out.println("\nProperties...");
        System.out.println("-------------");
        // Look at all the properties of the document
        System.out.println(doc.getName() + " properties start");
        List<Property<?>> props = doc.getProperties();
        for (Property<?> p : props) {
            System.out.println(p.getDefinition().getDisplayName() + "=" + p.getValuesAsString());
        }
        System.out.println(doc.getName() + " properties end");

        // Get some document properties explicitly
        System.out.println("VersionLabel property on " + doc.getName() + " is "
                + doc.getVersionLabel());
        // System.out.println("Is this the latest version of " + doc.getName() +
        // " ?:  "
        // + (doc.isLatestVersion() ? "yes" : "no"));

        // get a property by id
        System.out.println("get property by property id");
        Property<?> someProperty = props.get(0);
        System.out.println(someProperty.getDisplayName() + " property on " + doc.getName()
                + " (by getPropertValue()) is " + doc.getPropertyValue(someProperty.getId()));

        // get a property by query name
        System.out.println("get property by query name");
        if (session.getRepositoryInfo().getCapabilities().getQueryCapability()
                .equals(CapabilityQuery.METADATAONLY)) {
            System.out.println("Full search not supported");
        } else {
            String query = "SELECT * FROM cmis:document WHERE cmis:name = 'test.txt'";
            ItemIterable<QueryResult> queryResult = session.query(query, false);
            for (QueryResult item : queryResult) {
                System.out.println("property cmis:createdBy on test.txt is "
                        + item.getPropertyByQueryName("cmis:createdBy").getFirstValue());
            }
        }

        GregorianCalendar calendar = doc.getCreationDate();
        String DATE_FORMAT = "yyyyMMdd";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        System.out.println("Creation date of " + doc.getName() + " is  "
                + sdf.format(calendar.getTime()));

        System.out.println("\nQuery...");
        System.out.println("--------");
        // Query 1 - need full query capability for this
        if (session.getRepositoryInfo().getCapabilities().getQueryCapability()
                .equals(CapabilityQuery.METADATAONLY)) {
            System.out.println("Full search not supported");
        } else {
            String query = "SELECT * FROM cmis:document WHERE cmis:name LIKE 'test%'";
            ItemIterable<QueryResult> q = session.query(query, false);

            // Did it work?
            System.out.println("***results from query " + query);

            int i = 1;
            for (QueryResult qr : q) {
                System.out.println("--------------------------------------------\n" + i + " , "
                        + qr.getPropertyByQueryName("cmis:objectTypeId").getFirstValue() + " , "
                        + qr.getPropertyByQueryName("cmis:name").getFirstValue() + " , "
                        + qr.getPropertyByQueryName("cmis:createdBy").getFirstValue() + " , "
                        + qr.getPropertyByQueryName("cmis:objectId").getFirstValue() + " , "
                        + qr.getPropertyByQueryName("cmis:contentStreamFileName").getFirstValue()
                        + " , "
                        + qr.getPropertyByQueryName("cmis:contentStreamMimeType").getFirstValue()
                        + " , "
                        + qr.getPropertyByQueryName("cmis:contentStreamLength").getFirstValue());
                i++;
            }

            // Query 2
            query = "SELECT * FROM cmis:document WHERE cmis:name LIKE 'test%.txt' AND CONTAINS('test')";
            q = session.query(query, false);

            System.out.println("***results from query " + query);

            i = 1;
            for (QueryResult qr : q) {
                System.out.println("--------------------------------------------\n" + i + " , "
                        + qr.getPropertyByQueryName("cmis:objectTypeId").getFirstValue() + " , "
                        + qr.getPropertyByQueryName("cmis:name").getFirstValue() + " , "
                        + qr.getPropertyByQueryName("cmis:createdBy").getFirstValue() + " , "
                        + qr.getPropertyByQueryName("cmis:objectId").getFirstValue() + " , "
                        + qr.getPropertyByQueryName("cmis:contentStreamFileName").getFirstValue()
                        + " , "
                        + qr.getPropertyByQueryName("cmis:contentStreamMimeType").getFirstValue()
                        + " , "
                        + qr.getPropertyByQueryName("cmis:contentStreamLength").getFirstValue());
                i++;
            }
        }

        // Advanced use of types
        System.out.println("\nAdvanced use of types...");
        System.out.println("-------------------------");
        System.out.println("Finding an existing document of an advanced type");
        if (session.getRepositoryInfo().getCapabilities().getQueryCapability()
                .equals(CapabilityQuery.METADATAONLY)) {
            System.out.println("Full search not supported");
        } else {
            String query = "SELECT * FROM ia:calendarEvent";
            ItemIterable<QueryResult> queryResult = session.query(query, false);
            for (QueryResult item : queryResult) {
                System.out.println("Found "
                        + item.getPropertyByQueryName("cmis:name").getFirstValue() + " of type "
                        + item.getPropertyByQueryName("cmis:objectTypeId").getFirstValue());
                System.out.println("property ia:descriptionEvent is "
                        + item.getPropertyByQueryName("ia:descriptionEvent").getFirstValue());
                System.out.println("property ia:toDate is "
                        + item.getPropertyByQueryName("ia:toDate").getFirstValue());
                System.out.println("property ia:fromDate is "
                        + item.getPropertyByQueryName("ia:fromDate").getFirstValue());
            }
        }

        // Capabilities
        System.out.println("\nCapabilities...");
        System.out.println("---------------");
        // Check what capabilities our repository supports
        System.out.println("Printing repository capabilities...");
        final RepositoryInfo repInfo = session.getRepositoryInfo();
        RepositoryCapabilities cap = repInfo.getCapabilities();
        System.out.println("\nNavigation Capabilities");
        System.out.println("-----------------------");
        System.out.println("Get descendants supported: "
                + (cap.isGetDescendantsSupported() ? "true" : "false"));
        System.out.println("Get folder tree supported: "
                + (cap.isGetFolderTreeSupported() ? "true" : "false"));
        System.out.println("\nObject Capabilities");
        System.out.println("-----------------------");
        System.out.println("Content Stream: " + cap.getContentStreamUpdatesCapability().value());
        System.out.println("Changes: " + cap.getChangesCapability().value());
        System.out.println("Renditions: " + cap.getRenditionsCapability().value());
        System.out.println("\nFiling Capabilities");
        System.out.println("-----------------------");
        System.out.println("Multifiling supported: "
                + (cap.isMultifilingSupported() ? "true" : "false"));
        System.out.println("Unfiling supported: " + (cap.isUnfilingSupported() ? "true" : "false"));
        System.out.println("Version specific filing supported: "
                + (cap.isVersionSpecificFilingSupported() ? "true" : "false"));
        System.out.println("\nVersioning Capabilities");
        System.out.println("-----------------------");
        System.out
                .println("PWC searchable: " + (cap.isPwcSearchableSupported() ? "true" : "false"));
        System.out.println("PWC Updatable: " + (cap.isPwcUpdatableSupported() ? "true" : "false"));
        System.out.println("All versions searchable: "
                + (cap.isAllVersionsSearchableSupported() ? "true" : "false"));
        System.out.println("\nQuery Capabilities");
        System.out.println("-----------------------");
        System.out.println("Query: " + cap.getQueryCapability().value());
        System.out.println("Join: " + cap.getJoinCapability().value());
        System.out.println("\nACL Capabilities");
        System.out.println("-----------------------");
        System.out.println("ACL: " + cap.getAclCapability().value());
        System.out.println("End of  repository capabilities");

        System.out.println("\nAllowable actions...");
        System.out.println("--------------------");
        // find the current allowable actions for the test.txt document
        System.out.println("Getting the current allowable actions for the " + doc.getName()
                + " document object...");
        for (Action a : doc.getAllowableActions().getAllowableActions()) {
            System.out.println("\t" + a.value());
        }

        // find out if we can currently check out test.txt
        if (doc.getAllowableActions().getAllowableActions().contains(Action.CAN_CHECK_OUT)) {
            System.out.println("can check out " + doc.getName());
        } else {
            System.out.println("can not check out " + doc.getName());
        }

        System.out.println("\nMultifiling and Unfiling...");
        System.out.println("---------------------------");
        // Try out multifiling if it is supported
        System.out.println("Trying out multifiling");
        Folder newFolder2 = null;
        if (!(cap.isMultifilingSupported())) {
            System.out.println("Multifiling not supported by this repository");
        } else {
            // Add a new folder to the root folder
            System.out.println("Creating 'ADGNewFolder 2' in the root folder");
            newFolderProps = new HashMap<String, String>();
            newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            newFolderProps.put(PropertyIds.NAME, "ADGNewFolder 2");
            newFolder2 = root.createFolder(newFolderProps, null, null, null,
                    session.getDefaultContext());
            System.out.println("Adding " + textFileName + "to 'ADGNewFolder 2' in the root folder");
            doc.addToFolder(newFolder2, true);

            // Did it work?
            children = newFolder.getChildren();
            System.out.println("Now finding the following objects in the 'ADGNewFolder' folder:-");
            for (CmisObject o : children) {
                System.out.println(o.getName());
            }
            children = newFolder2.getChildren();
            System.out
                    .println("Now finding the following objects in the 'ADGNewFolder 2' folder:-");
            for (CmisObject o : children) {
                System.out.println(o.getName());
            }
        }

        // Try out unfiling if it is supported
        System.out.println("Trying out unfiling");
        if (!(cap.isUnfilingSupported())) {
            System.out.println("Unfiling not supported by this repository");
        } else {
            // remove our document from both folders
            System.out.println("removing: " + doc.getName() + " from 'ADGNewFolder':-");
            doc.removeFromFolder(newFolder);
            System.out.println("removing: " + doc.getName() + " from 'ADGNewFolder 2':-");
            doc.removeFromFolder(newFolder2);
            // Did it work?
            Document docTest = (Document) session.getObject(id);
            if (docTest != null) {
                System.out.println(docTest.getName() + " still exists");
            }

        }

        System.out.println("\nRelationships...");
        System.out.println("-----------------");

        // Check if the repo supports relationships
        ObjectType relationshipType = null;
        try {
            relationshipType = session.getTypeDefinition("cmis:relationship");
        } catch (CmisObjectNotFoundException e) {
            relationshipType = null;
        }

        if (relationshipType == null) {
            System.out.println("Repository does not support cmis:relationship objects");
        } else {
            ObjectType cmiscustomRelationshipType = null;
            try {
                cmiscustomRelationshipType = session.getTypeDefinition("R:cmiscustom:assoc");
            } catch (CmisObjectNotFoundException e) {
                cmiscustomRelationshipType = null;
            }
            if (cmiscustomRelationshipType == null) {
                System.out.println("Repository does not support R:cmiscustom:assoc objects");
            } else {

                System.out.println("Creating folders for relationships example");

                newFolderProps = new HashMap<String, String>();
                newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
                newFolderProps.put(PropertyIds.NAME, "ADGFolderAssociations");
                Folder folderAssociations = root.createFolder(newFolderProps);

                newFileProps = new HashMap<String, String>();
                newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "D:cmiscustom:document");
                newFileProps.put(PropertyIds.NAME, "ADGFileSource");
                Document sourceDoc = folderAssociations.createDocument(newFileProps, null,
                        VersioningState.MAJOR);
                newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
                newFileProps.put(PropertyIds.NAME, "ADGFileTarget");
                Document targetDoc = folderAssociations.createDocument(newFileProps, null,
                        VersioningState.MAJOR);

                Map<String, String> relProps = new HashMap<String, String>();
                relProps.put("cmis:sourceId", sourceDoc.getId());
                relProps.put("cmis:targetId", targetDoc.getId());
                relProps.put("cmis:objectTypeId", "R:cmiscustom:assoc");
                session.createRelationship(relProps, null, null, null);
                System.out.println("created relationship");

                operationContext = new OperationContextImpl();
                operationContext.setIncludeRelationships(IncludeRelationships.BOTH);
                ObjectType typeDefinition = session.getTypeDefinition("R:cmiscustom:assoc");
                RelationshipDirection direction = RelationshipDirection.EITHER;
                ItemIterable<Relationship> relationships = session.getRelationships(sourceDoc,
                        true, direction, typeDefinition, operationContext);
                int relationshipCount = 0;
                for (Relationship relationship : relationships) {
                    relationshipCount++;
                    System.out.println("found relationship " + relationshipCount);

                    // Look at allowable source and target type
                    RelationshipType relType = (RelationshipType) relationship.getType();

                    System.out.println(relType.getDisplayName()
                            + " has the following allowed source types:");
                    for (ObjectType objectType1 : relType.getAllowedSourceTypes()) {
                        System.out.println("\t" + objectType1.getDisplayName() + " with QueryName "
                                + objectType1.getQueryName());
                    }

                    System.out.println(relType.getDisplayName()
                            + " has the following allowed target types:");
                    for (ObjectType objectType1 : relType.getAllowedTargetTypes()) {
                        System.out.println("\t" + objectType1.getDisplayName() + " with QueryName "
                                + objectType1.getQueryName());
                    }
                }

            }
        }

        System.out.println("\tAccess Control...");
        System.out.println("-----------------");

        // Check if the repo supports ACLs
        if (!session.getRepositoryInfo().getCapabilities().getAclCapability()
                .equals(CapabilityAcl.MANAGE)) {
            System.out.println("Repository does not allow ACL management");
        } else {
            System.out.println("Repository allows ACL management");

            System.out.println("Creating folders for permissions example");

            newFolderProps = new HashMap<String, String>();
            newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            newFolderProps.put(PropertyIds.NAME, "ADGFolderPermissions");
            Folder folderAssociations = session.getRootFolder().createFolder(newFolderProps);

            newFileProps = new HashMap<String, String>();
            contentStream = new ContentStreamImpl("permissions.txt", null, "plain/text",
                    new ByteArrayInputStream("some content".getBytes()));
            newFileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            newFileProps.put(PropertyIds.NAME, "ADGFilePermissions");
            Document testDoc = folderAssociations.createDocument(newFileProps, contentStream,
                    VersioningState.MAJOR);

            operationContext = new OperationContextImpl();
            operationContext.setIncludeAcls(true);
            testDoc = (Document) session.getObject(testDoc, operationContext);

            System.out.println("ACL before adding an ace...");
            for (Ace ace : testDoc.getAcl().getAces()) {
                System.out.println("Found ace: " + ace.getPrincipalId() + " toString "
                        + ace.toString());
            }

            List<String> permissions = new ArrayList<String>();
            permissions.add("cmis:write");
            String principal = "admin";
            Ace aceIn = session.getObjectFactory().createAce(principal, permissions);
            List<Ace> aceListIn = new ArrayList<Ace>();
            aceListIn.add(aceIn);
            testDoc.addAcl(aceListIn, AclPropagation.REPOSITORYDETERMINED);
            testDoc = (Document) session.getObject(testDoc, operationContext);

            System.out.println("ACL after adding an ace...");
            for (Ace ace : testDoc.getAcl().getAces()) {
                System.out.println("Found ace: " + ace.getPrincipalId() + " toString "
                        + ace.toString());
            }

            System.out.println("getting ACL capabilities");
            AclCapabilities aclCapabilities = session.getRepositoryInfo().getAclCapabilities();

            System.out.println("Propogation for this repository is "
                    + aclCapabilities.getAclPropagation().toString());

            System.out.println("permissions for this repository are: ");
            for (PermissionDefinition definition : aclCapabilities.getPermissions()) {
                System.out.println(definition.toString());
            }

            System.out.println("\npermission mappings for this repository are: ");
            Map<String, PermissionMapping> repoMapping = aclCapabilities.getPermissionMapping();
            for (String key : repoMapping.keySet()) {
                System.out.println(key + " maps to " + repoMapping.get(key).getPermissions());
            }
        }

        // Versioning
        System.out.println("\nVersioning...");
        System.out.println("-------------");
        // Check whether a document is versionable
        boolean versionable = false;
        if (((DocumentType) (doc.getType())).isVersionable()) {
            System.out.println(doc.getName() + " is versionable");
            versionable = true;
        } else {
            System.out.println(doc.getName() + " is NOT versionable");
        }

        // check out the latest version of test.txt, make some changes to the
        // PWC, and
        // check in the new version
        if (versionable) {
            Document pwc = (Document) session.getObject(doc.checkOut());
            try {
                content = getContentAsString(pwc.getContentStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            String updatedContents = content + "\nLine added in new version";

            try {
                buf = updatedContents.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            contentStream = session.getObjectFactory().createContentStream(
                    doc.getContentStream().getFileName(), buf.length,
                    doc.getContentStream().getMimeType(), new ByteArrayInputStream(buf));

            // Check in the pwc
            try {
                pwc.checkIn(false, null, contentStream, "minor version");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("checkin failed, trying to cancel the checkout");
                pwc.cancelCheckOut();
            }

            System.out.println("Document version history");
            {
                List<Document> versions = doc.getAllVersions();
                for (Document version : versions) {
                    System.out.println("\tname: " + version.getName());
                    System.out.println("\tversion label: " + version.getVersionLabel());
                    System.out.println("\tversion series id: " + version.getVersionSeriesId());
                    System.out.println("\tchecked out by: "
                            + version.getVersionSeriesCheckedOutBy());
                    System.out.println("\tchecked out id: "
                            + version.getVersionSeriesCheckedOutId());
                    System.out.println("\tmajor version: " + version.isMajorVersion());
                    System.out.println("\tlatest version: " + version.isLatestVersion());
                    System.out.println("\tlatest major version: " + version.isLatestMajorVersion());
                    System.out.println("\tcheckin comment: " + version.getCheckinComment());
                    System.out.println("\tcontent length: " + version.getContentStreamLength()
                            + "\n");
                }
            }
        }

        System.out.println("\nRenditions...");
        System.out.println("-------------");

        // Renditions - find all objects and check for renditions
        if (session.getRepositoryInfo().getCapabilities().getRenditionsCapability()
                .equals(CapabilityRenditions.NONE)) {
            System.out.println("Repository does not support renditions");
        } else {
            System.out
                    .println("Finding first object in repository with thumbnail renditions - start");
            Folder node = root;
            Stack<Folder> stack = new Stack<Folder>();
            while (node != null) {
                children = node.getChildren();
                for (CmisObject o : children) {
                    if ((o.getType().isBaseType() && o.getType().getId().equals("cmis:folder"))
                            || o.getBaseType().getId().equals("cmis:folder")) {
                        stack.push((Folder) o);
                    } else {
                        operationContext = session.createOperationContext();
                        operationContext.setRenditionFilterString("cmis:thumbnail");
                        CmisObject oo = session.getObject(o.getId(), operationContext);
                        List<Rendition> rl = oo.getRenditions();
                        if (!rl.isEmpty()) {
                            System.out.println("found  " + o.getName() + " of type "
                                    + o.getType().getDisplayName() + "that has renditions...");
                            for (Rendition rendition : rl) {
                                System.out.print("kind: " + rendition.getKind());
                                System.out.print("\tmimetype: " + rendition.getMimeType());
                                System.out.print("\twidth: " + rendition.getWidth());
                                System.out.print("\theight: " + rendition.getHeight());
                                System.out.println("\tstream id: " + rendition.getStreamId());
                            }
                            break; // Just show the first object with
                                   // renditions. Remove this
                                   // Break to show them all

                        }
                    }
                }
                if (stack.isEmpty()) {
                    node = null;
                } else {
                    node = (Folder) stack.pop();
                }
            }
            System.out
                    .println("Finding first object in repository with thumbnail renditions - end");
        }

        System.out.println("Getting Started...end of");
    }

    /**
     * 
     * @param folder
     */
    private static void printTree(Tree<FileableCmisObject> tree) {
        System.out.println("Descendant " + tree.getItem().getName());
        for (Tree<FileableCmisObject> t : tree.getChildren()) {
            printTree(t);
        }
    }

    /**
     * 
     * @param folder
     */
    private static void printFolderTree(Tree<FileableCmisObject> tree) {
        System.out.println("Folder " + tree.getItem().getName());
        for (Tree<FileableCmisObject> t : tree.getChildren()) {
            printFolderTree(t);
        }
    }

    /**
     * Cleans up any objects we created in a previous run of this program on the
     * supplied session
     * 
     * @param s
     *            an active cmis session
     */
    private static void cleanup(Session s) {
        System.out.println("Starting cleaning up repository");
        String[] folders = { "ADGNewFolder", "ADGNewFolder 2", "ADGFolder1", "ADGFolderPaging",
                "ADGFolderAssociations", "ADGFolderPermissions" };
        for (int i = 0; i < folders.length; i++) {
            String path = "/" + folders[i];

            System.out.println("finding and deleting folder tree " + path);
            CmisObject o = null;
            try {
                o = s.getObjectByPath(path);
            } catch (CmisObjectNotFoundException e) {
                // ignore
            }
            if (o != null) {
                try {
                    ((Folder) o).deleteTree(true, UnfileObject.DELETE, true);
                } catch (Exception e) {
                    // Ignore any failures
                }
            }
        }

        System.out.println("Finished cleaning up repository");

    }

    /**
     * Helper method to get the contents of a stream
     * 
     * @param stream
     * @return
     * @throws IOException
     */
    private static String getContentAsString(ContentStream stream) throws IOException {
        InputStream in2 = stream.getStream();
        StringBuffer sbuf = null;
        sbuf = new StringBuffer(in2.available());
        int count;
        byte[] buf2 = new byte[100];
        while ((count = in2.read(buf2)) != -1) {
            for (int i = 0; i < count; i++) {
                sbuf.append((char) buf2[i]);
            }
        }
        in2.close();
        return sbuf.toString();
    }

    /**
     * Creates size folders under root with names ADGFolder0, ADGFolder1,...,
     * 
     * @param root
     *            - parent folder
     * @param size
     *            - number of folders to create
     */
    private static void createFolders(Folder root, int size) {
        HashMap<String, String> newFolderProps = new HashMap<String, String>();

        newFolderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");

        for (int i = 0; i < size; i++) {
            newFolderProps.put(PropertyIds.NAME, "ADGFolder" + i);
            root.createFolder(newFolderProps);
        }
    }

}