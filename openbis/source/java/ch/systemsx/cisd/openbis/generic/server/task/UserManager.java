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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.search.PersonSearchCriteria;
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
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * @author Franz-Josef Elmer
 */
public class UserManager
{
    private final IAuthenticationService authenticationService;

    private final IApplicationServerInternalApi service;

    private final ISimpleLogger logger;

    private final ITimeProvider timeProvider;

    private final Map<Role, List<String>> commonSpacesByRole;

    private final Map<String, UserInfo> userInfosByUserId = new TreeMap<>();

    private final Map<String, Map<String, Principal>> usersByGroupCode = new TreeMap<>();

    private final List<String> groupCodes = new ArrayList<>();

    public UserManager(IAuthenticationService authenticationService, IApplicationServerInternalApi service,
            Map<Role, List<String>> commonSpacesByRole, ISimpleLogger logger, ITimeProvider timeProvider)
    {
        this.authenticationService = authenticationService;
        this.service = service;
        this.commonSpacesByRole = commonSpacesByRole;
        this.logger = logger;
        this.timeProvider = timeProvider;
    }

    public void addGroup(UserGroup group, Map<String, Principal> principalsByUserId)
    {
        String groupCode = group.getKey().toUpperCase();
        usersByGroupCode.put(groupCode, principalsByUserId);
        groupCodes.add(groupCode);
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
            userInfo.addGroupInfo(new GroupInfo(groupCode, admins.contains(userId)));
        }
        logger.log(LogLevel.INFO, principalsByUserId.size() + " users for group " + groupCode);
    }

    public UserManagerReport manage()
    {
        UserManagerReport report = new UserManagerReport(timeProvider);
        try
        {
            String sessionToken = service.loginAsSystem();

            manageGroups(sessionToken, report);
            manageUsers(sessionToken, report);

            service.logout(sessionToken);
        } catch (Throwable e)
        {
            report.addErrorMessage("Error: " + e.toString());
            logger.log(LogLevel.ERROR, "", e);
        }
        return report;
    }

    private void manageGroups(String sessionToken, UserManagerReport report)
    {
        Map<String, Set<String>> usersByGroupCodes = getUsersByGroupCodes(sessionToken);
        for (String groupCode : groupCodes)
        {
            try
            {
                Context context = new Context(sessionToken);
                if (usersByGroupCodes.containsKey(groupCode))
                {
                    manageKnownGroup(context, groupCode, report);
                } else
                {
                    manageNewGroup(context, groupCode, report);
                }
                context.executeOperations();
            } catch (Exception e)
            {
                String message = String.format("Couldn't manage group '%s' because of the following error: %s",
                        groupCode, e);
                report.addErrorMessage(message);
                logger.log(LogLevel.ERROR, message, e);
            }
        }
    }

    private void manageUsers(String sessionToken, UserManagerReport report)
    {
        revokeUsersUnkownByAuthenticationService(sessionToken, report);
        Map<IPersonId, Person> users = getUsersWithRoleAssigments(sessionToken);
        Map<String, Set<String>> currentUsersByGroupCodes = getUsersByGroupCodes(sessionToken);
        Map<String, Set<String>> currentAdminUsersByGroupCodes = getAdminUsersByGroupCodes(sessionToken);
        for (UserInfo userInfo : userInfosByUserId.values())
        {
            try
            {
                manageUser(sessionToken, userInfo, users, currentUsersByGroupCodes, currentAdminUsersByGroupCodes, report);
            } catch (Exception e)
            {
                String message = String.format("Couldn't manage user '%s' because of the following error: %s",
                        userInfo.getPrincipal().getUserId(), e);
                report.addErrorMessage(message);
                logger.log(LogLevel.ERROR, message, e);
            }
        }

        for (String groupCode : currentUsersByGroupCodes.keySet())
        {
            try
            {
                manageUsersRemovedFromGroup(sessionToken, groupCode, currentUsersByGroupCodes,
                        currentAdminUsersByGroupCodes, report);
            } catch (Exception e)
            {
                String message = String.format("Couldn't manage users removed from group %s because of the following error: %s",
                        groupCode, e);
                report.addErrorMessage(message);
                logger.log(LogLevel.ERROR, message, e);
            }
        }
    }

    private void manageUsersRemovedFromGroup(String sessionToken, String groupCode, Map<String, Set<String>> currentUsersByGroupCodes,
            Map<String, Set<String>> currentAdminUsersByGroupCodes, UserManagerReport report)
    {
        Set<String> currentUsers = currentUsersByGroupCodes.get(groupCode);
        Set<String> newUsers = usersByGroupCode.get(groupCode).keySet();
        Context context = new Context(sessionToken);
        for (String currentUser : currentUsers)
        {
            if (newUsers.contains(currentUser) == false)
            {
                removePersonFromAuthorizationGroup(context, currentUsersByGroupCodes, groupCode, currentUser, report);
                String adminGroupCode = createAdminGroupCode(groupCode);
                removePersonFromAuthorizationGroup(context, currentAdminUsersByGroupCodes, adminGroupCode, currentUser, report);
            }
        }
        context.executeOperations();
    }

    private void revokeUsersUnkownByAuthenticationService(String sessionToken, UserManagerReport report)
    {
        List<PersonUpdate> updates = new ArrayList<>();
        List<Person> persons = service.searchPersons(sessionToken, new PersonSearchCriteria(), new PersonFetchOptions()).getObjects();
        for (Person person : persons)
        {
            if (person.isActive())
            {
                try
                {
                    authenticationService.getPrincipal(person.getUserId());
                } catch (IllegalArgumentException e)
                {
                    PersonUpdate update = new PersonUpdate();
                    update.setUserId(person.getPermId());
                    update.deactivate();
                    updates.add(update);
                    report.deactivateUser(person.getUserId());
                }
            }
        }
        if (updates.isEmpty() == false)
        {
            service.updatePersons(sessionToken, updates);
        }
    }

    private void manageKnownGroup(Context context, String groupCode, UserManagerReport report)
    {
        // TODO Auto-generated method stub
    }

    private void manageNewGroup(Context context, String groupCode, UserManagerReport report)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);
        assertAuthorizationGroupDoesNotExist(context, adminGroupCode);
        assertNoCommonSpaceExists(context, groupCode);

        createAuthorizationGroup(context, groupCode, report);
        createAuthorizationGroup(context, adminGroupCode, report);

        for (Entry<Role, List<String>> entry : commonSpacesByRole.entrySet())
        {
            Role role = entry.getKey();
            for (String space : entry.getValue())
            {
                ISpaceId spaceId = createSpace(context, createCommonSpaceCode(groupCode, space));
                report.addSpace(spaceId);
                createRoleAssignment(context, new AuthorizationGroupPermId(groupCode), role, spaceId, report);
                createRoleAssignment(context, new AuthorizationGroupPermId(adminGroupCode), Role.ADMIN, spaceId, report);
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

    private void manageUser(String sessionToken, UserInfo userInfo, Map<IPersonId, Person> knownUsers,
            Map<String, Set<String>> currentUsersByGroupCodes, Map<String, Set<String>> currentAdminUsersByGroupCodes,
            UserManagerReport report)
    {
        Context context = new Context(sessionToken);
        Person knownUser = knownUsers.get(new PersonPermId(userInfo.getPrincipal().getUserId()));
        if (knownUser != null)
        {
            manageKnownUser(context, userInfo, knownUser, currentUsersByGroupCodes, currentAdminUsersByGroupCodes, report);
        } else
        {
            manageNewUser(context, userInfo, userInfo.getPrincipal().getUserId().toUpperCase(),
                    currentUsersByGroupCodes, currentAdminUsersByGroupCodes, report);
        }
        context.executeOperations();
    }

    private void manageKnownUser(Context context, UserInfo userInfo, Person knownUser,
            Map<String, Set<String>> currentUsersByGroupCodes, Map<String, Set<String>> currentAdminUsersByGroupCodes,
            UserManagerReport report)
    {
        if (knownUser.isActive() == false)
        {
            int maxSequenceNumber = getMaxSequenceNumber(context, knownUser);
            String homeSpaceCode = knownUser.getUserId() + "_" + (maxSequenceNumber + 1);
            manageNewUser(context, userInfo, homeSpaceCode, currentUsersByGroupCodes, currentAdminUsersByGroupCodes, report);
        } else
        {
            String userId = userInfo.principal.getUserId();
            for (GroupInfo groupInfo : userInfo.getGroupInfosByGroupKey().values())
            {
                String groupCode = groupInfo.getKey();
                addPersonToAuthorizationGroup(context, currentUsersByGroupCodes, groupCode, userId, report);
                String adminGroupCode = createAdminGroupCode(groupCode);
                if (groupInfo.isAdmin())
                {
                    addPersonToAuthorizationGroup(context, currentAdminUsersByGroupCodes, adminGroupCode, userId, report);
                } else if (isCurrentAdminUser(currentAdminUsersByGroupCodes, adminGroupCode, userId))
                {
                    removePersonFromAuthorizationGroup(context, currentAdminUsersByGroupCodes, adminGroupCode, userId, report);
                }
            }
        }
    }

    private int getMaxSequenceNumber(Context context, Person knownUser)
    {
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        searchCriteria.withUserId().thatStartsWith(knownUser.getUserId());
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        int maxSequenceNumber = 0;
        for (Person person : service.searchPersons(context.getSessionToken(), searchCriteria, fetchOptions).getObjects())
        {
            String[] splittedUserId = person.getUserId().split("_");
            if (splittedUserId.length == 2)
            {
                try
                {
                    maxSequenceNumber = Math.max(maxSequenceNumber, Integer.parseInt(splittedUserId[1]));
                } catch (NumberFormatException e)
                {
                    // silently ignored
                }
            }
        }
        return maxSequenceNumber;
    }

    private boolean isCurrentAdminUser(Map<String, Set<String>> currentAdminUsersByGroupCodes, String groupCode, String userId)
    {
        Set<String> users = currentAdminUsersByGroupCodes.get(groupCode);
        return users == null ? false : users.contains(userId);
    }

    private void manageNewUser(Context context, UserInfo userInfo, String homeSpaceCode,
            Map<String, Set<String>> currentUsersByGroupCodes, Map<String, Set<String>> currentAdminUsersByGroupCodes,
            UserManagerReport report)
    {
        String userId = userInfo.getPrincipal().getUserId();
        if (getSpaces(context.getSessionToken(), Arrays.asList(homeSpaceCode)).isEmpty() == false)
        {
            throw new IllegalStateException("There is already a space with code " + homeSpaceCode + ".");
        }

        ISpaceId homeSpaceId = createSpace(context, homeSpaceCode);
        createUserWithHomeSpace(context, userId, homeSpaceId, report);

        for (GroupInfo groupInfo : userInfo.getGroupInfosByGroupKey().values())
        {
            String groupCode = groupInfo.getKey();
            addPersonToAuthorizationGroup(context, currentUsersByGroupCodes, groupCode, userId, report);
            String adminGroupCode = createAdminGroupCode(groupCode);
            createRoleAssignment(context, new AuthorizationGroupPermId(adminGroupCode), Role.ADMIN, homeSpaceId, report);
            if (groupInfo.isAdmin())
            {
                addPersonToAuthorizationGroup(context, currentAdminUsersByGroupCodes, adminGroupCode, userId, report);
            }
        }
    }

    private void createUserWithHomeSpace(Context context, String userId, ISpaceId homeSpaceId, UserManagerReport report)
    {
        PersonPermId personPermId = new PersonPermId(userId);
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        if (service.getPersons(context.getSessionToken(), Arrays.asList(personPermId), fetchOptions).isEmpty())
        {
            PersonCreation personCreation = new PersonCreation();
            personCreation.setUserId(userId);
            context.add(personCreation);
            report.addUser(userId, homeSpaceId);
        } else
        {
            report.reuseUser(userId, homeSpaceId);
        }
        PersonUpdate personUpdate = new PersonUpdate();
        personUpdate.setUserId(personPermId);
        personUpdate.setSpaceId(homeSpaceId);
        context.add(personUpdate);
        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setUserId(personPermId);
        roleCreation.setRole(Role.ADMIN);
        roleCreation.setSpaceId(homeSpaceId);
        context.add(roleCreation);
    }

    private void addPersonToAuthorizationGroup(Context context, Map<String, Set<String>> currentUsersByGroupCodes,
            String groupCode, String userId, UserManagerReport report)
    {
        Set<String> users = currentUsersByGroupCodes.get(groupCode);
        if (users == null || users.contains(userId) == false)
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().add(new PersonPermId(userId));
            context.add(groupUpdate);
            report.addUserToGroup(groupCode, userId);
        }
    }

    private void removePersonFromAuthorizationGroup(Context context, Map<String, Set<String>> currentUsersByGroupCodes,
            String groupCode, String userId, UserManagerReport report)
    {
        Set<String> users = currentUsersByGroupCodes.get(groupCode);
        if (users == null || users.contains(userId))
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().remove(new PersonPermId(userId));
            context.add(groupUpdate);
            report.removeUserFromGroup(groupCode, userId);
        }
    }

    private ISpaceId createSpace(Context context, String spaceCode)
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(spaceCode);
        context.add(spaceCreation);
        return new SpacePermId(spaceCode);
    }

    private void createAuthorizationGroup(Context context, String groupCode, UserManagerReport report)
    {
        AuthorizationGroupCreation creation = new AuthorizationGroupCreation();
        creation.setCode(groupCode);
        context.add(creation);
        report.addGroup(groupCode);
    }

    private void createRoleAssignment(Context context, AuthorizationGroupPermId groupId, Role role, ISpaceId spaceId,
            UserManagerReport report)
    {
        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setAuthorizationGroupId(groupId);
        roleCreation.setRole(role);
        roleCreation.setSpaceId(spaceId);
        context.add(roleCreation);
        report.assignRoleTo(groupId, role, spaceId);
    }

    private String createCommonSpaceCode(String groupCode, String spaceCode)
    {
        return groupCode + "_" + spaceCode;
    }

    public static String createAdminGroupCode(String groupCode)
    {
        return groupCode + "_ADMIN";
    }

    private AuthorizationGroupPermId createAdminGroupId(String groupCode)
    {
        return new AuthorizationGroupPermId(createAdminGroupCode(groupCode));
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

    private Map<String, Set<String>> getUsersByGroupCodes(String sessionToken)
    {
        return getUsersByGroupCodes(sessionToken, groupCode -> new AuthorizationGroupPermId(groupCode));
    }

    private Map<String, Set<String>> getAdminUsersByGroupCodes(String sessionToken)
    {
        return getUsersByGroupCodes(sessionToken, groupCode -> createAdminGroupId(groupCode));
    }

    private Map<String, Set<String>> getUsersByGroupCodes(String sessionToken, Function<String, AuthorizationGroupPermId> mapper)
    {
        List<AuthorizationGroupPermId> groupPermIds = groupCodes.stream().map(mapper).collect(Collectors.toList());
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers();
        Map<String, Set<String>> usersByGroupCodes = new TreeMap<>();
        for (AuthorizationGroup group : service.getAuthorizationGroups(sessionToken, groupPermIds, fetchOptions).values())
        {
            Set<String> users = group.getUsers().stream().map(Person::getUserId).collect(Collectors.toSet());
            usersByGroupCodes.put(group.getCode(), users);
        }
        return usersByGroupCodes;
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
