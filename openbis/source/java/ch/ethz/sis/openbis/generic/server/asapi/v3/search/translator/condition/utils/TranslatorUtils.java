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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.IAliasFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator.DATE_FORMAT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class TranslatorUtils
{

    /** Indicator that the property is internal. */
    private static final String INTERNAL_PROPERTY_PREFIX = "$";

    private TranslatorUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void appendStringComparatorOp(final AbstractStringValue value, final StringBuilder sqlBuilder, final List<Object> args)
    {
        appendStringComparatorOp(value.getClass(), value.getValue(), sqlBuilder, args);
    }

    public static void appendStringComparatorOp(final Class<?> valueClass, final String finalValue, final StringBuilder sqlBuilder,
            final List<Object> args)
    {
        sqlBuilder.append(SP);
        if (valueClass == StringEqualToValue.class)
        {
            if (!containsWildcards(finalValue))
            {
                sqlBuilder.append(EQ).append(SP).append(QU);
                args.add(finalValue);
            } else
            {
                sqlBuilder.append(ILIKE).append(SP).append(QU);
                args.add(toPSQLWildcards(finalValue));
            }
        } else if (valueClass == StringStartsWithValue.class)
        {
            sqlBuilder.append(ILIKE).append(SP).append(QU);
            args.add(toPSQLWildcards(finalValue) + PERCENT);
        } else if (valueClass == StringEndsWithValue.class)
        {
            sqlBuilder.append(ILIKE).append(SP).append(QU);
            args.add(PERCENT + toPSQLWildcards(finalValue));
        } else if (valueClass == StringContainsValue.class)
        {
            sqlBuilder.append(ILIKE).append(SP).append(QU);
            args.add(PERCENT + toPSQLWildcards(finalValue) + PERCENT);
        } else if (valueClass == AnyStringValue.class)
        {
            sqlBuilder.append(IS_NOT_NULL);
        } else
        {
            throw new IllegalArgumentException("Unsupported AbstractStringValue type: " + valueClass.getSimpleName());
        }
    }

    /**
     * Determines whether the string contains search wildcards.
     *
     * @param str string to be checked.
     * @return {@code true} if the string contains '*' or '?'.
     */
    private static boolean containsWildcards(final String str)
    {
        return str.matches(".*[*?].*");
    }

    /**
     * Changes '*' -> '%' and '?' -> '_' to match PSQL pattern matching standards. Escapes already existing '%', '_' and '\' characters with '\'.
     *
     * @param str string to be converted.
     * @return string that corresponds to the PSQL standard.
     */
    private static String toPSQLWildcards(final String str)
    {
        final StringBuilder sb = new StringBuilder();
        str.chars().forEach((value) ->
        {
            final char ch = (char) value;
            switch (ch)
            {
                case UNDERSCORE:
                    // Fall through.
                case PERCENT:
                    // Fall through.
                case BACKSLASH:
                    sb.append(BACKSLASH).append(ch);
                    break;
                case ASTERISK:
                    sb.append(PERCENT);
                    break;
                case QU:
                    sb.append(UNDERSCORE);
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        });
        return sb.toString();
    }

    public static void appendNumberComparatorOp(final AbstractNumberValue value, final StringBuilder sqlBuilder)
    {
        if (value.getClass() == NumberEqualToValue.class)
        {
            sqlBuilder.append(EQ);
        } else if (value.getClass() == NumberLessThanValue.class)
        {
            sqlBuilder.append(LT);
        } else if (value.getClass() == NumberLessThanOrEqualToValue.class)
        {
            sqlBuilder.append(LE);
        } else if (value.getClass() == NumberGreaterThanValue.class)
        {
            sqlBuilder.append(GT);
        } else if (value.getClass() == NumberGreaterThanOrEqualToValue.class)
        {
            sqlBuilder.append(GE);
        } else
        {
            throw new IllegalArgumentException("Unsupported AbstractNumberValue type: " + value.getClass().getSimpleName());
        }
        sqlBuilder.append(SP).append(QU);
    }

    public static Map<String, JoinInformation> getPropertyJoinInformationMap(final TableMapper tableMapper, final IAliasFactory aliasFactory,
            final JoinType joinType)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        final String valuesTableAlias = aliasFactory.createAlias();
        final String entityTypesAttributeTypesTableAlias = aliasFactory.createAlias();
        final String attributeTypesTableAlias = aliasFactory.createAlias();

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setJoinType(joinType);
        joinInformation1.setMainTable(tableMapper.getEntitiesTable());
        joinInformation1.setMainTableAlias(CriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(ID_COLUMN);
        joinInformation1.setSubTable(tableMapper.getValuesTable());
        joinInformation1.setSubTableAlias(valuesTableAlias);
        joinInformation1.setSubTableIdField(tableMapper.getValuesTableEntityIdField());
        result.put(tableMapper.getEntitiesTable(), joinInformation1);

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setJoinType(joinType);
        joinInformation2.setMainTable(tableMapper.getValuesTable());
        joinInformation2.setMainTableAlias(valuesTableAlias);
        joinInformation2.setMainTableIdField(tableMapper.getValuesTableEntityTypeAttributeTypeIdField());
        joinInformation2.setSubTable(tableMapper.getEntityTypesAttributeTypesTable());
        joinInformation2.setSubTableAlias(entityTypesAttributeTypesTableAlias);
        joinInformation2.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getValuesTable(), joinInformation2);

        final JoinInformation joinInformation3 = new JoinInformation();
        joinInformation3.setJoinType(joinType);
        joinInformation3.setMainTable(tableMapper.getEntityTypesAttributeTypesTable());
        joinInformation3.setMainTableAlias(entityTypesAttributeTypesTableAlias);
        joinInformation3.setMainTableIdField(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField());
        joinInformation3.setSubTable(tableMapper.getAttributeTypesTable());
        joinInformation3.setSubTableAlias(attributeTypesTableAlias);
        joinInformation3.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getEntityTypesAttributeTypesTable(), joinInformation3);

        final JoinInformation joinInformation4 = new JoinInformation();
        joinInformation4.setJoinType(joinType);
        joinInformation4.setMainTable(tableMapper.getAttributeTypesTable());
        joinInformation4.setMainTableAlias(attributeTypesTableAlias);
        joinInformation4.setMainTableIdField(tableMapper.getAttributeTypesTableDataTypeIdField());
        joinInformation4.setSubTable(TableNames.DATA_TYPES_TABLE);
        joinInformation4.setSubTableAlias(aliasFactory.createAlias());
        joinInformation4.setSubTableIdField(ColumnNames.ID_COLUMN);
        result.put(tableMapper.getAttributeTypesTable(), joinInformation4);

        return result;
    }

    public static Map<String, JoinInformation> getTypeJoinInformationMap(final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();

        final JoinInformation joinInformation = new JoinInformation();
        joinInformation.setJoinType(JoinType.INNER);
        joinInformation.setMainTable(tableMapper.getEntitiesTable());
        joinInformation.setMainTableAlias(CriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation.setMainTableIdField(tableMapper.getEntitiesTableEntityTypeIdField());
        joinInformation.setSubTable(tableMapper.getEntityTypesTable());
        joinInformation.setSubTableAlias(aliasFactory.createAlias());
        joinInformation.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getEntityTypesTable(), joinInformation);

        return result;
    }

    public static Map<String, JoinInformation> getRelationshipsJoinInformationMap(final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        final String relationshipsTableAlias = aliasFactory.createAlias();

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setJoinType(JoinType.INNER);
        joinInformation1.setMainTable(tableMapper.getEntitiesTable());
        joinInformation1.setMainTableAlias(CriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(ID_COLUMN);
        joinInformation1.setSubTable(tableMapper.getRelationshipsTable());
        joinInformation1.setSubTableAlias(relationshipsTableAlias);
        joinInformation1.setSubTableIdField(tableMapper.getRelationshipsTableParentIdField());
        result.put(tableMapper.getRelationshipsTable(), joinInformation1);

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setJoinType(JoinType.INNER);
        joinInformation2.setMainTable(tableMapper.getRelationshipsTable());
        joinInformation2.setMainTableAlias(relationshipsTableAlias);
        joinInformation2.setMainTableIdField(tableMapper.getRelationshipsTableChildIdField());
        joinInformation2.setSubTable(tableMapper.getEntitiesTable());
        joinInformation2.setSubTableAlias(aliasFactory.createAlias());
        joinInformation2.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getEntitiesTable(), joinInformation2);

        final JoinInformation joinInformation3 = new JoinInformation();
        joinInformation3.setJoinType(JoinType.INNER);
        joinInformation3.setMainTable(tableMapper.getRelationshipsTable());
        joinInformation3.setMainTableAlias(relationshipsTableAlias);
        joinInformation3.setMainTableIdField(RELATIONSHIP_COLUMN);
        joinInformation3.setSubTable(RELATIONSHIP_TYPES_TABLE);
        joinInformation3.setSubTableAlias(aliasFactory.createAlias());
        joinInformation3.setSubTableIdField(ID_COLUMN);
        result.put(RELATIONSHIP_TYPES_TABLE, joinInformation3);

        return result;
    }

    public static void addDateValueToArgs(final IDate fieldValue, final List<Object> args)
    {
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
    }

    public static void appendDateComparatorOp(final Object fieldValue, final StringBuilder sqlBuilder)
    {
        if (fieldValue instanceof DateEqualToValue || fieldValue instanceof DateObjectEqualToValue)
        {
            sqlBuilder.append(EQ);
        } else if (fieldValue instanceof DateEarlierThanOrEqualToValue || fieldValue instanceof DateObjectEarlierThanOrEqualToValue)
        {
            sqlBuilder.append(LE);
        } else if (fieldValue instanceof DateLaterThanOrEqualToValue || fieldValue instanceof DateObjectLaterThanOrEqualToValue)
        {
            sqlBuilder.append(GE);
        } else
        {
            throw new IllegalArgumentException("Unsupported field value: " + fieldValue.getClass().getSimpleName());
        }
        sqlBuilder.append(SP).append(QU);
    }

    public static boolean isPropertyInternal(final String propertyName)
    {
        return propertyName.startsWith(INTERNAL_PROPERTY_PREFIX);
    }

    public static String normalisePropertyName(final String propertyName)
    {
        return isPropertyInternal(propertyName) ? propertyName.substring(INTERNAL_PROPERTY_PREFIX.length()) : propertyName;
    }

    public static void appendInternalExternalConstraint(final StringBuilder sqlBuilder, final List<Object> args,
            final String entityTypesSubTableAlias, final boolean internalProperty)
    {
        sqlBuilder.append(entityTypesSubTableAlias).append(PERIOD).append(ColumnNames.IS_INTERNAL_NAMESPACE).append(SP)
                .append(EQ).append(SP).append(QU).append(SP).append(AND);
        args.add(internalProperty);
    }

    public static void appendJoin(final StringBuilder sqlBuilder, final JoinInformation joinInformation)
    {
        if (joinInformation.getSubTable() != null)
        {
            sqlBuilder.append(NL).append(joinInformation.getJoinType()).append(SP).append(JOIN).append(SP).append(joinInformation.getSubTable())
                    .append(SP).append(joinInformation.getSubTableAlias()).append(SP)
                    .append(ON).append(SP).append(joinInformation.getMainTableAlias())
                    .append(PERIOD).append(joinInformation.getMainTableIdField())
                    .append(SP)
                    .append(EQ).append(SP).append(joinInformation.getSubTableAlias()).append(PERIOD)
                    .append(joinInformation.getSubTableIdField());
        }
    }

    /**
     * Appends given string to string builder only when atomic boolean is false. Otherwise just sets atomic boolean to false.
     *
     * @param sb string builder to be updated.
     * @param value the value to be added when needed.
     * @param first atomic boolean, if {@code true} it will be set to false with no change to sb, otherwise the {@code value} will be appended to
     * {@code sb}.
     */
    public static void appendIfNotFirst(final StringBuilder sb, final String value, final AtomicBoolean first)
    {
        if (first.get())
        {
            first.set(false);
        } else
        {
            sb.append(value);
        }
    }

    public static boolean isPropertySearchFieldName(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.startsWith(EntityWithPropertiesSortOptions.PROPERTY);
    }

    public static Object convertStringToType(final String value, final Class klass)
    {
        // Integer numbers need to be converted from string to a real number first, because they can be presented with decimal point.
        if (Boolean.class == klass)
        {
            return Boolean.parseBoolean(value);
        }
        if (Byte.class == klass)
        {
            final float decimalValue = Float.parseFloat(value);
            return (byte) decimalValue;
        }
        if (Short.class == klass)
        {
            final float decimalValue = Float.parseFloat(value);
            return (short) decimalValue;
        }
        if (Integer.class == klass)
        {
            final float decimalValue = Float.parseFloat(value);
            return (int) decimalValue;
        }
        if (Long.class == klass)
        {
            final double decimalValue = Double.parseDouble(value);
            return (long) decimalValue;
        }
        if (Float.class == klass)
        {
            return Float.parseFloat(value);
        }
        if (Double.class == klass)
        {
            return Double.parseDouble(value);
        }

        return value;
    }

    public static String stripQuotationMarks(final String value)
    {
        if (value.startsWith("\"") && value.endsWith("\""))
        {
            return value.substring(1, value.length() - 1);
        } else
        {
            return value;
        }
    }

    /**
     * Appends one of the the following texts to sqlBuilder depending on the value of {@code samplesTableAlias}. If it is {@code null} the second
     * version will be appended.
     *
     * <pre>
     *     '/' || coalesce([spacesTableAlias].code || '/', '') || coalesce([projectsTableAlias].code || '/', '') || coalesce([samplesTableAlias].code || ':', '') || t0.code
     * </pre>
     * <pre>
     *     '/' || coalesce([spacesTableAlias].code || '/', '') || coalesce([projectsTableAlias].code || '/', '') || t0.code
     * </pre>
     *
     * @param sqlBuilder query builder.
     * @param spacesTableAlias alias of the spaces table.
     * @param projectsTableAlias alias of the projects table.
     * @param samplesTableAlias alias of the samples table, {@code null} indicates that the table should not be included.
     */
    public static void buildFullIdentifierConcatenationString(final StringBuilder sqlBuilder, final String spacesTableAlias,
            final String projectsTableAlias, final String samplesTableAlias)
    {
        final String slash = "/";
        final String colon = ":";
        sqlBuilder.append(SQ).append(slash).append(SQ).append(SP).append(BARS);

        if (spacesTableAlias != null)
        {
            appendCoalesce(sqlBuilder, spacesTableAlias, slash);
        }
        if (projectsTableAlias != null)
        {
            appendCoalesce(sqlBuilder, projectsTableAlias, slash);
        }
        if (samplesTableAlias != null)
        {
            appendCoalesce(sqlBuilder, samplesTableAlias, colon);
        }

        sqlBuilder.append(SP).append(MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP);
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

    public static Map<String, JoinInformation> getIdentifierJoinInformationMap(final TableMapper tableMapper,
            final IAliasFactory aliasFactory, final String prefix)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        appendIdentifierJoinInformationMap(result, tableMapper, aliasFactory, prefix);
        return result;
    }

    public static void appendIdentifierJoinInformationMap(final Map<String, JoinInformation> result, final TableMapper tableMapper,
            final IAliasFactory aliasFactory, final String prefix)
    {
        final String entitiesTable = tableMapper.getEntitiesTable();

        final String samplesTableName = TableMapper.SAMPLE.getEntitiesTable();
        if (entitiesTable.equals(samplesTableName))
        {
            // Only samples can have spaces.
            final JoinInformation joinInformation1 = new JoinInformation();
            joinInformation1.setJoinType(JoinType.LEFT);
            joinInformation1.setMainTable(entitiesTable);
            joinInformation1.setMainTableAlias(MAIN_TABLE_ALIAS);
            joinInformation1.setMainTableIdField(SPACE_COLUMN);
            joinInformation1.setSubTable(SPACES_TABLE);
            joinInformation1.setSubTableAlias(aliasFactory.createAlias());
            joinInformation1.setSubTableIdField(ID_COLUMN);
            result.put(prefix + SPACES_TABLE, joinInformation1);
        }

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setJoinType(JoinType.LEFT);
        joinInformation2.setMainTable(entitiesTable);
        joinInformation2.setMainTableAlias(MAIN_TABLE_ALIAS);
        joinInformation2.setMainTableIdField(PROJECT_COLUMN);
        joinInformation2.setSubTable(PROJECTS_TABLE);
        joinInformation2.setSubTableAlias(aliasFactory.createAlias());
        joinInformation2.setSubTableIdField(ID_COLUMN);
        result.put(prefix + PROJECTS_TABLE, joinInformation2);

        if (entitiesTable.equals(samplesTableName))
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
            result.put(prefix + entitiesTable, joinInformation3);
        }
    }

}
