/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.chemistry.opencmis.jcr;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.jcr.impl.DefaultDocumentTypeHandler;
import org.apache.chemistry.opencmis.jcr.impl.DefaultFolderTypeHandler;
import org.apache.chemistry.opencmis.jcr.impl.DefaultUnversionedDocumentTypeHandler;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.core.TransientRepository;
import org.junit.After;
import org.junit.Before;

/**
 * Abstract class for the test cases are covering JCR compliance.
 * The Jackrabbit's TransientRepository class uses as content repository.   
 */
public abstract class AbstractJcrSessionTest {
    private TransientRepository transientRepository;
    private JcrRepository jcrRepository;
    private Session session;
    private JcrTypeManager typeManager;
    private static final String MOUNT_PATH = "/";


    @Before
    public void setUp() throws Exception {
        transientRepository = new TransientRepository();
        session = transientRepository.login(new SimpleCredentials("adminId", "admin".toCharArray()));
        typeManager = new JcrTypeManager();
        PathManager pathManger = new PathManager(MOUNT_PATH);
        JcrTypeHandlerManager typeHandlerManager = createTypeHandlerManager(pathManger, typeManager);
        jcrRepository = new JcrRepository(transientRepository, pathManger, typeManager, typeHandlerManager);
    }

    @After
    public void tearDown() throws Exception {
        transientRepository.shutdown();
    }

    private static JcrTypeHandlerManager createTypeHandlerManager(PathManager pathManager, JcrTypeManager typeManager) {
        JcrTypeHandlerManager typeHandlerManager = new JcrTypeHandlerManager(pathManager, typeManager);
        typeHandlerManager.addHandler(new DefaultFolderTypeHandler());
        typeHandlerManager.addHandler(new DefaultDocumentTypeHandler());
        typeHandlerManager.addHandler(new DefaultUnversionedDocumentTypeHandler());
        return typeHandlerManager;
    }

    protected Session getSession() {
        try {
            //initialization of the UserManager
            ((JackrabbitSession) session).getUserManager();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        return session;
    }

    protected JcrRepository getJcrRepository() {
        return jcrRepository;
    }

    protected ObjectData getRootFolder(){
        return getJcrRepository().getObjectByPath(getSession(),
                MOUNT_PATH, null, false, false, null, false);
    }
}
