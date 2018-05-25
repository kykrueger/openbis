/*
 * Copyright 2018 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server;

import java.io.File;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import ch.systemsx.cisd.common.filesystem.QueueingPathRemoverService;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;

/**
 * @author pkupczyk
 */
@Component(value = "session-workspace-provider")
public class SessionWorkspaceProvider implements ISessionWorkspaceProvider
{

    private static final String SESSION_WORKSPACE_ROOT_DIR_KEY = "session-workspace-root-dir";

    private static final String SESSION_WORKSPACE_ROOT_DIR_DEFAULT = "sessionWorkspace";

    private static final String SESSION_WORKSPACE_SHREDDER_QUEUE_FILE = ".shredder";

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SessionWorkspaceProvider.class);

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer servicePropertiesPlaceholder;

    private File sessionWorkspaceRootDir;

    @PostConstruct
    private void init() throws Exception
    {
        Properties serviceProperties = servicePropertiesPlaceholder.getResolvedProps();

        String sessionWorkspaceRootDirString =
                PropertyUtils.getProperty(serviceProperties, SESSION_WORKSPACE_ROOT_DIR_KEY, SESSION_WORKSPACE_ROOT_DIR_DEFAULT);
        sessionWorkspaceRootDir = new File(sessionWorkspaceRootDirString);

        operationLog.info("Session workspace root dir '" + sessionWorkspaceRootDir.getCanonicalPath() + "'");

        if (false == sessionWorkspaceRootDir.exists())
        {
            sessionWorkspaceRootDir.mkdirs();
        }

        QueueingPathRemoverService.start(sessionWorkspaceRootDir, new File(SESSION_WORKSPACE_SHREDDER_QUEUE_FILE));

        operationLog.info("Session workspace shredder service started");
    }

    @Override
    public File getSessionWorkspace(String sessionToken)
    {
        File sessionWorkspace = new File(sessionWorkspaceRootDir, sessionToken);

        if (false == sessionWorkspace.exists())
        {
            sessionWorkspace.mkdirs();
            operationLog.info("Session workspace '" + sessionToken + "' created");
        }

        return sessionWorkspace;
    }

    @Override
    public void deleteSessionWorkspace(String sessionToken)
    {
        try
        {
            File sessionWorkspace = new File(sessionWorkspaceRootDir, sessionToken);

            if (sessionWorkspace.exists())
            {
                QueueingPathRemoverService.removeRecursively(sessionWorkspace);
                operationLog.info("Session workspace '" + sessionToken + "' added to shredder queue");
            }
        } catch (Exception e)
        {
            operationLog.warn("Session workspace '" + sessionToken + "' could not be shredded", e);
        }
    }

}
