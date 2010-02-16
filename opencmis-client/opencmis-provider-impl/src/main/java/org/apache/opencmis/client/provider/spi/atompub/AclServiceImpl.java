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
package org.apache.opencmis.client.provider.spi.atompub;

import static org.apache.opencmis.commons.impl.Converter.convert;

import org.apache.opencmis.client.provider.spi.Session;
import org.apache.opencmis.client.provider.spi.atompub.objects.Acl;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomElement;
import org.apache.opencmis.client.provider.spi.atompub.objects.AtomEntry;
import org.apache.opencmis.commons.api.ExtensionsData;
import org.apache.opencmis.commons.enums.AclPropagation;
import org.apache.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.opencmis.commons.impl.Constants;
import org.apache.opencmis.commons.impl.UrlBuilder;
import org.apache.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.opencmis.commons.provider.AccessControlList;
import org.apache.opencmis.commons.provider.AclService;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class AclServiceImpl extends AbstractAtomPubService implements AclService {

  /**
   * Constructor.
   */
  public AclServiceImpl(Session session) {
    setSession(session);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ACLService#applyACL(java.lang.String, java.lang.String,
   * org.apache.opencmis.client.provider.AccessControlList, org.apache.opencmis.client.provider.AccessControlList,
   * org.apache.opencmis.commons.enums.ACLPropagation, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public AccessControlList applyAcl(String repositoryId, String objectId,
      AccessControlList addAces, AccessControlList removeAces, AclPropagation aclPropagation,
      ExtensionsData extension) {
    AccessControlList result = null;

    // fetch the current ACL
    AccessControlList originalAces = getAcl(repositoryId, objectId, false, null);

    // if no changes required, just return the ACL
    if (!isAclMergeRequired(addAces, removeAces)) {
      return originalAces;
    }

    // merge ACLs
    AccessControlList newACL = mergeAcls(originalAces, addAces, removeAces);

    // update ACL
    AtomEntry entry = updateAcl(repositoryId, objectId, newACL, aclPropagation);

    // walk through the entry and find updated ACL
    for (AtomElement element : entry.getElements()) {
      if (element.getObject() instanceof CmisObjectType) {
        // extract new ACL
        CmisObjectType object = (CmisObjectType) element.getObject();
        result = convert(object.getAcl(), object.isExactACL());

        break;
      }
    }

    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.opencmis.client.provider.ACLService#getACL(java.lang.String, java.lang.String,
   * java.lang.Boolean, org.apache.opencmis.client.provider.ExtensionsData)
   */
  public AccessControlList getAcl(String repositoryId, String objectId,
      Boolean onlyBasicPermissions, ExtensionsData extension) {

    // find the link
    String link = loadLink(repositoryId, objectId, Constants.REL_ACL, Constants.MEDIATYPE_ACL);

    if (link == null) {
      throw new CmisObjectNotFoundException("Unknown repository or object or ACLs are not support!");
    }

    UrlBuilder url = new UrlBuilder(link);
    url.addParameter(Constants.PARAM_ONLY_BASIC_PERMISSIONS, onlyBasicPermissions);

    // read and parse
    HttpUtils.Response resp = read(url);
    Acl acl = parse(resp.getStream(), Acl.class);

    return convert(acl.getACL(), null);
  }

}
