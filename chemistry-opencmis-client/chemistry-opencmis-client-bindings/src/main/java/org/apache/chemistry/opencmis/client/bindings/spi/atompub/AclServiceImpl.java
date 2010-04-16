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
package org.apache.chemistry.opencmis.client.bindings.spi.atompub;

import static org.apache.chemistry.opencmis.commons.impl.Converter.convert;

import org.apache.chemistry.opencmis.client.bindings.spi.Session;
import org.apache.chemistry.opencmis.client.bindings.spi.atompub.objects.AtomAcl;
import org.apache.chemistry.opencmis.commons.api.Acl;
import org.apache.chemistry.opencmis.commons.api.AclService;
import org.apache.chemistry.opencmis.commons.api.ExtensionsData;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.impl.UrlBuilder;

/**
 * ACL Service AtomPub client.
 * 
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
	 * @see
	 * org.apache.opencmis.client.provider.ACLService#applyACL(java.lang.String,
	 * java.lang.String, org.apache.opencmis.client.provider.AccessControlList,
	 * org.apache.opencmis.client.provider.AccessControlList,
	 * org.apache.opencmis.commons.enums.ACLPropagation,
	 * org.apache.opencmis.client.provider.ExtensionsData)
	 */
	public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
			AclPropagation aclPropagation, ExtensionsData extension) {
		Acl result = null;

		// fetch the current ACL
		Acl originalAces = getAcl(repositoryId, objectId, false, null);

		// if no changes required, just return the ACL
		if (!isAclMergeRequired(addAces, removeAces)) {
			return originalAces;
		}

		// merge ACLs
		Acl newACL = mergeAcls(originalAces, addAces, removeAces);

		// update ACL
		AtomAcl acl = updateAcl(repositoryId, objectId, newACL, aclPropagation);
		result = convert(acl.getACL(), null);

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.opencmis.client.provider.ACLService#getACL(java.lang.String,
	 * java.lang.String, java.lang.Boolean,
	 * org.apache.opencmis.client.provider.ExtensionsData)
	 */
	public org.apache.chemistry.opencmis.commons.api.Acl getAcl(String repositoryId, String objectId,
			Boolean onlyBasicPermissions, ExtensionsData extension) {

		// find the link
		String link = loadLink(repositoryId, objectId, Constants.REL_ACL, Constants.MEDIATYPE_ACL);

		if (link == null) {
			throwLinkException(repositoryId, objectId, Constants.REL_ACL, Constants.MEDIATYPE_ACL);
		}

		UrlBuilder url = new UrlBuilder(link);
		url.addParameter(Constants.PARAM_ONLY_BASIC_PERMISSIONS, onlyBasicPermissions);

		// read and parse
		HttpUtils.Response resp = read(url);
		AtomAcl acl = parse(resp.getStream(), AtomAcl.class);

		return convert(acl.getACL(), null);
	}

}
