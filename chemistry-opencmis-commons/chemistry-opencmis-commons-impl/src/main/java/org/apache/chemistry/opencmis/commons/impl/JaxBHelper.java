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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.chemistry.opencmis.commons.impl.jaxb.ObjectFactory;

/**
 * JAXB helper class.
 */
public final class JaxBHelper {

    public static final ObjectFactory CMIS_OBJECT_FACTORY = new ObjectFactory();
    public static final JAXBContext CONTEXT;
    static {
        JAXBContext jc = null;
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(JaxBHelper.class.getClassLoader());

            jc = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        } finally {
            Thread.currentThread().setContextClassLoader(cl);
        }
        CONTEXT = jc;
    }

    /**
     * Private constructor.
     */
    private JaxBHelper() {
    }

    /**
     * Creates an Unmarshaller.
     */
    public static Unmarshaller createUnmarshaller() throws JAXBException {
        return CONTEXT.createUnmarshaller();
    }
}
