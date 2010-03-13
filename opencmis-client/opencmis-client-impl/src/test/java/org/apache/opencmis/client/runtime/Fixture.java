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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.SessionFactory;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;

/**
 * Definition of unit environment for running test cases. Default implementation
 * supports InMemory binding of OpenCMIS which can be used for stand alone test
 * cases. Within test unit suite it is possible to overwrite the fixture.
 * 
 */
public class Fixture {

	private static Log log = LogFactory.getLog(Fixture.class);

	public static String PROPERTY_FILTER = "*";
	/*
	 * general
	 */
	public static String TEST_ROOT_FOLDER_NAME = null;
	public static String FOLDER_TYPE_ID = "test.folder";
	public static String DOCUMENT_TYPE_ID = "test.file";
	public static String QUERY = "SELECT * FROM cmis:document";

	/*
	 * cmis objects
	 */
	public static String FOLDER1_NAME = "folder.1";
	public static String FOLDER2_NAME = "folder.2";
	public static String DOCUMENT1_NAME = "document.1.txt";
	public static String DOCUMENT2_NAME = "document.2.txt";

	/*
	 * properties
	 */
	public static String PROPERTY_NAME_STRING = "StringProperty";
	public static String PROPERTY_VALUE_STRING = "abc";
	public static String PROPERTY_NAME_INTEGER = "IntegerProperty";
	public static Integer PROPERTY_VALUE_INTEGER = new Integer(4711);
	public static String PROPERTY_NAME_BOOLEAN = "BooleanProperty";
	public static Boolean PROPERTY_VALUE_BOOLEAN = new Boolean(true);
	public static String PROPERTY_NAME_DOUBLE = "DoubleProperty";
	public static Double PROPERTY_VALUE_DOUBLE = new Double(1.0);
	public static String PROPERTY_NAME_FLOAT = "FloatProperty";
	public static Float PROPERTY_VALUE_FLOAT = new Float(1.0);
	public static String PROPERTY_NAME_ID = "DoubleProperty";
	public static String PROPERTY_VALUE_ID = "xyz";
	public static String PROPERTY_NAME_HTML = "HtmlProperty";
	public static String PROPERTY_VALUE_HTML = "<html><body>html value</body></html>";
	public static String PROPERTY_NAME_DATETIME = "DateTimeProperty";
	public static Calendar PROPERTY_VALUE_DATETIME = GregorianCalendar
			.getInstance();
	public static String PROPERTY_NAME_URI = "UriProperty";
	public static URI PROPERTY_VALUE_URI = URI.create("http://foo.com");
	public static final String PROPERTY_NAME_STRING_MULTI_VALUED = "MultiValuedStringProperty";

	/*
	 * test data setup
	 */
	private static DataSetup testData = new DataSetup();

	static {
		Fixture.TEST_ROOT_FOLDER_NAME = "test_" + UUID.randomUUID().toString();
	}

	/**
	 * @return session parameter
	 */
	public static Map<String, String> getParamter() {
		return parameter;
	}

	/**
	 * Overwriting default session parameter.
	 * 
	 * @param paramter
	 */
	public static void setParamter(Map<String, String> paramter) {
		Fixture.parameter = paramter;

		/* overload fixture values */
		String value;

		value = Fixture.parameter.get(TestSessionParameter.DOCUMENT_TYPE_ID);
		Fixture.DOCUMENT_TYPE_ID = (value != null ? value
				: Fixture.DOCUMENT_TYPE_ID);

		value = Fixture.parameter.get(TestSessionParameter.FOLDER_TYPE_ID);
		Fixture.FOLDER_TYPE_ID = (value != null ? value
				: Fixture.FOLDER_TYPE_ID);
	}

	/**
	 * session parameter.
	 */
	private static Map<String, String> parameter = null;

	/**
	 * Overwriting default session factory.
	 * 
	 * @param factory
	 */
	public static void setSessionFactory(SessionFactory factory) {
		Fixture.factory = factory;
	}

	/**
	 * @return factory
	 */
	public static SessionFactory getSessionFactory() {
		return Fixture.factory;
	}

	/**
	 * factory
	 */
	private static SessionFactory factory = null;

	static {
		// Mock as default
		Fixture.init();
	}

	public static void init() {
		/* get optional path from system properties */
		String pathname = System.getProperty(TestSessionParameter.CONFIG_PATH);
		pathname = (pathname != null) ? pathname.trim() : null;
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
				// get default settings
				InputStream in = Fixture.class
						.getResourceAsStream("/mock.properties");
				properties = new Properties();
				properties.load(in);
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
			Fixture.setParamter(sessionParameter);

			/* load factory class */
			factoryClassName = sessionParameter
					.get(TestSessionParameter.SESSION_FACTORY);
			if (factoryClassName != null
					&& !"".equalsIgnoreCase(factoryClassName)) {
				Class<?> clazz = Class.forName(factoryClassName);
				factory = (SessionFactory) clazz.newInstance();
			} else {
				/* default */
				factory = SessionFactoryImpl.newInstance();
			}
			Fixture.setSessionFactory(factory);
		} catch (IOException e) {
			Fixture.log.error(pathname, e);
			throw new CmisRuntimeException(pathname, e);
		} catch (Exception e) {
			Fixture.log.error(factoryClassName, e);
			throw new CmisRuntimeException(factoryClassName, e);
		}
	}

	public static void setUpTestData(Session session) {
		Fixture.testData.setup();
	}

	public static void teardownTestData(Session session) {
		Fixture.testData.teardown();
	}

	private static boolean isLogged = false;

	public static void logHeader() {
		if (!Fixture.isLogged) {
			/*
			 * log header only once
			 */
			Fixture.log
					.info("---------------------------------------------------------------");
			Fixture.log
					.info("--- OpenCMIS Client Test Suite --------------------------------");
			Fixture.log
					.info("---------------------------------------------------------------");
			Fixture.log.info("config path (prop): "
					+ System.getProperty(TestSessionParameter.CONFIG_PATH));
			Fixture.log.info("session factory:    "
					+ Fixture.getSessionFactory().getClass());
			Fixture.log.info("session parameter:  " + Fixture.getParamter());

			Fixture.log
					.info("---------------------------------------------------------------");

			Fixture.isLogged = true;
		}
	}

}
