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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.IRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventsSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventsSearchPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                processDeletions(lastTimestamps);

                return null;
            } catch (Throwable e)
            {
                operationLog.error("Execution failed", e);
                throw e;
            }
        });
    }

    private void processDeletions(LastTimestamps lastTimestamps)
    {
        SpaceSnapshots spaceSnapshots = new SpaceSnapshots(dataSource);
        ProjectSnapshots projectSnapshots = new ProjectSnapshots(dataSource);
        ExperimentSnapshots experimentSnapshots = new ExperimentSnapshots(dataSource);
        SampleSnapshots sampleSnapshots = new SampleSnapshots(dataSource);

        processSpaceDeletions(lastTimestamps, spaceSnapshots);
        processProjectDeletions(lastTimestamps, spaceSnapshots, projectSnapshots);
        processExperimentDeletions(lastTimestamps, spaceSnapshots, projectSnapshots, experimentSnapshots);
        processSampleDeletions(lastTimestamps, spaceSnapshots, projectSnapshots, experimentSnapshots, sampleSnapshots);
    }

    private void processSpaceDeletions(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots)
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
            spaceSnapshots.put(snapshot.spaceCode, snapshot);
        }
    }

    private void processProjectDeletions(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots, ProjectSnapshots projectSnapshots)
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
                for (EventPE deletion : deletions)
                {
                    try
                    {
                        processProjectDeletion(lastTimestamps, spaceSnapshots, projectSnapshots, deletion);

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
    }

    private void processProjectDeletion(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots, ProjectSnapshots projectSnapshots,
            EventPE deletion) throws Exception
    {
        final Date lastSeenProjectTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.PROJECT);

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

            String spaceCode = null;

            for (Map<String, String> projectEntry : projectEntries)
            {
                String type = projectEntry.get("type");
                String entityType = projectEntry.get("entityType");

                if ("RELATIONSHIP".equals(type) && "SPACE".equals(entityType))
                {
                    String value = projectEntry.get("value");
                    String validFrom = projectEntry.get("validFrom");
                    String validUntil = projectEntry.get("validUntil");

                    ProjectSnapshot snapshot = new ProjectSnapshot();
                    snapshot.projectCode = projectCode;
                    snapshot.projectPermId = projectPermId;
                    snapshot.spaceCode = value;

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
                        spaceCode = value;
                    }

                    projectSnapshots.put(snapshot.projectPermId, snapshot);
                }
            }

            if (lastSeenProjectTimestampOrNull == null || deletion.getRegistrationDateInternal().after(lastSeenProjectTimestampOrNull))
            {
                SpaceSnapshot spaceSnapshot = spaceSnapshots.get(spaceCode, deletion.getRegistrationDateInternal());

                EventsSearchPE newEvent = new EventsSearchPE();
                newEvent.setEventType(deletion.getEventType());
                newEvent.setEntityType(deletion.getEntityType());
                newEvent.setEntitySpace(spaceCode);
                newEvent.setEntitySpacePermId(spaceSnapshot.spaceTechId != null ? String.valueOf(spaceSnapshot.spaceTechId) : null);
                newEvent.setEntityProject(new ProjectIdentifier(spaceCode, projectCode).toString());
                newEvent.setEntityProjectPermId(projectPermId);
                newEvent.setEntityRegisterer(registerer);
                newEvent.setEntityRegistrationTimestamp(registrationTimestamp);
                newEvent.setIdentifier(projectPermId);
                newEvent.setDescription(deletion.getDescription());
                newEvent.setReason(deletion.getReason());
                newEvent.setContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(projectEntries));
                newEvent.setAttachmentContent(
                        deletion.getAttachmentContent() != null ? deletion.getAttachmentContent().getId() : null);
                newEvent.setRegisterer(deletion.getRegistrator());
                newEvent.setRegistrationTimestamp(deletion.getRegistrationDateInternal());
                dataSource.createEventsSearch(newEvent);
            }
        }
    }

    private void processExperimentDeletions(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots, ProjectSnapshots projectSnapshots,
            ExperimentSnapshots experimentSnapshots)
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
                for (EventPE deletion : deletions)
                {
                    try
                    {
                        processExperimentDeletion(lastTimestamps, spaceSnapshots, projectSnapshots, experimentSnapshots, deletion);

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
    }

    private void processExperimentDeletion(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots, ProjectSnapshots projectSnapshots,
            ExperimentSnapshots experimentSnapshots, EventPE deletion) throws Exception
    {
        final Date lastSeenExperimentTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.EXPERIMENT);

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

            String projectPermId = null;

            for (Map<String, String> experimentEntry : experimentEntries)
            {
                String type = experimentEntry.get("type");
                String entityType = experimentEntry.get("entityType");

                if ("RELATIONSHIP".equals(type) && "PROJECT".equals(entityType))
                {
                    String value = experimentEntry.get("value");
                    String validFrom = experimentEntry.get("validFrom");
                    String validUntil = experimentEntry.get("validUntil");

                    ExperimentSnapshot snapshot = new ExperimentSnapshot();
                    snapshot.experimentCode = experimentCode;
                    snapshot.experimentPermId = experimentPermId;
                    snapshot.projectPermId = value;

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
                        projectPermId = value;
                    }

                    experimentSnapshots.put(snapshot.experimentPermId, snapshot);
                }
            }

            if (lastSeenExperimentTimestampOrNull == null || deletion.getRegistrationDateInternal()
                    .after(lastSeenExperimentTimestampOrNull))
            {
                ProjectSnapshot projectSnapshot = projectSnapshots.get(projectPermId, deletion.getRegistrationDateInternal());
                SpaceSnapshot spaceSnapshot = spaceSnapshots.get(projectSnapshot.spaceCode, deletion.getRegistrationDateInternal());

                EventsSearchPE newEvent = new EventsSearchPE();
                newEvent.setEventType(deletion.getEventType());
                newEvent.setEntityType(deletion.getEntityType());
                newEvent.setEntitySpace(spaceSnapshot.spaceCode);
                newEvent.setEntitySpacePermId(
                        spaceSnapshot.spaceTechId != null ? String.valueOf(spaceSnapshot.spaceTechId) : null);
                newEvent.setEntityProject(new ProjectIdentifier(spaceSnapshot.spaceCode, projectSnapshot.projectCode).toString());
                newEvent.setEntityProjectPermId(projectPermId);
                newEvent.setEntityRegisterer(registerer);
                newEvent.setEntityRegistrationTimestamp(registrationTimestamp);
                newEvent.setIdentifier(experimentPermId);
                newEvent.setDescription(deletion.getDescription());
                newEvent.setReason(deletion.getReason());
                newEvent.setContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(experimentEntries));
                newEvent.setAttachmentContent(
                        deletion.getAttachmentContent() != null ? deletion.getAttachmentContent().getId() : null);
                newEvent.setRegisterer(deletion.getRegistrator());
                newEvent.setRegistrationTimestamp(deletion.getRegistrationDateInternal());
                dataSource.createEventsSearch(newEvent);
            }
        }
    }

    private void processSampleDeletions(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots, ProjectSnapshots projectSnapshots,
            ExperimentSnapshots experimentSnapshots, SampleSnapshots sampleSnapshots)
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
                for (EventPE deletion : deletions)
                {
                    try
                    {
                        processSampleDeletion(lastTimestamps, spaceSnapshots, projectSnapshots, experimentSnapshots, sampleSnapshots, deletion);

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
    }

    private void processSampleDeletion(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots, ProjectSnapshots projectSnapshots,
            ExperimentSnapshots experimentSnapshots, SampleSnapshots sampleSnapshots, EventPE deletion) throws Exception
    {
        final Date lastSeenSampleTimestampOrNull = lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.SAMPLE);

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

                    // TODO project samples (currently they are stored with "entityType" == "UNKNOWN")

                    if ("SPACE".equals(entityType) || "EXPERIMENT".equals(entityType))
                    {
                        SampleSnapshot snapshot = new SampleSnapshot();
                        snapshot.sampleCode = sampleCode;
                        snapshot.samplePermId = samplePermId;

                        String value = sampleEntry.get("value");
                        if ("SPACE".equals(entityType))
                        {
                            snapshot.spaceCode = value;
                        } else if ("EXPERIMENT".equals(entityType))
                        {
                            snapshot.experimentPermId = value;
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

                        sampleSnapshots.put(snapshot.samplePermId, snapshot);
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

                sampleSnapshots.put(snapshot.samplePermId, snapshot);
                lastSnapshot = snapshot;
            }

            if (lastSeenSampleTimestampOrNull == null || deletion.getRegistrationDateInternal()
                    .after(lastSeenSampleTimestampOrNull))
            {
                EventsSearchPE newEvent = new EventsSearchPE();
                newEvent.setEventType(deletion.getEventType());
                newEvent.setEntityType(deletion.getEntityType());
                newEvent.setEntityRegisterer(registerer);
                newEvent.setEntityRegistrationTimestamp(registrationTimestamp);
                newEvent.setIdentifier(samplePermId);
                newEvent.setDescription(deletion.getDescription());
                newEvent.setReason(deletion.getReason());
                newEvent.setContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sampleEntries));
                newEvent.setAttachmentContent(
                        deletion.getAttachmentContent() != null ? deletion.getAttachmentContent().getId() : null);
                newEvent.setRegisterer(deletion.getRegistrator());
                newEvent.setRegistrationTimestamp(deletion.getRegistrationDateInternal());

                // TODO project samples (currently they are stored with "entityType" == "UNKNOWN")

                if (lastSnapshot.experimentPermId != null)
                {
                    ExperimentSnapshot experimentSnapshot =
                            experimentSnapshots.get(lastSnapshot.experimentPermId, deletion.getRegistrationDateInternal());
                    ProjectSnapshot projectSnapshot =
                            projectSnapshots.get(experimentSnapshot.projectPermId, deletion.getRegistrationDateInternal());
                    SpaceSnapshot spaceSnapshot = spaceSnapshots.get(projectSnapshot.spaceCode, deletion.getRegistrationDateInternal());

                    newEvent.setEntitySpace(spaceSnapshot.spaceCode);
                    newEvent.setEntitySpacePermId(
                            spaceSnapshot.spaceTechId != null ? String.valueOf(spaceSnapshot.spaceTechId) : null);
                    newEvent.setEntityProject(new ProjectIdentifier(spaceSnapshot.spaceCode, projectSnapshot.projectCode).toString());
                    newEvent.setEntityProjectPermId(projectSnapshot.projectPermId);
                } else if (lastSnapshot.spaceCode != null)
                {
                    SpaceSnapshot spaceSnapshot = spaceSnapshots.get(lastSnapshot.spaceCode, deletion.getRegistrationDateInternal());

                    newEvent.setEntitySpace(spaceSnapshot.spaceCode);
                    newEvent.setEntitySpacePermId(
                            spaceSnapshot.spaceTechId != null ? String.valueOf(spaceSnapshot.spaceTechId) : null);
                }

                dataSource.createEventsSearch(newEvent);
            }
        }
    }

    interface IDataSource
    {

        <T> T executeInNewTransaction(TransactionCallback<T> callback);

        List<SpacePE> loadSpaces();

        List<Project> loadProjects(ProjectFetchOptions fo);

        List<Experiment> loadExperiments(ExperimentFetchOptions fo);

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
        public List<SpacePE> loadSpaces()
        {
            ISpaceDAO spaceDAO = CommonServiceProvider.getDAOFactory().getSpaceDAO();
            return spaceDAO.listSpaces();
        }

        @Override
        public List<Project> loadProjects(ProjectFetchOptions fo)
        {
            IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
            String sessionToken = v3.loginAsSystem();
            SearchResult<Project> result = v3.searchProjects(sessionToken, new ProjectSearchCriteria(), fo);
            return result.getObjects();
        }

        @Override
        public List<Experiment> loadExperiments(ExperimentFetchOptions fo)
        {
            IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
            String sessionToken = v3.loginAsSystem();
            SearchResult<Experiment> result = v3.searchExperiments(sessionToken, new ExperimentSearchCriteria(), fo);
            return result.getObjects();
        }

        @Override public List<Sample> loadSamples(List<ISampleId> ids, SampleFetchOptions fo)
        {
            return null;
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

    static abstract class AbstractSnapshots<T extends AbstractSnapshot>
    {
        protected final IDataSource dataSource;

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

        public T get(String key, Date date)
        {
            TreeMap<Date, T> snapshotsForKey = snapshots.get(key);

            if (snapshotsForKey != null)
            {
                Map.Entry<Date, T> potentialEntry = snapshotsForKey.floorEntry(date);

                if (potentialEntry != null)
                {
                    T potentialSnapshot = potentialEntry.getValue();
                    if (potentialSnapshot.to == null || date.before(potentialSnapshot.to))
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
    }

    static class SpaceSnapshots extends AbstractSnapshots<SpaceSnapshot>
    {

        public SpaceSnapshots(IDataSource dataSource)
        {
            super(dataSource);
            loadExisting();
        }

        private void loadExisting()
        {
            List<SpacePE> spaces = dataSource.loadSpaces();

            for (SpacePE space : spaces)
            {
                SpaceSnapshot snapshot = new SpaceSnapshot();
                snapshot.from = space.getRegistrationDateInternal();
                snapshot.spaceCode = space.getCode();
                snapshot.spaceTechId = space.getId();

                put(snapshot.spaceCode, snapshot);
            }
        }
    }

    static class SpaceSnapshot extends AbstractSnapshot
    {

        public Long spaceTechId;

        public String spaceCode;

    }

    static class ProjectSnapshots extends AbstractSnapshots<ProjectSnapshot>
    {

        public ProjectSnapshots(IDataSource dataSource)
        {
            super(dataSource);
            loadExisting();
        }

        private void loadExisting()
        {
            ProjectFetchOptions fo = new ProjectFetchOptions();
            fo.withSpace();
            fo.withHistory();

            List<Project> projects = dataSource.loadProjects(fo);

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

                            put(snapshot.projectPermId, snapshot);

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

                put(snapshot.projectPermId, snapshot);
            }
        }
    }

    static class ProjectSnapshot extends AbstractSnapshot
    {

        public String projectCode;

        public String projectPermId;

        public String spaceCode;

    }

    static class ExperimentSnapshots extends AbstractSnapshots<ExperimentSnapshot>
    {

        public ExperimentSnapshots(IDataSource dataSource)
        {
            super(dataSource);
            loadExisting();
        }

        private void loadExisting()
        {
            ExperimentFetchOptions fo = new ExperimentFetchOptions();
            fo.withProject();
            fo.withHistory();

            List<Experiment> experiments = dataSource.loadExperiments(fo);

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

                            put(snapshot.experimentPermId, snapshot);

                            if (lastProjectRelationship == null || relationHistoryEntry.getValidFrom().after(lastProjectRelationship.getValidFrom()))
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

                put(snapshot.experimentPermId, snapshot);
            }
        }
    }

    static class ExperimentSnapshot extends AbstractSnapshot
    {

        public String experimentCode;

        public String experimentPermId;

        public String projectPermId;

    }

    static class SampleSnapshots extends AbstractSnapshots<SampleSnapshot>
    {

        public SampleSnapshots(IDataSource dataSource)
        {
            super(dataSource);
        }

        public void loadExisting(List<ISampleId> ids)
        {
            SampleFetchOptions fo = new SampleFetchOptions();
            fo.withSpace();
            fo.withProject();
            fo.withExperiment();
            fo.withHistory();

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

                        // TODO project samples (currently relations with projects are not included)

                        if (SampleRelationType.SPACE.equals(relationType) || SampleRelationType.EXPERIMENT.equals(relationType))
                        {
                            SampleSnapshot snapshot = new SampleSnapshot();
                            snapshot.sampleCode = sample.getCode();
                            snapshot.samplePermId = sample.getPermId().getPermId();
                            snapshot.from = relationHistoryEntry.getValidFrom();
                            snapshot.to = relationHistoryEntry.getValidTo();

                            if (SampleRelationType.SPACE.equals(relationType))
                            {
                                snapshot.spaceCode = ((SpacePermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                            } else
                            {
                                snapshot.experimentPermId = ((ExperimentPermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                            }

                            put(snapshot.samplePermId, snapshot);

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

                put(snapshot.samplePermId, snapshot);
            }
        }
    }

    static class SampleSnapshot extends AbstractSnapshot
    {

        public String sampleCode;

        public String samplePermId;

        public String spaceCode;

        public String projectPermId;

        public String experimentPermId;

    }

}
