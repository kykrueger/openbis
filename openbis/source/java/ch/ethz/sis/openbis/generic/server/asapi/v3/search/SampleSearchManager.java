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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleChildrenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sort.ISortAndPage;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.SpaceProjectIDsVO;

import java.util.*;

/**
 * Manages detailed search with complex sample search criteria.
 * 
 * @author Viktor Kovtun
 * @author Juan Fuentes
 */
public class SampleSearchManager extends AbstractSearchManager<SampleSearchCriteria>
{

    public SampleSearchManager(ISQLSearchDAO searchDAO, ISortAndPage sortAndPage)
    {
        super(searchDAO, sortAndPage);
    }

    @Override
    public Set<Long> searchForIDs(final SampleSearchCriteria criteria)
    {
        final List<ISearchCriteria> parentsCriteria = getCriteria(criteria, SampleParentsSearchCriteria.class);
        final List<ISearchCriteria> childrenCriteria = getCriteria(criteria, SampleChildrenSearchCriteria.class);
        final List<ISearchCriteria> mainCriteria = getOtherCriteriaThan(criteria, SampleParentsSearchCriteria.class,
                SampleChildrenSearchCriteria.class);

        Set<Long> mainCriteriaIntermediateResults = null;
        Set<Long> parentCriteriaIntermediateResults = null;
        Set<Long> childrenCriteriaIntermediateResults  = null;

        // The main criteria have no recursive ISearchCriteria into it, to facilitate building a query
        if (!mainCriteria.isEmpty())
        {
            // TODO: implement.
            mainCriteriaIntermediateResults = searchDAO.queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE, mainCriteria,
                    criteria.getOperator());
        }

        // The parents criteria can be or not recursive, they are resolved by a recursive call
        if (!parentsCriteria.isEmpty())
        {
            final Set<Long> finalParentIds = findFinalRelationshipIds(criteria.getOperator(), parentsCriteria);
            childrenCriteriaIntermediateResults = getChildrenIdsOf(finalParentIds);
        }

        // The children criteria can be or not recursive, they are resolved by a recursive call
        if (!childrenCriteria.isEmpty())
        {
            final Set<Long> finalChildrenIds = findFinalRelationshipIds(criteria.getOperator(), childrenCriteria);
            parentCriteriaIntermediateResults = getParentsIdsOf(finalChildrenIds);
        }

        // Reaching this point we have the intermediate results of all recursive queries
        final Set<Long> finalResult;
        if (containsValues(mainCriteriaIntermediateResults) || containsValues(childrenCriteriaIntermediateResults) ||
                containsValues(parentCriteriaIntermediateResults))
        { // If we have results, we merge them
            finalResult = mergeResults(criteria.getOperator(),
                    Collections.singleton(mainCriteriaIntermediateResults),
                    Collections.singleton(parentCriteriaIntermediateResults),
                    Collections.singleton(childrenCriteriaIntermediateResults));
        } else if (mainCriteria.isEmpty() && parentsCriteria.isEmpty() && childrenCriteria.isEmpty())
        { // If we don't have results and criteria are empty, return all.
            finalResult = getAllIds();
        } else
        { // If we don't have results and criteria are not empty, there is no results.
            finalResult = Collections.emptySet();
        }

        return finalResult;
    }

    /**
     * Checks whether a collection contains any values.
     *
     * @param collection collection to be checked for values.
     * @return {@code false} if collection is {@code null} or empty, true otherwise.
     */
    private static boolean containsValues(final Collection<?> collection)
    {
        return collection != null && !collection.isEmpty();
    }

    @Override
    public Set<Long> filterIDsByUserRights(final Long userId, final Set<Long> ids)
    {
        final SpaceProjectIDsVO authorizedSpaceProjectIds = searchDAO.findAuthorisedSpaceProjectIDs(userId);
        final Set<Long> filteredIds = searchDAO.filterSampleIDsBySpaceAndProjectIDs(ids, authorizedSpaceProjectIds);
        return filteredIds;
    }

    @Override
    public List<Long> sortAndPage(final Set<Long> ids, final SampleSearchCriteria criteria,
            final FetchOptions<?> fetchOptions)
    {
        return sortAndPage.sortAndPage(new ArrayList<>(ids), criteria, fetchOptions);
    }

    /**
     * Returns IDs using parent or child relationship criteria.
     *
     * @param operator the operator used to merge the results.
     * @param relatedEntitiesCriteria parent or child criteria.
     * @return IDs found from parent/child criteria.
     */
    private Set<Long> findFinalRelationshipIds(final SearchOperator operator,
            final List<ISearchCriteria> relatedEntitiesCriteria)
    {
        final List<Set<Long>> relatedIds = new ArrayList<>();
        for (ISearchCriteria sampleSearchCriteria : relatedEntitiesCriteria)
        {
            Set<Long> foundParentIds = searchForIDs((SampleSearchCriteria) sampleSearchCriteria);
            if (!foundParentIds.isEmpty())
            {
                relatedIds.add(foundParentIds);
            }
        }
        return mergeResults(operator, relatedIds);
    }

    /*
     * These methods require a simple SQL query to the database
     */

    /**
     * Queries the DB to return all sample IDs.
     *
     * @return set of IDs of all samples.
     */
    private Set<Long> getAllIds()
    {
        final SampleSearchCriteria criteria = new SampleSearchCriteria();
        criteria.withCode().thatContains("");

        return searchDAO.queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE, Collections.singletonList(criteria),
                SearchOperator.OR);
    }

    private Set<Long> getChildrenIdsOf(final Set<Long> parentIdSet)
    {
        return searchDAO.findChildIDs(EntityKind.SAMPLE, parentIdSet);
    }

    private Set<Long> getParentsIdsOf(final Set<Long> childIdSet)
    {
        return searchDAO.findParentIDs(EntityKind.SAMPLE, childIdSet);
    }

}
