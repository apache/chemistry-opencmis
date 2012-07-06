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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Document;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.MultiFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.SingleFiling;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;

/**
 * The object store is the central core of the in-memory repository. It is based on huge HashMap
 * map mapping ids to objects in memory. To allow access from multiple threads a Java concurrent
 * HashMap is used that allows parallel access methods.
 * <p>
 * Certain methods in the in-memory repository must guarantee constraints. For example a folder
 * enforces that each child has a unique name. Therefore certain operations must occur in an
 * atomic manner. In the example it must be guaranteed that no write access occurs to the
 * map between acquiring the iterator to find the children and finishing the add operation when
 * no name conflicts can occur. For this purpose this class has methods to lock an unlock the
 * state of the repository. It is very important that the caller acquiring the lock enforces an
 * unlock under all circumstances. Typical code is:
 * <p>
 * <pre>
 * ObjectStoreImpl os = ... ;
 * try {
 *     os.lock();
 * } finally {
 *     os.unlock();
 * }
 * </pre>
 *
 * The locking is very coarse-grained. Productive implementations would probably implement finer
 * grained locks on a folder or document rather than the complete repository.
 */
public class ObjectStoreImpl implements ObjectStore {

    
    /**
     * user id for administrator always having all rights
     */
    public static final String ADMIN_PRINCIPAL_ID = "Admin";
    
    /**
     * Simple id generator that uses just an integer
     */
    private static int NEXT_UNUSED_ID = 100;

    /**
     * a concurrent HashMap as core element to hold all objects in the repository
     */
    private final Map<String, StoredObject> fStoredObjectMap = new ConcurrentHashMap<String, StoredObject>();

    /**
     * a concurrent HashMap to hold all Acls in the repository
     */
    private int nextUnusedAclId = 0;
    
    private final List<InMemoryAcl> fAcls = new ArrayList<InMemoryAcl>();

    private final Lock fLock = new ReentrantLock();

    final String fRepositoryId;
    FolderImpl fRootFolder = null;

    public ObjectStoreImpl(String repositoryId) {
        fRepositoryId = repositoryId;
        createRootFolder();
    }

    private static synchronized Integer getNextId() {
        return NEXT_UNUSED_ID++;
    }

    private synchronized Integer getNextAclId() {
        return nextUnusedAclId++;
    }
    
   public void lock() {
      fLock.lock();
    }

    public void unlock() {
      fLock.unlock();
    }

    public Folder getRootFolder() {
        return fRootFolder;
    }

    public StoredObject getObjectByPath(String path, String user) {
        for (StoredObject so : fStoredObjectMap.values()) {
            if (so instanceof SingleFiling) {
                String soPath = ((SingleFiling) so).getPath();
                if (soPath.equals(path)) {
                    return so;
                }
            } else if (so instanceof MultiFiling) {
                MultiFiling mfo = (MultiFiling) so;
                List<Folder> parents = mfo.getParents(user);
                for (Folder parent : parents) {
                    String parentPath = parent.getPath();
                    String mfPath = parentPath.equals(Folder.PATH_SEPARATOR) ? parentPath + mfo.getPathSegment()
                            : parentPath + Folder.PATH_SEPARATOR + mfo.getPathSegment();
                    if (mfPath.equals(path)) {
                        return so;
                    }
                }
            } else {
                return null;
            }
        }
        return null;
    }

    public StoredObject getObjectById(String objectId) {
        // we use path as id so we just can look it up in the map
        StoredObject so = fStoredObjectMap.get(objectId);
        return so;
    }

    public void deleteObject(String objectId, Boolean allVersions, String user) {
        StoredObject obj = fStoredObjectMap.get(objectId);

        if (null == obj) {
            throw new RuntimeException("Cannot delete object with id  " + objectId + ". Object does not exist.");
        }

        if (obj instanceof FolderImpl) {
        	deleteFolder(objectId, user);
        } else if (obj instanceof DocumentVersion) {
            DocumentVersion vers = (DocumentVersion) obj;
            VersionedDocument parentDoc = vers.getParentDocument();
            boolean otherVersionsExists;
            if (allVersions != null && allVersions) {
                otherVersionsExists = false;
                List<DocumentVersion> allVers = parentDoc.getAllVersions();
                for (DocumentVersion ver : allVers) {
                    fStoredObjectMap.remove(ver.getId());
                }
            } else {
                fStoredObjectMap.remove(objectId);
                otherVersionsExists = parentDoc.deleteVersion(vers);
            }
            
            if (!otherVersionsExists) {
                fStoredObjectMap.remove(parentDoc.getId());
            }
        } else {
            fStoredObjectMap.remove(objectId);
        }
    }

