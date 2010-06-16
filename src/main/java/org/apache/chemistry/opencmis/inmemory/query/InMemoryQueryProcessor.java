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
import org.apache.chemistry.opencmis.inmemory.TypeManager;
import org.apache.chemistry.opencmis.inmemory.query.QueryObject.SortSpec;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Filing;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.Folder;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A processor for a CMIS query for the In-Memory server. During tree traversal conditions
 * are checked against the data contained in the central hash map with all objects. In a first 
 * pass one time setup is performed, in a custom walk across the query expression tree an object
 * is checked if it matches. In case of a match it is appended to a list of matching objects.
 * 
 * @author Jens
 *
 */
public class InMemoryQueryProcessor implements IQueryConditionProcessor {

    private static Log LOG = LogFactory.getLog(InMemoryQueryProcessor.class);
    
    private List<StoredObject> matches = new ArrayList<StoredObject>();
    private QueryObject queryObj;
    private Tree whereTree;
    
    public InMemoryQueryProcessor() {
    }
    
    public void setQueryObject(QueryObject qo) {
        queryObj = qo;
    }
    
    public void onStartProcessing(Tree node) {
        // log.debug("onStartProcessing()");
        // checkRoot(node);
        whereTree = node;
    }

    public void onStopProcessing() {
        // log.debug("onStopProcessing()");
    }

    /**
     * Check for each object contained in the in-memory repository if it matches the current query
     * expression. If yes add it to the list of matched objects.
     * 
     * @param so
     *      object stored in the in-memory repository
     */
    public void checkMatch(StoredObject so) {
        // log.debug("checkMatch() for object: " + so.getId());
        match(so);
    }

