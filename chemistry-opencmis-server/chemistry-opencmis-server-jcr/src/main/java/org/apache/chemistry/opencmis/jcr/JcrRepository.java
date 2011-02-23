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
package org.apache.chemistry.opencmis.jcr;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jcr.Binary;
import javax.jcr.Credentials;
import javax.jcr.ItemNotFoundException;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JCR back-end for CMIS server.
 */
public final class JcrRepository {
    private static final Log log = LogFactory.getLog(JcrRepository.class);

    private static final String ROOT_ID = "[root]";

    private static final String USER_UNKNOWN = "<unknown>";
    private static final String MIME_UNKNOWN = "application/octet-stream";
    
    private final Repository repository;
    private final String rootPath;
    private final TypeManager typeManager;

    public JcrRepository(Repository repository, String rootPath, TypeManager typeManager) {
        this.repository = repository;
        this.rootPath = normalize(rootPath);
        this.typeManager = typeManager;
    }

    public Session login(Credentials credentials, String repositoryId) {
        try {
            return repository.login(credentials, repositoryId);
        }
        catch (LoginException e) {
            log.debug(e.getMessage(), e);
            throw new CmisPermissionDeniedException(e.getMessage(), e);
        }
        catch (NoSuchWorkspaceException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    public RepositoryInfo getRepositoryInfo(Session session) {
        log.debug("getRepositoryInfo");

        return compileRepositoryInfo(session.getWorkspace().getName());
    }

    public List<RepositoryInfo> getRepositoryInfos(Session session) {
        try {
            ArrayList<RepositoryInfo> infos = new ArrayList<RepositoryInfo>();
            for (String wspName : session.getWorkspace().getAccessibleWorkspaceNames()) {
                infos.add(compileRepositoryInfo(wspName));
            }

            return infos;
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    public TypeDefinitionList getTypesChildren(Session session, String typeId, boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount) {
        
        log.debug("getTypesChildren");
        return typeManager.getTypesChildren(typeId, includePropertyDefinitions, maxItems, skipCount);
    }

    public TypeDefinition getTypeDefinition(Session session, String typeId) {
        log.debug("getTypeDefinition");
        return typeManager.getTypeDefinition(typeId);
    }

    public List<TypeDefinitionContainer> getTypesDescendants(Session session, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions) {

        log.debug("getTypesDescendants");
        return typeManager.getTypesDescendants(typeId, depth, includePropertyDefinitions);
    }

    public String createDocument(Session session, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState) {

        log.debug("createDocument");

        // check properties
        if (properties == null || properties.getProperties() == null) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        // check type
        String typeId = getTypeId(properties);
        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        // check the name
        String name = getStringProperty(properties, PropertyIds.NAME);
        if (!JcrConverter.isValidJcrName(name)) {
            throw new CmisNameConstraintViolationException("Name is not valid: " + name);
        }

        // get parent Node
        Node parent = getNode(session, folderId);
        if (isFile(parent)) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }

        // create the file nodes
        Node fileNode;
        try {
            fileNode = parent.addNode(name, "nt:file");
            Node contentNode = fileNode.addNode("jcr:content", "nt:resource");
            contentNode.addMixin("mix:created");

            // compile the properties
            setProperties(contentNode, typeId, properties);

            // write content, if available
            Binary binary = contentStream == null || contentStream.getStream() == null
                    ? JcrBinary.EMPTY
                    : new JcrBinary(new BufferedInputStream(contentStream.getStream()));
            try {
                contentNode.setProperty("jcr:data", binary);
            }
            finally {
                binary.dispose();
            }

            session.save();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
        catch (IOException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }

        return getId(fileNode);
    }

    public String createDocumentFromSource(Session session, String sourceId, Properties properties, String folderId,
            VersioningState versioningState) {

        log.debug("createDocumentFromSource");

        // get parent folder Node
        Node parent = getNode(session, folderId);
        if (isFile(parent)) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }

        // get source file Node
        Node source = getNode(session, sourceId);
        if (!isFile(source)) {
            throw new CmisObjectNotFoundException("Source is not a document!");
        }

        try {
            // copy the node
            String destPath = parent.getPath();
            if (!destPath.equals("/")) {
                destPath += '/';
            }
            destPath += source.getName();
            session.getWorkspace().copy(source.getPath(), destPath);  // fixme this is not transient!

            Node newNode = session.getNode(destPath);
            String typeId = isFile(source)
                    ? TypeManager.DOCUMENT_TYPE_ID
                    : TypeManager.FOLDER_TYPE_ID;

            // overlay new properties
            updateProperties(newNode, typeId, properties);

            session.save();
            return getId(newNode);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    public String createFolder(Session session, Properties properties, String folderId) {
        log.debug("createFolder");

        // check properties
        if (properties == null || properties.getProperties() == null) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        // check type
        String typeId = getTypeId(properties);
        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        // get parent Node
        Node parent = getNode(session, folderId);
        if (isFile(parent)) {
            throw new CmisObjectNotFoundException("Parent is not a folder!");
        }

        // check the name
        String name = getStringProperty(properties, PropertyIds.NAME);
        if (!JcrConverter.isValidJcrName(name)) {
            throw new CmisNameConstraintViolationException("Name is not valid: " + name);
        }

        // create the folder node
        try {
            Node node = parent.addNode(name, "nt:folder");
            node.addMixin("mix:created");
            node.addMixin("mix:lastModified");

            // compile the properties
            setProperties(node, typeId, properties);

            session.save();
            return getId(node);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    public ObjectData moveObject(Session session, Holder<String> objectId, String targetFolderId,
            ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        log.debug("moveObject");

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the node and parent
        Node node = getNode(session, objectId.getValue());
        Node parent = getNode(session, targetFolderId);

        try {
            // move it
            String destPath = parent.getPath();
            if (!destPath.equals("/")) {
                destPath += '/';
            }
            destPath += node.getName();
            session.move(node.getPath(), destPath);
            Node newNode = session.getNode(destPath);
            objectId.setValue(getId(newNode));

            session.save();
            return compileObjectType(newNode, null, false, objectInfos, requiresObjectInfo);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    public void setContentStream(Session session, Holder<String> objectId, Boolean overwriteFlag,
            ContentStream contentStream) {

        log.debug("setContentStream or deleteContentStream");

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        try {
            // get file and content node
            Node fileNode = getNode(session, objectId.getValue());
            Node contentNode = fileNode.getNode("jcr:content");
            Property data = contentNode.getProperty("jcr:data");

            // check overwrite
            if (!Boolean.TRUE.equals(overwriteFlag) && data.getLength() != 0) {
                throw new CmisContentAlreadyExistsException("Content already exists!");
            }

            // write content, if available
            Binary binary = contentStream == null || contentStream.getStream() == null
                    ? JcrBinary.EMPTY
                    : new JcrBinary(new BufferedInputStream(contentStream.getStream()));
            try {
                contentNode.setProperty("jcr:data", binary);
            }
            finally {
                binary.dispose();
            }

            session.save();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
        catch (IOException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    public void deleteObject(Session session, String objectId) {
        log.debug("deleteObject");

        // get the node
        Node node = getNode(session, objectId);

        try {
            // check if it is a folder and if it is empty
            if (!isFile(node) && node.hasNodes()) {
                throw new CmisConstraintException("Folder is not empty!");
            }

            node.remove();
            session.save();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    public FailedToDeleteData deleteTree(Session session, String folderId) {
        log.debug("deleteTree");

        // get the node
        Node node = getNode(session, folderId);
        FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();

        // if it is a folder, remove it recursively
        if (isFile(node)) {
            throw new CmisInvalidArgumentException("Not a folder: " + folderId);
        }
        else {
            try {
                node.remove();
                session.save();
                result.setIds(Collections.<String>emptyList());
            }
            catch (RepositoryException e) {
                result.setIds(Collections.singletonList(folderId));
            }
        }

        return result;
    }

    public ObjectData updateProperties(Session session, Holder<String> objectId, Properties properties,
            ObjectInfoHandler objectInfos, boolean objectInfoRequired) {

        log.debug("updateProperties");

        if (objectId == null) {
            throw new CmisInvalidArgumentException("Id is not valid!");
        }

        // get the node
        Node node = getNode(session, objectId.getValue());

        try {
            // get and check the new name
            String newName = getStringProperty(properties, PropertyIds.NAME);
            boolean isRename = newName != null && !node.getName().equals(newName);
            if (isRename && !JcrConverter.isValidJcrName(newName)) {
                throw new CmisNameConstraintViolationException("Name is not valid: " + newName);
            }
            if (isRename && isRoot(node)) {
                throw new CmisUpdateConflictException("Cannot rename root node");
            }

            // get the type id
            String typeId = isFile(node)
                    ? TypeManager.DOCUMENT_TYPE_ID
                    : TypeManager.FOLDER_TYPE_ID;

            // update the properties
            updateProperties(node, typeId, properties);

            // rename file or folder if necessary
            Node newNode;
            if (isRename) {
                String destPath = node.getParent().getPath();
                if (!destPath.equals("/")) {
                    destPath += '/';
                }
                destPath += newName;
                session.move(node.getPath(), destPath);
                newNode = session.getNode(destPath);
                objectId.setValue(getId(newNode));
            }
            else {
                newNode = node;
            }

            session.save();
            return compileObjectType(newNode, null, false, objectInfos, objectInfoRequired);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);    
        }
    }

    public ObjectData getObject(Session session, String objectId, String filter, Boolean includeAllowableActions,
            ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        log.debug("getObject");

        // check id
        if (objectId == null) {
            throw new CmisInvalidArgumentException("Object Id must be set.");
        }

        // get the node
        Node node = getNode(session, objectId);

        // gather properties
        return compileObjectType(node, splitFilter(filter), includeAllowableActions, objectInfos, requiresObjectInfo);
    }

    public Properties getProperties(Session session, String objectId, String filter, Boolean includeAllowableActions,
            ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        ObjectData object = getObject(session, objectId, null, false, objectInfos, requiresObjectInfo);
        return object.getProperties();
    }

    public AllowableActions getAllowableActions(Session session, String objectId) {
        log.debug("getAllowableActions");

        Node node = getNode(session, objectId);
        return compileAllowableActions(node);
    }

    public ContentStream getContentStream(Session session, String objectId, BigInteger offset, BigInteger length) {
        log.debug("getContentStream");

        if (offset != null || length != null) {
            throw new CmisInvalidArgumentException("Offset and Length are not supported!");
        }

        // get the node
        Node node = getNode(session, objectId);
        if (!isFile(node)) {
            throw new CmisConstraintException("Not a file: " + objectId);
        }

        try {
            Node contentNode = node.getNode("jcr:content");
            Property data = contentNode.getProperty("jcr:data");

            // compile data
            ContentStreamImpl result = new ContentStreamImpl();
            result.setFileName(node.getName());
            result.setLength(BigInteger.valueOf(data.getLength()));
            result.setMimeType(getPropertyOrElse(contentNode, "jcr:mimeType", MIME_UNKNOWN));
            result.setStream(new BufferedInputStream(data.getBinary().getStream()));

            return result;
        }
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    public ObjectInFolderList getChildren(Session session, String folderId, String filter,
            Boolean includeAllowableActions, Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount,
            ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        log.debug("getChildren");

        // skip and max
        int skip = skipCount == null ? 0 : skipCount.intValue();
        if (skip < 0) {
            skip = 0;
        }

        int max = maxItems == null ? Integer.MAX_VALUE : maxItems.intValue();
        if (max < 0) {
            max = Integer.MAX_VALUE;
        }

        // get the folder
        Node node = getNode(session, folderId);
        if (isFile(node)) {
            throw new CmisObjectNotFoundException("Not a folder: " + folderId);
        }

        // set object info of the the folder
        if (requiresObjectInfo) {
            compileObjectType(node, null, false, objectInfos, requiresObjectInfo);
        }

        // prepare result
        ObjectInFolderListImpl result = new ObjectInFolderListImpl();
        result.setObjects(new ArrayList<ObjectInFolderData>());
        result.setHasMoreItems(false);
        int count = 0;

        try {
            // iterate through children
            NodeIterator childNodes = node.getNodes();
            while (childNodes.hasNext()) {
                Node child = childNodes.nextNode();
                count++;

                if (skip > 0) {
                    skip--;
                    continue;
                }

                if (result.getObjects().size() >= max) {
                    result.setHasMoreItems(true);
                    continue;
                }

                // build and add child object
                ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
                objectInFolder.setObject(compileObjectType(child, splitFilter(filter), includeAllowableActions,
                        objectInfos, requiresObjectInfo));

                if (Boolean.TRUE.equals(includePathSegment)) {
                    objectInFolder.setPathSegment(child.getName());
                }

                result.getObjects().add(objectInFolder);
            }

            result.setNumItems(BigInteger.valueOf(count));
            return result;
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    public List<ObjectInFolderContainer> getDescendants(Session session, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, Boolean includePathSegment, ObjectInfoHandler objectInfos,
            boolean requiresObjectInfo, boolean foldersOnly) {

        log.debug("getDescendants or getFolderTree");

        // check depth
        int d = depth == null ? 2 : depth.intValue();
        if (d == 0) {
            throw new CmisInvalidArgumentException("Depth must not be 0!");
        }
        if (d < -1) {
            d = -1;
        }

        // get the folder
        Node node = getNode(session, folderId);
        if (isFile(node)) {
            throw new CmisObjectNotFoundException("Not a folder: " + folderId);
        }

        // set object info of the the folder
        if (requiresObjectInfo) {
            compileObjectType(node, null, false, objectInfos, requiresObjectInfo);
        }

        // get the tree
        List<ObjectInFolderContainer> result = new ArrayList<ObjectInFolderContainer>();
        gatherDescendants(node, result, foldersOnly, d, splitFilter(filter), includeAllowableActions,
                includePathSegment, objectInfos, requiresObjectInfo);

        return result;
    }

    public ObjectData getFolderParent(Session session, String folderId, String filter, ObjectInfoHandler objectInfos,
            boolean requiresObjectInfo) {

        List<ObjectParentData> parents = getObjectParents(session, folderId, filter, false, false, objectInfos,
                requiresObjectInfo);

        if (parents.isEmpty()) {
            throw new CmisInvalidArgumentException("The root folder has no parent!");
        }

        return parents.get(0).getObject();
    }

    public List<ObjectParentData> getObjectParents(Session session, String objectId, String filter,
            Boolean includeAllowableActions, Boolean includeRelativePathSegment, ObjectInfoHandler objectInfos,
            boolean requiresObjectInfo) {

        log.debug("getObjectParents");

        // get the file or folder
        Node node = getNode(session, objectId);

        // don't climb above the root folder
        if (isRoot(node)) {
            return Collections.emptyList();
        }

        // set object info of the the object
        if (requiresObjectInfo) {
            compileObjectType(node, null, false, objectInfos, requiresObjectInfo);
        }

        try {
            // get parent
            ObjectData object = compileObjectType(node.getParent(), splitFilter(filter), includeAllowableActions,
                    objectInfos, requiresObjectInfo);

            ObjectParentDataImpl result = new ObjectParentDataImpl();
            result.setObject(object);
            if (Boolean.TRUE.equals(includeRelativePathSegment)) {
                result.setRelativePathSegment(node.getName());
            }

            return Collections.singletonList((ObjectParentData) result);
        }
        catch (ItemNotFoundException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);            
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    public ObjectData getObjectByPath(Session session, String folderPath, String filter, boolean includeAllowableActions, 
            boolean includeACL, ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        try {
            log.debug("getObjectByPath");

            // check path
            if (folderPath == null || !folderPath.startsWith("/")) {
                throw new CmisInvalidArgumentException("Invalid folder path!");
            }

            // get the file or folder
            Node root = getRootNode(session);
            Node node;
            node = folderPath.length() == 1
                ? root
                : root.getNode(folderPath.substring(1));

            return compileObjectType(node, splitFilter(filter), includeAllowableActions, objectInfos, requiresObjectInfo);
        }
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e); 
        }
    }

    //------------------------------------------< private >---

    private static String normalize(String path) {
        if (path == null || path.length() == 0) {
            return "/";
        }

        if (!path.startsWith("/")) {
            throw new CmisInvalidArgumentException("Root path must be absolute. Got: " + path);
        }
            
        while (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    private RepositoryInfo compileRepositoryInfo(String repositoryId) {
        RepositoryInfoImpl fRepositoryInfo = new RepositoryInfoImpl();

        fRepositoryInfo.setId(repositoryId);
        fRepositoryInfo.setName(getRepositoryName());
        fRepositoryInfo.setDescription(getRepositoryDescription());

        fRepositoryInfo.setCmisVersionSupported("1.0");

        fRepositoryInfo.setProductName("OpenCMIS JCR");
        fRepositoryInfo.setProductVersion("0.3");
        fRepositoryInfo.setVendorName("OpenCMIS");

        fRepositoryInfo.setRootFolder(ROOT_ID);
        fRepositoryInfo.setThinClientUri("");

        RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
        capabilities.setCapabilityAcl(CapabilityAcl.NONE);
        capabilities.setAllVersionsSearchable(false);
        capabilities.setCapabilityJoin(CapabilityJoin.NONE);
        capabilities.setSupportsMultifiling(false);
        capabilities.setSupportsUnfiling(false);
        capabilities.setSupportsVersionSpecificFiling(false);
        capabilities.setIsPwcSearchable(false);
        capabilities.setIsPwcUpdatable(false);
        capabilities.setCapabilityQuery(CapabilityQuery.NONE);
        capabilities.setCapabilityChanges(CapabilityChanges.NONE);
        capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
        capabilities.setSupportsGetDescendants(true);
        capabilities.setSupportsGetFolderTree(true);
        capabilities.setCapabilityRendition(CapabilityRenditions.NONE);
        fRepositoryInfo.setCapabilities(capabilities);

        return fRepositoryInfo;
    }

    private String getRepositoryName() {
        return repository.getDescriptor(Repository.REP_NAME_DESC);
    }

    private String getRepositoryDescription() {
        StringBuilder description = new StringBuilder();

        for (String key : repository.getDescriptorKeys()) {
            description
                    .append(key)
                    .append('=')
                    .append(repository.getDescriptor(key))
                    .append('\n');
        }

        return description.toString();
    }

    /**
     * Gather the children of a node.
     */
    private void gatherDescendants(Node node, List<ObjectInFolderContainer> list,
            boolean foldersOnly, int depth, Set<String> filter, Boolean includeAllowableActions,
            Boolean includePathSegments, ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        try {
            // iterate through children
            NodeIterator childNodes = node.getNodes();
            while (childNodes.hasNext()) {
                Node child = childNodes.nextNode();

                // folders only?
                if (foldersOnly && isFile(node)) {
                    continue;
                }

                // add to list
                ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
                objectInFolder.setObject(compileObjectType(child, filter, includeAllowableActions, objectInfos,
                        requiresObjectInfo));

                if (Boolean.TRUE.equals(includePathSegments)) {
                    objectInFolder.setPathSegment(child.getName());
                }

                ObjectInFolderContainerImpl container = new ObjectInFolderContainerImpl();
                container.setObject(objectInFolder);

                list.add(container);

                // move to next level
                if (depth != 1 && isFile(child)) {
                    container.setChildren(new ArrayList<ObjectInFolderContainer>());
                    gatherDescendants(child, container.getChildren(), foldersOnly, depth - 1, filter,
                            includeAllowableActions, includePathSegments, objectInfos, requiresObjectInfo);
                }
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);            
        }
    }

    /**
     * Compiles an object type object from a Node.
     */
    private ObjectData compileObjectType(Node node, Set<String> filter, Boolean includeAllowableActions,
            ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        ObjectDataImpl result = new ObjectDataImpl();
        ObjectInfoImpl objectInfo = new ObjectInfoImpl();

        result.setProperties(compileProperties(node, filter, objectInfo));

        if (Boolean.TRUE.equals(includeAllowableActions)) {
            result.setAllowableActions(compileAllowableActions(node));
        }

        if (requiresObjectInfo) {
            objectInfo.setObject(result);
            objectInfos.addObjectInfo(objectInfo);
        }

        return result;
    }

    /**
     * Gathers all base properties of a node.
     */
    private Properties compileProperties(Node node, Set<String> filter, ObjectInfoImpl objectInfo) {
        try {
            if (node == null) {
                throw new IllegalArgumentException("Node must not be null!");
            }

            String id = getId(node);

            // find base type
            String typeId;
            if (isFile(node)) {
                typeId = TypeManager.DOCUMENT_TYPE_ID;
                objectInfo.setBaseType(BaseTypeId.CMIS_DOCUMENT);
                objectInfo.setTypeId(typeId);
                objectInfo.setHasAcl(true);
                objectInfo.setHasContent(true);
                objectInfo.setHasParent(true);
                objectInfo.setVersionSeriesId(id);
                objectInfo.setIsCurrentVersion(true);
                objectInfo.setRelationshipSourceIds(null);
                objectInfo.setRelationshipTargetIds(null);
                objectInfo.setRenditionInfos(null);
                objectInfo.setSupportsDescendants(false);
                objectInfo.setSupportsFolderTree(false);
                objectInfo.setSupportsPolicies(false);
                objectInfo.setSupportsRelationships(false);
                objectInfo.setWorkingCopyId(null);
                objectInfo.setWorkingCopyOriginalId(null);
            }
            else {
                typeId = TypeManager.FOLDER_TYPE_ID;
                objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
                objectInfo.setTypeId(typeId);
                objectInfo.setContentType(null);
                objectInfo.setFileName(null);
                objectInfo.setHasAcl(true);
                objectInfo.setHasContent(false);
                objectInfo.setVersionSeriesId(null);
                objectInfo.setIsCurrentVersion(true);
                objectInfo.setRelationshipSourceIds(null);
                objectInfo.setRelationshipTargetIds(null);
                objectInfo.setRenditionInfos(null);
                objectInfo.setSupportsDescendants(true);
                objectInfo.setSupportsFolderTree(true);
                objectInfo.setSupportsPolicies(false);
                objectInfo.setSupportsRelationships(false);
                objectInfo.setWorkingCopyId(null);
                objectInfo.setWorkingCopyOriginalId(null);
            }

            Set<String> propertyFilter = filter == null ? null : new HashSet<String>(filter);
            PropertiesImpl result = new PropertiesImpl();

            // id
            addPropertyId(result, typeId, propertyFilter, PropertyIds.OBJECT_ID, id);
            objectInfo.setId(id);

            // name
            String name = node.getName();
            addPropertyString(result, typeId, propertyFilter, PropertyIds.NAME, name);
            objectInfo.setName(name);

            Node resourceNode;
            if (isFile(node)) {
                resourceNode = node.getNode("jcr:content");

                // base type and type name
                addPropertyId(result, typeId, propertyFilter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                addPropertyId(result, typeId, propertyFilter, PropertyIds.OBJECT_TYPE_ID, TypeManager.DOCUMENT_TYPE_ID);
                addPropertyBoolean(result, typeId, propertyFilter, PropertyIds.IS_IMMUTABLE, false);

                // file properties
                long length = getPropertyLength(resourceNode, "jcr:data");
                addPropertyInteger(result, typeId, propertyFilter, PropertyIds.CONTENT_STREAM_LENGTH, length);

                String mimeType = getPropertyOrElse(resourceNode, "jcr:mimeType", MIME_UNKNOWN);
                addPropertyString(result, typeId, propertyFilter, PropertyIds.CONTENT_STREAM_MIME_TYPE, mimeType);
                addPropertyString(result, typeId, propertyFilter, PropertyIds.CONTENT_STREAM_FILE_NAME, node.getName());
                addPropertyId(result, typeId, propertyFilter, PropertyIds.CONTENT_STREAM_ID, getId(resourceNode));

                // even though this is not CMIS compliant when document supports versioning
                addPropertyBoolean(result, typeId, propertyFilter, PropertyIds.IS_LATEST_VERSION, true);
                addPropertyBoolean(result, typeId, propertyFilter, PropertyIds.IS_MAJOR_VERSION, true);
                addPropertyBoolean(result, typeId, propertyFilter, PropertyIds.IS_LATEST_MAJOR_VERSION, true);
                addPropertyString(result, typeId, propertyFilter, PropertyIds.VERSION_LABEL, "");
                addPropertyId(result, typeId, propertyFilter, PropertyIds.VERSION_SERIES_ID, id);
                addPropertyBoolean(result, typeId, propertyFilter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false);
                addPropertyId(result, typeId, propertyFilter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, "");
                addPropertyString(result, typeId, propertyFilter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
                addPropertyString(result, typeId, propertyFilter, PropertyIds.CHECKIN_COMMENT, "");

                objectInfo.setContentType(mimeType);
                objectInfo.setFileName(node.getName());
            }
            else {
                resourceNode = node;

                // base type and type name
                addPropertyId(result, typeId, propertyFilter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
                addPropertyId(result, typeId, propertyFilter, PropertyIds.OBJECT_TYPE_ID, TypeManager.FOLDER_TYPE_ID);
                String path = '/' + getRepositoryPath(node);
                addPropertyString(result, typeId, propertyFilter, PropertyIds.PATH, path);

                // folder properties
                if (isRoot(node)) {
                    objectInfo.setHasParent(false);
                }
                else {
                    objectInfo.setHasParent(true);
                    addPropertyId(result, typeId, propertyFilter, PropertyIds.PARENT_ID, getId(node.getParent()));
                }
            }

            // created and modified by
            String createdBy = getPropertyOrElse(resourceNode, "jcr:createdBy", USER_UNKNOWN);
            addPropertyString(result, typeId, propertyFilter, PropertyIds.CREATED_BY, createdBy);
            objectInfo.setCreatedBy(createdBy);

            String lastModifiedBy = getPropertyOrElse(resourceNode, "jcr:lastModifiedBy", USER_UNKNOWN);
            addPropertyString(result, typeId, propertyFilter, PropertyIds.LAST_MODIFIED_BY, lastModifiedBy);

            // creation and modification date
            GregorianCalendar created = getPropertyOrElse(resourceNode, "jcr:created", (GregorianCalendar) null);
            addPropertyDateTime(result, typeId, propertyFilter, PropertyIds.CREATION_DATE, created);
            objectInfo.setCreationDate(created);

            GregorianCalendar lastModified = getPropertyOrElse(resourceNode, "jcr:lastModified", (GregorianCalendar) null);
            addPropertyDateTime(result, typeId, propertyFilter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
            objectInfo.setLastModificationDate(lastModified);

            addPropertyString(result, typeId, propertyFilter, PropertyIds.CHANGE_TOKEN, null);

            // read custom properties
            // todo how should we handle residuals?
            // addCustomProperties(resourceNode, result, propertyFilter);

            if (propertyFilter != null) {
                if (!propertyFilter.isEmpty()) {
                    log.debug("Unknown filter properties: " + propertyFilter.toString());
                }
            }

            return result;
        }
        catch (RepositoryException e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }


    /**
     * Reads and adds properties.
     */
    private static void addCustomProperties(Node node, PropertiesImpl properties, Set<String> filter) {

        try {
            PropertyIterator jcrProperties = node.getProperties();
            while (jcrProperties.hasNext()) {
                Property jcrProperty = jcrProperties.nextProperty();
                PropertyData<?> property = JcrConverter.convert(jcrProperty);
                if (property == null) {
                    continue;
                }

                // check filter
                if (filter != null) {
                    if (filter.contains(property.getId())) {
                        filter.remove(property.getId());
                    }
                    else {
                        continue;
                    }
                }

                // don't overwrite id
                if (PropertyIds.OBJECT_ID.equals(property.getId())) {
                    continue;
                }

                // don't overwrite base type
                if (PropertyIds.BASE_TYPE_ID.equals(property.getId())) {
                    continue;
                }

                // add it
                properties.addProperty(property);
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);            
        }
    }

    private void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null!");
        }

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyIdImpl prop = new PropertyIdImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    private void addPropertyString(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyStringImpl prop = new PropertyStringImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    private void addPropertyInteger(PropertiesImpl props, String typeId, Set<String> filter, String id, long value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyIntegerImpl prop = new PropertyIntegerImpl(id, BigInteger.valueOf(value));
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    private void addPropertyBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, boolean value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyBooleanImpl prop = new PropertyBooleanImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    private void addPropertyDateTime(PropertiesImpl props, String typeId, Set<String> filter, String id,
            GregorianCalendar value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyDateTimeImpl prop = new PropertyDateTimeImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    private boolean checkAddProperty(Properties properties, String typeId, Set<String> filter, String id) {
        if (properties == null || properties.getProperties() == null) {
            throw new IllegalArgumentException("Properties must not be null!");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }

        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new IllegalArgumentException("Unknown type: " + typeId);
        }
        if (!type.getPropertyDefinitions().containsKey(id)) {
            throw new IllegalArgumentException("Unknown property: " + id);
        }

        String queryName = type.getPropertyDefinitions().get(id).getQueryName();

        if (queryName != null && filter != null) {
            if (filter.contains(queryName)) {
                filter.remove(queryName);
            }
            else {
                return false;
            }
        }

        return true;
    }

    /**
     * Compiles the allowable actions for a Node.
     */
    private AllowableActions compileAllowableActions(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node must not be null!");
        }

        Set<Action> aas = new HashSet<Action>();
        addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot(node));
        addAction(aas, Action.CAN_GET_PROPERTIES, true);
        addAction(aas, Action.CAN_UPDATE_PROPERTIES, true);
        addAction(aas, Action.CAN_MOVE_OBJECT, true);
        addAction(aas, Action.CAN_DELETE_OBJECT, true);
        addAction(aas, Action.CAN_GET_ACL, false);
        addAction(aas, Action.CAN_APPLY_ACL, false);
        addAction(aas, Action.CAN_GET_OBJECT_RELATIONSHIPS, false);
        addAction(aas, Action.CAN_ADD_OBJECT_TO_FOLDER, false);
        addAction(aas, Action.CAN_REMOVE_OBJECT_FROM_FOLDER, false);
        addAction(aas, Action.CAN_APPLY_POLICY, false);
        addAction(aas, Action.CAN_GET_APPLIED_POLICIES, false);
        addAction(aas, Action.CAN_REMOVE_POLICY, false);
        addAction(aas, Action.CAN_CREATE_RELATIONSHIP, false);

        if (isFile(node)) {
            addAction(aas, Action.CAN_GET_CONTENT_STREAM, true);
            addAction(aas, Action.CAN_SET_CONTENT_STREAM, true);
            addAction(aas, Action.CAN_DELETE_CONTENT_STREAM, true);
            addAction(aas, Action.CAN_GET_ALL_VERSIONS, false);
            addAction(aas, Action.CAN_CHECK_OUT, false);
            addAction(aas, Action.CAN_CANCEL_CHECK_OUT, false);
            addAction(aas, Action.CAN_CHECK_IN, false);
            addAction(aas, Action.CAN_GET_RENDITIONS, false);
        }
        else {
            addAction(aas, Action.CAN_GET_DESCENDANTS, true);
            addAction(aas, Action.CAN_GET_CHILDREN, true);
            addAction(aas, Action.CAN_GET_FOLDER_PARENT, !isRoot(node));
            addAction(aas, Action.CAN_GET_FOLDER_TREE, true);
            addAction(aas, Action.CAN_CREATE_DOCUMENT, true);
            addAction(aas, Action.CAN_CREATE_FOLDER, true);
            addAction(aas, Action.CAN_DELETE_TREE, true);
        }

        AllowableActionsImpl result = new AllowableActionsImpl();
        result.setAllowableActions(aas);

        return result;
    }

    private static void addAction(Set<Action> aas, Action action, boolean condition) {
        if (condition) {
            aas.add(action);
        }
    }    

    /**
     * Splits a filter statement into a collection of properties. If
     * <code>filter</code> is <code>null</code>, empty or one of the properties
     * is '*' , an empty collection will be returned.
     */
    private static Set<String> splitFilter(String filter) {
        if (filter == null) {
            return null;
        }

        if (filter.trim().length() == 0) {
            return null;
        }

        Set<String> result = new HashSet<String>();
        for (String s : filter.split(",")) {
            s = s.trim();
            if (s.equals("*")) {
                return null;
            } else if (s.length() > 0) {
                result.add(s);
            }
        }

        // set a few base properties
        // query name == id (for base type properties)
        result.add(PropertyIds.OBJECT_ID);
        result.add(PropertyIds.OBJECT_TYPE_ID);
        result.add(PropertyIds.BASE_TYPE_ID);

        return result;
    }

    /**
     * Returns the Node by id or throws an appropriate exception.
     */
    private Node getNode(Session session, String id) {
        try {
            if (id == null || id.length() == 0) {
                throw new CmisInvalidArgumentException("Id is not valid!");
            }

            Node root = getRootNode(session);

            if (id.equals(ROOT_ID)) {
                return root;
            }

            try {
                // fixme harden against directory travel attacks
                String path = new String(Base64.decodeBase64(id.getBytes("ISO-8859-1")), "UTF-8");
                return root.getNode(path);
            }
            catch (UnsupportedEncodingException e) { // should never happen
                log.error(e.getMessage(), e);
                throw new CmisRuntimeException(e.getMessage(), e);
            }
        }
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Returns the id of a Node or throws an appropriate exception.
     */
    private String getId(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("File is not valid!");
        }

        if (isRoot(node)) {
            return ROOT_ID;
        }

        String path = getRepositoryPath(node);

        try {
            return new String(Base64.encodeBase64(path.getBytes("UTF-8")), "ISO-8859-1");
        }
        catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    private String getRepositoryPath(Node node) {
        try {
            String path = node.getPath().substring(rootPath.length());
            return path.startsWith("/") ? path.substring(1) : path;
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    private boolean isRoot(Node node) {
        try {
            return node.getPath().equals(rootPath);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    private Node getRootNode(Session session) {
        try {
            return session.getNode(rootPath);
        }
        catch (PathNotFoundException e) {
            log.debug(e.getMessage(), e);
            throw new CmisObjectNotFoundException(e.getMessage(), e);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    private static long getPropertyLength(Node node, String propertyName) {
        try {
            return node.hasProperty(propertyName)
                ? node.getProperty(propertyName).getLength()
                : -1;
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);            
        }
    }

    private static String getPropertyOrElse(Node node, String propertyName, String defaultValue) {
        try {
            return node.hasProperty(propertyName)
                ? node.getProperty(propertyName).getString()
                : defaultValue;
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    private static GregorianCalendar getPropertyOrElse(Node node, String propertyName, GregorianCalendar defaultValue) {
        try {
            if (node.hasProperty(propertyName)) {
                Calendar date = node.getProperty(propertyName).getDate();
                return Util.toCalendar(date);
            }
            else {
                return defaultValue;
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    private static boolean isFile(Node node) {
        try {
            return node.isNodeType("nt:file");
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }    

    /**
     * Gets the type id from a set of properties.
     */
    private static String getTypeId(Properties properties) {
        PropertyData<?> typeProperty = properties.getProperties().get(PropertyIds.OBJECT_TYPE_ID);
        if (!(typeProperty instanceof PropertyId)) {
            throw new CmisInvalidArgumentException("Type id must be set!");
        }

        String typeId = ((PropertyId) typeProperty).getFirstValue();
        if (typeId == null) {
            throw new CmisInvalidArgumentException("Type id must be set!");
        }

        return typeId;
    }

    /**
     * Check and set a property set on a Node
     */
    private void setProperties(Node node, String typeId, Properties properties) {
        if (properties == null || properties.getProperties() == null) {
            throw new CmisConstraintException("No properties!");
        }

        Set<String> addedProps = new HashSet<String>();
        
        // get the property definitions
        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        try {
            // check if all required properties are there
            for (PropertyData<?> prop : properties.getProperties().values()) {
                PropertyDefinition<?> propDef = type.getPropertyDefinitions().get(prop.getId());

                // do we know that property?
                if (propDef == null) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
                }

                // skip type id
                if (propDef.getId().equals(PropertyIds.OBJECT_TYPE_ID)) {
                    addedProps.add(prop.getId());
                    continue;
                }

                // can it be set?
                if (propDef.getUpdatability() == Updatability.READONLY) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
                }

                // empty properties are invalid
                if (isEmptyProperty(prop)) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' must not be empty!");
                }

                // add it
                JcrConverter.setProperty(node, prop);
                addedProps.add(prop.getId());
            }

            // check if required properties are missing and try to add default values if defined
            for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
                if (!addedProps.contains(propDef.getId()) && propDef.getUpdatability() != Updatability.READONLY) {
                    PropertyData<?> prop = getDefaultProperty(propDef);
                    if (prop == null && propDef.isRequired()) {
                        throw new CmisConstraintException("Property '" + propDef.getId() + "' is required!");
                    }
                    else {
                        JcrConverter.setProperty(node, prop);
                    }
                }
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);            
        }
    }

    /**
     * Check and updates a property set on a Node
     */
    private void updateProperties(Node node, String typeId, Properties properties) {
        if (properties == null) {
            throw new CmisConstraintException("No properties!");
        }

        // get the property definitions
        TypeDefinition type = typeManager.getType(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        // update properties
        for (PropertyData<?> prop : properties.getProperties().values()) {
            PropertyDefinition<?> propDef = type.getPropertyDefinitions().get(prop.getId());

            // do we know that property?
            if (propDef == null) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
            }

            // can it be set?
            if (propDef.getUpdatability() == Updatability.READONLY) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
            }

            if (propDef.getUpdatability() == Updatability.ONCREATE) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' can only be set on create!");
            }

            // default or value
            PropertyData<?> newProp;
            newProp = isEmptyProperty(prop)
                    ? getDefaultProperty(propDef)
                    : prop;

            try {
                if (newProp == null) {
                    JcrConverter.removeProperty(node, prop);
                }
                else {
                    JcrConverter.setProperty(node, newProp);
                }
            }
            catch (RepositoryException e) {
                log.debug(e.getMessage(), e);
                throw new CmisStorageException(e.getMessage(), e);
            }
        }
    }

    private static boolean isEmptyProperty(PropertyData<?> prop) {
        return prop == null || prop.getValues() == null || prop.getValues().isEmpty();
    }    

    @SuppressWarnings("unchecked")
    private static PropertyData<?> getDefaultProperty(PropertyDefinition<?> propDef) {
        if (propDef == null) {
            return null;
        }

        List<?> defaultValue = propDef.getDefaultValue();
        if (defaultValue != null && !defaultValue.isEmpty()) {
            switch (propDef.getPropertyType()) {
                case BOOLEAN:
                    return new PropertyBooleanImpl(propDef.getId(), (List<Boolean>) defaultValue);
                case DATETIME:
                    return new PropertyDateTimeImpl(propDef.getId(), (List<GregorianCalendar>) defaultValue);
                case DECIMAL:
                    return new PropertyDecimalImpl(propDef.getId(), (List<BigDecimal>) defaultValue);
                case HTML:
                    return new PropertyHtmlImpl(propDef.getId(), (List<String>) defaultValue);
                case ID:
                    return new PropertyIdImpl(propDef.getId(), (List<String>) defaultValue);
                case INTEGER:
                    return new PropertyIntegerImpl(propDef.getId(), (List<BigInteger>) defaultValue);
                case STRING:
                    return new PropertyStringImpl(propDef.getId(), (List<String>) defaultValue);
                case URI:
                    return new PropertyUriImpl(propDef.getId(), (List<String>) defaultValue);
                default:
                    throw new RuntimeException("Unknown datatype: " + propDef.getPropertyType());
            }
        }
        return null;
    }

    /**
     * Returns the first value of an string property.
     */
    private static String getStringProperty(Properties properties, String name) {
        PropertyData<?> property = properties.getProperties().get(name);
        if (!(property instanceof PropertyString)) {
            return null;
        }

        return ((PropertyString) property).getFirstValue();
    }

}
