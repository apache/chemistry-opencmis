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

package org.apache.chemistry.opencmis.jcr.query;

/**
 * The methods of this class map CMIS identifiers to JCR identifiers. Each implementation
 * of this interface is bound to a specific CMIS object type. That is, it implements the
 * identifier maps for that object type. 
 */
public interface IdentifierMap {

    /**
     * Map a column name in the CMIS query to the corresponding relative JCR path.
     * The path must be relative to the context node.
     *
     * @param name  column name
     * @return  relative JCR path
     */
    String jcrPathFromCol(String name);

    /**
     * JCR type name corresponding to the CMIS type bound to this instance.
     * @see #jcrTypeCondition()
     *
     * @return  name of the JCR type
     */
    String jcrTypeName();

    /**
     * Create and additional condition in order for the query to only return nodes
     * of the right type. This condition and-ed to the condition determined by the
     * CMIS query's where clause.
     * <p/>
     * A CMIS query for non versionable documents should for example result in the
     * following XPath query:
     * <p/>
     * <pre>
     *   element(*, nt:file)[not(@jcr:mixinTypes = 'mix:simpleVersionable')]
     * </pre>
     * Here the element test is covered by {@link #jcrTypeName()}
     * while the predicate is covered by this method.
     *
     * @see #jcrTypeName()
     *
     * @return  Additional condition or <code>null</code> if none.
     */
    String jcrTypeCondition();
}
