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

import java.io.Serializable;

import org.apache.opencmis.client.api.CmisObject;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;

public class CacheImpl implements Cache, Serializable {

	/**
	 * serialization
	 */
	private static final long serialVersionUID = 1978445442452564094L;

	public boolean containsId(String objectId){
		throw new CmisRuntimeException("not implemented");
	}

	public void clear() {
		throw new CmisRuntimeException("not implemented");
	}

	public boolean containsPath(String path) {
		throw new CmisRuntimeException("not implemented");
	}

	public CmisObject get(String objectId) {
		throw new CmisRuntimeException("not implemented");
	}

	public CmisObject getByPath(String path) {
		throw new CmisRuntimeException("not implemented");
	}

	public void put(CmisObject object) {
		throw new CmisRuntimeException("not implemented");
	}
	
}
