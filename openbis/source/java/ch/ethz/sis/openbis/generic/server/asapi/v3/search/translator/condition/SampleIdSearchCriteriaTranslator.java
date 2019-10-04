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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.FullSampleIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERM_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROJECTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SPACES_TABLE;

public class SampleIdSearchCriteriaTranslator extends AbstractConditionTranslator<IdSearchCriteria<ISampleId>>
{

    @Override
    public void translate(final IdSearchCriteria<ISampleId> criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases)
    {
        final ISampleId sampleId = criterion.getId();

        if (sampleId.getClass() == SampleIdentifier.class) {
            final FullSampleIdentifier fullSampleIdentifier = new FullSampleIdentifier(((SampleIdentifier) sampleId).getIdentifier(),
                    null);
            final String sampleCode = fullSampleIdentifier.getSampleCode();
            final SampleIdentifierParts identifierParts = fullSampleIdentifier.getParts();
            final String spaceCode = identifierParts.getSpaceCodeOrNull();
            final String projectCode = identifierParts.getProjectCodeOrNull();
            final String containerCode = identifierParts.getContainerCodeOrNull();

            if (spaceCode != null || projectCode != null || containerCode != null)
            {
                sqlBuilder.append(LP);

                if (spaceCode != null)
                {
                    buildSelectByIdConditionWithSubquery(sqlBuilder, SPACE_COLUMN, SPACES_TABLE);
                    args.add(spaceCode);
                }

                if (projectCode != null)
                {
                    buildSelectByIdConditionWithSubquery(sqlBuilder, PROJECT_COLUMN, PROJECTS_TABLE);
                    args.add(projectCode);
                }

                if (containerCode != null)
                {
                    buildSelectByIdConditionWithSubquery(sqlBuilder, PART_OF_SAMPLE_COLUMN, SAMPLES_ALL_TABLE);
                    args.add(containerCode);
                }

                sqlBuilder.setLength(sqlBuilder.length() - AND.length() - SP.length() * 2);
                sqlBuilder.append(RP).append(SP).append(AND).append(SP);
            }

            sqlBuilder.append(Translator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(EQ).append(QU);
            args.add(sampleCode);
        } else if (sampleId.getClass() == SamplePermId.class)
        {
            sqlBuilder.append(Translator.MAIN_TABLE_ALIAS).append(PERIOD).append(PERM_ID_COLUMN).append(EQ).append(QU);
            args.add(((SamplePermId) sampleId).getPermId());
        } else
        {
            throw new IllegalArgumentException("The following ID class is not supported: " + sampleId.getClass().getSimpleName());
        }
    }

    private static void buildSelectByIdConditionWithSubquery(final StringBuilder sqlBuilder, final String columnName, final String subqueryTable)
    {
        sqlBuilder.append(columnName).append(EQ).append(LP).
                append(SELECT).append(SP).append(ID_COLUMN).append(SP).append(FROM).append(SP).append(subqueryTable).append(SP).
                append(WHERE).append(SP).append(CODE_COLUMN).append(EQ).append(QU).
                append(RP).append(SP).append(AND).append(SP);
    }

}
