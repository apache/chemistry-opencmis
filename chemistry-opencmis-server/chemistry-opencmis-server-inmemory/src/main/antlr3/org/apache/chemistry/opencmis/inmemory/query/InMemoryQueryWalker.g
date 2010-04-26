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
 *
 * Authors:
 *     Florent Guillaume, Nuxeo
 */
/**
 * CMISQL tree grammar, walker for the inmemory implementation.
 * This aims at implementing proper semantics without any speed
 * optimization.
 */
tree grammar InMemoryQueryWalker;

options {
    tokenVocab = CMISQLParser;
    ASTLabelType = CommonTree;
    output = AST;
}

@header {
/*
 * THIS FILE IS AUTO-GENERATED, DO NOT EDIT.
 *
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
 *
 * Authors:
 *     Florent Guillaume, Nuxeo
 *
 * THIS FILE IS AUTO-GENERATED, DO NOT EDIT.
 */
package org.apache.chemistry.opencmis.inmemory.query;

import java.math.BigDecimal;

import org.apache.chemistry.opencmis.inmemory.storedobj.api.StoredObject;
import org.apache.chemistry.opencmis.server.support.query.CalendarHelper;
}

@members {
    public StoredObject object;

    // public SimpleConnection connection;

    public String errorMessage;

    @Override
    public void displayRecognitionError(String[] tokenNames,
            RecognitionException e) {
        if (errorMessage == null) {
            String hdr = getErrorHeader(e);
            String msg = getErrorMessage(e, tokenNames);
            errorMessage = hdr + " " + msg;
        }
    }
}

query [StoredObject o] returns [String tableName, boolean matches]
@init {
    object = $o;
    // connection = $ conn;
}:
    ^(SELECT DISTINCT? select_list from_clause where_clause order_by_clause?)
    {
        // TODO distinct
        // TODO select_list
        $tableName = $from_clause.tableName;
        $matches = $where_clause.matches;
        // TODO order_by_clause
    }
    ;

select_list:
      STAR
    | ^(LIST select_sublist+)
    ;

select_sublist:
      value_expression column_name?
    | qualifier DOT STAR
    ;

value_expression returns [Object value]:
      column_reference
        {
            $value = $column_reference.value;
        }
    | ^(FUNC SCORE)
        {
            $value = Double.valueOf(1);
        }
    // TODO provide extension points for other functions
    ;

column_reference returns [Object value]:
    ^(COL qualifier? column_name)
      {
          String col = $column_name.start.getText();
          // TODO should use query name
          // $value = object.getIgnoreCase(col); // TODO error if unknown prop
      }
    ;

// multi_valued_column_reference returns [Object value]:
//    ^(COL qualifier? column_name)

qualifier:
      table_name
//    | correlation_name
    ;

from_clause returns [String tableName]:
    ^(FROM table_reference)
      {
          $tableName = $table_reference.tableName;
      }
    ;

table_reference returns [String tableName]:
    one_table table_join*
      {
          $tableName = $one_table.tableName;
          // TODO joins
      }
    ;

table_join:
    ^(JOIN join_kind one_table join_specification?)
    ;

one_table returns [String tableName]:
    ^(TABLE table_name correlation_name?)
      {
          $tableName = $table_name.text;
      }
    ;

join_kind:
    INNER | LEFT | OUTER;

join_specification:
    ^(ON column_reference EQ column_reference)
    ;

where_clause returns [boolean matches]:
      ^(WHERE search_condition)
        {
            $matches = $search_condition.matches;
        }
    | /* nothing */
        {
            $matches = true;
        }
    ;

search_condition returns [boolean matches]:
    b1=boolean_term { $matches = $b1.matches; }
    (OR b2=boolean_term { $matches |= $b2.matches; })*
    ;

boolean_term returns [boolean matches]:
    b1=boolean_factor { $matches = $b1.matches; }
    (AND b2=boolean_factor { $matches &= $b2.matches; })*
    ;

boolean_factor returns [boolean matches]:
      b=boolean_test { $matches = $b.matches; }
    | NOT b=boolean_test { $matches = ! $b.matches; }
    ;

