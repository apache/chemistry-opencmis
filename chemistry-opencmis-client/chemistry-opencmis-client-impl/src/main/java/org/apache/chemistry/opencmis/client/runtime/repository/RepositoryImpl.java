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
package org.apache.chemistry.opencmis.client.runtime.repository;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;

public class RepositoryImpl extends RepositoryInfoImpl implements Repository {

    private static final long serialVersionUID = 1L;

    private final Map<String, String> parameters;
    private final SessionFactory sessionFactory;

    /**
     * Constructor.
     */
    public RepositoryImpl(RepositoryInfo data, Map<String, String> parameters, SessionFactory sessionFactory) {
        super(data);

        this.parameters = new HashMap<String, String>(parameters);
        this.parameters.put(SessionParameter.REPOSITORY_ID, getId());

        this.sessionFactory = sessionFactory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.opencmis.client.api.repository.Repository#createSession()
     */
    @SuppressWarnings("unchecked")
    public <T extends Session> T createSession() {
        return (T) sessionFactory.createSession(parameters);
    }
}
