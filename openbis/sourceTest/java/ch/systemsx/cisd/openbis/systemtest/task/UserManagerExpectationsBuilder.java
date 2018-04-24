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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import ch.systemsx.cisd.openbis.generic.shared.IOpenBisSessionManager;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

class UserManagerExpectationsBuilder
{
    private static final String TEST_USER = "test";

    private static final String PASSWORD = "password";

    private final IApplicationServerInternalApi v3api;

    private final UserManagerTestService testService;

    private final IOpenBisSessionManager sessionManager;

    private final Map<Role, List<String>> commonSpaces;

    private Map<String, List<Principal>> usersByGroup = new TreeMap<>();

    private Map<String, List<Principal>> disabledUsersByGroup = new TreeMap<>();

    private Map<String, List<Principal>> normalUsersByGroup = new TreeMap<>();

    private Map<String, List<Principal>> adminUsersByGroup = new TreeMap<>();

    UserManagerExpectationsBuilder(IApplicationServerInternalApi v3api, UserManagerTestService testService,
            IOpenBisSessionManager sessionManager, Map<Role, List<String>> commonSpaces)
    {
        this.v3api = v3api;
        this.testService = testService;
        this.sessionManager = sessionManager;
        this.commonSpaces = commonSpaces;
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
        return addUser(user, adminUsersByGroup, groups);
    }