    public void removeVersion(DocumentVersion vers) {
        StoredObject found = fStoredObjectMap.remove(vers.getId());

        if (null == found) {
            throw new CmisInvalidArgumentException("Cannot delete object with id  " + vers.getId() + ". Object does not exist.");
        }
    }

    public String storeObject(StoredObject so) {
        String id = so.getId();
        // check if update or create
        if (null == id) {
            id = getNextId().toString();
        }
        fStoredObjectMap.put(id, so);
        return id;
    }

    StoredObject getObject(String id) {
        return fStoredObjectMap.get(id);
    }

    void removeObject(String id) {
        fStoredObjectMap.remove(id);
    }

    public Set<String> getIds() {
        Set<String> entries = fStoredObjectMap.keySet();
        return entries;
    }

    /**
     * Clear repository and remove all data.
     */
    public void clear() {
        lock();
        fStoredObjectMap.clear();
        storeObject(fRootFolder);
        unlock();
    }

    public long getObjectCount() {
        return fStoredObjectMap.size();
    }

    // /////////////////////////////////////////
    // private helper methods

    private void createRootFolder() {
        FolderImpl rootFolder = new FolderImpl(this);
        rootFolder.setName("RootFolder");
        rootFolder.setParent(null);
        rootFolder.setTypeId(BaseTypeId.CMIS_FOLDER.value());
        rootFolder.setCreatedBy("Admin");
        rootFolder.setModifiedBy("Admin");
        rootFolder.setModifiedAtNow();
        rootFolder.setRepositoryId(fRepositoryId);
        rootFolder.setAclId(addAcl(InMemoryAcl.getDefaultAcl()));
        rootFolder.persist();
        fRootFolder = rootFolder;
    }

    public Document createDocument(String name,
			Map<String, PropertyData<?>> propMap, String user, Folder folder,
			Acl addACEs, Acl removeACEs)  {
    	DocumentImpl doc = new DocumentImpl(this);
        doc.createSystemBasePropertiesWhenCreated(propMap, user);
        doc.setCustomProperties(propMap);
        doc.setRepositoryId(fRepositoryId);
        doc.setName(name);
        if (null != folder) {
            ((FolderImpl)folder).addChildDocument(doc); // add document to folder and
        }
        int aclId = getAclId(((FolderImpl)folder), addACEs, removeACEs);
        doc.setAclId(aclId);
        return doc;
    }

    public DocumentVersion createVersionedDocument(String name,
    		Map<String, PropertyData<?>> propMap, String user, Folder folder,
			Acl addACEs, Acl removeACEs, ContentStream contentStream, VersioningState versioningState) {
    	VersionedDocumentImpl doc = new VersionedDocumentImpl(this);
        doc.createSystemBasePropertiesWhenCreated(propMap, user);
        doc.setCustomProperties(propMap);
        doc.setRepositoryId(fRepositoryId);
        doc.setName(name);
        DocumentVersion version = doc.addVersion(contentStream, versioningState, user);
        if (null != folder) {
        	((FolderImpl)folder).addChildDocument(doc); // add document to folder and set
        }
        version.createSystemBasePropertiesWhenCreated(propMap, user);
        version.setCustomProperties(propMap);
        int aclId = getAclId(((FolderImpl)folder), addACEs, removeACEs);
        doc.setAclId(aclId);
        doc.persist();
        return version;
    }

    public Folder createFolder(String name,
			Map<String, PropertyData<?>> propMap, String user, Folder parent,
			Acl addACEs, Acl removeACEs) {
    	
    	FolderImpl folder = new FolderImpl(this, name, null);
        if (null != propMap) {
        	folder.createSystemBasePropertiesWhenCreated(propMap, user);
        	folder.setCustomProperties(propMap);
        }
        folder.setRepositoryId(fRepositoryId);
        if (null != parent) {
        	((FolderImpl)parent).addChildFolder(folder); // add document to folder and set
        }

        int aclId = getAclId(((FolderImpl)parent), addACEs, removeACEs);
        folder.setAclId(aclId);
        
        return folder;
    }

    public Folder createFolder(String name) {
  	  Folder folder = new FolderImpl(this, name, null);
        folder.setRepositoryId(fRepositoryId);
        return folder;
	}
    
