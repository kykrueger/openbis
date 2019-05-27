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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateObjectValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractObjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CollectionFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateEarlierThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateLaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectEarlierThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectLaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ModificationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.RegistrationDateSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.NoExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.NoProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.FullSampleIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERM_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PROJECTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SPACES_TABLE;

public class Translator
{

    private static final String UNNEST = "unnest";

    private static final String SELECT = "SELECT";

    private static final String DISTINCT = "DISTINCT";

    private static final String FROM = "FROM";

    private static final String WHERE = "WHERE";

    private static final String LEFT = "LEFT";

    private static final String JOIN = "JOIN";

    private static final String LIKE = "LIKE";

    private static final String ON = "ON";

    private static final String IN = "IN";

    private static final String IS = "IS";

    private static final String NOT = "NOT";

    private static final String AND = "AND";

    private static final String NULL = "NULL";

    private static final String SP = " ";

    private static final String IS_NULL = IS + SP + NULL;

    private static final String IS_NOT_NULL = IS + SP + NOT + SP + NULL;

    private static final String COMMA = ",";

    private static final String PERIOD = ".";

    private static final String EQ = "=";

    private static final String GT = ">";

    private static final String LT = "<";

    private static final String GE = ">=";

    private static final String LE = "<=";

    private static final String NEW_LINE = "\n";

    private static final String ASTERISK = "*";

    private static final String QU = "?";

    private static final String PERCENT = "%";

    private static final String BARS = "||";

    private static final String LP = "(";

