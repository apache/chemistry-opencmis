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

/**
 * A folder is a StoredObject that that has a path and children. Children can be
 * folder or documents
 * 
 * @author Jens
 */
public interface Children {

    /**
     * get all the children of this folder. To support paging an initial offset
     * and a maximum number of children to retrieve can be passed
     * 
     * @param maxItems
     *            max. number of items to return
     * @param skipCount
     *            initial offset where to start fetching
     * @param user 
     * @return list of children objects
     */
    List<StoredObject> getChildren(int maxItems, int skipCount, String user );

    /**
     * get all the children of this folder which are folders. To support paging
     * an initial offset and a maximum number of childrent to retrieve can be
     * passed.
     * 
     * @param maxItems
     *            max. number of items to return
     * @param skipCount
     *            initial offset where to start fetching
     * @param user 
     * @return list of children folders
     */
    List<Folder> getFolderChildren(int maxItems, int skipCount, String user);

    /**
     * indicate if a child with the given name exists in this folder
     * 
     * @param name
     *            name to check
     * @return true if the name exists in the folderas child, false otherwise
     */
    boolean hasChild(String name);

}