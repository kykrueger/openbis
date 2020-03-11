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
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;

/**
 * @author pkupczyk
 */
@Test
public class CreateSampleTypeTest extends CreateEntityTypeTest<SampleTypeCreation, SampleType>
{

    @Override
    protected EntityKind getEntityKind()
    {
        return EntityKind.SAMPLE;
    }

    @Override
    protected SampleTypeCreation newTypeCreation()
    {
        return new SampleTypeCreation();
    }

    @Override
    protected void fillTypeSpecificFields(SampleTypeCreation creation)
    {
        creation.setAutoGeneratedCode(true);
        creation.setSubcodeUnique(true);
        creation.setGeneratedCodePrefix("TEST_PREFIX");
        creation.setListable(true);
        creation.setShowContainer(true);
        creation.setShowParents(true);
        creation.setShowParentMetadata(true);
    }

    @Override
    protected void createTypes(String sessionToken, List<SampleTypeCreation> creations)
    {
        v3api.createSampleTypes(sessionToken, creations);
    }

    @Override
    protected SampleType getType(String sessionToken, String code)
    {
        final SampleTypeFetchOptions fo = new SampleTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        fo.withPropertyAssignments().withRegistrator();

        final EntityTypePermId permId = new EntityTypePermId(code);
        return v3api.getSampleTypes(sessionToken, Collections.singletonList(permId), fo).get(permId);
    }

    @Override
    protected void assertTypeSpecificFields(SampleTypeCreation creation, SampleType type)
    {
        assertEquals(type.isAutoGeneratedCode(), (Boolean) creation.isAutoGeneratedCode());
        assertEquals(type.isSubcodeUnique(), (Boolean) creation.isSubcodeUnique());
        assertEquals(type.getGeneratedCodePrefix(), creation.getGeneratedCodePrefix());
        assertEquals(type.isListable(), (Boolean) creation.isListable());
        assertEquals(type.isShowContainer(), (Boolean) creation.isShowContainer());
        assertEquals(type.isShowParents(), (Boolean) creation.isShowParents());
        assertEquals(type.isShowParentMetadata(), (Boolean) creation.isShowParentMetadata());
    }

    @Test
    public void testLogging()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode("LOG_TEST_1");

        SampleTypeCreation creation2 = new SampleTypeCreation();
        creation2.setCode("LOG_TEST_2");

        v3api.createSampleTypes(sessionToken, Arrays.asList(creation, creation2));

        assertAccessLog(
                "create-sample-types  NEW_SAMPLE_TYPES('[SampleTypeCreation[code=LOG_TEST_1], SampleTypeCreation[code=LOG_TEST_2]]')");
    }

}
