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

import java.util.List;
import java.util.Set;

public interface ISearchManager<C extends ISearchCriteria>
{

    /**
     * Searches for entities using certain criteria.
     *
     * @param criteria search criteria.
     * @return set of IDs of found entities.
     */
    Set<Long> searchForIDs(final C criteria);

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
     * @param fetchOptions sorting options.
     * @return ids sorted by the specified options.
     */
    List<Long> sortAndPage(final Set<Long> ids, final C criteria, final FetchOptions<?> fetchOptions);

}
