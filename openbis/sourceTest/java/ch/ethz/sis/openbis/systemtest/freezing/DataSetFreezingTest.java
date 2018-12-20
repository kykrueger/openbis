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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;

/**
 * @author Franz-Josef Elmer
 */
public class DataSetFreezingTest extends FreezingTest
{
    private static final String PREFIX = "DSFT-";

    private static final String DATA_SET_1 = PREFIX + "1";

    private static final String DATA_SET_2 = PREFIX + "2";

    private TagPermId blueTag;

    private DataSetPermId dataSet1;

    private DataSetPermId dataSet2;

    @BeforeMethod
    public void createDataSetExamples()
    {
        DataSetCreation ds1 = dataSet(DATA_SET_1);
        SamplePermId sampleId = new SamplePermId("200811050929940-1018");
        ds1.setSampleId(sampleId);
        ds1.setDataSetKind(DataSetKind.CONTAINER);
        ds1.setProperty("DESCRIPTION", "testing");
        DataSetCreation ds2 = physicalDataSet(DATA_SET_2);
        ds2.setSampleId(sampleId);

        List<DataSetPermId> dataSetIds = v3api.createDataSets(systemSessionToken, Arrays.asList(ds1, ds2));
        dataSet1 = dataSetIds.get(0);
        dataSet2 = dataSetIds.get(1);

        TagCreation tagCreation = new TagCreation();
        tagCreation.setCode("blue");
        tagCreation.setDataSetIds(Arrays.asList(dataSet2));
        blueTag = v3api.createTags(systemSessionToken, Arrays.asList(tagCreation)).get(0);
    }

