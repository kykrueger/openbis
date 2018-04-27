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
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.systemtest.asapi.v3.AbstractTest;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.task.UserGroup;
import ch.systemsx.cisd.openbis.generic.server.task.UserManager;
import ch.systemsx.cisd.openbis.generic.server.task.UserManagerReport;
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
        UserManager userManager = createUserManager(commonSpaces, logger);
        Map<String, Principal> principals = principals(U3, U1, U2);
        UserGroup group = group("G1", U1.getUserId(), "blabla");
        userManager.addGroup(group, principals);

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-USER] u1 (home space: U1)\n"
                + "1970-01-01 01:00:12 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for U1\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:15 [ADD-USER] u2 (home space: U2)\n"
                + "1970-01-01 01:00:16 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:17 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for U2\n"
                + "1970-01-01 01:00:18 [ADD-USER] u3 (home space: U3)\n"
                + "1970-01-01 01:00:19 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u3\n"
                + "1970-01-01 01:00:20 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for U3\n");
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
        // 1. create group G2 with user U1
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId(), "blabla");
        userManager.addGroup(group, principals(U1));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").assertExpectations();
        // 2. add users U2 and U3 to group G2
        userManager = createUserManager(commonSpaces, logger);
        group.setAdmins(Arrays.asList(U1.getUserId()));
        userManager.addGroup(group, principals(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER] u2 (home space: U2)\n"
                + "1970-01-01 01:00:01 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U2\n"
                + "1970-01-01 01:00:03 [ADD-USER] u3 (home space: U3)\n"
                + "1970-01-01 01:00:04 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:05 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U3\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.user(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveNormalUserFromAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId(), "blabla");
        userManager.addGroup(group, principals(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. remove U2 from group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, principals(U1, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u2\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.disabledUser(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testChangeNormalUserToAdmin()
    {
        // Given
        // 1. create group G2 with users U1, U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2");
        userManager.addGroup(group, principals(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).user(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. make U1 admin
        userManager = createUserManager(commonSpaces, logger);
        group = group("G2", U1.getUserId());
        userManager.addGroup(group, principals(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.user(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testChangeAdminUserToNormalAdmin()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId());
        userManager.addGroup(group, principals(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. make U1 normal user
        userManager = createUserManager(commonSpaces, logger);
        group = group("G2");
        userManager.addGroup(group, principals(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.user(U1, "G2");
        builder.user(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveNormalUserFromAGroupAndAddItAgain()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId(), "blabla");
        userManager.addGroup(group, principals(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U2 from group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, principals(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").disabledUser(U2, "G2").user(U3, "G2").assertExpectations();
        // 3. add U2 again to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, principals(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.user(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAdminUserFromAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId(), "blabla");
        userManager.addGroup(group, principals(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. remove U1 from group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, principals(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:01 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.disabledUser(U1, "G2");
        builder.user(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAdminUserFromAGroupAndAddItAgain()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId(), "blabla");
        userManager.addGroup(group, principals(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U1 from group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, principals(U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).disabledUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 3. add U1 again to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, principals(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:01 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.user(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testUserFromAGroupHasLefted()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId(), "blabla");
        userManager.addGroup(group, principals(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. U2 is no longer known by the authentication service
        userManager = createUserManager(commonSpaces, logger, U2);

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [DEACTIVATE-USER] u2\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.unknownUser(U2);
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testReuseSameUserId()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId(), "blabla");
        userManager.addGroup(group, principals(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. U2 is no longer known by the authentication service
        userManager = createUserManager(commonSpaces, logger, U2);
        userManager.addGroup(group, principals(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").unknownUser(U2).user(U3, "G2").assertExpectations();
        // 3. U2 is reused and added to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, principals(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REUSE-USER] u2 (home space: U2_1)\n"
                + "1970-01-01 01:00:01 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U2_1\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.user(reuse(U2, "U2_1"), "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    private Principal reuse(Principal user, String homeSpace)
    {
        Map<String, String> props = new TreeMap<>();
        props.put(UserManagerExpectationsBuilder.HOME_SPACE_KEY, homeSpace);
        return new Principal(user.getUserId(), user.getFirstName(), user.getLastName(), user.getEmail(), true, props);
    }

    private UserManager createUserManager(Map<Role, List<String>> commonSpaces, MockLogger logger,
            Principal... usersUnknownByAuthenticationService)
    {
        Set<String> unknownUsers = Arrays.asList(usersUnknownByAuthenticationService).stream().map(Principal::getUserId).collect(Collectors.toSet());
        NullAuthenticationService authenticationService = new NullAuthenticationService()
            {
                @Override
                public Principal getPrincipal(String user) throws IllegalArgumentException
                {
                    if (unknownUsers.contains(user))
                    {
                        throw new IllegalArgumentException("Unknown user " + user);
                    }
                    return new Principal(user, "John", "Doe", "jd@abc.de");
                }
            };
        return new UserManager(authenticationService, v3api, commonSpaces, logger, new MockTimeProvider(0, 1000));
    }

    private UserManagerExpectationsBuilder createBuilder(Map<Role, List<String>> commonSpaces)
    {
        return new UserManagerExpectationsBuilder(v3api, testService, sessionManager, commonSpaces);
    }

    private UserManagerReport manage(UserManager userManager)
    {
        UserManagerReport errorReport = userManager.manage();
        daoFactory.getSessionFactory().getCurrentSession().flush();
        return errorReport;
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

    private UserGroup group(String groupKey, String... admins)
    {
        UserGroup userGroup = new UserGroup();
        userGroup.setKey(groupKey);
        userGroup.setAdmins(Arrays.asList(admins));
        return userGroup;
    }
}
