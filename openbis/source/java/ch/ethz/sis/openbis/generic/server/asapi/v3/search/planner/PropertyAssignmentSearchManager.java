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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyAssignmentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.IPropertyAssignmentSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.hibernate.IID2PETranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;

import java.util.HashSet;
import java.util.Set;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROPERTY_TYPE_COLUMN;

/**
 * Manages detailed search with complex property assignment search criteria.
 * 
 * @author Viktor Kovtun
 */
public class PropertyAssignmentSearchManager extends
        AbstractSearchManager<PropertyAssignmentSearchCriteria, PropertyAssignment, Long>
{

    private IPropertyAssignmentSearchDAO assignmentsSearchDAO;

    public PropertyAssignmentSearchManager(final ISQLSearchDAO searchDAO, final ISQLAuthorisationInformationProviderDAO authProvider,
            final IID2PETranslator idsTranslator, final IPropertyAssignmentSearchDAO assignmentsSearchDAO)
    {
        super(searchDAO, authProvider, idsTranslator);
        this.assignmentsSearchDAO = assignmentsSearchDAO;
    }

    @Override
    protected TableMapper getTableMapper()
    {
        // TODO: not always related to samples.
        return TableMapper.SAMPLE_PROPERTY_ASSIGNMENT;
    }

    @Override
    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids, final AuthorisationInformation authorisationInformation)
    {
        return ids;
    }

    @Override
    public Set<Long> searchForIDs(final Long userId, final PropertyAssignmentSearchCriteria criteria, final SortOptions<PropertyAssignment> sortOptions,
            final AbstractCompositeSearchCriteria parentCriteria, final String idsColumnName)
    {
        final Set<Long> mainCriteriaIntermediateResults = getSearchDAO().queryDBWithNonRecursiveCriteria(userId,
                criteria, getTableMapper(), idsColumnName);

        if (parentCriteria.getClass() == SampleTypeSearchCriteria.class)
        {
            final DummyCompositeSearchCriterion compositeSearchCriterion = new DummyCompositeSearchCriterion(
                    criteria.getCriteria(), criteria.getOperator());

            final Set<Long> propertyTypesIds = getSearchDAO().queryDBWithNonRecursiveCriteria(userId,
                    compositeSearchCriterion, TableMapper.SEMANTIC_ANNOTATION, PROPERTY_TYPE_COLUMN);

            final Set<Long> assignmentIDsWithoutAnnotations = assignmentsSearchDAO.findAssignmentsWithoutAnnotations(
                    propertyTypesIds, idsColumnName);

            final Set<Long> finalResults = new HashSet<>(mainCriteriaIntermediateResults);
            finalResults.addAll(assignmentIDsWithoutAnnotations);
            return finalResults;
        } else
        {
            return mainCriteriaIntermediateResults;
        }
    }

}
