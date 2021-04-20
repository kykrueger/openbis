package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

class Snapshot
{
    public String entityCode;

    public String entityPermId;

    public String spaceCode;

    public String projectPermId;

    public String experimentPermId;

    public String unknownPermId;

    public Date from;

    public Date to;

    public static Set<String> getSpaceCodesOrUnknown(Collection<Snapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (Snapshot snapshot : snapshots)
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

    public static Set<String> getProjectPermIdsOrUnknown(Collection<Snapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.projectPermId != null)
            {
                result.add(snapshot.projectPermId);
            } else if (snapshot.unknownPermId != null)
            {
                result.add(snapshot.unknownPermId);
            }
        }
        return result;
    }

    public static Set<String> getExperimentPermIdsOrUnknown(Collection<Snapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (Snapshot snapshot : snapshots)
        {
            if (snapshot.experimentPermId != null)
            {
                result.add(snapshot.experimentPermId);
            } else if (snapshot.unknownPermId != null)
            {
                result.add(snapshot.unknownPermId);
            }
        }
        return result;
    }

}
