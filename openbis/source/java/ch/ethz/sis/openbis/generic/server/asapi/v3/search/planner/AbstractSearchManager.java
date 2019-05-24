/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ISortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages detailed search with complex search criteria.
 *
 * @author Viktor Kovtun
 * @author Juan Fuentes
 */
public abstract class AbstractSearchManager<CRITERIA extends ISearchCriteria, ENTITY> implements ISearchManager<CRITERIA>
{
    private final ISQLSearchDAO searchDAO;

    private final ISQLAuthorisationInformationProviderDAO authProvider;

    private final ISortAndPage sortAndPage;

    public AbstractSearchManager(final ISQLSearchDAO searchDAO,
            ISQLAuthorisationInformationProviderDAO authProvider, final ISortAndPage sortAndPage)
    {
        this.searchDAO = searchDAO;
        this.authProvider = authProvider;
        this.sortAndPage = sortAndPage;
    }

    /**
     * Filters sample IDs set leaving the ones to which the user has access.
     *
     * @param userId the ID of the user.
     * @param ids IDs to filter.
     * @return IDs of samples which the user is authorised to access.
     */
    public abstract Set<Long> filterIDsByUserRights(final Long userId, final Set<Long> ids);

    /**
     * Sorts IDs using certain sort options.
     *
     * @param ids IDs of entities to sort.
     * @param fetchOptions sorting options.
     * @return ids sorted by the specified options.
     */
    public abstract List<Long> sortAndPage(final Set<Long> ids, final CRITERIA criteria, final FetchOptions<ENTITY> fetchOptions);

    protected List<ISearchCriteria> getOtherCriteriaThan(AbstractCompositeSearchCriteria compositeSearchCriteria,
            Class<? extends ISearchCriteria>... classes)
    {
        final List<ISearchCriteria> criteria = compositeSearchCriteria.getCriteria().stream().filter(criterion -> {
            final boolean isInstanceOfOneOfClasses = Arrays.stream(classes).anyMatch(clazz -> clazz.isInstance(criterion));
            return !isInstanceOfOneOfClasses;
        }).collect(Collectors.toList());

        return criteria;
    }

    protected List<ISearchCriteria> getCriteria(
            AbstractCompositeSearchCriteria compositeSearchCriteria, Class<? extends ISearchCriteria> clazz)
    {
        final List<ISearchCriteria> criteria = compositeSearchCriteria.getCriteria().stream().filter(clazz::isInstance)
                .collect(Collectors.toList());
        return criteria;
    }

    protected static <E> Set<E> mergeResults(final SearchOperator operator,
            final Collection<Set<E>>... intermediateResultsToMerge)
    {
        final Collection<Set<E>> intermediateResults = Arrays.stream(intermediateResultsToMerge).reduce(new ArrayList<>(), (sets, sets2) ->
                {
                    if (sets2 != null)
                    {
                        sets.addAll(sets2);
                    }
                    return sets;
                });

        switch (operator)
        {
            case AND:
                return intersection(intermediateResults);
            case OR:
                return union(intermediateResults);
            default:
                throw new IllegalArgumentException("Unexpected value for search operator: " + operator);
        }
    }

    protected static <E> Set<E> intersection(final Collection<Set<E>> sets)
    {
        return !sets.isEmpty() ? sets.stream().reduce(new HashSet<>(sets.iterator().next()), (set1, set2) ->
                {
                    if (set2 != null)
                    {
                        set1.retainAll(set2);
                    }
                    return set1;
                }) : new HashSet<>(0);
    }

    protected static <E> Set<E> union(final Collection<Set<E>> sets)
    {
        return sets.stream().reduce(new HashSet<>(), (set1, set2) ->
                {
                    if (set2 != null)
                    {
                        set1.addAll(set2);
                    }
                    return set1;
                });
    }

    /**
     * Find the smallest set.
     *
     * @param candidates collection of sets to search in.
     * @param <E> types of parameters of the sets.
     * @return the set with the smallest number of items.
     */
    protected static <E> Set<E> getSmallestSet(final Collection<Set<E>> candidates)
    {
        final Set<E> smallestSet = candidates.stream().min((o1, o2) ->
                {
                    if (o1 == null)
                    {
                        return (o2 == null) ? 0 : 1;
                    } else
                    {
                        return (o2 == null) ? -1 : o1.size() - o2.size();
                    }
                }).orElse(null);
        return smallestSet;
    }

    protected ISQLSearchDAO getSearchDAO()
    {
        return searchDAO;
    }

    protected ISQLAuthorisationInformationProviderDAO getAuthProvider()
    {
        return authProvider;
    }

    protected ISortAndPage getSortAndPage()
    {
        return sortAndPage;
    }

}
