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
package org.apache.chemistry.opencmis.fit.tck;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.tck.CmisTest;
import org.apache.chemistry.opencmis.tck.CmisTestGroup;
import org.apache.chemistry.opencmis.tck.CmisTestProgressMonitor;
import org.apache.chemistry.opencmis.tck.CmisTestReport;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;
import org.apache.chemistry.opencmis.tck.impl.TestParameters;
import org.apache.chemistry.opencmis.tck.report.TextReport;
import org.apache.chemistry.opencmis.tck.runner.AbstractRunner;
import org.junit.Test;

public abstract class AbstractTckIT extends AbstractRunner {
    public static final String HOST = "localhost";
    public static final int PORT = 19080;

    public static final String REPOSITORY_ID = "test";
    public static final String USER = "test";
    public static final String PASSWORD = "test";

    public abstract Map<String, String> getSessionParameters();

    public abstract BindingType getBindingType();

    public Map<String, String> getBaseSessionParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put(SessionParameter.REPOSITORY_ID, REPOSITORY_ID);
        parameters.put(SessionParameter.USER, USER);
        parameters.put(SessionParameter.PASSWORD, PASSWORD);

        parameters.put(TestParameters.DEFAULT_DOCUMENT_TYPE, "VersionableType");
        parameters.put(TestParameters.DEFAULT_FOLDER_TYPE, "cmis:folder");

        return parameters;
    }

    @Test
    public void runTck() throws Exception {
        // set up TCK and run it
        setParameters(getSessionParameters());
        loadDefaultTckGroups();

        run(new TestProgressMonitor());

        // write report
        File target = new File("target");
        target.mkdir();

        CmisTestReport report = new TextReport();
        report.createReport(getParameters(), getGroups(), new File(target, "tck-result-" + getBindingType().value()
                + ".txt"));

        // find failures
        for (CmisTestGroup group : getGroups()) {
            for (CmisTest test : group.getTests()) {
                for (CmisTestResult result : test.getResults()) {
                    assertTrue(result.getStatus() != CmisTestResultStatus.FAILURE);
                    assertTrue(result.getStatus() != CmisTestResultStatus.UNEXPECTED_EXCEPTION);
                }
            }
        }
    }

    public static CmisTestResultStatus getWorst(List<CmisTestResult> results) {
        if ((results == null) || (results.isEmpty())) {
            return CmisTestResultStatus.OK;
        }

        int max = 0;

        for (CmisTestResult result : results) {
            if (max < result.getStatus().getLevel()) {
                max = result.getStatus().getLevel();
            }
        }

        return CmisTestResultStatus.fromLevel(max);
    }

    private static class TestProgressMonitor implements CmisTestProgressMonitor {
        public void startGroup(CmisTestGroup group) {
            System.out.println();
            System.out.println(group.getName() + " (" + group.getTests().size() + " tests)");
        }

        public void endGroup(CmisTestGroup group) {
            System.out.println();
        }

        public void startTest(CmisTest test) {
            System.out.print("  " + test.getName());
        }

        public void endTest(CmisTest test) {
            System.out.print(" (" + test.getTime() + "ms): ");
            System.out.println(getWorst(test.getResults()));
        }

        public void message(String msg) {
            System.out.println(msg);
        }
    }
}
