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
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.JDBCSQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.StringFieldSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.COMMA;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DELETE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.INSERT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.INTO;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.VALUES;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class SampleSearchManagerDBTest
{

    public static final long USER_ID = 2L;

    private static final String PERM_ID1 = "20190612105000000-1";

    private static final String PERM_ID2 = "20190612105000000-2";

    private static final String PERM_ID3 = "20190612105000000-3";

    private static final long ID1 = 1001L;

    private static final long ID2 = 1002L;

    private static final long ID3 = 1003L;

    private static final String CODE1 = "MY_UNIQUE_CODE1";

    private static final String CODE2 = "NOT_UNIQUE_CODE2";

    private static final String CODE3 = "NOT_UNIQUE_CODE3";

    private static final String DEFAULT_CODE = "NOT_UNIQUE_CODE";

    private static final int VERSION1 = 101;

    private static final int VERSION2 = 102;

    private static final int VERSION3 = 103;

    private static final Date REGISTRATION_DATE1 = new Date(119, Calendar.JUNE, 11, 10, 50, 0);

    private static final Date REGISTRATION_DATE2 = new Date(119, Calendar.JUNE, 12, 10, 50, 0);

    private static final Date REGISTRATION_DATE3 = new Date(119, Calendar.JUNE, 13, 10, 50, 0);

    private static final String REGISTRATION_DATE_STRING1 = "2019-06-11 10:50:00 +0200";

    private static final String REGISTRATION_DATE_STRING2 = "2019-06-12 10:50:00 +0200";

    private static final String REGISTRATION_DATE_STRING3 = "2019-06-13 10:50:00 +0200";

    private SampleSearchManager searchManager;

    private JDBCSQLExecutor sqlExecutor;

    private Connection connection;

    public SampleSearchManagerDBTest() throws ClassNotFoundException
    {
        Class.forName("org.postgresql.Driver");
    }

    @BeforeClass
    public void setUp() throws Exception
    {
        connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/openbis_dev", "postgres", "");
        sqlExecutor = new JDBCSQLExecutor(connection);
        final PostgresSearchDAO searchDAO = new PostgresSearchDAO(sqlExecutor);
        final ISQLAuthorisationInformationProviderDAO authInfoProviderDAO =
                new PostgresAuthorisationInformationProviderDAO(sqlExecutor);

        searchManager = new SampleSearchManager(searchDAO, authInfoProviderDAO);

        populateDB();
    }

    private void populateDB()
    {
        // TODO: create related entities, and not just use 1 as ID.

        final Map<String, Object> valuesMap1 = getDefaultValuesMap();
        valuesMap1.put(ColumnNames.ID_COLUMN, ID1);
        valuesMap1.put(ColumnNames.PERM_ID_COLUMN, PERM_ID1);
        valuesMap1.put(ColumnNames.VERSION_COLUMN, VERSION1);
        valuesMap1.put(ColumnNames.CODE_COLUMN, CODE1);
        valuesMap1.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, REGISTRATION_DATE1);
        valuesMap1.put(ColumnNames.SPACE_COLUMN, 1);
//        valuesMap1.put(ColumnNames.PART_OF_SAMPLE_COLUMN, 1);

        final Map<String, Object> valuesMap2 = getDefaultValuesMap();
        valuesMap2.put(ColumnNames.PERM_ID_COLUMN, PERM_ID2);
        valuesMap2.put(ColumnNames.ID_COLUMN, ID2);
        valuesMap2.put(ColumnNames.VERSION_COLUMN, VERSION2);
        valuesMap2.put(ColumnNames.CODE_COLUMN, CODE2);
        valuesMap2.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, REGISTRATION_DATE2);
        valuesMap2.put(ColumnNames.PROJECT_COLUMN, 1);

        final Map<String, Object> valuesMap3 = getDefaultValuesMap();
        valuesMap3.put(ColumnNames.PERM_ID_COLUMN, PERM_ID3);
        valuesMap3.put(ColumnNames.ID_COLUMN, ID3);
        valuesMap3.put(ColumnNames.VERSION_COLUMN, VERSION3);
        valuesMap3.put(ColumnNames.CODE_COLUMN, CODE3);
        valuesMap3.put(ColumnNames.REGISTRATION_TIMESTAMP_COLUMN, REGISTRATION_DATE3);
        valuesMap3.put(ColumnNames.PROJECT_COLUMN, 1);
        valuesMap3.put(ColumnNames.EXPERIMENT_COLUMN, 1);

        insertRecord(TableNames.SAMPLES_ALL_TABLE, valuesMap1);
        insertRecord(TableNames.SAMPLES_ALL_TABLE, valuesMap2);
        insertRecord(TableNames.SAMPLES_ALL_TABLE, valuesMap3);
    }

    /**
     * Creates a map what contains default values for an object to be stored in DB.
     *
     * @return a map that represents an object to be stored in the DB with default values.
     */
    private Map<String, Object> getDefaultValuesMap()
    {
        final Map<String, Object> valuesMap = new LinkedHashMap<String, Object>();
        valuesMap.put(ColumnNames.CODE_COLUMN, DEFAULT_CODE);
        valuesMap.put(ColumnNames.EXPERIMENT_COLUMN, null);
        valuesMap.put(ColumnNames.SAMPLE_TYPE_COLUMN, 1);
        valuesMap.put(ColumnNames.MODIFICATION_TIMESTAMP_COLUMN, new Date(1019, Calendar.JUNE, 12, 10, 50, 0));
        valuesMap.put(ColumnNames.PERSON_REGISTERER_COLUMN, 1);
        valuesMap.put(ColumnNames.DELETION_COLUMN, null);
        valuesMap.put(ColumnNames.ORIGINAL_DELETION_COLUMN, null);
        valuesMap.put(ColumnNames.SPACE_COLUMN, null);
        valuesMap.put(ColumnNames.PART_OF_SAMPLE_COLUMN, null);
        valuesMap.put(ColumnNames.PERSON_MODIFIER_COLUMN, null);
        valuesMap.put(ColumnNames.VERSION_COLUMN, 0);
        valuesMap.put(ColumnNames.PROJECT_COLUMN, null);
        valuesMap.put(ColumnNames.FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_COMPONENT_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_CHILDREN_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_PARENTS_COLUMN, false);
        valuesMap.put(ColumnNames.FROZEN_FOR_DATA_SET_COLUMN, false);
        valuesMap.put(ColumnNames.SPACE_FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.PROJECT_FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.EXPERIMENT_FROZEN_COLUMN, false);
        valuesMap.put(ColumnNames.CONTAINER_FROZEN_COLUMN, false);
        return valuesMap;
    }

    private void insertRecord(final String tableName, final Map<String, Object> valuesMap)
    {
        final StringBuilder columnNames = new StringBuilder();
        final StringBuilder questionMarks = new StringBuilder();
        final List<Object> values = new ArrayList<>(valuesMap.size());

        final AtomicBoolean first = new AtomicBoolean(true);
        valuesMap.forEach((key, value) -> {
            if (first.get())
            {
                first.set(false);
            } else {
                columnNames.append(COMMA).append(SP);
                questionMarks.append(COMMA).append(SP);
            }

            columnNames.append(key);
            questionMarks.append(QU);
            values.add(value);
        });

        sqlExecutor.executeUpdate(INSERT + SP + INTO + SP + tableName + NL +
                LP + columnNames + RP + NL +
                VALUES + SP + LP + questionMarks + RP, values);
    }

    @AfterClass
    public void tearDown() throws Exception
    {
        cleanDB();

        if (connection != null)
        {
            connection.close();
        }
    }

    private void cleanDB()
    {
        deleteRecord(TableNames.SAMPLES_ALL_TABLE, ColumnNames.ID_COLUMN, ID1);
        deleteRecord(TableNames.SAMPLES_ALL_TABLE, ColumnNames.ID_COLUMN, ID2);
        deleteRecord(TableNames.SAMPLES_ALL_TABLE, ColumnNames.ID_COLUMN, ID3);
    }

    private void deleteRecord(final String tableName, final String key, final Object value)
    {
        sqlExecutor.executeUpdate(DELETE + SP + FROM + SP + tableName + NL +
                WHERE + SP + key + EQ + QU, Arrays.asList(value));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with string field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithStringFieldSearchCriteria()
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
        assertEquals(sampleIds.iterator().next().longValue(), ID1);
    }

