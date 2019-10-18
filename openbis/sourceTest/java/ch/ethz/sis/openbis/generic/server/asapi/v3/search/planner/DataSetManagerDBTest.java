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

import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ADMIN_USER_TECH_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_ID_1;
import static org.testng.Assert.assertEquals;

public class DataSetManagerDBTest
{
    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("testApplicationContext.xml");

    private DBTestHelper dbTestHelper = context.getBean(DBTestHelper.class);

    private DataSetSearchManager searchManager;

    public DataSetManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUpClass() throws Exception
    {
        searchManager = context.getBean("data-set-search-manager", DataSetSearchManager.class);
    }

    @AfterClass
    public void tearDownClass() throws Exception
    {
        context.registerShutdownHook();
    }

    @AfterMethod
    public void tearDown() throws Exception
    {
        dbTestHelper.resetConnection();
    }

    /**
     * Tests {@link DataSetSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withCode().thatEquals(DATA_SET_CODE_1);
        checkCodeCriterion(equalsCriterion);

        final DataSetSearchCriteria containsCriterion = new DataSetSearchCriteria();
        containsCriterion.withCode().thatContains(DATA_SET_CODE_1.substring(1, DATA_SET_CODE_1.length() - 1));
        checkCodeCriterion(containsCriterion);

        final DataSetSearchCriteria startsWithCriterion = new DataSetSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(DATA_SET_CODE_1.substring(0, 4));
        checkCodeCriterion(startsWithCriterion);

        final DataSetSearchCriteria endsWithCriterion = new DataSetSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(DATA_SET_CODE_1.substring(4));
        checkCodeCriterion(endsWithCriterion);
    }

    /**
     * Checks if the criterion returns expected results.
     *
     * @param criterion criterion to be checked.
     */
    private void checkCodeCriterion(final DataSetSearchCriteria criterion)
    {
        final Set<Long> ids = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertEquals(ids.size(), 1);
        assertEquals(ids.iterator().next().longValue(), DATA_SET_ID_1);
    }

}
