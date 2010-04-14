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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.opencmis.client.provider.spi.atompub.AbstractAtomPubService;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.opencmis.commons.provider.AccessControlEntry;
import org.apache.opencmis.commons.provider.AccessControlList;

/**
 * Test for the ACL merging that is necessary in the AtomPub binding implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AclMergeTest extends TestCase {

  public void testIsACLMergeRequired() throws Exception {
    AtomPubService service = new AtomPubService();

    assertFalse(service.publicIsACLMergeRequired(null, null));
    assertFalse(service.publicIsACLMergeRequired(new AccessControlListImpl(), null));
    assertFalse(service.publicIsACLMergeRequired(null, new AccessControlListImpl()));
    assertFalse(service.publicIsACLMergeRequired(new AccessControlListImpl(),
        new AccessControlListImpl()));
  }

  public void testAclMerge() throws Exception {
    AtomPubService service = new AtomPubService();

    // original
    Map<String, String[]> originalAceData = new HashMap<String, String[]>();

    originalAceData.put("p1", new String[] { "perm:read", "perm:write", "perm:delete" });
    originalAceData.put("p2", new String[] { "perm:read" });
    originalAceData.put("p3", new String[] { "perm:all" });

    AccessControlList originalACEs = createACL(originalAceData);

    // add
    Map<String, String[]> addAceData = new HashMap<String, String[]>();

    addAceData.put("p2", new String[] { "perm:write" });
    addAceData.put("p4", new String[] { "perm:all" });

    AccessControlList addACEs = createACL(addAceData);

    // remove
    Map<String, String[]> removeAceData = new HashMap<String, String[]>();

    removeAceData.put("p1", new String[] { "perm:write" });
    removeAceData.put("p3", new String[] { "perm:all" });

    AccessControlList removeACEs = createACL(removeAceData);

    AccessControlList newACL = service.publicMergeACLs(originalACEs, addACEs, removeACEs);

    assertEquals(3, newACL.getAces().size());

    for (AccessControlEntry ace : newACL.getAces()) {
      String principal = ace.getPrincipal().getPrincipalId();
      assertNotNull(principal);

      if (principal.equals("p1")) {
        assertEquals(2, ace.getPermissions().size());
        assertTrue(ace.getPermissions().contains("perm:read"));
        assertTrue(ace.getPermissions().contains("perm:delete"));
        assertFalse(ace.getPermissions().contains("perm:write"));
      }
      else if (principal.equals("p2")) {
        assertEquals(2, ace.getPermissions().size());
        assertTrue(ace.getPermissions().contains("perm:read"));
        assertTrue(ace.getPermissions().contains("perm:write"));
      }
      else if (principal.equals("p3")) {
        fail("Principal should be deleted!");
      }
      else if (principal.equals("p4")) {
        assertEquals(1, ace.getPermissions().size());
        assertTrue(ace.getPermissions().contains("perm:all"));
      }
    }
  }

  /**
   * Creates an ACL structure from a Map.
   */
  private AccessControlList createACL(Map<String, String[]> aceData) {
    AccessControlListImpl result = new AccessControlListImpl();

    List<AccessControlEntry> aces = new ArrayList<AccessControlEntry>();

    for (Map.Entry<String, String[]> e : aceData.entrySet()) {
      ArrayList<String> permissions = new ArrayList<String>();

      for (String s : e.getValue()) {
        permissions.add(s);
      }

      AccessControlEntryImpl ace = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(e
          .getKey()), permissions);

      aces.add(ace);
    }

    result.setAces(aces);

    return result;
  }

  /**
   * A class to make a few protected methods publicly available.
   */
  private static class AtomPubService extends AbstractAtomPubService {
    public AccessControlList publicMergeACLs(AccessControlList originalACEs,
        AccessControlList addACEs, AccessControlList removeACEs) {
      return mergeAcls(originalACEs, addACEs, removeACEs);
    }

    public boolean publicIsACLMergeRequired(AccessControlList addACEs, AccessControlList removeACEs) {
      return isAclMergeRequired(addACEs, removeACEs);
    }
  }
}
