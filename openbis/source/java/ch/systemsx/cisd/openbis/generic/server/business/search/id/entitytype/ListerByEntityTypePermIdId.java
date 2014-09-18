/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.search.id.entitytype;

import java.util.LinkedList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.business.search.id.IListerById;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.id.entitytype.EntityTypePermIdId;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * @author pkupczyk
 */
public class ListerByEntityTypePermIdId implements IListerById<EntityTypePermIdId, EntityTypePE>
{

    private IEntityTypeDAO entityTypeDAO;

    public ListerByEntityTypePermIdId(IDAOFactory daoFactory, EntityKind entityKind)
    {
        this.entityTypeDAO = daoFactory.getEntityTypeDAO(entityKind);
    }

    @Override
    public Class<EntityTypePermIdId> getIdClass()
    {
        return EntityTypePermIdId.class;
    }

    @Override
    public EntityTypePermIdId createId(EntityTypePE entityType)
    {
        return new EntityTypePermIdId(entityType.getCode());
    }

    @Override
    public List<EntityTypePE> listByIds(List<EntityTypePermIdId> ids)
    {
        List<String> permIds = new LinkedList<String>();

        for (EntityTypePermIdId id : ids)
        {
            permIds.add(id.getPermId());
        }

        return entityTypeDAO.tryToFindEntityTypeByCodes(permIds);
    }

}
