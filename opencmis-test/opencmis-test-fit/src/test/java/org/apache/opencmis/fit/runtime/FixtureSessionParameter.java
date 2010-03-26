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
package org.apache.opencmis.fit.runtime;

public interface FixtureSessionParameter {

	/*
	 * session parameter
	 */

	/**
	 * CONFIG_PATH is referenced in pom.xml! If you change here then change pom.
	 */
	static final String CONFIG_PATH =         "org.apache.opencmis.fit.runtime.config.path";

	static final String SESSION_FACTORY =     "org.apache.opencmis.fit.runtime.session.factory";
	static final String TEST_ROOT_FOLDER_ID = "org.apache.opencmis.fit.runtime.root.folder.id";

}
