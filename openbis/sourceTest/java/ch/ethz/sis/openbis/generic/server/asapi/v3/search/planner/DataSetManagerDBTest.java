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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ADMIN_USER_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ADMIN_USER_TECH_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_CODE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_CODE_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_ID_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_ID_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_MODIFICATION_DATE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_MODIFICATION_DATE_STRING_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_1_INTERNAL_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_1_NUMBER_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_2_DATE_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_2_DATE_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_2_EARLIER_DATE_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_2_EARLIER_DATE_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_2_LATER_DATE_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_2_LATER_DATE_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_2_STRING_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_3_NUMBER_VALUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_CODE_DATE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_CODE_DOUBLE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_CODE_LONG;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_PROPERTY_CODE_STRING;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_REGISTRATION_DATE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_REGISTRATION_DATE_STRING_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_REGISTRATION_DATE_STRING_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_STORE_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.INTERNAL_DATA_SET_PROPERTY_CODE_STRING;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.MODIFIER_EMAIL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.MODIFIER_FIRST_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.MODIFIER_LAST_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.MODIFIER_USER_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATOR_EMAIL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATOR_FIRST_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATOR_LAST_NAME;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.REGISTRATOR_USER_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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

    /**
     * Tests {@link DataSetSearchManager} with {@link RegistrationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithRegistrationDateField()
    {
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(DATA_SET_REGISTRATION_DATE_2);
        final Set<Long> equalCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionDataSetIds.size(), 1);
        assertTrue(equalCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria earlierThanCriterion = new DataSetSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(DATA_SET_REGISTRATION_DATE_2);
        final Set<Long> earlierThanCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionDataSetIds.isEmpty());
        assertTrue(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria laterThanCriterion = new DataSetSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(DATA_SET_REGISTRATION_DATE_2);
        final Set<Long> laterThanCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionDataSetIds.isEmpty());
        assertFalse(laterThanCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(laterThanCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(laterThanCriterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with {@link ModificationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithModificationDateField()
    {
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withModificationDate().thatEquals(DATA_SET_MODIFICATION_DATE_2);
        final Set<Long> equalCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionDataSetIds.size(), 1);
        assertTrue(equalCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria earlierThanCriterion = new DataSetSearchCriteria();
        earlierThanCriterion.withModificationDate().thatIsEarlierThanOrEqualTo(DATA_SET_MODIFICATION_DATE_2);
        final Set<Long> earlierThanCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionDataSetIds.isEmpty());
        assertTrue(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria laterThanCriterion = new DataSetSearchCriteria();
        laterThanCriterion.withModificationDate().thatIsLaterThanOrEqualTo(DATA_SET_MODIFICATION_DATE_2);
        final Set<Long> laterThanCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionDataSetIds.isEmpty());
        assertFalse(laterThanCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(laterThanCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(laterThanCriterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with {@link RegistrationDateSearchCriteria} attribute search criteria (in string form) using DB
     * connection.
     */
    @Test
    public void testQueryDBWithStringRegistrationDateField()
    {
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(DATA_SET_REGISTRATION_DATE_STRING_2);
        final Set<Long> equalCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionDataSetIds.size(), 1);
        assertTrue(equalCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria earlierThanCriterion = new DataSetSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(DATA_SET_REGISTRATION_DATE_STRING_2);
        final Set<Long> earlierThanCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionDataSetIds.isEmpty());
        assertTrue(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria laterThanCriterion = new DataSetSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(DATA_SET_REGISTRATION_DATE_STRING_2);
        final Set<Long> laterThanCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionDataSetIds.isEmpty());
        assertFalse(laterThanCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(laterThanCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(laterThanCriterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with {@link ModificationDateSearchCriteria} attribute search criteria (in string form) using DB
     * connection.
     */
    @Test
    public void testQueryDBWithStringModificationDateField()
    {
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withModificationDate().thatEquals(DATA_SET_MODIFICATION_DATE_STRING_2);
        final Set<Long> equalCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionDataSetIds.size(), 1);
        assertTrue(equalCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria earlierThanCriterion = new DataSetSearchCriteria();
        earlierThanCriterion.withModificationDate().thatIsEarlierThanOrEqualTo(DATA_SET_MODIFICATION_DATE_STRING_2);
        final Set<Long> earlierThanCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionDataSetIds.isEmpty());
        assertTrue(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(earlierThanCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria laterThanCriterion = new DataSetSearchCriteria();
        laterThanCriterion.withModificationDate().thatIsLaterThanOrEqualTo(DATA_SET_MODIFICATION_DATE_STRING_2);
        final Set<Long> laterThanCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionDataSetIds.isEmpty());
        assertFalse(laterThanCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(laterThanCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(laterThanCriterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with codes (collection attribute) search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCodes()
    {
        final DataSetSearchCriteria criterion = new DataSetSearchCriteria();
        criterion.withCodes().thatIn(Arrays.asList(DATA_SET_CODE_1, DATA_SET_CODE_3));
        final Set<Long> criterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertEquals(criterionDataSetIds.size(), 2);
        assertTrue(criterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(criterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithPermId()
    {
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withPermId().thatEquals(DATA_SET_CODE_2);
        final Set<Long> equalsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionDataSetIds.size(), 1);
        assertTrue(equalsCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria startsWithCriterion = new DataSetSearchCriteria();
        startsWithCriterion.withPermId().thatStartsWith(DATA_SET_CODE_1.substring(0, DATA_SET_CODE_1.length() - 2));
        final Set<Long> startsWithCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithCriterion);
        assertEquals(startsWithCriterionDataSetIds.size(), 1);
        assertTrue(startsWithCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria endsWithCriterion = new DataSetSearchCriteria();
        endsWithCriterion.withPermId().thatEndsWith(DATA_SET_CODE_1.substring(DATA_SET_CODE_1.length() - 4));
        final Set<Long> endsWithCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion);
        assertEquals(endsWithCriterionDataSetIds.size(), 1);
        assertTrue(endsWithCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria containsCriterion = new DataSetSearchCriteria();
        containsCriterion.withPermId().thatContains(DATA_SET_CODE_2.substring(4, 12));
        final Set<Long> containsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsCriterion);
        assertEquals(containsCriterionDataSetIds.size(), 2);
        assertTrue(containsCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(containsCriterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with {@link AnyFieldSearchCriteria} attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithAnyField()
    {
        // code attribute
        final DataSetSearchCriteria codeEqualsCriterion = new DataSetSearchCriteria();
        codeEqualsCriterion.withAnyField().thatEquals(DATA_SET_CODE_2);
        final Set<Long> codeEqualsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, codeEqualsCriterion);
        assertEquals(codeEqualsCriterionDataSetIds.size(), 1);
        assertTrue(codeEqualsCriterionDataSetIds.contains(DATA_SET_ID_2));

        // dast_id attribute
        final DataSetSearchCriteria projectIdEqualsCriterion = new DataSetSearchCriteria();
        projectIdEqualsCriterion.withAnyField().thatEquals(String.valueOf(DATA_STORE_ID_1));
        final Set<Long> projectIdEqualsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, projectIdEqualsCriterion);
        assertEquals(projectIdEqualsCriterionDataSetIds.size(), 3);
        assertTrue(projectIdEqualsCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(projectIdEqualsCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(projectIdEqualsCriterionDataSetIds.contains(DATA_SET_ID_3));

        // registration_timestamp attribute
        final DataSetSearchCriteria registrationDateEqualsCriterion = new DataSetSearchCriteria();
        registrationDateEqualsCriterion.withAnyField().thatEquals(DATA_SET_REGISTRATION_DATE_STRING_1);
        final Set<Long> registrationDateEqualsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, registrationDateEqualsCriterion);
        assertEquals(registrationDateEqualsCriterionDataSetIds.size(), 1);
        assertTrue(registrationDateEqualsCriterionDataSetIds.contains(DATA_SET_ID_1));

        // code attribute again
        final DataSetSearchCriteria startsWithCriterion = new DataSetSearchCriteria();
        startsWithCriterion.withAnyField().thatStartsWith(DATA_SET_CODE_1.substring(0, DATA_SET_CODE_1.length() - 2));
        final Set<Long> startsWithCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithCriterion);
        assertEquals(startsWithCriterionDataSetIds.size(), 1);
        assertTrue(startsWithCriterionDataSetIds.contains(DATA_SET_ID_1));

        // code attribute again
        final DataSetSearchCriteria endsWithCriterion1 = new DataSetSearchCriteria();
        endsWithCriterion1.withAnyField().thatEndsWith(DATA_SET_CODE_2.substring(DATA_SET_CODE_2.length() - 5));
        final Set<Long> endsWithCriterionDataSetIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion1);
        assertEquals(endsWithCriterionDataSetIds1.size(), 1);
        assertTrue(endsWithCriterionDataSetIds1.contains(DATA_SET_ID_2));

        // id, perm_id and samp_id_part_of fields
        final DataSetSearchCriteria endsWithCriterion2 = new DataSetSearchCriteria();
        endsWithCriterion2.withAnyField().thatEndsWith(String.valueOf(DATA_SET_ID_1));
        final Set<Long> endsWithCriterionDataSetIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion2);
        assertEquals(endsWithCriterionDataSetIds2.size(), 1);
        assertTrue(endsWithCriterionDataSetIds2.contains(DATA_SET_ID_1));

        // expe_id
        final DataSetSearchCriteria containsCriterion = new DataSetSearchCriteria();
        containsCriterion.withAnyField().thatContains(String.valueOf(EXPERIMENT_ID_3));
        final Set<Long> containsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsCriterion);
        assertEquals(containsCriterionDataSetIds.size(), 3);
        assertTrue(containsCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(containsCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(containsCriterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with without experiment attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutExperimentField()
    {
        final DataSetSearchCriteria criterion = new DataSetSearchCriteria();
        criterion.withoutExperiment();
        final Set<Long> criterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertFalse(criterionDataSetIds.isEmpty());
        assertTrue(criterionDataSetIds.contains(DATA_SET_ID_1));
        assertFalse(criterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(criterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with without sample attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutSampleField()
    {
        final DataSetSearchCriteria criterion = new DataSetSearchCriteria();
        criterion.withoutSample();
        final Set<Long> criterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertFalse(criterionDataSetIds.isEmpty());
        assertFalse(criterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(criterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(criterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with ID search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithId()
    {
        final DataSetSearchCriteria permIdCriterion = new DataSetSearchCriteria();
        permIdCriterion.withId().thatEquals(new DataSetPermId(DATA_SET_CODE_2));
        final Set<Long> permIdCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, permIdCriterion);
        assertEquals(permIdCriterionDataSetIds.size(), 1);
        assertTrue(permIdCriterionDataSetIds.contains(DATA_SET_ID_2));
    }

    /**
     * Tests {@link DataSetSearchManager} with string property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithAnyProperty()
    {
        final DataSetSearchCriteria anyPropertyCriterion = new DataSetSearchCriteria();
        anyPropertyCriterion.withAnyProperty();
        final Set<Long> anyPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, anyPropertyCriterion);
        assertTrue(anyPropertyCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(anyPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(anyPropertyCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria equalsNumberPropertyCriterion = new DataSetSearchCriteria();
        equalsNumberPropertyCriterion.withAnyProperty().thatEquals(String.valueOf(DATA_SET_PROPERTY_1_NUMBER_VALUE));
        final Set<Long> equalsNumberPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsNumberPropertyCriterion);
        assertEquals(equalsNumberPropertyCriterionDataSetIds.size(), 1);
        assertTrue(equalsNumberPropertyCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria equalsStringPropertyCriterion = new DataSetSearchCriteria();
        equalsStringPropertyCriterion.withAnyProperty().thatEquals(DATA_SET_PROPERTY_2_STRING_VALUE);
        final Set<Long> equalsStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterion);
        assertEquals(equalsStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(equalsStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria equalsStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        equalsStringPropertyCriterionNotFound.withAnyProperty().thatEquals(DATA_SET_PROPERTY_2_STRING_VALUE + "-");
        final Set<Long> equalsStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterionNotFound);
        assertEquals(equalsStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria startsWithStringPropertyCriterion = new DataSetSearchCriteria();
        startsWithStringPropertyCriterion.withAnyProperty().thatStartsWith(DATA_SET_PROPERTY_2_STRING_VALUE.substring(0, 10));
        final Set<Long> startsWithStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithStringPropertyCriterion);
        assertEquals(startsWithStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(startsWithStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria startsWithStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        startsWithStringPropertyCriterionNotFound.withAnyProperty().thatStartsWith(
                DATA_SET_PROPERTY_2_STRING_VALUE.substring(0, 10) + "-");
        final Set<Long> startsWithStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                startsWithStringPropertyCriterionNotFound);
        assertEquals(startsWithStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria endsWithStringPropertyCriterion = new DataSetSearchCriteria();
        endsWithStringPropertyCriterion.withAnyProperty().thatEndsWith(DATA_SET_PROPERTY_2_STRING_VALUE.substring(10));
        final Set<Long> endsWithStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithStringPropertyCriterion);
        assertEquals(endsWithStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(endsWithStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria endsWithStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        endsWithStringPropertyCriterionNotFound.withAnyProperty().thatEndsWith(DATA_SET_PROPERTY_2_STRING_VALUE.substring(10)
                + "-");
        final Set<Long> endsWithStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                endsWithStringPropertyCriterionNotFound);
        assertEquals(endsWithStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria containsStringPropertyCriterion = new DataSetSearchCriteria();
        containsStringPropertyCriterion.withAnyProperty().thatContains(DATA_SET_PROPERTY_2_STRING_VALUE.substring(3, 10));
        final Set<Long> containsStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsStringPropertyCriterion);
        assertEquals(containsStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(containsStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria containsStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        containsStringPropertyCriterionNotFound.withAnyProperty().thatContains(DATA_SET_PROPERTY_2_STRING_VALUE.substring(3, 10)
                + "-");
        final Set<Long> containsStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                containsStringPropertyCriterionNotFound);
        assertEquals(containsStringPropertyCriterionDataSetIdsNotFound.size(), 0);
    }

    /**
     * Tests {@link DataSetSearchManager} with string property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithStringProperty()
    {
        final DataSetSearchCriteria anyStringPropertyCriterion = new DataSetSearchCriteria();
        anyStringPropertyCriterion.withProperty(DATA_SET_PROPERTY_CODE_STRING);
        final Set<Long> anyStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, anyStringPropertyCriterion);
        assertFalse(anyStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(anyStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(anyStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria equalsStringPropertyCriterion = new DataSetSearchCriteria();
        equalsStringPropertyCriterion.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatEquals(DATA_SET_PROPERTY_2_STRING_VALUE);
        final Set<Long> equalsStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterion);
        assertEquals(equalsStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(equalsStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria equalsStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        equalsStringPropertyCriterionNotFound.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatEquals(DATA_SET_PROPERTY_2_STRING_VALUE + "-");
        final Set<Long> equalsStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterionNotFound);
        assertEquals(equalsStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria startsWithStringPropertyCriterion = new DataSetSearchCriteria();
        startsWithStringPropertyCriterion.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatStartsWith(DATA_SET_PROPERTY_2_STRING_VALUE.substring(0, 10));
        final Set<Long> startsWithStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithStringPropertyCriterion);
        assertEquals(startsWithStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(startsWithStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria startsWithStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        startsWithStringPropertyCriterionNotFound.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatStartsWith(
                DATA_SET_PROPERTY_2_STRING_VALUE.substring(0, 10) + "-");
        final Set<Long> startsWithStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                startsWithStringPropertyCriterionNotFound);
        assertEquals(startsWithStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria endsWithStringPropertyCriterion = new DataSetSearchCriteria();
        endsWithStringPropertyCriterion.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatEndsWith(DATA_SET_PROPERTY_2_STRING_VALUE.substring(10));
        final Set<Long> endsWithStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithStringPropertyCriterion);
        assertEquals(endsWithStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(endsWithStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria endsWithStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        endsWithStringPropertyCriterionNotFound.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatEndsWith(DATA_SET_PROPERTY_2_STRING_VALUE.substring(10)
                + "-");
        final Set<Long> endsWithStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                endsWithStringPropertyCriterionNotFound);
        assertEquals(endsWithStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria containsStringPropertyCriterion = new DataSetSearchCriteria();
        containsStringPropertyCriterion.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatContains(DATA_SET_PROPERTY_2_STRING_VALUE.substring(3, 10));
        final Set<Long> containsStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsStringPropertyCriterion);
        assertEquals(containsStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(containsStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));

        final DataSetSearchCriteria containsStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        containsStringPropertyCriterionNotFound.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatContains(DATA_SET_PROPERTY_2_STRING_VALUE.substring(3, 10)
                + "-");
        final Set<Long> containsStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                containsStringPropertyCriterionNotFound);
        assertEquals(containsStringPropertyCriterionDataSetIdsNotFound.size(), 0);
    }

    /**
     * Tests {@link DataSetSearchManager} with long number property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithLongNumberProperty()
    {
        // =
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_LONG).thatEquals(DATA_SET_PROPERTY_1_NUMBER_VALUE);
        final Set<Long> equalsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionDataSetIds.size(), 1);
        assertTrue(equalsCriterionDataSetIds.contains(DATA_SET_ID_1));

        // >
        final DataSetSearchCriteria gtCriterion = new DataSetSearchCriteria();
        gtCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_LONG).thatIsGreaterThan(DATA_SET_PROPERTY_1_NUMBER_VALUE);
        final Set<Long> gtCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, gtCriterion);
        assertEquals(gtCriterionDataSetIds.size(), 0);

        // >=
        final DataSetSearchCriteria geCriterion = new DataSetSearchCriteria();
        geCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_LONG).thatIsGreaterThan(DATA_SET_PROPERTY_1_NUMBER_VALUE - 1);
        final Set<Long> geCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, geCriterion);
        assertEquals(geCriterionDataSetIds.size(), 1);
        assertTrue(geCriterionDataSetIds.contains(DATA_SET_ID_1));

        // <
        final DataSetSearchCriteria ltCriterion = new DataSetSearchCriteria();
        ltCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_LONG).thatIsLessThan(DATA_SET_PROPERTY_1_NUMBER_VALUE);
        final Set<Long> ltCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, ltCriterion);
        assertEquals(ltCriterionDataSetIds.size(), 0);

        // <=
        final DataSetSearchCriteria leCriterion = new DataSetSearchCriteria();
        leCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_LONG).thatIsLessThanOrEqualTo(DATA_SET_PROPERTY_1_NUMBER_VALUE + 1);
        final Set<Long> leCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, leCriterion);
        assertEquals(leCriterionDataSetIds.size(), 1);
        assertTrue(leCriterionDataSetIds.contains(DATA_SET_ID_1));
    }

    /**
     * Tests {@link DataSetSearchManager} with double number property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithDoubleNumberProperty()
    {
        // =
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatEquals(DATA_SET_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> equalsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionDataSetIds.size(), 1);
        assertTrue(equalsCriterionDataSetIds.contains(DATA_SET_ID_3));

        // >
        final DataSetSearchCriteria gtCriterion = new DataSetSearchCriteria();
        gtCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsGreaterThan(DATA_SET_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> gtCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, gtCriterion);
        assertEquals(gtCriterionDataSetIds.size(), 0);

        // >=
        final DataSetSearchCriteria geCriterion = new DataSetSearchCriteria();
        geCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsGreaterThan(DATA_SET_PROPERTY_3_NUMBER_VALUE - 0.000001);
        final Set<Long> geCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, geCriterion);
        assertEquals(geCriterionDataSetIds.size(), 1);
        assertTrue(geCriterionDataSetIds.contains(DATA_SET_ID_3));

        // <
        final DataSetSearchCriteria ltCriterion = new DataSetSearchCriteria();
        ltCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsLessThan(DATA_SET_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> ltCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, ltCriterion);
        assertEquals(ltCriterionDataSetIds.size(), 0);

        // <=
        final DataSetSearchCriteria leCriterion = new DataSetSearchCriteria();
        leCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsLessThanOrEqualTo(DATA_SET_PROPERTY_3_NUMBER_VALUE + 0.000001);
        final Set<Long> leCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, leCriterion);
        assertEquals(leCriterionDataSetIds.size(), 1);
        assertTrue(leCriterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with date property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithDateProperty()
    {
        // =
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatEquals(DATA_SET_PROPERTY_2_DATE_VALUE);
        final Set<Long> equalsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionDataSetIds.size(), 1);
        assertTrue(equalsCriterionDataSetIds.contains(DATA_SET_ID_2));

        // <
        final DataSetSearchCriteria earlierCriterion1 = new DataSetSearchCriteria();
        earlierCriterion1.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(DATA_SET_PROPERTY_2_EARLIER_DATE_VALUE);
        final Set<Long> earlierCriterionDataSetIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion1);
        assertEquals(earlierCriterionDataSetIds1.size(), 0);

        // <
        final DataSetSearchCriteria earlierCriterion2 = new DataSetSearchCriteria();
        earlierCriterion2.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(DATA_SET_PROPERTY_2_DATE_VALUE);
        final Set<Long> earlierCriterionDataSetIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion2);
        assertEquals(earlierCriterionDataSetIds2.size(), 1);
        assertTrue(earlierCriterionDataSetIds2.contains(DATA_SET_ID_2));

        // <
        final DataSetSearchCriteria earlierCriterion3 = new DataSetSearchCriteria();
        earlierCriterion3.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(DATA_SET_PROPERTY_2_LATER_DATE_VALUE);
        final Set<Long> earlierCriterionDataSetIds3 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion3);
        assertEquals(earlierCriterionDataSetIds3.size(), 1);
        assertTrue(earlierCriterionDataSetIds3.contains(DATA_SET_ID_2));

        // >
        final DataSetSearchCriteria laterCriterion1 = new DataSetSearchCriteria();
        laterCriterion1.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(DATA_SET_PROPERTY_2_LATER_DATE_VALUE);
        final Set<Long> laterCriterionDataSetIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion1);
        assertEquals(laterCriterionDataSetIds1.size(), 0);

        // >
        final DataSetSearchCriteria laterCriterion2 = new DataSetSearchCriteria();
        laterCriterion2.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(DATA_SET_PROPERTY_2_DATE_VALUE);
        final Set<Long> laterCriterionDataSetIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion2);
        assertEquals(laterCriterionDataSetIds2.size(), 1);
        assertTrue(laterCriterionDataSetIds2.contains(DATA_SET_ID_2));

        // >
        final DataSetSearchCriteria laterCriterion3 = new DataSetSearchCriteria();
        laterCriterion3.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(DATA_SET_PROPERTY_2_EARLIER_DATE_VALUE);
        final Set<Long> laterCriterionDataSetIds3 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion3);
        assertEquals(laterCriterionDataSetIds3.size(), 1);
        assertTrue(laterCriterionDataSetIds3.contains(DATA_SET_ID_2));
    }

    /**
     * Tests {@link DataSetSearchManager} with date string date property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithStringDateProperty()
    {
        // =
        final DataSetSearchCriteria equalsCriterion = new DataSetSearchCriteria();
        equalsCriterion.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatEquals(DATA_SET_PROPERTY_2_DATE_STRING_VALUE);
        final Set<Long> equalsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionDataSetIds.size(), 1);
        assertTrue(equalsCriterionDataSetIds.contains(DATA_SET_ID_2));

        // <
        final DataSetSearchCriteria earlierCriterion1 = new DataSetSearchCriteria();
        earlierCriterion1.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(DATA_SET_PROPERTY_2_EARLIER_DATE_STRING_VALUE);
        final Set<Long> earlierCriterionDataSetIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion1);
        assertEquals(earlierCriterionDataSetIds1.size(), 0);

        // <
        final DataSetSearchCriteria earlierCriterion2 = new DataSetSearchCriteria();
        earlierCriterion2.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(DATA_SET_PROPERTY_2_DATE_STRING_VALUE);
        final Set<Long> earlierCriterionDataSetIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion2);
        assertEquals(earlierCriterionDataSetIds2.size(), 1);
        assertTrue(earlierCriterionDataSetIds2.contains(DATA_SET_ID_2));

        // <
        final DataSetSearchCriteria earlierCriterion3 = new DataSetSearchCriteria();
        earlierCriterion3.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsEarlierThanOrEqualTo(DATA_SET_PROPERTY_2_LATER_DATE_STRING_VALUE);
        final Set<Long> earlierCriterionDataSetIds3 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierCriterion3);
        assertEquals(earlierCriterionDataSetIds3.size(), 1);
        assertTrue(earlierCriterionDataSetIds3.contains(DATA_SET_ID_2));

        // >
        final DataSetSearchCriteria laterCriterion1 = new DataSetSearchCriteria();
        laterCriterion1.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(DATA_SET_PROPERTY_2_LATER_DATE_STRING_VALUE);
        final Set<Long> laterCriterionDataSetIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion1);
        assertEquals(laterCriterionDataSetIds1.size(), 0);

        // >
        final DataSetSearchCriteria laterCriterion2 = new DataSetSearchCriteria();
        laterCriterion2.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(DATA_SET_PROPERTY_2_DATE_STRING_VALUE);
        final Set<Long> laterCriterionDataSetIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion2);
        assertEquals(laterCriterionDataSetIds2.size(), 1);
        assertTrue(laterCriterionDataSetIds2.contains(DATA_SET_ID_2));

        // >
        final DataSetSearchCriteria laterCriterion3 = new DataSetSearchCriteria();
        laterCriterion3.withDateProperty(DATA_SET_PROPERTY_CODE_DATE).thatIsLaterThanOrEqualTo(DATA_SET_PROPERTY_2_EARLIER_DATE_STRING_VALUE);
        final Set<Long> laterCriterionDataSetIds3 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterCriterion3);
        assertEquals(laterCriterionDataSetIds3.size(), 1);
        assertTrue(laterCriterionDataSetIds3.contains(DATA_SET_ID_2));
    }

    /**
     * Tests {@link DataSetSearchManager} with internal property search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithInternalPropertyCriteria()
    {
        final DataSetSearchCriteria anyStringPropertyCriterion = new DataSetSearchCriteria();
        anyStringPropertyCriterion.withProperty(INTERNAL_DATA_SET_PROPERTY_CODE_STRING);
        final Set<Long> anyStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, anyStringPropertyCriterion);
        assertTrue(anyStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertFalse(anyStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(anyStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria equalsStringPropertyCriterion = new DataSetSearchCriteria();
        equalsStringPropertyCriterion.withProperty(INTERNAL_DATA_SET_PROPERTY_CODE_STRING).thatEquals(DATA_SET_PROPERTY_1_INTERNAL_STRING_VALUE);
        final Set<Long> equalsStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterion);
        assertEquals(equalsStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(equalsStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria equalsStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        equalsStringPropertyCriterionNotFound.withProperty(INTERNAL_DATA_SET_PROPERTY_CODE_STRING).thatEquals(DATA_SET_PROPERTY_1_INTERNAL_STRING_VALUE
                + "-");
        final Set<Long> equalsStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsStringPropertyCriterionNotFound);
        assertEquals(equalsStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria startsWithStringPropertyCriterion = new DataSetSearchCriteria();
        startsWithStringPropertyCriterion.withProperty(INTERNAL_DATA_SET_PROPERTY_CODE_STRING).thatStartsWith(DATA_SET_PROPERTY_1_INTERNAL_STRING_VALUE
                .substring(0, 10));
        final Set<Long> startsWithStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithStringPropertyCriterion);
        assertEquals(startsWithStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(startsWithStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria startsWithStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        startsWithStringPropertyCriterionNotFound.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatStartsWith(
                DATA_SET_PROPERTY_2_STRING_VALUE.substring(0, 10) + "-");
        final Set<Long> startsWithStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                startsWithStringPropertyCriterionNotFound);
        assertEquals(startsWithStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria endsWithStringPropertyCriterion = new DataSetSearchCriteria();
        endsWithStringPropertyCriterion.withProperty(INTERNAL_DATA_SET_PROPERTY_CODE_STRING).thatEndsWith(DATA_SET_PROPERTY_1_INTERNAL_STRING_VALUE
                .substring(10));
        final Set<Long> endsWithStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithStringPropertyCriterion);
        assertEquals(endsWithStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(endsWithStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria endsWithStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        endsWithStringPropertyCriterionNotFound.withProperty(INTERNAL_DATA_SET_PROPERTY_CODE_STRING).thatEndsWith(
                DATA_SET_PROPERTY_1_INTERNAL_STRING_VALUE.substring(10)
                        + "-");
        final Set<Long> endsWithStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                endsWithStringPropertyCriterionNotFound);
        assertEquals(endsWithStringPropertyCriterionDataSetIdsNotFound.size(), 0);

        final DataSetSearchCriteria containsStringPropertyCriterion = new DataSetSearchCriteria();
        containsStringPropertyCriterion.withProperty(INTERNAL_DATA_SET_PROPERTY_CODE_STRING).thatContains(
                DATA_SET_PROPERTY_1_INTERNAL_STRING_VALUE.substring(3, 10));
        final Set<Long> containsStringPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsStringPropertyCriterion);
        assertEquals(containsStringPropertyCriterionDataSetIds.size(), 1);
        assertTrue(containsStringPropertyCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria containsStringPropertyCriterionNotFound = new DataSetSearchCriteria();
        containsStringPropertyCriterionNotFound.withProperty(INTERNAL_DATA_SET_PROPERTY_CODE_STRING).thatContains(
                DATA_SET_PROPERTY_1_INTERNAL_STRING_VALUE.substring(3, 10) + "-");
        final Set<Long> containsStringPropertyCriterionDataSetIdsNotFound = searchManager.searchForIDs(ADMIN_USER_TECH_ID,
                containsStringPropertyCriterionNotFound);
        assertEquals(containsStringPropertyCriterionDataSetIdsNotFound.size(), 0);
    }

    /**
     * Tests {@link DataSetSearchManager} with compound field criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCompoundFieldCriteria()
    {
        final DataSetSearchCriteria compoundAndFieldCriterion = new DataSetSearchCriteria();
        compoundAndFieldCriterion.withAndOperator().withRegistrationDate().thatIsEarlierThanOrEqualTo(DATA_SET_REGISTRATION_DATE_2);
        compoundAndFieldCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(DATA_SET_REGISTRATION_DATE_2);
        final Set<Long> compoundAndFieldCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundAndFieldCriterion);
        assertFalse(compoundAndFieldCriterionDataSetIds.isEmpty());
        assertFalse(compoundAndFieldCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(compoundAndFieldCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertFalse(compoundAndFieldCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria compoundOrFieldCriterion = new DataSetSearchCriteria();
        compoundOrFieldCriterion.withOrOperator().withRegistrationDate().thatIsEarlierThanOrEqualTo(DATA_SET_REGISTRATION_DATE_2);
        compoundOrFieldCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(DATA_SET_REGISTRATION_DATE_2);
        final Set<Long> compoundOrFieldCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundOrFieldCriterion);
        assertFalse(compoundOrFieldCriterionDataSetIds.isEmpty());
        assertTrue(compoundOrFieldCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(compoundOrFieldCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(compoundOrFieldCriterionDataSetIds.contains(DATA_SET_ID_3));
    }

    /**
     * Tests {@link DataSetSearchManager} with compound field criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCompoundPropertyCriteria()
    {
        final DataSetSearchCriteria compoundAndPropertyCriterion1 = new DataSetSearchCriteria();
        compoundAndPropertyCriterion1.withAndOperator().withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsLessThanOrEqualTo(
                DATA_SET_PROPERTY_3_NUMBER_VALUE);
        compoundAndPropertyCriterion1.withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsGreaterThanOrEqualTo(DATA_SET_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> compoundAndPropertyCriterionDataSetIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundAndPropertyCriterion1);
        assertEquals(compoundAndPropertyCriterionDataSetIds1.size(), 1);
        assertTrue(compoundAndPropertyCriterionDataSetIds1.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria compoundAndPropertyCriterion2 = new DataSetSearchCriteria();
        compoundAndPropertyCriterion2.withAndOperator().withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsLessThan(
                DATA_SET_PROPERTY_3_NUMBER_VALUE);
        compoundAndPropertyCriterion2.withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsGreaterThanOrEqualTo(DATA_SET_PROPERTY_3_NUMBER_VALUE);
        final Set<Long> compoundAndPropertyCriterionDataSetIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundAndPropertyCriterion2);
        assertEquals(compoundAndPropertyCriterionDataSetIds2.size(), 0);

        final DataSetSearchCriteria compoundOrPropertyCriterion = new DataSetSearchCriteria();
        compoundOrPropertyCriterion.withOrOperator().withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatEquals(DATA_SET_PROPERTY_3_NUMBER_VALUE);
        compoundOrPropertyCriterion.withProperty(DATA_SET_PROPERTY_CODE_STRING).thatEquals(DATA_SET_PROPERTY_2_STRING_VALUE);
        final Set<Long> compoundOrPropertyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundOrPropertyCriterion);
        assertEquals(compoundOrPropertyCriterionDataSetIds.size(), 2);
        assertTrue(compoundOrPropertyCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(compoundOrPropertyCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria compoundAndPropertyFieldCriterion = new DataSetSearchCriteria();
        compoundAndPropertyFieldCriterion.withAndOperator().withCodes().thatIn(Arrays.asList(DATA_SET_CODE_1, DATA_SET_CODE_3));
        compoundAndPropertyFieldCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_DOUBLE).thatIsGreaterThan(DATA_SET_PROPERTY_3_NUMBER_VALUE
                - 0.000001);
        final Set<Long> compoundAndPropertyFieldCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundAndPropertyFieldCriterion);
        assertEquals(compoundAndPropertyFieldCriterionDataSetIds.size(), 1);
        assertTrue(compoundAndPropertyFieldCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria compoundOrPropertyFieldCriterion = new DataSetSearchCriteria();
        compoundOrPropertyFieldCriterion.withOrOperator().withPermId().thatEquals(DATA_SET_CODE_2);
        compoundOrPropertyFieldCriterion.withNumberProperty(DATA_SET_PROPERTY_CODE_LONG).thatEquals(DATA_SET_PROPERTY_1_NUMBER_VALUE);
        final Set<Long> compoundOrPropertyFieldCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, compoundOrPropertyFieldCriterion);
        assertEquals(compoundOrPropertyFieldCriterionDataSetIds.size(), 2);
        assertTrue(compoundOrPropertyFieldCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(compoundOrPropertyFieldCriterionDataSetIds.contains(DATA_SET_ID_2));
    }

    /**
     * Tests {@link DataSetSearchManager} with registrator field criteria using DB connection.
     */
    @Test
    public void testQueryDBWithRegistratorCriteria()
    {
        // Any registrator search
        // This is a trivial search since registrator is a mandatory field, so the result set will contain all records
        final DataSetSearchCriteria emptyCriterion = new DataSetSearchCriteria();
        emptyCriterion.withRegistrator();
        final Set<Long> emptyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, emptyCriterion);
        assertFalse(emptyCriterionDataSetIds.isEmpty());

        // By ID
        final DataSetSearchCriteria idCriterion = new DataSetSearchCriteria();
        idCriterion.withRegistrator().withUserId().thatEquals(REGISTRATOR_USER_ID);
        final Set<Long> idCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, idCriterion);
        assertEquals(idCriterionDataSetIds.size(), 1);
        assertTrue(idCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria notExistingIdCriterion = new DataSetSearchCriteria();
        notExistingIdCriterion.withRegistrator().withUserId().thatEquals("-");
        final Set<Long> notExistingIdCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingIdCriterion);
        assertTrue(notExistingIdCriterionDataSetIds.isEmpty());

        // By IDs
        final DataSetSearchCriteria idsCriterion = new DataSetSearchCriteria();
        idsCriterion.withRegistrator().withUserIds().thatIn(Arrays.asList(REGISTRATOR_USER_ID, ADMIN_USER_ID));
        final Set<Long> idsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, idsCriterion);
        assertEquals(idsCriterionDataSetIds.size(), 2);
        assertTrue(idsCriterionDataSetIds.contains(DATA_SET_ID_1));
        assertTrue(idsCriterionDataSetIds.contains(DATA_SET_ID_2));

        // By First Name
        final DataSetSearchCriteria firstNameCriterion = new DataSetSearchCriteria();
        firstNameCriterion.withRegistrator().withFirstName().thatEquals(REGISTRATOR_FIRST_NAME);
        final Set<Long> firstNameCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, firstNameCriterion);
        assertEquals(firstNameCriterionDataSetIds.size(), 1);
        assertTrue(firstNameCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria notExistingFirstNameCriterion = new DataSetSearchCriteria();
        notExistingFirstNameCriterion.withRegistrator().withFirstName().thatEquals("-");
        final Set<Long> notExistingFirstNameCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingFirstNameCriterion);
        assertTrue(notExistingFirstNameCriterionDataSetIds.isEmpty());

        // By Last Name
        final DataSetSearchCriteria lastNameCriterion = new DataSetSearchCriteria();
        lastNameCriterion.withRegistrator().withLastName().thatEquals(REGISTRATOR_LAST_NAME);
        final Set<Long> lastNameCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, lastNameCriterion);
        assertEquals(lastNameCriterionDataSetIds.size(), 1);
        assertTrue(lastNameCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria notExistingLastNameCriterion = new DataSetSearchCriteria();
        notExistingLastNameCriterion.withRegistrator().withLastName().thatEquals("-");
        final Set<Long> notExistingLastNameCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingLastNameCriterion);
        assertTrue(notExistingLastNameCriterionDataSetIds.isEmpty());

        // By Email
        final DataSetSearchCriteria emailCriterion = new DataSetSearchCriteria();
        emailCriterion.withRegistrator().withEmail().thatEquals(REGISTRATOR_EMAIL);
        final Set<Long> emailCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, emailCriterion);
        assertEquals(emailCriterionDataSetIds.size(), 1);
        assertTrue(emailCriterionDataSetIds.contains(DATA_SET_ID_1));

        final DataSetSearchCriteria notExistingEmailCriterion = new DataSetSearchCriteria();
        notExistingEmailCriterion.withRegistrator().withEmail().thatEquals("-");
        final Set<Long> notExistingEmailCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingEmailCriterion);
        assertTrue(notExistingEmailCriterionDataSetIds.isEmpty());
    }

    /**
     * Tests {@link DataSetSearchManager} with modifier field criteria using DB connection.
     */
    @Test
    public void testQueryDBWithModifierCriteria()
    {
        // Any modifier search
        // This is a trivial search since modifier is a mandatory field, so the result set will contain all records
        final DataSetSearchCriteria emptyCriterion = new DataSetSearchCriteria();
        emptyCriterion.withModifier();
        final Set<Long> emptyCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, emptyCriterion);
        assertFalse(emptyCriterionDataSetIds.isEmpty());

        // By ID
        final DataSetSearchCriteria idCriterion = new DataSetSearchCriteria();
        idCriterion.withModifier().withUserId().thatEquals(MODIFIER_USER_ID);
        final Set<Long> idCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, idCriterion);
        assertEquals(idCriterionDataSetIds.size(), 1);
        assertTrue(idCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria notExistingIdCriterion = new DataSetSearchCriteria();
        notExistingIdCriterion.withModifier().withUserId().thatEquals("-");
        final Set<Long> notExistingIdCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingIdCriterion);
        assertTrue(notExistingIdCriterionDataSetIds.isEmpty());

        // By IDs
        final DataSetSearchCriteria idsCriterion = new DataSetSearchCriteria();
        idsCriterion.withModifier().withUserIds().thatIn(Arrays.asList(MODIFIER_USER_ID, ADMIN_USER_ID));
        final Set<Long> idsCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, idsCriterion);
        assertEquals(idsCriterionDataSetIds.size(), 2);
        assertTrue(idsCriterionDataSetIds.contains(DATA_SET_ID_2));
        assertTrue(idsCriterionDataSetIds.contains(DATA_SET_ID_3));

        // By First Name
        final DataSetSearchCriteria firstNameCriterion = new DataSetSearchCriteria();
        firstNameCriterion.withModifier().withFirstName().thatEquals(MODIFIER_FIRST_NAME);
        final Set<Long> firstNameCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, firstNameCriterion);
        assertEquals(firstNameCriterionDataSetIds.size(), 1);
        assertTrue(firstNameCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria notExistingFirstNameCriterion = new DataSetSearchCriteria();
        notExistingFirstNameCriterion.withModifier().withFirstName().thatEquals("-");
        final Set<Long> notExistingFirstNameCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingFirstNameCriterion);
        assertTrue(notExistingFirstNameCriterionDataSetIds.isEmpty());

        // By Last Name
        final DataSetSearchCriteria lastNameCriterion = new DataSetSearchCriteria();
        lastNameCriterion.withModifier().withLastName().thatEquals(MODIFIER_LAST_NAME);
        final Set<Long> lastNameCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, lastNameCriterion);
        assertEquals(lastNameCriterionDataSetIds.size(), 1);
        assertTrue(lastNameCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria notExistingLastNameCriterion = new DataSetSearchCriteria();
        notExistingLastNameCriterion.withModifier().withLastName().thatEquals("-");
        final Set<Long> notExistingLastNameCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingLastNameCriterion);
        assertTrue(notExistingLastNameCriterionDataSetIds.isEmpty());

        // By Email
        final DataSetSearchCriteria emailCriterion = new DataSetSearchCriteria();
        emailCriterion.withModifier().withEmail().thatEquals(MODIFIER_EMAIL);
        final Set<Long> emailCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, emailCriterion);
        assertEquals(emailCriterionDataSetIds.size(), 1);
        assertTrue(emailCriterionDataSetIds.contains(DATA_SET_ID_3));

        final DataSetSearchCriteria notExistingEmailCriterion = new DataSetSearchCriteria();
        notExistingEmailCriterion.withModifier().withEmail().thatEquals("-");
        final Set<Long> notExistingEmailCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, notExistingEmailCriterion);
        assertTrue(notExistingEmailCriterionDataSetIds.isEmpty());
    }


//
//    /**
//     * Tests {@link DataSetSearchManager} with child dataSet criteria using DB connection.
//     */
//    @Test
//    public void testQueryDBWithChildCriteria()
//    {
//        final DataSetSearchCriteria childIdCriterion = new DataSetSearchCriteria();
//        childIdCriterion.withChildren().withCode().thatEquals(DATA_SET_CODE_4);
//        final Set<Long> childIdCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, childIdCriterion);
//        assertEquals(childIdCriterionDataSetIds.size(), 1);
//        assertTrue(childIdCriterionDataSetIds.contains(DATA_SET_ID_1));
//
//        final DataSetSearchCriteria childOrIdCriterion = new DataSetSearchCriteria().withOrOperator();
//        childOrIdCriterion.withChildren().withCode().thatEquals(DATA_SET_CODE_3);
//        childOrIdCriterion.withChildren().withCode().thatEquals(DATA_SET_CODE_4);
//        final Set<Long> childOrIdCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, childOrIdCriterion);
//        assertEquals(childOrIdCriterionDataSetIds.size(), 2);
//        assertTrue(childOrIdCriterionDataSetIds.contains(DATA_SET_ID_1));
//        assertTrue(childOrIdCriterionDataSetIds.contains(DATA_SET_ID_2));
//
//        final DataSetSearchCriteria childAndIdCriterion = new DataSetSearchCriteria().withAndOperator();
//        childAndIdCriterion.withChildren().withCode().thatEquals(DATA_SET_CODE_3);
//        childAndIdCriterion.withChildren().withCode().thatEquals(DATA_SET_CODE_4);
//        final Set<Long> childAndIdCriterionDataSetIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, childAndIdCriterion);
//        assertTrue(childAndIdCriterionDataSetIds.isEmpty());
//    }

}
