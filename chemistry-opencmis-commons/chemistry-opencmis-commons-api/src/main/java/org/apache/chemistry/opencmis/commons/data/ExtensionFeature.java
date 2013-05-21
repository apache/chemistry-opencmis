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
import java.util.Map;

/**
 * Representation of an extension feature.
 * 
 * @cmis 1.1
 */
public interface ExtensionFeature extends Serializable, ExtensionsData {

    /**
     * Returns the unique feature id.
     * 
     * @return feature id
     * 
     * @cmis 1.1
     */
    String getId();

    /**
     * Returns a URL that provides more information about the feature.
     * 
     * @return feature URL
     * 
     * @cmis 1.1
     */
    String getUrl();

    /**
     * Returns a human-readable name for the feature.
     * 
     * @return feature name
     * 
     * @cmis 1.1
     */
    String getCommonName();

    /**
     * Returns a feature version label.
     * 
     * @return feature version label
     * 
     * @cmis 1.1
     */
    String getVersionLabel();

    /**
     * Returns a human-readable description of the feature.
     * 
     * @return feature description
     * 
     * @cmis 1.1
     */
    String getDescription();

    /**
     * Returns extra feature data.
     * 
     * @return key-value pairs of extra data
     * 
     * @cmis 1.1
     */
    Map<String, String> getFeatureData();
}
