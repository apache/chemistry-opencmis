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
package org.apache.chemistry.opencmis.tck.impl;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.tck.CmisTest;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.CmisTestResultStatus;
import org.junit.Test;

/**
 * Base class for tests.
 */
public abstract class AbstractCmisTest implements CmisTest {

    private Map<String, String> parameters;
    private AbstractCmisTestGroup group;
    private String name;
    private String description;
    private boolean isEnabled = true;
    private List<CmisTestResult> results;
    private long time;

    @Override
    public void init(Map<String, String> parameters) {
        this.parameters = parameters;
        results = new ArrayList<CmisTestResult>();
    }

    protected Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGroup(AbstractCmisTestGroup group) {
        this.group = group;
    }

    public AbstractCmisTestGroup getGroup() {
        return group;
    }

    @Override
    public abstract void run() throws Exception;

    @Test
    public void junit() throws Exception {
        JUnitHelper.run(this);
    }

    @Override
    public List<CmisTestResult> getResults() {
        return results;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    @Override
    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public CmisTestResultImpl createResult(CmisTestResultStatus status, String message, Throwable exception,
            boolean isFatal) {
        return new CmisTestResultImpl(group.getName(), name, message, status, exception, isFatal);
    }

    public CmisTestResultImpl createResult(CmisTestResultStatus status, String message, boolean isFatal) {
        return new CmisTestResultImpl(group.getName(), name, message, status, null, isFatal);
    }

    public CmisTestResultImpl createResult(CmisTestResultStatus status, String message) {
        return new CmisTestResultImpl(group.getName(), name, message, status, null, false);
    }

    public CmisTestResultImpl createInfoResult(String message) {
        return new CmisTestResultImpl(group.getName(), name, message, CmisTestResultStatus.INFO, null, false);
    }

    public void addResult(CmisTestResult result) {
        if (result != null) {
            if (result instanceof CmisTestResultImpl) {
                ((CmisTestResultImpl) result).setStackTrace(getStackTrace());
            }

            results.add(result);
            if (result.isFatal()) {
                throw new FatalTestException(result.getMessage());
            }
        }
    }

    protected StackTraceElement[] getStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        StackTraceElement[] shortStackTrace = new StackTraceElement[0];
        if (stackTrace.length > 3) {
            shortStackTrace = new StackTraceElement[stackTrace.length - 3];
            System.arraycopy(stackTrace, 3, shortStackTrace, 0, stackTrace.length - 3);
        }

        return shortStackTrace;
    }

    protected CmisTestResult addResultChild(CmisTestResult result, CmisTestResult child) {
        if (result == null) {
            return null;
        }

        result.getChildren().add(child);

        return result;
    }

    // --- helpers ----

    protected String formatValue(Object o) {
        if (o == null) {
            return "null";
        }

        if (o instanceof String) {
            return "'" + o + "'";
        } else if (o instanceof Calendar) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            return sdf.format(((Calendar) o).getTime());
        }

