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

package ch.systemsx.cisd.openbis.generic.server.task;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.fetchoptions.RoleAssignmentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.MockLogger;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.test.ToStringMatcher;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagerTest
{
    private static final String SESSION_TOKEN = "session-123";

    private static final Principal U1 = new Principal("u1", "Albert", "Einstein", "a.e@abc.de");

    private static final Principal U2 = new Principal("u2", "Isaac", "Newton", "i.n@abc.de");

    private static final Principal U3 = new Principal("u3", "Alan", "Turing", "a.t@abc.de");

    private Mockery context;

    private IApplicationServerInternalApi service;

    private UserManager userManager;

    private MockLogger logger;

    @BeforeMethod
    public void setUp()
    {
        context = new Mockery();
        service = context.mock(IApplicationServerInternalApi.class);
        context.checking(new Expectations()
            {
                {
                    one(service).loginAsSystem();
                    will(returnValue(SESSION_TOKEN));

                    one(service).logout(SESSION_TOKEN);
                }
            });
        logger = new MockLogger();
        userManager = new UserManager(service, logger);
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testAddNewNormalUser()
    {
        // Given
        RecordingMatcher<List<IPersonId>> personsMatcher = prepareGetUsersWithRoleAssigments(new PersonBuilder(U1).get());
        RecordingMatcher<List<AuthorizationGroupPermId>> groupsMatcher = prepareGetAuthorizationGroups();

        userManager.addGroup("G1", new Group(), principals(U2, U1));

        // When
        userManager.manageUsers();

        // Then
        assertEquals(personsMatcher.recordedObject().toString(), "[u1, u2]");
        assertEquals(groupsMatcher.recordedObject().toString(), "[G1]");
        context.assertIsSatisfied();
    }

    // @Test
    public void test2()
    {
        // Given

        // When
        userManager.manageUsers();

        // Then
        context.assertIsSatisfied();
    }

    // @Test
    public void test3()
    {
        // Given

        // When
        userManager.manageUsers();

        // Then
        context.assertIsSatisfied();
    }

    private RecordingMatcher<List<IPersonId>> prepareGetUsersWithRoleAssigments(Person... persons)
    {
        Map<IPersonId, Person> result = new LinkedHashMap<>();
        for (Person person : persons)
        {
            result.put(person.getPermId(), person);
        }
        RecordingMatcher<List<IPersonId>> matcher = new RecordingMatcher<>();
        context.checking(new Expectations()
            {
                {
                    PersonFetchOptions fetchOptions = new PersonFetchOptions();
                    fetchOptions.withRoleAssignments().withSpace();
                    one(service).getPersons(with(SESSION_TOKEN), with(matcher), with(new ToStringMatcher<>(fetchOptions)));
                    will(returnValue(result));
                }
            });
        return matcher;
    }

    private RecordingMatcher<List<AuthorizationGroupPermId>> prepareGetAuthorizationGroups(AuthorizationGroup... authorizationGroups)
    {
        Map<IAuthorizationGroupId, AuthorizationGroup> result = new LinkedHashMap<>();
        for (AuthorizationGroup authorizationGroup : authorizationGroups)
        {
            result.put(authorizationGroup.getPermId(), authorizationGroup);
        }
        RecordingMatcher<List<AuthorizationGroupPermId>> matcher = new RecordingMatcher<>();
        context.checking(new Expectations()
            {
                {
                    AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
                    fetchOptions.withUsers();
                    one(service).getAuthorizationGroups(with(SESSION_TOKEN), with(matcher), with(new ToStringMatcher<>(fetchOptions)));
                    will(returnValue(result));
                }
            });
        return matcher;
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

    private RoleAssignment ra(Role role)
    {
        return ra(role, null);
    }

    private RoleAssignment ra(Role role, String spaceCodeOrNull)
    {
        RoleAssignment roleAssignment = new RoleAssignment();
        RoleAssignmentFetchOptions fetchOptions = new RoleAssignmentFetchOptions();
        fetchOptions.withSpace();
        roleAssignment.setFetchOptions(fetchOptions);
        roleAssignment.setRole(role);
        roleAssignment.setRoleLevel(RoleLevel.INSTANCE);
        if (spaceCodeOrNull != null)
        {
            Space space = new Space();
            space.setCode(spaceCodeOrNull);
            space.setPermId(new SpacePermId(spaceCodeOrNull));
            space.setFetchOptions(new SpaceFetchOptions());
            roleAssignment.setSpace(space);
            roleAssignment.setRoleLevel(RoleLevel.SPACE);
        }
        return roleAssignment;
    }

    private static Person createPerson(Principal principal, PersonFetchOptions fetchOptions)
    {
        Person person = new Person();
        person.setFetchOptions(fetchOptions);
        person.setUserId(principal.getUserId());
        person.setPermId(new PersonPermId(principal.getUserId()));
        person.setEmail(principal.getEmail());
        person.setFirstName(principal.getFirstName());
        person.setLastName(principal.getLastName());
        person.setRoleAssignments(new ArrayList<RoleAssignment>());
        person.setActive(true);
        return person;
    }

    private static final class PersonBuilder
    {
        private Person person;

        PersonBuilder(Principal principal)
        {
            PersonFetchOptions fetchOptions = new PersonFetchOptions();
            fetchOptions.withRoleAssignments().withSpace();
            person = createPerson(principal, fetchOptions);
        }

        Person get()
        {
            return person;
        }

        PersonBuilder roleAssignments(RoleAssignment... roleAssignments)
        {
            for (RoleAssignment roleAssignment : roleAssignments)
            {
                person.getRoleAssignments().add(roleAssignment);
            }
            return this;
        }

        PersonBuilder deactive()
        {
            person.setActive(false);
            return this;
        }
    }

    private static final class AuthorizationGroupBuilder
    {
        private AuthorizationGroup authorizationGroup;

        AuthorizationGroupBuilder(String code)
        {
            authorizationGroup = new AuthorizationGroup();
            AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
            fetchOptions.withUsers();
            authorizationGroup.setFetchOptions(fetchOptions);
            authorizationGroup.setCode(code);
            authorizationGroup.setPermId(new AuthorizationGroupPermId(code));
            authorizationGroup.setUsers(new ArrayList<>());
        }

        public AuthorizationGroupBuilder users(Principal... principals)
        {
            PersonFetchOptions personFetchOptions = new PersonFetchOptions();
            Function<Principal, Person> mapper = p -> createPerson(p, personFetchOptions);
            authorizationGroup.getUsers().addAll(Arrays.asList(principals).stream().map(mapper).collect(Collectors.toList()));
            return this;
        }

        AuthorizationGroup get()
        {
            return authorizationGroup;
        }
    }
}
