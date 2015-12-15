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
package org.apache.chemistry.opencmis.client.runtime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.SessionFactoryFinder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.commons.endpoints.CmisAuthentication;
import org.junit.Test;

public class SessionFactoryFinderTest {

    @Test
    public void testFind() throws ClassNotFoundException, InstantiationException {
        SessionFactory sf = SessionFactoryFinder.find();

        assertNotNull(sf);
        assertTrue(sf instanceof SessionFactoryImpl);
    }

    @Test
    public void testFindWithClassloader() throws ClassNotFoundException, InstantiationException {
        SessionFactory sf = SessionFactoryFinder.find(null, this.getClass().getClassLoader());

        assertNotNull(sf);
        assertTrue(sf instanceof SessionFactoryImpl);
    }

    @Test()
    public void testFindWithProperty1() throws ClassNotFoundException, InstantiationException {
        System.setProperty("org.apache.chemistry.test.sessionfactory1", MockSessionFactory1.class.getName());

        SessionFactory sf = SessionFactoryFinder.find("org.apache.chemistry.test.sessionfactory1");

        assertNotNull(sf);
        assertTrue(sf instanceof MockSessionFactory1);
    }

    @Test(expected = ClassNotFoundException.class)
    public void testFindWithProperty2() throws ClassNotFoundException, InstantiationException {
        System.setProperty("org.apache.chemistry.test.sessionfactory2", SessionFactoryFinderTest.class.getName());

        SessionFactoryFinder.find("org.apache.chemistry.test.sessionfactory2");
    }

    @Test()
    public void testFindWithProperty3() throws ClassNotFoundException, InstantiationException {
        System.setProperty("org.apache.chemistry.test.sessionfactory3", MockSessionFactory2.class.getName());

        SessionFactory sf = SessionFactoryFinder.find("org.apache.chemistry.test.sessionfactory3");

        assertNotNull(sf);
        assertTrue(sf instanceof MockSessionFactory2);
    }

    public static class MockSessionFactory1 implements SessionFactory {

        public MockSessionFactory1() {
        }

        public Map<String, String> pepareSessionParameters(CmisAuthentication authentication) {
            return null;
        }

        @Override
        public Session createSession(Map<String, String> parameters) {
            return null;
        }

        @Override
        public List<Repository> getRepositories(Map<String, String> parameters) {
            return null;
        }
    }

    public static class MockSessionFactory2 extends SessionFactoryImpl {

        private static final long serialVersionUID = 1L;

        private MockSessionFactory2() {
        }

        public static MockSessionFactory2 newInstance() {
            return new MockSessionFactory2();
        }
    }
}
