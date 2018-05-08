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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.systemtest.asapi.v3.AbstractTest;
import ch.systemsx.cisd.authentication.NullAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
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
    public void testCreateOneGroupWithAUserWhichAlreadyHasAHomeSpace()
    {
        // Given
        // 1. create user U2 with home space TEST-SPACE and SPACE_ADMIN on this space 
        createUserForTestSpace(U2);
        // 2. create groip G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
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
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:16 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:17 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:18 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:19 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("G1_ALPHA").admin(U1).user(U2);
        builder.space("G1_BETA").admin(U1).user(U2);
        builder.space("G1_GAMMA").admin(U1).observer(U2);
        builder.space("G1_U1").admin(U1).non(U2);
        builder.space("G1_U2").admin(U1, U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "TEST-SPACE");
        builder.assertExpectations();
    }

    private void createUserForTestSpace(Principal user)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId(user.getUserId());
        SpacePermId spaceId = new SpacePermId("TEST-SPACE");
        personCreation.setSpaceId(spaceId);
        v3api.createPersons(sessionToken, Arrays.asList(personCreation));
        RoleAssignmentCreation assignmentCreation = new RoleAssignmentCreation();
        assignmentCreation.setUserId(new PersonPermId(user.getUserId()));
        assignmentCreation.setRole(Role.ADMIN);
        assignmentCreation.setSpaceId(spaceId);
        v3api.createRoleAssignments(sessionToken, Arrays.asList(assignmentCreation));
    }

    @Test
    public void testCreateOneGroupWithAUserWhichAlreadyTriedToLogin()
    {
        // Given
        assertEquals(v3api.login(U2.getUserId(), PASSWORD), null);
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
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
                + "1970-01-01 01:00:13 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U1\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u1\n"
                + "1970-01-01 01:00:15 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, user: u1\n"
                + "1970-01-01 01:00:16 [ADD-SPACE] G1_U2\n"
                + "1970-01-01 01:00:17 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:18 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:19 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n"
                + "1970-01-01 01:00:20 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G1_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1").commonSpaces(commonSpaces).users(U1, U2);
        builder.space("G1_ALPHA").admin(U1).user(U2);
        builder.space("G1_BETA").admin(U1).user(U2);
        builder.space("G1_GAMMA").admin(U1).observer(U2);
        builder.space("G1_U1").admin(U1).non(U2);
        builder.space("G1_U2").admin(U1, U2);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.assertExpectations();
    }
    
    @Test
    public void testCreateTwoGroupsWithSamplesDistinctUsers()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger)
                .commonSpaces(commonSpaces).commonSample("GAMMA", "NORMAL").globalSpace("ALL").get();
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
                + "1970-01-01 01:00:31 [ADD-SAMPLE] /G1_GAMMA/G1_GAMMA\n"
                + "1970-01-01 01:00:32 [ADD-AUTHORIZATION-GROUP] G2\n"
                + "1970-01-01 01:00:33 [ADD-AUTHORIZATION-GROUP] G2_ADMIN\n"
                + "1970-01-01 01:00:34 [ADD-SPACE] G2_ALPHA\n"
                + "1970-01-01 01:00:35 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_ALPHA\n"
                + "1970-01-01 01:00:36 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_ALPHA\n"
                + "1970-01-01 01:00:37 [ADD-SPACE] G2_BETA\n"
                + "1970-01-01 01:00:38 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_USER for G2_BETA\n"
                + "1970-01-01 01:00:39 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_BETA\n"
                + "1970-01-01 01:00:40 [ADD-SPACE] G2_GAMMA\n"
                + "1970-01-01 01:00:41 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2, role: SPACE_OBSERVER for G2_GAMMA\n"
                + "1970-01-01 01:00:42 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_GAMMA\n"
                + "1970-01-01 01:00:43 [ADD-SPACE] G2_U4\n"
                + "1970-01-01 01:00:44 [ADD-USER] u4\n"
                + "1970-01-01 01:00:45 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:46 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:47 [ADD-USER-TO-AUTHORIZATION-GROUP] group: ALL_GROUPS, user: u4\n"
                + "1970-01-01 01:00:48 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u4\n"
                + "1970-01-01 01:00:49 [ADD-SAMPLE] /G2_GAMMA/G2_GAMMA\n"
                + "1970-01-01 01:00:50 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n"
                + "1970-01-01 01:00:51 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G1_U2\n"
                + "1970-01-01 01:00:52 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G1_U3\n"
                + "1970-01-01 01:00:53 [ASSIGN-HOME-SPACE-FOR-USER] user: u4, home space: G2_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.globalSpaces(globalSpaces).groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.samples("NORMAL", "/G1_GAMMA/G1_GAMMA", "/G2_GAMMA/G2_GAMMA");
        builder.space("A").observer(U1, U2, U3, U4);
        builder.space("B").observer(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2, U3).non(U4);
        builder.space("G1_BETA").admin(U1).user(U2, U3).non(U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2, U3).non(U4);
        builder.space("G1_U1").admin(U1).non(U2, U3, U4);
        builder.space("G1_U2").admin(U1, U2).non(U3, U4);
        builder.space("G1_U3").admin(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_ALPHA").non(U1, U2, U3).admin(U4);
        builder.space("G2_BETA").non(U1, U2, U3).admin(U4);
        builder.space("G2_GAMMA").non(U1, U2, U3).admin(U4);
        builder.space("G2_U4").non(U1, U2, U3).admin(U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G1_U3");
        builder.homeSpace(U4, "G2_U4");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsWithSharedUsers()
    {
        // Given
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
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
                + "1970-01-01 01:00:46 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u4\n"
                + "1970-01-01 01:00:47 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G1_U1\n"
                + "1970-01-01 01:00:48 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G1_U2\n"
                + "1970-01-01 01:00:49 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G1_U3\n"
                + "1970-01-01 01:00:50 [ASSIGN-HOME-SPACE-FOR-USER] user: u4, home space: G2_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2, U3).non(U4);
        builder.space("G1_BETA").admin(U1).user(U2, U3).non(U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2, U3).non(U4);
        builder.space("G1_U1").admin(U1).non(U2, U3, U4);
        builder.space("G1_U2").admin(U1, U2).non(U3, U4);
        builder.space("G1_U3").admin(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_ALPHA").non(U1).user(U2).admin(U3, U4);
        builder.space("G2_BETA").non(U1).user(U2).admin(U3, U4);
        builder.space("G2_GAMMA").non(U1).observer(U2).admin(U3, U4);
        builder.space("G2_U2").non(U1).admin(U2, U3, U4);
        builder.space("G2_U3").non(U1, U2).admin(U3, U4);
        builder.space("G2_U4").non(U1, U2).admin(U3, U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G1_U3");
        builder.homeSpace(U4, "G2_U4");
        builder.assertExpectations();
    }

    @Test
    public void testAddUsersToAnExistingGroup()
    {
        // Given
        // 1. create group G2 with user U1 (admin)
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId(), "blabla"), users(U1));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. add users U2 and U3 to group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
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
                + "1970-01-01 01:00:07 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:08 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2\n"
                + "1970-01-01 01:00:09 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1, U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveNormalUserFromAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U2 from group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:01 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:02 [REMOVE-HOME-SPACE-FROM-USER] u2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U3);
        builder.usersWithoutAuthentication(U2);
        builder.space("G2_ALPHA").admin(U1).user(U3);
        builder.space("G2_BETA").admin(U1).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U3);
        builder.space("G2_U1").admin(U1).non(U3);
        builder.space("G2_U2").admin(U1).non(U3);
        builder.space("G2_U3").admin(U1).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, null);
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testChangeNormalUserToAdmin()
    {
        // Given
        // 1. create group G2 with users U1, U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2"), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").space("G2_ALPHA").user(U1, U2, U3).assertExpectations();
        // 2. make U1 admin
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1, U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testChangeAdminUserToNormal()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").space("G2_ALPHA").admin(U1).user(U2, U3).assertExpectations();
        // 2. make U1 normal user
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2"), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").user(U1, U2, U3);
        builder.space("G2_BETA").user(U1, U2, U3);
        builder.space("G2_GAMMA").observer(U1, U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").non(U1).admin(U2).non(U3);
        builder.space("G2_U3").non(U1, U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveNormalUserFromAGroupAndAddItAgain()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").space("G2_GAMMA").admin(U1).observer(U2, U3).assertExpectations();
        // 2. remove U2 from group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").usersWithoutAuthentication(U2).space("G2_U2").admin(U1).non(U3).assertExpectations();
        // 3. add U2 again to group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U2_2\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:02 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:03 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2_2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1).non(U2, U3);
        builder.space("G2_U2_2").admin(U1, U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2_2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAdminUserFromAGroup()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").space("G2_U1").admin(U1).non(U2, U3).assertExpectations();
        // 2. remove U1 from group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:01 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:02 [UNASSIGN-ROLE-FORM-USER] user: u1, role: SPACE_ADMIN for G2_U1\n"
                + "1970-01-01 01:00:03 [REMOVE-HOME-SPACE-FROM-USER] u1\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U2, U3);
        builder.usersWithoutAuthentication(U1);
        builder.space("G2_ALPHA").user(U2, U3);
        builder.space("G2_BETA").user(U2, U3);
        builder.space("G2_GAMMA").observer(U2, U3);
        builder.space("G2_U1").non(U2, U3);
        builder.space("G2_U2").admin(U2).non(U3);
        builder.space("G2_U3").non(U2).admin(U3);
        builder.homeSpace(U1, null);
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testRemoveAdminUserFromAGroupAndAddItAgain()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. remove U1 from group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G2").usersWithoutAuthentication(U1).space("G2_U1").non(U2, U3).assertExpectations();
        // 3. add U1 again to group G2 as admin
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U1_2\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U1_2\n"
                + "1970-01-01 01:00:02 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:04 [ASSIGN-HOME-SPACE-FOR-USER] user: u1, home space: G2_U1_2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2, U3);
        builder.space("G2_BETA").admin(U1).user(U2, U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2, U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U1_2").admin(U1).non(U2, U3);
        builder.space("G2_U2").admin(U1, U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1_2");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testCreateSecondGroupWithNormalUserInBothGroups()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G1").space("G1_U2").admin(U1, U2).assertExpectations();
        // 2. create group G2 with users U2 and U3 (admin)
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
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
                + "1970-01-01 01:00:11 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:13 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:14 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:15 [ADD-USER] u3\n"
                + "1970-01-01 01:00:16 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:17 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:18 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u3\n"
                + "1970-01-01 01:00:19 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3);
        builder.space("G1_U1").admin(U1).non(U2, U3);
        builder.space("G1_U2").admin(U1, U2).non(U3);
        builder.space("G2_ALPHA").non(U1).user(U2).admin(U3);
        builder.space("G2_BETA").non(U1).user(U2).admin(U3);
        builder.space("G2_GAMMA").non(U1).observer(U2).admin(U3);
        builder.space("G2_U2").non(U1).admin(U2, U3);
        builder.space("G2_U3").non(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testCreateSecondGroupWithAdminUserInBothGroups()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G1").space("G1_U1").admin(U1).non(U2).assertExpectations();
        // 2. create group G2 with users U1 (admin) and U3
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
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
                + "1970-01-01 01:00:11 [ADD-SPACE] G2_U1\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U1\n"
                + "1970-01-01 01:00:13 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u1\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u1\n"
                + "1970-01-01 01:00:15 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:16 [ADD-USER] u3\n"
                + "1970-01-01 01:00:17 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:18 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:19 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3);
        builder.space("G1_U1").admin(U1).non(U2, U3);
        builder.space("G1_U2").admin(U1, U2).non(U3);
        builder.space("G2_ALPHA").admin(U1).non(U2).user(U3);
        builder.space("G2_BETA").admin(U1).non(U2).user(U3);
        builder.space("G2_GAMMA").admin(U1).non(U2).observer(U3);
        builder.space("G2_U1").admin(U1).non(U2, U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testCreateSecondGroupWithUserNormalInFirstGroupAndAdminInSecondGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        createBuilder().groups("G1").space("G1_U1").admin(U1).non(U2).assertExpectations();
        // 2. create group G2 with users U2 (admin) and U3
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
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
                + "1970-01-01 01:00:11 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:12 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:13 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:14 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, user: u2\n"
                + "1970-01-01 01:00:15 [ADD-SPACE] G2_U3\n"
                + "1970-01-01 01:00:16 [ADD-USER] u3\n"
                + "1970-01-01 01:00:17 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U3\n"
                + "1970-01-01 01:00:18 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u3\n"
                + "1970-01-01 01:00:19 [ASSIGN-HOME-SPACE-FOR-USER] user: u3, home space: G2_U3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3);
        builder.space("G1_U1").admin(U1).non(U2, U3);
        builder.space("G1_U2").admin(U1, U2).non(U3);
        builder.space("G2_ALPHA").non(U1).admin(U2).user(U3);
        builder.space("G2_BETA").non(U1).admin(U2).user(U3);
        builder.space("G2_GAMMA").non(U1).admin(U2).observer(U3);
        builder.space("G2_U2").non(U1).admin(U2).non(U3);
        builder.space("G2_U3").non(U1).admin(U2).admin(U3);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsAndMoveUserFromFirstGroupToSecondGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. create group G2 with users U3 (admin) and U4
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        userManager.addGroup(group("G2", U3.getUserId()), users(U3, U4));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. Move U2 from G1 -> G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1));
        userManager.addGroup(group("G2", U3.getUserId()), users(U2, U3, U4));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G1, user: u2\n"
                + "1970-01-01 01:00:01 [UNASSIGN-ROLE-FORM-USER] user: u2, role: SPACE_ADMIN for G1_U2\n"
                + "1970-01-01 01:00:02 [ADD-SPACE] G2_U2\n"
                + "1970-01-01 01:00:03 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2\n"
                + "1970-01-01 01:00:04 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:05 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).non(U2, U3, U4);
        builder.space("G1_BETA").admin(U1).non(U2, U3, U4);
        builder.space("G1_GAMMA").admin(U1).non(U2, U3, U4);
        builder.space("G1_U1").admin(U1).non(U2, U3, U4);
        builder.space("G1_U2").admin(U1).non(U2, U3, U4);
        builder.space("G2_ALPHA").non(U1).user(U2).admin(U3).user(U4);
        builder.space("G2_BETA").non(U1).user(U2).admin(U3).user(U4);
        builder.space("G2_GAMMA").non(U1).observer(U2).admin(U3).observer(U4);
        builder.space("G2_U2").non(U1).admin(U2).admin(U3).non(U4);
        builder.space("G2_U3").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U4").non(U1).non(U2).admin(U3).admin(U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G2_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.homeSpace(U4, "G2_U4");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsAndMoveUserFromSecondGroupToFirstGroup()
    {
        // Given
        // 1. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. create group G2 with users U3 (admin) and U4
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        userManager.addGroup(group("G2", U3.getUserId()), users(U3, U4));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. Move U4 from G2 -> G1
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2, U4));
        userManager.addGroup(group("G2", U3.getUserId()), users(U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G1_U4\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U4\n"
                + "1970-01-01 01:00:02 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u4\n"
                + "1970-01-01 01:00:03 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:04 [UNASSIGN-ROLE-FORM-USER] user: u4, role: SPACE_ADMIN for G2_U4\n"
                + "1970-01-01 01:00:05 [ASSIGN-HOME-SPACE-FOR-USER] user: u4, home space: G1_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3).user(U4);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3).user(U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3).observer(U4);
        builder.space("G1_U1").admin(U1).non(U2).non(U3).non(U4);
        builder.space("G1_U2").admin(U1).admin(U2).non(U3).non(U4);
        builder.space("G1_U4").admin(U1).non(U2).non(U3).admin(U4);
        builder.space("G2_ALPHA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_BETA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_GAMMA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U3").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U4").non(U1).non(U2).admin(U3).non(U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.homeSpace(U4, "G1_U4");
        builder.assertExpectations();
    }

    @Test
    public void testCreateTwoGroupsAndMoveUserWithTestSpaceAsHomeSpaceFromSecondGroupToFirstGroup()
    {
        // Given
        // 1. create user U4 with home space TEST-SPACE and SPACE_ADMIN on this space 
        createUserForTestSpace(U4);
        // 2. create group G1 with users U1 (admin) and U2
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. create group G2 with users U3 (admin) and U4
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2));
        userManager.addGroup(group("G2", U3.getUserId()), users(U3, U4));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 4. Move U4 from G2 -> G1
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G1", U1.getUserId()), users(U1, U2, U4));
        userManager.addGroup(group("G2", U3.getUserId()), users(U3));
        
        // When
        UserManagerReport report = manage(userManager);
        
        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G1_U4\n"
                + "1970-01-01 01:00:01 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G1_ADMIN, role: SPACE_ADMIN for G1_U4\n"
                + "1970-01-01 01:00:02 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G1, user: u4\n"
                + "1970-01-01 01:00:03 [REMOVE-USER-FROM-AUTHORIZATION-GROUP] group: G2, user: u4\n"
                + "1970-01-01 01:00:04 [UNASSIGN-ROLE-FORM-USER] user: u4, role: SPACE_ADMIN for G2_U4\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G1", "G2").commonSpaces(commonSpaces).users(U1, U2, U3, U4);
        builder.space("G1_ALPHA").admin(U1).user(U2).non(U3).user(U4);
        builder.space("G1_BETA").admin(U1).user(U2).non(U3).user(U4);
        builder.space("G1_GAMMA").admin(U1).observer(U2).non(U3).observer(U4);
        builder.space("G1_U1").admin(U1).non(U2).non(U3).non(U4);
        builder.space("G1_U2").admin(U1).admin(U2).non(U3).non(U4);
        builder.space("G1_U4").admin(U1).non(U2).non(U3).admin(U4);
        builder.space("G2_ALPHA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_BETA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_GAMMA").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U3").non(U1).non(U2).admin(U3).non(U4);
        builder.space("G2_U4").non(U1).non(U2).admin(U3).non(U4);
        builder.homeSpace(U1, "G1_U1");
        builder.homeSpace(U2, "G1_U2");
        builder.homeSpace(U3, "G2_U3");
        builder.homeSpace(U4, "TEST-SPACE");
        builder.assertExpectations();
    }
    
    @Test
    public void testUserFromAGroupHasLefted()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. U2 is no longer known by the authentication service
        userManager = new UserManagerBuilder(v3api, logger).unknownUser(U2).commonSpaces(commonSpaces).get();

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [DEACTIVATE-USER] u2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U3);
        builder.space("G2_ALPHA").admin(U1).user(U3);
        builder.space("G2_BETA").admin(U1).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U3);
        builder.space("G2_U1").admin(U1).non(U3);
        builder.space("G2_U2").admin(U1).non(U3);
        builder.space("G2_U3").admin(U1).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.unknownUser(U2);
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testReuseSameUserId()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. U2 is no longer known by the authentication service
        userManager = new UserManagerBuilder(v3api, logger).unknownUser(U2).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. U2 is reused and added to group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U2_2\n"
                + "1970-01-01 01:00:01 [REUSE-USER] u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2_2\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:04 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2_2\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2).user(U3);
        builder.space("G2_BETA").admin(U1).user(U2).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2).observer(U3);
        builder.space("G2_U1").admin(U1).non(U2).non(U3);
        builder.space("G2_U2").admin(U1).non(U2).non(U3);
        builder.space("G2_U2_2").admin(U1).admin(U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2_2");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    @Test
    public void testReuseSameUserIdTwice()
    {
        // Given
        // 1. create group G2 with users U1 (admin), U2 and U3
        MockLogger logger = new MockLogger();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        UserManager userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 2. U2 is no longer known by the authentication service
        userManager = new UserManagerBuilder(v3api, logger).unknownUser(U2).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 3. U2 is reused and added to group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 4. U2 is no longer known by the authentication service
        userManager = new UserManagerBuilder(v3api, logger).unknownUser(U2).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U3));
        assertEquals(manage(userManager).getErrorReport(), "");
        // 5. U2 is reused and added to group G2
        userManager = new UserManagerBuilder(v3api, logger).commonSpaces(commonSpaces).get();
        userManager.addGroup(group("G2", U1.getUserId()), users(U1, U2, U3));

        // When
        UserManagerReport report = manage(userManager);

        // Then
        assertEquals(report.getErrorReport(), "");
        assertEquals(report.getAuditLog(), "1970-01-01 01:00:00 [ADD-SPACE] G2_U2_3\n"
                + "1970-01-01 01:00:01 [REUSE-USER] u2\n"
                + "1970-01-01 01:00:02 [ASSIGN-ROLE-TO-AUTHORIZATION-GROUP] group: G2_ADMIN, role: SPACE_ADMIN for G2_U2_3\n"
                + "1970-01-01 01:00:03 [ADD-USER-TO-AUTHORIZATION-GROUP] group: G2, user: u2\n"
                + "1970-01-01 01:00:04 [ASSIGN-HOME-SPACE-FOR-USER] user: u2, home space: G2_U2_3\n");
        UserManagerExpectationsBuilder builder = createBuilder();
        builder.groups("G2").commonSpaces(commonSpaces).users(U1, U2, U3);
        builder.space("G2_ALPHA").admin(U1).user(U2).user(U3);
        builder.space("G2_BETA").admin(U1).user(U2).user(U3);
        builder.space("G2_GAMMA").admin(U1).observer(U2).observer(U3);
        builder.space("G2_U1").admin(U1).non(U2).non(U3);
        builder.space("G2_U2").admin(U1).non(U2).non(U3);
        builder.space("G2_U2_2").admin(U1).non(U2).non(U3);
        builder.space("G2_U2_3").admin(U1).admin(U2).non(U3);
        builder.space("G2_U3").admin(U1).non(U2).admin(U3);
        builder.homeSpace(U1, "G2_U1");
        builder.homeSpace(U2, "G2_U2_3");
        builder.homeSpace(U3, "G2_U3");
        builder.assertExpectations();
    }

    private UserManagerExpectationsBuilder createBuilder()
    {
        return new UserManagerExpectationsBuilder(v3api, testService, sessionManager);
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

    private static class UserManagerBuilder
    {
        private IApplicationServerInternalApi service;

        private ISimpleLogger logger;

        private Set<String> usersUnknownByAuthenticationService = new TreeSet<>();

        private List<String> globalSpaces = new ArrayList<>();

        private Map<Role, List<String>> commonSpacesByRole = new TreeMap<>();

        private Map<String, String> commonSamplesByCode = new TreeMap<>();

        UserManagerBuilder(IApplicationServerInternalApi service, ISimpleLogger logger)
        {
            this.service = service;
            this.logger = logger;
        }

        UserManager get()
        {
            NullAuthenticationService authenticationService = new NullAuthenticationService()
                {
                    @Override
                    public Principal getPrincipal(String user) throws IllegalArgumentException
                    {
                        if (usersUnknownByAuthenticationService.contains(user))
                        {
                            throw new IllegalArgumentException("Unknown user " + user);
                        }
                        return new Principal(user, "John", "Doe", "jd@abc.de");
                    }
                };
            UserManager userManager = new UserManager(authenticationService, service, logger, new MockTimeProvider(0, 1000));
            userManager.setGlobalSpaces(globalSpaces);
            userManager.setCommonSpacesAndSamples(commonSpacesByRole, commonSamplesByCode);
            return userManager;
        }

        private UserManagerBuilder unknownUser(Principal user)
        {
            usersUnknownByAuthenticationService.add(user.getUserId());
            return this;
        }

        private UserManagerBuilder globalSpace(String spaceCode)
        {
            globalSpaces.add(spaceCode);
            return this;
        }

        private UserManagerBuilder commonSpaces(Map<Role, List<String>> commonSpaces)
        {
            commonSpacesByRole = commonSpaces;
            return this;
        }

        private UserManagerBuilder commonSample(String sampleCode, String sampleType)
        {
            commonSamplesByCode.put(sampleCode, sampleType);
            return this;
        }
    }

}
