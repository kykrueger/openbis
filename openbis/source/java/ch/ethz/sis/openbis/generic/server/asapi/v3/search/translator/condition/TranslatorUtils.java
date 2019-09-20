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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateObjectValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractDateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractNumberValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
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
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.BARS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.GE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.GT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LIKE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERCENT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.DATE_FORMAT;

class TranslatorUtils
{

    /** Indicator that the property is internal. */
    private static final String INTERNAL_PROPERTY_PREFIX = "$";

    private TranslatorUtils()
    {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    static void appendStringComparatorOp(final AbstractStringValue value, final StringBuilder sqlBuilder)
    {
        if (value.getClass() == StringEqualToValue.class)
        {
            sqlBuilder.append(EQ).append(SP).append(QU);
        } else if (value.getClass() == StringStartsWithValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(QU).append(SP).append(BARS).append(SP).append(SQ).
                    append(PERCENT).append(SQ);
        } else if (value.getClass() == StringEndsWithValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(SQ).append(PERCENT).append(SQ).append(SP).append(BARS).append(SP).append(QU);
        } else if (value.getClass() == StringContainsValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(SQ).append(PERCENT).append(SQ).append(SP).append(BARS).append(SP).append(QU).
                    append(SP).append(BARS).append(SP).append(SQ).append(PERCENT).append(SQ);
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

    static Map<String, JoinInformation> getPropertyJoinInformationMap(final EntityMapper entityMapper, final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = new LinkedHashMap<>();
        final String valuesTableAlias = aliasFactory.createAlias();
        final String entityTypesAttributeTypesTableAlias = aliasFactory.createAlias();
        final String attributeTypesTableAlias = aliasFactory.createAlias();

        final JoinInformation joinInformation1 = new JoinInformation();
        joinInformation1.setMainTable(entityMapper.getEntitiesTable());
        joinInformation1.setMainTableAlias(Translator.MAIN_TABLE_ALIAS);
        joinInformation1.setMainTableIdField(entityMapper.getEntitiesTableIdField());
        joinInformation1.setSubTable(entityMapper.getValuesTable());
        joinInformation1.setSubTableAlias(valuesTableAlias);
        joinInformation1.setSubTableIdField(entityMapper.getValuesTableEntityIdField());
        result.put(entityMapper.getEntitiesTable(), joinInformation1);

        final JoinInformation joinInformation2 = new JoinInformation();
        joinInformation2.setMainTable(entityMapper.getValuesTable());
        joinInformation2.setMainTableAlias(valuesTableAlias);
        joinInformation2.setMainTableIdField(entityMapper.getValuesTableEntityTypeAttributeTypeIdField());
        joinInformation2.setSubTable(entityMapper.getEntityTypesAttributeTypesTable());
        joinInformation2.setSubTableAlias(entityTypesAttributeTypesTableAlias);
        joinInformation2.setSubTableIdField(entityMapper.getEntityTypesAttributeTypesTableIdField());
        result.put(entityMapper.getValuesTable(), joinInformation2);

        final JoinInformation joinInformation3 = new JoinInformation();
        joinInformation3.setMainTable(entityMapper.getEntityTypesAttributeTypesTable());
        joinInformation3.setMainTableAlias(entityTypesAttributeTypesTableAlias);
        joinInformation3.setMainTableIdField(entityMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField());
        joinInformation3.setSubTable(entityMapper.getAttributeTypesTable());
        joinInformation3.setSubTableAlias(attributeTypesTableAlias);
        joinInformation3.setSubTableIdField(entityMapper.getAttributeTypesTableIdField());
        result.put(entityMapper.getEntityTypesAttributeTypesTable(), joinInformation3);

        final JoinInformation joinInformation4 = new JoinInformation();
        joinInformation4.setMainTable(entityMapper.getAttributeTypesTable());
        joinInformation4.setMainTableAlias(attributeTypesTableAlias);
        joinInformation4.setMainTableIdField(entityMapper.getAttributeTypesTableDataTypeIdField());
        joinInformation4.setSubTable(TableNames.DATA_TYPES_TABLE);
        joinInformation4.setSubTableAlias(aliasFactory.createAlias());
        joinInformation4.setSubTableIdField(ColumnNames.ID_COLUMN);
        result.put(entityMapper.getAttributeTypesTable(), joinInformation4);

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

}
