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
package org.apache.chemistry.opencmis.client.api;

/**
 * Iterable for CMIS collections that allows ability to skip to specific
 * position.
 * 
 * @param <T>
 */
public interface PagingIterable<T> extends Iterable<T> {

    /**
     * Skip to position within CMIS collection
     * 
     * @param position
     * @return iterable whose starting point is the specified skip to position
     */
    PagingIterable<T> skipTo(long position);

    /**
     * Gets an iterable for the current page within the CMIS collection using
     * default page size
     * 
     * @return iterable for current page
     */
    PagingIterable<T> getPage();

    /**
     * Gets an iterable for the current page within the CMIS collection
     * 
     * @param maxNumItems
     *            maximum number of items the page will contain
     * 
     * @return iterable for current page
     */
    PagingIterable<T> getPage(int maxNumItems);

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    PagingIterator<T> iterator();
}
