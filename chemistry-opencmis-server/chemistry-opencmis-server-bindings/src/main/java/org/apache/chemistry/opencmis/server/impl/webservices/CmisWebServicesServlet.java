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
package org.apache.chemistry.opencmis.server.impl.webservices;

import javax.servlet.ServletConfig;
import javax.xml.ws.WebServiceFeature;

import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;

import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

public class CmisWebServicesServlet extends WSServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected WSServletDelegate getDelegate(ServletConfig servletConfig) {
        WSServletDelegate delegate = super.getDelegate(servletConfig);

        // set temp directory and the threshold for all services with a
        // StreamingAttachment annotation
        if (delegate.adapters != null) {
            // get the CmisService factory
            CmisServiceFactory factory = (CmisServiceFactory) getServletContext().getAttribute(
                    CmisRepositoryContextListener.SERVICES_FACTORY);

            // iterate of all adapters
            for (ServletAdapter adapter : delegate.adapters) {
                WSFeatureList wsfl = adapter.getEndpoint().getBinding().getFeatures();
                for (WebServiceFeature ft : wsfl) {
                    if (ft instanceof StreamingAttachmentFeature) {
                        ((StreamingAttachmentFeature) ft).setDir(factory.getTempDirectory().getAbsolutePath());
                        ((StreamingAttachmentFeature) ft).setMemoryThreshold(factory.getMemoryThreshold());
                    }
                }
            }
        }

        return delegate;
    }
}
