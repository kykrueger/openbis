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

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyPropertySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.TRUE;

public class AnyPropertySearchConditionTranslator implements IConditionTranslator<AnyPropertySearchCriteria>
{

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
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases,
            final Map<String, String> dataTypeByPropertyName)
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
                    TranslatorUtils.appendStringComparatorOp(value.getClass(), TranslatorUtils.stripQuotationMarks(value.getValue()), sqlBuilder,
                            args);
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

}
