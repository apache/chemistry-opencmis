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

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionList;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class TypeDefinitionListImpl extends AbstractExtensionData implements TypeDefinitionList {

  private List<TypeDefinition> fList;
  private Boolean fHasMoreItems = Boolean.FALSE;
  private BigInteger fNumItems;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefintionList#getList()
   */
  public List<TypeDefinition> getList() {
    return fList;
  }

  public void setList(List<TypeDefinition> list) {
    fList = list;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefintionList#hasMoreItems()
   */
  public Boolean hasMoreItems() {
    return fHasMoreItems;
  }

  public void setHasMoreItems(Boolean hasMoreItems) {
    fHasMoreItems = hasMoreItems;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefintionList#getNumItems()
   */
  public BigInteger getNumItems() {
    return fNumItems;
  }

  public void setNumItems(BigInteger numItems) {
    fNumItems = numItems;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Type Definition List [list=" + fList + ", has more items=" + fHasMoreItems
        + ", num items=" + fNumItems + "]" + super.toString();
  }
}
