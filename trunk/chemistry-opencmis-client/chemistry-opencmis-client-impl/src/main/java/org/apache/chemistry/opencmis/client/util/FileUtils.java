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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;

/**
 * A set of utility methods that simplify file and folder operations.
 */
public class FileUtils {

    private FileUtils() {
    }

    /**
     * Gets an object by path or object id.
     * 
     * @param pathOrIdOfObject
     *            the path or object id
     * @param session
     *            the session
     * @return the object
     * @throws CmisBaseException
     */
    public static CmisObject getObject(String pathOrIdOfObject, Session session) {
        if (session == null) {
            throw new IllegalArgumentException("session must be set!");
        }
        if (pathOrIdOfObject == null) {
            throw new IllegalArgumentException("pathOrIdOfObject must be set!");
        }

        CmisObject result = null;
        if (pathOrIdOfObject.startsWith("/")) {
            result = session.getObjectByPath(pathOrIdOfObject);
        } else {
            result = session.getObject(pathOrIdOfObject);
        }

        return result;
    }

    /**
     * Gets a folder by path or object id.
     * 
     * @param pathOrIdOfObject
     *            the path or folder id
     * @param session
     *            the session
     * @return the folder object
     * @throws CmisBaseException
     */
    public static Folder getFolder(String pathOrIdOfObject, Session session) {
        CmisObject folder = getObject(pathOrIdOfObject, session);

        if (folder instanceof Folder) {
            return (Folder) folder;
        } else {
            throw new IllegalArgumentException("Object is not a folder!");
        }
    }

    /**
     * Creates a document from a file.
     * 
     * @param parentIdOrPath
     *            the id or path of the parent folder
     * @param file
     *            the source file
     * @param type
     *            the document type (defaults to <code>cmis:document</code>)
     * @param versioningState
     *            the versioning state or <code>null</code>
     * @return the newly created document
     * @throws FileNotFoundException
     * @throws CmisBaseException
     */
    public static Document createDocumentFromFile(String parentIdOrPath, File file, String type,
            VersioningState versioningState, Session session) throws FileNotFoundException {
        if (type == null) {
            type = BaseTypeId.CMIS_DOCUMENT.value(); // "cmis:document";
        }

        Folder parentFolder = getFolder(parentIdOrPath, session);

        String name = file.getName();
        String mimetype = MimeTypes.getMIMEType(file);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);
        properties.put(PropertyIds.NAME, name);

        InputStream stream = new FileInputStream(file);
        ContentStream contentStream = new ContentStreamImpl(name, BigInteger.valueOf(file.length()), mimetype, stream);

