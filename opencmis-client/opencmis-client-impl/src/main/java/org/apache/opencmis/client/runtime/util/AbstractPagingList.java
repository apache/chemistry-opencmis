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

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.opencmis.client.api.util.PagingList;

/**
 * Base <code>PagingList</code> implementation.
 */
public abstract class AbstractPagingList<T> implements PagingList<T> {

  // number of item is unknown before the first fetch
  private int numItems = -1;

  // cache is disabled by default
  private int cacheSize = 0;
  private LinkedHashMap<Integer, FetchResult> cache = null;

  /**
   * Initializes the cache.
   * 
   * @param cacheSize
   *          size of the cache in pages. cacheSize < 1 disables the cache.
   */
  protected void initializeCache(final int cacheSize) {
    this.cacheSize = cacheSize;

    if (cacheSize > 0) {
      cache = new LinkedHashMap<Integer, FetchResult>(cacheSize + 1, 0.70f, true) {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean removeEldestEntry(Map.Entry<Integer, FetchResult> eldest) {
          return size() > cacheSize;
        }
      };
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.util.PagingList#get(int)
   */
  public List<T> get(int pageNumber) {
    FetchResult fr = getInternal(pageNumber);
    return (fr == null ? null : fr.getPage());
  }

  /**
   * Retrieves a page or gets it from cache.
   */
  protected FetchResult getInternal(int pageNumber) {
    if (pageNumber < 0) {
      throw new IllegalArgumentException("pageNumber must be >= 0!");
    }

    FetchResult result = null;

    if (cacheSize > 0) {
      result = cache.get(pageNumber);
      if (result == null) {
        result = fetchPage(pageNumber);
        cache.put(pageNumber, result);
      }
    }
    else {
      result = fetchPage(pageNumber);

      // set number of items
      if (result != null) {
        if (result.getNumItems() != null) {
          setNumItems(result.getNumItems().intValue());
        }
        else {
          setNumItems(-1);
        }
      }
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.util.PagingList#getNumItems()
   */
  public int getNumItems() {
    return numItems;
  }

  /**
   * Sets the number of items.
   */
  protected void setNumItems(int numItems) {
    this.numItems = numItems;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.util.PagingList#size()
   */
  public int size() {
    if (getNumItems() < 1) {
      return -1;
    }

    if (getMaxItemsPerPage() < 1) {
      return -1;
    }

    return (int) Math.ceil(((double) getNumItems() / (double) getMaxItemsPerPage()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Iterable#iterator()
   */
  public Iterator<List<T>> iterator() {
    return new PageIterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.util.PagingList#getMaxItemsPerPage()
   */
  public abstract int getMaxItemsPerPage();

  /**
   * Fetches the given page from the server.
   * 
   * @param pageNumber
   *          number of the page (>= 0).
   */
  protected abstract FetchResult fetchPage(int pageNumber);

  // --- fetch result class ---

  /**
   * Fetch result.
   */
  protected class FetchResult {
    private List<T> page;
    private BigInteger numItems;
    private Boolean hasMoreItems;

    public FetchResult(List<T> page, BigInteger numItems, Boolean hasMoreItems) {
      this.page = page;
      this.numItems = numItems;
      this.hasMoreItems = hasMoreItems;
    }

    public List<T> getPage() {
      return page;
    }

    public BigInteger getNumItems() {
      return numItems;
    }

    public Boolean getHasMoreItems() {
      return hasMoreItems;
    }
  }

  // --- iterator class ---

  /**
   * Page iterator.
   */
  class PageIterator implements Iterator<List<T>> {

    private int currentPage = -1;
    private boolean hasMoreItems = true;

    public boolean hasNext() {
      if (!hasMoreItems) {
        return false;
      }

      int size = size();
      if (size < 0) {
        // we don't know better
        return true;
      }

      return currentPage < size - 1;
    }

    public List<T> next() {
      currentPage++;
      FetchResult next = getInternal(currentPage);

      if (next == null) {
        hasMoreItems = false;
        return null;
      }

      if ((next.getPage() == null) || (next.getPage().isEmpty())) {
        hasMoreItems = false;
      }

      if (next.getHasMoreItems() != null) {
        hasMoreItems = next.getHasMoreItems().booleanValue();
      }

      return (next == null ? null : next.getPage());
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
