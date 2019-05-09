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
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLSearchDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchNotNullAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.translator.DtoConverters;

import java.util.*;

/**
 * Manages detailed search with complex search criteria.
 *
 * @author Viktor Kovtun
 */
public class AbstractSearchManager<T>
{
    protected final ISQLSearchDAO searchDAO;

    protected final T lister;

    public AbstractSearchManager(ISQLSearchDAO searchDAO, T lister)
    {
        this.searchDAO = searchDAO;
        this.lister = lister;
    }

    protected Collection<Long> restrictResultSetIfNecessary(final Collection<Long> ids)
    {
        if (ids == null)
        {
            return new ArrayList<>();
        } else
        {
            final int maxSize = searchDAO.getResultSetSizeLimit();

            if (ids.size() <= maxSize)
            {
                return ids;
            } else
            {
                return new ArrayList<>(ids).subList(0, maxSize);
            }
        }
    }

    protected IAssociationCriteria findAssociatedEntities(final String userId, final ISearchCriteria subCriteria)
    {
//        // TODO: implement.
//        return null;

        // This method is never called for parents and children.
        // For the "others" case subCriteria cannot be null.
//        if (subCriteria.getCriteria() == null)
//        {
//            return new DetailedSearchNullAssociationCriteria(subCriteria.getTargetEntityKind());
//        } else
//        {
            if (subCriteria.getCriteria().isEmpty())
            {
                return new DetailedSearchNotNullAssociationCriteria(subCriteria.getTargetEntityKind());
            } else
            {
                // where related objects meets given criteria (for now we don't support sub criteria of sub criteria)
                final Collection<Long> associatedIds =
                        searchDAO.searchForEntityIds(userId, subCriteria.getCriteria(), DtoConverters
                                .convertEntityKind(subCriteria.getTargetEntityKind().getEntityKind()),
                                Collections.emptyList());

                return new DetailedSearchAssociationCriteria(subCriteria.getTargetEntityKind(),
                        associatedIds);
            }
//        }
    }

    protected <C extends ISearchCriteria> Collection<Long> filterSearchResultsByCriteria(final String userId,
            final Collection<Long> idsToFilter, Collection<C> criteria,
            final IRelationshipHandler<C> relationshipHandler)
    {
        final Collection<Long> relatedIds = relationshipHandler.findRelatedIdsByCriteria(userId, criteria,
                Collections.emptyList());

        if (idsToFilter == null)
        {
            final Map<Long, Set<Long>> relatedIdToIdsMap = relationshipHandler.listRelatedIdsToIds(relatedIds);
            final Set<Long> result = new HashSet<>();

            for (final Set<Long> ids : relatedIdToIdsMap.values())
            {
                result.addAll(ids);
            }

            return result;
        } else
        {
            if (idsToFilter.size() > relatedIds.size())
            {
                final Map<Long, Set<Long>> relatedIdToIdsMap = relationshipHandler.listRelatedIdsToIds(relatedIds);
                return intersection(idsToFilter, relatedIdToIdsMap.values());
            } else
            {
                final Map<Long, Set<Long>> idToRelatedIdsMap = relationshipHandler.listIdsToRelatedIds(idsToFilter);
                return filterIdsByRelationship(idsToFilter, relatedIds, idToRelatedIdsMap);
            }
        }
    }

    /**
     * @return the intersection of a collection and a multi set (collection of sets).
     */
    protected Collection<Long> intersection(final Collection<Long> collection,
            final Collection<Set<Long>> multiSet)
    {
        final Set<Long> intersection = new HashSet<>();
        for (final Set<Long> set : multiSet)
        {
            set.retainAll(collection);
            intersection.addAll(set);
        }
        return intersection;
    }

    /**
     * Filters search results by a relationship. This comes in handy when filtering search results based on a
     * subcriteria which operates on a different entity than the encapsulating criteria.
     * 
     * @param idsToFilter the ids to be filtered.
     * @param relatedIds ids matching subcriteria.
     * @param relationshipMap a relationship map in the form <id, set<related-ids>>
     * @return all id-s having relationship to at least on id from relatedIds.
     */
    protected Collection<Long> filterIdsByRelationship(final Collection<Long> idsToFilter,
            final Collection<Long> relatedIds, final Map<Long, Set<Long>> relationshipMap)
    {
        for (final Iterator<Long> iterator = idsToFilter.iterator(); iterator.hasNext();)
        {
            final Long id = iterator.next();
            final Set<Long> relatedIdSet = relationshipMap.get(id);
            if (relatedIdSet == null || Collections.disjoint(relatedIds, relatedIdSet))
            {
                iterator.remove();
            }
        }
        return idsToFilter;
    }

    interface IRelationshipHandler<C extends ISearchCriteria>
    {

        Collection<Long> findRelatedIdsByCriteria(Collection<C> criterion);

        Map<Long, Set<Long>> listIdsToRelatedIds(Collection<Long> ids);

        Map<Long, Set<Long>> listRelatedIdsToIds(Collection<Long> relatedIds);

    }

}
