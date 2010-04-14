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
package org.apache.opencmis.fit.runtime;

import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.SessionFactory;
import org.apache.opencmis.client.api.TransientSession;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.enums.SessionType;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Independent session creation test (read only)
 */
public class ReadOnlyCreateSessionIT {

  protected Log log = LogFactory.getLog(this.getClass());

  @Before
  public void setup() {
  }

  @Test
  public void createDefaultSession() {
    SessionFactory factory = Fixture.getSessionFactory();

    Hashtable<String, String> parameter = new Hashtable<String, String>(Fixture.getParamter());
    parameter.remove(SessionParameter.SESSION_TYPE);

    Session s = factory.createSession(parameter);
    Assert.assertNotNull(s);
  }

  @Test
  public void createPersistentSession() {
    SessionFactory factory = Fixture.getSessionFactory();

    Hashtable<String, String> parameter = new Hashtable<String, String>(Fixture.getParamter());
    parameter.put(SessionParameter.SESSION_TYPE, SessionType.PERSISTENT.value());

    Session s = factory.createSession(parameter);
    Assert.assertNotNull(s);
  }

  @Test
  public void createTransientSession() {
    SessionFactory factory = Fixture.getSessionFactory();

    Hashtable<String, String> parameter = new Hashtable<String, String>(Fixture.getParamter());
    parameter.put(SessionParameter.SESSION_TYPE, SessionType.TRANSIENT.value());

    try {
      @SuppressWarnings("unused")
      TransientSession s = factory.createSession(parameter);
      Assert
          .fail("CmisNotSupportedException expected, because Transient Session is not supported yet.");
    }
    catch (CmisNotSupportedException e) {

    }
  }
}
