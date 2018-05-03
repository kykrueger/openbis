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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LinkedMap;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.AuthorizationGroupCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.create.CreateAuthorizationGroupsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.search.AuthorizationGroupSearchCriteria;
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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.RoleLevel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.CreateRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.create.RoleAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.DeleteRoleAssignmentsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.delete.RoleAssignmentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.id.IRoleAssignmentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.CreateSpacesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.ISpaceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
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
    private static final String GLOBAL_AUTHORIZATION_GROUP_CODE = "ALL_GROUPS";

    private static final AuthorizationGroupPermId GLOBAL_AUTHORIZATION_GROUP_ID = new AuthorizationGroupPermId(GLOBAL_AUTHORIZATION_GROUP_CODE);

    private final IAuthenticationService authenticationService;

    private final IApplicationServerInternalApi service;

    private final ISimpleLogger logger;

    private final ITimeProvider timeProvider;

    private final Map<String, UserInfo> userInfosByUserId = new TreeMap<>();

    private final Map<String, Map<String, Principal>> usersByGroupCode = new LinkedHashMap<>();

    private final List<String> groupCodes = new ArrayList<>();

    private List<String> globalSpaces = new ArrayList<>();

    private Map<Role, List<String>> commonSpacesByRole = new HashMap<>();

    private Map<String, String> samplesByType = new HashMap<>();

    public UserManager(IAuthenticationService authenticationService, IApplicationServerInternalApi service,
            ISimpleLogger logger, ITimeProvider timeProvider)
    {
        this.authenticationService = authenticationService;
        this.service = service;
        this.logger = logger;
        this.timeProvider = timeProvider;
    }

    public void setGlobalSpaces(List<String> globalSpaces)
    {
        this.globalSpaces = globalSpaces;
    }

    public void setCommonSpacesByRole(Map<Role, List<String>> commonSpacesByRole)
    {
        this.commonSpacesByRole = commonSpacesByRole;
    }

    public void setSamplesByType(Map<String, String> samplesByType)
    {
        this.samplesByType = samplesByType;
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

            manageGlobalSpaces(sessionToken, report);
            revokeUsersUnkownByAuthenticationService(sessionToken, report);
            CurrentState currentState = loadCurrentState(sessionToken, service);
            for (Entry<String, Map<String, Principal>> entry : usersByGroupCode.entrySet())
            {
                String groupCode = entry.getKey();
                manageGroup(sessionToken, groupCode, entry.getValue(), currentState, report);
            }

            service.logout(sessionToken);
        } catch (Throwable e)
        {
            report.addErrorMessage("Error: " + e.toString());
            logger.log(LogLevel.ERROR, "", e);
        }
        return report;
    }

    private void manageGlobalSpaces(String sessionToken, UserManagerReport report)
    {
        if (globalSpaces != null && globalSpaces.isEmpty() == false)
        {
            createGlobalSpaces(sessionToken, report);
            Set<String> knownGlobalSpaces = createGlobalGroupAndGetKnownSpaces(sessionToken, GLOBAL_AUTHORIZATION_GROUP_ID, report);
            createGlobalRoleAssignments(sessionToken, GLOBAL_AUTHORIZATION_GROUP_ID, knownGlobalSpaces, report);
        }
    }

    private void createGlobalSpaces(String sessionToken, UserManagerReport report)
    {
        List<SpacePermId> spaceIds = globalSpaces.stream().map(SpacePermId::new).collect(Collectors.toList());
        Set<ISpaceId> knownSpaces = service.getSpaces(sessionToken, spaceIds, new SpaceFetchOptions()).keySet();
        List<SpaceCreation> spaceCreations = new ArrayList<>();
        for (SpacePermId spaceId : spaceIds)
        {
            if (knownSpaces.contains(spaceId) == false)
            {
                SpaceCreation spaceCreation = new SpaceCreation();
                spaceCreation.setCode(spaceId.getPermId());
                spaceCreations.add(spaceCreation);
            }
        }
        if (spaceCreations.isEmpty() == false)
        {
            service.createSpaces(sessionToken, spaceCreations);
            report.addSpaces(spaceCreations);
        }
    }

    private Set<String> createGlobalGroupAndGetKnownSpaces(String sessionToken, AuthorizationGroupPermId groupId, UserManagerReport report)
    {
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        AuthorizationGroup group = service.getAuthorizationGroups(sessionToken, Arrays.asList(groupId), fetchOptions).get(groupId);
        Set<String> knownGlobalSpaces = new TreeSet<>();
        if (group == null)
        {
            AuthorizationGroupCreation groupCreation = new AuthorizationGroupCreation();
            groupCreation.setCode(GLOBAL_AUTHORIZATION_GROUP_CODE);
            groupCreation.setDescription("Authorization group for all users of all groups");
            service.createAuthorizationGroups(sessionToken, Arrays.asList(groupCreation));
            report.addGroup(GLOBAL_AUTHORIZATION_GROUP_CODE);
        } else
        {
            for (RoleAssignment roleAssignment : group.getRoleAssignments())
            {
                if (RoleLevel.SPACE.equals(roleAssignment.getRoleLevel()) && Role.OBSERVER.equals(roleAssignment.getRole()))
                {
                    knownGlobalSpaces.add(roleAssignment.getSpace().getCode());
                }
            }
        }
        return knownGlobalSpaces;
    }

    private void createGlobalRoleAssignments(String sessionToken, AuthorizationGroupPermId groupId, Set<String> knownGlobalSpaces,
            UserManagerReport report)
    {
        List<RoleAssignmentCreation> assignmentCreations = new ArrayList<>();
        for (String spaceCode : globalSpaces)
        {
            if (knownGlobalSpaces.contains(spaceCode) == false)
            {
                RoleAssignmentCreation assignmentCreation = new RoleAssignmentCreation();
                assignmentCreation.setAuthorizationGroupId(groupId);
                assignmentCreation.setRole(Role.OBSERVER);
                SpacePermId spaceId = new SpacePermId(spaceCode);
                assignmentCreation.setSpaceId(spaceId);
                assignmentCreations.add(assignmentCreation);
                report.assignRoleTo(groupId, assignmentCreation.getRole(), spaceId);
            }
        }
        if (assignmentCreations.isEmpty() == false)
        {
            service.createRoleAssignments(sessionToken, assignmentCreations);
        }
    }

    private void revokeUsersUnkownByAuthenticationService(String sessionToken, UserManagerReport report)
    {
        List<PersonUpdate> updates = new ArrayList<>();
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        List<Person> persons = service.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();
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

    private CurrentState loadCurrentState(String sessionToken, IApplicationServerInternalApi service)
    {
        List<AuthorizationGroup> authorizationGroups = getAllAuthorizationGroups(sessionToken, service);
        List<Person> users = getAllUsers(sessionToken, service);
        List<Space> spaces = getAllSpaces(sessionToken, service);
        List<AuthorizationGroupPermId> ids = Arrays.asList(GLOBAL_AUTHORIZATION_GROUP_ID);
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withUsers();
        AuthorizationGroup group = service.getAuthorizationGroups(sessionToken, ids, fetchOptions).get(GLOBAL_AUTHORIZATION_GROUP_ID);
        return new CurrentState(authorizationGroups, group, spaces, users);
    }

    private List<AuthorizationGroup> getAllAuthorizationGroups(String sessionToken, IApplicationServerInternalApi service)
    {
        AuthorizationGroupSearchCriteria searchCriteria = new AuthorizationGroupSearchCriteria();
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers().withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        return service.searchAuthorizationGroups(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private List<Person> getAllUsers(String sessionToken, IApplicationServerInternalApi service)
    {
        PersonSearchCriteria searchCriteria = new PersonSearchCriteria();
        PersonFetchOptions fetchOptions = new PersonFetchOptions();
        fetchOptions.withRoleAssignments().withSpace();
        fetchOptions.withSpace();
        return service.searchPersons(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private List<Space> getAllSpaces(String sessionToken, IApplicationServerInternalApi service)
    {
        SpaceSearchCriteria searchCriteria = new SpaceSearchCriteria();
        SpaceFetchOptions fetchOptions = new SpaceFetchOptions();
        return service.searchSpaces(sessionToken, searchCriteria, fetchOptions).getObjects();
    }

    private void manageGroup(String sessionToken, String groupCode, Map<String, Principal> groupUsers,
            CurrentState currentState, UserManagerReport report)
    {
        try
        {
            Context context = new Context(sessionToken, service, currentState, report);
            if (currentState.groupExists(groupCode))
            {
                manageKnownGroup(context, groupCode, groupUsers);
            } else
            {
                manageNewGroup(context, groupCode, groupUsers);
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

    private void manageKnownGroup(Context context, String groupCode, Map<String, Principal> groupUsers)
    {
        manageUsers(context, groupCode, groupUsers);
    }

    private void manageNewGroup(Context context, String groupCode, Map<String, Principal> groupUsers)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);
        assertNoCommonSpaceExists(context, groupCode);

        AuthorizationGroupPermId groupId = createAuthorizationGroup(context, groupCode);
        AuthorizationGroupPermId adminGroupId = createAuthorizationGroup(context, adminGroupCode);

        for (Entry<Role, List<String>> entry : commonSpacesByRole.entrySet())
        {
            Role role = entry.getKey();
            for (String space : entry.getValue())
            {
                ISpaceId spaceId = createSpace(context, createCommonSpaceCode(groupCode, space));
                createRoleAssignment(context, groupId, role, spaceId);
                createRoleAssignment(context, adminGroupId, Role.ADMIN, spaceId);
            }
        }

        manageUsers(context, groupCode, groupUsers);
    }

    private void manageUsers(Context context, String groupCode, Map<String, Principal> groupUsers)
    {
        Map<String, Person> currentUsersOfGroup = context.currentState.getCurrentUsersOfGroup(groupCode);
        Set<String> usersToBeRemoved = new TreeSet<>(currentUsersOfGroup.keySet());
        AuthorizationGroup globalGroup = context.currentState.getGlobalGroup();
        String adminGroupCode = createAdminGroupCode(groupCode);
        AuthorizationGroupPermId adminGroupId = new AuthorizationGroupPermId(adminGroupCode);
        for (Principal user : groupUsers.values())
        {
            String userId = user.getUserId();
            usersToBeRemoved.remove(userId);
            PersonPermId personId = new PersonPermId(userId);
            if (currentUsersOfGroup.containsKey(userId) == false)
            {
                ISpaceId userSpaceId = createUserSpace(context, groupCode, userId);
                Person knownUser = context.getCurrentState().getUser(userId);
                if (context.getCurrentState().userExists(userId) == false)
                {
                    PersonCreation personCreation = new PersonCreation();
                    personCreation.setUserId(userId);
                    context.add(personCreation);
                    context.getCurrentState().addNewUser(userId);
                    context.getReport().addUser(userId);
                    assignHomeSpace(context, personId, userSpaceId);
                } else if (knownUser != null)
                {
                    if (knownUser.isActive() == false)
                    {
                        context.getReport().reuseUser(userId);
                    }
                    if (knownUser.getSpace() == null || knownUser.isActive() == false)
                    {
                        assignHomeSpace(context, personId, userSpaceId);
                    }
                }
                RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
                roleCreation.setUserId(personId);
                roleCreation.setRole(Role.ADMIN);
                roleCreation.setSpaceId(userSpaceId);
                context.add(roleCreation);
                createRoleAssignment(context, adminGroupId, Role.ADMIN, userSpaceId);
            }
            addPersonToAuthorizationGroup(context, groupCode, userId);
            if (globalGroup != null)
            {
                addPersonToAuthorizationGroup(context, globalGroup.getCode(), userId);
            }
            if (isAdmin(userId, groupCode))
            {
                addPersonToAuthorizationGroup(context, adminGroupCode, userId);
            } else
            {
                removePersonFromAuthorizationGroup(context, adminGroupCode, userId);
            }
        }
        removeUsersFromGroup(context, groupCode, usersToBeRemoved);
    }
    
    private void removeUsersFromGroup(Context context, String groupCode, Set<String> usersToBeRemoved)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);
        Map<String, RoleAssignment> spaceRoles = context.currentState.getCurrentSpaceRolesOfGroup(adminGroupCode);
        for (String user : usersToBeRemoved)
        {
            removePersonFromAuthorizationGroup(context, groupCode, user);
            removePersonFromAuthorizationGroup(context, adminGroupCode, user);
            for (RoleAssignment role : spaceRoles.values())
            {
                if (role.getSpace().getCode().startsWith(groupCode + "_" + user))
                {
                    context.delete(role.getId());
                }
            }
        }
    }

    private ISpaceId createUserSpace(Context context, String groupCode, String userId)
    {
        String userSpaceCode = createCommonSpaceCode(groupCode, userId.toUpperCase());
        int n = context.getCurrentState().getNumberOfSpacesStartingWith(userSpaceCode);
        if (n > 0)
        {
            userSpaceCode += "_" + (n + 1);
        }
        return createSpace(context, userSpaceCode);
    }
    
    private void assignHomeSpace(Context context, PersonPermId personId, ISpaceId homeSpaceId)
    {
        PersonUpdate personUpdate = new PersonUpdate();
        personUpdate.setUserId(personId);
        personUpdate.setSpaceId(homeSpaceId);
        context.add(personUpdate);
    }
    
    private boolean isAdmin(String userId, String groupCode)
    {
        UserInfo userInfo = userInfosByUserId.get(userId);
        if (userInfo == null)
        {
            return false;
        }
        GroupInfo groupInfo = userInfo.getGroupInfosByGroupKey().get(groupCode);
        return groupInfo != null && groupInfo.isAdmin();
    }

    private void addPersonToAuthorizationGroup(Context context, String groupCode, String userId)
    {
        if (context.currentState.getCurrentUsersOfGroup(groupCode).keySet().contains(userId) == false)
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().add(new PersonPermId(userId));
            context.add(groupUpdate);
            context.getReport().addUserToGroup(groupCode, userId);
        }
    }

    private void removePersonFromAuthorizationGroup(Context context, String groupCode, String userId)
    {
        if (context.currentState.getCurrentUsersOfGroup(groupCode).keySet().contains(userId))
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().remove(new PersonPermId(userId));
            context.add(groupUpdate);
            context.getReport().removeUserFromGroup(groupCode, userId);
        }
    }

    private AuthorizationGroupPermId createAuthorizationGroup(Context context, String groupCode)
    {
        AuthorizationGroupCreation creation = new AuthorizationGroupCreation();
        creation.setCode(groupCode);
        context.add(creation);
        context.getReport().addGroup(groupCode);
        return new AuthorizationGroupPermId(groupCode);
    }

    private void createRoleAssignment(Context context, AuthorizationGroupPermId groupId, Role role, ISpaceId spaceId)
    {
        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setAuthorizationGroupId(groupId);
        roleCreation.setRole(role);
        roleCreation.setSpaceId(spaceId);
        context.add(roleCreation);
        context.getReport().assignRoleTo(groupId, role, spaceId);
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

    private static final class CurrentState
    {
        private Map<String, AuthorizationGroup> groupsByCode = new TreeMap<>();

        private Map<String, Space> spacesByCode = new TreeMap<>();

        private Map<String, Person> usersById = new TreeMap<>();

        private Set<String> newUsers = new TreeSet<>();

        private AuthorizationGroup globalGroup;

        CurrentState(List<AuthorizationGroup> authorizationGroups, AuthorizationGroup globalGroup, List<Space> spaces, List<Person> users)
        {
            this.globalGroup = globalGroup;
            authorizationGroups.forEach(group -> groupsByCode.put(group.getCode(), group));
            groupsByCode.put(GLOBAL_AUTHORIZATION_GROUP_CODE, globalGroup);
            spaces.forEach(space -> spacesByCode.put(space.getCode(), space));
            users.forEach(user -> usersById.put(user.getUserId(), user));
        }

        public Map<String, RoleAssignment> getCurrentSpaceRolesOfGroup(String groupCode)
        {
            Map<String, RoleAssignment> result = new TreeMap<>();
            AuthorizationGroup group = groupsByCode.get(groupCode);
            if (group != null)
            {
                List<RoleAssignment> roleAssignments = group.getRoleAssignments();
                for (RoleAssignment roleAssignment : roleAssignments)
                {
                    if (RoleLevel.SPACE.equals(roleAssignment.getRoleLevel()) && Role.OBSERVER.equals(roleAssignment.getRole()))
                    {
                        Space space = roleAssignment.getSpace();
                        result.put(space.getCode(), roleAssignment);
                    }
                }
            }
            return result;
        }

        public Map<String, Person> getCurrentUsersOfGroup(String groupCode)
        {
            Map<String, Person> result = new TreeMap<>();
            AuthorizationGroup group = groupsByCode.get(groupCode);
            if (group != null)
            {
                group.getUsers().forEach(user -> result.put(user.getUserId(), user));
            }
            return result;
        }

        public AuthorizationGroup getGlobalGroup()
        {
            return globalGroup;
        }

        public boolean userExists(String userId)
        {
            return newUsers.contains(userId) || usersById.containsKey(userId);
        }

        public int getNumberOfSpacesStartingWith(String userSpaceCode)
        {
            Predicate<String> predicate = code -> code.startsWith(userSpaceCode);
            return spacesByCode.keySet().stream().filter(predicate).collect(Collectors.counting()).intValue();
        }

        public Person getUser(String userId)
        {
            return usersById.get(userId);
        }

        public boolean groupExists(String groupCode)
        {
            boolean groupExists = groupsByCode.containsKey(groupCode);
            String adminGroupCode = createAdminGroupCode(groupCode);
            boolean adminGroupExists = groupsByCode.containsKey(adminGroupCode);
            if (groupExists)
            {
                if (adminGroupExists == false)
                {
                    throw new IllegalArgumentException("Group " + groupCode + " exists but not " + adminGroupCode);
                }
                return true;
            }
            if (adminGroupExists)
            {
                throw new IllegalArgumentException("Group " + groupCode + " does not exist but " + adminGroupCode);
            }
            return false;
        }

        public void addNewUser(String userId)
        {
            newUsers.add(userId);
        }
    }

    private void manageUsers(String sessionToken, Map<IPersonId, Person> currentUsers, UserManagerReport report)
    {
        Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByGroupCodes = getUsersAndSpacesByGroupCodes(sessionToken);
        Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByAdminGroupCodes = getUsersAndSpacesByAdminGroupCodes(sessionToken);
        for (UserInfo userInfo : userInfosByUserId.values())
        {
            try
            {
                manageUser(sessionToken, userInfo, currentUsers, currentUsersAndSpacesByGroupCodes, currentUsersAndSpacesByAdminGroupCodes, report);
            } catch (Exception e)
            {
                String message = String.format("Couldn't manage user '%s' because of the following error: %s",
                        userInfo.getPrincipal().getUserId(), e);
                report.addErrorMessage(message);
                logger.log(LogLevel.ERROR, message, e);
            }
        }

        for (String groupCode : currentUsersAndSpacesByGroupCodes.keySet())
        {
            try
            {
                manageUsersRemovedFromGroup(sessionToken, groupCode, currentUsersAndSpacesByGroupCodes,
                        currentUsersAndSpacesByAdminGroupCodes, report);
            } catch (Exception e)
            {
                String message = String.format("Couldn't manage users removed from group %s because of the following error: %s",
                        groupCode, e);
                report.addErrorMessage(message);
                logger.log(LogLevel.ERROR, message, e);
            }
        }
    }

    private void manageUsersRemovedFromGroup(String sessionToken, String groupCode,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByGroupCodes,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByAdminGroupCodes,
            UserManagerReport report)
    {
        Set<String> currentUsers = currentUsersAndSpacesByGroupCodes.get(groupCode).users;
        Set<String> newUsers = usersByGroupCode.get(groupCode).keySet();
        Context context = new Context(sessionToken, service, null, report);
        for (String currentUser : currentUsers)
        {
            if (newUsers.contains(currentUser) == false)
            {
                removePersonFromAuthorizationGroup(context, currentUsersAndSpacesByGroupCodes, groupCode, currentUser, report);
                String adminGroupCode = createAdminGroupCode(groupCode);
                removePersonFromAuthorizationGroup(context, currentUsersAndSpacesByAdminGroupCodes, adminGroupCode, currentUser, report);
            }
        }
        context.executeOperations();
    }

    private void manageKnownGroup(Context context, String groupCode, Map<IPersonId, Person> currentUsers, UserManagerReport report)
    {
        String adminGroupCode = createAdminGroupCode(groupCode);
        Map<String, UsersAndRoleAssignments> usersAndSpacesByAdminGroupCodes = getUsersAndSpacesByAdminGroupCodes(context.getSessionToken());
        UsersAndRoleAssignments usersAndSpaces = usersAndSpacesByAdminGroupCodes.get(adminGroupCode);
        Collection<Principal> users = usersByGroupCode.get(groupCode).values();
        if (usersAndSpaces != null && users != null)
        {
            for (Principal user : users)
            {
                String userId = user.getUserId();
                Person knownUser = currentUsers.get(new PersonPermId(userId));
                if (knownUser != null)
                {
                    Space homeSpace = knownUser.getSpace();
                    if (homeSpace != null)
                    {
                        RoleAssignment roleAssignment = usersAndSpaces.getRoleAssignment(homeSpace.getCode());
                        if (roleAssignment != null)
                        {
                            context.delete(roleAssignment.getId());
                            report.unassignRoleFrom(new AuthorizationGroupPermId(adminGroupCode), Role.ADMIN, homeSpace.getPermId());
                        }
                    }
                }
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

    private void manageUser(String sessionToken, UserInfo userInfo, Map<IPersonId, Person> knownUsers,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByGroupCodes,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByAdminGroupCodes,
            UserManagerReport report)
    {
        Context context = new Context(sessionToken, service, null, report);
        Person knownUser = knownUsers.get(new PersonPermId(userInfo.getPrincipal().getUserId()));
        if (knownUser != null)
        {
            manageKnownUser(context, userInfo, knownUser, currentUsersAndSpacesByGroupCodes,
                    currentUsersAndSpacesByAdminGroupCodes, report);
        } else
        {
            manageNewUser(context, userInfo, userInfo.getPrincipal().getUserId().toUpperCase(),
                    currentUsersAndSpacesByGroupCodes, currentUsersAndSpacesByAdminGroupCodes, report);
        }
        context.executeOperations();
    }

    private void manageKnownUser(Context context, UserInfo userInfo, Person knownUser,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByGroupCodes,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByAdminGroupCodes,
            UserManagerReport report)
    {
        int numberOfHomeSpaces = getNumberOfHomeSpaces(context, knownUser);
        if (knownUser.isActive() == false)
        {
            String homeSpaceCode = knownUser.getUserId().toUpperCase() + "_" + (numberOfHomeSpaces + 1);
            manageNewUser(context, userInfo, homeSpaceCode, currentUsersAndSpacesByGroupCodes,
                    currentUsersAndSpacesByAdminGroupCodes, report);
        } else
        {
            String userId = userInfo.principal.getUserId();
            for (GroupInfo groupInfo : userInfo.getGroupInfosByGroupKey().values())
            {
                String groupCode = groupInfo.getKey();
                addPersonToAuthorizationGroup(context, currentUsersAndSpacesByGroupCodes, groupCode, userId, report);
                String adminGroupCode = createAdminGroupCode(groupCode);
                String homeSpaceCode = knownUser.getUserId().toUpperCase() + (numberOfHomeSpaces > 1 ? "_" + numberOfHomeSpaces : "");
                UsersAndRoleAssignments usersAndSpaces = currentUsersAndSpacesByAdminGroupCodes.get(adminGroupCode);
                if (usersAndSpaces != null && usersAndSpaces.getRoleAssignment(homeSpaceCode) == null)
                {
                    AuthorizationGroupPermId adminGroupId = new AuthorizationGroupPermId(adminGroupCode);
                    SpacePermId homeSpaceId = new SpacePermId(homeSpaceCode);
                    createRoleAssignment(context, adminGroupId, Role.ADMIN, homeSpaceId);
                }
                if (groupInfo.isAdmin())
                {
                    addPersonToAuthorizationGroup(context, currentUsersAndSpacesByAdminGroupCodes, adminGroupCode, userId, report);
                } else if (isCurrentAdminUser(currentUsersAndSpacesByAdminGroupCodes, adminGroupCode, userId))
                {
                    removePersonFromAuthorizationGroup(context, currentUsersAndSpacesByAdminGroupCodes, adminGroupCode, userId, report);
                }
            }
        }
    }

    private int getNumberOfHomeSpaces(Context context, Person knownUser)
    {
        SpaceSearchCriteria searchCriteria = new SpaceSearchCriteria();
        searchCriteria.withCode().thatStartsWith(knownUser.getUserId().toUpperCase());
        return service.searchSpaces(context.getSessionToken(), searchCriteria, new SpaceFetchOptions()).getTotalCount();
    }

    private boolean isCurrentAdminUser(Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByAdminGroupCodes,
            String groupCode, String userId)
    {
        Set<String> users = currentUsersAndSpacesByAdminGroupCodes.get(groupCode).users;
        return users == null ? false : users.contains(userId);
    }

    private void manageNewUser(Context context, UserInfo userInfo, String homeSpaceCode,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByGroupCodes,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByAdminGroupCodes,
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
            addPersonToAuthorizationGroup(context, currentUsersAndSpacesByGroupCodes, groupCode, userId, report);
            String adminGroupCode = createAdminGroupCode(groupCode);
            createRoleAssignment(context, new AuthorizationGroupPermId(adminGroupCode), Role.ADMIN, homeSpaceId);
            if (groupInfo.isAdmin())
            {
                addPersonToAuthorizationGroup(context, currentUsersAndSpacesByAdminGroupCodes, adminGroupCode, userId, report);
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
            report.addUser(userId);
        } else
        {
            report.reuseUser(userId);
        }
        assignHomeSpace(context, personPermId, homeSpaceId);
        RoleAssignmentCreation roleCreation = new RoleAssignmentCreation();
        roleCreation.setUserId(personPermId);
        roleCreation.setRole(Role.ADMIN);
        roleCreation.setSpaceId(homeSpaceId);
        context.add(roleCreation);
    }

    private void addPersonToAuthorizationGroup(Context context,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByGroupCodes,
            String groupCode, String userId, UserManagerReport report)
    {
        Set<String> users = currentUsersAndSpacesByGroupCodes.get(groupCode).users;
        if (users == null || users.contains(userId) == false)
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().add(new PersonPermId(userId));
            context.add(groupUpdate);
            report.addUserToGroup(groupCode, userId);
        }
    }

    private void removePersonFromAuthorizationGroup(Context context,
            Map<String, UsersAndRoleAssignments> currentUsersAndSpacesByGroupCodes,
            String groupCode, String userId, UserManagerReport report)
    {
        Set<String> users = currentUsersAndSpacesByGroupCodes.get(groupCode).users;
        if (users == null || users.contains(userId))
        {
            AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
            groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
            groupUpdate.getUserIds().remove(new PersonPermId(userId));
            context.add(groupUpdate);
            report.removeUserFromGroup(groupCode, userId);
        }
    }

    private void removeSpaceAdminRoleFromAuthorizationGroup(Context context, String groupCode, String userId, UserManagerReport report)
    {

        AuthorizationGroupUpdate groupUpdate = new AuthorizationGroupUpdate();
        groupUpdate.setAuthorizationGroupId(new AuthorizationGroupPermId(groupCode));
    }

    private ISpaceId createSpace(Context context, String spaceCode)
    {
        SpaceCreation spaceCreation = new SpaceCreation();
        spaceCreation.setCode(spaceCode);
        context.add(spaceCreation);
        SpacePermId spaceId = new SpacePermId(spaceCode);
        context.getReport().addSpace(spaceId);
        return spaceId;
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
        fetchOptions.withSpace();
        fetchOptions.withRoleAssignments().withSpace();
        Map<IPersonId, Person> users = service.getPersons(sessionToken, userIds, fetchOptions);
        return users;
    }

    private Map<String, UsersAndRoleAssignments> getUsersAndSpacesByGroupCodes(String sessionToken)
    {
        return getUsersAndSpacesByGroupCodes(sessionToken, groupCode -> new AuthorizationGroupPermId(groupCode));
    }

    private Map<String, UsersAndRoleAssignments> getUsersAndSpacesByAdminGroupCodes(String sessionToken)
    {
        return getUsersAndSpacesByGroupCodes(sessionToken, groupCode -> createAdminGroupId(groupCode));
    }

    private Map<String, UsersAndRoleAssignments> getUsersAndSpacesByGroupCodes(String sessionToken, Function<String, AuthorizationGroupPermId> mapper)
    {
        List<AuthorizationGroupPermId> groupPermIds = groupCodes.stream().map(mapper).collect(Collectors.toList());
        AuthorizationGroupFetchOptions fetchOptions = new AuthorizationGroupFetchOptions();
        fetchOptions.withUsers();
        fetchOptions.withRoleAssignments().withSpace();
        Map<String, UsersAndRoleAssignments> usersByGroupCodes = new TreeMap<>();
        for (AuthorizationGroup group : service.getAuthorizationGroups(sessionToken, groupPermIds, fetchOptions).values())
        {
            Set<String> users = group.getUsers().stream().map(Person::getUserId).collect(Collectors.toSet());
            List<RoleAssignment> roleAssignments = group.getRoleAssignments().stream()
                    .filter(ra -> ra.getSpace() != null).collect(Collectors.toList());
            usersByGroupCodes.put(group.getCode(), new UsersAndRoleAssignments(users, roleAssignments));
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

    private static final class UsersAndRoleAssignments
    {
        private Set<String> users;

        private Map<String, RoleAssignment> roleAssignmentsBySpace = new TreeMap<>();

        UsersAndRoleAssignments(Set<String> users, List<RoleAssignment> roleAssignments)
        {
            this.users = users;
            for (RoleAssignment roleAssignment : roleAssignments)
            {
                roleAssignmentsBySpace.put(roleAssignment.getSpace().getCode(), roleAssignment);
            }
        }

        RoleAssignment getRoleAssignment(String spaceCode)
        {
            return roleAssignmentsBySpace.get(spaceCode);
        }

        @Override
        public String toString()
        {
            return "users: " + users + ", spaces: " + roleAssignmentsBySpace.keySet();
        }
    }

    private static final class Context
    {
        private String sessionToken;

        private Map<String, PersonCreation> personCreations = new LinkedMap<>();

        private List<PersonUpdate> personUpdates = new ArrayList<>();

        private List<SpaceCreation> spaceCreations = new ArrayList<>();

        private List<AuthorizationGroupCreation> groupCreations = new ArrayList<>();

        private List<AuthorizationGroupUpdate> groupUpdates = new ArrayList<>();

        private List<RoleAssignmentCreation> roleCreations = new ArrayList<>();

        private List<IRoleAssignmentId> roleDeletions = new ArrayList<>();

        private IApplicationServerInternalApi service;

        private CurrentState currentState;

        private UserManagerReport report;

        Context(String sessionToken, IApplicationServerInternalApi service, CurrentState currentState, UserManagerReport report)
        {
            this.sessionToken = sessionToken;
            this.service = service;
            this.currentState = currentState;
            this.report = report;
        }

        public String getSessionToken()
        {
            return sessionToken;
        }

        public CurrentState getCurrentState()
        {
            return currentState;
        }

        public UserManagerReport getReport()
        {
            return report;
        }

        public void add(PersonCreation personCreation)
        {
            personCreations.put(personCreation.getUserId(), personCreation);
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

        public void delete(IRoleAssignmentId roleAssignmentId)
        {
            roleDeletions.add(roleAssignmentId);
        }

        public void executeOperations()
        {
            List<IOperation> operations = new ArrayList<>();
            if (personCreations.isEmpty() == false)
            {
                operations.add(new CreatePersonsOperation(new ArrayList<>(personCreations.values())));
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
            if (roleDeletions.isEmpty() == false)
            {
                RoleAssignmentDeletionOptions options = new RoleAssignmentDeletionOptions();
                options.setReason("Users removed from a group");
                operations.add(new DeleteRoleAssignmentsOperation(roleDeletions, options));
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
