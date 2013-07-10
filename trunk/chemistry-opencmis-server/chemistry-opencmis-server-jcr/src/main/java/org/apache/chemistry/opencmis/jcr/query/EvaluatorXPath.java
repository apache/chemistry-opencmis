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

import org.apache.chemistry.opencmis.jcr.util.ISO8601;
import org.apache.chemistry.opencmis.jcr.util.Iterables;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * This implementation of {@link Evaluator} results in an instance of a {@link XPathBuilder} which
 * can be used to validated the where clause of the original CMIS query and translate it to a
 * corresponding (i.e. semantically equal) XPath condition. 
 */
public class EvaluatorXPath extends EvaluatorBase<XPathBuilder> {

    @Override
    public Evaluator<XPathBuilder> op() {
        // New instance delegates these methods to this instance in order
        // to account for the case where these methods are overridden.
        return new EvaluatorXPath() {
            @Override
            protected String jcrPathFromId(String id) {
                return EvaluatorXPath.this.jcrPathFromId(id);
            }

            @Override
            protected String jcrPathFromCol(String name) {
                return EvaluatorXPath.this.jcrPathFromCol(name);
            }
        };
    }

    @Override
    public XPathBuilder not(final XPathBuilder op) {
        return new XPathBuilder() {
            public String xPath() {
                if (eval(true) != null) {
                    return eval(true) ? "true()" : "false()";
                }
                else {
                    return "not(" + op.xPath() + ")";
                }
            }
            public Boolean eval(Boolean folderPredicateValuation) {
                return not(op.eval(folderPredicateValuation));
            }

            public Iterable<XPathBuilder> folderPredicates() {
                return op.folderPredicates();
            }
        };
    }

    @Override
    public XPathBuilder and(final XPathBuilder op1, final XPathBuilder op2) {
        return new XPathBuilder() {
            public String xPath() {
                if (eval(true) != null) {
                    return eval(true) ? "true()" : "false()";
                }
                else if (op1.eval(true) != null) {  // if not null, op1 must be true -> shortcut evaluation to op2
                    return op2.xPath();
                }
                else if (op2.eval(true) != null) {  // if not null, op2 must be true -> shortcut evaluation to op1
                    return op1.xPath();
                }
                else {
                    return op1.xPath() + " and " + op2.xPath();
                }
            }
            public Boolean eval(Boolean folderPredicateValuation) {
                return and(op1.eval(folderPredicateValuation), op2.eval(folderPredicateValuation));
            }

            public Iterable<XPathBuilder> folderPredicates() {
                return Iterables.concat(op1.folderPredicates(), op2.folderPredicates());
            }
        };
    }

    @Override
    public XPathBuilder or(final XPathBuilder op1, final XPathBuilder op2) {
        return new XPathBuilder() {
            public String xPath() {
                if (eval(true) != null) {
                    return eval(true) ? "true()" : "false()";
                }
                else if (op1.eval(true) != null) {  // if not null, op1 must be false -> shortcut evaluation to op2
                    return op2.xPath();
                }
                else if (op2.eval(true) != null) {  // if not null, op2 must be false -> shortcut evaluation to op2
                    return op1.xPath();
                }
                else {
                    return "(" + op1.xPath() + " or " + op2.xPath() + ")";
                }
            }
            public Boolean eval(Boolean folderPredicateValuation) {
                return or(op1.eval(folderPredicateValuation), op2.eval(folderPredicateValuation));
            }

            public Iterable<XPathBuilder> folderPredicates() {
                return Iterables.concat(op1.folderPredicates(), op2.folderPredicates());
            }
        };
    }

    @Override
    public XPathBuilder eq(XPathBuilder op1, XPathBuilder op2) {
        return new RelOpBuilder(op1, " = ", op2);
    }

    @Override
    public XPathBuilder neq(XPathBuilder op1, XPathBuilder op2) {
        return new RelOpBuilder(op1, " != ", op2);
    }

    @Override
    public XPathBuilder gt(XPathBuilder op1, XPathBuilder op2) {
        return new RelOpBuilder(op1, " > ", op2);
    }

    @Override
    public XPathBuilder gteq(XPathBuilder op1, XPathBuilder op2) {
        return new RelOpBuilder(op1, " >= ", op2);
    }

    @Override
    public XPathBuilder lt(XPathBuilder op1, XPathBuilder op2) {
        return new RelOpBuilder(op1, " < ", op2);
    }

    @Override
    public XPathBuilder lteq(XPathBuilder op1, XPathBuilder op2) {
        return new RelOpBuilder(op1, " <= ", op2);
    }

    @Override
    public XPathBuilder in(XPathBuilder op1, XPathBuilder op2) {
        return super.in(op1, op2);    // todo implement in
    }

    @Override
    public XPathBuilder notIn(XPathBuilder op1, XPathBuilder op2) {
        return super.notIn(op1, op2);    // todo implement notIn
    }

    @Override
    public XPathBuilder inAny(XPathBuilder op1, XPathBuilder op2) {
        return super.inAny(op1, op2);    // todo implement inAny
    }

