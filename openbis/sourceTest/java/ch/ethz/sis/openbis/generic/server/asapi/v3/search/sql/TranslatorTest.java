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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SelectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;
import org.testng.annotations.Test;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static org.testng.Assert.assertEquals;

public class TranslatorTest
{

    private static final Long USER_ID = 1L;

    private static final String SAMPLE_ID = "A";

    private static final String REGISTRATION_DATE = "2019-04-11 14:21:16.392852+02";

    private static final String MODIFICATION_DATE = "2019-04-11 14:57:55.74435+02";

    private static final String MAIN_TABLE_ALIAS = "t0";

    protected static final Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?>> CRITERIA_TO_MANAGER_MAP = new HashMap<>();

    static {
        CRITERIA_TO_MANAGER_MAP.put(SampleSearchCriteria.class, null);
        CRITERIA_TO_MANAGER_MAP.put(SampleTypeSearchCriteria.class, null);
    }

    @Test
    public void testTranslateSearchAllSamples()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        final SelectQuery result =
                Translator.translate(USER_ID, EntityMapper.SAMPLE, Collections.singletonList(sampleSearchCriteria), SearchOperator.AND,
                        CRITERIA_TO_MANAGER_MAP);

        assertEquals(result, new SelectQuery(String.format(
                "SELECT DISTINCT %s.%s\n" +
                "FROM %s %s\n",
                MAIN_TABLE_ALIAS, ID_COLUMN, SAMPLES_ALL_TABLE, MAIN_TABLE_ALIAS), Collections.emptyList()));
    }

    @Test
    public void testTranslateSearchSamplesById()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria().withAndOperator();
        final SampleIdentifier sampleIdentifier = new SampleIdentifier(SAMPLE_ID);
        sampleSearchCriteria.withId().thatEquals(sampleIdentifier);

        final SelectQuery result =
                Translator.translate(USER_ID, EntityMapper.SAMPLE, Collections.singletonList(sampleSearchCriteria), SearchOperator.AND,
                        CRITERIA_TO_MANAGER_MAP);

        assertEquals(result, new SelectQuery(String.format(
                "SELECT DISTINCT %s.%s\n" +
                "FROM %s %s\n" +
                "WHERE %s=?",
                MAIN_TABLE_ALIAS, ID_COLUMN, SAMPLES_ALL_TABLE, MAIN_TABLE_ALIAS, ID_COLUMN),
                Collections.singletonList(sampleIdentifier)));
    }

    @Test
    public void testTranslateSearchSamplesByOtherFieldsAnd()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria().withAndOperator();
        sampleSearchCriteria.withCode().thatEquals(SAMPLE_ID);
        sampleSearchCriteria.withRegistrationDate().thatEquals(REGISTRATION_DATE);
        sampleSearchCriteria.withModificationDate().thatEquals(MODIFICATION_DATE);

        final SelectQuery result =
                Translator.translate(USER_ID, EntityMapper.SAMPLE, Collections.singletonList(sampleSearchCriteria), SearchOperator.AND,
                CRITERIA_TO_MANAGER_MAP);

        assertEquals(result, new SelectQuery(String.format(
                "SELECT DISTINCT %s.%s\n" +
                "FROM %s %s\n" +
                "WHERE %s=? AND %s=? AND %s=?\n",
                MAIN_TABLE_ALIAS, ID_COLUMN, SAMPLES_ALL_TABLE, MAIN_TABLE_ALIAS, ID_COLUMN, REGISTRATION_TIMESTAMP_COLUMN,
                MODIFICATION_TIMESTAMP_COLUMN),
                Arrays.asList(SAMPLE_ID, REGISTRATION_DATE, MODIFICATION_DATE)));
    }

    @Test
    public void testTranslateSearchSamplesByOtherFieldsOr()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria().withOrOperator();
        sampleSearchCriteria.withCode().thatEquals(SAMPLE_ID);
        sampleSearchCriteria.withRegistrationDate().thatEquals(REGISTRATION_DATE);
        sampleSearchCriteria.withModificationDate().thatEquals(MODIFICATION_DATE);

        final SelectQuery result =
                Translator.translate(USER_ID, EntityMapper.SAMPLE, Collections.singletonList(sampleSearchCriteria), SearchOperator.AND,
                        CRITERIA_TO_MANAGER_MAP);

        assertEquals(result, new SelectQuery(String.format(
                "SELECT DISTINCT %s.%s\n" +
                "FROM %s %s\n" +
                "WHERE %s=? OR %s=? OR %s=?\n",
                MAIN_TABLE_ALIAS, ID_COLUMN, SAMPLES_ALL_TABLE, ID_COLUMN, MAIN_TABLE_ALIAS, REGISTRATION_TIMESTAMP_COLUMN,
                MODIFICATION_TIMESTAMP_COLUMN),
                Arrays.asList(SAMPLE_ID, REGISTRATION_DATE, MODIFICATION_DATE)));
    }

}