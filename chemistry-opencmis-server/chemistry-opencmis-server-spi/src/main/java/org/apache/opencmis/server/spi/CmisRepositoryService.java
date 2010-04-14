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
package org.apache.opencmis.server.spi;

import java.math.BigInteger;
import java.util.List;

import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.api.TypeDefinition;
import org.apache.opencmis.commons.api.TypeDefinitionContainer;
import org.apache.opencmis.commons.api.TypeDefinitionList;
import org.apache.opencmis.commons.provider.RepositoryInfoData;

/**
 * CMIS Repository Service interface. Please refer to the CMIS specification and the OpenCMIS
 * documentation for details.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public interface CmisRepositoryService {

  /**
   * Gets all repository infos.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  List<RepositoryInfoData> getRepositoryInfos(CallContext context, ExtensionsData extension);

  /**
   * Gets the repository info of the specified repository.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  RepositoryInfoData getRepositoryInfo(CallContext context, String repositoryId,
      ExtensionsData extension);

  /**
   * Gets the children of the given type.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  TypeDefinitionList getTypeChildren(CallContext context, String repositoryId, String typeId,
      Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount,
      ExtensionsData extension);

  /**
   * Gets the descendants of the given type.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  List<TypeDefinitionContainer> getTypeDescendants(CallContext context, String repositoryId,
      String typeId, BigInteger depth, Boolean includePropertyDefinitions, ExtensionsData extension);

  /**
   * Gets the type definition of the given type. It must return a valid type or throw an exception.
   * 
   * <p>
   * Bindings: AtomPub, Web Services
   * </p>
   */
  TypeDefinition getTypeDefinition(CallContext context, String repositoryId, String typeId,
      ExtensionsData extension);
}
