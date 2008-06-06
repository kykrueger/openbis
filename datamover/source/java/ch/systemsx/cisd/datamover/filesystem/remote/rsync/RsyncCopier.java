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

package ch.systemsx.cisd.datamover.filesystem.remote.rsync;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.filesystem.remote.rsync.RsyncVersionChecker.RsyncVersion;

/**
 * A class that encapsulates the <code>rsync</code> call for doing an archive copy.
 * 
 * @author Bernd Rinn
 */
public final class RsyncCopier implements IPathCopier
{

    /**
     * The maximal period to wait for the <code>rsync</code> list process to finish before killing
     * it (<i>30s</i>).
     */
    private static final long MILLIS_TO_WAIT_BEFORE_TIMEOUT = 30 * DateUtils.MILLIS_PER_SECOND;

    /**
     * The {@link Status} returned if the process was terminated by {@link Process#destroy()}.
     */
    protected static final Status TERMINATED_STATUS =
            new Status(StatusFlag.RETRIABLE_ERROR, "Process was terminated.");

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RsyncCopier.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RsyncCopier.class);

    private static final Status INTERRUPTED_STATUS =
            new Status(StatusFlag.RETRIABLE_ERROR, "Process was interrupted.");

    private final String rsyncExecutable;

    private final RsyncVersion rsyncVersion;

    private final String sshExecutable;

    private final List<String> additionalCmdLineFlags;

    private final boolean overwrite;

    /**
     * If <code>true</code>, the file system of the destination directory requires that already
     * existing files and directories on the remote side are removed before the copy process is
     * started.
     */
    private final boolean destinationDirectoryRequiresDeletionBeforeCreation;

    /**
     * Informs that a process has been started (and, in this case, is finished or not) or not.
     * <p>
     * Gets initialized to <code>false</code> just before {@link ProcessExecutionHelper} gets
     * called.
     * </p>
     */
    private AtomicBoolean terminated = new AtomicBoolean(false);

    /**
     * Constructs an <code>RsyncCopier</code>.
     * 
     * @param rsyncExecutable The <code>rsync</code> binary to call for copying.
     * @param sshExecutableOrNull The <code>ssh</code> binary to use for creating tunnels, or
     *            <code>null</code>, if no <code>ssh</code> is available on this machine.
     * @param destinationDirectoryRequiresDeletionBeforeCreation If <code>true</code>, already
     *            existing files and directories on the remote side will be deleted before starting
     *            the copy process (no overwriting of paths).
     */
    public RsyncCopier(final File rsyncExecutable, final File sshExecutableOrNull,
            final boolean destinationDirectoryRequiresDeletionBeforeCreation,
            final boolean overwrite, final String... cmdLineFlags)
    {
        assert rsyncExecutable != null && rsyncExecutable.exists();
        assert sshExecutableOrNull == null || rsyncExecutable.exists();

        this.rsyncExecutable = rsyncExecutable.getAbsolutePath();
        this.rsyncVersion = RsyncVersionChecker.getVersion(rsyncExecutable.getAbsolutePath());
        this.sshExecutable = (sshExecutableOrNull != null) ? sshExecutableOrNull.getPath() : null;
        this.destinationDirectoryRequiresDeletionBeforeCreation =
                destinationDirectoryRequiresDeletionBeforeCreation;
        this.overwrite = overwrite;
        if (cmdLineFlags.length > 0)
        {
            this.additionalCmdLineFlags = Arrays.asList(cmdLineFlags);
        } else
        {
            this.additionalCmdLineFlags = null;
        }
    }

    private boolean rsyncSupportsAppend()
    {
        assert rsyncVersion != null;

        return rsyncVersion.isNewerOrEqual(2, 6, 7);
    }

    private boolean isOverwriteMode()
    {
        return overwrite || destinationDirectoryRequiresDeletionBeforeCreation
                || (rsyncSupportsAppend() == false);
    }

    //
    // IPathCopier
    //

    public final Status copy(final File sourcePath, final File destinationDirectory)
    {
        return copy(sourcePath, null, destinationDirectory, null);
    }

    public final Status copyFromRemote(final File sourcePath, final String sourceHost,
            final File destinationDirectory)
    {
        return copy(sourcePath, sourceHost, destinationDirectory, null);
    }

    public final Status copyToRemote(final File sourcePath, final File destinationDirectory,
            final String destinationHost)
    {
        return copy(sourcePath, null, destinationDirectory, destinationHost);
    }

    /**
     * Terminates the copy process by calling {@link Process#destroy()}, if a copy process is
     * currently running. If no copy process is running, the method will return immediately.
     */
    public final boolean terminate()
    {
        // TODO 2008-06-02, Christian Ribeaud: Reimplement this once it is possible to run the
        // killer process NOW in ProcessExecutionHelper.
        // final Process copyProcess = copyProcessReference.get();
        // if (copyProcess != null)
        // {
        // copyProcess.destroy();
        // return true;
        // } else
        // {
        // return false;
        // }
        return terminated.get();
    }

    /**
     * Checks whether the <code>rsync</code> can be executed and has a version >= 2.6.0.
     * 
     * @throws ConfigurationFailureException If the check fails.
     */
    public final void check()
    {
        if (machineLog.isDebugEnabled())
        {
            machineLog.debug(String.format("Testing rsync executable '%s'", rsyncExecutable));
        }
        if (rsyncVersion == null)
        {
            if (OSUtilities.executableExists(rsyncExecutable))
            {
                throw new ConfigurationFailureException(String.format(
                        "Rsync executable '%s' is invalid.", rsyncExecutable));
            } else
            {
                throw new ConfigurationFailureException(String.format(
                        "Rsync executable '%s' does not exist.", rsyncExecutable));
            }
        }
        if (rsyncVersion.isNewerOrEqual(2, 6, 0) == false)
        {
            throw new ConfigurationFailureException(String.format(
                    "Rsync executable '%s' is too old (required: 2.6.0, found: %s)",
                    rsyncExecutable, rsyncVersion.getVersionString()));
        }
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format("Using rsync executable '%s', version %s, mode: %s",
                    rsyncExecutable, rsyncVersion.getVersionString(),
                    (isOverwriteMode() ? "overwrite" : "append")));
        }
        if (rsyncVersion.isRsyncPreReleaseVersion())
        {
            machineLog.warn(String
                    .format(
                            "The rsync executable '%s' is a pre-release version. It is not recommended "
                                    + "to use such a version in a production environment.",
                            rsyncExecutable));
        }
    }

    private final Status copy(final File sourcePath, final String sourceHostOrNull,
            final File destinationDirectory, final String destinationHostOrNull)
    {
        assert sourcePath != null;
        assert sourceHostOrNull != null || sourcePath.exists() : logNonExistent(sourcePath);
        assert destinationDirectory != null;
        assert destinationHostOrNull != null || destinationDirectory.isDirectory() : logNonExistent(sourcePath);
        // Only one side can be remote
        assert sourceHostOrNull == null || destinationHostOrNull == null;
        final List<String> commandLine =
                createCommandLine(sourcePath, sourceHostOrNull, destinationDirectory,
                        destinationHostOrNull);
        terminated.set(false);
        final ProcessResult processResult =
                ProcessExecutionHelper.run(commandLine, operationLog, machineLog,
                        MILLIS_TO_WAIT_BEFORE_TIMEOUT);
        terminated.set(true);
        processResult.log();
        return createStatus(processResult);
    }

    private final String logNonExistent(final File path)
    {
        if (path == null)
        {
            return "null";
        } else
        {
            return "path '" + path.getAbsolutePath() + "' does not exist";
        }
    }

    private final List<String> createCommandLine(final File sourcePath, final String sourceHost,
            final File destinationDirectory, final String destinationHost)
    {
        assert sourcePath != null && (sourceHost != null || sourcePath.exists());
        assert destinationDirectory != null
                && (destinationHost != null || destinationDirectory.isDirectory());
        assert (destinationHost != null && sshExecutable != null) || (destinationHost == null);
        assert (sourceHost != null && sshExecutable != null) || (sourceHost == null);

        final List<String> standardParameters = Arrays.asList("--archive", "--delete", "--inplace");
        final List<String> commandLineList = new ArrayList<String>();
        commandLineList.add(rsyncExecutable);
        commandLineList.addAll(standardParameters);
        if (isOverwriteMode())
        {
            commandLineList.add("--whole-file");
        } else
        {
            commandLineList.add("--append");
        }
        if (sshExecutable != null && destinationHost != null)
        {
            commandLineList.add("--rsh");
            commandLineList.add(getSshExecutableArgument(sshExecutable));
        }
        if (additionalCmdLineFlags != null)
        {
            commandLineList.addAll(additionalCmdLineFlags);
        }
        commandLineList.add(buildPath(sourceHost, sourcePath, false));
        commandLineList.add(buildPath(destinationHost, destinationDirectory, true));

        return commandLineList;
    }

    private final static String getSshExecutableArgument(final String sshExecutable)
    {
        if (OSUtilities.isWindows())
        {
            return toUnix(sshExecutable) + " -oBatchMode=yes";
        } else
        {
            return sshExecutable + " -oBatchMode=yes";
        }
    }

    private static String buildPath(final String host, final File resource,
            final boolean isDirectory)
    {
        if (null == host)
        {
            String path = resource.getAbsolutePath();
            if (isDirectory)
            {
                path += File.separator;
            }
            return toUnix(path);
        } else
        {
            String path = resource.getPath();
            if (isDirectory)
            {
                path += File.separator;
            }
            // We must not use the absolute path here because that is the business of the
            // destination host.
            return host + ":" + toUnix(path);
        }
    }

    /**
     * Since <code>rsync</code> under Windows is from Cygwin, we need to translate the path into a
     * Cygwin path.
     */
    private static String toUnix(final String path)
    {
        if (OSUtilities.isWindows() == false)
        {
            return path;
        }
        String resultPath = path.replace('\\', '/');
        // Get rid of drive letters.
        if (resultPath.charAt(1) == ':')
        {
            resultPath = "/cygdrive/" + resultPath.charAt(0) + resultPath.substring(2);
        }
        return resultPath;
    }

    private final static Status createStatus(final ProcessResult processResult)
    {
        if (processResult.isTerminated())
        {
            return TERMINATED_STATUS;
        }
        if (processResult.isInterruped())
        {
            return INTERRUPTED_STATUS;
        }
        int exitValue = processResult.getExitValue();
        final StatusFlag flag = RsyncExitValueTranslator.getStatus(exitValue);
        if (StatusFlag.OK.equals(flag))
        {
            return Status.OK;
        }
        return new Status(flag, RsyncExitValueTranslator.getMessage(exitValue));
    }
}
