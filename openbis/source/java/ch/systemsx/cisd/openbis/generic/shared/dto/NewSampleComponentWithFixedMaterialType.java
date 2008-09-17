/*
 * Copyright 2007 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.annotation.BeanProperty;
import ch.systemsx.cisd.openbis.generic.shared.GenericSharedConstants;

/**
 * The full description of a new sample component.
 * <p>
 * Used when registering a sample component to the LIMS.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public class NewSampleComponentWithFixedMaterialType extends
        Code<NewSampleComponentWithFixedMaterialType>
{
    private static final long serialVersionUID = GenericSharedConstants.VERSION;

    public final static NewSampleComponentWithFixedMaterialType[] EMPTY_ARRAY =
            new NewSampleComponentWithFixedMaterialType[0];

    private String materialCode;

    public final String getMaterialCode()
    {
        return materialCode;
    }

    @BeanProperty(label = "material")
    public final void setMaterialCode(final String materialCode)
    {
        this.materialCode = materialCode;
    }
}
