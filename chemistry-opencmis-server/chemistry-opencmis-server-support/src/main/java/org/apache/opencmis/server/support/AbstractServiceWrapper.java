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
package org.apache.opencmis.server.support;

import java.math.BigInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.enums.IncludeRelationships;
import org.apache.opencmis.commons.enums.RelationshipDirection;
import org.apache.opencmis.commons.enums.UnfileObjects;
import org.apache.opencmis.commons.enums.VersioningState;
import org.apache.opencmis.commons.exceptions.CmisBaseException;
import org.apache.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.opencmis.commons.provider.ContentStreamData;
import org.apache.opencmis.commons.provider.Holder;
import org.apache.opencmis.commons.provider.PropertiesData;

/**
 * Super class for service wrappers.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractServiceWrapper {

  private static final BigInteger MINUS_ONE = BigInteger.valueOf(-1);

  private static final Log log = LogFactory.getLog(AbstractServiceWrapper.class);

  private BigInteger fDefaultMaxItems = null;
  private BigInteger fDefaultDepth = MINUS_ONE;

  /**
   * Set the default maxItems.
   */
  protected void setDefaultMaxItems(BigInteger defaultMaxItems) {
    fDefaultMaxItems = defaultMaxItems;
  }

  /**
   * Set the default depth.
   */
  protected void setDefaultDepth(BigInteger defaultDepth) {
    fDefaultDepth = defaultDepth;
  }

  /**
   * Converts the given exception into a CMIS exception.
   */
  protected CmisBaseException createCmisException(Exception e) {
    if (e == null) {
      // should never happen
      // if it happens its the fault of the framework...

      return new CmisRuntimeException("Unknown exception!");
    }
    else if (e instanceof CmisBaseException) {
      return (CmisBaseException) e;
    }
    else {
      // should not happen if the connector works correctly
      // it's alarming enough to log the exception
      log.warn(e);

      return new CmisRuntimeException(e.getMessage(), e);
    }
  }

  /**
   * Throws an exception if the given id is <code>null</code> or empty.
   */
  protected void checkId(String name, String id) {
    if (id == null) {
      throw new CmisInvalidArgumentException(name + " must be set!");
    }

    if (id.length() == 0) {
      throw new CmisInvalidArgumentException(name + " must not be empty!");
    }
  }

  /**
   * Throws an exception if the given holder or id is <code>null</code> or empty.
   */
  protected void checkHolderId(String name, Holder<String> holder) {
    if (holder == null) {
      throw new CmisInvalidArgumentException(name + " must be set!");
    }

    checkId(name, holder.getValue());
  }

  /**
   * Throws an exception if the repository id is <code>null</code> or empty.
   */
  protected void checkRepositoryId(String repositoryId) {
    checkId("Repository Id", repositoryId);
  }

  /**
   * Throws an exception if the given path is <code>null</code> or invalid.
   */
  protected void checkPath(String name, String path) {
    if (path == null) {
      throw new CmisInvalidArgumentException(name + " must be set!");
    }

    if (path.length() == 0) {
      throw new CmisInvalidArgumentException(name + " must not be empty!");
    }

    if (path.charAt(0) != '/') {
      throw new CmisInvalidArgumentException(name + " must start with '/'!");
    }
  }

  /**
   * Throws an exception if the given properties set is <code>null</code>.
   */
  protected void checkProperties(PropertiesData properties) {
    if (properties == null) {
      throw new CmisInvalidArgumentException("Properties must be set!");
    }
  }

  /**
   * Throws an exception if the given content object is <code>null</code>.
   */
  protected void checkContentStream(ContentStreamData content) {
    if (content == null) {
      throw new CmisInvalidArgumentException("Content must be set!");
    }
  }

  /**
   * Throws an exception if the given query statement is <code>null</code> or empty.
   */
  protected void checkQueryStatement(String statement) {
    if (statement == null) {
      throw new CmisInvalidArgumentException("Statement must be set!");
    }

    if (statement.length() == 0) {
      throw new CmisInvalidArgumentException("Statement must not be empty!");
    }
  }

  /**
   * Returns <code>true<code> if <code>value</code> is <code>null</code>.
   */
  protected Boolean getDefaultTrue(Boolean value) {
    if (value == null) {
      return Boolean.TRUE;
    }

    return value;
  }

  /**
   * Returns <code>false<code> if <code>value</code> is <code>null</code>.
   */
  protected Boolean getDefaultFalse(Boolean value) {
    if (value == null) {
      return Boolean.FALSE;
    }

    return value;
  }

  /**
   * Returns the <code>IncludeRelationships.NONE<code> if <code>value</code> is <code>null</code>.
   */
  protected IncludeRelationships getDefault(IncludeRelationships value) {
    if (value == null) {
      return IncludeRelationships.NONE;
    }

    return value;
  }

  /**
   * Returns the <code>VersioningState.MAJOR<code> if <code>value</code> is <code>null</code>.
   */
  protected VersioningState getDefault(VersioningState value) {
    if (value == null) {
      return VersioningState.MAJOR;
    }

    return value;
  }

  /**
   * Returns the <code>UnfileObjects.DELETE<code> if <code>value</code> is <code>null</code>.
   */
  protected UnfileObjects getDefault(UnfileObjects value) {
    if (value == null) {
      return UnfileObjects.DELETE;
    }

    return value;
  }

  /**
   * Returns the <code>AclPropagation.REPOSITORYDETERMINED<code> if <code>value</code> is
   * <code>null</code>.
   */
  protected AclPropagation getDefault(AclPropagation value) {
    if (value == null) {
      return AclPropagation.REPOSITORYDETERMINED;
    }

    return value;
  }

  /**
   * Returns the <code>RelationshipDirection.SOURCE<code> if <code>value</code> is <code>null</code>
   * .
   */
  protected RelationshipDirection getDefault(RelationshipDirection value) {
    if (value == null) {
      return RelationshipDirection.SOURCE;
    }

    return value;
  }

  /**
   * Returns the <code>"cmis:none"<code> if <code>value</code> is <code>null</code>.
   */
  protected String getDefaultRenditionFilter(String value) {
    if ((value == null) || (value.length() == 0)) {
      return "cmis:none";
    }

    return value;
  }

  /**
   * Returns the default maxItems if <code>maxItems</code> == <code>null</code>, throws an exception
   * if <code>maxItems</code> &lt; 0, returns <code>maxItems</code> otherwise.
   */
  protected BigInteger getMaxItems(BigInteger maxItems) {
    if (maxItems == null) {
      return fDefaultMaxItems;
    }

    if (maxItems.compareTo(BigInteger.ZERO) == -1) {
      throw new CmisInvalidArgumentException("maxItems must not be negative!");
    }

    return maxItems;
  }

  /**
   * Returns 0 if <code>skipCount</code> == <code>null</code>, throws an exception if
   * <code>skipCount</code> &lt; 0, returns <code>skipCount</code> otherwise.
   */
  protected BigInteger getSkipCount(BigInteger skipCount) {
    if (skipCount == null) {
      return BigInteger.ZERO;
    }

    if (skipCount.compareTo(BigInteger.ZERO) == -1) {
      throw new CmisInvalidArgumentException("skipCount must not be negative!");
    }

    return skipCount;
  }

  /**
   * Checks the depth parameter if it complies with CMIS specification and returns the default value
   * if <code>depth</code> is <code>null</code>.
   */
  protected BigInteger getDepth(BigInteger depth) {
    if (depth == null) {
      return fDefaultDepth;
    }

    if (depth.compareTo(BigInteger.ZERO) == 0) {
      throw new CmisInvalidArgumentException("depth must not be 0!");
    }

    if (depth.compareTo(MINUS_ONE) == -1) {
      throw new CmisInvalidArgumentException("depth must not be <-1!");
    }

    return depth;
  }

  /**
   * Throws an exception if the given value is negative.
   */
  protected void checkNullOrPositive(String name, BigInteger value) {
    if (value == null) {
      return;
    }

    if (value.compareTo(BigInteger.ZERO) == -1) {
      throw new CmisInvalidArgumentException(name + " must not be negative!");
    }
  }
}
