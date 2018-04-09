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
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.authentication.ldap.LDAPAuthenticationService;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.maintenance.IMaintenanceTask;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;

/**
 * @author Franz-Josef Elmer
 */
public class UserManagementMaintenanceTask implements IMaintenanceTask
{
    private static final String CONFIGURATION_FILE_PATH_PROPERTY = "configuration-file-path";

    private static final String DEFAULT_CONFIGURATION_FILE_PATH = "etc/user-management-maintenance-config.json";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            UserManagementMaintenanceTask.class);

    private File configurationFile;

    private LDAPAuthenticationService ldapService;

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
        ldapService = (LDAPAuthenticationService) CommonServiceProvider.getApplicationContext().getBean("ldap-authentication-service");
        operationLog.info("Plugin '" + pluginName + "' initialized. Configuration file: " + configurationFile.getAbsolutePath());
        
    }

    @Override
    public void execute()
    {
        Map<String, Group> groups = readGroupDefinitions();
        if (groups == null)
        {
            return;
        }
        Log4jSimpleLogger logger = new Log4jSimpleLogger(operationLog);
        UserManager userManager = new UserManager(CommonServiceProvider.getApplicationServerApi(), logger);
        for (Entry<String, Group> entry : groups.entrySet())
        {
            String key = entry.getKey();
            Group group = entry.getValue();
            List<String> ldapGroupKeys = group.getLdapGroupKeys();
            if (ldapGroupKeys == null || ldapGroupKeys.isEmpty())
            {
                operationLog.error("No ldapGroupKeys specified for group '" + key + "'. Task aborted.");
                return;
            }
            Map<String, Principal> principalsByUserId = new TreeMap<>();
            for (String ldapGroupKey : ldapGroupKeys)
            {
                if (StringUtils.isBlank(ldapGroupKey))
                {
                    operationLog.error("Empty ldapGroupKey for group '" + key + "'. Task aborted.");
                    return;
                    
                }
                List<Principal> principals = ldapService.listPrincipalsByKeyValue("ou", ldapGroupKey);
                if (principals.isEmpty())
                {
                    operationLog.error("No users found for ldapGroupKey '" + ldapGroupKey + "' for group '" + key + "'. Task aborted.");
                    return;
                }
                for (Principal principal : principals)
                {
                    principalsByUserId.put(principal.getUserId(), principal);
                }
            }
            userManager.addGroup(key, group, principalsByUserId);
        }
        userManager.manageUsers();
        operationLog.info("finished");
    }
    
    private Map<String, Group> readGroupDefinitions()
    {
        if (configurationFile.isFile() == false)
        {
            operationLog.error("Configuration file '" + configurationFile.getAbsolutePath() + "' doesn't exist or is a directory.");
            return null;
        }
        String serializedConfig = FileUtilities.loadToString(configurationFile);
        try
        {
            return deserialize(serializedConfig);
        } catch (Exception e)
        {
            operationLog.error("Invalid content of configuration file '" + configurationFile.getAbsolutePath() + "': " + e, e);
            return null;
        }
    }

    private Map<String, Group> deserialize(String serializedConfig) throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(serializedConfig, new TypeReference<Map<String, Group>>(){});
    }
    
}
