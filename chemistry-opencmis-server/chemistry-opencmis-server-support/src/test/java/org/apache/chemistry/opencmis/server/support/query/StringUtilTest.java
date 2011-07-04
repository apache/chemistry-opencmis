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
package org.apache.chemistry.opencmis.server.support.query;

import static org.junit.Assert.*;

import org.junit.Test;

public class StringUtilTest {
    
    @Test
    public void testUnescape() {
        String s = "abc";
        String res = StringUtil.unescape(s, null);
        assertEquals("abc", res);
        
        s="ab\\\\c";
        res = StringUtil.unescape(s, null);
        assertEquals("ab\\c", res);

        s="ab\\'c";
        res = StringUtil.unescape(s, null);
        assertEquals("ab'c", res);
        
        s="ab\\'";
        res = StringUtil.unescape(s, null);
        assertEquals("ab'", res);

        s="\\\\abc";
        res = StringUtil.unescape(s, null);
        assertEquals("\\abc", res);
        
        s="abc\\\\";
        res = StringUtil.unescape(s, null);
        assertEquals("abc\\", res);
        
        s="abc\\";
        res = StringUtil.unescape(s, null);
        assertNull(res);

        s="ab\\xc";
        res = StringUtil.unescape(s, null);
        assertNull(res);
    
        s="ab\\xc";
        res = StringUtil.unescape(s, "\\'x");
        assertEquals("abxc", res);
        
        s="abc\\x";
        res = StringUtil.unescape(s, "\\'x");
        assertEquals("abcx", res);

        s="ab\\yc";
        res = StringUtil.unescape(s, "\\'x");
        assertNull(res);

        // double escaping
        s="ab\\\\\\\\c";
        res = StringUtil.unescape(s, null);
        assertEquals("ab\\\\c", res);

        s="ab\\\\'c";
        res = StringUtil.unescape(s, null);
        assertEquals("ab\\'c", res);
        
        s="ab\\'Johnny\\'c";
        res = StringUtil.unescape(s, null);
        assertEquals("ab'Johnny'c", res);

        s="ab\\\\'Johnny\\\\'c";
        res = StringUtil.unescape(s, null);
        assertEquals("ab\\'Johnny\\'c", res);

        s="\\\\";
        res = StringUtil.unescape(s, null);
        assertEquals("\\", res);

        s="\\";
        res = StringUtil.unescape(s, null);
        assertNull(res);

        s="a";
        res = StringUtil.unescape(s, null);
        assertEquals("a", res);
        
        s="";
        res = StringUtil.unescape(s, null);
        assertEquals("", res);

        res = StringUtil.unescape(null, null);
        assertNull(res);
    }

}