    private UserManagerExpectationsBuilder addUser(Principal user, Map<String, List<Principal>> users, String... groups)
    {
        for (String group : groups)
        {
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
        assertUsers(sessionToken);
        assertAuthorization(sessionToken);
        v3api.logout(sessionToken);
    }

    private void assertSpaces(String sessionToken)
    {
        List<SpacePermId> allCommonSpaces = getAllCommonSpaces();
        assertSpacesExist(sessionToken, allCommonSpaces);
        List<SpacePermId> allUserSpaces = applyMapperToAllUsers(user -> new SpacePermId(user.getUserId().toUpperCase()));
        assertSpacesExist(sessionToken, allUserSpaces);
    }

    private List<SpacePermId> getAllCommonSpaces()
    {
        List<SpacePermId> allCommonSpaces = new ArrayList<>();
        Set<String> groups = usersByGroup.keySet();
        for (String group : groups)
        {
            for (List<String> list : commonSpaces.values())
            {
                for (String commonSpace : list)
                {
                    allCommonSpaces.add(new SpacePermId(createCommonSpaceCode(group, commonSpace)));
                }
            }
        }
        return allCommonSpaces;
    }

    private void assertSpacesExist(String sessionToken, List<SpacePermId> spaces)
    {
        List<String> expectedSpaces = extractedSortedPermIds(spaces);
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        List<String> actualSpaces = extractedSortedCodes(v3api.getSpaces(sessionToken, spaces, fetchOptions).values());
        System.out.println("UserManagerTestExpectation: Spaces: " + expectedSpaces);
        assertEquals(actualSpaces.toString(), expectedSpaces.toString());
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
        System.out.println("UserManagerTestExpectation: Users: " + expectedUsers);
        assertEquals(actualUsers.toString(), expectedUsers.toString());
        for (PersonPermId id : allUsers)
        {
            Person person = persons.get(id);
            assertEquals(person.getUserId(), id.getPermId());
            assertEquals(person.getEmail(), "franz-josef.elmer@systemsx.ch", "Wrong email of " + person);
            assertEquals(person.getSpace().getCode(), person.getUserId().toUpperCase(), "Wrong home space of " + person);
        }
    }

    private void assertAuthorization(String sessionToken)
    {
        AuthorizationExpectations expectations = new AuthorizationExpectations(v3api, sessionManager, testService);
        createExpectationsForDisabledUsers(expectations);
        createExpectationsForNormalUsers(expectations);
        createExpectationsForAdminUsers(expectations);
        expectations.assertExpectations();
    }

    private void createExpectationsForDisabledUsers(AuthorizationExpectations expectations)
    {
        for (Entry<String, List<Principal>> entry : disabledUsersByGroup.entrySet())
        {
            String groupKey = entry.getKey();
            for (Principal user : entry.getValue())
            {
                commonSpaces.get(Role.USER).forEach(expectForSpace(expectations, user, groupKey, Level.NON));
                commonSpaces.get(Role.OBSERVER).forEach(expectForSpace(expectations, user, groupKey, Level.NON));
                for (List<Principal> users2 : usersByGroup.values())
                {
                    users2.forEach(user2 -> expectations.expect(user, getOwenSpace(user2), Level.NON));
                }
            }
        }
    }

    private void createExpectationsForNormalUsers(AuthorizationExpectations expectations)
    {
        for (Entry<String, List<Principal>> entry : normalUsersByGroup.entrySet())
        {
            String groupKey = entry.getKey();
            for (Principal user : entry.getValue())
            {
                commonSpaces.get(Role.USER).forEach(expectForSpace(expectations, user, groupKey, Level.SPACE_USER));
                commonSpaces.get(Role.OBSERVER).forEach(expectForSpace(expectations, user, groupKey, Level.SPACE_OBSERVER));
                for (List<Principal> users2 : usersByGroup.values())
                {
                    users2.forEach(user2 -> expectations.expect(user, getOwenSpace(user2),
                            equals(user, user2) ? Level.SPACE_ADMIN : Level.NON));
                }
            }
        }
    }

    private void createExpectationsForAdminUsers(AuthorizationExpectations expectations)
    {
        for (Entry<String, List<Principal>> entry : adminUsersByGroup.entrySet())
        {
            String groupKey = entry.getKey();
            for (Principal user : entry.getValue())
            {
                commonSpaces.get(Role.USER).forEach(expectForSpace(expectations, user, groupKey, Level.SPACE_ADMIN));
                commonSpaces.get(Role.OBSERVER).forEach(expectForSpace(expectations, user, groupKey, Level.SPACE_ADMIN));
                Set<Entry<String, List<Principal>>> entrySet = usersByGroup.entrySet();
                for (Entry<String, List<Principal>> entry2 : entrySet)
                {
                    String group2Key = entry2.getKey();
                    List<Principal> users2 = entry2.getValue();
                    if (groupKey.equals(group2Key))
                    {
                        users2.forEach(user2 -> expectations.expect(user, getOwenSpace(user2), Level.SPACE_ADMIN));
                    } else
                    {
                        users2.forEach(user2 -> expectations.expect(user, getOwenSpace(user2), Level.NON));
                    }
                }
            }
        }
    }

    private Consumer<String> expectForSpace(AuthorizationExpectations expectations, Principal user, String groupKey, Level level)
    {
        return space -> expectations.expect(user, createCommonSpaceCode(groupKey, space), level);
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
                            System.out.println("UserManagerTestExpectation: " + level + " for user " + userId + " on space " + space);
                            if (level.equals(actualLevel) == false)
                            {
                                builder.append("Authorization level for user ").append(userId).append(" on space ").append(space);
                                builder.append(". Expected: ").append(level).append(", but found: ").append(actualLevel).append("\n");
                            }
                        }
                    }
                } finally
                {
                    v3api.logout(sessionToken);
                }
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
        List<T> result = new ArrayList<>();
        for (List<Principal> users : usersByGroup.values())
        {
            result.addAll(users.stream().map(mapper).collect(Collectors.toList()));
        }
        return result;
    }

    private static String createCommonSpaceCode(String groupCode, String spaceCode)
    {
        return groupCode + "_" + spaceCode;
    }

    private static String getOwenSpace(Principal user)
    {
        return user.getUserId().toUpperCase();
    }

    private static boolean equals(Principal user1, Principal user2)
    {
        return user1.getUserId().equals(user2.getUserId());
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