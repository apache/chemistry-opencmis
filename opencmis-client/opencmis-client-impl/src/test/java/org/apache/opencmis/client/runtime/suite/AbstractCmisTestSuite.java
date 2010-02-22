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
package org.apache.opencmis.client.runtime.suite;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.apache.opencmis.client.runtime.Fixture;

/**
 * Abstract test suite for initialization of
 * <ul>
 * <li>logging
 * <li>abstract fixture initialization
 * </ul>
 * 
 */
public abstract class AbstractCmisTestSuite extends Suite {

  protected Log log = LogFactory.getLog(this.getClass());

  /**
   * Standard suite constructor for initialization.
   * 
   * @param klass
   * @param r
   * @throws InitializationError
   */
  public AbstractCmisTestSuite(Class<?> klass, RunnerBuilder r) throws InitializationError {
    super(klass, r);

    this.initializeLogging();
    this.initializeFixture();
    this.writeLogEntry();
  }

  private void writeLogEntry() {
    this.log.info("---------------------------------------------------------------");
    this.log.info("--- CMIS Test Suite Setup -------------------------------------");
    this.log.info("---------------------------------------------------------------");
    this.log.info("test suite:        " + this.getClass());
    this.log.info("session factory:   " + Fixture.getSessionFactory().getClass());
    this.log.info("session parameter: " + Fixture.getParamter());
    this.log.info("---------------------------------------------------------------");
  }

  /**
   * Abstract fixture initialization.
   */
  protected abstract void initializeFixture();

  /**
   * Initialize logging support.
   */
  private void initializeLogging() {
    Properties p = new Properties();
    try {
      p.load(AbstractCmisTestSuite.class.getResourceAsStream("/log4j.properties"));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    PropertyConfigurator.configure(p);

  }
}
