package ch.systemsx.cisd.openbis.generic.server.task;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.systemsx.cisd.openbis.generic.server.task.EventsSearchMaintenanceTask.SpaceSnapshot;
import ch.systemsx.cisd.openbis.generic.server.task.EventsSearchMaintenanceTask.SpaceSnapshots;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.jmock.Expectations;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class EventsSearchSpaceSnapshotsTest extends AbstractEventsSearchTest
{

    @Test
    public void test()
    {
        EventPE deletionA1 = new EventPE();
        deletionA1.setId(1L);
        deletionA1.setIdentifiers(Collections.singletonList("A"));
        deletionA1.setRegistrationDate(dateTime("2000-01-01 01:00:00"));

        EventPE deletionA2 = new EventPE();
        deletionA2.setId(2L);
        deletionA2.setIdentifiers(Collections.singletonList("A"));
        deletionA2.setRegistrationDate(dateTime("2000-01-01 02:00:00"));

        EventPE deletionB = new EventPE();
        deletionB.setId(3L);
        deletionB.setIdentifiers(Collections.singletonList("B"));
        deletionB.setRegistrationDate(dateTime("2000-01-01 03:00:00"));

        Space spaceA = new Space();
        spaceA.setCode("A");
        spaceA.setRegistrationDate(dateTime("2000-01-01 10:00:00"));

        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadSpaces(with(any(SpaceFetchOptions.class)));
                will(returnValue(Arrays.asList(spaceA)));

                one(dataSource).loadEvents(EventType.DELETION, EntityType.SPACE, null);
                will(returnValue(Arrays.asList(deletionA2, deletionB, deletionA1)));
            }
        });

        SpaceSnapshots snapshots = new SpaceSnapshots(dataSource);

        SpaceSnapshot snapshot = snapshots.get("A", dateTime("2000-01-01 00:59:59"));
        assertEquals(snapshot.spaceCode, "A");

        snapshot = snapshots.get("A", dateTime("2000-01-01 01:00:00"));
        assertEquals(snapshot.spaceCode, "A");

        snapshot = snapshots.get("A", dateTime("2000-01-01 01:00:01"));
        assertEquals(snapshot.spaceCode, "A");

        snapshot = snapshots.get("A", dateTime("2000-01-01 02:00:00"));
        assertEquals(snapshot.spaceCode, "A");

        snapshot = snapshots.get("A", dateTime("2000-01-01 02:00:01"));
        assertNull(snapshot);

        snapshot = snapshots.get("A", dateTime("2000-01-01 09:59:59"));
        assertNull(snapshot);

        snapshot = snapshots.get("A", dateTime("2000-01-01 10:00:00"));
        assertEquals(snapshot.spaceCode, "A");

        snapshot = snapshots.get("B", dateTime("2000-01-01 00:00:00"));
        assertEquals(snapshot.spaceCode, "B");

        snapshot = snapshots.get("B", dateTime("2000-01-01 03:00:00"));
        assertEquals(snapshot.spaceCode, "B");

        snapshot = snapshots.get("B", dateTime("2000-01-01 03:00:01"));
        assertNull(snapshot);

        mockery.assertIsSatisfied();
    }

}
