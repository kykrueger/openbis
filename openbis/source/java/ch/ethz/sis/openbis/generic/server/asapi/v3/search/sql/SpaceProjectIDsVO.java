package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import java.util.HashSet;
import java.util.Set;

public class SpaceProjectIDsVO
{

    private final Set<Long> spaceIds;

    private final Set<Long> projectIds;

    public SpaceProjectIDsVO() {
        this(new HashSet<>(), new HashSet<>());
    }

    public SpaceProjectIDsVO(final Set<Long> spaceIds, final Set<Long> projectIds) {
        this.spaceIds = spaceIds;
        this.projectIds = projectIds;
    }

    public Set<Long> getSpaceIds() {
        return spaceIds;
    }

    public Set<Long> getProjectIds() {
        return projectIds;
    }

}
