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
import java.util.regex.PatternSyntaxException;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.*;

/**
 * Test cases for the {@link Parameters} class.
 * 
 * @author bernd
 */
public class ParametersTest
{

    @Test()
    public void testSetRsyncExecutableLong() throws Exception
    {
        final String RSYNC_EXEC = "/usr/local/bin/rsync";
        final Parameters parameters = new Parameters(new String[]
            { "--rsync-executable", RSYNC_EXEC });
        assertEquals(RSYNC_EXEC, parameters.getRsyncExecutable());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test()
    public void testSetRsyncExecutableShort() throws Exception
    {
        final String RSYNC_EXEC = "/usr/local/bin/rsync";
        final Parameters parameters = new Parameters(new String[]
            { "-e", RSYNC_EXEC });
        assertEquals(RSYNC_EXEC, parameters.getRsyncExecutable());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test()
    public void testSetSshExecutableLong() throws Exception
    {
        final String SSH_EXEC = "/usr/local/bin/ssh";
        final Parameters parameters = new Parameters(new String[]
            { "--ssh-executable", SSH_EXEC });
        assertEquals(SSH_EXEC, parameters.getSshExecutable());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test()
    public void testSetSshExecutableShort() throws Exception
    {
        final String SSH_EXEC = "/usr/local/bin/ssh";
        final Parameters parameters = new Parameters(new String[]
            { "-s", SSH_EXEC });
        assertEquals(SSH_EXEC, parameters.getSshExecutable());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test()
    public void testSetCleansingRegexLong() throws Exception
    {
        final String CLEANSING_REGEX = "[0-9]+";
        final Parameters parameters = new Parameters(new String[]
            { "--cleansing-regex", CLEANSING_REGEX });
        assertEquals(CLEANSING_REGEX, parameters.getCleansingRegex().pattern());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test()
    public void testSetCleansingRegexShort() throws Exception
    {
        final String CLEANSING_REGEX = "[0-9]+";
        final Parameters parameters = new Parameters(new String[]
            { "-x", CLEANSING_REGEX });
        assertEquals(CLEANSING_REGEX, parameters.getCleansingRegex().pattern());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test(expectedExceptions =
        { PatternSyntaxException.class })
    public void testSetInvalidCleansingRegex() throws Exception
    {
        final String CLEANSING_REGEX = "[0-9}+";
        new Parameters(new String[]
            { "--cleansing-regex", CLEANSING_REGEX });
    }

    @Test
    public void testSetLocalDataDirLong() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "test_it_data";
        final Parameters parameters = new Parameters(new String[]
            { "--local-datadir", LOCAL_DATADIR });
        assertEquals(LOCAL_DATADIR, parameters.getLocalDataDirectory().getPath());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetLocalDataDirShort() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "test_it_data2";
        final Parameters parameters = new Parameters(new String[]
            { "-d", LOCAL_DATADIR });
        assertEquals(LOCAL_DATADIR, parameters.getLocalDataDirectory().getPath());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetLocalTempDirLong() throws Exception
    {
        final String LOCAL_TEMPDIR = "test_it_tmp";
        final Parameters parameters = new Parameters(new String[]
            { "--local-tempdir", LOCAL_TEMPDIR });
        assertEquals(LOCAL_TEMPDIR, parameters.getLocalTemporaryDirectory().getPath());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetLocalTempDirShort() throws Exception
    {
        final String LOCAL_TEMPDIR = "test_it_tmp3";
        final Parameters parameters = new Parameters(new String[]
            { "-t", LOCAL_TEMPDIR });
        assertEquals(LOCAL_TEMPDIR, parameters.getLocalTemporaryDirectory().getPath());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetRemoteDirLong() throws Exception
    {
        final String REMOTE_DATADIR = "test_it_remote";
        final Parameters parameters = new Parameters(new String[]
            { "--remotedir", REMOTE_DATADIR });
        assertEquals(REMOTE_DATADIR, parameters.getRemoteDataDirectory().getPath());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetRemoteDirShort() throws Exception
    {
        final String REMOTE_DATADIR = "test_it_remote4";
        final Parameters parameters = new Parameters(new String[]
            { "-r", REMOTE_DATADIR });
        assertEquals(REMOTE_DATADIR, parameters.getRemoteDataDirectory().getPath());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetRemoteHostLong() throws Exception
    {
        final String REMOTE_HOST = "test_it_remote";
        final Parameters parameters = new Parameters(new String[]
            { "--remotehost", REMOTE_HOST });
        assertEquals(REMOTE_HOST, parameters.getRemoteHost());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetRemoteHostShort() throws Exception
    {
        final String REMOTE_HOST = "test_it_remote4";
        final Parameters parameters = new Parameters(new String[]
            { "-h", REMOTE_HOST });
        assertEquals(REMOTE_HOST, parameters.getRemoteHost());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetCheckIntervalLong() throws Exception
    {
        final int CHECK_INTERVAL = 5;
        final Parameters parameters = new Parameters(new String[]
            { "--check-interval", Integer.toString(CHECK_INTERVAL) });
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalMillis());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetCheckIntervalShort() throws Exception
    {
        final int CHECK_INTERVAL = 11;
        final Parameters parameters = new Parameters(new String[]
            { "-c", Integer.toString(CHECK_INTERVAL) });
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalMillis());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetQuietPeriodLong() throws Exception
    {
        final int QUIET_PERIOD = 6;
        final Parameters parameters = new Parameters(new String[]
            { "--quiet-period", Integer.toString(QUIET_PERIOD) });
        assertEquals(1000 * QUIET_PERIOD, parameters.getQuietPeriodMillis());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetQuietPeriodShort() throws Exception
    {
        final int QUIET_PERIOD = 17;
        final Parameters parameters = new Parameters(new String[]
            { "-q", Integer.toString(QUIET_PERIOD) });
        assertEquals(1000 * QUIET_PERIOD, parameters.getQuietPeriodMillis());
        assert parameters.hasAllMandatoryOptions() == false;
    }

    @Test
    public void testSetMandatoryOptions() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "ldata";
        final String LOCAL_TEMPDIR = "l" + File.separator + "tmp";
        final String REMOTE_DATADIR = "rrr";
        final Parameters parameters = new Parameters(new String[]
            { "--local-datadir", LOCAL_DATADIR, "--local-tempdir", LOCAL_TEMPDIR, "--remotedir", REMOTE_DATADIR, });
        assertEquals(LOCAL_DATADIR, parameters.getLocalDataDirectory().getPath());
        assertEquals(LOCAL_TEMPDIR, parameters.getLocalTemporaryDirectory().getPath());
        assertEquals(REMOTE_DATADIR, parameters.getRemoteDataDirectory().getPath());
        assert parameters.hasAllMandatoryOptions();
    }

    @Test
    public void testSetEverything() throws Exception
    {
        final String LOCAL_DATADIR = ".." + File.separator + "ldata";
        final String LOCAL_TEMPDIR = "l" + File.separator + "tmp";
        final String REMOTE_DATADIR = "rrr";
        final String REMOTE_HOST = "myremotehost";
        final int CHECK_INTERVAL = 22;
        final int QUIET_PERIOD = 33;
        final Parameters parameters =
                new Parameters(new String[]
                    { "--local-datadir", LOCAL_DATADIR, "--local-tempdir", LOCAL_TEMPDIR, "--remotedir",
                            REMOTE_DATADIR, "--remotehost", REMOTE_HOST, "--check-interval",
                            Integer.toString(CHECK_INTERVAL), "--quiet-period", Integer.toString(QUIET_PERIOD), });
        assertEquals(LOCAL_DATADIR, parameters.getLocalDataDirectory().getPath());
        assertEquals(LOCAL_TEMPDIR, parameters.getLocalTemporaryDirectory().getPath());
        assertEquals(REMOTE_DATADIR, parameters.getRemoteDataDirectory().getPath());
        assertEquals(REMOTE_HOST, parameters.getRemoteHost());
        assertEquals(1000 * CHECK_INTERVAL, parameters.getCheckIntervalMillis());
        assertEquals(1000 * QUIET_PERIOD, parameters.getQuietPeriodMillis());
        assert parameters.hasAllMandatoryOptions();
    }

}
