/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.systemtest.task;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.systemtest.asapi.v3.AbstractTest;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.task.UserGroup;
import ch.systemsx.cisd.openbis.generic.server.task.UserManager;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagerTest extends AbstractTest
{
    private static final Principal U1 = new Principal("u1", "Albert", "Einstein", "a.e@abc.de");

    private static final Principal U2 = new Principal("u2", "Isaac", "Newton", "i.n@abc.de");

    private static final Principal U3 = new Principal("u3", "Alan", "Turing", "a.t@abc.de");

    @Autowired
    private UserManagerTestService testService;

    @Resource(name = ComponentNames.SESSION_MANAGER)
    private IOpenBisSessionManager sessionManager;

    private static Map<Role, List<String>> commonSpaces()
    {
        Map<Role, List<String>> commonSpacesByRole = new EnumMap<>(Role.class);
        commonSpacesByRole.put(Role.USER, Arrays.asList("ALPHA", "BETA"));
        commonSpacesByRole.put(Role.OBSERVER, Arrays.asList("GAMMA"));
        return commonSpacesByRole;
    }

    @Test
    public void testAddNewGroupWithUsers()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManager(v3api, commonSpaces, logger);
        Map<String, Principal> principals = principals(U3, U1, U2);
        UserGroup group = new UserGroup();
        group.setAdmins(Arrays.asList(U1.getUserId(), "blabla"));
        userManager.addGroup("G1", group, principals);

        // When
        String errorMessages = userManager.manageUsers();

        // Then
        assertEquals(errorMessages, "");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G1");
        builder.user(U2, "G1");
        builder.user(U3, "G1");
        builder.assertExpectations();
    }

    @Test
    public void testAddUsersToAnExistingGroup()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManager(v3api, commonSpaces, logger);
        UserGroup group = new UserGroup();
        group.setAdmins(Arrays.asList(U1.getUserId(), "blabla"));
        userManager.addGroup("G2", group, principals(U1));
        assertEquals(userManager.manageUsers(), "");

        userManager = new UserManager(v3api, commonSpaces, logger);
        userManager.addGroup("G2", group, principals(U2, U3));

        // When
        String errorMessages = userManager.manageUsers();

        // Then
        assertEquals(errorMessages, "");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.user(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    private UserManagerExpectationsBuilder createBuilder(Map<Role, List<String>> commonSpaces)
    {
        return new UserManagerExpectationsBuilder(v3api, testService, sessionManager, commonSpaces);
    }

    private Map<String, Principal> principals(Principal... principals)
    {
        Map<String, Principal> map = new TreeMap<>();
        for (Principal principal : principals)
        {
            map.put(principal.getUserId(), principal);
        }
        return map;
    }

}
