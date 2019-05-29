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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringStartsWithValue;

import java.util.List;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.BARS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.IS_NOT_NULL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.LIKE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.PERCENT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.SP;

public class StringFieldConditionTranslator extends FieldSearchCriteriaTranslator<StringFieldSearchCriteria>
{

    protected void appendValueComparator(final Object fieldValue, final List<Object> args, final StringBuilder sqlBuilder)
    {
        final Class fieldValueClass = (fieldValue != null) ? fieldValue.getClass() : null;
        if (fieldValueClass == null)
        {
            sqlBuilder.append(SP).append(IS_NOT_NULL).append(NL);
        } else if (fieldValueClass == StringEqualToValue.class)
        {
            sqlBuilder.append(EQ).append(QU).append(NL);
        } else if (fieldValueClass == StringContainsValue.class)
        {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(PERCENT).append(SP).append(BARS).append(SP).
                    append(QU).append(SP).append(BARS).append(SP).append(PERCENT).append(NL);
        } else if(fieldValueClass == StringStartsWithValue.class) {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(QU).append(SP).append(BARS).append(SP).append(PERCENT).append(NL);
        } else if(fieldValueClass == StringEndsWithValue.class) {
            sqlBuilder.append(SP).append(LIKE).append(SP).append(PERCENT).append(SP).append(BARS).append(SP).append(QU).append(NL);
        } else
        {
            throw new IllegalArgumentException("Unsupported field value: " + fieldValueClass.getSimpleName());
        }

        if (fieldValueClass != null)
        {
            args.add(((AbstractStringValue) fieldValue).getValue());
        }
    }

}
