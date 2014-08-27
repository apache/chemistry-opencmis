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
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.server.support.query.CmisQlStrictParser_CmisBaseGrammar.query_return;

/**
 * Support class to assist in parsing and processing CMIS queries.
 * This class inherits from QueryUtilBase to use the error handling
 * methods. It does not follow its design and is only maintained for
 * backwards compatibility.
 * 
 * @deprecated Use {@link QueryUtilBase} instead.
 */
@Deprecated
public class QueryUtil extends QueryUtilBase<CmisQueryWalker> {
    
    public QueryUtil() {
        super(null, null, null);
    }
    
    @Override
    public CommonTree parseStatement() throws RecognitionException {
        throw new CmisRuntimeException("Not supported, use getWalker to parse a query using this legacy class.");        
    }

    @Override
    public void walkStatement() throws CmisQueryException, RecognitionException {
        throw new CmisRuntimeException("Not supported, use getWalker to parse a query using this legacy class.");
    }

    /**
     * Parse a CMISQL statement and return a tree that can be walked to evaluate the expression
     * of the query (usually not used directly but through traverseStatement)
     * 
     * @param statement
     *      CMISQL statement
     * @return
     *      an AntLR tree grammar that can be traversed to evaluate the query
     *      
     * @throws RecognitionException
     */
    public static CmisQueryWalker getWalker(String statement) throws RecognitionException {
        CharStream input = new ANTLRStringStream(statement);
        CmisQlStrictLexer lexer = new CmisQlStrictLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        CmisQlStrictParser parser = new CmisQlStrictParser(tokens);
        CommonTree parserTree; // the ANTLR tree after parsing phase

        query_return parsedStatement = parser.query();
        if (lexer.hasErrors()) {
            throw new CmisInvalidArgumentException(lexer.getErrorMessages());
        } else if (parser.hasErrors()) {
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
    
    /**
     * Parse and process a CMISQL statement using the higher level support classes
     * 
     * @param statement
     *      CMISQL statement
     * @param queryObj
     *      CMIS query object filled with information what data need to be retrieved
     * @param pw
     *      predicate walker that evaluates the where clause
     * @return
     *      AntLR tree grammar created by this statement
     *      
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws RecognitionException
     */
    public CmisQueryWalker traverseStatement(String statement, QueryObject queryObj, PredicateWalkerBase pw)
            throws UnsupportedEncodingException, IOException, RecognitionException {
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
            throw new CmisInvalidArgumentException("Walking of statement failed with RecognitionException error: \n   " + errorMsg, e);
        } catch (CmisBaseException e) {
            throw e;
        } catch (Exception e) {
            throw new CmisInvalidArgumentException("Walking of statement failed with exception: \n   ", e);
        }
    }

}
