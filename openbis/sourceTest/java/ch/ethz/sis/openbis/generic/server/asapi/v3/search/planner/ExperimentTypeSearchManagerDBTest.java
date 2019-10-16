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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ADMIN_USER_TECH_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_TYPE_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_TYPE_CODE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_TYPE_CODE_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_TYPE_CODE_4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_TYPE_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_TYPE_ID_4;
import static org.testng.Assert.assertEquals;

public class ExperimentTypeSearchManagerDBTest
{

    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("testApplicationContext.xml");

    private DBTestHelper dbTestHelper = context.getBean(DBTestHelper.class);

    private ExperimentTypeSearchManager searchManager;

    public ExperimentTypeSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUpClass() throws Exception
    {
        searchManager = context.getBean("experiment-type-search-manager", ExperimentTypeSearchManager.class);
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
    private void checkCriterion(final ExperimentTypeSearchCriteria criterion, final long experimentTypeId)
    {
        final Set<Long> experimentTypeIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertEquals(experimentTypeIds.size(), 1);
        assertEquals(experimentTypeIds.iterator().next().longValue(), experimentTypeId);
    }

    /**
     * Tests {@link ExperimentTypeSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final ExperimentTypeSearchCriteria equalsCriterion = new ExperimentTypeSearchCriteria();
        equalsCriterion.withCode().thatEquals(EXPERIMENT_TYPE_CODE_1);
        checkCriterion(equalsCriterion, EXPERIMENT_TYPE_ID_1);

        final ExperimentTypeSearchCriteria containsCriterion = new ExperimentTypeSearchCriteria();
        containsCriterion.withCode().thatContains(EXPERIMENT_TYPE_CODE_2.substring(1, EXPERIMENT_TYPE_CODE_2.length() - 1));
        final Set<Long> containsCriterionExperimentTypeIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsCriterion);
        assertEquals(containsCriterionExperimentTypeIds.size(), 3);

        final ExperimentTypeSearchCriteria startsWithCriterion = new ExperimentTypeSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(EXPERIMENT_TYPE_CODE_3.substring(0, 4));
        final Set<Long> startsWithCriterionExperimentTypeIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithCriterion);
        assertEquals(startsWithCriterionExperimentTypeIds.size(), 3);

        final ExperimentTypeSearchCriteria endsWithCriterion = new ExperimentTypeSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(EXPERIMENT_TYPE_CODE_4.substring(4));
        checkCriterion(endsWithCriterion, EXPERIMENT_TYPE_ID_4);
    }

}
