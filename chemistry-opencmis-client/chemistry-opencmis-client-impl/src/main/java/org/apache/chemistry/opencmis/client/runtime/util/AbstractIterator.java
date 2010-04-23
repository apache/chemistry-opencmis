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

import org.apache.chemistry.opencmis.client.api.PagingIterator;
import org.apache.chemistry.opencmis.client.runtime.util.AbstractPageFetch.PageFetchResult;

/**
 * Abstract <code>PagingIterator</code> implementation.
 * 
 * @param <T>
 */
public abstract class AbstractIterator<T> implements PagingIterator<T> {

    private long skipCount;
    private int skipOffset;
    private AbstractPageFetch<T> pageFetch;

    private PageFetchResult<T> page = null;
    private Long totalItems = null;
    private Boolean hasMoreItems = null;

    /**
     * Construct
     * 
     * @param skipCount
     * @param pageFetch
     */
    public AbstractIterator(long skipCount, AbstractPageFetch<T> pageFetch) {
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
     * @see org.apache.chemistry.opencmis.client.api.PagingIterator#getTotalNumItems()
     */
    public long getTotalNumItems() {
        if (totalItems == null) {
            totalItems = -1L;
            PageFetchResult<T> page = getCurrentPage();
            if (page != null) {
                // set number of items
                if (page.getTotalItems() != null) {
                    totalItems = page.getTotalItems().longValue();
                }
            }
        }
        return totalItems;
    }
    
    /*
     * (non-Javadoc)
     * @see org.apache.chemistry.opencmis.client.api.PagingIterator#getHasMoreItems()
     */
    public boolean getHasMoreItems() {
        if (hasMoreItems == null) {
            hasMoreItems = false;
            PageFetchResult<T> page = getCurrentPage();
            if (page != null) {
                if (page.getHasMoreItems() != null) {
                    hasMoreItems = page.getHasMoreItems().booleanValue();
                }
            }
        }
        return hasMoreItems;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Iterator#remove()
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets current skip count
     * 
     * @return skip count
     */
    protected long getSkipCount() {
        return skipCount;
    }
    
    /**
     * Gets current skip offset (from skip count)
     * 
     * @return skip offset
     */
    protected int getSkipOffset() {
        return skipOffset;
    }
    
    /**
     * Increment the skip offset by one
     * 
     * @return incremented skip offset
     */
    protected int incrementSkipOffset() {
        return skipOffset++;
    }

    /**
     * Gets the current page of items within collection
     * 
     * @return current page
     */
    protected PageFetchResult<T> getCurrentPage() {
        if (page == null) {
            page = pageFetch.fetchPage(skipCount);
        }
        return page;
    }

    /**
     * Skip to the next page of items within collection
     * 
     * @return next page
     */
    protected PageFetchResult<T> incrementPage() {
        skipCount += skipOffset;
        skipOffset = 0;
        totalItems = null;
        hasMoreItems = null;
        page = pageFetch.fetchPage(skipCount);
        return page;
    }

}
