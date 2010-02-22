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

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.apache.opencmis.client.api.AclPermission;
import org.apache.opencmis.client.api.repository.AclPermissionMapping;
import org.apache.opencmis.client.api.repository.RepositoryAclCapabilities;
import org.apache.opencmis.client.api.repository.RepositoryCapabilities;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.CapabilityAcl;

public class ReadOnlyAclCapabilityTest extends AbstractSessionTest {

  private RepositoryAclCapabilities aclCapabilities = null;

  @Before
  public void setup() throws Exception {
    super.setUp();

    RepositoryInfo r = this.session.getRepositoryInfo();
    // capabilities
    RepositoryCapabilities repcap = r.getCapabilities();
    CapabilityAcl capacl = repcap.getAclSupport();

    if (capacl != CapabilityAcl.NONE) {
      // acl capabilities
      this.aclCapabilities = r.getAclCapabilities();
    }

    Assume.assumeNotNull(this.aclCapabilities);
  }

  @Test
  public void repositoryCapabilitiesAclPropagation() {
    AclPropagation aclprop = this.aclCapabilities.getAclPropagation();
    switch (aclprop) {
    case OBJECTONLY:
      break;
    case PROPAGATE:
      break;
    case REPOSITORYDETERMINED:
      break;
    default:
      Assert.fail("enumeration not supported");
    }
  }

  @Test
  public void repositoryCapabilitiesAclPermissionMapping() {
    AclPermissionMapping apm = this.aclCapabilities.getAddPolicyObjectPermissions();
    List<AclPermission> aclps = apm.getPermissions();
    Assert.assertNotNull(aclps);
  }
}
