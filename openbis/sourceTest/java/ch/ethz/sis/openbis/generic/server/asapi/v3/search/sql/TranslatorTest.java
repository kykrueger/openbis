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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SelectQuery;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.TranslationVo;
import org.testng.annotations.Test;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static org.testng.Assert.assertEquals;

public class TranslatorTest
{

    private static final Long USER_ID = 1L;

    private static final String EXPECTED_SAMPLE_ID = "A";

    private static final String SAMPLE_ID = "/" + EXPECTED_SAMPLE_ID;

    private static final Date REGISTRATION_DATE = new Date(2019, Calendar.APRIL, 11, 14, 21, 16);

    private static final Date MODIFICATION_DATE = new Date(2019, Calendar.APRIL, 11, 14, 57, 55);

    private static final String MAIN_TABLE_ALIAS = "t0";

    protected static final Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?, ?>> CRITERIA_TO_MANAGER_MAP = new HashMap<>();

    static {
        CRITERIA_TO_MANAGER_MAP.put(SampleSearchCriteria.class, null);
        CRITERIA_TO_MANAGER_MAP.put(SampleTypeSearchCriteria.class, null);
    }

    @Test
    public void testTranslateSearchAllSamples()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        final TranslationVo criteriaTranslationVo = new TranslationVo();
        criteriaTranslationVo.setUserId(USER_ID);
        criteriaTranslationVo.setTableMapper(TableMapper.SAMPLE);
        criteriaTranslationVo.setCriteria(Collections.singletonList(sampleSearchCriteria));
        criteriaTranslationVo.setOperator(SearchOperator.AND);
        criteriaTranslationVo.setCriteriaToManagerMap(CRITERIA_TO_MANAGER_MAP);
        final SelectQuery result = CriteriaTranslator.translate(criteriaTranslationVo);

        assertEquals(result, new SelectQuery(String.format(
                "SELECT DISTINCT %s.%s\n" +
                "FROM %s %s\n",
                MAIN_TABLE_ALIAS, ID_COLUMN, SAMPLES_ALL_TABLE, MAIN_TABLE_ALIAS), Collections.emptyList()));
    }

    @Test
    public void testTranslateSearchSamplesById()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria().withAndOperator();
        sampleSearchCriteria.withId().thatEquals(new SampleIdentifier(SAMPLE_ID));

        final TranslationVo criteriaTranslationVo = new TranslationVo();
        criteriaTranslationVo.setUserId(USER_ID);
        criteriaTranslationVo.setTableMapper(TableMapper.SAMPLE);
        criteriaTranslationVo.setCriteria(sampleSearchCriteria.getCriteria());
        criteriaTranslationVo.setOperator(SearchOperator.AND);
        criteriaTranslationVo.setCriteriaToManagerMap(CRITERIA_TO_MANAGER_MAP);

        final SelectQuery result = CriteriaTranslator.translate(criteriaTranslationVo);

        assertEquals(result, new SelectQuery(String.format(
                "SELECT DISTINCT %s.%s\n" +
                "FROM %s %s\n" +
                "WHERE %s.%s=?",
                MAIN_TABLE_ALIAS, ID_COLUMN, SAMPLES_ALL_TABLE, MAIN_TABLE_ALIAS, MAIN_TABLE_ALIAS, CODE_COLUMN),
                Collections.singletonList(EXPECTED_SAMPLE_ID)));
    }

    @Test
    public void testTranslateSearchSamplesByOtherFieldsAnd()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria().withAndOperator();
        sampleSearchCriteria.withCode().thatEquals(EXPECTED_SAMPLE_ID);
        sampleSearchCriteria.withRegistrationDate().thatEquals(REGISTRATION_DATE);
        sampleSearchCriteria.withModificationDate().thatEquals(MODIFICATION_DATE);

        final TranslationVo criteriaTranslationVo = new TranslationVo();
        criteriaTranslationVo.setUserId(USER_ID);
        criteriaTranslationVo.setTableMapper(TableMapper.SAMPLE);
        criteriaTranslationVo.setCriteria(sampleSearchCriteria.getCriteria());
        criteriaTranslationVo.setOperator(SearchOperator.AND);
        criteriaTranslationVo.setCriteriaToManagerMap(CRITERIA_TO_MANAGER_MAP);

        final SelectQuery result = CriteriaTranslator.translate(criteriaTranslationVo);

        assertEquals(result, new SelectQuery(String.format(
                "SELECT DISTINCT %s.%s\n" +
                "FROM %s %s\n" +
                "WHERE %s.%s = ? AND %s.%s = ? AND %s.%s = ?",
                MAIN_TABLE_ALIAS, ID_COLUMN, SAMPLES_ALL_TABLE, MAIN_TABLE_ALIAS, MAIN_TABLE_ALIAS, CODE_COLUMN, MAIN_TABLE_ALIAS,
                REGISTRATION_TIMESTAMP_COLUMN, MAIN_TABLE_ALIAS, MODIFICATION_TIMESTAMP_COLUMN),
                Arrays.asList(EXPECTED_SAMPLE_ID, REGISTRATION_DATE, MODIFICATION_DATE)));
    }

    @Test
    public void testTranslateSearchSamplesByOtherFieldsOr()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria().withOrOperator();
        sampleSearchCriteria.withCode().thatEquals(EXPECTED_SAMPLE_ID);
        sampleSearchCriteria.withRegistrationDate().thatEquals(REGISTRATION_DATE);
        sampleSearchCriteria.withModificationDate().thatEquals(MODIFICATION_DATE);

        final TranslationVo criteriaTranslationVo = new TranslationVo();
        criteriaTranslationVo.setUserId(USER_ID);
        criteriaTranslationVo.setTableMapper(TableMapper.SAMPLE);
        criteriaTranslationVo.setCriteria(sampleSearchCriteria.getCriteria());
        criteriaTranslationVo.setOperator(SearchOperator.OR);
        criteriaTranslationVo.setCriteriaToManagerMap(CRITERIA_TO_MANAGER_MAP);

        final SelectQuery result = CriteriaTranslator.translate(criteriaTranslationVo);

        assertEquals(result, new SelectQuery(String.format(
                "SELECT DISTINCT %s.%s\n" +
                "FROM %s %s\n" +
                "WHERE %s.%s = ? OR %s.%s = ? OR %s.%s = ?",
                MAIN_TABLE_ALIAS, ID_COLUMN, SAMPLES_ALL_TABLE, MAIN_TABLE_ALIAS, MAIN_TABLE_ALIAS, CODE_COLUMN,
                MAIN_TABLE_ALIAS, REGISTRATION_TIMESTAMP_COLUMN, MAIN_TABLE_ALIAS, MODIFICATION_TIMESTAMP_COLUMN),
                Arrays.asList(EXPECTED_SAMPLE_ID, REGISTRATION_DATE, MODIFICATION_DATE)));
    }

}