	public List<StoredObject> getCheckedOutDocuments(String orderBy,
			String user, IncludeRelationships includeRelationships) {
	    List<StoredObject> res = new ArrayList<StoredObject>();

        for (StoredObject so : fStoredObjectMap.values()) {
            if (so instanceof VersionedDocument) {
                VersionedDocument verDoc = (VersionedDocument) so;
                if (verDoc.isCheckedOut() && hasReadAccess(user, verDoc)) {
                    res.add(verDoc.getPwc());
                }
            }
        }

        return res;
    }

	public StoredObject createRelationship(StoredObject sourceObject,
			StoredObject targetObject, Map<String, PropertyData<?>> propMap,
			String user, Acl addACEs, Acl removeACEs) {
		// TODO Auto-generated method stub
		return null;
	}

    public Acl applyAcl(StoredObject so, Acl addAces, Acl removeAces, AclPropagation aclPropagation, String principalId) {
        if (aclPropagation==AclPropagation.OBJECTONLY || !(so instanceof Folder)) {
            return applyAcl(so, addAces, removeAces);
        } else {
            return applyAclRecursive(((Folder)so), addAces, removeAces, principalId);            
        }
    }
    
    public Acl applyAcl(StoredObject so, Acl acl, AclPropagation aclPropagation, String principalId) {
        if (aclPropagation==AclPropagation.OBJECTONLY || !(so instanceof Folder)) {
            return applyAcl(so, acl);
        } else {
            return applyAclRecursive(((Folder)so), acl, principalId);
        }
    }

    public List<Integer> getAllAclsForUser(String principalId, Permission permission) {
        List<Integer> acls = new ArrayList<Integer>();
        for (InMemoryAcl acl: fAcls) {
            if (acl.hasPermission(principalId, permission))
                acls.add(acl.getId());
        }
        return acls;
    }
    
    public Acl getAcl(int aclId) {
        InMemoryAcl acl = getInMemoryAcl(aclId);
        return acl==null ? InMemoryAcl.getDefaultAcl().toCommonsAcl() : acl.toCommonsAcl();
    }
    
    public int getAclId(StoredObjectImpl so, Acl addACEs, Acl removeACEs) {
        InMemoryAcl newAcl;
        boolean removeDefaultAcl = false;
        int aclId = 0;
        
        if (so == null) {
            newAcl = new InMemoryAcl();
        } else {
            aclId = so.getAclId();
            newAcl = getInMemoryAcl(aclId);
            if (null == newAcl)
                newAcl = new InMemoryAcl();
            else
                // copy list so that we can safely change it without effecting the original
                newAcl = new InMemoryAcl(newAcl.getAces()); 
        }

        if (newAcl.size() == 0 && addACEs == null && removeACEs == null)
            return 0;

        if (null != removeACEs)
            for (Ace ace: removeACEs.getAces()) {
            InMemoryAce inMemAce = new InMemoryAce(ace);
            if (inMemAce.equals(InMemoryAce.getDefaultAce()))
                removeDefaultAcl = true;
            }
        
        if ( so!= null && 0 == aclId  && !removeDefaultAcl)
            return 0; // if object grants full access to everyone and it will not be removed we do nothing

        // add ACEs
        if (null != addACEs)
            for (Ace ace: addACEs.getAces()) {
                InMemoryAce inMemAce = new InMemoryAce(ace);
                if (inMemAce.equals(InMemoryAce.getDefaultAce()))
                    return 0; // if everyone has full access there is no need to add additional ACLs.
                newAcl.addAce(inMemAce);
            }
        
        // remove ACEs
        if (null != removeACEs)
            for (Ace ace: removeACEs.getAces()) {
                InMemoryAce inMemAce = new InMemoryAce(ace);
                newAcl.removeAce(inMemAce);
            }

        if (newAcl.size() > 0)
            return addAcl(newAcl);
        else
            return 0;
    }
    
    private void deleteFolder(String folderId, String user) {
        StoredObject folder = fStoredObjectMap.get(folderId);
        if (folder == null) {
            throw new CmisInvalidArgumentException("Unknown object with id:  " + folderId);
        }

        if (!(folder instanceof FolderImpl)) {
            throw new CmisInvalidArgumentException("Cannot delete folder with id:  " + folderId
                    + ". Object exists but is not a folder.");
        }

        // check if children exist
        List<? extends StoredObject> children = ((Folder) folder).getChildren(-1, -1, user).getChildren();
        if (children != null && !children.isEmpty()) {
            throw new CmisConstraintException("Cannot delete folder with id:  " + folderId + ". Folder is not empty.");
        }

        fStoredObjectMap.remove(folderId);
    }

