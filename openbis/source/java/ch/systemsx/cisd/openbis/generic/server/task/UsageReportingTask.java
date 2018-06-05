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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.util.PluginUtils;

/**
 * Maintenance task which report usage of openBIS by users.
 * 
 * @author Franz-Josef Elmer
 */
public class UsageReportingTask extends AbstractMaintenanceTask
{
    static final String DELIM = "\t";

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private PeriodType periodType;

    private List<EMailAddress> eMailAddresses;

    public UsageReportingTask()
    {
        super(false);
    }

    @Override
    protected void setUpSpecific(Properties properties)
    {
        long interval = DateTimeUtils.getDurationInMillis(properties, MaintenanceTaskParameters.INTERVAL_KEY, DateUtils.MILLIS_PER_DAY);
        periodType = PeriodType.getBestType(interval);
        eMailAddresses = PluginUtils.getEMailAddresses(properties, ",");
    }

    @Override
    public void execute()
    {
        List<String> groups = getGroups();
        Period period = periodType.getPeriod(getActualTimeStamp());
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String fromDateString = dateFormat.format(period.getFrom());
        String untilDateString = dateFormat.format(period.getUntil());
        operationLog.info("Gather usage information for the period from " + fromDateString + " until " + untilDateString);
        Map<String, Map<String, UsageInfo>> usageByUsersAndGroups = gatherUsage(groups, period);
        String report = createReport(usageByUsersAndGroups, period, groups);
        sendReport(fromDateString, untilDateString, report);
        operationLog.info("Usage report created and sent.");
    }

    protected Date getActualTimeStamp()
    {
        return new Date();
    }

    protected Map<String, Map<String, UsageInfo>> gatherUsage(List<String> groups, Period period)
    {
        return new UsageGatherer(CommonServiceProvider.getApplicationServerApi()).gatherUsage(period, groups);
    }

    protected IMailClient getMailClient()
    {
        return CommonServiceProvider.createEMailClient();
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

    private void sendReport(String fromDateString, String untilDateString, String report)
    {
        IMailClient mailClient = getMailClient();
        String subject = "Usage report for the period from " + fromDateString + " until " + untilDateString;
        String fileName = "usage_report_" + fromDateString.split(" ")[0] + "_" + untilDateString.split(" ")[0] + ".tsv";
        try
        {
            for (EMailAddress eMailAddress : eMailAddresses)
            {
                mailClient.sendEmailMessageWithAttachment(subject, "The usage report can be found in the attached TSV file.",
                        fileName, new DataHandler(new ByteArrayDataSource(report, "text/plain")), null, null, eMailAddress);
            }
        } catch (IOException e)
        {
            notificationLog.error("Couldn't sent usage report", e);
        }
    }

    private String createReport(Map<String, Map<String, UsageInfo>> usageByUsersAndGroups, Period period, List<String> groups)
    {
        StringBuilder builder = new StringBuilder();
        Set<String> groupSet = groups == null ? new HashSet<>() : new HashSet<>(groups);
        Map<String, GroupInfo> groupInfos = new TreeMap<>();
        Map<String, GroupInfo> otherGroupInfos = new TreeMap<>();
        for (Entry<String, Map<String, UsageInfo>> entry : usageByUsersAndGroups.entrySet())
        {
            String user = entry.getKey();
            for (Entry<String, UsageInfo> entry2 : entry.getValue().entrySet())
            {
                String group = entry2.getKey();
                GroupInfo groupInfo = getGroupInfo(groupSet.contains(group) ? groupInfos : otherGroupInfos, group);
                UsageInfo info = entry2.getValue();
                groupInfo.handle(user, info);
            }
        }
        builder.append("period start" + DELIM + "period end" + DELIM + "group name" + DELIM + "number of users" + DELIM
                + "idle users" + DELIM + "number of new experiments" + DELIM + "number of new samples" + DELIM
                + "number of new data sets\n");
        addInfos(builder, period, groupInfos);
        addInfos(builder, period, otherGroupInfos);
        return builder.toString();
    }

    private GroupInfo getGroupInfo(Map<String, GroupInfo> infos, String group)
    {
        GroupInfo groupInfo = infos.get(group);
        if (groupInfo == null)
        {
            groupInfo = new GroupInfo();
            infos.put(group, groupInfo);
        }
        return groupInfo;
    }

    private void addInfos(StringBuilder builder, Period period, Map<String, GroupInfo> infos)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String fromDate = dateFormat.format(period.getFrom());
        String untilDate = dateFormat.format(period.getUntil());
        Set<Entry<String, GroupInfo>> entrySet = infos.entrySet();
        for (Entry<String, GroupInfo> entry : entrySet)
        {
            builder.append(fromDate).append(DELIM).append(untilDate).append(DELIM).append(entry.getKey()).append(DELIM);
            GroupInfo info = entry.getValue();
            builder.append(info.getNumberOfUsers()).append(DELIM);
            builder.append(StringUtils.join(info.getIdleUsers(), ' ')).append(DELIM);
            builder.append(info.getNumberOfNewExperiments()).append(DELIM);
            builder.append(info.getNumberOfNewSamples()).append(DELIM);
            builder.append(info.getNumberOfNewDataSets()).append("\n");
        }
    }

    private static final class GroupInfo
    {
        private Set<String> allUsers = new HashSet<>();

        private Set<String> activeUsers = new HashSet<>();

        private int numberOfNewExperiments;

        private int numberOfNewSamples;

        private int numberOfNewDataSets;

        void handle(String user, UsageInfo usageInfo)
        {
            allUsers.add(user);
            if (usageInfo.isIdle() == false)
            {
                activeUsers.add(user);
            }
            numberOfNewExperiments += usageInfo.getNumberOfNewExperiments();
            numberOfNewSamples += usageInfo.getNumberOfNewSamples();
            numberOfNewDataSets += usageInfo.getNumberOfNewDataSets();
        }

        int getNumberOfUsers()
        {
            return allUsers.size();
        }

        Set<String> getIdleUsers()
        {
            Set<String> idleUsers = new TreeSet<>(allUsers);
            idleUsers.removeAll(activeUsers);
            return idleUsers;
        }

        int getNumberOfNewExperiments()
        {
            return numberOfNewExperiments;
        }

        int getNumberOfNewSamples()
        {
            return numberOfNewSamples;
        }

        int getNumberOfNewDataSets()
        {
            return numberOfNewDataSets;
        }
    }
}
