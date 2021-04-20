package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class ExperimentSnapshots extends AbstractSnapshots
{

    public ExperimentSnapshots(IDataSource dataSource)
    {
        super(dataSource);
    }

    @Override protected String getKey(Snapshot snapshot)
    {
        return snapshot.entityPermId;
    }

    protected List<Snapshot> doLoad(Collection<String> experimentPermIds)
    {
        List<Snapshot> snapshots = new ArrayList<>();

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
                        Snapshot snapshot = new Snapshot();
                        snapshot.entityCode = experiment.getCode();
                        snapshot.entityPermId = experiment.getPermId().getPermId();
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

            Snapshot snapshot = new Snapshot();
            snapshot.entityCode = experiment.getCode();
            snapshot.entityPermId = experiment.getPermId().getPermId();
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