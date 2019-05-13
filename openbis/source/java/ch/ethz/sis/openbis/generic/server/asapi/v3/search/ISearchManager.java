package ch.ethz.sis.openbis.generic.server.asapi.v3.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;

import java.util.List;
import java.util.Set;

public interface ISearchManager
{

    /**
     * Searches for entities using certain criteria.
     *
     * @param criteria search criteria.
     * @return set of IDs of found entities.
     */
    Set<Long> searchForIDs(final SampleSearchCriteria criteria);

    /**
     * Filters sample IDs set leaving the ones to which the user has access.
     *
     * @param userId the ID of the user.
     * @param ids IDs to filter.
     * @return IDs of samples which the user is authorised to access.
     */
    Set<Long> filterIDsByUserRights(final Long userId, final Set<Long> ids);

    /**
     * Sorts IDs using certain sort options.
     *
     * @param ids IDs of entities to sort.
     * @param sortOptions sorting options.
     * @return ids sorted by the specified options.
     */
    List<Long> sortIDsByValue(final Set<Long> ids, final SortOptions sortOptions);

}
