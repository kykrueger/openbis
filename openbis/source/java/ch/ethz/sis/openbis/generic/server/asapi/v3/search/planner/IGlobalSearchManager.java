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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MatchingEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IGlobalSearchManager extends ISearchManager
{

    /**
     * Searches for entities using certain criteria.
     *
     *
     * @param authorisationInformation
     * @param criteria search criteria.
     * @param idsColumnName name of the column to select the ID's by; usually the
     * {@link ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN}.
     * @param tableMapper the table mapper that points to the table to run global search on.
     * @param fetchOptions
     * @return set of IDs of found entities.
     */
    Set<Map<String, Object>> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation,
            final GlobalSearchCriteria criteria, final String idsColumnName, final TableMapper tableMapper, final GlobalSearchObjectFetchOptions fetchOptions);

    List<MatchingEntity> sortRecords(Collection<MatchingEntity> records,
            SortOptions<GlobalSearchObject> sortOptions);

    Collection<MatchingEntity> map(final Collection<Map<String, Object>> ids, boolean withMatches);

}
