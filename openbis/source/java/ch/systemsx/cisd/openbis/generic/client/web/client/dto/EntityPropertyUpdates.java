/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.basic.ISerializable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ServiceVersionHolder;

/**
 * Updates of properties of an entity.
 * 
 * @author Piotr Buczek
 */
public class EntityPropertyUpdates implements ISerializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private EntityKind entityKind;

    private TechId entityId;

    private Map<String /* property code */, String /* new value */> modifiedProperties =
            new LinkedHashMap<String, String>();

    public EntityPropertyUpdates()
    {
    }

    public EntityPropertyUpdates(EntityKind entityKind, TechId entityId)
    {
        this.entityKind = entityKind;
        this.entityId = entityId;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public void setEntityKind(EntityKind entityKind)
    {
        this.entityKind = entityKind;
    }

    public TechId getEntityId()
    {
        return entityId;
    }

    public void setEntityId(TechId entityId)
    {
        this.entityId = entityId;
    }

    public Map<String, String> getModifiedProperties()
    {
        return modifiedProperties;
    }

    public void addModifiedProperty(String propertyCode, String propertyValue)
    {
        modifiedProperties.put(propertyCode, propertyValue);
    }

    public void setModifiedProperties(Map<String, String> modifiedProperties)
    {
        this.modifiedProperties = modifiedProperties;
    }

}
