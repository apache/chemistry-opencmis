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
package org.apache.opencmis.client.runtime.suite;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.opencmis.client.api.SessionFactory;
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
import org.apache.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.opencmis.client.runtime.misc.CacheTest;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * All session parameters have to be configured as environment variables.
 */
/**
 * SessionParameter are build from environment variables. If a config file
 * parameter is specified then session parameters are read from this file else
 * the whole environment variable map is used for session parameter. All entries
 * have to follow the {@code org.apache.opencmis...} convention as defined in
 * class {@code SessionParameter0
 */
@RunWith(GenericTestSuite.class)
@SuiteClasses( { CacheTest.class, ReadOnlyCreateSessionTest.class,
		ReadOnlySessionTest.class, ReadOnlyRepositoryInfoTest.class,
		ReadOnlyAclCapabilityTest.class, ReadOnlyObjectTest.class,
		ReadOnlyTypeTest.class, ReadOnlyNavigationTest.class,
		ReadOnlyContentStreamTest.class, ReadOnlyDiscoverTest.class })
public class GenericTestSuite extends AbstractCmisTestSuite {

	private static final String CONFIG_PATH = "org.apache.opencmis.client.runtime.suite.config.path";
	private static final String SESSION_FACTORY = "org.apache.opencmis.client.runtime.suite.session.factory";

	public GenericTestSuite(Class<?> klass, RunnerBuilder r)
			throws InitializationError {
		super(klass, r);
	}

	@Override
	protected void initializeFixture() {
		/* get optional path from system properties */
		String pathname = System.getProperty(GenericTestSuite.CONFIG_PATH)
				.trim();
		Properties properties = null;
		Map<String, String> sessionParameter = null;
		SessionFactory factory = null;
		String factoryClassName = null;
		try {
			if (pathname != null && !"".equalsIgnoreCase(pathname)) {
				// read from file
				properties = new Properties();
				FileInputStream in = new FileInputStream(new File(pathname));
				properties.load(in);
			} else {
				// take all system properties
				properties = System.getProperties();
			}

			/* convert to map, filter empty values */
			sessionParameter = new Hashtable<String, String>();
			for (Entry<Object, Object> se : properties.entrySet()) {
				String key = (String) se.getKey();
				String value = ((String) se.getValue()).trim();
				if (value != null && !"".equalsIgnoreCase(value)) {
					sessionParameter.put(key, value);
				}
			}

			/* load factory class */
			factoryClassName = sessionParameter
					.get(GenericTestSuite.SESSION_FACTORY);
			if (factoryClassName != null
					&& !"".equalsIgnoreCase(factoryClassName)) {
				Class<?> clazz = Class.forName(factoryClassName);
				factory = (SessionFactory) clazz.newInstance();
			} else {
				/* default */
				factory = SessionFactoryImpl.newInstance();
			}

			/* activate fixture and done */
			Fixture.setParamter(sessionParameter);
			Fixture.setSessionFactory(factory);

		} catch (InstantiationException e) {
			throw new CmisRuntimeException(factoryClassName, e);
		} catch (IllegalAccessException e) {
			throw new CmisRuntimeException(factoryClassName, e);
		} catch (ClassNotFoundException e) {
			throw new CmisRuntimeException(factoryClassName, e);
		} catch (IOException e) {
			throw new CmisRuntimeException(pathname, e);
		}
	}
}
