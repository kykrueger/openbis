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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntitySortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class OrderTranslator
{
    public static final String PROPERTY_CODE_ALIAS = "property_code";

    public static final String TYPE_CODE_ALIAS = "type_code";

    private static final String IDENTIFIER = "IDENTIFIER";

    private static final String UNIQUE_PREFIX = OrderTranslator.class.getName() + ":";

    /** Column name in select for sorting by identifier (which is a generated string). */
    private static final String IDENTIFIER_SORTING_COLUMN = "i";

    public static SelectQuery translateToOrderQuery(final TranslationVo vo)
    {
        if (vo.getSortOptions() == null)
        {
            throw new IllegalArgumentException("Null sort options provided.");
        }

        final String from = buildFrom(vo);
        final String where = buildWhere(vo);
        final String select = buildSelect(vo);
        final String orderBy = buildOrderBy(vo);

        return new SelectQuery(select  + NL + from + NL + where + NL + orderBy, vo.getArgs());
    }

    private static String buildOrderBy(final TranslationVo vo)
    {
        final StringBuilder orderByBuilder = vo.getSortOptions().getSortings().stream().collect(
                StringBuilder::new,
                (stringBuilder, sorting) ->
                {
                    stringBuilder.append(COMMA + SP);
                    appendSortingColumn(vo, stringBuilder, sorting, false);
                },
                StringBuilder::append
        );

        return ORDER_BY + orderByBuilder.substring(COMMA.length());
    }

    private static String buildSelect(final TranslationVo vo)
    {
        final StringBuilder sqlBuilder = new StringBuilder(SELECT + SP + DISTINCT + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN);

        vo.getSortOptions().getSortings().forEach((sorting) ->
        {
            sqlBuilder.append(COMMA).append(SP);
            appendSortingColumn(vo, sqlBuilder, sorting, true);
        });

        return sqlBuilder.toString();
    }

    private static String buildFrom(final TranslationVo vo)
    {
        final TableMapper tableMapper = vo.getTableMapper();
        final StringBuilder sqlBuilder = new StringBuilder(FROM + SP + tableMapper.getEntitiesTable() + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        final AtomicInteger indexCounter = new AtomicInteger(1);

        vo.getSortOptions().getSortings().forEach((sorting) ->
        {
            final String sortingCriterionFieldName = sorting.getField();
            final Map<Object, Map<String, JoinInformation>> aliases = vo.getAliases();
            final Map<String, JoinInformation> joinInformationMap;
            final Object aliasesMapKey;
            if (TranslatorUtils.isPropertySearchFieldName(sortingCriterionFieldName))
            {
                final String propertyName = sortingCriterionFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length()).toLowerCase();
                joinInformationMap = TranslatorUtils.getPropertyJoinInformationMap(tableMapper, () -> getOrderingAlias(indexCounter));
                aliasesMapKey = propertyName;
            } else if (isTypeSearchCriterion(sortingCriterionFieldName) || isSortingByMaterialPermId(vo, sortingCriterionFieldName))
            {
                joinInformationMap = TranslatorUtils.getTypeJoinInformationMap(tableMapper, () -> getOrderingAlias(indexCounter));
                aliasesMapKey = EntityWithPropertiesSortOptions.TYPE;
            } else if (isSortingByIdentifierCriterion(sortingCriterionFieldName))
            {
                joinInformationMap = TranslatorUtils.getIdentifierJoinInformationMap(tableMapper, () -> getOrderingAlias(indexCounter),
                        UNIQUE_PREFIX);
                aliasesMapKey = UNIQUE_PREFIX;
            } else
            {
                joinInformationMap = null;
                aliasesMapKey = null;
            }

            if (joinInformationMap != null)
            {
                joinInformationMap.values().forEach((joinInformation) -> TranslatorUtils.appendJoin(sqlBuilder, joinInformation));
                aliases.put(aliasesMapKey, joinInformationMap);
            }
        });
        return sqlBuilder.toString();
    }

    private static String buildWhere(final TranslationVo vo)
    {
        final StringBuilder sqlBuilder = new StringBuilder(WHERE + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN + SP + IN + SP +
                LP + SELECT + SP + UNNEST + LP + QU + RP + RP);
        final Map<Object, Map<String, JoinInformation>> aliases = vo.getAliases();
        final TableMapper tableMapper = vo.getTableMapper();
        final List<Object> args = vo.getArgs();

        args.add(vo.getIds().toArray(new Long[0]));

        vo.getSortOptions().getSortings().forEach((sorting) ->
        {
            final String sortingCriteriaFieldName = sorting.getField();
            if (TranslatorUtils.isPropertySearchFieldName(sortingCriteriaFieldName))
            {
                final String fullPropertyName = sortingCriteriaFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length());
                final String attributeTypesTableAlias = aliases.get(fullPropertyName.toLowerCase()).get(TableNames.DATA_TYPES_TABLE).
                        getMainTableAlias();
                sqlBuilder.append(SP).append(AND).append(SP).append(attributeTypesTableAlias).append(PERIOD).append(CODE_COLUMN).append(SP).
                        append(EQ).append(SP).append(QU);
                args.add(TranslatorUtils.normalisePropertyName(fullPropertyName));
            }
        });

        return sqlBuilder.toString();
    }

    /**
     * Appends sorting column to SQL builder. In some cases it can be more columns. Adds type casting when needed.
     *
     * @param vo order translation value object.
     * @param sqlBuilder string builder to which the column should be appended.
     * @param sorting sorting parameters.
     * @param inSelect {@code true} if this method is used in the {@code SELECT} clause.
     */
    private static void appendSortingColumn(final TranslationVo vo, final StringBuilder sqlBuilder, final Sorting sorting,
            final boolean inSelect)
    {
        final String sortingCriteriaFieldName = sorting.getField();
        final Map<String, JoinInformation> aliases = vo.getAliases().get(UNIQUE_PREFIX);
        if (TranslatorUtils.isPropertySearchFieldName(sortingCriteriaFieldName))
        {
            final String propertyName = sortingCriteriaFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length());
            final String propertyNameLowerCase = propertyName.toLowerCase();
            final String valuesTableAlias = vo.getAliases().get(propertyNameLowerCase).get(vo.getTableMapper().getEntityTypesAttributeTypesTable()).getMainTableAlias();
            sqlBuilder.append(valuesTableAlias).append(PERIOD).append(VALUE_COLUMN);

            final String casting = vo.getDataTypeByPropertyName().get(propertyName);
            if (casting != null)
            {
                sqlBuilder.append(DOUBLE_COLON).append(casting.toLowerCase());
            }
        } else if (isTypeSearchCriterion(sortingCriteriaFieldName))
        {
            final String typesTableAlias = vo.getAliases().get(EntityWithPropertiesSortOptions.TYPE).get(vo.getTableMapper().getEntityTypesTable()).
                    getSubTableAlias();
            sqlBuilder.append(typesTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else if (isSortingByIdentifierCriterion(sortingCriteriaFieldName))
        {
            if (inSelect)
            {
                final JoinInformation entitiesTableAlias = aliases.get(vo.getTableMapper().getEntitiesTable());
                final JoinInformation spacesTableAlias = aliases.get(UNIQUE_PREFIX + SPACES_TABLE);
                final JoinInformation projectsTableAlias = aliases.get(UNIQUE_PREFIX + PROJECTS_TABLE);

                buildFullIdentifierConcatenationString(sqlBuilder, (spacesTableAlias != null) ? spacesTableAlias.getSubTableAlias() : null,
                        (projectsTableAlias != null) ? projectsTableAlias.getSubTableAlias() : null,
                        (entitiesTableAlias != null) ? entitiesTableAlias.getSubTableAlias() : null);
                sqlBuilder.append(SP);
            }
            sqlBuilder.append(IDENTIFIER_SORTING_COLUMN);
        } else if (isSortingByMaterialPermId(vo, sortingCriteriaFieldName))
        {
            final String materialTypeTableAlias = vo.getAliases().get(EntityWithPropertiesSortOptions.TYPE)
                    .get(vo.getTableMapper().getEntityTypesTable()).getSubTableAlias();
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);

            if (!inSelect)
            {
                sqlBuilder.append(SP).append(sorting.getOrder());
            }

            sqlBuilder.append(COMMA).append(SP).append(materialTypeTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else if (isSortingBySpaceModificationDate(vo, sortingCriteriaFieldName))
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(REGISTRATION_TIMESTAMP_COLUMN.toLowerCase());
        } else
        {
            final String lowerCaseSortingCriteriaFieldName = sortingCriteriaFieldName.toLowerCase();
            final String fieldName = AttributesMapper.getColumnName(lowerCaseSortingCriteriaFieldName, vo.getTableMapper().getEntitiesTable(),
                    lowerCaseSortingCriteriaFieldName);
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName);
        }

        if (!inSelect)
        {
            sqlBuilder.append(SP).append(sorting.getOrder());
        }
    }

    private static boolean isSortingBySpaceModificationDate(final TranslationVo vo, final String sortingCriteriaFieldName)
    {
        return EntitySortOptions.MODIFICATION_DATE.equals(sortingCriteriaFieldName) && vo.getTableMapper() == TableMapper.SPACE;
    }

    private static boolean isSortingByMaterialPermId(final TranslationVo vo, final String sortingCriteriaFieldName)
    {
        return EntitySortOptions.PERM_ID.equals(sortingCriteriaFieldName) && vo.getTableMapper() == TableMapper.MATERIAL;
    }

    private static String getOrderingAlias(final AtomicInteger num)
    {
        return "o" + num.getAndIncrement();
    }

    private static boolean isSortingByIdentifierCriterion(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.equals(IDENTIFIER);
    }

    private static boolean isTypeSearchCriterion(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.equals(EntityWithPropertiesSortOptions.TYPE);
    }

    public static SelectQuery translateToSearchTypeQuery(final TranslationVo vo)
    {
        final TableMapper tableMapper = vo.getTableMapper();
        final String queryString = SELECT + SP + DISTINCT + SP + "o3" + PERIOD + CODE_COLUMN + SP + PROPERTY_CODE_ALIAS + COMMA + SP +
                "o4" + PERIOD + CODE_COLUMN + SP + TYPE_CODE_ALIAS + NL +
                FROM + SP + tableMapper.getEntitiesTable() + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS + NL +
                INNER_JOIN + SP + tableMapper.getValuesTable() + SP + "o1" + SP +
                ON + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN + SP + EQ + SP + "o1" + PERIOD + tableMapper.getValuesTableEntityIdField() + NL +
                INNER_JOIN + SP + tableMapper.getEntityTypesAttributeTypesTable() + SP + "o2" + SP +
                ON + SP + "o1" + PERIOD + tableMapper.getValuesTableEntityTypeAttributeTypeIdField() + SP + EQ + SP + "o2" + PERIOD + ID_COLUMN + NL +
                INNER_JOIN + SP + tableMapper.getAttributeTypesTable() + SP + "o3" + SP +
                ON + SP + "o2" + PERIOD + tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField() + SP + EQ + SP + "o3" + PERIOD +
                ID_COLUMN + NL +
                INNER_JOIN + SP + DATA_TYPES_TABLE + SP + "o4" + SP +
                ON + SP + "o3" + PERIOD + tableMapper.getAttributeTypesTableDataTypeIdField() + SP + EQ + SP + "o4" + PERIOD + ID_COLUMN + NL +
                WHERE + SP + "o4" + PERIOD + CODE_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP;

        return new SelectQuery(queryString, Collections.singletonList(vo.getTypesToFilter()));
    }

}
