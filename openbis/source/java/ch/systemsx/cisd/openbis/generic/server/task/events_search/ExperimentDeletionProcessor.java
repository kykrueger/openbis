package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ExperimentDeletionProcessor extends EntityDeletionProcessor
{
    ExperimentDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public void process(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull =
                lastTimestamps.getEarliestOrNull(EventType.DELETION, EventPE.EntityType.EXPERIMENT, EventPE.EntityType.SAMPLE,
                        EventPE.EntityType.DATASET);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EventPE.EntityType.EXPERIMENT, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

            if (deletions.isEmpty())
            {
                break;
            }

            dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
                List<NewEvent> newEvents = new LinkedList<>();
                List<Snapshot> newSnapshots = new LinkedList<>();

                for (EventPE deletion : deletions)
                {
                    try
                    {
                        process(lastTimestamps, deletion, newEvents, newSnapshots);

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

                snapshots.loadExistingProjects(Snapshot.getProjectPermIdsOrUnknown(newSnapshots));

                for (Snapshot newSnapshot : newSnapshots)
                {
                    if (newSnapshot.unknownPermId == null)
                    {
                        snapshots.putDeletedExperiment(newSnapshot);
                    } else
                    {
                        Snapshot projectSnapshot = snapshots.getProject(newSnapshot.unknownPermId, newSnapshot.from);
                        if (projectSnapshot != null)
                        {
                            newSnapshot.projectPermId = newSnapshot.unknownPermId;
                            snapshots.putDeletedExperiment(newSnapshot);
                        }
                    }
                }

                for (NewEvent newEvent : newEvents)
                {
                    try
                    {
                        snapshots.fillByExperimentPermId(newEvent.identifier, newEvent);
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

    private void process(LastTimestamps lastTimestamps, EventPE deletion, List<NewEvent> newEvents,
            List<Snapshot> newSnapshots) throws Exception
    {
        final Date lastSeenExperimentTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EventPE.EntityType.EXPERIMENT);

        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String experimentPermId : deletion.getIdentifiers())
            {
                Snapshot snapshot = new Snapshot();
                snapshot.entityPermId = experimentPermId;
                snapshot.from = new Date(0);
                snapshot.to = deletion.getRegistrationDateInternal();
                newSnapshots.add(snapshot);

                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = experimentPermId;
                newEvents.add(newEvent);
            }

            return;
        }

        @SuppressWarnings("unchecked") Map<String, Object> parsedContent =
                (Map<String, Object>) objectMapper.readValue(deletion.getContent(), Object.class);

        for (String experimentPermId : parsedContent.keySet())
        {
            @SuppressWarnings("unchecked") List<Map<String, String>> experimentEntries =
                    (List<Map<String, String>>) parsedContent.get(experimentPermId);

            String experimentCode = null;
            String registerer = null;
            Date registrationTimestamp = null;

            for (Map<String, String> experimentEntry : experimentEntries)
            {
                String type = experimentEntry.get("type");
                String key = experimentEntry.get("key");
                String value = experimentEntry.get("value");

                if ("ATTRIBUTE".equals(type))
                {
                    if ("CODE".equals(key))
                    {
                        experimentCode = value;
                    } else if ("REGISTRATOR".equals(key))
                    {
                        registerer = value;
                    } else if ("REGISTRATION_TIMESTAMP".equals(key))
                    {
                        registrationTimestamp = REGISTRATION_TIMESTAMP_FORMAT.parse(value);
                    }
                }
            }

            Snapshot lastSnapshot = null;

            for (Map<String, String> experimentEntry : experimentEntries)
            {
                String type = experimentEntry.get("type");
                String entityType = experimentEntry.get("entityType");

                if ("RELATIONSHIP".equals(type))
                {
                    if ("PROJECT".equals(entityType) || "UNKNOWN".equals(entityType))
                    {
                        String value = experimentEntry.get("value");
                        String validFrom = experimentEntry.get("validFrom");
                        String validUntil = experimentEntry.get("validUntil");

                        Snapshot snapshot = new Snapshot();
                        snapshot.entityCode = experimentCode;
                        snapshot.entityPermId = experimentPermId;

                        if ("PROJECT".equals(entityType))
                        {
                            snapshot.projectPermId = value;
                        } else
                        {
                            snapshot.unknownPermId = value;
                        }

                        if (validFrom != null)
                        {
                            snapshot.from = VALID_TIMESTAMP_FORMAT.parse(validFrom);
                        }

                        if (validUntil != null)
                        {
                            snapshot.to = VALID_TIMESTAMP_FORMAT.parse(validUntil);
                        } else
                        {
                            snapshot.to = deletion.getRegistrationDateInternal();
                            lastSnapshot = snapshot;
                        }

                        newSnapshots.add(snapshot);
                    }
                }
            }

            if (lastSnapshot == null)
            {
                Snapshot snapshot = new Snapshot();
                snapshot.entityCode = experimentCode;
                snapshot.entityPermId = experimentPermId;
                snapshot.from = registrationTimestamp;
                snapshot.to = deletion.getRegistrationDateInternal();

                newSnapshots.add(snapshot);
                lastSnapshot = snapshot;
            }

            if (lastSeenExperimentTimestampOrNull == null || deletion.getRegistrationDateInternal()
                    .after(lastSeenExperimentTimestampOrNull))
            {
                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.entityProjectPermId = lastSnapshot.projectPermId;
                newEvent.entityUnknownPermId = lastSnapshot.unknownPermId;
                newEvent.entityRegisterer = registerer;
                newEvent.entityRegistrationTimestamp = registrationTimestamp;
                newEvent.identifier = experimentPermId;
                newEvent.content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(experimentEntries);
                newEvents.add(newEvent);
            }
        }
    }
}