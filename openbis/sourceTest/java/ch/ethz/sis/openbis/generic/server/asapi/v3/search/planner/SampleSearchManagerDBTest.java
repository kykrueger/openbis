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

import java.util.Arrays;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.PostgresSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.ADMIN_USER_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.ADMIN_USER_TECH_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.CODE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.CODE_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.CODE_4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.CONTAINER_DELIMITER;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.EXPERIMENT_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.ID_DELIMITER;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.INTERNAL_SAMPLE_PROPERTY_CODE_STRING;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.MODIFICATION_DATE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.MODIFICATION_DATE_STRING_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.MODIFIER_EMAIL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.MODIFIER_FIRST_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.MODIFIER_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.MODIFIER_LAST_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.MODIFIER_USER_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.PERM_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.PERM_ID_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.PROJECT_CODE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.PROJECT_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.REGISTRATION_DATE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.REGISTRATION_DATE_STRING_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.REGISTRATION_DATE_STRING_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.REGISTRATOR_EMAIL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.REGISTRATOR_FIRST_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.REGISTRATOR_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.REGISTRATOR_LAST_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.REGISTRATOR_USER_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_ID_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_ID_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_ID_4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_ID_5;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_ID_6;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_1_NUMBER_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_2_DATE_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_2_DATE_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_2_EARLIER_DATE_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_2_EARLIER_DATE_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_2_LATER_DATE_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_2_LATER_DATE_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_2_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_3_NUMBER_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_CODE_DATE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_CODE_DOUBLE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_CODE_LONG;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SAMPLE_PROPERTY_CODE_STRING;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SPACE_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SamplesDBTestHelper.SPACE_CODE_2;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SampleSearchManagerDBTest
{

    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("testApplicationContext.xml");

    private SamplesDBTestHelper dbTestHelper = context.getBean(SamplesDBTestHelper.class);

    private PostgresSearchDAO searchDAO;

    private ISQLAuthorisationInformationProviderDAO authInfoProviderDAO;

    private IID2PETranslator iid2PETranslator;

    private SampleSearchManager searchManager;

    public SampleSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUpClass() throws Exception
    {
        searchDAO = context.getBean(PostgresSearchDAO.class);
        authInfoProviderDAO = context.getBean(ISQLAuthorisationInformationProviderDAO.class);
        iid2PETranslator = context.getBean("identity-translator", IID2PETranslator.class);
        searchManager = new SampleSearchManager(searchDAO, authInfoProviderDAO, iid2PETranslator);
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
     * Tests {@link SampleSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withCode().thatEquals(CODE_1);
        checkCriterion(equalsCriterion);

        final SampleSearchCriteria containsCriterion = new SampleSearchCriteria();
        containsCriterion.withCode().thatContains(CODE_1.substring(1, CODE_1.length() - 1));
        checkCriterion(containsCriterion);

        final SampleSearchCriteria startsWithCriterion = new SampleSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(CODE_1.substring(0, 4));
        checkCriterion(startsWithCriterion);

        final SampleSearchCriteria endsWithCriterion = new SampleSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(CODE_1.substring(4));
        checkCriterion(endsWithCriterion);
    }

    /**
     * Checks if the criterion returns expected results.
     *
     * @param criterion criterion to be checked.
     */
    private void checkCriterion(final SampleSearchCriteria criterion)
    {
        final Set<Long> sampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertEquals(sampleIds.size(), 1);
        assertEquals(sampleIds.iterator().next().longValue(), SAMPLE_ID_1);
    }

    /**
     * Tests {@link SampleSearchManager} with {@link RegistrationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithRegistrationDateField()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(REGISTRATION_DATE_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(SAMPLE_ID_2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(REGISTRATION_DATE_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(REGISTRATION_DATE_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with {@link ModificationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithModificationDateField()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withModificationDate().thatEquals(MODIFICATION_DATE_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(SAMPLE_ID_2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withModificationDate().thatIsEarlierThanOrEqualTo(MODIFICATION_DATE_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withModificationDate().thatIsLaterThanOrEqualTo(MODIFICATION_DATE_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with {@link RegistrationDateSearchCriteria} attribute search criteria (in string form) using DB
     * connection.
     */
    @Test
    public void testQueryDBWithStringRegistrationDateField()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(REGISTRATION_DATE_STRING_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(SAMPLE_ID_2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(REGISTRATION_DATE_STRING_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(REGISTRATION_DATE_STRING_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with {@link ModificationDateSearchCriteria} attribute search criteria (in string form) using DB
     * connection.
     */
    @Test
    public void testQueryDBWithStringModificationDateField()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withModificationDate().thatEquals(MODIFICATION_DATE_STRING_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(SAMPLE_ID_2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withModificationDate().thatIsEarlierThanOrEqualTo(MODIFICATION_DATE_STRING_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withModificationDate().thatIsLaterThanOrEqualTo(MODIFICATION_DATE_STRING_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with codes (collection attribute) search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCodes()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withCodes().thatIn(Arrays.asList(CODE_1, CODE_3));
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertEquals(criterionSampleIds.size(), 2);
        assertTrue(criterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithPermId()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withPermId().thatEquals(PERM_ID_2);
        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionSampleIds.size(), 1);
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(equalsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria startsWithCriterion = new SampleSearchCriteria();
        startsWithCriterion.withPermId().thatStartsWith(PERM_ID_1.substring(0, PERM_ID_1.length() - 2));
        final Set<Long> startsWithCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithCriterion);
        assertEquals(startsWithCriterionSampleIds.size(), 2);
        assertTrue(startsWithCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(startsWithCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(startsWithCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria endsWithCriterion = new SampleSearchCriteria();
        endsWithCriterion.withPermId().thatEndsWith(PERM_ID_1.substring(PERM_ID_1.length() - 4));
        final Set<Long> endsWithCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion);
        assertEquals(endsWithCriterionSampleIds.size(), 2);
        assertTrue(endsWithCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(endsWithCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(endsWithCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria containsCriterion = new SampleSearchCriteria();
        containsCriterion.withPermId().thatContains(PERM_ID_1.substring(4, 12));
        final Set<Long> containsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsCriterion);
        assertEquals(containsCriterionSampleIds.size(), 3);
        assertTrue(containsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(containsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(containsCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with {@link AnyFieldSearchCriteria} attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithAnyField()
    {
        // code attribute
        final SampleSearchCriteria codeEqualsCriterion = new SampleSearchCriteria();
        codeEqualsCriterion.withAnyField().thatEquals(CODE_2);
        final Set<Long> codeEqualsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, codeEqualsCriterion);
        assertEquals(codeEqualsCriterionSampleIds.size(), 1);
        assertFalse(codeEqualsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(codeEqualsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(codeEqualsCriterionSampleIds.contains(SAMPLE_ID_3));

        // project_id attribute
        final SampleSearchCriteria projectIdEqualsCriterion = new SampleSearchCriteria();
        projectIdEqualsCriterion.withAnyField().thatEquals(String.valueOf(PROJECT_ID));
        final Set<Long> projectIdEqualsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, projectIdEqualsCriterion);
        assertEquals(projectIdEqualsCriterionSampleIds.size(), 5);
        assertFalse(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID_3));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID_4));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID_5));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(SAMPLE_ID_6));

        // project_id attribute
        final SampleSearchCriteria registrationDateEqualsCriterion = new SampleSearchCriteria();
        registrationDateEqualsCriterion.withAnyField().thatEquals(REGISTRATION_DATE_STRING_1);
        final Set<Long> registrationDateEqualsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, registrationDateEqualsCriterion);
        assertEquals(registrationDateEqualsCriterionSampleIds.size(), 1);
        assertTrue(registrationDateEqualsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(registrationDateEqualsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(registrationDateEqualsCriterionSampleIds.contains(SAMPLE_ID_3));

        // perm_id attribute
        final SampleSearchCriteria startsWithCriterion = new SampleSearchCriteria();
        startsWithCriterion.withAnyField().thatStartsWith(PERM_ID_1.substring(0, PERM_ID_1.length() - 2));
        final Set<Long> startsWithCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithCriterion);
        assertEquals(startsWithCriterionSampleIds.size(), 2);
        assertTrue(startsWithCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(startsWithCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(startsWithCriterionSampleIds.contains(SAMPLE_ID_3));

        // code attribute
        final SampleSearchCriteria endsWithCriterion1 = new SampleSearchCriteria();
        endsWithCriterion1.withAnyField().thatEndsWith(CODE_2.substring(CODE_2.length() - 5));
        final Set<Long> endsWithCriterionSampleIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion1);
        assertEquals(endsWithCriterionSampleIds1.size(), 1);
        assertFalse(endsWithCriterionSampleIds1.contains(SAMPLE_ID_1));
        assertTrue(endsWithCriterionSampleIds1.contains(SAMPLE_ID_2));
        assertFalse(endsWithCriterionSampleIds1.contains(SAMPLE_ID_3));

        // id, perm_id and samp_id_part_of fields
        final SampleSearchCriteria endsWithCriterion2 = new SampleSearchCriteria();
        endsWithCriterion2.withAnyField().thatEndsWith(String.valueOf(SAMPLE_ID_1));
        final Set<Long> endsWithCriterionSampleIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion2);
        assertEquals(endsWithCriterionSampleIds2.size(), 3);
        assertTrue(endsWithCriterionSampleIds2.contains(SAMPLE_ID_1));
        assertTrue(endsWithCriterionSampleIds2.contains(SAMPLE_ID_2));
        assertTrue(endsWithCriterionSampleIds2.contains(SAMPLE_ID_3));
        assertFalse(endsWithCriterionSampleIds2.contains(SAMPLE_ID_4));
        assertFalse(endsWithCriterionSampleIds2.contains(SAMPLE_ID_4));

        // expe_id
        final SampleSearchCriteria containsCriterion = new SampleSearchCriteria();
        containsCriterion.withAnyField().thatContains(String.valueOf(EXPERIMENT_ID));
        final Set<Long> containsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsCriterion);
        assertEquals(containsCriterionSampleIds.size(), 1);
        assertFalse(containsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(containsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(containsCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with without experiment attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutExperimentField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutExperiment();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with without project attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutProjectField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutProject();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with without space attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutSpaceField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutSpace();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertFalse(criterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(criterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with without container attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutContainerField()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutContainer();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(criterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with ID search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithId()
    {
        final SampleSearchCriteria permIdCriterion = new SampleSearchCriteria();
        permIdCriterion.withId().thatEquals(new SamplePermId(PERM_ID_2));
        final Set<Long> permIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, permIdCriterion);
        assertEquals(permIdCriterionSampleIds.size(), 1);
        assertFalse(permIdCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(permIdCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(permIdCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria identifierCriterion1 = new SampleSearchCriteria();
        identifierCriterion1.withId().thatEquals(new SampleIdentifier(ID_DELIMITER + CODE_2));
        final Set<Long> identifierCriterion1SampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, identifierCriterion1);
        assertEquals(identifierCriterion1SampleIds.size(), 1);
        assertFalse(identifierCriterion1SampleIds.contains(SAMPLE_ID_1));
        assertTrue(identifierCriterion1SampleIds.contains(SAMPLE_ID_2));
        assertFalse(identifierCriterion1SampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria identifierCriterion2 = new SampleSearchCriteria();
        identifierCriterion2.withId().thatEquals(new SampleIdentifier(ID_DELIMITER + SPACE_CODE_1 + ID_DELIMITER + CODE_1));
        final Set<Long> identifierCriterion2SampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, identifierCriterion2);
        assertEquals(identifierCriterion2SampleIds.size(), 1);
        assertTrue(identifierCriterion2SampleIds.contains(SAMPLE_ID_1));
        assertFalse(identifierCriterion2SampleIds.contains(SAMPLE_ID_2));
        assertFalse(identifierCriterion2SampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria identifierCriterion3 = new SampleSearchCriteria();
        identifierCriterion3.withId().thatEquals(new SampleIdentifier(ID_DELIMITER + SPACE_CODE_2 + ID_DELIMITER + PROJECT_CODE + ID_DELIMITER +
                CODE_2));
        final Set<Long> identifierCriterion3SampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, identifierCriterion3);
        assertEquals(identifierCriterion3SampleIds.size(), 1);
        assertFalse(identifierCriterion3SampleIds.contains(SAMPLE_ID_1));
        assertTrue(identifierCriterion3SampleIds.contains(SAMPLE_ID_2));
        assertFalse(identifierCriterion3SampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria identifierCriterion4 = new SampleSearchCriteria();
        identifierCriterion4.withId().thatEquals(new SampleIdentifier(ID_DELIMITER + SPACE_CODE_2 + ID_DELIMITER + PROJECT_CODE + ID_DELIMITER +
                CODE_1 + CONTAINER_DELIMITER + CODE_2));
        final Set<Long> identifierCriterion4SampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, identifierCriterion4);
        assertEquals(identifierCriterion4SampleIds.size(), 1);
        assertFalse(identifierCriterion4SampleIds.contains(SAMPLE_ID_1));
        assertTrue(identifierCriterion4SampleIds.contains(SAMPLE_ID_2));
        assertFalse(identifierCriterion4SampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with string property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithAnyProperty()
    {
        final SampleSearchCriteria anyPropertyCriterion = new SampleSearchCriteria();
        anyPropertyCriterion.withAnyProperty();
        final Set<Long> anyPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, anyPropertyCriterion);
        assertTrue(anyPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(anyPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(anyPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria equalsNumberPropertyCriterion = new SampleSearchCriteria();
        equalsNumberPropertyCriterion.withAnyProperty().thatEquals(String.valueOf(SAMPLE_PROPERTY_1_NUMBER_VALUE));
        final Set<Long> equalsNumberPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsNumberPropertyCriterion);
        assertEquals(equalsNumberPropertyCriterionSampleIds.size(), 1);
        assertTrue(equalsNumberPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(equalsNumberPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(equalsNumberPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria equalsStringPropertyCriterion = new SampleSearchCriteria();
        equalsStringPropertyCriterion.withAnyProperty().thatEquals(SAMPLE_PROPERTY_2_STRING_VALUE);
        final Set<Long> equalsStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterion);
        assertEquals(equalsStringPropertyCriterionSampleIds.size(), 1);
        assertFalse(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria equalsStringPropertyCriterionNotFound = new SampleSearchCriteria();
        equalsStringPropertyCriterionNotFound.withAnyProperty().thatEquals(SAMPLE_PROPERTY_2_STRING_VALUE + "-");
        final Set<Long> equalsStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterionNotFound);
        assertEquals(equalsStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria startsWithStringPropertyCriterion = new SampleSearchCriteria();
        startsWithStringPropertyCriterion.withAnyProperty().thatStartsWith(SAMPLE_PROPERTY_2_STRING_VALUE.substring(0, 10));
        final Set<Long> startsWithStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithStringPropertyCriterion);
        assertEquals(startsWithStringPropertyCriterionSampleIds.size(), 1);
        assertFalse(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria startsWithStringPropertyCriterionNotFound = new SampleSearchCriteria();
        startsWithStringPropertyCriterionNotFound.withAnyProperty().thatStartsWith(
                SAMPLE_PROPERTY_2_STRING_VALUE.substring(0, 10) + "-");
        final Set<Long> startsWithStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                startsWithStringPropertyCriterionNotFound);
        assertEquals(startsWithStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria endsWithStringPropertyCriterion = new SampleSearchCriteria();
        endsWithStringPropertyCriterion.withAnyProperty().thatEndsWith(SAMPLE_PROPERTY_2_STRING_VALUE.substring(10));
        final Set<Long> endsWithStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithStringPropertyCriterion);
        assertEquals(endsWithStringPropertyCriterionSampleIds.size(), 1);
        assertFalse(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria endsWithStringPropertyCriterionNotFound = new SampleSearchCriteria();
        endsWithStringPropertyCriterionNotFound.withAnyProperty().thatEndsWith(SAMPLE_PROPERTY_2_STRING_VALUE.substring(10)
                + "-");
        final Set<Long> endsWithStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                endsWithStringPropertyCriterionNotFound);
        assertEquals(endsWithStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria containsStringPropertyCriterion = new SampleSearchCriteria();
        containsStringPropertyCriterion.withAnyProperty().thatContains(SAMPLE_PROPERTY_2_STRING_VALUE.substring(3, 10));
        final Set<Long> containsStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsStringPropertyCriterion);
        assertEquals(containsStringPropertyCriterionSampleIds.size(), 1);
        assertFalse(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria containsStringPropertyCriterionNotFound = new SampleSearchCriteria();
        containsStringPropertyCriterionNotFound.withAnyProperty().thatContains(SAMPLE_PROPERTY_2_STRING_VALUE.substring(3, 10)
                + "-");
        final Set<Long> containsStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                containsStringPropertyCriterionNotFound);
        assertEquals(containsStringPropertyCriterionSampleIdsNotFound.size(), 0);
    }

    /**
     * Tests {@link SampleSearchManager} with string property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithStringProperty()
    {
        final SampleSearchCriteria anyStringPropertyCriterion = new SampleSearchCriteria();
        anyStringPropertyCriterion.withProperty(SAMPLE_PROPERTY_CODE_STRING);
        final Set<Long> anyStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, anyStringPropertyCriterion);
        assertFalse(anyStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(anyStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(anyStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria equalsStringPropertyCriterion = new SampleSearchCriteria();
        equalsStringPropertyCriterion.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatEquals(SAMPLE_PROPERTY_2_STRING_VALUE);
        final Set<Long> equalsStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterion);
        assertEquals(equalsStringPropertyCriterionSampleIds.size(), 1);
        assertFalse(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria equalsStringPropertyCriterionNotFound = new SampleSearchCriteria();
        equalsStringPropertyCriterionNotFound.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatEquals(SAMPLE_PROPERTY_2_STRING_VALUE + "-");
        final Set<Long> equalsStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterionNotFound);
        assertEquals(equalsStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria startsWithStringPropertyCriterion = new SampleSearchCriteria();
        startsWithStringPropertyCriterion.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatStartsWith(SAMPLE_PROPERTY_2_STRING_VALUE.substring(0, 10));
        final Set<Long> startsWithStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithStringPropertyCriterion);
        assertEquals(startsWithStringPropertyCriterionSampleIds.size(), 1);
        assertFalse(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria startsWithStringPropertyCriterionNotFound = new SampleSearchCriteria();
        startsWithStringPropertyCriterionNotFound.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatStartsWith(
                SAMPLE_PROPERTY_2_STRING_VALUE.substring(0, 10) + "-");
        final Set<Long> startsWithStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                startsWithStringPropertyCriterionNotFound);
        assertEquals(startsWithStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria endsWithStringPropertyCriterion = new SampleSearchCriteria();
        endsWithStringPropertyCriterion.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatEndsWith(SAMPLE_PROPERTY_2_STRING_VALUE.substring(10));
        final Set<Long> endsWithStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithStringPropertyCriterion);
        assertEquals(endsWithStringPropertyCriterionSampleIds.size(), 1);
        assertFalse(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria endsWithStringPropertyCriterionNotFound = new SampleSearchCriteria();
        endsWithStringPropertyCriterionNotFound.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatEndsWith(SAMPLE_PROPERTY_2_STRING_VALUE.substring(10)
                + "-");
        final Set<Long> endsWithStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                endsWithStringPropertyCriterionNotFound);
        assertEquals(endsWithStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria containsStringPropertyCriterion = new SampleSearchCriteria();
        containsStringPropertyCriterion.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatContains(SAMPLE_PROPERTY_2_STRING_VALUE.substring(3, 10));
        final Set<Long> containsStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsStringPropertyCriterion);
        assertEquals(containsStringPropertyCriterionSampleIds.size(), 1);
        assertFalse(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria containsStringPropertyCriterionNotFound = new SampleSearchCriteria();
        containsStringPropertyCriterionNotFound.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatContains(SAMPLE_PROPERTY_2_STRING_VALUE.substring(3, 10)
                + "-");
        final Set<Long> containsStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                containsStringPropertyCriterionNotFound);
        assertEquals(containsStringPropertyCriterionSampleIdsNotFound.size(), 0);
    }

    /**
     * Tests {@link SampleSearchManager} with long number property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithLongNumberProperty()
    {
        // =
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_LONG).thatEquals(SAMPLE_PROPERTY_1_NUMBER_VALUE);
        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionSampleIds.size(), 1);
        assertTrue(equalsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_3));

        // >
        final SampleSearchCriteria gtCriterion = new SampleSearchCriteria();
        gtCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_LONG).thatIsGreaterThan(SAMPLE_PROPERTY_1_NUMBER_VALUE);
        final Set<Long> gtCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, gtCriterion);
        assertEquals(gtCriterionSampleIds.size(), 0);

        // >=
        final SampleSearchCriteria geCriterion = new SampleSearchCriteria();
        geCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_LONG).thatIsGreaterThan(SAMPLE_PROPERTY_1_NUMBER_VALUE - 1);
        final Set<Long> geCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, geCriterion);
        assertEquals(geCriterionSampleIds.size(), 1);
        assertTrue(geCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(geCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(geCriterionSampleIds.contains(SAMPLE_ID_3));

        // <
        final SampleSearchCriteria ltCriterion = new SampleSearchCriteria();
        ltCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_LONG).thatIsLessThan(SAMPLE_PROPERTY_1_NUMBER_VALUE);
        final Set<Long> ltCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, ltCriterion);
        assertEquals(ltCriterionSampleIds.size(), 0);

        // <=
        final SampleSearchCriteria leCriterion = new SampleSearchCriteria();
        leCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_LONG).thatIsLessThanOrEqualTo(SAMPLE_PROPERTY_1_NUMBER_VALUE + 1);
        final Set<Long> leCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, leCriterion);
        assertEquals(leCriterionSampleIds.size(), 1);
        assertTrue(leCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(leCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(leCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with double number property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithDoubleNumberProperty()
    {
        // =
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatEquals(SAMPLE_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionSampleIds.size(), 1);
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(equalsCriterionSampleIds.contains(SAMPLE_ID_3));

        // >
        final SampleSearchCriteria gtCriterion = new SampleSearchCriteria();
        gtCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsGreaterThan(SAMPLE_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> gtCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, gtCriterion);
        assertEquals(gtCriterionSampleIds.size(), 0);

        // >=
        final SampleSearchCriteria geCriterion = new SampleSearchCriteria();
        geCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsGreaterThan(SAMPLE_PROPERTY_3_NUMBER_VALUE - 0.000001);
        final Set<Long> geCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, geCriterion);
        assertEquals(geCriterionSampleIds.size(), 1);
        assertFalse(geCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(geCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(geCriterionSampleIds.contains(SAMPLE_ID_3));

        // <
        final SampleSearchCriteria ltCriterion = new SampleSearchCriteria();
        ltCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsLessThan(SAMPLE_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> ltCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, ltCriterion);
        assertEquals(ltCriterionSampleIds.size(), 0);

        // <=
        final SampleSearchCriteria leCriterion = new SampleSearchCriteria();
        leCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsLessThanOrEqualTo(SAMPLE_PROPERTY_3_NUMBER_VALUE + 0.000001);
        final Set<Long> leCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, leCriterion);
        assertEquals(leCriterionSampleIds.size(), 1);
        assertFalse(leCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(leCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(leCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with date property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithDateProperty()
    {
        // =
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatEquals(SAMPLE_PROPERTY_2_DATE_VALUE);
        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionSampleIds.size(), 1);
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(equalsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_3));

        // <
        final SampleSearchCriteria earlierCriterion1 = new SampleSearchCriteria();
        earlierCriterion1.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(SAMPLE_PROPERTY_2_EARLIER_DATE_VALUE);
        final Set<Long> earlierCriterionSampleIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion1);
        assertEquals(earlierCriterionSampleIds1.size(), 0);

        // <
        final SampleSearchCriteria earlierCriterion2 = new SampleSearchCriteria();
        earlierCriterion2.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(SAMPLE_PROPERTY_2_DATE_VALUE);
        final Set<Long> earlierCriterionSampleIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion2);
        assertEquals(earlierCriterionSampleIds2.size(), 1);
        assertFalse(earlierCriterionSampleIds2.contains(SAMPLE_ID_1));
        assertTrue(earlierCriterionSampleIds2.contains(SAMPLE_ID_2));
        assertFalse(earlierCriterionSampleIds2.contains(SAMPLE_ID_3));

        // <
        final SampleSearchCriteria earlierCriterion3 = new SampleSearchCriteria();
        earlierCriterion3.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(SAMPLE_PROPERTY_2_LATER_DATE_VALUE);
        final Set<Long> earlierCriterionSampleIds3 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion3);
        assertEquals(earlierCriterionSampleIds3.size(), 1);
        assertFalse(earlierCriterionSampleIds3.contains(SAMPLE_ID_1));
        assertTrue(earlierCriterionSampleIds3.contains(SAMPLE_ID_2));
        assertFalse(earlierCriterionSampleIds3.contains(SAMPLE_ID_3));

        // >
        final SampleSearchCriteria laterCriterion1 = new SampleSearchCriteria();
        laterCriterion1.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(SAMPLE_PROPERTY_2_LATER_DATE_VALUE);
        final Set<Long> laterCriterionSampleIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion1);
        assertEquals(laterCriterionSampleIds1.size(), 0);

        // >
        final SampleSearchCriteria laterCriterion2 = new SampleSearchCriteria();
        laterCriterion2.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(SAMPLE_PROPERTY_2_DATE_VALUE);
        final Set<Long> laterCriterionSampleIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion2);
        assertEquals(laterCriterionSampleIds2.size(), 1);
        assertFalse(laterCriterionSampleIds2.contains(SAMPLE_ID_1));
        assertTrue(laterCriterionSampleIds2.contains(SAMPLE_ID_2));
        assertFalse(laterCriterionSampleIds2.contains(SAMPLE_ID_3));

        // >
        final SampleSearchCriteria laterCriterion3 = new SampleSearchCriteria();
        laterCriterion3.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(SAMPLE_PROPERTY_2_EARLIER_DATE_VALUE);
        final Set<Long> laterCriterionSampleIds3 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion3);
        assertEquals(laterCriterionSampleIds3.size(), 1);
        assertFalse(laterCriterionSampleIds3.contains(SAMPLE_ID_1));
        assertTrue(laterCriterionSampleIds3.contains(SAMPLE_ID_2));
        assertFalse(laterCriterionSampleIds3.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with date string date property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithStringDateProperty()
    {
        // =
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatEquals(SAMPLE_PROPERTY_2_DATE_STRING_VALUE);
        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionSampleIds.size(), 1);
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(equalsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(equalsCriterionSampleIds.contains(SAMPLE_ID_3));

        // <
        final SampleSearchCriteria earlierCriterion1 = new SampleSearchCriteria();
        earlierCriterion1.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(SAMPLE_PROPERTY_2_EARLIER_DATE_STRING_VALUE);
        final Set<Long> earlierCriterionSampleIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion1);
        assertEquals(earlierCriterionSampleIds1.size(), 0);

        // <
        final SampleSearchCriteria earlierCriterion2 = new SampleSearchCriteria();
        earlierCriterion2.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(SAMPLE_PROPERTY_2_DATE_STRING_VALUE);
        final Set<Long> earlierCriterionSampleIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion2);
        assertEquals(earlierCriterionSampleIds2.size(), 1);
        assertFalse(earlierCriterionSampleIds2.contains(SAMPLE_ID_1));
        assertTrue(earlierCriterionSampleIds2.contains(SAMPLE_ID_2));
        assertFalse(earlierCriterionSampleIds2.contains(SAMPLE_ID_3));

        // <
        final SampleSearchCriteria earlierCriterion3 = new SampleSearchCriteria();
        earlierCriterion3.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(SAMPLE_PROPERTY_2_LATER_DATE_STRING_VALUE);
        final Set<Long> earlierCriterionSampleIds3 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion3);
        assertEquals(earlierCriterionSampleIds3.size(), 1);
        assertFalse(earlierCriterionSampleIds3.contains(SAMPLE_ID_1));
        assertTrue(earlierCriterionSampleIds3.contains(SAMPLE_ID_2));
        assertFalse(earlierCriterionSampleIds3.contains(SAMPLE_ID_3));

        // >
        final SampleSearchCriteria laterCriterion1 = new SampleSearchCriteria();
        laterCriterion1.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(SAMPLE_PROPERTY_2_LATER_DATE_STRING_VALUE);
        final Set<Long> laterCriterionSampleIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion1);
        assertEquals(laterCriterionSampleIds1.size(), 0);

        // >
        final SampleSearchCriteria laterCriterion2 = new SampleSearchCriteria();
        laterCriterion2.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(SAMPLE_PROPERTY_2_DATE_STRING_VALUE);
        final Set<Long> laterCriterionSampleIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion2);
        assertEquals(laterCriterionSampleIds2.size(), 1);
        assertFalse(laterCriterionSampleIds2.contains(SAMPLE_ID_1));
        assertTrue(laterCriterionSampleIds2.contains(SAMPLE_ID_2));
        assertFalse(laterCriterionSampleIds2.contains(SAMPLE_ID_3));

        // >
        final SampleSearchCriteria laterCriterion3 = new SampleSearchCriteria();
        laterCriterion3.withDateProperty(SAMPLE_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(SAMPLE_PROPERTY_2_EARLIER_DATE_STRING_VALUE);
        final Set<Long> laterCriterionSampleIds3 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion3);
        assertEquals(laterCriterionSampleIds3.size(), 1);
        assertFalse(laterCriterionSampleIds3.contains(SAMPLE_ID_1));
        assertTrue(laterCriterionSampleIds3.contains(SAMPLE_ID_2));
        assertFalse(laterCriterionSampleIds3.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with internal property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithInternalPropertyCriteria()
    {
        final SampleSearchCriteria anyStringPropertyCriterion = new SampleSearchCriteria();
        anyStringPropertyCriterion.withProperty(INTERNAL_SAMPLE_PROPERTY_CODE_STRING);
        final Set<Long> anyStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, anyStringPropertyCriterion);
        assertTrue(anyStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(anyStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(anyStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria equalsStringPropertyCriterion = new SampleSearchCriteria();
        equalsStringPropertyCriterion.withProperty(INTERNAL_SAMPLE_PROPERTY_CODE_STRING).thatEquals(SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE);
        final Set<Long> equalsStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterion);
        assertEquals(equalsStringPropertyCriterionSampleIds.size(), 1);
        assertTrue(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(equalsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria equalsStringPropertyCriterionNotFound = new SampleSearchCriteria();
        equalsStringPropertyCriterionNotFound.withProperty(INTERNAL_SAMPLE_PROPERTY_CODE_STRING).thatEquals(SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE
                + "-");
        final Set<Long> equalsStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterionNotFound);
        assertEquals(equalsStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria startsWithStringPropertyCriterion = new SampleSearchCriteria();
        startsWithStringPropertyCriterion.withProperty(INTERNAL_SAMPLE_PROPERTY_CODE_STRING).thatStartsWith(SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE
                .substring(0, 10));
        final Set<Long> startsWithStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithStringPropertyCriterion);
        assertEquals(startsWithStringPropertyCriterionSampleIds.size(), 1);
        assertTrue(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(startsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria startsWithStringPropertyCriterionNotFound = new SampleSearchCriteria();
        startsWithStringPropertyCriterionNotFound.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatStartsWith(
                SAMPLE_PROPERTY_2_STRING_VALUE.substring(0, 10) + "-");
        final Set<Long> startsWithStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                startsWithStringPropertyCriterionNotFound);
        assertEquals(startsWithStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria endsWithStringPropertyCriterion = new SampleSearchCriteria();
        endsWithStringPropertyCriterion.withProperty(INTERNAL_SAMPLE_PROPERTY_CODE_STRING).thatEndsWith(SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE
                .substring(10));
        final Set<Long> endsWithStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithStringPropertyCriterion);
        assertEquals(endsWithStringPropertyCriterionSampleIds.size(), 1);
        assertTrue(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(endsWithStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria endsWithStringPropertyCriterionNotFound = new SampleSearchCriteria();
        endsWithStringPropertyCriterionNotFound.withProperty(INTERNAL_SAMPLE_PROPERTY_CODE_STRING).thatEndsWith(
                SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE.substring(10)
                + "-");
        final Set<Long> endsWithStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                endsWithStringPropertyCriterionNotFound);
        assertEquals(endsWithStringPropertyCriterionSampleIdsNotFound.size(), 0);

        final SampleSearchCriteria containsStringPropertyCriterion = new SampleSearchCriteria();
        containsStringPropertyCriterion.withProperty(INTERNAL_SAMPLE_PROPERTY_CODE_STRING).thatContains(
                SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE.substring(3, 10));
        final Set<Long> containsStringPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsStringPropertyCriterion);
        assertEquals(containsStringPropertyCriterionSampleIds.size(), 1);
        assertTrue(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(containsStringPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria containsStringPropertyCriterionNotFound = new SampleSearchCriteria();
        containsStringPropertyCriterionNotFound.withProperty(INTERNAL_SAMPLE_PROPERTY_CODE_STRING).thatContains(
                SAMPLE_PROPERTY_1_INTERNAL_STRING_VALUE.substring(3, 10) + "-");
        final Set<Long> containsStringPropertyCriterionSampleIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                containsStringPropertyCriterionNotFound);
        assertEquals(containsStringPropertyCriterionSampleIdsNotFound.size(), 0);
    }

    /**
     * Tests {@link SampleSearchManager} with compound field criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCompoundFieldCriteria()
    {
        final SampleSearchCriteria compoundAndFieldCriterion = new SampleSearchCriteria();
        compoundAndFieldCriterion.withAndOperator().withRegistrationDate().thatIsEarlierThanOrEqualTo(REGISTRATION_DATE_2);
        compoundAndFieldCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(REGISTRATION_DATE_2);
        final Set<Long> compoundAndFieldCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundAndFieldCriterion);
        assertFalse(compoundAndFieldCriterionSampleIds.isEmpty());
        assertFalse(compoundAndFieldCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(compoundAndFieldCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(compoundAndFieldCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria compoundOrFieldCriterion = new SampleSearchCriteria();
        compoundOrFieldCriterion.withOrOperator().withRegistrationDate().thatIsEarlierThanOrEqualTo(REGISTRATION_DATE_2);
        compoundOrFieldCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(REGISTRATION_DATE_2);
        final Set<Long> compoundOrFieldCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundOrFieldCriterion);
        assertFalse(compoundOrFieldCriterionSampleIds.isEmpty());
        assertTrue(compoundOrFieldCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(compoundOrFieldCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(compoundOrFieldCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with compound field criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCompoundPropertyCriteria()
    {
        final SampleSearchCriteria compoundAndPropertyCriterion1 = new SampleSearchCriteria();
        compoundAndPropertyCriterion1.withAndOperator().withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsLessThanOrEqualTo(
                SAMPLE_PROPERTY_3_NUMBER_VALUE);
        compoundAndPropertyCriterion1.withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsGreaterThanOrEqualTo(SAMPLE_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> compoundAndPropertyCriterionSampleIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundAndPropertyCriterion1);
        assertEquals(compoundAndPropertyCriterionSampleIds1.size(), 1);
        assertFalse(compoundAndPropertyCriterionSampleIds1.contains(SAMPLE_ID_1));
        assertFalse(compoundAndPropertyCriterionSampleIds1.contains(SAMPLE_ID_2));
        assertTrue(compoundAndPropertyCriterionSampleIds1.contains(SAMPLE_ID_3));

        final SampleSearchCriteria compoundAndPropertyCriterion2 = new SampleSearchCriteria();
        compoundAndPropertyCriterion2.withAndOperator().withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsLessThan(
                SAMPLE_PROPERTY_3_NUMBER_VALUE);
        compoundAndPropertyCriterion2.withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsGreaterThanOrEqualTo(SAMPLE_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> compoundAndPropertyCriterionSampleIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundAndPropertyCriterion2);
        assertEquals(compoundAndPropertyCriterionSampleIds2.size(), 0);

        final SampleSearchCriteria compoundOrPropertyCriterion = new SampleSearchCriteria();
        compoundOrPropertyCriterion.withOrOperator().withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatEquals(SAMPLE_PROPERTY_3_NUMBER_VALUE);
        compoundOrPropertyCriterion.withProperty(SAMPLE_PROPERTY_CODE_STRING).thatEquals(SAMPLE_PROPERTY_2_STRING_VALUE);
        final Set<Long> compoundOrPropertyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundOrPropertyCriterion);
        assertEquals(compoundOrPropertyCriterionSampleIds.size(), 2);
        assertFalse(compoundOrPropertyCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(compoundOrPropertyCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(compoundOrPropertyCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria compoundAndPropertyFieldCriterion = new SampleSearchCriteria();
        compoundAndPropertyFieldCriterion.withAndOperator().withCodes().thatIn(Arrays.asList(CODE_1, CODE_3));
        compoundAndPropertyFieldCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_DOUBLE).thatIsGreaterThan(SAMPLE_PROPERTY_3_NUMBER_VALUE
                - 0.000001);
        final Set<Long> compoundAndPropertyFieldCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundAndPropertyFieldCriterion);
        assertEquals(compoundAndPropertyFieldCriterionSampleIds.size(), 1);
        assertFalse(compoundAndPropertyFieldCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(compoundAndPropertyFieldCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(compoundAndPropertyFieldCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria compoundOrPropertyFieldCriterion = new SampleSearchCriteria();
        compoundOrPropertyFieldCriterion.withOrOperator().withPermId().thatEquals(PERM_ID_2);
        compoundOrPropertyFieldCriterion.withNumberProperty(SAMPLE_PROPERTY_CODE_LONG).thatEquals(SAMPLE_PROPERTY_1_NUMBER_VALUE);
        final Set<Long> compoundOrPropertyFieldCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundOrPropertyFieldCriterion);
        assertEquals(compoundOrPropertyFieldCriterionSampleIds.size(), 2);
        assertTrue(compoundOrPropertyFieldCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(compoundOrPropertyFieldCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(compoundOrPropertyFieldCriterionSampleIds.contains(SAMPLE_ID_3));
    }

    /**
     * Tests {@link SampleSearchManager} with registrator field criteria using DB connection.
     */
    @Test
    public void testQueryDBWithRegistratorCriteria()
    {
        // Any registrator search
        // This is a trivial search since registrator is a mandatory field, so the result set will contain all records
        final SampleSearchCriteria emptyCriterion = new SampleSearchCriteria();
        emptyCriterion.withRegistrator();
        final Set<Long> emptyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, emptyCriterion);
        assertFalse(emptyCriterionSampleIds.isEmpty());

        // By ID
        final SampleSearchCriteria idCriterion = new SampleSearchCriteria();
        idCriterion.withRegistrator().withUserId().thatEquals(String.valueOf(REGISTRATOR_ID));
        final Set<Long> idCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, idCriterion);
        assertEquals(idCriterionSampleIds.size(), 1);
        assertFalse(idCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(idCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(idCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria notExistingIdCriterion = new SampleSearchCriteria();
        notExistingIdCriterion.withRegistrator().withUserId().thatEquals("-");
        final Set<Long> notExistingIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingIdCriterion);
        assertTrue(notExistingIdCriterionSampleIds.isEmpty());

        // By IDs
        final SampleSearchCriteria idsCriterion = new SampleSearchCriteria();
        idsCriterion.withRegistrator().withUserIds().thatIn(Arrays.asList(REGISTRATOR_USER_ID, ADMIN_USER_ID));
        final Set<Long> idsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, idsCriterion);
        assertEquals(idsCriterionSampleIds.size(), 2);
        assertTrue(idsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(idsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(idsCriterionSampleIds.contains(SAMPLE_ID_3));

        // By First Name
        final SampleSearchCriteria firstNameCriterion = new SampleSearchCriteria();
        firstNameCriterion.withRegistrator().withFirstName().thatEquals(REGISTRATOR_FIRST_NAME);
        final Set<Long> firstNameCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, firstNameCriterion);
        assertEquals(firstNameCriterionSampleIds.size(), 1);
        assertFalse(firstNameCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(firstNameCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(firstNameCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria notExistingFirstNameCriterion = new SampleSearchCriteria();
        notExistingFirstNameCriterion.withRegistrator().withFirstName().thatEquals("-");
        final Set<Long> notExistingFirstNameCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingFirstNameCriterion);
        assertTrue(notExistingFirstNameCriterionSampleIds.isEmpty());

        // By Last Name
        final SampleSearchCriteria lastNameCriterion = new SampleSearchCriteria();
        lastNameCriterion.withRegistrator().withLastName().thatEquals(REGISTRATOR_LAST_NAME);
        final Set<Long> lastNameCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, lastNameCriterion);
        assertEquals(lastNameCriterionSampleIds.size(), 1);
        assertFalse(lastNameCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(lastNameCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(lastNameCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria notExistingLastNameCriterion = new SampleSearchCriteria();
        notExistingLastNameCriterion.withRegistrator().withLastName().thatEquals("-");
        final Set<Long> notExistingLastNameCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingLastNameCriterion);
        assertTrue(notExistingLastNameCriterionSampleIds.isEmpty());

        // By Email
        final SampleSearchCriteria emailCriterion = new SampleSearchCriteria();
        emailCriterion.withRegistrator().withEmail().thatEquals(REGISTRATOR_EMAIL);
        final Set<Long> emailCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, emailCriterion);
        assertEquals(emailCriterionSampleIds.size(), 1);
        assertFalse(emailCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(emailCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(emailCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria notExistingEmailCriterion = new SampleSearchCriteria();
        notExistingEmailCriterion.withRegistrator().withEmail().thatEquals("-");
        final Set<Long> notExistingEmailCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingEmailCriterion);
        assertTrue(notExistingEmailCriterionSampleIds.isEmpty());
    }

    /**
     * Tests {@link SampleSearchManager} with modifier field criteria using DB connection.
     */
    @Test
    public void testQueryDBWithModifierCriteria()
    {
        // Any modifier search
        // This is a trivial search since modifier is a mandatory field, so the result set will contain all records
        final SampleSearchCriteria emptyCriterion = new SampleSearchCriteria();
        emptyCriterion.withModifier();
        final Set<Long> emptyCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, emptyCriterion);
        assertFalse(emptyCriterionSampleIds.isEmpty());

        // By ID
        final SampleSearchCriteria idCriterion = new SampleSearchCriteria();
        idCriterion.withModifier().withUserId().thatEquals(String.valueOf(MODIFIER_ID));
        final Set<Long> idCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, idCriterion);
        assertEquals(idCriterionSampleIds.size(), 1);
        assertFalse(idCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(idCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(idCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria notExistingIdCriterion = new SampleSearchCriteria();
        notExistingIdCriterion.withModifier().withUserId().thatEquals("-");
        final Set<Long> notExistingIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingIdCriterion);
        assertTrue(notExistingIdCriterionSampleIds.isEmpty());

        // By IDs
        final SampleSearchCriteria idsCriterion = new SampleSearchCriteria();
        idsCriterion.withModifier().withUserIds().thatIn(Arrays.asList(MODIFIER_USER_ID, ADMIN_USER_ID));
        final Set<Long> idsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, idsCriterion);
        assertEquals(idsCriterionSampleIds.size(), 2);
        assertTrue(idsCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(idsCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(idsCriterionSampleIds.contains(SAMPLE_ID_3));

        // By First Name
        final SampleSearchCriteria firstNameCriterion = new SampleSearchCriteria();
        firstNameCriterion.withModifier().withFirstName().thatEquals(MODIFIER_FIRST_NAME);
        final Set<Long> firstNameCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, firstNameCriterion);
        assertEquals(firstNameCriterionSampleIds.size(), 1);
        assertFalse(firstNameCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(firstNameCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(firstNameCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria notExistingFirstNameCriterion = new SampleSearchCriteria();
        notExistingFirstNameCriterion.withModifier().withFirstName().thatEquals("-");
        final Set<Long> notExistingFirstNameCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingFirstNameCriterion);
        assertTrue(notExistingFirstNameCriterionSampleIds.isEmpty());

        // By Last Name
        final SampleSearchCriteria lastNameCriterion = new SampleSearchCriteria();
        lastNameCriterion.withModifier().withLastName().thatEquals(MODIFIER_LAST_NAME);
        final Set<Long> lastNameCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, lastNameCriterion);
        assertEquals(lastNameCriterionSampleIds.size(), 1);
        assertFalse(lastNameCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(lastNameCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(lastNameCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria notExistingLastNameCriterion = new SampleSearchCriteria();
        notExistingLastNameCriterion.withModifier().withLastName().thatEquals("-");
        final Set<Long> notExistingLastNameCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingLastNameCriterion);
        assertTrue(notExistingLastNameCriterionSampleIds.isEmpty());

        // By Email
        final SampleSearchCriteria emailCriterion = new SampleSearchCriteria();
        emailCriterion.withModifier().withEmail().thatEquals(MODIFIER_EMAIL);
        final Set<Long> emailCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, emailCriterion);
        assertEquals(emailCriterionSampleIds.size(), 1);
        assertFalse(emailCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(emailCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(emailCriterionSampleIds.contains(SAMPLE_ID_3));

        final SampleSearchCriteria notExistingEmailCriterion = new SampleSearchCriteria();
        notExistingEmailCriterion.withModifier().withEmail().thatEquals("-");
        final Set<Long> notExistingEmailCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingEmailCriterion);
        assertTrue(notExistingEmailCriterionSampleIds.isEmpty());
    }

    /**
     * Tests {@link SampleSearchManager} with parent sample criteria using DB connection.
     */
    @Test
    public void testQueryDBWithParentCriteria()
    {
        final SampleSearchCriteria parentIdCriterion = new SampleSearchCriteria();
        parentIdCriterion.withParents().withCode().thatEquals(CODE_1);
        final Set<Long> parentIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, parentIdCriterion);
        assertEquals(parentIdCriterionSampleIds.size(), 2);
        assertFalse(parentIdCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(parentIdCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(parentIdCriterionSampleIds.contains(SAMPLE_ID_3));
        assertTrue(parentIdCriterionSampleIds.contains(SAMPLE_ID_4));
        assertTrue(parentIdCriterionSampleIds.contains(SAMPLE_ID_5));
        assertFalse(parentIdCriterionSampleIds.contains(SAMPLE_ID_6));

        final SampleSearchCriteria parentOrIdCriterion = new SampleSearchCriteria().withOrOperator();
        parentOrIdCriterion.withParents().withCode().thatEquals(CODE_1);
        parentOrIdCriterion.withParents().withCode().thatEquals(CODE_2);
        final Set<Long> parentOrIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, parentOrIdCriterion);
        assertEquals(parentOrIdCriterionSampleIds.size(), 3);
        assertFalse(parentOrIdCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(parentOrIdCriterionSampleIds.contains(SAMPLE_ID_2));
        assertTrue(parentOrIdCriterionSampleIds.contains(SAMPLE_ID_3));
        assertTrue(parentOrIdCriterionSampleIds.contains(SAMPLE_ID_4));
        assertTrue(parentOrIdCriterionSampleIds.contains(SAMPLE_ID_5));
        assertFalse(parentOrIdCriterionSampleIds.contains(SAMPLE_ID_6));

        final SampleSearchCriteria parentAndIdCriterion = new SampleSearchCriteria().withAndOperator();
        parentAndIdCriterion.withParents().withCode().thatEquals(CODE_1);
        parentAndIdCriterion.withParents().withCode().thatEquals(CODE_2);
        final Set<Long> parentAndIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, parentAndIdCriterion);
        assertTrue(parentAndIdCriterionSampleIds.isEmpty());
    }

    /**
     * Tests {@link SampleSearchManager} with child sample criteria using DB connection.
     */
    @Test
    public void testQueryDBWithChildCriteria()
    {
        final SampleSearchCriteria childIdCriterion = new SampleSearchCriteria();
        childIdCriterion.withChildren().withCode().thatEquals(CODE_4);
        final Set<Long> childIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, childIdCriterion);
        assertEquals(childIdCriterionSampleIds.size(), 1);
        assertTrue(childIdCriterionSampleIds.contains(SAMPLE_ID_1));
        assertFalse(childIdCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(childIdCriterionSampleIds.contains(SAMPLE_ID_3));
        assertFalse(childIdCriterionSampleIds.contains(SAMPLE_ID_4));
        assertFalse(childIdCriterionSampleIds.contains(SAMPLE_ID_5));
        assertFalse(childIdCriterionSampleIds.contains(SAMPLE_ID_6));

        final SampleSearchCriteria childOrIdCriterion = new SampleSearchCriteria().withOrOperator();
        childOrIdCriterion.withChildren().withCode().thatEquals(CODE_3);
        childOrIdCriterion.withChildren().withCode().thatEquals(CODE_4);
        final Set<Long> childOrIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, childOrIdCriterion);
        assertEquals(childOrIdCriterionSampleIds.size(), 2);
        assertTrue(childOrIdCriterionSampleIds.contains(SAMPLE_ID_1));
        assertTrue(childOrIdCriterionSampleIds.contains(SAMPLE_ID_2));
        assertFalse(childOrIdCriterionSampleIds.contains(SAMPLE_ID_3));
        assertFalse(childOrIdCriterionSampleIds.contains(SAMPLE_ID_4));
        assertFalse(childOrIdCriterionSampleIds.contains(SAMPLE_ID_5));
        assertFalse(childOrIdCriterionSampleIds.contains(SAMPLE_ID_6));

        final SampleSearchCriteria childAndIdCriterion = new SampleSearchCriteria().withAndOperator();
        childAndIdCriterion.withChildren().withCode().thatEquals(CODE_3);
        childAndIdCriterion.withChildren().withCode().thatEquals(CODE_4);
        final Set<Long> childAndIdCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, childAndIdCriterion);
        assertTrue(childAndIdCriterionSampleIds.isEmpty());
    }

}