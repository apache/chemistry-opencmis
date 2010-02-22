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
package org.apache.opencmis.client.runtime.suite.otx;

import java.util.HashMap;
import java.util.Map;

import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.apache.opencmis.client.runtime.Fixture;
import org.apache.opencmis.client.runtime.ReadOnlyAclCapabilityTest;
import org.apache.opencmis.client.runtime.ReadOnlyCreateSessionTest;
import org.apache.opencmis.client.runtime.ReadOnlyObjectTest;
import org.apache.opencmis.client.runtime.ReadOnlyRepositoryInfoTest;
import org.apache.opencmis.client.runtime.suite.AbstractCmisTestSuite;

/**
 * Test suite to run OTX SOAP binding.
 */

@RunWith(OtxWebServiceCmisTestSuite.class)
@SuiteClasses( { ReadOnlyCreateSessionTest.class, ReadOnlyRepositoryInfoTest.class,
    ReadOnlyAclCapabilityTest.class, ReadOnlyObjectTest.class })
public class OtxWebServiceCmisTestSuite extends AbstractCmisTestSuite {

  public OtxWebServiceCmisTestSuite(Class<?> klass, RunnerBuilder r) throws InitializationError {
    super(klass, r);
  }

  @Override
  protected void initializeFixture() {
    Map<String, String> parameter = new HashMap<String, String>();
    // parameter.put(Session.URL, "http://pwdf6227:8080/cmis/services");
    // parameter.put(Session.USER, "test");
    // parameter.put(Session.PASSWORD, "test");
    // parameter.put(Session.BINDING, "webservice");
    // parameter.put(Session.PROVIDER, "otx");
    // parameter.put(Session.REPOSITORY_ID, "myRepository");

    Fixture.setParamter(parameter);
    Fixture.setSessionFactory(null);
  }

}
