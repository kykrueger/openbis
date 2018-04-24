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

import java.util.ArrayList;
import java.util.Arrays;
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.CreateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.AuthorizationGroupUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.update.UpdateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.operation.IOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.operation.SynchronousOperationExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.CreatePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.PersonUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.update.UpdatePersonsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.CreateRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * @author Franz-Josef Elmer
 */
public class UserManager
{
    private final IApplicationServerInternalApi service;

    private final ISimpleLogger logger;

    private final Map<Role, List<String>> commonSpacesByRole;

    private final Map<String, UserInfo> userInfosByUserId = new TreeMap<>();

    private final List<String> groupCodes = new ArrayList<>();

    public UserManager(IApplicationServerInternalApi service, Map<Role, List<String>> commonSpacesByRole, ISimpleLogger logger)
    {
        this.service = service;
        this.commonSpacesByRole = commonSpacesByRole;
        this.logger = logger;
    }

    public void addGroup(String key, UserGroup group, Map<String, Principal> principalsByUserId)
    {
        groupCodes.add(key.toUpperCase());
        Set<String> admins = asSet(group.getAdmins());
        for (Principal principal : principalsByUserId.values())
        {
            String userId = principal.getUserId();
            UserInfo userInfo = userInfosByUserId.get(userId);
            if (userInfo == null)
            {
                userInfo = new UserInfo(principal);
                userInfosByUserId.put(userId, userInfo);
            }
            userInfo.addGroupInfo(new GroupInfo(key, admins.contains(userId)));
        }
        logger.log(LogLevel.INFO, principalsByUserId.size() + " users for group " + key);
    }

    public String manageUsers()
    {
        String sessionToken = service.loginAsSystem();
        Map<IPersonId, Person> users = getUsersWithRoleAssigments(sessionToken);
        Map<IAuthorizationGroupId, AuthorizationGroup> authorizationGroups = getAuthorizationGroups(sessionToken);
        StringBuilder builder = new StringBuilder();
        for (String groupCode : groupCodes)
        {
            try
            {
                manageGroup(sessionToken, groupCode, authorizationGroups);
            } catch (Exception e)
            {
                String message = String.format("Couldn't manage group '%s' because of the following error: %s",
                        groupCode, e);
                builder.append(message).append("\n");
                logger.log(LogLevel.ERROR, message, e);
            }
        }
        authorizationGroups = getAuthorizationGroups(sessionToken);
        for (UserInfo userInfo : userInfosByUserId.values())
        {
            try
            {
                manageUser(sessionToken, userInfo, users, authorizationGroups);
            } catch (Exception e)
            {
                String message = String.format("Couldn't manage user '%s' because of the following error: %s",
                        userInfo.getPrincipal().getUserId(), e);
                builder.append(message).append("\n");
                logger.log(LogLevel.ERROR, message, e);
            }
        }
        service.logout(sessionToken);
        return builder.toString();
    }

    private void manageGroup(String sessionToken, String groupCode, Map<IAuthorizationGroupId, AuthorizationGroup> knownAuthorizationGroups)
    {
        Context context = new Context(sessionToken);
        AuthorizationGroup knownGroup = knownAuthorizationGroups.get(new AuthorizationGroupPermId(groupCode));
        if (knownGroup != null)
        {
            manageKnownGroup(context, knownGroup);
        } else
        {
            manageNewGroup(context, groupCode);
        }
        context.executeOperations();
    }

