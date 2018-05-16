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

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagementMaintenanceTask implements IMaintenanceTask
{
    static final String CONFIGURATION_FILE_PATH_PROPERTY = "configuration-file-path";

    static final String DEFAULT_CONFIGURATION_FILE_PATH = "etc/user-management-maintenance-config.json";

    static final String AUDIT_LOG_FILE_PATH_PROPERTY = "audit-log-file-path";

    static final String DEFAULT_AUDIT_LOG_FILE_PATH = "logs/user-management-audit_log.txt";
    
    static final String SHARES_MAPPING_FILE_PATH_PROPERTY = "shares-mapping-file-path";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            UserManagementMaintenanceTask.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            UserManagementMaintenanceTask.class);

    private File configurationFile;

    private File auditLogFile;

    private LDAPAuthenticationService ldapService;

    private File shareIdsMappingFile;

    @Override
    public void setUp(String pluginName, Properties properties)
    {
        operationLog.info("Setup plugin " + pluginName);
        configurationFile = new File(properties.getProperty(CONFIGURATION_FILE_PATH_PROPERTY, DEFAULT_CONFIGURATION_FILE_PATH));
        if (configurationFile.isFile() == false)
        {
            throw new ConfigurationFailureException("Configuration file '" + configurationFile.getAbsolutePath()
                    + "' doesn't exist or is a directory.");
        }
        auditLogFile = new File(properties.getProperty(AUDIT_LOG_FILE_PATH_PROPERTY, DEFAULT_AUDIT_LOG_FILE_PATH));
        if (auditLogFile.isDirectory())
        {
            throw new ConfigurationFailureException("Audit log file '" + auditLogFile.getAbsolutePath() + "' is a directory.");
        }
        ldapService = getLdapAuthenticationService();
        if (ldapService.isConfigured() == false)
        {
            throw new ConfigurationFailureException("There is no LDAP authentication service configured. "
                    + "At least 'ldap.server.url', 'ldap.security.principal.distinguished.name', "
                    + "'ldap.security.principal.password' have to be specified in 'service.properties'.");
        }
        String shareIdsMappingFilePath = properties.getProperty(SHARES_MAPPING_FILE_PATH_PROPERTY);
        if (shareIdsMappingFilePath != null)
        {
            shareIdsMappingFile = new File(shareIdsMappingFilePath);
            if (shareIdsMappingFile.isDirectory())
            {
                throw new ConfigurationFailureException("Share ids mapping file '" + shareIdsMappingFile.getAbsolutePath() + "' is a directory.");
            }
        }
        operationLog.info("Plugin '" + pluginName + "' initialized. Configuration file: " + configurationFile.getAbsolutePath());
    }

    @Override
    public void execute()
    {
        UserManagerConfig config = readGroupDefinitions();
        if (config == null || config.getGroups() == null)
        {
            return;
        }
        operationLog.info("manage " + config.getGroups().size() + " groups");
        Log4jSimpleLogger logger = new Log4jSimpleLogger(operationLog);
        UserManager userManager = createUserManager(config, logger);
        for (UserGroup group : config.getGroups())
        {
            if (addGroup(userManager, group) == false)
            {
                return;
            }
        }
        UserManagerReport userManagerReport = userManager.manage();
        handleReport(userManagerReport);
        operationLog.info("finished");
    }

    private UserManagerConfig readGroupDefinitions()
    {
        if (configurationFile.isFile() == false)
        {
            operationLog.error("Configuration file '" + configurationFile.getAbsolutePath() + "' doesn't exist or is a directory.");
            return null;
        }
        String serializedConfig = FileUtilities.loadToString(configurationFile);
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(serializedConfig, UserManagerConfig.class);
        } catch (Exception e)
        {
            operationLog.error("Invalid content of configuration file '" + configurationFile.getAbsolutePath() + "': " + e, e);
            return null;
        }
    }
    
    private boolean addGroup(UserManager userManager, UserGroup group)
    {
        String key = group.getKey();
        if (shareIdsMappingFile != null)
        {
            List<String> shareIds = group.getShareIds();
            if (shareIds == null || shareIds.isEmpty())
            {
                operationLog.error("No shareIds specified for group '" + key + "'. Task aborted.");
                return false;
            }
        }
        List<String> ldapGroupKeys = group.getLdapGroupKeys();
        if (ldapGroupKeys == null || ldapGroupKeys.isEmpty())
        {
            operationLog.error("No ldapGroupKeys specified for group '" + key + "'. Task aborted.");
            return false;
        }
        Map<String, Principal> principalsByUserId = new TreeMap<>();
        for (String ldapGroupKey : ldapGroupKeys)
        {
            if (StringUtils.isBlank(ldapGroupKey))
            {
                operationLog.error("Empty ldapGroupKey for group '" + key + "'. Task aborted.");
                return false;

            }
            List<Principal> principals = getUsersOfGroup(ldapGroupKey);
            if (principals.isEmpty())
            {
                operationLog.error("No users found for ldapGroupKey '" + ldapGroupKey + "' for group '" + key + "'. Task aborted.");
                return false;
            }
            for (Principal principal : principals)
            {
                principalsByUserId.put(principal.getUserId(), principal);
            }
        }
        userManager.addGroup(group, principalsByUserId);
        return true;
    }

    private void handleReport(UserManagerReport report)
    {
        String errorReport = report.getErrorReport();
        if (StringUtils.isNotBlank(errorReport))
        {
            notificationLog.error("User management failed for the following reason(s):\n\n" + errorReport);
        }
        String auditLog = report.getAuditLog();
        if (StringUtils.isNotBlank(auditLog))
        {
            FileUtilities.appendToFile(auditLogFile, auditLog, true);
        }
    }

    protected List<Principal> getUsersOfGroup(String ldapGroupKey)
    {
        return ldapService.listPrincipalsByKeyValue("ou", ldapGroupKey);
    }

    protected LDAPAuthenticationService getLdapAuthenticationService()
    {
        return (LDAPAuthenticationService) CommonServiceProvider.tryToGetBean("ldap-authentication-service");
    }

    private UserManager createUserManager(UserManagerConfig config, Log4jSimpleLogger logger)
    {
        UserManager userManager = createUserManager(logger);
        userManager.setGlobalSpaces(config.getGlobalSpaces());
        try
        {
            userManager.setCommon(config.getCommonSpaces(), config.getCommonSamples(), config.getCommonExperiments());
        } catch (ConfigurationFailureException e)
        {
            notificationLog.error(e.getMessage());
        }
        return userManager;
    }

    protected UserManager createUserManager(Log4jSimpleLogger logger)
    {
        IAuthenticationService authenticationService = (IAuthenticationService) CommonServiceProvider.tryToGetBean("authentication-service");
        return new UserManager(authenticationService, CommonServiceProvider.getApplicationServerApi(), shareIdsMappingFile, 
                logger, SystemTimeProvider.SYSTEM_TIME_PROVIDER);
    }
}
