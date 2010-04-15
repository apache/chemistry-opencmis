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
package org.apache.chemistry.opencmis.client.api.util;


/**
 * Basically this is a nested list of lists where the outer list represent pages and the inner list
 * is the result set with items of type T. <code>PagingList</code> implementations can support lazy
 * load of result sets. The first page has the page number 0.
 * 
 * @param <T>
 */
public interface PagingIterable<T> extends Iterable<T> {

  PagingIterable<T> skipTo(long position);

  /**
   * Returns the maximum number of items in one page. The repository MUST NOT exceed this maximum.
   */
//  int getMaxItemsPerPage();

  /**
   * Returns the maximum number of pages calculated from number of <code>numItems</code> and
   * <code>maxItemsPerPage</code>. If the number of <code>numItems</code> is not known then -1 is
   * returned.
   * 
   * @return Number of pages or (-1)
   */
//  int size();

  /**
   * Return one page as list of items of type T.
   * 
   * @param pageNumber
   *          The number of the page to return.
   * @return a page of items
   */
//  List<T> get(int pageNumber);

  /**
   * Returns if the list contains items. This method might fetch the first page to determine the
   * return value!
   * 
   * @return <code>true</code> if the list does not contain items, <code>false</code>otherwise
   */
//  boolean isEmpty();

  /**
   * Sets the size of the page LRU cache. A size &lt; 1 de-activates the cache. Default cache size
   * is 0. Re-setting the cache size clears the cache.
   * 
   * @param cacheSize
   *          size of the cache in pages
   */
//  void setCacheSize(int cacheSize);
}
