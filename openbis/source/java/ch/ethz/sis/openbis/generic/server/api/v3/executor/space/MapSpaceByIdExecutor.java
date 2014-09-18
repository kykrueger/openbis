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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.space;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.collection.ListTranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.id.space.ISpaceIdTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.ListerById;
import ch.systemsx.cisd.openbis.generic.server.business.search.id.space.ListerBySpacePermIdId;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class MapSpaceByIdExecutor implements IMapSpaceByIdExecutor
{

    @Autowired
    private IDAOFactory daoFactory;

    @SuppressWarnings("unused")
    private MapSpaceByIdExecutor()
    {
    }

    public MapSpaceByIdExecutor(IDAOFactory daoFactory)
    {
        this.daoFactory = daoFactory;
    }

    @Override
    public Map<ISpaceId, SpacePE> map(IOperationContext context, Collection<? extends ISpaceId> spaceIds)
    {
        List<ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.space.ISpaceId> spaceIdsCore =
                new ListTranslator().translate(spaceIds, new ISpaceIdTranslator());

        @SuppressWarnings("rawtypes")
        List<IListerById> listers = new LinkedList<IListerById>();
        listers.add(new ListerBySpacePermIdId(daoFactory));

        List<SpacePE> list = new ListerById(listers).list(spaceIdsCore);
        Map<ISpaceId, SpacePE> map = new LinkedHashMap<ISpaceId, SpacePE>();
        int index = 0;

        for (ISpaceId spaceId : spaceIds)
        {
            map.put(spaceId, list.get(index));
            index++;
        }

        return map;
    }

}
