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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

/**
 * @author Franz-Josef Elmer
 */
public class ProjectExperimentRelationshipsFreezingTest extends FreezingTest
{
    private static final String PREFIX = "PERFT-";

    private static final String SPACE = PREFIX + "S1";

    private static final String PROJECT_1 = PREFIX + "P1";

    private static final String PROJECT_OF_EXP = PREFIX + "P-EXP";

    private static final String EXP = PREFIX + "EXP";

    private ProjectPermId project1;

    private ProjectPermId projectOfExperiment;

    private ExperimentPermId experiment;

    @BeforeMethod
    public void createExamples()
    {
        SpacePermId space = v3api.createSpaces(systemSessionToken, Arrays.asList(space(SPACE))).get(0);
        List<ProjectPermId> projects = v3api.createProjects(systemSessionToken,
                Arrays.asList(project(space, PROJECT_1), project(space, PROJECT_OF_EXP)));
        project1 = projects.get(0);
        projectOfExperiment = projects.get(1);
        experiment = v3api.createExperiments(systemSessionToken,
                Arrays.asList(experiment(projectOfExperiment, EXP))).get(0);
    }

    @Test
    public void testMoveLiquidExperimentFromLiquidProjectToFrozenProject()
    {
        // Given
        setFrozenFlagForProjects(true, project1);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }

    @Test
    public void testMoveLiquidExperimentFromFrozenProjectToLiquidProject()
    {
        // Given
        setFrozenFlagForProjects(true, projectOfExperiment);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }

    @Test
    public void testMoveLiquidExperimentFromFrozenProjectToFrozenProject()
    {
        // Given
        setFrozenFlagForProjects(true, project1, projectOfExperiment);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }
    
    @Test
    public void testMoveFrozenExperimentFromLiquidProjectToLiquidProject()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);
        
        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));
        
        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }
    
    @Test
    public void testMoveFrozenExperimentFromLiquidProjectToFrozenProject()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment);
        setFrozenFlagForProjects(true, project1);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);
        
        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation SET PROJECT is not allowed because experiment " + EXP + " and project " + PROJECT_1 + " are frozen.");
    }
    @Test
    public void testMoveMoltenExperimentFromLiquidProjectToMoltenProject()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment);
        setFrozenFlagForProjects(true, project1);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        setFrozenFlagForExperiments(false, experiment);
        setFrozenFlagForProjects(false, project1);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);
        
        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));
        
        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }
    @Test
    public void testMoveFrozenExperimentFromFrozenProjectToLiquidProject()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment);
        setFrozenFlagForProjects(true, projectOfExperiment);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);
        
        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation REMOVE PROJECT is not allowed because experiment " + EXP + " and project " + PROJECT_OF_EXP + " are frozen.");
    }
    @Test
    public void testMoveMoltenExperimentFromMoltenProjectToLiquidProject()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment);
        setFrozenFlagForProjects(true, projectOfExperiment);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        setFrozenFlagForExperiments(false, experiment);
        setFrozenFlagForProjects(false, projectOfExperiment);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);
        
        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));
        
        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }
    @Test
    public void testMoveFrozenExperimentFromFrozenProjectToFrozenProject()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment);
        setFrozenFlagForProjects(true, project1, projectOfExperiment);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);
        
        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation SET PROJECT is not allowed because experiment " + EXP + " and project " + PROJECT_1 + " are frozen.");
    }
    @Test
    public void testMoveMoltenExperimentFromMoltenProjectToMoltenProject()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment);
        setFrozenFlagForProjects(true, project1, projectOfExperiment);
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_OF_EXP);
        setFrozenFlagForExperiments(false, experiment);
        setFrozenFlagForProjects(false, project1, projectOfExperiment);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment);
        experimentUpdate.setProjectId(project1);
        
        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));
        
        // Then
        assertEquals(getExperiment(experiment).getProject().getCode(), PROJECT_1);
    }
}
