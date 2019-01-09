package ethz.ch;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;

public class MigrationMetadataHelper
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
}
