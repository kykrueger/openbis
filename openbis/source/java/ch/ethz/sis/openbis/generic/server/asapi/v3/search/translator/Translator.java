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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodesSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CollectionFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DatePropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.NoExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.EmailSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.FirstNameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.LastNameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.ModifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.RegistratorSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.UserIdsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.NoProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.ListableSampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AbsenceConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyPropertySearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.CollectionFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.DateFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.EmailSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.FirstNameSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IdSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.LastNameSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.ListableSampleTypeSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.NumberFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.SampleSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.StringFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.UserIdSearchCriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DISTINCT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.INNER_JOIN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.TRUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.UNNEST;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

public class Translator
{

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(BasicConstant.DATE_WITHOUT_TIMEZONE_PATTERN);

    private static final Map<Class<? extends ISearchCriteria>, IConditionTranslator<? extends ISearchCriteria>> CRITERIA_TO_CONDITION_TRANSLATOR_MAP =
            new HashMap<>();

    /** This map is used when a subquery manager is used. It maps criteria to column name. */
    private static final Map<Class<? extends ISearchCriteria>, String> CRITERIA_TO_SUBQUERY_COLUMN_MAP = new HashMap<>();

    public static final String MAIN_TABLE_ALIAS = getAlias(new AtomicInteger(0));

