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

import org.apache.opencmis.commons.provider.AccessControlPrincipalData;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AccessControlPrincipalDataImpl extends AbstractExtensionData implements
    AccessControlPrincipalData {

  private String fPrincipalId;

  /**
   * Constructor.
   */
  public AccessControlPrincipalDataImpl() {
  }

  /**
   * Constructor with principal id.
   */
  public AccessControlPrincipalDataImpl(String principalId) {
    setPrincipalId(principalId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.AccessControlPrincipalData#getPrincipalId()
   */
  public String getPrincipalId() {
    return fPrincipalId;
  }

  public void setPrincipalId(String principalId) {
    fPrincipalId = principalId;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Access Control Principal [principalId=" + fPrincipalId + "]" + super.toString();
  }
}
