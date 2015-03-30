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
package org.apache.chemistry.opencmis.commons.impl;

public final class ClassLoaderUtil {

    private ClassLoaderUtil() {
    }

    /**
     * Loads a class. If the context class loader is set, it is used.
     */
    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl == null) {
            return loadClass(className, null);
        }

        try {
            return loadClass(className, ccl);
        } catch (ClassNotFoundException cnf) {
            return loadClass(className, null);
        }
    }

    /**
     * Loads a class from the given class loader.
     */
    public static Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
        if (classLoader == null) {
            return Class.forName(className);
        } else {
            return Class.forName(className, true, classLoader);
        }
    }
}
