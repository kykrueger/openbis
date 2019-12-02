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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao;

import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

public interface ISQLSearchDAO
{

    /**
     * Queries the DB only with non recursive criteria.
     *
     * @param userId ID of the user who makes the query.
     * @param criterion the criterion that contains other criteria to search by.
     * @param tableMapper table mapper that contains extra information about tables related to the entities which can have parent-child relationships.
     * @param idsColumnName name of the column to select by, if {@code null} {@link ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN}
     *     is used.
     * @return set of numbers which represent the IDs of the scpecified ID column name.
     */
    Set<Long> queryDBWithNonRecursiveCriteria(final Long userId, final AbstractCompositeSearchCriteria criterion, final TableMapper tableMapper,
            final String idsColumnName);

    /**
     * Finds child IDs which correspond to parent IDs.
     *
     * @param tableMapper type of the entities to search for.
     * @param parentIdSet set of parent IDs to find the corresponding child IDs for.
     * @return a set of IDs od child entities of the parents specified by IDs.
     */
    Set<Long> findChildIDs(final TableMapper tableMapper, final Set<Long> parentIdSet);

    /**
     * Finds parent IDs which correspond to child IDs.
     *
     * @param tableMapper type of the entities to search for.
     * @param childIdSet set of child IDs to find the corresponding parent IDs for.
     * @return a set of IDs od parent entities of the children specified by IDs.
     */
    Set<Long> findParentIDs(final TableMapper tableMapper, final Set<Long> childIdSet);

    /**
     * Sorts IDs by certain fields.
     *
     * @param tableMapper mapper that contains information about entity tables.
     * @param filteredIDs the IDs to be sorted.
     * @param sortOptions contains fields to be sorted.
     * @return IDs of sorted entities.
     */
    Set<Long> sortIDs(final TableMapper tableMapper, final Set<Long> filteredIDs, final SortOptions<?> sortOptions);

}
