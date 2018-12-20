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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.CreationId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;

/**
 * @author Franz-Josef Elmer
 */
public class SampleFreezingTest extends FreezingTest
{
    private static final String PREFIX = "SFT-";

    private static final String SAMPLE_1 = PREFIX + "1";

    private static final String SAMPLE_COMP = PREFIX + "COMP";

    private static final String SAMPLE_CHILD = PREFIX + "CHILD";

    private static final String SAMPLE_PARENT_CONT = PREFIX + "PARENT_CONT";

    private SamplePermId sample1;

    private SamplePermId sampleComp;

    private SamplePermId sampleChild;

    private SamplePermId sampleParentCont;

    private TagPermId blueTag;

    @BeforeMethod
    public void createSampleExamples()
    {
        SampleCreation s1 = cellPlate(SAMPLE_1);
        s1.setProperty("SIZE", "1042");
        s1.setAttachments(Arrays.asList(attachment("f1.txt", "T1", "my t1", "abcdefgh")));
        SampleCreation s2 = cellPlate(SAMPLE_COMP);
        SampleCreation s3 = cellPlate(SAMPLE_CHILD);
        SampleCreation s4 = cellPlate(SAMPLE_PARENT_CONT);
        s4.setCreationId(new CreationId("s4"));
        s2.setContainerId(s4.getCreationId());
        s3.setParentIds(Arrays.asList(s4.getCreationId()));

        List<SamplePermId> sampleIds = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2, s3, s4));
        sample1 = sampleIds.get(0);
        sampleComp = sampleIds.get(1);
        sampleChild = sampleIds.get(2);
        sampleParentCont = sampleIds.get(3);

        TagCreation tagCreation = new TagCreation();
        tagCreation.setCode("blue");
        tagCreation.setSampleIds(Arrays.asList(sampleComp));
        blueTag = v3api.createTags(systemSessionToken, Arrays.asList(tagCreation)).get(0);
    }

    @Test
    public void testTrash()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteSamples(systemSessionToken, Arrays.asList(sample1), deletionOptions),
                // Then
                "ERROR: Operation TRASH is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanently()
    {
        // Given
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sample1), deletionOptions);
        setFrozenFlagForSamples(true, sample1);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanentlySampleWithoutPropertiesAndAttachments()
    {
        // Given
        SampleDeletionOptions deletionOptions = new SampleDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteSamples(systemSessionToken, Arrays.asList(sampleComp), deletionOptions);
        setFrozenFlagForSamples(true, sampleComp);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE is not allowed because sample " + SAMPLE_COMP + " is frozen.");
    }

    @Test
    public void testAddTag()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getSample(sample1).getTags().toString(), "[]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getTagIds().add(blueTag);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getTags().toString(), "[Tag blue]");
    }

    @Test
    public void testRemoveTag()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp);
        assertEquals(getSample(sampleComp).getTags().toString(), "[Tag blue]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getTagIds().remove(blueTag);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getTags().toString(), "[]");
    }

    @Test
    public void testAddProperty()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("COMMENT", "my comment");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation INSERT PROPERTY is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testAddPropertyForMoltenSample() throws Exception
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("COMMENT", "my comment");

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getProperty("COMMENT"), "my comment");
    }

    @Test
    public void testChangeProperty()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("SIZE", "1043");

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation UPDATE PROPERTY is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testChangePropertyForMoltenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        assertEquals(getSample(sample1).getProperty("SIZE"), "1042");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("SIZE", "1043");

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getProperty("SIZE"), "1043");
    }

    @Test
    public void testDeleteProperty()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("SIZE", null);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testDeletePropertyForMoltenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.setProperty("SIZE", null);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getProperty("SIZE"), null);
    }

    @Test
    public void testAddAttachment()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getAttachments().add(attachment("f2.txt", "F2", "my f2", "3.14159"));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation INSERT ATTACHMENT is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testAddAttachmentForMoltenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getAttachments().add(attachment("f2.txt", "F2", "my f2", "3.14159"));

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getAttachments().size(), 2);
    }

    @Test
    public void testDeleteAttachment()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getAttachments().remove(new AttachmentFileName("f1.txt"));

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation DELETE ATTACHMENT is not allowed because sample " + SAMPLE_1 + " is frozen.");
    }

    @Test
    public void testDeleteAttachmentForMoltenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        setFrozenFlagForSamples(false, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getAttachments().remove(new AttachmentFileName("f1.txt"));

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getAttachments().size(), 0);
    }

    @Test
    public void testAddLiquidComponentToFrozenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getComponentIds().add(sample1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getComponents().get(0).getCode(), SAMPLE_1);
    }

    @Test
    public void testAddFrozenComponentToLiquidContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getComponentIds().add(sample1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getComponents().get(0).getCode(), SAMPLE_1);
    }

    @Test
    public void testAddFrozenComponentToFrozenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sample1, sampleComp);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getComponentIds().add(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET CONTAINER is not allowed because samples " + SAMPLE_1 + " and " + SAMPLE_COMP + " are frozen.");
    }

    @Test
    public void testAddMoltenComponentToMoltenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sample1, sampleComp);
        setFrozenFlagForSamples(false, sample1, sampleComp);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.getComponentIds().add(sample1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getComponents().get(0).getCode(), SAMPLE_1);
    }

    @Test
    public void testRemoveLiquidComponentFromFrozenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleParentCont);
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[Sample " + sampleComp + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getComponentIds().remove(sampleComp);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[]");
    }

    @Test
    public void testRemoveFrozenComponentFromLiquidContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp);
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[Sample " + sampleComp + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getComponentIds().remove(sampleComp);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[]");
    }

    @Test
    public void testRemoveFrozenComponentFromFrozenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp, sampleParentCont);
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[Sample " + sampleComp + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getComponentIds().remove(sampleComp);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation REMOVE CONTAINER is not allowed because samples " + SAMPLE_COMP + " and "
                        + SAMPLE_PARENT_CONT + " are frozen.");
    }

    @Test
    public void testRemoveMoltenComponentFromMoltenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp, sampleParentCont);
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[Sample " + sampleComp + "]");
        setFrozenFlagForSamples(false, sampleComp, sampleParentCont);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getComponentIds().remove(sampleComp);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleParentCont).getComponents().toString(), "[]");
    }

    @Test
    public void testMoveLiquidComponentFromLiquidContainerToFrozenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_PARENT_CONT);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.setContainerId(sample1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveLiquidComponentFromFrozenContainerToLiquidContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleParentCont);
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_PARENT_CONT);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.setContainerId(sample1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenComponentFromLiquidContainerToLiquidContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp);
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_PARENT_CONT);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.setContainerId(sample1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenComponentFromLiquidContainerToFrozenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp, sample1);
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_PARENT_CONT);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.setContainerId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET CONTAINER is not allowed because samples " + SAMPLE_COMP + " and "
                        + SAMPLE_1 + " are frozen.");
    }

    @Test
    public void testMoveFrozenComponentFromFrozenContainerToLiquidContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp, sampleParentCont);
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_PARENT_CONT);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.setContainerId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation REMOVE CONTAINER is not allowed because samples " + SAMPLE_COMP + " and "
                        + SAMPLE_PARENT_CONT + " are frozen.");
    }

    @Test
    public void testMoveFrozenComponentFromFrozenContainerToFrozenContainer()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp, sample1, sampleParentCont);
        assertEquals(getSample(sampleComp).getContainer().getCode(), SAMPLE_PARENT_CONT);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleComp);
        sampleUpdate.setContainerId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET CONTAINER is not allowed because samples " + SAMPLE_COMP + " and "
                        + SAMPLE_1 + " are frozen.");
    }

    @Test
    public void testAddLiquidChildToFrozenParent()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getChildIds().add(sampleComp);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getChildren().get(0).getCode(), SAMPLE_COMP);
    }

    @Test
    public void testAddFrozenChildToLiquidParent()
    {
        // Given
        setFrozenFlagForSamples(true, sampleComp);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getChildIds().add(sampleComp);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getChildren().get(0).getCode(), SAMPLE_COMP);
    }

    @Test
    public void testAddFrozenChildToFrozenParent()
    {
        // Given
        setFrozenFlagForSamples(true, sample1, sampleComp);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getChildIds().add(sampleComp);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation INSERT is not allowed because samples " + SAMPLE_1 + " and " + SAMPLE_COMP + " are frozen.");
    }

    @Test
    public void testAddMoltenChildToMoltenParent()
    {
        // Given
        setFrozenFlagForSamples(true, sample1, sampleComp);
        setFrozenFlagForSamples(false, sample1, sampleComp);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sample1);
        sampleUpdate.getChildIds().add(sampleComp);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sample1).getChildren().get(0).getCode(), SAMPLE_COMP);
    }

    @Test
    public void testRemoveLiquidChildFromFrozenParent()
    {
        // Given
        setFrozenFlagForSamples(true, sampleParentCont);
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[Sample " + sampleChild + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getChildIds().remove(sampleChild);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[]");
    }

    @Test
    public void testRemoveFrozenChildFromLiquidParent()
    {
        // Given
        setFrozenFlagForSamples(true, sampleChild);
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[Sample " + sampleChild + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getChildIds().remove(sampleChild);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[]");
    }

    @Test
    public void testRemoveFrozenChildFromFrozenParent()
    {
        // Given
        setFrozenFlagForSamples(true, sampleChild, sampleParentCont);
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[Sample " + sampleChild + "]");
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getChildIds().remove(sampleChild);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation DELETE is not allowed because samples " + SAMPLE_PARENT_CONT + " and " + SAMPLE_CHILD + " are frozen.");
    }

    @Test
    public void testRemoveMoltenChildFromMoltenParent()
    {
        // Given
        setFrozenFlagForSamples(true, sampleChild, sampleParentCont);
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[Sample " + sampleChild + "]");
        setFrozenFlagForSamples(false, sampleChild, sampleParentCont);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sampleParentCont);
        sampleUpdate.getChildIds().remove(sampleChild);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sampleParentCont).getChildren().toString(), "[]");
    }

}
