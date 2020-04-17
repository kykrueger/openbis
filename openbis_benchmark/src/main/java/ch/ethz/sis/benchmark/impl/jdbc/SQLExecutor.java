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

package ch.ethz.sis.benchmark.impl.jdbc;

import ch.ethz.sis.logging.LogManager;
import ch.ethz.sis.logging.Logger;

import java.sql.*;
import java.util.Date;
import java.util.*;

public class SQLExecutor
{

    private static Logger logger = LogManager.getLogger(SQLExecutor.class);

    private static final Map<Class<?>, String> TYPE_CONVERSION_MAP = new HashMap<>();

    static
    {
        TYPE_CONVERSION_MAP.put(Boolean.class, "boolean");

        TYPE_CONVERSION_MAP.put(Character.class, "character");
        TYPE_CONVERSION_MAP.put(String.class, "varchar");

        TYPE_CONVERSION_MAP.put(Double.class, "float8");
        TYPE_CONVERSION_MAP.put(Float.class, "float4");

        TYPE_CONVERSION_MAP.put(Long.class, "int8");
        TYPE_CONVERSION_MAP.put(Integer.class, "int4");
        TYPE_CONVERSION_MAP.put(Short.class, "int2");
        TYPE_CONVERSION_MAP.put(Byte.class, "int2");
    }

    public static int executeUpdate(
            final Connection connection,
            final String sqlUpdate,
            final List<List<Object>> parametersBatch) {
        int results = 0;
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sqlUpdate))
        {
            for (List<Object> parameters : parametersBatch) {
                setArgsForPreparedStatement(parameters, preparedStatement);
                preparedStatement.addBatch();
                int[] batchResults = preparedStatement.executeBatch();
                for (int result : batchResults) {
                    results += result;
                }
            }
        } catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        return results;
    }

    public static List<Map<String, Object>> executeQuery(final Connection connection, final String sqlQuery, final List<Object> parameters) {

        logger.info("QUERY: " + sqlQuery);
        logger.info("PARAM: " + Arrays.deepToString(parameters.toArray()));

        final List<Map<String, Object>> results = new ArrayList<>();
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery))
        {
            setArgsForPreparedStatement(parameters, preparedStatement);

            try (final ResultSet resultSet = preparedStatement.executeQuery())
            {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final int columnCount = resultSetMetaData.getColumnCount();
                final List<String> columnNames = new ArrayList<>(columnCount);
                for (int index = 0; index < columnCount; index++)
                {
                    columnNames.add(resultSetMetaData.getColumnName(index + 1));
                }

                while (resultSet.next())
                {
                    final Map<String, Object> row = new HashMap<>();
                    for (final String columnName : columnNames)
                    {
                        row.put(columnName, resultSet.getObject(columnName));
                    }
                    results.add(row);
                }
            }
        } catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }

        logger.info("RESULTS COUNT: " + results.size());
        return results;
    }

    private static void setArgsForPreparedStatement(final List<Object> args, final PreparedStatement preparedStatement) throws SQLException
    {
        for (int index = 0; index < args.size(); index++)
        {
            final Object object = args.get(index);
            if (object != null && object.getClass().isArray())
            {
                final Object[] objectArray = (Object[]) object;
                final Class<?> arrayObjectType = object.getClass().getComponentType();
                final String psqlType = TYPE_CONVERSION_MAP.get(arrayObjectType);

                if (psqlType == null)
                {
                    throw new IllegalArgumentException("JDBCSQLExecutor don't support arrays of type: " + object.getClass().getName()
                            + " - With elements of type: " + arrayObjectType.getName() + " - Data: " + Arrays.toString(objectArray));
                }

                preparedStatement.setArray(index + 1, preparedStatement.getConnection().createArrayOf(psqlType, objectArray));
            } else if (object instanceof Date)
            {
                final Date date = (Date) object;
                preparedStatement.setTimestamp(index + 1, new Timestamp(date.getTime()));
            } else if (object instanceof Calendar)
            {
                final Calendar calendar = (Calendar) object;
                preparedStatement.setTimestamp(index + 1, new Timestamp(calendar.getTimeInMillis()), calendar);
            } else
            {
                preparedStatement.setObject(index + 1, object);
            }
        }
    }

}
