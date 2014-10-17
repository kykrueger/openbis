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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.IMapSpaceByIdExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.ObjectNotFoundException;
import ch.ethz.sis.openbis.generic.shared.api.v3.exceptions.UnauthorizedObjectAccessException;
import ch.systemsx.cisd.openbis.generic.server.authorization.validator.SimpleSpaceValidator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SetSampleSpaceExecutor implements ISetSampleSpaceExecutor
{

    @Autowired
    private IMapSpaceByIdExecutor mapSpaceByIdExecutor;

    @Override
    public void set(IOperationContext context, Map<SampleCreation, SamplePE> creationsMap)
    {
        List<ISpaceId> spaceIds = new LinkedList<ISpaceId>();

        for (SampleCreation creation : creationsMap.keySet())
        {
            if (creation.getSpaceId() != null)
            {
                spaceIds.add(creation.getSpaceId());
            }
        }

        Map<ISpaceId, SpacePE> spaceMap = mapSpaceByIdExecutor.map(context, spaceIds);

        for (Map.Entry<SampleCreation, SamplePE> creationEntry : creationsMap.entrySet())
        {
            SampleCreation creation = creationEntry.getKey();
            SamplePE sample = creationEntry.getValue();

            context.pushContextDescription("set space for sample " + creation.getCode());

            if (creation.getSpaceId() != null)
            {
                SpacePE space = spaceMap.get(creation.getSpaceId());
                if (space == null)
                {
                    throw new ObjectNotFoundException(creation.getSpaceId());
                }

                if (false == new SimpleSpaceValidator().doValidation(context.getSession().tryGetPerson(), space))
                {
                    throw new UnauthorizedObjectAccessException(creation.getSpaceId());
                }

                sample.setSpace(space);
            }

            context.popContextDescription();
        }
    }
}
