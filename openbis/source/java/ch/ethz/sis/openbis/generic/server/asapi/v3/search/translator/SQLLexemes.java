/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

public class SQLLexemes
{

    public static final String UNNEST = "unnest";

    public static final String NUMERIC = "numeric";

    public static final String TIMESTAMPTZ = "timestamptz";

    public static final String SELECT = "SELECT";

    public static final String INSERT = "INSERT";

    public static final String DELETE = "DELETE";

    public static final String DISTINCT = "DISTINCT";

    public static final String FROM = "FROM";

    public static final String INTO = "INTO";

    public static final String WHERE = "WHERE";

    public static final String CASE = "CASE";

    public static final String WHEN = "WHEN";

    public static final String THEN = "THEN";

    public static final String ELSE = "ELSE";

    public static final String END = "END";

    public static final String LEFT = "LEFT";

    public static final String INNER = "INNER";

    public static final String JOIN = "JOIN";

    public static final String VALUES = "VALUES";

    public static final String SP = " ";

    public static final String INNER_JOIN = INNER + SP + JOIN;

    public static final String LIKE = "LIKE";

    public static final String ON = "ON";

    public static final String IN = "IN";

    public static final String IS = "IS";

    public static final String AS = "AS";

    public static final String NOT = "NOT";

    public static final String AND = "AND";

    public static final String OR = "OR";

    public static final String NULL = "NULL";

    public static final String TRUE = Boolean.TRUE.toString();

    public static final String FALSE = Boolean.FALSE.toString();

    public static final String IS_NOT_NULL = IS + SP + NOT + SP + NULL;

    public static final String IS_NULL = IS + SP + NULL;

    public static final String NL = "\n";

    public static final String COMMA = ",";

    public static final String PERIOD = ".";

    public static final String DOUBLE_COLON = "::";

    public static final String EQ = "=";

    public static final String GT = ">";

    public static final String LT = "<";

    public static final String GE = ">=";

    public static final String LE = "<=";

    public static final String ASTERISK = "*";

    public static final String QU = "?";

    public static final String PERCENT = "%";

    public static final String BARS = "||";

    public static final String LP = "(";

    public static final String RP = ")";

    public static final String SQ = "'";

    public static final String TILDA = "~";

    private SQLLexemes()
    {
        throw new UnsupportedOperationException();
    }

}
