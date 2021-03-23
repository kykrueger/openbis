package ch.systemsx.cisd.openbis.generic.server.task;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.history.ProjectRelationType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.TestResources;
import ch.systemsx.cisd.openbis.generic.server.task.EventsSearchMaintenanceTask.ProjectSnapshot;
import ch.systemsx.cisd.openbis.generic.server.task.EventsSearchMaintenanceTask.ProjectSnapshots;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EventsSearchProjectSnapshotsTest extends AbstractEventsSearchTest
{

    private TestResources resources = new TestResources(getClass());

    @Test
    public void testCreate()
    {
        /*
        Tests snapshots for the following scenario:
        - create TEST_PROJECT (permId: 20210322115435296-205146) in SPACE_A
        */

        final String spaceACode = "SPACE_A";
        final String projectPermId = "20210322115435296-205146";
        final String projectCode = "TEST_PROJECT";

        Space spaceA = new Space();
        spaceA.setCode(spaceACode);

        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withSpace();
        fo.withHistory();

        Project project = new Project();
        project.setCode(projectCode);
        project.setPermId(new ProjectPermId(projectPermId));
        project.setSpace(spaceA);
        project.setHistory(Collections.emptyList());
        project.setRegistrationDate(dateTimeMillis("2021-03-22 11:55:24.753"));
        project.setFetchOptions(fo);

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(EventType.DELETION, EventPE.EntityType.PROJECT, null);
                will(returnValue(Collections.emptyList()));

                one(dataSource).loadProjects(with(any(ProjectFetchOptions.class)));
                will(returnValue(Arrays.asList(project)));
            }
        });

        ProjectSnapshots snapshots = new ProjectSnapshots(dataSource);

        ProjectSnapshot snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:55:24.700"));
        assertNull(snapshot);

        snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:55:24.800"));
        assertEquals(snapshot.projectCode, projectCode);
        assertEquals(snapshot.projectPermId, projectPermId);
        assertEquals(snapshot.spaceCode, spaceACode);
        assertEquals(snapshot.from, dateTimeMillis("2021-03-22 11:55:24.753"));
        assertNull(snapshot.to);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testCreateMove()
    {
        /*
        Tests snapshots for the following scenario:
        - create TEST_PROJECT (permId: 20210322115435296-205146) in SPACE_A
        - move it to SPACE_B
        */

        final String spaceACode = "SPACE_A";
        final String spaceBCode = "SPACE_B";
        final String projectPermId = "20210322115435296-205146";
        final String projectCode = "TEST_PROJECT";

        Space spaceA = new Space();
        spaceA.setCode(spaceACode);

        Space spaceB = new Space();
        spaceB.setCode(spaceBCode);

        RelationHistoryEntry relationshipWithSpaceA = new RelationHistoryEntry();
        relationshipWithSpaceA.setRelationType(ProjectRelationType.SPACE);
        relationshipWithSpaceA.setRelatedObjectId(new SpacePermId(spaceA.getCode()));
        relationshipWithSpaceA.setValidFrom(dateTimeMillis("2021-03-22 11:55:24.753"));
        relationshipWithSpaceA.setValidTo(dateTimeMillis("2021-03-22 11:55:39.182"));

        ProjectFetchOptions fo = new ProjectFetchOptions();
        fo.withSpace();
        fo.withHistory();

        Project project = new Project();
        project.setCode(projectCode);
        project.setPermId(new ProjectPermId(projectPermId));
        project.setSpace(spaceB);
        project.setHistory(Arrays.asList(relationshipWithSpaceA));
        project.setFetchOptions(fo);

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(EventType.DELETION, EventPE.EntityType.PROJECT, null);
                will(returnValue(Collections.emptyList()));

                one(dataSource).loadProjects(with(any(ProjectFetchOptions.class)));
                will(returnValue(Arrays.asList(project)));
            }
        });

        ProjectSnapshots snapshots = new ProjectSnapshots(dataSource);

        ProjectSnapshot snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:55:24.700"));
        assertNull(snapshot);

        snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:55:24.800"));
        assertEquals(snapshot.projectCode, projectCode);
        assertEquals(snapshot.projectPermId, projectPermId);
        assertEquals(snapshot.spaceCode, spaceACode);
        assertEquals(snapshot.from, dateTimeMillis("2021-03-22 11:55:24.753"));
        assertEquals(snapshot.to, dateTimeMillis("2021-03-22 11:55:39.182"));

        snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:55:39.200"));
        assertEquals(snapshot.projectCode, projectCode);
        assertEquals(snapshot.projectPermId, projectPermId);
        assertEquals(snapshot.spaceCode, spaceBCode);
        assertEquals(snapshot.from, dateTimeMillis("2021-03-22 11:55:39.182"));

        mockery.assertIsSatisfied();
    }

    @Test
    public void testCreateDelete()
    {
        /*
        Tests snapshots for the following scenario:
        - create TEST_PROJECT (permId: 20210322115435296-205145) in SPACE_B
        - delete it
        */

        final String spaceBCode = "SPACE_B";
        final String projectPermId = "20210322115435296-205145";
        final String projectCode = "TEST_PROJECT";

        EventPE deletion = new EventPE();
        deletion.setId(1L);
        deletion.setRegistrationDate(dateTimeMillis("2021-03-22 11:55:09.112"));
        deletion.setContent(loadFile("testCreateDelete.json"));

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(EventType.DELETION, EventPE.EntityType.PROJECT, null);
                will(returnValue(Arrays.asList(deletion)));

                one(dataSource).loadProjects(with(any(ProjectFetchOptions.class)));
                will(returnValue(Collections.emptyList()));
            }
        });

        ProjectSnapshots snapshots = new ProjectSnapshots(dataSource);

        ProjectSnapshot snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:54:35.200"));
        assertNull(snapshot);

        snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:54:35.300"));
        assertEquals(snapshot.projectCode, projectCode);
        assertEquals(snapshot.projectPermId, projectPermId);
        assertEquals(snapshot.spaceCode, spaceBCode);
        assertEquals(snapshot.from, dateTimeMillis("2021-03-22 11:54:35.296"));
        assertEquals(snapshot.to, dateTimeMillis("2021-03-22 11:55:09.112"));

        snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:55:09.200"));
        assertNull(snapshot);

        mockery.assertIsSatisfied();
    }

    @Test
    public void testCreateMoveDelete()
    {
        /*
        Tests snapshots for the following scenario:
        - create TEST_PROJECT (permId: 20210322115319628-205144) in SPACE_A
        - move it to SPACE_B
        - delete it
        */

        final String spaceACode = "SPACE_A";
        final String spaceBCode = "SPACE_B";
        final String projectPermId = "20210322115319628-205144";
        final String projectCode = "TEST_PROJECT";

        EventPE deletion = new EventPE();
        deletion.setId(1L);
        deletion.setRegistrationDate(dateTimeMillis("2021-03-22 11:54:17.025"));
        deletion.setContent(loadFile("testCreateMoveDelete.json"));

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadEvents(EventType.DELETION, EventPE.EntityType.PROJECT, null);
                will(returnValue(Arrays.asList(deletion)));

                one(dataSource).loadProjects(with(any(ProjectFetchOptions.class)));
                will(returnValue(Collections.emptyList()));
            }
        });

        ProjectSnapshots snapshots = new ProjectSnapshots(dataSource);

        ProjectSnapshot snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:53:19.000"));
        assertNull(snapshot);

        snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:53:19.700"));
        assertEquals(snapshot.projectCode, projectCode);
        assertEquals(snapshot.projectPermId, projectPermId);
        assertEquals(snapshot.spaceCode, spaceACode);
        assertEquals(snapshot.from, dateTimeMillis("2021-03-22 11:53:19.628"));
        assertEquals(snapshot.to, dateTimeMillis("2021-03-22 11:53:48.806"));

        snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:53:48.900"));
        assertEquals(snapshot.projectCode, projectCode);
        assertEquals(snapshot.projectPermId, projectPermId);
        assertEquals(snapshot.spaceCode, spaceBCode);
        assertEquals(snapshot.from, dateTimeMillis("2021-03-22 11:53:48.806"));
        assertEquals(snapshot.to, dateTimeMillis("2021-03-22 11:54:17.025"));

        snapshot = snapshots.get(projectPermId, dateTimeMillis("2021-03-22 11:54:17.100"));
        assertNull(snapshot);

        mockery.assertIsSatisfied();
    }

    private String loadFile(String fileName)
    {
        return FileUtilities.loadToString(resources.getResourceFile(fileName));
    }

}
