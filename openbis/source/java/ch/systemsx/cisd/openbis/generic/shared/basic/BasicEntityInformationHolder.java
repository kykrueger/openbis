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

package ch.systemsx.cisd.openbis.generic.shared.basic;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * A basic {@link IEntityInformationHolder} implementation.
 * 
 * @author Piotr Buczek
 */
public class BasicEntityInformationHolder implements IEntityInformationHolder, IsSerializable
{
    private EntityKind entityKind;

    private EntityType entityType;

    private String identifier;

    @SuppressWarnings("unused")
    private BasicEntityInformationHolder()
    {
        // needed for serialization purposes
    }

    public BasicEntityInformationHolder(EntityKind entityKind, EntityType entityType,
            String identifier)
    {
        this.entityKind = entityKind;
        this.entityType = entityType;
        this.identifier = identifier;
    }

    public EntityKind getEntityKind()
    {
        return entityKind;
    }

    public EntityType getEntityType()
    {
        return entityType;
    }

    public String getIdentifier()
    {
        return identifier;
    }

}
