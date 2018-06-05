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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.activation.DataHandler;

import org.apache.log4j.Level;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.maintenance.MaintenanceTaskParameters;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.util.PluginUtils;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;

/**
 * @author Franz-Josef Elmer
 */
public class UsageReportingTaskTest extends AbstractFileSystemTestCase
{
    private static final String D = UsageReportingTask.DELIM;

    private BufferedAppender logRecorder;

    private File configFile;

    private Properties properties;

    private Mockery context;

    private IMailClient mailClient;

    private RecordingMatcher<String> subjectRecorder;

    private RecordingMatcher<String> contentRecorder;

    private RecordingMatcher<String> fileNameRecorder;

    private RecordingMatcher<DataHandler> attachmentRecorder;

    private RecordingMatcher<EMailAddress[]> recipientRecorder;

    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.INFO);
        context = new Mockery();
        mailClient = context.mock(IMailClient.class);
        subjectRecorder = new RecordingMatcher<String>();
        contentRecorder = new RecordingMatcher<String>();
        fileNameRecorder = new RecordingMatcher<String>();
        attachmentRecorder = new RecordingMatcher<DataHandler>();
        recipientRecorder = new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    allowing(mailClient).sendEmailMessageWithAttachment(with(subjectRecorder), with(contentRecorder),
                            with(fileNameRecorder), with(attachmentRecorder), with(new IsNull<EMailAddress>()),
                            with(new IsNull<EMailAddress>()), with(recipientRecorder));
                }
            });
        configFile = new File(workingDirectory, "config.json");
        properties = new Properties();
        properties.setProperty(UsageReportingTask.CONFIGURATION_FILE_PATH_PROPERTY, configFile.getPath());
        properties.setProperty(PluginUtils.EMAIL_ADDRESSES_KEY, "a1@bc.de, a2@bc.de");
    }

    @Test
    public void testWeeklyReportForGroups() throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(12345));
        task.user("u1").group("A").newExperiments(2).newDataSets(1);
        task.user("u1").group("C").newSamples(1);
        task.user("u2").group("A");
        task.user("u2").group("B").newExperiments(2);
        task.user("u2").group("A1").newSamples(2).newDataSets(5);
        task.user("u3").group("A");
        task.user("u3").group("B").newExperiments(3);
        FileUtilities.writeToFile(configFile, "");
        properties.setProperty(MaintenanceTaskParameters.INTERVAL_KEY, "7 d");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"B\"}, {\"key\":\"A\"}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized. Configuration file: " + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1969-12-21 00:00:00 until 1969-12-28 00:00:00\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertPeriod("1969-12-21 00:00:00", "1969-12-28 00:00:00", task.period);
        assertEquals("[B, A]", task.groups.toString());
        assertRecorders(subjectRecorder, "Usage report for the period from 1969-12-21 00:00:00 until 1969-12-28 00:00:00", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1969-12-21_1969-12-28.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(0).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new experiments" + D + "number of new samples" + D + "number of new data sets\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "A" + D + "3" + D + "u2 u3" + D + "2" + D + "0" + D + "1\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "B" + D + "2" + D + D + "5" + D + "0" + D + "0\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "A1" + D + "1" + D + D + "0" + D + "2" + D + "5\n"
                + "1969-12-21 00:00:00" + D + "1969-12-28 00:00:00" + D + "C" + D + "1" + D + D + "0" + D + "1" + D + "0\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }

    @Test
    public void testMonthlyReportForGroups() throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(12345));
        task.user("u1").group("A").newExperiments(2).newDataSets(1);
        task.user("u2").group("B").newExperiments(3);
        FileUtilities.writeToFile(configFile, "");
        properties.setProperty(MaintenanceTaskParameters.INTERVAL_KEY, "30 d");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"B\"}]}");
        
        // When
        task.execute();
        
        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized. Configuration file: " + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1969-12-01 00:00:00 until 1970-01-01 00:00:00\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertPeriod("1969-12-01 00:00:00", "1970-01-01 00:00:00", task.period);
        assertEquals("[B]", task.groups.toString());
        assertRecorders(subjectRecorder, "Usage report for the period from 1969-12-01 00:00:00 until 1970-01-01 00:00:00", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1969-12-01_1970-01-01.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(0).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new experiments" + D + "number of new samples" + D + "number of new data sets\n"
                + "1969-12-01 00:00:00" + D + "1970-01-01 00:00:00" + D + "B" + D + "1" + D + D + "3" + D + "0" + D + "0\n"
                + "1969-12-01 00:00:00" + D + "1970-01-01 00:00:00" + D + "A" + D + "1" + D + D + "2" + D + "0" + D + "1\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }
    
    
    @Test
    public void testDailyReportWithoutGroups() throws IOException
    {
        // Given
        UsageReportingTaskWithMocks task = new UsageReportingTaskWithMocks(mailClient).time(new Date(12345));
        task.user("u1").group("A").newExperiments(2).newDataSets(1);
        task.user("u1").group("C").newSamples(1);
        task.user("u2").group("A");
        task.user("u2").group("B").newExperiments(2);
        task.user("u2").group("A1").newSamples(2).newDataSets(5);
        task.user("u3").group("A");
        task.user("u3").group("B").newExperiments(3);
        properties.remove(UsageReportingTask.CONFIGURATION_FILE_PATH_PROPERTY);
        properties.setProperty(MaintenanceTaskParameters.INTERVAL_KEY, "1 d");
        task.setUp("", properties);
        
        // When
        task.execute();
        
        // Then
        assertEquals("INFO  OPERATION.UsageReportingTaskWithMocks - Setup plugin \n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Plugin '' initialized.\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Gather usage information for the period from "
                + "1969-12-31 00:00:00 until 1970-01-01 00:00:00\n"
                + "INFO  OPERATION.UsageReportingTaskWithMocks - Usage report created and sent.", logRecorder.getLogContent());
        assertPeriod("1969-12-31 00:00:00", "1970-01-01 00:00:00", task.period);
        assertEquals(null, task.groups);
        assertRecorders(subjectRecorder, "Usage report for the period from 1969-12-31 00:00:00 until 1970-01-01 00:00:00", 2);
        assertRecorders(contentRecorder, "The usage report can be found in the attached TSV file.", 2);
        assertRecorders(fileNameRecorder, "usage_report_1969-12-31_1970-01-01.tsv", 2);
        assertEquals("EmailAddress{email=a1@bc.de}", recipientRecorder.getRecordedObjects().get(0)[0].toString());
        assertEquals("EmailAddress{email=a2@bc.de}", recipientRecorder.getRecordedObjects().get(1)[0].toString());
        assertEquals(2, recipientRecorder.getRecordedObjects().size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        attachmentRecorder.getRecordedObjects().get(1).writeTo(baos);
        assertEquals("period start" + D + "period end" + D + "group name" + D + "number of users" + D + "idle users" + D
                + "number of new experiments" + D + "number of new samples" + D + "number of new data sets\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "A" + D + "3" + D + "u2 u3" + D + "2" + D + "0" + D + "1\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "A1" + D + "1" + D + D + "0" + D + "2" + D + "5\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "B" + D + "2" + D + D + "5" + D + "0" + D + "0\n"
                + "1969-12-31 00:00:00" + D + "1970-01-01 00:00:00" + D + "C" + D + "1" + D + D + "0" + D + "1" + D + "0\n", baos.toString());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(0).getContentType());
        assertEquals("text/plain", attachmentRecorder.getRecordedObjects().get(1).getContentType());
        assertEquals(2, attachmentRecorder.getRecordedObjects().size());
        context.assertIsSatisfied();
    }
    
    private void assertRecorders(RecordingMatcher<?> recorder, String expectedRecord, int expectedCount)
    {
        List<?> objects = recorder.getRecordedObjects();
        for (int i = 0; i < expectedCount; i++)
        {
            assertEquals("Object index " + i, expectedRecord, objects.get(i).toString());
        }
    }

    private void assertPeriod(String expectedFrom, String expectedUntil, Period period)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat(UsageReportingTask.DATE_FORMAT);
        assertEquals(expectedFrom, dateFormat.format(period.getFrom()));
        assertEquals(expectedUntil, dateFormat.format(period.getUntil()));
    }

    private static final class UsageReportingTaskWithMocks extends UsageReportingTask
    {
        private final IMailClient mailClient;

        private Date actualTimeStamp;

        private Map<String, Map<String, UsageInfo>> usageByUsersAndGroups = new TreeMap<>();

        private List<String> groups;

        private Period period;

        UsageReportingTaskWithMocks(IMailClient mailClient)
        {
            this.mailClient = mailClient;

        }

        UsageReportingTaskWithMocks time(Date actualTimeStamp)
        {
            this.actualTimeStamp = actualTimeStamp;
            return this;
        }

        UsageByGroupsBuilder user(String user)
        {
            Map<String, UsageInfo> usageByGroups = usageByUsersAndGroups.get(user);
            if (usageByGroups == null)
            {
                usageByGroups = new TreeMap<>();
                usageByUsersAndGroups.put(user, usageByGroups);
            }
            return new UsageByGroupsBuilder(usageByGroups);
        }

        @Override
        protected Date getActualTimeStamp()
        {
            return actualTimeStamp;
        }

        @Override
        protected Map<String, Map<String, UsageInfo>> gatherUsage(List<String> groups, Period period)
        {
            this.groups = groups;
            this.period = period;
            return usageByUsersAndGroups;
        }

        @Override
        protected IMailClient getMailClient()
        {
            return mailClient;
        }
    }

    private static final class UsageByGroupsBuilder
    {
        private Map<String, UsageInfo> usageByGroups;

        UsageByGroupsBuilder(Map<String, UsageInfo> usageByGroups)
        {
            this.usageByGroups = usageByGroups;
        }

        UsageInfoBuilder group(String group)
        {
            UsageInfo usageInfo = new UsageInfo();
            usageByGroups.put(group, usageInfo);
            return new UsageInfoBuilder(usageInfo);
        }
    }

    private static final class UsageInfoBuilder
    {
        private UsageInfo usageInfo;

        UsageInfoBuilder(UsageInfo usageInfo)
        {
            this.usageInfo = usageInfo;
        }

        UsageInfoBuilder newExperiments(int numberOfNewExperiments)
        {
            for (; numberOfNewExperiments > 0; numberOfNewExperiments--)
            {
                usageInfo.addNewExperiment();
            }
            return this;
        }

        UsageInfoBuilder newSamples(int numberOfNewSamples)
        {
            for (; numberOfNewSamples > 0; numberOfNewSamples--)
            {
                usageInfo.addNewSample();
            }
            return this;
        }

        UsageInfoBuilder newDataSets(int numberOfNewDataSets)
        {
            for (; numberOfNewDataSets > 0; numberOfNewDataSets--)
            {
                usageInfo.addNewDataSet();
            }
            return this;
        }

    }
}
