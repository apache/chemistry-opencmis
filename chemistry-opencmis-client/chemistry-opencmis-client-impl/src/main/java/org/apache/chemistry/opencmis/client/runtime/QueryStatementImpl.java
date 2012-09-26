/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
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
package org.apache.chemistry.opencmis.client.runtime;

import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.QueryStatement;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

/**
 * QueryStatement implementation.
 */
public class QueryStatementImpl implements QueryStatement, Cloneable {

    private final Session session;
    private final String statement;
    private final Map<Integer, String> parametersMap;

    public QueryStatementImpl(Session session, String statement) {
        if (session == null) {
            throw new IllegalArgumentException("Session must be set!");
        }

        if (statement == null) {
            throw new IllegalArgumentException("Statement must be set!");
        }

        this.session = session;
        this.statement = statement.trim();

        parametersMap = new HashMap<Integer, String>();
    }

    public void setType(int parameterIndex, String typeId) {
        setType(parameterIndex, session.getTypeDefinition(typeId));
    }

    public void setType(int parameterIndex, ObjectType type) {
        if (type == null) {
            throw new IllegalArgumentException("Type must be set!");
        }

        String queryName = type.getQueryName();
        if (queryName == null) {
            throw new IllegalArgumentException("Type has no query name!");
        }

        parametersMap.put(parameterIndex, queryName);
    }

    public void setProperty(int parameterIndex, String typeId, String propertyId) {
        ObjectType type = session.getTypeDefinition(typeId);

        PropertyDefinition<?> propertyDefinition = type.getPropertyDefinitions().get(propertyId);
        if (propertyDefinition == null) {
            throw new IllegalArgumentException("Property does not exist!");
        }

        setProperty(parameterIndex, propertyDefinition);
    }

    public void setProperty(int parameterIndex, PropertyDefinition<?> propertyDefinition) {
        if (propertyDefinition == null) {
            throw new IllegalArgumentException("Property must be set!");
        }

        String queryName = propertyDefinition.getQueryName();
        if (queryName == null) {
            throw new IllegalArgumentException("Property has no query name!");
        }

        parametersMap.put(parameterIndex, queryName);
    }

