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
package org.apache.jackrabbit.core.security;

import java.lang.reflect.Field;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.security.simple.SimpleSecurityManager;
import org.apache.jackrabbit.core.security.user.UserImpl;
import org.apache.jackrabbit.core.security.user.UserManagerImpl;

/**
 * Provides access to the observation journal. It represents current user as administrator.  
 * The reason to use one is SimpleSecurityManager doesn't support UserManager of a session.
 */
public class TestSecurityManager extends SimpleSecurityManager {

    private static class MockAdmin extends UserImpl {

        protected MockAdmin(UserManagerImpl userManager) {
            super(null, userManager);
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public boolean isDisabled() throws RepositoryException {
            return false;
        }

        @Override
        public boolean isAdmin() {
            return true;
        }

    }

    private class MockUserManager extends UserManagerImpl {

        public MockUserManager() throws RepositoryException {
            super((SessionImpl) getSystemSession(), "admin");
        }

        @Override
        public Authorizable getAuthorizable(String id) throws RepositoryException {
            return new MockAdmin(this);
        }
    }

    @Override
    public UserManager getUserManager(Session session) throws RepositoryException {
        return new MockUserManager();
    }

    protected Session getSystemSession() {
        try {
            Field sessionField;
            sessionField = SimpleSecurityManager.class.getDeclaredField("systemSession");
            sessionField.setAccessible(true);
            return (Session) sessionField.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
