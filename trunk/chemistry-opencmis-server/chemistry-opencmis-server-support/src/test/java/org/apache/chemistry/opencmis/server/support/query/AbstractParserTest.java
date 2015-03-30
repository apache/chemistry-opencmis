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
package org.apache.chemistry.opencmis.server.support.query;

import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.stringtemplate.StringTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is clone of org.antlr.gunit.gUnitBase class adapted to Java style
 * Because the original class can't deal with composite grammar this is a
 * replacement working around this antlr bug.
 * 
 */
public class AbstractParserTest {

    private static final Logger log = LoggerFactory.getLogger(AbstractParserTest.class);

    protected String superGrammarName;
    protected String baseLexerName;
    Class<?> lexer;
    Class<?> parser;
    protected String treeParserPath;

    protected void setUp(Class<?> lexerClass, Class<?> parserClass, String baseGrammar, String baseLexer) {
        lexer = lexerClass;
        parser = parserClass;
        this.superGrammarName = baseGrammar;
        this.baseLexerName = baseLexer;
    }

    protected void tearDown() {
    }

    protected void testLexerOk(String rule, String statement) {
        // test input: "a"
        try {
            Object retval = execLexer(rule, statement);
            log.debug("testing rule " + rule + " parsed to: " + retval);
        } catch (Exception e) {
            fail("testing rule " + rule + ": " + e.toString());
        }
    }

    protected void testLexerFail(String rule, String statement) {
        // test input: "a"
        try {
            Object retval = execLexer(rule, statement);
            fail("testing rule should fail " + rule);
        } catch (Exception e) {
            log.debug("testing rule " + rule + " parsed with exception: " + e);
        }
    }

    protected void testParserOk(String rule, String statement) {
        try {
            Object retval = execParser(rule, statement);
            log.debug("testing rule " + rule + " parsed to: " + retval);
        } catch (Exception e) {
            fail("testing rule " + rule + " failed: " + e.toString());
        }
    }

    protected void testParserFail(String rule, String statement) {
        try {
            Object retval = execParser(rule, statement);
            fail("testing rule should fail " + rule);
        } catch (Exception e) {
            log.debug("testing rule " + rule + " failed: " + e.toString());
        }
    }

    protected void testParser(String rule, String statement, String expectedResult) {
        try {
            Object actual = execParser(rule, statement);
            log.debug("testing rule " + rule + " parsed to: " + actual);
        } catch (Exception e) {
            fail("testing rule " + rule + " failed: " + e);
        }
    }

    // Invoke target lexer.rule
    public String execLexer(String testRuleName, String testInput) throws Exception {
        String result = null;
        CharStream input;
        /** Set up ANTLR input stream based on input source, file or String */
        input = new ANTLRStringStream(testInput);

        /** Use Reflection to create instances of lexer and parser */
        Class<?>[] lexArgTypes = new Class[] { CharStream.class }; // assign
                                                                   // type to
                                                                   // lexer's
                                                                   // args
        Constructor<?> lexConstructor = lexer.getConstructor(lexArgTypes);
        Object[] lexArgs = new Object[] { input }; // assign value to lexer's
                                                   // args
        Object lexObj = lexConstructor.newInstance(lexArgs); // makes new
                                                             // instance of
                                                             // lexer
        Method ruleName = null;

        try {
            ruleName = lexer.getMethod("m" + testRuleName, new Class[0]);
        } catch (NoSuchMethodException e) {
            // try superclass lexers
            Class<?> lexerSuper = Class.forName(lexer.getName() + "_" + baseLexerName);
            ruleName = lexerSuper.getMethod("m" + testRuleName, new Class[0]);
            lexArgTypes = new Class[] { CharStream.class, CmisQlStrictLexer.class };
            lexArgs = new Object[] { input, lexObj };
            lexConstructor = lexerSuper.getConstructor(lexArgTypes);
            lexObj = lexConstructor.newInstance(lexArgs);
        }
        /** Invoke lexer rule, and get the current index in CharStream */
        ruleName.invoke(lexObj, new Object[0]);
        Method ruleName2 = lexer.getMethod("getCharIndex", new Class[0]);
        int currentIndex = (Integer) ruleName2.invoke(lexObj, new Object[0]);
        if (currentIndex != input.size()) {
            throw new RuntimeException("extra text found, '" + input.substring(currentIndex, input.size() - 1) + "'");
            // System.out.println("extra text found, '"+input.substring(currentIndex,
            // input.size()-1)+"'");
        }

        return result;
    }

    // Invoke target parser.rule
    public Object execParser(String testRuleName, String testInput) throws Exception {
        String result = null;
        CharStream input;
        /** Set up ANTLR input stream based on input source, file or String */
        input = new ANTLRStringStream(testInput);

        /** Use Reflection to create instances of lexer and parser */
        // assign type to lexer's args
        Class<?>[] lexArgTypes = new Class[] { CharStream.class };
        Constructor<?> lexConstructor = lexer.getConstructor(lexArgTypes);

        // assign value to lexer's args
        Object[] lexArgs = new Object[] { input };

        // makes new instance of lexer
        Object lexObj = lexConstructor.newInstance(lexArgs);

        CommonTokenStream tokens = new CommonTokenStream((Lexer) lexObj);
        
        // assign type to parser's args
        Class<?>[] parArgTypes = new Class[] { TokenStream.class };
        Constructor<?> parConstructor = parser.getConstructor(parArgTypes);

        // assign value to parser's args
        Object[] parArgs = new Object[] { tokens };

        // makes new instance of parser
        Object parObj = parConstructor.newInstance(parArgs);

        Method ruleName = parser.getMethod(testRuleName);

        /** Invoke grammar rule, and store if there is a return value */
        Object ruleReturn = ruleName.invoke(parObj);

        /** If rule has return value, determine if it contains an AST or a ST */
        if (ruleReturn != null) {
            if (ruleReturn.getClass().toString().indexOf(testRuleName + "_return") > 0) {
                try { // NullPointerException may happen here...
                    String classPath = parser.getName();
                    if (null != superGrammarName) {
                        classPath += "_" + superGrammarName;
                    }
                    Class<?> _return = Class.forName(classPath + "$" + testRuleName + "_return");
                    Method[] methods = _return.getDeclaredMethods();
                    for (Method method : methods) {
                        if (method.getName().equals("getTree")) {
                            Method returnName = _return.getMethod("getTree");
                            CommonTree tree = (CommonTree) returnName.invoke(ruleReturn);
                            result = tree.toStringTree();
                        } else if (method.getName().equals("getTemplate")) {
                            Method returnName = _return.getMethod("getTemplate");
                            StringTemplate st = (StringTemplate) returnName.invoke(ruleReturn);
                            result = st.toString();
                        }
                    }
                } catch (Exception e) {
                    // Note: If any exception occurs, the test is viewed as
                    // failed.
                    throw (e);
                }
            }
        }

        /** Invalid input */
        // Since AntLR 3.3 we have to skip EOF tokens at the end 
        // This requires modification of the default code 
        // if (tokens.index() != tokens.size()) {
        // to
        if (tokens.get(tokens.index()).getType() != Token.EOF) {
            throw new RuntimeException("Invalid input.");
        }

        /** Check for syntax errors */
        if (((BaseRecognizer) parObj).getNumberOfSyntaxErrors() > 0) {
            throw new RuntimeException("Syntax error occured");
        }
        return result;
    }

}
