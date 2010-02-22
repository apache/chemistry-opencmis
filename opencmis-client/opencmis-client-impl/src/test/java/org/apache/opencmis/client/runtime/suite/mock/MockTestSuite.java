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
package org.apache.opencmis.client.runtime.suite.mock;

import java.util.HashMap;
import java.util.Map;

import org.apache.opencmis.client.runtime.Fixture;
import org.apache.opencmis.client.runtime.ReadOnlyAclCapabilityTest;
import org.apache.opencmis.client.runtime.ReadOnlyContentStreamTest;
import org.apache.opencmis.client.runtime.ReadOnlyCreateSessionTest;
import org.apache.opencmis.client.runtime.ReadOnlyDiscoverTest;
import org.apache.opencmis.client.runtime.ReadOnlyNavigationTest;
import org.apache.opencmis.client.runtime.ReadOnlyObjectTest;
import org.apache.opencmis.client.runtime.ReadOnlyRepositoryInfoTest;
import org.apache.opencmis.client.runtime.ReadOnlySessionTest;
import org.apache.opencmis.client.runtime.ReadOnlyTypeTest;
import org.apache.opencmis.client.runtime.misc.CacheTest;
import org.apache.opencmis.client.runtime.mock.MockSessionFactory;
import org.apache.opencmis.client.runtime.suite.AbstractCmisTestSuite;
import org.apache.opencmis.commons.SessionParameter;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Test suite to run API Mock
 */
@RunWith(MockTestSuite.class)
@SuiteClasses( { ReadOnlyCreateSessionTest.class, ReadOnlySessionTest.class,
    ReadOnlyRepositoryInfoTest.class, ReadOnlyAclCapabilityTest.class, ReadOnlyObjectTest.class,
    ReadOnlyTypeTest.class, ReadOnlyNavigationTest.class, ReadOnlyContentStreamTest.class,
    ReadOnlyDiscoverTest.class, CacheTest.class })
public class MockTestSuite extends AbstractCmisTestSuite {

  public MockTestSuite(Class<?> klass, RunnerBuilder r) throws InitializationError {
    super(klass, r);
  }

  @Override
  protected void initializeFixture() {
    Map<String, String> parameter = new HashMap<String, String>();

    parameter.put(SessionParameter.USER, "Mr. Mock");
    parameter.put(SessionParameter.PASSWORD, "*mock#");
    parameter.put(SessionParameter.LOCALE_ISO639_LANGUAGE, "EN");

    Fixture.setSessionFactory(new MockSessionFactory());
    Fixture.setParamter(parameter);
  }

}
