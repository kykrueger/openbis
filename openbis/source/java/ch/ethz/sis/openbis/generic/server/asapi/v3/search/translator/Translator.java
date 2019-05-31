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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CollectionFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.*;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Translator
{

    // TODO: consider moving out constants to a separate class.

    public static final String UNNEST = "unnest";

    public static final String SELECT = "SELECT";

    public static final String DISTINCT = "DISTINCT";

    public static final String FROM = "FROM";

    public static final String WHERE = "WHERE";

    public static final String LEFT = "LEFT";

    public static final String INNER = "INNER";

    public static final String JOIN = "JOIN";

    public static final String SP = " ";

    public static final String INNER_JOIN = INNER + SP + JOIN;

    public static final String LIKE = "LIKE";

    public static final String ON = "ON";

    public static final String IN = "IN";

    public static final String IS = "IS";

    public static final String AS = "AS";

    public static final String NOT = "NOT";

    public static final String AND = "AND";

    public static final String NULL = "NULL";

    public static final String NL = "\n";

    public static final String IS_NULL = IS + SP + NULL;

    public static final String IS_NOT_NULL = IS + SP + NOT + SP + NULL;

    public static final String COMMA = ",";

    public static final String PERIOD = ".";

    public static final String EQ = "=";

    public static final String GT = ">";

    public static final String LT = "<";

    public static final String GE = ">=";

    public static final String LE = "<=";

    public static final String NEW_LINE = "\n";

    public static final String ASTERISK = "*";

    public static final String QU = "?";

    public static final String PERCENT = "%";

    public static final String BARS = "||";

    public static final String LP = "(";

    public static final String RP = ")";

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            SimplePropertyValidator.SupportedDatePattern.CANONICAL_DATE_PATTERN.getPattern());

    public static final Map<Class<? extends ISearchCriteria>, IConditionTranslator<? extends ISearchCriteria>> CRITERIA_TO_CONDITION_TRANSLATOR_MAP =
            new HashMap<>();

    static
    {
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(SampleSearchCriteria.class, new SampleConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StringFieldSearchCriteria.class, new StringFieldConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(DateFieldSearchCriteria.class, new DateFieldConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CollectionFieldSearchCriteria.class, new CollectionFieldConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdSearchCriteria.class, new IdConditionTranslator());
    }

    private static final AtomicBoolean FIRST = new AtomicBoolean();

    @SuppressWarnings("unchecked")
    private static <T extends ISearchCriteria> IConditionTranslator<T> getConditionTranslator(final Class<T> criterionClass)
    {
        return (IConditionTranslator<T>) CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterionClass);
    }

    public static SelectQuery translate(final EntityKind entityKind, final List<ISearchCriteria> criteria,
            final SearchOperator operator)
    {
        if (criteria == null && criteria.isEmpty())
        {
            throw new IllegalArgumentException("Empty or null criteria provided.");
        }

        final EntityMapper dbEntityKind = EntityMapper.toEntityMapper(entityKind);

        final Map<Object, JoinInformation> aliases = new HashMap<>();
        final List<Object> args = new ArrayList<>();

        String where = buildWhere(dbEntityKind, criteria, args, operator);
        String from = buildFrom(dbEntityKind, criteria, aliases);
        String select = buildSelect(dbEntityKind);

        return new SelectQuery(select + from + where, args);
    }

    private static String buildSelect(final EntityMapper dbEntityKind)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(SELECT).append(SP).append(DISTINCT).append(SP).append(dbEntityKind.getEntitiesTableIdField());
        builder.append(NEW_LINE);
        return builder.toString();

    }

    private static String getAlias(int num) {
        return "t" + num;
    }

    private static String buildFrom(final EntityMapper entityMapper, final List<ISearchCriteria> criteria, Map<Object, JoinInformation> aliases)
    {
        final StringBuilder sqlBuilder = new StringBuilder();

        final String entitiesTableName = entityMapper.getEntitiesTable();
        JoinInformation mainTable = new JoinInformation();
        mainTable.setMainTable(entitiesTableName);
        mainTable.setMainTableId(entityMapper.getEntitiesTableIdField());
        mainTable.setMainTableAlias(getAlias(aliases.size()));
        aliases.put(entitiesTableName, mainTable);

        sqlBuilder.append(FROM).append(SP).append(entitiesTableName).append(SP).append(AS).append(SP).append(mainTable.getMainTableAlias()).append(NEW_LINE);

        for (ISearchCriteria criterion:criteria) {
            IConditionTranslator conditionTranslator = CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion);
            JoinInformation joinInformation = conditionTranslator.getJoinInformation(criterion, entityMapper);
            if (joinInformation.getSubTable() != null) { // Join required
                joinInformation.setSubTableAlias(getAlias(aliases.size()));
                sqlBuilder.append(INNER_JOIN).append(SP).append(joinInformation.getSubTable()).append(SP).append(AS).append(joinInformation.getSubTableAlias()).append(SP)
                        .append(ON).append(SP).append(mainTable.getMainTableAlias()).append(PERIOD).append(joinInformation.getMainTableId()).append(SP)
                        .append(EQ).append(SP).append(joinInformation.getSubTableAlias()).append(PERIOD).append(joinInformation.getSubTableId()).append(RP).append(NEW_LINE);
            }
            aliases.put(criterion, joinInformation);
        }
        return sqlBuilder.toString();
    }


    private static String buildWhere(final EntityMapper entityMapper, final List<ISearchCriteria> criteria, final List<Object> args, final SearchOperator operator)
    {
        final StringBuilder sqlBuilder = new StringBuilder();
        if (isSearchAllCriteria(criteria)) {
            return "";
        }

        sqlBuilder.append(WHERE).append(SP);

        FIRST.set(true);
        final String logicalOperator = operator.toString();

        criteria.forEach((criterion) ->
        {
            if (FIRST.get())
            {
                sqlBuilder.append(SP).append(logicalOperator).append(SP);
                FIRST.set(false);
            }

            @SuppressWarnings("unchecked")
            final IConditionTranslator<ISearchCriteria> conditionTranslator =
                    (IConditionTranslator<ISearchCriteria>) CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion.getClass());
            if (conditionTranslator != null)
            {
                conditionTranslator.translate(criterion, entityMapper, args, sqlBuilder);
            } else
            {
                throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
            }
        });

        return sqlBuilder.toString();
    }

    /**
     * Checks whether the criteria is for searching all values.
     *
     * @param criteria the criteria to be checked.
     * @return {@code true} if the criteria contain only one entity search value which is empty.
     */
    private static boolean isSearchAllCriteria(final List<ISearchCriteria> criteria)
    {
        if (criteria.size() == 1)
        {
            final ISearchCriteria criterion = criteria.get(0);
            if (criterion instanceof AbstractEntitySearchCriteria<?> &&
                    ((AbstractEntitySearchCriteria<?>) criterion).getCriteria().isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    public static class TranslatorAlias
    {

        private String table;

        private String tableAlias; // table + "_" + <alias_idx>

        private ISearchCriteria reasonForAlias;

    }

}
