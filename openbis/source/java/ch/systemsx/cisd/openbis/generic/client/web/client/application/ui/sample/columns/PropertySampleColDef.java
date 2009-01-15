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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns;

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

import ch.systemsx.cisd.openbis.generic.client.shared.EntityProperty;
import ch.systemsx.cisd.openbis.generic.client.shared.PropertyType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;

class PropertySampleColDef extends AbstractColumnDefinition<Sample> implements IsSerializable
{
    private static final int PROPERTY_COLUMN_WIDTH = 80;

    private static final String PROPERTY_PREFIX = "property";

    private boolean isInternalNamespace;

    private String simpleCode;

    // GWT only
    public PropertySampleColDef()
    {
    }

    public PropertySampleColDef(PropertyType propertyType, boolean isDisplayedByDefault)
    {
        super(propertyType.getLabel(), PROPERTY_COLUMN_WIDTH, isDisplayedByDefault);
        this.isInternalNamespace = propertyType.isInternalNamespace();
        this.simpleCode = propertyType.getSimpleCode();
    }

    @Override
    protected String tryGetValue(Sample sample)
    {
        return tryGetValue(sample.getProperties());
    }

    private String tryGetValue(List<? extends EntityProperty<?, ?>> properties)
    {
        for (EntityProperty<?, ?> prop : properties)
        {
            if (isMatching(prop))
            {
                return prop.getValue();
            }
        }
        return null;
    }

    private boolean isMatching(EntityProperty<?, ?> prop)
    {
        PropertyType propertyType = prop.getEntityTypePropertyType().getPropertyType();
        return propertyType.isInternalNamespace() == isInternalNamespace
                && propertyType.getSimpleCode().equals(simpleCode);
    }

    public String getIdentifier()
    {
        return PROPERTY_PREFIX + isInternalNamespace + simpleCode;
    }
}