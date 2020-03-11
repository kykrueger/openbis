/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.deletion;

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.Deletion;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.fetchoptions.DeletionFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.DeletionSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.SearchDeletionsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.search.SearchDeletionsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.deletion.IDeletionTranslator;

/**
 * @author pkupczyk
 */
@Component
public class SearchDeletionsOperationExecutor extends
        AbstractSearchObjectsOperationExecutor<Deletion, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion, DeletionSearchCriteria, DeletionFetchOptions>
        implements ISearchDeletionsOperationExecutor
{

    @Autowired
    private ISearchDeletionExecutor searchExecutor;

    @Autowired
    private IDeletionTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<DeletionSearchCriteria, DeletionFetchOptions>> getOperationClass()
    {
        return SearchDeletionsOperation.class;
    }

    @Override
    protected List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion> doSearch(IOperationContext context, DeletionSearchCriteria criteria,
            DeletionFetchOptions fetchOptions)
    {
        return searchExecutor.search(context, criteria, fetchOptions);
    }

    @Override
    protected Map<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion, Deletion> doTranslate(TranslationContext translationContext,
            List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion> objects, DeletionFetchOptions fetchOptions)
    {
        return translator.translate(translationContext, objects, fetchOptions);
    }

    @Override
    protected SearchObjectsOperationResult<Deletion> getOperationResult(SearchResult<Deletion> searchResult)
    {
        return new SearchDeletionsOperationResult(searchResult);
    }

    @Override
    protected ISearchManager<DeletionSearchCriteria, Deletion, ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion> getSearchManager() {
        throw new RuntimeException("This method is not implemented yet.");
    }

}
