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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;

class UserManagerConfig
{
    private List<String> globalSpaces = new ArrayList<>();

    private Map<Role, List<String>> commonSpaces = new HashMap<>();

    private Map<String, String> commonSamples = new HashMap<>();

    private List<UserGroup> groups;

    public List<String> getGlobalSpaces()
    {
        return globalSpaces;
    }

    public void setGlobalSpaces(List<String> globalSpaces)
    {
        this.globalSpaces = globalSpaces;
    }

    public Map<Role, List<String>> getCommonSpaces()
    {
        return commonSpaces;
    }

    public void setCommonSpaces(Map<Role, List<String>> commonSpaces)
    {
        this.commonSpaces = commonSpaces;
    }

    public Map<String, String> getCommonSamples()
    {
        return commonSamples;
    }

    public void setCommonSamples(Map<String, String> commonSamples)
    {
        this.commonSamples = commonSamples;
    }

    public List<UserGroup> getGroups()
    {
        return groups;
    }

    public void setGroups(List<UserGroup> groups)
    {
        this.groups = groups;
    }

}