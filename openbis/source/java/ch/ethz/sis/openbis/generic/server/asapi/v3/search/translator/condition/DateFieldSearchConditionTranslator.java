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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.MODIFICATION_TIMESTAMP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.REGISTRATION_TIMESTAMP_COLUMN;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

public class DateFieldSearchConditionTranslator implements IConditionTranslator<DateFieldSearchCriteria>
{

    private static final String TIMESTAMP_DATA_TYPE_CODE = "TIMESTAMP";

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final DateFieldSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                return null;
            }

            case PROPERTY:
            {
                return TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory, JoinType.INNER);
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void translate(final DateFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyName)
    {
        final IDate value = criterion.getFieldValue();
        final ITimeZone timeZone = criterion.getTimeZone();
        final boolean bareDateValue = value instanceof AbstractDateValue &&
                TranslatorUtils.isDateWithoutTime(((AbstractDateValue) value).getValue());

        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String fieldName = criterion.getFieldName();

                if (bareDateValue)
                {
                    sqlBuilder.append(LP);
                }

                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD);
                if (criterion instanceof RegistrationDateSearchCriteria)
                {
                    sqlBuilder.append(REGISTRATION_TIMESTAMP_COLUMN);
                } else if (criterion instanceof ModificationDateSearchCriteria)
                {
                    sqlBuilder.append(MODIFICATION_TIMESTAMP_COLUMN);
                } else
                {
                    sqlBuilder.append(fieldName);
                }
                sqlBuilder.append(SP);
                TranslatorUtils.appendTimeZoneConversion(value, sqlBuilder, timeZone);

                if (bareDateValue)
                {
                    sqlBuilder.append(RP).append(DOUBLE_COLON).append(DATE);
                }

                sqlBuilder.append(SP);

                TranslatorUtils.appendDateComparatorOp(value, sqlBuilder, args);
                break;
            }

            case PROPERTY:
            {
                final String propertyName = TranslatorUtils.normalisePropertyName(criterion.getFieldName());
                final boolean internalProperty = TranslatorUtils.isPropertyInternal(criterion.getFieldName());
                final String entityTypesSubTableAlias = aliases.get(tableMapper.getAttributeTypesTable()).getSubTableAlias();

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);

                TranslatorUtils.appendInternalExternalConstraint(sqlBuilder, args, entityTypesSubTableAlias, internalProperty);

                sqlBuilder.append(SP).append(aliases.get(tableMapper.getAttributeTypesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(propertyName);

                sqlBuilder.append(SP).append(AND);

                sqlBuilder.append(SP).append(aliases.get(TableNames.DATA_TYPES_TABLE).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(TIMESTAMP_DATA_TYPE_CODE);

                sqlBuilder.append(SP).append(THEN).append(SP);

                if (bareDateValue)
                {
                    sqlBuilder.append(LP);
                }

                sqlBuilder.append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(DOUBLE_COLON).append(TIMESTAMPTZ)
                        .append(SP);
                TranslatorUtils.appendTimeZoneConversion(value, sqlBuilder, timeZone);

                if (bareDateValue)
                {
                    sqlBuilder.append(RP).append(DOUBLE_COLON).append(DATE).append(SP);
                }

                TranslatorUtils.appendDateComparatorOp(value, sqlBuilder, args);

                sqlBuilder.append(SP).append(ELSE).append(SP).append(false).append(SP).append(END);

                break;
            }
            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

}
