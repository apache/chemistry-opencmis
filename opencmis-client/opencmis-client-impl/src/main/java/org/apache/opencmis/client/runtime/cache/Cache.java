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
package org.apache.opencmis.client.runtime.cache;

import java.util.List;

import org.apache.opencmis.client.api.CmisObject;

/**
 * Implements a session cache providing following capabilities:
 * <p>
 * <lu>
 * <li>access CmisObject by object id</li>
 * <li>access CmisObject by object path</li>
 * <li>access CmisObjects by paging parameter</li>
 * </lu>
 */
public interface Cache {

	boolean containsId(String objectId);
	boolean containsPath(String path);

	void put(CmisObject object);
	// public void put(List<CmisObject> pageRange, int pageNumber);
	
	CmisObject get(String objectId);
	CmisObject getByPath(String path);
	// public List<CmisObject> get(int pageNumber);
	
	void clear();
}
