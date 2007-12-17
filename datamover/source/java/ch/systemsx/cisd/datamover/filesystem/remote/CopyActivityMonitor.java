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

package ch.systemsx.cisd.datamover.filesystem.remote;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ITerminable;
import ch.systemsx.cisd.common.utilities.StoreItem;
import ch.systemsx.cisd.datamover.filesystem.intf.FileStore;
import ch.systemsx.cisd.datamover.intf.ITimingParameters;

/**
 * A <code>CopyActivityMonitor</code> monitors write activity on a <var>destinationStore</var> and triggers an alarm
 * if there was a period of inactivity that exceeds a given inactivity period.
 * 
 * @author Bernd Rinn
 */
public class CopyActivityMonitor
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, CopyActivityMonitor.class);

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, CopyActivityMonitor.class);

    private final FileStore destinationStore;

    private final long checkIntervallMillis;

    private final long quickCheckActivityMillis;

    private final long inactivityPeriodMillis;

    private final String threadNamePrefix;

    private final ITerminable terminable;

    /**
     * We need to keep a reference to the timer since we want to be able to cancel it and because otherwise the timer
     * thread will terminate.
     */
    private Timer activityMonitoringTimer;

    /**
     * Unfortunately there is no way for a {@link TimerTask} to know whether it has been canceled. So we have to keep it
     * around in order to be able to terminate it directly.
     */
    private ActivityMonitoringTimerTask activityMonitoringTimerTask;

    /**
     * Creates a monitor. Uses 20% of <code>timingParameters.getCheckIntervalMillis()</code> for the quick check.
     * 
     * @param destinationStore The file store to monitor for write access.
     * @param copyProcess The {@link ITerminable} representing the copy process. This will get terminated if the copy
     *            process gets stuck.
     * @param timingParameters The {@link ITimingParameters} to get the check interval and the inactivity period from.
     */
    public CopyActivityMonitor(FileStore destinationStore, ITerminable copyProcess, ITimingParameters timingParameters)
    {
        this(destinationStore, copyProcess, timingParameters, (long) (timingParameters.getCheckIntervalMillis() * 0.2));
    }

    /**
     * Creates a monitor.
     * 
     * @param destinationStore The file store to monitor for write access.
     * @param copyProcess The {@link ITerminable} representing the copy process. This will get terminated if the copy
     *            process gets stuck.
     * @param timingParameters The {@link ITimingParameters} to get the check interval and the inactivity period from.
     * @param quickCheckActivityMillis The time to give the monitor for quickly check recently changed files.
     */
    public CopyActivityMonitor(FileStore destinationStore, ITerminable copyProcess, ITimingParameters timingParameters,
            long quickCheckActivityMillis)
    {
        assert destinationStore != null;
        assert copyProcess != null;
        assert timingParameters != null;

        this.destinationStore = destinationStore;
        this.terminable = copyProcess;
        this.checkIntervallMillis = timingParameters.getCheckIntervalMillis();
        this.inactivityPeriodMillis = timingParameters.getInactivityPeriodMillis();
        this.quickCheckActivityMillis = quickCheckActivityMillis;

        assert this.checkIntervallMillis > 0;

        final String currentThreadName = Thread.currentThread().getName();
        if ("main".equals(currentThreadName))
        {
            this.threadNamePrefix = "";
        } else
        {
            this.threadNamePrefix = currentThreadName + " - ";
        }
    }

    /**
     * Starts the activity monitoring.
     * 
     * @param itemToBeCopied The item that will be copied to the destination file store and whose write progress
     *            should be monitored.
     */
    public void start(StoreItem itemToBeCopied)
    {
        assert itemToBeCopied != null;

        activityMonitoringTimer = new Timer(threadNamePrefix + "Activity Monitor", true);
        activityMonitoringTimerTask = new ActivityMonitoringTimerTask(itemToBeCopied);
        activityMonitoringTimer.schedule(activityMonitoringTimerTask, 0, checkIntervallMillis);
    }

    /**
     * Stops the activity monitoring. The activity monitor must not be used after calling this method.
     */
    public void stop()
    {
        activityMonitoringTimer.cancel();
    }

    /**
     * A {@link TimerTask} that monitors writing activity on a directory.
     */
    private final class ActivityMonitoringTimerTask extends TimerTask
    {

        private static final String TERMINATION_LOG_TEMPLATE = "Terminating %s due to a lack of activity.";

        private static final String INACTIVITY_REPORT_TEMPLATE =
                "No progress on copying '%s' to '%s' for %f seconds - network connection might be stalled.";

        private final ExecutorService lastChangedExecutor =
                ConcurrencyUtilities.newNamedPool("Last Changed Explorer", 1, Integer.MAX_VALUE);

        private final StoreItem itemToBeCopied;
        
        private long monitoredItemLastChanged;

        private ActivityMonitoringTimerTask(StoreItem itemToBeCopied)
        {
            assert terminable != null;
            assert itemToBeCopied != null;
            assert destinationStore != null;

            // Ensure the alarm won't be run before the copier has a chance to get active.
            this.monitoredItemLastChanged = System.currentTimeMillis();
            this.itemToBeCopied = itemToBeCopied;
        }

        @Override
        public void run()
        {
            if (operationLog.isTraceEnabled())
            {
                operationLog.trace("Start activity monitoring run.");
            }

            try
            {
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace(String.format("Asking for last change time of '%s' inside '%s'.",
                            itemToBeCopied, destinationStore));
                }
                if (destinationStore.exists(itemToBeCopied) == false)
                {
                    operationLog.warn(String.format("File or directory '%s' inside '%s' does not (yet?) exist.",
                            itemToBeCopied, destinationStore));
                    return;
                }
                final long lastChangedAsFoundByPathChecker =
                        lastChanged(destinationStore, itemToBeCopied, monitoredItemLastChanged);
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace(String.format(
                            "Checker reported last changed time of '%s' inside '%s' to be %3$tF %3$tT.",
                            itemToBeCopied, destinationStore, lastChangedAsFoundByPathChecker));
                }
                // This catches the case where since the last check copying a files has been finished (and consequently
                // the "last changed" time has been set to that of the source file), but copying of the next file has
                // not yet been started.
                final long lastChanged = Math.max(lastChangedAsFoundByPathChecker, monitoredItemLastChanged);
                final long now = System.currentTimeMillis();
                if (lastChanged > now) // That can happen if the system clock of the data producer is screwed up.
                {
                    machineLog.error(String.format("Found \"last changed time\" in the future (%1$tF %1$tT), "
                            + "check system clock of data producer.", lastChanged));
                }
                monitoredItemLastChanged = lastChanged;
                final long noProgressSinceMillis = now - lastChanged;
                if (noProgressSinceMillis > inactivityPeriodMillis)
                {
                    machineLog.error(String.format(INACTIVITY_REPORT_TEMPLATE, itemToBeCopied, destinationStore,
                            noProgressSinceMillis / 1000.0f));
                    operationLog.warn(String.format(TERMINATION_LOG_TEMPLATE, terminable.getClass().getName()));
                    terminable.terminate();
                    stop();
                }
            } catch (CheckedExceptionTunnel ex)
            {
                if (ex.getCause() instanceof InterruptedException)
                {
                    operationLog.warn("Activity monitor got terminated.");
                } else
                {
                    throw ex;
                }
            } finally
            {
                if (operationLog.isTraceEnabled())
                {
                    operationLog.trace("Finished activity monitoring run.");
                }
            }
        }

        private long lastChanged(FileStore store, StoreItem item, long lastLastChanged)
        {
            // Give the system quickCheckActivityMillis to find recently changed files, otherwise perform full check
            final long stopWhenYoungerThan =
                    System.currentTimeMillis() - (inactivityPeriodMillis - 2 * quickCheckActivityMillis);
            final ISimpleLogger simpleMachineLog = new Log4jSimpleLogger(machineLog);
            final Future<Long> quickCheckLastChangedFuture =
                    lastChangedExecutor.submit(createCheckerCallable(store, item, stopWhenYoungerThan));
            final Long quickLastChanged =
                    ConcurrencyUtilities.tryGetResult(quickCheckLastChangedFuture, quickCheckActivityMillis,
                            simpleMachineLog, "Quick check for recent paths");
            if (quickLastChanged == null)
            {
                if (machineLog.isDebugEnabled())
                {
                    machineLog.debug("Performing full check for most recent path now.");
                }
                final Future<Long> lastChangedFuture =
                        lastChangedExecutor.submit(createCheckerCallable(store, item, 0L));
                final long timeoutMillis = Math.min(checkIntervallMillis * 3, inactivityPeriodMillis);
                final Long lastChanged =
                        ConcurrencyUtilities.tryGetResult(lastChangedFuture, timeoutMillis, simpleMachineLog,
                                "Check for recent paths");
                if (lastChanged == null)
                {
                    operationLog.error(String
                            .format("Could not determine \"last changed time\" of %s: time out.", item));
                    return lastLastChanged;
                }
                return lastChanged;
            } else
            {
                return quickLastChanged;
            }
        }

        private Callable<Long> createCheckerCallable(final FileStore store, final StoreItem item,
                final long stopWhenYoungerThan)
        {
            return new Callable<Long>()
                {
                    public Long call() throws Exception
                    {
                        if (machineLog.isTraceEnabled())
                        {
                            machineLog.trace("Starting quick check for recent paths on '" + item + "'.");
                        }
                        try
                        {
                            final long lastChanged = store.lastChanged(item, stopWhenYoungerThan);
                            if (machineLog.isTraceEnabled())
                            {
                                machineLog.trace(String.format(
                                        "Finishing quick check for recent paths on '%s', found to be %2$tF %2$tT.",
                                        item, lastChanged));
                            }
                            return lastChanged;
                        } catch (RuntimeException ex)
                        {
                            if (machineLog.isTraceEnabled())
                            {
                                final Throwable th = (ex instanceof CheckedExceptionTunnel) ? ex.getCause() : ex;
                                machineLog.trace("Failed on quick check for recent paths on '" + item + "'.", th);
                            }
                            throw ex;
                        }
                    }
                };
        }

    }

}
