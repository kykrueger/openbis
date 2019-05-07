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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleChildrenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;

import java.util.*;

/**
 * Manages detailed search with complex sample search criteria.
 * 
 * @author Viktor Kovtun
 */
public class SampleSearchManager extends AbstractSearchManager<ISampleLister>
{

    private final IRelationshipHandler PARENT_RELATIONSHIP_HANDLER = new IRelationshipHandler<SampleSearchCriteria>()
        {

            @Override
            public Collection<Long> findRelatedIdsByCriteria(final String userId, final SampleSearchCriteria criterion,
                    final List<ISearchCriteria> otherSubCriteria)
            {
                return findSampleIds(userId, criterion, otherSubCriteria);
            }

            @Override
            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> sampleIds)
            {
                return lister.getChildToParentsIdsMap(sampleIds);
            }

            @Override
            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> parentSampleIds)
            {
                return lister.getParentToChildrenIdsMap(parentSampleIds);
            }

        };

    private final IRelationshipHandler CHILDREN_RELATIONSHIP_HANDLER = new IRelationshipHandler<SampleSearchCriteria>()
        {

            @Override
            public Collection<Long> findRelatedIdsByCriteria(final String userId, final SampleSearchCriteria criterion,
                    final List<ISearchCriteria> otherSubCriteria)
            {
                return findSampleIds(userId, criterion, otherSubCriteria);
            }

            @Override
            public Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> sampleIds)
            {
                return lister.getParentToChildrenIdsMap(sampleIds);
            }

            @Override
            public Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> childrenSampleIds)
            {
                return lister.getChildToParentsIdsMap(childrenSampleIds);
            }

        };

    public SampleSearchManager(ISQLSearchDAO searchDAO, ISampleLister sampleLister)
    {
        super(searchDAO, sampleLister);
    }

    public Collection<Long> searchForSampleIDs(final String userId, final SampleSearchCriteria criterion)
    {
        final List<SampleParentsSearchCriteria> parentCriteria = new ArrayList<>();
        final List<SampleChildrenSearchCriteria> childCriteria = new ArrayList<>();
        final List<ISearchCriteria> mainCriteria = new ArrayList<>();

        groupSampleCriteria(criterion.getCriteria(), parentCriteria, childCriteria, mainCriteria);

        final boolean hasMainCriteria = !criterion.getCriteria().isEmpty() || !mainCriteria.isEmpty();
        final boolean hasParentCriteria = !parentCriteria.isEmpty();
        final boolean hasChildCriteria = !childCriteria.isEmpty();

        Collection<Long> sampleIds = null;
        if (hasMainCriteria || (!hasParentCriteria && !hasChildCriteria))
        {
            sampleIds = findSampleIds(userId, criterion, mainCriteria);
            if (sampleIds == null)
            {
                sampleIds = Collections.emptyList();
            }
        }

        if (hasParentCriteria)
        {
            // At most one criterion can be in parentCriteria.
            sampleIds = filterSearchResultsByCriteria(userId, sampleIds, parentCriteria.get(0),
                    PARENT_RELATIONSHIP_HANDLER);
        }

        if (hasChildCriteria)
        {
            // At most one criterion can be in childCriteria.
            sampleIds = filterSearchResultsByCriteria(userId, sampleIds, childCriteria.get(0),
                    CHILDREN_RELATIONSHIP_HANDLER);
        }

        return restrictResultSetIfNecessary(sampleIds);
    }

    private void groupSampleCriteria(final Collection<ISearchCriteria> allCriteria,
            final Collection<SampleParentsSearchCriteria> parentCriteria,
            final Collection<SampleChildrenSearchCriteria> childCriteria,
            final Collection<ISearchCriteria> otherCriteria)
    {
        parentCriteria.clear();
        childCriteria.clear();
        otherCriteria.clear();
        for (ISearchCriteria subCriterion : allCriteria)
        {
            if (subCriterion instanceof SampleParentsSearchCriteria)
            {
                parentCriteria.add((SampleParentsSearchCriteria) subCriterion);
            } else if (subCriterion instanceof SampleChildrenSearchCriteria)
            {
                childCriteria.add((SampleChildrenSearchCriteria) subCriterion);
            } else
            {
                otherCriteria.add(subCriterion);
            }
        }

        assert parentCriteria.size() <= 1 : "There should be at most one parent criterion.";
        assert childCriteria.size() <= 1 : "There should be at most one child criterion.";
    }

    private List<Long> findSampleIds(final String userId, final SampleSearchCriteria criterion,
            final List<ISearchCriteria> subCriteria)
    {
        // for now we connect all sub criteria with logical AND
        final List<IAssociationCriteria> associations = new ArrayList<>();
        for (final ISearchCriteria subCriterion : subCriteria)
        {
            // TODO: rewrite method findAssociatedEntities().
            associations.add(findAssociatedEntities(userId, subCriterion));
        }

        if (subCriteria.isEmpty() && criterion.getCriteria().isEmpty())
        {
            // if no criteria were provided find all samples
            criterion.withCode().thatContains("");
        }
        final List<Long> sampleIds = searchDAO.searchForEntityIds(userId, criterion, EntityKind.SAMPLE, associations);
        return sampleIds;
    }

}
