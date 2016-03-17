/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.vocabulary;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.Capability;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;

/**
 * @author pkupczyk
 */
@Component
public class CreateVocabularyTermAuthorizationExecutor implements ICreateVocabularyTermAuthorizationExecutor
{

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_POWER_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_OFFICIAL_VOCABULARY_TERM")
    public void checkCreateOfficialTerm(IOperationContext context)
    {
        // do nothing - authorization is handled by the annotations
    }

    @Override
    @RolesAllowed({ RoleWithHierarchy.SPACE_USER, RoleWithHierarchy.SPACE_ETL_SERVER })
    @Capability("CREATE_UNOFFICIAL_VOCABULARY_TERM")
    public void checkCreateUnofficialTerm(IOperationContext context)
    {
        // do nothing - authorization is handled by the annotations
    }

}