//    /**
//     * Tests {@link StringFieldSearchCriteriaTranslator} using DB connection.
//     */
//    @Test
//    public void testQueryDBWithNumberFieldSearchCriteria()
//    {
//        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
//        equalsCriterion.withCode().thatEquals(VERSION2);
//        final Set<Long> equalsCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
//        assertEquals(equalsCriterionSampleIds.size(), 1);
//        assertEquals(equalsCriterionSampleIds.iterator().next().longValue(), ID2);
//
//        final SampleSearchCriteria lessThanCriterion = new SampleSearchCriteria();
//        lessThanCriterion.withNumberProperty(ColumnNames.VERSION_COLUMN).thatIsLessThan(VERSION2);
//        final Set<Long> lessThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, lessThanCriterion);
//        assertFalse(lessThanCriterionSampleIds.isEmpty());
//        assertTrue(lessThanCriterionSampleIds.contains(ID1));
//        assertFalse(lessThanCriterionSampleIds.contains(ID2));
//        assertFalse(lessThanCriterionSampleIds.contains(ID3));
//
//        final SampleSearchCriteria lessThanOrEqualsToCriterion = new SampleSearchCriteria();
//        lessThanOrEqualsToCriterion.withNumberProperty(ColumnNames.VERSION_COLUMN).thatIsLessThanOrEqualTo(VERSION2);
//        final Set<Long> lessThanOrEqualsToCriterionSampleIds = searchManager.searchForIDs(USER_ID, lessThanOrEqualsToCriterion);
//        assertFalse(lessThanOrEqualsToCriterionSampleIds.isEmpty());
//        assertTrue(lessThanOrEqualsToCriterionSampleIds.contains(ID1));
//        assertTrue(lessThanOrEqualsToCriterionSampleIds.contains(ID2));
//        assertFalse(lessThanOrEqualsToCriterionSampleIds.contains(ID3));
//
//        final SampleSearchCriteria greaterThanCriterion = new SampleSearchCriteria();
//        greaterThanCriterion.withNumberProperty(ColumnNames.VERSION_COLUMN).thatIsGreaterThan(VERSION2);
//        final Set<Long> greaterThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, greaterThanCriterion);
//        assertFalse(greaterThanCriterionSampleIds.isEmpty());
//        assertFalse(greaterThanCriterionSampleIds.contains(ID1));
//        assertFalse(greaterThanCriterionSampleIds.contains(ID2));
//        assertTrue(greaterThanCriterionSampleIds.contains(ID3));
//
//        final SampleSearchCriteria greaterThanOrEqualsToCriterion = new SampleSearchCriteria();
//        greaterThanOrEqualsToCriterion.withNumberProperty(ColumnNames.VERSION_COLUMN).thatIsGreaterThanOrEqualTo(VERSION2);
//        final Set<Long> greaterThanOrEqualsToCriterionSampleIds = searchManager.searchForIDs(USER_ID, greaterThanOrEqualsToCriterion);
//        assertFalse(greaterThanOrEqualsToCriterionSampleIds.isEmpty());
//        assertFalse(greaterThanOrEqualsToCriterionSampleIds.contains(ID1));
//        assertTrue(greaterThanOrEqualsToCriterionSampleIds.contains(ID2));
//        assertTrue(greaterThanOrEqualsToCriterionSampleIds.contains(ID3));
//    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with date field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithDateFieldSearchCriteria()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(REGISTRATION_DATE2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(ID2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(REGISTRATION_DATE2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(ID1));
        assertTrue(earlierThanCriterionSampleIds.contains(ID2));
        assertFalse(earlierThanCriterionSampleIds.contains(ID3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(REGISTRATION_DATE2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(ID1));
        assertTrue(laterThanCriterionSampleIds.contains(ID2));
        assertTrue(laterThanCriterionSampleIds.contains(ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with date field search criteria (in string form) using DB connection.
     */
    @Test
    public void testQueryDBWithStringDateFieldSearchCriteria()
    {
        final SampleSearchCriteria equalsCriterion = new SampleSearchCriteria();
        equalsCriterion.withRegistrationDate().thatEquals(REGISTRATION_DATE_STRING2);
        final Set<Long> equalCriterionSampleIds = searchManager.searchForIDs(USER_ID, equalsCriterion);
        assertEquals(equalCriterionSampleIds.size(), 1);
        assertTrue(equalCriterionSampleIds.contains(ID2));

        final SampleSearchCriteria earlierThanCriterion = new SampleSearchCriteria();
        earlierThanCriterion.withRegistrationDate().thatIsEarlierThanOrEqualTo(REGISTRATION_DATE_STRING2);
        final Set<Long> earlierThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, earlierThanCriterion);
        assertFalse(earlierThanCriterionSampleIds.isEmpty());
        assertTrue(earlierThanCriterionSampleIds.contains(ID1));
        assertTrue(earlierThanCriterionSampleIds.contains(ID2));
        assertFalse(earlierThanCriterionSampleIds.contains(ID3));

        final SampleSearchCriteria laterThanCriterion = new SampleSearchCriteria();
        laterThanCriterion.withRegistrationDate().thatIsLaterThanOrEqualTo(REGISTRATION_DATE_STRING2);
        final Set<Long> laterThanCriterionSampleIds = searchManager.searchForIDs(USER_ID, laterThanCriterion);
        assertFalse(laterThanCriterionSampleIds.isEmpty());
        assertFalse(laterThanCriterionSampleIds.contains(ID1));
        assertTrue(laterThanCriterionSampleIds.contains(ID2));
        assertTrue(laterThanCriterionSampleIds.contains(ID3));
    }

//    /**
//     * Tests {@link StringFieldSearchCriteriaTranslator} with boolean field search criteria using DB connection.
//     */
//    @Test
//    public void testQueryDBWithBooleanFieldSearchCriteria()
//    {
//        final SampleSearchCriteria criterion = new SampleSearchCriteria();
//    }

//    /**
//     * Tests {@link StringFieldSearchCriteriaTranslator} with enum field search criteria using DB connection.
//     */
//    @Test
//    public void testQueryDBWithEnumFieldSearchCriteria()
//    {
//        final SampleSearchCriteria criterion = new SampleSearchCriteria();
//    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithCollectionFieldSearchCriteria()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withCodes().thatIn(Arrays.asList(CODE1, CODE3));
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertEquals(criterionSampleIds.size(), 2);
        assertTrue(criterionSampleIds.contains(ID1));
        assertFalse(criterionSampleIds.contains(ID2));
        assertTrue(criterionSampleIds.contains(ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutExperimentFieldSearchCriteria()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutExperiment();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(ID1));
        assertTrue(criterionSampleIds.contains(ID2));
        assertFalse(criterionSampleIds.contains(ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutProjectFieldSearchCriteria()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutProject();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertTrue(criterionSampleIds.contains(ID1));
        assertFalse(criterionSampleIds.contains(ID2));
        assertFalse(criterionSampleIds.contains(ID3));
    }

    /**
     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
     */
    @Test
    public void testQueryDBWithoutSpaceFieldSearchCriteria()
    {
        final SampleSearchCriteria criterion = new SampleSearchCriteria();
        criterion.withoutSpace();
        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
        assertFalse(criterionSampleIds.isEmpty());
        assertFalse(criterionSampleIds.contains(ID1));
        assertTrue(criterionSampleIds.contains(ID2));
        assertTrue(criterionSampleIds.contains(ID3));
    }

//    /**
//     * Tests {@link StringFieldSearchCriteriaTranslator} with collection field search criteria using DB connection.
//     */
//    @Test
//    public void testQueryDBWithoutContainerFieldSearchCriteria()
//    {
//        final SampleSearchCriteria criterion = new SampleSearchCriteria();
//        criterion.withoutContainer();
//        final Set<Long> criterionSampleIds = searchManager.searchForIDs(USER_ID, criterion);
//        assertFalse(criterionSampleIds.isEmpty());
//        assertTrue(criterionSampleIds.contains(ID1));
//        assertTrue(criterionSampleIds.contains(ID2));
//        assertFalse(criterionSampleIds.contains(ID3));
//    }

}