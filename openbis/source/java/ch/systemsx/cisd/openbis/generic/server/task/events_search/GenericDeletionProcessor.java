package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GenericDeletionProcessor extends DeletionEventProcessor
{

    private EntityType entityType;

    public GenericDeletionProcessor(IDataSource dataSource, EntityType entityType)
    {
        super(dataSource);
        this.entityType = entityType;
    }

    @Override protected EntityType getEntityType()
    {
        return entityType;
    }

    @Override protected Set<EntityType> getAscendantEntityTypes()
    {
        return Collections.emptySet();
    }

    @Override protected Set<EntityType> getDescendantEntityTypes()
    {
        return Collections.emptySet();
    }

    @Override protected void processDeletions(LastTimestamps lastTimestamps, Snapshots snapshots, List<NewEvent> newEvents,
            List<Snapshot> newSnapshots)
    {
        for (NewEvent newEvent : newEvents)
        {
            try
            {
                dataSource.createEventsSearch(newEvent.toNewEventPE());

            } catch (Exception e)
            {
                throw new RuntimeException(String.format("Processing of deletion failed: %s", newEvent), e);
            }
        }
    }

}
