package ch.systemsx.cisd.common.maintenance;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.utilities.ClassUtils;

public class MaintenancePlugin
{
    private final IMaintenanceTask task;

    private final MaintenanceTaskParameters parameters;

    public MaintenancePlugin(MaintenanceTaskParameters parameters)
    {
        this.parameters = parameters;
        try
        {
            this.task = ClassUtils.create(IMaintenanceTask.class, parameters.getClassName());
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot find the plugin class '"
                    + parameters.getClassName() + "'", CheckedExceptionTunnel.unwrapIfNecessary(ex));
        }
        task.setUp(parameters.getPluginName(), parameters.getProperties());
    }

    public void start()
    {
        final String timerThreadName = parameters.getPluginName() + " - Maintenance Plugin";
        final Timer workerTimer = new Timer(timerThreadName);
        TimerTask timerTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    task.execute();
                }
            };
        Date startDate = parameters.getStartDate();
        if (parameters.isExecuteOnlyOnce())
        {
            workerTimer.schedule(timerTask, startDate);
        } else
        {
            workerTimer.schedule(timerTask, startDate, parameters.getIntervalSeconds() * 1000);
        }
    }
}