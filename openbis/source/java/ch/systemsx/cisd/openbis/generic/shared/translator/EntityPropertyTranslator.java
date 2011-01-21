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

package ch.systemsx.cisd.openbis.generic.shared.translator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PropertyTypePE;

/**
 * Translates {@link EntityPropertyPE} to {@link IEntityProperty}.
 * 
 * @author Izabela Adamczyk
 */
public final class EntityPropertyTranslator
{
    private EntityPropertyTranslator()
    {
        // Can not be instantiated.
    }

    public final static IEntityProperty translate(final EntityPropertyPE propertyPE,
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        return translate(propertyPE, cacheOrNull, false);
    }

    public final static IEntityProperty translate(final EntityPropertyPE propertyPE,
            Map<PropertyTypePE, PropertyType> cacheOrNull, boolean rawManagedProperties)
    {
        final IEntityProperty basicProperty =
                PropertyTranslatorUtils.createEntityProperty(propertyPE);
        final PropertyType propertyType =
                PropertyTypeTranslator.translate(propertyPE.getEntityTypePropertyType()
                        .getPropertyType(), cacheOrNull);
        final Long ordinal = propertyPE.getEntityTypePropertyType().getOrdinal();

        PropertyTranslatorUtils.initializeEntityProperty(basicProperty, propertyType, ordinal);

        final DataTypeCode typeCode = PropertyTranslatorUtils.getDataTypeCode(propertyPE);
        switch (typeCode)
        {
            case CONTROLLEDVOCABULARY:
                basicProperty.setVocabularyTerm(VocabularyTermTranslator.translate(propertyPE
                        .getVocabularyTerm()));
                break;
            case MATERIAL:
                basicProperty.setMaterial(MaterialTranslator.translate(
                        propertyPE.getMaterialValue(), false));
                break;
            default:
                basicProperty.setValue(propertyPE.tryGetUntypedValue());
        }

        final IEntityProperty result;
        if (propertyPE.getEntityTypePropertyType().isManaged() && rawManagedProperties == false)
        {
            result = PropertyTranslatorUtils.createManagedEntityProperty(propertyPE, basicProperty);
            PropertyTranslatorUtils.initializeEntityProperty(result, propertyType, ordinal);
        } else
        {
            result = basicProperty;
        }

        return result;
    }

    public final static List<IEntityProperty> translateRaw(
            final Set<? extends EntityPropertyPE> list,
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        if (list == null)
        {
            return null;
        }
        final List<IEntityProperty> result = new ArrayList<IEntityProperty>();
        for (final EntityPropertyPE property : list)
        {
            result.add(translate(property, cacheOrNull, true));
        }
        return result;
    }

    public final static List<IEntityProperty> translate(final Set<? extends EntityPropertyPE> list,
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        if (list == null)
        {
            return null;
        }
        final List<IEntityProperty> result = new ArrayList<IEntityProperty>();
        for (final EntityPropertyPE property : list)
        {
            result.add(translate(property, cacheOrNull));
        }
        return result;
    }

    public final static IEntityProperty[] translate(final EntityPropertyPE[] list,
            Map<PropertyTypePE, PropertyType> cacheOrNull)
    {
        if (list == null)
        {
            return null;
        }
        final IEntityProperty[] result = new IEntityProperty[list.length];
        int idx = 0;
        for (final EntityPropertyPE property : list)
        {
            result[idx++] = translate(property, cacheOrNull);
        }
        return result;
    }
}
