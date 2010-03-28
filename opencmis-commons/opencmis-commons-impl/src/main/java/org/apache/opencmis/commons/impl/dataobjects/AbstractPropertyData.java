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
package org.apache.opencmis.commons.impl.dataobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.opencmis.commons.provider.PropertyData;

/**
 * Abstract property data implementation.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public abstract class AbstractPropertyData<T> extends AbstractExtensionData implements
    PropertyData<T> {

  private String fId;
  private String fDisplayName;
  private String fLocalName;
  private String fQueryName;

  private List<T> fValues;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyData#getId()
   */
  public String getId() {
    return fId;
  }

  public void setId(String id) {
    fId = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyData#getDisplayName()
   */
  public String getDisplayName() {
    return fDisplayName;
  }

  public void setDisplayName(String displayName) {
    fDisplayName = displayName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyData#getLocalName()
   */
  public String getLocalName() {
    return fLocalName;
  }

  public void setLocalName(String localName) {
    fLocalName = localName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyData#getQueryName()
   */
  public String getQueryName() {
    return fQueryName;
  }

  public void setQueryName(String queryName) {
    fQueryName = queryName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyData#getValues()
   */
  public List<T> getValues() {
    return fValues;
  }

  public void setValues(List<T> values) {
    if (values == null) {
      fValues = Collections.emptyList();
    }
    else {
      fValues = values;
    }
  }

  public void setValue(T value) {
    if (value == null) {
      fValues = Collections.emptyList();
    }
    else {
      fValues = new ArrayList<T>(1);
      fValues.add(value);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.PropertyData#getFirstValue()
   */
  public T getFirstValue() {
    if ((fValues != null) && (!fValues.isEmpty())) {
      return fValues.get(0);
    }

    return null;
  }

  @Override
  public String toString() {
    return "Property [id=" + fId + ", display Name=" + fDisplayName + ", local name=" + fLocalName
        + ", query name=" + fQueryName + ", values=" + fValues + "]" + super.toString();
  }
}
