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
package org.apache.chemistry.opencmis.fit.runtime;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runners.model.FrameworkMethod;

/**
 * Definition of unit environment for running test cases. Default implementation
 * supports InMemory binding of OpenCMIS which can be used for stand alone test
 * cases. Within test unit suite it is possible to overwrite the fixture.
 * 
 */
public class Fixture {

    private String connectionPath = Fixture.CONNECTION_ATOM_PATH;
    private static final String CONNECTION_ATOM_PATH = "/inmemory.atom.properties";
    private static final String CONNECTION_WS_PATH = "/inmemory.ws.properties";
    public static final String SESSION_FACTORY = "org.apache.chemistry.opencmis.fit.runtime.session.factory";
    public static final String TEST_ROOT_FOLDER_ID = "org.apache.chemistry.opencmis.fit.runtime.root.folder.id";

    private static Log log = LogFactory.getLog(Fixture.class);

    /*
     * general
     */
    public static String TEST_ROOT_FOLDER_NAME = "fit_" + UUID.randomUUID().toString();

    /*
     * test data setup
     */
    private FixtureSetup testData = new FixtureSetup(this);

    /**
     * @return session parameter
     */
    public Map<String, String> getParamter() {
        return parameter;
    }

    /**
     * Overwriting default session parameter.
     * 
     * @param paramter
     */
    public void setParamter(Map<String, String> paramter) {
        FixtureData.changeValues(paramter);
        this.parameter = paramter;
    }

    /**
     * session parameter.
     */
    private Map<String, String> parameter = null;

    /**
     * Overwriting default session factory.
     * 
     * @param factory
     */
    public void setSessionFactory(SessionFactory factory) {
        this.factory = factory;
    }

    /**
     * @return factory
     */
    public SessionFactory getSessionFactory() {
        return this.factory;
    }

    /**
     * factory
     */
    private SessionFactory factory = null;

    public Fixture() {
    }

    public void init() {
        /* get optional path from system properties */
        Properties properties = null;
        Map<String, String> sessionParameter = null;
        SessionFactory factory = null;
        String factoryClassName = null;
        try {
            // get settings
            InputStream in = Fixture.class.getResourceAsStream(this.connectionPath);
            properties = new Properties();
            properties.load(in);

            /* convert to map, filter empty values */
            sessionParameter = new Hashtable<String, String>();
            for (Entry<Object, Object> se : properties.entrySet()) {
                String key = (String) se.getKey();
                String value = ((String) se.getValue()).trim();
                if (value != null && !"".equalsIgnoreCase(value)) {
                    sessionParameter.put(key, value);
                }
            }
            this.setParamter(sessionParameter);

            /* load factory class */
            factoryClassName = sessionParameter.get(Fixture.SESSION_FACTORY);
            if (factoryClassName != null && !"".equalsIgnoreCase(factoryClassName)) {
                Class<?> clazz = Class.forName(factoryClassName);
                factory = (SessionFactory) clazz.newInstance();
            } else {
                /* default */
                factory = SessionFactoryImpl.newInstance();
            }
            this.setSessionFactory(factory);
        } catch (Exception e) {
            Fixture.log.error(factoryClassName, e);
            throw new CmisRuntimeException(factoryClassName, e);
        }
    }

    public void setUpTestData(Session session) {
        this.testData.setup();
    }

    public void teardownTestData(Session session) {
        this.testData.teardown();
    }

    private static boolean isHeaderLogged = false;

    public static void logHeader() {
        if (!Fixture.isHeaderLogged) {
            /*
             * log header only once
             */
            Fixture.log.info("---------------------------------------------------------------");
            Fixture.log.info("--- OpenCMIS FIT Test Suite -----------------------------------");
            Fixture.log.info("---------------------------------------------------------------");

            Fixture.isHeaderLogged = true;
        }
    }

    public <T> void logTestClassContext(Class<T> c, FrameworkMethod method) {
        Log l = LogFactory.getLog(c);
        l.info("---------------------------------------------------------------");
        l.info("test class:         " + c.getName());
        l.info("test method:        " + method.getName());
        l.info("session factory:    " + this.getSessionFactory().getClass());
        l.info("test root id:       " + this.getTestRootId());
//        l.info("session parameter:  " + this.getParamter());
        l.info("---------------------------------------------------------------");
    }

    public void enableAtomPub() {
        this.connectionPath = Fixture.CONNECTION_ATOM_PATH;
    }

    public void enableWebServices() {
        this.connectionPath = Fixture.CONNECTION_WS_PATH;
    }

    public String getTestRootId() {
        return this.testData.getTestRootId();
    }

}
