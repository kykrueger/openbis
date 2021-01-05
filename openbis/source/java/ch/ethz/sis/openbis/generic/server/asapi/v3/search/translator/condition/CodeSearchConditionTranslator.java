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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.FullEntityIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.SAMPLE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.TAG;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PERSONS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SPACES_TABLE;

public class CodeSearchConditionTranslator implements IConditionTranslator<StringFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final StringFieldSearchCriteria criterion,
            final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final StringFieldSearchCriteria criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final AbstractStringValue value = criterion.getFieldValue();
                if (value != null && value.getValue() != null)
                {
                    final String stringValue = value.getValue();
                    if (tableMapper == SAMPLE)
                    {
                        // Building the following query part
                        // ('=' is used instead of 'LIKE' when there are no wildcards):
                        // CASE
                        //  WHEN t0.samp_id_part_of IS NULL THEN code LIKE ?
                        //  ELSE substr(t0.sample_identifier, length(t0.sample_identifier)
                        //          - strpos(reverse(t0.sample_identifier), '/') + 2) LIKE ?
                        // END

                        sqlBuilder.append(CASE).append(NL)
                                .append(SP).append(SP).append(WHEN).append(SP)
                                .append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD)
                                .append(PART_OF_SAMPLE_COLUMN).append(SP).append(IS_NULL).append(SP)
                                .append(THEN).append(SP).append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS)
                                .append(PERIOD).append(CODE_COLUMN);
                        TranslatorUtils.appendStringComparatorOp(value.getClass(), stringValue, sqlBuilder, args);
                        sqlBuilder.append(NL).append(SP).append(SP).append(ELSE).append(SP)
                                .append(SUBSTR).append(LP).append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS)
                                .append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN).append(COMMA).append(SP)
                                .append(LENGTH).append(LP).append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS)
                                .append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN).append(RP).append(SP)
                                .append(MINUS).append(SP)
                                .append(STRPOS).append(LP)
                                .append(REVERSE).append(LP)
                                .append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS)
                                .append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN)
                                .append(RP).append(COMMA).append(SP).append(SQ).append('/')
                                .append(SQ)
                                .append(RP).append(SP).append(PLUS).append(SP).append(2)
                                .append(RP);
                        TranslatorUtils.appendStringComparatorOp(value.getClass(), stringValue, sqlBuilder, args);
                        sqlBuilder.append(NL).append(END);
                    } else
                    {
                        final String column = (tableMapper == TAG) ? NAME_COLUMN : CODE_COLUMN;
                        sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD)
                                .append(column).append(SP);
                        TranslatorUtils.appendStringComparatorOp(value.getClass(), stringValue.toUpperCase(),
                                sqlBuilder, args);
                    }
                } else
                {
                    sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN)
                            .append(SP).append(IS_NOT_NULL);
                }
                break;
            }

            case PROPERTY:
            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

}
