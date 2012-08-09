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
package org.apache.chemistry.opencmis.client.bindings.spi.browser;

import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.client.bindings.spi.BindingSession;
import org.apache.chemistry.opencmis.client.bindings.spi.http.HttpUtils;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;
import org.apache.chemistry.opencmis.commons.spi.PolicyService;

/**
 * Policy Service Browser Binding client.
 */
public class PolicyServiceImpl extends AbstractBrowserBindingService implements PolicyService {

    /**
     * Constructor.
     */
    public PolicyServiceImpl(BindingSession session) {
        setSession(session);
    }

    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        // build URL
        UrlBuilder url = getObjectUrl(repositoryId, objectId);

        // prepare form data
        final FormDataWriter formData = new FormDataWriter(Constants.CMISACTION_APPLY_POLICY);
        formData.addPoliciesParameters(Collections.singletonList(policyId));

        // send
        postAndConsume(url, formData.getContentType(), new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                formData.write(out);
            }
        });
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        // build URL
        UrlBuilder url = getObjectUrl(repositoryId, objectId);

        // prepare form data
        final FormDataWriter formData = new FormDataWriter(Constants.CMISACTION_REMOVE_POLICY);
        formData.addPoliciesParameters(Collections.singletonList(policyId));

        // send
        postAndConsume(url, formData.getContentType(), new HttpUtils.Output() {
            public void write(OutputStream out) throws Exception {
                formData.write(out);
            }
        });
    }

    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        // build URL
        UrlBuilder url = getObjectUrl(repositoryId, objectId, Constants.SELECTOR_POLICIES);
        url.addParameter(Constants.PARAM_FILTER, filter);
        url.addParameter(Constants.PARAM_SUCCINCT, getSuccinctParameter());

        // read and parse
        HttpUtils.Response resp = read(url);
        List<Object> json = parseArray(resp.getStream(), resp.getCharset());

        TypeCache typeCache = new ClientTypeCacheImpl(repositoryId, this);

        return JSONConverter.convertObjects(json, typeCache);
    }
}
