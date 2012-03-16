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
package org.apache.chemistry.opencmis.commons.impl.json.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.apache.chemistry.opencmis.commons.impl.json.JSONObject;

/**
 * Parser for JSON text. Please note that JSONParser is NOT thread-safe.
 * 
 * (Taken from JSON.simple <http://code.google.com/p/json-simple/> and modified
 * for OpenCMIS.)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONParser {
    public static final int S_INIT = 0;
    public static final int S_IN_FINISHED_VALUE = 1;// string,number,boolean,null,object,array
    public static final int S_IN_OBJECT = 2;
    public static final int S_IN_ARRAY = 3;
    public static final int S_PASSED_PAIR_KEY = 4;
    public static final int S_IN_PAIR_VALUE = 5;
    public static final int S_END = 6;
    public static final int S_IN_ERROR = -1;

    private LinkedList<Integer> handlerStatusStack;
    private Yylex lexer = new Yylex((Reader) null);
    private Yytoken token = null;
    private int status = S_INIT;

    private int peekStatus(LinkedList<Integer> statusStack) {
        if (statusStack.size() == 0) {
            return -1;
        }

        return statusStack.getFirst();
    }

    /**
     * Reset the parser to the initial state without resetting the underlying
     * reader.
     * 
     */
    public void reset() {
        token = null;
        status = S_INIT;
        handlerStatusStack = null;
    }

    /**
     * Reset the parser to the initial state with a new character reader.
     * 
     * @param in
     *            - The new character reader.
     * @throws IOException
     * @throws JSONParseException
     */
    public void reset(Reader in) {
        lexer.yyreset(in);
        reset();
    }

    /**
     * @return The position of the beginning of the current token.
     */
    public int getPosition() {
        return lexer.getPosition();
    }

    public Object parse(String s) throws JSONParseException {
        return parse(s, (ContainerFactory) null);
    }

    public Object parse(String s, ContainerFactory containerFactory) throws JSONParseException {
        StringReader in = new StringReader(s);
        try {
            return parse(in, containerFactory);
        } catch (IOException ie) {
            /*
             * Actually it will never happen.
             */
            throw new JSONParseException(-1, JSONParseException.ERROR_UNEXPECTED_EXCEPTION, ie);
        }
    }

    public Object parse(Reader in) throws IOException, JSONParseException {
        return parse(in, (ContainerFactory) null);
    }

    /**
     * Parse JSON text into java object from the input source.
     * 
     * @param in
     * @param containerFactory
     *            - Use this factory to createyour own JSON object and JSON
     *            array containers.
     * @return Instance of the following: org.json.simple.JSONObject,
     *         org.json.simple.JSONArray, java.lang.String, java.lang.Number,
     *         java.lang.Boolean, null
     * 
     * @throws IOException
     * @throws JSONParseException
     */
    @SuppressWarnings("unchecked")
    public Object parse(Reader in, ContainerFactory containerFactory) throws IOException, JSONParseException {
        reset(in);
        LinkedList<Integer> statusStack = new LinkedList<Integer>();
        LinkedList<Object> valueStack = new LinkedList<Object>();

        try {
            do {
                nextToken();
                switch (status) {
                case S_INIT:
                    switch (token.type) {
                    case Yytoken.TYPE_VALUE:
                        status = S_IN_FINISHED_VALUE;
                        statusStack.addFirst(new Integer(status));
                        valueStack.addFirst(token.value);
                        break;
                    case Yytoken.TYPE_LEFT_BRACE:
                        status = S_IN_OBJECT;
                        statusStack.addFirst(new Integer(status));
                        valueStack.addFirst(createObjectContainer(containerFactory));
                        break;
                    case Yytoken.TYPE_LEFT_SQUARE:
                        status = S_IN_ARRAY;
                        statusStack.addFirst(new Integer(status));
                        valueStack.addFirst(createArrayContainer(containerFactory));
                        break;
                    default:
                        status = S_IN_ERROR;
                    }// inner switch
                    break;

                case S_IN_FINISHED_VALUE:
                    if (token.type == Yytoken.TYPE_EOF)
                        return valueStack.removeFirst();
                    else
                        throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);

                case S_IN_OBJECT:
                    switch (token.type) {
                    case Yytoken.TYPE_COMMA:
                        break;
                    case Yytoken.TYPE_VALUE:
                        if (token.value instanceof String) {
                            String key = (String) token.value;
                            valueStack.addFirst(key);
                            status = S_PASSED_PAIR_KEY;
                            statusStack.addFirst(new Integer(status));
                        } else {
                            status = S_IN_ERROR;
                        }
                        break;
                    case Yytoken.TYPE_RIGHT_BRACE:
                        if (valueStack.size() > 1) {
                            statusStack.removeFirst();
                            valueStack.removeFirst();
                            status = peekStatus(statusStack);
                        } else {
                            status = S_IN_FINISHED_VALUE;
                        }
                        break;
                    default:
                        status = S_IN_ERROR;
                        break;
                    }// inner switch
                    break;

                case S_PASSED_PAIR_KEY:
                    switch (token.type) {
                    case Yytoken.TYPE_COLON:
                        break;
                    case Yytoken.TYPE_VALUE:
                        statusStack.removeFirst();
                        String key = (String) valueStack.removeFirst();
                        Map<String, Object> parent = (Map<String, Object>) valueStack.getFirst();
                        parent.put(key, token.value);
                        status = peekStatus(statusStack);
                        break;
                    case Yytoken.TYPE_LEFT_SQUARE:
                        statusStack.removeFirst();
                        key = (String) valueStack.removeFirst();
                        parent = (Map<String, Object>) valueStack.getFirst();
                        List<Object> newArray = createArrayContainer(containerFactory);
                        parent.put(key, newArray);
                        status = S_IN_ARRAY;
                        statusStack.addFirst(new Integer(status));
                        valueStack.addFirst(newArray);
                        break;
                    case Yytoken.TYPE_LEFT_BRACE:
                        statusStack.removeFirst();
                        key = (String) valueStack.removeFirst();
                        parent = (Map<String, Object>) valueStack.getFirst();
                        Map<String, Object> newObject = createObjectContainer(containerFactory);
                        parent.put(key, newObject);
                        status = S_IN_OBJECT;
                        statusStack.addFirst(new Integer(status));
                        valueStack.addFirst(newObject);
                        break;
                    default:
                        status = S_IN_ERROR;
                    }
                    break;

                case S_IN_ARRAY:
                    switch (token.type) {
                    case Yytoken.TYPE_COMMA:
                        break;
                    case Yytoken.TYPE_VALUE:
                        List<Object> val = (List<Object>) valueStack.getFirst();
                        val.add(token.value);
                        break;
                    case Yytoken.TYPE_RIGHT_SQUARE:
                        if (valueStack.size() > 1) {
                            statusStack.removeFirst();
                            valueStack.removeFirst();
                            status = peekStatus(statusStack);
                        } else {
                            status = S_IN_FINISHED_VALUE;
                        }
                        break;
                    case Yytoken.TYPE_LEFT_BRACE:
                        val = (List<Object>) valueStack.getFirst();
                        Map<String, Object> newObject = createObjectContainer(containerFactory);
                        val.add(newObject);
                        status = S_IN_OBJECT;
                        statusStack.addFirst(new Integer(status));
                        valueStack.addFirst(newObject);
                        break;
                    case Yytoken.TYPE_LEFT_SQUARE:
                        val = (List<Object>) valueStack.getFirst();
                        List<Object> newArray = createArrayContainer(containerFactory);
                        val.add(newArray);
                        status = S_IN_ARRAY;
                        statusStack.addFirst(new Integer(status));
                        valueStack.addFirst(newArray);
                        break;
                    default:
                        status = S_IN_ERROR;
                    }// inner switch
                    break;
                case S_IN_ERROR:
                    throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);
                }// switch
                if (status == S_IN_ERROR) {
                    throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);
                }
            } while (token.type != Yytoken.TYPE_EOF);
        } catch (IOException ie) {
            throw ie;
        }

        throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);
    }

    private void nextToken() throws JSONParseException, IOException {
        token = lexer.yylex();
        if (token == null)
            token = new Yytoken(Yytoken.TYPE_EOF, null);
    }

    private Map<String, Object> createObjectContainer(ContainerFactory containerFactory) {
        if (containerFactory == null) {
            return new JSONObject();
        }

        Map<String, Object> m = containerFactory.createObjectContainer();

        if (m == null) {
            return new JSONObject();
        }

        return m;
    }

    private List<Object> createArrayContainer(ContainerFactory containerFactory) {
        if (containerFactory == null) {
            return new JSONArray();
        }

        List<Object> l = containerFactory.creatArrayContainer();

        if (l == null) {
            return new JSONArray();
        }

        return l;
    }

    public void parse(String s, ContentHandler contentHandler) throws JSONParseException {
        parse(s, contentHandler, false);
    }

    public void parse(String s, ContentHandler contentHandler, boolean isResume) throws JSONParseException {
        StringReader in = new StringReader(s);
        try {
            parse(in, contentHandler, isResume);
        } catch (IOException ie) {
            /*
             * Actually it will never happen.
             */
            throw new JSONParseException(-1, JSONParseException.ERROR_UNEXPECTED_EXCEPTION, ie);
        }
    }

    public void parse(Reader in, ContentHandler contentHandler) throws IOException, JSONParseException {
        parse(in, contentHandler, false);
    }

    /**
     * Stream processing of JSON text.
     * 
     * @see ContentHandler
     * 
     * @param in
     * @param contentHandler
     * @param isResume
     *            - Indicates if it continues previous parsing operation. If set
     *            to true, resume parsing the old stream, and parameter 'in'
     *            will be ignored. If this method is called for the first time
     *            in this instance, isResume will be ignored.
     * 
     * @throws IOException
     * @throws JSONParseException
     */
    public void parse(Reader in, ContentHandler contentHandler, boolean isResume) throws IOException, JSONParseException {
        if (!isResume) {
            reset(in);
            handlerStatusStack = new LinkedList<Integer>();
        } else {
            if (handlerStatusStack == null) {
                isResume = false;
                reset(in);
                handlerStatusStack = new LinkedList<Integer>();
            }
        }

        LinkedList<Integer> statusStack = handlerStatusStack;

        try {
            do {
                switch (status) {
                case S_INIT:
                    contentHandler.startJSON();
                    nextToken();
                    switch (token.type) {
                    case Yytoken.TYPE_VALUE:
                        status = S_IN_FINISHED_VALUE;
                        statusStack.addFirst(new Integer(status));
                        if (!contentHandler.primitive(token.value))
                            return;
                        break;
                    case Yytoken.TYPE_LEFT_BRACE:
                        status = S_IN_OBJECT;
                        statusStack.addFirst(new Integer(status));
                        if (!contentHandler.startObject())
                            return;
                        break;
                    case Yytoken.TYPE_LEFT_SQUARE:
                        status = S_IN_ARRAY;
                        statusStack.addFirst(new Integer(status));
                        if (!contentHandler.startArray())
                            return;
                        break;
                    default:
                        status = S_IN_ERROR;
                    }// inner switch
                    break;

                case S_IN_FINISHED_VALUE:
                    nextToken();
                    if (token.type == Yytoken.TYPE_EOF) {
                        contentHandler.endJSON();
                        status = S_END;
                        return;
                    } else {
                        status = S_IN_ERROR;
                        throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);
                    }

                case S_IN_OBJECT:
                    nextToken();
                    switch (token.type) {
                    case Yytoken.TYPE_COMMA:
                        break;
                    case Yytoken.TYPE_VALUE:
                        if (token.value instanceof String) {
                            String key = (String) token.value;
                            status = S_PASSED_PAIR_KEY;
                            statusStack.addFirst(new Integer(status));
                            if (!contentHandler.startObjectEntry(key))
                                return;
                        } else {
                            status = S_IN_ERROR;
                        }
                        break;
                    case Yytoken.TYPE_RIGHT_BRACE:
                        if (statusStack.size() > 1) {
                            statusStack.removeFirst();
                            status = peekStatus(statusStack);
                        } else {
                            status = S_IN_FINISHED_VALUE;
                        }
                        if (!contentHandler.endObject())
                            return;
                        break;
                    default:
                        status = S_IN_ERROR;
                        break;
                    }// inner switch
                    break;

                case S_PASSED_PAIR_KEY:
                    nextToken();
                    switch (token.type) {
                    case Yytoken.TYPE_COLON:
                        break;
                    case Yytoken.TYPE_VALUE:
                        statusStack.removeFirst();
                        status = peekStatus(statusStack);
                        if (!contentHandler.primitive(token.value))
                            return;
                        if (!contentHandler.endObjectEntry())
                            return;
                        break;
                    case Yytoken.TYPE_LEFT_SQUARE:
                        statusStack.removeFirst();
                        statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
                        status = S_IN_ARRAY;
                        statusStack.addFirst(new Integer(status));
                        if (!contentHandler.startArray())
                            return;
                        break;
                    case Yytoken.TYPE_LEFT_BRACE:
                        statusStack.removeFirst();
                        statusStack.addFirst(new Integer(S_IN_PAIR_VALUE));
                        status = S_IN_OBJECT;
                        statusStack.addFirst(new Integer(status));
                        if (!contentHandler.startObject())
                            return;
                        break;
                    default:
                        status = S_IN_ERROR;
                    }
                    break;

                case S_IN_PAIR_VALUE:
                    /*
                     * S_IN_PAIR_VALUE is just a marker to indicate the end of
                     * an object entry, it doesn't proccess any token, therefore
                     * delay consuming token until next round.
                     */
                    statusStack.removeFirst();
                    status = peekStatus(statusStack);
                    if (!contentHandler.endObjectEntry())
                        return;
                    break;

                case S_IN_ARRAY:
                    nextToken();
                    switch (token.type) {
                    case Yytoken.TYPE_COMMA:
                        break;
                    case Yytoken.TYPE_VALUE:
                        if (!contentHandler.primitive(token.value))
                            return;
                        break;
                    case Yytoken.TYPE_RIGHT_SQUARE:
                        if (statusStack.size() > 1) {
                            statusStack.removeFirst();
                            status = peekStatus(statusStack);
                        } else {
                            status = S_IN_FINISHED_VALUE;
                        }
                        if (!contentHandler.endArray())
                            return;
                        break;
                    case Yytoken.TYPE_LEFT_BRACE:
                        status = S_IN_OBJECT;
                        statusStack.addFirst(new Integer(status));
                        if (!contentHandler.startObject())
                            return;
                        break;
                    case Yytoken.TYPE_LEFT_SQUARE:
                        status = S_IN_ARRAY;
                        statusStack.addFirst(new Integer(status));
                        if (!contentHandler.startArray())
                            return;
                        break;
                    default:
                        status = S_IN_ERROR;
                    }// inner switch
                    break;

                case S_END:
                    return;

                case S_IN_ERROR:
                    throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);
                }// switch
                if (status == S_IN_ERROR) {
                    throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);
                }
            } while (token.type != Yytoken.TYPE_EOF);
        } catch (IOException ie) {
            status = S_IN_ERROR;
            throw ie;
        } catch (JSONParseException pe) {
            status = S_IN_ERROR;
            throw pe;
        } catch (RuntimeException re) {
            status = S_IN_ERROR;
            throw re;
        } catch (Error e) {
            status = S_IN_ERROR;
            throw e;
        }

        status = S_IN_ERROR;
        throw new JSONParseException(getPosition(), JSONParseException.ERROR_UNEXPECTED_TOKEN, token);
    }
}
