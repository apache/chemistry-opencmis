/*
 * Li
censed to the Apache Software Foundation (ASF) under one
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
package org.apache.chemistry.opencmis.tools.mapper;

import java.util.Properties;

public abstract class AbstractPropertyMapper implements PropertyMapper {
    
    private static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    protected String[] contentTypes;
    protected String cmisTypeId;
    protected String propPrefix;
    protected String dateFormat = DEFAULT_DATE_FORMAT;    

    public boolean initialize(String cfgPrefix, String typeKey, Properties properties) {
        propPrefix = cfgPrefix + "." + typeKey;
        cmisTypeId =   properties.getProperty(propPrefix + ".typeId");
        String contentTypeEntry = properties.getProperty(propPrefix);
         
        contentTypes = contentTypeEntry.split("\\:");
        for (int i=0; i<contentTypes.length; i++) {
            contentTypes[i] = contentTypes[i].trim();
        }

        String df = properties.getProperty(propPrefix + ".dateFormat");
        if (null!=df)
            dateFormat = df;
                
        if (null == cmisTypeId) 
            throw new MapperException("Missingt type id in properties: " + propPrefix + ".typeId");
        
        return true;
    }

    public String getMappedTypeId() {
        return cmisTypeId;
    }
    
    public String[] getContentTypes() {
        return contentTypes;
    }

}
