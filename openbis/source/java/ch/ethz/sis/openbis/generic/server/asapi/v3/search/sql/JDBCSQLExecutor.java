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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCSQLExecutor implements ISQLExecutor
{
    /** Connection used for this executor. */
    private final Connection connection;

    public JDBCSQLExecutor(final Connection connection)
    {
        this.connection = connection;
    }

    @Override
    public List<Map<String, Object>> execute(String sqlQuery, List<Object> args)
    {
        System.out.println("QUERY: " + sqlQuery);
        System.out.println("ARGS: " + args);

        final List<Map<String, Object>> results = new ArrayList<>();
        try (final PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery))
        {
            for (int aIdx = 0; aIdx < args.size(); aIdx++)
            {
                final Object object = args.get(aIdx);
                if (object.getClass().isArray())
                {
                    preparedStatement.setArray(aIdx + 1, connection.createArrayOf("bigint", (Object[]) object));
                } if (object instanceof Date)
                {
                    final Date date = (Date) object;
                    preparedStatement.setTimestamp(aIdx + 1, new Timestamp(date.getTime()));
                } else
                {
                    preparedStatement.setObject(aIdx + 1, object);
                }
            }

            try (final ResultSet resultSet = preparedStatement.executeQuery())
            {
                final ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                final int columnCount = resultSetMetaData.getColumnCount();
                final List<String> columnNames = new ArrayList<>(columnCount);
                for (int cIdx = 0; cIdx < columnCount; cIdx++)
                {
                    columnNames.add(resultSetMetaData.getColumnName(cIdx + 1));
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
        return results;
    }


}
