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

package ch.ethz.sis.openbis.systemtest.freezing;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.create.SpaceCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.delete.SpaceDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.update.SpaceUpdate;

/**
 * @author Franz-Josef Elmer
 */
public class SpaceFreezingTest extends FreezingTest
{
    private static final String PREFIX = "SFT-";

    private static final String SPACE_1 = PREFIX + "1";
    private static final String SPACE_2 = PREFIX + "2";

    private SpacePermId space1;

    private SpacePermId space2;

    @BeforeMethod
    public void createExamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        SpaceCreation s1 = space(SPACE_1);
        SpaceCreation s2 = space(SPACE_2);
        List<SpacePermId> spaces = v3api.createSpaces(sessionToken, Arrays.asList(s1, s2));
        space1 = spaces.get(0);
        space2 = spaces.get(1);
        v3api.logout(sessionToken);
    }

    @Test
    public void testDelete()
    {
        // Given
        setFrozenFlagForSpaces(true, space2);
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteSpaces(systemSessionToken, Arrays.asList(space2), deletionOptions),
                // Then
                "ERROR: Operation DELETE is not allowed because space " + SPACE_2 + " is frozen.");
    }

    @Test
    public void testDeleteMoltenSpace()
    {
        // Given
        setFrozenFlagForSpaces(true, space2);
        setFrozenFlagForSpaces(false, space2);
        SpaceDeletionOptions deletionOptions = new SpaceDeletionOptions();
        deletionOptions.setReason("test");

        // When
        v3api.deleteSpaces(systemSessionToken, Arrays.asList(space2), deletionOptions);

        // Then
        assertEquals(getSpace(space2), null);
    }

    @Test
    public void testSetDescription()
    {
        // Given
        setFrozenFlagForSpaces(true, space1);
        assertEquals(getSpace(space1).getDescription(), null);
        SpaceUpdate spaceUpdate = new SpaceUpdate();
        spaceUpdate.setSpaceId(space1);
        spaceUpdate.setDescription("hello");

        // When
        assertUserFailureException(Void -> v3api.updateSpaces(systemSessionToken, Arrays.asList(spaceUpdate)),
                // Then
                "ERROR: Operation UPDATE is not allowed because space " + SPACE_1 + " is frozen.");
    }

    @Test
    public void testChangeDescription()
    {
        // Given
        SpaceUpdate spaceUpdate = new SpaceUpdate();
        spaceUpdate.setSpaceId(space1);
        spaceUpdate.setDescription("hello");
        v3api.updateSpaces(systemSessionToken, Arrays.asList(spaceUpdate));

        setFrozenFlagForSpaces(true, space1);
        assertEquals(getSpace(space1).getDescription(), "hello");
        SpaceUpdate spaceUpdate2 = new SpaceUpdate();
        spaceUpdate2.setSpaceId(space1);
        spaceUpdate2.setDescription("hello2");
        
        // When
        assertUserFailureException(Void -> v3api.updateSpaces(systemSessionToken, Arrays.asList(spaceUpdate2)),
                // Then
                "ERROR: Operation UPDATE is not allowed because space " + SPACE_1 + " is frozen.");
    }
    
    @Test
    public void testChangeDescriptionForMoltenSpace()
    {
        // Given
        setFrozenFlagForSpaces(true, space1);
        assertEquals(getSpace(space1).getDescription(), null);
        setFrozenFlagForSpaces(false, space1);
        SpaceUpdate spaceUpdate = new SpaceUpdate();
        spaceUpdate.setSpaceId(space1);
        spaceUpdate.setDescription("hello");

        // When
        v3api.updateSpaces(systemSessionToken, Arrays.asList(spaceUpdate));

        // Then
        assertEquals(getSpace(space1).getDescription(), "hello");
    }

}
