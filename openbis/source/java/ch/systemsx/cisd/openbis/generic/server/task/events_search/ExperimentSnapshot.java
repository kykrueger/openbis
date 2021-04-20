package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class ExperimentSnapshot extends AbstractSnapshot
{

    public String experimentCode;

    public String experimentPermId;

    public String projectPermId;

    public String unknownPermId;

    @Override protected String getKey()
    {
        return experimentPermId;
    }

    public static Set<String> getProjectPermIdsOrUnknown(Collection<ExperimentSnapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (ExperimentSnapshot snapshot : snapshots)
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
}