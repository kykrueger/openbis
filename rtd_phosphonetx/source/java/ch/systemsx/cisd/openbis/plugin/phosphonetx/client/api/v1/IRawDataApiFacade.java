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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.api.v1;

import java.util.List;

import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;


/**
 * Facade for openBIS PhosphoNetX raw data (aka MS_INJECTION data) service. 
 *
 * @author Franz-Josef Elmer
 */
public interface IRawDataApiFacade
{
    /**
     * Return the session token for the logged-in user.
     */
    public String getSessionToken();

    /**
     * Returns all samples of type MS_INJECTION in space MS_DATA which have a parent sample which
     * the specified user is allow to read.
     */
    public List<MsInjectionDataInfo> listRawDataSamples(String userID);

    /**
     * Lists all processing plugins on DSS.
     */
    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos();
    
    /**
     * Processes the data sets of specified samples by the DSS processing plug-in of specified key
     * for the specified user. Only the most recent data sets of specified type are processed.
     */
    public void processingRawData(String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType);

    /**
     * Logs current user out.
     */
    public void logout();

}
