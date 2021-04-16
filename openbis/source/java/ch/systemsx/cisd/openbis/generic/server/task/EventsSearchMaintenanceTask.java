/*
 * Copyright 2018 ETH Zuerich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.openbis.generic.server.task;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.IRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventsSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.*;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pkupczyk
 */
public class EventsSearchMaintenanceTask implements IMaintenanceTask
{

    private static final SimpleDateFormat REGISTRATION_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

    private static final SimpleDateFormat VALID_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private static final int BATCH_SIZE = 1000;

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final IDataSource dataSource;

    public EventsSearchMaintenanceTask()
    {
        this.dataSource = new DataSource();
    }

    EventsSearchMaintenanceTask(IDataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override public void setUp(String pluginName, Properties properties)
    {
    }

    @Override
    public void execute()
    {
        dataSource.executeInNewTransaction((TransactionCallback<Void>) status -> {
            try
            {
                LastTimestamps lastTimestamps = new LastTimestamps(dataSource);
                Snapshots snapshots = new Snapshots(dataSource);

                processSpaceDeletions(lastTimestamps, snapshots);
                processProjectDeletions(lastTimestamps, snapshots);
                processExperimentDeletions(lastTimestamps, snapshots);
                processSampleDeletions(lastTimestamps, snapshots);
                processDataSetDeletions(lastTimestamps, snapshots);

                return null;
            } catch (Throwable e)
            {
                operationLog.error("Execution failed", e);
                throw e;
            }
        });
    }

    private void processSpaceDeletions(LastTimestamps lastTimestamps, Snapshots snapshots)
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

    private void processProjectDeletions(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull =
                lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.PROJECT, EntityType.EXPERIMENT, EntityType.SAMPLE,
                        EntityType.DATASET);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EntityType.PROJECT, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

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
                        newEvents.addAll(processProjectDeletion(lastTimestamps, snapshots, deletion));

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

                snapshots.loadExistingSpaces(NewEvent.getEntitySpaceCodesOrUnknown(newEvents));

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

    private List<NewEvent> processProjectDeletion(LastTimestamps lastTimestamps, Snapshots snapshots, EventPE deletion) throws Exception
    {
        final List<NewEvent> newEvents = new ArrayList<>();

        final Date lastSeenProjectTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.PROJECT);

        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String projectPermId : deletion.getIdentifiers())
            {
                ProjectSnapshot snapshot = new ProjectSnapshot();
                snapshot.projectPermId = projectPermId;
                snapshot.from = new Date(0);
                snapshot.to = deletion.getRegistrationDateInternal();
                snapshots.putDeletedProject(snapshot);

                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = projectPermId;
                newEvents.add(newEvent);
            }

            return newEvents;
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

                        snapshots.putDeletedProject(snapshot);
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

                snapshots.putDeletedProject(snapshot);
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

