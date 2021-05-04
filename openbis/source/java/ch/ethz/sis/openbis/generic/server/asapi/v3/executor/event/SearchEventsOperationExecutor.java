/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.event;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchObjectsOperationResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.EventSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.SearchEventsOperation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.search.SearchEventsOperationResult;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.AbstractSearchObjectsOperationExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.ILocalSearchManager;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.event.IEventTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author pkupczyk
 */
@Component
public class SearchEventsOperationExecutor extends AbstractSearchObjectsOperationExecutor<Event, EventPE, EventSearchCriteria, EventFetchOptions>
        implements ISearchEventsOperationExecutor
{

    @Autowired
    private ISearchEventExecutor searchExecutor;

    @Autowired
    private IEventTranslator translator;

    @Override
    protected Class<? extends SearchObjectsOperation<EventSearchCriteria, EventFetchOptions>> getOperationClass()
    {
        return SearchEventsOperation.class;
    }

    @Override
    protected List<EventPE> doSearch(IOperationContext context, EventSearchCriteria criteria,
            EventFetchOptions fetchOptions)
    {
        return searchExecutor.search(context, criteria, fetchOptions);
    }

    @Override
    protected Map<EventPE, Event> doTranslate(TranslationContext translationContext, Collection<EventPE> objects, EventFetchOptions fetchOptions)
    {
        return translator.translate(translationContext, objects, fetchOptions);
    }

    @Override
    protected SearchObjectsOperationResult<Event> getOperationResult(SearchResult<Event> searchResult)
    {
        return new SearchEventsOperationResult(searchResult);
    }

    @Override
    protected ILocalSearchManager<EventSearchCriteria, Event, EventPE> getSearchManager()
    {
        throw new RuntimeException("This method is not implemented yet.");
    }

}
