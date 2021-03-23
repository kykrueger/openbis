package ch.systemsx.cisd.openbis.generic.server.task;

import ch.systemsx.cisd.openbis.generic.server.task.EventsSearchMaintenanceTask.IDataSource;
import ch.systemsx.cisd.openbis.generic.server.task.EventsSearchMaintenanceTask.IDataSourceTransaction;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractEventsSearchTest
{
    private static final DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final DateFormat DATE_TIME_MILLIS_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    protected Mockery mockery;

    protected IDataSource dataSource;

    protected IDataSourceTransaction transaction;

    @BeforeMethod
    public void beforeMethod()
    {
        mockery = new Mockery();
        dataSource = mockery.mock(IDataSource.class);
        transaction = mockery.mock(IDataSourceTransaction.class);
    }

    public static Date dateTime(String dateTime)
    {
        try
        {
            return DATE_TIME_FORMAT.parse(dateTime);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static Date dateTimeMillis(String dateTimeMillis)
    {
        try
        {
            return DATE_TIME_MILLIS_FORMAT.parse(dateTimeMillis);
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

}
