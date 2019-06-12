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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.TO_DELETE;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.AbstractConditionTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.Attributes;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.List;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.INNER_JOIN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;

public abstract class AbstractFieldSearchCriteriaTranslator<T extends AbstractFieldSearchCriteria<?>> extends AbstractConditionTranslator<T>
{

    @Override
    public void translate(final T criterion, final EntityMapper entityMapper, final List<Object> args,
            final StringBuilder sqlBuilder)
    {
        final SearchFieldType searchFieldType = criterion.getFieldType();
        final String fieldName = criterion.getFieldName();
        final Object fieldValue = criterion.getFieldValue();

        switch (searchFieldType)
        {
            case ATTRIBUTE:
            {
                sqlBuilder.append(Attributes.ATTRIBUTE_ID_TO_COLUMN_NAME.get(fieldName)).append(SP);
                appendValueComparator(fieldValue, args, sqlBuilder);
                break;
            }
            case PROPERTY:
            {
                if (entityMapper == null)
                {
                    throw new IllegalArgumentException();
                }

                args.add(fieldName);

                final String valuesTable = entityMapper.getValuesTable();
                final String entityTypesAttributeTypesTable = entityMapper.getEntityTypesAttributeTypesTable();
                final String attributeTypesTable = entityMapper.getAttributeTypesTable();
                sqlBuilder.append(ColumnNames.ID_COLUMN).append(SP).append(IN).append(SP).append(LP).append(NL).
                        append(SELECT).append(SP).append(entityMapper.getValuesTableEntityIdField()).append(NL).
                        append(FROM).append(SP).append(valuesTable).append(NL).
                        append(INNER_JOIN).append(SP).append(entityTypesAttributeTypesTable).append(SP).append(ON).append(SP).
                        append(valuesTable).append(PERIOD).append(entityMapper.getValuesTableEntityTypeAttributeTypeIdField()).append(EQ).
                        append(entityTypesAttributeTypesTable).append(PERIOD).append(entityMapper.getEntityTypesAttributeTypesTableIdField()).
                        append(NL).
                        append(INNER_JOIN).append(SP).append(attributeTypesTable).append(SP).append(ON).append(SP).
                        append(entityTypesAttributeTypesTable).append(PERIOD).
                        append(entityMapper.getEntityTypesAttributeTypesTableAttributeTypeIdField()).append(EQ).
                        append(attributeTypesTable).append(PERIOD).append(entityMapper.getAttributeTypesTableIdField()).append(NL).
                        append(WHERE).append(SP).append(attributeTypesTable).append(PERIOD).append(ColumnNames.CODE_COLUMN).append(EQ).append(QU).
                        append(SP).append(AND).append(NL).
                        append(valuesTable).append(PERIOD).append(ColumnNames.VALUE_COLUMN);
                appendValueComparator(fieldValue, args, sqlBuilder);
                sqlBuilder.append(NL).append(RP);
                break;
            }
            case ANY_PROPERTY:
            {
                if (entityMapper == null)
                {
                    throw new IllegalArgumentException();
                }

                final String valuesTable = entityMapper.getValuesTable();
                sqlBuilder.append(ColumnNames.ID_COLUMN).append(SP).append(IN).append(SP).append(LP).append(NL).
                        append(SELECT).append(SP).append(entityMapper.getValuesTableEntityIdField()).append(NL).
                        append(FROM).append(SP).append(valuesTable).append(NL).
                        append(WHERE).append(SP).append(ColumnNames.VALUE_COLUMN);
                appendValueComparator(fieldValue, args, sqlBuilder);
                sqlBuilder.append(RP);
                args.add(fieldValue);
                break;
            }
            case ANY_FIELD:
            {
                break;
            }
        }
    }

    protected abstract void appendValueComparator(final Object fieldValue, final List<Object> args, final StringBuilder sqlBuilder);

}
