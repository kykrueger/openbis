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
import java.util.Collection;
import java.util.HashMap;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AbsenceConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyFieldSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyPropertySearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.CodeSearchCriteriaTranslator;
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
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.TranslatorUtils;
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
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.METAPROJECT_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.METAPROJECTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.METAPROJECT_ASSIGNMENTS_ALL_TABLE;

public class CriteriaTranslator
{

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(BasicConstant.DATE_WITHOUT_TIMEZONE_PATTERN);

    /** This map is used when subqeury is not needed. Either no tables should be joined or they are joined in the FROM clause. */
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
        final CodeSearchCriteriaTranslator codeSearchCriteriaTranslator = new CodeSearchCriteriaTranslator();

        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdSearchCriteria.class, idSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(SampleSearchCriteria.class, new SampleSearchCriteriaTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StringFieldSearchCriteria.class, stringFieldSearchCriteriaTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CodeSearchCriteria.class, codeSearchCriteriaTranslator);
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
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NameSearchCriteria.class, codeSearchCriteriaTranslator);

        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(DataSetSearchCriteria.class, ColumnNames.DATA_SET_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(RegistratorSearchCriteria.class, ColumnNames.PERSON_REGISTERER_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(ModifierSearchCriteria.class, ColumnNames.PERSON_MODIFIER_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(SampleSearchCriteria.class, ColumnNames.SAMPLE_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(SampleContainerSearchCriteria.class, ColumnNames.PART_OF_SAMPLE_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(SampleTypeSearchCriteria.class, ColumnNames.SAMPLE_TYPE_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(ExperimentTypeSearchCriteria.class, ColumnNames.EXPERIMENT_TYPE_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(ExperimentSearchCriteria.class, ColumnNames.EXPERIMENT_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(ProjectSearchCriteria.class, ColumnNames.PROJECT_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(SpaceSearchCriteria.class, ColumnNames.SPACE_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(TagSearchCriteria.class, METAPROJECT_ID_COLUMN);
    }

    public static SelectQuery translate(final TranslationVo vo)
    {
        if (vo.getCriteria() == null)
        {
            throw new IllegalArgumentException("Null criteria provided.");
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

    private static String getAlias(final AtomicInteger num)
    {
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
                final IConditionTranslator conditionTranslator = CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion.getClass());
                if (conditionTranslator != null)
                {
                    @SuppressWarnings("unchecked")
                    final Map<String, JoinInformation> joinInformationMap = conditionTranslator.getJoinInformationMap(criterion,
                            vo.getTableMapper(), () -> getAlias(indexCounter));

                    if (joinInformationMap != null)
                    {
                        joinInformationMap.values().forEach((joinInformation) ->
                        {
                            TranslatorUtils.appendJoin(sqlBuilder, joinInformation, INNER_JOIN);
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
        if (isSearchAllCriteria(vo.getCriteria()))
        {
            return "";
        }

        final StringBuilder sqlBuilder = new StringBuilder().append(WHERE).append(SP);

        if (vo.getCriteria().isEmpty()) {
            sqlBuilder.append(TRUE);
        } else
        {
            final String logicalOperator = vo.getOperator().toString();
            final AtomicBoolean first = new AtomicBoolean(true);

            vo.getCriteria().forEach((criterion) ->
            {
                TranslatorUtils.appendIfFirst(sqlBuilder, SP + logicalOperator + SP, first);

                final ISearchManager<ISearchCriteria, ?, ?> subqueryManager = vo.getCriteriaToManagerMap().get(criterion.getClass());
                final TableMapper tableMapper = vo.getTableMapper();
                if (subqueryManager != null)
                {
                    final String column = (!(criterion instanceof TagSearchCriteria))
                            ? CRITERIA_TO_SUBQUERY_COLUMN_MAP.get(criterion.getClass())
                            : ID_COLUMN;
                    if (tableMapper != null && column != null)
                    {
                        final Set<Long> ids = subqueryManager.searchForIDs(vo.getUserId(), criterion, null);
                        appendInStatement(sqlBuilder, criterion, column, tableMapper);
                        vo.getArgs().add(ids.toArray(new Long[0]));
                    } else
                    {
                        throw new NullPointerException("tableMapper = " + tableMapper + " column = " + column + ", criterion.getClass() = " +
                                criterion.getClass());
                    }
                } else
                {
                    @SuppressWarnings("unchecked")
                    final IConditionTranslator<ISearchCriteria> conditionTranslator =
                            (IConditionTranslator<ISearchCriteria>) CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(criterion.getClass());
                    if (conditionTranslator != null)
                    {
                        conditionTranslator.translate(criterion, tableMapper, vo.getArgs(), sqlBuilder, vo.getAliases(),
                                vo.getDataTypeByPropertyName());
                    } else
                    {
                        throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
                    }
                }
            });
        }

        return sqlBuilder.toString();
    }

    private static void appendInStatement(final StringBuilder sqlBuilder, final ISearchCriteria criterion, final String column,
            final TableMapper tableMapper)
    {
        sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(column).append(SP).append(IN).append(SP).append(LP);
        if (!(criterion instanceof TagSearchCriteria))
        {
            sqlBuilder.append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP);
        } else
        {
            final String e = "e";
            final String mpa = "mpa";
            final String mp = "mp";
            sqlBuilder.append(SELECT).append(SP).append(e).append(PERIOD).append(column).append(NL).
                    append(FROM).append(SP).append(tableMapper.getEntitiesTable()).append(SP).append(e).append(NL).
                    append(INNER_JOIN).append(SP).append(METAPROJECT_ASSIGNMENTS_ALL_TABLE).append(SP).append(mpa).append(SP).
                    append(ON).append(SP).append(e).append(PERIOD).append(ID_COLUMN).append(SP).append(EQ).append(SP).append(mpa).append(PERIOD).
                    append(tableMapper.getMetaprojectAssignmentsEntityIdField()).append(NL).
                    append(INNER_JOIN).append(SP).append(METAPROJECTS_TABLE).append(SP).append(mp).append(SP).
                    append(ON).append(SP).append(mpa).append(PERIOD).append(METAPROJECT_ID_COLUMN).append(SP).append(EQ).append(SP).append(mp).append(PERIOD).
                    append(ID_COLUMN).append(NL).
                    append(WHERE).append(SP).append(mp).append(PERIOD).append(ID_COLUMN).append(SP).append(IN).append(SP).append(LP);
            sqlBuilder.append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP);
            sqlBuilder.append(RP);
        }
        sqlBuilder.append(RP);
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

}
