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
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.tck.CmisTest;
import org.apache.chemistry.opencmis.tck.CmisTestGroup;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;

/**
 * HTML Report without header and footer.
 */
public class CoreHtmlReport extends AbstractCmisTestReport {
    public CoreHtmlReport() {
    }

    @Override
    public void createReport(Map<String, String> parameters, List<CmisTestGroup> groups, Writer writer)
            throws IOException {
        writer.write("<h1>OpenCMIS TCK Report</h1>\n");
        writer.write((new Date()) + "\n");

        writer.write("\n<h2>Parameters</h2>\n");

        if (parameters != null) {
            writer.write("<table>\n");
            for (Map.Entry<String, String> p : (new TreeMap<String, String>(parameters)).entrySet()) {
                String value = p.getValue();
                if (SessionParameter.PASSWORD.endsWith(p.getKey())) {
                    value = "*****";
                }

                writer.write("<tr><td>" + escape(p.getKey()) + "</td><td>" + escape(value) + "</td></tr>\n");
            }
            writer.write("</table>\n");
        }

        writer.write("\n<h2>Groups</h2>\n");

        if (groups != null) {
            for (CmisTestGroup group : groups) {
                printGroupResults(group, writer);
            }
        }

        writer.flush();
    }

    public static void printStyle(Writer writer) throws IOException {
        writer.write("<style TYPE=\"text/css\">\n");
        writer.write(".tckResultINFO { margin-left: 5px; margin-right: 5px; }\n");
        writer.write(".tckResultSKIPPED { margin-left: 5px; margin-right: 5px; background-color: #FFFFFF; }\n");
        writer.write(".tckResultOK { margin-left: 5px; margin-right: 5px; background-color: #00FF00; }\n");
        writer.write(".tckResultWARNING { margin-left: 5px; margin-right: 5px; background-color: #FFFF00; }\n");
        writer.write(".tckResultFAILURE { margin-left: 5px; margin-right: 5px; background-color: #FF6000; }\n");
        writer.write(".tckResultUNEXPECTED_EXCEPTION { margin-left: 5px; margin-right: 5px; background-color: #FF0000; }\n");
        writer.write("</style>");
    }

    private void printGroupResults(CmisTestGroup group, Writer writer) throws IOException {
        if (!group.isEnabled()) {
            return;
        }

        writer.write("\n<hr>\n<h3>" + escape(group.getName()) + "</h3>\n");

        if (group.getDescription() != null) {
            writer.write("\n<p><i>" + escape(group.getDescription()) + "</i></p>\n");
        }

        if (group.getTests() != null) {
            for (CmisTest test : group.getTests()) {
                printTestResults(test, writer);
            }
        }        
    }

    private void printTestResults(CmisTest test, Writer writer) throws IOException {
        if (!test.isEnabled()) {
            return;
        }

        writer.write("\n<h4>" + escape(test.getName()) + " (" + test.getTime() + " ms)</h4>\n");

        if (test.getDescription() != null) {
            writer.write("\n<p><i>" + escape(test.getDescription()) + "</i></p>\n");
        }

        if (test.getResults() != null) {
            for (CmisTestResult result : test.getResults()) {
                writer.write("<div style=\"padding: 5px;\">\n");
                printResult(result, writer);
                writer.write("</div>\n");
            }
        }
    }

    private void printResult(CmisTestResult result, Writer writer) throws IOException {
        writer.write("<div class=\"tckResult" + result.getStatus().name() + "\">\n");

        writer.write("<b>" + result.getStatus() + "</b>: " + escape(result.getMessage()));

        if ((result.getStackTrace() != null) && (result.getStackTrace().length > 0)) {
            writer.write(" (" + escape(result.getStackTrace()[0].getFileName()) + ":"
                    + result.getStackTrace()[0].getLineNumber() + ")");
        }

        writer.write("<br/>\n");

        if (result.getStatus() == CmisTestResultStatus.UNEXPECTED_EXCEPTION && result.getException() != null) {
            final Writer sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            result.getException().printStackTrace(pw);

            writer.write("\n<!--\nStacktrace:\n\n" + escape(sw.toString()) + "-->\n");

            if (result.getException() instanceof CmisBaseException) {
                CmisBaseException cbe = (CmisBaseException) result.getException();
                if (cbe.getErrorContent() != null) {
                    writer.write("\n<!--\nError Content:\n\n" + escape(cbe.getErrorContent()) + "-->\n");
                }
            }
        }

        for (CmisTestResult child : result.getChildren()) {
            printResult(child, writer);
        }

        writer.write("</div>\n");
    }

    protected String escape(String s) {
        if (s == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
            case '\"':
                sb.append("&quot;");
                break;
            case '&':
                sb.append("&amp;");
                break;
            case '<':
                sb.append("&lt;");
                break;
            case '>':
                sb.append("&gt;");
                break;
            default:
                sb.append(c);
            }
        }

        return sb.toString();
    }
}
