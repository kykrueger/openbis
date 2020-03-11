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

import java.sql.*;
import java.util.Date;
import java.util.*;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;

public class JDBCSQLExecutor implements ISQLExecutor
{

    private static final Map<Class<?>, PSQLTypes> TYPE_CONVERSION_MAP = new HashMap<>();

    static {
        TYPE_CONVERSION_MAP.put(Boolean[].class, PSQLTypes.BOOLEAN);

        TYPE_CONVERSION_MAP.put(Character[].class, PSQLTypes.CHARACTER);
        TYPE_CONVERSION_MAP.put(String[].class, PSQLTypes.VARCHAR);

        TYPE_CONVERSION_MAP.put(Double[].class, PSQLTypes.FLOAT8);
        TYPE_CONVERSION_MAP.put(Float[].class, PSQLTypes.FLOAT4);

        TYPE_CONVERSION_MAP.put(Long[].class, PSQLTypes.INT8);
        TYPE_CONVERSION_MAP.put(Integer[].class, PSQLTypes.INT4);
        TYPE_CONVERSION_MAP.put(Short[].class, PSQLTypes.INT2);
        TYPE_CONVERSION_MAP.put(Byte[].class, PSQLTypes.INT2);
    }

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
                PSQLTypes psqlType = null;
                final Object[] objectArray = (Object[]) object;
                if (TYPE_CONVERSION_MAP.containsKey(object.getClass()))
                {
                    psqlType = TYPE_CONVERSION_MAP.get(object.getClass());
                } else if (object.getClass() == Object[].class)
                {
                    if(objectArray.length > 0)
                    {
                        final Object objectArrayItem = objectArray[0];
                        psqlType = TYPE_CONVERSION_MAP.get(objectArrayItem.getClass());
                    } else
                    {
                        psqlType = TYPE_CONVERSION_MAP.get(String[].class); // Default for unknown array types
                    }
                }
                
                if (psqlType == null)
                {
                    throw new IllegalArgumentException("JDBCSQLExecutor don't support arrays of type: " + object.getClass().getName());
                }

                preparedStatement.setArray(index + 1, connection.createArrayOf(psqlType.toString(), objectArray));
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