    @Override
    public XPathBuilder notInAny(XPathBuilder op1, XPathBuilder op2) {
        return super.notInAny(op1, op2);    // todo implement notInAny
    }

    @Override
    public XPathBuilder eqAny(XPathBuilder op1, XPathBuilder op2) {
        return super.eqAny(op1, op2);    // todo implement eqAny
    }

    @Override
    public XPathBuilder isNull(XPathBuilder op) {
        return new FunctionBuilder(op);
    }

    @Override
    public XPathBuilder notIsNull(XPathBuilder op) {
        return new FunctionBuilder("not", op);
    }

    @Override
    public XPathBuilder like(XPathBuilder op1, XPathBuilder op2) {
        return new FunctionBuilder("jcr:like", op1, op2);
    }

    @Override
    public XPathBuilder notLike(XPathBuilder op1, XPathBuilder op2) {
        return new FunctionBuilder("jcr:like", op1, op2) {
            @Override
            public String xPath() {
                return "not(" + super.xPath() + ")"; 
            }
        };
    }

    @Override
    public XPathBuilder contains(XPathBuilder op1, XPathBuilder op2) {
        return new ContainsBuilder(op2);
    }

    @Override
    public XPathBuilder inFolder(XPathBuilder op1,XPathBuilder op2) {
        return new FolderPredicateBuilder(op2.xPath(), false);
    }

    @Override
    public XPathBuilder inTree(XPathBuilder op1, XPathBuilder op2) {
        return new FolderPredicateBuilder(op2.xPath(), true);
    }

    @Override
    public XPathBuilder list(List<XPathBuilder> ops) {
        return super.list(ops);    // todo implement list
    }

    @Override
    public XPathBuilder value(boolean value) {
        return new LiteralBuilder(value);
    }

    @Override
    public XPathBuilder value(double value) {
        return new LiteralBuilder(value);
    }

    @Override
    public XPathBuilder value(long value) {
        return new LiteralBuilder(value);
    }

    @Override
    public XPathBuilder value(String value) {
        return new LiteralBuilder(value);
    }

    @Override
    public XPathBuilder value(GregorianCalendar value) {
        return new LiteralBuilder(value);
    }

    @Override
    public XPathBuilder col(String name) {
        return new ColRefBuilder(name);
    }

    @Override
    public XPathBuilder textAnd(List<XPathBuilder> ops) {
        return new TextOpBuilder(ops, " ");
    }

    @Override
    public XPathBuilder textOr(List<XPathBuilder> ops) {
        return new TextOpBuilder(ops, " OR ");
    }

    @Override
    public XPathBuilder textMinus(String text) {
        return new TextMinusBuilder(text);
    }

    @Override
    public XPathBuilder textWord(String word) {
        return new TextWordBuilder(word);
    }

    @Override
    public XPathBuilder textPhrase(String phrase) {
        return new TextPhraseBuilder(phrase);
    }

    //------------------------------------------< protected >---

    /**
     * Resolve from a CMIS object id to the corresponding absolute JCR path.
     * This default implementations simply returns <code>id</code>.
     */
    protected String jcrPathFromId(String id) {
        return id;
    }

    /**
     * Resolve from a column name in the query to the corresponding
     * relative JCR path. The path must be relative to the context node.
     * This default implementations simply returns <code>name</code>.
     */
    protected String jcrPathFromCol(String name) {
        return name; 
    }

    //------------------------------------------< private >---

    /**
     * @return  <code>null</code> if <code>b</code> is <code>null</code>, <code>!b</code> otherwise.
     */
    private static Boolean not(Boolean b) {
        return b == null ? null : !b;
    }

    /**
     * @return 
     *  <ul><li><code>true</code> if either of <code>b1</code> and <code>b2</code> is <code>true</code>,</li>
     *      <li><code>false</code> if both <code>b1</code> and <code>b2</code> are <code>false</code>,</li>
     *      <li><code>null</code> otherwise.</li></ul>
     */
    private static Boolean or(Boolean b1, Boolean b2) {
        return Boolean.TRUE.equals(b1) || Boolean.TRUE.equals(b2)
                ? Boolean.TRUE
                : Boolean.FALSE.equals(b1) && Boolean.FALSE.equals(b2)
                        ? Boolean.FALSE
                        : null;
    }

    /**
     * @return
     *  <ul><li><code>false</code> if either of <code>b1</code> and <code>b2</code> is <code>false</code>,</li>
     *      <li><code>true</code> if both <code>b1</code> and <code>b2</code> are <code>true</code>,</li>
     *      <li><code>null</code> otherwise.</li></ul>
     */
    private static Boolean and(Boolean b1, Boolean b2) {
        return Boolean.FALSE.equals(b1) || Boolean.FALSE.equals(b2)
                ? Boolean.FALSE
                : Boolean.TRUE.equals(b1) && Boolean.TRUE.equals(b2)
                        ? Boolean.TRUE
                        : null;
    }

    private static class RelOpBuilder implements XPathBuilder {
        private final String relOp;
        private final XPathBuilder op1;
        private final XPathBuilder op2;

        public RelOpBuilder(XPathBuilder op1, String relOp, XPathBuilder op2) {
            this.relOp = relOp;
            this.op1 = op1;
            this.op2 = op2;
        }

