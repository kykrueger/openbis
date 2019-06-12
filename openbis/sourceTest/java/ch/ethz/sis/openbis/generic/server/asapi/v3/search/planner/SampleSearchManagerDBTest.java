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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.PostgresAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.PostgresSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.JDBCSQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.TO_DELETE.StringFieldSearchCriteriaTranslator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class SampleSearchManagerDBTest
{

    private SampleSearchManager searchManager;

    private Connection connection;

    public SampleSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeMethod
    public void setUp() throws Exception
    {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/openbis_dev", "postgres", "");
        final JDBCSQLExecutor sqlExecutor = new JDBCSQLExecutor(connection);
        final PostgresSearchDAO searchDAO = new PostgresSearchDAO(sqlExecutor);
        final ISQLAuthorisationInformationProviderDAO authInfoProviderDAO =
                new PostgresAuthorisationInformationProviderDAO(sqlExecutor);

        searchManager = new SampleSearchManager(searchDAO, authInfoProviderDAO);
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        if (connection != null)
        {
            connection.close();
        }
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} using DB connection.
     */
    @Test
    public void testQueryDBWithStringFieldSearchCriteria()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withCode().thatEquals("DEFAULT");

        final Set<Long> sampleIds = searchManager.searchForIDs(2L, criterion);

        assertEquals(sampleIds.size(), 1);
        assertEquals(sampleIds.iterator().next().longValue(), 1L);
    }

}