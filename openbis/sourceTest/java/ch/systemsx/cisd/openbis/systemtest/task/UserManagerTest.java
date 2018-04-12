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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.ethz.sis.openbis.systemtest.asapi.v3.AbstractTest;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.common.test.AssertionUtil;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.task.UserGroup;
import ch.systemsx.cisd.openbis.generic.server.task.UserManager;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

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
    public void testAddNewGroupWithNewUsers()
    {
        // Given
        MockLogger logger = new MockLogger();
        UserManager userManager = new UserManager(v3api, commonSpaces(), logger);
        Map<String, Principal> principals = principals(U3, U1, U2);
        UserGroup group = new UserGroup();
        group.setAdmins(Arrays.asList(U1.getUserId(), "blabla"));
        userManager.addGroup("G1", group, principals);

        // When
        String errorMessages = userManager.manageUsers();

        // Then
        assertEquals(errorMessages, "");
        List<String> allSpaces = getAllSpaces();
        System.err.println("all spaces: " + allSpaces);
        for (Principal user : principals.values())
        {
            List<Person> persons = getPersons(user.getUserId());
            Person person = persons.get(0);
            assertEquals(person.getUserId(), user.getUserId());
            assertEquals(person.getEmail(), "franz-josef.elmer@systemsx.ch");
            String space = getOwnSpace(user);
            assertEquals(person.getSpace().getCode(), space);
            if (group.getAdmins() != null && group.getAdmins().contains(user.getUserId()))
            {
                assertAdminUserLevels("G1", user, principals.values());
            } else
            {
                assertNormalUserLevels("G1", user);
            }
        }
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
        Collection<Principal> users = principals(U1, U2, U3).values();
        for (Principal user : users)
        {
            List<Person> persons = getPersons(user.getUserId());
            Person person = persons.get(0);
            assertEquals(person.getUserId(), user.getUserId());
            assertEquals(person.getEmail(), "franz-josef.elmer@systemsx.ch");
            String space = getOwnSpace(user);
            assertEquals(person.getSpace().getCode(), space);
            if (group.getAdmins() != null && group.getAdmins().contains(user.getUserId()))
            {
                assertAdminUserLevels("G2", user, users);
            } else
            {
                assertNormalUserLevels("G2", user);
            }
        }
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

    private void assertNormalUserLevels(String groupCode, Principal user)
    {
        String userId = user.getUserId();
        Map<Role, List<String>> commonSpaces = commonSpaces();
        assertLevel(userId, Level.SPACE_OBSERVER, getCommonSpaces(groupCode, commonSpaces, Role.OBSERVER));
        assertLevel(userId, Level.SPACE_USER, getCommonSpaces(groupCode, commonSpaces, Role.USER));
        assertLevel(userId, Level.SPACE_ADMIN, Arrays.asList(userId.toUpperCase()));
    }

    private void assertAdminUserLevels(String groupCode, Principal adminUser, Collection<Principal> users)
    {
        List<String> spaces = users.stream().map(p -> getOwnSpace(p)).collect(Collectors.toList());
        Map<Role, List<String>> commonSpaces = commonSpaces();
        spaces.addAll(getCommonSpaces(groupCode, commonSpaces, Role.OBSERVER));
        spaces.addAll(getCommonSpaces(groupCode, commonSpaces, Role.USER));
        assertLevel(adminUser.getUserId(), Level.SPACE_ADMIN, spaces);
    }

    private List<String> getCommonSpaces(String groupCode, Map<Role, List<String>> commonSpaces, Role role)
    {
        return commonSpaces.get(role).stream().map(s -> groupCode + "_" + s).collect(Collectors.toList());
    }

    private void assertLevel(String userId, Level level, List<String> spaceCodes)
    {
        List<Level> levels = probeAuthorizationLevel(userId, spaceCodes);
        for (int i = 0; i < spaceCodes.size(); i++)
        {
            assertLevel(levels.get(i), level, userId, spaceCodes.get(i));
        }
    }

    private void assertLevel(Level actualLevel, Level expectedLevel, String userId, String spaceCode)
    {
        assertEquals(userId + ":" + spaceCode + ":" + actualLevel, userId + ":" + spaceCode + ":" + expectedLevel);
    }

    private List<String> getAllSpaces()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SearchResult<Space> spaces = v3api.searchSpaces(sessionToken, new SpaceSearchCriteria(), new SpaceFetchOptions());
        v3api.logout(sessionToken);
        List<String> spaceCodes = spaces.getObjects().stream().map(Space::getCode).collect(Collectors.toList());
        Collections.sort(spaceCodes);
        return spaceCodes;
    }

    private List<Person> getPersons(String... userIds)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Function<String, PersonPermId> mapper = u -> new PersonPermId(u);
        List<PersonPermId> permIds = Arrays.asList(userIds).stream().map(mapper).collect(Collectors.toList());
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Collection<Person> persons = v3api.getPersons(sessionToken, permIds, fetchOptions).values();
        v3api.logout(sessionToken);
        List<Person> result = new ArrayList<>(persons);
        Collections.sort(result, Comparator.comparing(Person::getUserId));
        return result;
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

    private String getOwnSpace(Principal principal)
    {
        return principal.getUserId().toUpperCase();
    }

    private List<Level> probeAuthorizationLevel(String userId, List<String> spaceCodes)
    {
        String sessionToken = v3api.login(userId, PASSWORD);
        try
        {
            List<Level> levels = new ArrayList<>();
            Session session = sessionManager.getSession(sessionToken);
            IOperationContext context = new OperationContext(session);
            for (String spaceCode : spaceCodes)
            {
                SpacePE space = new SpacePE();
                space.setCode(spaceCode);
                Level previousLevel = null;
                for (Level level : Level.values())
                {
                    try
                    {
                        level.probe(testService, context, space);
                    } catch (Exception e)
                    {
                        break;
                    }
                    previousLevel = level;
                }
                levels.add(previousLevel);
            }
            return levels;
        } finally
        {
            v3api.logout(sessionToken);
        }
    }

    private enum Level
    {
        NON
        {
            @Override
            public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
            {
            }
        },
        SPACE_OBSERVER
        {
            @Override
            public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
            {
                testService.allowedForSpaceObservers(context, space);
            }
        },
        SPACE_USER
        {
            @Override
            public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
            {
                testService.allowedForSpaceUsers(context, space);
            }
        },
        SPACE_ADMIN
        {
            @Override
            public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
            {
                testService.allowedForSpaceAdmins(context, space);
            }
        },
        INSTANCE_ADMIN
        {
            @Override
            public void probe(UserManagerTestService testService, IOperationContext context, SpacePE space)
            {
                testService.allowedForInstanceAdmins(context, space);
            }
        };

        public abstract void probe(UserManagerTestService testService, IOperationContext context, SpacePE space);
    }

}
