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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictParser_CmisBaseGrammar.query_return;

/**
 * Utility class providing convenience methods for parsing CMIS queries. 
 *
 */
public class QueryUtil {

    private CmisQueryWalker walker;

    // convenience method because everybody needs this piece of code
    public static CmisQueryWalker getWalker(String statement) throws RecognitionException {
        
        CharStream input = new ANTLRStringStream(statement);
        TokenSource lexer = new CmisQlStrictLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        CmisQlStrictParser parser = new CmisQlStrictParser(tokens);
        CommonTree parserTree; // the ANTLR tree after parsing phase

        query_return parsedStatement = parser.query();
        if (parser.hasErrors()) {
            throw new CmisInvalidArgumentException(parser.getErrorMessages());
        } else if ( tokens.index()!=tokens.size() ) {
            throw new  CmisInvalidArgumentException("Query String has illegal tokens after end of statement: " + tokens.get(tokens.index()));
        }
        
        parserTree = (CommonTree) parsedStatement.getTree();

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(parserTree);
        nodes.setTokenStream(tokens);
        CmisQueryWalker walker = new CmisQueryWalker(nodes);
        return walker;
    }

    public CmisQueryWalker traverseStatement(String statement, QueryObject queryObj, PredicateWalkerBase pw) throws UnsupportedEncodingException, IOException, RecognitionException {
        walker = getWalker(statement);
        walker.query(queryObj, pw);
        walker.getWherePredicateTree();
        return walker;        
    }
    
    public CmisQueryWalker traverseStatementAndCatchExc(String statement, QueryObject queryObj, PredicateWalkerBase pw) {
        try {
            return traverseStatement(statement, queryObj, pw);
        } catch (RecognitionException e) {
            String errorMsg = queryObj.getErrorMessage();
            throw new CmisInvalidArgumentException("Walking of statement failed with RecognitionException error: \n   " + errorMsg);
        } catch (CmisBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new CmisInvalidArgumentException("Walking of statement failed with exception: \n   " + e);
        }
    }

    public String getErrorMessage(RecognitionException e) {
        if (null == walker)
            return e.toString();
        else
            return getErrorMessage(walker, e);
    }
    
    private static String getErrorMessage(BaseRecognizer recognizer, RecognitionException e) {
        String[] tokenNames = recognizer.getTokenNames();
        // String hdr = walker.getErrorHeader(e);
        String hdr = "Line "+e.line+":"+e.charPositionInLine;
        String msg = recognizer.getErrorMessage(e, tokenNames);
        return hdr + " " + msg;
    }
    
}