    public ObjectList buildResultList(TypeManager tm, String user, 
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount) {
        
        sortMatches();

        ObjectListImpl res = new ObjectListImpl();
        List<ObjectData> objDataList = new ArrayList<ObjectData>();
        Map<String, String> props = queryObj.getRequestedProperties();
        Map<String, String> funcs = queryObj.getRequestedFuncs();
        for (StoredObject so : matches) {
            TypeDefinition td = tm.getTypeById(so.getTypeId()).getTypeDefinition();
            ObjectData od = PropertyCreationHelper.getObjectDataQueryResult(td, so, user, 
                    props, funcs, includeAllowableActions, includeRelationships, renditionFilter);
            
            objDataList.add(od); 
        }
        res.setObjects(objDataList);
        res.setNumItems(BigInteger.valueOf(objDataList.size()));
        res.setHasMoreItems(false);
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

            public int compare(StoredObject so1, StoredObject so2) {
                SortSpec s = orderBy.get(0);
                CmisSelector sel = s.getSelector();
                int result;
                
                if (sel instanceof ColumnReference) {
                    String propId = ((ColumnReference)sel).getPropertyId();
                    Object propVal1 = so1.getProperties().get(propId).getFirstValue();
                    Object propVal2 = so2.getProperties().get(propId).getFirstValue();
                    if (propVal1 == null && propVal2 == null)
                        result = 0;
                    else if (propVal1 == null)
                        result = -1;
                    else if (propVal2 == null)
                        result = 1;
                    else 
                        result = ((Comparable)propVal1).compareTo(propVal2);                    
                } else {
                    String funcName = ((FunctionReference)sel).getName();
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

    private void match(StoredObject so) {
        // log.debug("Tree is: " + (whereTree == null ? "(empty)" : whereTree.toStringTree()));
        // first check if type is matching...
        String queryName = queryObj.getTypes().values().iterator().next(); // as we don't support JOINS take first type
        TypeDefinition td = queryObj.getTypeDefinitionFromQueryName(queryName);
        boolean typeMatches = typeMatches(td, so);
        // ... then check expression...
        if (typeMatches)
            evalWhereTree(whereTree, so);        
    }
    
    private void evalWhereTree(Tree node, StoredObject so) {
        boolean match=true;
        if (null != node)
            match = evalWhereNode(so, node.getChild(0));
        if (match)
            matches.add(so); // add to list
    }

    // Standard expression evaluator for single pass walking. This is used as first
    // pass walk in this object for one-time setup tasks (e.g. setup maps)
    public void onEquals(Tree eqNode, Tree leftNode, Tree rightNode) {
        
    }
    
    public void onNotEquals(Tree neNode, Tree leftNode, Tree rightNode) {
        
    }
    
    public void onGreaterThan(Tree gtNode, Tree leftNode, Tree rightNode) {
        
    }
    
    public void onGreaterOrEquals(Tree geNode, Tree leftNode, Tree rightNode) {
        
    }
    
    public void onLessThan(Tree ltNode, Tree leftNode, Tree rightNode) {
        
    }
    
    public void onLessOrEquals(Tree leqNode, Tree leftNode, Tree rightNode) { 
        
    }

    // Boolean operators
    public void onNot(Tree opNode, Tree leftNode) {
        
    }
    
    public void onAnd(Tree opNode, Tree leftNode, Tree rightNode) {
        
    }
    
    public void onOr(Tree opNode, Tree leftNode, Tree rightNode) {
        
    }

    // Multi-value:
    public void onIn(Tree node, Tree colNode, Tree listNode) {
        
    }
    
    public void onNotIn(Tree nod, Tree colNode, Tree listNode) {
        
    }
    
    public void onNotInList(Tree node) {
        
    }
    
    public void onInAny(Tree node, Tree colNode, Tree listNode) {
        
    }
    
    public void onNotInAny(Tree node, Tree colNode, Tree listNode) {
        
    }
    
    public void onEqAny(Tree node, Tree literalNode, Tree colNode) {
        
    }

    // Null comparisons:
    public void onIsNull(Tree nullNode, Tree colNode) {
        
    }
    
    public void onIsNotNull(Tree notNullNode, Tree colNode) {
        
    }

    // String matching:
    public void onIsLike(Tree node, Tree colNode, Tree stringNode) {
        
    }
    
    public void onIsNotLike(Tree node, Tree colNode, Tree stringNode) {
        
    }

    // Functions:
    public void onContains(Tree node, Tree colNode, Tree paramNode) {
        
    }
    
    public void onInFolder(Tree node, Tree colNode, Tree paramNode) {
        
    }
    
    public void onInTree(Tree node, Tree colNode, Tree paramNode) {
        
    }
    public void onScore(Tree node, Tree paramNode) { 
        
    }

    private void checkRoot(Tree root) {
        if (root.getType() == CMISQLLexerStrict.WHERE)
            LOG.debug("Found root with where node.");
        else
            LOG.debug("NOT Found root with where node!! " + root.toStringTree());        
    }

    /**
     * For each object check if it matches and append it to match-list if it does.
     * We do here our own walking mechanism so that we can pass additional parameters
     * and define the return types.
     *   
     * @param node
     *      node in where clause
     * @return
     *      true if it matches, false if it not matches
     */
    boolean evalWhereNode(StoredObject so, Tree node) {
        boolean matches = true;

        switch (node.getType()) {
        case CMISQLLexerStrict.WHERE:
            matches = evalWhereNode(so, node.getChild(0));
            break; // ignore
        case CMISQLLexerStrict.EQ:
            matches = evalWhereEquals(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.NEQ:
            matches = evalWhereNotEquals(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.GT:
            matches = evalWhereGreaterThan(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.GTEQ:
            matches = evalWhereGreaterOrEquals(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.LT:
            matches = evalWhereLessThan(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.LTEQ:
            matches = evalWhereLessOrEquals(so, node, node.getChild(0), node.getChild(1));
            break;

        case CMISQLLexerStrict.NOT:
            matches = evalWhereNot(so, node, node.getChild(0));
            break;
        case CMISQLLexerStrict.AND:
            matches = evalWhereAnd(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.OR:
            matches = evalWhereOr(so, node, node.getChild(0), node.getChild(1));
            break;

        // Multi-value:
        case CMISQLLexerStrict.IN:
            matches = evalWhereIn(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.NOT_IN:
            matches = evalWhereNotIn(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.IN_ANY:
            matches = evalWhereInAny(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.NOT_IN_ANY:
            matches = evalWhereNotInAny(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.EQ_ANY:
            matches = evalWhereEqAny(so, node, node.getChild(0), node.getChild(1));
            break;

        // Null comparisons:
        case CMISQLLexerStrict.IS_NULL:
            matches = evalWhereIsNull(so, node, node.getChild(0));
            break;
        case CMISQLLexerStrict.IS_NOT_NULL:
            matches = evalWhereIsNotNull(so, node, node.getChild(0));
            break;

        // String matching
        case CMISQLLexerStrict.LIKE:
            matches = evalWhereIsLike(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.NOT_LIKE:
            matches = evalWhereIsNotLike(so, node, node.getChild(0), node.getChild(1));
            break;

        // Functions
        case CMISQLLexerStrict.CONTAINS:
            if (node.getChildCount() == 1)
                matches = evalWhereContains(so, node, null, node.getChild(0));
            else
                matches = evalWhereContains(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.IN_FOLDER:
            if (node.getChildCount() == 1)
                matches = evalWhereInFolder(so, node, null, node.getChild(0));
            else
                matches = evalWhereInFolder(so, node, node.getChild(0), node.getChild(1));
            break;
        case CMISQLLexerStrict.IN_TREE:
            if (node.getChildCount() == 1)
                matches = evalWhereInTree(so, node, null, node.getChild(0));
            else
                matches = evalWhereInTree(so, node, node.getChild(0), node.getChild(1));
            break;

        default:
            // do nothing;
        }

        return matches;
    }

    private boolean evalWhereEquals(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        Integer cmp = compareTo(so, leftChild, rightChild);
        if (null == cmp)
            return false; // property is not set
        else
            return cmp == 0;
    }

    private boolean evalWhereNotEquals(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        Integer cmp = compareTo(so, leftChild, rightChild);
        if (null == cmp)
            return false; // property is not set
        else
            return cmp != 0;
    }

    private boolean evalWhereGreaterThan(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        Integer cmp = compareTo(so, leftChild, rightChild);
        if (null == cmp)
            return false; // property is not set
        else
            return cmp > 0;
    }

    private boolean evalWhereGreaterOrEquals(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        Integer cmp = compareTo(so, leftChild, rightChild);
        if (null == cmp)
            return false; // property is not set
        else
            return cmp >= 0;
    }

    private boolean evalWhereLessThan(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        Integer cmp = compareTo(so, leftChild, rightChild);
        if (null == cmp)
            return false; // property is not set
        else
            return cmp < 0;
    }

    private boolean evalWhereLessOrEquals(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        Integer cmp = compareTo(so, leftChild, rightChild);
        if (null == cmp)
            return false; // property is not set
        else
            return cmp <= 0;
    }

    private boolean evalWhereNot(StoredObject so, Tree node, Tree child) {
        boolean matches = evalWhereNode(so, child);
        return !matches;
    }

    private boolean evalWhereAnd(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        boolean matches1 = evalWhereNode(so, leftChild);
        boolean matches2 = evalWhereNode(so, rightChild);        
        return matches1 && matches2;
    }

    private boolean evalWhereOr(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        boolean matches1 = evalWhereNode(so, leftChild);
        boolean matches2 = evalWhereNode(so, rightChild);        
        return matches1 || matches2;
    }

    private boolean evalWhereIn(StoredObject so, Tree node, Tree leftChild, Tree rightChild) {
        throw new RuntimeException("Operator IN not supported in InMemory server.");
    }

    private boolean evalWhereNotIn(StoredObject so, Tree node, Tree colNode, Tree listNode) {
        throw new RuntimeException("Operator NOT IN not supported in InMemory server.");
    }

    private boolean evalWhereInAny(StoredObject so, Tree node, Tree colNode, Tree listNode) {
        throw new RuntimeException("Operator IN ANY not supported in InMemory server.");
    }

    private boolean evalWhereNotInAny(StoredObject so, Tree node, Tree colNode, Tree listNode) {
        throw new RuntimeException("Operator NOT IN ANY not supported in InMemory server.");
    }

    private boolean evalWhereEqAny(StoredObject so, Tree node, Tree colNode, Tree listNode) {
        throw new RuntimeException("Operator = ANY not supported in InMemory server.");
    }

    private boolean evalWhereIsNull(StoredObject so, Tree node, Tree child) {
       Object propVal = getPropertyValue(child, so);
       return null == propVal;
    }

    private boolean evalWhereIsNotNull(StoredObject so, Tree node, Tree child) {
        Object propVal = getPropertyValue(child, so);
        return null != propVal;
    }

    private boolean evalWhereIsLike(StoredObject so, Tree node, Tree colNode, Tree StringNode) {
        Object rVal = onLiteral(StringNode);
        if (!(rVal instanceof String))
                throw new RuntimeException("LIKE operator requires String literal on right hand side.");
        
        ColumnReference colRef = getColumnReference(colNode);
        TypeDefinition td = colRef.getTypeDefinition();
        PropertyDefinition<?> pd = td.getPropertyDefinitions().get(colRef.getPropertyId());
        PropertyType propType = pd.getPropertyType();
        if (propType != PropertyType.STRING && propType != PropertyType.HTML &&  propType != PropertyType.ID &&
                propType != PropertyType.URI)
            throw new RuntimeException("Property type "+ propType.value() + " is not allowed FOR LIKE");
        if (pd.getCardinality() != Cardinality.SINGLE)
            throw new RuntimeException("LIKE is not allowed for multi-value properties ");
        
        String propVal = (String) so.getProperties().get(colRef.getPropertyId()).getFirstValue();
        String pattern = translatePattern((String) rVal); // SQL to Java regex syntax
        Pattern p = Pattern.compile(pattern);
        return p.matcher(propVal).matches();
    }

    private boolean evalWhereIsNotLike(StoredObject so, Tree node, Tree colNode, Tree stringNode) {
        return ! evalWhereIsLike(so, node, colNode, stringNode);
    }

    private boolean evalWhereContains(StoredObject so, Tree node, Tree colNode, Tree paramNode) {
        throw new RuntimeException("Operator CONTAINS not supported in InMemory server.");
    }

    private boolean evalWhereInFolder(StoredObject so, Tree node, Tree colNode, Tree paramNode) {
        if (null != colNode) {
            getTableReference(colNode); 
            // just for error checking we do not evaluate this, there is only one from without join support
        }
        Object lit = onLiteral(paramNode);
        if (!(lit instanceof String))
            throw new RuntimeException("Folder id in IN_FOLDER must be of type String");
        String folderId = (String) lit;
        
        // check if object is in folder
        if (so instanceof Filing)
            return hasParent((Filing)so, folderId);
        else
            return false;
    }

    private boolean evalWhereInTree(StoredObject so, Tree node, Tree colNode, Tree paramNode) {
        if (null != colNode) {
            getTableReference(colNode); 
            // just for error checking we do not evaluate this, there is only one from without join support
        }
        Object lit = onLiteral(paramNode);
        if (!(lit instanceof String))
            throw new RuntimeException("Folder id in IN_FOLDER must be of type String");
        String folderId = (String) lit;
        
        // check if object is in folder
        if (so instanceof Filing)
            return hasAncestor((Filing) so, folderId);
        else
            return false;
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

    private Object onLiteral(Tree node) {
        int type = node.getType();
        String text = node.getText();
        switch (type) {
        case CMISQLLexerStrict.BOOL_LIT:
            return Boolean.parseBoolean(node.getText());
        case CMISQLLexerStrict.NUM_LIT:
            if (text.contains(".") || text.contains("e") || text.contains("E"))
                return Double.parseDouble(text);
            else    
                return Long.parseLong(text);
        case CMISQLLexerStrict.STRING_LIT:
            return text.substring(1, text.length()-1);
        case CMISQLLexerStrict.TIME_LIT:
            GregorianCalendar gc = CalendarHelper.fromString(text.substring(text.indexOf('\'')+1, text.lastIndexOf('\'')));
            return gc; 
        default:
            LOG.error("Unknown literal. " + node);
            return null;
        }
    }
    
    private Integer compareTo(StoredObject so, Tree leftChild, Tree rightChild) {
        Object rVal = onLiteral(rightChild);
        
        //log.debug("retrieve node from where: " + System.identityHashCode(leftChild) + " is " + leftChild);
        ColumnReference colRef = getColumnReference(leftChild);
        TypeDefinition td = colRef.getTypeDefinition();
        PropertyDefinition<?> pd = td.getPropertyDefinitions().get(colRef.getPropertyId());
        PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
        if (lVal instanceof List<?>)
            throw new RuntimeException("You can't query operators <, <=, ==, !=, >=, > on multi-value properties ");
        else
            return compareTo(pd, lVal, rVal);
    }
    
    private int compareTo(PropertyDefinition<?> td, PropertyData<?> lVal, Object rVal) {
        Object lValue = lVal.getFirstValue();
        switch (td.getPropertyType()) {
        case BOOLEAN:
            if (rVal instanceof Boolean)
                return ((Boolean)lValue).compareTo((Boolean)rVal);
            else 
                throwIncompatibleTypesException(lValue, rVal);
            break;
        case INTEGER: 
        { 
            Long lLongValue = ((BigInteger)lVal.getFirstValue()).longValue();
            if (rVal instanceof Long)
                return (lLongValue).compareTo((Long)rVal);
            else if (rVal instanceof Double)
                return Double.valueOf(((Integer)lValue).doubleValue()).compareTo((Double)rVal);
            else 
                throwIncompatibleTypesException(lValue, rVal);
            break;
        }
        case DATETIME:
            if (rVal instanceof GregorianCalendar) {
                // LOG.debug("left:" + CalendarHelper.toString((GregorianCalendar)lValue) + " right: " + CalendarHelper.toString((GregorianCalendar)rVal));
                return ((GregorianCalendar)lValue).compareTo((GregorianCalendar)rVal);
            } else 
                throwIncompatibleTypesException(lValue, rVal);
            break;
        case DECIMAL:
        { 
            Double lDoubleValue = ((BigDecimal)lVal.getFirstValue()).doubleValue();
            if (rVal instanceof Double)
                return lDoubleValue.compareTo((Double)rVal);
            else if (rVal instanceof Long)
                return Double.valueOf(((Integer)lValue).doubleValue()).compareTo((Double)rVal);
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
                return ((String)lValue).compareTo((String)rVal);
            } else 
                throwIncompatibleTypesException(lValue, rVal);
            break;
        }
        return 0;
    }
    
    private ColumnReference getColumnReference(Tree columnNode) {
        CmisSelector sel = queryObj.getColumnReference(columnNode.getTokenStartIndex());
        if (null == sel)
            throw new RuntimeException("Unknown property query name " + columnNode.getChild(0));
        else if (sel instanceof ColumnReference)
            return (ColumnReference) sel;
        else
            throw new RuntimeException("Unexpected numerical value function in where clause");
    }
    
    private String getTableReference(Tree tableNode) {
        String typeQueryName = queryObj.getTypeQueryName(tableNode.getText());
        if (null == typeQueryName)
            throw new RuntimeException("Inavlid type in IN_FOLDER() or IN_TREE(), must be in FROM list: " + tableNode.getText());
        return typeQueryName;
    }

    private Object getPropertyValue(Tree columnNode, StoredObject so) {
        ColumnReference colRef = getColumnReference(columnNode);
        TypeDefinition td = colRef.getTypeDefinition();
        PropertyDefinition<?> pd = td.getPropertyDefinitions().get(colRef.getPropertyId());
        PropertyData<?> lVal = so.getProperties().get(colRef.getPropertyId());
        if (null == lVal)
            return null;
        else {
            if (pd.getCardinality() == Cardinality.SINGLE)
                return null == lVal ? null : lVal.getFirstValue();
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
            else if (index == 0 || index > 0 && wildcardString.charAt(index-1) != '\\') {
                res.append(wildcardString.substring(start, index));
                res.append(".*");
            } else 
                res.append(wildcardString.substring(start, index+1));
            start = index+1;
        }
        wildcardString = res.toString();
        
        index = 0;
        start = 0;
        res = new StringBuffer();
        
        while (index >= 0) {
            index = wildcardString.indexOf('_', start);
            if (index < 0) 
                res.append(wildcardString.substring(start));
            else if (index == 0 || index > 0 && wildcardString.charAt(index-1) != '\\') {
                res.append(wildcardString.substring(start, index));
                res.append(".");
            } else 
                res.append(wildcardString.substring(start, index+1));
            start = index+1;
        }
        return res.toString();
    }
    
    private void throwIncompatibleTypesException(Object o1, Object o2) {
        throw new RuntimeException("Incompatible Types to compare: " + o1 + " and " + o2);
    }

}
