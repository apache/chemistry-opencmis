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

import java.util.ArrayList;
import java.util.List;

import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinition;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class TypeDefinitionContainerImpl extends AbstractExtensionData implements
    TypeDefinitionContainer {

  private TypeDefinition fType;
  private List<TypeDefinitionContainer> fChildren;

  public TypeDefinitionContainerImpl() {
  }

  public TypeDefinitionContainerImpl(TypeDefinition typeDef) {
    setTypeDefinition(typeDef);
    fChildren = new ArrayList<TypeDefinitionContainer>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionContainer#getTypeDefinition()
   */
  public TypeDefinition getTypeDefinition() {
    return fType;
  }

  public void setTypeDefinition(TypeDefinition type) {
    fType = type;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.TypeDefinitionContainer#getChildren()
   */
  public List<TypeDefinitionContainer> getChildren() {
    return fChildren;
  }

  public void setChildren(List<TypeDefinitionContainer> children) {
    fChildren = children;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Type Definition Container [type=" + fType + " ,children=" + fChildren + "]"
        + super.toString();
  }
}
