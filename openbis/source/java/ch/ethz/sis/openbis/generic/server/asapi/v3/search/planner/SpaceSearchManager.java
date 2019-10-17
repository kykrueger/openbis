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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

/**
 * Manages detailed search with complex space search criteria.
 *
 * @author Viktor Kovtun
 */
public class SpaceSearchManager extends AbstractSearchManager<SpaceSearchCriteria, Long>
{

    public SpaceSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PETranslator<Long> idsTranslator)
    {
        super(searchDAO, authProvider, idsTranslator);
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return authorisationInformation.getSpaceIds().stream().filter(ids::contains).collect(Collectors.toSet());
    }

    private CodeSearchCriteria convertToCodeSearchCriterion(final PermIdSearchCriteria permIdSearchCriteria)
    {
        final CodeSearchCriteria codeSearchCriteria = new CodeSearchCriteria();
        codeSearchCriteria.setFieldValue(permIdSearchCriteria.getFieldValue());
        return codeSearchCriteria;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final SpaceSearchCriteria criteria)
    {
        final Collection<ISearchCriteria> newCriteria = criteria.getCriteria().stream().map(searchCriterion ->
        {
            if (searchCriterion instanceof PermIdSearchCriteria)
            {
                return convertToCodeSearchCriterion((PermIdSearchCriteria) searchCriterion);
            } else
            {
                return searchCriterion;
            }
        }).collect(Collectors.toList());

        final Set<Long> mainCriteriaIntermediateResults = getSearchDAO().queryDBWithNonRecursiveCriteria(userId, TableMapper.SPACE,
                newCriteria, criteria.getOperator());

        // If we have results, we use them
        // If we don't have results and criteria are not empty, there are no results.
        final Set<Long> resultBeforeFiltering =
                containsValues(mainCriteriaIntermediateResults) ? mainCriteriaIntermediateResults : Collections.emptySet();

        return filterIDsByUserRights(userId, resultBeforeFiltering);
    }

}
