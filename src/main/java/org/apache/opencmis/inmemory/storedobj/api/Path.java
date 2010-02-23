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
package org.apache.opencmis.inmemory.storedobj.api;


/**
 * Path is the capability of an object to get accessed by a path in addition to the identifier.
 * Paths are hierachical, each object with a path has a parent where the parent is always a 
 * folder. Paths do not exist on its own but are part of other objects (documents and folders).
 * 
 * @author Jens
 */
public interface Path {

  /**
   * character indicating how folders are separated within a path string. This char must not be a
   * valid character of an object name.
   */
  public static final String PATH_SEPARATOR = "/";

  /**
   * @return
   */
  String getPath();

  /**
   * @return
   */
  Folder getParent();

  /** Put the document in a folder and set the parent. This method should not 
   * be used to file a document in a folder. It is used internally when a document
   * is filed to the folder. The document does not get persisted in this call.
   * 
   * @param parent
   *    parent folder of the document to be assigned.
   */
  void setParent(Folder parent);
  
  /**
   * Move an object to a different folder. Source and target object are persisted in this 
   * call as part of a transactional step.
   *  
   * @param newParent
   *    new parent folder for the object
   */
  public void move(Folder oldParent, Folder newParent);
  
}