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


/**
 * Documents can have in the CMIS specification multiple parents. This interface
 * describes the behavior of objects with multiple parent objects.
 * 
 * @author Jens
 * 
 */
public interface MultiFiling extends Filing {

    /**
     * retrieve the path segment of this folder
     * 
     * @return
     */
    String getPathSegment();

    /**
     * Add this document to a new parent folder as child object
     * 
     * @param parent
     *            new parent folder of the document.
     */
    void addParent(Folder parent);

    /**
     * Remove this object from the children of parent
     * 
     * @param parent
     *            parent folder of the document
     */
    void removeParent(Folder parent);
}
