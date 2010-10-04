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
package org.apache.chemistry.opencmis.server.shared;

import java.math.BigInteger;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;

/**
 * Utility methods that are used by the AtomPub and Browser binding.
 */
public class HttpUtils {

    /**
     * Creates a {@link CallContext} object from a servlet request.
     */
    public static CallContext createContext(HttpServletRequest request, String binding,
            CallContextHandler callContextHandler) {
        String[] pathFragments = splitPath(request);

        String repositoryId = null;
        if (pathFragments.length > 0) {
            repositoryId = pathFragments[0];
        }

        CallContextImpl context = new CallContextImpl(binding, repositoryId, true);

        // call call context handler
        if (callContextHandler != null) {
            Map<String, String> callContextMap = callContextHandler.getCallContextMap(request);
            if (callContextMap != null) {
                for (Map.Entry<String, String> e : callContextMap.entrySet()) {
                    context.put(e.getKey(), e.getValue());
                }
            }
        }

        // decode range
        String rangeHeader = request.getHeader("Range");
        if (rangeHeader != null) {
            rangeHeader = rangeHeader.trim();
            BigInteger offset = null;
            BigInteger length = null;

            int eq = rangeHeader.indexOf('=');
            int ds = rangeHeader.indexOf('-');
            if ((eq > 0) && (ds > eq)) {
                String offsetStr = rangeHeader.substring(eq + 1, ds).trim();
                if (offsetStr.length() > 0) {
                    offset = new BigInteger(offsetStr);
                }

                if (ds < rangeHeader.length()) {
                    String lengthStr = rangeHeader.substring(ds + 1).trim();
                    if (lengthStr.length() > 0) {
                        if (offset == null) {
                            length = new BigInteger(lengthStr);
                        } else {
                            length = (new BigInteger(lengthStr)).subtract(offset);
                        }
                    }

                    if (offset != null) {
                        context.put(CallContext.OFFSET, offset.toString());
                    }
                    if (length != null) {
                        context.put(CallContext.LENGTH, length.toString());
                    }
                }
            }
        }

        return context;
    }

    /**
     * Splits the path into its fragments.
     */
    public static String[] splitPath(HttpServletRequest request) {
        String p = request.getPathInfo();
        if (p == null) {
            return new String[0];
        }

        return p.substring(1).split("/");
    }
}
