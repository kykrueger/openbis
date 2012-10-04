/*
 * Copyright 2012 ETH Zuerich, CISD
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

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import ch.systemsx.cisd.common.exception.ConfigurationFailureException;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebAppProperty;

/**
 * @author pkupczyk
 */
public class WebAppProperties
{

    private Properties properties;

    public WebAppProperties(Properties properties)
    {
        if (properties == null)
        {
            throw new IllegalArgumentException("Properties cannot be null");
        }
        this.properties = properties;
    }

    public String getLabel()
    {
        return getPropertyStringValue(WebAppProperty.LABEL);
    }

    public Integer getSorting()
    {
        return getPropertyIntegerValue(WebAppProperty.SORTING);
    }

    public String[] getContexts()
    {
        return getPropertyStringValues(WebAppProperty.CONTEXTS);
    }

    public Map<EntityKind, String[]> getEntityTypes()
    {
        Map<EntityKind, String[]> entityTypes = new HashMap<EntityKind, String[]>();
        entityTypes.put(EntityKind.EXPERIMENT,
                getPropertyStringValues(WebAppProperty.EXPERIMENT_TYPES));
        entityTypes.put(EntityKind.SAMPLE, getPropertyStringValues(WebAppProperty.SAMPLE_TYPES));
        entityTypes
                .put(EntityKind.DATA_SET, getPropertyStringValues(WebAppProperty.DATA_SET_TYPES));
        entityTypes
                .put(EntityKind.MATERIAL, getPropertyStringValues(WebAppProperty.MATERIAL_TYPES));
        return entityTypes;
    }

    private String getPropertyStringValue(WebAppProperty property)
    {
        String value = properties.getProperty(property.getName());

        if (value == null || value.trim().length() == 0)
        {
            return null;
        } else
        {
            return value.trim();
        }
    }

    private Integer getPropertyIntegerValue(WebAppProperty property)
    {
        String value = getPropertyStringValue(property);

        if (value == null)
        {
            return null;
        } else
        {
            try
            {
                return Integer.valueOf(value);
            } catch (NumberFormatException e)
            {
                throw new ConfigurationFailureException("Illegal value of " + property.getName()
                        + " web application property. Value: " + value + " is not a number.");
            }
        }
    }

    private String[] getPropertyStringValues(WebAppProperty property)
    {
        String value = getPropertyStringValue(property);

        if (value == null)
        {
            return new String[0];
        } else
        {
            return PropertyParametersUtil.parseItemisedProperty(value, property.getName());
        }
    }

}