    private static final String RP = ")";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            SimplePropertyValidator.SupportedDatePattern.CANONICAL_DATE_PATTERN.getPattern());

    //    private static final Map<Class<ISearchCriteria>, IConditionTranslator> CRITERIA_TO_CONDITION_TRANSLATOR_MAP;

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

        final String fromClause = buildFrom(dbEntityKind, criteria);
        final String whereClause = buildWhere(dbEntityKind, criteria, args, operator);
        final String selectClause = buildSelect(dbEntityKind, aliases);

        return new SelectQuery(selectClause + fromClause + whereClause, args);
    }

    private static String buildSelect(final EntityMapper dbEntityKind, final List<String> aliasesPresentInOrderBy)
    {
        final StringBuilder builder = new StringBuilder();
        builder.append(SELECT).append(SP).append(DISTINCT).append(SP).append(dbEntityKind.getEntitiesTableIdField());
        aliasesPresentInOrderBy.forEach(alias -> builder.append(COMMA).append(SP).append(alias));
        builder.append(NEW_LINE);

        return builder.toString();
    }

    private static String buildFrom(final EntityMapper entityMapper, final List<ISearchCriteria> criteria)
    {
        final StringBuilder builder = new StringBuilder();
        final String entitiesTableName = entityMapper.getEntitiesTable();
        builder.append(FROM).append(SP).append(entitiesTableName).append(NEW_LINE);
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
//                    builder.append(LEFT).append(SP).append(JOIN).append(SP).append().append(SP).append(entitiesTableName).append(SP).append(ON)
//                            .append(SP).append(entitiesTableName).append(PERIOD).append().append(EQ).append().append(PERIOD).append();
        });
        return builder.toString();
    }

    private static String buildWhere(final EntityMapper entityMapper, final List<ISearchCriteria> criteria, final List<Object> args,
            final SearchOperator operator)
    {
        if (isSearchAllCriteria(criteria))
        {
            return "";
        }

        final StringBuilder sqlBuilder = new StringBuilder();

        final String logicalOperator = operator.toString();
        sqlBuilder.append(WHERE).append(SP);

        criteria.forEach(subcriterion ->
                {
                    if (subcriterion instanceof AbstractFieldSearchCriteria<?>)
                    {
                        final AbstractFieldSearchCriteria<?> fieldSearchSubcriterion = (AbstractFieldSearchCriteria<?>) subcriterion;
                        final Object fieldName = fieldSearchSubcriterion.getFieldName();
                        final Object fieldValue = fieldSearchSubcriterion.getFieldValue();
                        final SearchFieldType fieldType = fieldSearchSubcriterion.getFieldType();

                        if (subcriterion instanceof CollectionFieldSearchCriteria<?>)
                        {
                            sqlBuilder.append(fieldName).append(SP).append(IN).append(SP).append(LP).
                                    append(SELECT).append(SP).append(UNNEST).append(LP).append(QU).append(RP).
                                    append(RP);
                        } else
                        {
                            if (fieldValue == null)
                            {
                                sqlBuilder.append(fieldName).append(SP).append(IS_NOT_NULL);
                            } else
                            {
                                if (subcriterion instanceof StringFieldSearchCriteria)
                                {
                                    sqlBuilder.append(fieldName);
                                    if (fieldValue.getClass() == StringEqualToValue.class)
                                    {
                                        sqlBuilder.append(EQ).append(QU);
                                        args.add(((AbstractStringValue) fieldValue).getValue());
                                    } else if (fieldValue.getClass() == StringContainsValue.class)
                                    {
                                        sqlBuilder.append(SP).append(LIKE).append(SP).append(PERCENT).append(SP).append(BARS).append(SP).
                                                append(QU).append(SP).append(BARS).append(SP).append(PERCENT);
                                        args.add(((AbstractStringValue) fieldValue).getValue());
                                    } else if (fieldValue.getClass() == StringStartsWithValue.class)
                                    {
                                        sqlBuilder.append(SP).append(LIKE).append(SP).append(QU).append(SP).append(BARS).append(SP).
                                                append(PERCENT);
                                        args.add(((AbstractStringValue) fieldValue).getValue());
                                    } else if (fieldValue.getClass() == StringEndsWithValue.class)
                                    {
                                        sqlBuilder.append(SP).append(LIKE).append(SP).append(PERCENT).append(SP).append(BARS).append(SP).append(QU);
                                        args.add(((AbstractStringValue) fieldValue).getValue());
                                    } else if (subcriterion instanceof AnyFieldSearchCriteria)
                                    {
                                        throw new IllegalArgumentException();
                                    } else if (subcriterion instanceof StringPropertySearchCriteria)
                                    {
                                        throw new IllegalArgumentException();
                                    } else
                                    {
                                        throw new IllegalArgumentException("Unsupported field value: " + fieldValue.getClass().getSimpleName());
                                    }
                                } else if (subcriterion instanceof DateFieldSearchCriteria)
                                {
                                    if (subcriterion instanceof RegistrationDateSearchCriteria)
                                    {
                                        sqlBuilder.append(REGISTRATION_TIMESTAMP_COLUMN);
                                    } else if (subcriterion instanceof ModificationDateSearchCriteria)
                                    {
                                        sqlBuilder.append(MODIFICATION_TIMESTAMP_COLUMN);
                                    } else
                                    {
                                        sqlBuilder.append(fieldName);
                                    }

                                    if (fieldValue instanceof DateEqualToValue || fieldValue instanceof DateObjectEqualToValue)
                                    {
                                        sqlBuilder.append(EQ).append(QU);
                                    } else if (fieldValue instanceof DateEarlierThanOrEqualToValue ||
                                            fieldValue instanceof DateObjectEarlierThanOrEqualToValue)
                                    {
                                        sqlBuilder.append(LE).append(QU);
                                    } else if (fieldValue instanceof DateLaterThanOrEqualToValue ||
                                            fieldValue instanceof DateObjectLaterThanOrEqualToValue)
                                    {
                                        sqlBuilder.append(GE).append(QU);
                                    } else
                                    {
                                        throw new IllegalArgumentException("Unsupported field value: " + fieldValue.getClass().getSimpleName());
                                    }

                                    if (fieldValue instanceof AbstractDateValue)
                                    {
                                        // String type date value.
                                        final String dateString = ((AbstractDateValue) fieldValue).getValue();
                                        try
                                        {
                                            args.add(DATE_FORMAT.parse(dateString));
                                        } catch (ParseException e)
                                        {
                                            throw new IllegalArgumentException("Illegal date [dateString='" + dateString + "']", e);
                                        }
                                    } else
                                    {
                                        // Date type date value.
                                        args.add(((AbstractDateObjectValue) fieldValue).getValue());
                                    }
                                } else if (subcriterion instanceof NumberPropertySearchCriteria)
                                {
                                    throw new IllegalArgumentException();
                                } else
                                {
                                    sqlBuilder.append(fieldName).append(EQ).append(QU);
                                    args.add(fieldValue);
                                }
                            }
                        }
                    } else if (subcriterion instanceof AbstractObjectSearchCriteria<?>)
                    {
                        if (subcriterion instanceof SampleSearchCriteria)
                        {
                            throw new IllegalArgumentException("Unsupported criterion type: " +
                                    subcriterion.getClass().getSimpleName());
                        } else if (subcriterion instanceof ExperimentSearchCriteria)
                        {
                            throw new IllegalArgumentException("Unsupported criterion type: " +
                                    subcriterion.getClass().getSimpleName());
                        } else if (subcriterion instanceof ProjectSearchCriteria)
                        {
                            throw new IllegalArgumentException("Unsupported criterion type: " +
                                    subcriterion.getClass().getSimpleName());
                        } else if (subcriterion instanceof SpaceSearchCriteria)
                        {
                            throw new IllegalArgumentException("Unsupported criterion type: " +
                                    subcriterion.getClass().getSimpleName());
                        } else if (subcriterion instanceof SampleContainerSearchCriteria)
                        {
                            throw new IllegalArgumentException("Unsupported criterion type: " +
                                    subcriterion.getClass().getSimpleName());
                        } else if (subcriterion instanceof SampleTypeSearchCriteria)
                        {
                            throw new IllegalArgumentException("Unsupported criterion type: " +
                                    subcriterion.getClass().getSimpleName());
                        } else if (subcriterion instanceof PersonSearchCriteria)
                        {
                            throw new IllegalArgumentException("Unsupported criterion type: " +
                                    subcriterion.getClass().getSimpleName());
                        } else if (subcriterion instanceof TagSearchCriteria)
                        {
                            throw new IllegalArgumentException("Unsupported criterion type: " +
                                    subcriterion.getClass().getSimpleName());
                        } else
                        {
                            throw new IllegalArgumentException("Unsupported criterion type");
                        }
                    } else if (subcriterion instanceof IdSearchCriteria<?>)
                    {
                        final IdSearchCriteria<ISampleId> fieldSearchSubcriterion = (IdSearchCriteria<ISampleId>) subcriterion;
                        final ISampleId sampleId = fieldSearchSubcriterion.getId();

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

                            sqlBuilder.append(CODE_COLUMN).append(EQ).append(QU);
                            args.add(sampleCode);
                        } else if (sampleId.getClass() == SamplePermId.class)
                        {
                            sqlBuilder.append(PERM_ID_COLUMN).append(EQ).append(QU);
                            args.add(((SamplePermId) sampleId).getPermId());
                        } else
                        {
                            throw new IllegalArgumentException("The following ID class is not supported: " + sampleId.getClass().getSimpleName());
                        }
                    } else if (subcriterion instanceof NoSampleSearchCriteria)
                    {
                        sqlBuilder.append(PART_OF_SAMPLE_COLUMN).append(SP).append(IS_NULL);
                    } else if (subcriterion instanceof NoExperimentSearchCriteria)
                    {
                        sqlBuilder.append(ColumnNames.EXPERIMENT_COLUMN).append(SP).append(IS_NULL);
                    } else if (subcriterion instanceof NoProjectSearchCriteria)
                    {
                        sqlBuilder.append(ColumnNames.PROJECT_COLUMN).append(SP).append(IS_NULL);
                    } else if (subcriterion instanceof NoSpaceSearchCriteria)
                    {
                        sqlBuilder.append(ColumnNames.SPACE_COLUMN).append(SP).append(IS_NULL);
                    } else
                    {
                        throw new IllegalArgumentException("Unsupported criterion type");
                    }
                    sqlBuilder.append(SP).append(logicalOperator).append(SP);
                });

        criteria.forEach(criterion ->
        {
            if (criterion.getClass() == SampleSearchCriteria.class)
            {

            }
        });
        sqlBuilder.setLength(sqlBuilder.length() - logicalOperator.length() - SP.length() * 2);
        return sqlBuilder.toString();
    }

    private static void buildSelectByIdConditionWithSubquery(final StringBuilder sqlBuilder, final String columnName, final String subqueryTable)
    {
        sqlBuilder.append(columnName).append(EQ).append(LP).
                append(SELECT).append(SP).append(ID_COLUMN).append(SP).append(FROM).append(SP).append(subqueryTable).append(SP).
                append(WHERE).append(SP).append(CODE_COLUMN).append(EQ).append(QU).
                append(RP).append(SP).append(AND).append(SP);
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
