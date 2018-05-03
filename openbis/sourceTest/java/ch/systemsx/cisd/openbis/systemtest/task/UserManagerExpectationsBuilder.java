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
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.OperationContext;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.openbis.generic.server.task.UserManager;
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

class UserManagerExpectationsBuilder
{
    static final String USER_SPACE_POSTFIX_KEY = "user-space-postfix";

    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private final IApplicationServerInternalApi v3api;

    private final UserManagerTestService testService;

    private final IOpenBisSessionManager sessionManager;

    private final Map<Role, List<String>> commonSpaces;

    private Map<String, Map<String, String>> userSpacesByGroupAndUserId = new TreeMap<>();

    private Map<String, List<Principal>> usersByGroup = new TreeMap<>();

    private List<Principal> unknownUsers = new ArrayList<>();

    private Map<String, List<Principal>> disabledUsersByGroup = new TreeMap<>();

    private Map<String, List<Principal>> normalUsersByGroup = new TreeMap<>();

    private Map<String, List<Principal>> adminUsersByGroup = new TreeMap<>();

    private Map<String, Set<String>> groupsByAdminUsers = new TreeMap<>();

    private Map<String, Set<String>> userSpacesByGroups = new TreeMap<>();

    private Map<String, String> homeSpacesByUserId = new TreeMap<>();

    private List<String> globalSpaces = new ArrayList<>();;

    UserManagerExpectationsBuilder(IApplicationServerInternalApi v3api, UserManagerTestService testService,
            IOpenBisSessionManager sessionManager, Map<Role, List<String>> commonSpaces)
    {
        this.v3api = v3api;
        this.testService = testService;
        this.sessionManager = sessionManager;
        this.commonSpaces = commonSpaces;
    }

    public void setGlobalSpaces(List<String> globalSpaces)
    {
        this.globalSpaces = globalSpaces;
    }

    UserManagerExpectationsBuilder unknownUser(Principal user)
    {
        unknownUsers.add(user);
        return this;
    }

    UserManagerExpectationsBuilder disabledUser(Principal user, String... groups)
    {
        return addUser(user, disabledUsersByGroup, groups);
    }

    UserManagerExpectationsBuilder user(Principal user, String... groups)
    {
        return addUser(user, normalUsersByGroup, groups);
    }

    UserManagerExpectationsBuilder adminUser(Principal user, String... groups)
    {
        Set<String> usersAdminGroups = groupsByAdminUsers.get(user.getUserId());
        if (usersAdminGroups == null)
        {
            usersAdminGroups = new TreeSet<>();
            groupsByAdminUsers.put(user.getUserId(), usersAdminGroups);
        }
        usersAdminGroups.addAll(Arrays.asList(groups));
        return addUser(user, adminUsersByGroup, groups);
    }

    private UserManagerExpectationsBuilder addUser(Principal user, Map<String, List<Principal>> users, String... groups)
    {
        String userId = user.getUserId();
        if (groups.length == 0)
        {
            fail("No groups specified for " + userId);
        }
        String userSpacePostfix = user.getProperty(USER_SPACE_POSTFIX_KEY);
        if (userSpacePostfix == null)
        {
            userSpacePostfix = userId.toUpperCase();
        }
        for (String group : groups)
        {
            if (homeSpacesByUserId.containsKey(userId) == false)
            {
                homeSpacesByUserId.put(userId, group + "_" + userSpacePostfix);
            }
            Map<String, String> userSpacesByUserId = userSpacesByGroupAndUserId.get(group);
            if (userSpacesByUserId == null)
            {
                userSpacesByUserId = new TreeMap<>();
                userSpacesByGroupAndUserId.put(group, userSpacesByUserId);
            }
            userSpacesByUserId.put(userId, group + "_" + userSpacePostfix);
            addUserToGroup(user, group, users);
            addUserToGroup(user, group, usersByGroup);
        }
        return this;
    }

