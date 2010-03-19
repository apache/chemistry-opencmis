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
package org.apache.opencmis.client.runtime;

import junit.framework.Assert;

import org.apache.opencmis.client.api.Folder;
import org.junit.Test;

/**
 * Testing session
 */
public class ReadOnlySessionTest extends AbstractSessionTest {

	@Test
	public void testSession() {
		Assert.assertNotNull(this.session.getDefaultContext());
		Assert.assertNotNull(this.session.getLocale());
		Assert.assertNotNull(this.session.getObjectFactory());
		Assert.assertNotNull(this.session.getRepositoryInfo());
	}

	@Test
	public void testSessionObjectAccess() {
		Folder root = this.session.getRootFolder();
		Assert.assertNotNull(root);
		String id = root.getId();
	}

	@Test
	public void testSessionClear() {
		this.session.clear();
	}

}
