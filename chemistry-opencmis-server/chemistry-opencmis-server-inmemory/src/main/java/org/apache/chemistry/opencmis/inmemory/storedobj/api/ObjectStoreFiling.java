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
package org.apache.chemistry.opencmis.inmemory.storedobj.api;

import java.util.List;

public interface ObjectStoreFiling {

    public static class ChildrenResult {
        private int noItems;
        private List<Fileable> children;
                
        public ChildrenResult(List<Fileable> children, int noItems) {
            this.children = children;
            this.noItems = noItems;
        }
        
        public int getNoItems() {
            return noItems;
        }
        
        public List<Fileable> getChildren() {
            return children;
        }
    }

    /**
     * get all the children of this folder. To support paging an initial offset
     * and a maximum number of children to retrieve can be passed
     * 
     * @param folder
     *            folder to get children from
     * @param maxItems
     *            max. number of items to return
     * @param skipCount
     *            initial offset where to start fetching
     * @param user 
     * 			user to determine visible children
     * @param usePwc 
     * 			if true return private working copy otherwise return latest version; 
     * 
     * @return list of children objects
     */
    ChildrenResult getChildren(Folder folder, int maxItems, int skipCount, String user, boolean usePwc);

    /**
     * get all the children of this folder which are folders. To support paging
     * an initial offset and a maximum number of children to retrieve can be
     * passed.
     * 
     * @param folder
     *            folder to get children from
     * @param maxItems
     *            max. number of items to return
     * @param skipCount
     *            initial offset where to start fetching
     * @param user 
     * @return list of children folders
     */
    ChildrenResult getFolderChildren(Folder folder, int maxItems, int skipCount, String user);

    /**
     * get all parent ids of this object visible for a user
     * @param user
     *      user who can see parents
     * @return
     *      list of folder ids
     */
    public List<String> getParentIds(Filing spo, String user);
    
    /**
     * Move an object to a different folder. 
     * 
     * @param so
     *            object to be moved
     * @param oldParent
     *            old parent folder for the object
     * @param newParent
     *            new parent folder for the object
     */
    void move(StoredObject so, Folder oldParent, Folder newParent);

    /**
     * Rename an object
     * @param so
     *      object to be renamed
     * @param newName
     *      new name to be assigned
     */
    public void rename(Fileable so, String newName);
}
