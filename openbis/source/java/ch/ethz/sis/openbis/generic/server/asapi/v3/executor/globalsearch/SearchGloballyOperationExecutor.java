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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOrder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions;
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
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.person.IPersonTranslator;
import ch.systemsx.cisd.openbis.generic.shared.authorization.AuthorizationConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions.SCORE;

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
    private IPersonTranslator personTranslator;

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
                    authorisationInformation, criteria, null, TableMapper.SAMPLE, fetchOptions.hasMatch());
            allResultMaps.addAll(sampleResultMaps);
        }

        if (objectKinds.contains(GlobalSearchObjectKind.EXPERIMENT))
        {
            final Set<Map<String, Object>> experimentResultMaps = globalSearchManager.searchForIDs(userId,
                    authorisationInformation, criteria, null, TableMapper.EXPERIMENT, fetchOptions.hasMatch());
            allResultMaps.addAll(experimentResultMaps);
        }

        if (objectKinds.contains(GlobalSearchObjectKind.DATA_SET))
        {
            final Set<Map<String, Object>> dataSetResultMaps = globalSearchManager.searchForIDs(userId,
                    authorisationInformation, criteria, null, TableMapper.DATA_SET, fetchOptions.hasMatch());
            allResultMaps.addAll(dataSetResultMaps);
        }

        if (objectKinds.contains(GlobalSearchObjectKind.MATERIAL))
        {
            final Set<Map<String, Object>> materialResultMaps = globalSearchManager.searchForIDs(userId,
                    authorisationInformation, criteria, null, TableMapper.MATERIAL, fetchOptions.hasMatch());
            allResultMaps.addAll(materialResultMaps);
        }

        final Collection<MatchingEntity> matchingEntities = globalSearchManager.map(allResultMaps,
                fetchOptions.hasMatch());
        final List<MatchingEntity> pagedMatchingEntities = sortAndPage(matchingEntities, fetchOptions);
        // TODO: doTranslate() should only filter nested objects of the results (parents, children, components...).

        //
        // Path/Hack: doTranslate only works with entities of one kind - START
        //

        final Map<EntityKind, List<MatchingEntity>> pagedMatchingEntitiesByKind = new HashMap<>();
        for (MatchingEntity matchingEntity:pagedMatchingEntities) {
            List<MatchingEntity> pagedMatchingEntitiesOfType = pagedMatchingEntitiesByKind.get(matchingEntity.getEntityKind());
            if (pagedMatchingEntitiesOfType == null) {
                pagedMatchingEntitiesOfType = new ArrayList<>(pagedMatchingEntities.size());
                pagedMatchingEntitiesByKind.put(matchingEntity.getEntityKind(), pagedMatchingEntitiesOfType);
            }
            pagedMatchingEntitiesOfType.add(matchingEntity);
        }

        final Map<String, GlobalSearchObject> pagedResultV3DTOs = new HashMap<>();

        for (List<MatchingEntity> pagedMatchingEntitiesOfType:pagedMatchingEntitiesByKind.values()) {
            final Map<MatchingEntity, GlobalSearchObject> pagedResultV3DTOsForType = doTranslate(translationContext, pagedMatchingEntitiesOfType, fetchOptions);
            translationContext.getTranslationCache().clear();
            for (MatchingEntity matchingEntity:pagedResultV3DTOsForType.keySet()) {
                pagedResultV3DTOs.put(matchingEntity.getPermId(), pagedResultV3DTOsForType.get(matchingEntity));
            }
        }

        //
        // Path/Hack: doTranslate only works with entities of one kind - END
        //

        assert pagedMatchingEntities.size() == pagedResultV3DTOs.size() : "The number of results after translation should not change. " +
                "[pagedResultPEs.size()=" + pagedMatchingEntities.size() + ", pagedResultV3DTOs.size()=" + pagedResultV3DTOs.size() + "]";

        // Reordering of pagedResultV3DTOs is needed because translation mixes the order
        final List<GlobalSearchObject> objectResults = new ArrayList<>(pagedMatchingEntities.size());
        for (MatchingEntity pagedMatchingEntity:pagedMatchingEntities) {
            objectResults.add(pagedResultV3DTOs.get(pagedMatchingEntity.getPermId()));
        }
        // Sorting and paging parents and children in a "conventional" way.
        new SortAndPage().nest(objectResults, criteria, fetchOptions);

        final SearchResult<GlobalSearchObject> searchResult =
                new SearchResult<>(objectResults, matchingEntities.size());
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

    protected List<MatchingEntity> sortAndPage(final Collection<MatchingEntity> results, final FetchOptions<GlobalSearchObject> fo)
    {
        final SortOptions<GlobalSearchObject> sortOptions;
        if (fo.getSortBy() != null)
        {
            sortOptions = fo.getSortBy();
        } else
        {
            final GlobalSearchObjectSortOptions defaultSortOptions = new GlobalSearchObjectSortOptions();
            final SortOrder sortOrder = new SortOrder();
            sortOrder.desc();
            defaultSortOptions.getSortings().add(new Sorting(SCORE, sortOrder));
            sortOptions = defaultSortOptions;
        }

        final List<MatchingEntity> sortedResults = globalSearchManager.sortRecords(results, sortOptions);

        final Integer fromRecord = fo.getFrom();
        final Integer recordsCount = fo.getCount();
        final boolean hasPaging = fromRecord != null && recordsCount != null;
        return hasPaging ? sortedResults.subList(fromRecord, Math.min(fromRecord + recordsCount, results.size()))
                : sortedResults;
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
