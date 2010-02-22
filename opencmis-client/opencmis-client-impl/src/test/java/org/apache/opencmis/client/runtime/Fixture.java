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

import java.net.URI;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.SessionFactory;
import org.apache.opencmis.client.api.util.Testable;
import org.apache.opencmis.client.runtime.mock.MockSessionFactory;
import org.apache.opencmis.commons.SessionParameter;

/**
 * Definition of unit environment for running test cases. Default implementation
 * supports InMemory binding of OpenCMIS which can be used for stand alone test
 * cases. Within test unit suite it is possible to overwrite the fixture.
 * 
 */
public class Fixture {

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
	public static String PROPERTY_VALUE_HTML = "<body>";
	public static String PROPERTY_NAME_DATETIME = "DateTimeProperty";
	public static Calendar PROPERTY_VALUE_DATETIME = GregorianCalendar
			.getInstance();
	public static String PROPERTY_NAME_URI = "UriProperty";
	public static URI PROPERTY_VALUE_URI = URI.create("http://foo.com");
	public static final String PROPERTY_NAME_STRING_MULTI_VALUED = "MultiValuedStringProperty";

	static {
		Fixture.TEST_ROOT_FOLDER_NAME = "test_" + UUID.randomUUID().toString();
	}

	/**
	 * @return session parameter
	 */
	public static Map<String, String> getParamter() {
		return paramter;
	}

	/**
	 * Overwriting default session parameter.
	 * 
	 * @param paramter
	 */
	public static void setParamter(Map<String, String> paramter) {
		Fixture.paramter = paramter;
	}

	/**
	 * session parameter.
	 */
	private static Map<String, String> paramter = null;

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
		Map<String, String> parameter = new HashMap<String, String>();

		parameter.put(SessionParameter.USER, "Mr. Mock");
		parameter.put(SessionParameter.PASSWORD, "*mock#");
		parameter.put(SessionParameter.LOCALE_ISO639_LANGUAGE, "EN");

		Fixture.paramter = parameter;
		Fixture.factory = new MockSessionFactory();
	}

	public static void setUpTestData(Session session) {
		if (session instanceof Testable) {
			Map<String, String> p = new Hashtable<String, String>();
			p.put(Testable.FOLDER_TYPE_ID_PARAMETER, Fixture.FOLDER_TYPE_ID);
			p
					.put(Testable.DOCUMENT_TYPE_ID_PARAMETER,
							Fixture.DOCUMENT_TYPE_ID);
			((Testable) session).generateTestData(p);
		}
	}

	public static void teardownTestData(Session session) {
		if (session instanceof Testable) {
			((Testable) session).cleanUpTestData();
		}
	}
}