        try {
            return parentFolder.createDocument(properties, contentStream, versioningState);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException ioe) {
                    throw new CmisRuntimeException("Cannot close source stream!", ioe);
                }
            }
        }
    }

    /**
     * Creates a text document from a string.
     * 
     * @param parentIdOrPath
     *            the id or path of the parent folder
     * @param name
     *            the document name
     * @param content
     *            the content string
     * @param type
     *            the document type (defaults to <code>cmis:document</code>)
     * @param versioningState
     *            the versioning state or <code>null</code>
     * @param session
     *            the session
     * @return the newly created document
     */
    public static Document createTextDocument(String parentIdOrPath, String name, String content, String type,
            VersioningState versioningState, Session session) {
        if (type == null) {
            type = BaseTypeId.CMIS_DOCUMENT.value(); // "cmis:document";
        }

        Folder parentFolder = getFolder(parentIdOrPath, session);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);
        properties.put(PropertyIds.NAME, name);

        byte[] contentBytes = new byte[0];
        if (content != null) {
            try {
                contentBytes = content.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                contentBytes = content.getBytes();
            }
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(contentBytes);
        ContentStream contentStream = new ContentStreamImpl(name, BigInteger.valueOf(contentBytes.length),
                "text/plain", bais);

        return parentFolder.createDocument(properties, contentStream, versioningState);
    }

    /**
     * Creates a child folder with the name specified of the type specified. If
     * type is null then will default to cmis:folder.
     * 
     * @param parentFolder
     *            the parent folder
     * @param name
     *            the folder name
     * @param type
     *            the folder type (defaults to <code>cmis:folder</code>)
     * @return the newly created folder
     * @throws CmisBaseException
     */
    public static Folder createFolder(Folder parentFolder, String name, String type) {
        if (type == null) {
            type = BaseTypeId.CMIS_FOLDER.value();
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);
        properties.put(PropertyIds.NAME, name);

        return parentFolder.createFolder(properties);
    }

    /**
     * Creates a folder using a String identifier.
     * 
     * @param parentIdOrPath
     *            the id or path of the parent folder
     * @param name
     *            the folder name
     * @param type
     *            the folder type (defaults to <code>cmis:folder</code>)
     * @param session
     *            the session
     * @return the newly created folder
     * @throws CmisBaseException
     */
    public static Folder createFolder(String parentIdOrPath, String name, String type, Session session) {
        Folder parentFolder = getFolder(parentIdOrPath, session);

        if (type == null) {
            type = BaseTypeId.CMIS_FOLDER.value();
        }

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);
        properties.put(PropertyIds.NAME, name);

        return parentFolder.createFolder(properties);
    }

    /**
     * Downloads the contentStream for the given doc to the specified path.
     * 
     * @param doc
     *            the document
     * @param destinationPath
     *            the destination path
     * @throws IOException
     * @throws CmisBaseException
     */
    public static void download(Document doc, String destinationPath) throws IOException {
        FileOutputStream fileStream = new FileOutputStream(destinationPath);
        BufferedOutputStream out = new BufferedOutputStream(fileStream);
        copyStream(doc.getContentStream().getStream(), out);
        out.close();
    }

    /**
     * Downloads a document by its id or path.
     * 
     * @param docIdOrPath
     *            the id or path of the document
     * @param destinationPath
     *            the destination path
     * @param session
     *            the session
     * @throws IOException
     * @throws CmisBaseException
     */
    public static void download(String docIdOrPath, String destinationPath, Session session) throws IOException {
        CmisObject doc = getObject(docIdOrPath, session);

        if (doc instanceof Document) {
            download((Document) doc, destinationPath);
        } else {
            throw new IllegalArgumentException("Object is not a document!");
        }
    }

    static void copyStream(InputStream inStream, OutputStream outStream) throws IOException {
        byte[] buffer = new byte[48 * 1024];
        int bytesRead = 0;
        while ((bytesRead = inStream.read(buffer, 0, buffer.length)) > 0) {
            outStream.write(buffer, 0, bytesRead);
        }
    }

    /**
     * Deletes an object by path or id (string identifier).
     * 
     * @param pathOrIdOfObject
     *            the id or path of the object
     * @param session
     *            the session
     * @throws CmisBaseException
     */
    public static void delete(String pathOrIdOfObject, Session session) {
        CmisObject object = getObject(pathOrIdOfObject, session);

        if (object instanceof Folder) {
            ((Folder) object).deleteTree(true, UnfileObject.DELETE, true);
        } else {
            object.delete(true);
        }
    }

    /**
     * Prints out all of the properties for this object to System.out.
     * 
     * @param object
     *            the object
     */
    public static void printProperties(CmisObject object) {
        printProperties(object, System.out);
    }

    /**
     * Prints out all of the properties for this object to the given
     * PrintStream.
     * 
     * @param object
     *            the object
     */
    public static void printProperties(CmisObject object, PrintStream out) {
        for (Property<?> prop : object.getProperties()) {
            printProperty(prop, out);
        }
    }

    public static void printProperty(Property<?> prop) {
        printProperty(prop, System.out);
    }

    public static void printProperty(Property<?> prop, PrintStream out) {
        out.println(prop.getId() + ": " + prop.getValuesAsString());
    }
}
