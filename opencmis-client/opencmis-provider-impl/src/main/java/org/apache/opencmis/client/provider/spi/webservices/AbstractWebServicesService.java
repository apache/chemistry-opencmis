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
package org.apache.opencmis.client.provider.spi.webservices;

import java.math.BigInteger;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.commons.exceptions.CmisBaseException;
import org.apache.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.exceptions.CmisStorageException;
import org.apache.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.opencmis.commons.impl.jaxb.CmisException;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractWebServicesService {

  private Session fSession;

  /**
   * Sets the current session.
   */
  protected void setSession(Session session) {
    fSession = session;
  }

  /**
   * Gets the current session.
   */
  protected Session getSession() {
    return fSession;
  }

  /**
   * Converts a Web Services Exception into a CMIS Client exception.
   */
  protected CmisBaseException convertException(CmisException ex) {
    if ((ex == null) || (ex.getFaultInfo() == null)) {
      return new CmisRuntimeException("CmisException has no fault!");
    }

    String msg = ex.getFaultInfo().getMessage();
    BigInteger code = ex.getFaultInfo().getCode();

    switch (ex.getFaultInfo().getType()) {
    case CONSTRAINT:
      return new CmisConstraintException(msg, code);
    case CONTENT_ALREADY_EXISTS:
      return new CmisContentAlreadyExistsException(msg, code);
    case FILTER_NOT_VALID:
      return new CmisFilterNotValidException(msg, code);
    case INVALID_ARGUMENT:
      return new CmisInvalidArgumentException(msg, code);
    case NAME_CONSTRAINT_VIOLATION:
      return new CmisNameConstraintViolationException(msg, code);
    case NOT_SUPPORTED:
      return new CmisNotSupportedException(msg, code);
    case OBJECT_NOT_FOUND:
      return new CmisObjectNotFoundException(msg, code);
    case PERMISSION_DENIED:
      return new CmisPermissionDeniedException(msg, code);
    case RUNTIME:
      return new CmisRuntimeException(msg, code);
    case STORAGE:
      return new CmisStorageException(msg, code);
    case STREAM_NOT_SUPPORTED:
      return new CmisStreamNotSupportedException(msg, code);
    case UPDATE_CONFLICT:
      return new CmisUpdateConflictException(msg, code);
    case VERSIONING:
      return new CmisVersioningException(msg, code);
    }

    return new CmisRuntimeException("Unknown exception[" + ex.getFaultInfo().getType().value()
        + "]: " + msg);
  }
}
