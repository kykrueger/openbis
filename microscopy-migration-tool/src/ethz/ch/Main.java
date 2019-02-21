package ethz.ch;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ethz.ch.dataset.DatasetCreationHelper;
import ethz.ch.experiment.Experiment2Sample;
import ethz.ch.experiment.Experiment2SampleTranslator;
import ethz.ch.experiment.ExperimentType2SampleType;
import ethz.ch.property.PropertyType2SampleType;
import ethz.ch.ssl.SslCertificateHelper;
import ethz.ch.tag.Tag2SampleTranslator;

public class Main
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
    
    private static void doTheWork(boolean COMMIT_CHANGES_TO_OPENBIS, String AS_URL, String DSS_URL, String userId, String pass, boolean installELNTypes, boolean migrateData, boolean deleteOldExperiments) throws Exception {
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
        
        Tag2SampleTranslator.init("tags.txt");
        
        if(installELNTypes) {
            MasterdataHelper.installELNTypes(sessionToken, v3, COMMIT_CHANGES_TO_OPENBIS);
        }
        if(migrateData) {
            migrate(sessionToken, v3, v3dss, COMMIT_CHANGES_TO_OPENBIS);
        }
        v3.logout(sessionToken);
        System.out.println("Migration Finished");
    }
    
    private static void migrate(String sessionToken, IApplicationServerApi v3, IDataStoreServerApi v3dss,boolean COMMIT_CHANGES_TO_OPENBIS) throws Exception {
        
        //
        // Experiment types to migrate as samples
        //
        
        ExperimentType2SampleType MICROSCOPY_EXPERIMENT = new ExperimentType2SampleType("MICROSCOPY_EXPERIMENT",    "MICROSCOPY_EXPERIMENTS_COLLECTION", "MICROSCOPY_EXPERIMENT_NAME");
        ExperimentType2SampleType FACS_ARIA_EXPERIMENT = new ExperimentType2SampleType("FACS_ARIA_EXPERIMENT",      "FLOW_SORTERS_EXPERIMENTS_COLLECTION", "FACS_ARIA_EXPERIMENT_NAME");
        ExperimentType2SampleType INFLUX_EXPERIMENT = new ExperimentType2SampleType("INFLUX_EXPERIMENT",            "FLOW_SORTERS_EXPERIMENTS_COLLECTION", "INFLUX_EXPERIMENT_NAME");
        ExperimentType2SampleType LSR_FORTESSA_EXPERIMENT = new ExperimentType2SampleType("LSR_FORTESSA_EXPERIMENT","FLOW_ANALYZERS_EXPERIMENTS_COLLECTION", "LSR_FORTESSA_EXPERIMENT_NAME");
        ExperimentType2SampleType MOFLO_XDP_EXPERIMENT = new ExperimentType2SampleType("MOFLO_XDP_EXPERIMENT",      "FLOW_SORTERS_EXPERIMENTS_COLLECTION", "MOFLO_XDP_EXPERIMENT_NAME");
        ExperimentType2SampleType S3E_EXPERIMENT = new ExperimentType2SampleType("S3E_EXPERIMENT",                  "FLOW_SORTERS_EXPERIMENTS_COLLECTION", "S3E_EXPERIMENT_NAME");
        
        //
        // 1. Installing new sample types
        //
        System.out.println("1. Installing types");
        
        // Install Sample Types for Experiment Types
        List<ExperimentType2SampleType> experimentMigrationConfigs = Arrays.asList(MICROSCOPY_EXPERIMENT,
                FACS_ARIA_EXPERIMENT,
                INFLUX_EXPERIMENT,
                LSR_FORTESSA_EXPERIMENT,
                MOFLO_XDP_EXPERIMENT,
                S3E_EXPERIMENT);
        
        for(ExperimentType2SampleType experimentMigrationConfig:experimentMigrationConfigs) {
            if(COMMIT_CHANGES_TO_OPENBIS && !MasterdataHelper.doSampleTypeExist(sessionToken, v3, experimentMigrationConfig.getTypeCode())) {
                MasterdataHelper.createSampleTypesFromExperimentTypes(sessionToken, v3, Arrays.asList(experimentMigrationConfig.getTypeCode()));
                System.out.println(experimentMigrationConfig.getTypeCode() + " Sample Type installed.");
            } else {
                System.out.println(experimentMigrationConfig.getTypeCode() + " Sample Type installation skipped.");
            }
        }
        
        // Install Sample Type ORGANIZATION_UNIT
        if(COMMIT_CHANGES_TO_OPENBIS && !MasterdataHelper.doSampleTypeExist(sessionToken, v3, "ORGANIZATION_UNIT")) {
            v3.createSampleTypes(sessionToken, Collections.singletonList(MasterdataHelper.getSampleTypeORGANIZATION_UNIT()));
        }
        System.out.println("ORGANIZATION_UNIT Sample Type installed.");
        
        if(COMMIT_CHANGES_TO_OPENBIS && !MasterdataHelper.doDataSetTypeExist(sessionToken, v3, "ATTACHMENT")) {
            v3.createDataSetTypes(sessionToken, Collections.singletonList(MasterdataHelper.getDataSetTypeATTACHMENT()));
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
                    if(COMMIT_CHANGES_TO_OPENBIS && !MetadataHelper.doExperimentExist(v3, sessionToken, experimentIdentifierOU)) {
                        v3.createExperiments(sessionToken, Collections.singletonList(MetadataHelper.getOrganizationUnitCollectionCreation(project.getIdentifier(), "ORGANIZATION_UNITS_COLLECTION")));
                    }
                    System.out.println("Project Experiment Created: " + experimentIdentifierOU);
                }
                
                // Install Space level project and collection
                String projectIdentifier = "/" + spaceCode + "/COMMON_ORGANIZATION_UNITS";
                if(COMMIT_CHANGES_TO_OPENBIS && !MetadataHelper.doProjectExist(v3, sessionToken, projectIdentifier)) {
                    v3.createProjects(sessionToken, Collections.singletonList(MetadataHelper.getProjectCreation(spaceCode, "COMMON_ORGANIZATION_UNITS", "Folder to share common organization units collections.")));
                }
                System.out.println("Space Project Created: " + projectIdentifier);
                
                String experimentIdentifier = "/" + spaceCode + "/COMMON_ORGANIZATION_UNITS/ORGANIZATION_UNITS_COLLECTION";
                if(COMMIT_CHANGES_TO_OPENBIS && !MetadataHelper.doExperimentExist(v3, sessionToken, experimentIdentifier)) {
                    v3.createExperiments(sessionToken, Collections.singletonList(MetadataHelper.getOrganizationUnitCollectionCreation(new ProjectIdentifier("/" + spaceCode + "/COMMON_ORGANIZATION_UNITS"), "ORGANIZATION_UNITS_COLLECTION")));
                }
                System.out.println("Space Experiment Created: " + experimentIdentifier);
            }
        }
        
        System.out.println("3. Translate Experiment to Samples");
        
        for(ExperimentType2SampleType config:experimentMigrationConfigs) {
            int total = 0;
            ExperimentSearchCriteria experimentSearchCriteria = new ExperimentSearchCriteria();
            experimentSearchCriteria.withType().withCode().thatEquals(config.getTypeCode());
            SearchResult<Experiment> experiments = v3.searchExperiments(sessionToken, experimentSearchCriteria, new ExperimentFetchOptions());
            for(Experiment experiment:experiments.getObjects()) {
                Experiment2Sample toMigrate = new Experiment2Sample(config, experiment.getPermId());
                Experiment2SampleTranslator.translate(sessionToken, v3, v3dss, toMigrate, COMMIT_CHANGES_TO_OPENBIS);
                total++;
                System.out.println("[DONE] " + config.getTypeCode() + "\t" + total + "/" + experiments.getTotalCount());
            }
        }
        
        //
        //
        //
        
        System.out.println("4. Translate Properties to Samples");

        PropertyType2SampleType FACS_ARIA_TUBE =    new PropertyType2SampleType(        "FACS_ARIA_TUBE",   "FACS_ARIA_SPECIMEN",   "FACS_ARIA_SPECIMEN",   "$NAME");
        PropertyType2SampleType FACS_ARIA_WELL =    new PropertyType2SampleType(        "FACS_ARIA_WELL",   "FACS_ARIA_SPECIMEN",   "FACS_ARIA_SPECIMEN",   "$NAME");
        PropertyType2SampleType INFLUX_TUBE =       new PropertyType2SampleType(        "INFLUX_TUBE",      "INFLUX_SPECIMEN",      "INFLUX_SPECIMEN",      "$NAME");
        PropertyType2SampleType LSR_FORTESSA_TUBE = new PropertyType2SampleType(        "LSR_FORTESSA_TUBE","LSR_FORTESSA_SPECIMEN","LSR_FORTESSA_SPECIMEN","$NAME");
        PropertyType2SampleType LSR_FORTESSA_WELL = new PropertyType2SampleType(        "LSR_FORTESSA_WELL","LSR_FORTESSA_SPECIMEN","LSR_FORTESSA_SPECIMEN","$NAME");
        PropertyType2SampleType MOFLO_XDP_TUBE =    new PropertyType2SampleType(        "MOFLO_XDP_TUBE",   "MOFLO_XDP_SPECIMEN",   "MOFLO_XDP_SPECIMEN",   "$NAME");
        PropertyType2SampleType SE3_TUBE =          new PropertyType2SampleType(        "SE3_TUBE",         "SE3_SPECIMEN",         "SE3_SPECIMEN",         "$NAME");
        
        List<PropertyType2SampleType> propertiesMigrationConfigs = Arrays.asList(FACS_ARIA_TUBE,
                FACS_ARIA_WELL,
                INFLUX_TUBE,
                LSR_FORTESSA_TUBE,
                LSR_FORTESSA_WELL,
                MOFLO_XDP_TUBE,
                SE3_TUBE);
        
    }
}
