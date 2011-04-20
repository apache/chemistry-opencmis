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
 *
 * Contributors:
 *     Jens Huebel
 *     Florent Guillaume, Nuxeo
 */
package org.apache.chemistry.opencmis.inmemory.query;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Cardinality;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.PropertyType;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.DocumentVersion;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Filing;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.VersionedDocument;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.ObjectStoreImpl;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.chemistry.opencmis.server.support.TypeManager;
import org.apache.chemistry.opencmis.server.support.query.AbstractPredicateWalker;
import org.apache.chemistry.opencmis.server.support.query.CmisQueryWalker;
import org.apache.chemistry.opencmis.server.support.query.CmisSelector;
import org.apache.chemistry.opencmis.server.support.query.ColumnReference;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.chemistry.opencmis.server.support.query.QueryObject.SortSpec;
import org.apache.chemistry.opencmis.server.support.query.QueryUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A processor for a CMIS query for the In-Memory server. During tree traversal
 * conditions are checked against the data contained in the central hash map
 * with all objects. In a first pass one time setup is performed, in a custom
 * walk across the query expression tree an object is checked if it matches. In
 * case of a match it is appended to a list of matching objects.
 */
public class InMemoryQueryProcessor {

    private static final Log LOG = LogFactory.getLog(InMemoryQueryProcessor.class);

    private List<StoredObject> matches = new ArrayList<StoredObject>();
    private QueryObject queryObj;
    private Tree whereTree;

    public InMemoryQueryProcessor() {
    }

    /**
     * Main entry function to process a query from discovery service
     */
    public ObjectList query(TypeManager tm, ObjectStore objectStore, String user, String repositoryId,
            String statement, Boolean searchAllVersions, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount) {

        queryObj = new QueryObject(tm);
        processQueryAndCatchExc(statement); // calls query processor

        // iterate over all the objects and check for each if the query matches
        for (String objectId : ((ObjectStoreImpl) objectStore).getIds()) {
            StoredObject so = objectStore.getObjectById(objectId);
            match(so, searchAllVersions == null ? true : searchAllVersions.booleanValue());
        }

        ObjectList objList = buildResultList(tm, user, includeAllowableActions, includeRelationships, renditionFilter,
                maxItems, skipCount);
        LOG.debug("Query result, number of matching objects: " + objList.getNumItems());
        return objList;
    }

    public void processQueryAndCatchExc(String statement) {
        QueryUtil queryUtil = new QueryUtil();
        CmisQueryWalker walker = queryUtil.traverseStatementAndCatchExc(statement, queryObj, null);
        whereTree = walker.getWherePredicateTree();
    }

    public ObjectList buildResultList(TypeManager tm, String user, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, BigInteger maxItems, BigInteger skipCount) {

        sortMatches();

        ObjectListImpl res = new ObjectListImpl();
        res.setNumItems(BigInteger.valueOf(matches.size()));
        int start = 0;
        if (skipCount != null)
            start = (int) skipCount.longValue();
        if (start < 0)
            start = 0;
        if (start > matches.size())
            start = matches.size();
        int stop = 0;
        if (maxItems != null)
            stop = start + (int) maxItems.longValue();
        if (stop <= 0 || stop > matches.size())
            stop = matches.size();
        res.setHasMoreItems(stop < matches.size());
        if (start > 0 || stop > 0)
            matches = matches.subList(start, stop);
        List<ObjectData> objDataList = new ArrayList<ObjectData>();
        Map<String, String> props = queryObj.getRequestedProperties();
        Map<String, String> funcs = queryObj.getRequestedFuncs();
        for (StoredObject so : matches) {
            TypeDefinition td = tm.getTypeById(so.getTypeId()).getTypeDefinition();
            ObjectData od = PropertyCreationHelper.getObjectDataQueryResult(td, so, user, props, funcs,
                    includeAllowableActions, includeRelationships, renditionFilter);

            objDataList.add(od);
        }
        res.setObjects(objDataList);
        return res;
    }

    private boolean typeMatches(TypeDefinition td, StoredObject so) {
        String typeId = so.getTypeId();
        while (typeId != null) {
            if (typeId.equals(td.getId())) {
                return true;
            }
            // check parent type
            TypeDefinition parentTD = queryObj.getParentType(typeId);
            typeId = parentTD == null ? null : parentTD.getId();
        }
        return false;
    }