    public void setNumber(int parameterIndex, Number... num) {
        if (num == null || num.length == 0) {
            throw new IllegalArgumentException("Number must be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (Number n : num) {
            if (n == null) {
                throw new IllegalArgumentException("Number is null!");
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(n.toString());
        }

        parametersMap.put(parameterIndex, sb.toString());
    }

    public void setString(int parameterIndex, String... str) {
        if (str == null || str.length == 0) {
            throw new IllegalArgumentException("String must be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (String s : str) {
            if (s == null) {
                throw new IllegalArgumentException("String is null!");
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(escape(s));
        }

        parametersMap.put(parameterIndex, sb.toString());
    }


    public void setStringContains(int parameterIndex, String str) {
        if (str == null) {
            throw new IllegalArgumentException("String must be set!");
        }

        parametersMap.put(parameterIndex, escapeContains(str));
    }

    public void setStringLike(int parameterIndex, String str) {
        if (str == null) {
            throw new IllegalArgumentException("String must be set!");
        }

        parametersMap.put(parameterIndex, escapeLike(str));
    }

    public void setId(int parameterIndex, ObjectId... id) {
        if (id == null || id.length == 0) {
            throw new IllegalArgumentException("Id must be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (ObjectId oid : id) {
            if (oid == null || oid.getId() == null) {
                throw new IllegalArgumentException("Id is null!");
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(escape(oid.getId()));
        }

        parametersMap.put(parameterIndex, sb.toString());
    }

    public void setUri(int parameterIndex, URI... uri) {
        if (uri == null) {
            throw new IllegalArgumentException("URI must be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (URI u : uri) {
            if (u == null) {
                throw new IllegalArgumentException("URI is null!");
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(escape(u.toString()));
        }

        parametersMap.put(parameterIndex, sb.toString());
    }

    public void setUrl(int parameterIndex, URL... url) {
        if (url == null) {
            throw new IllegalArgumentException("URL must be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (URL u : url) {
            if (u == null) {
                throw new IllegalArgumentException("URI is null!");
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(escape(u.toString()));
        }

        parametersMap.put(parameterIndex, sb.toString());
    }

    public void setBoolean(int parameterIndex, boolean... bool) {
        if (bool == null || bool.length == 0) {
            throw new IllegalArgumentException("Boolean must not be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (boolean b : bool) {
            if (sb.length() > 0) {
                sb.append(",");
            }

            sb.append(b ? "TRUE" : "FALSE");
        }

        parametersMap.put(parameterIndex, sb.toString());
    }

    public void setDateTime(int parameterIndex, Calendar... cal) {
        setDateTime(parameterIndex, false, cal);
    }

    public void setDateTimeTimestamp(int parameterIndex, Calendar... cal) {
        setDateTime(parameterIndex, true, cal);
    }

    protected void setDateTime(int parameterIndex, boolean prefix, Calendar... cal) {
        if (cal == null || cal.length == 0) {
            throw new IllegalArgumentException("Calendar must be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (Calendar c : cal) {
            if (c == null) {
                throw new IllegalArgumentException("DateTime is null!");
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            if (prefix) {
                sb.append("TIMESTAMP ");
            }

            sb.append(convert(c.getTime()));
        }

        parametersMap.put(parameterIndex, sb.toString());
    }

    public void setDateTime(int parameterIndex, Date... date) {
        setDateTime(parameterIndex, false, date);
    }

    public void setDateTimeTimestamp(int parameterIndex, Date... date) {
        setDateTime(parameterIndex, true, date);
    }

    protected void setDateTime(int parameterIndex, boolean prefix, Date... date) {
        if (date == null || date.length == 0) {
            throw new IllegalArgumentException("Date must be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (Date d : date) {
            if (d == null) {
                throw new IllegalArgumentException("DateTime is null!");
            }

            if (sb.length() > 0) {
                sb.append(",");
            }

            if (prefix) {
                sb.append("TIMESTAMP ");
            }

            sb.append(convert(d));
        }

        parametersMap.put(parameterIndex, sb.toString());
    }

    public void setDateTime(int parameterIndex, long... ms) {
        setDateTime(parameterIndex, false, ms);
    }

    public void setDateTimeTimestamp(int parameterIndex, long... ms) {
        setDateTime(parameterIndex, true, ms);
    }

    protected void setDateTime(int parameterIndex, boolean prefix, long... ms) {
        if (ms == null || ms.length == 0) {
            throw new IllegalArgumentException("Timestamp must be set!");
        }

        StringBuilder sb = new StringBuilder();
        for (long l : ms) {
            if (sb.length() > 0) {
                sb.append(",");
            }

            if (prefix) {
                sb.append("TIMESTAMP ");
            }

            sb.append(convert(new Date(l)));
        }

        parametersMap.put(parameterIndex, sb.toString());
    }

    public String toQueryString() {
        boolean inStr = false;
        int parameterIndex = 0;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < statement.length(); i++) {
            char c = statement.charAt(i);

            if (c == '\'') {
                if (inStr && statement.charAt(i - 1) == '\\') {
                    inStr = true;
                } else {
                    inStr = !inStr;
                }
                sb.append(c);
            } else if (c == '?' && !inStr) {
                parameterIndex++;
                String s = parametersMap.get(parameterIndex);
                if (s == null) {
                    sb.append(c);
                } else {
                    sb.append(s);
                }
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public ItemIterable<QueryResult> query(boolean searchAllVersions) {
        return session.query(toQueryString(), searchAllVersions);
    }

    public ItemIterable<QueryResult> query(boolean searchAllVersions, OperationContext context) {
        return session.query(toQueryString(), searchAllVersions, context);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        QueryStatementImpl qs = new QueryStatementImpl(session, statement);
        qs.parametersMap.putAll(parametersMap);

        return qs;
    }

    @Override
    public String toString() {
        return toQueryString();
    }

    // --- internal ---

    private static String escape(String str) {
        StringBuilder sb = new StringBuilder("'");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '\'' || c == '\\') {
                sb.append("\\");
            }

            sb.append(c);
        }

        sb.append("'");

        return sb.toString();
    }

    private static String escapeLike(String str) {
        StringBuilder sb = new StringBuilder("'");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '\'') {
                sb.append("\\");
            } else if (c == '\\') {
                if (i + 1 < str.length() && (str.charAt(i + 1) == '%' || str.charAt(i + 1) == '_')) {
                    // no additional back slash
                } else {
                    sb.append("\\");
                }
            }

            sb.append(c);
        }

        sb.append("'");

        return sb.toString();
    }

    private static String escapeContains(String str) {
        StringBuilder sb = new StringBuilder("'");
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if (c == '\\') {
                sb.append("\\");
            } else if (c == '\'' || c == '\"') {
                sb.append("\\\\\\");
            }

            sb.append(c);
        }

        sb.append("'");

        return sb.toString();
    }

    private static String convert(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        return "'" + sdf.format(date) + "'";
    }
}
