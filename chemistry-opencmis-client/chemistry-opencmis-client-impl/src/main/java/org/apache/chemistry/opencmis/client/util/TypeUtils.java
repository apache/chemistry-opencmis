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
package org.apache.chemistry.opencmis.client.util;

import static org.apache.chemistry.opencmis.commons.impl.Converter.convert;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.runtime.repository.ObjectFactoryImpl;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.Converter;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.JaxBHelper;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisTypeDefinitionType;
import org.apache.chemistry.opencmis.commons.impl.json.parser.JSONParser;

public class TypeUtils {

    private TypeUtils() {
    }

    /**
     * Serializes the type definition to XML, using the format defined in the
     * CMIS specification.
     * 
     * The XML is UTF-8 encoded and the stream is not closed.
     */
    public static void writeToXML(ObjectType type, OutputStream stream) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("Type must be set!");
        }
        if (stream == null) {
            throw new IllegalArgumentException("Output stream must be set!");
        }

        try {
            JaxBHelper.marshal(JaxBHelper.CMIS_EXTRA_OBJECT_FACTORY.createTypeDefinition(convert(type)), stream, false);
        } catch (JAXBException e) {
            throw new IOException("Marshaling failed!", e);
        }
    }

    /**
     * Serializes the type definition to JSON, using the format defined in the
     * CMIS specification.
     * 
     * The JSON is UTF-8 encoded and the stream is not closed.
     */
    public static void writeToJSON(ObjectType type, OutputStream stream) throws Exception {
        if (type == null) {
            throw new IllegalArgumentException("Type must be set!");
        }
        if (stream == null) {
            throw new IllegalArgumentException("Output stream must be set!");
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));
        JSONConverter.convert(type).writeJSONString(writer);
        writer.flush();
    }

    /**
     * Reads a type definition from a XML stream.
     * 
     * The stream must be UTF-8 encoded.
     */
    public static ObjectType readFromXML(InputStream stream) throws Exception {
        if (stream == null) {
            throw new IllegalArgumentException("Input stream must be set!");
        }

        Unmarshaller u = JaxBHelper.createUnmarshaller();

        @SuppressWarnings("unchecked")
        JAXBElement<CmisTypeDefinitionType> jaxb = (JAXBElement<CmisTypeDefinitionType>) u.unmarshal(stream);

        TypeDefinition typeDef = Converter.convert(jaxb.getValue());

        ObjectFactoryImpl of = new ObjectFactoryImpl();
        return of.convertTypeDefinition(typeDef);
    }

    /**
     * Reads a type definition from a JSON stream.
     * 
     * The stream must be UTF-8 encoded.
     */
    public static ObjectType readFromJSON(InputStream stream) throws Exception {
        if (stream == null) {
            throw new IllegalArgumentException("Input stream must be set!");
        }

        JSONParser parser = new JSONParser();
        Object json = parser.parse(new InputStreamReader(stream, "UTF-8"));

        if (!(json instanceof Map)) {
            throw new CmisRuntimeException("Invalid stream! Not a type definition!");
        }

        @SuppressWarnings("unchecked")
        TypeDefinition typeDef = JSONConverter.convertTypeDefinition((Map<String, Object>) json);

        ObjectFactoryImpl of = new ObjectFactoryImpl();
        return of.convertTypeDefinition(typeDef);
    }
}
