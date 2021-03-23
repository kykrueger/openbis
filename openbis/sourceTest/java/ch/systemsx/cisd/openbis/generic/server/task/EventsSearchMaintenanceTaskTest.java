package ch.systemsx.cisd.openbis.generic.server.task;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

public class EventsSearchMaintenanceTaskTest extends AbstractEventsSearchTest
{

    @BeforeMethod
    public void beforeMethod()
    {
        super.beforeMethod();
        mockery.checking(new Expectations()
        {
            {
                one(dataSource).open();

                one(dataSource).createTransaction();
                will(returnValue(transaction));

                one(dataSource).close();
            }
        });
    }

    @Test
    public void test()
    {
        mockery.checking(new Expectations()
        {
            {
                one(dataSource).loadSpaces(with(any(SpaceFetchOptions.class)));
                will(returnValue(Collections.emptyList()));

                one(dataSource).loadEvents(EventType.DELETION, EntityType.SPACE, null);
                will(returnValue(Collections.emptyList()));

                one(transaction).commit();
            }
        });

        EventsSearchMaintenanceTask task = new EventsSearchMaintenanceTask(dataSource);
        task.execute();

        mockery.assertIsSatisfied();
    }

}
