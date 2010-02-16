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
package org.apache.opencmis.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.server.spi.AbstractServicesFactory;

/**
 * CMIS context listener.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class CmisRepositoryContextListener implements ServletContextListener {

  public static final String SERVICES_FACTORY = "org.apache.opencmis.servicesfactory";

  private static final Log log = LogFactory.getLog(CmisRepositoryContextListener.class.getName());

  private static final String CONFIG_FILENAME = "/repository.properties";
  private static final String PROPERTY_CLASS = "class";

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
   */
  public void contextInitialized(ServletContextEvent sce) {
    // create services factory
    AbstractServicesFactory factory = createServicesFactory(CONFIG_FILENAME);

    // set the services factory into the servlet context
    sce.getServletContext().setAttribute(SERVICES_FACTORY, factory);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
   */
  public void contextDestroyed(ServletContextEvent sce) {
    // destroy services factory
    AbstractServicesFactory factory = (AbstractServicesFactory) sce.getServletContext()
        .getAttribute(SERVICES_FACTORY);
    if (factory != null) {
      factory.destroy();
    }
  }

  /**
   * Creates a services factory.
   */
  private AbstractServicesFactory createServicesFactory(String filename) {
    // load properties
    InputStream stream = this.getClass().getResourceAsStream(filename);

    if (stream == null) {
      log.warn("Cannot find configuration!");
      return null;
    }

    Properties props = new Properties();
    try {
      props.load(stream);
    }
    catch (IOException e) {
      log.warn("Cannot load configuration: " + e, e);
      return null;
    }

    // get 'class' property
    String className = props.getProperty(PROPERTY_CLASS);
    if (className == null) {
      log.warn("Configuration doesn't contain the property 'class'!");
      return null;
    }

    // create a factory instance
    Object object = null;
    try {
      object = Class.forName(className).newInstance();
    }
    catch (Exception e) {
      log.warn("Could not create a services factory instance: " + e, e);
      return null;
    }

    if (!(object instanceof AbstractServicesFactory)) {
      log.warn("The provided class is not a sub class of AbstractServicesFactory!");
    }

    AbstractServicesFactory factory = (AbstractServicesFactory) object;

    // initialize factory instance
    Map<String, String> parameters = new HashMap<String, String>();

    for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements();) {
      String key = (String) e.nextElement();
      String value = props.getProperty(key);
      parameters.put(key, value);
    }

    factory.init(parameters);

    log.info("Initialized Services Factory: " + parameters);

    return factory;
  }
}
