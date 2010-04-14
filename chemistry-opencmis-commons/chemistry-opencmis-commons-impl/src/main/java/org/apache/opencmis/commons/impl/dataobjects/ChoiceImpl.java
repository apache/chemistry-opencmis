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

import java.util.List;

import org.apache.opencmis.commons.api.Choice;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class ChoiceImpl<T> extends AbstractExtensionData implements Choice<T> {

  private String fDisplayName;
  private List<T> fValue;
  private List<Choice<T>> fChoice;

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.Choice#getDisplayName()
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
   * @see org.apache.opencmis.client.provider.Choice#getValue()
   */
  public List<T> getValue() {
    return fValue;
  }

  public void setValue(List<T> value) {
    fValue = value;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.Choice#getChoice()
   */
  public List<Choice<T>> getChoice() {
    return fChoice;
  }

  public void setChoice(List<Choice<T>> choice) {
    fChoice = choice;
  }
}
