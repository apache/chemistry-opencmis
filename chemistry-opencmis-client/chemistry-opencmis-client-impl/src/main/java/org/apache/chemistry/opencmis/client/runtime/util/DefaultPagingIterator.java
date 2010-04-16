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

import java.util.List;

import org.apache.chemistry.opencmis.client.api.PagingIterator;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetch.PageFetchResult;

/**
 * Base <code>PagingIterator</code> implementation.
 * 
 * @param <T>
 */
public class DefaultPagingIterator<T> implements PagingIterator<T> {

	private long skipCount;
	private int skipOffset = 0;

	private AbstractPageFetch<T> pageFetch;

	private Long totalItems = null;
	private PageFetchResult<T> page = null;

	/**
	 * Construct
	 * 
	 * @param skipCount
	 * @param pageFetch
	 */
	public DefaultPagingIterator(long skipCount, AbstractPageFetch<T> pageFetch) {
		this.skipCount = skipCount;
		this.pageFetch = pageFetch;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.chemistry.opencmis.client.api.util.PagingIterator#getPosition
	 * ()
	 */
	public long getPosition() {
		return skipCount + skipOffset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.chemistry.opencmis.client.api.util.PagingIterator#getTotalNumItems
	 * ()
	 */
	public long getTotalNumItems() {
		if (totalItems == null) {
			PageFetchResult<T> page = getPage();
			if (page != null) {
				// set number of items
				if (page.getTotalItems() != null) {
					totalItems = page.getTotalItems().longValue();
				} else {
					totalItems = -1L;
				}
			}
		}
		return totalItems;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (!hasMoreItems()) {
			return false;
		}

		long totalItems = getTotalNumItems();
		if (totalItems < 0) {
			// we don't know better
			return true;
		}

		return (skipCount + skipOffset) < totalItems - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	public T next() {
		PageFetchResult<T> currentPage = getPage();
		skipOffset++;

		List<T> items = currentPage.getPage();
		if (items == null || items.isEmpty()) {
			return null;
		}

		if (skipOffset == items.size()) {
			skipCount += skipOffset;
			skipOffset = 0;
			this.page = pageFetch.fetchPage(skipCount);
			currentPage = this.page;
			if (currentPage != null) {
				items = currentPage.getPage();
			}
		}

		if (items == null || items.isEmpty() || skipOffset == items.size()) {
			return null;
		}

		return items.get(skipOffset);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private boolean hasMoreItems() {
		PageFetchResult<T> page = getPage();
		if (page == null) {
			return false;
		}
		if (page.getHasMoreItems() != null) {
			return page.getHasMoreItems().booleanValue();
		}
		return false;
	}

	private PageFetchResult<T> getPage() {
		if (page == null) {
			page = pageFetch.fetchPage(skipCount);
		}
		return page;
	}

}
