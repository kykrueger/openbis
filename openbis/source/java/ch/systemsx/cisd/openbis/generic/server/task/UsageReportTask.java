/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

/**
 * @author Franz-Josef Elmer
 */
public class UsageReportTask extends AbstractMaintenanceTask
{
    private PeriodType periodType;

    @Override
    protected void setUpSpecific(Properties properties)
    {
        long interval = DateTimeUtils.getDurationInMillis(properties, MaintenanceTaskParameters.INTERVAL_KEY, DateUtils.MILLIS_PER_DAY);
        periodType = PeriodType.getBestType(interval);
    }

    @Override
    public void execute()
    {
        List<String> groups = getGroups();
        Period period = periodType.getPeriod(getActualTimeStamp());
        Map<String, Map<String, UsageInfo>> usageByUsersAndGroups = createGatherer().gatherUsage(period, groups);
    }

    private List<String> getGroups()
    {
        UserManagerConfig config = readGroupDefinitions(null);
        if (config == null)
        {
            return null;
        }
        return config.getGroups().stream().map(UserGroup::getKey).collect(Collectors.toList());
    }

    protected Date getActualTimeStamp()
    {
        return new Date();
    }
    
    protected UsageGatherer createGatherer()
    {
        return new UsageGatherer(CommonServiceProvider.getApplicationServerApi());
    }

}
