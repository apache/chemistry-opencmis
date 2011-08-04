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
package org.apache.chemistry.opencmis.inmemory.storedobj.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;

public class InMemoryAcl {
    
    private List<InMemoryAce> acl;
    
    private static class AceComparator<T extends InMemoryAce> implements Comparator<T> {

        public int compare(T o1, T o2) {            
            int res = o1.getPrincipalId().compareTo(o2.getPrincipalId());
            return res;
        }
        
    };
    
    private static Comparator<? super InMemoryAce> COMP = new AceComparator<InMemoryAce>();
    
    public static InMemoryAcl createFromCommonsAcl(Acl commonsAcl) {
        InMemoryAcl acl = new InMemoryAcl();
        for (Ace cace : commonsAcl.getAces()) {
            if (acl.hasPrincipal(cace.getPrincipalId())) {
                Permission perm = acl.getPermission(cace.getPrincipalId());
                Permission newPerm = Permission.fromCmisString(cace.getPermissions().get(0));
                if (perm.ordinal() > newPerm.ordinal())
                    acl.setPermission(cace.getPrincipalId(), newPerm);
            } else {
                acl.addAce(new InMemoryAce(cace));
            }
            
        }
        return acl;
    }


    public InMemoryAcl() {
        acl = new ArrayList<InMemoryAce>(3);
    }

    public InMemoryAcl(final List<InMemoryAce> arg ) {        
        this.acl = new ArrayList<InMemoryAce>(arg);
        Collections.sort(this.acl, COMP);
        for (int i=0 ; i<acl.size(); i++) {
            InMemoryAce ace = acl.get(i);
            if (ace == null)
                throw new IllegalArgumentException("Cannot create ACLs with a null principal id or permission.");        
        }
        for (int i=0 ; i<acl.size()-1; i++) {
            if (acl.get(i).equals(acl.get(i+1)))
                throw new IllegalArgumentException("Cannot create ACLs with same principal id in more than one ACE.");
        }
    }
    
    public final List<InMemoryAce> getAces() {
        return acl;
    }
    
    public boolean addAce(InMemoryAce ace) {
        if (ace == null)
            return false;
        for (InMemoryAce ace2: acl) {
            if (ace2.getPrincipalId().equals(ace.getPrincipalId()))
                return false;
        }
        acl.add(ace);
        Collections.sort(acl, COMP);
        return true;
    }
    
    public boolean removeAce(InMemoryAce ace) {
        return acl.remove(ace);
    }
    
    public Permission getPermission(String principalId) {
        if (null == principalId)
            return null;
        
        for (InMemoryAce ace : acl) {
            if (ace.getPrincipalId().equals(principalId))
                return ace.getPermission();
        }
        return Permission.NONE;
    }

    public boolean hasPermission(String principalId, Permission permission) {
        if (null == principalId || null == permission)
            return false;
        
        for (InMemoryAce ace : acl) {
            if (ace.getPrincipalId().equals(principalId))
                return ace.hasPermission(permission);
        }
        return false;
    }

    public void setPermission(String principalId, Permission permission) {        
        for (InMemoryAce ace : acl) {
            if (ace.getPrincipalId().equals(principalId))
                ace.setPermission(permission);
        }
        throw new IllegalArgumentException("Unknown principalId in setPermission: " + principalId);
    }
   
    public int size() {
        return acl.size();
    }
     
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((acl == null) ? 0 : acl.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        InMemoryAcl other = (InMemoryAcl) obj;
        if (acl == null) {
            if (other.acl != null)
                return false;
        } else if (!acl.equals(other.acl))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "InMemoryAcl [acl=" + acl + "]";
    }
            
    private boolean hasPrincipal(String principalId) {
        for (InMemoryAce ace: acl) {
            if (ace.getPrincipalId().equals(principalId))
                return true;
        }
        return false;
    }

}
