package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventsSearchPE;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class SpaceDeletionProcessor extends EventProcessor
{

    SpaceDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    public void process(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull = lastTimestamps
                .getEarliestOrNull(EventType.DELETION, EntityType.SPACE, EntityType.PROJECT, EntityType.EXPERIMENT, EntityType.SAMPLE,
                        EntityType.DATASET);
        final Date lastSeenSpaceTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.SPACE);

        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);
        final Map<String, EventPE> latestDeletions = new HashMap<>();

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EntityType.SPACE, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

            if (deletions.isEmpty())
            {
                break;
            }

            dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
                for (EventPE deletion : deletions)
                {
                    try
                    {
                        String spaceCode = deletion.getIdentifiers().get(0);

                        EventPE latestDeletion = latestDeletions.get(spaceCode);
                        if (latestDeletion == null || latestDeletion.getRegistrationDateInternal().before(deletion.getRegistrationDateInternal()))
                        {
                            latestDeletions.put(spaceCode, deletion);
                        }

                        if (lastSeenSpaceTimestampOrNull == null || deletion.getRegistrationDateInternal().after(lastSeenSpaceTimestampOrNull))
                        {
                            EventsSearchPE newEvent = new EventsSearchPE();
                            newEvent.setEventType(deletion.getEventType());
                            newEvent.setEntityType(deletion.getEntityType());
                            newEvent.setEntitySpace(spaceCode);
                            newEvent.setIdentifier(spaceCode);
                            newEvent.setDescription(deletion.getDescription());
                            newEvent.setReason(deletion.getReason());
                            newEvent.setContent(deletion.getContent());
                            newEvent.setAttachmentContent(deletion.getAttachmentContent() != null ? deletion.getAttachmentContent().getId() : null);
                            newEvent.setRegisterer(deletion.getRegistrator());
                            newEvent.setRegistrationTimestamp(deletion.getRegistrationDateInternal());
                            dataSource.createEventsSearch(newEvent);
                        }

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

                return null;
            });
        }

        for (EventPE latestDeletion : latestDeletions.values())
        {
            SpaceSnapshot snapshot = new SpaceSnapshot();
            snapshot.spaceCode = latestDeletion.getIdentifiers().get(0);
            snapshot.from = new Date(0);
            snapshot.to = latestDeletion.getRegistrationDateInternal();
            snapshots.putDeletedSpace(snapshot);
        }
    }
}