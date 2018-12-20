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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class SpaceExperimentSampleRelationshipsFreezingTest extends FreezingTest
{
    private static final String PREFIX = "SESRFT-";

    private static final String SPACE_1 = PREFIX + "S1";

    private static final String EXPERIMENT_1 = PREFIX + "E1";

    private static final String SPACE_OF_SAMPLE = PREFIX + "SPACE-SAMP";

    private static final String EXP_OF_SAMPLE = PREFIX + "EXP-SAMP";

    private static final String SHARED_SAMPLE = PREFIX + "SAMP-SHARED";

    private static final String SPACE_SAMPLE = PREFIX + "SAMP-SPACE";

    private static final String EXPE_SAMPLE = PREFIX + "SAMP-EXPE";

    private SpacePermId space1;

    private SpacePermId spaceOfSample;

    private ExperimentPermId experiment1;

    private ExperimentPermId expOfSample;

    private SamplePermId sharedSample;

    private SamplePermId spaceSample;

    private SamplePermId experimentSample;

    @BeforeMethod
    public void createExamples()
    {
        List<SpacePermId> spaces = v3api.createSpaces(systemSessionToken, Arrays.asList(space(SPACE_1), space(SPACE_OF_SAMPLE)));
        space1 = spaces.get(0);
        spaceOfSample = spaces.get(1);

        List<ProjectPermId> projects = v3api.createProjects(systemSessionToken, Arrays.asList(project(spaceOfSample, "P")));
        ProjectPermId p = projects.get(0);

        List<ExperimentPermId> experiments = v3api.createExperiments(systemSessionToken,
                Arrays.asList(experiment(p, EXPERIMENT_1), experiment(p, EXP_OF_SAMPLE)));
        experiment1 = experiments.get(0);
        expOfSample = experiments.get(1);

        SampleCreation p1 = cellPlate(SHARED_SAMPLE);
        p1.setSpaceId(null);
        SampleCreation p2 = cellPlate(SPACE_SAMPLE);
        p2.setSpaceId(spaceOfSample);
        SampleCreation p3 = cellPlate(EXPE_SAMPLE);
        p3.setSpaceId(spaceOfSample);
        p3.setExperimentId(expOfSample);
        List<SamplePermId> samples = v3api.createSamples(systemSessionToken, Arrays.asList(p1, p2, p3));
        ;
        sharedSample = samples.get(0);
        spaceSample = samples.get(1);
        experimentSample = samples.get(2);
    }

    @Test
    public void testAddLiquidSharedSampleToFrozenSpace()
    {
        // Given
        setFrozenFlagForSpaces(true, space1);
        assertEquals(getSample(sharedSample).getSpace(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sharedSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testAddFrozenSharedSampleToLiquidSpace()
    {
        // Given
        setFrozenFlagForSamples(true, sharedSample);
        assertEquals(getSample(sharedSample).getSpace(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sharedSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testAddFrozenSharedSampleToFrozenSpace()
    {
        // Given
        setFrozenFlagForSpaces(true, space1);
        setFrozenFlagForSamples(true, sharedSample);
        assertEquals(getSample(sharedSample).getSpace(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setSpaceId(space1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET SPACE is not allowed because sample " + SHARED_SAMPLE + " and space " + SPACE_1 + " are frozen.");
    }

    @Test
    public void testAddMoltenSharedSampleToMoltenSpace()
    {
        // Given
        setFrozenFlagForSpaces(true, space1);
        setFrozenFlagForSamples(true, sharedSample);
        assertEquals(getSample(sharedSample).getSpace(), null);
        setFrozenFlagForSpaces(false, space1);
        setFrozenFlagForSamples(false, sharedSample);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(sharedSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(sharedSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testAddLiquidSpaceSampleToFrozenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getSample(spaceSample).getExperiment(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testAddFrozenSpaceSampleToLiquidExperiment()
    {
        // Given
        setFrozenFlagForSamples(true, spaceSample);
        assertEquals(getSample(spaceSample).getExperiment(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testAddFrozenSpaceSampleToFrozenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        setFrozenFlagForSamples(true, spaceSample);
        assertEquals(getSample(spaceSample).getExperiment(), null);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET EXPERIMENT is not allowed because sample " + SPACE_SAMPLE + " and experiment " + EXPERIMENT_1 + " are frozen.");
    }

    @Test
    public void testAddMoltenSpaceSampleToMoltenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        setFrozenFlagForSamples(true, spaceSample);
        assertEquals(getSample(spaceSample).getExperiment(), null);
        setFrozenFlagForExperiments(false, experiment1);
        setFrozenFlagForSamples(false, spaceSample);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testMoveLiquidSampleFromLiquidSpaceToFrozenSpace()
    {
        // Given
        setFrozenFlagForSpaces(true, space1);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testMoveLiquidSampleFromFrozenSpaceToLiquidSpace()
    {
        // Given
        setFrozenFlagForSpaces(true, spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testMoveLiquidSampleFromFrozenSpaceToFrozenSpace()
    {
        // Given
        setFrozenFlagForSpaces(true, space1, spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testMoveFrozenSampleFromLiquidSpaceToLiquidSpace()
    {
        // Given
        setFrozenFlagForSamples(true, spaceSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testMoveFrozenSampleFromLiquidSpaceToFrozenSpace()
    {
        // Given
        setFrozenFlagForSamples(true, spaceSample);
        setFrozenFlagForSpaces(true, space1);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET SPACE is not allowed because sample " + SPACE_SAMPLE + " and space " + SPACE_1 + " are frozen.");
    }

    @Test
    public void testMoveMoltenSampleFromLiquidSpaceToMoltenSpace()
    {
        // Given
        setFrozenFlagForSamples(true, spaceSample);
        setFrozenFlagForSpaces(true, space1);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        setFrozenFlagForSamples(false, spaceSample);
        setFrozenFlagForSpaces(false, space1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testMoveFrozenSampleFromFrozenSpaceToLiquidSpace()
    {
        // Given
        setFrozenFlagForSamples(true, spaceSample);
        setFrozenFlagForSpaces(true, spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation REMOVE SPACE is not allowed because sample " + SPACE_SAMPLE + " and space " + SPACE_OF_SAMPLE + " are frozen.");
    }

    @Test
    public void testMoveMoltenSampleFromMoltenSpaceToLiquidSpace()
    {
        // Given
        setFrozenFlagForSamples(true, spaceSample);
        setFrozenFlagForSpaces(true, spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        setFrozenFlagForSamples(false, spaceSample);
        setFrozenFlagForSpaces(false, spaceOfSample);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testMoveFrozenSampleFromFrozenSpaceToFrozenSpace()
    {
        // Given
        setFrozenFlagForSamples(true, spaceSample);
        setFrozenFlagForSpaces(true, space1, spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET SPACE is not allowed because sample " + SPACE_SAMPLE + " and space " + SPACE_1 + " are frozen.");
    }

    @Test
    public void testMoveMoltenSampleFromMoltenSpaceToMoltenSpace()
    {
        // Given
        setFrozenFlagForSamples(true, spaceSample);
        setFrozenFlagForSpaces(true, space1, spaceOfSample);
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_OF_SAMPLE);
        setFrozenFlagForSamples(false, spaceSample);
        setFrozenFlagForSpaces(false, space1, spaceOfSample);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(spaceSample);
        sampleUpdate.setSpaceId(space1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(spaceSample).getSpace().getCode(), SPACE_1);
    }

    @Test
    public void testMoveLiquidSampleFromLiquidExperimentToFrozenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testMoveLiquidSampleFromFrozenExperimentToLiquidExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testMoveLiquidSampleFromFrozenExperimentToFrozenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1, expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testMoveFrozenSampleFromLiquidExperimentToLiquidExperiment()
    {
        // Given
        setFrozenFlagForSamples(true, experimentSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testMoveFrozenSampleFromLiquidExperimentToFrozenExperiment()
    {
        // Given
        setFrozenFlagForSamples(true, experimentSample);
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET EXPERIMENT is not allowed because sample " + EXPE_SAMPLE + " and experiment " + EXPERIMENT_1 + " are frozen.");
    }

    @Test
    public void testMoveMoltenSampleFromLiquidExperimentToMoltenExperiment()
    {
        // Given
        setFrozenFlagForSamples(true, experimentSample);
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        setFrozenFlagForSamples(false, experimentSample);
        setFrozenFlagForExperiments(false, experiment1);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testMoveFrozenSampleFromFrozenExperimentToLiquidExperiment()
    {
        // Given
        setFrozenFlagForSamples(true, experimentSample);
        setFrozenFlagForExperiments(true, expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation REMOVE EXPERIMENT is not allowed because sample " + EXPE_SAMPLE + " and experiment " + EXP_OF_SAMPLE
                        + " are frozen.");
    }

    @Test
    public void testMoveMoltenSampleFromMoltenExperimentToLiquidExperiment()
    {
        // Given
        setFrozenFlagForSamples(true, experimentSample);
        setFrozenFlagForExperiments(true, expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        setFrozenFlagForSamples(false, experimentSample);
        setFrozenFlagForExperiments(false, expOfSample);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

    @Test
    public void testMoveFrozenSampleFromFrozenExperimentToFrozenExperiment()
    {
        // Given
        setFrozenFlagForSamples(true, experimentSample);
        setFrozenFlagForExperiments(true, experiment1, expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        assertUserFailureException(Void -> v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate)),
                // Then
                "ERROR: Operation SET EXPERIMENT is not allowed because sample " + EXPE_SAMPLE + " and experiment " + EXPERIMENT_1 + " are frozen.");
    }

    @Test
    public void testMoveMoltenSampleFromMoltenExperimentToMoltenExperiment()
    {
        // Given
        setFrozenFlagForSamples(true, experimentSample);
        setFrozenFlagForExperiments(true, experiment1, expOfSample);
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXP_OF_SAMPLE);
        setFrozenFlagForSamples(false, experimentSample);
        setFrozenFlagForExperiments(false, experiment1, expOfSample);
        SampleUpdate sampleUpdate = new SampleUpdate();
        sampleUpdate.setSampleId(experimentSample);
        sampleUpdate.setExperimentId(experiment1);

        // When
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));

        // Then
        assertEquals(getSample(experimentSample).getExperiment().getCode(), EXPERIMENT_1);
    }

}
