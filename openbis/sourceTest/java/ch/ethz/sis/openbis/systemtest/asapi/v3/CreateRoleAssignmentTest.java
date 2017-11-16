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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.RoleAssignmentTechId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class CreateRoleAssignmentTest extends AbstractTest
{
    @Test
    public void testCreateInstanceETLServerUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.ETL_SERVER);
        creation.setUserId(new PersonPermId(TEST_OBSERVER_CISD));
        
        // When
        List<RoleAssignmentTechId> ids = v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
        
        // Then
        assertEquals(ids.size(), 1);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        RoleAssignment roleAssignment = v3api.getRoleAssignments(sessionToken, ids, fetchOptions).get(ids.get(0));
        assertEquals(roleAssignment.getRole(), Role.ETL_SERVER);
        assertEquals(roleAssignment.getRoleLevel(), RoleLevel.INSTANCE);
        assertEquals(roleAssignment.getSpace(), null);
        assertEquals(roleAssignment.getProject(), null);
    }
    
    @Test
    public void testCreateSpaceETLServerUser()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        RoleAssignmentCreation creation = new RoleAssignmentCreation();
        creation.setRole(Role.ETL_SERVER);
        creation.setUserId(new PersonPermId(TEST_OBSERVER_CISD));
        creation.setSpaceId(new SpacePermId("CISD"));
        
        // When
        List<RoleAssignmentTechId> ids = v3api.createRoleAssignments(sessionToken, Arrays.asList(creation));
        
        // Then
        assertEquals(ids.size(), 1);
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        RoleAssignment roleAssignment = v3api.getRoleAssignments(sessionToken, ids, fetchOptions).get(ids.get(0));
        assertEquals(roleAssignment.getRole(), Role.ETL_SERVER);
        assertEquals(roleAssignment.getRoleLevel(), RoleLevel.SPACE);
        assertEquals(roleAssignment.getSpace().getCode(), "CISD");
        assertEquals(roleAssignment.getProject(), null);
    }
}
