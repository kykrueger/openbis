/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.material;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.lemnik.eodsql.QueryTool;

import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.property.PropertyRelation;

/**
 * @author pkupczyk
 */
public class MaterialPropertyRelation extends PropertyRelation
{

    public MaterialPropertyRelation(Collection<Long> entityIds)
    {
        super(entityIds);
    }

    @Override
    protected Map<Long, Map<String, String>> loadProperties(Collection<Long> entityIds)
    {
        MaterialPropertyQuery query = QueryTool.getManagedQuery(MaterialPropertyQuery.class);
        List<MaterialProperty> properties = query.getProperties(new LongOpenHashSet(entityIds));
        Map<Long, Map<String, String>> result = new HashMap<Long, Map<String, String>>();

        for (MaterialProperty property : properties)
        {
            Map<String, String> materialProperties = result.get(property.materialId);

            if (materialProperties == null)
            {
                materialProperties = new HashMap<String, String>();
                result.put(property.materialId, materialProperties);
            }

            if (property.propertyValue == null)
            {
                materialProperties.put(property.propertyCode, property.materialPropertyCode + " (" + property.materialPropertyTypeCode + ")");
            } else
            {
                materialProperties.put(property.propertyCode, property.propertyValue);
            }
        }

        return result;
    }

}
