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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static org.testng.Assert.assertEquals;

public class TranslatorTest
{

    private static final String SAMPLE_ID = "A";

    private static final String REGISTRATION_DATE = "2019-04-11 14:21:16.392852+02";

    private static final String MODIFICATION_DATE = "2019-04-11 14:57:55.74435+02";

    @Test
    public void testTranslateSearchAllSamples()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
        final Translator.TranslatorResult result =
                Translator.translate(EntityKind.SAMPLE, Collections.singletonList(sampleSearchCriteria), SearchOperator.AND);

        assertEquals(result, new Translator.TranslatorResult(String.format(
                "SELECT *\n" +
                "FROM %s\n",
                SAMPLES_ALL_TABLE), Collections.emptyList()));
    }

    @Test
    public void testTranslateSearchSamplesById()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria().withAndOperator();
        sampleSearchCriteria.withIdentifier().thatEquals(SAMPLE_ID);

        final Translator.TranslatorResult result =
                Translator.translate(EntityKind.SAMPLE, Collections.singletonList(sampleSearchCriteria), SearchOperator.AND);

        assertEquals(result, new Translator.TranslatorResult(String.format(
                "SELECT %s\n" +
                "FROM %s\n" +
                "WHERE %s=?\n",
                ID_COLUMN, SAMPLES_ALL_TABLE, ID_COLUMN),
                Collections.singletonList(SAMPLE_ID)));
    }

    @Test
    public void testTranslateSearchSamplesByOtherFieldsAnd()
    {
        final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria().withAndOperator();
        sampleSearchCriteria.withCode().thatEquals(SAMPLE_ID);
        sampleSearchCriteria.withRegistrationDate().thatEquals(REGISTRATION_DATE);
        sampleSearchCriteria.withModificationDate().thatEquals(MODIFICATION_DATE);

        final Translator.TranslatorResult result =
                Translator.translate(EntityKind.SAMPLE, Collections.singletonList(sampleSearchCriteria), SearchOperator.AND);

        assertEquals(result, new Translator.TranslatorResult(String.format(
                "SELECT %s\n" +
                "FROM %s\n" +
                "WHERE %s=? AND %s=? AND %s=?\n",
                ID_COLUMN, SAMPLES_ALL_TABLE, ID_COLUMN, REGISTRATION_TIMESTAMP_COLUMN, MODIFICATION_TIMESTAMP_COLUMN),
                Arrays.asList(SAMPLE_ID, REGISTRATION_DATE, MODIFICATION_DATE)));
    }

}