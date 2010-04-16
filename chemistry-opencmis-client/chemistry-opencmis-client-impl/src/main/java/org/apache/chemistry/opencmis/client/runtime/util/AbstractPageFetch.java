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
import java.util.List;

/**
 * Abstract page fetch.
 * 
 * @param <T>
 */
public abstract class AbstractPageFetch<T> {

	/**
	 * Fetches the given page from the server.
	 * 
	 * @param pageNumber
	 *            number of the page (>= 0).
	 */
	protected abstract PageFetchResult<T> fetchPage(long skipCount);

	// --- fetch result class ---

	protected static class PageFetchResult<T> {
		private List<T> page;
		private BigInteger totalItems;
		private Boolean hasMoreItems;

		public PageFetchResult(List<T> page, BigInteger totalItems, Boolean hasMoreItems) {
			this.page = page;
			this.totalItems = totalItems;
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

}