        public String xPath() {
            return op1.xPath() + relOp + op2.xPath();
        }

        public Boolean eval(Boolean folderPredicateValuation) {
            return null;
        }

        public Iterable<XPathBuilder> folderPredicates() {
            return Iterables.concat(op1.folderPredicates(), op2.folderPredicates());
        }
    }

    private class FolderPredicateBuilder implements XPathBuilder {
        private final String folderId;
        private final boolean includeDescendants;
        
        public FolderPredicateBuilder(String folderId, boolean includeDescendants) {
            this.folderId = stripQuotes(folderId);
            this.includeDescendants = includeDescendants;
        }

        public String xPath() {
            return jcrPathFromId(folderId) + (includeDescendants ? "//" : "/");
        }

        public Boolean eval(Boolean folderPredicateValuation) {
            return folderPredicateValuation;
        }

        public Iterable<XPathBuilder> folderPredicates() {
            return Iterables.singleton((XPathBuilder) this);
        }

        private String stripQuotes(String string) {
            return (string.startsWith("'") || string.startsWith("\"")) && string.length() >= 2
                    ? string.substring(1, string.length() - 1)
                    : string;
        }

    }

    private abstract static class PrimitiveBuilder implements XPathBuilder {
        public Boolean eval(Boolean folderPredicateValuation) {
            return null;
        }

        public Iterable<XPathBuilder> folderPredicates() {
            return Iterables.empty();
        }
    }

    private static class LiteralBuilder extends PrimitiveBuilder  {
        private final String xPath;

        public LiteralBuilder(String value) {
            xPath = "'" + value + "'";
        }

        public LiteralBuilder(boolean value) {
            xPath = Boolean.toString(value);
        }

        public LiteralBuilder(long value) {
            xPath = Long.toString(value);
        }

        public LiteralBuilder(double value) {
            xPath = Double.toString(value);
        }

        public LiteralBuilder(GregorianCalendar value) {
            xPath = "xs:dateTime('" + ISO8601.format(value) + "')";
        }

        public String xPath() {
            return xPath;
        }
    }

    private class ColRefBuilder extends PrimitiveBuilder  {
        private final String colRef;

        public ColRefBuilder(String colRef) {
            this.colRef = colRef;
        }

        public String xPath() {
            return jcrPathFromCol(colRef);   
        }

    }

    private static class FunctionBuilder extends PrimitiveBuilder {
        private final String function;
        private final XPathBuilder op1;
        private final XPathBuilder op2;

        private FunctionBuilder(String function, XPathBuilder op1, XPathBuilder op2) {
            this.function = function;
            this.op1 = op1;
            this.op2 = op2;
        }

        public FunctionBuilder(String function, XPathBuilder op1) {
            this(function, op1, null);
        }

        public FunctionBuilder(XPathBuilder op1) {
            this(null, op1, null);
        }

        public String xPath() {
            return function == null
                ? op1.xPath()
                : function + "(" + op1.xPath() + (op2 == null ? "" : ", " + op2.xPath()) + ")";
        }
    }

    private static class ContainsBuilder extends PrimitiveBuilder {
        private final XPathBuilder op;

        public ContainsBuilder(XPathBuilder op) {
            this.op = op;
        }

        public String xPath() {
            return "jcr:contains(jcr:content, '" + op.xPath() + "')";
        }
    }

    private static class TextOpBuilder extends PrimitiveBuilder {
        private final List<XPathBuilder> ops;
        private final String relOp;

        public TextOpBuilder(List<XPathBuilder> ops, String relOp) {
            this.ops = ops;   
            this.relOp = relOp;
        }

        public String xPath() {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (XPathBuilder op: ops) {
                sb.append(sep).append(op.xPath());
                sep = relOp;
            }

            return sb.toString();
        }
    }
    
    private static class TextMinusBuilder extends PrimitiveBuilder {
        private final String text;

        public TextMinusBuilder(String text) {
            this.text = text;
        }

        public String xPath() {
            return "-" + escape(text);
        }
    }
    
    private static class TextWordBuilder extends PrimitiveBuilder {
        private final String word;

        public TextWordBuilder(String word) {
            this.word = word;
        }

        public String xPath() {
            return escape(word);
        }
    }

    private static class TextPhraseBuilder extends PrimitiveBuilder {
        private final String phrase;

        public TextPhraseBuilder(String phrase) {
            this.phrase = phrase;
        }

        public String xPath() {
            return "\"" + escape(phrase.substring(1, phrase.length() - 1)) + "\"";
        }
    }

    /**
     * Within the searchexp literal instances of single quote ('), double quote (")
     * and hyphen (-) must be escaped with a backslash (\). Backslash itself must
     * therefore also be escaped, ending up as double backslash (\\).
     */
    private static String escape(String s) {
        if (s == null) {
            return "";
        }

        s = s.replaceAll("'", "\\'");
        s = s.replaceAll("\"", "\\\"");
        s = s.replaceAll("-", "\\-");
        s = s.replaceAll("\\\\", "\\\\\\\\");
        return s;
    }
}
