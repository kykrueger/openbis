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
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.StringFieldSearchCriteriaTranslator;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Set;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.CODE1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.CODE3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATION_DATE2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATION_DATE_STRING2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.SAMPLE_ID1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.SAMPLE_ID2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.SAMPLE_ID3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.USER_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SampleSearchManagerDBTest
{

    private SampleSearchManager searchManager;

    private DBTestHelper dbTestHelper = new DBTestHelper();

    public SampleSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUpClass() throws Exception
    {
        dbTestHelper.setUp();
    }

    @BeforeTest
    public void setUp() throws Exception
    {
        dbTestHelper.resetConnection();

        final ISQLExecutor sqlExecutor = dbTestHelper.getSqlExecutor();
        final PostgresSearchDAO searchDAO = new PostgresSearchDAO(sqlExecutor);
        final ISQLAuthorisationInformationProviderDAO authInfoProviderDAO =
                new PostgresAuthorisationInformationProviderDAO(sqlExecutor);

        searchManager = new SampleSearchManager(searchDAO, authInfoProviderDAO);
    }

    @AfterClass
    public void tearDownClass() throws Exception
    {
        dbTestHelper.cleanDB();
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with string field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withCode().thatEquals(CODE1);

        final SampleSearchCriteria containsCriterion = new SampleSearchCriteria();
        containsCriterion.withCode().thatContains(CODE1.substring(1, CODE1.length() - 1));

        final SampleSearchCriteria startsWithCriterion = new SampleSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(CODE1.substring(0, 4));

        final SampleSearchCriteria endsWithCriterion = new SampleSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(CODE1.substring(4));

        checkCriterion(equalsCriterion);
        checkCriterion(containsCriterion);
        checkCriterion(startsWithCriterion);
        checkCriterion(endsWithCriterion);
    }

    /**
     * Checks if the criterion returns expected results.
     *
     * @param criterion criterion to be checked.
     */
    private void checkCriterion(final SampleSearchCriteria criterion)
    {
        final Set<Long> sampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertEquals(sampleIds.size(), 1);
        assertEquals(sampleIds.iterator().next().longValue(), SAMPLE_ID1);
    }

//    /**
//     * Tests {@link StringFieldSearchCriteriaTranslator} using DB connection.
//     */
//    @Test
//    public void testQueryDBWithNumberField()
//    {
//        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
//        equalsCriterion.withCode().thatEquals(VERSION2);
//        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
//        assertEquals(equalsCriterionSampleIds.size(), 1);
//        assertEquals(equalsCriterionSampleIds.iterator().next().longValue(), SAMPLE_ID2);
//
//        final SampleSearchCriteria lessThanCriterion = new SampleSearchCriteria();
//        lessThanCriterion.withNumberProperty(ColumnNames.VERSION_COLUMN).thatIsLessThan(VERSION2);
//        final Set<Long> lessThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, lessThanCriterion);
//        assertFalse(lessThanCriterionSampleIds.isEmpty());
//        assertTrue(lessThanCriterionSampleIds.contains(SAMPLE_ID1));
//        assertFalse(lessThanCriterionSampleIds.contains(SAMPLE_ID2));
//        assertFalse(lessThanCriterionSampleIds.contains(SAMPLE_ID3));
//
//        final SampleSearchCriteria lessThanOrEqualsToCriterion = new SampleSearchCriteria();
//        lessThanOrEqualsToCriterion.withNumberProperty(ColumnNames.VERSION_COLUMN).thatIsLessThanOrEqualTo(VERSION2);
//        final Set<Long> lessThanOrEqualsToCriterionSampleIds = searchManager.searchForIDs(USER_ID, lessThanOrEqualsToCriterion);
//        assertFalse(lessThanOrEqualsToCriterionSampleIds.isEmpty());
//        assertTrue(lessThanOrEqualsToCriterionSampleIds.contains(SAMPLE_ID1));
//        assertTrue(lessThanOrEqualsToCriterionSampleIds.contains(SAMPLE_ID2));
//        assertFalse(lessThanOrEqualsToCriterionSampleIds.contains(SAMPLE_ID3));
//
//        final SampleSearchCriteria greaterThanCriterion = new SampleSearchCriteria();
//        greaterThanCriterion.withNumberProperty(ColumnNames.VERSION_COLUMN).thatIsGreaterThan(VERSION2);
//        final Set<Long> greaterThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, greaterThanCriterion);
//        assertFalse(greaterThanCriterionSampleIds.isEmpty());
//        assertFalse(greaterThanCriterionSampleIds.contains(SAMPLE_ID1));
//        assertFalse(greaterThanCriterionSampleIds.contains(SAMPLE_ID2));
//        assertTrue(greaterThanCriterionSampleIds.contains(SAMPLE_ID3));
//
//        final SampleSearchCriteria greaterThanOrEqualsToCriterion = new SampleSearchCriteria();
//        greaterThanOrEqualsToCriterion.withNumberProperty(ColumnNames.VERSION_COLUMN).thatIsGreaterThanOrEqualTo(VERSION2);
//        final Set<Long> greaterThanOrEqualsToCriterionSampleIds = searchManager.searchForIDs(USER_ID, greaterThanOrEqualsToCriterion);
//        assertFalse(greaterThanOrEqualsToCriterionSampleIds.isEmpty());
//        assertFalse(greaterThanOrEqualsToCriterionSampleIds.contains(SAMPLE_ID1));
//        assertTrue(greaterThanOrEqualsToCriterionSampleIds.contains(SAMPLE_ID2));
//        assertTrue(greaterThanOrEqualsToCriterionSampleIds.contains(SAMPLE_ID3));
//    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with date field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithDateField()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(REGISTRATION_DATE2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(SAMPLE_ID2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(REGISTRATION_DATE2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(earlierThanCriterionSampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(REGISTRATION_DATE2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with date field search criteria (in string form) using DB connection.
     */
    @Test
    public void testQueryDBWithStringDateField()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(REGISTRATION_DATE_STRING2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(SAMPLE_ID2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(REGISTRATION_DATE_STRING2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(earlierThanCriterionSampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(REGISTRATION_DATE_STRING2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID3));
    }

//    /**
//     * Tests {@link StringFieldSearchCriteriaTranslator} with boolean field search criteria using DB connection.
//     */
//    @Test
//    public void testQueryDBWithBooleanField()
//    {
//        final SampleSearchCriteria criterion = new SampleSearchCriteria();
//    }

//    /**
//     * Tests {@link StringFieldSearchCriteriaTranslator} with enum field search criteria using DB connection.
//     */
//    @Test
//    public void testQueryDBWithEnumField()
//    {
//        final SampleSearchCriteria criterion = new SampleSearchCriteria();
//    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCollectionField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withCodes().thatIn(Arrays.asList(CODE1, CODE3));
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertEquals(criterionSampleIds.size(), 2);
        assertTrue(criterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID3));
    }

//    /**
//     * Tests {@link StringFieldSearchCriteriaTranslator} with string field search criteria using DB connection.
//     */
//    @Test
//    public void testQueryDBWithPermId()
//    {
//        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
//        equalsCriterion.withPermId().thatEquals(PERM_ID2);
//        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
//        assertEquals(equalsCriterionSampleIds.size(), 1);
//        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID1));
//        assertTrue(equalsCriterionSampleIds.contains(SAMPLE_ID2));
//        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID3));
//    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutExperimentField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutExperiment();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutProjectField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutProject();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutSpaceField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutSpace();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertFalse(criterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutContainerField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutContainer();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID3));
    }

}