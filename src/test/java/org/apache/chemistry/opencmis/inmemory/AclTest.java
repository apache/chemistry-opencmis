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
package org.apache.chemistry.opencmis.inmemory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.InMemoryAce;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.InMemoryAcl;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.Permission;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

public class AclTest {

    private static final Log LOG = LogFactory.getLog(AclTest.class);

    final static String ANDREAS = "andreas";
    final static String BERTA = "berta";
    final static String CHRISTIAN = "christian";
    final static String DOROTHEE = "dorothee";
    
    final InMemoryAce aceN = new InMemoryAce(ANDREAS, Permission.NONE);
    final InMemoryAce aceR = new InMemoryAce(BERTA, Permission.READ);
    final InMemoryAce aceW = new InMemoryAce(CHRISTIAN, Permission.WRITE);
    final InMemoryAce aceA = new InMemoryAce(DOROTHEE, Permission.ALL);

    @Test
    public void testCreateAce() {
        InMemoryAce ace;
        try {
            ace = new InMemoryAce(null, Permission.NONE);
            fail("create an ACE with null principalId should fail.");
        } catch (RuntimeException e) {            
        }

        try {
            ace = new InMemoryAce("xxx", null);
            fail("create an ACE with null permission should fail.");
        } catch (RuntimeException e) {            
        }

    }
    
    @Test
    public void testCreate() {
    
        InMemoryAcl acl = new InMemoryAcl();
        acl.addAce(aceA);
        assertEquals(1, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceA);
    
        acl = new InMemoryAcl(createAceList());
        LOG.debug(acl);
        
        assertEquals(2, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceR);
        assertTrue(acl.getAces().get(1) == aceA);
    
        acl = createDefaultAcl();
        checkDefaultAcl(acl);
        
        try {
            List<InMemoryAce> aces = createAceList();
            aces.add(null);
            acl = new InMemoryAcl(aces);
            fail("create an ACL with a null ACE should fail.");
        } catch (RuntimeException e) {            
        }
    }
    
    @Test
    public void testAdd() {
        InMemoryAcl acl = new InMemoryAcl();
        acl.addAce(aceR);
        assertEquals(1, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceR);
        acl.addAce(aceW);
        assertEquals(2, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceR);
        assertTrue(acl.getAces().get(1) == aceW);
        acl.addAce(aceN);
        assertEquals(3, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceN);
        assertTrue(acl.getAces().get(1) == aceR);
        assertTrue(acl.getAces().get(2) == aceW);
        acl.addAce(aceA);
        assertEquals(4, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceN);
        assertTrue(acl.getAces().get(1) == aceR);
        assertTrue(acl.getAces().get(2) == aceW);
        assertTrue(acl.getAces().get(3) == aceA);    

        assertFalse("Adding an existing ACE to an ACL should fail.", acl.addAce(aceN));
        assertFalse("Adding null to an ACL should fail.", acl.addAce(null));
    }
    
    @Test
    public void testRemove() {
        InMemoryAcl acl = createDefaultAcl();
        checkDefaultAcl(acl);

        acl.removeAce(aceR);
        assertEquals(3, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceN);
        assertTrue(acl.getAces().get(1) == aceW);
        assertTrue(acl.getAces().get(2) == aceA);
        acl.removeAce(aceW);
        assertEquals(2, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceN);
        assertTrue(acl.getAces().get(1) == aceA);
        acl.removeAce(aceN);
        assertEquals(1, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceA);
        acl.removeAce(aceA);
        assertEquals(0, acl.getAces().size());
        
        acl = createDefaultAcl();
        final InMemoryAce ace = new InMemoryAce("xyu", Permission.ALL);
        assertFalse("Removing an unknown ACE from an ACL should fail.", acl.removeAce(ace));
        assertFalse("Removing null from an ACL should fail.", acl.removeAce(null));
    }

