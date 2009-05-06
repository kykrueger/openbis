/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.dto;

/**
 * An entity property is composed of:
 * <ol>
 * <li>{@link IIdHolder#getId()}</li>
 * <li>{@link #getEntity}</li>
 * <li>{@link #getEntityTypePropertyType()}</li>
 * </ol>
 * 
 * @author Christian Ribeaud
 */
public interface IEntityProperty extends IIdHolder
{
    /**
     * Returns the entity type property type for this entity property.
     */
    public EntityTypePropertyTypePE getEntityTypePropertyType();

    /**
     * Returns the entity for this entity property.
     */
    public IEntityPropertiesHolder getEntity();

    /**
     * Return untyped value or <code>null</code>.
     */
    public String tryGetUntypedValue();
}
