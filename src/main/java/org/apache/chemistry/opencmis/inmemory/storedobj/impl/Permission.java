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


public enum Permission {
    NONE("none"),
    READ("read"),
    WRITE("write"),
    ALL("all");
    
    private final String value;
    
    Permission(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Permission fromValue(String v) {
        for (Permission c : Permission.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
    
    public static Permission fromCmisString(String strPerm) {
        Permission permission;
        if (strPerm.equals("cmis:read")) 
            permission = Permission.READ;
        else if (strPerm.equals("cmis:write")) 
            permission = Permission.WRITE;
        else if (strPerm.equals("cmis:all")) 
            permission = Permission.ALL;
        else
            throw new IllegalArgumentException("InMemory only supports CMIS basic permissions read, write, all.");
        return permission;
     }

}
