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
package org.apache.opencmis.client.api.util;

import java.util.List;

/**
 * Basically this is a nested list of lists where the outer list represent pages and the inner list
 * is the result set with items of type T. <code>PagingList</code> implementations can support lazy
 * load of result sets. The first page has the page number 0.
 * 
 * @param <T>
 */
public interface PagingList<T> extends Iterable<List<T>> {

  /**
   * If the repository knows the total number of items in a result set, the repository SHOULD
   * include the number here. If the repository does not know the number of items in a result set,
   * this parameter SHOULD not be set. The value in the parameter MAY NOT be accurate the next time
   * the client retrieves the result set or the next page in the result set.
   * 
   * @return total number of items or (-1)
   */
  int getNumItems();

  /**
   * This is the maximum number of items to return in one page. The repository MUST NOT exceed this
   * maximum.
   */
  int getMaxItemsPerPage();

  /**
   * This is the maximum number of pages calculated from number of <code>totalItems</code> and
   * <code>maxItems</code>. If the number of <code>totalItems</code> is not known then -1 is
   * returned.
   * 
   * @return Number of pages or (-1)
   */
  int size();

  /**
   * Return one page as list of items of type T.
   * 
   * @param pageNumber
   *          The number of the page to return.
   * @return a page of items
   */
  List<T> get(int pageNumber);
}
