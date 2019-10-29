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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes;

public class JDBCSQLExecutor implements ISQLExecutor
{

    /** Connection used for this executor. */
    private Connection connection;

    public void setConnection(final Connection connection)
    {
        this.connection = connection;
    }

    @Override
    public List<Map<String, Object>> execute(final String sqlQuery, final List<Object> args)
    {
        System.out.println("QUERY: " + sqlQuery);
        System.out.println("ARGS: " + Arrays.deepToString(args.toArray()));

        final List<Map<String, Object>> results = new ArrayList<>();
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery))
        {
            setArgsForPreparedStatement(args, preparedStatement);

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

        System.out.println("RESULTS: " + results);
        System.out.println("RESULTS COUNT: " + results.size());
        return results;
    }

    public void executeUpdate(final String sqlQuery, final List<Object> args)
    {
        System.out.println("QUERY: " + sqlQuery);
        System.out.println("ARGS: " + args);

        try (final PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery))
        {
            setArgsForPreparedStatement(args, preparedStatement);
            preparedStatement.executeUpdate();
        } catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    private void setArgsForPreparedStatement(final List<Object> args, final PreparedStatement preparedStatement) throws SQLException
    {
        for (int index = 0; index < args.size(); index++)
        {
            final Object object = args.get(index);
            if (object != null && object.getClass().isArray())
            {
                final Class<?> componentType = object.getClass().getComponentType();
                final String dbArrayTypeName;

                if (componentType.isPrimitive()) {
                    throw new IllegalArgumentException("Arrays of primitive types are not supported");
                } else
                {
                    if (Boolean.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.BOOLEAN.toString();
                    } else if (Byte.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.INT2.toString();
                    } else if (Character.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.CHARACTER.toString();
                    } else if (Double.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.FLOAT8.toString();
                    } else if (Float.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.FLOAT4.toString();
                    } else if (Integer.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.INT4.toString();
                    } else if (Long.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.INT8.toString();
                    } else if (Short.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.INT2.toString();
                    } else if (String.class.isAssignableFrom(componentType))
                    {
                        dbArrayTypeName = SQLTypes.VARCHAR.toString();
                    } else
                    {
                        throw new IllegalArgumentException("Arrays of objects are not supported");
                    }
                }

                final Object[] objectArray = (Object[]) object;
                preparedStatement.setArray(index + 1, connection.createArrayOf(dbArrayTypeName, objectArray));
            } else if (object instanceof Date)
            {
                final Date date = (Date) object;
                preparedStatement.setTimestamp(index + 1, new Timestamp(date.getTime()));
            } else
            {
                preparedStatement.setObject(index + 1, object);
            }
        }
    }

}
