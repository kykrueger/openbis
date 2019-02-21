package ethz.ch.experiment;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ethz.ch.MetadataHelper;
import ethz.ch.dataset.DatasetCreationHelper;
import ethz.ch.sample.SampleAuditDataHelper;
import ethz.ch.tag.Tag2SampleTranslator;

public class Experiment2SampleTranslator
{
    
    public static void translate(String sessionToken, 
                                IApplicationServerApi v3, 
                                IDataStoreServerApi v3dss,
                                Experiment2Sample toMigrate, 
                                boolean COMMIT_CHANGES_TO_OPENBIS) throws Exception {
        System.out.println("[START]\t" + toMigrate.getExperimentPermId());
        Experiment experiment = MetadataHelper.getExperiment(sessionToken, v3, toMigrate.getExperimentPermId());
        
        // Test mode, only with complete experiments
//        if(experiment.getAttachments().isEmpty() || 
//                experiment.getSamples().isEmpty() || 
//                experiment.getDataSets().isEmpty()) {
//            return;
//        }
        //
        
        SampleCreation sampleCreation = new SampleCreation();
        sampleCreation.setCode(experiment.getCode());
        sampleCreation.setSpaceId(new SpacePermId(experiment.getIdentifier().getIdentifier().split("/")[1]));
        sampleCreation.setTypeId(new EntityTypePermId(toMigrate.getConfig().getTypeCode()));
        
        ExperimentIdentifier experimentCollectionIdentifier = new ExperimentIdentifier(experiment.getIdentifier().getIdentifier().split("/")[1], 
                experiment.getIdentifier().getIdentifier().split("/")[2], toMigrate.getConfig().getExperimentToMigrateTo());
        sampleCreation.setExperimentId(experimentCollectionIdentifier);
        sampleCreation.setProperties(experiment.getProperties());
        
        for(String from:toMigrate.getConfig().getPropertyTypesFromTo().keySet()) {
            String to = toMigrate.getConfig().getPropertyTypesFromTo().get(from);
            if(experiment.getProperties().containsKey(from)) {
                sampleCreation.setProperty(to, experiment.getProperties().get(from));
            }
        }
        
        // 1. Does the new collection for the sample exists? Create if not
        if(COMMIT_CHANGES_TO_OPENBIS && !MetadataHelper.doExperimentExist(v3, sessionToken, experimentCollectionIdentifier.getIdentifier())) {
            ProjectIdentifier projectIdentifier = new ProjectIdentifier("/" + experiment.getIdentifier().getIdentifier().split("/")[1] + "/" + experiment.getIdentifier().getIdentifier().split("/")[2]);
            v3.createExperiments(sessionToken, Collections.singletonList(MetadataHelper.getMicroscopyExperimentCollectionCreation(projectIdentifier, toMigrate.getConfig().getExperimentToMigrateTo())));
            System.out.println("Created Experiment\t" + experimentCollectionIdentifier.getIdentifier());
        }
      
        // 2. Does the new sample exists? Create if not
        SampleIdentifier sampleIdentifier = new SampleIdentifier(experiment.getIdentifier().getIdentifier());
        SamplePermId samplePermId = null;
        Sample sample = MetadataHelper.getSample(sessionToken, v3, sampleIdentifier);
        if(COMMIT_CHANGES_TO_OPENBIS && sample == null) {
            samplePermId = v3.createSamples(sessionToken, Collections.singletonList(sampleCreation)).get(0);
            System.out.println("Created Sample\t" + sampleIdentifier.getIdentifier());
        } else {
            samplePermId = sample.getPermId();
        }
        
        // 3. Does the new sample have assigned as children the samples of the experiment? Assign if not to the new sample and experiment collection.
        if(COMMIT_CHANGES_TO_OPENBIS && !experiment.getSamples().isEmpty()) {
            Deque<SampleUpdate> toSetNewParentAndCollection = new LinkedList<SampleUpdate>();
            for(Sample experimentSample:experiment.getSamples()) {
                SampleUpdate update = new SampleUpdate();
                update.setSampleId(experimentSample.getPermId());
                update.setExperimentId(experimentCollectionIdentifier);
                update.getParentIds().add(samplePermId);
                toSetNewParentAndCollection.add(update);
                System.out.println("Update Children\t" + experimentSample.getIdentifier());
            }
            MetadataHelper.executeSampleUpdates(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS, toSetNewParentAndCollection);
        }
        
        // 4. Does the new sample have assigned the experiment datasets? Assign if not
        if(COMMIT_CHANGES_TO_OPENBIS && !experiment.getDataSets().isEmpty()) {
            Deque<DataSetUpdate> toSetNewSampleAndCollection = new LinkedList<DataSetUpdate>();
            for(DataSet experimentDataSet:experiment.getDataSets()) {
                DataSetUpdate update = new DataSetUpdate();
                update.setDataSetId(experimentDataSet.getPermId());
                update.setExperimentId(experimentCollectionIdentifier); // Update experiment
                // dataSets that are already assigned to a sample, we leave them assigned to them, they should be the containers
                // dataSets that are not assigned to a sample should be assigned to their experiment sample
                if(experimentDataSet.getSample() == null) {
                    update.setSampleId(samplePermId);
                }
                toSetNewSampleAndCollection.add(update);
                System.out.println("Update DataSet\t" + experimentDataSet.getPermId());
            }
            MetadataHelper.executeDataSetUpdates(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS, toSetNewSampleAndCollection);
        }
        
        // 5. Does the new sample have assigned tags as organisational units? Create and add if not.
        if(COMMIT_CHANGES_TO_OPENBIS && (sample == null || sample.getParents().isEmpty())) {
            List<SamplePermId> tagsAsParentOU = Tag2SampleTranslator.getOrganizationUnitsFromTags(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS, experiment);
            if(tagsAsParentOU != null) {
                SampleUpdate sampleUpdate = new SampleUpdate();
                sampleUpdate.setSampleId(samplePermId);
                for(SamplePermId tagAsParentOU:tagsAsParentOU) {
                    sampleUpdate.getParentIds().add(tagAsParentOU);
                }
                System.out.println("Update Tags\t" + tagsAsParentOU);
                Deque<SampleUpdate> sampleUpdates = new LinkedList(Arrays.asList(sampleUpdate));
                MetadataHelper.executeSampleUpdates(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS, sampleUpdates);
            }
        }
        
        // 6. Does the new sample have assigned as datasets the attachments of the experiment? Create if not.
        if(COMMIT_CHANGES_TO_OPENBIS && !experiment.getAttachments().isEmpty() && (sample == null || !hasAttachments(sample))) {
            for(Attachment attachment:experiment.getAttachments()) {
                System.out.println("Add Attachment\t" + attachment.getFileName());
                Map<String, String> properties = new HashMap<>();
                properties.put("$NAME", attachment.getFileName());
                if(attachment.getDescription() != null) {
                    properties.put("$DESCRIPTION", attachment.getDescription());
                }
                DatasetCreationHelper.createOneFileDataset(v3dss, sessionToken, EntityKind.SAMPLE, sampleIdentifier.getIdentifier(), "ATTACHMENT", attachment.getFileName(), attachment.getContent(), properties);
            }
        }
        
        // 7. Add audit data always, repeating updates doesn't hurt
        System.out.println("Attach Audit data\t" + toMigrate.getExperimentPermId() + "\t" + sampleIdentifier.getIdentifier());
        SampleAuditDataHelper.appendAuditUpdate("openbis_audit_data_update.sql", samplePermId, experiment);
        System.out.println("Delete Experiment\t" + toMigrate.getExperimentPermId());
        
        // 8. Delete the migrated experiment
        deleteExperiment(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS, toMigrate.getExperimentPermId());
        System.out.println("[FINISH]\t" + toMigrate.getExperimentPermId());
    }
    
    private static boolean hasAttachments(Sample sample) {
        for(DataSet dataSet:sample.getDataSets()) {
            if(dataSet.getType().getCode().equals("ATTACHMENT")) {
                return true;
            }
        }
        return false;
    }
    
    private static void deleteExperiment(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS, ExperimentPermId experimentPermId) {
        // Sanity checks before deleting an Experiment
        
        Experiment experiment = MetadataHelper.getExperiment(sessionToken, v3, experimentPermId);
        
        if((experiment.getDataSets().size() + experiment.getSamples().size()) > 0) {
            System.out.println("Experiment " + experiment.getIdentifier() + " can't be deleted until DataSets " + experiment.getDataSets().size() + " and Samples " + experiment.getSamples().size() + " are moved. Try again later.");
            return;
        }
        
        // Deleting Experiments.
        
        if(COMMIT_CHANGES_TO_OPENBIS) {
            ExperimentDeletionOptions deleteOptions = new ExperimentDeletionOptions();
            deleteOptions.setReason("Microscopy Migration");
            IDeletionId deletionId = v3.deleteExperiments(sessionToken, Arrays.asList(experimentPermId), deleteOptions);
            v3.confirmDeletions(sessionToken, Arrays.asList(deletionId));
        }
    }
}
