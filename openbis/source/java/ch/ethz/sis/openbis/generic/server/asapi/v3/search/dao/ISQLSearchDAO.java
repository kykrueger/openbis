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

import java.util.Collection;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;

public interface ISQLSearchDAO
{

    /**
     *
     */
    Set<Long> queryDBWithNonRecursiveCriteria(EntityKind entityKind, Collection<ISearchCriteria> criteria,
            SearchOperator operator, final boolean entityTypeSearch);


    /**
     * Finds child IDs which correspond to parent IDs.
     *
     * @param entityKind type of the entities to search for.
     * @param parentIdSet set of parent IDs to find the corresponding child IDs for.
     * @return a set of IDs od child entities of the parents specified by IDs.
     */
    Set<Long> findChildIDs(EntityKind entityKind, Set<Long> parentIdSet);

    /**
     * Finds parent IDs which correspond to child IDs.
     *
     *
     * @param entityKind type of the entities to search for.
     * @param childIdSet set of child IDs to find the corresponding parent IDs for.
     * @return a set of IDs od parent entities of the children specified by IDs.
     */
    Set<Long> findParentIDs(EntityKind entityKind, Set<Long> childIdSet);

}
