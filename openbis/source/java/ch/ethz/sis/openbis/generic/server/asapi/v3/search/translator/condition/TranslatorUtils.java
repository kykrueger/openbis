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

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateObjectValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractNumberValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateEarlierThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateLaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectEarlierThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateObjectLaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IDate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberGreaterThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberGreaterThanValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberLessThanOrEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberLessThanValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator.DATE_FORMAT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.BARS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.GE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.GT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IS_NOT_NULL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LIKE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERCENT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

public class TranslatorUtils
{

    /** Indicator that the property is internal. */
    private static final String INTERNAL_PROPERTY_PREFIX = "$";

    private TranslatorUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    static void appendStringComparatorOp(final AbstractStringValue value, final StringBuilder sqlBuilder, final List<Object> args)
    {
        if (value.getClass() == StringEqualToValue.class)
        {
            sqlBuilder.append(EQ).append(SP).append(QU);
            args.add(value.getValue());
        } else if (value.getClass() == StringStartsWithValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(QU).append(SP).append(BARS).append(SP).append(SQ).
                    append(PERCENT).append(SQ);
            args.add(value.getValue());
        } else if (value.getClass() == StringEndsWithValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(SQ).append(PERCENT).append(SQ).append(SP).append(BARS).append(SP).append(QU);
            args.add(value.getValue());
        } else if (value.getClass() == StringContainsValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(SQ).append(PERCENT).append(SQ).append(SP).append(BARS).append(SP).append(QU).
                    append(SP).append(BARS).append(SP).append(SQ).append(PERCENT).append(SQ);
            args.add(value.getValue());
        } else if (value.getClass() == AnyStringValue.class)
        {
            sqlBuilder.append(SP).append(IS_NOT_NULL);
        } else
        {
            throw new IllegalArgumentException("Unsupported AbstractStringValue type: " + value.getClass().getSimpleName());
        }
    }

    static void appendNumberComparatorOp(final AbstractNumberValue value, final StringBuilder sqlBuilder)
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

    public static Map<String, JoinInformation> getPropertyJoinInformationMap(final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        final String valuesTableAlias = aliasFactory.createAlias();
        final String entityTypesAttributeTypesTableAlias = aliasFactory.createAlias();
        final String attributeTypesTableAlias = aliasFactory.createAlias();

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setMainTable(tableMapper.getEntitiesTable());
        joinInformation1.setMainTableAlias(CriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(ID_COLUMN);
        joinInformation1.setSubTable(tableMapper.getValuesTable());
        joinInformation1.setSubTableAlias(valuesTableAlias);
        joinInformation1.setSubTableIdField(tableMapper.getValuesTableEntityIdField());
        result.put(tableMapper.getEntitiesTable(), joinInformation1);

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setMainTable(tableMapper.getValuesTable());
        joinInformation2.setMainTableAlias(valuesTableAlias);
        joinInformation2.setMainTableIdField(tableMapper.getValuesTableEntityTypeAttributeTypeIdField());
        joinInformation2.setSubTable(tableMapper.getEntityTypesAttributeTypesTable());
        joinInformation2.setSubTableAlias(entityTypesAttributeTypesTableAlias);
        joinInformation2.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getValuesTable(), joinInformation2);

        final JoinInformation joinInformation3 = new JoinInformation();
        joinInformation3.setMainTable(tableMapper.getEntityTypesAttributeTypesTable());
        joinInformation3.setMainTableAlias(entityTypesAttributeTypesTableAlias);
        joinInformation3.setMainTableIdField(tableMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField());
        joinInformation3.setSubTable(tableMapper.getAttributeTypesTable());
        joinInformation3.setSubTableAlias(attributeTypesTableAlias);
        joinInformation3.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getEntityTypesAttributeTypesTable(), joinInformation3);

        final JoinInformation joinInformation4 = new JoinInformation();
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
        joinInformation.setMainTable(tableMapper.getEntitiesTable());
        joinInformation.setMainTableAlias(CriteriaTranslator.MAIN_TABLE_ALIAS);
        joinInformation.setMainTableIdField(tableMapper.getEntitiesTableEntityTypeIdField());
        joinInformation.setSubTable(tableMapper.getEntityTypesTable());
        joinInformation.setSubTableAlias(aliasFactory.createAlias());
        joinInformation.setSubTableIdField(ID_COLUMN);
        result.put(tableMapper.getEntityTypesTable(), joinInformation);

        return result;
    }

    static void addDateValueToArgs(final IDate fieldValue, final List<Object> args)
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

    static void appendDateComparatorOp(final Object fieldValue, final StringBuilder sqlBuilder)
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

    static boolean isPropertyInternal(final String propertyName)
    {
        return propertyName.startsWith(INTERNAL_PROPERTY_PREFIX);
    }

    static String normalisePropertyName(final String propertyName)
    {
        return isPropertyInternal(propertyName) ? propertyName.substring(INTERNAL_PROPERTY_PREFIX.length()) : propertyName;
    }

    static void appendInternalExternalConstraint(final StringBuilder sqlBuilder, final List<Object> args,
            final String entityTypesSubTableAlias, final boolean internalProperty)
    {
        sqlBuilder.append(entityTypesSubTableAlias).append(PERIOD).append(ColumnNames.IS_INTERNAL_NAMESPACE).append(SP)
                .append(EQ).append(SP).append(QU).append(SP).append(AND);
        args.add(internalProperty);
    }

    public static void appendJoin(final StringBuilder sqlBuilder, final JoinInformation joinInformation, final String joinType)
    {
        if (joinInformation.getSubTable() != null)
        {
            sqlBuilder.append(NL).append(joinType).append(SP).append(joinInformation.getSubTable()).append(SP)
                    .append(joinInformation.getSubTableAlias()).append(SP)
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
    public static void appendIfFirst(final StringBuilder sb, final String value, final AtomicBoolean first)
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

}
