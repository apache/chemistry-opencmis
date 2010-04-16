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

import java.util.Iterator;

/**
 * Iterator for scrolling through CMIS collections
 * 
 * @param <T>
 */
public interface PagingIterator<T> extends Iterator<T> {

	/**
	 * Returns the current position within the iterator.
	 * 
	 * @return iterator position
	 */
	long getPosition();

	/**
	 * Returns the total number of items. If the repository knows the total
	 * number of items in a result set, the repository SHOULD include the number
	 * here. If the repository does not know the number of items in a result
	 * set, this parameter SHOULD not be set. The value in the parameter MAY NOT
	 * be accurate the next time the client retrieves the result set or the next
	 * page in the result set.
	 * 
	 * @return total number of items or (-1)
	 */
	long getTotalNumItems();

}
