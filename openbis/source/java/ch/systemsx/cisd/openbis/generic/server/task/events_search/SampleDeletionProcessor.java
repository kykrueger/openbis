package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class SampleDeletionProcessor extends EntityDeletionProcessor
{

    SampleDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected EntityType getEntityType()
    {
        return EntityType.SAMPLE;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return EnumSet.of(EntityType.SPACE, EntityType.PROJECT, EntityType.EXPERIMENT);
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return EnumSet.of(EntityType.DATASET);
    }

    @Override protected void process(LastTimestamps lastTimestamps, Snapshots snapshots, List<NewEvent> newEvents, List<Snapshot> newSnapshots)
    {
        snapshots.loadExistingSpaces(Snapshot.getSpaceCodesOrUnknown(newSnapshots));
        snapshots.loadExistingProjects(Snapshot.getProjectPermIdsOrUnknown(newSnapshots));
        snapshots.loadExistingExperiments(Snapshot.getExperimentPermIdsOrUnknown(newSnapshots));

        for (Snapshot newSnapshot : newSnapshots)
        {
            if (newSnapshot.unknownPermId == null)
            {
                snapshots.putDeletedSample(newSnapshot);
            } else
            {
                Snapshot experimentSnapshot = snapshots.getExperiment(newSnapshot.unknownPermId, newSnapshot.from);
                if (experimentSnapshot != null)
                {
                    newSnapshot.experimentPermId = newSnapshot.unknownPermId;
                    snapshots.putDeletedSample(newSnapshot);
                } else
                {
                    Snapshot projectSnapshot = snapshots.getProject(newSnapshot.unknownPermId, newSnapshot.from);
                    if (projectSnapshot != null)
                    {
                        newSnapshot.projectPermId = newSnapshot.unknownPermId;
                        snapshots.putDeletedSample(newSnapshot);
                    } else
                    {
                        Snapshot spaceSnapshot = snapshots.getSpace(newSnapshot.unknownPermId, newSnapshot.from);
                        if (spaceSnapshot != null)
                        {
                            newSnapshot.spaceCode = newSnapshot.unknownPermId;
                            snapshots.putDeletedSample(newSnapshot);
                        }
                    }
                }
            }
        }

        for (NewEvent newEvent : newEvents)
        {
            try
            {
                snapshots.fillBySamplePermId(newEvent.identifier, newEvent);
                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }
}