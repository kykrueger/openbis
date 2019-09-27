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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;

public abstract class AbstractCompositeEntitySearchManager<CRITERIA extends AbstractCompositeSearchCriteria, OBJECT_PE>
        extends AbstractSearchManager<CRITERIA, OBJECT_PE>
{

    public AbstractCompositeEntitySearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PETranslator idsTranslator)
    {
        super(searchDAO, authProvider, idsTranslator);
    }

    /**
     * Returns what kind of entity should be searched.
     *
     * @return an entity kind.
     */
    protected abstract EntityKind getEntityKind();

    protected abstract Class<? extends AbstractCompositeSearchCriteria> getParentsSearchCriteriaClass();

    protected abstract Class<? extends AbstractCompositeSearchCriteria> getChildrenSearchCriteriaClass();

    protected abstract CRITERIA createEmptyCriteria();

    @Override
    public Set<Long> searchForIDs(final Long userId, final CRITERIA criteria)
    {
        final Class<? extends AbstractCompositeSearchCriteria> parentsSearchCriteriaClass = getParentsSearchCriteriaClass();
        final Class<? extends AbstractCompositeSearchCriteria> childrenSearchCriteriaClass = getChildrenSearchCriteriaClass();
        final List<ISearchCriteria> parentsCriteria = getCriteria(criteria, parentsSearchCriteriaClass);
        final List<ISearchCriteria> childrenCriteria = getCriteria(criteria, childrenSearchCriteriaClass);
        final List<ISearchCriteria> mainCriteria = getOtherCriteriaThan(criteria, parentsSearchCriteriaClass, childrenSearchCriteriaClass);

        Set<Long> mainCriteriaIntermediateResults = null;
        Set<Long> parentCriteriaIntermediateResults = null;
        Set<Long> childrenCriteriaIntermediateResults  = null;

        // The main criteria have no recursive ISearchCriteria into it, to facilitate building a query
        if (!mainCriteria.isEmpty())
        {
            mainCriteriaIntermediateResults = getSearchDAO().queryDBWithNonRecursiveCriteria(getEntityKind(), mainCriteria,
                    criteria.getOperator(), false);
        }

        // The parents criteria can be or not recursive, they are resolved by a recursive call
        if (!parentsCriteria.isEmpty())
        {
            final Set<Long> finalParentIds = findFinalRelationshipIds(userId, criteria.getOperator(), parentsCriteria);
            final Set<Long> finalParentIdsFiltered = filterIDsByUserRights(userId, finalParentIds);
            childrenCriteriaIntermediateResults = getChildrenIdsOf(finalParentIdsFiltered);
        }

        // The children criteria can be or not recursive, they are resolved by a recursive call
        if (!childrenCriteria.isEmpty())
        {
            final Set<Long> finalChildrenIds = findFinalRelationshipIds(userId, criteria.getOperator(), childrenCriteria);
            final Set<Long> finalChildrenIdsFiltered = filterIDsByUserRights(userId, finalChildrenIds);
            parentCriteriaIntermediateResults = getParentsIdsOf(finalChildrenIdsFiltered);
        }

        // Reaching this point we have the intermediate results of all recursive queries
        final Set<Long> resultBeforeFiltering;
        if (containsValues(mainCriteriaIntermediateResults) || containsValues(childrenCriteriaIntermediateResults) ||
                containsValues(parentCriteriaIntermediateResults))
        {
            // If we have results, we merge them
            resultBeforeFiltering = mergeResults(criteria.getOperator(),
                    Collections.singleton(mainCriteriaIntermediateResults),
                    Collections.singleton(parentCriteriaIntermediateResults),
                    Collections.singleton(childrenCriteriaIntermediateResults));
        } else if (mainCriteria.isEmpty() && parentsCriteria.isEmpty() && childrenCriteria.isEmpty())
        {
            // If we don't have results and criteria are empty, return all.
            resultBeforeFiltering = getAllIds();
        } else
        {
            // If we don't have results and criteria are not empty, there are no results.
            resultBeforeFiltering = Collections.emptySet();
        }

        return filterIDsByUserRights(userId, resultBeforeFiltering);
    }

    /**
     * Returns IDs using parent or child relationship criteria.
     *
     * @param operator the operator used to merge the results.
     * @param relatedEntitiesCriteria parent or child criteria.
     * @return IDs found from parent/child criteria.
     */
    private Set<Long> findFinalRelationshipIds(final Long userId, final SearchOperator operator,
            final List<ISearchCriteria> relatedEntitiesCriteria)
    {
        final List<Set<Long>> relatedIds =  relatedEntitiesCriteria.stream().flatMap(entitySearchCriteria -> {
            final Set<Long> foundParentIds = searchForIDs(userId, (CRITERIA) entitySearchCriteria);
            return foundParentIds.isEmpty() ? Stream.empty() : Stream.of(foundParentIds);
        }).collect(Collectors.toList());

        return mergeResults(operator, relatedIds);
    }

    /*
     * These methods require a simple SQL query to the database
     */

    /**
     * Queries the DB to return all entity IDs.
     *
     * @return set of IDs of all entities.
     */
    private Set<Long> getAllIds()
    {
        final CRITERIA criteria = createEmptyCriteria();
        return getSearchDAO().queryDBWithNonRecursiveCriteria(getEntityKind(), Collections.singletonList(criteria),
                SearchOperator.OR, false);
    }

    private Set<Long> getChildrenIdsOf(final Set<Long> parentIdSet)
    {
        try
        {
            return getSearchDAO().findChildIDs(getEntityKind(), parentIdSet);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private Set<Long> getParentsIdsOf(final Set<Long> childIdSet)
    {
        try
        {
            return getSearchDAO().findParentIDs(getEntityKind(), childIdSet);
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
