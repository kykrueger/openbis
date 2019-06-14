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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AnyFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.EntityMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;

public class AnyFieldSearchCriteriaTranslator implements IConditionTranslator<AnyFieldSearchCriteria>
{
    @Override
    public JoinInformation getJoinInformation(final AnyFieldSearchCriteria criterion, final EntityMapper entityMapper)
    {
        return null;
    }

    @Override
    public void translate(final AnyFieldSearchCriteria criterion, final EntityMapper entityMapper, final List<Object> args,
            final StringBuilder sqlBuilder)
    {
        final String alias = Translator.getAlias(0);
        final AbstractStringValue value = criterion.getFieldValue();
        final String[] criterionFieldNames = entityMapper.getAllFields();


        final AtomicBoolean first = new AtomicBoolean(true);
        Arrays.stream(criterionFieldNames).forEach(fieldName ->
        {
            if (first.get())
            {
                first.set(false);
            } else
            {
                sqlBuilder.append(SP).append(SQLLexemes.OR).append(SP);
            }

            sqlBuilder.append(alias).append(PERIOD).append(fieldName);
            StringFieldSearchCriteriaTranslator.appendStringComparatorOp(criterion.getFieldValue(), sqlBuilder);

            args.add(value.getValue());
        });

        sqlBuilder.append(NL);
    }

}
