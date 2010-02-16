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
package org.apache.opencmis.commons.impl.dataobjects;

import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;

/**
 * Abstract extension data implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractExtensionData implements ExtensionsData {

  private List<Object> fExtensions;

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.ExtensionsData#getExtensions()
   */
  public List<Object> getExtensions() {
    return fExtensions;
  }

  /* (non-Javadoc)
   * @see org.apache.opencmis.client.provider.ExtensionsData#setExtensions(java.util.List)
   */
  public void setExtensions(List<Object> extensions) {
    fExtensions = extensions;
  }

  @Override
  public String toString() {
    return "[extensions=" + fExtensions + "]";
  }
}
