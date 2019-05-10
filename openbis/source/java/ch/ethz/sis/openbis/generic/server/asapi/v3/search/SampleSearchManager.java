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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleChildrenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.NewSQLQueryTranslator.queryDBWithNonRecursiveCriteria;

/**
 * Manages detailed search with complex sample search criteria.
 * 
 * @author Viktor Kovtun
 * @author Juan Fuentes
 */
public class SampleSearchManager extends AbstractSearchManager<ISampleLister>
{

    public SampleSearchManager(final ISQLSearchDAO searchDAO, final ISampleLister sampleLister)
    {
        super(searchDAO, sampleLister);
    }

    public Set<Long> searchForSampleIDs(final SampleSearchCriteria criteria)
    {
        List<ISearchCriteria> parentsCriteria = getCriteria(criteria, SampleParentsSearchCriteria.class);
        List<ISearchCriteria> childrenCriteria = getCriteria(criteria, SampleChildrenSearchCriteria.class);
        List<ISearchCriteria> mainCriteria = getOtherCriteriaThan(criteria, SampleParentsSearchCriteria.class,
                SampleChildrenSearchCriteria.class);

        Set<Long> mainCriteriaIntermediateResults = Collections.emptySet();
        Set<Long> parentCriteriaIntermediateResults = Collections.emptySet();
        Set<Long> childrenCriteriaIntermediateResults  = Collections.emptySet();

        // The main criteria have no recursive ISearchCriteria into it, to facilitate building a query
        if (!mainCriteria.isEmpty())
        {
            mainCriteriaIntermediateResults = queryDBWithNonRecursiveCriteria(EntityKind.SAMPLE, mainCriteria,
                    criteria.getOperator());
        }

        // The parents criterias can be or not recursive, they are resolved by a recursive call
        if (!parentsCriteria.isEmpty()) {
            final Set<Long> finalParentIds = findFinalRelationshipIds(criteria.getOperator(), parentsCriteria);
            parentCriteriaIntermediateResults = getChildrenIdsOf(finalParentIds);
        }

        // The children criterias can be or not recursive, they are resolved by a recursive call
        if (!childrenCriteria.isEmpty()) {
            final Set<Long> finalChildrenIds = findFinalRelationshipIds(criteria.getOperator(), childrenCriteria);
            parentCriteriaIntermediateResults = getParentsIdsOf(finalChildrenIds);
        }

        // Reaching this point we have the intermediate results of all recursive queries
        Set<Long> finalResult = null;
        if (!mainCriteriaIntermediateResults.isEmpty() ||
                !childrenCriteriaIntermediateResults.isEmpty() ||
                !parentCriteriaIntermediateResults.isEmpty()) { // If we have results, we merge them
            finalResult = mergeResults(criteria.getOperator(),
                    Collections.singleton(mainCriteriaIntermediateResults),
                    Collections.singleton(parentCriteriaIntermediateResults),
                    Collections.singleton(childrenCriteriaIntermediateResults));
        } else if ( mainCriteria.isEmpty() &&
                parentsCriteria.isEmpty() &&
                childrenCriteria.isEmpty()) { // If we don't have results and criterias are empty, return all.
            finalResult = getAllIds();
        } else { // If we don't have results and criterias are not empty, there is no results.
            finalResult = Collections.emptySet();
        }

        return finalResult;
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
        for (ISearchCriteria sampleSearchCriteria : relatedEntitiesCriteria) {
            Set<Long> foundParentIds = searchForSampleIDs((SampleSearchCriteria) sampleSearchCriteria);
            if (!foundParentIds.isEmpty()) {
                relatedIds.add(foundParentIds);
            }
        }
        return mergeResults(operator, relatedIds);
    }

    /*
     * These methods require a simple SQL query to the database
     */
    private static Set<Long> getAllIds() {
        throw new UnsupportedOperationException();
    }

    private static Set<Long> getChildrenIdsOf(final Set<Long> parents) {
        throw new UnsupportedOperationException();
    }

    private static Set<Long> getParentsIdsOf(final Set<Long> children) {
        throw new UnsupportedOperationException();
    }

}
