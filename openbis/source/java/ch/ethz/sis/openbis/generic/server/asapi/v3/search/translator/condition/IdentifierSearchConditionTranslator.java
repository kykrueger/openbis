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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdentifierSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.FullEntityIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.BARS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.COALESCE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.COMMA;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LIKE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;
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
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        final String entitiesTable = tableMapper.getEntitiesTable();

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setJoinType(JoinType.LEFT);
        joinInformation1.setMainTable(entitiesTable);
        joinInformation1.setMainTableAlias(MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(SPACE_COLUMN);
        joinInformation1.setSubTable(SPACES_TABLE);
        joinInformation1.setSubTableAlias(aliasFactory.createAlias());
        joinInformation1.setSubTableIdField(ID_COLUMN);
        result.put(SPACES_TABLE, joinInformation1);

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setJoinType(JoinType.LEFT);
        joinInformation2.setMainTable(entitiesTable);
        joinInformation2.setMainTableAlias(MAIN_TABLE_ALIAS);
        joinInformation2.setMainTableIdField(PROJECT_COLUMN);
        joinInformation2.setSubTable(PROJECTS_TABLE);
        joinInformation2.setSubTableAlias(aliasFactory.createAlias());
        joinInformation2.setSubTableIdField(ID_COLUMN);
        result.put(PROJECTS_TABLE, joinInformation2);

        if (entitiesTable.equals(SAMPLES_ALL_TABLE))
        {
            // Only samples can have containers.
            final JoinInformation joinInformation3 = new JoinInformation();
            joinInformation3.setJoinType(JoinType.LEFT);
            joinInformation3.setMainTable(entitiesTable);
            joinInformation3.setMainTableAlias(MAIN_TABLE_ALIAS);
            joinInformation3.setMainTableIdField(PART_OF_SAMPLE_COLUMN);
            joinInformation3.setSubTable(entitiesTable);
            joinInformation3.setSubTableAlias(aliasFactory.createAlias());
            joinInformation3.setSubTableIdField(ID_COLUMN);
            result.put(entitiesTable, joinInformation3);
        }

        return result;
    }

    @Override
    public void translate(final IdentifierSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final Map<String, String> dataTypeByPropertyName)
    {
        final AbstractStringValue fieldValue = criterion.getFieldValue();
        final String entitiesTable = tableMapper.getEntitiesTable();
        final String slash = "/";
        final String colon = ":";

        sqlBuilder.append(SQ).append(slash).append(SQ).append(SP).append(BARS);

        appendCoalesce(sqlBuilder, aliases.get(SPACES_TABLE).getSubTableAlias(), slash);
        appendCoalesce(sqlBuilder, aliases.get(PROJECTS_TABLE).getSubTableAlias(), slash);
        if (entitiesTable.equals(SAMPLES_ALL_TABLE))
        {
            appendCoalesce(sqlBuilder, aliases.get(entitiesTable).getSubTableAlias(), colon);
        }

        sqlBuilder.append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP);
        TranslatorUtils.appendStringComparatorOp(fieldValue.getClass(), fieldValue.getValue(), sqlBuilder, args);
    }

    /**
     * Appends the following text to sqlBuilder.
     *
     * <pre>
     *     coalesce([alias].code || '[separator]', '') ||
     * </pre>
     *
     * @param sqlBuilder query builder.
     * @param alias alias of the table.
     * @param separator string to be appender at the end in the first parameter.
     */
    private static void appendCoalesce(final StringBuilder sqlBuilder, final String alias, final String separator)
    {
        sqlBuilder.append(SP).append(COALESCE).append(LP).append(alias).append(PERIOD).append(CODE_COLUMN).append(SP)
                .append(BARS)
                .append(SP).append(SQ).append(separator).append(SQ).append(COMMA).append(SP).append(SQ).append(SQ).append(RP).append(SP)
                .append(BARS);
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
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP).append(IN).append(SP).append(LP).
                append(SELECT).append(SP).append(ID_COLUMN).append(SP).append(FROM).append(SP).append(subqueryTable).append(SP).
                append(WHERE).append(SP).append(subqueryTableColumn).append(SP);
        TranslatorUtils.appendStringComparatorOp(valueClass, finalValue, sqlBuilder, args);
        sqlBuilder.append(RP);
    }

}
