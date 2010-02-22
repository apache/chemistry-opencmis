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
package org.apache.opencmis.fit.sample;

import static org.junit.Assert.assertNotNull;

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Sample test case that demonstrates how to build integration tests.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractSampleIT {

  private static Session fSession;

  /**
   * Returns the current Session object.
   */
  protected Session getSession() {
    return fSession;
  }

  /**
   * Returns a new Session object.
   */
  protected abstract Session createSession();

  @BeforeClass
  public static void setUpClass() {
    fSession = null;
  }

  @Before
  public void setUp() {
    if (fSession == null) {
      fSession = createSession();
    }
  }

  /**
   * Simple sample test.
   */
  @Test
  public void testRepositoryInfo() {
    // RepositoryInfo ri = getSession().getRepositoryInfo();
    // assertNotNull(ri);
  }
}
