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
package org.apache.chemistry.opencmis.client.runtime.util;

import org.apache.chemistry.opencmis.client.api.util.PagingIterator;

public class DefaultPageIterator<T> implements PagingIterator<T> {

  private long position = -1;
  private long maxItems = -1;
  
  private Long totalItems = null;
//  private FetchResult page = null;
  
  
  public DefaultPageIterator(long skipCount, long maxItems) {
    this.position = skipCount;
    this.maxItems = maxItems;
  }
  
  public long getPosition() {
    return position;
  }

  public long getTotalNumItems() {
//    if (totalItems == null) {
//      if (page == null) {
//        page = getInternal(position);
//      }
//      if (page != null) {
//        // set number of items
//        if (page.getTotalItems() != null) {
//          totalItems = page.getTotalItems().longValue();
//        }
//        else {
//          totalItems = -1L;
//        }
//      }
//    }
//    
//    return totalItems;
    return -1L;
  }

  public boolean hasNext() {
    // TODO Auto-generated method stub
    return false;
  }

  public T next() {
    // TODO Auto-generated method stub
    return null;
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }

  

  /**
   * Retrieves a page or gets it from cache.
   */
//  protected FetchResult getInternal(long skipCount) {
//    if (skipCount < 0) {
//      throw new IllegalArgumentException("position must be >= 0!");
//    }
//
//    return fetchPage(skipCount);
//  }
  
}
