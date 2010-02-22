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
import org.apache.opencmis.client.runtime.ReadOnlyCreateSessionTest;
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
 * Test suite to run OTX AtomPub binding.
 */
@RunWith(OtxAtomPubCmisTestSuite.class)
@SuiteClasses( { CacheTest.class, ReadOnlyCreateSessionTest.class, ReadOnlySessionTest.class })
public class OtxAtomPubCmisTestSuite extends AbstractCmisTestSuite {

  public OtxAtomPubCmisTestSuite(Class<?> klass, RunnerBuilder r) throws InitializationError {
    super(klass, r);
  }

  @Override
  protected void initializeFixture() {
    Map<String, String> parameter = new HashMap<String, String>();

    parameter.put(SessionParameter.USER, "admin");
    parameter.put(SessionParameter.PASSWORD, "livelink");
    parameter.put(SessionParameter.SESSION_TYPE, SessionType.PERSISTENT.value());
    parameter.put(SessionParameter.LOCALE_ISO3166_COUNTRY, Locale.GERMANY.getISO3Country());
    parameter.put(SessionParameter.LOCALE_ISO639_LANGUAGE, Locale.GERMANY.getISO3Language());
    parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOM.value());
    parameter.put(SessionParameter.ATOMPUB_URL, "http://pwdf6227:8080/cmis/atom");
    parameter.put(SessionParameter.REPOSITORY_ID, "testApp");
    
    Fixture.DOCUMENT_TYPE_ID = "sap.doc";
    Fixture.FOLDER_TYPE_ID = "sap.folder";
    
//    -Dopencmis.test=true
//    -Dopencmis.test.username=admin
//    -Dopencmis.test.password=livelink
//    -Dopencmis.test.repository=testApp
//    -Dopencmis.test.testfolder=default/F34485
//    -Dopencmis.test.documenttype=sap.doc
//    -Dopencmis.test.foldertype=sap.folder
//    -Dopencmis.test.webservices.url=http://pwdf6227:8080/cmis/services/
//    -Dopencmis.test.atompub.url=http://pwdf6227:8080/cmis/atom
    
    Fixture.setParamter(parameter);
    Fixture.setSessionFactory(SessionFactoryImpl.newInstance());
  }

}
