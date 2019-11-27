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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdentifierSearchCriteria;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.ListableSampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.search.SemanticAnnotationSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AbsenceConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AnyPropertySearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.CodeSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.CollectionFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.DateFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.EmailSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.FirstNameSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IdSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IdentifierSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.LastNameSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.ListableSampleTypeSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.NumberFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.SampleSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.StringFieldSearchConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.UserIdSearchConditionTranslator;
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
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROPERTY_TYPE_COLUMN;
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
        final StringFieldSearchConditionTranslator stringFieldSearchConditionTranslator = new StringFieldSearchConditionTranslator();
        final DateFieldSearchConditionTranslator dateFieldSearchConditionTranslator = new DateFieldSearchConditionTranslator();
        final NumberFieldSearchConditionTranslator numberFieldSearchConditionTranslator = new NumberFieldSearchConditionTranslator();
        final CollectionFieldSearchConditionTranslator collectionFieldSearchConditionTranslator = new CollectionFieldSearchConditionTranslator();
        final AbsenceConditionTranslator absenceConditionTranslator = new AbsenceConditionTranslator();
        final CodeSearchConditionTranslator codeSearchConditionTranslator = new CodeSearchConditionTranslator();

        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdSearchCriteria.class, new IdSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdentifierSearchCriteria.class, new IdentifierSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(SampleSearchCriteria.class, new SampleSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StringFieldSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CodeSearchCriteria.class, codeSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(PermIdSearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NumberFieldSearchCriteria.class, numberFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(DateFieldSearchCriteria.class, dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(RegistrationDateSearchCriteria.class, dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ModificationDateSearchCriteria.class, dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CollectionFieldSearchCriteria.class, collectionFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(CodesSearchCriteria.class, collectionFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(IdsSearchCriteria.class, collectionFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(UserIdsSearchCriteria.class, collectionFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSampleSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSampleContainerSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoExperimentSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoProjectSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NoSpaceSearchCriteria.class, absenceConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyFieldSearchCriteria.class, new AnyFieldSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(StringPropertySearchCriteria.class, stringFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NumberPropertySearchCriteria.class, numberFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(DatePropertySearchCriteria.class, dateFieldSearchConditionTranslator);
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(AnyPropertySearchCriteria.class, new AnyPropertySearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(ListableSampleTypeSearchCriteria.class, new ListableSampleTypeSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(UserIdSearchCriteria.class, new UserIdSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(FirstNameSearchCriteria.class, new FirstNameSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(LastNameSearchCriteria.class, new LastNameSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(EmailSearchCriteria.class, new EmailSearchConditionTranslator());
        CRITERIA_TO_CONDITION_TRANSLATOR_MAP.put(NameSearchCriteria.class, codeSearchConditionTranslator);

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
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(PropertyTypeSearchCriteria.class, PROPERTY_TYPE_COLUMN);

        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(TagSearchCriteria.class, ID_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(SemanticAnnotationSearchCriteria.class, ID_COLUMN);
        CRITERIA_TO_SUBQUERY_COLUMN_MAP.put(PropertyAssignmentSearchCriteria.class, ID_COLUMN);
    }

    public static SelectQuery translate(final TranslationVo vo)
    {
        if (vo.getCriteria() == null)
        {
            throw new IllegalArgumentException("Null criteria provided.");
        }

        final String from = buildFrom(vo);
        final String where = buildWhere(vo);
        final String select = buildSelect(vo);

        return new SelectQuery(select  + NL + from + NL + where, vo.getArgs());
    }

    private static String buildSelect(final TranslationVo vo)
    {
        return SELECT + SP + DISTINCT + SP + MAIN_TABLE_ALIAS + PERIOD + vo.getIdColumnName();
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
                                TranslatorUtils.appendJoin(sqlBuilder, joinInformation));
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
            return WHERE + SP + TRUE;
        } else
        {
            final String logicalOperator = vo.getOperator().toString();
            final String separator = SP + logicalOperator + SP;

            final StringBuilder resultSqlBuilder = vo.getCriteria().stream().collect(
                    StringBuilder::new,
                    (sqlBuilder, criterion) ->
                    {
                        sqlBuilder.append(separator);
                        appendCriterionCondition(vo, sqlBuilder, criterion);
                    },
                    StringBuilder::append
            );

            return WHERE + SP + resultSqlBuilder.substring(separator.length());
        }
    }

    /**
     * Appends condition translated from a criterion.
     *
     * @param vo value object with miscellaneous information.
     * @param sqlBuilder string builder to append the condition to.
     * @param criterion criterion to be translated.
     */
    private static void appendCriterionCondition(final TranslationVo vo, final StringBuilder sqlBuilder, final ISearchCriteria criterion)
    {
        final ISearchManager<ISearchCriteria, ?, ?> subqueryManager = vo.getCriteriaToManagerMap().get(criterion.getClass());
        final TableMapper tableMapper = vo.getTableMapper();
        if (subqueryManager != null)
        {
            final String column = CRITERIA_TO_SUBQUERY_COLUMN_MAP.get(criterion.getClass());
            if (tableMapper != null && column != null)
            {
                final Set<Long> ids = subqueryManager.searchForIDs(vo.getUserId(), criterion, null);
                appendInStatement(sqlBuilder, criterion, column, tableMapper);
                vo.getArgs().add(ids.toArray(new Long[0]));
            } else
            {
                throw new NullPointerException("tableMapper = " + tableMapper + ", column = " + column + ", criterion.getClass() = " +
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
        switch (criteria.size())
        {
            case 0:
            {
                return true;
            }

            case 1:
            {
                final ISearchCriteria criterion = criteria.iterator().next();
                return criterion instanceof AbstractEntitySearchCriteria<?> &&
                        ((AbstractEntitySearchCriteria<?>) criterion).getCriteria().isEmpty();
            }

            default:
            {
                return false;
            }
        }
    }

}
