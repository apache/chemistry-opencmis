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
package org.apache.chemistry.opencmis.commons.impl.misc;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.junit.Test;

public class MimeTypesTest {

    @Test
    public void testMimeTypes() {
        assertEquals(".txt", MimeTypes.getExtension("text/plain"));
        assertEquals(".txt", MimeTypes.getExtension("TEXT/PLAIN"));
        assertEquals(".txt", MimeTypes.getExtension("text/plain ; charset=UTF-8"));
        assertEquals("", MimeTypes.getExtension("unknown/type"));
        assertEquals("", MimeTypes.getExtension(null));

        assertEquals("text/plain", MimeTypes.getMIMEType("txt"));
        assertEquals("text/plain", MimeTypes.getMIMEType(".txt"));
        assertEquals("application/octet-stream", MimeTypes.getMIMEType("someUnknownExtension"));
        assertEquals("application/octet-stream", MimeTypes.getMIMEType((String) null));

        assertEquals("text/plain", MimeTypes.getMIMEType(new File("test.txt")));
        assertEquals("application/octet-stream", MimeTypes.getMIMEType((File) null));
    }
}