    private void sortMatches() {
        final List<SortSpec> orderBy = queryObj.getOrderBys();
        if (orderBy.size() > 1)
            LOG.warn("ORDER BY has more than one sort criterium, all but the first are ignored.");
        class ResultComparator implements Comparator<StoredObject> {

            @SuppressWarnings("unchecked")
            public int compare(StoredObject so1, StoredObject so2) {
                SortSpec s = orderBy.get(0);
                CmisSelector sel = s.getSelector();
                int result;

                if (sel instanceof ColumnReference) {
                    String propId = ((ColumnReference) sel).getPropertyId();
                    Object propVal1 = so1.getProperties().get(propId).getFirstValue();
                    Object propVal2 = so2.getProperties().get(propId).getFirstValue();
                    if (propVal1 == null && propVal2 == null)
                        result = 0;
                    else if (propVal1 == null)
                        result = -1;
                    else if (propVal2 == null)
                        result = 1;
                    else
                        result = ((Comparable<Object>) propVal1).compareTo(propVal2);
                } else {
                    // String funcName = ((FunctionReference) sel).getName();
                    // evaluate function here, currently ignore
                    result = 0;
                }
                if (!s.isAscending())
                    result = -result;
                return result;
            }
        }

        if (orderBy.size() > 0)
            Collections.sort(matches, new ResultComparator());

    }

    /**
     * Check for each object contained in the in-memory repository if it matches
     * the current query expression. If yes add it to the list of matched
     * objects.
     * 
     * @param so
     *            object stored in the in-memory repository
     */
    private void match(StoredObject so, boolean searchAllVersions) {
        // log.debug("checkMatch() for object: " + so.getId());
        // first check if type is matching...
        String queryName = queryObj.getTypes().values().iterator().next(); // as
                                                                           // we
                                                                           // don't
                                                                           // support
                                                                           // JOINS
                                                                           // take
                                                                           // first
                                                                           // type
        TypeDefinition td = queryObj.getTypeDefinitionFromQueryName(queryName);
        boolean skip = so instanceof VersionedDocument; // we are only
                                                        // interested in
                                                        // versions not in the
                                                        // series
        boolean typeMatches = typeMatches(td, so);
        if (!searchAllVersions && so instanceof DocumentVersion
                && ((DocumentVersion) so).getParentDocument().getLatestVersion(false) != so)
            skip = true;
        // ... then check expression...
        if (typeMatches && !skip)
            evalWhereTree(whereTree, so);
    }

    private void evalWhereTree(Tree node, StoredObject so) {
        boolean match = true;
        if (null != node)
            match = evalWhereNode(so, node);
        if (match)
            matches.add(so); // add to list
    }

    /**
     * For each object check if it matches and append it to match-list if it
     * does. We do here our own walking mechanism so that we can pass additional
     * parameters and define the return types.
     * 
     * @param node
     *            node in where clause
     * @return true if it matches, false if it not matches
     */
    boolean evalWhereNode(StoredObject so, Tree node) {
        return new InMemoryWhereClauseWalker(so).walkPredicate(node);
    }

    public class InMemoryWhereClauseWalker extends AbstractPredicateWalker {

        protected final StoredObject so;

        public InMemoryWhereClauseWalker(StoredObject so) {
            this.so = so;
        }

        @Override
        public Boolean walkNot(Tree opNode, Tree node) {
            boolean matches = walkPredicate(node);
            return !matches;
        }

        @Override
        public Boolean walkAnd(Tree opNode, Tree leftNode, Tree rightNode) {
            boolean matches1 = walkPredicate(leftNode);
            boolean matches2 = walkPredicate(rightNode);
            return matches1 && matches2;
        }

        @Override
        public Boolean walkOr(Tree opNode, Tree leftNode, Tree rightNode) {
            boolean matches1 = walkPredicate(leftNode);
            boolean matches2 = walkPredicate(rightNode);
            return matches1 || matches2;
        }

        @Override
        public Boolean walkEquals(Tree opNode, Tree leftNode, Tree rightNode) {
            Integer cmp = compareTo(leftNode, rightNode);
            return cmp == null ? false : cmp == 0;
        }

        @Override
        public Boolean walkNotEquals(Tree opNode, Tree leftNode, Tree rightNode) {
            Integer cmp = compareTo(leftNode, rightNode);
            return cmp == null ? false : cmp != 0;
        }

