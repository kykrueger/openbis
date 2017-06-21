/*
 * Copyright 2017 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.experiment;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.validator.CommonValidatorSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.authorization.validator.experiment.ExperimentValidatorTestService;

/**
 * @author pkupczyk
 */
public class ExperimentByIdentiferValidatorSystemTest extends CommonValidatorSystemTest<Experiment>
{

    @Override
    protected Experiment createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        ExperimentPE experimentPE = getExperiment(spacePE, projectPE);
        Experiment experiment = new Experiment();
        experiment.setIdentifier(experimentPE.getIdentifier());
        return experiment;
    }

    @Override
    protected Experiment validateObject(IAuthSessionProvider sessionProvider, Experiment object)
    {
        return getBean(ExperimentValidatorTestService.class).testExperimentByIdentifierValidator(sessionProvider, object);
    }

}