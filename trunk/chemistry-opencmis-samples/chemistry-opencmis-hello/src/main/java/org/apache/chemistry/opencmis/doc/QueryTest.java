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
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

public class QueryTest {
    private static final String CMIS_ENDPOINT_TEST_SERVER = "http://localhost:8080/inmemory/atom";
    private Session session;
    
    private void getCmisClientSession(){
        // default factory implementation
        SessionFactory factory = SessionFactoryImpl.newInstance();
        Map<String, String> parameters = new HashMap<String, String>();
        // user credentials
        parameters.put(SessionParameter.USER, "dummyuser");
        parameters.put(SessionParameter.PASSWORD, "dummysecret");
        // connection settings
        parameters.put(SessionParameter.ATOMPUB_URL, 
                CMIS_ENDPOINT_TEST_SERVER );
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB 
                .value());
        // create session
        session =  factory.getRepositories(parameters).get(0).createSession();
    }

    public void createTestArea()
            throws Exception
            {

        //creating a new folder
        Folder root = session.getRootFolder();
        Map<String, Object> folderProperties = new HashMap<String, Object>();
        folderProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        folderProperties.put(PropertyIds.NAME, "testdata");

        Folder newFolder = root.createFolder(folderProperties);
        //create a new content in the folder
        String name = "testdata1.txt";
        // properties
        // (minimal set: name and object type id)
        Map<String, Object> contentProperties = new HashMap<String, Object>();
        contentProperties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        contentProperties.put(PropertyIds.NAME, name);

        // content
        byte[] content = "CMIS Testdata One".getBytes();
        InputStream stream = new ByteArrayInputStream(content);
        ContentStream contentStream = new ContentStreamImpl(name, new BigInteger(content), "text/plain", stream);

        // create a major version
        Document newContent1 =  newFolder.createDocument(contentProperties, contentStream, null);
        System.out.println("Document created: " + newContent1.getId());
    }

    private void doQuery() {
        ItemIterable<QueryResult> results = session.query("SELECT * FROM cmis:folder WHERE cmis:name='testdata'", false);
        for (QueryResult result : results) {
            String id = result.getPropertyValueById(PropertyIds.OBJECT_ID);
            System.out.println("doQuery() found id: " + id);
        }
    }
    
    public static void main(String args[]) {
        QueryTest o = new QueryTest();
        try {
            o.getCmisClientSession();
            o.createTestArea();
            o.doQuery();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void xmain(String args[]) {
        
        System.out.println(Hello.class.getName() + " started");

        // Create a SessionFactory and set up the SessionParameter map
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameter = new HashMap<String, String>();

        // connection settings - we're connecting to a public cmis repo,
        // using the AtomPUB binding
        parameter.put(SessionParameter.ATOMPUB_URL, "http://localhost:8080/inmemory/atom/");
        parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

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

        System.out.println("Got a connection to repository: " + repository.getName() + ", with id: "
                + repository.getId());

//        // Get everything in the root folder and print the names of the objects
//        Folder root = session.getRootFolder();
//        ItemIterable<CmisObject> children = root.getChildren();
//        System.out.println("Found the following objects in the root folder:-");
//        for (CmisObject o : children) {
//            System.out.println(o.getName());
//        }
//        
        System.out.println(QueryTest.class.getName() + " ended");
    }
}
