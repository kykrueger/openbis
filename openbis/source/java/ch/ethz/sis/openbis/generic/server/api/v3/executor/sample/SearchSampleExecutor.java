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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractSearchObjectExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriterion;
import ch.systemsx.cisd.openbis.generic.server.business.search.SampleSearchManager;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSampleExecutor extends AbstractSearchObjectExecutor<SampleSearchCriterion, SamplePE> implements ISearchSampleExecutor
{

    @Override
    protected List<SamplePE> doSearch(IOperationContext context, DetailedSearchCriteria criteria)
    {
        SampleSearchManager searchManager =
                new SampleSearchManager(daoFactory.getHibernateSearchDAO(), businessObjectFactory.createSampleLister(context.getSession()));

        Collection<Long> sampleIds =
                searchManager.searchForSampleIDs(context.getSession().getUserName(), criteria);

        return daoFactory.getSampleDAO().listByIDs(sampleIds);
    }

}