boolean_test returns [boolean matches]:
      predicate { $matches = $predicate.matches; }
    | LPAR search_condition RPAR { $matches = $search_condition.matches; }
    ;

predicate returns [boolean matches]
@init {
    List<Object> literals;
}:
      ^(UN_OP IS_NULL arg) { $matches = $arg.value == null; }
    | ^(UN_OP IS_NOT_NULL arg) { $matches = $arg.value != null; }
    | ^(BIN_OP bin_op arg1=arg arg2=arg)
        {
            int token = $bin_op.start.getType();
            Object value1 = $arg1.value;
            Object value2 = $arg2.value;
            switch (token) {
                case EQ:
                    $matches = value1 != null && value1.equals(value2);
                    break;
                case NEQ:
                    $matches = value1 != null && value2 != null && ! value1.equals(value2);
                    break;
                case LT:
                    throw new UnsupportedOperationException("<"); // TODO
                case LTEQ:
                    throw new UnsupportedOperationException("<="); // TODO
                case GT:
                    throw new UnsupportedOperationException(">"); // TODO
                case GTEQ:
                    throw new UnsupportedOperationException(">="); // TODO
                case LIKE:
                    throw new UnsupportedOperationException("LIKE "); // TODO
                case NOT_LIKE:
                    throw new UnsupportedOperationException("NOT LIKE"); // TODO
                default:
                    throw new UnwantedTokenException(token, input);
            }
        }
    // | ^(BIN_OP_ANY bin_op_any arg mvc=multi_valued_column_reference)
    | ^(FUNC bool_func_name { literals = new ArrayList<Object>(); }
         (literal { literals.add($literal.value); })*)
        {
            int func = $bool_func_name.start.getType();
            switch (func) {
                case IN_FOLDER:
                    throw new UnsupportedOperationException("IN_FOLDER"); // TODO
                    // $matches = connection.isInFolder(object, literals.get(0));
                    // break;
                case IN_TREE:
                    throw new UnsupportedOperationException("IN_TREE"); // TODO
                    // $matches = connection.isInTree(object, literals.get(0));
                    // break;
                case CONTAINS:
                    throw new UnsupportedOperationException("CONTAINS"); // TODO
                    // $matches = connection.fulltextContains(object, literals);
                    // break;
                case ID:
                    break;
                default:
                    throw new UnwantedTokenException(Token.INVALID_TOKEN_TYPE, input);
            }
        }
    ;

bin_op:
    EQ | NEQ | LT | GT | LTEQ | GTEQ | LIKE | NOT_LIKE;

bool_func_name:
    IN_FOLDER | IN_TREE | CONTAINS | ID;

arg returns [Object value]
@init {
    List<Object> literals;
}:
      v=value_expression { $value = $v.value; }
    | l=literal { $value = $l.value; }
    | ^(LIST { literals = new ArrayList<Object>(); }
         (l=literal { literals.add($l.value); } )+)
            { $value = literals; }
    ;

literal returns [Object value]:
      NUM_LIT
        {
            try {
                $value = Long.valueOf($NUM_LIT.text);
            } catch (NumberFormatException e) {
                $value = new BigDecimal($NUM_LIT.text);
            }
        }
    | STRING_LIT
        {
            String s = $STRING_LIT.text;
            $value = s.substring(1, s.length() - 1);
        }
    | TIME_LIT
        {
            String s = $TIME_LIT.text;
            s = s.substring(s.indexOf('\'') + 1, s.length() - 1);
            try {
                $value = CalendarHelper.fromString(s);
            } catch (IllegalArgumentException e) {
                throw new UnwantedTokenException(Token.INVALID_TOKEN_TYPE, input);
            }
        }
    | BOOL_LIT
        {
            $value = Boolean.valueOf($BOOL_LIT.text);
        }
    ;

order_by_clause:
    ^(ORDER_BY sort_specification+)
    ;

sort_specification:
    column_reference ( ASC | DESC )
    ;

correlation_name:
    ID;

table_name:
    ID;

column_name:
    ID;