    private void addUserToGroup(Principal user, String group, Map<String, List<Principal>> users)
    {
        List<Principal> principals = users.get(group);
        if (principals == null)
        {
            principals = new ArrayList<>();
            users.put(group, principals);
        }
        principals.add(user);
    }

    void assertExpectations()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        assertSpaces(sessionToken);
        assertUnknownUsers(sessionToken);
        assertUsers(sessionToken);
        assertAuthorization(sessionToken);
        v3api.logout(sessionToken);
    }

    private void assertSpaces(String sessionToken)
    {
        List<SpacePermId> expectedSpaces = globalSpaces.stream().map(SpacePermId::new).collect(Collectors.toList());
        for (Entry<String, List<Principal>> entry : usersByGroup.entrySet())
        {
            String groupCode = entry.getKey();
            for (List<String> list : commonSpaces.values())
            {
                for (String commonSpace : list)
                {
                    expectedSpaces.add(new SpacePermId(createCommonSpaceCode(groupCode, commonSpace)));
                }
            }
            for (Principal user : entry.getValue())
            {
                expectedSpaces.add(new SpacePermId(userSpacesByGroupAndUserId.get(groupCode).get(user.getUserId())));
            }
        }
        List<String> expectedSpaceCodes = extractedSortedPermIds(expectedSpaces);
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        List<String> actualSpaces = extractedSortedCodes(v3api.getSpaces(sessionToken, expectedSpaces, fetchOptions).values());
        assertEquals(actualSpaces.toString(), expectedSpaceCodes.toString(), "Spaces");
    }

    private void assertUnknownUsers(String sessionToken)
    {
        Map<String, Set<String>> usersByGroupId = getAllUsersOfGroups(sessionToken);
        assertEquals(usersByGroupId.isEmpty(), false);
        List<PersonPermId> personIds = unknownUsers.stream().map(p -> new PersonPermId(p.getUserId())).collect(Collectors.toList());
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRoleAssignments();
        for (Person person : v3api.getPersons(sessionToken, personIds, fetchOptions).values())
        {
            String userId = person.getUserId();
            assertEquals(person.isActive(), Boolean.FALSE, "Active flag of user " + userId);
            assertEquals(person.getRoleAssignments().size(), 0, "Role assignments of user " + userId);
            for (Entry<String, Set<String>> entry : usersByGroupId.entrySet())
            {
                if (entry.getValue().contains(userId))
                {
                    fail("User " + userId + " still in authorization group " + entry.getKey());
                }
            }
        }
    }

    private Map<String, Set<String>> getAllUsersOfGroups(String sessionToken)
    {
        AuthorizationGroupFetchOptions groupFetchOptions = new AuthorizationGroupFetchOptions();
        groupFetchOptions.withUsers();
        List<AuthorizationGroupPermId> ids = new ArrayList<>();
        for (String groupCode : usersByGroup.keySet())
        {
            ids.add(new AuthorizationGroupPermId(groupCode));
            ids.add(new AuthorizationGroupPermId(UserManager.createAdminGroupCode(groupCode)));
        }
        Map<String, Set<String>> usersByGroupId = new TreeMap<>();
        for (AuthorizationGroup group : v3api.getAuthorizationGroups(sessionToken, ids, groupFetchOptions).values())
        {
            usersByGroupId.put(group.getCode(), group.getUsers().stream().map(Person::getUserId).collect(Collectors.toSet()));
        }
        return usersByGroupId;
    }

    private void assertUsers(String sessionToken)
    {
        List<PersonPermId> allUsers = applyMapperToAllUsers(user -> new PersonPermId(user.getUserId()));
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withSpace();
        Map<IPersonId, Person> persons = v3api.getPersons(sessionToken, allUsers, fetchOptions);
        Function<IPersonId, PersonPermId> mapper = id -> (PersonPermId) id;
        List<String> expectedUsers = extractedSortedPermIds(allUsers);
        List<String> actualUsers = extractedSortedPermIds(persons.keySet().stream().map(mapper).collect(Collectors.toSet()));
        assertEquals(actualUsers.toString(), expectedUsers.toString(), "Users");
        for (PersonPermId id : allUsers)
        {
            Person person = persons.get(id);
            assertEquals(person.getUserId(), id.getPermId());
            assertEquals(person.isActive(), Boolean.TRUE);
            assertEquals(person.getEmail(), "franz-josef.elmer@systemsx.ch", "Wrong email of " + person);
            assertEquals(person.getSpace().getCode(), homeSpacesByUserId.get(person.getUserId()), "Wrong home space of " + person);
        }
    }

    private void assertAuthorization(String sessionToken)
    {
        assertAuthorizationOfGlobalSpaces();
        assertAuthorizationOfCommonSpaces(disabledUsersByGroup, Level.NON, Level.NON);
        assertAuthorizationOfCommonSpaces(normalUsersByGroup, Level.SPACE_USER, Level.SPACE_OBSERVER);
        assertAuthorizationOfCommonSpaces(adminUsersByGroup, Level.SPACE_ADMIN, Level.SPACE_ADMIN);
        assertAuthorizationOfUserSpaces();
    }

    private void assertAuthorizationOfGlobalSpaces()
    {
        if (globalSpaces.isEmpty() == false)
        {
            AuthorizationExpectations expectations = new AuthorizationExpectations(v3api, sessionManager, testService);
            for (List<Principal> users : usersByGroup.values())
            {
                for (Principal user : users)
                {
                    globalSpaces.forEach(space -> expectations.expect(user, space, Level.SPACE_OBSERVER));
                }
            }
            expectations.assertExpectations();
        }
    }

    private void assertAuthorizationOfCommonSpaces(Map<String, List<Principal>> users, Level userLevel, Level observerLevel)
    {
        AuthorizationExpectations expectations = new AuthorizationExpectations(v3api, sessionManager, testService);
        for (Entry<String, List<Principal>> entry : users.entrySet())
        {
            String groupCode = entry.getKey();
            for (Principal user : entry.getValue())
            {
                commonSpaces.get(Role.USER).forEach(expectForSpace(expectations, user, groupCode, userLevel));
                commonSpaces.get(Role.OBSERVER).forEach(expectForSpace(expectations, user, groupCode, observerLevel));
            }
        }
        expectations.assertExpectations();
    }
    
    private void assertAuthorizationOfUserSpaces()
    {
        Set<String> allUserSpaces = new TreeSet<>();
        for (Entry<String, List<Principal>> entry : usersByGroup.entrySet())
        {
            String groupCode = entry.getKey();
            for (Principal user : entry.getValue())
            {
                allUserSpaces.add(userSpacesByGroupAndUserId.get(groupCode).get(user.getUserId()));
            }
        }
        AuthorizationExpectations expectations = new AuthorizationExpectations(v3api, sessionManager, testService);
        for (List<Principal> users : usersByGroup.values())
        {
            for (Principal user : users)
            {
                Set<String> accessibleUserSpaces = getAllAccessibleUserSpacesFor(user.getUserId());
                for (String space : allUserSpaces)
                {
                    expectations.expect(user, space, accessibleUserSpaces.contains(space) ? Level.SPACE_ADMIN : Level.NON);
                }
            }
        }
        expectations.assertExpectations();
    }

    private Set<String> getAllAccessibleUserSpacesFor(String userId)
    {
        Set<String> spaces = new TreeSet<>();
        for (String group : usersByGroup.keySet())
        {
            Map<String, String> userSpacesByUserId = userSpacesByGroupAndUserId.get(group);
            if (userSpacesByUserId != null)
            {
                String space = userSpacesByUserId.get(userId);
                if (space != null)
                {
                    spaces.add(space);
                }
            }
        }
        Set<String> groups = groupsByAdminUsers.get(userId);
        if (groups != null)
        {
            for (String group : groups)
            {
                Map<String, String> userSpacesByUserId = userSpacesByGroupAndUserId.get(group);
                if (userSpacesByUserId != null)
                {
                    userSpacesByUserId.values().forEach(space -> spaces.add(space));
                }
            }
        }
        System.err.println(userId + " is admin of " + spaces);
        return spaces;
    }

    private Consumer<String> expectForSpace(AuthorizationExpectations expectations, Principal user, String groupCode, Level level)
    {
        return space -> expectations.expect(user, createCommonSpaceCode(groupCode, space), level);
    }

    private static final class AuthorizationExpectations
    {
        private Map<String, Map<Level, Set<String>>> spacesByLevelsByUsers = new TreeMap<>();

        private IApplicationServerInternalApi v3api;

        private IOpenBisSessionManager sessionManager;

        private UserManagerTestService testService;

        public AuthorizationExpectations(IApplicationServerInternalApi v3api, IOpenBisSessionManager sessionManager,
                UserManagerTestService testService)
        {
            this.v3api = v3api;
            this.sessionManager = sessionManager;
            this.testService = testService;
        }

        void expect(Principal user, String space, Level level)
        {
            String userId = user.getUserId();
            Map<Level, Set<String>> spacesByLevels = spacesByLevelsByUsers.get(userId);
            if (spacesByLevels == null)
            {
                spacesByLevels = new TreeMap<>();
                spacesByLevelsByUsers.put(userId, spacesByLevels);
            }
            Set<String> spaces = spacesByLevels.get(level);
            if (spaces == null)
            {
                spaces = new TreeSet<>();
                spacesByLevels.put(level, spaces);
            }
            spaces.add(space);
        }

        void assertExpectations()
        {
            StringBuilder builder = new StringBuilder();
            for (Entry<String, Map<Level, Set<String>>> entry : spacesByLevelsByUsers.entrySet())
            {
                String userId = entry.getKey();
                String sessionToken = v3api.login(userId, PASSWORD);
                int count = 0;
                try
                {
                    Session session = sessionManager.getSession(sessionToken);
                    IOperationContext context = new OperationContext(session);
                    for (Entry<Level, Set<String>> entry2 : entry.getValue().entrySet())
                    {
                        Level level = entry2.getKey();
                        for (String space : entry2.getValue())
                        {
                            Level actualLevel = getActualLevel(context, space);
                            if (level.equals(actualLevel) == false)
                            {
                                builder.append("Authorization level for user ").append(userId).append(" on space ").append(space);
                                builder.append(". Expected: ").append(level).append(", but found: ").append(actualLevel).append("\n");
                            }
                            count++;
                        }
                    }
                } finally
                {
                    v3api.logout(sessionToken);
                }
                System.out.println(count + " authorization expectations tested for user " + userId);
            }
            if (builder.length() > 0)
            {
                fail(builder.toString().trim());
            }
        }

        private Level getActualLevel(IOperationContext context, String spaceCode)
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
            return previousLevel;
        }
    }

    private List<String> extractedSortedPermIds(Collection<? extends ObjectPermId> ids)
    {
        List<String> result = ids.stream().map(ObjectPermId::getPermId).collect(Collectors.toList());
        Collections.sort(result);
        return result;
    }

    private List<String> extractedSortedCodes(Collection<? extends ICodeHolder> codeHolders)
    {
        List<String> result = codeHolders.stream().map(ICodeHolder::getCode).collect(Collectors.toList());
        Collections.sort(result);
        return result;
    }

    private <T> List<T> applyMapperToAllUsers(Function<Principal, T> mapper)
    {
        Set<T> result = new LinkedHashSet<>();
        for (List<Principal> users : usersByGroup.values())
        {
            result.addAll(users.stream().map(mapper).collect(Collectors.toList()));
        }
        return new ArrayList<>(result);
    }

    private static String createCommonSpaceCode(String groupCode, String spaceCode)
    {
        return groupCode + "_" + spaceCode;
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