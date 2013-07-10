/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.chemistry.opencmis.jcr.util;

import org.apache.commons.collections.iterators.EmptyIterator;
import org.apache.commons.collections.iterators.IteratorChain;
import org.apache.commons.collections.iterators.SingletonIterator;

import java.util.Iterator;

public class Iterables {
    private Iterables() {}

    public static <T> Iterable<T> concat(final Iterable<T> it1, final Iterable<T> it2) {
        return new Iterable<T>() {
            @SuppressWarnings("unchecked")
            public Iterator<T> iterator() {
                return new IteratorChain(it1.iterator(), it2.iterator());
            }
        };
    }

    public static <T> Iterable<T> singleton(final T element) {
        return new Iterable<T>() {
            @SuppressWarnings("unchecked")
            public Iterator<T> iterator() {
                return new SingletonIterator(element);
            }
        };
    }

    public static <T> Iterable<T> empty() {
        return new Iterable<T>() {
            @SuppressWarnings("unchecked")
            public Iterator<T> iterator() {
                return EmptyIterator.INSTANCE;
            }
        };
    }
}
