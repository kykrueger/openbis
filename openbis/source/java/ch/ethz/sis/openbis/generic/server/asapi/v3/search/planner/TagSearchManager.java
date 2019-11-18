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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NameSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.search.TagSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

/**
 * Manages detailed search with tag search criteria.
 *
 * @author Viktor Kovtun
 */
public class TagSearchManager extends AbstractSearchManager<TagSearchCriteria, TagFetchOptions, Tag, Long>
{

    public TagSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
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
        return TableMapper.TAG;
    }

    private NameSearchCriteria convertToNameSearchCriterion(final AbstractFieldSearchCriteria<AbstractStringValue> criterion)
    {
        return convertToOtherCriterion(criterion, NameSearchCriteria::new);
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final TagSearchCriteria criteria, final SortOptions<Tag> sortOptions)
    {
        // Replacing perm ID and code search criteria with name search criteria, because for tags perm ID and code are equivalent to name
        final Collection<ISearchCriteria> newCriteria = criteria.getCriteria().stream().map(searchCriterion ->
        {
            if (searchCriterion instanceof PermIdSearchCriteria)
            {
                return convertToNameSearchCriterion((PermIdSearchCriteria) searchCriterion);
            } else if (searchCriterion instanceof CodeSearchCriteria)
            {
                return convertToNameSearchCriterion((CodeSearchCriteria) searchCriterion);
            } else
            {
                return searchCriterion;
            }
        }).collect(Collectors.toList());

        final Set<Long> mainCriteriaIntermediateResults = getSearchDAO().queryDBWithNonRecursiveCriteria(userId, TableMapper.TAG,
                newCriteria, criteria.getOperator());

        // If we have results, we use them
        final Set<Long> resultBeforeFiltering =
                containsValues(mainCriteriaIntermediateResults) ? mainCriteriaIntermediateResults : Collections.emptySet();

        final Set<Long> resultAfterFiltering = getAuthProvider().getTagsOfUser(resultBeforeFiltering, userId);

        return filterIDsByUserRights(userId, resultAfterFiltering);
    }

}
