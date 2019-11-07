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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.dataset.SearchDataSetsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.experiment.SearchExperimentsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample.SearchSamplesOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.IPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ISearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;

/**
 * @author pkupczyk
 */
public abstract class SearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA extends AbstractSearchCriteria,
        FETCH_OPTIONS extends FetchOptions<OBJECT>> extends AbstractSearchObjectsOperationExecutor<OBJECT, OBJECT_PE, CRITERIA, FETCH_OPTIONS>
{

    protected abstract ISearchObjectExecutor<CRITERIA, OBJECT_PE> getExecutor();

    protected abstract ITranslator<OBJECT_PE, OBJECT, FETCH_OPTIONS> getTranslator();

    protected abstract ISearchManager<CRITERIA, OBJECT, OBJECT_PE> getSearchManager();

    @Override
    protected final List<OBJECT_PE> doSearch(IOperationContext context, CRITERIA criteria, FETCH_OPTIONS fetchOptions)
    {
        return getExecutor().search(context, criteria);
    }

    @Override
    protected final Map<OBJECT_PE, OBJECT> doTranslate(TranslationContext translationContext, List<OBJECT_PE> ids, FETCH_OPTIONS fetchOptions)
    {
        return getTranslator().translate(translationContext, ids, fetchOptions);
    }

    @Override
    protected SearchObjectsOperationResult<OBJECT> doExecute(final IOperationContext context,
            final SearchObjectsOperation<CRITERIA, FETCH_OPTIONS> operation)
    {
        // TODO: remove this hack when all implementations of this executor implement getSearchManager() which is not throwing an exception
        if (!(this instanceof SearchSamplesOperationExecutor || this instanceof SearchExperimentsOperationExecutor ||
                this instanceof SearchDataSetsOperationExecutor))
        {
            return super.doExecute(context, operation);
        }

        final CRITERIA criteria = operation.getCriteria();
        final FETCH_OPTIONS fetchOptions = operation.getFetchOptions();

        if (criteria == null)
        {
            throw new IllegalArgumentException("Criteria cannot be null.");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null.");
        }

        final Long userId = context.getSession().tryGetPerson().getId();
        final IPage page = new IPage()
        {

            @Override
            public <T, C extends Collection<T>> C page(final C objects, final ISearchCriteria c, final FetchOptions fo)
            {
                final List<T> toPage = new ArrayList<>(objects);
                final Integer fromRecord = fo.getFrom();
                final Integer recordsCount = fo.getCount();
                final boolean hasPaging = fromRecord != null || recordsCount != null;
                return hasPaging ? (C) toPage.subList(fromRecord, Math.min(fromRecord + recordsCount, toPage.size())) : (C) toPage;
            }

        };
        final TranslationContext translationContext = new TranslationContext(context.getSession());
        final SortOptions<OBJECT> sortOptions = fetchOptions.getSortBy();

        final Set<Long> allResultsIds = getSearchManager().searchForIDs(userId, criteria, sortOptions);
        final Set<Long> filteredResults = getSearchManager().filterIDsByUserRights(userId, allResultsIds);
        final List<Long> sortedAndPagedResultIds = (List<Long>) page.page(filteredResults, criteria, fetchOptions);
        final List<OBJECT_PE> sortedAndPagedResultPEs = getSearchManager().translate(sortedAndPagedResultIds);
        final Map<OBJECT_PE, OBJECT> sortedAndPagedResultV3DTOs = doTranslate(translationContext, sortedAndPagedResultPEs, fetchOptions);
        final List<OBJECT> finalResults = new ArrayList<>(sortedAndPagedResultV3DTOs.values());

        final Integer from = fetchOptions.getFrom();
        fetchOptions.from(null);
        final Integer count = fetchOptions.getCount();
        fetchOptions.count(null);
        final List<OBJECT> sortedFinalResults = new SortAndPage().sortAndPage(finalResults, criteria, fetchOptions);
        fetchOptions.from(from);
        fetchOptions.count(count);

        final SearchResult<OBJECT> searchResult = new SearchResult<>(sortedFinalResults, allResultsIds.size());
        return getOperationResult(searchResult);
    }

}
