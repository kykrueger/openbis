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

package ch.systemsx.cisd.openbis.common.api.server.json.introspector;

import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.openbis.common.api.server.json.common.JsonConstants;
import ch.systemsx.cisd.openbis.common.api.server.json.mapping.IJsonClassValueToClassObjectsMapping;
import ch.systemsx.cisd.openbis.common.api.server.json.resolver.JsonTypeAndClassResolverBuilder;

/**
 * An annotation introspector that returns the custom type resolver which recognizes @type and @class fields. The custom resolver is used only for
 * classes marked with {@link JsonObject} annotation. For standard classes like {@link List} or {@link HashMap} type information is not included.
 * 
 * @author pkupczyk
 */
public class JsonTypeAndClassAnnotationIntrospector extends JacksonAnnotationIntrospector
{

    private IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping;

    public JsonTypeAndClassAnnotationIntrospector(
            IJsonClassValueToClassObjectsMapping classValueToClassObjectsMapping)
    {
        this.classValueToClassObjectsMapping = classValueToClassObjectsMapping;
    }

    @Override
    public String findTypeName(AnnotatedClass ac)
    {
        JsonObject tn = ac.getAnnotation(JsonObject.class);
        return (tn == null) ? null : tn.value();
    }

    @Override
    public ObjectIdInfo findObjectIdInfo(Annotated ann)
    {
        return new ObjectIdInfo(JsonConstants.getIdField(), Object.class,
                ObjectIdGenerators.IntSequenceGenerator.class);
    }

    @Override
    public TypeResolverBuilder<?> findTypeResolver(MapperConfig<?> config, AnnotatedClass ac,
            JavaType baseType)
    {

        if (useCustomResolver(baseType))
        {
            return new JsonTypeAndClassResolverBuilder(classValueToClassObjectsMapping);
        } else
        {
            return super.findTypeResolver(config, ac, baseType);
        }
    }

    private boolean useCustomResolver(JavaType type)
    {
        return type.getRawClass().equals(Object.class)
                || (type.getRawClass().isAnnotationPresent(JsonObject.class) && !type.getRawClass()
                        .isEnum());
    }

    @Override
    public Object findFilterId(AnnotatedClass ac)
    {
        if (ac.hasAnnotation(JsonObject.class))
        {
            // all classed annotated with @JsonObject should use the same filter (the returned filter id does not matter,
            // it just has to be not null for ch.systemsx.cisd.openbis.generic.shared.api.v3.json.OpenbisFilterProvider to be called)
            return new Object();
        } else
        {
            return null;
        }
    }

}
