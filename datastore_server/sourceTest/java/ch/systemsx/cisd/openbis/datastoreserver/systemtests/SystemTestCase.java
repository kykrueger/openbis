/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil.DOWNLOAD_URL_KEY;
import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil.OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX;
import static ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil.SERVER_URL_KEY;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.common.time.DateTimeUtils;
import ch.systemsx.cisd.etlserver.ETLDaemon;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetRegistrationTransaction;
import ch.systemsx.cisd.openbis.dss.generic.server.DataStoreServer;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.server.util.TestInitializer;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.util.TestInstanceHostUtils;

/**
 * @author Franz-Josef Elmer
 */
public abstract class SystemTestCase extends AssertJUnit
{
    private Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, getClass());

    private static final Pattern PATTERN = Pattern.compile("::(\\d+-\\d+);.*");

    private static final String SOURCE_TEST_CORE_PLUGINS = "sourceTest/core-plugins";

    private static final String UNIT_TEST_WORKING_DIRECTORY = "unit-test-wd";

    private static final String TARGETS_DIRECTORY = "targets";

    private static final File UNIT_TEST_ROOT_DIRECTORY = new File(TARGETS_DIRECTORY
            + File.separator + UNIT_TEST_WORKING_DIRECTORY);

    private static final String ROOT_DIR_KEY = "root-dir";

    private static final String DATA_SET_IMPORTED_LOG_MARKER = "Successfully registered data set";

    // this message appears if the dropbox has successfully completed the registration, even if no
    // datasets have been imported
    private static final String REGISTRATION_FINISHED_LOG_MARKER =
            DataSetRegistrationTransaction.SUCCESS_MESSAGE;

    protected static GenericWebApplicationContext applicationContext;

    protected File workingDirectory;

    protected File rootDir;

    protected File store;

    protected BufferedAppender logAppender;

    protected SystemTestCase()
    {
        createWorkingDirectory();
        rootDir = new File(workingDirectory, "dss-root");
        store = new File(rootDir, "store");
        store.mkdirs();
    }

    protected void createWorkingDirectory()
    {
        workingDirectory = new File(UNIT_TEST_ROOT_DIRECTORY, "SystemTests");
        if (workingDirectory.exists())
        {
            try
            {
                FileUtils.deleteDirectory(workingDirectory);
            } catch (IOException ioex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
            }
        }
        workingDirectory.mkdirs();
        workingDirectory.deleteOnExit();
    }

    @BeforeMethod
    public void beforeTest(Method method)
    {
        System.out.println("BEFORE " + render(method));
    }

    @AfterMethod
    public void afterTest(Method method)
    {
        System.out.println("AFTER  " + render(method));
    }

    private String render(Method method)
    {
        return method.getDeclaringClass().getName() + "." + method.getName();
    }

    // @BeforeTest
    // public void setUpLogAppender()
    // {
    // logAppender = new BufferedAppender();
    // }

    @BeforeSuite
    public void beforeSuite() throws Exception
    {
        setUpDatabaseProperties();
        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        connector.setPort(TestInstanceHostUtils.getOpenBISPort());
        server.addConnector(connector);
        DispatcherServlet dispatcherServlet = new DispatcherServlet()
            {
                private static final long serialVersionUID = 1L;

                @Override
                protected WebApplicationContext findWebApplicationContext()
                {
                    XmlBeanFactory f =
                            new XmlBeanFactory(new FileSystemResource(
                                    "../openbis/resource/server/spring-servlet.xml"));
                    applicationContext = new GenericWebApplicationContext(f);
                    applicationContext.setParent(new ClassPathXmlApplicationContext(
                            getApplicationContextLocation()));
                    applicationContext.refresh();
                    return applicationContext;
                }
            };
        ServletContextHandler sch =
                new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        sch.addServlet(new ServletHolder(dispatcherServlet), "/*");
        server.start();

        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "inputs", "");
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "core-plugins-folder",
                SOURCE_TEST_CORE_PLUGINS);
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + Constants.ENABLED_MODULES_KEY,
                getEnabledTechnologies());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + ROOT_DIR_KEY,
                rootDir.getAbsolutePath());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX
                + DssPropertyParametersUtil.DSS_REGISTRATION_LOG_DIR_PATH, getRegistrationLogDir()
                .getAbsolutePath());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "dss-rpc.put-default", "test");
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + SERVER_URL_KEY,
                TestInstanceHostUtils.getOpenBISUrl());
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + "port",
                Integer.toString(TestInstanceHostUtils.getDSSPort()));
        System.setProperty(OPENBIS_DSS_SYSTEM_PROPERTIES_PREFIX + DOWNLOAD_URL_KEY,
                TestInstanceHostUtils.getDSSUrl());
        System.setProperty(SERVER_URL_KEY, TestInstanceHostUtils.getOpenBISUrl());
        System.setProperty("port", Integer.toString(TestInstanceHostUtils.getDSSPort()));
        System.setProperty(DOWNLOAD_URL_KEY, TestInstanceHostUtils.getDSSUrl());

        QueueingPathRemoverService.start(rootDir, ETLDaemon.shredderQueueFile);
        DataStoreServer.main(new String[0]);
        ETLDaemon.runForTesting(new String[0]);
    }

    private String getEnabledTechnologies()
    {
        File corePluginsFolder = new File(SOURCE_TEST_CORE_PLUGINS);
        String[] list = corePluginsFolder.list();
        CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
        for (String technology : list)
        {
            builder.append(technology);
        }
        return builder.toString();
    }

    /**
     * sets up the openbis database to be used by the tests.
     */
    protected void setUpDatabaseProperties()
    {
        TestInitializer.initWithIndex();
    }

    /**
     * Return the location of the openBIS application context config.
     */
    protected String getApplicationContextLocation()
    {
        return "classpath:applicationContext.xml";
    }

    /**
     * the path to the default incoming directory
     */
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming");
    }

    protected File getRegistrationLogDir()
    {
        return new File(workingDirectory, "log-registrations");
    }

    protected void waitUntilDataSetImported() throws Exception
    {
        waitUntilDataSetImported(new ILogMonitoringStopCondition()
            {
                @Override
                public boolean stopConditionFulfilled(ParsedLogEntry logEntry)
                {
                    String logMessage = logEntry.getLogMessage();
                    return logMessage.contains(DATA_SET_IMPORTED_LOG_MARKER)
                            || logMessage.contains(REGISTRATION_FINISHED_LOG_MARKER);
                }
            });
    }

    protected void waitUntilDataSetImported(ILogMonitoringStopCondition stopCondition) throws Exception
    {
        final int maxLoops = dataSetImportWaitDurationInSeconds();

        for (int loops = 0; loops < maxLoops; loops++)
        {
            Thread.sleep(1000);
            List<ParsedLogEntry> logEntries = getLogEntries();
            for (ParsedLogEntry logEntry : logEntries)
            {
                if (stopCondition.stopConditionFulfilled(logEntry))
                {
                    operationLog.info("Monitoring log stop after this log entry: " + logEntry);
                    return;
                }
            }
        }
        fail("Log monitoring stop condition (" + stopCondition + ") never fulfilled after " + maxLoops + " seconds.");

    }
    
    private List<ParsedLogEntry> getLogEntries()
    {
        List<ParsedLogEntry> result = new ArrayList<ParsedLogEntry>();
        String[] logLines = getLogAppender().getLogContent().split("\n");
        Pattern pattern = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}),\\d{3} ([^ ]*) \\[(.*)\\] (.*)$");
        SimpleDateFormat dateFormat = new SimpleDateFormat(BasicConstant.DATE_WITHOUT_TIMEZONE_PATTERN);
        for (String logLine : logLines)
        {
            Matcher matcher = pattern.matcher(logLine);
            if (matcher.matches())
            {
                try
                {
                    Date timestamp = dateFormat.parse(matcher.group(1));
                    String logLevel = matcher.group(2);
                    String threadName = matcher.group(3);
                    String logMessage = matcher.group(4);
                    result.add(new ParsedLogEntry(timestamp, logLevel, threadName, logMessage));
                } catch (ParseException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
        }
        return result;
    }

    protected void waitUntilDataSetImportedWithError(String dropboxName) throws Exception
    {
        final int maxLoops = dataSetImportWaitDurationInSeconds();

        for (int loops = 0; loops < maxLoops; loops++)
        {
            Thread.sleep(1000);

            String logContent = getLogAppender().getLogContent();
            if (logContent.contains("ERROR") && logContent.contains(dropboxName))
            {
                return;
            }
        }

        fail("Failed to determine whether data set import was executed with error");
    }
    
    protected void waitUntilIndexedByLucene(Class<?> entityPeClass, Long entityId) throws Exception
    {
        operationLog.info("Waiting for " + entityPeClass.getName() + " with id: " + entityId + " to be indexed by Lucene");

        final int maxLoops = indexByLuceneWaitDurationInSeconds();

        for (int loops = 0; loops < maxLoops; loops++)
        {
            Thread.sleep(1000);

            if (getLogAppender().getLogContent().contains("REINDEX " + entityPeClass.getName() + ": [" + entityId + "]"))
            {
                operationLog.info("Stopped waiting for " + entityPeClass.getName() + " with id: " + entityId + " to be indexed by Lucene");
                return;
            }
        }

        fail("Failed to determine whether enity: " + entityPeClass.getName() + " with id: " + entityId + " was indexed by Lucene");
    }

    /**
     * Time to wait to determine if a data set has been registered or not. Subclasses may override.
     */
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 20;
    }

    protected int indexByLuceneWaitDurationInSeconds()
    {
        return 20;
    }

    protected void moveFileToIncoming(File exampleDataSet) throws IOException
    {
        FileUtils.moveDirectoryToDirectory(exampleDataSet, getIncomingDirectory(), false);
    }

    private BufferedAppender getLogAppender()
    {
        if (logAppender == null)
        {
            logAppender = new BufferedAppender("%d %p [%t] %c - %m%n", Level.INFO);
        }
        return logAppender;
    }

    @SuppressWarnings("unchecked")
    protected static <T> T getBean(String beanName)
    {
        return (T) applicationContext.getBean(beanName);
    }

}
