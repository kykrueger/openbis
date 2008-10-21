/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentTypePE;

/**
 * @author Tomasz Pylak
 */
public class ExperimentTranslator
{

    public static Experiment translate(ExperimentPE experiment)
    {
        if (experiment == null)
        {
            return null;
        }
        Experiment result = new Experiment();
        result.setCode(experiment.getCode());
        result.setExperimentType(translate(experiment.getExperimentType()));
        result.setProject(ProjectTranslator.translate(experiment.getProject()));
        return result;
    }

    private static ExperimentType translate(ExperimentTypePE experimentType)
    {
        ExperimentType result = new ExperimentType();
        result.setCode(experimentType.getCode());
        result.setDescription(experimentType.getDescription());
        result.setDatabaseInstance(DatabaseInstanceTranslator.translate(experimentType
                .getDatabaseInstance()));
        return result;
    }

}
