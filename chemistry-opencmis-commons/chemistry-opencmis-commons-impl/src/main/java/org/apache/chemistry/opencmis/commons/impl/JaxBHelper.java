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
package org.apache.chemistry.opencmis.commons.impl;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisRepositoryInfoType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.ObjectFactory;

/**
 * JAXB helper class.
 * 
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public final class JaxBHelper {

    private static final QName CMIS_OBJECT = new QName(Constants.NAMESPACE_RESTATOM, "object");
    private static final QName CMIS_TYPE_DEFINITION = new QName(Constants.NAMESPACE_RESTATOM, "type");
    private static final QName CMIS_REPOSITORY_INFO = new QName(Constants.NAMESPACE_RESTATOM, "repositoryInfo");

    public static final ObjectFactory CMIS_OBJECT_FACTORY = new ObjectFactory();
    public static final CMISExtraObjectFactory CMIS_EXTRA_OBJECT_FACTORY = new CMISExtraObjectFactory();
    public static final JAXBContext CONTEXT;
    static {
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(ObjectFactory.class, CMISExtraObjectFactory.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        CONTEXT = jc;
    }

    @XmlRegistry
    public static class CMISExtraObjectFactory {
        @XmlElementDecl(namespace = Constants.NAMESPACE_RESTATOM, name = "object")
        public JAXBElement<CmisObjectType> createObject(CmisObjectType value) {
            return new JAXBElement<CmisObjectType>(CMIS_OBJECT, CmisObjectType.class, value);
        }

        @XmlElementDecl(namespace = Constants.NAMESPACE_RESTATOM, name = "type")
        public JAXBElement<CmisTypeDefinitionType> createTypeDefinition(CmisTypeDefinitionType value) {
            return new JAXBElement<CmisTypeDefinitionType>(CMIS_TYPE_DEFINITION, CmisTypeDefinitionType.class, value);
        }

        @XmlElementDecl(namespace = Constants.NAMESPACE_RESTATOM, name = "repositoryInfo")
        public JAXBElement<CmisRepositoryInfoType> createRepositoryInfo(CmisRepositoryInfoType value) {
            return new JAXBElement<CmisRepositoryInfoType>(CMIS_REPOSITORY_INFO, CmisRepositoryInfoType.class, value);
        }
    }

    /**
     * Private constructor.
     */
    private JaxBHelper() {
    }

    /**
     * Creates an Unmarshaller.
     */
    public static Unmarshaller createUnmarshaller() throws JAXBException {
        return CONTEXT.createUnmarshaller();
    }

    /**
     * Creates an Marshaller.
     */
    public static Marshaller createMarshaller() throws JAXBException {
        return CONTEXT.createMarshaller();
    }

    /**
     * Marshals an object to a stream.
     */
    public static <T> void marshal(JAXBElement<T> object, OutputStream out, boolean fragment) throws JAXBException {
        if (object == null) {
            return;
        }

        Marshaller m = CONTEXT.createMarshaller();
        if (fragment) {
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        }

        m.marshal(object, out);
    }

    /**
     * Marshals an object to a XMLStreamWriter.
     */
    public static void marshal(Object object, XMLStreamWriter out, boolean fragment) throws JAXBException {
        if (object == null) {
            return;
        }

        Marshaller m = CONTEXT.createMarshaller();
        if (fragment) {
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        }

        m.marshal(object, out);
    }
}
