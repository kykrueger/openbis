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

package ch.systemsx.cisd.common.api.server.json.object;

import java.util.Map;

import ch.systemsx.cisd.common.api.server.json.common.ObjectCounter;
import ch.systemsx.cisd.common.api.server.json.common.ObjectFactory;
import ch.systemsx.cisd.common.api.server.json.common.ObjectMap;
import ch.systemsx.cisd.common.api.server.json.common.ObjectType;

/**
 * @author pkupczyk
 */
public class ObjectWithNestedTypesFactory extends ObjectFactory<ObjectWithNestedTypes>
{

    public static final String TYPE = "ObjectWithNestedTypes";

    public static final String CLASS = ".LegacyObjectWithNestedTypes";

    public static final String PROPERTY_OBJECT = "propertyObject";

    public static final String PROPERTY_NESTED = "propertyNested";

    public static final String PROPERTY_NESTED_CHILD = "propertyNestedChild";

    @Override
    public ObjectWithNestedTypes createObject()
    {
        ObjectWithNestedTypes object = new ObjectWithNestedTypes();
        object.propertyNested = new ObjectNestedFactory().createObject();
        object.propertyNestedChild = new ObjectNestedChildFactory().createObject();
        object.propertyObject = new ObjectNestedFactory().createObject();
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField(PROPERTY_NESTED,
                new ObjectNestedFactory().createMap(objectCounter, objectType));
        map.putField(PROPERTY_NESTED_CHILD,
                new ObjectNestedChildFactory().createMap(objectCounter, objectType));
        map.putField(PROPERTY_OBJECT,
                new ObjectNestedFactory().createMap(objectCounter, objectType));
        return map.toMap();
    }

}
