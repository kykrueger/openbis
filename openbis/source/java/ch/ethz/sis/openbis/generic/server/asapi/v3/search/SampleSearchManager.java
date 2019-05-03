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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleChildrenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;
import org.springframework.dao.DataAccessException;

import java.util.*;

/**
 * Manages detailed search with complex sample search criteria.
 * 
 * @author Viktor Kovtun
 */
public class SampleSearchManager extends AbstractSearchManager<ISampleLister>
{
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

    public SampleSearchManager(ISQLSearchDAO searchDAO, ISampleLister sampleLister)
    {
        super(searchDAO, sampleLister);
    }

    public List<Sample> searchForSamples(String userId, SampleSearchCriteria criteria)
            throws DataAccessException
    {
        return lister.list(new ListOrSearchSampleCriteria(searchForSampleIDs(userId, criteria)));
    }

    public Collection<Long> searchForSampleIDs(final String userId, final SampleSearchCriteria criterion)
    {
        final List<ISearchCriteria> parentCriteria = new ArrayList<>();
        final List<ISearchCriteria> childCriteria = new ArrayList<>();
        final List<ISearchCriteria> otherCriteria = new ArrayList<>();

        groupSampleCriteria(criterion.getCriteria(), parentCriteria, childCriteria, otherCriteria);

        final boolean hasMainCriteria = !criterion.getCriteria().isEmpty() || !otherCriteria.isEmpty();
        final boolean hasParentCriteria = !parentCriteria.isEmpty();
        final boolean hasChildCriteria = !childCriteria.isEmpty();

        // TODO: continue from here.

        Collection<Long> sampleIds = null;
        if (hasMainCriteria || (!hasParentCriteria && !hasChildCriteria))
        {
            sampleIds = findSampleIds(userId, criterion, otherCriteria);
            if (sampleIds == null)
            {
                sampleIds = Collections.emptyList();
            }
        }

        if (hasParentCriteria)
        {
            final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
            sampleSearchCriteria.getCriteria().addAll(parentCriteria);
            sampleIds = filterSearchResultsByCriteria(userId, sampleIds, sampleSearchCriteria,
                    PARENT_RELATIONSHIP_HANDLER);
        }

        if (hasChildCriteria)
        {
            final SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
            sampleSearchCriteria.getCriteria().addAll(parentCriteria);
            sampleIds = filterSearchResultsByCriteria(userId, sampleIds, sampleSearchCriteria,
                    CHILDREN_RELATIONSHIP_HANDLER);
        }

        return restrictResultSetIfNecessary(sampleIds);
    }

    private void groupSampleCriteria(final Collection<ISearchCriteria> allCriteria,
            final Collection<ISearchCriteria> parentCriteria, final Collection<ISearchCriteria> childCriteria,
            final Collection<ISearchCriteria> otherCriteria)
    {
        parentCriteria.clear();
        childCriteria.clear();
        otherCriteria.clear();
        for (ISearchCriteria subCriterion : allCriteria)
        {
            if (subCriterion instanceof SampleParentsSearchCriteria)
            {
                mergeCriteria(parentCriteria, subCriterion);
            } else if (subCriterion instanceof SampleChildrenSearchCriteria)
            {
                mergeCriteria(childCriteria, subCriterion);
            } else
            {
                otherCriteria.add(subCriterion);
            }
        }
    }

    private List<Long> findSampleIds(final String userId, final SampleSearchCriteria criterion,
            final List<ISearchCriteria> subCriteria)
    {
        // for now we connect all sub criteria with logical AND
        final List<IAssociationCriteria> associations = new ArrayList<>();
        for (final ISearchCriteria subCriterion : subCriteria)
        {
            // TODO: rewrite method findAssociatedEntities().
//            associations.add(findAssociatedEntities(userId, subCriterion));
        }

        if (subCriteria.isEmpty() && criterion.getCriteria().isEmpty())
        {
            // if no criteria were provided find all samples
            SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
            sampleSearchCriteria.withCode().thatContains("");
            criterion.getCriteria().add(sampleSearchCriteria);
        }
        final List<Long> sampleIds = searchDAO.searchForEntityIds(userId, criterion,
                DtoConverters.convertEntityKind(EntityKind.SAMPLE), associations);
        return sampleIds;
    }

}
