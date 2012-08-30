/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.chemistry.opencmis.jcr;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.definitions.DocumentTypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;
import org.apache.chemistry.opencmis.jcr.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for all JCR <code>Node</code>s to be represented as CMIS objects. Instances of this class
 * are responsible for mapping from CMIS to JCR and vice versa.
 */
public abstract class JcrNode {

    private static final Logger log = LoggerFactory.getLogger(JcrNode.class);

    /**
     * Default value for last cmis:createdBy and cmis:modifiedBy
     */
    public static final String USER_UNKNOWN = "unknown";

    /**
     * Default value for cmis:createdBy and cmis:lastModifiedDate
     * (Thu Jan 01 01:00:00 CET 1970)
     */
    public static final GregorianCalendar DATE_UNKNOWN;

    static {
        DATE_UNKNOWN = new GregorianCalendar();
        DATE_UNKNOWN.setTimeInMillis(0);
    }

    private final Node node;
    protected final JcrTypeManager typeManager;
    protected final PathManager pathManager;
    protected final JcrTypeHandlerManager typeHandlerManager;

    /**
     * Create a new instance wrapping a JCR <code>node</code>.
     *
     * @param node  the JCR <code>node</code> to represent
     * @param typeManager
     * @param pathManager
     * @param typeHandlerManager
     */
    protected JcrNode(Node node, JcrTypeManager typeManager, PathManager pathManager, JcrTypeHandlerManager typeHandlerManager) {
        this.node = node;
        this.typeManager = typeManager;
        this.pathManager = pathManager;
        this.typeHandlerManager = typeHandlerManager;
    }

    /**
     * @return  the JCR <code>node</code> represented by this instance
     */
    public Node getNode() {
        return node;
    }

