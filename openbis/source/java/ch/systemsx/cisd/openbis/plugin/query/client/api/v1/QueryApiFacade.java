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

package ch.systemsx.cisd.openbis.plugin.query.client.api.v1;

import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.IQueryApiServer;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryDescription;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.QueryTableModel;
import ch.systemsx.cisd.openbis.plugin.query.shared.api.v1.dto.ReportDescription;

/**
 * @author Franz-Josef Elmer
 */
class QueryApiFacade implements IQueryApiFacade
{
    private final IQueryApiServer service;

    private final IGeneralInformationService generalInformationService;

    private final String sessionToken;

    QueryApiFacade(IQueryApiServer service, IGeneralInformationService generalInformationService,
            String sessionToken)
    {
        this.service = service;
        this.generalInformationService = generalInformationService;
        this.sessionToken = sessionToken;
    }

    public String getSessionToken()
    {
        return sessionToken;
    }

    public void logout()
    {
        service.logout(sessionToken);
    }

    public IGeneralInformationService getGeneralInformationService()
    {
        return generalInformationService;
    }

    public List<QueryDescription> listQueries()
    {
        return service.listQueries(sessionToken);
    }

    public QueryTableModel executeQuery(long queryID, Map<String, String> parameterBindings)
    {
        return service.executeQuery(sessionToken, queryID, parameterBindings);
    }

    public List<ReportDescription> listTableReportDescriptions()
    {
        return service.listTableReportDescriptions(sessionToken);
    }

    public QueryTableModel createReportFromDataSets(ReportDescription reportDescription,
            List<String> dataSetCodes)
    {
        return service.createReportFromDataSets(sessionToken, reportDescription.getDataStoreCode(),
                reportDescription.getKey(), dataSetCodes);
    }
}
