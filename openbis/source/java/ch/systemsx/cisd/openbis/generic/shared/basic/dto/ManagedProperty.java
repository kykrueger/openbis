/*
 * Copyright 2011 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedUiDescription;

/**
 * A holder of information specific to a managed property value.
 * 
 * @author Piotr Buczek
 */
public class ManagedProperty implements IManagedProperty
{

    private static final long serialVersionUID = ServiceVersionHolder.VERSION;

    // NOTE: defaults are set for testing - scripts should override them

    private String value = BasicConstant.MANAGED_PROPERTY_PLACEHOLDER_VALUE;

    private String propertyTypeCode = null;

    private boolean ownTab = true;

    private ManagedUiDescription uiDescription = new ManagedUiDescription();

    //
    // IManagedEntityProperty
    //

    public boolean isOwnTab()
    {
        return ownTab;
    }

    public void setOwnTab(boolean ownTab)
    {
        this.ownTab = ownTab;
    }

    public IManagedUiDescription getUiDescription()
    {
        return uiDescription;
    }

    public String getPropertyTypeCode()
    {
        return propertyTypeCode;
    }

    public void setPropertyTypeCode(String propertyTypeCode)
    {
        this.propertyTypeCode = propertyTypeCode;
    }

    public String getValue()
    {
        return value;
    }

    public void setValue(String value)
    {
        this.value = value;
    }

    //
    // For serialization
    //

    public ManagedProperty()
    {
    }

}
