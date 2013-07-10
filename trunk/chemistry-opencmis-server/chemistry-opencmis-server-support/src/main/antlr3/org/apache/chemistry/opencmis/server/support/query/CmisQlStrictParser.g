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
 *
*/

parser grammar CmisQlStrictParser;

// Note: ANTLR is very sensitive to compilation errors, you must
// have first options, then import and then @header.
// @header must only be in derived grammars not in base grammars
// no package declarations in base grammars


options {
    tokenVocab = CmisQlStrictLexer;
    output = AST;
}

import CmisBaseGrammar;

@header {
/*
 * THIS FILE IS AUTO-GENERATED, DO NOT EDIT.
 *
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
 *
 * Authors:
 *     Stefane Fermigier, Nuxeo
 *     Florent Guillaume, Nuxeo
 *
 * THIS FILE IS AUTO-GENERATED, DO NOT EDIT.
 */
package org.apache.chemistry.opencmis.server.support.query;
}

@members {
    public boolean hasErrors() {
    	return gCmisBaseGrammar.hasErrors();
    }

	public String getErrorMessages() {
    	return gCmisBaseGrammar.getErrorMessages();
	}
}

  // Rules can't be empty so we have one dummy rule here
root : query;
