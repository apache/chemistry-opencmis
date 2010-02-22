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
package org.apache.opencmis.inmemory.server;

import org.apache.opencmis.server.spi.CallContext;


/**
 * This class uses a thread local storage to store the runtime context. The runtime
 * context can be accessed from everywhere using thread local storage access The runtime
 * context is only valid during the lifetime of a single request.
 * @author jens
 *
 */
public class RuntimeContext {
  public static class ThreadLocalRuntimeConfig extends ThreadLocal<CallContext> {    

    public void attachCfg(CallContext ctx) {
      set(ctx);      
    }

    public synchronized String getConfigValue(String key) {
      return get().get(key) ;
    }
  };

  private static ThreadLocalRuntimeConfig CONN = new ThreadLocalRuntimeConfig();
  
  public static ThreadLocalRuntimeConfig getRuntimeConfig() {
    return CONN;
  }

  public static String getRuntimeConfigValue(String key) {
    return CONN.getConfigValue(key);
  }
  
}
