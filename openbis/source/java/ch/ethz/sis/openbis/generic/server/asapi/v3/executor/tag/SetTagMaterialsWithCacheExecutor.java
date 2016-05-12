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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.tag;

import java.util.Collection;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.IMaterialId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;

/**
 * @author pkupczyk
 */
@Component
public class SetTagMaterialsWithCacheExecutor extends SetTagEntitiesWithCacheExecutor<IMaterialId, MaterialPE>
        implements ISetTagMaterialsWithCacheExecutor
{

    @Override
    protected String getRelationName()
    {
        return "tag-materials";
    }

    @Override
    protected Class<MaterialPE> getRelatedClass()
    {
        return MaterialPE.class;
    }

    @Override
    protected Collection<? extends IMaterialId> getRelatedIds(IOperationContext context, TagCreation creation)
    {
        return creation.getMaterialIds();
    }

}
