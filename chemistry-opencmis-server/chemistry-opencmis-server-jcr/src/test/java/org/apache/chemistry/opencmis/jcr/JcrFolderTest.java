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
 *
 */
package org.apache.chemistry.opencmis.jcr;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * The test covers org.apache.chemistry.opencmis.jcr.JcrRepository class.
 */
@Ignore
public class JcrFolderTest extends AbstractJcrSessionTest {

    /**
     * Test of the root folder
     */
    @Test
    public void testRootFolder(){
        ObjectData objData = getRootFolder();
        Assert.assertNotNull(objData);
    }

    /**
     * Test of the <code>PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS</code> of the CMIS object.
     */
    @Test
    public void testJcrTypeProperty(){
        getRootFolder().getProperties();
        PropertiesImpl properties = new PropertiesImpl();

        properties.addProperty(new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID, "cmis:folder"));
        properties.addProperty(new PropertyStringImpl(PropertyIds.NAME, "TestFolder"));
        String testFolderId = getJcrRepository().createFolder(getSession(), properties, getRootFolder().getId());
        ObjectData testFolder = getJcrRepository().getObject(getSession(), testFolderId, null, null, null, false);
        Assert.assertNotNull(testFolder.getProperties().getProperties()
                .get(PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS).getValues());
    }



}
