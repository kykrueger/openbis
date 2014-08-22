/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.systemsx.cisd.openbis.dss.generic.server.oaipmh;

import java.util.HashMap;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.IPluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.PluginScriptRunnerFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataStoreServiceInternal;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

import de.schlichtherle.io.File;

/**
 * @author pkupczyk
 */
public class JythonBasedRequestHandler implements IRequestHandler
{

    private static final String SCRIPT_PATH_PARAMETER_NAME = "script-path";

    private String scriptPath;

    @Override
    public void init(Properties properties)
    {
        this.scriptPath = initScriptPath(properties);
    }

    private String initScriptPath(Properties properties)
    {
        String path = properties.getProperty(SCRIPT_PATH_PARAMETER_NAME);

        if (path == null || path.trim().length() == 0)
        {
            throw new IllegalArgumentException("Script path is null or empty");
        }

        File file = new File(path);

        if (false == file.exists())
        {
            throw new IllegalArgumentException("Script path: '" + file.getAbsolutePath() + "' does not exist");
        }

        if (file.isDirectory())
        {
            throw new IllegalArgumentException("Script path: '" + file.getAbsolutePath() + "' represents a directory");
        }

        return path;
    }

    @Override
    public void handle(SessionContextDTO session, HttpServletRequest req, HttpServletResponse resp)
    {
        handleWithScript(session, req, resp, getScriptRunnerFactory(scriptPath));
    }

    protected void handleWithScript(SessionContextDTO session, HttpServletRequest req, HttpServletResponse resp,
            IPluginScriptRunnerFactory factory)
    {
        IShareIdManager manager = ServiceProvider.getShareIdManager();
        try
        {
            IDataStoreServiceInternal service = ServiceProvider.getDataStoreService();
            DataSetProcessingContext context = new DataSetProcessingContext(ServiceProvider.getHierarchicalContentProvider(),
                    service.getDataSetDirectoryProvider(),
                    service.getSessionWorkspaceProvider(session.getSessionToken()),
                    new HashMap<String, String>(), service.createEMailClient(), session.getUserName(), session.getUserEmail(),
                    session.getSessionToken());
            factory.createRequestHandlerPluginRunner(context).handle(req, resp);
        } finally
        {
            manager.releaseLocks();
        }
    }

    @SuppressWarnings("hiding")
    protected IPluginScriptRunnerFactory getScriptRunnerFactory(String scriptPath)
    {
        return new PluginScriptRunnerFactory(scriptPath);
    }

}
