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
 * Base exception class for all CMIS client exceptions.
 */
public abstract class CmisBaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Error code used by the Web Services binding. */
    private BigInteger code = BigInteger.ZERO;

    /** Content the of the error page returned by the AtomPub server. */
    private String errorContent;

    /**
     * Default constructor.
     */
    protected CmisBaseException() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param message
     *            error message
     * @param code
     *            error code
     * @param cause
     *            the cause
     */
    protected CmisBaseException(String message, BigInteger code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Constructor.
     * 
     * @param message
     *            error message
     * @param errorContent
     *            error page content
     * @param cause
     *            the cause
     */
    protected CmisBaseException(String message, String errorContent, Throwable cause) {
        super(message, cause);
        this.errorContent = errorContent;
    }

    /**
     * Constructor.
     * 
     * @param message
     *            error message
     * @param code
     *            error code
     */
    protected CmisBaseException(String message, BigInteger code) {
        super(message);
        this.code = code;
    }

    /**
     * Constructor.
     * 
     * @param message
     *            error message
     * @param code
     *            error code
     * @param errorContent
     *            error page content
     */
    protected CmisBaseException(String message, BigInteger code, String errorContent) {
        super(message);
        this.code = code;
        this.errorContent = errorContent;
    }

    /**
     * Constructor.
     * 
     * @param message
     *            error message
     * @param errorContent
     *            error page content
     */
    protected CmisBaseException(String message, String errorContent) {
        super(message);
        this.errorContent = errorContent;
    }

    /**
     * Constructor.
     * 
     * @param message
     *            error message
     * @param cause
     *            the cause
     */
    protected CmisBaseException(String message, Throwable cause) {
        this(message, (BigInteger) null, cause);
    }

    /**
     * Constructor.
     * 
     * @param message
     *            error message
     */
    protected CmisBaseException(String message) {
        this(message, (BigInteger) null);
    }

    /**
     * Returns the error code sent by the CMIS repository (Web Services binding
     * only).
     * 
     * @return error code or <code>null</code> if the CMIS repository didn't
     *         send an error code or the binding doesn't support error codes.
     */
    public BigInteger getCode() {
        return code;
    }

    /**
     * Returns the content of the error page sent by the web server (AtomPub
     * binding only).
     * 
     * @return the content of the error page or <code>null</code> if the server
     *         didn't send text content.
     */
    public String getErrorContent() {
        return errorContent;
    }

    /**
     * Returns the name of the exception as defined in the CMIS specification.
     */
    public abstract String getExceptionName();
}
