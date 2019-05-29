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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.SP;

public class CompositeSearchCriteriaConditionTranslator implements IConditionTranslator<AbstractCompositeSearchCriteria>
{
    @Override
    public void translate(final AbstractCompositeSearchCriteria criterion, final List<Object> args,
            final StringBuilder sqlBuilder)
    {
        final Collection<ISearchCriteria> subcriteria = criterion.getCriteria();
        final String operator = criterion.getOperator().toString();

        final AtomicBoolean first = new AtomicBoolean(true);
        subcriteria.forEach((subcriterion) ->
        {
            if (first.get())
            {
                sqlBuilder.append(SP).append(operator).append(SP);
                first.set(false);
            }

            @SuppressWarnings("unchecked")
            final IConditionTranslator<ISearchCriteria> conditionTranslator =
                    (IConditionTranslator<ISearchCriteria>) Translator.CRITERIA_TO_CONDITION_TRANSLATOR_MAP.get(subcriterion.getClass());
            if (conditionTranslator != null)
            {
                conditionTranslator.translate(subcriterion, args, sqlBuilder);
            } else
            {
                throw new IllegalArgumentException("Unsupported criterion type: " + subcriterion.getClass().getSimpleName());
            }
        });
    }

}
