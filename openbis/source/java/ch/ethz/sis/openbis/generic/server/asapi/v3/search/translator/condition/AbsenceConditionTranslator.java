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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.NoExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.NoProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.List;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.IS_NULL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.Translator.SP;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_SAMPLE_COLUMN;

public class AbsenceConditionTranslator implements IConditionTranslator<ISearchCriteria>
{
    @Override
    public void translate(final ISearchCriteria criterion, final EntityKind entityKind,
            final List<Object> args,
            final StringBuilder sqlBuilder)
    {
        if (criterion instanceof NoSampleSearchCriteria)
        {
            sqlBuilder.append(PART_OF_SAMPLE_COLUMN).append(SP).append(IS_NULL);
        } else if (criterion instanceof NoExperimentSearchCriteria)
        {
            sqlBuilder.append(ColumnNames.EXPERIMENT_COLUMN).append(SP).append(IS_NULL);
        } else if (criterion instanceof NoProjectSearchCriteria)
        {
            sqlBuilder.append(ColumnNames.PROJECT_COLUMN).append(SP).append(IS_NULL);
        } else if (criterion instanceof NoSpaceSearchCriteria)
        {
            sqlBuilder.append(ColumnNames.SPACE_COLUMN).append(SP).append(IS_NULL);
        } else
        {
            throw new IllegalArgumentException("Unsupported criterion type: " + criterion.getClass().getSimpleName());
        }
    }

}
