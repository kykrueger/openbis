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

package ch.systemsx.cisd.datamover.rsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A class that helps checking an <code>rsync</code> binary for its version.
 * 
 * @author Bernd Rinn
 */
final class RsyncVersionChecker
{

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, RsyncVersionChecker.class);

    /**
     * The class holding the version information about an <code>rsync</code> binary.
     * 
     * @author bernd
     */
    static class RsyncVersion
    {

        /**
         * The version string of the <code>rsync</code> binary.
         */
        private final String rsyncVersion;

        /**
         * The major version of the <code>rsync</code> binary.
         */
        private final int rsyncMajorVersion;

        /**
         * The minor version of the <code>rsync</code> binary.
         */
        private final int rsyncMinorVersion;

        /**
         * The patch version of the <code>rsync</code> binary.
         */
        private final int rsyncPatchVersion;

        private RsyncVersion(String rsyncVersion, int rsyncMajorVersion, int rsyncMinorVersion, int rsyncPatchVersion)
        {
            this.rsyncVersion = rsyncVersion;
            this.rsyncMajorVersion = rsyncMajorVersion;
            this.rsyncMinorVersion = rsyncMinorVersion;
            this.rsyncPatchVersion = rsyncPatchVersion;
        }

        /**
         * @return The version string of the <code>rsync</code> binary.
         */
        public String getVersionString()
        {
            return rsyncVersion;
        }

        /**
         * @return The major version of the <code>rsync</code> binary.
         */
        public int getMajorVersion()
        {
            return rsyncMajorVersion;
        }

        /**
         * @return The minor version of the <code>rsync</code> binary.
         */
        public int getMinorVersion()
        {
            return rsyncMinorVersion;
        }

        /**
         * @return The patch version of the <code>rsync</code> binary.
         */
        public int getPatchVersion()
        {
            return rsyncPatchVersion;
        }

    }

    /**
     * Looks up the version of the <var>rsyncExecutable</var>, and, on its way, checks whether its a good executable at
     * all.
     * 
     * @param rsyncExecutable The executable to find the version for.
     * @return The version information, or <code>null</code>, if the executable doesn't work.
     */
    static RsyncVersion getVersion(String rsyncExecutable)
    {
        assert rsyncExecutable != null;

        String rsyncVersion = getRsyncVersion(rsyncExecutable);
        if (rsyncVersion == null)
        {
            return null;
        }
        String[] rsyncVersionParts = rsyncVersion.split("\\.");
        if (rsyncVersionParts.length != 3)
        {
            return null;
        }
        final int rsyncMajorVersion = Integer.parseInt(rsyncVersionParts[0]);
        final int rsyncMinorVersion = Integer.parseInt(rsyncVersionParts[1]);
        final int rsyncPatchVersion = Integer.parseInt(rsyncVersionParts[2]);

        return new RsyncVersion(rsyncVersion, rsyncMajorVersion, rsyncMinorVersion, rsyncPatchVersion);
    }

    private static String getRsyncVersion(String rsyncExecutableToCheck)
    {
        BufferedReader reader = null;
        try
        {
            final Process process = new ProcessBuilder(rsyncExecutableToCheck, "--version").start();
            if (machineLog.isDebugEnabled())
            {
                machineLog.debug("Waiting for rsync process to finish.");
            }
            final long TIME_TO_WAIT_FOR_COMPLETION = 2 * 1000;
            final Timer watchDog = new Timer("Rsync Watch Dog");
            watchDog.schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        process.destroy();
                    }
                }, TIME_TO_WAIT_FOR_COMPLETION);
            final int exitValue = process.waitFor();
            watchDog.cancel();
            if (machineLog.isDebugEnabled())
            {
                machineLog.debug(String.format("Rsync process finished with exit value %s", exitValue));
            }
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            final String versionString = extractRsyncVersion(reader.readLine());
            return versionString;
        } catch (IOException e)
        {
            return null;
        } catch (InterruptedException e)
        {
            // This should never happen.
            throw new CheckedExceptionTunnel(e);
        } finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    // Ignore.
                }
            }
        }
    }

    private static String extractRsyncVersion(String rsyncVersionLine)
    {
        if (rsyncVersionLine.startsWith("rsync  version") == false)
        {
            return null;
        } else
        {
            final String[] versionStringParts = rsyncVersionLine.split("\\s+");
            if (versionStringParts.length < 3)
            {
                return null;
            }
            return versionStringParts[2];
        }
    }

}
