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
import org.apache.commons.collections4.OrderedMap;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Set;

import static org.testng.Assert.assertEquals;

public class SampleSearchManagerDBTest
{

    public static final String PERM_ID = "20190612105000000-1";

    private SampleSearchManager searchManager;

    private JDBCSQLExecutor sqlExecutor;

    private Connection connection;

    public SampleSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeMethod
    public void setUp() throws Exception
    {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/openbis_dev", "postgres", "");
        sqlExecutor = new JDBCSQLExecutor(connection);
        final PostgresSearchDAO searchDAO = new PostgresSearchDAO(sqlExecutor);
        final ISQLAuthorisationInformationProviderDAO authInfoProviderDAO =
                new PostgresAuthorisationInformationProviderDAO(sqlExecutor);

        searchManager = new SampleSearchManager(searchDAO, authInfoProviderDAO);

        populateDB();
    }

    private void populateDB()
    {
        sqlExecutor.executeUpdate("INSERT INTO samples_all\n" +
                "(perm_id, code, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, del_id, orig_del, " +
                        "space_id, samp_id_part_of, pers_id_modifier, code_unique_check, subcode_unique_check, version, proj_id, frozen, " +
                        "frozen_for_comp, frozen_for_children, frozen_for_parents, frozen_for_data, space_frozen, proj_frozen, expe_frozen," +
                        "cont_frozen)\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Arrays.asList(PERM_ID, "DEFAULT1", "1", "1", "2019-06-12 10:50:00.000000+01", "2019-06-12 10:50:00.000000+01", "1",
                        null, null, "1", null, null,"DEFAULT,-1,1,1", null, 0, "1", false, false, false, false, false, false, false, false, false));
    }

    private void insertRecord(final OrderedMap<String, Object> valuesMap)
    {

    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        cleanDB();

        if (connection != null)
        {
            connection.close();
        }
    }

    private void cleanDB()
    {
        sqlExecutor.executeUpdate("DELETE FROM samples_all\n" +
                "WHERE perm_id=?", Arrays.asList(PERM_ID));
    }

    private void deleteRecord(final OrderedMap<String, Object> valuesMap)
    {

    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} using DB connection.
     */
    @Test
    public void testQueryDBWithStringFieldSearchCriteria()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withCode().thatEquals("DEFAULT1");

        final Set<Long> sampleIds = searchManager.searchForIDs(2L, criterion);

        assertEquals(sampleIds.size(), 1);
        assertEquals(sampleIds.iterator().next().longValue(), 1L);
    }

}