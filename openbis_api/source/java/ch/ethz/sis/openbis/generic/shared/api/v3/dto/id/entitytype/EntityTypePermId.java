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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.ObjectPermId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * Entity type perm id.
 * 
 * @author pkupczyk
 */
@JsonObject("EntityTypePermId")
public class EntityTypePermId extends ObjectPermId implements IEntityTypeId
{

    private static final long serialVersionUID = 1L;

    /**
     * @param permId Entity type perm id, e.g. "MY_ENTITY_TYPE".
     */
    public EntityTypePermId(String permId)
    {
        super(permId);
    }

    //
    // JSON-RPC
    //

    @SuppressWarnings("unused")
    private EntityTypePermId()
    {
        super();
    }

}