    @Test
    public void testAclEquality() {
        final InMemoryAce aceNew = new InMemoryAce("Hugo", Permission.WRITE);
        final InMemoryAce aceRCopy = new InMemoryAce(BERTA, Permission.READ);

        InMemoryAcl acl1 = createDefaultAcl();
        InMemoryAcl acl2 = new InMemoryAcl(new ArrayList<InMemoryAce>() {{ add(aceRCopy); add(aceA);  add(aceW);  add(aceN); }});
        InMemoryAcl acl3 = new InMemoryAcl(new ArrayList<InMemoryAce>() {{ add(aceR); add(aceNew);  add(aceW);  add(aceN); }});
        assertEquals(acl1, acl2);
        assertFalse(acl1.equals(acl3));      
    }
    
    
    @Test
    public void testCheckPermissions() {
        InMemoryAcl acl = createDefaultAcl();

        assertTrue(acl.hasPermission(ANDREAS, Permission.NONE));
        assertFalse(acl.hasPermission(ANDREAS, Permission.READ));
        assertFalse(acl.hasPermission(ANDREAS, Permission.WRITE));
        assertFalse(acl.hasPermission(ANDREAS, Permission.ALL));

        assertTrue(acl.hasPermission(BERTA, Permission.NONE));
        assertTrue(acl.hasPermission(BERTA, Permission.READ));
        assertFalse(acl.hasPermission(BERTA, Permission.WRITE));
        assertFalse(acl.hasPermission(BERTA, Permission.ALL));
    
        assertTrue(acl.hasPermission(CHRISTIAN, Permission.NONE));
        assertTrue(acl.hasPermission(CHRISTIAN, Permission.READ));
        assertTrue(acl.hasPermission(CHRISTIAN, Permission.WRITE));
        assertFalse(acl.hasPermission(CHRISTIAN, Permission.ALL));

        assertTrue(acl.hasPermission(DOROTHEE, Permission.NONE));
        assertTrue(acl.hasPermission(DOROTHEE, Permission.READ));
        assertTrue(acl.hasPermission(DOROTHEE, Permission.WRITE));
        assertTrue(acl.hasPermission(DOROTHEE, Permission.ALL));
    }

    @Test
    public void testConvertFomCmisAcl() {
        List<Ace> aces = Arrays.asList(new Ace[] { createAce(ANDREAS, "cmis:read"), createAce(DOROTHEE, "cmis:write") });
        AccessControlListImpl cAcl = new AccessControlListImpl(aces);
        InMemoryAcl acl = InMemoryAcl.createFromCommonsAcl(cAcl);        
        assertEquals(2, acl.size());
        assertEquals(Permission.READ, acl.getPermission(ANDREAS));
        assertEquals(Permission.WRITE, acl.getPermission(DOROTHEE));
        
        try {
            List<Ace> aces2 = Arrays.asList(new Ace[] { new AccessControlEntryImpl(null,  Arrays.asList(new String[] { "cmis:read"}))});
            acl = InMemoryAcl.createFromCommonsAcl(new AccessControlListImpl(aces2));
            fail("create Ace will null principal should raise exception.");
        } catch (RuntimeException e) { }
        try {
            List<Ace> aces2 = Arrays.asList(new Ace[] { new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(ANDREAS),  null)});
            acl = InMemoryAcl.createFromCommonsAcl(new AccessControlListImpl(aces2));
            fail("create Ace will null permission should raise exception.");
        } catch (RuntimeException e) { }
    }
    
    private InMemoryAcl createDefaultAcl() {
        return  new InMemoryAcl(new ArrayList<InMemoryAce>() {{ add(aceA); add(aceR);  add(aceN);  add(aceW); }});
    }
    
    private void checkDefaultAcl(InMemoryAcl acl) {
        assertEquals(4, acl.getAces().size());
        assertTrue(acl.getAces().get(0) == aceN);
        assertTrue(acl.getAces().get(1) == aceR);
        assertTrue(acl.getAces().get(2) == aceW);
        assertTrue(acl.getAces().get(3) == aceA);        
    }

    private List<InMemoryAce> createAceList() {
        return new ArrayList<InMemoryAce>() {{ add(aceA); add(aceR); }};
    }
    
    private Ace createAce(String principalId, String permission) {
        AccessControlEntryImpl ace = new AccessControlEntryImpl(new AccessControlPrincipalDataImpl(principalId),
                Arrays.asList(new String[] { permission }));
        return ace;
    }

}
