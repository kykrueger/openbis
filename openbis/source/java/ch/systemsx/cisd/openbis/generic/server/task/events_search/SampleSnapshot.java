package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

class SampleSnapshot extends AbstractSnapshot
{

    public String sampleCode;

    public String samplePermId;

    public String spaceCode;

    public String projectPermId;

    public String experimentPermId;

    public String unknownPermId;

    @Override protected String getKey()
    {
        return samplePermId;
    }

    public static Set<String> getSpaceCodesOrUnknown(Collection<SampleSnapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (SampleSnapshot snapshot : snapshots)
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

    public static Set<String> getProjectPermIdsOrUnknown(Collection<SampleSnapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (SampleSnapshot snapshot : snapshots)
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

    public static Set<String> getExperimentPermIdsOrUnknown(Collection<SampleSnapshot> snapshots)
    {
        Set<String> result = new HashSet<>();
        for (SampleSnapshot snapshot : snapshots)
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