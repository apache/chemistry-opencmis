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
package org.apache.opencmis.client.provider.atompub;

import java.util.HashMap;
import java.util.Map;

import org.apache.opencmis.client.provider.factory.CmisProviderFactory;
import org.apache.opencmis.commons.SessionParameter;
import org.apache.opencmis.commons.provider.CmisProvider;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AtomPubTestProviderFactory {

  public static CmisProvider createProvider(String atomPubUrl, String user, String password) {
    // gather parameters
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(SessionParameter.USER, user);
    parameters.put(SessionParameter.PASSWORD, password);

    parameters.put(SessionParameter.ATOMPUB_URL, atomPubUrl);

    // get factory and create provider
    CmisProviderFactory factory = CmisProviderFactory.newInstance();
    CmisProvider provider = factory.createCmisAtomPubProvider(parameters);

    return provider;
  }
}
