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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.server.support.TypeManager;

/**
 * A helper class doing some consistency checks when new type definitions are added to the system
 * 
 * @author Jens
 *
 */
public class TypeValidator {
    
    private static final Object CMIS_USER = "cmis:user";

	public static void checkType(TypeManager tm, TypeDefinition td) {

        if (null == tm.getTypeById(td.getParentTypeId())) {
            throw new CmisInvalidArgumentException("Cannot add type, because parent with id " + td.getParentTypeId()
                    + " does not exist.");
        }

        checkTypeId(tm, td.getId());
        checkTypeQueryName(tm, td.getQueryName());
        checkTypeLocalName(tm, td.getLocalName());
    }
    
    public static void checkProperties(TypeManager tm, Collection<PropertyDefinition<?>> pds) {

        Collection<TypeDefinitionContainer> tdl = tm.getTypeDefinitionList();
        for (PropertyDefinition<?> pd2 : pds) {
            // check id syntax
            if (null == pd2.getId())
                throw new CmisInvalidArgumentException("property id cannot be null.");
            if (!NameValidator.isValidId(pd2.getId()))
                throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
    
            // check query name syntax
            if (null == pd2.getQueryName())
                throw new CmisInvalidArgumentException("property query name cannot be null.");
            if (!NameValidator.isValidQueryName(pd2.getQueryName()))
                throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
    
            // check local name syntax
            if (null == pd2.getLocalName())
                throw new CmisInvalidArgumentException("property local name cannot be null.");
            if (!NameValidator.isValidLocalName(pd2.getLocalName()))
                throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
            
            for (TypeDefinitionContainer tdc : tdl) {
                TypeDefinition td = tdc.getTypeDefinition();
                for (PropertyDefinition<?> pd1 : td.getPropertyDefinitions().values()) {
                    // check if id is used
                    if (pd1.getId().equals(pd2.getId()))
                        throw new CmisConstraintException("Property id " + pd2.getId() + " already in use in type "
                                + td.getId());
                    // check if query name is used
                    if (pd1.getQueryName().equals(pd2.getQueryName()))
                        throw new CmisConstraintException("Property query name " + pd2.getQueryName() + " already in use in type "
                                + td.getQueryName());
                    // check if local name is used
                    if (pd1.getLocalName().equals(pd2.getLocalName()))
                        throw new CmisConstraintException("Property local name " + pd2.getLocalName() + " already in use in type "
                                + td.getId());
                }
            }
        }        
    }
    
    public static Acl expandAclMakros(String user, Acl acl) {
     	boolean mustCopy = false;
    	
    	if (user == null || acl == null || acl.getAces() == null)
    		return acl;
    	
     	for (Ace ace: acl.getAces()) {
    		String principal = ace.getPrincipalId();
    		if (principal != null && principal.equals(CMIS_USER)) {
    			mustCopy = true;
    		}
    	}
    	
    	if (mustCopy) {
    		AccessControlListImpl result = new AccessControlListImpl();
    		List<Ace> list = new ArrayList<Ace>(acl.getAces().size());
        	for (Ace ace: acl.getAces()) {
        		String principal = ace.getPrincipalId();
        		if (principal != null && principal.equals(CMIS_USER)) {
        			AccessControlEntryImpl ace2 = new AccessControlEntryImpl();
        			ace2.setPermissions(ace.getPermissions());
        			ace2.setExtensions(ace.getExtensions());
        			ace2.setPrincipal(new AccessControlPrincipalDataImpl(user));
        			list.add(ace2);
        		} else
        			list.add(ace);        		
        	}    		
    		result.setAces(list);
    		return result;
    	} else
    		return acl;
    }
    
    private static void checkTypeId(TypeManager tm, String typeId) {

        if (null == typeId) {
            throw new CmisInvalidArgumentException("Type id cannot be null.");
        }

        // check name syntax
        if (!NameValidator.isValidId(typeId)) {
            throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_ID);
        }

        if (null != tm.getTypeById(typeId)) {
            throw new CmisInvalidArgumentException("You cannot add type with id " + typeId
                    + " because it already exists.");           
        }
    }
    
    private static void checkTypeQueryName(TypeManager tm, String queryName) {

        if (null == queryName) {
            throw new CmisInvalidArgumentException("Query name cannot be null.");
        }

        if (!NameValidator.isValidQueryName(queryName)) {
            throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
        }
        
        // check set query name is unique in the type system
        if (null != tm.getTypeByQueryName(queryName)) {
            throw new CmisInvalidArgumentException("You cannot add type with query name " + queryName
                    + " because it already exists.");           
        }
    }
    
    private static void checkTypeLocalName(TypeManager tm, String localName) {
        
        if (null == localName) {
            throw new CmisInvalidArgumentException("Local name cannot be null.");
        }

        if (!NameValidator.isValidLocalName(localName)) {
            throw new CmisInvalidArgumentException(NameValidator.ERROR_ILLEGAL_NAME);
        }
        
        for (TypeDefinitionContainer tdc : tm.getTypeDefinitionList()) {
            if (tdc.getTypeDefinition().getLocalName().equals(localName))
                throw new CmisConstraintException("You cannot add type with local name " + localName
                        + " because it already exists.");                       
        }
    }

}
