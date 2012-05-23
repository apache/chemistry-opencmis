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

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.JaxBHelper;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;

/**
 * Service document class.
 */
public class ServiceDocument extends AtomDocumentBase {

    public ServiceDocument() {
    }

    public void startServiceDocument() throws XMLStreamException {
        XMLStreamWriter xsw = getWriter();
        xsw.writeStartElement(Constants.NAMESPACE_APP, "service");
        writeNamespace(Constants.NAMESPACE_APP);
        writeNamespace(Constants.NAMESPACE_ATOM);
        writeNamespace(Constants.NAMESPACE_CMIS);
        writeNamespace(Constants.NAMESPACE_RESTATOM);
        writeAllCustomNamespace();
    }

    public void endServiceDocument() throws XMLStreamException {
        getWriter().writeEndElement();
    }

    public void startWorkspace(String title) throws XMLStreamException {
        getWriter().writeStartElement(Constants.NAMESPACE_APP, "workspace");
        writeSimpleTag(Constants.NAMESPACE_ATOM, "title", title);
    }

    public void endWorkspace() throws XMLStreamException {
        getWriter().writeEndElement();
    }

    public void writeRepositoryInfo(RepositoryInfo repInfo) throws JAXBException {
        CmisRepositoryInfoType repInfoJaxb = convert(repInfo);
        if (repInfoJaxb == null) {
            return;
        }

        JaxBHelper.marshal(JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY.createRepositoryInfo(repInfoJaxb), getWriter(), true);
    }

    public void writeUriTemplate(String template, String type, String mediatype) throws XMLStreamException {
        XMLStreamWriter xsw = getWriter();

        xsw.writeStartElement(Constants.NAMESPACE_RESTATOM, "uritemplate");
        writeSimpleTag(Constants.NAMESPACE_RESTATOM, "template", template);
        writeSimpleTag(Constants.NAMESPACE_RESTATOM, "type", type);
        writeSimpleTag(Constants.NAMESPACE_RESTATOM, "mediatype", mediatype);
        xsw.writeEndElement();
    }
}
