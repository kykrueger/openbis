/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import java.util.Collections;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.deletion.material.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * @author Jakub Straszewski
 */
public class DeleteMaterialsTest extends AbstractDeletionTest
{

    private static MaterialDeletionOptions options;

    public static MaterialDeletionOptions getOptions()
    {
        if (options == null)
        {
            options = new MaterialDeletionOptions();
            options.setReason("Just for testing");
        }
        return options;
    }

    @Test
    public void testDeleteMaterial()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        MaterialPermId materialId = new MaterialPermId("796", "GENE");
        v3api.deleteMaterials(sessionToken, Collections.singletonList(materialId), getOptions());
        assertMaterialDoesNotExist(materialId);
    }

    @Test
    public void testDeleteMaterialLinkedAsAProperty()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        final MaterialPermId materialId = new MaterialPermId("BACTERIUM-X", "BACTERIUM");

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    v3api.deleteMaterials(sessionToken, Collections.singletonList(materialId), getOptions());
                }
            }, "BACTERIUM-X (BACTERIUM) is being used. Delete all connected data  first.");

    }
}
