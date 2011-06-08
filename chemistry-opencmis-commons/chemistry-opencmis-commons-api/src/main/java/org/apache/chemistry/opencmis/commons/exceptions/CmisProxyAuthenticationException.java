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
package org.apache.chemistry.opencmis.commons.exceptions;

import java.math.BigInteger;

/**
 * Proxy authentication exception.
 * 
 * (This is exception is not defined in the CMIS specification and is therefore
 * derived from {@link CmisRuntimeException}.)
 */
public class CmisProxyAuthenticationException extends CmisRuntimeException {

    private static final long serialVersionUID = 1L;

    public CmisProxyAuthenticationException() {
        super();
    }

    public CmisProxyAuthenticationException(String message, BigInteger code, Throwable cause) {
        super(message, code, cause);
    }

    public CmisProxyAuthenticationException(String message, String errorContent) {
        super(message, errorContent);
    }

    public CmisProxyAuthenticationException(String message, BigInteger code) {
        super(message, code);
    }

    public CmisProxyAuthenticationException(String message, BigInteger code, String errorContent) {
        super(message, code, errorContent);
    }

    public CmisProxyAuthenticationException(String message, String errorContent, Throwable cause) {
        super(message, errorContent, cause);
    }

    public CmisProxyAuthenticationException(String message, Throwable cause) {
        super(message, BigInteger.ZERO, cause);
    }

    public CmisProxyAuthenticationException(String message) {
        super(message, BigInteger.ZERO);
    }

}
