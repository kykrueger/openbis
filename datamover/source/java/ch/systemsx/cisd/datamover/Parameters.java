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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.BuildAndEnvironmentInfo;

/**
 * The class to process the command line parameters.
 * 
 * @author Bernd Rinn
 */
public class Parameters implements ITimingParameters
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, Parameters.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY, Parameters.class);

    /**
     * The number of milliseconds in a minute.
     */
    public static final long MINUTE_MILLIS = 60 * 1000;

    private static final Option RSYNC_EXECUTABLE_OPT =
            new Option("e", "rsync-executable", true, "The rsync executable to use for copy operations.");

    private static final Option SSH_EXECUTABLE_OPT =
        new Option("s", "ssh-executable", true, "The ssh executable to use for creating tunnels.");

    private static final Option LOCAL_DATA_DIR_OPT =
            new Option("d", "local-datadir", true, "The local directory where the data producer writes to (mandatory).");

    private static final Option LOCAL_TEMP_DIR_OPT =
            new Option("t", "local-tempdir", true,
                    "The local directory to store the paths to be transfered temporarily (mandatory).");

    private static final Option REMOTE_DIR_OPT =
            new Option("r", "remotedir", true, "The remote directory to move the data to (mandatory).");

    private static final Option REMOTE_HOST_OPT =
            new Option("h", "remotehost", true, "The remote host to move the data to (only with rsync).");

    /**
     * The default interval to wait beween two checks for activity (in milliseconds).
     */
    private static final long CHECK_INTERVAL_MILLIS_DEFAULT = 2 * MINUTE_MILLIS;

    private static final Option CHECK_INTERVAL_OPT =
            new Option("c", "check-interval", true, String.format(
                    "The interval to wait between two checks (in seconds) [default: %d].",
                    CHECK_INTERVAL_MILLIS_DEFAULT / 1000));

    /**
     * The default period to wait before a file or directory is considered "quiet" (in milliseconds). This setting is
     * used when deciding whether a file or directory is ready to be moved to the remote side.
     */
    private static final long QUIET_PERIOD_MILLIS_DEFAULT = 5 * MINUTE_MILLIS;

    private static final Option QUIET_PERIOD_OPT =
            new Option("q", "quiet-period", true, String.format(
                    "The period that needs to pass before a path item is considered quiet (in seconds) [default: %d].",
                    QUIET_PERIOD_MILLIS_DEFAULT / 1000));

    /**
     * The default time intervall to wait after a failure has occurred before the operation is retried.
     */
    private static final long INTERVAL_TO_WAIT_AFTER_FAILURE_MILLIS = 2 * MINUTE_MILLIS;

    private static final Option INTERVAL_TO_WAIT_AFTER_FAILURE_OPT =
            new Option(
                    "f",
                    "failure-interval",
                    true,
                    String
                            .format(
                                    "The interval to wait after a failure has occurred before retrying the operation (in seconds) [default: %d].",
                                    INTERVAL_TO_WAIT_AFTER_FAILURE_MILLIS / 1000));

    /**
     * The default number of times a failed operation is retried (note that this means that the total number that the
     * operation is tried is one more).
     */
    private static final int MAXIMAL_NUMBER_OF_RETRIES_DEFAULT = 2;

    private static final Option MAXIMAL_NUMBER_OF_RETRIES_OPT =
            new Option("m", "max-retries", true, String.format(
                    "The number of retries of a failed operation before the data mover gives up on it. [default: %d].",
                    MAXIMAL_NUMBER_OF_RETRIES_DEFAULT));

    /**
     * The default period to wait before a file or directory is considered "inactive" or "stalled" (in milliseconds).
     * This setting is used when deciding whether a copy operation of a file or directory is "stalled".
     */
    private static final long INACTIVITY_PERIOD_MILLIS_DEFAULT = 10 * MINUTE_MILLIS;

    private static final Option INACTIVITY_PERIOD_MILLIS_OPT =
            new Option(
                    "i",
                    "inactivity-period",
                    true,
                    String
                            .format(
                                    "The period to wait before a file or directory is considered \"inactive\" or \"stalled\" (in seconds) [default: %d].",
                                    INACTIVITY_PERIOD_MILLIS_DEFAULT / 1000));

    private static final Option CLEANSING_REGEX_OPT =
            new Option("x", "cleansing-regex", true,
                    "The regular expression to use for cleansing before moving to remote.");

    private static final Option HELP_OPT = new Option("H", "help", false, "Prints out a description of the options.");

    private static final Option VERSION_OPT = new Option("V", "version", false, "Prints out the version information.");

    private static final Option TEST_NOTIFY_OPT =
            new Option("N", "test-notify", false, "Tests the notify log (i.e. that mail is sent out).");

    /**
     * The name of the <code>rsync</code> executable to use for copy operations.
     */
    private String rsyncExecutable = null;

    /**
     * The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    private String sshExecutable = null;
    
    /**
     * The interval to wait beween to checks for activity (in milliseconds).
     */
    private long checkIntervalMillis = CHECK_INTERVAL_MILLIS_DEFAULT;

    /**
     * The period to wait before a file or directory is considered "inactive" or "stalled" (in milliseconds). This
     * setting is used when deciding whether a copy operation of a file or directory is "stalled".
     */
    private long inactivityPeriodMillis = INACTIVITY_PERIOD_MILLIS_DEFAULT;

    /**
     * The period to wait before a file or directory is considered "quiet" (in milliseconds). This setting is used when
     * deciding whether a file or directory is ready to be moved to the remote side.
     */
    private long quietPeriodMillis = QUIET_PERIOD_MILLIS_DEFAULT;

    /**
     * The time intervall to wait after a failure has occurred before the operation is retried.
     */
    private long intervalToWaitAfterFailureMillis = INTERVAL_TO_WAIT_AFTER_FAILURE_MILLIS;

    /**
     * The number of times a failed operation is retried (note that this means that the total number that the operation
     * is tried is one more).
     */
    private int maximalNumberOfRetries = MAXIMAL_NUMBER_OF_RETRIES_DEFAULT;

    /**
     * The (local) directory to monitor for new files and directories to move to the remote side.
     */
    private File localDataDirectory = null;

    /**
     * The directory to move files and directories to that have been quiet in the local data directory for long enough
     * and thus are considered to be ready to be moved to remote. Note that this directory needs to be on the same file
     * system than {@link #localDataDirectory}.
     */
    private File localTemporaryDirectory = null;

    /**
     * The directory on the remote side to move the local files to once they are quiet.
     */
    private File remoteDataDirectory = null;

    /**
     * The remote host to copy the data to (only with rsync, will use ssh tunnel).
     */
    private String remoteHost = null;

    /**
     * The regular expression to use for cleansing on the local path before moving it to remote.
     */
    private Pattern cleansingRegex = null;

    /**
     * <code>true</code> if the command line provided all necessary options.
     */
    private boolean hasAllMandatoryOptions;

    /**
     * Class that wraps {@link Options} and keeps an order.
     */
    private static class OptionsWrapper
    {
        private final Options optionMap = new Options();

        private final List<Option> optionList = new ArrayList<Option>();

        private final List<Option> mandatoryOptionList = new ArrayList<Option>();

        void add(Option option, boolean mandatory)
        {
            optionMap.addOption(option);
            optionList.add(option);
            if (mandatory)
            {
                mandatoryOptionList.add(option);
            }
        }

        void addEmptyLine()
        {
            optionList.add(null);
        }

        Options getOptionMap()
        {
            return optionMap;
        }

        List<Option> getOptionList()
        {
            return optionList;
        }

        List<Option> getMandatoryOptionList()
        {
            return mandatoryOptionList;
        }

    }

    public Parameters(String[] args)
    {
        final OptionsWrapper options = getOptions();
        try
        {
            final CommandLine cmdLine = (new GnuParser()).parse(options.getOptionMap(), args);
            hasAllMandatoryOptions = hasAllMandatoryOptions(cmdLine, options.getMandatoryOptionList());
            if (cmdLine.hasOption(HELP_OPT.getOpt()))
            {
                printVersion();
                printOptions(options);
                System.exit(1);
            }
            if (cmdLine.hasOption(VERSION_OPT.getOpt()))
            {
                printVersion();
                System.exit(1);
            }
            if (cmdLine.hasOption(TEST_NOTIFY_OPT.getOpt()))
            {
                notificationLog.error("This is a test notification given due to specifying the --test-notify option.");
                System.exit(1);
            }
            updateParametersFromCommandLine(cmdLine);
        } catch (ParseException e)
        {
            printVersion();
            System.err.printf("Exception when parsing command line: [%s: %s]\n", e.getClass().getSimpleName(), e
                    .getMessage());
            printOptions(options);
            System.exit(1);
        }
    }

    private OptionsWrapper getOptions()
    {
        final OptionsWrapper options = new OptionsWrapper();
        options.add(LOCAL_DATA_DIR_OPT, true);
        options.add(LOCAL_TEMP_DIR_OPT, true);
        options.add(REMOTE_DIR_OPT, true);
        options.add(REMOTE_HOST_OPT, false);
        options.addEmptyLine();
        options.add(CHECK_INTERVAL_OPT, false);
        options.add(QUIET_PERIOD_OPT, false);
        options.add(INACTIVITY_PERIOD_MILLIS_OPT, false);
        options.add(INTERVAL_TO_WAIT_AFTER_FAILURE_OPT, false);
        options.add(MAXIMAL_NUMBER_OF_RETRIES_OPT, false);
        options.addEmptyLine();
        options.add(RSYNC_EXECUTABLE_OPT, false);
        options.add(SSH_EXECUTABLE_OPT, false);
        options.addEmptyLine();
        options.add(CLEANSING_REGEX_OPT, false);
        options.addEmptyLine();
        options.add(HELP_OPT, false);
        options.add(VERSION_OPT, false);
        options.add(TEST_NOTIFY_OPT, false);

        return options;
    }

    private boolean hasAllMandatoryOptions(CommandLine cmdLine, List<Option> mandatoryOptionList)
    {
        for (Option option : mandatoryOptionList)
        {
            if (cmdLine.hasOption(option.getOpt()) == false)
            {
                return false;
            }
        }
        return true;
    }

    private void printOptions(final OptionsWrapper options)
    {
        System.err.println("List of options:");
        for (Option option : options.getOptionList())
        {
            if (option == null)
            {
                System.err.println();
            } else
            {
                System.err.printf("  -%s [--%s]: %s\n", option.getOpt(), option.getLongOpt(), option.getDescription());
            }
        }
    }

    private void printVersion()
    {
        System.err.println("Data mover version " + BuildAndEnvironmentInfo.INSTANCE.getBuildNumber());
    }

    /**
     * Prints out the options.
     */
    public void printVersionAndOptions()
    {
        printVersion();
        printOptions(getOptions());
    }

    private void updateParametersFromCommandLine(final CommandLine cmdLine)
    {
        if (cmdLine.hasOption(RSYNC_EXECUTABLE_OPT.getOpt()))
        {
            rsyncExecutable = cmdLine.getOptionValue(RSYNC_EXECUTABLE_OPT.getOpt());
        }
        if (cmdLine.hasOption(SSH_EXECUTABLE_OPT.getOpt()))
        {
            sshExecutable = cmdLine.getOptionValue(SSH_EXECUTABLE_OPT.getOpt());
        }
        if (cmdLine.hasOption(LOCAL_DATA_DIR_OPT.getOpt()))
        {
            localDataDirectory = new File(cmdLine.getOptionValue(LOCAL_DATA_DIR_OPT.getOpt()));
        }
        if (cmdLine.hasOption(LOCAL_TEMP_DIR_OPT.getOpt()))
        {
            localTemporaryDirectory = new File(cmdLine.getOptionValue(LOCAL_TEMP_DIR_OPT.getOpt()));
        }
        if (cmdLine.hasOption(REMOTE_DIR_OPT.getOpt()))
        {
            remoteDataDirectory = new File(cmdLine.getOptionValue(REMOTE_DIR_OPT.getOpt()));
        }
        if (cmdLine.hasOption(REMOTE_HOST_OPT.getOpt()))
        {
            remoteHost = cmdLine.getOptionValue(REMOTE_HOST_OPT.getOpt());
        }
        if (cmdLine.hasOption(CHECK_INTERVAL_OPT.getOpt()))
        {
            checkIntervalMillis = 1000 * Long.parseLong(cmdLine.getOptionValue(CHECK_INTERVAL_OPT.getOpt()));
        }
        if (cmdLine.hasOption(QUIET_PERIOD_OPT.getOpt()))
        {
            quietPeriodMillis = 1000 * Long.parseLong(cmdLine.getOptionValue(QUIET_PERIOD_OPT.getOpt()));
        }
        if (cmdLine.hasOption(INTERVAL_TO_WAIT_AFTER_FAILURE_OPT.getOpt()))
        {
            intervalToWaitAfterFailureMillis =
                    1000 * Long.parseLong(cmdLine.getOptionValue(INTERVAL_TO_WAIT_AFTER_FAILURE_OPT.getOpt()));
        }
        if (cmdLine.hasOption(MAXIMAL_NUMBER_OF_RETRIES_OPT.getOpt()))
        {
            maximalNumberOfRetries = Integer.parseInt(cmdLine.getOptionValue(MAXIMAL_NUMBER_OF_RETRIES_OPT.getOpt()));
        }
        if (cmdLine.hasOption(INACTIVITY_PERIOD_MILLIS_OPT.getOpt()))
        {
            inactivityPeriodMillis =
                    1000 * Long.parseLong(cmdLine.getOptionValue(INACTIVITY_PERIOD_MILLIS_OPT.getOpt()));
        }
        if (cmdLine.hasOption(CLEANSING_REGEX_OPT.getOpt()))
        {
            final String cleansingRegexStr = cmdLine.getOptionValue(CLEANSING_REGEX_OPT.getOpt());
            cleansingRegex = Pattern.compile(cleansingRegexStr);
        }
    }

    /**
     * @return <code>true</code> if and only if all mandatory options have been provided to this
     *         <code>Parameter</code> object.
     */
    public boolean hasAllMandatoryOptions()
    {
        return hasAllMandatoryOptions;
    }

    /**
     * @return The name of the <code>rsync</code> executable to use for copy operations.
     */
    public String getRsyncExecutable()
    {
        return rsyncExecutable;
    }

    /**
     * @return The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    public String getSshExecutable()
    {
        return sshExecutable;
    }

    /**
     * @return The interval to wait beween to checks for activity (in milliseconds).
     */
    public long getCheckIntervalMillis()
    {
        return checkIntervalMillis;
    }

    /**
     * @return The period to wait before a file or directory is considered "inactive" (in milliseconds). This setting is
     *         used when deciding whether a copy operation of a file or directory is "stalled".
     */
    public long getInactivityPeriodMillis()
    {
        return inactivityPeriodMillis;
    }

    /**
     * @return The period to wait before a file or directory is considered "quiet" (in milliseconds). This setting is
     *         used when deciding whether a file or directory is ready to be moved to the remote side.
     */
    public long getQuietPeriodMillis()
    {
        return quietPeriodMillis;
    }

    /**
     * @return The time interval to wait after a failure has occurred before the operation is retried.
     */
    public long getIntervalToWaitAfterFailure()
    {
        return intervalToWaitAfterFailureMillis;
    }

    /**
     * @return The number of times a failed operation is retried (note that this means that the total number that the
     *         operation is tried is one more).
     */
    public int getMaximalNumberOfRetries()
    {
        return maximalNumberOfRetries;
    }

    /**
     * @return The (local) directory to monitor for new files and directories to move to the remote side.
     */
    public File getLocalDataDirectory()
    {
        return localDataDirectory;
    }

    /**
     * @return The directory to move files and directories to that have been quiet in the local data directory for long
     *         enough and thus are considered to be ready to be moved to remote. Note that this directory needs to be on
     *         the same file system than {@link #getLocalDataDirectory}.
     */
    public File getLocalTemporaryDirectory()
    {
        return localTemporaryDirectory;
    }

    /**
     * @return The directory on the remote side to move the local files to once they are quiet.
     */
    public File getRemoteDataDirectory()
    {
        return remoteDataDirectory;
    }

    /**
     * @return The remote host to copy the data to (only with rsync, will use an ssh tunnel).
     */
    public String getRemoteHost()
    {
        return remoteHost;
    }

    /**
     * @return The regular expression to use for cleansing on the local path before moving it to remote or
     *         <code>null</code>, if no regular expression for cleansing has been provided.
     */
    public Pattern getCleansingRegex()
    {
        return cleansingRegex;
    }

    /**
     * Logs the current parameters to the {@link LogCategory#OPERATION} log.
     */
    public void log()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Local data directory: '%s'.", getLocalDataDirectory().getAbsolutePath()));
            operationLog.info(String.format("Local temporary directory: '%s'.", getLocalTemporaryDirectory()
                    .getAbsolutePath()));
            operationLog.info(String.format("Remote directory: '%s'.", getRemoteDataDirectory().getAbsolutePath()));
            if (null != getRemoteHost())
            {
                operationLog.info(String.format("Remote host: '%s'.", getRemoteHost()));
            }
            operationLog.info(String.format("Check intervall: %d s.", getCheckIntervalMillis() / 1000));
            operationLog.info(String.format("Quiet period: %d s.", getQuietPeriodMillis() / 1000));
            operationLog.info(String.format("Inactivity (stall) period: %d s.", getInactivityPeriodMillis() / 1000));
            operationLog.info(String.format("Intervall to wait after failure: %d s.",
                    getIntervalToWaitAfterFailure() / 1000));
            operationLog.info(String.format("Maximum number of retries: %d.", getMaximalNumberOfRetries()));
            if (getCleansingRegex() != null)
            {
                operationLog.info(String.format("Regular expression used for cleansing before moving: '%s'",
                        getCleansingRegex().pattern()));
            }
        }
    }

}
