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
package org.apache.chemistry.opencmis.server.impl.atompub;

import static org.apache.chemistry.opencmis.commons.impl.Converter.convert;

import java.io.OutputStream;

import javax.xml.bind.JAXBException;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.impl.JaxBHelper;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisAccessControlListType;

/**
 * ACL document.
 *
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 */
public class AclDocument {

    /**
     * Creates an ACL document.
     */
    public AclDocument() {
    }

    /**
     * Writes an object.
     */
    public void writeAcl(Acl acl, OutputStream out) throws JAXBException {
        CmisAccessControlListType aclJaxb = convert(acl);
        if (aclJaxb == null) {
            return;
        }

        JaxBHelper.marshal(JaxBHelper.CMIS_OBJECT_FACTORY.createAcl(aclJaxb), out, false);
    }
}
