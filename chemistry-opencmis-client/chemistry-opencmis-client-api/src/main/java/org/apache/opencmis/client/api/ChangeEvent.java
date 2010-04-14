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
package org.apache.opencmis.client.api;

import java.util.List;

import org.apache.opencmis.commons.enums.TypeOfChanges;

/**
 * Change Event from the change log.
 * 
 * @see Session#getContentChanges(String, StringBuffer)
 * 
 *      See CMIS Domain Model - section 2.1.11.
 */
public interface ChangeEvent {

  String getObjectId();

  /**
   * Get the type of the change.
   * 
   * @return the type of change
   */
  TypeOfChanges getChangeType();

  /**
   * For change events with change type "updated": The list of properties now applied to the object.
   * 
   * @return the list with the new properties, might be {@code null}
   */
  List<Property<?>> getNewProperties();

}
