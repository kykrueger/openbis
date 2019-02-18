package ethz.ch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.Attachment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search.DataSetTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.update.SampleUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.id.SpacePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class Migration
{
    
    private static final String OPENBIS_LOCAL_DEV = "http://localhost:8888";
    private static final String DSS_LOCAL_DEV = "http://localhost:8889";
//    private static final String OPENBIS_LOCAL_PROD = "https://localhost:8443";
//    private static final String OPENBIS_SCU = "https://openbis-scu.ethz.ch";
//    private static final String OPENBIS_SCU_TEST = "https://bs-lamp09.ethz.ch:8443/";
    
    private static final int TIMEOUT = Integer.MAX_VALUE;
    
    private static final List<String> EXCLUDE_SPACES = Collections.EMPTY_LIST;
    
    public static void main(String[] args) throws Exception
    {
        if(args.length == 7) {
            boolean COMMIT_CHANGES_TO_OPENBIS = Boolean.parseBoolean(args[0]);
            String AS_URL = args[1] + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
            String DSS_URL = args[2] + "/datastore_server" + IDataStoreServerApi.SERVICE_URL;
            DatasetCreationHelper.setDssURL(args[2]);
            String user = args[3];
            String pass = args[4];
            doTheWork(COMMIT_CHANGES_TO_OPENBIS, AS_URL, DSS_URL, user, pass, true, true, true);
        } else {
            System.out.println("Example: java -jar microscopy_migration_tool.jar https://openbis-domain.ethz.ch user password");
            DatasetCreationHelper.setDssURL(DSS_LOCAL_DEV);
            doTheWork(true, 
                    OPENBIS_LOCAL_DEV + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL, 
                    DSS_LOCAL_DEV + "/datastore_server" + IDataStoreServerApi.SERVICE_URL, 
                    "pontia", "migrationtool", true, true, true);
        }
    }
    
    private static void doTheWork(boolean COMMIT_CHANGES_TO_OPENBIS, String AS_URL, String DSS_URL, String userId, String pass, boolean installELNTypes, boolean migrateData, boolean deleteOldExperiments) {
        System.out.println("Migration Started");
        SslCertificateHelper.trustAnyCertificate(AS_URL);
        SslCertificateHelper.trustAnyCertificate(DSS_URL);
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, AS_URL, TIMEOUT);
        IDataStoreServerApi v3dss = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, DSS_URL, TIMEOUT);
        String sessionToken = v3.login(userId, pass);
        Map<String, String> serverInfo = v3.getServerInformation(sessionToken);
        
        if(serverInfo.containsKey("project-samples-enabled") && serverInfo.get("project-samples-enabled").equals("true")) {
            System.out.println("Project samples enabled.");
        } else {
            System.out.println("Enable project samples before running the migration.");
            return;
        }
        
        if(installELNTypes) {
            MigrationMasterdataHelper.installELNTypes(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS);
        }
        if(migrateData) {
            migrate(sessionToken, v3, v3dss, COMMIT_CHANGES_TO_OPENBIS);
        }
        if(deleteOldExperiments) {
            deleteMICROSCOPY_EXPERIMENTExperiments(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS);
        }
        v3.logout(sessionToken);
        System.out.println("Migration Finished");
    }
    
    private static class ExperimentTypeMigrationConfig {
        private final String typeCode;
        private final String experimentToMigrateTo;
        private final Map<String, String> propertyTypesFromTo = new HashMap<String, String>();
        private final List<String> propertyTypesToDeleteAfterMigration = new ArrayList<String>();
        
        public ExperimentTypeMigrationConfig(String typeCode, String experimentToMigrateTo, String nameProperty) {
            this.typeCode = typeCode;
            this.experimentToMigrateTo = experimentToMigrateTo;
            this.propertyTypesFromTo.put("$NAME", nameProperty);
            this.propertyTypesToDeleteAfterMigration.add(nameProperty);
        }

        public String getTypeCode()
        {
            return typeCode;
        }

        public String getExperimentToMigrateTo()
        {
            return experimentToMigrateTo;
        }

        public Map<String, String> getPropertyTypesFromTo()
        {
            return propertyTypesFromTo;
        }

        public List<String> getPropertyTypesToDeleteAfterMigration()
        {
            return propertyTypesToDeleteAfterMigration;
        }
        
        
    }
    
    private static void migrate(String sessionToken, IApplicationServerApi v3, IDataStoreServerApi v3dss,boolean COMMIT_CHANGES_TO_OPENBIS) {
        
        //
        // Experiment types to migrate as samples
        //
        
        ExperimentTypeMigrationConfig MICROSCOPY_EXPERIMENT = new ExperimentTypeMigrationConfig("MICROSCOPY_EXPERIMENT", "MICROSCOPY_EXPERIMENTS_COLLECTION", "MICROSCOPY_EXPERIMENT_NAME");
        ExperimentTypeMigrationConfig FACS_ARIA_EXPERIMENT = new ExperimentTypeMigrationConfig("FACS_ARIA_EXPERIMENT", "FLOW_CYTOMETRY_SORTERS", "FACS_ARIA_EXPERIMENT_NAME");
        ExperimentTypeMigrationConfig INFLUX_EXPERIMENT = new ExperimentTypeMigrationConfig("INFLUX_EXPERIMENT", "FLOW_CYTOMETRY_SORTERS", "INFLUX_EXPERIMENT_NAME");
        ExperimentTypeMigrationConfig LSR_FORTESSA_EXPERIMENT = new ExperimentTypeMigrationConfig("LSR_FORTESSA_EXPERIMENT", "LSR_FORTESSA_ANALYZERS", "LSR_FORTESSA_EXPERIMENT_NAME");
        ExperimentTypeMigrationConfig MOFLO_XDP_EXPERIMENT = new ExperimentTypeMigrationConfig("MOFLO_XDP_EXPERIMENT", "FLOW_CYTOMETRY_SORTERS", "MOFLO_XDP_EXPERIMENT_NAME");
        ExperimentTypeMigrationConfig S3E_EXPERIMENT = new ExperimentTypeMigrationConfig("S3E_EXPERIMENT", "FLOW_CYTOMETRY_SORTERS", "S3E_EXPERIMENT_NAME");
        
        //
        // 1. Installing new sample types
        //
        System.out.println("1. Installing types");
        
        // Install Sample Types for Experiment Types
        List<ExperimentTypeMigrationConfig> experimentMigrationConfigs = Arrays.asList(MICROSCOPY_EXPERIMENT,
                FACS_ARIA_EXPERIMENT,
                INFLUX_EXPERIMENT,
                LSR_FORTESSA_EXPERIMENT,
                MOFLO_XDP_EXPERIMENT,
                S3E_EXPERIMENT);
        
        for(ExperimentTypeMigrationConfig experimentMigrationConfig:experimentMigrationConfigs) {
            if(COMMIT_CHANGES_TO_OPENBIS && !doSampleTypeExist(v3, sessionToken, experimentMigrationConfig.getTypeCode())) {
                MigrationMasterdataHelper.createSampleTypesFromExperimentTypes(sessionToken, v3, Arrays.asList(experimentMigrationConfig.getTypeCode()));
                System.out.println(experimentMigrationConfig.getTypeCode() + " Sample Type installed.");
            } else {
                System.out.println(experimentMigrationConfig.getTypeCode() + " Sample Type installation skipped.");
            }
        }
        
        // Install Sample Type ORGANIZATION_UNIT
        if(COMMIT_CHANGES_TO_OPENBIS && !doSampleTypeExist(v3, sessionToken, "ORGANIZATION_UNIT")) {
            v3.createSampleTypes(sessionToken, Collections.singletonList(MigrationMasterdataHelper.getSampleTypeORGANIZATION_UNIT()));
        }
        System.out.println("ORGANIZATION_UNIT Sample Type installed.");
        
        if(COMMIT_CHANGES_TO_OPENBIS && !doDataSetTypeExist(v3, sessionToken, "ATTACHMENT")) {
            v3.createDataSetTypes(sessionToken, Collections.singletonList(MigrationMasterdataHelper.getDataSetTypeATTACHMENT()));
        }
        
        System.out.println("ATTACHMENT DataSet Type installed.");
        
        //
        // 2. Creating new ORGANIZATION_UNITS_COLLECTION and MICROSCOPY_EXPERIMENTS_COLLECTION
        //
        System.out.println("2. Creating new ORGANIZATION_UNITS_COLLECTION and MICROSCOPY_EXPERIMENTS_COLLECTION");
        
        // Create General ORGANIZATION_UNIT Collection for every PROJECT following the pattern /SPACE/PROJECT/ORGANIZATION_UNIT_COLLECTION
        // Create General ORGANIZATION_UNIT Collection for every SPACE following the pattern /SPACE/COMMON_ORGANIZATION_UNITS/ORGANIZATION_UNIT_COLLECTION
        SpaceFetchOptions spaceFetchOptions = new SpaceFetchOptions();
        spaceFetchOptions.withProjects();
        
        SearchResult<Space> spaces = v3.searchSpaces(sessionToken, new SpaceSearchCriteria(), spaceFetchOptions);
        
        for(Space space:spaces.getObjects()) {
            String spaceCode = space.getCode();
            if(!EXCLUDE_SPACES.contains(spaceCode)) {
                // Install Project level collection
                ProjectSearchCriteria projectSearchCriteria = new ProjectSearchCriteria();
                projectSearchCriteria.withSpace().withCode().equals(spaceCode);
                
                for(Project project:space.getProjects()) {
                    String experimentIdentifierOU = "/" + spaceCode + "/" + project.getCode() + "/ORGANIZATION_UNITS_COLLECTION";
                    if(COMMIT_CHANGES_TO_OPENBIS && !doExperimentExist(v3, sessionToken, experimentIdentifierOU)) {
                        v3.createExperiments(sessionToken, Collections.singletonList(MigrationMetadataHelper.getOrganizationUnitCollectionCreation(project.getIdentifier(), "ORGANIZATION_UNITS_COLLECTION")));
                    }
                    System.out.println("Project Experiment Created: " + experimentIdentifierOU);
                    
                    String experimentIdentifierC = "/" + spaceCode + "/" + project.getCode() + "/MICROSCOPY_EXPERIMENTS_COLLECTION";
                    if(COMMIT_CHANGES_TO_OPENBIS && !doExperimentExist(v3, sessionToken, experimentIdentifierC)) {
                        v3.createExperiments(sessionToken, Collections.singletonList(MigrationMetadataHelper.getMicroscopyExperimentCollectionCreation(project.getIdentifier(), "MICROSCOPY_EXPERIMENTS_COLLECTION")));
                    }
                    System.out.println("Project Experiment Created: " + experimentIdentifierC);
                }
                
                // Install Space level project and collection
                String projectIdentifier = "/" + spaceCode + "/COMMON_ORGANIZATION_UNITS";
                if(COMMIT_CHANGES_TO_OPENBIS && !doProjectExist(v3, sessionToken, projectIdentifier)) {
                    v3.createProjects(sessionToken, Collections.singletonList(MigrationMetadataHelper.getProjectCreation(spaceCode, "COMMON_ORGANIZATION_UNITS", "Folder to share common organization units collections.")));
                }
                System.out.println("Space Project Created: " + projectIdentifier);
                
                String experimentIdentifier = "/" + spaceCode + "/COMMON_ORGANIZATION_UNITS/ORGANIZATION_UNITS_COLLECTION";
                if(COMMIT_CHANGES_TO_OPENBIS && !doExperimentExist(v3, sessionToken, experimentIdentifier)) {
                    v3.createExperiments(sessionToken, Collections.singletonList(MigrationMetadataHelper.getOrganizationUnitCollectionCreation(new ProjectIdentifier("/" + spaceCode + "/COMMON_ORGANIZATION_UNITS"), "ORGANIZATION_UNITS_COLLECTION")));
                }
                System.out.println("Space Experiment Created: " + experimentIdentifier);
            }
        }
        
        //
        // 3. Preparing copy of experiment of type MICROSCOPY_EXPERIMENT to samples of type MICROSCOPY_EXPERIMENT
        //
        System.out.println("3. Preparing copy of experiment of type MICROSCOPY_EXPERIMENT to samples of type MICROSCOPY_EXPERIMENT");
        
        ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
        experimentSearchCriteria.withType().withCode().thatEquals("MICROSCOPY_EXPERIMENT");
        
        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withProperties();
        experimentFetchOptions.withSamples();
        experimentFetchOptions.withDataSets().withSample();
        experimentFetchOptions.withRegistrator();
        experimentFetchOptions.withModifier();
        experimentFetchOptions.withAttachments().withContent();
        
        SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken, experimentSearchCriteria, experimentFetchOptions);
        Map<SampleCreation, Experiment> experimentsToMigrateBySampleCreation = new HashMap<>();
        
        System.out.println("Found " + experiments.getTotalCount() + " MICROSCOPY_EXPERIMENT to migrate ");
        List<SampleCreation> sampleCreationsToMigrateExperiments = new ArrayList<SampleCreation>(experiments.getTotalCount());
        Deque<SampleUpdate> sampleUpdatesToMigrateExperiments = new LinkedList<SampleUpdate>();
        Map<SampleIdentifier, List<DataSet>> datasetsQueuedToAssignToMigratedExperiments = new HashMap<>();
        Deque<DataSetUpdate> dataSetUpdatesToMigrateExperiments = new LinkedList<>();
        List<ExperimentPermId> experimentsQueuedToDelete = new ArrayList<>(experiments.getTotalCount());
        
        for(Experiment experiment:experiments.getObjects()) {
            // Create new sample
            SampleCreation sampleCreation = new SampleCreation();
            sampleCreationsToMigrateExperiments.add(sampleCreation);
            experimentsToMigrateBySampleCreation.put(sampleCreation, experiment);
            sampleCreation.setCode(experiment.getCode());
            sampleCreation.setSpaceId(new SpacePermId(experiment.getIdentifier().getIdentifier().split("/")[1]));
            sampleCreation.setTypeId(new EntityTypePermId("MICROSCOPY_EXPERIMENT"));
            ExperimentIdentifier experimentCollectionIdentifier = new ExperimentIdentifier(experiment.getIdentifier().getIdentifier().split("/")[1], 
                    experiment.getIdentifier().getIdentifier().split("/")[2], "MICROSCOPY_EXPERIMENTS_COLLECTION");
            sampleCreation.setExperimentId(experimentCollectionIdentifier);
            sampleCreation.setProperties(experiment.getProperties());
            
            if(experiment.getProperties().containsKey("MICROSCOPY_EXPERIMENT_NAME")) {
                sampleCreation.setProperty("$NAME", experiment.getProperties().get("MICROSCOPY_EXPERIMENT_NAME"));
            }
            
            // Assign all old samples to the new collection experiment
            List<ISampleId> childIds = new ArrayList<ISampleId>(experiment.getSamples().size());
            for(Sample experimentSample:experiment.getSamples()) {
                childIds.add(experimentSample.getPermId());
                
                SampleUpdate update = new SampleUpdate();
                update.setSampleId(experimentSample.getPermId());
                update.setExperimentId(experimentCollectionIdentifier);
                sampleUpdatesToMigrateExperiments.add(update);
            }
            
            // Assign all old samples to the new MICROSCOPY_EXPERIMENT sample
            sampleCreation.setChildIds(childIds);
            
            // Queue all old datasets to be assigned to the new MICROSCOPY_EXPERIMENT sample
            String sampleIdentifier = "/" + experiment.getIdentifier().getIdentifier().split("/")[1] + "/" + experiment.getIdentifier().getIdentifier().split("/")[2] + "/" + experiment.getCode(); 
            datasetsQueuedToAssignToMigratedExperiments.put(new SampleIdentifier(sampleIdentifier), experiment.getDataSets());
            
            // Queue all old experiments to delete
            experimentsQueuedToDelete.add(experiment.getPermId());
            
            // SQL Audit data update
            AuditDataHelper.addAuditData(sampleIdentifier, experiment);
            
            // Save Attachments
            MigrationAttachmentsHelper.addAttachmentData(experiment);
        }
        
        //
        // 4. Copy of experiment of type MICROSCOPY_EXPERIMENT to new samples of type MICROSCOPY_EXPERIMENT
        //
        System.out.println("4. Copy of experiments of type MICROSCOPY_EXPERIMENT to samples of type MICROSCOPY_EXPERIMENT");
        
        Map<ISampleId, Sample> newMicroscopyExperimentSamplesByIdentifier = new HashMap<>();
        if(COMMIT_CHANGES_TO_OPENBIS) {
            for(SampleCreation sampleCreation:sampleCreationsToMigrateExperiments) {
                Experiment experiment = experimentsToMigrateBySampleCreation.get(sampleCreation);
                List<SamplePermId> tagsAsParentOU = TagsHelper.getOrganizationUnits(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS, experiment);
                if(tagsAsParentOU != null) {
                    sampleCreation.setParentIds(tagsAsParentOU);
                }
            }
            
            List<SamplePermId> createdSamplesPermIds = v3.createSamples(sessionToken, sampleCreationsToMigrateExperiments);
            Map<ISampleId, Sample> microscopyExperimentSamplesByPermId = v3.getSamples(sessionToken, createdSamplesPermIds, new SampleFetchOptions());
            for(Sample sample:microscopyExperimentSamplesByPermId.values()) {
                newMicroscopyExperimentSamplesByIdentifier.put(sample.getIdentifier(), sample);
                AuditDataHelper.addSamplePermId(sample.getIdentifier().getIdentifier(), sample.getPermId().getPermId());
            }
        }
        System.out.println("Created " + sampleCreationsToMigrateExperiments.size() + " MICROSCOPY_EXPERIMENT Samples.");
        
        System.out.println("4.2 Write SQL Audit Update");
        
        AuditDataHelper.writeSQLAuditUpdate();
        
        System.out.println("4.3 Migrate Attachments");
        
        Map<String, List<Attachment>> attachments = MigrationAttachmentsHelper.getAttachments();
        for(String sampleIdentifier :attachments.keySet()) {
            for(Attachment attachment:attachments.get(sampleIdentifier)) {
                if(COMMIT_CHANGES_TO_OPENBIS) {
                    Map<String, String> properties = new HashMap<>();
                    if(attachment.getDescription() != null) {
                        properties.put("NOTES", attachment.getDescription());
                    }
                    DatasetCreationHelper.createDataset(v3dss, sessionToken, sampleIdentifier, "ATTACHMENT", attachment.getFileName(), attachment.getContent(), properties);
                }
            }
        }
            
        //
        // 5. Moving Samples out from MICROSCOPY_EXPERIMENT Experiments to MICROSCOPY_EXPERIMENT Samples
        //
        System.out.println("5. Moving Samples out from MICROSCOPY_EXPERIMENT Experiments to MICROSCOPY_EXPERIMENT Samples");
        System.out.println("Moving " + sampleUpdatesToMigrateExperiments.size() + " Samples from Experiment MICROSCOPY_EXPERIMENT to COLLECTION.");
        MigrationMetadataHelper.executeSampleUpdates(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS, sampleUpdatesToMigrateExperiments);
        System.out.println("Moved all Samples from the MICROSCOPY_EXPERIMENT to the COLLECTION.");
        
        //
        // 6. Preparing DataSets to move out from MICROSCOPY_EXPERIMENT
        //
        System.out.println("6. Preparing DataSets to move out from MICROSCOPY_EXPERIMENT to new MICROSCOPY_EXPERIMENT sample");
        
        for(SampleIdentifier sampleIdentifier : datasetsQueuedToAssignToMigratedExperiments.keySet()) {
            for(DataSet dataSet : datasetsQueuedToAssignToMigratedExperiments.get(sampleIdentifier)) {
                DataSetUpdate dataSetUpdate = new DataSetUpdate();
                dataSetUpdate.setDataSetId(dataSet.getPermId());
                // dataSets that are already assigned to a sample, we leave them assigned to them, they should be the containers
                // dataSets that are not assigned to a sample should be assigned to their experiment sample
                if(dataSet.getSample() == null) {
                    Sample newSample = newMicroscopyExperimentSamplesByIdentifier.get(sampleIdentifier); //This will only be not null if we actually create them
                    if(newSample != null || COMMIT_CHANGES_TO_OPENBIS) {
                        dataSetUpdate.setSampleId(newSample.getPermId()); //We do this by permId because fetching by identifier could fail due to a bug
                    }
                }
                //All datasets need to be attached to the new experiment so the old one can be deleted latter
                String experimentIdentifier = "/" + sampleIdentifier.getIdentifier().split("/")[1] + "/" + sampleIdentifier.getIdentifier().split("/")[2] + "/MICROSCOPY_EXPERIMENTS_COLLECTION"; 
                dataSetUpdate.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
                dataSetUpdatesToMigrateExperiments.add(dataSetUpdate);
            }
        }
        
        //
        // 7. Moving DataSets out from MICROSCOPY_EXPERIMENT
        //
        System.out.println("7. Moving DataSets out from MICROSCOPY_EXPERIMENT to new MICROSCOPY_EXPERIMENT sample");
        MigrationMetadataHelper.executeDataSetUpdates(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS, dataSetUpdatesToMigrateExperiments);
        System.out.println("Moved all MICROSCOPY_EXPERIMENT Experiment DataSets without Sample to MICROSCOPY_EXPERIMENT Samples.");
    }
    
    private static void deleteMICROSCOPY_EXPERIMENTExperiments(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS) {
        // Sanity checks before deleting MICROSCOPY_EXPERIMENT Experiments.
        
        System.out.println("Deleting MICROSCOPY_EXPERIMENT Experiments.");
        
        ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
        experimentSearchCriteria.withType().withCode().thatEquals("MICROSCOPY_EXPERIMENT");
        
        ExperimentFetchOptions experimentFetchOptions = new ExperimentFetchOptions();
        experimentFetchOptions.withDataSets();
        experimentFetchOptions.withSamples();
        
        SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken, experimentSearchCriteria, experimentFetchOptions);
        List<ExperimentPermId> experimentsToDelete = new ArrayList<>(experiments.getTotalCount());
        
        int dataSetsFound = 0;
        int samplesFound = 0;
        for(Experiment experiment:experiments.getObjects()) {
            if(experiment.getDataSets().isEmpty() && experiment.getSamples().isEmpty()) {
                experimentsToDelete.add(experiment.getPermId());
            } else {
                if(!experiment.getDataSets().isEmpty()) {
                    dataSetsFound += experiment.getDataSets().size();
                    System.out.println(experiment.getIdentifier() + " has "+ experiment.getDataSets().size() + " dataSets.");
                }
                if(!experiment.getSamples().isEmpty()) {
                    samplesFound += experiment.getSamples().size();
                    System.out.println(experiment.getIdentifier() + " has "+ experiment.getSamples().size() + " samples.");
                }
            }
        }
        System.out.println("Ready experiments to delete: " + experimentsToDelete.size());
        System.out.println("Found " + dataSetsFound + " datasets and " + samplesFound + " samples on MICROSCOPY_EXPERIMENT Experiments with DataSets or Samples.");
        if((dataSetsFound + samplesFound) > 0) {
            System.out.println("Experiments can be deleted until DataSets and Samples are moved. Try again later.");
            return;
        }
        
        // Deleting MICROSCOPY_EXPERIMENT Experiments.
        
        System.out.println("Deleting " + experimentsToDelete.size() + " MICROSCOPY_EXPERIMENT Experiments.");
        if(COMMIT_CHANGES_TO_OPENBIS) {
            ExperimentDeletionOptions deleteOptions = new ExperimentDeletionOptions();
            deleteOptions.setReason("Microscopy Migration");
            IDeletionId deletionId = v3.deleteExperiments(sessionToken, experimentsToDelete, deleteOptions);
            v3.confirmDeletions(sessionToken, Arrays.asList(deletionId));
        }
        System.out.println("Deleted " + experimentsToDelete.size() + " MICROSCOPY_EXPERIMENT Experiments.");
        
        // Deleting Type MICROSCOPY_EXPERIMENT Experiment
        // This will not work until trashcan is emptied
        System.out.println("Deleting Type MICROSCOPY_EXPERIMENT Experiment.");
        if(COMMIT_CHANGES_TO_OPENBIS) {
            ExperimentTypeDeletionOptions deleteOptions = new ExperimentTypeDeletionOptions();
            deleteOptions.setReason("Microscopy Migration");
            v3.deleteExperimentTypes(sessionToken, Arrays.asList(new EntityTypePermId("MICROSCOPY_EXPERIMENT")), deleteOptions);
        }
        System.out.println("Deleted Type MICROSCOPY_EXPERIMENT Experiment.");
    }
    
    //
    // Helper functions
    //
    private static boolean doDataSetTypeExist(IApplicationServerApi v3, String sessionToken, String dataSetTypeCode) {
        DataSetTypeSearchCriteria criteria = new DataSetTypeSearchCriteria();
        criteria.withCode().thatEquals(dataSetTypeCode);

        SearchResult<DataSetType> type = v3.searchDataSetTypes(sessionToken, criteria, new DataSetTypeFetchOptions());

        return !type.getObjects().isEmpty();
    }
    
    private static boolean doSampleTypeExist(IApplicationServerApi v3, String sessionToken, String sampleTypeCode) {
        SampleTypeSearchCriteria criteria = new SampleTypeSearchCriteria();
        criteria.withCode().thatEquals(sampleTypeCode);

        SearchResult<SampleType> type = v3.searchSampleTypes(sessionToken, criteria, new SampleTypeFetchOptions());

        return !type.getObjects().isEmpty();
    }
    
    private static boolean doProjectExist(IApplicationServerApi v3, String sessionToken, String projectIdentifier) {
        Map<IProjectId, Project> projects = v3.getProjects(sessionToken, Collections.singletonList(new ProjectIdentifier(projectIdentifier)), new ProjectFetchOptions());
        return !projects.isEmpty();
    }
    private static boolean doExperimentExist(IApplicationServerApi v3, String sessionToken, String experimentIdentifier) {
        Map<IExperimentId, Experiment> experiments = v3.getExperiments(sessionToken, Collections.singletonList(new ExperimentIdentifier(experimentIdentifier)), new ExperimentFetchOptions());
        return !experiments.isEmpty();
    }
    
}
