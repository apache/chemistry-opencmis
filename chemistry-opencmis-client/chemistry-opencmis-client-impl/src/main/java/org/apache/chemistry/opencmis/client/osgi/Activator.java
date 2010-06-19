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
package org.apache.chemistry.opencmis.client.osgi;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

/**
 * OSGi Bundle activator for the OpenCMIS client which registers an instance of
 * the {@link SessionFactory} in the OSGi service registry.
 */
public class Activator implements BundleActivator {

    public void start(BundleContext context) throws Exception {
        // register the MetaTypeService now, that we are ready
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(Constants.SERVICE_DESCRIPTION, "Apache Chemistry OpenCMIS Client Session Factory");
        props.put(Constants.SERVICE_VENDOR, "Apache Software Foundation");

        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        context.registerService(SessionFactory.class.getName(), sessionFactory, props);
    }

    public void stop(BundleContext context) throws Exception {
        // The SessionFactory service will be unregistered automatically
    }
}
