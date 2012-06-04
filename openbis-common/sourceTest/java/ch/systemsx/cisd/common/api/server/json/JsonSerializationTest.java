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

package ch.systemsx.cisd.common.api.server.json;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNested;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNestedChild;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithType;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeAA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeButNoSubtypes;

/**
 * @author pkupczyk
 */
public class JsonSerializationTest
{

    @Test
    public void testSerializeRootType() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeValues(values, false);

        ObjectWithType object = new ObjectWithType();
        setObjectWithTypeFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeRootTypeEmpty() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeValues(values, true);

        ObjectWithType object = new ObjectWithType();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeFirstLevelSubType() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeAValues(values, false);

        ObjectWithTypeA object = new ObjectWithTypeA();
        setObjectWithTypeAFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeFirstLevelSubTypeEmpty() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeAValues(values, true);

        ObjectWithTypeA object = new ObjectWithTypeA();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeSecondLevelSubType() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeAAValues(values, false);

        ObjectWithTypeAA object = new ObjectWithTypeAA();
        setObjectWithTypeAAFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeSecondLevelSubTypeEmpty() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeAAValues(values, true);

        ObjectWithTypeAA object = new ObjectWithTypeAA();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNestedRootType() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeNestedValues(values, false);

        ObjectNested object = new ObjectNested();
        setObjectWithTypeNestedFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNestedRootTypeEmpty() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeNestedValues(values, true);

        ObjectNested object = new ObjectNested();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNestedSubType() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeNestedChildValues(values, false);

        ObjectNestedChild object = new ObjectNestedChild();
        setObjectWithTypeNestedChildFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNestedSubTypeEmpty() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeNestedChildValues(values, true);

        ObjectNestedChild object = new ObjectNestedChild();

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializePolymorphicType() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeValues(values, false);

        ObjectWithType object = new ObjectWithType();
        setObjectWithTypeFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    @Test
    public void testSerializeNotPolymorphicType() throws Exception
    {
        Map<String, Object> values = new HashMap<String, Object>();
        putObjectWithTypeButNoSubtypesValues(values, false);

        ObjectWithTypeButNoSubtypes object = new ObjectWithTypeButNoSubtypes();
        setObjectWithTypeButNoSubtypesFields(object);

        serializeObjectAndCheckItsValues(object, values);
    }

    private void putObjectWithTypeValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectWithType");

        if (empty)
        {
            map.put("base", null);
        } else
        {
            map.put("base", "baseValue");
        }
    }

    private void setObjectWithTypeFields(ObjectWithType object)
    {
        object.base = "baseValue";
    }

    private void putObjectWithTypeAValues(Map<String, Object> map, boolean empty)
    {
        putObjectWithTypeValues(map, empty);
        map.put("@type", "ObjectWithTypeA");

        if (empty)
        {
            map.put("a", null);
        } else
        {
            map.put("a", "aValue");
        }
    }

    private void setObjectWithTypeAFields(ObjectWithTypeA object)
    {
        setObjectWithTypeFields(object);
        object.a = "aValue";
    }

    private void putObjectWithTypeAAValues(Map<String, Object> map, boolean empty)
    {
        putObjectWithTypeAValues(map, empty);
        map.put("@type", "ObjectWithTypeAA");

        if (empty)
        {
            map.put("aa", null);
        } else
        {
            map.put("aa", "aaValue");
        }
    }

    private void setObjectWithTypeAAFields(ObjectWithTypeAA object)
    {
        setObjectWithTypeAFields(object);
        object.aa = "aaValue";
    }

    private void putObjectWithTypeNestedValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectNested");

        if (empty)
        {
            map.put("nested", null);
        } else
        {
            map.put("nested", "nestedValue");
        }

    }

    private void setObjectWithTypeNestedFields(ObjectNested object)
    {
        object.nested = "nestedValue";
    }

    private void putObjectWithTypeNestedChildValues(Map<String, Object> map, boolean empty)
    {
        putObjectWithTypeNestedValues(map, empty);
        map.put("@type", "ObjectNestedChild");

        if (empty)
        {
            map.put("nestedChild", null);
        } else
        {
            map.put("nestedChild", "nestedChildValue");
        }
    }

    private void setObjectWithTypeNestedChildFields(ObjectNestedChild object)
    {
        setObjectWithTypeNestedFields(object);
        object.nestedChild = "nestedChildValue";
    }

    private void putObjectWithTypeButNoSubtypesValues(Map<String, Object> map, boolean empty)
    {
        map.put("@type", "ObjectWithTypeButNoSubtypes");

        if (empty)
        {
            map.put("a", null);
            map.put("b", null);
        } else
        {
            map.put("a", "aValue");
            map.put("b", "bValue");
        }
    }

    private void setObjectWithTypeButNoSubtypesFields(ObjectWithTypeButNoSubtypes object)
    {
        object.a = "aValue";
        object.b = "bValue";
    }

    private void serializeObjectAndCheckItsValues(Object object, Map<String, Object> expectedMap)
            throws Exception
    {
        String jsonFromObject = new JsonTestObjectMapper().writeValueAsString(object);

        TypeReference<HashMap<String, Object>> mapType =
                new TypeReference<HashMap<String, Object>>()
                    {
                    };

        Map<String, Object> mapFromObject = new ObjectMapper().readValue(jsonFromObject, mapType);

        Assert.assertEquals(mapFromObject, expectedMap);
    }
}
