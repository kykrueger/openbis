/*
 * Copyright 2020 ETH Zuerich, SIS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.dss.plugins;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.utilities.ITimeProvider;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.etlserver.TopLevelDataSetRegistratorGlobalState;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;

/**
 * @author Franz-Josef Elmer
 */
public class PreStagingCleanUpMaintenanceTask implements IMaintenanceTask
{

    public static final String DEFAULT_MAINTENANCE_TASK_NAME = "pre-staging-clean-up-task";

    public static final int DEFAULT_MAINTENANCE_TASK_INTERVAL = (int) (DateUtils.MILLIS_PER_DAY / 1000);

    public static final String MINIMUM_AGE_IN_DAYS = "minimum-age-in-days";

    public static final int DEFAULT_MINIMUM_AGE_IN_DAYS = 30;

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            PreStagingCleanUpMaintenanceTask.class);

    private long minimumAge;

    private File storeRoot;

    private ITimeProvider timeProvider;

    public PreStagingCleanUpMaintenanceTask()
    {
    }

    PreStagingCleanUpMaintenanceTask(File storeRoot, ITimeProvider timeProvider)
    {
        this.storeRoot = storeRoot;
        this.timeProvider = timeProvider;
    }

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        minimumAge = PropertyUtils.getInt(properties, MINIMUM_AGE_IN_DAYS, DEFAULT_MINIMUM_AGE_IN_DAYS) * DateUtils.MILLIS_PER_DAY;

    }

    @Override
    public void execute()
    {
        File[] shares = SegmentedStoreUtils.getShares(getStoreRoot());
        for (File share : shares)
        {
            File[] files = new File(share, TopLevelDataSetRegistratorGlobalState.DEFAULT_PRE_STAGING_DIR).listFiles();
            if (files != null)
            {
                for (File file : files)
                {
                    String dateStamp = file.getName().split("_")[0];
                    try
                    {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStamp);
                        if (date.getTime() + minimumAge < getTimeProvider().getTimeInMilliseconds())
                        {
                            FileUtilities.deleteRecursively(file);
                            operationLog.info("Stale folder deleted: " + file.getAbsolutePath());
                        }
                    } catch (ParseException e)
                    {
                        // such files are not of interest
                    }
                }
            }
        }
    }

    private File getStoreRoot()
    {
        if (storeRoot == null)
        {
            storeRoot = ServiceProvider.getConfigProvider().getStoreRoot();
        }
        return storeRoot;
    }

    private ITimeProvider getTimeProvider()
    {
        if (timeProvider == null)
        {
            timeProvider = SystemTimeProvider.SYSTEM_TIME_PROVIDER;
        }
        return timeProvider;
    }

}