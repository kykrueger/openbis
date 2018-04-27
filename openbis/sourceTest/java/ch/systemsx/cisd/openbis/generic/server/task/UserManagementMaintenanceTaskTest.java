/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.generic.server.task;

import static ch.systemsx.cisd.common.test.AssertionUtil.assertContains;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Level;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService;
import ch.systemsx.cisd.authentication.ldap.LDAPDirectoryConfiguration;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.shared.basic.string.CommaSeparatedListBuilder;
import ch.systemsx.cisd.common.utilities.MockTimeProvider;
import ch.systemsx.cisd.openbis.util.LogRecordingUtils;
import de.schlichtherle.io.File;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagementMaintenanceTaskTest extends AbstractFileSystemTestCase
{
    private static final Principal U1 = new Principal("u1", "Albert", "Einstein", "a.e@abc.de");

    private BufferedAppender logRecorder;

    private File configFile;

    private File auditLogFile;

    private Properties properties;

    @BeforeMethod
    public void setUp() throws IOException
    {
        super.setUp();
        logRecorder = LogRecordingUtils.createRecorder("%-5p %c - %m%n", Level.DEBUG);
        configFile = new File(workingDirectory, "config.json");
        auditLogFile = new File(workingDirectory, "audit_log.txt");
        properties = new Properties();
        properties.setProperty(UserManagementMaintenanceTask.CONFIGURATION_FILE_PATH_PROPERTY, configFile.getPath());
        properties.setProperty(UserManagementMaintenanceTask.AUDIT_LOG_FILE_PATH_PROPERTY, auditLogFile.getPath());
    }

    @Test
    public void testSetUpFailedBecauseConfigFileDoesNotExist()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        properties.remove(UserManagementMaintenanceTask.CONFIGURATION_FILE_PATH_PROPERTY);

        // When + Then
        assertConfigFailure(task, UserManagementMaintenanceTask.DEFAULT_CONFIGURATION_FILE_PATH + "' doesn't exist or is a directory.");
    }

    @Test
    public void testSetUpFailedBecauseConfigFileIsADirectory()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        configFile.mkdirs();

        // When + Then
        assertConfigFailure(task, configFile.getPath() + "' doesn't exist or is a directory.");
    }

    @Test
    public void testSetUpFailedBecauseAuditFileIsADirectory()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        auditLogFile.mkdirs();

        // When + Then
        assertConfigFailure(task, auditLogFile.getPath() + "' is a directory.");
    }

    @Test
    public void testSetUpFailedBecauseLdapServiceIsNotConfigured()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks().withNotConfiguredLdap();
        FileUtilities.writeToFile(configFile, "");

        // When + Then
        assertConfigFailure(task, "There is no LDAP authentication service configured.");
    }

    @Test
    public void testExecuteFailedBecauseOfConfigFileHasBeenDeleted()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        configFile.delete();

        // When
        task.execute();

        // Then
        assertContains(configFile + "' doesn't exist", logRecorder.getLogContent());
    }

    @Test
    public void testExecuteFailedBecauseOfInvalidConfigFile()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);

        // When
        task.execute();

        // Then
        assertContains("ERROR OPERATION.UserManagementMaintenanceTask - Invalid content of configuration file '"
                + configFile.getAbsolutePath(), logRecorder.getLogContent());
    }

    @Test
    public void testExecuteNoGroups()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTask - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath(), logRecorder.getLogContent());
    }

    @Test
    public void testExecuteMissingLdapGroupKeys()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"ABC\"}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTask - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - manage 1 groups\n"
                + "DEBUG OPERATION.UserManagementMaintenanceTask - Common spaces: {}\n"
                + "ERROR OPERATION.UserManagementMaintenanceTask - No ldapGroupKeys specified for group 'ABC'. Task aborted.",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteEmptyLdapGroupKeys()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"ABC\",\"ldapGroupKeys\":[\"\"]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTask - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - manage 1 groups\n"
                + "DEBUG OPERATION.UserManagementMaintenanceTask - Common spaces: {}\n"
                + "ERROR OPERATION.UserManagementMaintenanceTask - Empty ldapGroupKey for group 'ABC'. Task aborted.",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteEmptyLdapGroups()
    {
        // Given
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks();
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"groups\": [{\"key\":\"ABC\",\"ldapGroupKeys\":[\"a1\"]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTask - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - manage 1 groups\n"
                + "DEBUG OPERATION.UserManagementMaintenanceTask - Common spaces: {}\n"
                + "ERROR OPERATION.UserManagementMaintenanceTask - No users found for ldapGroupKey 'a1' for group 'ABC'. Task aborted.",
                logRecorder.getLogContent());
    }

    @Test
    public void testExecuteHappyCase()
    {
        // Given
        UserManagerReport report = new UserManagerReport(new MockTimeProvider(0, 1000));
        report.addErrorMessage("This is a test error message");
        report.addGroup("blabla");
        report.addUser("a", "A");
        UserManagementMaintenanceTaskWithMocks task = new UserManagementMaintenanceTaskWithMocks().withGroup("s", U1)
                .withUserManagerReport(report);
        FileUtilities.writeToFile(configFile, "");
        task.setUp("", properties);
        FileUtilities.writeToFile(configFile, "{\"commonSpaces\":{\"USER\": [\"ALPHA\"]},"
                + "\"groups\": [{\"name\":\"sis\",\"key\":\"SIS\",\"ldapGroupKeys\": [\"s\"],\"admins\": [\"u2\"]}]}");

        // When
        task.execute();

        // Then
        assertEquals("INFO  OPERATION.UserManagementMaintenanceTask - Setup plugin \n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - Plugin '' initialized. Configuration file: "
                + configFile.getAbsolutePath() + "\n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - manage 1 groups\n"
                + "DEBUG OPERATION.UserManagementMaintenanceTask - Common spaces: {USER=[ALPHA]}\n"
                + "DEBUG OPERATION.UserManagementMaintenanceTask - Add group SIS[name:sis, ldapGroupKeys:[s], admins:[u2]] with users [u1=u1]\n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - 1 users for group SIS\n"
                + "ERROR NOTIFY.UserManagementMaintenanceTask - User management failed for the following reason(s):\n\n"
                + "This is a test error message\n\n"
                + "INFO  OPERATION.UserManagementMaintenanceTask - finished",
                logRecorder.getLogContent());
        assertEquals("1970-01-01 01:00:00 [ADD-GROUP] blabla\n"
                + "1970-01-01 01:00:01 [ADD-USER] a (home space: A)\n\n",
                FileUtilities.loadToString(auditLogFile));
    }

    private void assertConfigFailure(IMaintenanceTask task, String errorMessageSnippet)
    {
        try
        {
            task.setUp("", properties);
            fail("ConfigurationFailureException expected");
        } catch (ConfigurationFailureException e)
        {
            assertContains(errorMessageSnippet, e.getMessage());
        }
    }

    private final class UserManagementMaintenanceTaskWithMocks extends UserManagementMaintenanceTask
    {
        private Map<String, List<Principal>> usersByGroup = new TreeMap<>();

        private LDAPDirectoryConfiguration config;

        private UserManagerReport report;

        private UserManagementMaintenanceTaskWithMocks()
        {
            config = new LDAPDirectoryConfiguration();
            config.setServerUrl("blabla");
            config.setSecurityPrincipalDistinguishedName("blabla");
            config.setSecurityPrincipalPassword("blabla");
        }

        @Override
        protected List<Principal> getUsersOfGroup(String ldapGroupKey)
        {
            List<Principal> users = usersByGroup.get(ldapGroupKey);
            return users == null ? new LinkedList<>() : users;
        }

        @Override
        protected LDAPAuthenticationService getLdapAuthenticationService()
        {
            return new LDAPAuthenticationService(config);
        }

        @Override
        protected UserManager createUserManager(UserManagerConfig config, Log4jSimpleLogger logger)
        {
            return new MockUserManager(config.getCommonSpaces(), logger);
        }

        private UserManagementMaintenanceTaskWithMocks withGroup(String groupCode, Principal... users)
        {
            usersByGroup.put(groupCode, Arrays.asList(users));
            return this;
        }

        private UserManagementMaintenanceTaskWithMocks withNotConfiguredLdap()
        {
            config.setSecurityPrincipalPassword(null);
            return this;
        }

        private UserManagementMaintenanceTaskWithMocks withUserManagerReport(UserManagerReport report)
        {
            this.report = report;
            return this;
        }

        private final class MockUserManager extends UserManager
        {
            private ISimpleLogger logger;

            MockUserManager(Map<Role, List<String>> commonSpacesByRole, ISimpleLogger logger)
            {
                super(null, null, commonSpacesByRole, logger, null);
                this.logger = logger;
                logger.log(LogLevel.DEBUG, "Common spaces: " + commonSpacesByRole);
            }

            @Override
            public void addGroup(UserGroup group, Map<String, Principal> principalsByUserId)
            {
                String renderedGroup = group.getKey() + "[name:" + group.getName() + ", ldapGroupKeys:"
                        + group.getLdapGroupKeys() + ", admins:" + group.getAdmins() + "]";
                CommaSeparatedListBuilder builder = new CommaSeparatedListBuilder();
                for (Entry<String, Principal> entry : principalsByUserId.entrySet())
                {
                    builder.append(entry.getKey() + "=" + entry.getValue().getUserId());
                }
                logger.log(LogLevel.DEBUG, "Add group " + renderedGroup + " with users [" + builder + "]");
                super.addGroup(group, principalsByUserId);
            }

            @Override
            public UserManagerReport manage()
            {
                return report;
            }
        }
    }

}
