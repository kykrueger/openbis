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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.SearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.GlobalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.globalsearch.IGlobalSearchObjectTranslator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private AuthorizationConfig authorizationConfig;

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
        final AuthorisationInformation authorisationInformation = AuthorisationInformation.getInstance(personPE,
                authorizationConfig);

        final Long userId = context.getSession().tryGetPerson().getId();
        final TranslationContext translationContext = new TranslationContext(context.getSession());

        final Set<GlobalSearchObjectKind> objectKinds = getObjectKinds(criteria);
        final Set<Map<String, Object>> allResultMaps = new HashSet<>();

        // There results from the manager should already be filtered.
        if (objectKinds.contains(GlobalSearchObjectKind.SAMPLE))
        {
            final Set<Map<String, Object>> sampleResultMaps = globalSearchManager.searchForIDs(userId,
                    authorisationInformation, criteria, null, TableMapper.SAMPLE, fetchOptions);
            allResultMaps.addAll(sampleResultMaps);
        }

        if (objectKinds.contains(GlobalSearchObjectKind.EXPERIMENT))
        {
            final Set<Map<String, Object>> experimentResultMaps = globalSearchManager.searchForIDs(userId,
                    authorisationInformation, criteria, null, TableMapper.EXPERIMENT, fetchOptions);
            allResultMaps.addAll(experimentResultMaps);
        }

        if (objectKinds.contains(GlobalSearchObjectKind.DATA_SET))
        {
            final Set<Map<String, Object>> dataSetResultMaps = globalSearchManager.searchForIDs(userId,
                    authorisationInformation, criteria, null, TableMapper.DATA_SET, fetchOptions);
            allResultMaps.addAll(dataSetResultMaps);
        }

        if (objectKinds.contains(GlobalSearchObjectKind.MATERIAL))
        {
            final Set<Map<String, Object>> materialResultMaps = globalSearchManager.searchForIDs(userId,
                    authorisationInformation, criteria, null, TableMapper.MATERIAL, fetchOptions);
            allResultMaps.addAll(materialResultMaps);
        }

        final List<Map<String, Object>> sortedResultMaps = sort(allResultMaps, fetchOptions.getSortBy());

        final Collection<MatchingEntity> matchingEntities = globalSearchManager.map(sortedResultMaps,
                fetchOptions.hasMatch());
        final List<MatchingEntity> pagedMatchingEntities = page(new ArrayList<>(matchingEntities), fetchOptions);
        // TODO: doTranslate() should only filter nested objects of the results (parents, children, components...).
        final Map<MatchingEntity, GlobalSearchObject> pagedResultV3DTOs = doTranslate(translationContext, pagedMatchingEntities, fetchOptions);

        assert pagedMatchingEntities.size() == pagedResultV3DTOs.size() : "The number of results after translation should not change. " +
                "[pagedResultPEs.size()=" + pagedMatchingEntities.size() + ", pagedResultV3DTOs.size()=" + pagedResultV3DTOs.size() + "]";

        // Reordering of pagedResultV3DTOs is needed because translation mixes the order
        final List<GlobalSearchObject> objectResults = pagedMatchingEntities.stream().map(pagedResultV3DTOs::get)
                .collect(Collectors.toList());

        // Sorting and paging parents and children in a "conventional" way.
        new SortAndPage().nest(objectResults, criteria, fetchOptions);

        final SearchResult<GlobalSearchObject> searchResult = new SearchResult<>(objectResults, allResultMaps.size());
        return getOperationResult(searchResult);
    }

    private static Set<GlobalSearchObjectKind> getObjectKinds(final GlobalSearchCriteria globalSearchCriteria)
    {
        final Stream<GlobalSearchObjectKindCriteria> objectKindCriteriaStream = globalSearchCriteria.getCriteria()
                .stream().filter((criterion) -> criterion instanceof GlobalSearchObjectKindCriteria)
                .map((criterion) -> (GlobalSearchObjectKindCriteria) criterion);
        final Set<GlobalSearchObjectKind> objectKinds;
        switch (globalSearchCriteria.getOperator())
        {
            case OR:
            {
                objectKinds = EnumSet.noneOf(GlobalSearchObjectKind.class);
                objectKindCriteriaStream.forEach(
                        (objectKindCriterion) -> objectKinds.addAll(objectKindCriterion.getObjectKinds()));
                break;
            }
            case AND:
            {
                objectKinds = EnumSet.allOf(GlobalSearchObjectKind.class);
                objectKindCriteriaStream.forEach(
                        (objectKindCriterion) -> objectKinds.retainAll(objectKindCriterion.getObjectKinds()));
                break;
            }
            default:
            {
                throw new RuntimeException();
            }
        }

        return objectKinds.isEmpty() ? EnumSet.allOf(GlobalSearchObjectKind.class) : objectKinds;
    }

    protected List<Map<String, Object>> sort(final Set<Map<String, Object>> results,
            final SortOptions<GlobalSearchObject> sortOptions)
    {
        return (sortOptions != null)
                ? globalSearchManager.sortRecords(results, sortOptions)
                : new ArrayList<>(results);
    }

    protected List<MatchingEntity> page(final List<MatchingEntity> results, final FetchOptions<GlobalSearchObject> fo)
    {
        final Integer fromRecord = fo.getFrom();
        final Integer recordsCount = fo.getCount();
        final boolean hasPaging = fromRecord != null && recordsCount != null;
        return hasPaging ? results.subList(fromRecord, Math.min(fromRecord + recordsCount, results.size())) : results;
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
        throw new IllegalStateException("This method should not be executed. globalSearchManager should be used instead.");
    }

    @Override
    protected SearchObjectsOperationResult<GlobalSearchObject> getOperationResult(SearchResult<GlobalSearchObject> searchResult)
    {
        return new SearchGloballyOperationResult(searchResult);
    }

}
