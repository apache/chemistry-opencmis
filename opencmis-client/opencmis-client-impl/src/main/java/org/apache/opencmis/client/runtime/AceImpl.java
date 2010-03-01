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

import java.util.List;

import org.apache.opencmis.client.api.Ace;

/**
 * ACE implementation.
 */
public class AceImpl implements Ace {

  private String principalId;
  private List<String> permissions;
  private boolean isDirect;

  public AceImpl(String principalId, List<String> permissions, boolean isDirect) {
    this.principalId = principalId;
    this.permissions = permissions;
    this.isDirect = isDirect;
  }

  public String getPrincipalId() {
    return this.principalId;
  }

  public List<String> getPermissions() {
    return this.permissions;
  }

  public boolean isDirect() {
    return this.isDirect;
  }
}
