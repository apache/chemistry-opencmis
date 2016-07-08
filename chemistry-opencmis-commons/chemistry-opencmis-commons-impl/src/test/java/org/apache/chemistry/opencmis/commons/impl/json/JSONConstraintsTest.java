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
package org.apache.chemistry.opencmis.commons.impl.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.JSONConstraints;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParseException;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;
import org.junit.Test;

public class JSONConstraintsTest {

    private enum JSONElement {
        VALUE, OBJECT, ARRAY
    };

    @SuppressWarnings("rawtypes")
    @Test
    public void testObjectSize() {
        JSONParser parser = new JSONParser();

        for (JSONElement jsonElement : JSONElement.values()) {
            JSONObject jsonObject1 = createObject(JSONConstraints.MAX_OBJECT_SIZE, jsonElement);
            try {
                Object parsedObject = parser.parse(jsonObject1.toJSONString());
                assertNotNull(parsedObject);
                assertTrue(parsedObject instanceof Map);
                assertEquals(JSONConstraints.MAX_OBJECT_SIZE, ((Map) parsedObject).size());
            } catch (JSONParseException e) {
                fail();
            }

            JSONObject jsonObject2 = createObject(JSONConstraints.MAX_OBJECT_SIZE + 1, jsonElement);
            try {
                parser.parse(jsonObject2.toJSONString());
                fail();
            } catch (JSONParseException e) {
                assertEquals(JSONParseException.ERROR_JSON_TOO_BIG, e.getErrorType());
            }
        }
    }

    private JSONObject createObject(int size, JSONElement jsonElement) {
        JSONObject result = new JSONObject();

        for (int i = 0; i < size; i++) {
            switch (jsonElement) {
            case VALUE:
                result.put("key" + i, "value" + i);
                break;
            case OBJECT:
                result.put("key" + i, new JSONObject());
                break;
            case ARRAY:
                result.put("key" + i, new JSONArray());
                break;
            default:
                break;
            }
        }

        return result;
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void testArraySize() {
        JSONParser parser = new JSONParser();

        for (JSONElement jsonElement : JSONElement.values()) {
            JSONArray jsonArray1 = createArray(JSONConstraints.MAX_ARRAY_SIZE, jsonElement);
            try {
                Object parsedObject = parser.parse(jsonArray1.toJSONString());
                assertNotNull(parsedObject);
                assertTrue(parsedObject instanceof List);
                assertEquals(JSONConstraints.MAX_ARRAY_SIZE, ((List) parsedObject).size());
            } catch (JSONParseException e) {
                fail();
            }

            JSONArray jsonArray2 = createArray(JSONConstraints.MAX_ARRAY_SIZE + 1, jsonElement);
            try {
                parser.parse(jsonArray2.toJSONString());
                fail();
            } catch (JSONParseException e) {
                assertEquals(JSONParseException.ERROR_JSON_TOO_BIG, e.getErrorType());
            }
        }
    }

    private JSONArray createArray(int size, JSONElement jsonElement) {
        JSONArray result = new JSONArray();

        for (int i = 0; i < size; i++) {
            switch (jsonElement) {
            case VALUE:
                result.add("value" + i);
                break;
            case OBJECT:
                result.add(new JSONObject());
                break;
            case ARRAY:
                result.add(new JSONArray());
                break;
            default:
                break;
            }
        }

        return result;
    }

    @Test
    public void testDepth() {
        JSONParser parser = new JSONParser();

        JSONObject jsonObject1 = createDeepObject(JSONConstraints.MAX_DEPTH);
        try {
            parser.parse(jsonObject1.toJSONString());
        } catch (JSONParseException e) {
            fail();
        }

        JSONObject jsonObject2 = createDeepObject(JSONConstraints.MAX_DEPTH + 1);
        try {
            parser.parse(jsonObject2.toJSONString());
            fail();
        } catch (JSONParseException e) {
            assertEquals(JSONParseException.ERROR_JSON_TOO_BIG, e.getErrorType());
        }
    }

    private JSONObject createDeepObject(int depth) {
        JSONObject result = new JSONObject();

        JSONObject lastObject = result;
        for (int i = 1; i < depth; i++) {
            JSONObject newObject = new JSONObject();
            lastObject.put("obj", newObject);
            lastObject = newObject;
        }

        return result;
    }
}
