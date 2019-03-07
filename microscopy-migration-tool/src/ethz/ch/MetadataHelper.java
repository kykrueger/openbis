package ethz.ch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class MetadataHelper
{
    
    public static ProjectCreation getProjectCreation(String spaceCode, String projectCode, String description) {
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode(projectCode);
        projectCreation.setSpaceId(new SpacePermId(spaceCode));
        projectCreation.setDescription(description);
        return projectCreation;
    }
    
    public static ExperimentCreation getMicroscopyExperimentCollectionCreation(ProjectIdentifier projectIdentifier, String experimentCode) {
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("COLLECTION"));
        experimentCreation.setProperty("$NAME", "Microscopy Experiment Collection");
        experimentCreation.setProperty("$DEFAULT_OBJECT_TYPE", "MICROSCOPY_EXPERIMENT");
        experimentCreation.setCode(experimentCode);
        experimentCreation.setProjectId(projectIdentifier);
        return experimentCreation;
    }
    
    public static ExperimentCreation getOrganizationUnitCollectionCreation(ProjectIdentifier projectIdentifier, String experimentCode) {
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("COLLECTION"));
        experimentCreation.setProperty("$NAME", "Organization Unit Collection");
        experimentCreation.setProperty("$DEFAULT_OBJECT_TYPE", "ORGANIZATION_UNIT");
        experimentCreation.setCode(experimentCode);
        experimentCreation.setProjectId(projectIdentifier);
        return experimentCreation;
    }
    
    public static SampleCreation getBasicSampleCreation(ExperimentIdentifier experimentIdentifier, String sampleTypeCode, String name) {
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setTypeId(new EntityTypePermId(sampleTypeCode));
        sampleCreation.setProperty("$NAME", name);
        sampleCreation.setSpaceId(new SpacePermId(experimentIdentifier.getIdentifier().split("/")[1]));
        sampleCreation.setExperimentId(experimentIdentifier);
        return sampleCreation;
    }
    
    //
    // Batch Updates
    //
    private static int SAMPLE_BATCH_SIZE = 1000;
    
    public static void executeSampleUpdates(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS, Deque<SampleUpdate> updates) {
        int totalUpdates = updates.size();
        List<SampleUpdate> toUpdate = new ArrayList<>();
        List<SampleUpdate> updated = new ArrayList<>();
        while(!updates.isEmpty()) {
            toUpdate.add(updates.removeFirst());
            if(toUpdate.size() == SAMPLE_BATCH_SIZE || updates.isEmpty()) {
                if(COMMIT_CHANGES_TO_OPENBIS) {
                    v3.updateSamples(sessionToken, toUpdate);
                }
                updated.addAll(toUpdate);
                toUpdate.clear();
                System.out.println("Updated " + updated.size() +"/" + totalUpdates);
            }
        }
    }
    
    private static int DATASET_BATCH_SIZE = 5000;
    
    public static void executeDataSetUpdates(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS, Deque<DataSetUpdate> updates) {
        int totalUpdates = updates.size();
        List<DataSetUpdate> toUpdate = new ArrayList<>();
        List<DataSetUpdate> updated = new ArrayList<>();
        while(!updates.isEmpty()) {
            toUpdate.add(updates.removeFirst());
            if(toUpdate.size() == DATASET_BATCH_SIZE || updates.isEmpty()) {
                if(COMMIT_CHANGES_TO_OPENBIS) {
                    v3.updateDataSets(sessionToken, toUpdate);
                }
                updated.addAll(toUpdate);
                toUpdate.clear();
                System.out.println("Updated " + updated.size() +"/" + totalUpdates);
            }
        }
    }
    
    //
    // Exists
    //
    
    public static boolean doProjectExist(IApplicationServerApi v3, String sessionToken, String projectIdentifier) {
        Map<IProjectId, Project> projects = v3.getProjects(sessionToken, Collections.singletonList(new ProjectIdentifier(projectIdentifier)), new ProjectFetchOptions());
        return !projects.isEmpty();
    }
    
    public static boolean doExperimentExist(IApplicationServerApi v3, String sessionToken, String experimentIdentifier) {
        Map<IExperimentId, Experiment> experiments = v3.getExperiments(sessionToken, Collections.singletonList(new ExperimentIdentifier(experimentIdentifier)), new ExperimentFetchOptions());
        return !experiments.isEmpty();
    }
    
    public static boolean doSampleExist(IApplicationServerApi v3, String sessionToken, String sampleIdentifier) {
        Map<ISampleId, Sample> samples = v3.getSamples(sessionToken, Collections.singletonList(new SampleIdentifier(sampleIdentifier)), new SampleFetchOptions());
        return !samples.isEmpty();
    }
    
    //
    // Gets
    //
    
    public static Sample getSample(String sessionToken, IApplicationServerApi v3, ISampleId sampleId) {
        SampleFetchOptions fo = new SampleFetchOptions();
        fo.withProperties();
        fo.withChildren();
        fo.withParents();
        fo.withContainer();
        fo.withSpace();
        fo.withExperiment();
        fo.withDataSets().withType();
        fo.withRegistrator();
        fo.withModifier();
        fo.withAttachments();
        Map<ISampleId, Sample> samples = v3.getSamples(sessionToken, Collections.singletonList(sampleId), fo);
        return samples.get(sampleId);
    }
    
    public static Experiment getExperiment(String sessionToken,  IApplicationServerApi v3, ExperimentPermId permId) {
        ExperimentFetchOptions fo = new ExperimentFetchOptions();
        fo.withProperties();
        fo.withSamples();
        fo.withDataSets().withSample();
        fo.withRegistrator();
        fo.withModifier();
        fo.withAttachments().withContent();
        
        Collection<Experiment> es = v3.getExperiments(sessionToken, Arrays.asList(permId), fo).values();
        if(es.size() == 1) {
            return es.iterator().next();
        } else {
            throw new RuntimeException("Experiment with PermId not found: " + permId.getPermId());
        }
    }
    
}
