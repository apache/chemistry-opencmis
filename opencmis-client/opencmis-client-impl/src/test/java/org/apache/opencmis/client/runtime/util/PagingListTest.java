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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.opencmis.client.api.util.PagingList;
import org.junit.Before;
import org.junit.Test;

public class PagingListTest {

  private static final int ITEMS = 201;
  private static final int MAX_ITEMS_PER_PAGE = 20;

  private static final String PREFIX_1 = "1$";
  private static final String PREFIX_2 = "2$";

  private String[] sourceData;
  private PagingList<String> testList;
  private PagingList<String> testCacheList;

  @Before
  public void setUp() throws Exception {
    sourceData = new String[ITEMS];
    for (int i = 0; i < sourceData.length; i++) {
      sourceData[i] = PREFIX_1 + i;
    }

    testList = new TestPagingList(0);
    testCacheList = new TestPagingList(2);
  }

  @Test
  public void testPagingList() {
    // test setup
    assertNotNull(testList);

    // test number of items per pages
    assertEquals(MAX_ITEMS_PER_PAGE, testList.getMaxItemsPerPage());

    // we haven't fetched data yet -> number of item should be unknown
    assertEquals(-1, testList.getNumItems());

    // fetch first page and check it
    List<String> page = testList.get(0);
    assertPage(page, 0, MAX_ITEMS_PER_PAGE, PREFIX_1);

    // number of item should be known now
    assertEquals(sourceData.length, testList.getNumItems());

    // number of pages should be known too
    assertEquals(11, testList.size());

    // check all pages
    for (int i = 0; i < testList.size(); i++) {
      page = testList.get(i);

      int pageSize = (i == testList.size() - 1 ? ITEMS % MAX_ITEMS_PER_PAGE : MAX_ITEMS_PER_PAGE);
      assertPage(page, i, pageSize, PREFIX_1);
    }
  }

  @Test
  public void testPagingListIterator() {
    // test setup
    assertNotNull(testList);

    // test iterator
    int pageNumber = 0;
    for (List<String> page : testList) {
      assertTrue(pageNumber < testList.size());

      int pageSize = (pageNumber == testList.size() - 1 ? ITEMS % MAX_ITEMS_PER_PAGE
          : MAX_ITEMS_PER_PAGE);

      assertPage(page, pageNumber, pageSize, PREFIX_1);

      pageNumber++;
    }
  }

  @Test
  public void testPagingListExceptions() {
    // test setup
    assertNotNull(testList);

    // check negative page numbers
    try {
      testList.get(-1);
      fail("Should throw a IllegalArgumentException!");
    }
    catch (IllegalArgumentException e) {
    }

    // check page numbers greater than the last page
    try {
      testList.get(12);
      fail("Should throw a NoSuchElementException!");
    }
    catch (NoSuchElementException e) {
    }
  }

  @Test
  public void testPagingCache() {
    // test setup
    assertNotNull(testList);

    // read first page, should be cached now
    List<String> firstPage = testCacheList.get(0);
    assertPage(firstPage, 0, MAX_ITEMS_PER_PAGE, PREFIX_1);

    // change original data
    for (int i = 0; i < sourceData.length; i++) {
      sourceData[i] = PREFIX_2 + i;
    }

    // get second page with new content
    List<String> secondPage = testCacheList.get(1);
    assertPage(secondPage, 1, MAX_ITEMS_PER_PAGE, PREFIX_2);

    // fetch first page again, should have the old values since it is cached
    firstPage = testCacheList.get(0);
    assertPage(firstPage, 0, MAX_ITEMS_PER_PAGE, PREFIX_1);

    // read a few more pages
    testCacheList.get(2);
    testCacheList.get(3);

    // fetch first page again, should have the new values since it is not cached anymore
    firstPage = testCacheList.get(0);
    assertPage(firstPage, 0, MAX_ITEMS_PER_PAGE, PREFIX_2);
  }

  void assertPage(List<String> page, int pageNumber, int size, String prefix) {
    assertNotNull(page);

    // the first page should be full
    assertEquals(size, page.size());

    // check page content
    int counter = 0;
    for (String s : page) {
      assertEquals(prefix + ((pageNumber * MAX_ITEMS_PER_PAGE) + counter), s);
      counter++;
    }
  }

  // --- Test PagingList ---

  class TestPagingList extends AbstractPagingList<String> {

    public TestPagingList(int cacheSize) {
      setCacheSize(cacheSize);
    }

    @Override
    protected FetchResult fetchPage(int pageNumber) {
      int skipCount = pageNumber * getMaxItemsPerPage();
      int lastIndex = skipCount + getMaxItemsPerPage() - 1;
      if (lastIndex >= sourceData.length) {
        lastIndex = sourceData.length - 1;
      }

      if (skipCount >= sourceData.length) {
        throw new NoSuchElementException();
      }

      List<String> page = new ArrayList<String>();
      for (int i = skipCount; i <= lastIndex; i++) {
        page.add(sourceData[i]);
      }

      return new FetchResult(page, BigInteger.valueOf(sourceData.length), skipCount
          + getMaxItemsPerPage() < sourceData.length);
    }

    @Override
    public int getMaxItemsPerPage() {
      return MAX_ITEMS_PER_PAGE;
    }
  }
}
