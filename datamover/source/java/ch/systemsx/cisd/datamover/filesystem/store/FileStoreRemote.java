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

package ch.systemsx.cisd.datamover.filesystem.store;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusWithResult;
import ch.systemsx.cisd.common.filesystem.BooleanStatus;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.StoreItem;
import ch.systemsx.cisd.common.filesystem.highwatermark.HighwaterMarkWatcher;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandBuilder;
import ch.systemsx.cisd.common.filesystem.ssh.SshCommandExecutor;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.datamover.filesystem.intf.AbstractFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IExtendedFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IStoreCopier;

/**
 * Allows to operate on files by executing all file system operations on a remote machine with ssh command.
 * 
 * @author Tomasz Pylak
 */
public class FileStoreRemote extends AbstractFileStore
{
    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            FileStoreRemote.class);

    private static final String NO_SUCH_FILE_OR_DIRECTORY_MSG = "No such file or directory";

    public static final long DEFAULT_REMOTE_OPERATION_TIMEOUT_MILLIS = 100 * 1000;

    private final long remoteOperationAndConnectionTimeoutMillis;

    // -- bash commands -------------

    // Creates bash command. The command returns the age of the most recently modified file as the
    // number of seconds (note that it's not in miliseconds!) form the epoch
    private static String mkFindYoungestModificationTimestampSecCommand(final String path,
            final String findExec)
    {
        return findExec + " " + path + " -printf \"%T@\\\\n\" | sort -n | head -1 ";
    }

    private static String mkLastchangedCommand(final String path,
            final long stopWhenFindYoungerMillis, boolean isRelative, final String lastchangedExec)
    {
        return lastchangedExec + " " + path + " " + (isRelative ? "r" : "")
                + Long.toString(stopWhenFindYoungerMillis / 1000);
    }

    // Creates bash command. The command deletes file or recursively deletes the whole directory.
    // Be careful!
    private static String mkDeleteFileCommand(final String pathString)
    {
        return "rm -fr " + pathString;
    }

    // Creates bash command. The command returns 0 if the command exists and is a file
    private static String mkCheckCommandExistsCommand(final String commandName)
    {
        return "type -p " + commandName;
    }

    // Creates bash command. The command returns 0 if the command exists and is a file
    private static String getVersionCommand(final String commandName)
    {
        return commandName + " --version";
    }

    // Creates bash command. The command returns the list of files inside the directory, sorted by
    // modification time, oldest first
    private static String mkListByOldestModifiedCommand(final String directoryPath)
    {
        // -A: show all entries except of . and ..
        // -1: show one entry per line, names only
        // -t -r: sort by modification time (the oldest first thanx to -r)
        return "ls -1 -A -t -r " + directoryPath;
    }

    // ---------------

    private final SshCommandExecutor sshCommandExecutor;

    private final HighwaterMarkWatcher highwaterMarkWatcher;

    private String remoteLastchangedExecutableOrNull;

    private String remoteFindExecutableOrNull;

    /**
     * @param remoteFindExecutableOrNull The executable to use for checking the last modification time of files on the remote outgoing host. It should
     *            be a GNU find supporting -printf option.
     * @param kind Description of the directory used in logs
     */
    public FileStoreRemote(final HostAwareFileWithHighwaterMark fileWithHighwaterMark,
            final String kind, final IFileSysOperationsFactory factory,
            final boolean skipAccessibilityTest, final String remoteFindExecutableOrNull,
            final String remoteLastchangedExecutableOrNull,
            final long remoteConnectionTimeoutMillis, final long remoteOperationTimeoutMillis)
    {
        this(fileWithHighwaterMark, kind, SshCommandExecutor
                .createSshCommandBuilder(findSSHOrDie(factory)), factory, skipAccessibilityTest,
                remoteFindExecutableOrNull, remoteLastchangedExecutableOrNull,
                remoteConnectionTimeoutMillis, remoteOperationTimeoutMillis);
    }

    @Private
    FileStoreRemote(final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark,
            final String kind, final ISshCommandBuilder sshCommandBuilder,
            final IFileSysOperationsFactory factory, final boolean skipAccessibilityTest,
            final String remoteFindExecutableOrNull, final String remoteLastchangedExecutableOrNull)
    {
        this(hostAwareFileWithHighwaterMark, kind, sshCommandBuilder, factory,
                skipAccessibilityTest, remoteFindExecutableOrNull,
                remoteLastchangedExecutableOrNull, DEFAULT_REMOTE_CONNECTION_TIMEOUT_MILLIS,
                DEFAULT_REMOTE_OPERATION_TIMEOUT_MILLIS);
    }

    @Private
    FileStoreRemote(final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark,
            final String kind, final ISshCommandBuilder sshCommandBuilder,
            final IFileSysOperationsFactory factory, final boolean skipAccessibilityTest,
            final String remoteFindExecutableOrNull,
            final String remoteLastchangedExecutableOrNull,
            final long remoteConnectionTimeoutMillis, final long remoteOperationTimeoutMillis)
    {
        super(hostAwareFileWithHighwaterMark, kind, factory, skipAccessibilityTest,
                remoteConnectionTimeoutMillis);
        assert hostAwareFileWithHighwaterMark.tryGetHost() != null : "Unspecified host";
        this.sshCommandExecutor =
                new SshCommandExecutor(sshCommandBuilder,
                        hostAwareFileWithHighwaterMark.tryGetHost());
        this.highwaterMarkWatcher =
                createHighwaterMarkWatcher(hostAwareFileWithHighwaterMark, sshCommandBuilder);
        this.remoteOperationAndConnectionTimeoutMillis =
                remoteOperationTimeoutMillis + remoteConnectionTimeoutMillis;
        setAndLogLastchangedExecutable(remoteLastchangedExecutableOrNull);
        setAndLogFindExecutable(remoteFindExecutableOrNull);
        if (remoteFindExecutableOrNull != null)
        {
            ensureSpecifiedFindExists(hostAwareFileWithHighwaterMark.tryGetHost(),
                    remoteFindExecutableOrNull);
        }
    }

    private void ensureSpecifiedFindExists(final String host, final String remoteFindExecutable)
    {
        if (checkFindExecutable(remoteFindExecutableOrNull) == null)
        {
            throw new EnvironmentFailureException("Cannot find specified find executable '"
                    + remoteFindExecutableOrNull + "' on the remote host '" + host + "'.");
        }
    }

    private static File findSSHOrDie(final IFileSysOperationsFactory factory)
    {
        final File ssh = factory.tryFindSshExecutable();
        if (ssh == null)
        {
            throw new EnvironmentFailureException("Cannot find ssh program");
        }
        return ssh;
    }

    private final static HighwaterMarkWatcher createHighwaterMarkWatcher(
            final HostAwareFileWithHighwaterMark hostAwareFileWithHighwaterMark,
            final ISshCommandBuilder sshCommandBuilder)
    {
        final IFreeSpaceProvider freeSpaceProvider = new RemoteFreeSpaceProvider(sshCommandBuilder);
        final HighwaterMarkWatcher highwaterMarkWatcher =
                new HighwaterMarkWatcher(hostAwareFileWithHighwaterMark.getHighwaterMark(),
                        freeSpaceProvider);
        highwaterMarkWatcher.setPath(hostAwareFileWithHighwaterMark);
        return highwaterMarkWatcher;
    }

    //
    // FileStore
    //

    @Override
    public final IExtendedFileStore tryAsExtended()
    {
        return null;
    }

    @Override
    public final Status delete(final StoreItem item)
    {
        final String pathString = toUnixPathString(item);
        final String cmd = mkDeleteFileCommand(pathString);
        final ProcessResult result =
                sshCommandExecutor.executeCommandRemotely(cmd, remoteConnectionTimeoutMillis);
        final String errMsg = getErrorMessageOrNull(result);
        if (errMsg == null)
        {
            return Status.OK;
        } else
        {
            return Status.createRetriableError(errMsg);
        }
    }

    @Override
    public final BooleanStatus exists(final StoreItem item)
    {
        final String pathString = toUnixPathString(item);
        return sshCommandExecutor.exists(pathString, remoteConnectionTimeoutMillis);
    }

    @Override
    public final IStoreCopier getCopier(final IFileStore destinationDirectory)
    {
        final boolean requiresDeletion = false;
        return constructStoreCopier(destinationDirectory, requiresDeletion);
    }

    @Override
    public final StatusWithResult<Long> lastChanged(final StoreItem item,
            final long stopWhenFindYounger)
    {
        if (remoteLastchangedExecutableOrNull != null)
        {
            return lastChangedExec(item, stopWhenFindYounger, false);
        } else
        {
            return lastChangedEmulatedGNUFindExec(item);
        }
    }

    private final StatusWithResult<Long> lastChangedExec(final StoreItem item,
            final long stopWhenFindYoungerMillis, boolean isRelative)
    {
        final String itemPath = toUnixPathString(item);

        final String cmd =
                mkLastchangedCommand(itemPath, stopWhenFindYoungerMillis, isRelative,
                        remoteLastchangedExecutableOrNull);
        final ProcessResult result =
                sshCommandExecutor.executeCommandRemotely(cmd,
                        remoteOperationAndConnectionTimeoutMillis);
        final String errMsg = getErrorMessageOrNullForLastchanged(result);
        if (errMsg == null)
        {
            final String resultLine = result.getOutput().get(0);
            final long lastChanged = tryParseLastChangedMillis(resultLine);
            return StatusWithResult.<Long> create(lastChanged);
        } else
        {
            return createLastChangeError(item, errMsg);
        }
    }

    private final StatusWithResult<Long> lastChangedEmulatedGNUFindExec(final StoreItem item)
    {
        final String itemPath = toUnixPathString(item);

        final String findExec = getRemoteFindExecutableOrDie();
        final String cmd = mkFindYoungestModificationTimestampSecCommand(itemPath, findExec);
        final ProcessResult result =
                sshCommandExecutor.executeCommandRemotely(cmd,
                        remoteOperationAndConnectionTimeoutMillis);
        final String errMsg = getErrorMessageOrNullForFind(result);
        if (errMsg == null)
        {
            final String resultLine = result.getOutput().get(0);
            final long lastChanged = tryParseLastChangedMillis(resultLine);
            return StatusWithResult.<Long> create(lastChanged);
        } else
        {
            return createLastChangeError(item, errMsg);
        }
    }

    private String toUnixPathString(final StoreItem itemOrNull)
    {
        if (itemOrNull == null)
        {
            return getPath().replace('\\', '/');
        } else
        {
            return StoreItem.asFile(getPath(), itemOrNull).getPath().replace('\\', '/');
        }
    }

    private static StatusWithResult<Long> createLastChangeError(StoreItem item, String errorMsg)
    {
        return StatusWithResult.<Long> createErrorWithResult("Cannot obtain last change time of the item "
                + item + ". Reason: " + errorMsg);
    }

    private String getRemoteFindExecutableOrDie()
    {
        if (remoteFindExecutableOrNull == null)
        {
            checkAvailableAndSetFindUtil();
        }
        if (remoteFindExecutableOrNull == null)
        {
            throw new EnvironmentFailureException(createNoFindUtilMessage());
        } else
        {
            return remoteFindExecutableOrNull;
        }
    }

    @Override
    public final StatusWithResult<Long> lastChangedRelative(final StoreItem item,
            final long stopWhenFindYoungerRelative)
    {
        if (remoteLastchangedExecutableOrNull != null)
        {
            return lastChangedExec(item, stopWhenFindYoungerRelative, true);
        } else
        {
            return lastChangedEmulatedGNUFindExec(item);
        }
    }

    // outgoing and self-test
    @Override
    public final BooleanStatus checkDirectoryFullyAccessible(final long timeOutMillis)
    {
        final BooleanStatus status =
                skipAccessibilityTest ? BooleanStatus.createTrue() : sshCommandExecutor
                        .checkDirectoryAccessible(toUnixPathString(null), timeOutMillis);
        if (status.isSuccess())
        {
            if (this.remoteLastchangedExecutableOrNull != null
                    || ((skipAccessibilityTest == false) && checkAvailableAndSetLastchangedUtil()))
            {
                return BooleanStatus.createTrue();
            } else if (this.remoteFindExecutableOrNull != null
                    || ((skipAccessibilityTest == false) && checkAvailableAndSetFindUtil()))
            {
                return BooleanStatus.createTrue();
            } else
            {
                return BooleanStatus
                        .createError(skipAccessibilityTest ? createSkipAccessibilityNoFindUtilMessage()
                                : createNoFindUtilMessage());
            }
        } else
        {
            return status;
        }
    }

    @Override
    public boolean isRemote()
    {
        return true;
    }

    private String createNoFindUtilMessage()
    {
        return "Neither the lastchanged utility nor the GNU find utility can be found on the remote machine '"
                + getHost() + "'";
    }

    private String createSkipAccessibilityNoFindUtilMessage()
    {
        return "The inital accessibility test is configured to be skipped on the remote machine '"
                + getHost()
                + "', but neither the path to the lastchanged utility nor to the GNU find utility "
                + "is provided in the configuration.";
    }

    // tries to execute different find versions with appropriate options on the remote host. If
    // successful sets the executable script name and returns null. Otherwise returns error message.
    private boolean checkAvailableAndSetFindUtil()
    {
        final String[] findExecutables =
        { "gfind", "find" };
        for (final String findExec : findExecutables)
        {
            final String findExecutableOrNull = checkFindExecutable(findExec);
            if (findExecutableOrNull != null)
            {
                setAndLogFindExecutable(findExecutableOrNull);
                return true;
            }
        }
        return false;
    }

    private String checkFindExecutable(final String findExec)
    {
        final String cmd = mkCheckCommandExistsCommand(findExec);
        final ProcessResult result =
                sshCommandExecutor
                        .executeCommandRemotely(cmd, remoteConnectionTimeoutMillis, false);
        if (machineLog.isDebugEnabled())
        {
            result.log();
        }
        if (result.isOK() && hasAnyOutput(result))
        {
            final String findExecutable = result.getOutput().get(0);
            final String verCmd = getVersionCommand(findExec);
            final ProcessResult verResult =
                    sshCommandExecutor.executeCommandRemotely(verCmd,
                            remoteConnectionTimeoutMillis,
                            false);
            if (machineLog.isDebugEnabled())
            {
                verResult.log();
            }
            if (verResult.isOK() && isGNUFind(verResult.getOutput()))
            {
                return findExecutable;
            }
        }
        return null;
    }

    private static boolean hasAnyOutput(ProcessResult result)
    {
        return result.getOutput().size() > 0;
    }

    private boolean isGNUFind(List<String> output)
    {
        return output.size() > 0 && output.get(0).contains("GNU") && output.get(0).contains("find");
    }

    private void setAndLogFindExecutable(final String findExecutableOrNull)
    {
        if (findExecutableOrNull != null)
        {
            this.remoteFindExecutableOrNull = findExecutableOrNull;
            machineLog.info("Using GNU find executable '" + findExecutableOrNull + "' on store '"
                    + this + "'.");
        }
    }

    private boolean checkAvailableAndSetLastchangedUtil()
    {
        final String lastchangedExecutableOrNull = checkExecutable("lastchanged");
        if (lastchangedExecutableOrNull != null)
        {
            setAndLogLastchangedExecutable(lastchangedExecutableOrNull);
        }
        return (lastchangedExecutableOrNull != null);
    }

    private String checkExecutable(final String exec)
    {
        final String cmd = mkCheckCommandExistsCommand(exec);
        final ProcessResult result =
                sshCommandExecutor
                        .executeCommandRemotely(cmd, remoteConnectionTimeoutMillis, false);
        if (machineLog.isDebugEnabled())
        {
            result.log();
        }
        if (result.isOK() && hasAnyOutput(result))
        {
            return result.getOutput().get(0);
        } else
        {
            return null;
        }
    }

    private void setAndLogLastchangedExecutable(final String lastchangedExecutableOrNull)
    {
        if (lastchangedExecutableOrNull != null)
        {
            this.remoteLastchangedExecutableOrNull = lastchangedExecutableOrNull;
            machineLog.info("Using 'lastchanged' executable '" + lastchangedExecutableOrNull
                    + "' on store '" + this + "'.");
        }
    }

    @Override
    public final String getLocationDescription(final StoreItem item)
    {
        return getHost() + ":" + getChildFile(item).getPath();
    }

    @Override
    public StoreItem asStoreItem(String locationDescription)
    {
        final int beginIndex = locationDescription.indexOf(':');
        final String path =
                (beginIndex < 0) ? locationDescription : locationDescription
                        .substring(beginIndex + 1);
        return new StoreItem(FilenameUtils.getName(path));
    }

    private String getHost()
    {
        final String host = tryGetHost();
        assert host != null : "host cannot be null";
        return host;
    }

    @Override
    public final StoreItem[] tryListSortByLastModified(final ISimpleLogger loggerOrNull)
    {
        final String simpleCmd = mkListByOldestModifiedCommand(toUnixPathString(null));
        final ProcessResult result =
                sshCommandExecutor.executeCommandRemotely(simpleCmd,
                        remoteOperationAndConnectionTimeoutMillis);
        if (result.isOK())
        {
            return asStoreItems(result.getOutput());
        } else
        {
            return null;
        }
    }

    private static StoreItem[] asStoreItems(final List<String> lines)
    {
        final StoreItem[] items = new StoreItem[lines.size()];
        int i = 0;
        for (final String line : lines)
        {
            items[i] = new StoreItem(line);
            i++;
        }
        return items;
    }

    @Override
    public final HighwaterMarkWatcher getHighwaterMarkWatcher()
    {
        return highwaterMarkWatcher;
    }

    // -----------------------

    private static String getErrorMessageOrNull(final ProcessResult result)
    {
        if (result.isOK() == false)
        {
            return String.format("Command '%s' failed with error exitval=%d, output=[%s]",
                    result.getCommandLine(), result.getExitValue(),
                    StringUtils.join(result.getOutput(), '\n'));
        } else
        {
            return null;
        }
    }

    private static String getErrorMessageOrNullForLastchanged(final ProcessResult result)
    {
        if (result.isOK() == false)
        {
            return String.format("Command '%s' failed with error exitval=%d, output=[%s]",
                    result.getCommandLine(), result.getExitValue(),
                    StringUtils.join(result.getOutput(), '\n'));
        } else if (hasAnyOutput(result) == false)
        {
            return String.format("Command '%s' ended succesfully, but without any output.",
                    result.getCommandLine());
        } else
        {
            return null;
        }
    }

    // Implementation note: As the find command used via ssh is a pipe, the exit value is the exit
    // value of the last command of the pipe. This is a "head" command that usually doesn't fail.
    // Thus we need other means to find out whether the command was successful than the exit value.
    private static String getErrorMessageOrNullForFind(final ProcessResult result)
    {
        if (result.isOK() == false)
        {
            return String.format("Command '%s' failed with error exitval=%d, output=[%s]",
                    result.getCommandLine(), result.getExitValue(),
                    StringUtils.join(result.getOutput(), '\n'));
        } else if (result.getOutput().size() != 1
                || tryParseLastChangedMillis(result.getOutput().get(0)) == null)
        {
            if (result.getOutput().size() > 0
                    && result.getOutput().get(0).indexOf(NO_SUCH_FILE_OR_DIRECTORY_MSG) > -1)
            {
                return NO_SUCH_FILE_OR_DIRECTORY_MSG;
            } else
            {
                return String.format("Command '%s' failed with output=[%s]",
                        result.getCommandLine(), StringUtils.join(result.getOutput(), '\n'));
            }
        } else
        {
            return null;
        }
    }

    private static Long tryParseLastChangedMillis(String numberStr)
    {
        try
        {
            return (long) (Double.parseDouble(numberStr) * 1000);
        } catch (final NumberFormatException e)
        {
            return null;
        }
    }

    //
    // FileStore
    //

    @Override
    public final String toString()
    {
        final String pathStr = toUnixPathString(null);
        if (tryGetRsyncModuleName() != null)
        {
            return "[remote fs] " + getHost() + ":" + tryGetRsyncModuleName() + ":" + pathStr;
        } else
        {
            return "[remote fs] " + getHost() + ":" + pathStr;
        }
    }
}
