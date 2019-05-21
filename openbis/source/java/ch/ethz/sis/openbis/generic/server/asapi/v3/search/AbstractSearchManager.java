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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ISortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages detailed search with complex search criteria.
 *
 * @param <C> type search criteria to be used.
 *
 * @author Viktor Kovtun
 * @author Juan Fuentes
 */
public abstract class AbstractSearchManager<C extends ISearchCriteria> implements ISearchManager<C>
{
    protected final ISQLSearchDAO searchDAO;

    protected final ISortAndPage sortAndPage;

    public AbstractSearchManager(final ISQLSearchDAO searchDAO, final ISortAndPage sortAndPage)
    {
        this.searchDAO = searchDAO;
        this.sortAndPage = sortAndPage;
    }

    protected List<ISearchCriteria> getOtherCriteriaThan(AbstractCompositeSearchCriteria compositeSearchCriteria,
            Class<? extends ISearchCriteria>... classes)
    {
        List<ISearchCriteria> criterias = new ArrayList<>();
        for (ISearchCriteria criteria : compositeSearchCriteria.getCriteria())
        {
            boolean isInstanceOfOneOf = false;
            for (Class<? extends ISearchCriteria> clazz : classes)
            {
                if (clazz.isInstance(criteria))
                {
                    isInstanceOfOneOf = true;
                    break;
                }
            }
            if (false == isInstanceOfOneOf)
            {
                criterias.add(criteria);
            }
        }
        return criterias;
    }

    protected List<ISearchCriteria> getCriteria(
            AbstractCompositeSearchCriteria compositeSearchCriteria, Class<? extends ISearchCriteria> clazz)
    {
        List<ISearchCriteria> criteria = new ArrayList<>();
        for (ISearchCriteria criterion : compositeSearchCriteria.getCriteria())
        {
            if (clazz.isInstance(criterion))
            {
                criteria.add(criterion);
            }
        }
        return criteria;
    }

    protected static <E> Set<E> mergeResults(final SearchOperator operator,
            final Collection<Set<E>>... intermediateResultsToMerge)
    {
        final Collection<Set<E>> intermediateResults = new ArrayList();
        for (final Collection<Set<E>> intermediateResultToMerge : intermediateResultsToMerge)
        {
            if (intermediateResultToMerge != null)
            {
                intermediateResults.addAll(intermediateResultToMerge);
            }
        }

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
        final Set<E> pivot = getSmallestSet(sets);
        sets.remove(pivot);
        for (final Set<E> intermediateResult : sets)
        {
            if (intermediateResult != null)
            {
                pivot.retainAll(intermediateResult);
            }
        }
        return pivot;
    }

    protected static <E> Set<E> union(final Collection<Set<E>> sets)
    {
        final Set<E> all = new HashSet<>();
        for (final Set<E> intermediateResult : sets)
        {
            if (intermediateResult != null)
            {
                all.addAll(intermediateResult);
            }
        }
        return all;
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
        Set<E> smallest = null;
        for (final Set<E> candidate : candidates)
        {
            if (candidate != null && (smallest == null || smallest.size() > candidate.size()))
            {
                smallest = candidate;
            }
        }
        return smallest;
    }

}
