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

import java.io.Serializable;
import java.util.List;

import org.apache.opencmis.client.api.Property;
import org.apache.opencmis.commons.api.PropertyDefinition;
import org.apache.opencmis.commons.enums.CmisProperties;
import org.apache.opencmis.commons.enums.PropertyType;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;

public class PersistentPropertyImpl<T> implements Property<T>, Serializable {

	/**
	 * serialization
	 */
	private static final long serialVersionUID = -6586532350183649719L;

	public PersistentPropertyImpl(PersistentSessionImpl session,
			CmisProperties cmisProperties, T value) {
	}

	public PersistentPropertyImpl(PersistentSessionImpl session,
			PropertyType type, T value) {
		throw new CmisRuntimeException("not implemented");
	}

	public PersistentPropertyImpl(PersistentSessionImpl session,
			PropertyType type, List<T> value) {
		throw new CmisRuntimeException("not implemented");
	}

	public PropertyDefinition<T> getDefinition() {
		throw new CmisRuntimeException("not implemented");
	}

	public String getDisplayName() {
		throw new CmisRuntimeException("not implemented");
	}

	public String getId() {
		throw new CmisRuntimeException("not implemented");
	}

	public String getLocalName() {
		throw new CmisRuntimeException("not implemented");
	}

	public String getQueryName() {
		throw new CmisRuntimeException("not implemented");
	}

	public PropertyType getType() {
		throw new CmisRuntimeException("not implemented");
	}

	public T getValue() {
		throw new CmisRuntimeException("not implemented");
	}

	public String getValueAsString() {
		throw new CmisRuntimeException("not implemented");
	}

	public List<T> getValues() {
		throw new CmisRuntimeException("not implemented");
	}

	public boolean isMultiValued() {
		throw new CmisRuntimeException("not implemented");
	}

}
