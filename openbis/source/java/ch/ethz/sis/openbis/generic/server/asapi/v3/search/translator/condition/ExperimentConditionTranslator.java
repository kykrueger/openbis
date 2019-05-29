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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;

import java.util.List;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.IS_NOT_NULL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.SP;

public class ExperimentConditionTranslator implements IConditionTranslator<ExperimentSearchCriteria>
{

    @Override
    public void translate(final ExperimentSearchCriteria criterion, final List<Object> args, final SearchOperator operator,
            final StringBuilder sqlBuilder)
    {
        criterion.getCriteria().forEach(subcriterion ->
                {
                    if (subcriterion instanceof AbstractFieldSearchCriteria<?>)
                    {
                        final AbstractFieldSearchCriteria<?> fieldSearchSubcriterion = (AbstractFieldSearchCriteria<?>) subcriterion;
                        final Object fieldName = fieldSearchSubcriterion.getFieldName();
                        final Object fieldValue = fieldSearchSubcriterion.getFieldValue();

                        if (fieldValue == null)
                        {
                            sqlBuilder.append(fieldName).append(SP).append(IS_NOT_NULL);
                        } else
                        {

                        }
                    }
                });
    }

}
