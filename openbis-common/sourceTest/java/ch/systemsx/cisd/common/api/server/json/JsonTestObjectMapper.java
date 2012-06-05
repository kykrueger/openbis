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

import org.codehaus.jackson.map.ObjectMapper;

import ch.systemsx.cisd.common.api.server.json.deserializer.JsonDeserializerProvider;
import ch.systemsx.cisd.common.api.server.json.introspector.JsonTypeAndClassAnnotationIntrospector;
import ch.systemsx.cisd.common.api.server.json.mapping.JsonReflectionsBaseTypeToSubTypesMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.JsonReflectionsTypeValueToClassObjectMapping;
import ch.systemsx.cisd.common.api.server.json.mapping.JsonStaticClassValueToClassObjectsMapping;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithContainerTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithEnumTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithIgnoredProperties;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNested;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithNestedTypes.ObjectNestedChild;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithPrimitiveTypes;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithPrivateAccess;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithRenamedProperties;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithType;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeAA;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeALegalDuplicate;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeB;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeBIllegalDuplicate;
import ch.systemsx.cisd.common.api.server.json.object.ObjectWithTypeButNoSubtypes;
import ch.systemsx.cisd.common.api.server.json.resolver.JsonReflectionsSubTypeResolver;

/**
 * @author pkupczyk
 */
public class JsonTestObjectMapper extends ObjectMapper
{

    public JsonTestObjectMapper()
    {
        JsonReflectionsTypeValueToClassObjectMapping typeMapping =
                new JsonReflectionsTypeValueToClassObjectMapping(getClass().getPackage().getName());

        JsonReflectionsBaseTypeToSubTypesMapping subTypesMapping =
                new JsonReflectionsBaseTypeToSubTypesMapping(getClass().getPackage().getName());

        JsonStaticClassValueToClassObjectsMapping classMapping =
                new JsonStaticClassValueToClassObjectsMapping();

        classMapping.addClass(".LegacyObjectWithType", ObjectWithType.class);
        classMapping.addClass(".LegacyObjectWithTypeA", ObjectWithTypeA.class);
        classMapping.addClass(".LegacyObjectWithTypeA", ObjectWithTypeALegalDuplicate.class);
        classMapping.addClass(".LegacyObjectWithTypeAA", ObjectWithTypeAA.class);
        classMapping.addClass(".LegacyObjectWithTypeB", ObjectWithTypeB.class);
        classMapping.addClass(".LegacyObjectWithTypeB", ObjectWithTypeBIllegalDuplicate.class);
        classMapping.addClass(".LegacyObjectWithTypeButNoSubtypes",
                ObjectWithTypeButNoSubtypes.class);
        classMapping.addClass(".LegacyObjectNested", ObjectNested.class);
        classMapping.addClass(".LegacyObjectNestedChild", ObjectNestedChild.class);
        classMapping.addClass(".LegacyObjectWithPrimitiveTypes", ObjectWithPrimitiveTypes.class);
        classMapping.addClass(".LegacyObjectWithNestedTypes", ObjectWithNestedTypes.class);
        classMapping.addClass(".LegacyObjectWithEnumTypes", ObjectWithEnumTypes.class);
        classMapping.addClass(".LegacyObjectWithContainerTypes", ObjectWithContainerTypes.class);
        classMapping.addClass(".LegacyObjectWithIgnoredProperties",
                ObjectWithIgnoredProperties.class);
        classMapping.addClass(".LegacyObjectWithRenamedProperties",
                ObjectWithRenamedProperties.class);
        classMapping.addClass(".LegacyObjectWithPrivateAccess", ObjectWithPrivateAccess.class);

        setAnnotationIntrospector(new JsonTypeAndClassAnnotationIntrospector(classMapping));
        setSubtypeResolver(new JsonReflectionsSubTypeResolver(subTypesMapping));
        setDeserializerProvider(new JsonDeserializerProvider(typeMapping, classMapping));
    }

}
