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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.tag;

import java.util.Collection;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;

/**
 * @author pkupczyk
 */
@Component
public class AddTagToEntityExecutor implements IAddTagToEntityExecutor
{

    @Autowired
    private IGetTagMapExecutor getTagMapExecutor;

    @Autowired
    private ICreateTagExecutor createTagExecutor;

    @SuppressWarnings("unused")
    private AddTagToEntityExecutor()
    {
    }

    public AddTagToEntityExecutor(IGetTagMapExecutor getTagMapExecutor, ICreateTagExecutor createTagExecutor)
    {
        this.getTagMapExecutor = getTagMapExecutor;
        this.createTagExecutor = createTagExecutor;
    }

    @Override
    public void add(IOperationContext context, IEntityWithMetaprojects entity, Collection<? extends ITagId> tagIds)
    {
        if (tagIds == null || tagIds.isEmpty())
        {
            return;
        }

        Map<ITagId, MetaprojectPE> tagMap = getTagMapExecutor.getTagMap(context, tagIds);

        for (ITagId tagId : tagIds)
        {
            MetaprojectPE tag = tagMap.get(tagId);

            if (tag == null)
            {
                tag = createTagExecutor.createTag(context, tagId);
            }

            entity.addMetaproject(tag);
        }
    }

}
