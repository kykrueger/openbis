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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.testng.Assert;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.ICodeHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
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

    private Map<String, List<Principal>> noUsersByGroup = new TreeMap<>();

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

    UserManagerExpectationsBuilder noUser(Principal user, String... groups)
    {
        return addUser(user, noUsersByGroup, groups);
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
                    allCommonSpaces.add(new SpacePermId(group + "_" + commonSpace));
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
        for (Entry<String, List<Principal>> entry : normalUsersByGroup.entrySet())
        {
            String groupKey = entry.getKey();
            List<Principal> users = entry.getValue();
            users.forEach(user -> expectations.expect(user, groupKey, Level.SPACE_USER));
        }
    }

    private static final class AuthorizationExpectations
    {
        private Map<String, Map<Level, Set<String>>> groupsByLevelsByUsers = new TreeMap<>();

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

        void expect(Principal user, String group, Level level)
        {
            String userId = user.getUserId();
            Map<Level, Set<String>> groupsByLevels = groupsByLevelsByUsers.get(userId);
            if (groupsByLevels == null)
            {
                groupsByLevels = new TreeMap<>();
                groupsByLevelsByUsers.put(userId, groupsByLevels);
            }
            Set<String> groups = groupsByLevels.get(level);
            if (groups == null)
            {
                groups = new TreeSet<>();
                groupsByLevels.put(level, groups);
            }
            groups.add(group);
        }

        void assertExpectations()
        {

            for (Entry<String, Map<Level, Set<String>>> entry : groupsByLevelsByUsers.entrySet())
            {
                String userId = entry.getKey();
                String sessionToken = v3api.login(userId, PASSWORD);

                try
                {
                    Session session = sessionManager.getSession(sessionToken);
                    IOperationContext context = new OperationContext(session);
                    Set<Entry<Level, Set<String>>> entrySet = entry.getValue().entrySet();
                    for (Entry<Level, Set<String>> entry2 : entrySet)
                    {
                        Level level = entry2.getKey();
                        Set<String> groups = entry2.getValue();
                        for (String group : groups)
                        {
//                            getActualLevel(context, spaceCode);
                        }
//                        probeAuthorizationLevel(user, spaceCodes);
                    }
                } finally
                {
                    v3api.logout(sessionToken);
                }

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
        
        private static final class LevelAndSpace
        {
            private Level level;
            private String spaceCode;

            LevelAndSpace(Level level, String spaceCode)
            {
                this.level = level;
                this.spaceCode = spaceCode;
            }
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
                Level previousLevel = getActualLevel(context, spaceCode);
                levels.add(previousLevel);
            }
            return levels;
        } finally
        {
            v3api.logout(sessionToken);
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