    private void manageNewGroup(Context context, String groupCode)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);
        assertAuthorizationGroupDoesNotExist(context, adminGroupCode);
        assertNoCommonSpaceExists(context, groupCode);

        createAuthorizationGroup(context, groupCode);
        createAuthorizationGroup(context, adminGroupCode);

        for (Entry<Role, List<String>> entry : commonSpacesByRole.entrySet())
        {
            Role role = entry.getKey();
            for (String space : entry.getValue())
            {
                ISpaceId spaceId = createSpace(context, createCommonSpaceCode(groupCode, space));
                createRoleAssignment(context, new AuthorizationGroupPermId(groupCode), role, spaceId);
                createRoleAssignment(context, new AuthorizationGroupPermId(adminGroupCode), Role.ADMIN, spaceId);
            }
        }
    }

    private void assertAuthorizationGroupDoesNotExist(Context context, String adminGroup)
    {
        if (service.getAuthorizationGroups(context.getSessionToken(), Arrays.asList(new AuthorizationGroupPermId(adminGroup)),
                new AuthorizationGroupFetchOptions()).isEmpty() == false)
        {
            throw new IllegalStateException("Authorization group " + adminGroup + " already exists.");
        }
    }

    private void assertNoCommonSpaceExists(Context context, String groupCode)
    {
        Set<String> commonSpaces = new TreeSet<>();
        for (List<String> set : commonSpacesByRole.values())
        {
            commonSpaces.addAll(set.stream().map(s -> createCommonSpaceCode(groupCode, s)).collect(Collectors.toList()));
        }
        Map<ISpaceId, Space> spaces = getSpaces(context.getSessionToken(), commonSpaces);
        if (spaces.isEmpty())
        {
            return;
        }
        List<String> existingSpaces = new ArrayList<>(spaces.values()).stream().map(Space::getCode).collect(Collectors.toList());
        Collections.sort(existingSpaces);
        throw new IllegalStateException("The group '" + groupCode + "' has already the following spaces: " + existingSpaces);
    }

    private void manageKnownGroup(Context context, AuthorizationGroup knownGroup)
    {
        // TODO Auto-generated method stub

    }

    private void manageUser(String sessionToken, UserInfo userInfo, Map<IPersonId, Person> knownUsers,
            Map<IAuthorizationGroupId, AuthorizationGroup> knownAuthorizationGroups)
    {
        Context context = new Context(sessionToken);
        Person knownUser = knownUsers.get(new PersonPermId(userInfo.getPrincipal().getUserId()));
        if (knownUser != null)
        {
            manageKnownUser(context, userInfo, knownUser, knownAuthorizationGroups);
        } else
        {
            manageNewUser(context, userInfo, knownAuthorizationGroups);
        }
        context.executeOperations();
    }

    private void manageKnownUser(Context context, UserInfo userInfo, Person knownUser,
            Map<IAuthorizationGroupId, AuthorizationGroup> knownAuthorizationGroups)
    {
        System.out.println("UserManager.manageKnownUser() " + userInfo);
    }

    private void manageNewUser(Context context, UserInfo userInfo,
            Map<IAuthorizationGroupId, AuthorizationGroup> knownAuthorizationGroups)
    {
        Principal principal = userInfo.getPrincipal();
        String userId = principal.getUserId();
        String spaceCode = userId.toUpperCase();
        if (getSpaces(context.getSessionToken(), Arrays.asList(spaceCode)).isEmpty() == false)
        {
            throw new IllegalStateException("There is already a space with code " + spaceCode + ".");
        }

        ISpaceId homeSpaceId = createSpace(context, spaceCode);
        IPersonId personId = createPerson(context, userId);
        assignHomeSpace(context, homeSpaceId, personId);

        for (GroupInfo groupInfo : userInfo.getGroupInfosByGroupKey().values())
        {
            String groupCode = groupInfo.getKey();
            addPersonToAuthorizationGroup(context, new AuthorizationGroupPermId(groupCode), personId);
            AuthorizationGroupPermId adminGroupId = new AuthorizationGroupPermId(createAdminGroupCode(groupCode));
            createRoleAssignment(context, adminGroupId, Role.ADMIN, homeSpaceId);
            if (groupInfo.isAdmin())
            {
                addPersonToAuthorizationGroup(context, adminGroupId, personId);
            }
        }
    }

    private void assignHomeSpace(Context context, ISpaceId homeSpaceId, IPersonId personId)
    {
        PersonUpdate personUpdate = new PersonUpdate();
        personUpdate.setUserId(personId);
        personUpdate.setSpaceId(homeSpaceId);
        context.add(personUpdate);
        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setUserId(personId);
        roleCreation.setRole(Role.ADMIN);
        roleCreation.setSpaceId(homeSpaceId);
        context.add(roleCreation);
    }

    private void addPersonToAuthorizationGroup(Context context, AuthorizationGroupPermId groupId, IPersonId personId)
    {
        AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
        groupUpdate.setAuthorizationGroupId(groupId);
        groupUpdate.getUserIds().add(personId);
        context.add(groupUpdate);
    }

    private ISpaceId createSpace(Context context, String spaceCode)
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(spaceCode);
        context.add(spaceCreation);
        return new SpacePermId(spaceCode);
    }

    private IPersonId createPerson(Context context, String userId)
    {
        PersonCreation personCreation = new PersonCreation();
        personCreation.setUserId(userId);
        context.add(personCreation);
        return new PersonPermId(userId);
    }

    private void createAuthorizationGroup(Context context, String groupCode)
    {
        AuthorizationGroupCreation creation = new AuthorizationGroupCreation();
        creation.setCode(groupCode);
        context.add(creation);
    }

    private void createRoleAssignment(Context context, AuthorizationGroupPermId groupId, Role role, ISpaceId spaceId)
    {
        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setAuthorizationGroupId(groupId);
        roleCreation.setRole(role);
        roleCreation.setSpaceId(spaceId);
        context.add(roleCreation);
    }

    private String createCommonSpaceCode(String groupCode, String spaceCode)
    {
        return groupCode + "_" + spaceCode;
    }

    private String createAdminGroupCode(String groupCode)
    {
        return groupCode + "_ADMIN";
    }

    private Map<ISpaceId, Space> getSpaces(String sessionToken, Collection<String> spaceCodes)
    {
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        return service.getSpaces(sessionToken, spaceCodes.stream().map(SpacePermId::new).collect(Collectors.toList()), fetchOptions);
    }

    private Map<IPersonId, Person> getUsersWithRoleAssigments(String sessionToken)
    {
        Function<String, PersonPermId> mapper = userId -> new PersonPermId(userId);
        List<PersonPermId> userIds = userInfosByUserId.keySet().stream().map(mapper).collect(Collectors.toList());
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        Map<IPersonId, Person> users = service.getPersons(sessionToken, userIds, fetchOptions);
        return users;
    }

    private Map<IAuthorizationGroupId, AuthorizationGroup> getAuthorizationGroups(String sessionToken)
    {
        Function<String, AuthorizationGroupPermId> mapper = key -> new AuthorizationGroupPermId(key);
        List<AuthorizationGroupPermId> groupPermIds = groupCodes.stream().map(mapper).collect(Collectors.toList());
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers();
        return service.getAuthorizationGroups(sessionToken, groupPermIds, fetchOptions);
    }

    private Set<String> asSet(List<String> users)
    {
        return users == null ? Collections.emptySet() : new TreeSet<>(users);
    }

    private static class UserInfo
    {
        private Principal principal;

        private Map<String, GroupInfo> groupInfosByGroupKey = new TreeMap<>();

        public UserInfo(Principal principal)
        {
            this.principal = principal;
        }

        public Principal getPrincipal()
        {
            return principal;
        }

        public void addGroupInfo(GroupInfo groupInfo)
        {
            groupInfosByGroupKey.put(groupInfo.getKey(), groupInfo);
        }

        public Map<String, GroupInfo> getGroupInfosByGroupKey()
        {
            return groupInfosByGroupKey;
        }

        @Override
        public String toString()
        {
            return principal.getUserId() + " " + groupInfosByGroupKey.values();
        }
    }

    private static class GroupInfo
    {
        private String key;

        private boolean admin;

        GroupInfo(String key, boolean admin)
        {
            this.key = key;
            this.admin = admin;
        }

        public String getKey()
        {
            return key;
        }

        public boolean isAdmin()
        {
            return admin;
        }

        @Override
        public String toString()
        {
            return key + (admin ? "*" : "");
        }

    }

    private final class Context
    {
        private String sessionToken;

        private List<PersonCreation> personCreations = new ArrayList<>();

        private List<PersonUpdate> personUpdates = new ArrayList<>();

        private List<SpaceCreation> spaceCreations = new ArrayList<>();

        private List<AuthorizationGroupCreation> groupCreations = new ArrayList<>();

        private List<AuthorizationGroupUpdate> groupUpdates = new ArrayList<>();

        private List<RoleAssignmentCreation> roleCreations = new ArrayList<>();

        Context(String sessionToken)
        {
            this.sessionToken = sessionToken;
        }

        public String getSessionToken()
        {
            return sessionToken;
        }

        public void add(PersonCreation personCreation)
        {
            personCreations.add(personCreation);
        }

        public void add(PersonUpdate personUpdate)
        {
            personUpdates.add(personUpdate);
        }

        public void add(SpaceCreation spaceCreation)
        {
            spaceCreations.add(spaceCreation);
        }

        public void add(AuthorizationGroupCreation creation)
        {
            groupCreations.add(creation);
        }

        public void add(RoleAssignmentCreation roleCreation)
        {
            roleCreations.add(roleCreation);
        }

        public void add(AuthorizationGroupUpdate groupUpdate)
        {
            groupUpdates.add(groupUpdate);
        }

        public void executeOperations()
        {
            List<IOperation> operations = new ArrayList<>();
            if (personCreations.isEmpty() == false)
            {
                operations.add(new CreatePersonsOperation(personCreations));
            }
            if (personUpdates.isEmpty() == false)
            {
                operations.add(new UpdatePersonsOperation(personUpdates));
            }
            if (spaceCreations.isEmpty() == false)
            {
                operations.add(new CreateSpacesOperation(spaceCreations));
            }
            if (groupCreations.isEmpty() == false)
            {
                operations.add(new CreateAuthorizationGroupsOperation(groupCreations));
            }
            if (groupUpdates.isEmpty() == false)
            {
                operations.add(new UpdateAuthorizationGroupsOperation(groupUpdates));
            }
            if (roleCreations.isEmpty() == false)
            {
                operations.add(new CreateRoleAssignmentsOperation(roleCreations));
            }
            if (operations.isEmpty())
            {
                return;
            }
            SynchronousOperationExecutionOptions options = new SynchronousOperationExecutionOptions();
            service.executeOperations(sessionToken, operations, options);
        }
    }
}
