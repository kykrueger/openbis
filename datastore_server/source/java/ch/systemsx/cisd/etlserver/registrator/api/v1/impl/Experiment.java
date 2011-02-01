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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import ch.systemsx.cisd.etlserver.registrator.api.v1.IExperiment;

/**
 * Implementation of {@link IExperiment}.
 *
 * @author Franz-Josef Elmer
 */
class Experiment extends ExperimentImmutable implements IExperiment
{
    Experiment(String identifier, String permID)
    {
        super(new ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment());
        ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment experiment = getExperiment();
        experiment.setIdentifier(identifier);
        experiment.setPermId(permID);
    }

    @Override
    public boolean isExistingExperiment()
    {
        return false;
    }
    


}
