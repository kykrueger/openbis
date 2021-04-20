package ch.systemsx.cisd.openbis.generic.server.task.events_search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.IRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.history.SampleRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class SampleSnapshots extends AbstractSnapshots<SampleSnapshot>
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