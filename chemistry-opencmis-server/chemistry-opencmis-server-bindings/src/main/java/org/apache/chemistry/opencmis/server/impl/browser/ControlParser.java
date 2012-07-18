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
package org.apache.chemistry.opencmis.server.impl.browser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

/**
 * Parses HTML form controls.
 */
public class ControlParser {

    private final HttpServletRequest request;

    private final Map<String, String> zeroDim = new HashMap<String, String>();
    private final Map<String, Map<Integer, String>> oneDim = new HashMap<String, Map<Integer, String>>();
    private final Map<String, Map<Integer, Map<Integer, String>>> twoDim = new HashMap<String, Map<Integer, Map<Integer, String>>>();

    public ControlParser(HttpServletRequest request) {
        this.request = request;
        parse();
    }

    @SuppressWarnings("unchecked")
    private void parse() {
        // gather all controls
        Map<String, String[]> controls = request.getParameterMap();
        for (Map.Entry<String, String[]> control : controls.entrySet()) {
            String controlName = control.getKey().trim().toLowerCase(Locale.ENGLISH);

            int firstIndex = getFirstIndex(controlName);

            if (firstIndex == -1) {
                zeroDim.put(controlName, control.getValue()[0]);
            } else {
                String strippedControlName = controlName.substring(0, controlName.indexOf('['));
                int secondIndex = getSecondIndex(controlName);

                if (secondIndex == -1) {
                    Map<Integer, String> values = oneDim.get(strippedControlName);
                    if (values == null) {
                        values = new HashMap<Integer, String>();
                        oneDim.put(strippedControlName, values);
                    }

                    values.put(firstIndex, control.getValue()[0]);
                } else {
                    Map<Integer, Map<Integer, String>> values = twoDim.get(strippedControlName);
                    if (values == null) {
                        values = new HashMap<Integer, Map<Integer, String>>();
                        twoDim.put(strippedControlName, values);
                    }

                    Map<Integer, String> list = values.get(firstIndex);
                    if (list == null) {
                        list = new HashMap<Integer, String>();
                        values.put(firstIndex, list);
                    }

                    list.put(secondIndex, control.getValue()[0]);
                }
            }
        }
    }

    private static int getFirstIndex(String controlName) {
        int result = -1;

        int open = controlName.indexOf('[');
        int close = controlName.indexOf(']');

        if (open == -1 || close == -1 || close < open) {
            return result;
        }

        String indexStr = controlName.substring(open + 1, close);
        try {
            result = Integer.parseInt(indexStr);
            if (result < 0) {
                result = -1;
            }
        } catch (NumberFormatException e) {
        }

        return result;
    }

    private static int getSecondIndex(String controlName) {
        int result = -1;

        int open = controlName.indexOf("][");
        int close = controlName.lastIndexOf(']');

        if (open == -1 || close == -1 || close < open) {
            return result;
        }

        String indexStr = controlName.substring(open + 2, close);
        try {
            result = Integer.parseInt(indexStr);
            if (result < 0) {
                result = -1;
            }
        } catch (NumberFormatException e) {
        }

        return result;
    }

    private static List<String> convertToList(String controlName, Map<Integer, String> map) {
        if (map == null) {
            return null;
        }

        int count = map.size();
        List<String> result = new ArrayList<String>(count);

        for (int i = 0; i < count; i++) {
            String value = map.get(i);
            if (value == null) {
                throw new CmisInvalidArgumentException(controlName + " has gaps!");
            }
            result.add(value);
        }

        return result;
    }

    public String getValue(String controlName) {
        if (controlName == null) {
            throw new IllegalArgumentException("controlName must not be null!");
        }

        return zeroDim.get(controlName.toLowerCase(Locale.ENGLISH));
    }

    public List<String> getValues(String controlName) {
        if (controlName == null) {
            throw new IllegalArgumentException("controlName must not be null!");
        }

        return convertToList(controlName, oneDim.get(controlName.toLowerCase(Locale.ENGLISH)));
    }

    public List<String> getValues(String controlName, int index) {
        if (controlName == null) {
            throw new IllegalArgumentException("controlName must not be null!");
        }

        Map<Integer, Map<Integer, String>> map = twoDim.get(controlName.toLowerCase(Locale.ENGLISH));
        if (map == null) {
            return null;
        }

        return convertToList(controlName, map.get(index));
    }

    public Map<Integer, String> getOneDimMap(String controlName) {
        if (controlName == null) {
            throw new IllegalArgumentException("controlName must not be null!");
        }

        return oneDim.get(controlName.toLowerCase(Locale.ENGLISH));
    }

    public Map<Integer, Map<Integer, String>> getTwoDimMap(String controlName) {
        if (controlName == null) {
            throw new IllegalArgumentException("controlName must not be null!");
        }

        return twoDim.get(controlName.toLowerCase(Locale.ENGLISH));
    }
}
