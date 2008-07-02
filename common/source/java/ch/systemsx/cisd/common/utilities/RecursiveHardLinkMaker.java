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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.CallableExecutor;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;

/**
 * Utility to create a hard link of a file or copy recursively a directories structure, creating a
 * hard link for each file inside. Note that presence of <code>ln</code> executable is required,
 * which is not available under Windows.
 * 
 * @author Tomasz Pylak
 */
public final class RecursiveHardLinkMaker implements IPathImmutableCopier
{
    private static final String HARD_LINK_EXEC = "ln";
    
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RecursiveHardLinkMaker.class);

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RecursiveHardLinkMaker.class);

    private final String linkExecPath;

    private final RetryingOperationTimeout singleFileLinkTimeout;

    private RecursiveHardLinkMaker(final String linkExecPath,
            RetryingOperationTimeout singleFileLinkTimeoutOrNull)
    {
        this.linkExecPath = linkExecPath;
        this.singleFileLinkTimeout =
                singleFileLinkTimeoutOrNull != null ? singleFileLinkTimeoutOrNull
                        : createNoTimeout();
    }

    private RetryingOperationTimeout createNoTimeout()
    {
        return new RetryingOperationTimeout(0, 1, 0);
    }

    private static class RetryingOperationTimeout
    {
        private final long millisToWaitForCompletion;

        private final int maxRetryOnFailure;

        private final long millisToSleepOnFailure;

        private RetryingOperationTimeout(long millisToWaitForCompletion, int maxRetryOnFailure,
                long millisToSleepOnFailure)
        {
            this.millisToWaitForCompletion = millisToWaitForCompletion;
            this.maxRetryOnFailure = maxRetryOnFailure;
            this.millisToSleepOnFailure = millisToSleepOnFailure;
        }

        public long getMillisToWaitForCompletion()
        {
            return millisToWaitForCompletion;
        }

        public int getMaxRetryOnFailure()
        {
            return maxRetryOnFailure;
        }

        public long getMillisToSleepOnFailure()
        {
            return millisToSleepOnFailure;
        }
    }

    //
    // Factory methods
    //
    
    /**
     * Creates copier which won't retry an operation if it fails.
     * 
     * @param linkExecPath The path to the <code>ln</code> executable.
     */
    public static final IPathImmutableCopier create(final String linkExecPath)
    {
        return new RecursiveHardLinkMaker(linkExecPath, null);
    }

    /** 
     * Creates copier trying to find the path to the <code>ln</code> executable.
     * 
     * @return <code>null</code> if the <code>ln</code> executable was not found. 
     */
    public static final IPathImmutableCopier tryCreate()
    {
        return tryCreate(null);
    }

    /**
     * Creates copier which is able to retry the operation of creating each hard link of a file if
     * it does not complete after a specified timeout.
     * 
     * @param millisToWaitForCompletion The time to wait for the process creating one hard link to a
     *            file to complete in milli seconds. If the process is not finished after that time,
     *            it will be terminated.
     * @param maxRetryOnFailure The number of times we should try to create each hard link if copy
     *            operation fails.
     * @param millisToSleepOnFailure The number of milliseconds we should wait before re-executing
     *            the copy of a single file. Specify 0 to wait till the first operation completes.
     */
    public static final IPathImmutableCopier tryCreateRetrying(
            final long millisToWaitForCompletion, final int maxRetryOnFailure,
            final long millisToSleepOnFailure)
    {
        RetryingOperationTimeout timeout =
                new RetryingOperationTimeout(millisToWaitForCompletion, maxRetryOnFailure,
                        millisToSleepOnFailure);
        return tryCreate(timeout);
    }

    private static final IPathImmutableCopier tryCreate(
            RetryingOperationTimeout singleFileLinkTimeoutOrNull)
    {
        final File lnExec = OSUtilities.findExecutable(HARD_LINK_EXEC);
        if (lnExec == null)
        {
            return null;
        }
        return new RecursiveHardLinkMaker(lnExec.getAbsolutePath(), singleFileLinkTimeoutOrNull);
    }

    //
    // IPathImmutableCopier
    //
    
    /**
     * Copies <var>path</var> (file or directory) to <var>destinationDirectory</var> by
     * duplicating directory structure and creating hard link for each file.
     * <p>
     * <i>Note that <var>nameOrNull</var> cannot already exist in given <var>destinationDirectory</var>.</i>
     * </p>
     */
    public final File tryImmutableCopy(final File path, final File destinationDirectory,
            final String nameOrNull)
    {
        assert path != null : "Given path can not be null.";
        assert destinationDirectory != null && destinationDirectory.isDirectory() : "Given destination directory can not be null and must be a directory.";
        final String destName = nameOrNull == null ? path.getName() : nameOrNull;
        final File destFile = new File(destinationDirectory, destName);
        if (destFile.exists())
        {
            operationLog.error(String.format(
                    "File '%s' already exists in given destination directory '%s'", destName,
                    destinationDirectory));
            return null;
        }
        if (operationLog.isTraceEnabled())
        {
            operationLog.trace(String.format("Creating a hard link copy of '%s' in '%s'.", path
                    .getPath(), destinationDirectory.getPath()));
        }
        return tryMakeCopy(path, destinationDirectory, nameOrNull);
    }

    private final File tryMakeCopy(final File resource, final File destinationDirectory,
            final String nameOrNull)
    {
        if (resource.isFile())
        {
            return tryCreateHardLinkIn(resource, destinationDirectory, nameOrNull);
        } else
        {
            final String name = nameOrNull == null ? resource.getName() : nameOrNull;
            final File dir = tryCreateDir(name, destinationDirectory);
            if (dir == null)
            {
                return null;
            }
            final File[] files = resource.listFiles();
            if (files != null)
            {
                for (final File file : files)
                {
                    StopException.check();
                    if (tryMakeCopy(file, dir, null) == null)
                    {
                        return null;
                    }
                }
            } else
            // Shouldn't happen, but just to be sure.
            {
                if (resource.exists() == false)
                {
                    operationLog.error(String.format("Path '%s' vanished during processing.",
                            resource));
                } else
                {
                    operationLog.error(String.format(
                            "Found path '%s' that is neither a file nor a directory.", resource));
                }
            }
            return dir;
        }
    }

    private final static File tryCreateDir(final String name, final File destDir)
    {
        final File dir = new File(destDir, name);
        boolean ok = dir.mkdir();
        if (ok == false)
        {
            if (dir.isDirectory())
            {
                machineLog.error(String.format("Directory %s already exists in %s", name, destDir
                        .getAbsolutePath()));
                ok = true;
            } else
            {
                machineLog.error(String.format("Could not create directory %s inside %s.", name,
                        destDir.getAbsolutePath()));
                if (dir.isFile())
                {
                    machineLog.error("There is a file with a same name.");
                }
            }
        }
        return ok ? dir : null;
    }

    private final File tryCreateHardLinkIn(final File file, final File destDir,
            final String nameOrNull)
    {
        assert file.isFile() : String.format("Given file '%s' must be a file and is not.", file);
        final File destFile = new File(destDir, nameOrNull == null ? file.getName() : nameOrNull);
        final List<String> cmd = createLnCmdLine(file, destFile);
        final Callable<Boolean> processTask = new Callable<Boolean>()
            {
                //
                // Callable
                //

                public final Boolean call()
                {
                    boolean result =
                            ProcessExecutionHelper.runAndLog(cmd, operationLog, machineLog,
                                    singleFileLinkTimeout.getMillisToWaitForCompletion());
                    // NOTE: we have noticed that sometimes the result is false although the file
                    // have been copied
                    if (result == false && destFile.exists()
                            && checkIfIdenticalContent(file, destFile))
                    {
                        machineLog
                                .warn("Link creator reported failure, but the exact copy of the file '"
                                        + file.getPath()
                                        + "' seems to exist in '"
                                        + destFile.getPath() + "'. Error will be ignored.");
                        result = true;
                    }
                    return result;
                }
            };
        boolean ok =
                runRepeatableProcess(processTask, singleFileLinkTimeout.getMaxRetryOnFailure(),
                        singleFileLinkTimeout.getMillisToSleepOnFailure());
        return ok ? destFile : null;
    }

    private static boolean checkIfIdenticalContent(final File file1, final File file2)
    {
        StopException.check();
        try
        {
            return FileUtils.contentEquals(file1, file2);
        } catch (IOException e)
        {
            machineLog
                    .warn("It was not possible to compare the content of the file to check if creating links worked: "
                            + e.getMessage());
        }
        return false;
    }

    private final List<String> createLnCmdLine(final File srcFile, final File destFile)
    {
        final List<String> tokens = new ArrayList<String>();
        tokens.add(linkExecPath);
        tokens.add(srcFile.getAbsolutePath());
        tokens.add(destFile.getAbsolutePath());
        return tokens;
    }

    private static boolean runRepeatableProcess(final Callable<Boolean> task,
            final int maxRetryOnFailure, final long millisToSleepOnFailure)
    {
        return new CallableExecutor(maxRetryOnFailure, millisToSleepOnFailure)
                .executeCallable(task);
    }
}
