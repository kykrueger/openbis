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
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;

import java.util.List;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;

public class NumberFieldSearchCriteriaTranslator implements IConditionTranslator<NumberFieldSearchCriteria>
{
    @Override
    public JoinInformation getJoinInformation(final NumberFieldSearchCriteria criterion, final EntityMapper entityMapper)
    {
        return null;
    }

    @Override
    public void translate(final NumberFieldSearchCriteria criterion, final EntityMapper entityMapper, final List<Object> args,
            final StringBuilder sqlBuilder)
    {
        switch (criterion.getFieldType()) {
            case ATTRIBUTE:
            {
                final AbstractNumberValue value = criterion.getFieldValue();

                sqlBuilder.append(Translator.getAlias(0)).append(PERIOD).append(criterion.getFieldName()).append(SP);
                TranslatorUtils.appendNumberComparatorOp(value, sqlBuilder);
                sqlBuilder.append(NL);
                args.add(value.getValue());
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
