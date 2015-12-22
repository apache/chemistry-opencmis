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
package org.apache.chemistry.opencmis.client.runtime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetcher;
import org.apache.chemistry.opencmis.client.runtime.util.CollectionIterable;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemIterableTest {

    private static final Logger LOG = LoggerFactory.getLogger(ItemIterableTest.class);
//    static {
//        Properties p = new Properties();
//        try {
//            p.load(ItemIterableTest.class.getResourceAsStream("/log4j.properties"));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        PropertyConfigurator.configure(p);
//
//    }
    private final String[] data10 = { "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9" };
    private final String[] data1 = { "A0" };
    private final String[] data0 = {};

    private ItemIterable<String> getIterable(final String[] data, long pageSize) {
        return new CollectionIterable<String>(new AbstractPageFetcher<String>(pageSize) {

            @Override
            protected Page<String> fetchPage(long skipCount) {
                boolean hasMoreItems = true;
                List<String> page = new ArrayList<String>();

                ItemIterableTest.LOG.info("(" + skipCount + "|" + this.maxNumItems + ") ");

                int from = (int) skipCount;
                int to = (int) (skipCount + this.maxNumItems);

                if (to >= data.length) {
                    to = data.length;
                    hasMoreItems = false;
                }

                // . simulate rolling total number of items (for repositories
                // that don't necessarily know up front)
                int totalItems = (to == data.length) ? to : to + 1;

                for (int i = from; i < to; i++) {
                    page.add(data[i]);
                }

                Page<String> result = new AbstractPageFetcher.Page<String>(page, totalItems, hasMoreItems);

                return result;
            }
        });
    }

    @Test
    public void loopAll() {
        this.loopAll(this.data10, 100); // tolerate out of bound
        this.loopAll(this.data10, 10);
        this.loopAll(this.data10, 9);
        this.loopAll(this.data10, 8);

        this.loopAll(this.data10, 2);
        this.loopAll(this.data10, 1);
        // this.loopAll(0); maxNumItems must be > 0

        this.loopAll(this.data1, 1);
        this.loopAll(this.data1, 5);

        this.loopAll(this.data0, 1);
        this.loopAll(this.data0, 5);
    }

    @Test
    public void loopSubPage() {
        this.loopSubPage(this.data10, 0, 3, 5);
        this.loopSubPage(this.data10, 2, 3, 5);
        this.loopSubPage(this.data10, 9, 3, 5);
        this.loopSubPage(this.data10, 10, 3, 5);

        this.loopSubPage(this.data10, 2, 3, 3);
        this.loopSubPage(this.data10, 2, 1000, 1000);
    }

    @Test
    public void loopSkip() {
        this.loopSkip(this.data10, 0, 5);
        this.loopSkip(this.data10, 1, 5);
        this.loopSkip(this.data10, 2, 5);
        this.loopSkip(this.data10, 3, 5);

        this.loopSkip(this.data10, 8, 5);
        this.loopSkip(this.data10, 9, 5);
        this.loopSkip(this.data10, 10, 5);
        // this.loopSkip(100, 5); skip out of bound

        // this.loopSkip(0, 0);
        this.loopSkip(this.data10, 0, 1);
        this.loopSkip(this.data10, 0, 10);
        this.loopSkip(this.data10, 0, 100);

        // this.loopSkip(0, 0);
        this.loopSkip(this.data10, 10, 1);
        this.loopSkip(this.data10, 10, 10);
        this.loopSkip(this.data10, 10, 100);

        this.loopSkip(this.data1, 0, 5);
        this.loopSkip(this.data1, 1, 5);

        this.loopSkip(this.data0, 0, 5);
    }

    @Test
    public void loopPage() {
        this.loopPage(this.data10, 0, 5);
        this.loopPage(this.data10, 1, 5);
        this.loopPage(this.data10, 2, 5);
        this.loopPage(this.data10, 3, 5);

        this.loopPage(this.data10, 8, 5);
        this.loopPage(this.data10, 9, 5);
        this.loopPage(this.data10, 10, 5);
        // this.loopPage(100, 5); skip out of bound

        // this.loopPage(0, 0);
        this.loopPage(this.data10, 0, 1);
        this.loopPage(this.data10, 0, 10);
        this.loopPage(this.data10, 0, 100);

        // this.loopPage(0, 0);
        this.loopPage(this.data10, 10, 1);
        this.loopPage(this.data10, 10, 10);
        this.loopPage(this.data10, 10, 100);

        this.loopPage(this.data1, 0, 5);
        this.loopPage(this.data1, 1, 5);

        this.loopPage(this.data0, 0, 5);
    }

    @Test
    public void totalNumItems() {
        ItemIterableTest.LOG.info("totalNumItems");

        int pageSize = 5;
        ItemIterable<String> p = this.getIterable(this.data10, pageSize);
        assertNotNull(p);
        Iterator<String> i = p.iterator();
        assertNotNull(i);
        assertEquals(pageSize + 1, p.getTotalNumItems());
        for (int idx = 0; i.hasNext() && idx < (pageSize + 1); idx++) {
            assertNotNull(i.next());
        }
        assertEquals(this.data10.length, p.getTotalNumItems());
    }

    @Test
    public void hasMoreItems() {
        ItemIterableTest.LOG.info("totalHasMoreItems");

        int pageSize = 5;
        ItemIterable<String> p = this.getIterable(this.data10, pageSize);
        assertNotNull(p);
        Iterator<String> i = p.iterator();
        assertNotNull(i);
        assertTrue(p.getHasMoreItems());
        for (int idx = 0; i.hasNext() && idx < (pageSize + 1); idx++) {
            i.next();
        }
        assertFalse(p.getHasMoreItems());
    }

    @Test
    public void pageNumItems() {
        ItemIterableTest.LOG.info("totalPageNumItems");

        int pageSize = 7;
        ItemIterable<String> p = this.getIterable(this.data10, pageSize);
        assertNotNull(p);
        Iterator<String> i = p.iterator();
        assertNotNull(i);
        assertEquals(pageSize, p.getPageNumItems());
        for (int idx = 0; i.hasNext() && idx < (pageSize + 1); idx++) {
            assertNotNull(i.next());
        }
        assertEquals(this.data10.length - pageSize, p.getPageNumItems());
    }

    private void loopSubPage(String[] data, int skipCount, int maxItems, int pageSize) {
        ItemIterableTest.LOG.info("loopSubPage (" + skipCount + ", " + maxItems + ", " + pageSize + ")");
        String msg = "";

        ItemIterable<String> p = this.getIterable(data, pageSize);
        assertNotNull(p);
        ItemIterable<String> pp = p.skipTo(skipCount);
        assertNotNull(pp);
        ItemIterable<String> ppp = pp.getPage(maxItems);
        assertNotNull(ppp);

        int count = 0;
        for (String s : ppp) {
            assertNotNull(s);
            assertEquals("A" + (count + skipCount), s);
            msg += (s + " ");
            count++;
        }
        ItemIterableTest.LOG.info(msg);
        assertTrue(count <= pageSize);
    }

    private void loopSkip(String[] data, int skipCount, int pageSize) {
        ItemIterableTest.LOG.info("loopSkip (" + skipCount + ", " + pageSize + ")");
        String msg = "";

        ItemIterable<String> p = this.getIterable(data, pageSize);
        assertNotNull(p);
        ItemIterable<String> pp = p.skipTo(skipCount);
        assertNotNull(pp);

        int count = 0;
        for (String s : pp) {
            assertNotNull(s);
            assertEquals("A" + (count + skipCount), s);
            msg += (s + " ");
            count++;
        }
        ItemIterableTest.LOG.info(msg);
        assertEquals(data.length - skipCount, count);
    }

    private void loopAll(String[] data, int pageSize) {
        ItemIterableTest.LOG.info("loopAll (" + pageSize + ")");
        String msg = "";

        ItemIterable<String> p = this.getIterable(data, pageSize);
        assertNotNull(p);

        int count = 0;
        for (String s : p) {
            assertNotNull(s);
            assertEquals("A" + count, s);
            msg += (s + " ");
            count++;
        }
        ItemIterableTest.LOG.info(msg);
        assertEquals(data.length, count);
    }

    private void loopPage(String[] data, int skipCount, int pageSize) {
        ItemIterableTest.LOG.info("loopPage (" + skipCount + ", " + pageSize + ")");
        String msg = "";

        ItemIterable<String> p = this.getIterable(data, pageSize);
        assertNotNull(p);
        ItemIterable<String> pp = p.skipTo(skipCount).getPage();
        assertNotNull(pp);

        int count = 0;
        for (String s : pp) {
            assertNotNull(s);
            assertEquals("A" + (count + skipCount), s);
            msg += (s + " ");
            count++;
        }
        ItemIterableTest.LOG.info(msg);
        assertEquals(Math.min(data.length - skipCount, pageSize), count);
    }

}
