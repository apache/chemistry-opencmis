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
package org.apache.chemistry.opencmis.tck.tests.basics;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.util.Locale;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class SecurityTest extends AbstractSessionTest {

    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Security Test");
        setDescription("Checks if HTTPS is used.");
    }

    @Override
    public void run(Session session) throws Exception {
        CmisTestResult f;

        BindingType binding = getBinding();

        addResult(createInfoResult("Binding: " + binding));

        f = createResult(WARNING, "HTTPS is not used. Credentials might be transferred as plain text!");

        switch (binding) {
        case ATOMPUB:
            if (!isHttpsUrl(getParameters().get(SessionParameter.ATOMPUB_URL))) {
                addResult(f);
            }
            break;
        case WEBSERVICES:
            if (!isHttpsUrl(getParameters().get(SessionParameter.WEBSERVICES_REPOSITORY_SERVICE))) {
                addResult(f);
            }
            break;
        default:
            // nothing to do
        }
    }

    private static boolean isHttpsUrl(String url) {
        if (url == null) {
            return false;
        }

        return url.trim().toLowerCase(Locale.ENGLISH).startsWith("https://");
    }
}
