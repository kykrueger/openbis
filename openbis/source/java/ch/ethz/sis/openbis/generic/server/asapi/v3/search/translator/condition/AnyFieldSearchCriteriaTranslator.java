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

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.BOOLEAN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.FLOAT4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.FLOAT8;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.INT2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.INT4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.INT8;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.TIMESTAMP_WITH_TZ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.VARCHAR;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DOUBLE_COLON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FALSE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;

public class AnyFieldSearchCriteriaTranslator implements IConditionTranslator<AnyFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final AnyFieldSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final AnyFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases,
            final Map<String, String> dataTypeByPropertyName)
    {
        switch (criterion.getFieldType())
        {
            case ANY_FIELD:
            {
                final String alias = CriteriaTranslator.MAIN_TABLE_ALIAS;
                final AbstractStringValue value = criterion.getFieldValue();
                final Map<String, PSQLTypes> fieldToSQLTypeMap = tableMapper.getFieldToSQLTypeMap();
                final String stringValue = stripQuotationMarks(value.getValue().trim());
                final Set<PSQLTypes> compatiblePSQLTypesForValue = findCompatibleSqlTypesForValue(stringValue);

                final AtomicBoolean first = new AtomicBoolean(true);
                fieldToSQLTypeMap.forEach((fieldName, fieldSQLType) ->
                {
                    final boolean equalsToComparison = (value.getClass() == StringEqualToValue.class);
                    final boolean includeColumn = compatiblePSQLTypesForValue.contains(fieldSQLType);

                    if (!equalsToComparison || includeColumn)
                    {
                        if (first.get())
                        {
                            first.set(false);
                        } else
                        {
                            sqlBuilder.append(SP).append(SQLLexemes.OR).append(SP);
                        }
                    }

                    if (equalsToComparison)
                    {
                        if (includeColumn)
                        {
                            sqlBuilder.append(alias).append(PERIOD).append(fieldName).append(EQ).append(QU).append(DOUBLE_COLON).
                                    append(fieldSQLType.toString());
                            args.add(stringValue);
                        }
                    } else
                    {
                        sqlBuilder.append(alias).append(PERIOD).append(fieldName).append(DOUBLE_COLON).append(VARCHAR);
                        TranslatorUtils.appendStringComparatorOp(value, sqlBuilder, args);
                    }
                });

                if (args.isEmpty())
                {
                    // When there are no columns selected (no values added), then the query should return nothing
                    sqlBuilder.append(FALSE);
                }

                sqlBuilder.append(NL);

                break;
            }

            case PROPERTY:
            case ANY_PROPERTY:
            case ATTRIBUTE:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

    private String stripQuotationMarks(final String value)
    {
        if (value.startsWith("\"") && value.endsWith("\""))
        {
            return value.substring(1, value.length() - 1);
        } else
        {
            return value;
        }
    }

    private Set<PSQLTypes> findCompatibleSqlTypesForValue(final String value)
    {
        final SimplePropertyValidator validator = new SimplePropertyValidator();
        try
        {
            validator.validatePropertyValue(DataTypeCode.TIMESTAMP, value);
            return EnumSet.of(TIMESTAMP_WITH_TZ);
        } catch (UserFailureException e1)
        {
            try
            {
                validator.validatePropertyValue(DataTypeCode.BOOLEAN, value);
                return EnumSet.of(BOOLEAN);
            } catch (UserFailureException e2)
            {
                try
                {
                    validator.validatePropertyValue(DataTypeCode.INTEGER, value);
                    return EnumSet.of(INT8, INT4, INT2, FLOAT4, FLOAT8);
                } catch (UserFailureException e3)
                {
                    try
                    {
                        validator.validatePropertyValue(DataTypeCode.REAL, value);
                        return EnumSet.of(FLOAT4, FLOAT8);
                    } catch (UserFailureException e4)
                    {
                        validator.validatePropertyValue(DataTypeCode.VARCHAR, value);
                        return EnumSet.of(VARCHAR);
                    }
                }
            }
        }
    }

}
