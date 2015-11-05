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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.entity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.Resource;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;

/**
 * @author pkupczyk
 */
public abstract class AbstractSetEntityToManyRelationExecutor<ENTITY_CREATION, ENTITY_PE, ENTITY_ID>
{

    @Resource(name = ComponentNames.RELATIONSHIP_SERVICE)
    protected IRelationshipService relationshipService;

    public void set(IOperationContext context, Map<ENTITY_CREATION, ENTITY_PE> creationsMap, Map<ENTITY_ID, ENTITY_PE> relatedMap)
    {
        for (ENTITY_CREATION creation : creationsMap.keySet())
        {
            ENTITY_PE entity = creationsMap.get(creation);
            Collection<? extends ENTITY_ID> relatedIds = getRelatedIds(context, creation);

            if (relatedIds != null)
            {
                Collection<ENTITY_PE> related = new LinkedList<ENTITY_PE>();

                for (ENTITY_ID relatedId : relatedIds)
                {
                    related.add(relatedMap.get(relatedId));
                }

                if (false == related.isEmpty())
                {
                    setRelated(context, entity, related);
                }
            }
        }
    }

    protected abstract Collection<? extends ENTITY_ID> getRelatedIds(IOperationContext context, ENTITY_CREATION creation);

    protected abstract void setRelated(IOperationContext context, ENTITY_PE entity, Collection<ENTITY_PE> related);

}