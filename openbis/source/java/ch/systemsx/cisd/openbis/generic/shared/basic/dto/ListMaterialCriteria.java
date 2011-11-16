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

package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;
import java.util.Collection;

/**
 * Criteria for listing <i>materials</i>.
 * 
 * @author Izabela Adamczyk
 */
public final class ListMaterialCriteria implements Serializable
{
    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    private MaterialType materialTypeOrNull;

    private Collection<Long> materialIdsOrNull;

    // GWT only
    public ListMaterialCriteria()
    {
    }

    public ListMaterialCriteria(MaterialType materialType)
    {
        this(materialType, null);
    }

    public ListMaterialCriteria(Collection<Long> materialIds)
    {
        this(null, materialIds);
    }

    private ListMaterialCriteria(MaterialType materialTypeOrNull, Collection<Long> materialIdsOrNull)
    {
        assert materialIdsOrNull != null || materialTypeOrNull != null;
        assert materialIdsOrNull == null || materialTypeOrNull == null;
        this.materialTypeOrNull = materialTypeOrNull;
        this.materialIdsOrNull = materialIdsOrNull;
    }

    public MaterialType tryGetMaterialType()
    {
        return materialTypeOrNull;
    }

    public Collection<Long> tryGetMaterialIds()
    {
        return materialIdsOrNull;
    }

}
