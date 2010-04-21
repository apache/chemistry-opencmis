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
package org.apache.chemistry.opencmis.inmemory.server;

import java.math.BigInteger;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.api.server.CallContext;
import org.apache.chemistry.opencmis.commons.api.server.CmisService;
import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.inmemory.ConfigConstants;
import org.apache.chemistry.opencmis.server.support.CmisServiceWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemoryServiceFactoryImpl extends AbstractServiceFactory {

	private static final Log LOG = LogFactory.getLog(InMemoryServiceFactoryImpl.class.getName());
	private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(1000);
	private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(100);
	private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(2);
	private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1);
	private static CallContext OVERRIDE_CTX;

	private InMemoryService inMemoryService;
//	private CmisServiceWrapper<InMemoryService> wrapperService;
	private ThreadLocal<CmisServiceWrapper<InMemoryService>> threadLocalService = new ThreadLocal<CmisServiceWrapper<InMemoryService>>();
	private boolean fUseOverrideCtx = false;
	
	@Override
	public void init(Map<String, String> parameters) {
		LOG.info("Initializing in-memory repository...");


		inMemoryService = new InMemoryService(parameters);
//		wrapperService = new CmisServiceWrapper<InMemoryService>(inMemoryService, DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES,
//				DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);
		String overrideCtx = parameters.get(ConfigConstants.OVERRIDE_CALL_CONTEXT);
		if (null != overrideCtx)
			fUseOverrideCtx = true;

		LOG.info("...initialized in-memory repository.");
	}

	public static void setOverrideCallContext(CallContext ctx) {
		OVERRIDE_CTX = ctx;
	}
	
	@Override
	public CmisService getService(CallContext context) {
		LOG.debug("start getService()");

		// Attach the CallContext to a thread local context that can be
		// accessed from everywhere
		// Some unit tests set their own context. So if we find one then we use
		// this one and ignore the provided one. Otherwise we set a new context.
		if (fUseOverrideCtx && null != OVERRIDE_CTX) {
			context = OVERRIDE_CTX;
		}
		
		CmisServiceWrapper<InMemoryService> wrapperService = threadLocalService.get();
		if (wrapperService == null) {
			wrapperService = new CmisServiceWrapper<InMemoryService>(inMemoryService, DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES,
					DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);
			threadLocalService.set(wrapperService);
		}

		wrapperService.getWrappedService().setCallContext(context);

		LOG.debug("stop getService()");
		return inMemoryService; //wrapperService;
			
	}
	
	@Override
	public void destroy() {
		threadLocalService = null;
//		RuntimeContext.remove();
	}
	
}
