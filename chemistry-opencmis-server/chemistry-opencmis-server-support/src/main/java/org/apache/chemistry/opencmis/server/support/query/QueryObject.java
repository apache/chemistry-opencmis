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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.tree.Tree;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.server.support.TypeManager;
import org.apache.chemistry.opencmis.server.support.TypeValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * QueryObject is a class used to encapsulate a CMIS query. It is created from
 * an ANTLR parser on an incoming query string. During parsing various
 * informations are collected and stored in objects suitable for evaluating the
 * query (like selected properties, effected types and order statements. A query
 * evaluator can use this information to perform the query and build the result.
 */
public class QueryObject {

    private static Log LOG = LogFactory.getLog(QueryObject.class);

    // For error handling see:
    // http://www.antlr.org/pipermail/antlr-interest/2008-April/027600.html
    // select part
    protected TypeManager typeMgr;
    protected List<CmisSelector> selectReferences = new ArrayList<CmisSelector>();
    protected List<CmisSelector> whereReferences = new ArrayList<CmisSelector>();
    protected List<CmisSelector> joinReferences = new ArrayList<CmisSelector>();
    // --> Join not implemented yet
    protected Map<String, CmisSelector> colOrFuncAlias = new HashMap<String, CmisSelector>();

    // from part
    /** map from alias name to type query name */
    protected Map<String, String> froms = new LinkedHashMap<String, String>();

    /** main from alias name */
    protected String from = null;

    protected List<JoinSpec> joinSpecs = new LinkedList<JoinSpec>();

    // where part
    protected Map<Integer, CmisSelector> columnReferences = new HashMap<Integer, CmisSelector>();

    // order by part
    protected List<SortSpec> sortSpecs = new ArrayList<SortSpec>();

    public static class JoinSpec {

        /** INNER / LEFT / RIGHT */
        public String kind;

        /** Alias or full table type */
        public String alias;

        public ColumnReference onLeft;

        public ColumnReference onRight;

        public JoinSpec(String kind, String alias) {
            this.kind = kind;
            this.alias = alias;
        }

        public void setSelectors(ColumnReference onLeft, ColumnReference onRight) {
            this.onLeft = onLeft;
            this.onRight = onRight;
        }

        @Override
        public String toString() {
            return "JoinReference(" + kind + "," + alias + "," + onLeft + ","
                    + onRight + ")";
        }
    }

    public class SortSpec {
        public boolean ascending;
        public Integer colRefKey; // key in columnReferencesMap point to column
                                    // descriptions

        public SortSpec(Integer key, boolean ascending) {
            this.colRefKey = key;
            this.ascending = ascending;
        }

        public CmisSelector getSelector() {
            return columnReferences.get(colRefKey);
        }

        public boolean isAscending() {
            return ascending;
        }
    };

    public QueryObject() {
    }

    public QueryObject(TypeManager tm) {
        typeMgr = tm;
    }

    public Map<Integer, CmisSelector> getColumnReferences() {
        return Collections.unmodifiableMap(columnReferences);
    }

    public CmisSelector getColumnReference(Integer token) {
        return columnReferences.get(token);
    }

    // ///////////////////////////////////////////////////////
    // SELECT part

    // public accessor methods
    public List<CmisSelector> getSelectReferences() {
        return selectReferences;
    }

    public void addSelectReference(Tree node, CmisSelector selRef) {
        selectReferences.add(selRef);
        columnReferences.put(node.getTokenStartIndex(), selRef);
    }

    public void addAlias(String aliasName, CmisSelector aliasRef) {
        LOG.debug("add alias: " + aliasName + " for: " + aliasRef);
        if (colOrFuncAlias.containsKey(aliasName)) {
            throw new CmisInvalidArgumentException("You cannot use name " + aliasName
                    + " more than once as alias in a select.");
        } else {
            aliasRef.setAliasName(aliasName);
            colOrFuncAlias.put(aliasName, aliasRef);
        }
    }

    public CmisSelector getSelectAlias(String aliasName) {
        return colOrFuncAlias.get(aliasName);
    }

    // ///////////////////////////////////////////////////////
    // FROM part

    public String addType(String aliasName, String typeQueryName) {
        LOG.debug("add alias: " + aliasName + " for: " + typeQueryName);
        if (froms.containsKey(aliasName)) {
            throw new CmisInvalidArgumentException("You cannot use name " + aliasName
                    + " more than once as alias in a from part.");
        }
        if (aliasName == null) {
            aliasName = typeQueryName;
        }
        froms.put(aliasName, typeQueryName);
        if (from == null) {
            from = aliasName;
        }
        return aliasName;
    }

    public String getMainTypeAlias() {
        return from;
    }

    public Map<String, String> getTypes() {
        return Collections.unmodifiableMap(froms);
    }

    public String getTypeQueryName(String alias) {
        return froms.get(alias);
    }

    public TypeDefinition getTypeDefinitionFromQueryName(String queryName) {
        return typeMgr.getTypeByQueryName(queryName);
    }

    public TypeDefinition getParentType(TypeDefinition td) {
        String parentType = td.getParentTypeId();
        return parentType == null ? null : typeMgr.getTypeById(parentType).getTypeDefinition();
    }

    public TypeDefinition getParentType(String typeId) {
        TypeDefinition td = typeMgr.getTypeById(typeId).getTypeDefinition();
        String parentType = td == null ? null : td.getParentTypeId();
        return parentType == null ? null : typeMgr.getTypeById(parentType).getTypeDefinition();
    }

    public TypeDefinition getMainFromName() {
        // as we don't support JOINS take first type
        String queryName = froms.values().iterator().next();
        TypeDefinition td = getTypeDefinitionFromQueryName(queryName);
        return td;
    }

    /**
     * return a map of all columns that have been requested in the SELECT part
     * of the statement.
     *
     * @return a map with a String as a key and value. key is the query name of
     *         the property, value is the alias if an alias was given or the
     *         query name otherwise.
     */
    public Map<String, String> getRequestedProperties() {
        Map<String, String> res = new HashMap<String, String>();
        for (CmisSelector sel : selectReferences) {
            if (sel instanceof ColumnReference) {
                ColumnReference colRef = (ColumnReference) sel;
                String key = colRef.getPropertyId();
                if (null == key)
                    key = colRef.getPropertyQueryName(); // happens for *
                String propDescr = colRef.getAliasName() == null ? colRef.getPropertyQueryName() : colRef
                        .getAliasName();
                res.put(key, propDescr);
            }
        }
        return res;
    }

    /**
     * return a map of all functions that have been requested in the SELECT part
     * of the statement.
     *
     * @return a map with a String as a key and value. key is the function name
     *         of the property, value is the alias if an alias was given or the
     *         function name otherwise.
     */
    public Map<String, String> getRequestedFuncs() {
        Map<String, String> res = new HashMap<String, String>();
        for (CmisSelector sel : selectReferences) {
            if (sel instanceof FunctionReference) {
                FunctionReference funcRef = (FunctionReference) sel;
                String propDescr = funcRef.getAliasName() == null ? funcRef.getName() : funcRef.getAliasName();
                res.put(funcRef.getName(), propDescr);
            }
        }
        return res;
    }

    // ///////////////////////////////////////////////////////
    // JOINS

    public void addJoinReference(Tree node, CmisSelector reference) {
        columnReferences.put(node.getTokenStartIndex(), reference);
        joinReferences.add(reference);
    }

    public List<CmisSelector> getJoinReferences() {
        return Collections.unmodifiableList(joinReferences);
    }

    public void addJoin(String kind, String alias, boolean hasSpec) {
        JoinSpec join = new JoinSpec(kind, alias);
        if (hasSpec) {
            // get columns from last added references
            int n = joinReferences.size();
            ColumnReference onLeft = (ColumnReference) joinReferences.get(n - 2);
            ColumnReference onRight = (ColumnReference) joinReferences.get(n - 1);
            join.setSelectors(onLeft, onRight);
        }
        joinSpecs.add(join);
    }

    public List<JoinSpec> getJoins() {
        return joinSpecs;
    }

    // ///////////////////////////////////////////////////////
    // WHERE part


    public void addWhereReference(Tree node, CmisSelector reference) {
        LOG.debug("add node to where: " + System.identityHashCode(node));

        columnReferences.put(node.getTokenStartIndex(), reference);
        whereReferences.add(reference);
    }

    public List<CmisSelector> getWhereReferences() {
        return Collections.unmodifiableList(whereReferences);
    }

    // ///////////////////////////////////////////////////////
    // ORDER_BY part

    public List<SortSpec> getOrderBys() {
        return Collections.unmodifiableList(sortSpecs);
    }

    public void addSortCriterium(Tree node, ColumnReference colRef, boolean ascending) {
        LOG.debug("addSortCriterium: " + colRef + " ascending: " + ascending);
        columnReferences.put(node.getTokenStartIndex(), colRef);
        sortSpecs.add(new SortSpec(node.getTokenStartIndex(), ascending));
    }

    // ///////////////////////////////////////////////////////
    // resolve types after first pass traversing the AST is complete

    public void resolveTypes() {
        LOG.debug("First pass of query traversal is complete, resolving types");
        if (null == typeMgr)
            return;

        // First resolve all alias names defined in SELECT:
        for (CmisSelector alias : colOrFuncAlias.values()) {
            if (alias instanceof ColumnReference) {
                ColumnReference colRef = ((ColumnReference) alias);
                resolveTypeForAlias(colRef);
            }
        }

        // Then replace all aliases used somewhere by their resolved column
        // reference:
        for (Integer obj : columnReferences.keySet()) {
            CmisSelector selector = columnReferences.get(obj);
            String key = selector.getName();
            if (colOrFuncAlias.containsKey(key)) { // it is an alias
                CmisSelector resolvedReference = colOrFuncAlias.get(key);
                columnReferences.put(obj, resolvedReference);
                // Note: ^ This may replace the value in the map with the same
                // value, but this does not harm.
                // Otherwise we need to check if it is resolved or not which
                // causes two more ifs:
                // if (selector instanceof ColumnReference) {
                // ColumnReference colRef = ((ColumnReference) selector);
                // if (colRef.getTypeDefinition() == null) // it is not yet
                // resolved
                // // replace unresolved column reference by resolved on from
                // alias map
                // columnReferences.put(obj,
                // colOrFuncAlias.get(selector.getAliasName()));
                // } else
                // columnReferences.put(obj,
                // colOrFuncAlias.get(selector.getAliasName()));
                if (whereReferences.remove(selector))
                    // replace unresolved by resolved reference
                    whereReferences.add(resolvedReference);
                if (joinReferences.remove(selector))
                    // replace unresolved by resolved reference
                    joinReferences.add(resolvedReference);
            }
        }

        // The replace all remaining column references not using an alias
        for (CmisSelector select : columnReferences.values()) {
            // ignore functions here
            if (select instanceof ColumnReference) {
                ColumnReference colRef = ((ColumnReference) select);
                if (colRef.getTypeDefinition() == null) { // not yet resolved
                    if (colRef.getTypeQueryName() == null) {
                        // unqualified select: SELECT p FROM
                        resolveTypeForColumnReference(colRef);
                    } else {
                        // qualified select: SELECT t.p FROM
                        validateColumnReferenceAndResolveType(colRef);
                    }
                }
            }
        }
    }

    protected void resolveTypeForAlias(ColumnReference colRef) {
        String aliasName = colRef.getAliasName();

        if (colOrFuncAlias.containsKey(aliasName)) {
            CmisSelector selector = colOrFuncAlias.get(aliasName);
            if (selector instanceof ColumnReference) {
                colRef = (ColumnReference) selector; // alias target
                if (colRef.getTypeQueryName() == null) {
                    // unqualified select: SELECT p FROM
                    resolveTypeForColumnReference(colRef);
                } else {
                    // qualified select: SELECT t.p FROM
                    validateColumnReferenceAndResolveType(colRef);
                }
            }
            // else --> ignore FunctionReference
        }
    }

    // for a select x from y, z ... find the type in type manager for x
    protected void resolveTypeForColumnReference(ColumnReference colRef) {
        String propName = colRef.getPropertyQueryName();
        boolean isStar = propName.equals("*");

        // it is property query name without a type, so find type
        int noFound = 0;
        TypeDefinition tdFound = null;
        for (String typeQueryName : froms.values()) {
            TypeDefinition td = typeMgr.getTypeByQueryName(typeQueryName);
            if (null == td)
                throw new CmisInvalidArgumentException(typeQueryName + " is neither a type query name nor an alias.");
            else if (isStar) {
                ++noFound;
                tdFound = null;
            } else if (TypeValidator.typeContainsPropertyWithQueryName(td, propName)) {
                ++noFound;
                tdFound = td;
            }
        }
        if (noFound == 0)
            throw new CmisInvalidArgumentException(propName
                    + " is not a property query name in any of the types in from ...");
        else if (noFound > 1 && !isStar)
            throw new CmisInvalidArgumentException(propName
                    + " is not a unique property query name within the types in from ...");
        else {
            if (null != tdFound) // can be null in select * from t1 JOIN t2....
                validateColumnReferenceAndResolveType(tdFound, colRef);
        }
    }

    // for a select x.y from x ... check that x has property y and that x is in
    // from
    protected void validateColumnReferenceAndResolveType(ColumnReference colRef) {
        // either same name or mapped alias
        String typeQueryName = getReferencedTypeQueryName(colRef.getTypeQueryName());
        TypeDefinition td = typeMgr.getTypeByQueryName(typeQueryName);
        if (null == td)
            throw new CmisInvalidArgumentException(colRef.getTypeQueryName()
                    + " is neither a type query name nor an alias.");

        validateColumnReferenceAndResolveType(td, colRef);
    }

    protected void validateColumnReferenceAndResolveType(TypeDefinition td, ColumnReference colRef) {

        // type found, check if property exists
        boolean hasProp;
        if (colRef.getPropertyQueryName().equals("*"))
            hasProp = true;
        else
            hasProp = TypeValidator.typeContainsPropertyWithQueryName(td, colRef.getPropertyQueryName());
        if (!hasProp)
            throw new CmisInvalidArgumentException(colRef.getPropertyQueryName()
                    + " is not a valid property query name in type " + td.getId() + ".");

        colRef.setTypeDefinition(typeMgr.getPropertyIdForQueryName(td, colRef.getPropertyQueryName()), td);
    }

    // return type query name for a referenced column (which can be the name
    // itself or an alias
    protected String getReferencedTypeQueryName(String typeQueryNameOrAlias) {
        String typeQueryName = froms.get(typeQueryNameOrAlias);
        if (null == typeQueryName) {
            // if an alias was defined but still the original is used we have to
            // search case: SELECT T.p FROM T AS TAlias
            for (String tqn : froms.values()) {
                if (typeQueryNameOrAlias.equals(tqn))
                    return tqn;
            }
            return null;
        } else
            return typeQueryName;
    }

}
