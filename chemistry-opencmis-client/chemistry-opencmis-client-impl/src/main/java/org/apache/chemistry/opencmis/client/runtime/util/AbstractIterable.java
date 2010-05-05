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

import java.util.Iterator;

import org.apache.chemistry.opencmis.client.api.ItemIterable;

/**
 * Abstract <code>ItemIterable</code> implementation.
 * 
 * @param <T>
 */
public abstract class AbstractIterable<T> implements ItemIterable<T> {

    private AbstractPageFetch<T> pageFetch;
    private long skipCount;
    private AbstractIterator<T> iterator;

    /**
     * Construct
     * 
     * @param pageFetch
     */
    public AbstractIterable(AbstractPageFetch<T> pageFetch) {
        this(0, pageFetch);
    }

    /**
     * Construct
     * 
     * @param position
     * @param pageFetch
     */
    protected AbstractIterable(long position, AbstractPageFetch<T> pageFetch) {
        this.pageFetch = pageFetch;
        this.skipCount = position;
    }

    /**
     * Gets skip count
     * @return  skip count
     */
    protected long getSkipCount() {
        return skipCount;
    }
    
    /**
     * Gets the page fetcher
     * 
     * @return  page fetcher
     */
    protected AbstractPageFetch<T> getPageFetch() {
        return pageFetch;
    }
    
    /**
     * Construct Iterator
     * 
     * @return  iterator
     */
    protected abstract AbstractIterator<T> createIterator();
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<T> iterator() {
        return getIterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.chemistry.opencmis.client.api.util.PagingIterable#skipTo(long)
     */
    public ItemIterable<T> skipTo(long position) {
        return new CollectionIterable<T>(position, pageFetch);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.chemistry.opencmis.client.api.ItemIterable#getPage()
     */
    public ItemIterable<T> getPage() {
        return new CollectionPageIterable<T>(skipCount, pageFetch);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.chemistry.opencmis.client.api.ItemIterable#getPage(int)
     */
    public ItemIterable<T> getPage(int maxNumItems) {
        this.pageFetch.setMaxNumItems(maxNumItems);
        return new CollectionPageIterable<T>(skipCount, pageFetch);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.chemistry.opencmis.client.api.ItemIterable#getPageNumItems()
     */
    public long getPageNumItems() {
        return getIterator().getPageNumItems();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.chemistry.opencmis.client.api.ItemIterable#getHasMoreItems()
     */
    public boolean getHasMoreItems() {
        return getIterator().getHasMoreItems();
    }

    /*
     * (non-Javadoc)
     * @see org.apache.chemistry.opencmis.client.api.ItemIterable#getTotalNumItems()
     */
    public long getTotalNumItems() {
        return getIterator().getTotalNumItems();
    }
    
    private AbstractIterator<T> getIterator() {
        if (this.iterator == null) {
            this.iterator = createIterator();
        }
        return this.iterator;
    }
}

