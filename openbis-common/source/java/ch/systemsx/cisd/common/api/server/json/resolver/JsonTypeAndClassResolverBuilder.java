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

package ch.systemsx.cisd.common.api.server.json.resolver;

import java.util.Collection;

import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.impl.StdTypeResolverBuilder;
import org.codehaus.jackson.type.JavaType;

import ch.systemsx.cisd.common.api.server.json.deserializer.JsonTypeAndClassDeserializer;
import ch.systemsx.cisd.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;

/**
 * @author pkupczyk
 */
public class JsonTypeAndClassResolverBuilder extends StdTypeResolverBuilder
{

    private IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping;

    public JsonTypeAndClassResolverBuilder(
            IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping)
    {
        this.classValueToClassObjectsMapping = classValueToClassObjectsMapping;
        init(Id.NAME, null);
        inclusion(As.PROPERTY);
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType,
            Collection<NamedType> subtypes, BeanProperty property)
    {
        TypeIdResolver idRes = idResolver(config, baseType, subtypes, false, true);
        JsonTypeAndClassDeserializer deserializer =
                new JsonTypeAndClassDeserializer(baseType, subtypes, idRes, property, _typeProperty);
        deserializer.setClassValueToClassObjectsMapping(classValueToClassObjectsMapping);
        return deserializer;
    }

}
