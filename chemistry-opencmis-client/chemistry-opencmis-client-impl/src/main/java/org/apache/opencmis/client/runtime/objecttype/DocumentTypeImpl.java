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
package org.apache.opencmis.client.runtime.objecttype;

import org.apache.opencmis.client.api.Session;
import org.apache.opencmis.client.api.objecttype.DocumentType;
import org.apache.opencmis.commons.api.DocumentTypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.enums.ContentStreamAllowed;

/**
 * Document type.
 */
public class DocumentTypeImpl extends AbstractObjectType implements DocumentType {

  /**
   * Constructor.
   */
  public DocumentTypeImpl(Session session, TypeDefinition typeDefinition) {
    initialize(session, typeDefinition);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.DocumentType#getContentStreamAllowed()
   */
  public ContentStreamAllowed getContentStreamAllowed() {
    return ((DocumentTypeDefinition) getTypeDefinition()).getContentStreamAllowed();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.objecttype.DocumentType#isVersionable()
   */
  public Boolean isVersionable() {
    return ((DocumentTypeDefinition) getTypeDefinition()).isVersionable();
  }
}
