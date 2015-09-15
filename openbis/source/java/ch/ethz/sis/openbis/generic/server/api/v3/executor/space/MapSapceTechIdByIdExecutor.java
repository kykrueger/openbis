/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.space;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.helper.common.IListObjectById;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Component
public class MapSapceTechIdByIdExecutor extends AbstractMapObjectByIdExecutor<ISpaceId, Long> implements IMapSpaceTechIdByIdExecutor
{

    @Override
    protected List<IListObjectById<? extends ISpaceId, Long>> createListers(IOperationContext context)
    {
        List<IListObjectById<? extends ISpaceId, Long>> listers =
                new LinkedList<IListObjectById<? extends ISpaceId, Long>>();
        listers.add(new ListSpaceTechIdByPermId());
        return listers;
    }

}
