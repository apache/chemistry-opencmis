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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

import javax.xml.bind.annotation.XmlType;

import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDocumentDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeFolderDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypePolicyDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeRelationshipDefinitionType;

/**
 * Constants for AtomPub.
 */
public interface CmisAtomPubConstants {

    // service doc
    String TAG_SERVICE = "service";
    String TAG_WORKSPACE = "workspace";
    String TAG_REPOSITORY_INFO = "repositoryInfo";
    String TAG_COLLECTION = "collection";
    String TAG_COLLECTION_TYPE = "collectionType";
    String TAG_URI_TEMPLATE = "uritemplate";
    String TAG_TEMPLATE_TEMPLATE = "template";
    String TAG_TEMPLATE_TYPE = "type";
    String TAG_LINK = "link";

    // atom
    String TAG_ATOM_ID = "id";
    String TAG_ATOM_TITLE = "title";
    String TAG_ATOM_UPDATED = "updated";

    // feed
    String TAG_FEED = "feed";

    // entry
    String TAG_ENTRY = "entry";
    String TAG_OBJECT = "object";
    String TAG_NUM_ITEMS = "numItems";
    String TAG_PATH_SEGMENT = "pathSegment";
    String TAG_RELATIVE_PATH_SEGMENT = "relativePathSegment";
    String TAG_TYPE = "type";
    String TAG_CHILDREN = "children";
    String TAG_CONTENT = "content";
    String TAG_CONTENT_MEDIATYPE = "mediatype";
    String TAG_CONTENT_BASE64 = "base64";

    String ATTR_DOCUMENT_TYPE = CmisTypeDocumentDefinitionType.class.getAnnotation(XmlType.class).name();
    String ATTR_FOLDER_TYPE = CmisTypeFolderDefinitionType.class.getAnnotation(XmlType.class).name();
    String ATTR_RELATIONSHIP_TYPE = CmisTypeRelationshipDefinitionType.class.getAnnotation(XmlType.class).name();
    String ATTR_POLICY_TYPE = CmisTypePolicyDefinitionType.class.getAnnotation(XmlType.class).name();

    // allowable actions
    String TAG_ALLOWABLEACTIONS = "allowableActions";

    // ACL
    String TAG_ACL = "acl";

    // links
    String LINK_REL = "rel";
    String LINK_HREF = "href";
    String LINK_TYPE = "type";
    String CONTENT_SRC = "src";
}
