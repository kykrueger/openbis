/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IEntityType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.plugin.id.IPluginId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.semanticannotation.id.SemanticAnnotationPermId;

/**
 * @author pkupczyk
 */
public class GetSampleTypeTest extends AbstractGetEntityTypeTest
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected EntityTypePermId createEntityType(String sessionToken, String entityTypeCode, List<PropertyAssignmentCreation> properties,
            IPluginId validationPluginId)
    {
        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode(entityTypeCode);
        creation.setPropertyAssignments(properties);
        creation.setValidationPluginId(validationPluginId);

        List<EntityTypePermId> permIds = v3api.createSampleTypes(sessionToken, Arrays.asList(creation));
        return permIds.get(0);
    }

    @Override
    protected FetchOptions<?> createFetchOptions(boolean withProperties, boolean withValidationPlugin)
    {
        SampleTypeFetchOptions fo = new SampleTypeFetchOptions();
        if (withProperties)
        {
            fo.withPropertyAssignments().withEntityType();
            fo.withPropertyAssignments().withPropertyType();
        }
        if (withValidationPlugin)
        {
            fo.withValidationPlugin();
        }
        return fo;
    }

    @Override
    protected Map<IEntityTypeId, ? extends IEntityType> getEntityTypes(String sessionToken, List<? extends IEntityTypeId> entityTypeIds,
            FetchOptions<?> fetchOptions)
    {
        return v3api.getSampleTypes(sessionToken, entityTypeIds, (SampleTypeFetchOptions) fetchOptions);
    }

    @Test
    public void testGetByIdsWithBasicFields()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode("TEST_SAMPLE_TYPE");
        creation.setAutoGeneratedCode(true);
        creation.setDescription("test description");
        creation.setGeneratedCodePrefix("test_prefix");
        creation.setListable(true);
        creation.setShowContainer(true);
        creation.setShowParentMetadata(true);
        creation.setSubcodeUnique(true);

        List<EntityTypePermId> permIds = v3api.createSampleTypes(sessionToken, Arrays.asList(creation));
        Map<IEntityTypeId, SampleType> types = v3api.getSampleTypes(sessionToken, permIds, new SampleTypeFetchOptions());

        SampleType type = types.values().iterator().next();
        assertEquals(type.getPermId().getPermId(), creation.getCode());
        assertEquals(type.getCode(), creation.getCode());
        assertEquals(type.isAutoGeneratedCode(), (Boolean) creation.isAutoGeneratedCode());
        assertEquals(type.getDescription(), creation.getDescription());
        assertEquals(type.getGeneratedCodePrefix(), creation.getGeneratedCodePrefix());
        assertEquals(type.isListable(), (Boolean) creation.isListable());
        assertEquals(type.isShowContainer(), (Boolean) creation.isShowContainer());
        assertEquals(type.isShowParentMetadata(), (Boolean) creation.isShowParentMetadata());
        assertEquals(type.isSubcodeUnique(), (Boolean) creation.isSubcodeUnique());

        assertPropertyAssignmentsNotFetched(type);
        assertValidationPluginNotFetched(type);
        assertSemanticAnnotationsNotFetched(type);
    }

    @Test
    public void testGetByIdsWithSemanticAnnotations()
    {
        EntityTypePermId permId = new EntityTypePermId("DILUTION_PLATE");

        SampleTypeFetchOptions fetchOptions = new SampleTypeFetchOptions();
        fetchOptions.withSemanticAnnotations();

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IEntityTypeId, SampleType> map = v3api.getSampleTypes(sessionToken, Arrays.asList(permId), fetchOptions);

        SampleType type = map.get(permId);

        assertEquals(type.getPermId().getPermId(), permId.getPermId());
        assertEquals(type.getSemanticAnnotations().size(), 1);
        assertEquals(type.getSemanticAnnotations().get(0).getPermId(), new SemanticAnnotationPermId("ST_DILUTION_PLATE"));

        assertPropertyAssignmentsNotFetched(type);
        assertValidationPluginNotFetched(type);
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeFetchOptions fo = new SampleTypeFetchOptions();
        fo.withPropertyAssignments();

        v3api.getSampleTypes(sessionToken, Arrays.asList(new EntityTypePermId("MASTER_PLATE"), new EntityTypePermId("WELL")), fo);

        assertAccessLog(
                "get-sample-types  SAMPLE_TYPE_IDS('[MASTER_PLATE, WELL]') FETCH_OPTIONS('SampleType\n    with PropertyAssignments\n')");
    }

}
