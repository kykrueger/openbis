/*
 * Copyright 2017 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.get;

import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.get.GetObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@JsonObject("as.dto.roleassignment.get.GetRoleAssignmentsOperation")
public class GetRoleAssignmentsOperation extends GetObjectsOperation<IRoleAssignmentId, RoleAssignmentFetchOptions>
{

    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
    private GetRoleAssignmentsOperation()
    {
    }

    public GetRoleAssignmentsOperation(List<? extends IRoleAssignmentId> ids, RoleAssignmentFetchOptions fetchOptions)
    {
        super(ids, fetchOptions);
    }

}
