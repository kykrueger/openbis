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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import com.fasterxml.jackson.databind.ObjectMapper;
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

            SpaceSnapshots spaceSnapshots = new SpaceSnapshots(dataSource);
            ProjectSnapshots projectSnapshots = new ProjectSnapshots(dataSource);

            transaction.commit();
        } catch (Exception e)
        {
            transaction.rollback();
        } finally
        {
            dataSource.close();
        }
    }

    interface IDataSource
    {

        void open();

        void close();

        IDataSourceTransaction createTransaction();

        List<Space> loadSpaces(SpaceFetchOptions fo);

        List<Project> loadProjects(ProjectFetchOptions fo);

        List<Experiment> loadExperiments(ExperimentFetchOptions fo);

        List<EventPE> loadEvents(EventType eventType, EntityType entityType, Long lastSeenEventIdOrNull);

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
        public List<Space> loadSpaces(SpaceFetchOptions fo)
        {
            IApplicationServerInternalApi v3 = CommonServiceProvider.getApplicationServerApi();
            String sessionToken = v3.loginAsSystem();
            SearchResult<Space> result = v3.searchSpaces(sessionToken, new SpaceSearchCriteria(), fo);
            return result.getObjects();
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
        public List<EventPE> loadEvents(EventType eventType, EntityType entityType, Long lastSeenEventIdOrNull)
        {
            IEventDAO eventDAO = CommonServiceProvider.getDAOFactory().getEventDAO();
            return eventDAO.listEvents(eventType, entityType, lastSeenEventIdOrNull);
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

    static abstract class AbstractSnapshots<T extends AbstractSnapshot>
    {
        private Map<String, List<T>> snapshots = new HashMap<>();

        public AbstractSnapshots(IDataSource dataSource)
        {
            this.snapshots = this.load(dataSource);
        }

        protected abstract Map<String, List<T>> load(IDataSource dataSource);

        public T get(String key, Date date)
        {
            List<T> snapshotsForKey = snapshots.get(key);

            if (snapshotsForKey != null)
            {
                for (T snapshot : snapshotsForKey)
                {
                    if ((snapshot.from == null || snapshot.from.compareTo(date) <= 0) && (snapshot.to == null
                            || snapshot.to.compareTo(date) >= 0))
                    {
                        return snapshot;
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
        }

        @Override
        protected Map<String, List<SpaceSnapshot>> load(IDataSource dataSource)
        {
            Map<String, List<SpaceSnapshot>> snapshotsMap = new HashMap<>();

            List<EventPE> deletions = dataSource.loadEvents(EventType.DELETION, EntityType.SPACE, null);

            ArrayList<EventPE> sortedDeletions = new ArrayList<>(deletions);
            sortedDeletions.sort(new Comparator<EventPE>()
            {
                @Override public int compare(EventPE o1, EventPE o2)
                {
                    return 10 * o1.getIdentifiers().get(0).compareTo(o2.getIdentifiers().get(0)) + o1.getRegistrationDate()
                            .compareTo(o2.getRegistrationDate());
                }
            });

            EventPE previousDeletion = null;
            for (EventPE sortedDeletion : sortedDeletions)
            {
                SpaceSnapshot snapshot = new SpaceSnapshot();

                if (previousDeletion != null && previousDeletion.getIdentifiers().equals(sortedDeletion.getIdentifiers()))
                {
                    snapshot.from = previousDeletion.getRegistrationDate();
                }

                snapshot.to = sortedDeletion.getRegistrationDate();
                snapshot.spaceCode = sortedDeletion.getIdentifiers().get(0);

                List<SpaceSnapshot> snapshots = snapshotsMap.computeIfAbsent(snapshot.spaceCode, k -> new ArrayList<>());
                snapshots.add(snapshot);

                previousDeletion = sortedDeletion;
            }

            List<Space> spaces = dataSource.loadSpaces(new SpaceFetchOptions());

            for (Space space : spaces)
            {
                SpaceSnapshot snapshot = new SpaceSnapshot();
                snapshot.from = space.getRegistrationDate();
                snapshot.spaceCode = space.getCode();

                List<SpaceSnapshot> snapshots = snapshotsMap.computeIfAbsent(snapshot.spaceCode, k -> new ArrayList<>());
                snapshots.add(snapshot);
            }

            return snapshotsMap;
        }
    }

    static class SpaceSnapshot extends AbstractSnapshot
    {

        public String spaceCode;

    }

    static class ProjectSnapshots extends AbstractSnapshots<ProjectSnapshot>
    {

        private static final SimpleDateFormat validDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        private static final ObjectMapper objectMapper = new ObjectMapper();

        public ProjectSnapshots(IDataSource dataSource)
        {
            super(dataSource);
        }

        @Override
        protected Map<String, List<ProjectSnapshot>> load(IDataSource dataSource)
        {
            Map<String, List<ProjectSnapshot>> snapshotsMap = new HashMap<>();

            loadDeleted(dataSource, snapshotsMap);
            loadExisting(dataSource, snapshotsMap);

            return snapshotsMap;
        }

        private void loadDeleted(IDataSource dataSource, Map<String, List<ProjectSnapshot>> snapshotsMap)
        {
            List<EventPE> deletions = dataSource.loadEvents(EventType.DELETION, EntityType.PROJECT, null);

            for (EventPE deletion : deletions)
            {
                try
                {
                    Map<String, Object> parsedContent = (Map<String, Object>) objectMapper.readValue(deletion.getContent(), Object.class);

                    for (String projectPermId : parsedContent.keySet())
                    {
                        List<Map<String, String>> projectEntries = (List<Map<String, String>>) parsedContent.get(projectPermId);

                        String projectCode = null;
                        for (Map<String, String> projectEntry : projectEntries)
                        {
                            String type = projectEntry.get("type");
                            String key = projectEntry.get("key");

                            if ("ATTRIBUTE".equals(type) && "CODE".equals(key))
                            {
                                projectCode = projectEntry.get("value");
                            }
                        }

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
                                    snapshot.from = validDateFormat.parse(validFrom);
                                }

                                if (validUntil != null)
                                {
                                    snapshot.to = validDateFormat.parse(validUntil);
                                } else
                                {
                                    snapshot.to = deletion.getRegistrationDate();
                                }

                                List<ProjectSnapshot> snapshots = snapshotsMap.computeIfAbsent(snapshot.projectPermId, k -> new ArrayList<>());
                                snapshots.add(snapshot);
                            }
                        }
                    }
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        private void loadExisting(IDataSource dataSource, Map<String, List<ProjectSnapshot>> snapshotsMap)
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

                            List<ProjectSnapshot> snapshots = snapshotsMap.computeIfAbsent(snapshot.projectPermId, k -> new ArrayList<>());
                            snapshots.add(snapshot);

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

                List<ProjectSnapshot> snapshots = snapshotsMap.computeIfAbsent(snapshot.projectPermId, k -> new ArrayList<>());
                snapshots.add(snapshot);
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

        private static final SimpleDateFormat validDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        private static final ObjectMapper objectMapper = new ObjectMapper();

        public ExperimentSnapshots(IDataSource dataSource)
        {
            super(dataSource);
        }

        @Override
        protected Map<String, List<ExperimentSnapshot>> load(IDataSource dataSource)
        {
            Map<String, List<ExperimentSnapshot>> snapshotsMap = new HashMap<>();

            loadDeleted(dataSource, snapshotsMap);
            loadExisting(dataSource, snapshotsMap);

            return snapshotsMap;
        }

        private void loadDeleted(IDataSource dataSource, Map<String, List<ExperimentSnapshot>> snapshotsMap)
        {
            List<EventPE> deletions = dataSource.loadEvents(EventType.DELETION, EntityType.EXPERIMENT, null);

            for (EventPE deletion : deletions)
            {
                try
                {
                    Map<String, Object> parsedContent = (Map<String, Object>) objectMapper.readValue(deletion.getContent(), Object.class);

                    for (String experimentPermId : parsedContent.keySet())
                    {
                        List<Map<String, String>> experimentEntries = (List<Map<String, String>>) parsedContent.get(experimentPermId);

                        String experimentCode = null;
                        for (Map<String, String> experimentEntry : experimentEntries)
                        {
                            String type = experimentEntry.get("type");
                            String key = experimentEntry.get("key");

                            if ("ATTRIBUTE".equals(type) && "CODE".equals(key))
                            {
                                experimentCode = experimentEntry.get("value");
                            }
                        }

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
                                    snapshot.from = validDateFormat.parse(validFrom);
                                }

                                if (validUntil != null)
                                {
                                    snapshot.to = validDateFormat.parse(validUntil);
                                } else
                                {
                                    snapshot.to = deletion.getRegistrationDate();
                                }

                                List<ExperimentSnapshot> snapshots = snapshotsMap.computeIfAbsent(snapshot.experimentPermId, k -> new ArrayList<>());
                                snapshots.add(snapshot);
                            }
                        }
                    }
                } catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        private void loadExisting(IDataSource dataSource, Map<String, List<ExperimentSnapshot>> snapshotsMap)
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

                            List<ExperimentSnapshot> snapshots = snapshotsMap.computeIfAbsent(snapshot.experimentPermId, k -> new ArrayList<>());
                            snapshots.add(snapshot);

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

                List<ExperimentSnapshot> snapshots = snapshotsMap.computeIfAbsent(snapshot.experimentPermId, k -> new ArrayList<>());
                snapshots.add(snapshot);
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
