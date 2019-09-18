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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum SQLTypes
{

    VARCHAR("varchar"),

    TIMESTAMP_WITH_TZ("timestamptz"),

    BOOLEAN("boolean"),

    CHARACTER("character"),

    FLOAT8("float8"),

    FLOAT4("float4"),

    INT2("int2"),

    INT4("int4"),

    INT8("int8");

    private static final Map<Class<?>, SQLTypes> JAVA_TO_SQL_TYPE_MAP = new HashMap<>();

    private static final Map<Class<?>, Set<SQLTypes>> JAVA_TO_COMPATIBLE_SQL_TYPES_MAP = new HashMap<>();

    static
    {
        JAVA_TO_SQL_TYPE_MAP.put(String.class, VARCHAR);
        JAVA_TO_SQL_TYPE_MAP.put(Date.class, TIMESTAMP_WITH_TZ);
        JAVA_TO_SQL_TYPE_MAP.put(boolean.class, BOOLEAN);
        JAVA_TO_SQL_TYPE_MAP.put(Boolean.class, BOOLEAN);
        JAVA_TO_SQL_TYPE_MAP.put(char.class, CHARACTER);
        JAVA_TO_SQL_TYPE_MAP.put(Character.class, CHARACTER);
        JAVA_TO_SQL_TYPE_MAP.put(double.class, FLOAT8);
        JAVA_TO_SQL_TYPE_MAP.put(Double.class, FLOAT8);
        JAVA_TO_SQL_TYPE_MAP.put(float.class, FLOAT4);
        JAVA_TO_SQL_TYPE_MAP.put(Float.class, FLOAT4);
        JAVA_TO_SQL_TYPE_MAP.put(byte.class, INT2);
        JAVA_TO_SQL_TYPE_MAP.put(Byte.class, INT2);
        JAVA_TO_SQL_TYPE_MAP.put(short.class, INT2);
        JAVA_TO_SQL_TYPE_MAP.put(Short.class, INT2);
        JAVA_TO_SQL_TYPE_MAP.put(int.class, INT4);
        JAVA_TO_SQL_TYPE_MAP.put(Integer.class, INT4);
        JAVA_TO_SQL_TYPE_MAP.put(long.class, INT8);
        JAVA_TO_SQL_TYPE_MAP.put(Long.class, INT8);

        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(String.class, EnumSet.of(VARCHAR));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Date.class, EnumSet.of(TIMESTAMP_WITH_TZ));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(boolean.class, EnumSet.of(BOOLEAN));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Boolean.class, EnumSet.of(BOOLEAN));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(char.class, EnumSet.of(CHARACTER, VARCHAR));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Character.class, EnumSet.of(CHARACTER, VARCHAR));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(double.class, EnumSet.of(FLOAT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Double.class, EnumSet.of(FLOAT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(float.class, EnumSet.of(FLOAT4, FLOAT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Float.class, EnumSet.of(FLOAT4, FLOAT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(byte.class, EnumSet.of(INT2, INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Byte.class, EnumSet.of(INT2, INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(short.class, EnumSet.of(INT2, INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Short.class, EnumSet.of(INT2, INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(int.class, EnumSet.of(INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Integer.class, EnumSet.of(INT4, INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(long.class, EnumSet.of(INT8));
        JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.put(Long.class, EnumSet.of(INT8));
    }

    private String name;

    private SQLTypes(final String name)
    {
        this.name = name;
    }

    public static SQLTypes javaClassToSQLType(final Class<?> klass)
    {
        return JAVA_TO_SQL_TYPE_MAP.get(klass);
    }

    /**
     * Returns a set of SQL types compatible with the given Java class, i.e. which can be stored in the DB with no loss. Strings are
     * excluded, so that they are only assignable from strings (char is an exception).
     *
     * @param klass the class to which compatible SQL types should be found.
     * @return a set of SQL types which correspond to the given Java type.
     */
    public static Set<SQLTypes> javaClassToCompatibleForSaveSQLTypes(final Class<?> klass)
    {
        return JAVA_TO_COMPATIBLE_SQL_TYPES_MAP.get(klass);
    }

    @Override
    public String toString()
    {
        return name;
    }

}
