/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.chemistry.opencmis.server.support;

import java.math.BigInteger;

import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;

/**
 * Service wrapper.
 * 
 * @deprecated use {@link ConformanceCmisServiceWrapper} instead
 */
@Deprecated
public class CmisServiceWrapper<T extends CmisService> extends ConformanceCmisServiceWrapper {

    /**
     * Constructor.
     */
    public CmisServiceWrapper(T service, BigInteger defaultTypesMaxItems, BigInteger defaultTypesDepth,
            BigInteger defaultMaxItems, BigInteger defaultDepth) {
        super(service, defaultTypesMaxItems, defaultTypesDepth, defaultMaxItems, defaultDepth);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getWrappedService() {
        return (T) super.getWrappedService();
    }
}
