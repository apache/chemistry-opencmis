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
package org.apache.opencmis.client.runtime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.SessionFactory;
import org.junit.After;
import org.junit.Before;

/**
 * Create a OpenCMIS test session based on fixture parameter.
 */
public abstract class AbstractSessionTest {

  protected Log log = LogFactory.getLog(this.getClass());

  /**
   * test session
   */
  protected Session session = null;

  @Before
  public void setUp() throws Exception {
    SessionFactory factory = Fixture.getSessionFactory();
    this.session = factory.createSession(Fixture.getParamter());

    Fixture.setUpTestData(this.session);
  }

  @After
  public void tearDown() throws Exception {
    Fixture.teardownTestData(this.session);
  }
}
