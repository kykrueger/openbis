/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.test.LogMonitoringAppender;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.IExitHandler;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * Test cases for the {@link IncomingProcessor} class.
 * 
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataMoverProcess.class)
public final class IncomingProcessorTest
{

    private static final File TEST_FOLDER = new File("targets/unit-test/IncomingProcessorTest");

    private static final String INCOMING_DIR = "incoming";

    private static final String COPY_IN_PROGRESS_DIR = "copy-in-progress";

    private static final String COPY_COMPLETE_DIR = "copy-complete";

    private static final String READY_TO_MOVE_DIR = "ready-to-move";

    private static final String TEMP_DIR = "temp";

    private static final String EXAMPLE_SCRIPT_NAME = "example-script.sh";

    private static final String EXAMPLE_SCRIPT = "echo hello world";

    private static final File TEST_FILE = new File(TEST_FOLDER, "blabla.txt");

    private BufferedAppender logRecorder;

    private Mockery context;

    private IFileSysOperationsFactory fileSysOpertationFactory;

    private IPathMover mover;

    private IPathRemover remover;

    private File incomingDir;

    private IExitHandler exitHandler;

    private File copyInProgressDir;

    private File copyCompleteDir;

    private File exampleScript;

    @BeforeMethod
    public void setUp()
    {
        LogInitializer.init();
        logRecorder = new BufferedAppender("%m%n", Level.DEBUG);
        context = new Mockery();
        fileSysOpertationFactory = context.mock(IFileSysOperationsFactory.class);
        mover = context.mock(IPathMover.class);
        remover = context.mock(IPathRemover.class);
        exitHandler = context.mock(IExitHandler.class);

        FileUtilities.deleteRecursively(TEST_FOLDER);
        TEST_FOLDER.mkdirs();
        exampleScript = new File(TEST_FOLDER, EXAMPLE_SCRIPT_NAME);
        incomingDir = new File(TEST_FOLDER, INCOMING_DIR);
        incomingDir.mkdir();
        copyInProgressDir = new File(TEST_FOLDER, COPY_IN_PROGRESS_DIR);
        copyInProgressDir.mkdir();
        copyCompleteDir = new File(TEST_FOLDER, COPY_COMPLETE_DIR);
        copyCompleteDir.mkdir();
        new File(TEST_FOLDER, READY_TO_MOVE_DIR).mkdir();
        new File(TEST_FOLDER, TEMP_DIR).mkdir();
    }

    @AfterMethod
    public void tearDown()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testWithoutDataCompletedScript() throws IOException
    {
        final File testDataFile = new File(incomingDir, "test-data.txt");
        testDataFile.createNewFile();
        context.checking(new Expectations()
            {
                {
                    one(mover).tryMove(testDataFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
                }
            });

        final DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_TARGET, incomingDir.toString(), "-q",
                        "1");
        final TimerTask dataMoverTimerTask = process.getTimerTask();

        final LogMonitoringAppender operationAppender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION,
                        "Following store item 'test-data.txt' has been handled",
                        "has been added to faulty paths file");
        dataMoverTimerTask.run(); // 1. round finds a file to process
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        operationAppender.verifyLogHasHappened();
        LogMonitoringAppender.removeAppender(operationAppender);
        context.assertIsSatisfied();
    }

    @Test
    public void testWithDataCompletedScript() throws IOException
    {
        FileUtilities.writeToFile(exampleScript, EXAMPLE_SCRIPT);
        final File testDataFile = new File(incomingDir, "test-data.txt");
        testDataFile.createNewFile();
        context.checking(new Expectations()
            {
                {
                    one(mover).tryMove(testDataFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
                }
            });

        final DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_TARGET, incomingDir.toString(), "-q",
                        "1", "--" + PropertyNames.DATA_COMPLETED_SCRIPT, exampleScript.toString());
        final LogMonitoringAppender notifyAppender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        "Processing status of data completed script has changed");
        final LogMonitoringAppender operationAppender =
                LogMonitoringAppender.addAppender(LogCategory.OPERATION, "Running command",
                        "process returned with exit value 0");

        final TimerTask dataMoverTimerTask = process.getTimerTask();
        dataMoverTimerTask.run(); // 1. round finds a file to process
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        notifyAppender.verifyLogHasHappened();
        operationAppender.verifyLogHasHappened();

        logRecorder.resetLogContent();
        dataMoverTimerTask.run(); // 3. round does not change status, thus no log
        assertEquals("", logRecorder.getLogContent());
        context.assertIsSatisfied();
    }

    @Test
    public void testWithDataCompletedScriptWhichFailsInitially() throws IOException
    {
        FileUtilities.writeToFile(exampleScript, EXAMPLE_SCRIPT + "\nrm -v "
                + TEST_FILE.toString().replace('\\', '/'));
        final File testDataFile = new File(incomingDir, "test-data.txt");
        testDataFile.createNewFile();
        context.checking(new Expectations()
            {
                {
                    one(mover).tryMove(testDataFile, copyCompleteDir, "");
                    will(returnValue(new File(copyCompleteDir, testDataFile.getName())));
                }
            });

        final DataMoverProcess process =
                createProcess("--" + PropertyNames.INCOMING_TARGET, incomingDir.toString(), "-q",
                        "1", "--" + PropertyNames.DATA_COMPLETED_SCRIPT, exampleScript.toString());
        final TimerTask dataMoverTimerTask = process.getTimerTask();
        dataMoverTimerTask.run(); // 1. round finds a file to process
        dataMoverTimerTask.run(); // 2. round finds that quiet period is over
        final LogMonitoringAppender notifyAppender =
                LogMonitoringAppender.addAppender(LogCategory.NOTIFY,
                        "Processing status of data completed script has changed");

        logRecorder.resetLogContent();
        dataMoverTimerTask.run(); // 3. round does not change status, thus no log
        assertEquals("", logRecorder.getLogContent());
        notifyAppender.verifyLogHasNotHappened();

        logRecorder.resetLogContent();
        notifyAppender.reset();
        TEST_FILE.createNewFile();
        dataMoverTimerTask.run(); // 4. round finds changed status, thus log
        assertTrue(logRecorder.getLogContent().length() > 0);
        notifyAppender.verifyLogHasHappened();

        context.assertIsSatisfied();
    }

    private DataMoverProcess createProcess(final String... args)
    {
        final Parameters parameters = new Parameters(args, exitHandler);
        final LocalBufferDirs localBufferDirs =
                new LocalBufferDirs(new HostAwareFileWithHighwaterMark(TEST_FOLDER),
                        COPY_IN_PROGRESS_DIR, COPY_COMPLETE_DIR, READY_TO_MOVE_DIR, TEMP_DIR);
        context.checking(new Expectations()
            {
                {
                    allowing(fileSysOpertationFactory).getMover();
                    will(returnValue(mover));

                    allowing(fileSysOpertationFactory).getRemover();
                    will(returnValue(remover));
                }
            });
        return IncomingProcessor.createMovingProcess(parameters, ".marker", fileSysOpertationFactory,
                new MockTimeProvider(), localBufferDirs);

    }
}
