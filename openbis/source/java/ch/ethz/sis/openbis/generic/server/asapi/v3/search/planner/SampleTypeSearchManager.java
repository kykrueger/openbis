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

import java.util.Collections;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;

/**
 * Manages detailed search with complex sample type search criteria.
 *
 * @author Viktor Kovtun
 */
public class SampleTypeSearchManager extends AbstractSearchManager<SampleTypeSearchCriteria, SampleType>
{

    public SampleTypeSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PETranslator idsTranslator)
    {
        super(searchDAO, authProvider, idsTranslator);
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorizedSpaceProjectIds)
    {
        throw new RuntimeException("Filter method not implemented yet.");
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final SampleTypeSearchCriteria criteria)
    {
        final Set<Long> mainCriteriaIntermediateResults = getSearchDAO().queryDBWithNonRecursiveCriteria(getEntityKind(),
                criteria.getCriteria(), criteria.getOperator(), true);

        // Reaching this point we have the intermediate results of all recursive queries
        final Set<Long> resultBeforeFiltering;
        if (containsValues(mainCriteriaIntermediateResults))
        { // If we have results, we use them
            resultBeforeFiltering = mainCriteriaIntermediateResults;
        } else
        { // If we don't have results and criteria are not empty, there is no results.
            resultBeforeFiltering = Collections.emptySet();
        }

        return filterIDsByUserRights(userId, resultBeforeFiltering);
    }

    /**
     * Returns entity kind related to the entity type of this manager.
     *
     * @return entity kind related to the entity type of this manager.
     */
    private EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    private SampleTypeSearchCriteria createEmptyCriteria()
    {
        return new SampleTypeSearchCriteria();
    }

}
