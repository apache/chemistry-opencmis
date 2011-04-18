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
package org.apache.chemistry.opencmis.fit.runtime.misc;

import java.util.Hashtable;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.fit.runtime.Fixture;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.rules.TestWatchman;
import org.junit.runners.model.FrameworkMethod;

/**
 * Independent session creation test (read only)
 */
public abstract class AbstractReadOnlyCreateSessionIT {

    protected Log log = LogFactory.getLog(this.getClass());

    /**
     * trace each junit error
     */
    @Rule
    public MethodRule watch = new TestWatchman() {
        @Override
        public void failed(Throwable e, FrameworkMethod method) {
            super.failed(e, method);
            AbstractReadOnlyCreateSessionIT.this.log.error(method.getName(), e);
        }

        @Override
        public void starting(FrameworkMethod method) {
            super.starting(method);

            AbstractReadOnlyCreateSessionIT.this.fixture.logTestClassContext(
                    AbstractReadOnlyCreateSessionIT.this.getClass(), method);
        }
    };

    protected Fixture fixture = null;

    @Before
    public void setup() {
        this.init();
    }

    protected abstract void init();

    @Test
    public void createDefaultSession() {
        SessionFactory factory = this.fixture.getSessionFactory();

        Hashtable<String, String> parameter = new Hashtable<String, String>(this.fixture.getParamter());

        Session s = factory.createSession(parameter);
        Assert.assertNotNull(s);
    }
}
