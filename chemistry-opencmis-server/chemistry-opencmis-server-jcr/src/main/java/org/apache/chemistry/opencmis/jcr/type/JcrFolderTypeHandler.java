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
package org.apache.chemistry.opencmis.jcr.type;

import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.jcr.JcrFolder;

import javax.jcr.Node;

/**
 * Implemented by type handlers that provides a type that is or inherits from cmis:folder.
 */
public interface JcrFolderTypeHandler extends JcrTypeHandler {

    JcrFolder getJcrNode(Node node);

    /**
     * See CMIS 1.0 section 2.2.4.3 createFolder
     *
     * @throws org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException
     *
     */
    JcrFolder createFolder(JcrFolder parentFolder, String name, Properties properties);
}
