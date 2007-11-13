/*
 * Copyright 2007 ETH Zuerich, CISD
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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.datamover.filesystem.FileStoreFactory;
import ch.systemsx.cisd.datamover.filesystem.FileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.utils.LocalBufferDirs;

/**
 * The main class of the Datamover.
 * 
 * @author Bernd Rinn
 */
public class Main
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, Main.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, Main.class);

    private static final UncaughtExceptionHandler loggingExceptionHandler = new UncaughtExceptionHandler()
        {
            public void uncaughtException(Thread t, Throwable e)
            {
                notificationLog.error("An exception has occurred [thread: '" + t.getName() + "'].", e);
            }
        };

    private static final Runnable loggingShutdownHook = new Runnable()
        {
            public void run()
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Datamover is shutting down.");
                }
            }
        };

    private static void initLog()
    {
        LogInitializer.init();
        Thread.setDefaultUncaughtExceptionHandler(loggingExceptionHandler);
    }

    private static void printInitialLogMessage(final Parameters parameters)
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Datamover is starting up.");
        }
        for (String line : BuildAndEnvironmentInfo.INSTANCE.getEnvironmentInfo())
        {
            operationLog.info(line);
        }
        parameters.log();
        Runtime.getRuntime().addShutdownHook(new Thread(loggingShutdownHook, "Shutdown Hook"));
    }

    /**
     * performs a self-test.
     */
    private static void selfTest(final Parameters parameters)
    {
        final String msgStart = "Datamover self test failed:";
        try
        {
            ArrayList<FileStore> stores = new ArrayList<FileStore>();
            FileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
            stores.add(parameters.getIncomingStore(factory));
            FileStore buferStore =
                    FileStoreFactory.createLocal(parameters.getBufferDirectoryPath(), Parameters.BUFFER_KIND_DESC,
                            factory);
            stores.add(buferStore);
            stores.add(parameters.getOutgoingStore(factory));
            if (parameters.tryGetManualInterventionDir() != null)
            {
                FileStore dummyStore =
                        FileStoreFactory.createLocal(parameters.tryGetManualInterventionDir(), "manual intervention",
                                factory);
                stores.add(dummyStore);
            }
            if (parameters.tryGetExtraCopyDir() != null)
            {
                FileStore dummyStore =
                        FileStoreFactory.createLocal(parameters.tryGetExtraCopyDir(), "extra-copy", factory);
                stores.add(dummyStore);
            }
            IPathCopier copyProcess = factory.getCopier(false);
            SelfTest.check(copyProcess, stores.toArray(new FileStore[] {}));
        } catch (HighLevelException e)
        {
            System.err.printf(msgStart + " [%s: %s]\n", e.getClass().getSimpleName(), e.getMessage());
            System.exit(1);
        } catch (RuntimeException e)
        {
            System.err.println(msgStart);
            e.printStackTrace();
            System.exit(1);
        }
    }

    /** exposed for testing purposes */
    static ITerminable startupServer(Parameters parameters, LocalBufferDirs bufferDirs)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        return DataMover.start(parameters, factory, bufferDirs);
    }

    private static void startupServer(Parameters parameters)
    {
        final IFileSysOperationsFactory factory = new FileSysOperationsFactory(parameters);
        DataMover.start(parameters, factory);
    }

    public static void main(String[] args)
    {
        initLog();
        final Parameters parameters = new Parameters(args);
        printInitialLogMessage(parameters);
        selfTest(parameters);
        startupServer(parameters);
        operationLog.info("Datamover ready and waiting for data.");
    }

}