    /**
     * @return  the name of the CMIS object represented by this instance
     * @throws  CmisRuntimeException
     */
    public String getName() {
        try {
            return getNodeName();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @return  the id of the CMIS object represented by this instance
     * @throws  CmisRuntimeException
     */
    public String getId() {
        try {
            return getObjectId();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @return  the typeId of the CMIS object represented by this instance
     */
    public String getTypeId() {
        return getTypeIdInternal();
    }

    /**
     * @return  <code>true</code> iff this instance represent the root of the CMIS folder hierarchy.
     */
    public boolean isRoot() {
        return pathManager.isRoot(node);
    }

    /**
     * @return  <code>true</code> iff this instance represents a cmis:document type
     */
    public boolean isDocument() {
        return BaseTypeId.CMIS_DOCUMENT == getBaseTypeId();
    }

    /**
     * @return  <code>true</code> iff this instance represents a cmis:folder type
     */
    public boolean isFolder() {
        return BaseTypeId.CMIS_FOLDER == getBaseTypeId();
    }

    /**
     * @return  <code>true</code> iff this instance represents a versionable CMIS object
     */
    public boolean isVersionable() {
        TypeDefinition typeDef = typeManager.getType(getTypeIdInternal());
        return typeDef instanceof DocumentTypeDefinition
                ? ((DocumentTypeDefinition) typeDef).isVersionable()
                : false;
    }

    /**
     * @return  this instance as a <code>JcrDocument</code>
     * @throws CmisConstraintException if <code>this.isDocument() == false</code>
     */
    public JcrDocument asDocument() {
        if (isDocument()) {
            return (JcrDocument) this;
        }
        else {
            throw new CmisConstraintException("Not a document: " + this);
        }
    }

    /**
     * @return  this instance as a <code>JcrFolder</code>
     * @throws CmisConstraintException if <code>this.isFolder() == false</code>
     */
    public JcrFolder asFolder() {
        if (isFolder()) {
            return (JcrFolder) this;
        }
        else {
            throw new CmisObjectNotFoundException("Not a folder: " + this);
        }
    }

    /**
     * @return  this instance as a <code>JcrVersionBase</code>
     * @throws CmisConstraintException if <code>this.isVersionable() == false</code>
     */
    public JcrVersionBase asVersion() {
        if (isVersionable()) {
            return (JcrVersionBase) this;
        }
        else {
            throw new CmisObjectNotFoundException("Not a version: " + this);
        }
    }

    /**
     * Factory method creating a new <code>JcrNode</code> from a node at a given JCR path.
     *
     * @param path  JCR path of the node
     * @return  A new instance representing the JCR node at <code>path</code>
     * @throws CmisObjectNotFoundException  if <code>path</code> does not identify a JCR node
     * @throws CmisRuntimeException
     */
    public JcrNode getNode(String path) {
        try {
            return create(node.getNode(path));
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
     * Compile the <code>ObjectData</code> for this node
     */
    public ObjectData compileObjectType(Set<String> filter, Boolean includeAllowableActions,
            ObjectInfoHandler objectInfos, boolean requiresObjectInfo) {

        try {
            ObjectDataImpl result = new ObjectDataImpl();
            ObjectInfoImpl objectInfo = new ObjectInfoImpl();

            PropertiesImpl properties = new PropertiesImpl();
            filter = filter == null ? null : new HashSet<String>(filter);
            compileProperties(properties, filter, objectInfo);
            result.setProperties(properties);
            if (filter != null && !filter.isEmpty()) {
                log.debug("Unknown filter properties: " + filter.toString());
            }

            if (Boolean.TRUE.equals(includeAllowableActions)) {
                result.setAllowableActions(getAllowableActions());
            }

            if (requiresObjectInfo) {
                objectInfo.setObject(result);
                objectInfos.addObjectInfo(objectInfo);
            }

            return result;
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.6 getAllowableActions
     */
    public AllowableActions getAllowableActions() {
        AllowableActionsImpl aas = new AllowableActionsImpl();
        aas.setAllowableActions(compileAllowableActions(new HashSet<Action>()));
        return aas;
    }

    /**
     * See CMIS 1.0 section 2.2.3.5 getObjectParents
     *
     * @return  parent of this object
     * @throws  CmisObjectNotFoundException  if this is the root folder
     * @throws  CmisRuntimeException
     */
    public JcrFolder getParent() {
        try {
            return create(node.getParent()).asFolder();
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

    /**
     * See CMIS 1.0 section 2.2.4.12 updateProperties
     *
     * @throws CmisStorageException
     */
    public JcrNode updateProperties(Properties properties) {
        // get and check the new name
        String newName = JcrConverter.toJcrName(PropertyHelper.getStringProperty(properties, PropertyIds.NAME));
        boolean rename = newName != null && !getName().equals(newName);
        if (rename && isRoot()) {
            throw new CmisUpdateConflictException("Cannot rename root node");
        }
        try {
            // rename file or folder if necessary
            Session session = getNode().getSession();
            Node newNode;
            if (rename) {
                String destPath = PathManager.createCmisPath(node.getParent().getPath(), newName);
                session.move(node.getPath(), destPath);
                newNode = session.getNode(destPath);
            }
            else {
                newNode = node;
            }

            // Are there properties to update?
            PropertyUpdater propertyUpdater = PropertyUpdater.create(typeManager, getTypeId(), properties);

            JcrVersionBase jcrVersion = isVersionable()
                    ? asVersion()
                    : null;

            // Update properties. Checkout if required
            boolean autoCheckout = false;
            if (!propertyUpdater.isEmpty()) {
                autoCheckout = jcrVersion != null && !jcrVersion.isCheckedOut();
                if (autoCheckout) {
                    jcrVersion.checkout();
                }

                // update the properties
                propertyUpdater.apply(node);
            }

            session.save();

            if (autoCheckout) {
                // auto versioning -> return new version created by checkin
                return jcrVersion.checkin(null, null, "auto checkout");
            }
            else if (jcrVersion != null && jcrVersion.isCheckedOut()) {
                // the node is checked out -> return pwc.
                JcrVersionBase jcrNewVersion = create(newNode).asVersion();
                return jcrNewVersion.getPwc();
            }
            else {
                // non versionable or not a new node -> return this
                return create(newNode);
            }
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }

    }

    /**
     * See CMIS 1.0 section 2.2.4.14 deleteObject
     *
     * @throws CmisRuntimeException
     */
    public void delete(boolean allVersions, boolean isPwc) {
        try {
            Session session = getNode().getSession();
            getNode().remove();
            session.save();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }

    /**
     * See CMIS 1.0 section 2.2.4.13 moveObject
     *
     * @throws CmisStorageException
     */
    public JcrNode move(JcrFolder parent) {
        try {
            // move it if target location is not same as source location
            String destPath = PathManager.createCmisPath(parent.getNode().getPath(), node.getName());
            String srcPath = node.getPath();
            Node newNode;
            if (srcPath.equals(destPath)) {
                newNode = node;
            }
            else {
                Session session = getNode().getSession();
                session.move(srcPath, destPath);
                newNode = session.getNode(destPath);
                session.save();
            }

            return create(newNode);
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            throw new CmisStorageException(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        try {
            return node.getPath();
        }
        catch (RepositoryException e) {
            log.debug(e.getMessage(), e);
            return e.getMessage();
        }
    }

    //------------------------------------------< protected >---

    /**
     * Retrieve the context node of the CMIS object represented by this instance. The
     * context node is the node which is used to derive the common properties from
     * (creation date, modification date, ...)
     *
     * @return  the context node
     * @throws RepositoryException
     */
    protected abstract Node getContextNode() throws RepositoryException;

    /**
     * @return  the value of the <code>cmis:baseTypeId</code> property
     */
    protected abstract BaseTypeId getBaseTypeId();

    /**
     * @return  the value of the <code>cmis:objectTypeId</code> property
     */
    protected abstract String getTypeIdInternal();

    /**
     * Compile the properties of the CMIS object represented by this instance.
     * See CMIS 1.0 section 2.2.4.7 getObject
     *
     * @param properties  compilation of properties
     * @param filter
     * @param objectInfo
     * @throws RepositoryException
     */
    protected void compileProperties(PropertiesImpl properties, Set<String> filter, ObjectInfoImpl objectInfo)
            throws RepositoryException {

        String typeId = getTypeIdInternal();
        BaseTypeId baseTypeId = getBaseTypeId();

        objectInfo.setBaseType(baseTypeId);
        objectInfo.setTypeId(typeId);
        objectInfo.setHasAcl(false);
        objectInfo.setVersionSeriesId(getVersionSeriesId());
        objectInfo.setRelationshipSourceIds(null);
        objectInfo.setRelationshipTargetIds(null);
        objectInfo.setRenditionInfos(null);
        objectInfo.setSupportsPolicies(false);
        objectInfo.setSupportsRelationships(false);

        // id
        String objectId = getObjectId();
        addPropertyId(properties, typeId, filter, PropertyIds.OBJECT_ID, objectId);
        objectInfo.setId(objectId);

        // name
        String name = getNodeName();
        addPropertyString(properties, typeId, filter, PropertyIds.NAME, name);
        objectInfo.setName(name);

        // base type and type name
        addPropertyId(properties, typeId, filter, PropertyIds.BASE_TYPE_ID, baseTypeId.value());
        addPropertyId(properties, typeId, filter, PropertyIds.OBJECT_TYPE_ID, typeId);

        // created and modified by
        String createdBy = getCreatedBy();
        addPropertyString(properties, typeId, filter, PropertyIds.CREATED_BY, createdBy);
        objectInfo.setCreatedBy(createdBy);

        addPropertyString(properties, typeId, filter, PropertyIds.LAST_MODIFIED_BY, getLastModifiedBy());

        // creation and modification date
        GregorianCalendar created = getCreated();
        addPropertyDateTime(properties, typeId, filter, PropertyIds.CREATION_DATE, created);
        objectInfo.setCreationDate(created);

        GregorianCalendar lastModified = getLastModified();
        addPropertyDateTime(properties, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
        objectInfo.setLastModificationDate(lastModified);

        addPropertyString(properties, typeId, filter, PropertyIds.CHANGE_TOKEN, getChangeToken());
    }

    /**
     * Compile the allowed actions on the CMIS object represented by this instance
     * See CMIS 1.0 section 2.2.4.6 getAllowableActions
     *
     * @param aas  compilation of allowed actions
     * @return
     */
    protected Set<Action> compileAllowableActions(Set<Action> aas) {
        setAction(aas, Action.CAN_GET_OBJECT_PARENTS, true);
        setAction(aas, Action.CAN_GET_PROPERTIES, true);
        setAction(aas, Action.CAN_UPDATE_PROPERTIES, true);
        setAction(aas, Action.CAN_MOVE_OBJECT, true);
        setAction(aas, Action.CAN_DELETE_OBJECT, true);
        setAction(aas, Action.CAN_GET_ACL, false);
        setAction(aas, Action.CAN_APPLY_ACL, false);
        setAction(aas, Action.CAN_GET_OBJECT_RELATIONSHIPS, false);
        setAction(aas, Action.CAN_ADD_OBJECT_TO_FOLDER, false);
        setAction(aas, Action.CAN_REMOVE_OBJECT_FROM_FOLDER, false);
        setAction(aas, Action.CAN_APPLY_POLICY, false);
        setAction(aas, Action.CAN_GET_APPLIED_POLICIES, false);
        setAction(aas, Action.CAN_REMOVE_POLICY, false);
        setAction(aas, Action.CAN_CREATE_RELATIONSHIP, false);
        return aas;
    }

    /**
     * @return  the change token of the CMIS object represented by this instance
     * @throws RepositoryException
     */
    protected String getChangeToken() throws RepositoryException {
        return null;
    }

    /**
     * @return  the last modifier of the CMIS object represented by this instance
     * @throws RepositoryException
     */
    protected String getLastModifiedBy() throws RepositoryException {
        return getPropertyOrElse(getContextNode(), Property.JCR_LAST_MODIFIED_BY, USER_UNKNOWN);
    }

    /**
     * @return  the last modification date of the CMIS object represented by this instance
     * @throws RepositoryException
     */
    protected GregorianCalendar getLastModified() throws RepositoryException {
        return getPropertyOrElse(getContextNode(), Property.JCR_LAST_MODIFIED, DATE_UNKNOWN);
    }

    /**
     * @return  the creation date of the CMIS object represented by this instance
     * @throws RepositoryException
     */
    protected GregorianCalendar getCreated() throws RepositoryException {
        return getPropertyOrElse(getContextNode(), Property.JCR_CREATED, DATE_UNKNOWN);
    }

    /**
     * @return  the creator of the CMIS object represented by this instance
     * @throws RepositoryException
     */
    protected String getCreatedBy() throws RepositoryException {
        return getPropertyOrElse(getContextNode(), Property.JCR_CREATED_BY, USER_UNKNOWN);
    }

    /**
     * @return  the name of the underlying JCR <code>node</code>.
     * @throws RepositoryException
     */
    protected String getNodeName() throws RepositoryException {
        return node.getName();
    }

    /**
     * @return  the object id of the CMIS object represented by this instance
     * @throws RepositoryException
     */
    protected String getObjectId() throws RepositoryException {
        return getVersionSeriesId();
    }

    /**
     * @return  the versions series id of the CMIS object represented by this instance
     * @throws RepositoryException
     */
    protected String getVersionSeriesId() throws RepositoryException {
        return node.getIdentifier();
    }

    /**
     * Factory method for creating a new <code>JcrNode</code> instance from a JCR <code>Node</code>
     *
     * @param node  the JCR <code>Node</code>
     * @return  a new <code>JcrNode</code>
     */
    protected final JcrNode create(Node node) {
        return typeHandlerManager.create(node);
    }

    /**
     * Add Id property to the CMIS object represented by this instance
     */
    protected final void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
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

    /**
     * Add string property to the CMIS object represented by this instance
     */
    protected final void addPropertyString(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyStringImpl prop = new PropertyStringImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Add integer property to the CMIS object represented by this instance
     */
    protected final void addPropertyInteger(PropertiesImpl props, String typeId, Set<String> filter, String id, long value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyIntegerImpl prop = new PropertyIntegerImpl(id, BigInteger.valueOf(value));
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Add boolean property to the CMIS object represented by this instance
     */
    protected final void addPropertyBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, boolean value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyBooleanImpl prop = new PropertyBooleanImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Add date-time property to the CMIS object represented by this instance
     */
    protected final void addPropertyDateTime(PropertiesImpl props, String typeId, Set<String> filter, String id,
            GregorianCalendar value) {

        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        PropertyDateTimeImpl prop = new PropertyDateTimeImpl(id, value);
        prop.setQueryName(id);
        props.addProperty(prop);
    }

    /**
     * Validate a set of properties against a filter and its definitions
     */
    protected final boolean checkAddProperty(Properties properties, String typeId, Set<String> filter, String id) {
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
     * Thunk for {@link JcrNode#updateProperties(Node, String, Properties)}
     */
    protected static final class PropertyUpdater {
        private final List<PropertyData<?>> removeProperties = new ArrayList<PropertyData<?>>();
        private final List<PropertyData<?>> updateProperties = new ArrayList<PropertyData<?>>();

        private PropertyUpdater() { }

        public static PropertyUpdater create(JcrTypeManager typeManager, String typeId, Properties properties) {
            if (properties == null) {
                throw new CmisConstraintException("No properties!");
            }

            // get the property definitions
            TypeDefinition type = typeManager.getType(typeId);
            if (type == null) {
                throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
            }

            PropertyUpdater propertyUpdater = new PropertyUpdater();
            // update properties
            for (PropertyData<?> prop : properties.getProperties().values()) {
                PropertyDefinition<?> propDef = type.getPropertyDefinitions().get(prop.getId());

                // do we know that property?
                if (propDef == null) {
                    throw new CmisInvalidArgumentException("Property '" + prop.getId() + "' is unknown!");
                }

                // skip content stream file name
                if (propDef.getId().equals(PropertyIds.CONTENT_STREAM_FILE_NAME)) {
                    log.warn("Cannot set " + PropertyIds.CONTENT_STREAM_FILE_NAME + ". Ignoring");
                    continue;
                }

                // silently skip name
                if (propDef.getId().equals(PropertyIds.NAME)) {
                    continue;
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
                newProp = PropertyHelper.isPropertyEmpty(prop)
                        ? PropertyHelper.getDefaultValue(propDef)
                        : prop;

                // Schedule for remove or update
                if (newProp == null) {
                    propertyUpdater.removeProperties.add(prop);
                }
                else {
                    propertyUpdater.updateProperties.add(newProp);
                }
            }

            return propertyUpdater;
        }

        public boolean isEmpty() {
            return removeProperties.isEmpty() && updateProperties.isEmpty();
        }

        public void apply(Node node) {
            try {
                for (PropertyData<?> prop: removeProperties) {
                    JcrConverter.removeProperty(node, prop);
                }
                for (PropertyData<?> prop: updateProperties) {
                    JcrConverter.setProperty(node, prop);
                }
            }
            catch (RepositoryException e) {
                log.debug(e.getMessage(), e);
                throw new CmisStorageException(e.getMessage(), e);
            }
        }
    }

    /**
     * Update the properties of the CMIS object represented by this instance
     */
    protected final void updateProperties(Node node, String typeId, Properties properties) {
        PropertyUpdater.create(typeManager, typeId, properties).apply(node);
    }

    /**
     * Utility function for retrieving the version history of a JCR <code>Node</code>.
     *
     * @param node  the node for which to retrieve the version history
     * @return  version history of <code>node</code>
     * @throws RepositoryException  if <code>node</code> is not versionable
     */
    protected static VersionHistory getVersionHistory(Node node) throws RepositoryException {
        return getVersionManager(node).getVersionHistory(node.getPath());
    }

    /**
     * Utility function for retrieving the version manager from a JCR <code>Node</code>.
     *
     * @param node
     * @return
     * @throws RepositoryException
     */
    protected static VersionManager getVersionManager(Node node) throws RepositoryException {
        return node.getSession().getWorkspace().getVersionManager();
    }

    /**
     * Utility function for retrieving the base version of a JCR <code>Node</code>.
     *
     * @param node  the node for which to retrieve the base version
     * @return  version base version of <code>node</code>
     * @throws RepositoryException  if <code>node</code> is not versionable
     */
    protected static Version getBaseVersion(Node node) throws RepositoryException {
        return getVersionManager(node).getBaseVersion(node.getPath());
    }

    /**
     * Utility function to retrieve the length of a property of a JCR <code>Node</code>.
     *
     * @param node
     * @param propertyName
     * @return
     * @throws RepositoryException
     */
    protected static long getPropertyLength(Node node, String propertyName) throws RepositoryException {
        return node.hasProperty(propertyName)
            ? node.getProperty(propertyName).getLength()
            : -1;
    }

    /**
     * Utility function for retrieving a string property from a JCR <code>Node</code> or a default
     * value in case of an error.
     *
     * @param node
     * @param propertyName
     * @param defaultValue
     * @return
     * @throws RepositoryException
     */
    protected static String getPropertyOrElse(Node node, String propertyName, String defaultValue)
            throws RepositoryException {

        return node.hasProperty(propertyName)
            ? node.getProperty(propertyName).getString()
            : defaultValue;
    }

    /**
     * Utility function for retrieving a date property from a JCR <code>Node</code> or a default
     * value in case of an error.
     *
     * @param node
     * @param propertyName
     * @param defaultValue
     * @return
     * @throws RepositoryException
     */
    protected static GregorianCalendar getPropertyOrElse(Node node, String propertyName, GregorianCalendar defaultValue)
            throws RepositoryException {

        if (node.hasProperty(propertyName)) {
            Calendar date = node.getProperty(propertyName).getDate();
            return Util.toCalendar(date);
        }
        else {
            return defaultValue;
        }
    }

    /**
     * Add <code>action</code> to <code>actions</code> iff <code>condition</code> is true.
     *
     * @param actions
     * @param action
     * @param condition
     */
    protected static void setAction(Set<Action> actions, Action action, boolean condition) {
        if (condition) {
            actions.add(action);
        }
        else {
            actions.remove(action);
        }
    }
}
