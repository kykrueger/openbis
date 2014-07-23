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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.search;

import java.util.Stack;

import ch.ethz.sis.openbis.generic.server.api.v3.context.Context;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author pkupczyk
 */
public class SearchTranslationContext extends Context
{

    private final Stack<EntityKind> entityKinds = new Stack<EntityKind>();

    public SearchTranslationContext(Session session)
    {
        super(session);
    }

    public void pushEntityKind(EntityKind entityKind)
    {
        entityKinds.push(entityKind);
    }

    public EntityKind popEntityKind()
    {
        return entityKinds.pop();
    }

    public EntityKind peekEntityKind()
    {
        if (entityKinds.isEmpty())
        {
            return null;
        } else
        {
            return entityKinds.peek();
        }
    }

}
