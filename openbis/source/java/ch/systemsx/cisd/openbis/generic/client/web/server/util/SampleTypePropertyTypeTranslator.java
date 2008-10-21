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

package ch.systemsx.cisd.openbis.generic.client.web.server.util;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Hibernate;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePropertyTypePE;

/**
 * @author Franz-Josef Elmer
 */
public class SampleTypePropertyTypeTranslator
{
    private SampleTypePropertyTypeTranslator()
    {
    }

    public static SampleTypePropertyType translate(final SampleTypePropertyTypePE s,
            final SampleType sampleType)
    {
        final SampleTypePropertyType result = new SampleTypePropertyType();
        result.setDisplayed(s.isDisplayed());
        result.setManagedInternally(s.isManagedInternally());
        result.setMandatory(s.isMandatory());
        result.setPropertyType(PropertyTypeTranslator.translate(s.getPropertyType()));
        result.setEntityType(sampleType);
        return result;

    }

    public static List<SampleTypePropertyType> translate(final List<SampleTypePropertyTypePE> list,
            final SampleType sampleType)
    {
        final List<SampleTypePropertyType> result = new ArrayList<SampleTypePropertyType>();
        if (Hibernate.isInitialized(list) == false)
        {
            return new ArrayList<SampleTypePropertyType>();
        }
        for (final SampleTypePropertyTypePE st : list)
        {
            result.add(translate(st, sampleType));
        }
        return result;

    }

}
