package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;

import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class Snapshots
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

        Collection<Snapshot> snapshots = projectSnapshots.get(projectPermIds);
        Set<String> spaceCodes = snapshots.stream().map(snapshot -> snapshot.spaceCode).collect(Collectors.toSet());

        loadExistingSpaces(spaceCodes);
    }

    public void loadExistingExperiments(Collection<String> experimentPermIds)
    {
        experimentSnapshots.load(experimentPermIds);

        Collection<Snapshot> snapshots = experimentSnapshots.get(experimentPermIds);
        Set<String> projectPermIds = snapshots.stream().map(snapshot -> snapshot.projectPermId).collect(Collectors.toSet());

        loadExistingProjects(projectPermIds);
    }

    public void loadExistingSamples(Collection<String> samplePermIds)
    {
        sampleSnapshots.load(samplePermIds);

        Collection<Snapshot> snapshots = sampleSnapshots.get(samplePermIds);

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

    public void putDeletedSpace(Snapshot snapshot)
    {
        spaceSnapshots.put(snapshot.entityCode, snapshot);
    }

    public void putDeletedProject(Snapshot snapshot)
    {
        projectSnapshots.put(snapshot.entityPermId, snapshot);
    }

    public void putDeletedExperiment(Snapshot snapshot)
    {
        experimentSnapshots.put(snapshot.entityPermId, snapshot);
    }

    public void putDeletedSample(Snapshot snapshot)
    {
        sampleSnapshots.put(snapshot.entityPermId, snapshot);
    }

    public Snapshot getSpace(String spaceCode, Date date)
    {
        return spaceSnapshots.get(spaceCode, date);
    }

    public Snapshot getProject(String projectPermId, Date date)
    {
        return projectSnapshots.get(projectPermId, date);
    }

    public Snapshot getExperiment(String experimentPermId, Date date)
    {
        return experimentSnapshots.get(experimentPermId, date);
    }

    public Snapshot getSample(String samplePermId, Date date)
    {
        return sampleSnapshots.get(samplePermId, date);
    }

    public void fillBySpaceCode(String spaceCode, NewEvent newEvent)
    {
        Snapshot spaceSnapshot = spaceSnapshots.get(spaceCode, newEvent.registrationTimestamp);

        if (spaceSnapshot != null)
        {
            newEvent.entitySpaceCode = spaceSnapshot.entityCode;
            newEvent.entitySpacePermId = spaceSnapshot.entityPermId;
        }
    }

    public void fillByProjectPermId(String projectPermId, NewEvent newEvent)
    {
        Snapshot projectSnapshot = projectSnapshots.get(projectPermId, newEvent.registrationTimestamp);

        if (projectSnapshot != null)
        {
            newEvent.entityProjectPermId = projectPermId;

            fillBySpaceCode(projectSnapshot.spaceCode, newEvent);

            if (newEvent.entitySpaceCode != null)
            {
                newEvent.entityProject = new ProjectIdentifier(newEvent.entitySpaceCode, projectSnapshot.entityCode).toString();
            }
        }
    }

    public void fillByExperimentPermId(String experimentPermId, NewEvent newEvent)
    {
        Snapshot experimentSnapshot = experimentSnapshots.get(experimentPermId, newEvent.registrationTimestamp);

        if (experimentSnapshot != null)
        {
            fillByProjectPermId(experimentSnapshot.projectPermId, newEvent);
        }
    }

    public void fillBySamplePermId(String samplePermId, NewEvent newEvent)
    {
        Snapshot sampleSnapshot = sampleSnapshots.get(samplePermId, newEvent.registrationTimestamp);

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
            }
        }
    }
}