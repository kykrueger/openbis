package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.apache.commons.lang3.mutable.MutableObject;
import org.springframework.transaction.support.TransactionCallback;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class DataSetDeletionProcessor extends EntityDeletionProcessor
{
    DataSetDeletionProcessor(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public void process(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EventPE.EntityType.DATASET);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EventPE.EntityType.DATASET, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

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
                        processDataSetDeletion(deletion, newEvents);

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

                return null;
            });
        }
    }

    private void processDataSetDeletion(EventPE deletion, List<NewEvent> newEvents) throws Exception
    {
        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String dataSetPermId : deletion.getIdentifiers())
            {
                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = dataSetPermId;
                newEvents.add(newEvent);
            }

            return;
        }

        @SuppressWarnings("unchecked") Map<String, Object> parsedContent =
                (Map<String, Object>) objectMapper.readValue(deletion.getContent(), Object.class);

        for (String dataSetPermId : parsedContent.keySet())
        {
            @SuppressWarnings("unchecked") List<Map<String, String>> dataSetEntries =
                    (List<Map<String, String>>) parsedContent.get(dataSetPermId);

            String registerer = null;
            Date registrationTimestamp = null;

            for (Map<String, String> dataSetEntry : dataSetEntries)
            {
                String type = dataSetEntry.get("type");
                String key = dataSetEntry.get("key");
                String value = dataSetEntry.get("value");

                if ("ATTRIBUTE".equals(type))
                {
                    if ("REGISTRATOR".equals(key))
                    {
                        registerer = value;
                    } else if ("REGISTRATION_TIMESTAMP".equals(key))
                    {
                        registrationTimestamp = REGISTRATION_TIMESTAMP_FORMAT.parse(value);
                    }
                }
            }

            String experimentPermId = null;
            String samplePermId = null;
            String unknownPermId = null;

            for (Map<String, String> dataSetEntry : dataSetEntries)
            {
                String type = dataSetEntry.get("type");
                String value = dataSetEntry.get("value");

                if ("RELATIONSHIP".equals(type))
                {
                    String entityType = dataSetEntry.get("entityType");
                    String validUntil = dataSetEntry.get("validUntil");

                    if (validUntil == null)
                    {
                        if ("EXPERIMENT".equals(entityType))
                        {
                            experimentPermId = value;
                        } else if ("SAMPLE".equals(entityType))
                        {
                            samplePermId = value;
                        } else if ("UNKNOWN".equals(entityType))
                        {
                            unknownPermId = value;
                        }
                    }
                }
            }

            NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
            newEvent.entityExperimentPermId = experimentPermId;
            newEvent.entitySamplePermId = samplePermId;
            newEvent.entityUnknownPermId = unknownPermId;
            newEvent.entityRegisterer = registerer;
            newEvent.entityRegistrationTimestamp = registrationTimestamp;
            newEvent.identifier = dataSetPermId;
            newEvent.content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(dataSetEntries);
            newEvents.add(newEvent);
        }
    }
}
