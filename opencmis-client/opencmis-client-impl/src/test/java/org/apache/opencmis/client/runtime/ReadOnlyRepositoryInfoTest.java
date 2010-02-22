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

import junit.framework.Assert;

import org.apache.opencmis.client.api.ChangeEvent;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.repository.RepositoryCapabilities;
import org.apache.opencmis.client.api.repository.RepositoryInfo;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.enums.CapabilityAcl;
import org.apache.opencmis.commons.enums.CapabilityChanges;
import org.apache.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.opencmis.commons.enums.CapabilityJoin;
import org.apache.opencmis.commons.enums.CapabilityQuery;
import org.apache.opencmis.commons.enums.CapabilityRendition;
import org.apache.opencmis.commons.enums.TypeOfChanges;
import org.junit.Test;

/**
 * Testing folder and files.
 */
public class ReadOnlyRepositoryInfoTest extends AbstractSessionTest {

  @Test
  public void changesIncomplete() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.changesIncomplete());
  }

  @Test
  public void changesOnType() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getChangesOnType());
  }

  @Test
  public void cmisVersionSupported() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getCmisVersionSupported());
    this.log.info("getCmisVersionSupported = " + r.getCmisVersionSupported());
  }

  @Test
  public void description() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getDescription());
    this.log.info("getDescription = " + r.getDescription());
  }

  @Test
  public void id() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getId());
    this.log.info("getId = " + r.getId());
  }

  @Test
  public void name() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getName());
    this.log.info("getName = " + r.getName());
  }

  @Test
  public void principalIdAnonymous() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getPrincipalIdAnonymous());
    this.log.info("getPrincipalIdAnonymous = " + r.getPrincipalIdAnonymous());
  }

  @Test
  public void principalIdAnyone() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getPrincipalIdAnyone());
    this.log.info("getPrincipalIdAnyone = " + r.getPrincipalIdAnyone());
  }

  @Test
  public void productName() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getProductName());
    this.log.info("getProductName = " + r.getProductName());
  }

  @Test
  public void thinClientUri() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getThinClientUri());
    this.log.info("getThinClientUri = " + r.getThinClientUri());
  }

  @Test
  public void vendorName() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);
    Assert.assertNotNull(r.getVendorName());
    this.log.info("getVendorName = " + r.getVendorName());
  }

  @Test
  public void repositoryCapabilitiesAclSupport() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);

    // capabilities
    RepositoryCapabilities repcap = r.getCapabilities();
    Assert.assertNotNull(repcap);

    CapabilityAcl capacl = repcap.getAclSupport();
    Assert.assertNotNull(capacl);
    switch (capacl) {
    case DISCOVER:
    case MANAGE:
    case NONE:
      break;
    default:
      Assert.fail("enumeration not supported: " + capacl);
    }
    this.log.info("CapabilityAcl = " + capacl);
  }

  @Test
  public void repositoryCapabilitiesChangesSupport() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);

    // capabilities
    RepositoryCapabilities repcap = r.getCapabilities();
    Assert.assertNotNull(repcap);
    CapabilityChanges capch = repcap.getChangesSupport();
    Assert.assertNotNull(capch);
    switch (capch) {
    case ALL:
    case OBJECTIDSONLY:
    case PROPERTIES:
      PagingList<ChangeEvent> cep = this.session.getContentChanges(null, -1);
      Assert.assertNotNull(cep);
      for (List<ChangeEvent> le : cep) {
        for (ChangeEvent ce : le) {
          TypeOfChanges toc = ce.getChangeType();
          Assert.assertNotNull(toc);
          switch (toc) {
          case CREATED:
          case DELETED:
          case SECURITY:
          case UPDATED:
            break;
          default:
            Assert.fail("change type not supported: " + toc);
          }
          List<Property<?>> pl = ce.getNewProperties();
          Assert.assertNotNull(pl);
          String id = ce.getObjectId();
          Assert.assertNotNull(id);
        }
      }
      break;
    case NONE:
      break;
    default:
      Assert.fail("enumeration not supported: " + capch);
    }
    this.log.info("CapabilityChanges = " + capch);
  }

  @Test
  public void repositoryCapabilitiesContentStreamUpdateabilitySupport() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);

    // capabilities
    RepositoryCapabilities repcap = r.getCapabilities();
    Assert.assertNotNull(repcap);
    CapabilityContentStreamUpdates ccsu = repcap.getContentStreamUpdatabilitySupport();
    Assert.assertNotNull(ccsu);
    switch (ccsu) {
    case ANYTIME:
    case NONE:
    case PWCONLY:
      break;
    default:
      Assert.fail("enumeration not supported: " + ccsu);
    }
    this.log.info("CapabilityContentStreamUpdates = " + ccsu);
  }

  @Test
  public void repositoryCapabilitiesJointSupport() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);

    // capabilities
    RepositoryCapabilities repcap = r.getCapabilities();
    Assert.assertNotNull(repcap);

    CapabilityJoin capj = repcap.getJoinSupport();
    Assert.assertNotNull(capj);

    switch (capj) {
    case INNERANDOUTER:
    case INNERONLY:
    case NONE:
      break;
    default:
      Assert.fail("enumeration not supported: " + capj);
    }
    this.log.info("CapabilityJoin = " + capj);
  }

  @Test
  public void repositoryCapabilitiesQuerySupport() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);

    // capabilities
    RepositoryCapabilities repcap = r.getCapabilities();
    Assert.assertNotNull(repcap);

    CapabilityQuery capq = repcap.getQuerySupport();
    Assert.assertNotNull(capq);
    switch (capq) {
    case BOTHCOMBINED:
    case BOTHSEPARATE:
    case FULLTEXTONLY:
    case METADATAONLY:
      PagingList<CmisObject> resultSet = this.session.query(Fixture.QUERY, false, -1);
      Assert.assertNotNull(resultSet);
      Assert.assertFalse(resultSet.isEmpty());
      break;
    case NONE:
    default:
      Assert.fail("enumeration not supported: " + capq);
    }
    this.log.info("CapabilityQuery = " + capq);
  }

  @Test
  public void repositoryCapabilitiesRenditionSupport() {
    RepositoryInfo r = this.session.getRepositoryInfo();
    Assert.assertNotNull(r);

    // capabilities
    RepositoryCapabilities repcap = r.getCapabilities();
    Assert.assertNotNull(repcap);

    CapabilityRendition caprend = repcap.getRenditionsSupport();
    Assert.assertNotNull(caprend);
    switch (caprend) {
    case NONE:
    case READ:
      break;
    default:
      Assert.fail("enumeration not supported: " + caprend);
    }
    this.log.info("CapabilityRendition = " + caprend);
  }

}