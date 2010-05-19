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
package org.apache.chemistry.opencmis.inmemory.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenSource;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.ChangeType;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ChangeEventInfoDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.inmemory.query.InMemoryQueryWalker;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.ObjectStore;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoreManager;
import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.inmemory.storedobj.impl.ObjectStoreImpl;
import org.apache.chemistry.opencmis.inmemory.types.PropertyCreationHelper;
import org.apache.chemistry.opencmis.server.support.query.CMISQLLexer;
import org.apache.chemistry.opencmis.server.support.query.CMISQLParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemoryDiscoveryServiceImpl extends InMemoryAbstractServiceImpl{

    private static final Log LOG = LogFactory.getLog(InMemoryDiscoveryServiceImpl.class.getName());

    AtomLinkInfoProvider fAtomLinkProvider;
    InMemoryNavigationServiceImpl fNavigationService; // real implementation of
    // the service
    InMemoryRepositoryServiceImpl fRepositoryService;

    public InMemoryDiscoveryServiceImpl(StoreManager storeManager, InMemoryRepositoryServiceImpl repSvc,
            InMemoryNavigationServiceImpl navSvc) {
        super(storeManager);
        fAtomLinkProvider = new AtomLinkInfoProvider(fStoreManager);
        fNavigationService = navSvc;
        fRepositoryService = repSvc;
    }

    public ObjectList getContentChanges(CallContext context, String repositoryId, Holder<String> changeLogToken,
            Boolean includeProperties, String filter, Boolean includePolicyIds, Boolean includeAcl,
            BigInteger maxItems, ExtensionsData extension, ObjectInfoHandler objectInfos) {
        // dummy implementation using hard coded values

        RepositoryInfo rep = fRepositoryService.getRepositoryInfo(context, repositoryId, null);
        String rootFolderId = rep.getRootFolderId();

        ObjectListImpl objList = new ObjectListImpl();
        List<ObjectInFolderContainer> tempRes = fNavigationService.getDescendants(context, repositoryId, rootFolderId,
                BigInteger.valueOf(3), filter, false, IncludeRelationships.NONE, null, false, extension, null);

        // convert ObjectInFolderContainerList to objectList
        List<ObjectData> lod = new ArrayList<ObjectData>();
        for (ObjectInFolderContainer obj : tempRes) {
            convertList(lod, obj);
        }
        objList.setObjects(lod);
        objList.setNumItems(BigInteger.valueOf(lod.size()));

        // To be able to provide all Atom links in the response we need
        // additional information:
        fAtomLinkProvider.fillInformationForAtomLinks(repositoryId, null, objectInfos, objList);
        return objList;
    }

    private void convertList(List<ObjectData> lod, ObjectInFolderContainer obj) {
        lod.add(obj.getObject().getObject());
        // add dummy event info
        ObjectData oif = obj.getObject().getObject();
        ObjectDataImpl oifImpl = (ObjectDataImpl) oif;
        ChangeEventInfoDataImpl changeEventInfo = new ChangeEventInfoDataImpl();
        changeEventInfo.setChangeType(ChangeType.UPDATED);
        changeEventInfo.setChangeTime(new GregorianCalendar());
        oifImpl.setChangeEventInfo(changeEventInfo);
        if (null != obj.getChildren()) {
            for (ObjectInFolderContainer oifc : obj.getChildren()) {
                convertList(lod, oifc);
            }
        }
    }

     public ObjectList query(CallContext context, String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {

        LOG.debug("start query()");
        checkRepositoryId(repositoryId);
        ObjectStore objectStore = fStoreManager.getObjectStore(repositoryId);

        String user = context.getUsername();
        List<ObjectData> lod = new ArrayList<ObjectData>();

        try {
            CMISQLParser parser = getParser(statement);

            CMISQLParser.query_return parsedStatement = parser.query();
            if (parser.errorMessage != null) {
                throw new CmisRuntimeException("Cannot parse query: " + statement + " (" + parser.errorMessage + ")");
            }
            CommonTree tree = (CommonTree) parsedStatement.getTree();            
            TokenStream tokens = parser.getTokenStream();

            String tableName = null;
            // iterate over all the objects and check for each if the query matches
            for (String objectId : ((ObjectStoreImpl) objectStore).getIds()) {
                StoredObject so = objectStore.getObjectById(objectId);
                if (tableName != null) {
                    // type already available: check early
                    if (!typeMatches(context, repositoryId, tableName, so.getTypeId())) {
                        continue;
                    }
                }
                CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
                nodes.setTokenStream(tokens);
                InMemoryQueryWalker walker = new InMemoryQueryWalker(nodes);
                InMemoryQueryWalker.query_return ret = matchStoredObject(walker, so);
                if (tableName == null) {
                    // first time: check late
                    tableName = ret.tableName.toLowerCase();
                    if (!typeMatches(context, repositoryId, tableName, so.getTypeId())) {
                        continue;
                    }
                }
                if (ret.matches) {
                    String filter = "*"; // TODO select_list
                    ObjectData od = PropertyCreationHelper.getObjectData(fStoreManager, so, filter, user,
                            includeAllowableActions, includeRelationships, renditionFilter, false, false, null);
                    lod.add(od);
                }
            }

        } catch (IOException e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        } catch (RecognitionException e) {
            throw new CmisRuntimeException("Cannot parse query: " + statement, e);
        }
        // TODO order_by_clause

        ObjectListImpl objList = new ObjectListImpl();
        objList.setObjects(lod);
        objList.setNumItems(BigInteger.valueOf(lod.size()));

        LOG.debug("stop query()");
        return objList;
    }

    protected boolean typeMatches(CallContext context, String repositoryId, String tableName, String typeId) {
        do {
            TypeDefinition td = fRepositoryService.getTypeDefinition(context, repositoryId, typeId, null);
            if (tableName.equals(td.getQueryName().toLowerCase())) {
                return true;
            }
            // check parent type
            typeId = td.getParentTypeId();
        } while (typeId != null);
        return false;
    }


    private CMISQLParser getParser(String statement) throws RecognitionException, IOException {
        CharStream input = new ANTLRInputStream(new ByteArrayInputStream(statement.getBytes("UTF-8")));
        TokenSource lexer = new CMISQLLexer(input);
        TokenStream tokens = new CommonTokenStream(lexer);
        CMISQLParser parser = new CMISQLParser(tokens);
        return parser;
    }
        
    private InMemoryQueryWalker.query_return matchStoredObject(InMemoryQueryWalker walker, StoredObject so) throws RecognitionException {
        InMemoryQueryWalker.query_return res = walker.query(so);
        return res;
    }
    
    protected InMemoryQueryWalker.query_return queryStoredObject(String statement, StoredObject so) {
        try {
            CharStream input = new ANTLRInputStream(new ByteArrayInputStream(statement.getBytes("UTF-8")));
            TokenSource lexer = new CMISQLLexer(input);
            TokenStream tokens = new CommonTokenStream(lexer);
            CMISQLParser parser = new CMISQLParser(tokens);
            CMISQLParser.query_return query = parser.query();
            if (parser.errorMessage != null) {
                throw new CmisRuntimeException("Cannot parse query: " + statement + " (" + parser.errorMessage + ")");
            }
            CommonTree tree = (CommonTree) query.getTree();
            CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
            nodes.setTokenStream(tokens);
            InMemoryQueryWalker walker = new InMemoryQueryWalker(nodes);
            InMemoryQueryWalker.query_return res = walker.query(so);
            if (walker.errorMessage != null) {
                throw new CmisRuntimeException("Cannot parse query: " + statement + " (" + walker.errorMessage + ")");
            }
            return res;
        } catch (IOException e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        } catch (RecognitionException e) {
            throw new CmisRuntimeException("Cannot parse query: " + statement, e);
        }
    }

}
