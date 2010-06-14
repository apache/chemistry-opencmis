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
package org.apache.chemistry.opencmis.inmemory.query;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.definitions.PropertyBooleanDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.inmemory.types.InMemoryDocumentTypeDefinition;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.chemistry.opencmis.server.support.query.CMISQLParser;

public abstract class AbstractQueryTest {

    protected CommonTree parserTree; // the ANTLR tree after parsing phase
    protected CommonTree walkerTree; // the ANTLR tree after walking phase
    protected QueryObject queryObj;
    protected TypeDefinition myType, myTypeCopy, bookType;

    protected static final String MY_DOC_TYPE = "MyDocType";
    protected static final String MY_DOC_TYPE_COPY = "MyDocTypeCopy";
    protected static final String BOOL_PROP = "MyBooleanProp";
    protected static final String STRING_PROP = "MyStringProp";
    protected static final String INT_PROP = "MyIntegerProp";

    protected static final String BOOK_TYPE = "BookType";
    protected static final String TITLE_PROP = "Title";
    protected static final String AUTHOR_PROP = "Author";
    protected static final String ISBN_PROP = "ISBN";
    protected static final String PUB_DATE_PROP = "PublishingDate";
    
    protected void setUp(QueryObject qo) throws Exception {
            queryObj = qo;
    }

    protected void tearDown() throws Exception {
    }

    protected CmisQueryWalker getWalker(String statement) throws UnsupportedEncodingException, IOException, RecognitionException {
        CharStream input = new ANTLRInputStream(new ByteArrayInputStream(statement.getBytes("UTF-8")));
        TokenSource lexer = new CMISQLLexerStrict(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        CMISQLParserStrict parser = new CMISQLParserStrict(tokens);

        CMISQLParserStrict.query_return parsedStatement = parser.query();
        if (parser.errorMessage != null) {
            throw new RuntimeException("Cannot parse query: " + statement + " (" + parser.errorMessage + ")");
        }
        parserTree = (CommonTree) parsedStatement.getTree();            
        // printTree(tree);

        CommonTreeNodeStream nodes = new CommonTreeNodeStream(parserTree);
        nodes.setTokenStream(tokens);
        CmisQueryWalker walker = new CmisQueryWalker(nodes);
        return walker;
    }

    protected CmisQueryWalker traverseStatement(String statement) throws UnsupportedEncodingException, IOException, RecognitionException {
        CmisQueryWalker walker = getWalker(statement);
        walker.query(queryObj);
        String errMsg = walker.getErrorMessageString();
        if (null != errMsg) {
            fail("Walking of statement failed with error: \n   " + errMsg + 
                    "\n   Statement was: " + statement);
        }
        walkerTree = (CommonTree) walker.getTreeNodeStream().getTreeSource();
        return walker;
    }

    protected CmisQueryWalker traverseStatementAndCatchExc(String statement) {
        try {
            return traverseStatement(statement);
        } catch (RecognitionException e) {
            fail("Walking of statement failed with RecognitionException error: \n   " + e); 
            return null;
        } catch (Exception e) {
            fail("Walking of statement failed with other exception: \n   " + e); 
            return null;
        }
    }
    
    protected Tree getWhereTree(Tree root) {
        int count = root.getChildCount();
        for (int i=0; i<count; i++) {
            Tree child = root.getChild(i);
            if (child.getType() == CMISQLLexerStrict.WHERE) {
                return child;
            }
        }
        return null;
    }

    // Helper to create some types for testing

    protected  List<TypeDefinition> createTypes() {

        List<TypeDefinition> typeDefs = new ArrayList<TypeDefinition>();
        
        // First test type
        InMemoryDocumentTypeDefinition cmisType = new InMemoryDocumentTypeDefinition(MY_DOC_TYPE,
                "Document Type for Validation", InMemoryDocumentTypeDefinition.getRootDocumentType());

        Map<String, PropertyDefinition<?>> propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

        PropertyBooleanDefinition prop1 = PropertyCreationHelper.createBooleanDefinition(BOOL_PROP,
        "Sample Boolean Property");
        ((PropertyBooleanDefinitionImpl) prop1).setIsRequired(true);
        propertyDefinitions.put(prop1.getId(), prop1);

        PropertyStringDefinition prop2 = PropertyCreationHelper.createStringDefinition(STRING_PROP,
        "Sample String Property");
        propertyDefinitions.put(prop2.getId(), prop2);

        PropertyIntegerDefinitionImpl prop3 = PropertyCreationHelper.createIntegerDefinition(INT_PROP,
        "Sample Integer Property");
        propertyDefinitions.put(prop2.getId(), prop2);

        cmisType.setPropertyDefinitions(propertyDefinitions);

        typeDefs.add(cmisType);
        myType = cmisType;
        
        // add another type definition with exactly the same properties
        cmisType = new InMemoryDocumentTypeDefinition(MY_DOC_TYPE_COPY,
                "Document Type Duplicated", InMemoryDocumentTypeDefinition.getRootDocumentType());
        cmisType.setPropertyDefinitions(propertyDefinitions); // add same properties
        typeDefs.add(cmisType);
        myTypeCopy = cmisType;


        // Second test type

        cmisType = new InMemoryDocumentTypeDefinition(BOOK_TYPE,
                "Book Document Type", InMemoryDocumentTypeDefinition.getRootDocumentType());

        propertyDefinitions = new HashMap<String, PropertyDefinition<?>>();

        prop2 = PropertyCreationHelper.createStringDefinition(TITLE_PROP, "Title of Book");
        propertyDefinitions.put(prop2.getId(), prop2);

        prop2 = PropertyCreationHelper.createStringDefinition(AUTHOR_PROP, "Author of Book");
        propertyDefinitions.put(prop2.getId(), prop2);

        prop3 = PropertyCreationHelper.createIntegerDefinition(ISBN_PROP,
        "ISBN of Book");
        propertyDefinitions.put(prop3.getId(), prop3);

        PropertyDateTimeDefinitionImpl prop4 = PropertyCreationHelper.createDateTimeDefinition(PUB_DATE_PROP,
        "Publishing Date of Book");
        propertyDefinitions.put(prop4.getId(), prop4);

        cmisType.setPropertyDefinitions(propertyDefinitions);
        
        typeDefs.add(cmisType);
        bookType = cmisType;
        
        return typeDefs;
    }


}
