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
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentTypeDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.create.ProjectCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.fetchoptions.ProjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.IProjectId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
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
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class Migration
{
    
    private static final String OPENBIS_LOCAL_DEV = "http://localhost:8888";
    private static final String OPENBIS_LOCAL_PROD = "https://localhost:8443";
    private static final String OPENBIS_SCU = "https://openbis-scu.ethz.ch";
    private static final String OPENBIS_SCU_TEST = "https://bs-lamp09.ethz.ch:8443/";
    
    private static final int TIMEOUT = Integer.MAX_VALUE;
    
    private static final List<String> EXCLUDE_SPACES = Collections.EMPTY_LIST;
    
    public static void main(String[] args) throws Exception
    {
        if(args.length == 7) {
            boolean COMMIT_CHANGES_TO_OPENBIS = Boolean.parseBoolean(args[0]);
            String URL = args[1] + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
            String user = args[2];
            String pass = args[3];
            doTheWork(COMMIT_CHANGES_TO_OPENBIS, URL, user, pass, true, true, true);
        } else {
            System.out.println("Example: java -jar microscopy_migration_tool.jar https://openbis-domain.ethz.ch user password");
            doTheWork(false, OPENBIS_SCU_TEST + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL, "migration", "migrationtool", true, true, true);
        }
    }
    
    private static void doTheWork(boolean COMMIT_CHANGES_TO_OPENBIS, String URL, String userId, String pass, boolean installELNTypes, boolean migrateData, boolean deleteOldExperiments) {
        System.out.println("Migration Started");
        SslCertificateHelper.trustAnyCertificate(URL);
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, URL, TIMEOUT);
        String sessionToken = v3.login(userId, pass);
        Map<String, String> serverInfo = v3.getServerInformation(sessionToken);
        
        if(serverInfo.containsKey("project-samples-enabled") && serverInfo.get("project-samples-enabled").equals("true")) {
            System.out.println("Project samples enabled.");
        } else {
            System.out.println("Enable project samples before running the migration.");
            return;
        }
        
        if(installELNTypes) {
            installELNTypes(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS);
        }
        if(migrateData) {
            migrate(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS);
        }
        if(deleteOldExperiments) {
            deleteMICROSCOPY_EXPERIMENTExperiments(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS);
        }
        v3.logout(sessionToken);
        System.out.println("Migration Finished");
    }
    
    private static void deleteMICROSCOPY_EXPERIMENTExperiments(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS) {
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
                    System.out.println(experiment.getIdentifier() + " has "+ experiment.getDataSets().size() + " samples.");
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
        System.out.println("Deleting " + experimentsToDelete.size() + " MICROSCOPY_EXPERIMENT Experiments.");
        if(COMMIT_CHANGES_TO_OPENBIS) {
            ExperimentDeletionOptions deleteOptions = new ExperimentDeletionOptions();
            deleteOptions.setReason("Microscopy Migration");
            v3.deleteExperiments(sessionToken, experimentsToDelete, deleteOptions);
        }

        System.out.println("Deleted " + experimentsToDelete.size() + " MICROSCOPY_EXPERIMENT Experiments.");
//        System.out.println("Deliting Type MICROSCOPY_EXPERIMENT Experiment.");
//        if(COMMIT_CHANGES_TO_OPENBIS) {
//            ExperimentTypeDeletionOptions deleteOptions = new ExperimentTypeDeletionOptions();
//            deleteOptions.setReason("Microscopy Migration");
//            v3.deleteExperimentTypes(sessionToken, Arrays.asList(new EntityTypePermId("MICROSCOPY_EXPERIMENT")), deleteOptions);
//        }
//        System.out.println("Deleted Type MICROSCOPY_EXPERIMENT Experiment.");
    }
    
    private static void installELNTypes(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS) {
        System.out.println("Installing Missing ELN Types.");
        if(COMMIT_CHANGES_TO_OPENBIS) {
            createPropertiesIfMissing(sessionToken, v3);
            createExperimentTypesIfMissing(sessionToken, v3);
        }
        System.out.println("ELN Types installed.");
    }
    
    private static void migrate(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS) {
        
        //
        // 1. Installing new sample types
        //
        System.out.println("1. Installing new sample types");
        
        // Install Sample Type MICROSCOPY_EXPERIMENT
        if(COMMIT_CHANGES_TO_OPENBIS && !doSampleTypeExist(v3, sessionToken, "MICROSCOPY_EXPERIMENT")) {
            v3.createSampleTypes(sessionToken, Collections.singletonList(getSampleTypeMICROSCOPY_EXPERIMENT()));
        }
        System.out.println("MICROSCOPY_EXPERIMENT Sample Type installed.");
        
        // Install Sample Type ORGANIZATION_UNIT
        if(COMMIT_CHANGES_TO_OPENBIS && !doSampleTypeExist(v3, sessionToken, "ORGANIZATION_UNIT")) {
            v3.createSampleTypes(sessionToken, Collections.singletonList(getSampleTypeORGANIZATION_UNIT()));
        }
        System.out.println("ORGANIZATION_UNIT Sample Type installed.");
        
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
                        v3.createExperiments(sessionToken, Collections.singletonList(getOrganizationUnitCollectionCreation(project.getIdentifier(), "ORGANIZATION_UNITS_COLLECTION")));
                    }
                    System.out.println("Project Experiment Created: " + experimentIdentifierOU);
                    
                    String experimentIdentifierC = "/" + spaceCode + "/" + project.getCode() + "/MICROSCOPY_EXPERIMENTS_COLLECTION";
                    if(COMMIT_CHANGES_TO_OPENBIS && !doExperimentExist(v3, sessionToken, experimentIdentifierC)) {
                        v3.createExperiments(sessionToken, Collections.singletonList(getMicroscopyExperimentCollectionCreation(project.getIdentifier(), "MICROSCOPY_EXPERIMENTS_COLLECTION")));
                    }
                    System.out.println("Project Experiment Created: " + experimentIdentifierC);
                }
                
                // Install Space level project and collection
                String projectIdentifier = "/" + spaceCode + "/COMMON_ORGANIZATION_UNITS";
                if(COMMIT_CHANGES_TO_OPENBIS && !doProjectExist(v3, sessionToken, projectIdentifier)) {
                    v3.createProjects(sessionToken, Collections.singletonList(getProjectCreation(spaceCode, "COMMON_ORGANIZATION_UNITS", "Folder to share common organization units collections.")));
                }
                System.out.println("Space Project Created: " + projectIdentifier);
                
                String experimentIdentifier = "/" + spaceCode + "/COMMON_ORGANIZATION_UNITS/ORGANIZATION_UNITS_COLLECTION";
                if(COMMIT_CHANGES_TO_OPENBIS && !doExperimentExist(v3, sessionToken, experimentIdentifier)) {
                    v3.createExperiments(sessionToken, Collections.singletonList(getOrganizationUnitCollectionCreation(new ProjectIdentifier("/" + spaceCode + "/COMMON_ORGANIZATION_UNITS"), "ORGANIZATION_UNITS_COLLECTION")));
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
        
        SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken, experimentSearchCriteria, experimentFetchOptions);
        List<ExperimentPermId> experimentsToDelete = new ArrayList<>(experiments.getTotalCount());
        
        System.out.println("Found " + experiments.getTotalCount() + " MICROSCOPY_EXPERIMENT to migrate ");
        List<SampleCreation> sampleCreationsToMigrateExperiments = new ArrayList<SampleCreation>(experiments.getTotalCount());
        Deque<SampleUpdate> sampleUpdatesToMigrateExperiments = new LinkedList<SampleUpdate>();
        
        Map<SampleIdentifier, List<DataSet>> datasetsToUpdate = new HashMap<>();
        
        for(Experiment experiment:experiments.getObjects()) {
            SampleCreation sampleCreation = new SampleCreation();
            sampleCreation.setCode(experiment.getCode());
            sampleCreation.setSpaceId(new SpacePermId(experiment.getIdentifier().getIdentifier().split("/")[1]));
            sampleCreation.setTypeId(new EntityTypePermId("MICROSCOPY_EXPERIMENT"));
            ExperimentIdentifier experimentCollectionIdentifier = new ExperimentIdentifier(experiment.getIdentifier().getIdentifier().split("/")[1], 
                    experiment.getIdentifier().getIdentifier().split("/")[2], "MICROSCOPY_EXPERIMENTS_COLLECTION");
            sampleCreation.setExperimentId(experimentCollectionIdentifier);
            sampleCreation.setProperties(experiment.getProperties());
            if(experiment.getProperties().containsKey("MICROSCOPY_EXPERIMENT_NAME")) {
                sampleCreation.setProperty("NAME", experiment.getProperties().get("MICROSCOPY_EXPERIMENT_NAME"));
            }
            
            List<ISampleId> childIds = new ArrayList<ISampleId>(experiment.getSamples().size());
            for(Sample experimentSample:experiment.getSamples()) {
                childIds.add(experimentSample.getPermId());
                
                // We need to assign all samples to the new collection experiment
                SampleUpdate update = new SampleUpdate();
                update.setSampleId(experimentSample.getPermId());
                update.setExperimentId(experimentCollectionIdentifier);
                sampleUpdatesToMigrateExperiments.add(update);
            }
            sampleCreation.setChildIds(childIds);
            sampleCreationsToMigrateExperiments.add(sampleCreation);
            experimentsToDelete.add(experiment.getPermId());
            String sampleIdentifier = "/" + experiment.getIdentifier().getIdentifier().split("/")[1] + "/" + experiment.getIdentifier().getIdentifier().split("/")[2] + "/" + experiment.getCode(); 
            datasetsToUpdate.put(new SampleIdentifier(sampleIdentifier), experiment.getDataSets());
        }
        
        //
        // 4. Copy of experiment of type MICROSCOPY_EXPERIMENT to samples of type MICROSCOPY_EXPERIMENT
        //
        System.out.println("4. Copy of experiment of type MICROSCOPY_EXPERIMENT to samples of type MICROSCOPY_EXPERIMENT");
        
        Map<ISampleId, Sample> microscopyExperimentSamplesByIdentifier = null;
        if(COMMIT_CHANGES_TO_OPENBIS) {
            List<SamplePermId> createdSamplesPermIds = v3.createSamples(sessionToken, sampleCreationsToMigrateExperiments);
            Map<ISampleId, Sample> microscopyExperimentSamplesByPermId = v3.getSamples(sessionToken, createdSamplesPermIds, new SampleFetchOptions());
            microscopyExperimentSamplesByIdentifier = new HashMap<>(microscopyExperimentSamplesByPermId.size());
            for(Sample sample:microscopyExperimentSamplesByPermId.values()) {
                microscopyExperimentSamplesByIdentifier.put(sample.getIdentifier(), sample);
            }
        }
        System.out.println("Created " + sampleCreationsToMigrateExperiments.size() + " MICROSCOPY_EXPERIMENT Samples.");
        
        //
        // 5. Moving Samples out from MICROSCOPY_EXPERIMENT Experiments to MICROSCOPY_EXPERIMENT Samples
        //
        System.out.println("5. Moving Samples out from MICROSCOPY_EXPERIMENT Experiments to MICROSCOPY_EXPERIMENT Samples");
        
        int totalSampleUpdates = sampleUpdatesToMigrateExperiments.size();
        System.out.println("Moving " + totalSampleUpdates + " Samples from Experiment MICROSCOPY_EXPERIMENT to COLLECTION.");
        
        List<SampleUpdate> samplesToUpdate = new ArrayList<>();
        List<SampleUpdate> samplesUpdated = new ArrayList<>();
        while(!sampleUpdatesToMigrateExperiments.isEmpty()) {
            samplesToUpdate.add(sampleUpdatesToMigrateExperiments.removeFirst());
            if(samplesToUpdate.size() == 1000 || sampleUpdatesToMigrateExperiments.isEmpty()) {
                if(COMMIT_CHANGES_TO_OPENBIS) {
                    v3.updateSamples(sessionToken, samplesToUpdate);
                }
                samplesUpdated.addAll(samplesToUpdate);
                samplesToUpdate.clear();
                System.out.println("Moved " + samplesUpdated.size() +"/" + totalSampleUpdates + " Samples from the MICROSCOPY_EXPERIMENT to the COLLECTION.");
            }
        }
        
        System.out.println("Moved all Samples from the MICROSCOPY_EXPERIMENT to the COLLECTION.");
        
        //
        // 6. Preparing DataSets to move out from MICROSCOPY_EXPERIMENT
        //
        System.out.println("6. Preparing DataSets to move out from MICROSCOPY_EXPERIMENT");
        
        Deque<DataSetUpdate> dataSetUpdates = new LinkedList<>();
        for(SampleIdentifier sampleIdentifier : datasetsToUpdate.keySet()) {
            for(DataSet dataSet : datasetsToUpdate.get(sampleIdentifier)) {
                DataSetUpdate dataSetUpdate = new DataSetUpdate();
                dataSetUpdate.setDataSetId(dataSet.getPermId());
                // dataSets that are already assigned to a sample, we leave them assigned to them, they should be the containers
                // dataSets that are not assigned to a sample should be assigned to their experiment sample
                if(dataSet.getSample() == null) {
                    dataSetUpdate.setSampleId(microscopyExperimentSamplesByIdentifier.get(sampleIdentifier).getPermId()); //We do this by permId because fetching by identifier could fail due to a bug
                }
                //All datasets need to be attached to the new experiment so the old one can be deleted latter
                String experimentIdentifier = "/" + sampleIdentifier.getIdentifier().split("/")[1] + "/" + sampleIdentifier.getIdentifier().split("/")[2] + "/MICROSCOPY_EXPERIMENTS_COLLECTION"; 
                dataSetUpdate.setExperimentId(new ExperimentIdentifier(experimentIdentifier));
                dataSetUpdates.add(dataSetUpdate);
            }
        }
        
        //
        // 7. Moving DataSets out from MICROSCOPY_EXPERIMENT
        //
        System.out.println("7. Moving DataSets out from MICROSCOPY_EXPERIMENT");
        
        List<DataSetUpdate> dataSetUpdated = new ArrayList<>();
        List<DataSetUpdate> dataSetToUpdate = new ArrayList<>();
        int totalDataSetUpdates = dataSetUpdates.size();
        while(!dataSetUpdates.isEmpty()) {
            dataSetToUpdate.add(dataSetUpdates.removeFirst());
            if(dataSetToUpdate.size() == 5000 || dataSetUpdates.isEmpty()) {
                if(COMMIT_CHANGES_TO_OPENBIS) {
                    v3.updateDataSets(sessionToken, dataSetToUpdate);
                }
                dataSetUpdated.addAll(dataSetToUpdate);
                dataSetToUpdate.clear();
                System.out.println("Moved " + dataSetUpdated.size() +"/" + totalDataSetUpdates + " MICROSCOPY_EXPERIMENT Experiment DataSets.");
            }
        }
        
        System.out.println("Moved " + dataSetUpdated.size() +"/" + totalDataSetUpdates + " MICROSCOPY_EXPERIMENT Experiment DataSets without Sample.");
        
        System.out.println("Moved all MICROSCOPY_EXPERIMENT Experiment DataSets without Sample to MICROSCOPY_EXPERIMENT Samples.");
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
    
    private static SampleTypeCreation getSampleTypeMICROSCOPY_EXPERIMENT() {
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("NAME"));
        
        PropertyAssignmentCreation MICROSCOPY_EXPERIMENT_NAME = new PropertyAssignmentCreation();
        MICROSCOPY_EXPERIMENT_NAME.setPropertyTypeId(new PropertyTypePermId("MICROSCOPY_EXPERIMENT_NAME"));
        
        PropertyAssignmentCreation MICROSCOPY_EXPERIMENT_DESCRIPTION = new PropertyAssignmentCreation();
        MICROSCOPY_EXPERIMENT_DESCRIPTION.setPropertyTypeId(new PropertyTypePermId("MICROSCOPY_EXPERIMENT_DESCRIPTION"));
        
        PropertyAssignmentCreation MICROSCOPY_EXPERIMENT_VERSION = new PropertyAssignmentCreation();
        MICROSCOPY_EXPERIMENT_VERSION.setPropertyTypeId(new PropertyTypePermId("MICROSCOPY_EXPERIMENT_VERSION"));
        MICROSCOPY_EXPERIMENT_VERSION.setShowInEditView(Boolean.FALSE);
        
        PropertyAssignmentCreation MICROSCOPY_EXPERIMENT_ACQ_HARDWARE_FRIENDLY_NAME = new PropertyAssignmentCreation();
        MICROSCOPY_EXPERIMENT_ACQ_HARDWARE_FRIENDLY_NAME.setPropertyTypeId(new PropertyTypePermId("MICROSCOPY_EXPERIMENT_ACQ_HARDWARE_FRIENDLY_NAME"));
        
        PropertyAssignmentCreation ANNOTATIONS_STATE = new PropertyAssignmentCreation();
        ANNOTATIONS_STATE.setPropertyTypeId(new PropertyTypePermId("ANNOTATIONS_STATE"));
        ANNOTATIONS_STATE.setShowInEditView(Boolean.FALSE);
        
        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode("MICROSCOPY_EXPERIMENT");
        creation.setDescription("Generic microscopy experiment.");
        creation.setPropertyAssignments(Arrays.asList(
                NAME,
                MICROSCOPY_EXPERIMENT_NAME, 
                MICROSCOPY_EXPERIMENT_DESCRIPTION, 
                MICROSCOPY_EXPERIMENT_VERSION, 
                MICROSCOPY_EXPERIMENT_ACQ_HARDWARE_FRIENDLY_NAME,
                ANNOTATIONS_STATE));
        return creation;
    }
    
    private static void createPropertiesIfMissing(String sessionToken, IApplicationServerApi v3) {
        PropertyTypeSearchCriteria propertyTypeSearchCriteria = new PropertyTypeSearchCriteria();
        propertyTypeSearchCriteria.withCodes().setFieldValue(Arrays.asList("NAME", "DEFAULT_OBJECT_TYPE", "XMLCOMMENTS", "ANNOTATIONS_STATE"));
        
        List<PropertyType> propertyTypes = v3.searchPropertyTypes(sessionToken, new PropertyTypeSearchCriteria(), new PropertyTypeFetchOptions()).getObjects();
        boolean name = false;
        boolean default_object_type = false;
        boolean xmlcomments = false;
        boolean annotations_state = false;
        
        for(PropertyType propertyType:propertyTypes) {
            if(propertyType.getCode().equals("NAME")) {
                name = true;
            }
            if(propertyType.getCode().equals("DEFAULT_OBJECT_TYPE")) {
                default_object_type = true;
            }
            if(propertyType.getCode().equals("XMLCOMMENTS")) {
                xmlcomments = true;
            }
            if(propertyType.getCode().equals("ANNOTATIONS_STATE")) {
                annotations_state = true;
            }
        }
        
        List<PropertyTypeCreation> toCreate = new ArrayList<>();
        
        if(!name) {
            PropertyTypeCreation NAME = new PropertyTypeCreation();
            NAME.setCode("NAME");
            NAME.setLabel("Name");
            NAME.setDescription("Name");
            NAME.setDataType(DataType.VARCHAR);
            toCreate.add(NAME);
        }
        
        if(!default_object_type) {
            PropertyTypeCreation DEFAULT_OBJECT_TYPE = new PropertyTypeCreation();
            DEFAULT_OBJECT_TYPE.setCode("DEFAULT_OBJECT_TYPE");
            DEFAULT_OBJECT_TYPE.setLabel("Default Object Type");
            DEFAULT_OBJECT_TYPE.setDescription("Default Object Type");
            DEFAULT_OBJECT_TYPE.setDataType(DataType.VARCHAR);
            toCreate.add(DEFAULT_OBJECT_TYPE);
        }
        
        if(!xmlcomments) {
            PropertyTypeCreation XMLCOMMENTS = new PropertyTypeCreation();
            XMLCOMMENTS.setCode("XMLCOMMENTS");
            XMLCOMMENTS.setLabel("XML Comments");
            XMLCOMMENTS.setDescription("XML Comments");
            XMLCOMMENTS.setDataType(DataType.XML);
            toCreate.add(XMLCOMMENTS);
        }
        
        if(!annotations_state) {
            PropertyTypeCreation ANNOTATIONS_STATE = new PropertyTypeCreation();
            ANNOTATIONS_STATE.setCode("ANNOTATIONS_STATE");
            ANNOTATIONS_STATE.setLabel("Annotations State");
            ANNOTATIONS_STATE.setDescription("Annotations State");
            ANNOTATIONS_STATE.setDataType(DataType.XML);
            toCreate.add(ANNOTATIONS_STATE);
        }
        
        if(!toCreate.isEmpty()) {
            v3.createPropertyTypes(sessionToken, toCreate);
        }
    }
    
    private static void createExperimentTypesIfMissing(String sessionToken, IApplicationServerApi v3) {
        ExperimentTypeSearchCriteria experimentTypeSearchCriteria = new ExperimentTypeSearchCriteria();
        experimentTypeSearchCriteria.withCode().thatEquals("COLLECTION");
        
        List<ExperimentType> experimentType = v3.searchExperimentTypes(sessionToken, new ExperimentTypeSearchCriteria(), new ExperimentTypeFetchOptions()).getObjects();
        boolean collection = !experimentType.isEmpty();
        
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("NAME"));
        
        PropertyAssignmentCreation DEFAULT_OBJECT_TYPE = new PropertyAssignmentCreation();
        DEFAULT_OBJECT_TYPE.setPropertyTypeId(new PropertyTypePermId("DEFAULT_OBJECT_TYPE"));
        
        ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode("COLLECTION");
        creation.setDescription("Used as a folder for things.");
        creation.setPropertyAssignments(Arrays.asList(NAME,  DEFAULT_OBJECT_TYPE));
        
        if(!collection) {
            v3.createExperimentTypes(sessionToken, Arrays.asList(creation));
        }
    }
    
    private static SampleTypeCreation getSampleTypeORGANIZATION_UNIT() {
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("NAME"));
        
        PropertyAssignmentCreation XMLCOMMENTS = new PropertyAssignmentCreation();
        XMLCOMMENTS.setPropertyTypeId(new PropertyTypePermId("XMLCOMMENTS"));
        XMLCOMMENTS.setShowInEditView(Boolean.FALSE);
        
        PropertyAssignmentCreation ANNOTATIONS_STATE = new PropertyAssignmentCreation();
        ANNOTATIONS_STATE.setPropertyTypeId(new PropertyTypePermId("ANNOTATIONS_STATE"));
        ANNOTATIONS_STATE.setShowInEditView(Boolean.FALSE);
        
        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode("ORGANIZATION_UNIT");
        creation.setDescription("Used to create different organisations for samples since they can't belong to more than one experiment.");
        creation.setPropertyAssignments(Arrays.asList(NAME, 
                XMLCOMMENTS, 
                ANNOTATIONS_STATE));
        return creation;
    }
    
    private static ProjectCreation getProjectCreation(String spaceCode, String projectCode, String description) {
        ProjectCreation projectCreation = new ProjectCreation();
        projectCreation.setCode(projectCode);
        projectCreation.setSpaceId(new SpacePermId(spaceCode));
        projectCreation.setDescription(description);
        return projectCreation;
    }
    
    private static ExperimentCreation getMicroscopyExperimentCollectionCreation(ProjectIdentifier projectIdentifier, String experimentCode) {
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("COLLECTION"));
        experimentCreation.setProperty("NAME", "Microscopy Experiment Collection");
        experimentCreation.setProperty("DEFAULT_OBJECT_TYPE", "MICROSCOPY_EXPERIMENT");
        experimentCreation.setCode(experimentCode);
        experimentCreation.setProjectId(projectIdentifier);
        return experimentCreation;
    }
    
    private static ExperimentCreation getOrganizationUnitCollectionCreation(ProjectIdentifier projectIdentifier, String experimentCode) {
        ExperimentCreation experimentCreation = new ExperimentCreation();
        experimentCreation.setTypeId(new EntityTypePermId("COLLECTION"));
        experimentCreation.setProperty("NAME", "Organization Unit Collection");
        experimentCreation.setProperty("DEFAULT_OBJECT_TYPE", "ORGANIZATION_UNIT");
        experimentCreation.setCode(experimentCode);
        experimentCreation.setProjectId(projectIdentifier);
        return experimentCreation;
    }
}
