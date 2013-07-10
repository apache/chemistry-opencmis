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
package org.apache.chemistry.opencmis.tck;

import java.util.List;
import java.util.Map;

/**
 * CMIS TCK Test.
 */
public interface CmisTest {

    /**
     * Initializes the test with test parameters.
     */
    void init(Map<String, String> parameters) throws Exception;

    /**
     * Returns the name of the test.
     */
    String getName();

    /**
     * Returns the description of the test group.
     */
    String getDescription();
    
    /**
     * Runs the test.
     */
    void run() throws Exception;

    /**
     * Returns if the test is enabled or not.
     */
    boolean isEnabled();

    /**
     * Enables or disables the test.
     */
    void setEnabled(boolean enabled);

    /**
     * Returns the results of the test after {@link #run()} has be called.
     */
    List<CmisTestResult> getResults();

    /**
     * Gets the time (in milliseconds) that the test took to run.
     */
    long getTime();
}