    static
    {
        final StringFieldSearchCriteriaTranslator stringFieldSearchCriteriaTranslator = new StringFieldSearchCriteriaTranslator();
        final DateFieldSearchCriteriaTranslator dateFieldSearchCriteriaTranslator = new DateFieldSearchCriteriaTranslator();
        final NumberFieldSearchCriteriaTranslator numberFieldSearchCriteriaTranslator = new NumberFieldSearchCriteriaTranslator();
        final CollectionFieldSearchCriteriaTranslator collectionFieldSearchCriteriaTranslator = new CollectionFieldSearchCriteriaTranslator();
        final AbsenceConditionTranslator absenceConditionTranslator = new AbsenceConditionTranslator();
        final IdSearchCriteriaTranslator idSearchCriteriaTranslator = new IdSearchCriteriaTranslator();

        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdSearchCriteria.class, idSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(SampleSearchCriteria.class, new SampleSearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StringFieldSearchCriteria.class, stringFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CodeSearchCriteria.class, stringFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(PermIdSearchCriteria.class, stringFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NumberFieldSearchCriteria.class, numberFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(DateFieldSearchCriteria.class, dateFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(RegistrationDateSearchCriteria.class, dateFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ModificationDateSearchCriteria.class, dateFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CollectionFieldSearchCriteria.class, collectionFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CodesSearchCriteria.class, collectionFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdsSearchCriteria.class, collectionFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(UserIdsSearchCriteria.class, collectionFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSampleSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSampleContainerSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoExperimentSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoProjectSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSpaceSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyFieldSearchCriteria.class, new AnyFieldSearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StringPropertySearchCriteria.class, stringFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NumberPropertySearchCriteria.class, numberFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(DatePropertySearchCriteria.class, dateFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyPropertySearchCriteria.class, new AnyPropertySearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ListableSampleTypeSearchCriteria.class, new ListableSampleTypeSearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(UserIdSearchCriteria.class, new UserIdSearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(FirstNameSearchCriteria.class, new FirstNameSearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(LastNameSearchCriteria.class, new LastNameSearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EmailSearchCriteria.class, new EmailSearchCriteriaTranslator());

        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(RegistratorSearchCriteria.class, ColumnNames.PERSON_REGISTERER_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(ModifierSearchCriteria.class, ColumnNames.PERSON_MODIFIER_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(SampleSearchCriteria.class, ColumnNames.SAMPLE_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(SampleTypeSearchCriteria.class, ColumnNames.SAMPLE_TYPE_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(ExperimentTypeSearchCriteria.class, ColumnNames.EXPERIMENT_TYPE_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(ExperimentSearchCriteria.class, ColumnNames.EXPERIMENT_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(ProjectSearchCriteria.class, ColumnNames.PROJECT_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(SpaceSearchCriteria.class, ColumnNames.SPACE_COLUMN);
    }

    private static final AtomicBoolean FIRST = new AtomicBoolean();

    public static SelectQuery translate(final TranslationVo vo)
    {
        if (vo.getCriteria() == null && vo.getCriteria().isEmpty())
        {
            throw new IllegalArgumentException("Empty or null criteria provided.");
        }

        final String from = buildFrom(vo);
        final String where = buildWhere(vo);
        final String select = buildSelect();

        return new SelectQuery(select  + NL + from + NL + where, vo.getArgs());
    }

    private static String buildSelect()
    {
        return SELECT + SP + DISTINCT + SP + MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN;
    }

    public static String getAlias(final AtomicInteger num) {
        return "t" + num.getAndIncrement();
    }

    private static String buildFrom(final TranslationVo vo)
    {
        final StringBuilder sqlBuilder = new StringBuilder();

        final String entitiesTableName = vo.getTableMapper().getEntitiesTable();
        sqlBuilder.append(FROM).append(SP).append(entitiesTableName).append(SP).append(MAIN_TABLE_ALIAS);

        final AtomicInteger indexCounter = new AtomicInteger(1);
        vo.getCriteria().forEach(criterion ->
        {
            if (!vo.getCriteriaToManagerMap().containsKey(criterion.getClass()))
            {
                // Search for a translator only when you don't have a manager for the criterion.
                final IConditionTranslator conditionTranslator = CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion.getClass());
                if (conditionTranslator != null)
                {
                    final Map<String, JoinInformation> joinInformationMap = conditionTranslator.getJoinInformationMap(criterion,
                            vo.getTableMapper(), () -> getAlias(indexCounter));

                    if (joinInformationMap != null)
                    {
                        joinInformationMap.values().forEach(joinInformation ->
                        {
                            if (joinInformation.getSubTable() != null)
                            { // Join required
                                sqlBuilder.append(NL).append(INNER_JOIN).append(SP).append(joinInformation.getSubTable()).append(SP)
                                        .append(joinInformation.getSubTableAlias()).append(SP)
                                        .append(ON).append(SP).append(joinInformation.getMainTableAlias())
                                        .append(PERIOD).append(joinInformation.getMainTableIdField())
                                        .append(SP)
                                        .append(EQ).append(SP).append(joinInformation.getSubTableAlias()).append(PERIOD)
                                        .append(joinInformation.getSubTableIdField());
                            }
                        });
                        vo.getAliases().put(criterion, joinInformationMap);
                    }
                } else
                {
                    throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
                }
            }
        });
        return sqlBuilder.toString();
    }

    private static String buildWhere(final TranslationVo vo)
    {
        final StringBuilder sqlBuilder = new StringBuilder();
        if (isSearchAllCriteria(vo.getCriteria()))
        {
            return "";
        }

        sqlBuilder.append(WHERE).append(SP);

        if (vo.getCriteria().isEmpty()) {
            sqlBuilder.append(TRUE);
        } else
        {
            final String logicalOperator = vo.getOperator().toString();

            FIRST.set(true);
            vo.getCriteria().forEach((criterion) ->
            {
                if (FIRST.get())
                {
                    FIRST.set(false);
                } else
                {
                    sqlBuilder.append(SP).append(logicalOperator).append(SP);
                }

                final ISearchManager<ISearchCriteria, ?> subqueryManager = vo.getCriteriaToManagerMap().get(criterion.getClass());
                final TableMapper tableMapper = vo.getTableMapper();
                if (subqueryManager != null)
                {
                    final String column = CRITERIA_TO_SUBQUERY_COLUMN_MAP.get(criterion.getClass());
                    if (tableMapper != null && column != null)
                    {
                        final Set<Long> ids = subqueryManager.searchForIDs(vo.getUserId(), criterion);
                        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(column)
                                .append(SP).append(IN).append(SP).append(LP).append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP)
                                .append(RP);
                        vo.getArgs().add(ids.toArray(new Long[0]));
                    } else
                    {
                        throw new NullPointerException("tableMapper = " + tableMapper + " column = " + column);
                    }
                } else
                {
                    @SuppressWarnings("unchecked") final IConditionTranslator<ISearchCriteria> conditionTranslator =
                            (IConditionTranslator<ISearchCriteria>) CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion.getClass());
                    if (conditionTranslator != null)
                    {
                        conditionTranslator.translate(criterion, tableMapper, vo.getArgs(), sqlBuilder, vo.getAliases());
                    } else
                    {
                        throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
                    }
                }
            });
        }

        return sqlBuilder.toString();
    }

    /**
     * Checks whether the criteria is for searching all values.
     *
     * @param criteria the criteria to be checked.
     * @return {@code true} if the criteria contain only one entity search value which is empty.
     */
    private static boolean isSearchAllCriteria(final Collection<ISearchCriteria> criteria)
    {
        if (criteria.size() == 1)
        {
            final ISearchCriteria criterion = criteria.iterator().next();
            if (criterion instanceof AbstractEntitySearchCriteria<?> &&
                    ((AbstractEntitySearchCriteria<?>) criterion).getCriteria().isEmpty())
            {
                return true;
            }
        }
        return false;
    }

    public static class TranslationVo
    {
        private Long userId;

        private TableMapper tableMapper;

        private Collection<ISearchCriteria> criteria;

        private SearchOperator operator;

        private Map<Object, Map<String, JoinInformation>> aliases = new HashMap<>();

        private Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?>> criteriaToManagerMap;

        private List<Object> args = new ArrayList<>();

        public Long getUserId()
        {
            return userId;
        }

        public void setUserId(final Long userId)
        {
            this.userId = userId;
        }

        public TableMapper getTableMapper()
        {
            return tableMapper;
        }

        public void setTableMapper(final TableMapper tableMapper)
        {
            this.tableMapper = tableMapper;
        }

        public Collection<ISearchCriteria> getCriteria()
        {
            return criteria;
        }

        public void setCriteria(final Collection<ISearchCriteria> criteria)
        {
            this.criteria = criteria;
        }

        public SearchOperator getOperator()
        {
            return operator;
        }

        public void setOperator(final SearchOperator operator)
        {
            this.operator = operator;
        }

        public Map<Object, Map<String, JoinInformation>> getAliases()
        {
            return aliases;
        }

        public void setAliases(
                final Map<Object, Map<String, JoinInformation>> aliases)
        {
            this.aliases = aliases;
        }

        public Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?>> getCriteriaToManagerMap()
        {
            return criteriaToManagerMap;
        }

        public void setCriteriaToManagerMap(
                final Map<Class<? extends ISearchCriteria>, ISearchManager<ISearchCriteria, ?>> criteriaToManagerMap)
        {
            this.criteriaToManagerMap = criteriaToManagerMap;
        }

        public List<Object> getArgs()
        {
            return args;
        }

        public void setArgs(final List<Object> args)
        {
            this.args = args;
        }
    }

}
