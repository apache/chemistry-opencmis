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
package org.apache.opencmis.fileshare;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.impl.Converter;
import org.apache.opencmis.commons.impl.JaxBHelper;
import org.apache.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.opencmis.server.spi.AbstractServicesFactory;
import org.apache.opencmis.server.spi.CmisAclService;
import org.apache.opencmis.server.spi.CmisNavigationService;
import org.apache.opencmis.server.spi.CmisObjectService;
import org.apache.opencmis.server.spi.CmisRepositoryService;
import org.apache.opencmis.server.spi.CmisVersioningService;
import org.apache.opencmis.server.support.AclServiceWrapper;
import org.apache.opencmis.server.support.NavigationServiceWrapper;
import org.apache.opencmis.server.support.ObjectServiceWrapper;
import org.apache.opencmis.server.support.RepositoryServiceWrapper;
import org.apache.opencmis.server.support.VersioningServiceWrapper;

/**
 * Services Factory for file share back-end.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ServicesFactory extends AbstractServicesFactory {

  private static final String PREFIX_LOGIN = "login.";
  private static final String PREFIX_REPOSITORY = "repository.";
  private static final String PREFIX_TYPE = "type.";
  private static final String SUFFIX_READWRITE = ".readwrite";
  private static final String SUFFIX_READONLY = ".readonly";

  private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200);
  private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50);
  private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10);
  private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);

  private static final Log log = LogFactory.getLog(AbstractServicesFactory.class);

  private RepositoryMap fRepositoryMap;
  private TypeManager fTypeManager;

  private CmisRepositoryService fRepositoryService;
  private CmisNavigationService fNavigationService;
  private CmisObjectService fObjectService;
  private CmisVersioningService fVersioningService;
  private CmisAclService fAclService;

  public ServicesFactory() {
  }

  @Override
  public void init(Map<String, String> parameters) {
    fRepositoryMap = new RepositoryMap();
    fTypeManager = new TypeManager();

    readConfiguration(parameters);

    // create service object with wrappers
    fRepositoryService = new RepositoryServiceWrapper(new RepositoryService(fRepositoryMap),
        DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES);
    fNavigationService = new NavigationServiceWrapper(new NavigationService(fRepositoryMap),
        DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);
    fObjectService = new ObjectServiceWrapper(new ObjectService(fRepositoryMap),
        DEFAULT_MAX_ITEMS_OBJECTS);
    fVersioningService = new VersioningServiceWrapper(new VersioningService(fRepositoryMap));
    fAclService = new AclServiceWrapper(new AclService(fRepositoryMap));
  }

  @Override
  public CmisRepositoryService getRepositoryService() {
    return fRepositoryService;
  }

  @Override
  public CmisNavigationService getNavigationService() {
    return fNavigationService;
  }

  @Override
  public CmisObjectService getObjectService() {
    return fObjectService;
  }

  @Override
  public CmisVersioningService getVersioningService() {
    return fVersioningService;
  }

  @Override
  public CmisAclService getAclService() {
    return fAclService;
  }

  // ---- helpers ----

  private void readConfiguration(Map<String, String> parameters) {
    List<String> keys = new ArrayList<String>(parameters.keySet());
    Collections.sort(keys);

    for (String key : keys) {
      if (key.startsWith(PREFIX_LOGIN)) {
        // get logins
        String usernameAndPassword = replaceSystemProperties(parameters.get(key));
        if (usernameAndPassword == null) {
          continue;
        }

        String username = usernameAndPassword;
        String password = "";

        int x = usernameAndPassword.indexOf(':');
        if (x > -1) {
          username = usernameAndPassword.substring(0, x);
          password = usernameAndPassword.substring(x + 1);
        }

        fRepositoryMap.addLogin(username, password);

        log.info("Added login '" + username + "'.");
      }
      else if (key.startsWith(PREFIX_TYPE)) {
        // load type definition
        TypeDefinition type = loadType(replaceSystemProperties(parameters.get(key)));
        if (type != null) {
          fTypeManager.addType(type);
        }
      }
      else if (key.startsWith(PREFIX_REPOSITORY)) {
        // configure repositories
        String repositoryId = key.substring(PREFIX_REPOSITORY.length()).trim();
        int x = repositoryId.lastIndexOf('.');
        if (x > 0) {
          repositoryId = repositoryId.substring(0, x);
        }

        if (repositoryId.length() == 0) {
          throw new IllegalArgumentException("No repository id!");
        }

        if (key.endsWith(SUFFIX_READWRITE)) {
          // read-write users
          FileShareRepository fsr = fRepositoryMap.getRepository(repositoryId);
          for (String user : split(parameters.get(key))) {
            fsr.addUser(replaceSystemProperties(user), false);
          }
        }
        else if (key.endsWith(SUFFIX_READONLY)) {
          // read-only users
          FileShareRepository fsr = fRepositoryMap.getRepository(repositoryId);
          for (String user : split(parameters.get(key))) {
            fsr.addUser(replaceSystemProperties(user), true);
          }
        }
        else {
          // new repository
          String root = replaceSystemProperties(parameters.get(key));
          FileShareRepository fsr = new FileShareRepository(repositoryId, root, fTypeManager);

          fRepositoryMap.addRepository(fsr);

          log.info("Added repository '" + fsr.getRepositoryId() + "': " + root);
        }
      }
    }
  }

  private List<String> split(String csl) {
    if (csl == null) {
      return Collections.emptyList();
    }

    List<String> result = new ArrayList<String>();
    for (String s : csl.split(",")) {
      result.add(s.trim());
    }

    return result;
  }

  private String replaceSystemProperties(String s) {
    if (s == null) {
      return null;
    }

    StringBuilder result = new StringBuilder();
    StringBuilder property = null;
    boolean inProperty = false;

    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);

      if (inProperty) {
        if (c == '}') {
          String value = System.getProperty(property.toString());
          if (value != null) {
            result.append(value);
          }
          inProperty = false;
        }
        else {
          property.append(c);
        }
      }
      else {
        if (c == '{') {
          property = new StringBuilder();
          inProperty = true;
        }
        else {
          result.append(c);
        }
      }
    }

    return result.toString();
  }

  @SuppressWarnings("unchecked")
  private TypeDefinition loadType(String filename) {
    TypeDefinition result = null;

    try {
      Unmarshaller u = JaxBHelper.createUnmarshaller();
      JAXBElement<CmisTypeDefinitionType> type = (JAXBElement<CmisTypeDefinitionType>) u
          .unmarshal(new File(filename));
      result = Converter.convert(type.getValue());
    }
    catch (Exception e) {
      log.info("Could not load type: '" + filename + "'", e);
    }

    return result;
  }
}
