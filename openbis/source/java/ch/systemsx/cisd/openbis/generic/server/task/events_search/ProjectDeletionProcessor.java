package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class ProjectDeletionProcessor extends EntityDeletionProcessor
{

    ProjectDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public void process(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull =
                lastTimestamps
                        .getEarliestOrNull(EventType.DELETION, EventPE.EntityType.PROJECT, EventPE.EntityType.EXPERIMENT, EventPE.EntityType.SAMPLE,
                                EventPE.EntityType.DATASET);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EventPE.EntityType.PROJECT, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

            if (deletions.isEmpty())
            {
                break;
            }

            dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
                List<NewEvent> newEvents = new LinkedList<>();
                List<ProjectSnapshot> newSnapshots = new LinkedList<>();

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

                snapshots.loadExistingSpaces(ProjectSnapshot.getSpaceCodesOrUnknown(newSnapshots));

                for (ProjectSnapshot newSnapshot : newSnapshots)
                {
                    if (newSnapshot.unknownPermId == null)
                    {
                        snapshots.putDeletedProject(newSnapshot);
                    } else
                    {
                        SpaceSnapshot spaceSnapshot = snapshots.getSpace(newSnapshot.unknownPermId, newSnapshot.from);
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

                return null;
            });
        }
    }

    private void process(LastTimestamps lastTimestamps, EventPE deletion, List<NewEvent> newEvents,
            List<ProjectSnapshot> newSnapshots) throws Exception
    {
        final Date lastSeenProjectTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EventPE.EntityType.PROJECT);

        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String projectPermId : deletion.getIdentifiers())
            {
                ProjectSnapshot snapshot = new ProjectSnapshot();
                snapshot.projectPermId = projectPermId;
                snapshot.from = new Date(0);
                snapshot.to = deletion.getRegistrationDateInternal();
                newSnapshots.add(snapshot);

                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = projectPermId;
                newEvents.add(newEvent);
            }

            return;
        }

        @SuppressWarnings("unchecked") Map<String, Object> parsedContent =
                (Map<String, Object>) objectMapper.readValue(deletion.getContent(), Object.class);

        for (String projectPermId : parsedContent.keySet())
        {
            @SuppressWarnings("unchecked") List<Map<String, String>> projectEntries = (List<Map<String, String>>) parsedContent.get(projectPermId);

            String projectCode = null;
            String registerer = null;
            Date registrationTimestamp = null;

            for (Map<String, String> projectEntry : projectEntries)
            {
                String type = projectEntry.get("type");
                String key = projectEntry.get("key");
                String value = projectEntry.get("value");

                if ("ATTRIBUTE".equals(type))
                {
                    if ("CODE".equals(key))
                    {
                        projectCode = value;
                    } else if ("REGISTRATOR".equals(key))
                    {
                        registerer = value;
                    } else if ("REGISTRATION_TIMESTAMP".equals(key))
                    {
                        registrationTimestamp = REGISTRATION_TIMESTAMP_FORMAT.parse(value);
                    }
                }
            }

            ProjectSnapshot lastSnapshot = null;

            for (Map<String, String> projectEntry : projectEntries)
            {
                String type = projectEntry.get("type");
                String entityType = projectEntry.get("entityType");

                if ("RELATIONSHIP".equals(type))
                {
                    if ("SPACE".equals(entityType) || "UNKNOWN".equals(entityType))
                    {
                        String value = projectEntry.get("value");
                        String validFrom = projectEntry.get("validFrom");
                        String validUntil = projectEntry.get("validUntil");

                        ProjectSnapshot snapshot = new ProjectSnapshot();
                        snapshot.projectCode = projectCode;
                        snapshot.projectPermId = projectPermId;

                        if ("SPACE".equals(entityType))
                        {
                            snapshot.spaceCode = value;
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
                ProjectSnapshot snapshot = new ProjectSnapshot();
                snapshot.projectCode = projectCode;
                snapshot.projectPermId = projectPermId;
                snapshot.from = registrationTimestamp;
                snapshot.to = deletion.getRegistrationDateInternal();

                newSnapshots.add(snapshot);
                lastSnapshot = snapshot;
            }

            if (lastSeenProjectTimestampOrNull == null || deletion.getRegistrationDateInternal().after(lastSeenProjectTimestampOrNull))
            {
                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.entitySpaceCode = lastSnapshot.spaceCode;
                newEvent.entityProjectPermId = lastSnapshot.projectPermId;
                newEvent.entityUnknownPermId = lastSnapshot.unknownPermId;
                newEvent.entityRegisterer = registerer;
                newEvent.entityRegistrationTimestamp = registrationTimestamp;
                newEvent.identifier = projectPermId;
                newEvent.content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(projectEntries);
                newEvents.add(newEvent);
            }
        }
    }

}