        @Override
        public Boolean walkGreaterThan(Tree opNode, Tree leftNode, Tree rightNode) {
            Integer cmp = compareTo(leftNode, rightNode);
            return cmp == null ? false : cmp > 0;
        }

        @Override
        public Boolean walkGreaterOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
            Integer cmp = compareTo(leftNode, rightNode);
            return cmp == null ? false : cmp >= 0;
        }

        @Override
        public Boolean walkLessThan(Tree opNode, Tree leftNode, Tree rightNode) {
            Integer cmp = compareTo(leftNode, rightNode);
            return cmp == null ? false : cmp < 0;
        }

        @Override
        public Boolean walkLessOrEquals(Tree opNode, Tree leftNode, Tree rightNode) {
            Integer cmp = compareTo(leftNode, rightNode);
            return cmp == null ? false : cmp <= 0;
        }

        @Override
        public Boolean walkIn(Tree opNode, Tree colNode, Tree listNode) {
            ColumnReference colRef = getColumnReference(colNode);
            PropertyDefinition<?> pd = colRef.getPropertyDefinition();
            PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
            List<Object> literals = onLiteralList(listNode);
            if (pd.getCardinality() != Cardinality.SINGLE)
                throw new IllegalStateException("Operator IN only is allowed on single-value properties ");
            else if (lVal == null)
                return false;
            else {
                Object prop = lVal.getFirstValue();
                return literals.contains(prop);
            }
        }

        @Override
        public Boolean walkNotIn(Tree opNode, Tree colNode, Tree listNode) {
            // Note just return !walkIn(node, colNode, listNode) is wrong,
            // because
            // then it evaluates to true for null values (not set properties).
            ColumnReference colRef = getColumnReference(colNode);
            PropertyDefinition<?> pd = colRef.getPropertyDefinition();
            PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
            List<Object> literals = onLiteralList(listNode);
            if (pd.getCardinality() != Cardinality.SINGLE)
                throw new IllegalStateException("Operator IN only is allowed on single-value properties ");
            else if (lVal == null)
                return false;
            else {
                Object prop = lVal.getFirstValue();
                return !literals.contains(prop);
            }
        }

        @Override
        public Boolean walkInAny(Tree opNode, Tree colNode, Tree listNode) {
            ColumnReference colRef = getColumnReference(colNode);
            PropertyDefinition<?> pd = colRef.getPropertyDefinition();
            PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
            List<Object> literals = onLiteralList(listNode);
            if (pd.getCardinality() != Cardinality.MULTI)
                throw new IllegalStateException("Operator ANY...IN only is allowed on multi-value properties ");
            else if (lVal == null)
                return false;
            else {
                List<?> props = lVal.getValues();
                for (Object prop : props) {
                    LOG.debug("comparing with: " + prop);
                    if (literals.contains(prop))
                        return true;
                }
                return false;
            }
        }

        @Override
        public Boolean walkNotInAny(Tree opNode, Tree colNode, Tree listNode) {
            // Note just return !walkNotInAny(node, colNode, listNode) is
            // wrong, because
            // then it evaluates to true for null values (not set properties).
            ColumnReference colRef = getColumnReference(colNode);
            PropertyDefinition<?> pd = colRef.getPropertyDefinition();
            PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
            List<Object> literals = onLiteralList(listNode);
            if (pd.getCardinality() != Cardinality.MULTI)
                throw new IllegalStateException("Operator ANY...IN only is allowed on multi-value properties ");
            else if (lVal == null)
                return false;
            else {
                List<?> props = lVal.getValues();
                for (Object prop : props) {
                    LOG.debug("comparing with: " + prop);
                    if (literals.contains(prop))
                        return false;
                }
                return true;
            }
        }

        @Override
        public Boolean walkEqAny(Tree opNode, Tree literalNode, Tree colNode) {
            ColumnReference colRef = getColumnReference(colNode);
            PropertyDefinition<?> pd = colRef.getPropertyDefinition();
            PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
            Object literal = walkExpr(literalNode);
            if (pd.getCardinality() != Cardinality.MULTI)
                throw new IllegalStateException("Operator = ANY only is allowed on multi-value properties ");
            else if (lVal == null)
                return false;
            else {
                List<?> props = lVal.getValues();
                return props.contains(literal);
            }
        }

        @Override
        public Boolean walkIsNull(Tree opNode, Tree colNode) {
            Object propVal = getPropertyValue(colNode, so);
            return propVal == null;
        }

