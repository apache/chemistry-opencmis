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

import org.apache.opencmis.commons.provider.ContentStreamData;

public interface Content {
  /**
   * retrieve the content of a document
   * @return
   *    object containing mime-type, length and a stream with content
   */
  ContentStreamData getContent(long offset, long length);

  /**
   * Assign content do a document. Existing content gets overwritten.
   * The document is not yet persisted in the new state.
   * @param content
   *    content to be assigned to the document.
   * @param mustPersist
   *    persist document (set to false if content is set during creation of a document)
   */
  void setContent(ContentStreamData content, boolean mustPersist);
  
}
