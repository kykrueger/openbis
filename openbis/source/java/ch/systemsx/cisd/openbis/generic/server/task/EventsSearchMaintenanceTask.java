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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
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
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author pkupczyk
 */
public class EventsSearchMaintenanceTask implements IMaintenanceTask
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private IDataSource dataSource;

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
        dataSource.open();

        IDataSourceTransaction transaction = null;

        try
        {
            transaction = dataSource.createTransaction();

            LastTimestamps lastTimestamps = new LastTimestamps(dataSource);
            processDeletions(lastTimestamps);

            transaction.commit();
        } catch (Exception e)
        {
            if (transaction != null)
            {
                transaction.rollback();
            }
        } finally
        {
            dataSource.close();
        }
    }

    private void processDeletions(LastTimestamps lastTimestamps)
    {
        SpaceSnapshots spaceSnapshots = new SpaceSnapshots(dataSource);
        ProjectSnapshots projectSnapshots = new ProjectSnapshots(dataSource);
        ExperimentSnapshots experimentSnapshots = new ExperimentSnapshots(dataSource);

        processSpaceDeletions(lastTimestamps, spaceSnapshots);
        processProjectDeletions(lastTimestamps, spaceSnapshots, projectSnapshots);
        processExperimentDeletions(lastTimestamps, spaceSnapshots, projectSnapshots, experimentSnapshots);
    }

    private void processSpaceDeletions(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots)
    {
        Date lastSeenTimestampOrNull =
                lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.SPACE, EntityType.PROJECT, EntityType.EXPERIMENT, EntityType.SAMPLE,
                        EntityType.DATASET);

        List<EventPE> deletions = dataSource.loadEvents(EventType.DELETION, EntityType.SPACE, lastSeenTimestampOrNull);

        Map<String, EventPE> latestDeletions = new HashMap<>();

        for (EventPE deletion : deletions)
        {
            String spaceCode = deletion.getIdentifiers().get(0);

            EventPE latestDeletion = latestDeletions.get(spaceCode);
            if (latestDeletion == null || latestDeletion.getRegistrationDate().before(deletion.getRegistrationDate()))
            {
                latestDeletions.put(spaceCode, deletion);
            }

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
            newEvent.setRegistrationTimestamp(deletion.getRegistrationDate());
            dataSource.createEventsSearch(newEvent);
        }

        for (EventPE latestDeletion : latestDeletions.values())
        {
            SpaceSnapshot snapshot = new SpaceSnapshot();
            snapshot.spaceCode = latestDeletion.getIdentifiers().get(0);
            snapshot.to = latestDeletion.getRegistrationDate();
            spaceSnapshots.put(snapshot.spaceCode, snapshot);
        }
    }

    private void processProjectDeletions(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots, ProjectSnapshots projectSnapshots)
    {
        final SimpleDateFormat registrationTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        final SimpleDateFormat validTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final ObjectMapper objectMapper = new ObjectMapper();

        Date lastSeenTimestampOrNull =
                lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.PROJECT, EntityType.EXPERIMENT, EntityType.SAMPLE,
                        EntityType.DATASET);

        List<EventPE> deletions = dataSource.loadEvents(EventType.DELETION, EntityType.PROJECT, lastSeenTimestampOrNull);

        for (EventPE deletion : deletions)
        {
            try
            {
                Map<String, Object> parsedContent = (Map<String, Object>) objectMapper.readValue(deletion.getContent(), Object.class);

                for (String projectPermId : parsedContent.keySet())
                {
                    List<Map<String, String>> projectEntries = (List<Map<String, String>>) parsedContent.get(projectPermId);

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
                                registrationTimestamp = registrationTimestampFormat.parse(value);
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
                                snapshot.from = validTimestampFormat.parse(validFrom);
                            }

                            if (validUntil != null)
                            {
                                snapshot.to = validTimestampFormat.parse(validUntil);
                            } else
                            {
                                snapshot.to = deletion.getRegistrationDate();
                                spaceCode = value;
                            }

                            projectSnapshots.put(snapshot.projectPermId, snapshot);
                        }
                    }

                    SpaceSnapshot spaceSnapshot = spaceSnapshots.get(spaceCode, deletion.getRegistrationDate());

                    EventsSearchPE newEvent = new EventsSearchPE();
                    newEvent.setEventType(deletion.getEventType());
                    newEvent.setEntityType(deletion.getEntityType());
                    newEvent.setEntitySpace(spaceCode);
                    newEvent.setEntitySpacePermId(spaceSnapshot.spaceTechId != null ? String.valueOf(spaceSnapshot.spaceTechId) : null);
                    newEvent.setEntityProject(new ProjectIdentifier(spaceCode, projectCode).toString());
                    newEvent.setEntityProjectPermId(projectPermId);
                    newEvent.setEntityRegisterer(registerer);
                    newEvent.setEntityRegistrationTimestamp(registrationTimestamp);
                    newEvent.setIdentifier(new ProjectIdentifier(spaceCode, projectCode).toString());
                    newEvent.setDescription(deletion.getDescription());
                    newEvent.setReason(deletion.getReason());
                    newEvent.setContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(projectEntries));
                    newEvent.setAttachmentContent(deletion.getAttachmentContent() != null ? deletion.getAttachmentContent().getId() : null);
                    newEvent.setRegisterer(deletion.getRegistrator());
                    newEvent.setRegistrationTimestamp(deletion.getRegistrationDate());
                    dataSource.createEventsSearch(newEvent);
                }

            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void processExperimentDeletions(LastTimestamps lastTimestamps, SpaceSnapshots spaceSnapshots, ProjectSnapshots projectSnapshots,
            ExperimentSnapshots experimentSnapshots)
    {
        final SimpleDateFormat registrationTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
        final SimpleDateFormat validTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        final ObjectMapper objectMapper = new ObjectMapper();

        Date lastSeenTimestampOrNull =
                lastTimestamps.getEarliestOrNull(EventType.DELETION, EntityType.EXPERIMENT, EntityType.SAMPLE,
                        EntityType.DATASET);

        List<EventPE> deletions = dataSource.loadEvents(EventType.DELETION, EntityType.EXPERIMENT, lastSeenTimestampOrNull);

        for (EventPE deletion : deletions)
        {
            try
            {
                Map<String, Object> parsedContent = (Map<String, Object>) objectMapper.readValue(deletion.getContent(), Object.class);

                for (String experimentPermId : parsedContent.keySet())
                {
                    List<Map<String, String>> experimentEntries = (List<Map<String, String>>) parsedContent.get(experimentPermId);

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
                                registrationTimestamp = registrationTimestampFormat.parse(value);
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
                                snapshot.from = validTimestampFormat.parse(validFrom);
                            }

                            if (validUntil != null)
                            {
                                snapshot.to = validTimestampFormat.parse(validUntil);
                            } else
                            {
                                snapshot.to = deletion.getRegistrationDate();
                                projectPermId = value;
                            }

                            experimentSnapshots.put(snapshot.experimentPermId, snapshot);
                        }
                    }

                    ProjectSnapshot projectSnapshot = projectSnapshots.get(projectPermId, deletion.getRegistrationDate());
                    SpaceSnapshot spaceSnapshot = spaceSnapshots.get(projectSnapshot.spaceCode, deletion.getRegistrationDate());

                    EventsSearchPE newEvent = new EventsSearchPE();
                    newEvent.setEventType(deletion.getEventType());
                    newEvent.setEntityType(deletion.getEntityType());
                    newEvent.setEntitySpace(spaceSnapshot.spaceCode);
                    newEvent.setEntitySpacePermId(spaceSnapshot.spaceTechId != null ? String.valueOf(spaceSnapshot.spaceTechId) : null);
                    newEvent.setEntityProject(new ProjectIdentifier(spaceSnapshot.spaceCode, projectSnapshot.projectCode).toString());
                    newEvent.setEntityProjectPermId(projectPermId);
                    newEvent.setEntityRegisterer(registerer);
                    newEvent.setEntityRegistrationTimestamp(registrationTimestamp);
                    newEvent.setIdentifier(new ProjectIdentifier(spaceSnapshot.spaceCode, projectSnapshot.projectCode).toString());
                    newEvent.setDescription(deletion.getDescription());
                    newEvent.setReason(deletion.getReason());
                    newEvent.setContent(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(experimentEntries));
                    newEvent.setAttachmentContent(deletion.getAttachmentContent() != null ? deletion.getAttachmentContent().getId() : null);
                    newEvent.setRegisterer(deletion.getRegistrator());
                    newEvent.setRegistrationTimestamp(deletion.getRegistrationDate());
                    dataSource.createEventsSearch(newEvent);
                }
            } catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    interface IDataSource
    {

        void open();

        void close();

        IDataSourceTransaction createTransaction();

        List<SpacePE> loadSpaces();

        List<Project> loadProjects(ProjectFetchOptions fo);

        List<Experiment> loadExperiments(ExperimentFetchOptions fo);

        List<EventPE> loadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestampOrNull);

        Date loadLastEventsSearchTimestamp(EventType eventType, EntityType entityType);

        void createEventsSearch(EventsSearchPE eventsSearch);

    }

    interface IDataSourceTransaction
    {
        void commit();

        void rollback();
    }

    private class DataSource implements IDataSource
    {

        private Session session;

        @Override
        public void open()
        {
            session = CommonServiceProvider.getDAOFactory().getSessionFactory().openSession();
        }

        @Override
        public void close()
        {
            session.close();
        }

        @Override
        public IDataSourceTransaction createTransaction()
        {
            Transaction transaction = session.beginTransaction();
            return new DataSourceTransaction(transaction);
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

        @Override
        public List<EventPE> loadEvents(EventType eventType, EntityType entityType, Date lastSeenTimestampOrNull)
        {
            IEventDAO eventDAO = CommonServiceProvider.getDAOFactory().getEventDAO();
            return eventDAO.listEvents(eventType, entityType, lastSeenTimestampOrNull);
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

    private static class DataSourceTransaction implements IDataSourceTransaction
    {

        private Transaction transaction;

        public DataSourceTransaction(Transaction transaction)
        {
            this.transaction = transaction;
        }

        @Override
        public void commit()
        {
            transaction.commit();
        }

        @Override
        public void rollback()
        {
            transaction.rollback();
        }
    }

    static class LastTimestamps
    {
        private IDataSource dataSource;

        private Map<Pair<EventType, EntityType>, Date> timestamps;

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
        protected IDataSource dataSource;

        private Map<String, TreeMap<Date, T>> snapshots = new HashMap<>();

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
                Map.Entry<Date, T> potentialEntry = snapshotsForKey.ceilingEntry(date);

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
                snapshot.from = space.getRegistrationDate();
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
                            lastSpaceRelationship = relationHistoryEntry;
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
                            lastProjectRelationship = relationHistoryEntry;
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

}
