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
import java.util.Locale;
import java.util.Map;

import org.apache.opencmis.client.runtime.Fixture;
import org.apache.opencmis.client.runtime.ReadOnlyAclCapabilityTest;
import org.apache.opencmis.client.runtime.ReadOnlyCreateSessionTest;
import org.apache.opencmis.client.runtime.ReadOnlyRepositoryInfoTest;
import org.apache.opencmis.client.runtime.ReadOnlySessionTest;
import org.apache.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.opencmis.client.runtime.misc.CacheTest;
import org.apache.opencmis.client.runtime.suite.AbstractCmisTestSuite;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.enums.BindingType;
import org.apache.opencmis.commons.enums.SessionType;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Test suite to run InMemory binding.
 */
@RunWith(OtxInMemoryCmisTestSuite.class)
@SuiteClasses( { CacheTest.class, ReadOnlyCreateSessionTest.class, ReadOnlySessionTest.class,
		ReadOnlyRepositoryInfoTest.class,
		ReadOnlyAclCapabilityTest.class })
public class OtxInMemoryCmisTestSuite extends AbstractCmisTestSuite {

	public OtxInMemoryCmisTestSuite(Class<?> klass, RunnerBuilder r)
			throws InitializationError {
		super(klass, r);
	}

	@Override
	protected void initializeFixture() {
		Map<String, String> parameter = new HashMap<String, String>();

		parameter.put(SessionParameter.USER, "test");
		parameter.put(SessionParameter.PASSWORD, "test");
		parameter.put(SessionParameter.SESSION_TYPE, SessionType.PERSISTENT
				.value());
		parameter.put(SessionParameter.LOCALE_ISO3166_COUNTRY, Locale.GERMANY
				.getISO3Country());
		parameter.put(SessionParameter.LOCALE_ISO639_LANGUAGE, Locale.GERMANY
				.getISO3Language());
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.CUSTOM
				.value());
		parameter.put(SessionParameter.REPOSITORY_ID, "InMemory");
		parameter.put(SessionParameter.BINDING_SPI_CLASS,
				"org.apache.opencmis.inmemory.clientprovider.CmisInMemorySpiFactory");

		Fixture.DOCUMENT_TYPE_ID = "cmis:document";
		Fixture.FOLDER_TYPE_ID = "cmis:folder";

		Fixture.setParamter(parameter);
		Fixture.setSessionFactory(SessionFactoryImpl.newInstance());
	}

}
