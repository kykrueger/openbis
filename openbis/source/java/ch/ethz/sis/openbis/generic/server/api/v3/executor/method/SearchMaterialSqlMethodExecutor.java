/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.material.ISearchMaterialIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material.sql.IMaterialSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.material.MaterialFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sort.SortAndPage;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.MaterialSearchCriterion;

/**
 * @author pkupczyk
 */
@Component
public class SearchMaterialSqlMethodExecutor extends AbstractMethodExecutor implements ISearchMaterialMethodExecutor
{

    @Autowired
    private ISearchMaterialIdExecutor searchExecutor;

    @Autowired
    private IMaterialSqlTranslator translator;

    @Override
    public List<Material> search(final String sessionToken, final MaterialSearchCriterion criterion, final MaterialFetchOptions fetchOptions)
    {
        return executeInContext(sessionToken, new IMethodAction<List<Material>>()
            {
                @Override
                public List<Material> execute(IOperationContext context)
                {
                    Collection<Material> results = searchAndTranslate(context, criterion, fetchOptions);
                    return sortAndPage(context, results, fetchOptions);
                }
            });
    }

    @SuppressWarnings("unchecked")
    private Collection<Material> searchAndTranslate(IOperationContext context, MaterialSearchCriterion criterion, MaterialFetchOptions fetchOptions)
    {
        if (fetchOptions.getCacheMode() != null)
        {
            Collection<Material> results = (Collection<Material>) context.getSession().getAttributes().get(getClass().getName() + "_results");

            if (results == null)
            {
                results = doSearchAndTranslate(context, criterion, fetchOptions);
                context.getSession().getAttributes().put(getClass().getName() + "_results", results);
            }

            return results;
        } else
        {
            return doSearchAndTranslate(context, criterion, fetchOptions);
        }
    }

    private Collection<Material> doSearchAndTranslate(IOperationContext context, MaterialSearchCriterion criterion, MaterialFetchOptions fetchOptions)
    {
        List<Long> ids = searchExecutor.search(context, criterion);
        TranslationContext translationContext = new TranslationContext(context.getSession());
        Map<Long, Material> idToObjectMap = translator.translate(translationContext, ids, fetchOptions);
        return idToObjectMap.values();
    }

    private List<Material> sortAndPage(IOperationContext context, Collection<Material> results, MaterialFetchOptions fetchOptions)
    {
        if (results == null || results.isEmpty())
        {
            return Collections.emptyList();
        }

        SortAndPage sap = new SortAndPage();
        Collection<Material> objects = sap.sortAndPage(results, fetchOptions);

        return new ArrayList<Material>(objects);
    }
}
