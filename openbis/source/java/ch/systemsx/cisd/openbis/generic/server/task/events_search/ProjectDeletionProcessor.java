package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class ProjectDeletionProcessor extends DeletionEventProcessor
{

    ProjectDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected EntityType getEntityType()
    {
        return EntityType.PROJECT;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return EnumSet.of(EntityType.SPACE);
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return EnumSet.of(EntityType.EXPERIMENT, EntityType.SAMPLE, EntityType.DATASET);
    }

    @Override protected void processDeletions(LastTimestamps lastTimestamps, Snapshots snapshots, List<NewEvent> newEvents, List<Snapshot> newSnapshots)
    {
        snapshots.loadExistingSpaces(Snapshot.getSpaceCodesOrUnknown(newSnapshots));

        for (Snapshot newSnapshot : newSnapshots)
        {
            if (newSnapshot.unknownPermId == null)
            {
                snapshots.putDeletedProject(newSnapshot);
            } else
            {
                Snapshot spaceSnapshot = snapshots.getSpace(newSnapshot.unknownPermId, newSnapshot.from);
                if (spaceSnapshot != null)
                {
                    newSnapshot.spaceCode = newSnapshot.unknownPermId;
                    snapshots.putDeletedProject(newSnapshot);
                }
            }
        }

        for (NewEvent newEvent : newEvents)
        {
            try
            {
                snapshots.fillByProjectPermId(newEvent.identifier, newEvent);
                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }
}
