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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.opencmis.server.spi.CallContext;

/**
 * Repository map.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class RepositoryMap {

  private Map<String, FileShareRepository> fMap;
  private Map<String, String> fLogins;

  public RepositoryMap() {
    fMap = new HashMap<String, FileShareRepository>();
    fLogins = new HashMap<String, String>();
  }

  /**
   * Adds a repository object.
   */
  public void addRepository(FileShareRepository fsr) {
    if ((fsr == null) || (fsr.getRepositoryId() == null)) {
      return;
    }

    fMap.put(fsr.getRepositoryId(), fsr);
  }

  /**
   * Gets a repository object by id.
   */
  public FileShareRepository getRepository(String repositoryId) {
    // get repository object
    FileShareRepository result = fMap.get(repositoryId);
    if (result == null) {
      throw new CmisObjectNotFoundException("Unknown repository '" + repositoryId + "'!");
    }

    return result;
  }

  /**
   * Gets a repository object by id.
   */
  public FileShareRepository getAuthenticatedRepository(CallContext context, String repositoryId) {
    // check user and password first
    if (!authenticate(context.getUsername(), context.getPassword())) {
      throw new CmisPermissionDeniedException();
    }

    // get repository object
    return getRepository(repositoryId);
  }

  /**
   * Returns all repository objects.
   */
  public Collection<FileShareRepository> getRepositories() {
    return fMap.values();
  }

  /**
   * Adds a login.
   */
  public void addLogin(String username, String password) {
    if ((username == null) || (password == null)) {
      return;
    }

    fLogins.put(username.trim(), password);
  }

  /**
   * Authenticates a user against the configured logins.
   */
  private boolean authenticate(String username, String password) {
    String pwd = fLogins.get(username);
    if (pwd == null) {
      return false;
    }

    return pwd.equals(password);
  }
}
