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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.project;

import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.datastoreserver.systemtests.authorization.predicate.CommonPredicateSystemTest;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSessionProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.systemtest.authorization.predicate.project.ProjectPredicateTestService;

/**
 * @author pkupczyk
 */
public class ProjectIdentifierPredicateSystemTest extends CommonPredicateSystemTest<ProjectIdentifier>
{

    @Override
    protected ProjectIdentifier createNonexistentObject()
    {
        return new ProjectIdentifier("IDONTEXIST", "IDONTEXIST");
    }

    @Override
    protected ProjectIdentifier createObject(SpacePE spacePE, ProjectPE projectPE)
    {
        return new ProjectIdentifier(spacePE.getCode(), projectPE.getCode());
    }

    @Override
    protected void evaluateObjects(IAuthSessionProvider sessionProvider, List<ProjectIdentifier> objects)
    {
        getBean(ProjectPredicateTestService.class).testProjectIdentifierPredicate(sessionProvider, objects.get(0));
    }

    @Override
    protected void assertWithNull(PersonPE person, Throwable t)
    {
        assertException(t, UserFailureException.class, "No project specified.");
    }

    @Override
    protected void assertWithNonexistentObjectForInstanceUser(PersonPE person, Throwable t)
    {
        assertNoException(t);
    }

}