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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
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
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.CODE2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.CODE3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.CONTAINER_DELIMITER;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ID_DELIMITER;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.MODIFICATION_DATE2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.MODIFICATION_DATE_STRING2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.PERM_ID1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.PERM_ID2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.PROJECT_CODE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.PROJECT_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATION_DATE2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATION_DATE_STRING1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATION_DATE_STRING2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.SAMPLE_ID1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.SAMPLE_ID2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.SAMPLE_ID3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.SPACE_CODE1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.SPACE_CODE2;
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
     * Tests {@link StringFieldSearchCriteriaTranslator} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withCode().thatEquals(CODE1);
        checkCriterion(equalsCriterion);

        final SampleSearchCriteria containsCriterion = new SampleSearchCriteria();
        containsCriterion.withCode().thatContains(CODE1.substring(1, CODE1.length() - 1));
        checkCriterion(containsCriterion);

        final SampleSearchCriteria startsWithCriterion = new SampleSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(CODE1.substring(0, 4));
        checkCriterion(startsWithCriterion);

        final SampleSearchCriteria endsWithCriterion = new SampleSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(CODE1.substring(4));
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

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with {@link RegistrationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithRegistrationDateField()
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
     * Tests {@link StringFieldSearchCriteriaTranslator} with {@link ModificationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithModificationDateField()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withModificationDate().thatEquals(MODIFICATION_DATE2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(SAMPLE_ID2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withModificationDate().thatIsEarlierThanOrEqualTo(MODIFICATION_DATE2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(earlierThanCriterionSampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withModificationDate().thatIsLaterThanOrEqualTo(MODIFICATION_DATE2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with {@link RegistrationDateSearchCriteria} attribute search criteria (in string form) using DB
     * connection.
     */
    @Test
    public void testQueryDBWithStringRegistrationDateField()
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

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with {@link ModificationDateSearchCriteria} attribute search criteria (in string form) using DB
     * connection.
     */
    @Test
    public void testQueryDBWithStringModificationDateField()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withModificationDate().thatEquals(MODIFICATION_DATE_STRING2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(SAMPLE_ID2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withModificationDate().thatIsEarlierThanOrEqualTo(MODIFICATION_DATE_STRING2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(earlierThanCriterionSampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withModificationDate().thatIsLaterThanOrEqualTo(MODIFICATION_DATE_STRING2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with codes (collection attribute) search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCodes()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withCodes().thatIn(Arrays.asList(CODE1, CODE3));
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertEquals(criterionSampleIds.size(), 2);
        assertTrue(criterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithPermId()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withPermId().thatEquals(PERM_ID2);
        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
        assertEquals(equalsCriterionSampleIds.size(), 1);
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(equalsCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria startsWithCriterion = new SampleSearchCriteria();
        startsWithCriterion.withPermId().thatStartsWith(PERM_ID1.substring(0, PERM_ID1.length() - 2));
        final Set<Long> startsWithCriterionSampleIds = searchManager.searchForIDs(USER_ID, startsWithCriterion);
        assertEquals(startsWithCriterionSampleIds.size(), 2);
        assertTrue(startsWithCriterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(startsWithCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(startsWithCriterionSampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria endsWithCriterion = new SampleSearchCriteria();
        endsWithCriterion.withPermId().thatEndsWith(PERM_ID1.substring(PERM_ID1.length() - 4));
        final Set<Long> endsWithCriterionSampleIds = searchManager.searchForIDs(USER_ID, endsWithCriterion);
        assertEquals(endsWithCriterionSampleIds.size(), 2);
        assertTrue(endsWithCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(endsWithCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(endsWithCriterionSampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria containsCriterion = new SampleSearchCriteria();
        containsCriterion.withPermId().thatContains(PERM_ID1.substring(4, 12));
        final Set<Long> containsCriterionSampleIds = searchManager.searchForIDs(USER_ID, containsCriterion);
        assertEquals(containsCriterionSampleIds.size(), 3);
        assertTrue(containsCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(containsCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(containsCriterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with {@link AnyFieldSearchCriteria} attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithAnyField()
    {
        // code attribute
        final SampleSearchCriteria codeEqualsCriterion = new SampleSearchCriteria();
        codeEqualsCriterion.withAnyField().thatEquals(CODE2);
        final Set<Long> codeEqualsCriterionSampleIds = searchManager.searchForIDs(USER_ID, codeEqualsCriterion);
        assertEquals(codeEqualsCriterionSampleIds.size(), 1);
        assertFalse(codeEqualsCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(codeEqualsCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(codeEqualsCriterionSampleIds.contains(SAMPLE_ID3));

        // project_id attribute
        final SampleSearchCriteria projectIdEqualsCriterion = new SampleSearchCriteria();
        projectIdEqualsCriterion.withAnyField().thatEquals(String.valueOf(PROJECT_ID));
        final Set<Long> projectIdEqualsCriterionSampleIds = searchManager.searchForIDs(USER_ID, projectIdEqualsCriterion);
        assertEquals(projectIdEqualsCriterionSampleIds.size(), 2);
        assertFalse(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID3));

        // project_id attribute
        final SampleSearchCriteria registrationDateEqualsCriterion = new SampleSearchCriteria();
        registrationDateEqualsCriterion.withAnyField().thatEquals(REGISTRATION_DATE_STRING1);
        final Set<Long> registrationDateEqualsCriterionSampleIds = searchManager.searchForIDs(USER_ID, registrationDateEqualsCriterion);
        assertEquals(registrationDateEqualsCriterionSampleIds.size(), 1);
        assertTrue(registrationDateEqualsCriterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(registrationDateEqualsCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(registrationDateEqualsCriterionSampleIds.contains(SAMPLE_ID3));

        // perm_id attribute
        final SampleSearchCriteria startsWithCriterion = new SampleSearchCriteria();
        startsWithCriterion.withAnyField().thatStartsWith(PERM_ID1.substring(0, PERM_ID1.length() - 2));
        final Set<Long> startsWithCriterionSampleIds = searchManager.searchForIDs(USER_ID, startsWithCriterion);
        assertEquals(startsWithCriterionSampleIds.size(), 2);
        assertTrue(startsWithCriterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(startsWithCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(startsWithCriterionSampleIds.contains(SAMPLE_ID3));

        // code attribute
        final SampleSearchCriteria endsWithCriterion1 = new SampleSearchCriteria();
        endsWithCriterion1.withAnyField().thatEndsWith(CODE2.substring(CODE2.length() - 5));
        final Set<Long> endsWithCriterionSampleIds1 = searchManager.searchForIDs(USER_ID, endsWithCriterion1);
        assertEquals(endsWithCriterionSampleIds1.size(), 1);
        assertFalse(endsWithCriterionSampleIds1.contains(SAMPLE_ID1));
        assertTrue(endsWithCriterionSampleIds1.contains(SAMPLE_ID2));
        assertFalse(endsWithCriterionSampleIds1.contains(SAMPLE_ID3));

        // id, perm_id and samp_id_part_of fields
        final SampleSearchCriteria endsWithCriterion2 = new SampleSearchCriteria();
        endsWithCriterion2.withAnyField().thatEndsWith(String.valueOf(SAMPLE_ID1));
        final Set<Long> endsWithCriterionSampleIds2 = searchManager.searchForIDs(USER_ID, endsWithCriterion2);
        assertEquals(endsWithCriterionSampleIds2.size(), 3);
        assertTrue(endsWithCriterionSampleIds2.contains(SAMPLE_ID1));
        assertTrue(endsWithCriterionSampleIds2.contains(SAMPLE_ID2));
        assertTrue(endsWithCriterionSampleIds2.contains(SAMPLE_ID3));

        // expe_id
        final SampleSearchCriteria containsCriterion = new SampleSearchCriteria();
        containsCriterion.withAnyField().thatContains(String.valueOf(EXPERIMENT_ID));
        final Set<Long> containsCriterionSampleIds = searchManager.searchForIDs(USER_ID, containsCriterion);
        assertEquals(containsCriterionSampleIds.size(), 1);
        assertFalse(containsCriterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(containsCriterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(containsCriterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with without experiment attribute search criteria using DB connection.
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
     * Tests {@link StringFieldSearchCriteriaTranslator} with without project attribute search criteria using DB connection.
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
     * Tests {@link StringFieldSearchCriteriaTranslator} with without space attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutSpaceField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutSpace();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertFalse(criterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID2));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with without container attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutContainerField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutContainer();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(SAMPLE_ID1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with identifier search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithId()
    {
        final SampleSearchCriteria permIdCriterion = new SampleSearchCriteria();
        permIdCriterion.withId().thatEquals(new SamplePermId(PERM_ID2));
        final Set<Long> permIdCriterionSampleIds = searchManager.searchForIDs(USER_ID, permIdCriterion);
        assertEquals(permIdCriterionSampleIds.size(), 1);
        assertFalse(permIdCriterionSampleIds.contains(SAMPLE_ID1));
        assertTrue(permIdCriterionSampleIds.contains(SAMPLE_ID2));
        assertFalse(permIdCriterionSampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria identifierCriterion1 = new SampleSearchCriteria();
        identifierCriterion1.withId().thatEquals(new SampleIdentifier(ID_DELIMITER + CODE2));
        final Set<Long> identifierCriterion1SampleIds = searchManager.searchForIDs(USER_ID, identifierCriterion1);
        assertEquals(identifierCriterion1SampleIds.size(), 1);
        assertFalse(identifierCriterion1SampleIds.contains(SAMPLE_ID1));
        assertTrue(identifierCriterion1SampleIds.contains(SAMPLE_ID2));
        assertFalse(identifierCriterion1SampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria identifierCriterion2 = new SampleSearchCriteria();
        identifierCriterion2.withId().thatEquals(new SampleIdentifier(ID_DELIMITER + SPACE_CODE1 + ID_DELIMITER + CODE1));
        final Set<Long> identifierCriterion2SampleIds = searchManager.searchForIDs(USER_ID, identifierCriterion2);
        assertEquals(identifierCriterion2SampleIds.size(), 1);
        assertTrue(identifierCriterion2SampleIds.contains(SAMPLE_ID1));
        assertFalse(identifierCriterion2SampleIds.contains(SAMPLE_ID2));
        assertFalse(identifierCriterion2SampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria identifierCriterion3 = new SampleSearchCriteria();
        identifierCriterion3.withId().thatEquals(new SampleIdentifier(ID_DELIMITER + SPACE_CODE2 + ID_DELIMITER + PROJECT_CODE + ID_DELIMITER +
                CODE2));
        final Set<Long> identifierCriterion3SampleIds = searchManager.searchForIDs(USER_ID, identifierCriterion3);
        assertEquals(identifierCriterion3SampleIds.size(), 1);
        assertFalse(identifierCriterion3SampleIds.contains(SAMPLE_ID1));
        assertTrue(identifierCriterion3SampleIds.contains(SAMPLE_ID2));
        assertFalse(identifierCriterion3SampleIds.contains(SAMPLE_ID3));

        final SampleSearchCriteria identifierCriterion4 = new SampleSearchCriteria();
        identifierCriterion4.withId().thatEquals(new SampleIdentifier(ID_DELIMITER + SPACE_CODE2 + ID_DELIMITER + PROJECT_CODE + ID_DELIMITER +
                CODE1 + CONTAINER_DELIMITER + CODE2));
        final Set<Long> identifierCriterion4SampleIds = searchManager.searchForIDs(USER_ID, identifierCriterion4);
        assertEquals(identifierCriterion4SampleIds.size(), 1);
        assertFalse(identifierCriterion4SampleIds.contains(SAMPLE_ID1));
        assertTrue(identifierCriterion4SampleIds.contains(SAMPLE_ID2));
        assertFalse(identifierCriterion4SampleIds.contains(SAMPLE_ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithProperty()
    {
//        final SampleSearchCriteria permIdCriterion = new SampleSearchCriteria();
//        permIdCriterion.withProperty("");
//
//        final Set<Long> permIdCriterionSampleIds = searchManager.searchForIDs(USER_ID, permIdCriterion);
//        assertEquals(permIdCriterionSampleIds.size(), 1);
//        assertFalse(permIdCriterionSampleIds.contains(SAMPLE_ID1));
//        assertTrue(permIdCriterionSampleIds.contains(SAMPLE_ID2));
//        assertFalse(permIdCriterionSampleIds.contains(SAMPLE_ID3));
    }

}