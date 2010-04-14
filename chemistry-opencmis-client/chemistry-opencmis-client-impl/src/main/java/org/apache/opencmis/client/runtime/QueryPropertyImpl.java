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
package org.apache.opencmis.client.runtime;

import java.io.Serializable;
import java.util.List;

import org.apache.opencmis.client.api.QueryProperty;

/**
 * Implementation of <code>QueryProperty</code>.
 */
public class QueryPropertyImpl<T> implements QueryProperty<T>, Serializable {

  private static final long serialVersionUID = 1L;

  private String id;
  private String queryName;
  private List<T> values;

  /**
   * Constructor.
   */
  public QueryPropertyImpl(String id, String queryName, List<T> values) {
    this.id = id;
    this.queryName = queryName;
    this.values = values;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryProperty#getId()
   */
  public String getId() {
    return this.id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryProperty#getQueryName()
   */
  public String getQueryName() {
    return this.queryName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryProperty#getValue()
   */
  public T getValue() {
    return this.values.get(0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.api.QueryProperty#getValues()
   */
  public List<T> getValues() {
    return this.values;
  }

}
