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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdentifierSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.FullEntityIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROJECTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SPACES_TABLE;

public class IdentifierSearchConditionTranslator implements IConditionTranslator<IdentifierSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final IdentifierSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final IdentifierSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases, final Map<String, String> dataTypeByPropertyName)
    {
        final AbstractStringValue fieldValue = criterion.getFieldValue();
        final Class<? extends AbstractStringValue> valueClass = fieldValue.getClass();

        final FullEntityIdentifier fullObjectIdentifier = new FullEntityIdentifier(fieldValue.getValue(), null);
        final String entityCode = fullObjectIdentifier.getEntityCode();
        final SampleIdentifierParts identifierParts = fullObjectIdentifier.getParts();

        final String spaceCode = identifierParts.getSpaceCodeOrNull();
        final String projectCode = identifierParts.getProjectCodeOrNull();
        final String containerCode = identifierParts.getContainerCodeOrNull();

        if (spaceCode != null || projectCode != null || containerCode != null)
        {
            sqlBuilder.append(LP);

            if (spaceCode != null)
            {
                buildSelectByIdConditionWithSubquery(sqlBuilder, SPACE_COLUMN, SPACES_TABLE, CODE_COLUMN, valueClass, spaceCode, args);
                sqlBuilder.append(SP).append(AND).append(SP);
            }

            if (projectCode != null)
            {
                buildSelectByIdConditionWithSubquery(sqlBuilder, PROJECT_COLUMN, PROJECTS_TABLE, CODE_COLUMN, valueClass, projectCode,
                        args);
                sqlBuilder.append(SP).append(AND).append(SP);
            }

            if (containerCode != null)
            {
                buildSelectByIdConditionWithSubquery(sqlBuilder, PART_OF_SAMPLE_COLUMN, SAMPLES_ALL_TABLE, CODE_COLUMN, valueClass,
                        containerCode, args);
                sqlBuilder.append(SP).append(AND).append(SP);
            }

            sqlBuilder.setLength(sqlBuilder.length() - AND.length() - SP.length() * 2);
            sqlBuilder.append(RP).append(SP).append(AND).append(SP);
        }

        sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP);
        TranslatorUtils.appendStringComparatorOp(valueClass, entityCode, sqlBuilder, args);
    }

    /**
     * Builds the following query:<p/>
     * <code>
     *     t0.[columnName] = (SELECT id FROM [subqueryTable] WHERE [subqueryTableColumn] = ?)
     * </code>
     * @param sqlBuilder SQL builder to add the query part to.
     * @param columnName name of the column in the main table to be equal to the result in the subquery.
     * @param subqueryTable table which should be queried for code.
     * @param subqueryTableColumn name of the column in the subtable to search by.
     */
    private static void buildSelectByIdConditionWithSubquery(final StringBuilder sqlBuilder, final String columnName, final String subqueryTable,
            final String subqueryTableColumn, final Class<?> valueClass, final String finalValue, final List<Object> args)
    {
        sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP).append(IN).append(SP).append(LP).
                append(SELECT).append(SP).append(ID_COLUMN).append(SP).append(FROM).append(SP).append(subqueryTable).append(SP).
                append(WHERE).append(SP).append(subqueryTableColumn).append(SP);
        TranslatorUtils.appendStringComparatorOp(valueClass, finalValue, sqlBuilder, args);
        sqlBuilder.append(RP);
    }

}
