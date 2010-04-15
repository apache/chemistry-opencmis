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

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.util.PagingIterable;

/**
 * Base <code>PagingList</code> implementation.
 */
public abstract class AbstractPagingIterable<T> implements PagingIterable<T> {

  private long skipCount = 0;
  private long maxItems = -1;    // page size
  

  public AbstractPagingIterable(long numItems) {
    this.maxItems = numItems;
  }
  
  public AbstractPagingIterable(long position, long maxItems) {
    this.skipCount = position;
    this.maxItems = maxItems;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Iterable#iterator()
   */
  public Iterator<T> iterator() {
    return new DefaultPageIterator<T>(skipCount, maxItems);
  }

  public PagingIterable<T> skipTo(long position) {
    return null;
    //return new AbstractPagingIterable<T>(position, maxItems);
  }


  /**
   * Fetches the given page from the server.
   * 
   * @param pageNumber
   *          number of the page (>= 0).
   */
  protected abstract FetchResult fetchPage(long position);

  // --- fetch result class ---

  protected class FetchResult {
    private List<T> page;
    private BigInteger totalItems;
    private Boolean hasMoreItems;

    public FetchResult(List<T> page, BigInteger maxItems, Boolean hasMoreItems) {
      this.page = page;
      this.totalItems = maxItems;
      this.hasMoreItems = hasMoreItems;
    }

    public List<T> getPage() {
      return page;
    }

    public BigInteger getTotalItems() {
      return totalItems;
    }

    public Boolean getHasMoreItems() {
      return hasMoreItems;
    }
  }
  

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.util.PagingList#size()
   */
//  public int size() {
//    if (getNumItems() < 1) {
//      return -1;
//    }
//
//    if (getMaxItemsPerPage() < 1) {
//      return -1;
//    }
//
//    return (int) Math.ceil(((double) getNumItems() / (double) getMaxItemsPerPage()));
//  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.util.PagingList#isEmpty()
   */
//  public boolean isEmpty() {
//    if (getNumItems() > 0) {
//      return false;
//    }
//
//    if (getNumItems() == 0) {
//      return true;
//    }
//
//    List<T> page = get(0);
//    if (page == null) {
//      return true;
//    }
//
//    return page.isEmpty();
//  }

}
