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
package org.apache.opencmis.server.impl.dummy;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CmisRepositoryService;

/**
 * Implementation of a repository factory without back-end for test purposes.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class DummyServicesFactory extends AbstractServicesFactory {

  private static final String REPOSITORY_ID = "repository.id";
  private static final String REPOSITORY_ID_DEFAULT = "test-rep";

  private static final String REPOSITORY_NAME = "repository.name";
  private static final String REPOSITORY_NAME_DEFAULT = "Test Repository";

  private static final Log LOG = LogFactory.getLog(DummyServicesFactory.class.getName());

  private DummyRepositoryService fRepositoryService;
  private String fId;
  private String fName;

  @Override
  public void init(Map<String, String> parameters) {
    // get the id
    fId = parameters.get(REPOSITORY_ID);
    if ((fId == null) || (fId.trim().length() == 0)) {
      fId = REPOSITORY_ID_DEFAULT;
    }

    // get the name
    fName = parameters.get(REPOSITORY_NAME);
    if ((fName == null) || (fName.trim().length() == 0)) {
      fName = REPOSITORY_NAME_DEFAULT;
    }

    // create a repository service
    fRepositoryService = new DummyRepositoryService(fId, fName);

    LOG.info("Initialized dummy repository '" + fName + "' (" + fId + ")");
  }

  @Override
  public void destroy() {
    LOG.info("Destroyed dummy repository '" + fName + "' (" + fId + ")");
  }

  @Override
  public CmisRepositoryService getRepositoryService() {
    return fRepositoryService;
  }

}
