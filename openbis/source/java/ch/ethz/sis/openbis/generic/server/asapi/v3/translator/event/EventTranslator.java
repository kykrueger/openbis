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

package ch.ethz.sis.openbis.generic.server.asapi.v3.translator.event;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.Event;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.event.fetchoptions.EventFetchOptions;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.AbstractCachingTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.TranslationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author pkupczyk
 */
@Component
public class EventTranslator extends AbstractCachingTranslator<EventPE, Event, EventFetchOptions> implements IEventTranslator
{

    @Autowired
    private IDAOFactory daoFactory;

    @Override
    protected boolean shouldTranslate(TranslationContext context, EventPE input, EventFetchOptions fetchOptions)
    {
        return true;
    }

    @Override
    protected Event createObject(TranslationContext context, EventPE input, EventFetchOptions fetchOptions)
    {
        return null;
    }

    @Override
    protected void updateObject(TranslationContext context, EventPE input, Event output, Object relations, EventFetchOptions fetchOptions)
    {
    }
}
