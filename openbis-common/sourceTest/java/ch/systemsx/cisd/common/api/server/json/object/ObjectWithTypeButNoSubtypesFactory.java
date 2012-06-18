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
public class ObjectWithTypeButNoSubtypesFactory extends ObjectFactory<ObjectWithTypeButNoSubtypes>
{

    public static final String TYPE = "ObjectWithTypeButNoSubtypes";

    public static final String CLASS = ".LegacyObjectWithTypeButNoSubtypes";

    public static final String A = "a";

    public static final String A_VALUE = "aValue";

    public static final String B = "b";

    public static final String B_VALUE = "bValue";

    @Override
    public ObjectWithTypeButNoSubtypes createObject()
    {
        ObjectWithTypeButNoSubtypes object = new ObjectWithTypeButNoSubtypes();
        object.a = A_VALUE;
        object.b = B_VALUE;
        return object;
    }

    @Override
    public Map<String, Object> createMap(ObjectCounter objectCounter, ObjectType objectType)
    {
        ObjectMap map = new ObjectMap();
        map.putId(objectCounter);
        map.putType(TYPE, CLASS, objectType);
        map.putField("a", "aValue");
        map.putField("b", "bValue");
        return map.toMap();
    }

}
