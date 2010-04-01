/*
 * Copyright 2010 ETH Zuerich, CISD
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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetLocationUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;

/**
 * Abstract superclass of DssServiceRpc implementations.
 * <p>
 * Provides methods to check security and access to data sets.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractDssServiceRpc
{
    // private static final Logger notificationLog =
    // LogFactory.getLogger(LogCategory.NOTIFY, DssServiceRpcV1.class);

    static protected final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, AbstractDssServiceRpc.class);

    private final IEncapsulatedOpenBISService openBISService;

    private File storeDirectory;

    private DatabaseInstance homeDatabaseInstance;

    /**
     * Configuration method to set the path to the DSS store. Should only be called by the object
     * that configures the RPC services.
     */
    public void setStoreDirectory(File aFile)
    {
        storeDirectory = aFile;
    }

    /**
     * Constructor with required reference to the openBIS service.
     * 
     * @param openBISService
     */
    protected AbstractDssServiceRpc(IEncapsulatedOpenBISService openBISService)
    {
        this.openBISService = openBISService;
    }

    protected IEncapsulatedOpenBISService getOpenBISService()
    {
        return openBISService;
    }

    /**
     * Get a file representing the root of the DSS store.
     */
    protected File getStoreDirectory()
    {
        return storeDirectory;
    }

    /**
     * Get the home database instance for the openBIS instance I connect to.
     */
    protected DatabaseInstance getHomeDatabaseInstance()
    {
        // Not synchronized because it doesn't cause any harm if the ivar is initialized twice.
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = openBISService.getHomeDatabaseInstance();
        }
        return homeDatabaseInstance;
    }

    /**
     * Check with openBIS if the user with the given sessionToken is allowed to access the data set
     * specified by the dataSetCode.
     */
    protected boolean isDatasetAccessible(String sessionToken, String dataSetCode)
    {
        boolean access;
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("Check access to the data set '%s' on openBIS server.",
                    dataSetCode));
        }

        try
        {
            openBISService.checkDataSetAccess(sessionToken, dataSetCode);
            access = true;
        } catch (UserFailureException ex)
        {
            access = false;
        }

        return access;
    }

    /**
     * Get the top level of the folder for the data set.
     */
    protected File getRootDirectoryForDataSet(String code)
    {
        File dataSetRootDirectory =
                DatasetLocationUtil.getDatasetLocationPath(getStoreDirectory(), code,
                        getHomeDatabaseInstance().getUuid());
        return dataSetRootDirectory;
    }

    protected File checkAccessAndGetRootDirectory(String sessionToken, String dataSetCode)
            throws IllegalArgumentException
    {
        if (isDatasetAccessible(sessionToken, dataSetCode) == false)
            throw new IllegalArgumentException("Path does not exist.");

        File dataSetRootDirectory = getRootDirectoryForDataSet(dataSetCode);
        if (dataSetRootDirectory.exists() == false)
        {
            throw new IllegalArgumentException("Path does not exist.");
        }

        return dataSetRootDirectory;
    }

    protected File checkAccessAndGetFile(String sessionToken, String dataSetCode, String path)
            throws IOException, IllegalArgumentException
    {
        File dataSetRootDirectory = checkAccessAndGetRootDirectory(sessionToken, dataSetCode);

        String dataSetRootPath = dataSetRootDirectory.getCanonicalPath();
        File requestedFile = new File(dataSetRootDirectory, path);
        // Make sure the requested file is under the root of the data set
        if (requestedFile.getCanonicalPath().startsWith(dataSetRootPath) == false)
        {
            throw new IllegalArgumentException("Path does not exist.");
        }
        return requestedFile;
    }
}