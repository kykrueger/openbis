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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.id.DataStorePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.ReportingService;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.DssServicePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.id.IDssServiceId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.service.search.ReportingServiceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.Matcher;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.StringFieldMatcher;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;

/**
 * @author Franz-Josef Elmer
 */
@Component
public class SearchReportingServiceExecutor
        extends AbstractSearchObjectManuallyExecutor<ReportingServiceSearchCriteria, ReportingService>
        implements ISearchReportingServiceExecutor
{
    @Autowired
    private IDAOFactory daoFactory;

    @Autowired
    private IReportingServiceAuthorizationExecutor authorizationExecutor;

    @Override
    public List<ReportingService> search(IOperationContext context, ReportingServiceSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);
        return super.search(context, criteria);
    }

    @Override
    protected List<ReportingService> listAll()
    {
        List<DataStorePE> dataStores = daoFactory.getDataStoreDAO().listDataStores();
        List<ReportingService> services = new ArrayList<>();
        for (DataStorePE dataStore : dataStores)
        {
            for (DataStoreServicePE dsService : dataStore.getServices())
            {
                if (dsService.isTableReport())
                {
                    ReportingService reportingService = new ReportingService();
                    reportingService.setPermId(new DssServicePermId(dsService.getKey(), new DataStorePermId(dataStore.getCode())));
                    reportingService.setName(dsService.getKey());
                    reportingService.setLabel(dsService.getLabel());
                    reportingService.setDataSetTypeCodes(
                            dsService.getDatasetTypes().stream().map(t -> t.getCode()).collect(Collectors.toList()));
                    services.add(reportingService);
                }
            }
        }
        return services;
    }

    @Override
    protected Matcher<ReportingService> getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof NameSearchCriteria)
        {
            return new NameMatcher();
        }
        throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
    }

    private static class IdMatcher extends Matcher<ReportingService>
    {
        @Override
        public List<ReportingService> getMatching(IOperationContext context, List<ReportingService> objects, ISearchCriteria criteria)
        {
            @SuppressWarnings("unchecked")
            IDssServiceId id = ((IdSearchCriteria<IDssServiceId>) criteria).getId();
            if (id == null)
            {
                return objects;
            }
            return objects.stream().filter(s -> s.getPermId().equals(id)).collect(Collectors.toList());
        }
    }

    private static class NameMatcher extends StringFieldMatcher<ReportingService>
    {
        @Override
        protected String getFieldValue(ReportingService service)
        {
            return service.getName();
        }
    }
}
