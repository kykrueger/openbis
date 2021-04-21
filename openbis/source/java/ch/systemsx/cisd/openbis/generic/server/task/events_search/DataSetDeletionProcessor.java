package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

class DataSetDeletionProcessor extends DeletionEventProcessor
{
    DataSetDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected EntityType getEntityType()
    {
        return EntityType.DATASET;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return EnumSet.of(EntityType.EXPERIMENT, EntityType.SAMPLE);
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return Collections.emptySet();
    }

    @Override protected void processDeletions(LastTimestamps lastTimestamps, Snapshots snapshots, List<NewEvent> newEvents, List<Snapshot> newSnapshots)
    {
        snapshots.loadExistingExperiments(NewEvent.getEntityExperimentPermIdsOrUnknown(newEvents));
        snapshots.loadExistingSamples(NewEvent.getEntitySamplePermIdsOrUnknown(newEvents));

        for (NewEvent newEvent : newEvents)
        {
            try
            {
                if (newEvent.entityExperimentPermId != null)
                {
                    snapshots.fillByExperimentPermId(newEvent.entityExperimentPermId, newEvent);
                } else if (newEvent.entitySamplePermId != null)
                {
                    snapshots.fillBySamplePermId(newEvent.entitySamplePermId, newEvent);
                } else if (newEvent.entityUnknownPermId != null)
                {
                    snapshots.fillByExperimentPermId(newEvent.entityUnknownPermId, newEvent);
                    if (newEvent.entitySpaceCode == null)
                    {
                        snapshots.fillBySamplePermId(newEvent.entityUnknownPermId, newEvent);
                    }
                }

                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }
}
