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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.PhysicalDataSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.Set;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.CONTENT_COPIES;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper.EXTERNAL_DATA;

/**
 * Manages detailed search with physical data set search criteria.
 *
 * @author Viktor Kovtun
 */
public class PhysicalDataSetKindSearchManager extends AbstractSearchManager<PhysicalDataSearchCriteria, DataSetType, Long>
{

    public PhysicalDataSetKindSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PETranslator<Long> idsTranslator)
    {
        super(searchDAO, authProvider, idsTranslator);
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return ids;
    }

    @Override
    protected TableMapper getTableMapper()
    {
        return CONTENT_COPIES;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final PhysicalDataSearchCriteria criteria, final SortOptions<DataSetType> sortOptions,
            final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName)
    {
        if (criteria.getCriteria() == null || criteria.getCriteria().isEmpty())
        {
            return super.searchForIDs(userId, criteria, idsColumnName);
        } else {
            return doSearchForIDs(userId, criteria, criteria.getOperator(), idsColumnName);
        }
    }

    private Set<Long> doSearchForIDs(final Long userId, final PhysicalDataSearchCriteria criteria, final SearchOperator searchOperator, final String idsColumnName)
    {
        final SearchOperator finalSearchOperator = (searchOperator == null) ? criteria.getOperator() : searchOperator;

        final Set<Long> mainCriteriaIds = searchForIDs(userId, new PhysicalDataSearchCriteria(), idsColumnName);
        final Set<Long> childCriteriaIds = searchForIDsByCriteriaCollection(userId, criteria.getCriteria(), finalSearchOperator, EXTERNAL_DATA, idsColumnName);

        mainCriteriaIds.retainAll(childCriteriaIds);

        return mainCriteriaIds;
    }

}
