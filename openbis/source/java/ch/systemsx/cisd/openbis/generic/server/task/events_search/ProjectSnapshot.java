package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class ProjectSnapshot extends AbstractSnapshot
{

    public String projectCode;

    public String projectPermId;

    public String spaceCode;

    public String unknownPermId;

    @Override protected String getKey()
    {
        return projectPermId;
    }

    public static Set<String> getSpaceCodesOrUnknown(Collection<ProjectSnapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (ProjectSnapshot snapshot : snapshots)
        {
            if (snapshot.spaceCode != null)
            {
                result.add(snapshot.spaceCode);
            } else if (snapshot.unknownPermId != null)
            {
                result.add(snapshot.unknownPermId);
            }
        }
        return result;
    }

}
