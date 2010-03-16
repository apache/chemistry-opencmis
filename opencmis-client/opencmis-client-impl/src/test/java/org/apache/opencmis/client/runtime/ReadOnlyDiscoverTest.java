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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.client.api.ChangeEvent;
import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.client.api.QueryResult;
import org.apache.opencmis.client.api.util.PagingList;
import org.apache.opencmis.commons.enums.CapabilityChanges;
import org.apache.opencmis.commons.enums.CapabilityQuery;
import org.apache.opencmis.commons.enums.TypeOfChanges;
import org.junit.Test;

public class ReadOnlyDiscoverTest extends AbstractSessionTest {

  private static Log log = LogFactory.getLog(ReadOnlyDiscoverTest.class);

  @Test
  public void query() {
    CapabilityQuery query = this.session.getRepositoryInfo().getCapabilities().getQuerySupport();

    switch (query) {
    case NONE:
      ReadOnlyDiscoverTest.log.info("queries not supported");
      break;
    default:
      PagingList<QueryResult> resultSet = this.session.query(FixtureData.QUERY.toString(), false, 2);
      Assert.assertNotNull(resultSet);
      //Assert.assertFalse(resultSet.isEmpty());
      for (List<QueryResult> lo : resultSet) {
        for (QueryResult o : lo) {
          Assert.assertNotNull(o);
        }
      }

      break;
    }

  }

  @Test
  public void changes() {
    CapabilityChanges changes = this.session.getRepositoryInfo().getCapabilities()
        .getChangesSupport();

    switch (changes) {
    case NONE:
      ReadOnlyDiscoverTest.log.info("changes not supported");
      break;
    default:
//      PagingList<ChangeEvent> cep = this.session.getContentChanges(null, 2);
//      Assert.assertNotNull(cep);
//      for (List<ChangeEvent> cel : cep)
//        for (ChangeEvent ce : cel) {
//          TypeOfChanges toc = ce.getChangeType();
//          Assert.assertNotNull(toc);
//          switch (toc) {
//          case CREATED:
//          case DELETED:
//          case SECURITY:
//          case UPDATED:
//            break;
//          default:
//            Assert.fail("change type not supported: " + toc);
//          }
//          List<Property<?>> pl = ce.getNewProperties();
//          Assert.assertNotNull(pl);
//          String id = ce.getObjectId();
//          Assert.assertNotNull(id);
//        }
      break;
    }
  }
}
