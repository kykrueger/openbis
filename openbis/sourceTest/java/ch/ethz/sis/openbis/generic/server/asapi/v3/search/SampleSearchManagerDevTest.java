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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.PostgresSearchDAO;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SampleSearchManagerDevTest
{
    @Test
    public void testPipeline()
    {
        final Long userId = 2L; // Default ETL Server that is supposed to see everything
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        final SampleFetchOptions sampleFetchOption = new SampleFetchOptions();
        final PostgresSearchDAO searchDAO = new PostgresSearchDAO();
        searchDAO.setSqlExecutor((sqlQuery, args) ->
                {
                    System.out.println("QUERY: " + sqlQuery);
                    System.out.println("ARGS: " + args);
                    try
                    {
                        Class.forName("org.postgresql.Driver");
                        Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/openbis_dev", "postgres", "");

                        PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);
                        for (int aIdx = 0; aIdx < args.size(); aIdx++)
                        {
                            final Object object = args.get(aIdx);
                            if (object.getClass().isArray())
                            {
                                preparedStatement.setArray(aIdx + 1, connection.createArrayOf("bigint", (Object[]) object));
                            } else
                            {
                                preparedStatement.setObject(aIdx + 1, object);
                            }
                        }

                        ResultSet resultSet = preparedStatement.executeQuery();
                        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                        List<String> columnNames = new ArrayList<>();
                        for (int cIdx = 0; cIdx < resultSetMetaData.getColumnCount(); cIdx++)
                        {
                            columnNames.add(resultSetMetaData.getColumnName(cIdx + 1));
                        }

                        List<Map<String, Object>> results = new ArrayList<>();
                        while (resultSet.next())
                        {
                            Map<String, Object> row = new HashMap<>();
                            for (String columnName : columnNames)
                            {
                                row.put(columnName, resultSet.getObject(columnName));
                            }
                            results.add(row);
                        }

                        resultSet.close();
                        preparedStatement.close();
                        connection.close();
                        System.out.println("RESULTS: " + results);
                        return results;
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });
        // final SortAndPage sortAndPage = new SortAndPage();
        final SampleSearchManager sampleSearchManager = new SampleSearchManager(searchDAO, null);
        final Set<Long> unSortedResults = sampleSearchManager.searchForIDs(userId, sampleSearchCriteria);
        // List<Long> sortedResults = sampleSearchManager.sortAndPage(unSortedResults, sampleSearchCriteria, sampleFetchOption);

        System.out.println("Final results: " + unSortedResults);
    }
}
