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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.apache.chemistry.opencmis.commons.definitions.MutableTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyBooleanDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyStringDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeDefinitionImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerDefinitionImpl;
import org.apache.chemistry.opencmis.inmemory.types.DocumentTypeCreationHelper;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory;
import org.apache.chemistry.opencmis.server.support.TypeManager;
import org.apache.chemistry.opencmis.server.support.query.CmisQueryWalker;
import org.apache.chemistry.opencmis.server.support.query.PredicateWalkerBase;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.chemistry.opencmis.server.support.query.QueryUtilStrict;

public abstract class AbstractQueryTest {

    protected PredicateWalkerBase predicateWalker;
    protected TypeManager typeManager;
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

    protected void setUp(TypeManager tm, PredicateWalkerBase pw) {
        typeManager = tm;
        predicateWalker = pw;
    }

    protected QueryUtilStrict traverseStatement(String statement) throws UnsupportedEncodingException, IOException,
            RecognitionException {
        QueryUtilStrict queryUtil = new QueryUtilStrict(statement, typeManager, predicateWalker);
        queryUtil.processStatement();
        return queryUtil;
    }

    protected QueryUtilStrict traverseStatementAndCatchExc(String statement) {
        QueryUtilStrict queryUtil = new QueryUtilStrict(statement, typeManager, predicateWalker);
        queryUtil.processStatementUsingCmisExceptions();
        return queryUtil;
    }

    protected CmisQueryWalker getWalker(String statement) throws RecognitionException {
        QueryUtilStrict queryUtil = new QueryUtilStrict(statement, typeManager, predicateWalker);
        queryUtil.processStatementUsingCmisExceptions();
        queryObj = queryUtil.getQueryObject();
        return queryUtil.getWalker();
    }

    protected CmisQueryWalker getWalker(String statement, QueryObject.ParserMode mode) throws RecognitionException {
        QueryUtilStrict queryUtil = new QueryUtilStrict(statement, typeManager, predicateWalker, true, mode);
        queryUtil.processStatementUsingCmisExceptions();
        queryObj = queryUtil.getQueryObject();
        return queryUtil.getWalker();
    }

    // Helper to create some types for testing

    protected List<TypeDefinition> createTypes() {
        TypeDefinitionFactory typeFactory = DocumentTypeCreationHelper.getTypeDefinitionFactory();
        List<TypeDefinition> typeDefs = new ArrayList<TypeDefinition>();

        try {
            // First test type
            MutableTypeDefinition cmisType;
            cmisType = typeFactory.createChildTypeDefinition(DocumentTypeCreationHelper.getCmisDocumentType(),
                    MY_DOC_TYPE);
            cmisType.setId(MY_DOC_TYPE);
            cmisType.setDisplayName("Document Type for Validation");

            PropertyBooleanDefinition prop1 = PropertyCreationHelper.createBooleanDefinition(BOOL_PROP,
                    "Sample Boolean Property", Updatability.READWRITE);
            ((PropertyBooleanDefinitionImpl) prop1).setIsRequired(true);

            PropertyStringDefinition prop2 = PropertyCreationHelper.createStringDefinition(STRING_PROP,
                    "Sample String Property", Updatability.READWRITE);

            PropertyIntegerDefinitionImpl prop3 = PropertyCreationHelper.createIntegerDefinition(INT_PROP,
                    "Sample Integer Property", Updatability.READWRITE);

            cmisType.addPropertyDefinition(prop1);
            cmisType.addPropertyDefinition(prop2);
            cmisType.addPropertyDefinition(prop3);

            typeDefs.add(cmisType);
            myType = cmisType;

            // add another type definition with exactly the same properties
            cmisType = typeFactory.createChildTypeDefinition(DocumentTypeCreationHelper.getCmisDocumentType(),
                    MY_DOC_TYPE_COPY);
            cmisType.setDisplayName("Document Type for Duplicated");

            // add same properties
            cmisType.addPropertyDefinition(prop1);
            cmisType.addPropertyDefinition(prop2);
            cmisType.addPropertyDefinition(prop3);
            typeDefs.add(cmisType);
            myTypeCopy = cmisType;

            // Second test type

            cmisType = typeFactory.createChildTypeDefinition(DocumentTypeCreationHelper.getCmisDocumentType(),
                    BOOK_TYPE);
            cmisType.setDisplayName("Book Document Type");

            prop2 = PropertyCreationHelper.createStringDefinition(TITLE_PROP, "Title of Book", Updatability.READWRITE);
            cmisType.addPropertyDefinition(prop2);

            prop2 = PropertyCreationHelper
                    .createStringDefinition(AUTHOR_PROP, "Author of Book", Updatability.READWRITE);
            cmisType.addPropertyDefinition(prop2);

            prop3 = PropertyCreationHelper.createIntegerDefinition(ISBN_PROP, "ISBN of Book", Updatability.READWRITE);
            cmisType.addPropertyDefinition(prop3);

            PropertyDateTimeDefinitionImpl prop4 = PropertyCreationHelper.createDateTimeDefinition(PUB_DATE_PROP,
                    "Publishing Date of Book", Updatability.READWRITE);
            cmisType.addPropertyDefinition(prop4);

            typeDefs.add(cmisType);
            bookType = cmisType;

            return typeDefs;
        } catch (Exception e) {
            throw new CmisRuntimeException("Error when creating built-in InMemory types.", e);
        }
    }

}
