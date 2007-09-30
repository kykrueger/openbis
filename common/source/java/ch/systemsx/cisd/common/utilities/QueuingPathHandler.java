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

package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Asynchronous path handler. Queues tasks and processes them in a separate thread. Use {{@link #terminate()} to clean
 * resources if you do not need the instance of this class anymore.
 * 
 * @author Tomasz Pylak on Aug 24, 2007
 */
public class QueuingPathHandler implements ITerminable, IPathHandler, IRecoverable
{
    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, QueuingPathHandler.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, QueuingPathHandler.class);

    private final PathHandlerThread thread;

    private QueuingPathHandler(PathHandlerThread thread)
    {
        this.thread = thread;
    }

    public static QueuingPathHandler create(final IPathHandler handler, String threadName)
    {
        return create(handler, null, threadName);
    }

    public static QueuingPathHandler create(final IPathHandler handler, final IRecoverable recoverableOrNull,
            String threadName)
    {
        assert handler != null;
        assert threadName != null;

        final PathHandlerThread thread = new PathHandlerThread(handler, recoverableOrNull);
        final QueuingPathHandler lazyHandler = new QueuingPathHandler(thread);
        lazyHandler.recover();
        thread.setName(threadName);
        thread.start();
        return lazyHandler;
    }

    private static class PathHandlerThread extends Thread
    {
        private static final File DUMMY_FILE = new File(".");
        
        private final BlockingQueue<File> queue;

        private final IPathHandler handler;

        private final IRecoverable recoverableOrNull;

        public PathHandlerThread(IPathHandler handler, IRecoverable recoverableOrNull)
        {
            this.queue = new LinkedBlockingQueue<File>();
            this.handler = handler;
            this.recoverableOrNull = recoverableOrNull;
        }

        @Override
        public void run()
        {
            try
            {
                while (isInterrupted() == false)
                {
                    try
                    {
                        if (operationLog.isTraceEnabled())
                        {
                            operationLog.trace("Waiting for new element in queue.");
                        }
                        File path = queue.take(); // blocks if empty
                        if (path == DUMMY_FILE)
                        {
                            runRecover();
                        } else
                        {
                            if (operationLog.isTraceEnabled())
                            {
                                operationLog.trace("Processing path '" + path + "'");
                            }
                            handler.handle(path);
                        }
                    } catch (InterruptedException ex)
                    {
                        return;
                    }
                }
            } catch (Exception ex)
            {
                // Just log it but ensure that the thread won't die.
                notificationLog.error("An exception has occurred. (thread still running)", ex);
            }
        }

        synchronized void queue(File resource)
        {
            queue.add(resource);
        }

        private void runRecover()
        {
            if (recoverableOrNull != null)
            {
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Triggering recovery.");
                }
                recoverableOrNull.recover();
            }
        }

        void queueRecover()
        {
            queue(DUMMY_FILE);
        }

    }

    /**
     * Interrupts the processing thread.
     */
    public boolean terminate()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Terminating thread '" + thread.getName() + "'");
        }
        thread.interrupt();
        return true;
    }

    /**
     * Queues <var>path</var> processing and exits immediately.
     */
    public void handle(File path)
    {
        assert thread.isInterrupted() == false;

        if (operationLog.isTraceEnabled())
        {
            operationLog.trace("Queing path '" + path + "'");
        }
        thread.queue(path);
    }

    public void recover()
    {
        thread.queueRecover();
    }

}
