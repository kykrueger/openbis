package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class ProjectSnapshots extends AbstractSnapshots
{

    public ProjectSnapshots(IDataSource dataSource)
    {
        super(dataSource);
    }

    protected void doLoad(Collection<String> projectPermIds)
    {
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
                        Snapshot snapshot = new Snapshot();
                        snapshot.entityCode = project.getCode();
                        snapshot.entityPermId = project.getPermId().getPermId();
                        snapshot.spaceCode = ((SpacePermId) relationHistoryEntry.getRelatedObjectId()).getPermId();
                        snapshot.from = relationHistoryEntry.getValidFrom();
                        snapshot.to = relationHistoryEntry.getValidTo();

                        put(snapshot.entityPermId, snapshot);

                        if (lastSpaceRelationship == null || relationHistoryEntry.getValidFrom().after(lastSpaceRelationship.getValidFrom()))
                        {
                            lastSpaceRelationship = relationHistoryEntry;
                        }
                    }
                }
            }

            Snapshot snapshot = new Snapshot();
            snapshot.entityCode = project.getCode();
            snapshot.entityPermId = project.getPermId().getPermId();
            snapshot.spaceCode = project.getSpace().getCode();

            if (lastSpaceRelationship != null)
            {
                snapshot.from = lastSpaceRelationship.getValidTo();
            } else
            {
                snapshot.from = project.getRegistrationDate();
            }

            put(snapshot.entityPermId, snapshot);
        }
    }
}