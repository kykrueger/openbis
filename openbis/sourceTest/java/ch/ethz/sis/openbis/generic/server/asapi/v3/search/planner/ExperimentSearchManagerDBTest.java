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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.ADMIN_USER_TECH_ID;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_CODE_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_CODE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_CODE_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_ID_3;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_MODIFICATION_DATE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_MODIFICATION_DATE_STRING_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_PERM_ID_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_PERM_ID_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_REGISTRATION_DATE_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_REGISTRATION_DATE_STRING_1;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.EXPERIMENT_REGISTRATION_DATE_STRING_2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DBTestHelper.PROJECT_ID_2;
import static junit.framework.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ExperimentSearchManagerDBTest
{
    private static final AbstractApplicationContext context = new ClassPathXmlApplicationContext("testApplicationContext.xml");

    private DBTestHelper dbTestHelper = context.getBean(DBTestHelper.class);

    private ExperimentSearchManager searchManager;

    public ExperimentSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUpClass() throws Exception
    {
        searchManager = context.getBean("experiment-search-manager", ExperimentSearchManager.class);
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
     * Tests {@link ExperimentSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCode()
    {
        final ExperimentSearchCriteria equalsCriterion = new ExperimentSearchCriteria();
        equalsCriterion.withCode().thatEquals(EXPERIMENT_CODE_1);
        checkCodeCriterion(equalsCriterion);

        final ExperimentSearchCriteria containsCriterion = new ExperimentSearchCriteria();
        containsCriterion.withCode().thatContains(EXPERIMENT_CODE_1.substring(1, EXPERIMENT_CODE_1.length() - 1));
        checkCodeCriterion(containsCriterion);

        final ExperimentSearchCriteria startsWithCriterion = new ExperimentSearchCriteria();
        startsWithCriterion.withCode().thatStartsWith(EXPERIMENT_CODE_1.substring(0, 4));
        checkCodeCriterion(startsWithCriterion);

        final ExperimentSearchCriteria endsWithCriterion = new ExperimentSearchCriteria();
        endsWithCriterion.withCode().thatEndsWith(EXPERIMENT_CODE_1.substring(4));
        checkCodeCriterion(endsWithCriterion);
    }

    /**
     * Checks if the criterion returns expected results.
     *
     * @param criterion criterion to be checked.
     */
    private void checkCodeCriterion(final ExperimentSearchCriteria criterion)
    {
        final Set<Long> sampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertEquals(sampleIds.size(), 1);
        assertEquals(sampleIds.iterator().next().longValue(), EXPERIMENT_ID_1);
    }

    /**
     * Tests {@link ExperimentSearchManager} with {@link RegistrationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithRegistrationDateField()
    {
        final ExperimentSearchCriteria equalsCriterion = new ExperimentSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(EXPERIMENT_REGISTRATION_DATE_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(EXPERIMENT_ID_2));

        final ExperimentSearchCriteria earlierThanCriterion = new ExperimentSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(EXPERIMENT_REGISTRATION_DATE_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_3));

        final ExperimentSearchCriteria laterThanCriterion = new ExperimentSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(EXPERIMENT_REGISTRATION_DATE_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_3));
    }

    /**
     * Tests {@link ExperimentSearchManager} with {@link ModificationDateSearchCriteria} search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithModificationDateField()
    {
        final ExperimentSearchCriteria equalsCriterion = new ExperimentSearchCriteria();
        equalsCriterion.withModificationDate().thatEquals(EXPERIMENT_MODIFICATION_DATE_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(EXPERIMENT_ID_2));

        final ExperimentSearchCriteria earlierThanCriterion = new ExperimentSearchCriteria();
        earlierThanCriterion.withModificationDate().thatIsEarlierThanOrEqualTo(EXPERIMENT_MODIFICATION_DATE_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_3));

        final ExperimentSearchCriteria laterThanCriterion = new ExperimentSearchCriteria();
        laterThanCriterion.withModificationDate().thatIsLaterThanOrEqualTo(EXPERIMENT_MODIFICATION_DATE_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_3));
    }

    /**
     * Tests {@link ExperimentSearchManager} with {@link RegistrationDateSearchCriteria} attribute search criteria (in string form) using DB
     * connection.
     */
    @Test
    public void testQueryDBWithStringRegistrationDateField()
    {
        final ExperimentSearchCriteria equalsCriterion = new ExperimentSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(EXPERIMENT_REGISTRATION_DATE_STRING_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(EXPERIMENT_ID_2));

        final ExperimentSearchCriteria earlierThanCriterion = new ExperimentSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(EXPERIMENT_REGISTRATION_DATE_STRING_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_3));

        final ExperimentSearchCriteria laterThanCriterion = new ExperimentSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(EXPERIMENT_REGISTRATION_DATE_STRING_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_3));
    }

    /**
     * Tests {@link ExperimentSearchManager} with {@link ModificationDateSearchCriteria} attribute search criteria (in string form) using DB
     * connection.
     */
    @Test
    public void testQueryDBWithStringModificationDateField()
    {
        final ExperimentSearchCriteria equalsCriterion = new ExperimentSearchCriteria();
        equalsCriterion.withModificationDate().thatEquals(EXPERIMENT_MODIFICATION_DATE_STRING_2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(EXPERIMENT_ID_2));

        final ExperimentSearchCriteria earlierThanCriterion = new ExperimentSearchCriteria();
        earlierThanCriterion.withModificationDate().thatIsEarlierThanOrEqualTo(EXPERIMENT_MODIFICATION_DATE_STRING_2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(earlierThanCriterionSampleIds.contains(EXPERIMENT_ID_3));

        final ExperimentSearchCriteria laterThanCriterion = new ExperimentSearchCriteria();
        laterThanCriterion.withModificationDate().thatIsLaterThanOrEqualTo(EXPERIMENT_MODIFICATION_DATE_STRING_2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(laterThanCriterionSampleIds.contains(EXPERIMENT_ID_3));
    }

    /**
     * Tests {@link ExperimentSearchManager} with codes (collection attribute) search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCodes()
    {
        final ExperimentSearchCriteria criterion = new ExperimentSearchCriteria();
        criterion.withCodes().thatIn(Arrays.asList(EXPERIMENT_CODE_1, EXPERIMENT_CODE_3));
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, criterion);
        assertEquals(criterionSampleIds.size(), 2);
        assertTrue(criterionSampleIds.contains(EXPERIMENT_ID_1));
        assertFalse(criterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(criterionSampleIds.contains(EXPERIMENT_ID_3));
    }

    /**
     * Tests {@link ExperimentSearchManager} with string attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithPermId()
    {
        final ExperimentSearchCriteria equalsCriterion = new ExperimentSearchCriteria();
        equalsCriterion.withPermId().thatEquals(EXPERIMENT_PERM_ID_2);
        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, equalsCriterion);
        assertEquals(equalsCriterionSampleIds.size(), 1);
        assertFalse(equalsCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(equalsCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(equalsCriterionSampleIds.contains(EXPERIMENT_ID_3));

        final ExperimentSearchCriteria startsWithCriterion = new ExperimentSearchCriteria();
        startsWithCriterion.withPermId().thatStartsWith(EXPERIMENT_PERM_ID_1.substring(0, EXPERIMENT_PERM_ID_1.length() - 2));
        final Set<Long> startsWithCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithCriterion);
        assertEquals(startsWithCriterionSampleIds.size(), 2);
        assertTrue(startsWithCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertFalse(startsWithCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(startsWithCriterionSampleIds.contains(EXPERIMENT_ID_3));

        final ExperimentSearchCriteria endsWithCriterion = new ExperimentSearchCriteria();
        endsWithCriterion.withPermId().thatEndsWith(EXPERIMENT_PERM_ID_1.substring(EXPERIMENT_PERM_ID_1.length() - 4));
        final Set<Long> endsWithCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion);
        assertEquals(endsWithCriterionSampleIds.size(), 2);
        assertTrue(endsWithCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(endsWithCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(endsWithCriterionSampleIds.contains(EXPERIMENT_ID_3));

        final ExperimentSearchCriteria containsCriterion = new ExperimentSearchCriteria();
        containsCriterion.withPermId().thatContains(EXPERIMENT_PERM_ID_1.substring(4, 12));
        final Set<Long> containsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsCriterion);
        assertEquals(containsCriterionSampleIds.size(), 3);
        assertTrue(containsCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(containsCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(containsCriterionSampleIds.contains(EXPERIMENT_ID_3));
    }

    /**
     * Tests {@link ExperimentSearchManager} with {@link AnyFieldSearchCriteria} attribute search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithAnyField()
    {
        // code attribute
        final ExperimentSearchCriteria codeEqualsCriterion = new ExperimentSearchCriteria();
        codeEqualsCriterion.withAnyField().thatEquals(EXPERIMENT_CODE_2);
        final Set<Long> codeEqualsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, codeEqualsCriterion);
        assertEquals(codeEqualsCriterionSampleIds.size(), 1);
        assertFalse(codeEqualsCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(codeEqualsCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(codeEqualsCriterionSampleIds.contains(EXPERIMENT_ID_3));

        // project_id attribute
        final ExperimentSearchCriteria projectIdEqualsCriterion = new ExperimentSearchCriteria();
        projectIdEqualsCriterion.withAnyField().thatEquals(String.valueOf(PROJECT_ID_2));
        final Set<Long> projectIdEqualsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, projectIdEqualsCriterion);
        assertEquals(projectIdEqualsCriterionSampleIds.size(), 2);
        assertFalse(projectIdEqualsCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(projectIdEqualsCriterionSampleIds.contains(EXPERIMENT_ID_3));

        // project_id attribute
        final ExperimentSearchCriteria registrationDateEqualsCriterion = new ExperimentSearchCriteria();
        registrationDateEqualsCriterion.withAnyField().thatEquals(EXPERIMENT_REGISTRATION_DATE_STRING_1);
        final Set<Long> registrationDateEqualsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, registrationDateEqualsCriterion);
        assertEquals(registrationDateEqualsCriterionSampleIds.size(), 1);
        assertTrue(registrationDateEqualsCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertFalse(registrationDateEqualsCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertFalse(registrationDateEqualsCriterionSampleIds.contains(EXPERIMENT_ID_3));

        // perm_id attribute
        final ExperimentSearchCriteria startsWithCriterion = new ExperimentSearchCriteria();
        startsWithCriterion.withAnyField().thatStartsWith(EXPERIMENT_PERM_ID_1.substring(0, EXPERIMENT_PERM_ID_1.length() - 2));
        final Set<Long> startsWithCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, startsWithCriterion);
        assertEquals(startsWithCriterionSampleIds.size(), 2);
        assertTrue(startsWithCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertFalse(startsWithCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(startsWithCriterionSampleIds.contains(EXPERIMENT_ID_3));

        // code attribute
        final ExperimentSearchCriteria endsWithCriterion1 = new ExperimentSearchCriteria();
        endsWithCriterion1.withAnyField().thatEndsWith(EXPERIMENT_CODE_2.substring(EXPERIMENT_CODE_2.length() - 5));
        final Set<Long> endsWithCriterionSampleIds1 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion1);
        assertEquals(endsWithCriterionSampleIds1.size(), 1);
        assertFalse(endsWithCriterionSampleIds1.contains(EXPERIMENT_ID_1));
        assertTrue(endsWithCriterionSampleIds1.contains(EXPERIMENT_ID_2));
        assertFalse(endsWithCriterionSampleIds1.contains(EXPERIMENT_ID_3));

        // id, perm_id and samp_id_part_of fields
        final ExperimentSearchCriteria endsWithCriterion2 = new ExperimentSearchCriteria();
        endsWithCriterion2.withAnyField().thatEndsWith(String.valueOf(EXPERIMENT_ID_1));
        final Set<Long> endsWithCriterionSampleIds2 = searchManager.searchForIDs(ADMIN_USER_TECH_ID, endsWithCriterion2);
        assertEquals(endsWithCriterionSampleIds2.size(), 2);
        assertTrue(endsWithCriterionSampleIds2.contains(EXPERIMENT_ID_1));
        assertTrue(endsWithCriterionSampleIds2.contains(EXPERIMENT_ID_2));
        assertFalse(endsWithCriterionSampleIds2.contains(EXPERIMENT_ID_3));

        // expe_id
        final ExperimentSearchCriteria containsCriterion = new ExperimentSearchCriteria();
        containsCriterion.withAnyField().thatContains(String.valueOf(EXPERIMENT_ID_3));
        final Set<Long> containsCriterionSampleIds = searchManager.searchForIDs(ADMIN_USER_TECH_ID, containsCriterion);
        assertEquals(containsCriterionSampleIds.size(), 1);
        assertFalse(containsCriterionSampleIds.contains(EXPERIMENT_ID_1));
        assertFalse(containsCriterionSampleIds.contains(EXPERIMENT_ID_2));
        assertTrue(containsCriterionSampleIds.contains(EXPERIMENT_ID_3));
    }

}
