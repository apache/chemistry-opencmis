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
package org.apache.chemistry.opencmis.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import org.apache.chemistry.opencmis.client.api.SessionFactory;

/**
 * Finds a {@link SessionFactory} implementation and creates an object.
 */
public class SessionFactoryFinder {

    /**
     * Creates a default {@link SessionFactory} object.
     */
    public static SessionFactory find() throws ClassNotFoundException, InstantiationException {
        return find("org.apache.chemistry.opencmis.client.SessionFactory", null);
    }

    /**
     * Creates a {@link SessionFactory} object.
     * 
     * @param factoryId
     *            the factory id of the {@link SessionFactory}
     */
    public static SessionFactory find(String factoryId) throws ClassNotFoundException, InstantiationException {
        return find(factoryId, null);
    }

    /**
     * Creates a {@link SessionFactory} object.
     * 
     * @param factoryId
     *            the factory id of the {@link SessionFactory}
     * @param classLoader
     *            the class loader to use
     */
    public static SessionFactory find(String factoryId, ClassLoader classLoader) throws ClassNotFoundException,
            InstantiationException {
        return find(factoryId, classLoader, "org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl");
    }

    private static SessionFactory find(String factoryId, ClassLoader classLoader, String fallbackClassName)
            throws ClassNotFoundException, InstantiationException {
        ClassLoader cl = classLoader;
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
            if (cl == null) {
                cl = SessionFactoryFinder.class.getClassLoader();
            }
        }

        String factoryClassName = null;

        if (factoryId != null) {
            factoryClassName = System.getProperty(factoryId);

            if (factoryClassName == null) {
                String serviceId = "META-INF/services/" + factoryId;
                InputStream stream = cl.getResourceAsStream(serviceId);
                if (stream != null) {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                        factoryClassName = reader.readLine();
                        reader.close();
                    } catch (IOException e) {
                        factoryClassName = null;
                    }
                }
            }
        }

        if (factoryClassName == null) {
            factoryClassName = fallbackClassName;
        }

        Class<?> clazz = cl.loadClass(factoryClassName);

        SessionFactory result = null;
        try {
            Method newInstanceMethod = clazz.getMethod("newInstance", new Class[0]);

            if (!SessionFactory.class.isAssignableFrom(newInstanceMethod.getReturnType())) {
                throw new ClassNotFoundException("newInstance() method does not return a SessionFactory object!");
            }

            try {
                result = (SessionFactory) newInstanceMethod.invoke(null, new Object[0]);
            } catch (Exception e) {
                throw new InstantiationException("Could not create SessionFactory object!");
            }
        } catch (NoSuchMethodException nsme) {
            if (!SessionFactory.class.isAssignableFrom(clazz)) {
                throw new ClassNotFoundException("The class does not implemnt the SessionFactory interface!", nsme);
            }

            try {
                result = (SessionFactory) clazz.newInstance();
            } catch (Exception e) {
                throw new InstantiationException("Could not create SessionFactory object!");
            }
        }

        return result;
    }
}
