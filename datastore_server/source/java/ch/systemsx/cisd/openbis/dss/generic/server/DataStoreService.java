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

import ch.systemsx.cisd.cifex.rpc.ICIFEXRPCService;
import ch.systemsx.cisd.cifex.rpc.client.RPCServiceFactory;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.common.exceptions.InvalidAuthenticationException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.spring.AbstractServiceWithLogger;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;

/**
 * Implementation of {@link IDataStoreService} which will be accessed remotely by the opneBIS
 * server.
 * 
 * @author Franz-Josef Elmer
 */
public class DataStoreService extends AbstractServiceWithLogger<IDataStoreService> implements
        IDataStoreService, InitializingBean
{
    private static ICIFEXRPCService createCIFEXService(String baseURL)
    {
        final String serviceURL = baseURL + Constants.CIFEX_RPC_PATH;
        final ICIFEXRPCService service =
                RPCServiceFactory.createServiceProxy(serviceURL, true);
        final int serverVersion = service.getVersion();
        if (ICIFEXRPCService.VERSION != serverVersion)
        {
            System.err.println("This client has the wrong service version for the server (client: "
                    + ICIFEXRPCService.VERSION + ", server: " + serverVersion + ").");
            return null;
        }
        return service;
    }
    
    private final SessionTokenManager sessionTokenManager;
    
    private final IDataSetCommandExecutorFactory commandExecutorFactory;
    
    private File storeRoot;

    private IDataSetCommandExecutor commandExecuter;

    private final ICIFEXRPCService cifexService;

    public DataStoreService(SessionTokenManager sessionTokenManager, String baseURL)
    {
        this(sessionTokenManager, new IDataSetCommandExecutorFactory()
            {
                public IDataSetCommandExecutor create(File store)
                {
                    return new DataSetCommandExecuter(new File(store, "commandQueue"));
                }
            }, createCIFEXService(baseURL));
    }

    DataStoreService(SessionTokenManager sessionTokenManager,
            IDataSetCommandExecutorFactory commandExecutorFactory, ICIFEXRPCService cifexService)
    {
        this.sessionTokenManager = sessionTokenManager;
        this.commandExecutorFactory = commandExecutorFactory;
        this.cifexService = cifexService;
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
                throw new WrappedIOException(new IOException(
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
            String comment, String userID, String password) throws InvalidAuthenticationException
    {
        sessionTokenManager.assertValidSessionToken(sessionToken);

        if (cifexService.login(userID, password) == null)
        {
            throw new InvalidSessionException("User couldn't be authenticated at CIFEX.");
        }
        commandExecuter.scheduleCommand(new UploadingCommand(cifexService, dataSetLocations,
                comment, userID, password));
    }

}
