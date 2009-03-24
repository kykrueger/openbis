/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;

/**
 * Implementation of {@link IDataStoreService} which will be accessed remotely by the opneBIS
 * server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreService extends AbstractServiceWithLogger<IDataStoreService> implements
        IDataStoreService, InitializingBean
{
    private final SessionTokenManager sessionTokenManager;
    
    private final IDataSetCommandExecutorFactory commandExecutorFactory;
    
    private final MailClientParameters mailClientParameters;
    
    private File storeRoot;

    private IDataSetCommandExecutor commandExecuter;

    public DataStoreService(SessionTokenManager sessionTokenManager, MailClientParameters mailClientParameters)
    {
        this(sessionTokenManager, new IDataSetCommandExecutorFactory()
            {
                public IDataSetCommandExecutor create(File store)
                {
                    return new DataSetCommandExecuter(store);
                }
            }, mailClientParameters);
    }

    DataStoreService(SessionTokenManager sessionTokenManager,
            IDataSetCommandExecutorFactory commandExecutorFactory, MailClientParameters mailClientParameters)
    {
        this.sessionTokenManager = sessionTokenManager;
        this.commandExecutorFactory = commandExecutorFactory;
        this.mailClientParameters = mailClientParameters;
    }

    public final void setStoreRoot(File storeRoot)
    {
        this.storeRoot = storeRoot;
    }

    public void afterPropertiesSet() throws Exception
    {
        String prefix = "Property 'storeRoot' ";
        if (storeRoot == null)
        {
            throw new IllegalStateException(prefix + "not set.");
        }
        String storeRootPath = storeRoot.getAbsolutePath();
        if (storeRoot.isFile())
        {
            throw new IllegalArgumentException(prefix + "is a file instead of a directory: "
                    + storeRootPath);
        }
        if (storeRoot.exists() == false)
        {
            if (storeRoot.mkdirs() == false)
            {
                throw new IOExceptionUnchecked(new IOException(
                        "Couldn't create root directory of the data store: " + storeRootPath));
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Creates root directory of the data store: " + storeRootPath);
            }
        }
        commandExecuter = commandExecutorFactory.create(storeRoot);
        commandExecuter.start();
    }

    @Override
    protected Class<IDataStoreService> getProxyInterface()
    {
        return IDataStoreService.class;
    }

    public IDataStoreService createLogger(boolean invocationSuccessful)
    {
        return new DataStoreServiceLogger(operationLog, invocationSuccessful);
    }

    public int getVersion(String sessionToken)
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        
        return IDataStoreService.VERSION;
    }

    public List<String> getKnownDataSets(String sessionToken, List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        
        List<String> knownLocations = new ArrayList<String>();
        for (String location : dataSetLocations)
        {
            if (new File(storeRoot, location).exists())
            {
                knownLocations.add(location);
            }
        }
        return knownLocations;
    }

    public void deleteDataSets(String sessionToken, final List<String> dataSetLocations)
            throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);
        
        commandExecuter.scheduleCommand(new DeletionCommand(dataSetLocations));
    }

    public void uploadDataSetsToCIFEX(String sessionToken, List<String> dataSetLocations,
            DataSetUploadContext context) throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        CIFEXRPCServiceFactory serviceFactory = new CIFEXRPCServiceFactory(context.getCifexURL());
        ICIFEXRPCService service = serviceFactory.createService();
        String userID = context.getUserID();
        String password = context.getPassword();
        if (service.login(userID, password) == null)
        {
            throw new InvalidSessionException("User couldn't be authenticated at CIFEX.");
        }
        commandExecuter.scheduleCommand(new UploadingCommand(serviceFactory, mailClientParameters,
                dataSetLocations, context));
    }
    
}
