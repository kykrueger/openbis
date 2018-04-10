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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.AuthorizationGroup;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.fetchoptions.AuthorizationGroupFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.AuthorizationGroupPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.authorizationgroup.id.IAuthorizationGroupId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.Person;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.IPersonId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * @author Franz-Josef Elmer
 */
class UserManager
{
    private final IApplicationServerInternalApi service;

    private final ISimpleLogger logger;
    
    private final Map<String, UserInfo> userInfosByUserId = new TreeMap<>();
    
    private final List<String> groupCodes = new ArrayList<>();

    UserManager(IApplicationServerInternalApi service, ISimpleLogger logger)
    {
        this.service = service;
        this.logger = logger;
    }

    public void addGroup(String key, Group group, Map<String, Principal> principalsByUserId)
    {
        groupCodes.add(key);
        Set<String> admins = asSet(group.getAdmins());
        Set<String> blackListedUsers = asSet(group.getUsersBlackList());
        for (Principal principal : principalsByUserId.values())
        {
            String userId = principal.getUserId();
            UserInfo userInfo = userInfosByUserId.get(userId);
            if (userInfo == null)
            {
                userInfo = new UserInfo(principal);
                userInfosByUserId.put(userId, userInfo);
            }
            userInfo.addGroupInfo(new GroupInfo(key, admins.contains(userId), blackListedUsers.contains(userId)));
        }
        logger.log(LogLevel.INFO, principalsByUserId.size() + " users for group " + key);
    }

    public void manageUsers()
    {
        String sessionToken = service.loginAsSystem();
        Map<IPersonId, Person> users = getUsersWithRoleAssigments(sessionToken);
        Map<IAuthorizationGroupId, AuthorizationGroup> authorizationGroups = getAuthorizationGroups(sessionToken);
        for (UserInfo userInfo : userInfosByUserId.values())
        {
            manageUser(userInfo, users, authorizationGroups);
        }
        service.logout(sessionToken);
    }
    
    private void manageUser(UserInfo userInfo, Map<IPersonId, Person> knownUsers, 
            Map<IAuthorizationGroupId, AuthorizationGroup> knownAuthorizationGroups)
    {
        
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

        private boolean onBlackList;

        GroupInfo(String key, boolean admin, boolean onBlackList)
        {
            this.key = key;
            this.admin = admin;
            this.onBlackList = onBlackList;
        }

        public String getKey()
        {
            return key;
        }

        public boolean isAdmin()
        {
            return admin;
        }

        public boolean isOnBlackList()
        {
            return onBlackList;
        }

        @Override
        public String toString()
        {
            return (onBlackList ? "." : "") + key + (admin ? "*" : "");
        }

    }

}
