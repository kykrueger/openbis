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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes.BOOLEAN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes.FLOAT4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes.FLOAT8;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes.INT2;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes.INT4;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes.INT8;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes.TIMESTAMP_WITH_TZ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.SQLTypes.VARCHAR;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.TRUE;

public class AnyPropertySearchCriteriaTranslator implements IConditionTranslator<AnyPropertySearchCriteria>
{

    private final AtomicBoolean first = new AtomicBoolean();

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final AnyPropertySearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        if (criterion.getFieldType() == SearchFieldType.ANY_PROPERTY)
        {
            return TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory);
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void translate(final AnyPropertySearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases)
    {
        switch (criterion.getFieldType())
        {
            case ANY_PROPERTY:
            {
                final AbstractStringValue value = criterion.getFieldValue();
                final Map<String, JoinInformation> joinInformationMap = aliases.get(criterion);

                if (value.getClass() != AnyStringValue.class)
                {
                    sqlBuilder.append(SP).append(joinInformationMap.get(tableMapper.getEntitiesTable()).getSubTableAlias())
                            .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(SP);
                    TranslatorUtils.appendStringComparatorOp(value, sqlBuilder, args);
                } else
                {
                    sqlBuilder.append(TRUE);
                }
                break;
            }

            case ANY_FIELD:
            case PROPERTY:
            case ATTRIBUTE:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

    private Set<SQLTypes> findCompatibleSqlTypesForValue(final String value)
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
