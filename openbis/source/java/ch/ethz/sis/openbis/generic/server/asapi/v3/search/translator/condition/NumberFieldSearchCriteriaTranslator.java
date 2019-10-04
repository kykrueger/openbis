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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractNumberValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.CASE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DOUBLE_COLON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ELSE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.END;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NUMERIC;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.OR;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.THEN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHEN;

public class NumberFieldSearchCriteriaTranslator implements IConditionTranslator<NumberFieldSearchCriteria>
{

    private static final String INTEGER_DATA_TYPE_CODE = "INTEGER";

    private static final String REAL_DATA_TYPE_CODE = "REAL";

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
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases)
    {
        switch (criterion.getFieldType()) {
            case ATTRIBUTE:
            {
                final AbstractNumberValue value = criterion.getFieldValue();

                sqlBuilder.append(Translator.MAIN_TABLE_ALIAS).append(PERIOD).append(criterion.getFieldName()).append(SP);
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
                final Map<String, JoinInformation> joinInformationMap = aliases.get(criterion);
                final String entityTypesSubTableAlias = joinInformationMap.get(tableMapper.getEntityTypesAttributeTypesTable()).getSubTableAlias();

                sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);

                TranslatorUtils.appendInternalExternalConstraint(sqlBuilder, args, entityTypesSubTableAlias, internalProperty);

                sqlBuilder.append(SP).append(joinInformationMap.get(tableMapper.getEntityTypesAttributeTypesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(propertyName);

                sqlBuilder.append(SP).append(AND).append(SP).append(LP);

                sqlBuilder.append(joinInformationMap.get(tableMapper.getAttributeTypesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(INTEGER_DATA_TYPE_CODE);

                sqlBuilder.append(SP).append(OR).append(SP);

                sqlBuilder.append(joinInformationMap.get(tableMapper.getAttributeTypesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(REAL_DATA_TYPE_CODE);

                sqlBuilder.append(RP);

                sqlBuilder.append(SP).append(THEN).append(SP);
                sqlBuilder.append(joinInformationMap.get(tableMapper.getEntitiesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(DOUBLE_COLON).append(NUMERIC).append(SP);
                TranslatorUtils.appendNumberComparatorOp(value, sqlBuilder);
                args.add(value.getValue());

                sqlBuilder.append(SP).append(ELSE).append(SP).append(false).append(SP).append(END);
                break;
            }

            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

}
