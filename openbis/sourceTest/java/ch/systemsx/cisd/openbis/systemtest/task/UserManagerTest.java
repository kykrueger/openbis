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

    private static final Principal U4 = new Principal("u4", "Leonard", "Euler", "l.e@abc.de");

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
    public void testCreateOneGroupWithAUserWhichAlreadyTriedLoggedIn()
    {
        // Given
        assertEquals(v3api.login(U2.getUserId(), PASSWORD), null);
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId(), "blabla"), users(U1, U2));

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
                + "1970-01-01 01:00:11 [ADD-SPACE] G1_U1\n"
                + "1970-01-01 01:00:12 [ADD-USER] u1\n"
                + "1970-01-01 01:00:13 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:15 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:16 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:17 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:18 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G1");
        builder.user(U2, "G1");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsWithDistinctUsers()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        List<String> globalSpaces = Arrays.asList("A", "B");
        userManager.setGlobalSpaces(globalSpaces);
        userManager.addGroup(group("G1", U1.getUserId(), "blabla"), users(U3, U1, U2));
        userManager.addGroup(group("G2", U4.getUserId()), users(U4));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACES] [A, B]\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] ALL_GROUPS\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, role: SPACE_OBSERVER for A\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, role: SPACE_OBSERVER for B\n"
                + "1970-01-01 01:00:04 [ADD-AUTHORIZATION-GROUP] G1\n"
                + "1970-01-01 01:00:05 [ADD-AUTHORIZATION-GROUP] G1_ADMIN\n"
                + "1970-01-01 01:00:06 [ADD-SPACE] G1_ALPHA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_ALPHA\n"
                + "1970-01-01 01:00:08 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_ALPHA\n"
                + "1970-01-01 01:00:09 [ADD-SPACE] G1_BETA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_USER for G1_BETA\n"
                + "1970-01-01 01:00:11 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_BETA\n"
                + "1970-01-01 01:00:12 [ADD-SPACE] G1_GAMMA\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1, role: SPACE_OBSERVER for G1_GAMMA\n"
                + "1970-01-01 01:00:14 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_GAMMA\n"
                + "1970-01-01 01:00:15 [ADD-SPACE] G1_U1\n"
                + "1970-01-01 01:00:16 [ADD-USER] u1\n"
                + "1970-01-01 01:00:17 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:18 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:19 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u1\n"
                + "1970-01-01 01:00:20 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:21 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:22 [ADD-USER] u2\n"
                + "1970-01-01 01:00:23 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:24 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:25 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u2\n"
                + "1970-01-01 01:00:26 [ADD-SPACE] G1_U3\n"
                + "1970-01-01 01:00:27 [ADD-USER] u3\n"
                + "1970-01-01 01:00:28 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:29 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u3\n"
                + "1970-01-01 01:00:30 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u3\n"
                + "1970-01-01 01:00:31 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:32 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:33 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:34 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:35 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:36 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:37 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:38 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:39 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:40 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:41 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:42 [ADD-SPACE] G2_U4\n"
                + "1970-01-01 01:00:43 [ADD-USER] u4\n"
                + "1970-01-01 01:00:44 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:45 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:46 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u4\n"
                + "1970-01-01 01:00:47 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u4\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.setGlobalSpaces(globalSpaces);
        builder.adminUser(U1, "G1");
        builder.user(U2, "G1");
        builder.user(U3, "G1");
        builder.adminUser(U4, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsWithSharedUsers()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2, U3));
        userManager.addGroup(group("G2", U3.getUserId(), U4.getUserId()), users(U2, U3, U4));

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
                + "1970-01-01 01:00:11 [ADD-SPACE] G1_U1\n"
                + "1970-01-01 01:00:12 [ADD-USER] u1\n"
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:16 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:17 [ADD-USER] u2\n"
                + "1970-01-01 01:00:18 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:19 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:20 [ADD-SPACE] G1_U3\n"
                + "1970-01-01 01:00:21 [ADD-USER] u3\n"
                + "1970-01-01 01:00:22 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U3\n"
                + "1970-01-01 01:00:23 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u3\n"
                + "1970-01-01 01:00:24 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:25 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:26 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:27 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:28 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:29 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:30 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:31 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:32 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:33 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:34 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:35 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:36 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:37 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:38 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:39 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:40 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:41 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u3\n"
                + "1970-01-01 01:00:42 [ADD-SPACE] G2_U4\n"
                + "1970-01-01 01:00:43 [ADD-USER] u4\n"
                + "1970-01-01 01:00:44 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:45 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:46 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u4\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G1");
        builder.user(U2, "G1", "G2");
        builder.user(U3, "G1");
        builder.adminUser(U3, "G2");
        builder.adminUser(U4, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testAddUsersToAnExistingGroup()
    {
        // Given
        // 1. create group G2 with user U1 (admin)
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G2", U1.getUserId(), "blabla"), users(U1));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").assertExpectations();
        // 2. add users U2 and U3 to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:01 [ADD-USER] u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:04 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:05 [ADD-USER] u3\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:07 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n");
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
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. remove U2 from group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U3));

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
        userManager.addGroup(group, users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).user(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. make U1 admin
        userManager = createUserManager(commonSpaces, logger);
        group = group("G2", U1.getUserId());
        userManager.addGroup(group, users(U1, U2, U3));

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
        userManager.addGroup(group, users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. make U1 normal user
        userManager = createUserManager(commonSpaces, logger);
        group = group("G2");
        userManager.addGroup(group, users(U1, U2, U3));

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
        userManager.addGroup(group, users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U2 from group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").disabledUser(U2, "G2").user(U3, "G2").assertExpectations();
        // 3. add U2 again to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, users(U1, U2, U3));

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
        userManager.addGroup(group, users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. remove U1 from group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, users(U2, U3));

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
        userManager.addGroup(group, users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. remove U1 from group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, users(U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).disabledUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 3. add U1 again to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, users(U1, U2, U3));

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
    public void testCreateSecondGroupWithNormalUserInBothGroups()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G1").user(U2, "G1").unknownUser(U3).assertExpectations();
        // 2. create group G2 with users U2 and U3 (admin)
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        userManager.addGroup(group("G2", U3.getUserId()), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U2\n"
                + "1970-01-01 01:00:13 [ADD-USER] u3 (home space: U3)\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:15 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U3\n"
                + "1970-01-01 01:00:16 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u3\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G1");
        builder.user(U2, "G1", "G2");
        builder.adminUser(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testCreateSecondGroupWithAdminUserInBothGroups()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G1").user(U2, "G1").unknownUser(U3).assertExpectations();
        // 2. create group G2 with users U1 (admin) and U3
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U1\n"
                + "1970-01-01 01:00:13 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:14 [ADD-USER] u3 (home space: U3)\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:16 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U3\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G1", "G2");
        builder.user(U2, "G1");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testCreateSecondGroupWithUserNormalInFirstGroupAndAdminInSecondGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G1").user(U2, "G1").unknownUser(U3).assertExpectations();
        // 2. create group G2 with users U2 (admin) and U3
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        userManager.addGroup(group("G2", U2.getUserId()), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:01 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:04 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:05 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:06 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:07 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:08 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:09 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:10 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:11 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U2\n"
                + "1970-01-01 01:00:13 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u2\n"
                + "1970-01-01 01:00:14 [ADD-USER] u3 (home space: U3)\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:16 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U3\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G1");
        builder.user(U2, "G1");
        builder.adminUser(U2, "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsAndMoveUserBetweenGroups()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G1").user(U2, "G1").unknownUser(U3).assertExpectations();
        // 2. create group G2 with users U3 (admin) and U4
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        userManager.addGroup(group("G2", U3.getUserId()), users(U3, U4));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G1").user(U2, "G1").adminUser(U3, "G2").user(U4, "G2").assertExpectations();
        // 3. Move U2 from G1 -> G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group("G1", U1.getUserId()), users(U1));
        userManager.addGroup(group("G2", U3.getUserId()), users(U2, U3, U4));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U2\n"
                + "1970-01-01 01:00:02 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u2\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G1");
        builder.user(U2, "G2");
        builder.adminUser(U3, "G2");
        builder.user(U4, "G2");
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
        userManager.addGroup(group, users(U1, U2, U3));
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
        userManager.addGroup(group, users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. U2 is no longer known by the authentication service
        userManager = createUserManager(commonSpaces, logger, U2);
        userManager.addGroup(group, users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").unknownUser(U2).user(U3, "G2").assertExpectations();
        // 3. U2 is reused and added to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REUSE-USER] u2 (home space: U2_2)\n"
                + "1970-01-01 01:00:01 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U2_2\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.user(reuse(U2, "U2_2"), "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    @Test
    public void testReuseSameUserIdTwice()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = createUserManager(commonSpaces, logger);
        UserGroup group = group("G2", U1.getUserId(), "blabla");
        userManager.addGroup(group, users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(U2, "G2").user(U3, "G2").assertExpectations();
        // 2. U2 is no longer known by the authentication service
        userManager = createUserManager(commonSpaces, logger, U2);
        userManager.addGroup(group, users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").unknownUser(U2).user(U3, "G2").assertExpectations();
        // 3. U2 is reused and added to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").user(reuse(U2, "U2_2"), "G2").user(U3, "G2").assertExpectations();
        // 4. U2 is no longer known by the authentication service
        userManager = createUserManager(commonSpaces, logger, U2);
        userManager.addGroup(group, users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder(commonSpaces).adminUser(U1, "G2").unknownUser(U2).user(U3, "G2").assertExpectations();
        // 5. U2 is reused and added to group G2
        userManager = createUserManager(commonSpaces, logger);
        userManager.addGroup(group, users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REUSE-USER] u2 (home space: U2_3)\n"
                + "1970-01-01 01:00:01 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for U2_3\n");
        UserManagerExpectationsBuilder builder = createBuilder(commonSpaces);
        builder.adminUser(U1, "G2");
        builder.user(reuse(U2, "U2_3"), "G2");
        builder.user(U3, "G2");
        builder.assertExpectations();
    }

    private Principal reuse(Principal user, String userSpacePostfix)
    {
        Map<String, String> props = new TreeMap<>();
        props.put(UserManagerExpectationsBuilder.USER_SPACE_POSTFIX_KEY, userSpacePostfix);
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
        UserManager userManager = new UserManager(authenticationService, v3api, logger, new MockTimeProvider(0, 1000));
        userManager.setCommonSpacesByRole(commonSpaces);
        return userManager;
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

    private Map<String, Principal> users(Principal... principals)
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
