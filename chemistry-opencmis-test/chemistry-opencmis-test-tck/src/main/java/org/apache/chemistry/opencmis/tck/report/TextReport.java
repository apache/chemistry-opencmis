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
package org.apache.chemistry.opencmis.tck.report;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.tck.CmisTest;
import org.apache.chemistry.opencmis.tck.CmisTestGroup;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;

/**
 * Text Report.
 */
public class TextReport extends AbstractCmisTestReport {
    public TextReport() {
    }

    @Override
    public void createReport(Map<String, String> parameters, List<CmisTestGroup> groups, Writer writer)
            throws IOException {
        writer.write("***************************************************************\n");
        writer.write("Test Report: " + (new Date()) + "\n");

        writer.write("***************************************************************\n");
        if (parameters != null) {
            for (Map.Entry<String, String> p : (new TreeMap<String, String>(parameters)).entrySet()) {
                writer.write(p.getKey() + " = " + p.getValue() + "\n");
            }
        }
        writer.write("***************************************************************\n");

        if (groups != null) {
            for (CmisTestGroup group : groups) {
                printGroupResults(group, writer);
            }
        }

        writer.flush();
    }

    private void printGroupResults(CmisTestGroup group, Writer writer) throws IOException {
        writer.write("===============================================================\n");
        writer.write(group.getName() + "\n");
        writer.write("===============================================================\n");

        if (group.getTests() != null) {
            for (CmisTest test : group.getTests()) {
                printTestResults(test, writer);
            }
        }
    }

    private void printTestResults(CmisTest test, Writer writer) throws IOException {
        writer.write("---------------------------------------------------------------\n");
        writer.write(test.getName() + " (" + test.getTime() + " ms)\n");
        writer.write("---------------------------------------------------------------\n\n");

        if (test.getResults() != null) {
            for (CmisTestResult result : test.getResults()) {
                printResult(1, result, writer);
                writer.write("\n");
            }
        }

        writer.write("\n");
    }

    private void printResult(int level, CmisTestResult result, Writer writer) throws IOException {
        printIntend(level, writer);
        writer.write(result.getStatus() + ": " + result.getMessage());

        if ((result.getStackTrace() != null) && (result.getStackTrace().length > 0)) {
            writer.write(" (" + result.getStackTrace()[0].getFileName() + ":"
                    + result.getStackTrace()[0].getLineNumber() + ")");
        }

        writer.write("\n");

        if (result.getStatus() == CmisTestResultStatus.UNEXPECTED_EXCEPTION && result.getException() != null) {
            writer.write("\nStacktrace:\n\n");
            result.getException().printStackTrace(new PrintWriter(writer));

            if (result.getException() instanceof CmisBaseException) {
                CmisBaseException cbe = (CmisBaseException) result.getException();
                if (cbe.getErrorContent() != null) {
                    writer.write("\nError Content:\n\n");
                    writer.write(cbe.getErrorContent());
                }
            }
        }

        if (result.getException() != null) {
            printIntend(level, writer);
            writer.write("Exception: " + result.getException().getMessage() + "\n");
        }

        if (result.getRequest() != null) {
            printIntend(level, writer);
            writer.write("Request: " + result.getRequest() + "\n");
        }

        if (result.getRequest() != null) {
            printIntend(level, writer);
            writer.write("Response: " + result.getRequest() + "\n");
        }

        for (CmisTestResult child : result.getChildren()) {
            printResult(level + 1, child, writer);
        }
    }

    private void printIntend(int x, Writer writer) throws IOException {
        for (int i = 0; i < x; i++) {
            writer.write("  ");
        }
    }
}
