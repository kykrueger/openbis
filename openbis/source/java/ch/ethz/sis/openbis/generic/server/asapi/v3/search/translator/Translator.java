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
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.CollectionFieldConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.DateFieldConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IdConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.SampleConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.StringFieldConditionTranslator;
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

    public static final String JOIN = "JOIN";

    public static final String LIKE = "LIKE";

    public static final String ON = "ON";

    public static final String IN = "IN";

    public static final String IS = "IS";

    public static final String NOT = "NOT";

    public static final String AND = "AND";

    public static final String NULL = "NULL";

    public static final String SP = " ";

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

        final List<String> aliases = new ArrayList<>();
        final List<Object> args = new ArrayList<>();
        final StringBuilder sqlBuilder = new StringBuilder();

        buildFrom(dbEntityKind, criteria, sqlBuilder);
        buildWhere(dbEntityKind, criteria, args, operator, sqlBuilder);
        buildSelect(dbEntityKind, aliases, sqlBuilder);

        return new SelectQuery(sqlBuilder.toString(), args);
    }

    private static void buildSelect(final EntityMapper dbEntityKind, final List<String> aliasesPresentInOrderBy,
            final StringBuilder sqlBuilder)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(SELECT).append(SP).append(DISTINCT).append(SP).append(dbEntityKind.getEntitiesTableIdField());
        aliasesPresentInOrderBy.forEach(alias -> builder.append(COMMA).append(SP).append(alias));
        builder.append(NEW_LINE);
        sqlBuilder.insert(0, builder);
    }

    private static void buildFrom(final EntityMapper entityMapper, final List<ISearchCriteria> criteria,
            final StringBuilder sqlBuilder)
    {
        final String entitiesTableName = entityMapper.getEntitiesTable();
        sqlBuilder.append(FROM).append(SP).append(entitiesTableName).append(NEW_LINE);
        criteria.forEach(criterion ->
                {
                    if (criterion instanceof AbstractEntitySearchCriteria<?>)
                    {
                        final AbstractEntitySearchCriteria<?> entitySearchCriterion = (AbstractEntitySearchCriteria<?>) criterion;
                        if (entitySearchCriterion.getCriteria().isEmpty())
                        {

                        }
                    }

                    if (criterion instanceof SampleSearchCriteria)
                    {
                        final SampleSearchCriteria sampleSearchCriterion = (SampleSearchCriteria) criterion;

                    }

//                    if(isAliasPresentInWhere) {
//
//                    }
//                    sqlBuilder.append(LEFT).append(SP).append(JOIN).append(SP).append().append(SP).append(entitiesTableName).append(SP).append(ON)
//                            .append(SP).append(entitiesTableName).append(PERIOD).append().append(EQ).append().append(PERIOD).append();
                });
    }


    private static void buildWhere(final EntityMapper entityMapper, final List<ISearchCriteria> criteria, final List<Object> args,
            final SearchOperator operator, final StringBuilder sqlBuilder)
    {
        if (isSearchAllCriteria(criteria))
        {
            return;
        }

        sqlBuilder.append(WHERE).append(SP);

        final AtomicBoolean first = new AtomicBoolean(true);
        final String logicalOperator = operator.toString();

        criteria.forEach((criterion) ->
        {
            if (first.get())
            {
                sqlBuilder.append(SP).append(logicalOperator).append(SP);
                first.set(false);
            }

            @SuppressWarnings("unchecked")
            final IConditionTranslator<ISearchCriteria> conditionTranslator =
                    (IConditionTranslator<ISearchCriteria>) CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion.getClass());
            if (conditionTranslator != null)
            {
                conditionTranslator.translate(criterion, args, sqlBuilder);
            } else
            {
                throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
            }
        });
