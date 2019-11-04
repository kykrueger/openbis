/*
 * Copyright 2019 ETH Zuerich, SIS
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
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.IObjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;

/**
 * @author Franz-Josef Elmer
 */
public class GetRightsTest extends AbstractTest
{
    @Test
    public void testGetSampleRights()
    {
        // Given
        String sessionToken = v3api.login(TEST_ROLE_V3, PASSWORD);
        IObjectId s1 = new SampleIdentifier("/TEST-SPACE/CP-TEST-4");
        IObjectId s2 = new SampleIdentifier("/CISD/CP-TEST-1");

        // When
        Map<IObjectId, Rights> map = v3api.getRights(sessionToken, Arrays.asList(s1, s2), new RightsFetchOptions());

        // Then
        assertEquals(map.get(s1).getRights().toString(), "[]");
        assertEquals(map.get(s2).getRights().toString(), "[UPDATE]");
    }
}
