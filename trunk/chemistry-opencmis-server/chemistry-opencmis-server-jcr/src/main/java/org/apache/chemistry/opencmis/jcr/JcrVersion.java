/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.chemistry.opencmis.jcr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.apache.chemistry.opencmis.jcr.type.JcrTypeHandlerManager;

/**
 * Instances of this class represent a specific version of a cmis:document backed by an underlying
 * JCR <code>Node</code>.
 */
public class JcrVersion extends JcrVersionBase {
    private static final Pattern VERSION_LABEL_PATTERN = Pattern.compile("(\\d+)(\\.(\\d+))?.*");
    private static final int GROUP_MAJOR = 1;
    private static final int GROUP_MINOR = 3;

    private final Version version;

    public JcrVersion(Node node, Version version, JcrTypeManager typeManager, PathManager pathManager,
            JcrTypeHandlerManager typeHandlerManager) {

        super(node, typeManager, pathManager, typeHandlerManager);
        this.version = version;
    }

    //------------------------------------------< protected >---

    @Override
    protected Node getContextNode() throws RepositoryException {
        Node frozen = version.getFrozenNode();
        if (frozen.hasNode(Node.JCR_CONTENT)) {
            return frozen.getNode(Node.JCR_CONTENT);
        }
        else {
            return getNode().getNode(Node.JCR_CONTENT);  // root version
        }
    }
    
    @Override
    protected String getObjectId() throws RepositoryException {
        return getVersionSeriesId();
    }

    @Override
    protected boolean isLatestVersion() throws RepositoryException {
        Version baseVersion = getBaseVersion(getNode());
        return baseVersion.isSame(version);
    }

    @Override
    protected boolean isMajorVersion() {
        return true;
    }

    @Override
    protected boolean isLatestMajorVersion() throws RepositoryException {
        return isLatestVersion();
    }

    @Override
    protected String getVersionLabel() throws RepositoryException {
        String name = version.getName();
        String major = parseVersion(name, GROUP_MINOR);

        return major == null
                ? name
                : (Integer.parseInt(major) + 1) + ".0";
    }

    @Override
    protected String getCheckInComment() throws RepositoryException {
        // todo set checkinComment
        return "";
    }
    
    //------------------------------------------< private >---

    private static String parseVersion(String name, int group) {
        Matcher matcher = VERSION_LABEL_PATTERN.matcher(name);
        return matcher.matches()
                ? matcher.group(group)
                : null;
    }

    /**
     * Retrieve version name.
     * @return version name
     * @throws RepositoryException
     */
    public String getVersionName() throws RepositoryException {
    	return version.getName();
    }

}
