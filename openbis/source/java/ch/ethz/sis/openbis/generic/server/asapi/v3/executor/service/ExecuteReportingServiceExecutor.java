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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.ReportingServiceExecutionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.execute.TableModel;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class ExecuteReportingServiceExecutor extends AbstractDssServiceExecutor implements IExecuteReportingServiceExecutor
{
    @Autowired
    private IReportingServiceAuthorizationExecutor authorizationExecutor;

    @Resource(name = ComponentNames.COMMON_BUSINESS_OBJECT_FACTORY)
    protected ICommonBusinessObjectFactory businessObjectFactory;

    @Override
    public TableModel execute(IOperationContext context, IDssServiceId serviceId, ReportingServiceExecutionOptions options)
    {
        checkData(serviceId, options);
        authorizationExecutor.canExecute(context, options.getDataSetCodes());

        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(context.getSession());
        DssServicePermId permId = (DssServicePermId) serviceId;
        String key = permId.getPermId();
        String datastoreCode = ((DataStorePermId) permId.getDataStoreId()).getPermId();
        return translate(dataSetTable.createReportFromDatasets(key, datastoreCode, options.getDataSetCodes()));
    }
    
    private void checkData(IDssServiceId serviceId, ReportingServiceExecutionOptions options)
    {
        checkData(serviceId);
        if (options == null)
        {
            throw new UserFailureException("Options cannot be null.");
        }
        List<String> dataSetCodes = options.getDataSetCodes();
        if (dataSetCodes == null)
        {
            throw new UserFailureException("Data set codes cannot be null.");
        }
        if (dataSetCodes.isEmpty())
        {
            throw new UserFailureException("No data set code specified.");
        }
    }
}