        @Override
        public Boolean walkIsNotNull(Tree opNode, Tree colNode) {
            Object propVal = getPropertyValue(colNode, so);
            return propVal != null;
        }

        @Override
        public Boolean walkLike(Tree opNode, Tree colNode, Tree stringNode) {
            Object rVal = walkExpr(stringNode);
            if (!(rVal instanceof String))
                throw new IllegalStateException("LIKE operator requires String literal on right hand side.");

            ColumnReference colRef = getColumnReference(colNode);
            PropertyDefinition<?> pd = colRef.getPropertyDefinition();
            PropertyType propType = pd.getPropertyType();
            if (propType != PropertyType.STRING && propType != PropertyType.HTML && propType != PropertyType.ID
                    && propType != PropertyType.URI)
                throw new IllegalStateException("Property type " + propType.value() + " is not allowed FOR LIKE");
            if (pd.getCardinality() != Cardinality.SINGLE)
                throw new IllegalStateException("LIKE is not allowed for multi-value properties ");

            String propVal = (String) so.getProperties().get(colRef.getPropertyId()).getFirstValue();
            String pattern = translatePattern((String) rVal); // SQL to Java
                                                              // regex
                                                              // syntax
            Pattern p = Pattern.compile(pattern);
            return p.matcher(propVal).matches();
        }

        @Override
        public Boolean walkNotLike(Tree opNode, Tree colNode, Tree stringNode) {
            return !walkLike(opNode, colNode, stringNode);
        }

        @Override
        public Boolean walkContains(Tree qualNode, Tree colNode, Tree queryNode) {
            throw new IllegalStateException("Operator CONTAINS not supported in InMemory server.");
        }

        @Override
        public Boolean walkInFolder(Tree opNode, Tree qualNode, Tree paramNode) {
            if (null != qualNode) {
                getTableReference(qualNode);
                // just for error checking we do not evaluate this, there is
                // only one from without join support
            }
            Object lit = walkExpr(paramNode);
            if (!(lit instanceof String))
                throw new IllegalStateException("Folder id in IN_FOLDER must be of type String");
            String folderId = (String) lit;

            // check if object is in folder
            if (so instanceof Filing)
                return hasParent((Filing) so, folderId);
            else
                return false;
        }

        @Override
        public Boolean walkInTree(Tree opNode, Tree qualNode, Tree paramNode) {
            if (null != qualNode) {
                getTableReference(qualNode);
                // just for error checking we do not evaluate this, there is
                // only one from without join support
            }
            Object lit = walkExpr(paramNode);
            if (!(lit instanceof String))
                throw new IllegalStateException("Folder id in IN_FOLDER must be of type String");
            String folderId = (String) lit;

            // check if object is in folder
            if (so instanceof Filing)
                return hasAncestor((Filing) so, folderId);
            else
                return false;
        }

        protected Integer compareTo(Tree leftChild, Tree rightChild) {
            Object rVal = walkExpr(rightChild);

            // log.debug("retrieve node from where: " +
            // System.identityHashCode(leftChild) + " is " + leftChild);
            ColumnReference colRef = getColumnReference(leftChild);
            PropertyDefinition<?> pd = colRef.getPropertyDefinition();
            PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
            if (lVal instanceof List<?>)
                throw new IllegalStateException("You can't query operators <, <=, ==, !=, >=, > on multi-value properties ");
            else
                return InMemoryQueryProcessor.this.compareTo(pd, lVal, rVal);
        }

