package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.List;

public class GenericEventProcessor extends EventProcessor
{
    private final EventType eventType;

    GenericEventProcessor(IDataSource dataSource, EventType eventType)
    {
        super(dataSource);
        this.eventType = eventType;
    }

    @Override final public void process(LastTimestamps lastTimestamps, SnapshotsFacade snapshots)
    {
        final Date lastSeenTimestampOrNull = lastTimestamps.getEarliestOrNull(eventType, EntityType.values());
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> events = dataSource.loadEvents(eventType, null, latestLastSeenTimestamp.getValue());

            if (events.isEmpty())
            {
                break;
            }

            dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
                for (EventPE event : events)
                {
                    try
                    {
                        NewEvent newEvent = NewEvent.fromOldEventPE(event);
                        newEvent.identifier = event.getIdentifiers() != null ? String.join(", ", event.getIdentifiers()) : null;

                        dataSource.createEventsSearch(newEvent.toNewEventPE());

                        if (latestLastSeenTimestamp.getValue() == null || event.getRegistrationDateInternal()
                                .after(latestLastSeenTimestamp.getValue()))
                        {
                            latestLastSeenTimestamp.setValue(event.getRegistrationDateInternal());
                        }
                    } catch (Exception e)
                    {
                        throw new RuntimeException(String.format("Processing of deletion failed: %s", event), e);
                    }
                }

                return null;
            });
        }
    }
}
