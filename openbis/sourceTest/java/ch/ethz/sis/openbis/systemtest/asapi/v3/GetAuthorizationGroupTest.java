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
import java.util.Map;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class GetAuthorizationGroupTest extends AbstractTest
{
    @Test
    public void testGetFetchingOnlyBasic()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupPermId permId1 = new AuthorizationGroupPermId("AGROUP");
        AuthorizationGroupPermId permId2 = new AuthorizationGroupPermId("NON_EXISTENT_GROUP");
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        
        // When
        Map<IAuthorizationGroupId, AuthorizationGroup> map 
                = v3api.getAuthorizationGroups(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);
        
        // Then
        AuthorizationGroup authorizationGroup = map.get(permId1);
        assertEquals(authorizationGroup.getPermId().getPermId(), "AGROUP");
        assertEquals(authorizationGroup.getCode(), "AGROUP");
        assertEquals(authorizationGroup.getDescription(), "myDescription");
        assertEquals(authorizationGroup.getFetchOptions().hasRegistrator(), false);
        assertEquals(authorizationGroup.getFetchOptions().hasUsers(), false);
        assertEquals(map.size(), 1);

        v3api.logout(sessionToken);
    }
    
    @Test
    public void testGetFetchingRegistrator()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        AuthorizationGroupPermId permId1 = new AuthorizationGroupPermId("AGROUP");
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRegistrator();
        
        // When
        Map<IAuthorizationGroupId, AuthorizationGroup> map 
        = v3api.getAuthorizationGroups(sessionToken, Arrays.asList(permId1), fetchOptions);
        
        // Then
        AuthorizationGroup authorizationGroup = map.get(permId1);
        assertEquals(authorizationGroup.getCode(), "AGROUP");
        assertEquals(authorizationGroup.getDescription(), "myDescription");
        assertEquals(authorizationGroup.getFetchOptions().hasRegistrator(), true);
        assertEquals(authorizationGroup.getRegistrator().getUserId(), "test");
        assertEquals(authorizationGroup.getFetchOptions().hasUsers(), false);
        
        v3api.logout(sessionToken);
    }
    
    @Test(dataProvider = "usersAllowedToGetAuthorizationGroups")
    public void testCreateWithUser(String user, boolean spaceOfTestUserVisible)
    {
        // Given
        String sessionToken = v3api.login(user, PASSWORD);
        AuthorizationGroupPermId permId1 = new AuthorizationGroupPermId("AGROUP");
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers().withSpace();
        
        // When
        Map<IAuthorizationGroupId, AuthorizationGroup> map 
                = v3api.getAuthorizationGroups(sessionToken, Arrays.asList(permId1), fetchOptions);
        
        // Then
        AuthorizationGroup authorizationGroup = map.get(permId1);
        assertEquals(authorizationGroup.getCode(), "AGROUP");
        assertEquals(authorizationGroup.getDescription(), "myDescription");
        assertEquals(authorizationGroup.getFetchOptions().hasRegistrator(), false);
        assertEquals(authorizationGroup.getFetchOptions().hasUsers(), true);
        List<Person> users = authorizationGroup.getUsers();
        assertEquals(users.get(0).getUserId(), "test");
        assertEquals(users.get(0).getEmail(), "franz-josef.elmer@systemsx.ch");
        if (spaceOfTestUserVisible)
        {
            assertEquals(users.get(0).getSpace().getCode(), "CISD");
        }
        assertEquals(users.size(), 1);
        
        v3api.logout(sessionToken);
    }

    @DataProvider
    Object[][] usersAllowedToGetAuthorizationGroups()
    {
        String[] users = {TEST_GROUP_ADMIN, TEST_OBSERVER_CISD, TEST_SPACE_USER, TEST_USER};
        boolean[] visible = {false, true, false, true};
        Object[][] objects = new Object[users.length][];
        for (int i = 0; i < users.length; i++)
        {
            objects[i] = new Object[] {users[i], visible[i]};
        }
        return objects;
    }
    
    @Test(dataProvider = "usersNotAllowedToGetAuthorizationGroups")
    public void testCreateWithUserCausingAuthorizationFailure(final String user)
    {
        assertAuthorizationFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                String sessionToken = v3api.login(user, PASSWORD);
                AuthorizationGroupPermId permId1 = new AuthorizationGroupPermId("AGROUP");
                AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
                v3api.getAuthorizationGroups(sessionToken, Arrays.asList(permId1), fetchOptions);
            }
        });
    }
    
    @DataProvider
    Object[][] usersNotAllowedToGetAuthorizationGroups()
    {
        String[] users = {TEST_GROUP_OBSERVER, TEST_GROUP_POWERUSER, TEST_INSTANCE_OBSERVER, TEST_POWER_USER_CISD};
        Object[][] objects = new Object[users.length][];
        for (int i = 0; i < users.length; i++)
        {
            objects[i] = new Object[] {users[i]};
        }
        return objects;
    }
}
