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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentSampleDataSetRelationshipsFreezingTest extends FreezingTest
{
    private static final String PREFIX = "ESDSRFT-";

    private static final String SAMPLE_1 = PREFIX + "S1";

    private static final String EXP_OF_DATA_SET = PREFIX + "EXP-DS";

    private static final String SAMP_OF_DATA_SET = PREFIX + "SAMP-DS";

    private static final String DATA_SET_EXP = PREFIX + "DS-EXP";

    private static final String DATA_SET_SAMP = PREFIX + "DS-SAMP";

    private ExperimentPermId experimentOfDataSet;

    private SamplePermId sample1;

    private SamplePermId sampleOfDataSet;

    private DataSetPermId dataSetExp;

    private DataSetPermId dataSetSamp;

    @BeforeMethod
    public void createExamples()
    {
        experimentOfDataSet = v3api.createExperiments(systemSessionToken, 
                Arrays.asList(experiment(DEFAULT_PROJECT_ID, EXP_OF_DATA_SET))).get(0);
        SampleCreation s1 = cellPlate(SAMPLE_1);
        SampleCreation s2 = cellPlate(SAMP_OF_DATA_SET);
        List<SamplePermId> samples = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2));
        sample1 = samples.get(0);
        sampleOfDataSet = samples.get(1);

        DataSetCreation ds1 = physicalDataSet(DATA_SET_EXP);
        ds1.setExperimentId(experimentOfDataSet);
        DataSetCreation ds2 = physicalDataSet(DATA_SET_SAMP);
        ds2.setSampleId(sampleOfDataSet);
        List<DataSetPermId> dataSets = v3api.createDataSets(systemSessionToken, Arrays.asList(ds1, ds2));
        dataSetExp = dataSets.get(0);
        dataSetSamp = dataSets.get(1);
    }

    @Test
    public void testMoveLiquidDataSetFromLiquidSampleToFrozenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveLiquidDataSetFromFrozenSampleToLiquidSample()
    {
        // Given
        setFrozenFlagForSamples(true, sampleOfDataSet);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveLiquidDataSetFromFrozenSampleToFrozenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sampleOfDataSet, sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenDataSetFromLiquidSampleToLiquidSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetSamp);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenDataSetFromLiquidSampleToFrozenSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetSamp);
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation SET SAMPLE is not allowed because data set " + DATA_SET_SAMP + " and sample "
                        + SAMPLE_1 + " are frozen.");
    }

    @Test
    public void testMoveMoltenDataSetFromLiquidSampleToMoltenSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetSamp);
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        setFrozenFlagForDataSets(false, dataSetSamp);
        setFrozenFlagForSamples(false, sample1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenDataSetFromFrozenSampleToLiquidSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetSamp);
        setFrozenFlagForSamples(true, sampleOfDataSet);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation REMOVE SAMPLE is not allowed because data set " + DATA_SET_SAMP + " and sample "
                        + SAMP_OF_DATA_SET + " are frozen.");
    }

    @Test
    public void testMoveMoltenDataSetFromMoltenSampleToLiquidSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetSamp);
        setFrozenFlagForSamples(true, sampleOfDataSet);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        setFrozenFlagForDataSets(false, dataSetSamp);
        setFrozenFlagForSamples(false, sampleOfDataSet);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenDataSetFromFrozenSampleToFrozenSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetSamp);
        setFrozenFlagForSamples(true, sampleOfDataSet, sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation SET SAMPLE is not allowed because data set " + DATA_SET_SAMP + " and sample "
                        + SAMPLE_1 + " are frozen.");
    }

    @Test
    public void testMoveMoltenDataSetFromMoltenSampleToMoltenSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetSamp);
        setFrozenFlagForSamples(true, sampleOfDataSet, sample1);
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMP_OF_DATA_SET);
        setFrozenFlagForDataSets(false, dataSetSamp);
        setFrozenFlagForSamples(false, sampleOfDataSet, sample1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetSamp);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetSamp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveLiquidDataSetFromLiquidExperimentToFrozenSample()
    {
        // Given
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveLiquidDataSetFromFrozenExperimentToLiquidSample()
    {
        // Given
        setFrozenFlagForExperiments(true, experimentOfDataSet);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveLiquidDataSetFromFrozenExperimentToFrozenSample()
    {
        // Given
        setFrozenFlagForExperiments(true, experimentOfDataSet);
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenDataSetFromLiquidExperimentToLiquidSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetExp);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenDataSetFromLiquidExperimentToFrozenSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetExp);
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation SET SAMPLE is not allowed because data set " + DATA_SET_EXP + " and sample "
                        + SAMPLE_1 + " are frozen.");
    }

    @Test
    public void testMoveMoltenDataSetFromLiquidExperimentToMoltenSample()
    {
        // Given
        setFrozenFlagForDataSets(true, dataSetExp);
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        setFrozenFlagForDataSets(false, dataSetExp);
        setFrozenFlagForSamples(false, sample1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenDataSetFromFrozenExperimentToLiquidSample()
    {
        // Given
        setFrozenFlagForExperiments(true, experimentOfDataSet);
        setFrozenFlagForDataSets(true, dataSetExp);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation REMOVE EXPERIMENT is not allowed because data set " + DATA_SET_EXP + " and experiment "
                        + EXP_OF_DATA_SET + " are frozen.");
    }

    @Test
    public void testMoveMoltenDataSetFromMoltenExperimentToLiquidSample()
    {
        // Given
        setFrozenFlagForExperiments(true, experimentOfDataSet);
        setFrozenFlagForDataSets(true, dataSetExp);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        setFrozenFlagForExperiments(false, experimentOfDataSet);
        setFrozenFlagForDataSets(false, dataSetExp);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getSample().getCode(), SAMPLE_1);
    }

    @Test
    public void testMoveFrozenDataSetFromFrozenExperimentToFrozenSample()
    {
        // Given
        setFrozenFlagForExperiments(true, experimentOfDataSet);
        setFrozenFlagForDataSets(true, dataSetExp);
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        assertUserFailureException(Void -> v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate)),
                // Then
                "ERROR: Operation REMOVE EXPERIMENT is not allowed because data set " + DATA_SET_EXP + " and experiment "
                        + EXP_OF_DATA_SET + " are frozen.");
    }

    @Test
    public void testMoveMoltenDataSetFromMoltenExperimentToMoltenSample()
    {
        // Given
        setFrozenFlagForExperiments(true, experimentOfDataSet);
        setFrozenFlagForDataSets(true, dataSetExp);
        setFrozenFlagForSamples(true, sample1);
        assertEquals(getDataSet(dataSetExp).getExperiment().getCode(), EXP_OF_DATA_SET);
        setFrozenFlagForExperiments(false, experimentOfDataSet);
        setFrozenFlagForDataSets(false, dataSetExp);
        setFrozenFlagForSamples(false, sample1);
        DataSetUpdate dataSetUpdate = new DataSetUpdate();
        dataSetUpdate.setDataSetId(dataSetExp);
        dataSetUpdate.setExperimentId(null);
        dataSetUpdate.setSampleId(sample1);

        // When
        v3api.updateDataSets(systemSessionToken, Arrays.asList(dataSetUpdate));

        // Then
        assertEquals(getDataSet(dataSetExp).getSample().getCode(), SAMPLE_1);
    }

}
