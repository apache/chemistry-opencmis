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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects;

import java.io.Serializable;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AtomLink implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fRel;
    private String fType;
    private String fHref;

    public AtomLink() {
    }

    public String getRel() {
        return fRel;
    }

    public void setRel(String rel) {
        fRel = rel;
    }

    public String getType() {
        return fType;
    }

    public void setType(String type) {
        fType = type;
    }

    public String getHref() {
        return fHref;
    }

    public void setHref(String href) {
        fHref = href;
    }

    @Override
    public String toString() {
        return "Link: rel=\"" + fRel + "\" type=\"" + fType + "\" href=\"" + fHref + "\"";
    }
}
