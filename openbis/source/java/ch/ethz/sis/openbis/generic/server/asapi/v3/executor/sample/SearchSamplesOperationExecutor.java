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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.sample;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SearchSamplesOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ISortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.PostgresAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.PostgresSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.SampleSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.HibernateSQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.sample.ISampleTranslator;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author pkupczyk
 */
@Component
public class SearchSamplesOperationExecutor extends SearchObjectsOperationExecutor<Sample, Long, SampleSearchCriteria, SampleFetchOptions>
        implements ISearchSamplesOperationExecutor
{

    @Autowired
    private ISearchSampleExecutor searchExecutor;

    @Autowired
    private ISampleTranslator translator;

    @Autowired
    protected IDAOFactory daoFactory;

    @Override
    protected Class<? extends SearchObjectsOperation<SampleSearchCriteria, SampleFetchOptions>> getOperationClass()
    {
        return SearchSamplesOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<SampleSearchCriteria, Long> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<Long, Sample, SampleFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected SearchObjectsOperationResult<Sample> getOperationResult(SearchResult<Sample> searchResult)
    {
        return new SearchSamplesOperationResult(searchResult);
    }

    @Override
    protected SearchObjectsOperationResult<Sample> doExecute(final IOperationContext context,
            final SearchObjectsOperation<SampleSearchCriteria, SampleFetchOptions> operation)
    {
        final SampleSearchCriteria criteria = operation.getCriteria();
        final SampleFetchOptions fetchOptions = operation.getFetchOptions();

        if (criteria == null)
        {
            throw new IllegalArgumentException("Criteria cannot be null.");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null.");
        }

        final ISQLExecutor sqlExecutor = new HibernateSQLExecutor(daoFactory.getSessionFactory().getCurrentSession());
        final Long userId = context.getSession().tryGetPerson().getId();
        final ISortAndPage sortAndPage = new ISortAndPage()
        {
            @Override
            public <T, C extends Collection<T>> C sortAndPage(final C objects, final ISearchCriteria c, final FetchOptions fo)
            {
                List<T> toPage = new ArrayList<>(objects);
                return (C) toPage.subList(fo.getFrom(), Math.min(fo.getFrom() + fo.getCount(), toPage.size()));
            }

        };
        final TranslationContext translationContext = new TranslationContext(context.getSession());

        final PostgresSearchDAO searchDAO = new PostgresSearchDAO(sqlExecutor);
        final PostgresAuthorisationInformationProviderDAO informationProviderDAO = new PostgresAuthorisationInformationProviderDAO(sqlExecutor);
        final SampleSearchManager sampleSearchManager = new SampleSearchManager(searchDAO, informationProviderDAO);

        final Set<Long> allResultsIds = sampleSearchManager.searchForIDs(userId, criteria);
        final Set<Long> filteredResults = sampleSearchManager.filterIDsByUserRights(userId, allResultsIds);
        final Collection<Long> sortedAndPagedResults = sortAndPage.sortAndPage(filteredResults, criteria, fetchOptions);

        final Map<Long, Sample> translated = translator.translate(translationContext, sortedAndPagedResults, fetchOptions);

        final List<Sample> finalResults = new ArrayList<>(sortedAndPagedResults.size());
        for (Long sortedAndPagedResult : sortedAndPagedResults) {
            finalResults.add(translated.get(sortedAndPagedResult));
        }

        final SearchResult<Sample> searchResult = new SearchResult<>(finalResults, allResultsIds.size());
        return getOperationResult(searchResult);
    }

}
