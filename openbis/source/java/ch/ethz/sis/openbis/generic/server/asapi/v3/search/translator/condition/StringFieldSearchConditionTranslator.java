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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.VARCHAR;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.CONTROLLED_VOCABULARY_TERM_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.MATERIALS_TABLE;

public class StringFieldSearchConditionTranslator implements IConditionTranslator<StringFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final StringFieldSearchCriteria criterion, final TableMapper tableMapper,
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
    public void translate(final StringFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final Map<String, String> dataTypeByPropertyName)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String criterionFieldName = criterion.getFieldName();
                final String columnName = AttributesMapper.getColumnName(criterionFieldName, tableMapper.getEntitiesTable(), criterionFieldName);
                final AbstractStringValue value = criterion.getFieldValue();
                normalizeValue(value, columnName);

                TranslatorUtils.translateStringComparison(SearchCriteriaTranslator.MAIN_TABLE_ALIAS, columnName, value, VARCHAR, sqlBuilder, args);
                break;
            }

            case PROPERTY:
            {
                final AbstractStringValue value = criterion.getFieldValue();
                final String stringValue = (value.getValue() != null) ? TranslatorUtils.stripQuotationMarks(value.getValue().trim()) : null;
                final String propertyName = TranslatorUtils.normalisePropertyName(criterion.getFieldName());
                final boolean internalProperty = TranslatorUtils.isPropertyInternal(criterion.getFieldName());
                final String entityTypesSubTableAlias = aliases.get(tableMapper.getAttributeTypesTable()).getSubTableAlias();

                if (value.getClass() != AnyStringValue.class)
                {
                    sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
                }

                TranslatorUtils.appendInternalExternalConstraint(sqlBuilder, args, entityTypesSubTableAlias, internalProperty);

                sqlBuilder.append(SP).append(entityTypesSubTableAlias).append(PERIOD).append(CODE_COLUMN).append(SP).append(EQ).
                        append(SP).append(QU);
                args.add(propertyName);

                if (value.getClass() != AnyStringValue.class)
                {
                    sqlBuilder.append(SP).append(THEN).append(SP);

                    final String casting = dataTypeByPropertyName.get(propertyName);
                    if (casting != null)
                    {
                        if (!(criterion.getFieldValue() instanceof StringEqualToValue))
                        {
                            if (casting.equals(DataTypeCode.INTEGER.toString())
                                    || casting.equals(DataTypeCode.REAL.toString()))
                            {
                                throw new UserFailureException("Can't be computed we suggest you use " +
                                        "withNumberProperty to see operators available");
                            }
                            if (casting.equals(DataTypeCode.TIMESTAMP.toString())
                                    || casting.equals(DataTypeCode.DATE.toString()))
                            {
                                throw new UserFailureException("Can't be computed we suggest you use " +
                                        "withDateProperty to see operators available");
                            }
                            if (casting.equals(DataTypeCode.BOOLEAN.toString())
                                    || casting.equals(DataTypeCode.MATERIAL.toString())
                                    || casting.equals(DataTypeCode.SAMPLE.toString()))
                            {
                                throw new UserFailureException("Can't be computed for " + casting + " type.");
                            }
                        }

                        sqlBuilder.append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias())
                                .append(PERIOD).append(VALUE_COLUMN);

                        final String lowerCaseCasting = casting.toLowerCase();
                        sqlBuilder.append(DOUBLE_COLON).append(lowerCaseCasting).append(SP).append(EQ).append(SP).append(QU);

                        final Object convertedValue = TranslatorUtils.convertStringToType(stringValue,
                                PSQLTypes.sqlTypeToJavaClass(lowerCaseCasting));
                        args.add(convertedValue);
                    } else
                    {
                        TranslatorUtils.translateStringComparison(aliases.get(tableMapper.getValuesTable()).getSubTableAlias(),
                                VALUE_COLUMN, value, null, sqlBuilder, args);
                    }
                    sqlBuilder.append(SP).append(OR).append(SP);
                    TranslatorUtils.translateStringComparison(aliases.get(CONTROLLED_VOCABULARY_TERM_TABLE).getSubTableAlias(),
                            CODE_COLUMN, value, null, sqlBuilder, args);

                    sqlBuilder.append(SP).append(OR).append(SP);
                    TranslatorUtils.translateStringComparison(aliases.get(MATERIALS_TABLE).getSubTableAlias(),
                            CODE_COLUMN, value, null, sqlBuilder, args);

                    final JoinInformation samplesPropertyTable = aliases.get(SAMPLE_PROP_COLUMN);
                    if (samplesPropertyTable != null)
                    {
                        sqlBuilder.append(SP).append(OR).append(SP);
                        TranslatorUtils.translateStringComparison(samplesPropertyTable.getSubTableAlias(),
                                CODE_COLUMN, value, null, sqlBuilder, args);

                        sqlBuilder.append(SP).append(OR).append(SP);
                        TranslatorUtils.translateStringComparison(samplesPropertyTable.getSubTableAlias(),
                                PERM_ID_COLUMN, value, null, sqlBuilder, args);

                        sqlBuilder.append(SP).append(OR).append(SP);
                        TranslatorUtils.translateStringComparison(samplesPropertyTable.getSubTableAlias(),
                                SAMPLE_IDENTIFIER_COLUMN, value, null, sqlBuilder, args);
                    }

                    sqlBuilder.append(SP).append(END);
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

    private static void normalizeValue(final AbstractStringValue value, final String columnName)
    {
        if (columnName.equals(CODE_COLUMN) && value.getValue().startsWith("/"))
        {
            value.setValue(value.getValue().substring(1));
        }
    }

}
