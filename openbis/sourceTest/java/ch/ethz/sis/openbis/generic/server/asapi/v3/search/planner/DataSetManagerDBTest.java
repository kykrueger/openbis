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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ADMIN_USER_TECH_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_CODE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_CODE_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_ID_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_ID_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_MODIFICATION_DATE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_MODIFICATION_DATE_STRING_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_REGISTRATION_DATE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_REGISTRATION_DATE_STRING_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_SET_REGISTRATION_DATE_STRING_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.DATA_STORE_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID_3;
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

}
