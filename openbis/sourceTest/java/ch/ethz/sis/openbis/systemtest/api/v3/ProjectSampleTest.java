/*
 * Copyright 2015 ETH Zuerich, SIS
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

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleCreation;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.SampleUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.project.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.sample.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.entitytype.EntityTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.IProjectId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.project.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchResult;
import ch.systemsx.cisd.common.action.IDelegatedAction;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Test(groups = "project-samples")
public class ProjectSampleTest extends AbstractTest
{
    @Test
    public void testCreateSampleAndMapSamples()
    {
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode("PROJECT-SAMPLE");
        sampleCreation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        sampleCreation.setSpaceId(new SpacePermId("CISD"));
        ProjectIdentifier projectId = new ProjectIdentifier("/CISD/NEMO");
        sampleCreation.setProjectId(projectId);
        
        List<SamplePermId> ids = v3api.createSamples(systemSessionToken, Arrays.asList(sampleCreation));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, ids, fetchOptions);
        Sample sample = samples.get(ids.get(0));
        assertProjectIdentifiers(Arrays.asList(sample.getProject()), projectId.getIdentifier());
    }
    
    @Test
    public void testCreateThreeSamplesWithSameCodeInDifferentProjectOfSameSpace()
    {
        SampleCreation s1 = new SampleCreation();
        s1.setCode("S1");
        s1.setTypeId(new EntityTypePermId("CELL_PLATE"));
        s1.setSpaceId(new SpacePermId("CISD"));
        ProjectIdentifier projectId1 = new ProjectIdentifier("/CISD/NEMO");
        s1.setProjectId(projectId1);
        SampleCreation s2 = new SampleCreation();
        s2.setCode("S1");
        s2.setTypeId(new EntityTypePermId("CELL_PLATE"));
        s2.setSpaceId(new SpacePermId("CISD"));
        ProjectIdentifier projectId2 = new ProjectIdentifier("/CISD/NOE");
        s2.setProjectId(projectId2);
        SampleCreation s3 = new SampleCreation();
        s3.setCode("S1");
        s3.setTypeId(new EntityTypePermId("CELL_PLATE"));
        s3.setSpaceId(new SpacePermId("CISD"));
        
        List<SamplePermId> ids = v3api.createSamples(systemSessionToken, Arrays.asList(s1, s2, s3));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, ids, fetchOptions);
        assertEquals(samples.get(ids.get(0)).getProject().getIdentifier().toString(), "/CISD/NEMO");
        assertEquals(samples.get(ids.get(1)).getProject().getIdentifier().toString(), "/CISD/NOE");
        assertEquals(samples.get(ids.get(2)).getProject(), null);
        
    }
    
    @Test
    public void testAssignSpaceSampleToAProject()
    {
        SampleUpdate sampleUpdate = new SampleUpdate();
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        sampleUpdate.setSampleId(sampleId);
        ProjectIdentifier projectId = new ProjectIdentifier("/CISD/NEMO");
        sampleUpdate.setProjectId(projectId);
        Date now = new Date();
        
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, Arrays.asList(sampleId), fetchOptions);
        Sample sample = samples.get(sampleId);
        assertNotOlder(sample.getModificationDate(), now);
        assertSpaceCodes(Arrays.asList(sample.getSpace()), "CISD");
        Project project = sample.getProject();
        assertProjectIdentifiers(Arrays.asList(project), projectId.getIdentifier());
        assertNotOlder(project.getModificationDate(), now);
    }
    
    @Test
    public void testAssignProjectSampleToADifferentProjectInTheSameSpace()
    {
        SampleUpdate sampleUpdate = new SampleUpdate();
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        sampleUpdate.setSampleId(sampleId);
        sampleUpdate.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        ProjectIdentifier projectId = new ProjectIdentifier("/CISD/NOE");
        sampleUpdate.setProjectId(projectId);
        Date now = new Date();
        
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, Arrays.asList(sampleId), fetchOptions);
        Sample sample = samples.get(sampleId);
        assertNotOlder(sample.getModificationDate(), now);
        assertSpaceCodes(Arrays.asList(sample.getSpace()), "CISD");
        Project project = sample.getProject();
        assertProjectIdentifiers(Arrays.asList(project), projectId.getIdentifier());
        assertNotOlder(project.getModificationDate(), now);
        Map<IProjectId, Project> projects = v3api.mapProjects(systemSessionToken, Arrays.asList(projectId), 
                new ProjectFetchOptions());
        assertNotOlder(projects.values().iterator().next().getModificationDate(), now);
    }
    
    @Test
    public void testAssignProjectSampleToADifferentProjectInADifferentSpace()
    {
        SampleUpdate sampleUpdate = new SampleUpdate();
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        sampleUpdate.setSampleId(sampleId);
        sampleUpdate.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        sampleUpdate.setSpaceId(new SpacePermId("TEST-SPACE"));
        sampleUpdate.setProjectId(new ProjectIdentifier("/TEST-SPACE/NOE"));
        Date now = new Date();
        
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, 
                Arrays.asList(new SampleIdentifier("/TEST-SPACE/C1")), fetchOptions);
        Sample sample = samples.values().iterator().next();
        assertNotOlder(sample.getModificationDate(), now);
        assertSpaceCodes(Arrays.asList(sample.getSpace()), "TEST-SPACE");
        Project project = sample.getProject();
        assertProjectIdentifiers(Arrays.asList(project), new ProjectIdentifier("/TEST-SPACE/NOE").getIdentifier());
        assertNotOlder(project.getModificationDate(), now);
        Map<IProjectId, Project> projects = v3api.mapProjects(systemSessionToken, 
                Arrays.asList(new ProjectIdentifier("/CISD/NEMO")), new ProjectFetchOptions());
        assertNotOlder(projects.values().iterator().next().getModificationDate(), now);
    }
    
    @Test
    public void testUnassignProjectSampleFromProject()
    {
        SampleUpdate sampleUpdate = new SampleUpdate();
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        sampleUpdate.setSampleId(sampleId);
        sampleUpdate.setProjectId(new ProjectIdentifier("/CISD/NEMO"));
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        sampleUpdate.setProjectId(null);
        Date now = new Date();
        
        v3api.updateSamples(systemSessionToken, Arrays.asList(sampleUpdate));
        
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withSpace();
        fetchOptions.withProject();
        Map<ISampleId, Sample> samples = v3api.mapSamples(systemSessionToken, 
                Arrays.asList(new SampleIdentifier("/CISD/C1")), fetchOptions);
        Sample sample = samples.values().iterator().next();
        assertNotOlder(sample.getModificationDate(), now);
        assertSpaceCodes(Arrays.asList(sample.getSpace()), "CISD");
        Project project = sample.getProject();
        assertEquals(project, null);
        Map<IProjectId, Project> projects = v3api.mapProjects(systemSessionToken, 
                Arrays.asList(new ProjectIdentifier("/CISD/NEMO")), new ProjectFetchOptions());
        assertNotOlder(projects.values().iterator().next().getModificationDate(), now);
    }
    
    @Test
    public void testCreateWithProjectAndSpaceInconsistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_SPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("TEST-SPACE"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, "Sample space must be the same as project space");
    }
    
    @Test
    public void testCreateWithProjectAndNoSpaceInconsistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_NOSPACE");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NEMO"));

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.createSamples(sessionToken, Collections.singletonList(creation));
                }
            }, "Shared samples cannot be attached to projects. "
                    + "Sample: /SAMPLE_WITH_INCONSISTENT_PROJECT_AND_NOSPACE, "
                    + "Project: /CISD/NEMO "
                    + "(Context: [verify project for sample SAMPLE_WITH_INCONSISTENT_PROJECT_AND_NOSPACE])");
    }
    
    @Test
    public void testCreateWithProjectAndExperimentInconsistent()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        
        final SampleCreation creation = new SampleCreation();
        creation.setCode("SAMPLE_WITH_INCONSISTENT_PROJECT_AND_EXPERIMENT");
        creation.setTypeId(new EntityTypePermId("CELL_PLATE"));
        creation.setSpaceId(new SpacePermId("CISD"));
        creation.setProjectId(new ProjectIdentifier("/CISD/NOE"));
        creation.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));
        
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.createSamples(sessionToken, Collections.singletonList(creation));
            }
        }, "Sample project must be the same as experiment project. "
                + "Sample: /CISD/SAMPLE_WITH_INCONSISTENT_PROJECT_AND_EXPERIMENT, "
                + "Project: /CISD/NOE, "
                + "Experiment: /CISD/NEMO/EXP1 "
                + "(Context: [verify experiment for sample SAMPLE_WITH_INCONSISTENT_PROJECT_AND_EXPERIMENT])");
    }
    
    @Test
    public void testAssignSpaceSampleToProjectInDifferentSpace()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        
        final SampleUpdate sampleUpdate = new SampleUpdate();
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/C1");
        sampleUpdate.setSampleId(sampleId);
        ProjectIdentifier projectId = new ProjectIdentifier("/TEST-SPACE/NOE");
        sampleUpdate.setProjectId(projectId);

        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Collections.singletonList(sampleUpdate));
            }
        }, "Sample space must be the same as project space. "
                + "Sample: /CISD/C1, "
                + "Project: /TEST-SPACE/NOE "
                + "(Context: [verify project for sample C1])");
    }
    
    @Test
    public void testAssignSampleOfAnExperimentToProjectDifferentToTheExperimentProject()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        
        final SampleUpdate sampleUpdate = new SampleUpdate();
        SampleIdentifier sampleId = new SampleIdentifier("/CISD/3VCP5");
        sampleUpdate.setSampleId(sampleId);
        ProjectIdentifier projectId = new ProjectIdentifier("/CISD/NOE");
        sampleUpdate.setProjectId(projectId);
        
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Collections.singletonList(sampleUpdate));
            }
        }, "Sample project must be the same as experiment project. "
                + "Sample: /CISD/3VCP5, "
                + "Project: /CISD/NOE, "
                + "Experiment: /CISD/NEMO/EXP10 "
                + "(Context: [verify experiment for sample 3VCP5])");
        
    }
    
    @Test
    public void testAssignSharedSampleToProject()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        
        final SampleUpdate sampleUpdate = new SampleUpdate();
        SampleIdentifier sampleId = new SampleIdentifier("/MP");
        sampleUpdate.setSampleId(sampleId);
        ProjectIdentifier projectId = new ProjectIdentifier("/TEST-SPACE/NOE");
        sampleUpdate.setProjectId(projectId);
        
        assertUserFailureException(new IDelegatedAction()
        {
            @Override
            public void execute()
            {
                v3api.updateSamples(sessionToken, Collections.singletonList(sampleUpdate));
            }
        }, "Shared samples cannot be attached to projects. "
                + "Sample: /MP, "
                + "Project: /TEST-SPACE/NOE "
                + "(Context: [verify project for sample MP])");
    }
    
    @Test
    public void testSearchForSamplesWithProject() throws InterruptedException
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/CISD/3VCP5");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(result.getTotalCount(), 1);
    }
    
    @Test
    public void testSearchForSamplesWithCodeAndWithProject()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withCode().thatStartsWith("3V");
        searchCriteria.withProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/CISD/3VCP5");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(result.getTotalCount(), 1);
    }
    
    @Test
    public void testSearchForSamplesWithProjectWithSpaceWithCode()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject().withSpace().withCode().thatEquals("CISD");
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/CISD/3VCP5");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(result.getTotalCount(), 1);
    }
    
    @Test
    public void testSearchForSamplesWithProjectWithPermId()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withProject().withPermId().thatContains("1738-103");
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/CISD/3VCP5");
        assertEquals(result.getObjects().get(0).getProject().getIdentifier().getIdentifier(), "/CISD/NEMO");
        assertEquals(result.getTotalCount(), 1);
    }
    
    @Test
    public void testSearchForSamplesWithoutProjects()
    {
        SampleSearchCriteria searchCriteria = new SampleSearchCriteria();
        searchCriteria.withCode().thatStartsWith("3VCP");
        searchCriteria.withExperiment();
        searchCriteria.withoutProject();
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withProject();
        
        SearchResult<Sample> result = v3api.searchSamples(systemSessionToken, searchCriteria, fetchOptions);
        
        assertEquals(result.getObjects().get(0).getIdentifier().getIdentifier(), "/CISD/3VCP6");
        assertEquals(result.getObjects().get(0).getProject(), null);
        assertEquals(result.getTotalCount(), 1);
    }

}
