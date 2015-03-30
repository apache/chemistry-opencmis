/*
 *
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
 *
 */
package org.apache.chemistry.opencmis.server.support.wrapper;

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.junit.Test;

public class CmisServiceWrapperManagerTest {

    @Test
    public void testWrapperManager() {
        CmisServiceWrapperManager manager = new CmisServiceWrapperManager();

        manager.addOuterWrapper(SimpleLoggingCmisServiceWrapper.class);
        manager.addOuterWrapper(ConformanceCmisServiceWrapper.class, -1, 100, -1, 1000);
        manager.addOuterWrapper(SimpleLoggingCmisServiceWrapper.class, "outer");
        manager.addInnerWrapper(SimpleLoggingCmisServiceWrapper.class, "inner");

        DummyService service = new DummyService();
        CmisService wrapperedService = manager.wrap(service);
        assertTrue(wrapperedService instanceof AbstractCmisServiceWrapper);

        AbstractCmisServiceWrapper wrapper = (AbstractCmisServiceWrapper) wrapperedService;
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof ConformanceCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        assertTrue(wrapper.getWrappedService() instanceof DummyService);

        // removing outer wrapper
        manager.removeOuterWrapper();

        service = new DummyService();
        wrapperedService = manager.wrap(service);

        assertTrue(wrapperedService instanceof AbstractCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapperedService;
        assertTrue(wrapper instanceof ConformanceCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        assertTrue(wrapper.getWrappedService() instanceof DummyService);

        // removing inner wrapper
        manager.removeInnerWrapper();

        service = new DummyService();
        wrapperedService = manager.wrap(service);

        assertTrue(wrapperedService instanceof AbstractCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapperedService;
        assertTrue(wrapper instanceof ConformanceCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        assertTrue(wrapper.getWrappedService() instanceof DummyService);
    }

    @Test
    public void testWrapperManagerFromParameters() {
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("dummy", "something");

        parameters.put("servicewrapper.1", SimpleLoggingCmisServiceWrapper.class.getName() + ",1");
        parameters.put("servicewrapper.2", ConformanceCmisServiceWrapper.class.getName() + ",2");
        parameters.put("servicewrapper.3", SimpleLoggingCmisServiceWrapper.class.getName() + ",3");
        parameters.put("servicewrapper.4", SimpleLoggingCmisServiceWrapper.class.getName() + ",4");

        CmisServiceWrapperManager manager = new CmisServiceWrapperManager();
        manager.addWrappersFromServiceFactoryParameters(parameters);

        DummyService service = new DummyService();
        CmisService wrapperedService = manager.wrap(service);
        assertTrue(wrapperedService instanceof AbstractCmisServiceWrapper);

        AbstractCmisServiceWrapper wrapper = (AbstractCmisServiceWrapper) wrapperedService;
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof ConformanceCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        assertTrue(wrapper.getWrappedService() instanceof DummyService);

        // removing outer wrapper
        manager.removeOuterWrapper();

        service = new DummyService();
        wrapperedService = manager.wrap(service);

        assertTrue(wrapperedService instanceof AbstractCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapperedService;
        assertTrue(wrapper instanceof ConformanceCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        assertTrue(wrapper.getWrappedService() instanceof DummyService);

        // removing inner wrapper
        manager.removeInnerWrapper();

        service = new DummyService();
        wrapperedService = manager.wrap(service);

        assertTrue(wrapperedService instanceof AbstractCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapperedService;
        assertTrue(wrapper instanceof ConformanceCmisServiceWrapper);

        wrapper = (AbstractCmisServiceWrapper) wrapper.getWrappedService();
        assertTrue(wrapper instanceof SimpleLoggingCmisServiceWrapper);

        assertTrue(wrapper.getWrappedService() instanceof DummyService);
    }

    private static class DummyService extends AbstractCmisService {

        @Override
        public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
            return null;
        }

        @Override
        public TypeDefinitionList getTypeChildren(String repositoryId, String typeId,
                Boolean includePropertyDefinitions, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
            return null;
        }

        @Override
        public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
            return null;
        }

        @Override
        public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
                Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
            return null;
        }

        @Override
        public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
                Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                Boolean includeRelativePathSegment, ExtensionsData extension) {
            return null;
        }

        @Override
        public ObjectData getObject(String repositoryId, String objectId, String filter,
                Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
                Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
            return null;
        }
    }
}
