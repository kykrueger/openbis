/*
 * Copyright 2018 ETH Zuerich, SIS
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

import java.util.Map;
import java.util.TreeMap;

import org.testng.annotations.Test;

/**
 * @author Franz-Josef Elmer
 */
public class GetServerInformationTest extends AbstractTest
{
    @Test
    public void testGetServerInformation()
    {
        // Given
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // When
        Map<String, String> result = v3api.getServerInformation(sessionToken);

        // Then
        assertEquals(new TreeMap<>(result).toString(), "{api-version=3.5, archiving-configured=false, "
                + "authentication-service=dummy-authentication-service, enabled-technologies=test-.*, "
                + "project-samples-enabled=false}");
        v3api.logout(sessionToken);
    }
}
