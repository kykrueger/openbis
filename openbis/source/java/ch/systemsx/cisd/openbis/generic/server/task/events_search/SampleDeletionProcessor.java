package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class SampleDeletionProcessor extends EntityDeletionProcessor
{

    SampleDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public void process(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull =
                lastTimestamps.getEarliestOrNull(EventType.DELETION, EventPE.EntityType.SAMPLE, EventPE.EntityType.DATASET);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EventPE.EntityType.SAMPLE, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

            if (deletions.isEmpty())
            {
                break;
            }

            dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
                List<NewEvent> newEvents = new LinkedList<>();
                List<SampleSnapshot> newSnapshots = new LinkedList<>();

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

                snapshots.loadExistingSpaces(SampleSnapshot.getSpaceCodesOrUnknown(newSnapshots));
                snapshots.loadExistingProjects(SampleSnapshot.getProjectPermIdsOrUnknown(newSnapshots));
                snapshots.loadExistingExperiments(SampleSnapshot.getExperimentPermIdsOrUnknown(newSnapshots));

                for (SampleSnapshot newSnapshot : newSnapshots)
                {
                    if (newSnapshot.unknownPermId == null)
                    {
                        snapshots.putDeletedSample(newSnapshot);
                    } else
                    {
                        ExperimentSnapshot experimentSnapshot = snapshots.getExperiment(newSnapshot.unknownPermId, newSnapshot.from);
                        if (experimentSnapshot != null)
                        {
                            newSnapshot.experimentPermId = newSnapshot.unknownPermId;
                            snapshots.putDeletedSample(newSnapshot);
                        } else
                        {
                            ProjectSnapshot projectSnapshot = snapshots.getProject(newSnapshot.unknownPermId, newSnapshot.from);
                            if (projectSnapshot != null)
                            {
                                newSnapshot.projectPermId = newSnapshot.unknownPermId;
                                snapshots.putDeletedSample(newSnapshot);
                            } else
                            {
                                SpaceSnapshot spaceSnapshot = snapshots.getSpace(newSnapshot.unknownPermId, newSnapshot.from);
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

                return null;
            });
        }
    }

    private void process(LastTimestamps lastTimestamps, EventPE deletion, List<NewEvent> newEvents,
            List<SampleSnapshot> newSnapshots)
            throws Exception
    {
        final Date lastSeenSampleTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EventPE.EntityType.SAMPLE);

        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String samplePermId : deletion.getIdentifiers())
            {
                SampleSnapshot snapshot = new SampleSnapshot();
                snapshot.samplePermId = samplePermId;
                snapshot.from = new Date(0);
                snapshot.to = deletion.getRegistrationDateInternal();
                newSnapshots.add(snapshot);

                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = samplePermId;
                newEvents.add(newEvent);
            }

            return;
        }

        @SuppressWarnings("unchecked") Map<String, Object> parsedContent =
                (Map<String, Object>) objectMapper.readValue(deletion.getContent(), Object.class);

        for (String samplePermId : parsedContent.keySet())
        {
            @SuppressWarnings("unchecked") List<Map<String, String>> sampleEntries =
                    (List<Map<String, String>>) parsedContent.get(samplePermId);

            String sampleCode = null;
            String registerer = null;
            Date registrationTimestamp = null;

            for (Map<String, String> sampleEntry : sampleEntries)
            {
                String type = sampleEntry.get("type");
                String key = sampleEntry.get("key");
                String value = sampleEntry.get("value");

                if ("ATTRIBUTE".equals(type))
                {
                    if ("CODE".equals(key))
                    {
                        sampleCode = value;
                    } else if ("REGISTRATOR".equals(key))
                    {
                        registerer = value;
                    } else if ("REGISTRATION_TIMESTAMP".equals(key))
                    {
                        registrationTimestamp = REGISTRATION_TIMESTAMP_FORMAT.parse(value);
                    }
                }
            }

            SampleSnapshot lastSnapshot = null;

            for (Map<String, String> sampleEntry : sampleEntries)
            {
                String type = sampleEntry.get("type");

                if ("RELATIONSHIP".equals(type))
                {
                    String entityType = sampleEntry.get("entityType");

                    if ("SPACE".equals(entityType) || "EXPERIMENT".equals(entityType) || "PROJECT".equals(entityType) || "UNKNOWN"
                            .equals(entityType))
                    {
                        SampleSnapshot snapshot = new SampleSnapshot();
                        snapshot.sampleCode = sampleCode;
                        snapshot.samplePermId = samplePermId;

                        String value = sampleEntry.get("value");
                        if ("SPACE".equals(entityType))
                        {
                            snapshot.spaceCode = value;
                        } else if ("PROJECT".equals(entityType))
                        {
                            snapshot.projectPermId = value;
                        } else if ("EXPERIMENT".equals(entityType))
                        {
                            snapshot.experimentPermId = value;
                        } else
                        {
                            snapshot.unknownPermId = value;
                        }

                        String validFrom = sampleEntry.get("validFrom");
                        if (validFrom != null)
                        {
                            snapshot.from = VALID_TIMESTAMP_FORMAT.parse(validFrom);
                        }

                        String validUntil = sampleEntry.get("validUntil");
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
                SampleSnapshot snapshot = new SampleSnapshot();
                snapshot.sampleCode = sampleCode;
                snapshot.samplePermId = samplePermId;
                snapshot.from = registrationTimestamp;
                snapshot.to = deletion.getRegistrationDateInternal();

                newSnapshots.add(snapshot);
                lastSnapshot = snapshot;
            }

            if (lastSeenSampleTimestampOrNull == null || deletion.getRegistrationDateInternal()
                    .after(lastSeenSampleTimestampOrNull))
            {
                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.entitySpaceCode = lastSnapshot.spaceCode;
                newEvent.entityProjectPermId = lastSnapshot.projectPermId;
                newEvent.entityExperimentPermId = lastSnapshot.experimentPermId;
                newEvent.entityUnknownPermId = lastSnapshot.unknownPermId;
                newEvent.entityRegisterer = registerer;
                newEvent.entityRegistrationTimestamp = registrationTimestamp;
                newEvent.identifier = samplePermId;
                newEvent.content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sampleEntries);
                newEvents.add(newEvent);
            }
        }
    }

}