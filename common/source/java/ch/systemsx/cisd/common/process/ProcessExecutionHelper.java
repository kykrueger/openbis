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

package ch.systemsx.cisd.common.process;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.concurrent.ExecutionResult;
import ch.systemsx.cisd.common.concurrent.ExecutionStatus;
import ch.systemsx.cisd.common.concurrent.NamedCallable;
import ch.systemsx.cisd.common.concurrent.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.concurrent.StopException;

/**
 * Utility to execute a command from a command line and log all events.
 * 
 * @author Bernd Rinn
 */
public class ProcessExecutionHelper
{

    /**
     * Strategy on whether to read the process output or not.
     */
    public enum OutputReadingStrategy
    {
        /** Never read the output. */
        NEVER,

        /** Read the output if the process failed in some way. */
        ON_ERROR,

        /** Always read the output. */
        ALWAYS;
    }

    /**
     * The default strategy for when to read the process output.
     */
    public static final OutputReadingStrategy DEFAULT_OUTPUT_READING_STRATEGY =
            OutputReadingStrategy.ON_ERROR;

    /** Corresponds to no timeout at all for the process execution. */
    public static final long NO_TIMEOUT = ConcurrencyUtilities.NO_TIMEOUT;

    /** Corresponds to a short timeout of 1/10 s. */
    private static final long SHORT_TIMEOUT = 100;

    /** Corresponds to an immediate timeout for the process execution. */
    private static final long IMMEDIATE_TIMEOUT = ConcurrencyUtilities.IMMEDIATE_TIMEOUT;

    /** The executor service handling the threads that OS processes are spawned in. */
    private static final ExecutorService executor = new NamingThreadPoolExecutor("osproc", 10);

    /** The counter to draw the <var>processNumber</var> from. */
    private static final AtomicInteger processCounter = new AtomicInteger();

    private final Logger operationLog;

    private final Logger machineLog;

    /** Read-only! */
    private final List<String> commandLine;

    private final long millisToWaitForCompletion;

    private final OutputReadingStrategy outputReadingStrategy;

    /** The number used in thread names to distinguish the process. */
    private final int processNumber;

