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

import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * This abstract base class implements all methods of the {@link Evaluator} interface
 * by throwing a {@link CmisNotSupportedException}.
 */
public abstract class EvaluatorBase<T> implements Evaluator<T> {
    public Evaluator<T> op() {
        throw new CmisNotSupportedException();
    }

    public T not(T op) {
        throw new CmisNotSupportedException("Not supported in query: not");
    }

    public T and(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: and");
    }

    public T or(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: or");
    }

    public T eq(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: =");
    }

    public T neq(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: !=");
    }

    public T gt(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: >");
    }

    public T gteq(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: >=");
    }

    public T lt(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: <");
    }

    public T lteq(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: <=");
    }

    public T in(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: in");
    }

    public T notIn(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: not in");
    }

    public T inAny(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: in");
    }

    public T notInAny(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: not in");
    }

    public T eqAny(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: = ANY");
    }

    public T isNull(T op) {
        throw new CmisNotSupportedException("Not supported in query: is null");
    }

    public T notIsNull(T op) {
        throw new CmisNotSupportedException("Not supported in query: is not null");
    }

    public T like(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: like");
    }

    public T notLike(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: not like");
    }

    public T contains(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: contains");
    }

    public T inFolder(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: in_folder");
    }

    public T inTree(T op1, T op2) {
        throw new CmisNotSupportedException("Not supported in query: in_tree");
    }

    public T list(List<T> ops) {
        throw new CmisNotSupportedException("Not supported in query: list");
    }

    public T value(boolean value) {
        throw new CmisNotSupportedException("Not supported in query: boolean value " + value);
    }

    public T value(double value) {
        throw new CmisNotSupportedException("Not supported in query: double value " + value);
    }

    public T value(long value) {
        throw new CmisNotSupportedException("Not supported in query: long value " + value);
    }

    public T value(String value) {
        throw new CmisNotSupportedException("Not supported in query: string value " + value);
    }

    public T value(GregorianCalendar value) {
        throw new CmisNotSupportedException("Not supported in query: date value " + value);
    }

    public T col(String name) {
        throw new CmisNotSupportedException("Not supported in query: column name " + name);
    }

    public T textAnd(List<T> ops) {
        throw new CmisNotSupportedException("Not supported in query: text and");
    }

    public T textOr(List<T> ops) {
        throw new CmisNotSupportedException("Not supported in query: text or");
    }

    public T textMinus(String text) {
        throw new CmisNotSupportedException("Not supported in query: text minus");
    }

    public T textWord(String word) {
        throw new CmisNotSupportedException("Not supported in query: text word");
    }

    public T textPhrase(String phrase) {
        throw new CmisNotSupportedException("Not supported in query: text phrase");
    }
}
