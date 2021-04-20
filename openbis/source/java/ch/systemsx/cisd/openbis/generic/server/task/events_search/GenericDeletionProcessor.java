package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GenericDeletionProcessor extends EventProcessor
{

    private EntityType entityType;

    public GenericDeletionProcessor(IDataSource dataSource, EntityType entityType)
    {
        super(dataSource);
        this.entityType = entityType;
    }

    @Override
    public void process(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, entityType);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, entityType, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

            if (deletions.isEmpty())
            {
                break;
            }

            dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
                List<NewEvent> newEvents = new LinkedList<>();

                for (EventPE deletion : deletions)
                {
                    try
                    {
                        process(deletion, newEvents);

                        if (latestLastSeenTimestamp.getValue() == null || deletion.getRegistrationDateInternal()
                                .after(latestLastSeenTimestamp.getValue()))
                        {
                            latestLastSeenTimestamp.setValue(deletion.getRegistrationDateInternal());
                        }
                    } catch (Exception e)
                    {
                        throw new RuntimeException(String.format("Processing of deletion failed: %s", deletion), e);
                    }
                }

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

                return null;
            });
        }
    }

    private void process(EventPE deletion, List<NewEvent> newEvents) throws Exception
    {
        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String dataSetPermId : deletion.getIdentifiers())
            {
                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = dataSetPermId;
                newEvents.add(newEvent);
            }
        }
    }

}
