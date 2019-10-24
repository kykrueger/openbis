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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ADMIN_USER_TECH_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_TYPE_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_TYPE_CODE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_TYPE_CODE_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_TYPE_CODE_4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_TYPE_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_TYPE_ID_4;
import static org.testng.Assert.assertEquals;

public class DataSetTypeSearchManagerDBTest
{

    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("testApplicationContext.xml");

    private DBTestHelper dbTestHelper = context.getBean(DBTestHelper.class);

    private DataSetTypeSearchManager searchManager;

    public DataSetTypeSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUpClass() throws Exception
    {
        searchManager = context.getBean("data-set-type-search-manager", DataSetTypeSearchManager.class);
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
     * Checks if the criterion returns expected results.
     *
     * @param criterion criterion to be checked.
     */
    private void checkCriterion(final DataSetTypeSearchCriteria criterion, final long dataSetTypeId)
    {
        final Set<Long> dataSetTypeIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion, new DataSetTypeSortOptions());
        assertEquals(dataSetTypeIds.size(), 1);
        assertEquals(dataSetTypeIds.iterator().next().longValue(), dataSetTypeId);
    }

    /**
     * Tests {@link DataSetTypeSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final DataSetTypeSearchCriteria equalsCriterion = new DataSetTypeSearchCriteria();
        equalsCriterion.withCode().thatEquals(DATA_SET_TYPE_CODE_1);
        checkCriterion(equalsCriterion, DATA_SET_TYPE_ID_1);

        final DataSetTypeSearchCriteria containsCriterion = new DataSetTypeSearchCriteria();
        containsCriterion.withCode().thatContains(DATA_SET_TYPE_CODE_2.substring(1, DATA_SET_TYPE_CODE_2.length() - 1));
        final Set<Long> containsCriterionDataSetTypeIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsCriterion,
                new DataSetTypeSortOptions());
        assertEquals(containsCriterionDataSetTypeIds.size(), 4);

        final DataSetTypeSearchCriteria startsWithCriterion = new DataSetTypeSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(DATA_SET_TYPE_CODE_3.substring(0, 4));
        final Set<Long> startsWithCriterionDataSetTypeIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithCriterion,
                new DataSetTypeSortOptions());
        assertEquals(startsWithCriterionDataSetTypeIds.size(), 4);

        final DataSetTypeSearchCriteria endsWithCriterion = new DataSetTypeSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(DATA_SET_TYPE_CODE_4.substring(4));
        checkCriterion(endsWithCriterion, DATA_SET_TYPE_ID_4);
    }

}