        @SuppressWarnings("unchecked")
        public List<Object> onLiteralList(Tree node) {
            return (List<Object>) walkExpr(node);
        }
    }

    private boolean hasParent(Filing objInFolder, String folderId) {
        List<Folder> parents = objInFolder.getParents();

        for (Folder folder : parents)
            if (folderId.equals(folder.getId()))
                return true;
        return false;
    }

    private boolean hasAncestor(Filing objInFolder, String folderId) {
        List<Folder> parents = objInFolder.getParents();

        for (Folder folder : parents)
            if (folderId.equals(folder.getId()))
                return true;
        for (Folder folder : parents)
            if (hasAncestor(folder, folderId))
                return true;
        return false;
    }

    protected int compareTo(PropertyDefinition<?> td, PropertyData<?> lVal, Object rVal) {
        Object lValue = lVal.getFirstValue();
        switch (td.getPropertyType()) {
        case BOOLEAN:
            if (rVal instanceof Boolean)
                return ((Boolean) lValue).compareTo((Boolean) rVal);
            else
                throwIncompatibleTypesException(lValue, rVal);
            break;
        case INTEGER: {
            Long lLongValue = ((BigInteger) lVal.getFirstValue()).longValue();
            if (rVal instanceof Long)
                return (lLongValue).compareTo((Long) rVal);
            else if (rVal instanceof Double)
                return Double.valueOf(((Integer) lValue).doubleValue()).compareTo((Double) rVal);
            else
                throwIncompatibleTypesException(lValue, rVal);
            break;
        }
        case DATETIME:
            if (rVal instanceof GregorianCalendar) {
                // LOG.debug("left:" +
                // CalendarHelper.toString((GregorianCalendar)lValue) +
                // " right: " +
                // CalendarHelper.toString((GregorianCalendar)rVal));
                return ((GregorianCalendar) lValue).compareTo((GregorianCalendar) rVal);
            } else
                throwIncompatibleTypesException(lValue, rVal);
            break;
        case DECIMAL: {
            Double lDoubleValue = ((BigDecimal) lVal.getFirstValue()).doubleValue();
            if (rVal instanceof Double)
                return lDoubleValue.compareTo((Double) rVal);
            else if (rVal instanceof Long)
                return Double.valueOf(((Integer) lValue).doubleValue()).compareTo(((Long)rVal).doubleValue());
            else
                throwIncompatibleTypesException(lValue, rVal);
            break;
        }
        case HTML:
        case STRING:
        case URI:
        case ID:
            if (rVal instanceof String) {
                LOG.debug("compare strings: " + lValue + " with " + rVal);
                return ((String) lValue).compareTo((String) rVal);
            } else
                throwIncompatibleTypesException(lValue, rVal);
            break;
        }
        return 0;
    }

    private ColumnReference getColumnReference(Tree columnNode) {
        CmisSelector sel = queryObj.getColumnReference(columnNode.getTokenStartIndex());
        if (null == sel)
            throw new IllegalStateException("Unknown property query name " + columnNode.getChild(0));
        else if (sel instanceof ColumnReference)
            return (ColumnReference) sel;
        else
            throw new IllegalStateException("Unexpected numerical value function in where clause");
    }

    private String getTableReference(Tree tableNode) {
        String typeQueryName = queryObj.getTypeQueryName(tableNode.getText());
        if (null == typeQueryName)
            throw new IllegalStateException("Inavlid type in IN_FOLDER() or IN_TREE(), must be in FROM list: "
                    + tableNode.getText());
        return typeQueryName;
    }

    private Object getPropertyValue(Tree columnNode, StoredObject so) {
        ColumnReference colRef = getColumnReference(columnNode);
        PropertyDefinition<?> pd = colRef.getPropertyDefinition();
        PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
        if (null == lVal)
            return null;
        else {
            if (pd.getCardinality() == Cardinality.SINGLE)
                return lVal.getFirstValue();
            else
                return lVal.getValues();
        }
    }

    // translate SQL wildcards %, _ to Java regex syntax
    public static String translatePattern(String wildcardString) {
        int index = 0;
        int start = 0;
        StringBuffer res = new StringBuffer();

        while (index >= 0) {
            index = wildcardString.indexOf('%', start);
            if (index < 0)
                res.append(wildcardString.substring(start));
            else if (index == 0 || index > 0 && wildcardString.charAt(index - 1) != '\\') {
                res.append(wildcardString.substring(start, index));
                res.append(".*");
            } else
                res.append(wildcardString.substring(start, index + 1));
            start = index + 1;
        }
        wildcardString = res.toString();

        index = 0;
        start = 0;
        res = new StringBuffer();

        while (index >= 0) {
            index = wildcardString.indexOf('_', start);
            if (index < 0)
                res.append(wildcardString.substring(start));
            else if (index == 0 || index > 0 && wildcardString.charAt(index - 1) != '\\') {
                res.append(wildcardString.substring(start, index));
                res.append(".");
            } else
                res.append(wildcardString.substring(start, index + 1));
            start = index + 1;
        }
        return res.toString();
    }

    private void throwIncompatibleTypesException(Object o1, Object o2) {
        throw new IllegalArgumentException("Incompatible Types to compare: " + o1 + " and " + o2);
    }

}