        return o.toString();
    }

    // --- asserts ----

    protected boolean isEqual(Object expected, Object actual) {
        if (expected == null && actual == null) {
            return true;
        }

        if (expected != null && expected.equals(actual)) {
            return true;
        }

        return false;
    }

    protected CmisTestResult assertIsTrue(Boolean test, CmisTestResult success, CmisTestResult failure) {
        if (test != null && test) {
            return success;
        }

        if (test == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Null!"));
        } else {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "False!"));
        }
    }

    protected CmisTestResult assertIsFalse(Boolean test, CmisTestResult success, CmisTestResult failure) {
        if (test != null && !test) {
            return success;
        }

        if (test == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Null!"));
        } else {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "True!"));
        }
    }

    protected CmisTestResult assertNull(Object object, CmisTestResult success, CmisTestResult failure) {
        if (object == null) {
            return success;
        }

        return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Object is not null!"));
    }

    protected CmisTestResult assertNotNull(Object object, CmisTestResult success, CmisTestResult failure) {
        if (object != null) {
            return success;
        }

        return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Object is null!"));
    }

    protected CmisTestResult assertStringNullOrEmpty(String str, CmisTestResult success, CmisTestResult failure) {
        if (str == null || str.length() == 0) {
            return success;
        }

        return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "String has this value: " + str));
    }

    protected CmisTestResult assertStringNotEmpty(String str, CmisTestResult success, CmisTestResult failure) {
        if (str != null && str.length() > 0) {
            return success;
        }

        if (str == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "String is null!"));
        } else {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "String is empty!"));
        }
    }

    protected CmisTestResult assertListNotEmpty(List<?> list, CmisTestResult success, CmisTestResult failure) {
        if (list != null && !list.isEmpty()) {
            return success;
        }

        if (list == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "List is null!"));
        } else {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "List is empty!"));
        }
    }

    protected CmisTestResult assertListNullOrEmpty(List<?> list, CmisTestResult success, CmisTestResult failure) {
        if (list == null || list.isEmpty()) {
            return success;
        }

        return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "List is not empty!"));

    }

    protected CmisTestResult assertEquals(Object expected, Object actual, CmisTestResult success, CmisTestResult failure) {
        if (isEqual(expected, actual)) {
            return success;
        }

        return addResultChild(
                failure,
                createResult(CmisTestResultStatus.INFO, "expected: " + formatValue(expected) + " / actual: "
                        + formatValue(actual)));
    }

    protected CmisTestResult assertContains(Collection<?> collection, Object value, CmisTestResult success,
            CmisTestResult failure) {
        if (collection == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Collection is null!"));
        }

        if (collection.contains(value)) {
            return success;
        }

        return addResultChild(failure,
                createResult(CmisTestResultStatus.INFO, "Collection does not contain " + formatValue(value)));
    }

    protected CmisTestResult assertEqualLists(List<?> expected, List<?> actual, CmisTestResult success,
            CmisTestResult failure) {
        if (expected == null && actual == null) {
            return success;
        }

        if (expected == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Expected list is null!"));
        }

        if (actual == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Actual list is null!"));
        }

        if (expected.size() != actual.size()) {
            return addResultChild(
                    failure,
                    createResult(CmisTestResultStatus.INFO, "List sizes don't match! expected: " + expected.size()
                            + " / actual: " + actual.size()));
        }

        for (int i = 0; i < expected.size(); i++) {
            if (!isEqual(expected.get(i), actual.get(i))) {
                return addResultChild(
                        failure,
                        createResult(CmisTestResultStatus.INFO, "expected list item[" + i + "]: "
                                + formatValue(expected.get(i)) + " / actual list item[" + i + "]: "
                                + formatValue(actual.get(i))));
            }
        }

        return success;
    }

    protected CmisTestResult assertEqualArray(Object expected, Object actual, CmisTestResult success,
            CmisTestResult failure) {
        if (expected == null && actual == null) {
            return success;
        }

        if (expected == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Expected array is null!"));
        }

        if (!expected.getClass().isArray()) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Expected array is not an array!"));
        }

        if (actual == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Actual array is null!"));
        }

        if (!actual.getClass().isArray()) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Actual array is not an array!"));
        }

        if (Array.getLength(expected) != Array.getLength(actual)) {
            return addResultChild(
                    failure,
                    createResult(
                            CmisTestResultStatus.INFO,
                            "Array sizes don't match! expected: " + Array.getLength(expected) + " / actual: "
                                    + Array.getLength(actual)));
        }

        for (int i = 0; i < Array.getLength(expected); i++) {
            if (!isEqual(Array.get(expected, i), Array.get(actual, i))) {
                return addResultChild(
                        failure,
                        createResult(CmisTestResultStatus.INFO,
                                "expected array item[" + i + "]: " + formatValue(Array.get(expected, i))
                                        + " / actual array item[" + i + "]: " + formatValue(Array.get(actual, i))));
            }
        }

        return success;
    }

    protected CmisTestResult assertEqualSet(Set<?> expected, Set<?> actual, CmisTestResult success,
            CmisTestResult failure) {
        if (expected == null && actual == null) {
            return success;
        }

        if (expected == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Expected set is null!"));
        }

        if (actual == null) {
            return addResultChild(failure, createResult(CmisTestResultStatus.INFO, "Actual set is null!"));
        }

        if (expected.size() != actual.size()) {
            return addResultChild(
                    failure,
                    createResult(CmisTestResultStatus.INFO, "Set sizes don't match! expected: " + expected.size()
                            + " / actual: " + actual.size()));
        }

        for (Object o : expected) {
            if (!actual.contains(o)) {
                return addResultChild(failure,
                        createResult(CmisTestResultStatus.INFO, "Item not in actual set: " + formatValue(o)));
            }
        }

        return success;
    }
}
