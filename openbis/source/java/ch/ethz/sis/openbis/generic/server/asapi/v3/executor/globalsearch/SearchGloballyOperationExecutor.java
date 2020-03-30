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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.globalsearch;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.SearchGloballyOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.SearchGloballyOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.globalsearch.IGlobalSearchObjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author pkupczyk
 */
@Component
public class SearchGloballyOperationExecutor
        extends SearchObjectsOperationExecutor<GlobalSearchObject, MatchingEntity, GlobalSearchCriteria, GlobalSearchObjectFetchOptions>
        implements ISearchGloballyOperationExecutor
{

    @Autowired
    private IGlobalSearchExecutor searchExecutor;

    @Autowired
    private IGlobalSearchObjectTranslator translator;

    @Autowired
    private IGlobalAuthorizationExecutor authorizationExecutor;

    @Autowired
    private GlobalSearchManager globalSearchManager;

    @Override
    protected SearchObjectsOperationResult<GlobalSearchObject> doExecute(IOperationContext context,
            SearchObjectsOperation<GlobalSearchCriteria, GlobalSearchObjectFetchOptions> operation)
    {
        authorizationExecutor.canSearch(context);

        final GlobalSearchCriteria criteria = operation.getCriteria();
        final GlobalSearchObjectFetchOptions fetchOptions = operation.getFetchOptions();

        if (criteria == null)
        {
            throw new IllegalArgumentException("Criteria cannot be null.");
        }
        if (fetchOptions == null)
        {
            throw new IllegalArgumentException("Fetch options cannot be null.");
        }

        final PersonPE personPE = context.getSession().tryGetPerson();
        final Set<Long> spaceIds = personPE.getAllPersonRoles().stream().filter((roleAssignmentPE) -> roleAssignmentPE.getSpace() != null)
                .map((roleAssignmentPE) -> roleAssignmentPE.getSpace().getId()).collect(Collectors.toSet());
        final Set<Long> projectIds = personPE.getAllPersonRoles().stream().filter((roleAssignmentPE) -> roleAssignmentPE.getProject() != null)
                .map((roleAssignmentPE) -> roleAssignmentPE.getProject().getId()).collect(Collectors.toSet());
        final AuthorisationInformation authorisationInformation = new AuthorisationInformation(!personPE.getRoleAssignments().isEmpty(),
                spaceIds, projectIds);

        final Long userId = context.getSession().tryGetPerson().getId();
        final TranslationContext translationContext = new TranslationContext(context.getSession());

        // There results from the manager should already be filtered.
        final Set<Long> sampleResultsIds = globalSearchManager.searchForIDs(userId, authorisationInformation, criteria, null, TableMapper.SAMPLE);
        final Set<Long> experimentResultsIds = globalSearchManager.searchForIDs(userId, authorisationInformation, criteria, null, TableMapper.EXPERIMENT);
        final Set<Long> dataSetResultsIds = globalSearchManager.searchForIDs(userId, authorisationInformation, criteria, null, TableMapper.DATA_SET);
        final Set<Long> materialResultsIds = globalSearchManager.searchForIDs(userId, authorisationInformation, criteria, null, TableMapper.MATERIAL);

        final Set<Long> allResultsIds = new HashSet<>(sampleResultsIds);
        allResultsIds.addAll(experimentResultsIds);
        allResultsIds.addAll(dataSetResultsIds);
        allResultsIds.addAll(materialResultsIds);

        final List<Long> sortedAndPagedResultIds = sortAndPage(allResultsIds, fetchOptions);
        final List<MatchingEntity> sortedAndPagedResultPEs = getSearchManager().translate(sortedAndPagedResultIds);
        final Map<MatchingEntity, GlobalSearchObject> sortedAndPagedResultV3DTOs = doTranslate(translationContext, sortedAndPagedResultPEs, fetchOptions);

        final List<GlobalSearchObject> finalResults = new ArrayList<>(sortedAndPagedResultV3DTOs.values());
        final List<GlobalSearchObject> sortedFinalResults = getSortedFinalResults(criteria, fetchOptions, finalResults);
        final SearchResult<GlobalSearchObject> searchResult = new SearchResult<>(sortedFinalResults, allResultsIds.size());

        final SearchObjectsOperationResult<GlobalSearchObject> results = getOperationResult(searchResult);
        return results;
    }

    protected List<Long> sortAndPage(final Set<Long> ids, final FetchOptions<GlobalSearchObject> fo)
    {
        final SortOptions<GlobalSearchObject> sortOptions = fo.getSortBy();
        final Set<Long> orderedIDs = (sortOptions != null) ? getSearchManager().sortIDs(ids, sortOptions) : ids;

        final List<Long> toPage = new ArrayList<>(orderedIDs);
        final Integer fromRecord = fo.getFrom();
        final Integer recordsCount = fo.getCount();
        final boolean hasPaging = fromRecord != null && recordsCount != null;
        return hasPaging ? toPage.subList(fromRecord, Math.min(fromRecord + recordsCount, toPage.size())) : toPage;
    }

    @Override
    protected Class<? extends SearchObjectsOperation<GlobalSearchCriteria, GlobalSearchObjectFetchOptions>> getOperationClass()
    {
        return SearchGloballyOperation.class;
    }

    @Override
    protected ISearchObjectExecutor<GlobalSearchCriteria, MatchingEntity> getExecutor()
    {
        return searchExecutor;
    }

    @Override
    protected ITranslator<MatchingEntity, GlobalSearchObject, GlobalSearchObjectFetchOptions> getTranslator()
    {
        return translator;
    }

    @Override
    protected ILocalSearchManager<GlobalSearchCriteria, GlobalSearchObject, MatchingEntity> getSearchManager()
    {
        throw new RuntimeException("This method is not implemented yet.");
    }

    @Override
    protected SearchObjectsOperationResult<GlobalSearchObject> getOperationResult(SearchResult<GlobalSearchObject> searchResult)
    {
        return new SearchGloballyOperationResult(searchResult);
    }

}