        return newEvents;
    }

    private void processExperimentDeletions(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull =
                lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.EXPERIMENT, EntityType.SAMPLE,
                        EntityType.DATASET);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EntityType.EXPERIMENT, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

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
                        newEvents.addAll(processExperimentDeletion(lastTimestamps, snapshots, deletion));

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

                snapshots.loadExistingProjects(NewEvent.getEntityProjectPermIdsOrUnknown(newEvents));

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

    private List<NewEvent> processExperimentDeletion(LastTimestamps lastTimestamps, Snapshots snapshots, EventPE deletion) throws Exception
    {
        final List<NewEvent> newEvents = new ArrayList<>();

        final Date lastSeenExperimentTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.EXPERIMENT);

        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String experimentPermId : deletion.getIdentifiers())
            {
                ExperimentSnapshot snapshot = new ExperimentSnapshot();
                snapshot.experimentPermId = experimentPermId;
                snapshot.from = new Date(0);
                snapshot.to = deletion.getRegistrationDateInternal();
                snapshots.putDeletedExperiment(snapshot);

                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = experimentPermId;
                newEvents.add(newEvent);
            }

            return newEvents;
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

            ExperimentSnapshot lastSnapshot = null;

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

                        ExperimentSnapshot snapshot = new ExperimentSnapshot();
                        snapshot.experimentCode = experimentCode;
                        snapshot.experimentPermId = experimentPermId;

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

                        snapshots.putDeletedExperiment(snapshot);
                    }
                }
            }

            if (lastSnapshot == null)
            {
                ExperimentSnapshot snapshot = new ExperimentSnapshot();
                snapshot.experimentCode = experimentCode;
                snapshot.experimentPermId = experimentPermId;
                snapshot.from = registrationTimestamp;
                snapshot.to = deletion.getRegistrationDateInternal();

                snapshots.putDeletedExperiment(snapshot);
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

        return newEvents;
    }

    private void processSampleDeletions(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.SAMPLE, EntityType.DATASET);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EntityType.SAMPLE, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

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
                        newEvents.addAll(processSampleDeletion(lastTimestamps, snapshots, deletion));

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

                snapshots.loadExistingSpaces(NewEvent.getEntitySpaceCodesOrUnknown(newEvents));
                snapshots.loadExistingProjects(NewEvent.getEntityProjectPermIdsOrUnknown(newEvents));
                snapshots.loadExistingExperiments(NewEvent.getEntityExperimentPermIdsOrUnknown(newEvents));

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

    private List<NewEvent> processSampleDeletion(LastTimestamps lastTimestamps, Snapshots snapshots, EventPE deletion) throws Exception
    {
        final List<NewEvent> newEvents = new ArrayList<>();

        final Date lastSeenSampleTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.SAMPLE);

        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String samplePermId : deletion.getIdentifiers())
            {
                SampleSnapshot snapshot = new SampleSnapshot();
                snapshot.samplePermId = samplePermId;
                snapshot.from = new Date(0);
                snapshot.to = deletion.getRegistrationDateInternal();
                snapshots.putDeletedSample(snapshot);

                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = samplePermId;
                newEvents.add(newEvent);
            }

            return newEvents;
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

                        snapshots.putDeletedSample(snapshot);
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

                snapshots.putDeletedSample(snapshot);
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

        return newEvents;
    }

    private void processDataSetDeletions(LastTimestamps lastTimestamps, Snapshots snapshots)
    {
        final Date lastSeenTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.DATASET);
        final MutableObject<Date> latestLastSeenTimestamp = new MutableObject<>(lastSeenTimestampOrNull);

        while (true)
        {
            final List<EventPE> deletions =
                    dataSource.loadEvents(EventType.DELETION, EntityType.DATASET, latestLastSeenTimestamp.getValue(), BATCH_SIZE);

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
                        newEvents.addAll(processDataSetDeletion(deletion));

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

    private List<NewEvent> processDataSetDeletion(EventPE deletion) throws Exception
    {
        final List<NewEvent> newEvents = new ArrayList<>();

        if (deletion.getContent() == null || deletion.getContent().trim().isEmpty())
        {
            for (String dataSetPermId : deletion.getIdentifiers())
            {
                NewEvent newEvent = NewEvent.fromOldEventPE(deletion);
                newEvent.identifier = dataSetPermId;
                newEvents.add(newEvent);
            }

            return newEvents;
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

        return newEvents;
    }

    interface IDataSource
    {

        <T> T executeInNewTransaction(TransactionCallback<T> callback);

        List<SpacePE> loadSpaces(List<String> codes);

        List<Project> loadProjects(List<IProjectId> ids, ProjectFetchOptions fo);

        List<Experiment> loadExperiments(List<IExperimentId> ids, ExperimentFetchOptions fo);

        List<Sample> loadSamples(List<ISampleId> ids, SampleFetchOptions fo);

        List<EventPE> loadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestampOrNull, Integer limit);

        Date loadLastEventsSearchTimestamp(EventType eventType, EntityType entityType);

        void createEventsSearch(EventsSearchPE eventsSearch);

    }

    private static class DataSource implements IDataSource
    {

        @Override public <T> T executeInNewTransaction(TransactionCallback<T> callback)
        {
            PlatformTransactionManager manager = CommonServiceProvider.getApplicationContext().getBean(PlatformTransactionManager.class);
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            definition.setReadOnly(false);
            TransactionTemplate template = new TransactionTemplate(manager, definition);
            return template.execute(callback);
        }

        @Override
        public List<SpacePE> loadSpaces(List<String> codes)
        {
            ISpaceDAO spaceDAO = CommonServiceProvider.getDAOFactory().getSpaceDAO();
            return spaceDAO.tryFindSpaceByCodes(codes);
        }

        @Override
        public List<Project> loadProjects(List<IProjectId> ids, ProjectFetchOptions fo)
        {
            IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
            String sessionToken = v3.loginAsSystem();
            Map<IProjectId, Project> result = v3.getProjects(sessionToken, ids, fo);
            return new ArrayList<>(result.values());
        }

        @Override
        public List<Experiment> loadExperiments(List<IExperimentId> ids, ExperimentFetchOptions fo)
        {
            IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
            String sessionToken = v3.loginAsSystem();
            Map<IExperimentId, Experiment> result = v3.getExperiments(sessionToken, ids, fo);
            return new ArrayList<>(result.values());
        }

        @Override public List<Sample> loadSamples(List<ISampleId> ids, SampleFetchOptions fo)
        {
            IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
            String sessionToken = v3.loginAsSystem();
            Map<ISampleId, Sample> result = v3.getSamples(sessionToken, ids, fo);
            return new ArrayList<>(result.values());
        }

        @Override
        public List<EventPE> loadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestampOrNull, Integer limit)
        {
            IEventDAO eventDAO = CommonServiceProvider.getDAOFactory().getEventDAO();
            return eventDAO.listEvents(eventType, entityType, lastSeenTimestampOrNull, limit);
        }

        @Override public Date loadLastEventsSearchTimestamp(EventType eventType, EntityType entityType)
        {
            IEventsSearchDAO eventsSearchDAO = CommonServiceProvider.getDAOFactory().getEventsSearchDAO();
            return eventsSearchDAO.getLastTimestamp(eventType, entityType);
        }

        @Override public void createEventsSearch(EventsSearchPE eventsSearch)
        {
            IEventsSearchDAO eventsSearchDAO = CommonServiceProvider.getDAOFactory().getEventsSearchDAO();
            eventsSearchDAO.createOrUpdate(eventsSearch);
        }
    }

    static class LastTimestamps
    {
        private final Map<Pair<EventType, EntityType>, Date> timestamps = new HashMap<>();

        public LastTimestamps(IDataSource dataSource)
        {
            for (EventType eventType : EventType.values())
            {
                for (EntityType entityType : EntityType.values())
                {
                    Date lastTimestamp = dataSource.loadLastEventsSearchTimestamp(eventType, entityType);
                    timestamps.put(new ImmutablePair<>(eventType, entityType), lastTimestamp);
                }
            }
        }

        public Date getEarliestOrNull(EventType eventType, EntityType... entityTypes)
        {
            Date earliest = null;

            for (EntityType entityType : entityTypes)
            {
                Date timestamp = timestamps.get(new ImmutablePair<>(eventType, entityType));

                if (timestamp == null)
                {
                    return null;
                }

                if (earliest == null || timestamp.before(earliest))
                {
                    earliest = timestamp;
                }
            }

            return earliest;
        }

    }

    static class Snapshots
    {

        private final SpaceSnapshots spaceSnapshots;

        private final ProjectSnapshots projectSnapshots;

        private final ExperimentSnapshots experimentSnapshots;

        private final SampleSnapshots sampleSnapshots;

        public Snapshots(IDataSource dataSource)
        {
            this.spaceSnapshots = new SpaceSnapshots(dataSource);
            this.projectSnapshots = new ProjectSnapshots(dataSource);
            this.experimentSnapshots = new ExperimentSnapshots(dataSource);
            this.sampleSnapshots = new SampleSnapshots(dataSource);
        }

        public void loadExistingSpaces(Collection<String> spaceCodes)
        {
            spaceSnapshots.load(spaceCodes);
        }

        public void loadExistingProjects(Collection<String> projectPermIds)
        {
            projectSnapshots.load(projectPermIds);

            Collection<ProjectSnapshot> snapshots = projectSnapshots.get(projectPermIds);
            Set<String> spaceCodes = snapshots.stream().map(snapshot -> snapshot.spaceCode).collect(Collectors.toSet());

            loadExistingSpaces(spaceCodes);
        }

        public void loadExistingExperiments(Collection<String> experimentPermIds)
        {
            experimentSnapshots.load(experimentPermIds);

            Collection<ExperimentSnapshot> snapshots = experimentSnapshots.get(experimentPermIds);
            Set<String> projectPermIds = snapshots.stream().map(snapshot -> snapshot.projectPermId).collect(Collectors.toSet());

            loadExistingProjects(projectPermIds);
        }

        public void loadExistingSamples(Collection<String> samplePermIds)
        {
            sampleSnapshots.load(samplePermIds);

            Collection<SampleSnapshot> snapshots = sampleSnapshots.get(samplePermIds);

            Set<String> spaceCodes =
                    snapshots.stream().map(snapshot -> snapshot.spaceCode).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<String> projectPermIds =
                    snapshots.stream().map(snapshot -> snapshot.projectPermId).filter(Objects::nonNull).collect(Collectors.toSet());
            Set<String> experimentPermIds =
                    snapshots.stream().map(snapshot -> snapshot.experimentPermId).filter(Objects::nonNull).collect(Collectors.toSet());

            loadExistingSpaces(spaceCodes);
            loadExistingProjects(projectPermIds);
            loadExistingExperiments(experimentPermIds);
        }

        public void putDeletedSpace(SpaceSnapshot snapshot)
        {
            spaceSnapshots.put(snapshot.spaceCode, snapshot);
        }

        public void putDeletedProject(ProjectSnapshot snapshot)
        {
            projectSnapshots.put(snapshot.projectPermId, snapshot);
        }

        public void putDeletedExperiment(ExperimentSnapshot snapshot)
        {
            experimentSnapshots.put(snapshot.experimentPermId, snapshot);
        }

        public void putDeletedSample(SampleSnapshot snapshot)
        {
            sampleSnapshots.put(snapshot.samplePermId, snapshot);
        }

        public void fillBySpaceCode(String spaceCode, NewEvent newEvent)
        {
            SpaceSnapshot spaceSnapshot = spaceSnapshots.get(spaceCode, newEvent.registrationTimestamp);

            if (spaceSnapshot != null)
            {
                newEvent.entitySpaceCode = spaceSnapshot.spaceCode;
                newEvent.entitySpacePermId = spaceSnapshot.spaceTechId != null ? String.valueOf(spaceSnapshot.spaceTechId) : null;
            }
        }

        public void fillByProjectPermId(String projectPermId, NewEvent newEvent)
        {
            ProjectSnapshot projectSnapshot = projectSnapshots.get(projectPermId, newEvent.registrationTimestamp);

            if (projectSnapshot != null)
            {
                if (projectSnapshot.spaceCode != null)
                {
                    fillBySpaceCode(projectSnapshot.spaceCode, newEvent);
                } else if (projectSnapshot.unknownPermId != null)
                {
                    fillBySpaceCode(projectSnapshot.unknownPermId, newEvent);
                }

                newEvent.entityProjectPermId = projectPermId;
                if (newEvent.entitySpaceCode != null)
                {
                    newEvent.entityProject = new ProjectIdentifier(newEvent.entitySpaceCode, projectSnapshot.projectCode).toString();
                }
            }
        }

        public void fillByExperimentPermId(String experimentPermId, NewEvent newEvent)
        {
            ExperimentSnapshot experimentSnapshot = experimentSnapshots.get(experimentPermId, newEvent.registrationTimestamp);

            if (experimentSnapshot != null)
            {
                if (experimentSnapshot.projectPermId != null)
                {
                    fillByProjectPermId(experimentSnapshot.projectPermId, newEvent);
                } else if (experimentSnapshot.unknownPermId != null)
                {
                    fillByProjectPermId(experimentSnapshot.unknownPermId, newEvent);
                }
            }
        }

        public void fillBySamplePermId(String samplePermId, NewEvent newEvent)
        {
            SampleSnapshot sampleSnapshot = sampleSnapshots.get(samplePermId, newEvent.registrationTimestamp);

            if (sampleSnapshot != null)
            {
                if (sampleSnapshot.experimentPermId != null)
                {
                    fillByExperimentPermId(sampleSnapshot.experimentPermId, newEvent);
                } else if (sampleSnapshot.projectPermId != null)
                {
                    fillByProjectPermId(sampleSnapshot.projectPermId, newEvent);
                } else if (sampleSnapshot.spaceCode != null)
                {
                    fillBySpaceCode(sampleSnapshot.spaceCode, newEvent);
                } else if (sampleSnapshot.unknownPermId != null)
                {
                    fillByExperimentPermId(sampleSnapshot.unknownPermId, newEvent);
                    if (newEvent.entitySpaceCode == null)
                    {
                        fillByProjectPermId(sampleSnapshot.unknownPermId, newEvent);
                        if (newEvent.entitySpaceCode == null)
                        {
                            fillBySpaceCode(sampleSnapshot.unknownPermId, newEvent);
                        }
                    }
                }
            }
        }
    }

    static abstract class AbstractSnapshots<T extends AbstractSnapshot>
    {
        protected final IDataSource dataSource;

        private final Set<String> loadedKeys = new HashSet<>();

        private final Map<String, TreeMap<Date, T>> snapshots = new HashMap<>();

        public AbstractSnapshots(IDataSource dataSource)
        {
            this.dataSource = dataSource;
        }

        public void put(String key, T snapshot)
        {
            TreeMap<Date, T> snapshotsForKey = snapshots.computeIfAbsent(key, k -> new TreeMap<>());
            snapshotsForKey.put(snapshot.from, snapshot);
        }

        public void load(Collection<String> keysToLoad)
        {
            Set<String> notLoaded = new HashSet<>(keysToLoad);
            notLoaded.removeAll(loadedKeys);

            if (notLoaded.size() > 0)
            {
                List<T> snapshots = doLoad(notLoaded);
                for (T snapshot : snapshots)
                {
                    put(snapshot.getKey(), snapshot);
                }
                loadedKeys.addAll(notLoaded);
            }
        }

        protected abstract List<T> doLoad(Collection<String> keysToLoad);

        public Collection<T> get(Collection<String> keys)
        {
            Collection<T> snapshotsForKeys = new ArrayList<>();

            for (String key : keys)
            {
                TreeMap<Date, T> snapshotsForKey = snapshots.get(key);

                if (snapshotsForKey != null)
                {
                    snapshotsForKeys.addAll(snapshotsForKey.values());
                }
            }

            return snapshotsForKeys;
        }

        public T get(String key, Date date)
        {
            TreeMap<Date, T> snapshotsForKey = snapshots.get(key);

            if (snapshotsForKey != null)
            {
                Map.Entry<Date, T> potentialEntry = snapshotsForKey.floorEntry(date);

                if (potentialEntry != null)
                {
                    T potentialSnapshot = potentialEntry.getValue();
                    if (potentialSnapshot.to == null || date.compareTo(potentialSnapshot.to) <= 0)
                    {
                        return potentialSnapshot;
                    }
                }
            }

            return null;
        }
    }

    static abstract class AbstractSnapshot
    {
        public Date from;

        public Date to;

        protected abstract String getKey();
    }

    static class SpaceSnapshots extends AbstractSnapshots<SpaceSnapshot>
    {

        public SpaceSnapshots(IDataSource dataSource)
        {
            super(dataSource);
        }

        protected List<SpaceSnapshot> doLoad(Collection<String> spaceCodes)
        {
            List<SpaceSnapshot> snapshots = new ArrayList<>();

            List<SpacePE> spaces = dataSource.loadSpaces(new ArrayList<>(spaceCodes));
            for (SpacePE space : spaces)
            {
                SpaceSnapshot snapshot = new SpaceSnapshot();
                snapshot.from = space.getRegistrationDateInternal();
                snapshot.spaceCode = space.getCode();
                snapshot.spaceTechId = space.getId();
                snapshots.add(snapshot);
            }

            return snapshots;
        }
    }

    static class SpaceSnapshot extends AbstractSnapshot
    {

        public Long spaceTechId;

        public String spaceCode;

        @Override protected String getKey()
        {
            return spaceCode;
        }
    }

    static class ProjectSnapshots extends AbstractSnapshots<ProjectSnapshot>
    {

        public ProjectSnapshots(IDataSource dataSource)
        {
            super(dataSource);
        }

        protected List<ProjectSnapshot> doLoad(Collection<String> projectPermIds)
        {
            List<ProjectSnapshot> snapshots = new ArrayList<>();

            ProjectFetchOptions fo = new ProjectFetchOptions();
            fo.withSpace();
            fo.withHistory();

            List<IProjectId> ids = projectPermIds.stream().map(ProjectPermId::new).collect(Collectors.toList());
            List<Project> projects = dataSource.loadProjects(ids, fo);

            for (Project project : projects)
            {
                RelationHistoryEntry lastSpaceRelationship = null;

                for (HistoryEntry historyEntry : project.getHistory())
                {
                    if (historyEntry instanceof RelationHistoryEntry)
                    {
                        RelationHistoryEntry relationHistoryEntry = (RelationHistoryEntry) historyEntry;

                        if (ProjectRelationType.SPACE.equals(relationHistoryEntry.getRelationType()))
                        {
                            ProjectSnapshot snapshot = new ProjectSnapshot();
                            snapshot.projectCode = project.getCode();
                            snapshot.projectPermId = project.getPermId().getPermId();
                            snapshot.spaceCode = ((SpacePermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                            snapshot.from = relationHistoryEntry.getValidFrom();
                            snapshot.to = relationHistoryEntry.getValidTo();

                            snapshots.add(snapshot);

                            if (lastSpaceRelationship == null || relationHistoryEntry.getValidFrom().after(lastSpaceRelationship.getValidFrom()))
                            {
                                lastSpaceRelationship = relationHistoryEntry;
                            }
                        }
                    }
                }

                ProjectSnapshot snapshot = new ProjectSnapshot();
                snapshot.projectCode = project.getCode();
                snapshot.projectPermId = project.getPermId().getPermId();
                snapshot.spaceCode = project.getSpace().getCode();

                if (lastSpaceRelationship != null)
                {
                    snapshot.from = lastSpaceRelationship.getValidTo();
                } else
                {
                    snapshot.from = project.getRegistrationDate();
                }

                snapshots.add(snapshot);
            }

            return snapshots;
        }
    }

    static class ProjectSnapshot extends AbstractSnapshot
    {

        public String projectCode;

        public String projectPermId;

        public String spaceCode;

        public String unknownPermId;

        @Override protected String getKey()
        {
            return projectPermId;
        }
    }

    static class ExperimentSnapshots extends AbstractSnapshots<ExperimentSnapshot>
    {

        public ExperimentSnapshots(IDataSource dataSource)
        {
            super(dataSource);
        }

        protected List<ExperimentSnapshot> doLoad(Collection<String> experimentPermIds)
        {
            List<ExperimentSnapshot> snapshots = new ArrayList<>();

            ExperimentFetchOptions fo = new ExperimentFetchOptions();
            fo.withProject();
            fo.withHistory();

            List<IExperimentId> ids = experimentPermIds.stream().map(ExperimentPermId::new).collect(Collectors.toList());
            List<Experiment> experiments = dataSource.loadExperiments(ids, fo);

            for (Experiment experiment : experiments)
            {
                RelationHistoryEntry lastProjectRelationship = null;

                for (HistoryEntry historyEntry : experiment.getHistory())
                {
                    if (historyEntry instanceof RelationHistoryEntry)
                    {
                        RelationHistoryEntry relationHistoryEntry = (RelationHistoryEntry) historyEntry;

                        if (ExperimentRelationType.PROJECT.equals(relationHistoryEntry.getRelationType()))
                        {
                            ExperimentSnapshot snapshot = new ExperimentSnapshot();
                            snapshot.experimentCode = experiment.getCode();
                            snapshot.experimentPermId = experiment.getPermId().getPermId();
                            snapshot.projectPermId = ((ProjectPermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                            snapshot.from = relationHistoryEntry.getValidFrom();
                            snapshot.to = relationHistoryEntry.getValidTo();

                            snapshots.add(snapshot);

                            if (lastProjectRelationship == null || relationHistoryEntry.getValidFrom()
                                    .after(lastProjectRelationship.getValidFrom()))
                            {
                                lastProjectRelationship = relationHistoryEntry;
                            }
                        }
                    }
                }

                ExperimentSnapshot snapshot = new ExperimentSnapshot();
                snapshot.experimentCode = experiment.getCode();
                snapshot.experimentPermId = experiment.getPermId().getPermId();
                snapshot.projectPermId = experiment.getProject().getPermId().getPermId();

                if (lastProjectRelationship != null)
                {
                    snapshot.from = lastProjectRelationship.getValidTo();
                } else
                {
                    snapshot.from = experiment.getRegistrationDate();
                }

                snapshots.add(snapshot);
            }

            return snapshots;
        }
    }

    static class ExperimentSnapshot extends AbstractSnapshot
    {

        public String experimentCode;

        public String experimentPermId;

        public String projectPermId;

        public String unknownPermId;

        @Override protected String getKey()
        {
            return experimentPermId;
        }
    }

    static class SampleSnapshots extends AbstractSnapshots<SampleSnapshot>
    {

        public SampleSnapshots(IDataSource dataSource)
        {
            super(dataSource);
        }

        protected List<SampleSnapshot> doLoad(Collection<String> samplePermIds)
        {
            List<SampleSnapshot> snapshots = new ArrayList<>();

            SampleFetchOptions fo = new SampleFetchOptions();
            fo.withSpace();
            fo.withProject();
            fo.withExperiment();
            fo.withHistory();

            List<ISampleId> ids = samplePermIds.stream().map(SamplePermId::new).collect(Collectors.toList());
            List<Sample> samples = dataSource.loadSamples(ids, fo);

            for (Sample sample : samples)
            {
                RelationHistoryEntry lastRelationship = null;

                for (HistoryEntry historyEntry : sample.getHistory())
                {
                    if (historyEntry instanceof RelationHistoryEntry)
                    {
                        RelationHistoryEntry relationHistoryEntry = (RelationHistoryEntry) historyEntry;
                        IRelationType relationType = relationHistoryEntry.getRelationType();

                        if (SampleRelationType.SPACE.equals(relationType) || SampleRelationType.PROJECT.equals(relationType)
                                || SampleRelationType.EXPERIMENT.equals(relationType))
                        {
                            SampleSnapshot snapshot = new SampleSnapshot();
                            snapshot.sampleCode = sample.getCode();
                            snapshot.samplePermId = sample.getPermId().getPermId();
                            snapshot.from = relationHistoryEntry.getValidFrom();
                            snapshot.to = relationHistoryEntry.getValidTo();

                            if (SampleRelationType.SPACE.equals(relationType))
                            {
                                snapshot.spaceCode = ((SpacePermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                            } else if (SampleRelationType.PROJECT.equals(relationType))
                            {
                                snapshot.projectPermId = ((ProjectPermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                            } else
                            {
                                snapshot.experimentPermId = ((ExperimentPermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                            }

                            snapshots.add(snapshot);

                            if (lastRelationship == null || relationHistoryEntry.getValidFrom().after(lastRelationship.getValidFrom()))
                            {
                                lastRelationship = relationHistoryEntry;
                            }
                        }
                    }
                }

                SampleSnapshot snapshot = new SampleSnapshot();
                snapshot.sampleCode = sample.getCode();
                snapshot.samplePermId = sample.getPermId().getPermId();

                if (sample.getExperiment() != null)
                {
                    snapshot.experimentPermId = sample.getExperiment().getPermId().getPermId();
                } else if (sample.getProject() != null)
                {
                    snapshot.projectPermId = sample.getProject().getPermId().getPermId();
                } else if (sample.getSpace() != null)
                {
                    snapshot.spaceCode = sample.getSpace().getCode();
                }

                if (lastRelationship != null)
                {
                    snapshot.from = lastRelationship.getValidTo();
                } else
                {
                    snapshot.from = sample.getRegistrationDate();
                }

                snapshots.add(snapshot);
            }

            return snapshots;
        }
    }

    static class SampleSnapshot extends AbstractSnapshot
    {

        public String sampleCode;

        public String samplePermId;

        public String spaceCode;

        public String projectPermId;

        public String experimentPermId;

        public String unknownPermId;

        @Override protected String getKey()
        {
            return samplePermId;
        }
    }

    static class NewEvent
    {
        public Long id;

        public EventType eventType;

        public EntityType entityType;

        public String entitySpaceCode;

        public String entitySpacePermId;

        public String entityProject;

        public String entityProjectPermId;

        public String entityExperimentPermId;

        public String entitySamplePermId;

        public String entityUnknownPermId;

        public String entityRegisterer;

        public Date entityRegistrationTimestamp;

        public String identifier;

        public String description;

        public String reason;

        public String content;

        public Long attachmentContent;

        public PersonPE registerer;

        public Date registrationTimestamp;

        public static Set<String> getEntitySpaceCodesOrUnknown(Collection<NewEvent> newEvents)
        {
            Set<String> result = new HashSet<>();
            for (NewEvent newEvent : newEvents)
            {
                if (newEvent.entitySpaceCode != null)
                {
                    result.add(newEvent.entitySpaceCode);
                } else if (newEvent.entityUnknownPermId != null)
                {
                    result.add(newEvent.entityUnknownPermId);
                }
            }
            return result;
        }

        public static Set<String> getEntityProjectPermIdsOrUnknown(Collection<NewEvent> newEvents)
        {
            Set<String> result = new HashSet<>();
            for (NewEvent newEvent : newEvents)
            {
                if (newEvent.entityProjectPermId != null)
                {
                    result.add(newEvent.entityProjectPermId);
                } else if (newEvent.entityUnknownPermId != null)
                {
                    result.add(newEvent.entityUnknownPermId);
                }
            }
            return result;
        }

        public static Set<String> getEntityExperimentPermIdsOrUnknown(Collection<NewEvent> newEvents)
        {
            Set<String> result = new HashSet<>();
            for (NewEvent newEvent : newEvents)
            {
                if (newEvent.entityExperimentPermId != null)
                {
                    result.add(newEvent.entityExperimentPermId);
                } else if (newEvent.entityUnknownPermId != null)
                {
                    result.add(newEvent.entityUnknownPermId);
                }
            }
            return result;
        }

        public static Set<String> getEntitySamplePermIdsOrUnknown(Collection<NewEvent> newEvents)
        {
            Set<String> result = new HashSet<>();
            for (NewEvent newEvent : newEvents)
            {
                if (newEvent.entitySamplePermId != null)
                {
                    result.add(newEvent.entitySamplePermId);
                } else if (newEvent.entityUnknownPermId != null)
                {
                    result.add(newEvent.entityUnknownPermId);
                }
            }
            return result;
        }

        public static NewEvent fromOldEventPE(EventPE oldEvent)
        {
            NewEvent newEvent = new NewEvent();
            newEvent.id = oldEvent.getId();
            newEvent.eventType = oldEvent.getEventType();
            newEvent.entityType = oldEvent.getEntityType();
            newEvent.description = oldEvent.getDescription();
            newEvent.reason = oldEvent.getReason();
            newEvent.attachmentContent = oldEvent.getAttachmentContent() != null ? oldEvent.getAttachmentContent().getId() : null;
            newEvent.registerer = oldEvent.getRegistrator();
            newEvent.registrationTimestamp = oldEvent.getRegistrationDateInternal();
            return newEvent;
        }

        public EventsSearchPE toNewEventPE()
        {
            EventsSearchPE newEventPE = new EventsSearchPE();
            newEventPE.setEventType(eventType);
            newEventPE.setEntityType(entityType);
            newEventPE.setEntitySpace(entitySpaceCode);
            newEventPE.setEntitySpacePermId(entitySpacePermId);
            newEventPE.setEntityProject(entityProject);
            newEventPE.setEntityProjectPermId(entityProjectPermId);
            newEventPE.setEntityRegisterer(entityRegisterer);
            newEventPE.setEntityRegistrationTimestamp(entityRegistrationTimestamp);
            newEventPE.setIdentifier(identifier);
            newEventPE.setDescription(description);
            newEventPE.setReason(reason);
            newEventPE.setContent(content);
            newEventPE.setAttachmentContent(attachmentContent);
            newEventPE.setRegisterer(registerer);
            newEventPE.setRegistrationTimestamp(registrationTimestamp);
            return newEventPE;
        }

        @Override public String toString()
        {
            return ToStringBuilder.reflectionToString(this);
        }
    }

}
