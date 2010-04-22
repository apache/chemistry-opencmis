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
import static org.junit.Assert.assertNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.PagingIterable;
import org.apache.chemistry.opencmis.client.api.PagingIterator;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetch;
import org.apache.chemistry.opencmis.client.runtime.util.DefaultPagingIterable;
import org.junit.Test;

public class PagingListTest {

	private String[] data10 = { "A0", "A1", "A2", "A3", "A4", "A5", "A6", "A7",
			"A8", "A9" };
	private String[] data1 = { "A0" };
	private String[] data0 = { };

	private PagingIterable<String> getIterable(final String[] data, final long pageSize) {
		return new DefaultPagingIterable<String>(
				new AbstractPageFetch<String>() {

					@Override
					protected PageFetchResult<String> fetchPage(long skipCount) {
						Boolean hasMoreItems = Boolean.TRUE;
						List<String> page = new ArrayList<String>();

						System.out.print("(" + skipCount + "|" + pageSize
								+ ") ");

						int from = (int) skipCount;
						int to = (int) (skipCount + pageSize);

						if (to >= data.length) {
							to = data.length;
							hasMoreItems = Boolean.FALSE;
						}

						for (int i = from; i < to; i++) {
							page.add(data[i]);
						}

						PageFetchResult<String> result = new AbstractPageFetch.PageFetchResult<String>(
								page,
								BigInteger
										.valueOf(data.length),
								hasMoreItems);

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
		// this.loopAll(0); pageSize must be > 0

		this.loopAll(this.data1, 1);
		this.loopAll(this.data1, 5);

		this.loopAll(this.data0, 1);
		this.loopAll(this.data0, 5);
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
//		this.loopSkip(100, 5); skip out of bound

//		this.loopSkip(0, 0);
		this.loopSkip(this.data10, 0, 1);
		this.loopSkip(this.data10, 0, 10);
		this.loopSkip(this.data10, 0, 100);

//		this.loopSkip(0, 0);
		this.loopSkip(this.data10, 10, 1);
		this.loopSkip(this.data10, 10, 10);
		this.loopSkip(this.data10, 10, 100);

		this.loopSkip(this.data1, 0, 5);
		this.loopSkip(this.data1, 1, 5);

		this.loopSkip(this.data0, 0, 5);
}

	private void loopSkip(String [] data, int skipCount, int pageSize) {
		System.out.println("\nloopSkip (" + skipCount + ", " + pageSize + ")");

		PagingIterable<String> p = this.getIterable(data, pageSize);
		assertNotNull(p);
		PagingIterator<String> i = (PagingIterator<String>) p.iterator();
		assertNotNull(i);
		assertEquals(data.length, i.getTotalNumItems());

		PagingIterable<String> pp = p.skipTo(skipCount);
		assertNotNull(pp);
		PagingIterator<String> ii = (PagingIterator<String>) pp.iterator();
		assertNotNull(ii);
		assertEquals(data.length, ii.getTotalNumItems());

		int count = 0;
		for (String s : pp) {
			assertNotNull(s);
			assertEquals("A" + (count + skipCount), s);
			System.out.print(s + " ");
			count++;
		}
		System.out.print("\n");
		assertEquals(data.length - skipCount, count);
	}

	private void loopAll(String [] data, int pageSize) {
		System.out.println("\nloopAll (" + pageSize + ")");

		PagingIterable<String> p = this.getIterable(data, pageSize);
		assertNotNull(p);
		PagingIterator<String> i = (PagingIterator<String>) p.iterator();
		assertNotNull(i);
		assertEquals(data.length, i.getTotalNumItems());

		int count = 0;
		for (String s : p) {
			assertNotNull(s);
			System.out.print(s + " ");
			count++;
		}
		System.out.print("\n");
		assertEquals(data.length, count);
	}

}
