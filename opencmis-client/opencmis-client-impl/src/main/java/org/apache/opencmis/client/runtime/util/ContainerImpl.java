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
package org.apache.opencmis.client.runtime.util;

import java.util.List;

import org.apache.opencmis.client.api.util.Container;

public class ContainerImpl<T> implements Container<T> {

  private T item;
  private List<Container<T>> children;

  public ContainerImpl(T item, List<Container<T>> children) {
    if (item == null) {
      throw new IllegalArgumentException("Item must be set!");
    }

    this.item = item;
    this.children = children;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.util.Container#getItem()
   */
  public T getItem() {
    return item;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.util.Container#getChildren()
   */
  public List<Container<T>> getChildren() {
    return this.children;
  }
}