    // Use this reference to make sure the process is as dead as you can get it to be.
    private final AtomicReference<Process> processWrapper;

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code>
     *         otherwise.
     * @throws StopException If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog) throws StopException
    {
        return new ProcessExecutionHelper(cmd, NO_TIMEOUT, DEFAULT_OUTPUT_READING_STRATEGY,
                operationLog, machineLog).runAndLog();
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @return The process result.
     * @throws StopException If the thread got interrupted.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog) throws StopException
    {
        return new ProcessExecutionHelper(cmd, NO_TIMEOUT, DEFAULT_OUTPUT_READING_STRATEGY,
                operationLog, machineLog).run(true);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in
     *            milli-seconds. If the process is not finished after that time, it will be
     *            terminated by a watch dog.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code>
     *         otherwise.
     * @throws StopException If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion) throws StopException
    {
        return new ProcessExecutionHelper(cmd, millisToWaitForCompletion,
                DEFAULT_OUTPUT_READING_STRATEGY, operationLog, machineLog).runAndLog();
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @return The process result.
     * @throws StopException If the thread got interrupted.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion) throws StopException
    {
        return new ProcessExecutionHelper(cmd, millisToWaitForCompletion,
                DEFAULT_OUTPUT_READING_STRATEGY, operationLog, machineLog).run(true);
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in
     *            milli-seconds. If the process is not finished after that time, it will be
     *            terminated by a watch dog.
     * @param outputReadingStrategy The strategy for when to read the output (both
     *            <code>stdout</code> and <code>sterr</code>) of the process.
     * @return <code>true</code>, if the process did complete successfully, <code>false</code>
     *         otherwise.
     * @throws StopException If the thread got interrupted.
     */
    public static boolean runAndLog(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final OutputReadingStrategy outputReadingStrategy) throws StopException
    {
        return new ProcessExecutionHelper(cmd, millisToWaitForCompletion, outputReadingStrategy,
                operationLog, machineLog).runAndLog();
    }

    /**
     * Runs an Operating System process, specified by <var>cmd</var>.
     * 
     * @param cmd The command line to run.
     * @param operationLog The {@link Logger} to use for all message on the higher level.
     * @param machineLog The {@link Logger} to use for all message on the lower (machine) level.
     * @param millisToWaitForCompletion The time to wait for the process to complete in milli
     *            seconds. If the process is not finished after that time, it will be terminated by
     *            a watch dog.
     * @param outputReadingStrategy The strategy for when to read the output (both
     *            <code>stdout</code> and <code>sterr</code>) of the process.
     * @param stopOnInterrupt If <code>true</code>, throw a {@link StopException} if the thread
     *            gets interrupted while waiting on the future.
     * @return The process result.
     * @throws StopException If the thread got interrupted and <var>stopOnInterrupt</var> is
     *             <code>true</code>.
     */
    public static ProcessResult run(final List<String> cmd, final Logger operationLog,
            final Logger machineLog, final long millisToWaitForCompletion,
            final OutputReadingStrategy outputReadingStrategy, final boolean stopOnInterrupt)
            throws StopException
    {
        return new ProcessExecutionHelper(cmd, millisToWaitForCompletion, outputReadingStrategy,
                operationLog, machineLog).run(stopOnInterrupt);
    }

    /**
     * Returns the name of the command represented by <var>commandLine</var>.
     */
    static String getCommandName(final List<String> commandLine)
    {
        return new File(commandLine.get(0)).getName();
    }

    /**
     * Returns the command represented by <var>commandLine</var>.
     */
    private static String getCommand(final List<String> commandLine)
    {
        return StringUtils.join(commandLine, ' ');
    }

    /**
     * Returns the <code>stdout</code> (and <code>stderr</code> of the <var>process</var>.
     */
    private final static List<String> readProcessOutputLines(final Process process,
            final Logger machineLog, final boolean wait)
    {
        assert process != null;
        assert machineLog != null;

        final List<String> processOutput = new ArrayList<String>();
        final BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));
        try
        {
            while ((wait || reader.ready()))
            {
                final String line = reader.readLine();
                if (line == null)
                {
                    break;
                }
                processOutput.add(line);
            }
        } catch (final IOException e)
        {
            machineLog.warn(String.format("IOException when reading stdout/stderr, msg='%s'.", e
                    .getMessage()));
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
        return processOutput;
    }

    //
    // Implementation
    //

    /**
     * The class that performs the actual calling and interaction with the Operating System process.
     * Since we observed hangs of several process-related methods we call all of this in a separate
     * thread.
     */
    private class ProcessRunner implements NamedCallable<ProcessResult>
    {
        private Process launch() throws IOException
        {
            final ProcessBuilder processBuilder = new ProcessBuilder(commandLine);
            processBuilder.redirectErrorStream(true);
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug("Running command: " + getCommand(commandLine));
            }
            final Process process = processBuilder.start();
            return process;
        }

        public ProcessResult call() throws Exception
        {
            try
            {
                final Process process = launch();
                try
                {
                    processWrapper.set(process);
                    final int exitValue = process.waitFor();
                    if (processWrapper.getAndSet(null) == null)
                    {
                        // Value is irrelevant, the ProcessKiller got us.
                        return null;
                    }
                    List<String> processOutput = null;
                    if (OutputReadingStrategy.ALWAYS.equals(outputReadingStrategy)
                            || (OutputReadingStrategy.ON_ERROR.equals(outputReadingStrategy) && ProcessResult
                                    .isProcessOK(exitValue) == false))
                    {
                        processOutput = readProcessOutputLines(process, machineLog, true);
                    }
                    return new ProcessResult(commandLine, processNumber, ExecutionStatus.COMPLETE,
                            "", exitValue, processOutput, operationLog, machineLog);
                } finally
                {
                    IOUtils.closeQuietly(process.getErrorStream());
                    IOUtils.closeQuietly(process.getInputStream());
                    IOUtils.closeQuietly(process.getOutputStream());
                }
            } catch (final Exception ex)
            {
                machineLog.error("Exception when launching: " + ex.getMessage());
                throw ex;
            }
        }

        public String getCallableName()
        {
            return "run-P" + processNumber + "-{" + getCommandName(commandLine) + "}";
        }
    }

    /**
     * The class that performs the destruction of a process that has timed-out. We do this in a
     * separate thread because we have observed that, depending on the operating system and Java
     * version, processes can hang indefinitely on launching.
     */
    private class ProcessKiller implements NamedCallable<ProcessResult>
    {
        public ProcessResult call()
        {
            final Process process = processWrapper.getAndSet(null);
            if (process != null)
            {
                List<String> processOutput = null;
                if (OutputReadingStrategy.NEVER.equals(outputReadingStrategy) == false)
                {
                    processOutput = readProcessOutputLines(process, machineLog, false);
                }
                process.destroy(); // Note: this also closes the I/O streams.
                if (machineLog.isInfoEnabled())
                {
                    machineLog.info(String.format("Killed '" + getCommand(commandLine)) + "'.");
                }
                final int exitValue = getExitValue(process);
                return new ProcessResult(commandLine, processNumber, ExecutionStatus.TIMED_OUT, "",
                        exitValue, processOutput, operationLog, machineLog);
            } else
            {
                return null; // Value signals that the ProcessRunner got us.
            }
        }

        private int getExitValue(final Process process)
        {
            try
            {
                return process.exitValue();
            } catch (final IllegalThreadStateException ex)
            {
                return ProcessResult.NO_EXIT_VALUE;
            }
        }

        public String getCallableName()
        {
            return "kill-P" + processNumber + "-{" + getCommandName(commandLine) + "}";
        }
    }

    private ProcessExecutionHelper(final List<String> commandLine,
            final long millisToWaitForCompletion,
            final OutputReadingStrategy outputReadingStrategy, final Logger operationLog,
            final Logger machineLog)
    {
        this.processNumber = processCounter.getAndIncrement();
        this.operationLog = operationLog;
        this.machineLog = machineLog;
        // Backward compatibility.
        if (millisToWaitForCompletion == IMMEDIATE_TIMEOUT)
        {
            this.millisToWaitForCompletion = NO_TIMEOUT;
        } else
        {
            this.millisToWaitForCompletion = millisToWaitForCompletion;
        }
        this.outputReadingStrategy = outputReadingStrategy;
        this.commandLine = Collections.unmodifiableList(commandLine);
        this.processWrapper = new AtomicReference<Process>();
    }

    private ProcessResult run(final boolean stopOnInterrupt)
    {
        final Future<ProcessResult> runnerFuture = executor.submit(new ProcessRunner());
        ExecutionResult<ProcessResult> result =
                ConcurrencyUtilities.getResult(runnerFuture, millisToWaitForCompletion, false,
                        null, null);
        if (result.getStatus() == ExecutionStatus.TIMED_OUT)
        {
            final Future<ProcessResult> killerFuture = executor.submit(new ProcessKiller());
            result = ConcurrencyUtilities.getResult(killerFuture, SHORT_TIMEOUT);
            if (result.tryGetResult() == null)
            {
                result = ConcurrencyUtilities.getResult(runnerFuture, IMMEDIATE_TIMEOUT);
            }
        }
        if (result.tryGetResult() != null)
        {
            return result.tryGetResult();
        } else if (stopOnInterrupt && ExecutionStatus.INTERRUPTED.equals(result.getStatus()))
        {
            throw new StopException();
        } else
        {
            return new ProcessResult(commandLine, processNumber, result.getStatus(),
                    tryGetStartupFailureMessage(result.tryGetException()),
                    ProcessResult.NO_EXIT_VALUE, null, operationLog, machineLog);
        }
    }

    private static String tryGetStartupFailureMessage(final Throwable throwableOrNull)
    {
        if (throwableOrNull != null && throwableOrNull instanceof IOException)
        {
            return throwableOrNull.getMessage();
        } else
        {
            return null;
        }
    }

    private boolean runAndLog() throws StopException
    {
        final ProcessResult result = run(false);
        result.log();
        if (result.isInterruped())
        {
            throw new StopException();
        }
        return result.isOK();
    }

}
