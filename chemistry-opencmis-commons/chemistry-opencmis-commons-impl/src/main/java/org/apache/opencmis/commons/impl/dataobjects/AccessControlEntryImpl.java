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
package org.apache.opencmis.commons.impl.dataobjects;

import java.util.List;

import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlPrincipalData;

/**
 * Access Control Entry data implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AccessControlEntryImpl extends AbstractExtensionData implements AccessControlEntry {

  private List<String> fPermissions;
  private AccessControlPrincipalData fPrincipal;
  private boolean fIsDirect = true;

  /**
   * Constructor.
   */
  public AccessControlEntryImpl() {
  }

  /**
   * Constructor.
   */
  public AccessControlEntryImpl(AccessControlPrincipalData principal, List<String> permissions) {
    setPrincipal(principal);
    setPermissions(permissions);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.AccessControlEntry#getPrincipal()
   */
  public AccessControlPrincipalData getPrincipal() {
    return fPrincipal;
  }

  public void setPrincipal(AccessControlPrincipalData principal) {
    fPrincipal = principal;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.AccessControlEntry#getPermissions()
   */
  public List<String> getPermissions() {
    return fPermissions;
  }

  public void setPermissions(List<String> permissions) {
    fPermissions = permissions;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.AccessControlEntry#isDirect()
   */
  public boolean isDirect() {
    return fIsDirect;
  }

  public void setDirect(boolean direct) {
    fIsDirect = direct;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Access Control Entry [principal=" + fPrincipal + ", permissions=" + fPermissions
        + ", is direct=" + fIsDirect + "]" + super.toString();
  }
}
