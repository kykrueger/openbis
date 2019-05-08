/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.dataset.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.AbstractDataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetChildrenSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetContainerSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetParentsSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.AbstractCompositeSearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.IObjectAttributeProviderFactory;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchCriteriaTranslationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchObjectKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.common.search.SearchTranslationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;

/**
 * @author pkupczyk
 */
public class DataSetSearchCriteriaTranslator extends AbstractCompositeSearchCriteriaTranslator
{

    public DataSetSearchCriteriaTranslator(IDAOFactory idaoFactory, IObjectAttributeProviderFactory entityAttributeProviderFactory)
    {
        super(idaoFactory, entityAttributeProviderFactory);
    }

    @Override
    protected boolean doAccepts(ISearchCriteria criteria)
    {
        return criteria instanceof AbstractDataSetSearchCriteria;
    }

    @Override
    protected SearchCriteriaTranslationResult doTranslate(SearchTranslationContext context, ISearchCriteria criteria)
    {
        context.pushObjectKind(SearchObjectKind.DATA_SET);
        SearchCriteriaTranslationResult translationResult = super.doTranslate(context, criteria);
        context.popObjectKind();

        if (criteria instanceof DataSetSearchCriteria && context.peekObjectKind() == null)
        {
            return translationResult;
        }

        AssociatedEntityKind entityKind;
        if (criteria instanceof DataSetParentsSearchCriteria)
        {
            entityKind = AssociatedEntityKind.DATA_SET_PARENT;
        } else if (criteria instanceof DataSetChildrenSearchCriteria)
        {
            entityKind = AssociatedEntityKind.DATA_SET_CHILD;
        } else if (criteria instanceof DataSetContainerSearchCriteria)
        {
            entityKind = AssociatedEntityKind.DATA_SET_CONTAINER;
        } else if (criteria instanceof DataSetSearchCriteria)
        {
            entityKind = AssociatedEntityKind.DATA_SET;
        } else
        {
            throw new IllegalArgumentException("Unknown criteria: " + criteria);
        }
        return new SearchCriteriaTranslationResult(
                new DetailedSearchSubCriteria(entityKind, translationResult.getCriteria()));
    }

}
