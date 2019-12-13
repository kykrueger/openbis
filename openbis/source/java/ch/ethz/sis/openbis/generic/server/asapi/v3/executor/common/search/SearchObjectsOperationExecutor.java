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

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
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


    // TODO: remove this hack when all implementations of this executor implement getSearchManager() which is not throwing an exception
    protected SearchObjectsOperationResult<OBJECT> doExecuteNewSearch(final IOperationContext context,
            final SearchObjectsOperation<CRITERIA, FETCH_OPTIONS> operation)
    {
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
        final TranslationContext translationContext = new TranslationContext(context.getSession());
        final SortOptions<OBJECT> sortOptions = fetchOptions.getSortBy();

        // There results from the manager should already be filtered.
        final Set<Long> allResultsIds = getSearchManager().searchForIDs(userId, criteria, sortOptions, null, ID_COLUMN);
        final List<Long> sortedAndPagedResultIds = sortAndPage(allResultsIds, fetchOptions);
        final List<OBJECT_PE> sortedAndPagedResultPEs = getSearchManager().translate(sortedAndPagedResultIds);
        final Map<OBJECT_PE, OBJECT> sortedAndPagedResultV3DTOs = doTranslate(translationContext, sortedAndPagedResultPEs, fetchOptions);

        final List<OBJECT> finalResults = new ArrayList<>(sortedAndPagedResultV3DTOs.values());
        final List<OBJECT> sortedFinalResults = getSortedFinalResults(criteria, fetchOptions, finalResults);
        final SearchResult<OBJECT> searchResult = new SearchResult<>(sortedFinalResults, allResultsIds.size());

        return getOperationResult(searchResult);
    }

    private List<OBJECT> getSortedFinalResults(final CRITERIA criteria, final FETCH_OPTIONS fetchOptions, final List<OBJECT> finalResults)
    {
        // No paging is needed, the result should just be sorted.
        final Integer from = fetchOptions.getFrom();
        fetchOptions.from(null);
        final Integer count = fetchOptions.getCount();
        fetchOptions.count(null);
        final List<OBJECT> sortedFinalResults = new SortAndPage().sortAndPage(finalResults, criteria, fetchOptions);
        fetchOptions.from(from);
        fetchOptions.count(count);
        return sortedFinalResults;
    }

    private List<Long> sortAndPage(final Set<Long> ids, final FetchOptions fo)
    {
        final SortOptions<OBJECT> sortOptions = fo.getSortBy();
        final Set<Long> orderedIDs = (sortOptions != null) ? getSearchManager().sortIDs(ids, sortOptions) : ids;

        final List<Long> toPage = new ArrayList<>(orderedIDs);
        final Integer fromRecord = fo.getFrom();
        final Integer recordsCount = fo.getCount();
        final boolean hasPaging = fromRecord != null && recordsCount != null;
        return hasPaging ? toPage.subList(fromRecord, Math.min(fromRecord + recordsCount, toPage.size())) : toPage;
    }

}
