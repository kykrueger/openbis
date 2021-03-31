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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.concurrent.MessageChannel;

/**
 * @author Franz-Josef Elmer
 */
public class MaintenancePluginTest
{
    @DataProvider(name = "properties")
    public static Object[][] properties()
    {
        return new Object[][] {
                new Object[] { MaintenanceTaskParameters.INTERVAL_KEY, "2 sec" },
                new Object[] { MaintenanceTaskParameters.RUN_SCHEDULE_KEY,
                        MaintenanceTaskParameters.CRON_PREFIX + "   */2   *  * * *   *   " }
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
        System.out.println("Execution dates: " + executionDates);
        for (int i = 1; i < executionDates.size(); i++)
        {
            long tim1 = executionDates.get(i - 1).getTime();
            long ti = executionDates.get(i).getTime();
            int expectedDifference = 2000;
            int tolerance = 10;
            if (Math.abs(ti - tim1 - expectedDifference) > tolerance)
            {
                fail("Unexpected execution time stamp difference between "
                        + "timestamp[" + (i - 1) + "] = " + tim1 + " and timestamp[" + i + "] = " + ti
                        + ". Expected " + expectedDifference + " +- " + tolerance + ".");
            }
        }
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