    @Test
    public void testTrash()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet1), deletionOptions),
                // Then
                "ERROR: Operation TRASH is not allowed because data set " + DATA_SET_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanently()
    {
        // Given
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet1), deletionOptions);
        setFrozenFlagForDataSets(true, dataSet1);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because data set " + DATA_SET_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanentlyDataSetWithoutProperties()
    {
        // Given
        DataSetDeletionOptions deletionOptions = new DataSetDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteDataSets(systemSessionToken, Arrays.asList(dataSet2), deletionOptions);
        setFrozenFlagForDataSets(true, dataSet2);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE is not allowed because data set " + DATA_SET_2 + " is frozen.");
    }

    @Test
    public void testAddTag()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        assertEquals(getDataSet(dataSet1).getTags().toString(), "[]");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getTagIds().add(blueTag);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getTags().toString(), "[Tag blue]");
    }

    @Test
    public void testRemoveTag()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet2);
        assertEquals(getDataSet(dataSet2).getTags().toString(), "[Tag blue]");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet2);
        dataSetUpdate.getTagIds().remove(blueTag);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet2).getTags().toString(), "[]");
    }

    @Test
    public void testAddProperty()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet2);
        dataSetUpdate.setProperty("DESCRIPTION", "my description");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT PROPERTY is not allowed because data set " + DATA_SET_2 + " is frozen.");
    }

    @Test
    public void testAddPropertyToMoltenDataSet()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet2);
        setFrozenFlagForDataSets(false, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet2);
        dataSetUpdate.setProperty("DESCRIPTION", "my description");

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet2).getProperty("DESCRIPTION"), "my description");
    }

    @Test
    public void testChangeProperty()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.setProperty("DESCRIPTION", "my description");

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation UPDATE PROPERTY is not allowed because data set " + DATA_SET_1 + " is frozen.");
    }

    @Test
    public void testChangePropertyForMoltenDataSet()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        setFrozenFlagForDataSets(false, dataSet1);
        assertEquals(getDataSet(dataSet1).getProperty("DESCRIPTION"), "testing");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.setProperty("DESCRIPTION", "my description");

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getProperty("DESCRIPTION"), "my description");
    }

    @Test
    public void testDeleteProperty()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.setProperty("DESCRIPTION", null);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because data set " + DATA_SET_1 + " is frozen.");
    }

    @Test
    public void testDeletePropertyForMoltenDataSet()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        setFrozenFlagForDataSets(false, dataSet1);
        assertEquals(getDataSet(dataSet1).getProperty("DESCRIPTION"), "testing");
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.setProperty("DESCRIPTION", null);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getProperty("DESCRIPTION"), null);
    }

    @Test
    public void testAddLiquidComponentToFrozenContainer()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
    }

    @Test
    public void testAddFrozenComponentToLiquidContainer()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
    }

    @Test
    public void testAddFrozenComponentToFrozenContainer()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT is not allowed because data sets " + DATA_SET_1 + " and " + DATA_SET_2 + " are frozen.");
    }

    @Test
    public void testAddMoltenComponentToMoltenContainer()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1, dataSet2);
        setFrozenFlagForDataSets(false, dataSet1, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
    }

    @Test
    public void testRemoveLiquidComponentFromFrozenContainer()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
        setFrozenFlagForDataSets(true, dataSet1);

        dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().toString(), "[]");
    }

    @Test
    public void testRemoveFrozenComponentFromLiquidContainer()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
        setFrozenFlagForDataSets(true, dataSet2);

        dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().toString(), "[]");
    }

    @Test
    public void testRemoveFrozenComponentFromFrozenContainer()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
        setFrozenFlagForDataSets(true, dataSet1, dataSet2);

        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet1);
        dataSetUpdate2.getComponentIds().remove(dataSet2);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate2)),
                // Then
                "ERROR: Operation DELETE is not allowed because data sets " + DATA_SET_1 + " and " + DATA_SET_2 + " are frozen.");
    }

    @Test
    public void testRemoveMoltenComponentFromMoltenContainer()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getComponentIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getComponents().get(0).getCode(), DATA_SET_2);
        setFrozenFlagForDataSets(true, dataSet1, dataSet2);
        setFrozenFlagForDataSets(false, dataSet1, dataSet2);

        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet1);
        dataSetUpdate2.getComponentIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate2));

        // Then
        assertEquals(getDataSet(dataSet1).getComponents().toString(), "[]");
    }

    @Test
    public void testAddLiquidChildToFrozenParent()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
    }

    @Test
    public void testAddFrozenChildToLiquidParent()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
    }

    @Test
    public void testAddFrozenChildToFrozenParent()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation INSERT is not allowed because data sets " + DATA_SET_1 + " and " + DATA_SET_2 + " are frozen.");
    }

    @Test
    public void testAddMoltenChildToMoltenParent()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSet1, dataSet2);
        setFrozenFlagForDataSets(false, dataSet1, dataSet2);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
    }

    @Test
    public void testRemoveLiquidChildFromFrozenParent()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
        setFrozenFlagForDataSets(true, dataSet1);

        dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().toString(), "[]");
    }

    @Test
    public void testRemoveFrozenChildFromLiquidParent()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
        setFrozenFlagForDataSets(true, dataSet2);

        dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().toString(), "[]");
    }

    @Test
    public void testRemoveFrozenChildFromFrozenParent()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
        setFrozenFlagForDataSets(true, dataSet1, dataSet2);

        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet1);
        dataSetUpdate2.getChildIds().remove(dataSet2);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate2)),
                // Then
                "ERROR: Operation DELETE is not allowed because data sets " + DATA_SET_1 + " and " + DATA_SET_2 + " are frozen.");
    }

    @Test
    public void testRemoveMoltenChildFromMoltenParent()
    {
        // Given
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSet1);
        dataSetUpdate.getChildIds().add(dataSet2);
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));
        assertEquals(getDataSet(dataSet1).getChildren().get(0).getCode(), DATA_SET_2);
        setFrozenFlagForDataSets(true, dataSet1, dataSet2);
        setFrozenFlagForDataSets(false, dataSet1, dataSet2);

        DataSetUpdate dataSetUpdate2 = new DataSetUpdate();
        dataSetUpdate2.setDataSetId(dataSet1);
        dataSetUpdate2.getChildIds().remove(dataSet2);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate2));

        // Then
        assertEquals(getDataSet(dataSet1).getChildren().toString(), "[]");
    }

}
