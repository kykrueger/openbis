/*
 * Copyright 2021 ETH Zuerich, SIS
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

package ch.systemsx.cisd.common.maintenance;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.concurrent.MessageChannel;
import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * @author Franz-Josef Elmer
 */
public class MaintenancePluginTest extends AbstractFileSystemTestCase
{
    private static final String CRON = MaintenanceTaskParameters.CRON_PREFIX;

    @DataProvider(name = "properties")
    public static Object[][] properties()
    {
        return new Object[][] {
                new Object[] { MaintenanceTaskParameters.INTERVAL_KEY, "2 sec" },
                new Object[] { MaintenanceTaskParameters.RUN_SCHEDULE_KEY, CRON + "*/2 * * * * *" }
        };
    }

    @Test(dataProvider = "properties")
    public void test(String property, String value)
    {
        // Given
        MessageChannel messageChannel = new MessageChannel(10000);
        List<Date> executionDates = new ArrayList<>();
        TestMaintenanceTask task = new TestMaintenanceTask(messageChannel, executionDates);
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, TestMaintenanceTask.class.getName());
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_FILE_KEY, getPersistentNextDateFile().getPath());
        properties.setProperty(property, value);
        MaintenanceTaskParameters parameters = new MaintenanceTaskParameters(properties, "test");
        MaintenancePlugin plugin = new MaintenancePlugin(task, parameters);

        // When
        plugin.start();
        for (int i = 1; i <= 3; i++)
        {
            messageChannel.assertNextMessage(i + ". execution");
        }
        plugin.shutdown();

        // Then
        assertEquals(executionDates.size(), 3);
        System.out.println("(" + property + "=" + value + "): Execution dates: " + executionDates);
        int expectedDifference = 2000;
        int tolerance = 10;
        for (int i = 1; i < executionDates.size(); i++)
        {
            assertTimestampDifference(executionDates, i, expectedDifference, tolerance);
        }
    }

    @Test
    public void testMissedNext()
    {
        // Given
        MessageChannel messageChannel = new MessageChannel(10000);
        List<Date> executionDates = new ArrayList<>();
        TestMaintenanceTask task = new TestMaintenanceTask(messageChannel, executionDates);
        Properties properties = new Properties();
        properties.setProperty(MaintenanceTaskParameters.CLASS_KEY, TestMaintenanceTask.class.getName());
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_KEY, CRON + "*/5 * * * * *");
        properties.setProperty(MaintenanceTaskParameters.RUN_SCHEDULE_FILE_KEY, getPersistentNextDateFile().getPath());
        MaintenanceTaskParameters parameters = new MaintenanceTaskParameters(properties, "test");
        MaintenancePlugin plugin = new MaintenancePlugin(task, parameters);
        Date now = new Date();
        Date missedNext = new Date(parameters.getNextTimestampProvider().getNextTimestamp(now).getTime() - 5000);
        System.out.println("now:" + now + ", missedNext:" + missedNext);
        FileUtilities.writeToFile(getPersistentNextDateFile(),
                new SimpleDateFormat(MaintenancePlugin.TIME_STAMP_FORMAT).format(missedNext));

        // When
        plugin.start();
        for (int i = 1; i <= 3; i++)
        {
            messageChannel.assertNextMessage(i + ". execution");
        }
        plugin.shutdown();

        // Then
        assertEquals(executionDates.size(), 3);
        System.out.println("Execution dates: " + executionDates);
        assertTimestampDifference(executionDates, 1, missedNext.getTime() + 5000 - now.getTime(), 10);
        assertTimestampDifference(executionDates, 2, 5000, 10);
    }

    private void assertTimestampDifference(List<Date> dates, int i, long expectedDifference, int tolerance)
    {
        long tim1 = dates.get(i - 1).getTime();
        long ti = dates.get(i).getTime();
        if (Math.abs(ti - tim1 - expectedDifference) > tolerance)
        {
            fail("Unexpected execution time stamp difference between "
                    + "timestamp[" + (i - 1) + "] = " + tim1 + " and timestamp[" + i + "] = " + ti
                    + ". Expected " + expectedDifference + " +- " + tolerance + ".");
        }
    }

    private File getPersistentNextDateFile()
    {
        return new File(workingDirectory, "nextDate.txt");
    }

    public static class TestMaintenanceTask implements IMaintenanceTask
    {
        private MessageChannel messageChannel;

        private List<Date> executionDates;

        private int counter;

        TestMaintenanceTask(MessageChannel messageChannel, List<Date> executionDates)
        {
            this.messageChannel = messageChannel;
            this.executionDates = executionDates;
        }

        @Override
        public void setUp(String pluginName, Properties properties)
        {
        }

        @Override
        public void execute()
        {
            executionDates.add(new Date());
            messageChannel.send(++counter + ". execution");
        }

    }
}
