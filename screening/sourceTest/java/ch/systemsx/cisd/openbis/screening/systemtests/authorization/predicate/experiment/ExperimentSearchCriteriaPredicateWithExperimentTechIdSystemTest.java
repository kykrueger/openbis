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

package ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.experiment;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.SingleExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.screening.systemtests.authorization.predicate.CommonPredicateScreeningSystemTest;

/**
 * @author pkupczyk
 */
public class ExperimentSearchCriteriaPredicateWithExperimentTechIdSystemTest extends CommonPredicateScreeningSystemTest<ExperimentSearchCriteria>
{

    @Override
    protected ExperimentSearchCriteria createNonexistentObject()
    {
        return ExperimentSearchCriteria.createExperiment(new SingleExperimentSearchCriteria(-1L, "IDONTEXIST", "/IDONTEXIST/IDONTEXIST/IDONTEXIST"),
                false);
    }

    @Override
    protected ExperimentSearchCriteria createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        ExperimentPE experimentPE = getExperiment(spacePE, projectPE);
        return ExperimentSearchCriteria.createExperiment(
                new SingleExperimentSearchCriteria(experimentPE.getId(), experimentPE.getPermId(), experimentPE.getIdentifier()), false);
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider session, List<ExperimentSearchCriteria> objects)
    {
        getBean(ExperimentPredicateScreeningTestService.class).testExperimentSearchCriteriaPredicate(session, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

    @Override
    protected void assertWithNonexistentObject(PersonPE person, Throwable t)
    {
        assertUserFailureExceptionThatExperimentDoesNotExist(t);
    }

}