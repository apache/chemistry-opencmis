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

import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;

public class XMLUtils {

    public static boolean next(XMLStreamReader parser) throws XMLStreamException {
        if (parser.hasNext()) {
            parser.next();
            return true;
        }

        return false;
    }

    /**
     * Creates a new XML parser with OpenCMIS default settings.
     */
    public static XMLStreamReader createParser(InputStream stream) throws XMLStreamException {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.FALSE);
        return factory.createXMLStreamReader(stream);
    }

    /**
     * Moves the parser to the next start element.
     * 
     * @return <code>true</code> if another start element has been found,
     *         <code>false</code> otherwise
     */
    public static boolean findNextStartElemenet(XMLStreamReader parser) throws XMLStreamException {
        while (true) {
            int event = parser.getEventType();

            if (event == XMLStreamReader.START_ELEMENT) {
                return true;
            }

            if (parser.hasNext()) {
                parser.next();
            } else {
                return false;
            }
        }
    }

    /**
     * Parses a tag that contains text.
     */
    public static String readText(XMLStreamReader parser, int maxLength) throws XMLStreamException {
        StringBuilder sb = null;

        next(parser);

        while (true) {
            int event = parser.getEventType();
            if (event == XMLStreamReader.END_ELEMENT) {
                break;
            } else if (event == XMLStreamReader.CHARACTERS) {
                String s = parser.getText();
                if (s != null) {
                    if (sb == null) {
                        sb = new StringBuilder();
                    }

                    if (sb.length() + s.length() > maxLength) {
                        throw new CmisInvalidArgumentException("String limit exceeded!");
                    }
                    sb.append(s);
                }
            } else if (event == XMLStreamReader.START_ELEMENT) {
                throw new RuntimeException("Unexpected tag: " + parser.getName());
            }

            if (!next(parser)) {
                break;
            }
        }

        next(parser);

        return sb == null ? null : sb.toString();
    }
}
