/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.material;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.search.MaterialSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectExecutor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAssociationCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;

/**
 * @author pkupczyk
 */
@Component
public class SearchMaterialExecutor extends AbstractSearchObjectExecutor<MaterialSearchCriteria, Long> implements
        ISearchMaterialExecutor
{

    @Autowired
    private IMaterialAuthorizationExecutor authorizationExecutor;

    @Override
    protected List<Long> doSearch(IOperationContext context, DetailedSearchCriteria criteria)
    {
        authorizationExecutor.canSearch(context);

        if (criteria.getCriteria().isEmpty())
        {
            // if no criteria were provided find all materials
            criteria.getCriteria().add(
                    new DetailedSearchCriterion(DetailedSearchField
                            .createAttributeField(SampleAttributeSearchFieldKind.CODE), "*"));
        }

        List<Long> materialIds = daoFactory.getHibernateSearchDAO().searchForEntityIds(context.getSession().tryGetPerson().getUserId(), criteria,
                ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind.MATERIAL,
                Collections.<IAssociationCriteria> emptyList());

        return materialIds;
    }

}
