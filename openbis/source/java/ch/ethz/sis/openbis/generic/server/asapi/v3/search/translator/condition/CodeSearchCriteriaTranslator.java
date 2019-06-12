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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.List;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.BARS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LIKE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERCENT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;

public class CodeSearchCriteriaTranslator implements IConditionTranslator<CodeSearchCriteria>
{
    @Override
    public JoinInformation getJoinInformation(final CodeSearchCriteria criterion, final EntityMapper entityMapper)
    {
        return null;
    }

    @Override
    public void translate(final CodeSearchCriteria criterion, final EntityMapper entityMapper, final List args, final StringBuilder sqlBuilder)
    {
        final AbstractStringValue value = criterion.getFieldValue();

        sqlBuilder.append(Translator.getAlias(0)).append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP);
        appendStringComparatorOp(value, sqlBuilder);
        sqlBuilder.append(NL);
    }

    private void appendStringComparatorOp(final AbstractStringValue value, final StringBuilder sqlBuilder) {
        if (value.getClass() == StringEqualToValue.class) {
            sqlBuilder.append(EQ).append(SP).append(SQ).append(value.getValue()).append(SQ);
        } else if (value.getClass() == StringStartsWithValue.class) {
            sqlBuilder.append(LIKE).append(SP).append(SQ).append(value.getValue()).append(SQ).append(SP).append(BARS).append(SP).append(PERCENT);
        } else if (value.getClass() == StringEndsWithValue.class) {
            sqlBuilder.append(LIKE).append(SP).append(PERCENT).append(SP).append(BARS).append(SP).append(SQ).append(value.getValue()).append(SQ);
        } else if (value.getClass() == StringContainsValue.class) {
            sqlBuilder.append(LIKE).append(SP).append(PERCENT).append(SP).append(BARS).append(SP).append(SQ).append(value.getValue()).append(SQ).
                    append(SP).append(BARS).append(SP).append(PERCENT);
        } else {
            throw new IllegalArgumentException("Unsupported AbstractStringValue type: " + value.getClass().getSimpleName());
        }
    }

}
