package ch.systemsx.cisd.openbis.generic.server.task;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.history.ExperimentRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.server.task.EventsSearchMaintenanceTask.ExperimentSnapshot;
import ch.systemsx.cisd.openbis.generic.server.task.EventsSearchMaintenanceTask.ExperimentSnapshots;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EventsSearchExperimentSnapshotsTest extends AbstractEventsSearchTest
{

    private TestResources resources = new TestResources(getClass());

    @Test
    public void testCreate()
    {
        /*
        Tests snapshots for the following scenario:
        - create TEST_EXPERIMENT (permId: 20210322160406305-205156) in project (permId: 20210322160218429-205152)
        */

        final String projectPermId = "20210322160218429-205152";
        final String experimentPermId = "20210322160406305-205156";
        final String experimentCode = "TEST_EXPERIMENT";

        Project project = new Project();
        project.setPermId(new ProjectPermId(projectPermId));

        ExperimentFetchOptions experimentFo = new ExperimentFetchOptions();
        experimentFo.withProject();
        experimentFo.withHistory();

        Experiment experiment = new Experiment();
        experiment.setCode(experimentCode);
        experiment.setPermId(new ExperimentPermId(experimentPermId));
        experiment.setProject(project);
        experiment.setHistory(Collections.emptyList());
        experiment.setRegistrationDate(dateTimeMillis("2021-03-22 16:02:43.750"));
        experiment.setFetchOptions(experimentFo);

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(EventType.DELETION, EventPE.EntityType.EXPERIMENT, null);
                will(returnValue(Collections.emptyList()));

                one(dataSource).loadExperiments(with(any(ExperimentFetchOptions.class)));
                will(returnValue(Arrays.asList(experiment)));
            }
        });

        ExperimentSnapshots experimentSnapshots = new ExperimentSnapshots(dataSource);

        ExperimentSnapshot experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:02:43.700"));
        assertNull(experimentSnapshot);

        experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:02:43.800"));
        assertEquals(experimentSnapshot.experimentCode, experimentCode);
        assertEquals(experimentSnapshot.experimentPermId, experimentPermId);
        assertEquals(experimentSnapshot.projectPermId, projectPermId);
        assertEquals(experimentSnapshot.from, dateTimeMillis("2021-03-22 16:02:43.750"));
        assertNull(experimentSnapshot.to);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testCreateMove()
    {
        /*
        Tests snapshots for the following scenario:
        - create TEST_EXPERIMENT (permId: 20210322160406305-205156) in project A (permId: 20210322160218429-205152)
        - move it to project B (permId: 20210322160225869-205153)
        */

        final String projectAPermId = "20210322160218429-205152";
        final String projectBPermId = "20210322160225869-205153";
        final String experimentPermId = "20210322160406305-205156";
        final String experimentCode = "TEST_EXPERIMENT";

        Project projectA = new Project();
        projectA.setPermId(new ProjectPermId(projectAPermId));

        Project projectB = new Project();
        projectB.setPermId(new ProjectPermId(projectBPermId));

        RelationHistoryEntry relationshipWithProjectB = new RelationHistoryEntry();
        relationshipWithProjectB.setRelationType(ExperimentRelationType.PROJECT);
        relationshipWithProjectB.setRelatedObjectId(projectB.getPermId());
        relationshipWithProjectB.setValidFrom(dateTimeMillis("2021-03-22 16:04:53.247"));
        relationshipWithProjectB.setValidTo(dateTimeMillis("2021-03-22 16:05:09.071"));

        ExperimentFetchOptions experimentFo = new ExperimentFetchOptions();
        experimentFo.withProject();
        experimentFo.withHistory();

        Experiment experiment = new Experiment();
        experiment.setCode(experimentCode);
        experiment.setPermId(new ExperimentPermId(experimentPermId));
        experiment.setProject(projectA);
        experiment.setHistory(Arrays.asList(relationshipWithProjectB));
        experiment.setFetchOptions(experimentFo);

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(EventType.DELETION, EventPE.EntityType.EXPERIMENT, null);
                will(returnValue(Collections.emptyList()));

                one(dataSource).loadExperiments(with(any(ExperimentFetchOptions.class)));
                will(returnValue(Arrays.asList(experiment)));
            }
        });

        ExperimentSnapshots experimentSnapshots = new ExperimentSnapshots(dataSource);

        ExperimentSnapshot experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:04:53.200"));
        assertNull(experimentSnapshot);

        experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:04:53.300"));
        assertEquals(experimentSnapshot.experimentCode, experimentCode);
        assertEquals(experimentSnapshot.experimentPermId, experimentPermId);
        assertEquals(experimentSnapshot.projectPermId, projectBPermId);
        assertEquals(experimentSnapshot.from, dateTimeMillis("2021-03-22 16:04:53.247"));
        assertEquals(experimentSnapshot.to, dateTimeMillis("2021-03-22 16:05:09.071"));

        experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:05:09.100"));
        assertEquals(experimentSnapshot.experimentCode, experimentCode);
        assertEquals(experimentSnapshot.experimentPermId, experimentPermId);
        assertEquals(experimentSnapshot.projectPermId, projectAPermId);
        assertEquals(experimentSnapshot.from, dateTimeMillis("2021-03-22 16:05:09.071"));
        assertNull(experimentSnapshot.to);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testCreateDelete()
    {
        /*
        Tests snapshots for the following scenario:
        - create TEST_EXPERIMENT (permId: 20210322160406305-205155) in project (permId: 20210322160225869-205153)
        - delete it
        */

        final String projectPermId = "20210322160225869-205153";
        final String experimentPermId = "20210322160406305-205155";
        final String experimentCode = "TEST_EXPERIMENT";

        EventPE deletion = new EventPE();
        deletion.setId(1L);
        deletion.setRegistrationDate(dateTimeMillis("2021-03-22 16:04:41.679"));
        deletion.setContent(loadFile("testCreateDelete.json"));

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(EventType.DELETION, EventPE.EntityType.EXPERIMENT, null);
                will(returnValue(Arrays.asList(deletion)));

                one(dataSource).loadExperiments(with(any(ExperimentFetchOptions.class)));
                will(returnValue(Collections.emptyList()));
            }
        });

        ExperimentSnapshots experimentSnapshots = new ExperimentSnapshots(dataSource);

        ExperimentSnapshot experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:04:06.300"));
        assertNull(experimentSnapshot);

        experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:04:06.400"));
        assertEquals(experimentSnapshot.experimentCode, experimentCode);
        assertEquals(experimentSnapshot.experimentPermId, experimentPermId);
        assertEquals(experimentSnapshot.projectPermId, projectPermId);
        assertEquals(experimentSnapshot.from, dateTimeMillis("2021-03-22 16:04:06.305"));
        assertEquals(experimentSnapshot.to, dateTimeMillis("2021-03-22 16:04:41.679"));

        experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:04:41.700"));
        assertNull(experimentSnapshot);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testCreateMoveDelete()
    {
        /*
        Tests snapshots for the following scenario:
        - create TEST_EXPERIMENT (permId: 20210322160243866-205154) in project A (permId: 20210322160218429-205152)
        - move it to project B (permId: 20210322160225869-205153)
        - delete it
        */

        final String projectAPermId = "20210322160218429-205152";
        final String projectBPermId = "20210322160225869-205153";
        final String experimentPermId = "20210322160243866-205154";
        final String experimentCode = "TEST_EXPERIMENT";

        EventPE deletion = new EventPE();
        deletion.setId(1L);
        deletion.setRegistrationDate(dateTimeMillis("2021-03-22 16:03:40.178"));
        deletion.setContent(loadFile("testCreateMoveDelete.json"));

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(EventType.DELETION, EventPE.EntityType.EXPERIMENT, null);
                will(returnValue(Arrays.asList(deletion)));

                one(dataSource).loadExperiments(with(any(ExperimentFetchOptions.class)));
                will(returnValue(Collections.emptyList()));
            }
        });

        ExperimentSnapshots experimentSnapshots = new ExperimentSnapshots(dataSource);

        ExperimentSnapshot experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:02:43.800"));
        assertNull(experimentSnapshot);

        experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:02:43.900"));
        assertEquals(experimentSnapshot.experimentCode, experimentCode);
        assertEquals(experimentSnapshot.experimentPermId, experimentPermId);
        assertEquals(experimentSnapshot.projectPermId, projectAPermId);
        assertEquals(experimentSnapshot.from, dateTimeMillis("2021-03-22 16:02:43.866"));
        assertEquals(experimentSnapshot.to, dateTimeMillis("2021-03-22 16:02:59.066"));

        experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:02:59.100"));
        assertEquals(experimentSnapshot.experimentCode, experimentCode);
        assertEquals(experimentSnapshot.experimentPermId, experimentPermId);
        assertEquals(experimentSnapshot.projectPermId, projectBPermId);
        assertEquals(experimentSnapshot.from, dateTimeMillis("2021-03-22 16:02:59.066"));
        assertEquals(experimentSnapshot.to, dateTimeMillis("2021-03-22 16:03:40.178"));

        experimentSnapshot = experimentSnapshots.get(experimentPermId, dateTimeMillis("2021-03-22 16:03:40.200"));
        assertNull(experimentSnapshot);

        mockery.assertIsSatisfied();
    }

    private String loadFile(String fileName)
    {
        return FileUtilities.loadToString(resources.getResourceFile(fileName));
    }

}