//
//            getConditionTranslator(IdSearchCriteria.class).translate((IdSearchCriteria<?>) subcriterion, args, operator, sqlBuilder);
//
//            if (subcriterion instanceof AbstractFieldSearchCriteria<?>)
//            {
//                final AbstractFieldSearchCriteria<?> fieldSearchSubcriterion = (AbstractFieldSearchCriteria<?>) subcriterion;
//                final Object fieldName = fieldSearchSubcriterion.getFieldName();
//                final Object fieldValue = fieldSearchSubcriterion.getFieldValue();
//
//                if (subcriterion instanceof CollectionFieldSearchCriteria<?>)
//                {
//                    getConditionTranslator(CollectionFieldSearchCriteria.class).
//                            translate((CollectionFieldSearchCriteria<?>) subcriterion, args, operator, sqlBuilder);
//                } else
//                {
//                    if (fieldValue == null)
//                    {
//                        sqlBuilder.append(fieldName).append(SP).append(IS_NOT_NULL);
//                    } else
//                    {
//                        if (subcriterion instanceof StringFieldSearchCriteria)
//                        {
//                            getConditionTranslator(StringFieldSearchCriteria.class)
//                                    .translate((StringFieldSearchCriteria) subcriterion, args, operator, sqlBuilder);
//                        } else if (subcriterion instanceof DateFieldSearchCriteria)
//                        {
//                            getConditionTranslator(DateFieldSearchCriteria.class).
//                                    translate((DateFieldSearchCriteria) subcriterion, args, operator, sqlBuilder);
//                        } else if (subcriterion instanceof NumberPropertySearchCriteria)
//                        {
//                            throw new IllegalArgumentException();
//                        } else
//                        {
//                            sqlBuilder.append(fieldName).append(EQ).append(QU);
//                            args.add(fieldValue);
//                        }
//                    }
//                }
//            } else if (subcriterion instanceof AbstractObjectSearchCriteria<?>)
//            {
//                if (subcriterion instanceof ExperimentSearchCriteria)
//                {
//                    final IConditionTranslator<ExperimentSearchCriteria> conditionTranslator = getConditionTranslator(
//                            ExperimentSearchCriteria.class);
//                    conditionTranslator.translate((ExperimentSearchCriteria) subcriterion, args, operator, sqlBuilder);
//                } else if (subcriterion instanceof ProjectSearchCriteria)
//                {
//                    throw new IllegalArgumentException("Unsupported criterion type: " + subcriterion.getClass().getSimpleName());
//                } else if (subcriterion instanceof SpaceSearchCriteria)
//                {
//                    throw new IllegalArgumentException("Unsupported criterion type: " + subcriterion.getClass().getSimpleName());
//                } else if (subcriterion instanceof SampleTypeSearchCriteria)
//                {
//                    throw new IllegalArgumentException("Unsupported criterion type: " + subcriterion.getClass().getSimpleName());
//                } else if (subcriterion instanceof PersonSearchCriteria)
//                {
//                    throw new IllegalArgumentException("Unsupported criterion type: " + subcriterion.getClass().getSimpleName());
//                } else if (subcriterion instanceof TagSearchCriteria)
//                {
//                    throw new IllegalArgumentException("Unsupported criterion type: " + subcriterion.getClass().getSimpleName());
//                } else
//                {
//                    throw new IllegalArgumentException("Unsupported criterion type: " + subcriterion.getClass().getSimpleName());
//                }
//            } else if (subcriterion instanceof IdSearchCriteria<?>)
//            {
//                getConditionTranslator(IdSearchCriteria.class).translate((IdSearchCriteria<?>) subcriterion, args, operator, sqlBuilder);
//            } else if (subcriterion instanceof NoSampleSearchCriteria)
//            {
//                sqlBuilder.append(PART_OF_SAMPLE_COLUMN).append(SP).append(IS_NULL);
//            } else if (subcriterion instanceof NoExperimentSearchCriteria)
//            {
//                sqlBuilder.append(ColumnNames.EXPERIMENT_COLUMN).append(SP).append(IS_NULL);
//            } else if (subcriterion instanceof NoProjectSearchCriteria)
//            {
//                sqlBuilder.append(ColumnNames.PROJECT_COLUMN).append(SP).append(IS_NULL);
//            } else if (subcriterion instanceof NoSpaceSearchCriteria)
//            {
//                sqlBuilder.append(ColumnNames.SPACE_COLUMN).append(SP).append(IS_NULL);
//            } else
//            {
//                throw new IllegalArgumentException("Unsupported criterion type: " + subcriterion.getClass().getSimpleName());
//            }
//            sqlBuilder.append(SP).append(logicalOperator).append(SP);
//        }
//
//        criteria.forEach(criterion ->
//                {
//                    if (criterion.getClass() == SampleSearchCriteria.class)
//                    {
//
//                    }
//                });
//        sqlBuilder.setLength(sqlBuilder.length() - logicalOperator.length() - SP.length() * 2);
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
