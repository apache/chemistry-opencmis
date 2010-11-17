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
package org.apache.chemistry.opencmis.commons.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * This class represents one node in the extension tree.
 */
public interface CmisExtensionElement extends Serializable {

    /**
     * Returns the name of the extension. The name is never <code>null</code>.
     */
    String getName();

    /**
     * Returns the namespace of the extension. If the binding doesn't support
     * namespaces this method will return <code>null</code>.
     * 
     * Don't rely on namespaces because they are binding specific!
     */
    String getNamespace();

    /**
     * Returns the value of the extension as a String. If this extension has
     * children than this method returns <code>null</code>.
     */
    String getValue();

    /**
     * Returns the attributes of the extension. If the binding doesn't support
     * attributes this method will return <code>null</code>.
     * 
     * Try to avoid attributes because they are binding specific!
     */
    Map<String, String> getAttributes();

    /**
     * Returns the children of this extension.
     */
    List<CmisExtensionElement> getChildren();
}
