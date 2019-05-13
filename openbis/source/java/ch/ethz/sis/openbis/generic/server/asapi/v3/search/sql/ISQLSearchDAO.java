package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;

import java.util.List;
import java.util.Set;

public interface ISQLSearchDAO
{

    /**
     *
     */
    Set<Long> queryDBWithNonRecursiveCriteria(EntityKind entityKind, List<ISearchCriteria> criteria,
            SearchOperator operator);

    /**
     * Returns the maximum size of a search result set. The standard implementations returns <code>hibernate.search.maxResults</code> of
     * <code>service.properties</code>.
     */
    int getResultSetSizeLimit();

    /**
     * Finds IDs of spaces and projects to which the current user has access.
     *
     * @param userId the ID of the user.
     * @return set of IDs of spaces authorized for the current user.
     */
    SpaceProjectIDsVO getAuthorisedSpaceProjectIds(Long userId);

    /**
     * Filters sample IDs based on their relations to space and projects.
     *
     * @param ids the IDs to be filtered
     * @param authorizedSpaceProjectIds value object that contains space and project IDs, which should be related to the
     *     resulting IDs.
     * @return the subset of IDs which are related either to one of the specified projects or spaces.
     */
    Set<Long> filterSampleIDsBySpaceAndProjectIDs(Set<Long> ids, SpaceProjectIDsVO authorizedSpaceProjectIds);

}
