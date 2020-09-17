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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractNumberValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;

public class NumberFieldSearchConditionTranslator implements IConditionTranslator<NumberFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final NumberFieldSearchCriteria criterion, final TableMapper tableMapper,
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
                return TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory);
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void translate(final NumberFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyName)
    {
        switch (criterion.getFieldType()) {
            case ATTRIBUTE:
            {
                final String criterionFieldName = criterion.getFieldName();
                final String columnName = AttributesMapper.getColumnName(criterionFieldName, tableMapper.getValuesTable(), criterion.getFieldName());
                final AbstractNumberValue value = criterion.getFieldValue();

                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP);
                TranslatorUtils.appendNumberComparatorOp(value, sqlBuilder);
                sqlBuilder.append(NL);
                args.add(value.getValue());
                break;
            }

            case PROPERTY:
            {
                final AbstractNumberValue value = criterion.getFieldValue();
                final String propertyName = TranslatorUtils.normalisePropertyName(criterion.getFieldName());
                final boolean internalProperty = TranslatorUtils.isPropertyInternal(criterion.getFieldName());
                translateNumberProperty(tableMapper, args, sqlBuilder, aliases, value, propertyName, internalProperty);
                break;
            }

            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    static void translateNumberProperty(final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final AbstractNumberValue value,
            final String propertyName, final boolean internalProperty)
    {
        final String entityTypesSubTableAlias = aliases.get(tableMapper.getAttributeTypesTable()).getSubTableAlias();

        if (value != null)
        {
            sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
        }

        TranslatorUtils.appendInternalExternalConstraint(sqlBuilder, args, entityTypesSubTableAlias, internalProperty);

        sqlBuilder.append(SP).append(aliases.get(tableMapper.getAttributeTypesTable()).getSubTableAlias())
                .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
        args.add(propertyName);

        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        sqlBuilder.append(aliases.get(TableNames.DATA_TYPES_TABLE).getSubTableAlias())
                .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
        args.add(DataTypeCode.INTEGER.toString());

        sqlBuilder.append(SP).append(OR).append(SP);

        sqlBuilder.append(aliases.get(TableNames.DATA_TYPES_TABLE).getSubTableAlias())
                .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
        args.add(DataTypeCode.REAL.toString());

        sqlBuilder.append(RP);

        if (value != null)
        {
            sqlBuilder.append(SP).append(THEN).append(SP);
            sqlBuilder.append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias())
                    .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(DOUBLE_COLON)
                    .append(PSQLTypes.NUMERIC.toString()).append(SP);
            TranslatorUtils.appendNumberComparatorOp(value, sqlBuilder);
            args.add(value.getValue());

            sqlBuilder.append(SP).append(ELSE).append(SP).append(false).append(SP).append(END);
        }
    }

}
