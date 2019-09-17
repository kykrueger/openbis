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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;

public class StringFieldSearchCriteriaTranslator implements IConditionTranslator<StringFieldSearchCriteria>
{
    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final StringFieldSearchCriteria criterion, final EntityMapper entityMapper,
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
                final Map<String, JoinInformation> result = new LinkedHashMap<>();
                final String valuesTableAlias = aliasFactory.createAlias();
                final String entityTypesAttributeTypesTableAlias = aliasFactory.createAlias();

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
                joinInformation2.setMainTableIdField(entityMapper.getValuesTableEntityIdField());
                joinInformation2.setSubTable(entityMapper.getEntityTypesAttributeTypesTable());
                joinInformation2.setSubTableAlias(entityTypesAttributeTypesTableAlias);
                joinInformation2.setSubTableIdField(entityMapper.getEntityTypesAttributeTypesTableIdField());
                result.put(entityMapper.getValuesTable(), joinInformation2);

                final JoinInformation joinInformation3 = new JoinInformation();
                joinInformation3.setMainTable(entityMapper.getEntityTypesAttributeTypesTable());
                joinInformation3.setMainTableAlias(entityTypesAttributeTypesTableAlias);
                joinInformation3.setMainTableIdField(entityMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField());
                joinInformation3.setSubTable(entityMapper.getAttributeTypesTable());
                joinInformation3.setSubTableAlias(aliasFactory.createAlias());
                joinInformation3.setSubTableIdField(entityMapper.getAttributeTypesTableIdField());
                result.put(entityMapper.getEntityTypesAttributeTypesTable(), joinInformation3);

                return result;
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void translate(final StringFieldSearchCriteria criterion, final EntityMapper entityMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<Object, Map<String, JoinInformation>> aliases)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final AbstractStringValue value = criterion.getFieldValue();
                final String criterionFieldName = criterion.getFieldName();
                final String fieldName = Attributes.ATTRIBUTE_ID_TO_COLUMN_NAME.getOrDefault(criterionFieldName, criterionFieldName);

                sqlBuilder.append(Translator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName);
                TranslatorUtils.appendStringComparatorOp(value, sqlBuilder);
                args.add(value.getValue());
                sqlBuilder.append(NL);
                break;
            }

            case PROPERTY:
            {
                final AbstractStringValue value = criterion.getFieldValue();
                final String propertyName = criterion.getFieldName();

                final Map<String, JoinInformation> joinInformationMap = aliases.get(criterion);

                sqlBuilder.append(joinInformationMap.get(entityMapper.getEntityTypesAttributeTypesTable()).getSubTableAlias())
                        .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(propertyName);

                if (value.getClass() != AnyStringValue.class)
                {
                    sqlBuilder.append(SP).append(AND).append(SP).append(SP)
                            .append(joinInformationMap.get(entityMapper.getEntitiesTable()).getSubTableAlias())
                            .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(SP);
                    TranslatorUtils.appendStringComparatorOp(value, sqlBuilder);
                    args.add(value.getValue());
                }
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
