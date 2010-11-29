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

package ch.systemsx.cisd.openbis.generic.shared.authorization.validator;

import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * A {@link IValidator} implementation suitable for {@link Space}.
 * 
 * @author Christian Ribeaud
 */
public final class SpaceValidator extends AbstractValidator<Space>
{
    private final IValidator<DatabaseInstance> databaseInstanceValidator;

    public SpaceValidator()
    {
        databaseInstanceValidator = new DatabaseInstanceValidator();
    }

    //
    // AbstractValidator
    //

    @Override
    public final boolean doValidation(final PersonPE person, final Space value)
    {
        final Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            final SpacePE group = roleAssignment.getSpace();
            if (group != null && group.getCode().equals(value.getCode())
                    && group.getDatabaseInstance().getUuid().equals(value.getInstance().getUuid()))
            {
                return true;
            }
        }
        return databaseInstanceValidator.isValid(person, value.getInstance());
    }
}
