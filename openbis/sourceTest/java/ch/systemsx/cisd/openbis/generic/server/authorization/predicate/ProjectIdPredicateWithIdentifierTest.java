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

package ch.systemsx.cisd.openbis.generic.server.authorization.predicate;

import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.project.ProjectIdentifierId;
import ch.systemsx.cisd.openbis.generic.shared.authorization.IAuthorizationConfig;

/**
 * @author pkupczyk
 */
public class ProjectIdPredicateWithIdentifierTest extends ProjectAugmentedCodePredicateTest
{

    @Override
    protected Status evaluateObjects(List<String> objects, RoleWithIdentifier... roles)
    {
        ProjectIdPredicate predicate = new ProjectIdPredicate();
        predicate.init(provider);
        return predicate.evaluate(PERSON_PE, Arrays.asList(roles), objects.get(0) != null ? new ProjectIdentifierId(objects.get(0)) : null);
    }

    @Override
    protected void assertWithNull(IAuthorizationConfig config, Status result, Throwable t)
    {
        assertNull(result);
        assertException(t, UserFailureException.class, "No project id specified.");
    }

}
