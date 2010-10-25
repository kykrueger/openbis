/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.dynamic_property.calculator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Abstract {@link IEntityAdaptor} implementation.
 * 
 * @author Piotr Buczek
 */
public class AbstractEntityAdaptor implements IEntityAdaptor
{
    protected final Map<String, IEntityPropertyAdaptor> propertiesByCode =
            new HashMap<String, IEntityPropertyAdaptor>();

    private final String code;

    public AbstractEntityAdaptor(String code)
    {
        this.code = code;
    }

    protected void initProperties(IEntityPropertiesHolder propertiesHolder)
    {
        for (EntityPropertyPE property : propertiesHolder.getProperties())
        {
            final PropertyTypePE propertyType =
                    property.getEntityTypePropertyType().getPropertyType();
            final String propertyTypeCode = propertyType.getCode();
            final String value;
            if (property.getMaterialValue() != null)
            {
                value = property.getMaterialValue().getCode();
            } else if (property.getVocabularyTerm() != null)
            {
                value = property.getVocabularyTerm().getCode();
            } else
            {
                value = property.getValue();
            }
            if (propertyType.getTransformation() == null)
            {
                addProperty(new BasicPropertyAdaptor(propertyTypeCode, value, property));
            } else
            {
                addProperty(new XmlPropertyAdaptor(propertyTypeCode, value, property,
                        propertyType.getTransformation()));
            }
        }
    }

    public void addProperty(IEntityPropertyAdaptor property)
    {
        propertiesByCode.put(property.propertyTypeCode(), property);
    }

    public String code()
    {
        return code;
    }

    public IEntityPropertyAdaptor property(String propertyTypeCode)
    {
        return propertiesByCode.get(propertyTypeCode);
    }

    public String propertyValue(String propertyTypeCode)
    {
        final IEntityPropertyAdaptor propertyOrNull = property(propertyTypeCode);
        return propertyOrNull == null ? "" : propertyOrNull.valueAsString();
    }

    public String propertyRendered(String propertyTypeCode)
    {
        final IEntityPropertyAdaptor propertyOrNull = property(propertyTypeCode);
        return propertyOrNull == null ? "" : propertyOrNull.renderedValue();
    }

    public Collection<IEntityPropertyAdaptor> properties()
    {
        return propertiesByCode.values();
    }

}
