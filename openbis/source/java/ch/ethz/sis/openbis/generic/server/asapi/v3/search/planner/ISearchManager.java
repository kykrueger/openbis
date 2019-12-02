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

import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;

public interface ISearchManager<CRITERIA extends ISearchCriteria, OBJECT, OBJECT_PE> extends IID2PETranslator<OBJECT_PE>
{

    /**
     * Searches for entities using certain criteria.
     *
     * @param criteria search criteria.
     * @param sortOptions sorting columns and the directions of sorting.
     * @param parentCriteria parent criteria (if there is one) to {@code criteria}.
     * @param idsColumnName name of the column to select the ID's by; usually the
     * {@link ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN}.
     * @return set of IDs of found entities.
     */
    Set<Long> searchForIDs(final Long userId, final CRITERIA criteria, final SortOptions<OBJECT> sortOptions,
            final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName);

    /**
     * Filters IDs of certain
     *
     * @param userId
     * @param ids
     * @return
     */
    Set<Long> filterIDsByUserRights(Long userId, Set<Long> ids);

    Set<Long> sortIDs(Set<Long> filteredIDs, SortOptions<OBJECT> sortOptions);
}