    public boolean hasReadAccess(String principalId, StoredObject so) {       
        return hasAccess(principalId, so, Permission.READ);
    }

    
    public boolean hasWriteAccess(String principalId, StoredObject so) {       
        return hasAccess(principalId, so, Permission.WRITE);
    }

    
    public boolean hasAllAccess(String principalId, StoredObject so) {       
        return hasAccess(principalId, so, Permission.ALL);
    }
    

    public void checkReadAccess(String principalId, StoredObject so) {
        checkAccess(principalId, so, Permission.READ);
    }
    
    public void checkWriteAccess(String principalId, StoredObject so) {
        checkAccess(principalId, so, Permission.WRITE);
    }
    
    public void checkAllAccess(String principalId, StoredObject so) {
        checkAccess(principalId, so, Permission.ALL);
    }
 
    private void checkAccess(String principalId, StoredObject so, Permission permission) {
        if (!hasAccess(principalId, so, permission))
            throw new CmisPermissionDeniedException("Object with id " + so.getId() + " and name " + so.getName()
                    + " does not grant " + permission.toString() + " access to principal " + principalId);
    }

    private boolean hasAccess(String principalId, StoredObject so, Permission permission) {
        if (null != principalId && principalId.equals(ADMIN_PRINCIPAL_ID))
            return true;
        List<Integer> aclIds = getAllAclsForUser(principalId, permission);        
        return aclIds.contains(((StoredObjectImpl)so).getAclId());
    }

    private InMemoryAcl getInMemoryAcl(int aclId) {
        
        for (InMemoryAcl acl : fAcls) {
            if (aclId == acl.getId())
                return acl;
        }
        return null;
    }

    private int setAcl(StoredObjectImpl so, Acl acl) {
        int aclId;
        if (null == acl || acl.getAces().isEmpty())
            aclId = 0;
        else {
            aclId = getAclId(null, acl, null);
        }
        so.setAclId(aclId);
        return aclId;
    }
    
	/**
	 * check if an Acl is already known
	 * @param acl
	 *     acl to be checked
	 * @return
	 *     0 if Acl is not known, id of Acl otherwise
	 */
	private int hasAcl(InMemoryAcl acl) {
	    for (InMemoryAcl acl2: fAcls) {
	        if (acl2.equals(acl))
	            return acl2.getId();
	    }
	    return -1;
	}

    private int addAcl(InMemoryAcl acl) {
        int aclId = -1;
        
        if (null == acl)
            return 0;
        
        lock();
        try {
            aclId = hasAcl(acl);
            if (aclId < 0) {
                aclId = getNextAclId();
                acl.setId(aclId);
                fAcls.add(acl);
            }
        } finally {
            unlock();
        }
        return aclId;
    }
    
    private Acl applyAcl(StoredObject so, Acl acl) {
        int aclId = setAcl((StoredObjectImpl) so, acl);
        return getAcl(aclId);
    }

    private Acl applyAcl(StoredObject so, Acl addAces, Acl removeAces) {
        int aclId = getAclId((StoredObjectImpl) so, addAces, removeAces);
        ((StoredObjectImpl) so).setAclId(aclId);
        return getAcl(aclId);
    }

    private Acl applyAclRecursive(Folder folder, Acl addAces, Acl removeAces, String principalId) {
        List<? extends StoredObject> children = folder.getChildren(-1, -1, ADMIN_PRINCIPAL_ID).getChildren();
        
        Acl result = applyAcl(folder, addAces, removeAces);  

        if (null == children) {
            return result;
        }
        
        for (StoredObject child : children) {
            if (hasAllAccess(principalId, child)) {
                if (child instanceof Folder) {
                    applyAclRecursive((Folder) child, addAces, removeAces, principalId);                
                } else {
                    applyAcl(child, addAces, removeAces);               
                }
            }
        }
        
        return result;
    }
    
    private Acl applyAclRecursive(Folder folder, Acl acl, String principalId) {
        List<? extends StoredObject> children = folder.getChildren(-1, -1, ADMIN_PRINCIPAL_ID).getChildren();

        Acl result = applyAcl(folder, acl);  

        if (null == children) {
            return result;
        }

        for (StoredObject child : children) {
            if (hasAllAccess(principalId, child)) {
                if (child instanceof Folder) {
                    applyAclRecursive((Folder) child, acl, principalId);                
                } else {
                    applyAcl(child, acl);               
                }
            }
        }
        
        return result;
    }

    public boolean isTypeInUse(String typeId) {
        // iterate over all the objects and check for each if the type matches
        for (String objectId : getIds()) {
            StoredObject so = getObjectById(objectId);
            if (so.getTypeId().equals(typeId))
                return true;
        }
        return false;
    }

}
