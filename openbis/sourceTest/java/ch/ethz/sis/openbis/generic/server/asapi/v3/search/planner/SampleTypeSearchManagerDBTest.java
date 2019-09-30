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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.PostgresSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.LISTABLE_SAMPLE_TYPE_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.NOT_LISTABLE_SAMPLE_TYPE_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_TYPE_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_TYPE_CODE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_TYPE_CODE_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_TYPE_CODE_4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_TYPE_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_TYPE_ID_4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.USER_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SampleTypeSearchManagerDBTest
{

    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("testApplicationContext.xml");

    private SamplesDBTestHelper dbTestHelper = context.getBean(SamplesDBTestHelper.class);

    private PostgresSearchDAO searchDAO;

    private ISQLAuthorisationInformationProviderDAO authInfoProviderDAO;

    private IID2PETranslator<Long> iid2PETranslator;

    private SampleTypeSearchManager searchManager;

    public SampleTypeSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUpClass() throws Exception
    {
        searchDAO = context.getBean(PostgresSearchDAO.class);
        authInfoProviderDAO = context.getBean(ISQLAuthorisationInformationProviderDAO.class);
        iid2PETranslator = context.getBean("sample-id-2-pet-translator", IID2PETranslator.class);
        searchManager = new SampleTypeSearchManager(searchDAO, authInfoProviderDAO, iid2PETranslator);
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
    private void checkCriterion(final SampleTypeSearchCriteria criterion, final long sampleTypeId)
    {
        final Set<Long> sampleTypeIds = searchManager.searchForIDs(USER_ID, criterion);
        assertEquals(sampleTypeIds.size(), 1);
        assertEquals(sampleTypeIds.iterator().next().longValue(), sampleTypeId);
    }

    /**
     * Tests {@link SampleTypeSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final SampleTypeSearchCriteria equalsCriterion = new SampleTypeSearchCriteria();
        equalsCriterion.withCode().thatEquals(SAMPLE_TYPE_CODE_1);
        checkCriterion(equalsCriterion, SAMPLE_TYPE_ID_1);

        final SampleTypeSearchCriteria containsCriterion = new SampleTypeSearchCriteria();
        containsCriterion.withCode().thatContains(SAMPLE_TYPE_CODE_2.substring(1, SAMPLE_TYPE_CODE_2.length() - 1));
        final Set<Long> containsCriterionSampleTypeIds = searchManager.searchForIDs(USER_ID, containsCriterion);
        assertEquals(containsCriterionSampleTypeIds.size(), 5);

        final SampleTypeSearchCriteria startsWithCriterion = new SampleTypeSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(SAMPLE_TYPE_CODE_3.substring(0, 4));
        final Set<Long> startsWithCriterionSampleTypeIds = searchManager.searchForIDs(USER_ID, containsCriterion);
        assertEquals(startsWithCriterionSampleTypeIds.size(), 5);

        final SampleTypeSearchCriteria endsWithCriterion = new SampleTypeSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(SAMPLE_TYPE_CODE_4.substring(4));
        checkCriterion(endsWithCriterion, SAMPLE_TYPE_ID_4);
    }

    /**
     * Tests {@link SampleTypeSearchManager} with boolean search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithListable()
    {
        final SampleTypeSearchCriteria trueCriterion = new SampleTypeSearchCriteria();
        trueCriterion.withListable().thatEquals(true);
        final Set<Long> trueCriterionSampleTypeIds = searchManager.searchForIDs(USER_ID, trueCriterion);
        assertTrue(trueCriterionSampleTypeIds.contains(LISTABLE_SAMPLE_TYPE_ID));

        final SampleTypeSearchCriteria falseCriterion = new SampleTypeSearchCriteria();
        falseCriterion.withListable().thatEquals(false);
        final Set<Long> falseCriterionSampleTypeIds = searchManager.searchForIDs(USER_ID, falseCriterion);
        assertTrue(falseCriterionSampleTypeIds.contains(NOT_LISTABLE_SAMPLE_TYPE_ID));
    }

}