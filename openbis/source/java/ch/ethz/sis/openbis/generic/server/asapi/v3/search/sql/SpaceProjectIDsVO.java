/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import java.util.HashSet;
import java.util.Set;

public class SpaceProjectIDsVO
{

    private boolean isInstanceAdmin;

    private boolean isETLServer;

    private final Set<Long> spaceIds;

    private final Set<Long> projectIds;

    public SpaceProjectIDsVO()
    {
        this(new HashSet<>(), new HashSet<>());
    }

    public SpaceProjectIDsVO(final Set<Long> spaceIds, final Set<Long> projectIds)
    {
        this.spaceIds = spaceIds;
        this.projectIds = projectIds;
    }

    public Set<Long> getSpaceIds()
    {
        return spaceIds;
    }

    public Set<Long> getProjectIds()
    {
        return projectIds;
    }

    public boolean isInstanceAdmin()
    {
        return isInstanceAdmin;
    }

    public void setInstanceAdmin(boolean instanceAdmin)
    {
        isInstanceAdmin = instanceAdmin;
    }

    public boolean isETLServer()
    {
        return isETLServer;
    }

    public void setETLServer(boolean ETLServer)
    {
        isETLServer = ETLServer;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SpaceProjectIDsVO that = (SpaceProjectIDsVO) o;

        if (!spaceIds.equals(that.spaceIds))
        {
            return false;
        }
        return projectIds.equals(that.projectIds);

    }

    @Override
    public int hashCode()
    {
        int result = spaceIds.hashCode();
        result = 31 * result + projectIds.hashCode();
        return result;
    }

}
