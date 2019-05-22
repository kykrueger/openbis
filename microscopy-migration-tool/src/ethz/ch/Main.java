package ethz.ch;

import java.util.*;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.Project;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ethz.ch.dataset.DataSetPropertyCopy;
import ethz.ch.dataset.DatasetCreationHelper;
import ethz.ch.experiment.Experiment2Sample;
import ethz.ch.experiment.Experiment2SampleTranslator;
import ethz.ch.experiment.ExperimentType2SampleType;
import ethz.ch.property.Property2Sample;
import ethz.ch.property.Property2SampleTranslator;
import ethz.ch.property.EntityPropertyCopy;
import ethz.ch.property.PropertyType2SampleType;
import ethz.ch.sample.SamplePropertyCopy;
import ethz.ch.ssl.SslCertificateHelper;
import ethz.ch.tag.Tag2SampleTranslator;

public class Main
{   
    private static final String OPENBIS_LOCAL_DEV = "http://localhost:8888";
    private static final String DSS_LOCAL_DEV = "http://localhost:8889";
    
    private static final int TIMEOUT = Integer.MAX_VALUE;
    
    private static final List<String> EXCLUDE_SPACES = Collections.EMPTY_LIST;
    
    public static void main(String[] args) throws Exception
    {
        if(args.length == 4) {
            String AS_URL = args[0] + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
            String DSS_URL = args[1] + "/datastore_server" + IDataStoreServerApi.SERVICE_URL;
            DatasetCreationHelper.setDssURL(args[1]);
            String user = args[2];
            String pass = args[3];
            
            System.out.println("AS_URL : [" + AS_URL +"]");
            System.out.println("DSS_URL : [" + DSS_URL +"]");
            System.out.println("user : [" + user +"]");
            System.out.println("pass : [" + pass +"]");
            
            doTheWork(true, AS_URL, DSS_URL, user, pass, true, true, true);
        } else {
// Used for development
//            String AS_URL = OPENBIS_LOCAL_DEV + "/openbis/openbis" + IApplicationServerApi.SERVICE_URL;
//            String DSS_URL = DSS_LOCAL_DEV + "/datastore_server" + IDataStoreServerApi.SERVICE_URL;
//            doTheWork(true, AS_URL, DSS_URL, "pontia", "test", true, true, true);
            System.out.println("Example: java -jar microscopy_migration_tool.jar https://openbis-as-domain.ethz.ch https://openbis-dss-domain.ethz.ch user password");
        }
    }
    
    private static void doTheWork(boolean COMMIT_CHANGES_TO_OPENBIS, String AS_URL, String DSS_URL, String userId, String pass, boolean installELNTypes, boolean migrateData, boolean deleteOldExperiments) throws Exception {
        System.out.println("Migration Started");
        SslCertificateHelper.trustAnyCertificate(AS_URL);
        SslCertificateHelper.trustAnyCertificate(DSS_URL);
        IApplicationServerApi v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, AS_URL, TIMEOUT);
        IDataStoreServerApi v3dss = HttpInvokerUtils.createServiceStub(IDataStoreServerApi.class, DSS_URL, TIMEOUT);
        String sessionToken = v3.login(userId, pass);
        System.out.println("userId, pass, sessionToken : [" + userId +"][" + pass +"][" + sessionToken +"]");
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
            // Delete old experiment type
            MasterdataHelper.deleteExperimentType(sessionToken, v3, config.getTypeCode());
            // Delete old name property from new sample type
            config.getSamplePropertyDelete().deleteOldPropertyType(sessionToken, v3);
        }

        System.out.println("4. Translate Properties to Samples");

        PropertyType2SampleType FACS_ARIA_TUBE =    new PropertyType2SampleType(        "FACS_ARIA_TUBE",   "FACS_ARIA_SPECIMEN",   "FACS_ARIA_SPECIMEN",   "$NAME", true, false);
        PropertyType2SampleType FACS_ARIA_WELL =    new PropertyType2SampleType(        "FACS_ARIA_WELL",   "FACS_ARIA_SPECIMEN",   "FACS_ARIA_SPECIMEN",   "$NAME", true, true);
        PropertyType2SampleType INFLUX_TUBE =       new PropertyType2SampleType(        "INFLUX_TUBE",      "INFLUX_SPECIMEN",      "INFLUX_SPECIMEN",      "$NAME", true, true);
        PropertyType2SampleType LSR_FORTESSA_TUBE = new PropertyType2SampleType(        "LSR_FORTESSA_TUBE","LSR_FORTESSA_SPECIMEN","LSR_FORTESSA_SPECIMEN","$NAME", true, false);
        PropertyType2SampleType LSR_FORTESSA_WELL = new PropertyType2SampleType(        "LSR_FORTESSA_WELL","LSR_FORTESSA_SPECIMEN","LSR_FORTESSA_SPECIMEN","$NAME", true, true);
        PropertyType2SampleType MOFLO_XDP_TUBE =    new PropertyType2SampleType(        "MOFLO_XDP_TUBE",   "MOFLO_XDP_SPECIMEN",   "MOFLO_XDP_SPECIMEN",   "$NAME", true, true);
        PropertyType2SampleType S3E_TUBE =          new PropertyType2SampleType(        "S3E_TUBE",         "S3E_SPECIMEN",         "S3E_SPECIMEN",         "$NAME", true, true);

        List<PropertyType2SampleType> propertiesMigrationConfigs = Arrays.asList(FACS_ARIA_TUBE,
                FACS_ARIA_WELL,
                INFLUX_TUBE,
                LSR_FORTESSA_TUBE,
                LSR_FORTESSA_WELL,
                MOFLO_XDP_TUBE,
                S3E_TUBE);

        for(PropertyType2SampleType propertyMigrationConfigs:propertiesMigrationConfigs) {
            if(COMMIT_CHANGES_TO_OPENBIS && !MasterdataHelper.doSampleTypeExist(sessionToken, v3, propertyMigrationConfigs.getNewSampleTypeCode())) {
                MasterdataHelper.createDefaultSampleType(sessionToken, v3, propertyMigrationConfigs.getNewSampleTypeCode());
                System.out.println(propertyMigrationConfigs.getNewSampleTypeCode() + " Sample Type installed.");
            } else {
                System.out.println(propertyMigrationConfigs.getNewSampleTypeCode() + " Sample Type installation skipped.");
            }
        }

        for(PropertyType2SampleType config:propertiesMigrationConfigs) {
            int total = 0;
            SampleSearchCriteria sampleSearchCriteria = new SampleSearchCriteria();
            sampleSearchCriteria.withType().withCode().thatEquals(config.getOldSampleTypeCode());
            SearchResult<Sample> samples = v3.searchSamples(sessionToken, sampleSearchCriteria, new SampleFetchOptions());
            for(Sample sample:samples.getObjects()) {
                Property2Sample toMigrate = new Property2Sample(config, sample.getPermId());
                Property2SampleTranslator.translate(sessionToken, v3, v3dss, toMigrate, COMMIT_CHANGES_TO_OPENBIS);
                total++;
                System.out.println("[DONE] " + config.getOldSampleTypeCode() + "\t" + total + "/" + samples.getTotalCount());
            }
            // Delete old copied property that become a sample
            config.getEntityPropertyDelete().deleteOldPropertyType(sessionToken, v3);
        }

        System.out.println("5. Copy Property A to Property B on Samples and DataSets");
        EntityPropertyCopy LSR_FORTESSA_PLATE_p =           new SamplePropertyCopy( "LSR_FORTESSA_PLATE",       "LSR_FORTESSA_PLATE_NAME",             "$NAME");
        EntityPropertyCopy LSR_FORTESSA_TUBE_p =            new SamplePropertyCopy( "LSR_FORTESSA_TUBE",        "LSR_FORTESSA_TUBE_NAME",              "$NAME");
        EntityPropertyCopy LSR_FORTESSA_WELL_p =            new SamplePropertyCopy( "LSR_FORTESSA_WELL",        "LSR_FORTESSA_WELL_NAME",              "$NAME");
        EntityPropertyCopy FACS_ARIA_TUBE_p =               new SamplePropertyCopy( "FACS_ARIA_TUBE",           "FACS_ARIA_TUBE_NAME",                 "$NAME");
        EntityPropertyCopy INFLUX_TUBE_p =                  new SamplePropertyCopy( "INFLUX_TUBE",              "INFLUX_TUBE_NAME",                    "$NAME");
        EntityPropertyCopy MOFLO_XDP_TUBE_p =               new SamplePropertyCopy( "MOFLO_XDP_TUBE",           "MOFLO_XDP_TUBE_NAME",                 "$NAME");
        EntityPropertyCopy S3E_TUBE_p =                     new SamplePropertyCopy( "S3E_TUBE",                 "S3E_NAME",                            "$NAME");
        EntityPropertyCopy MICROSCOPY_SAMPLE_TYPE_p =       new SamplePropertyCopy( "MICROSCOPY_SAMPLE_TYPE",   "MICROSCOPY_SAMPLE_NAME",              "$NAME");
        EntityPropertyCopy MICROSCOPY_ACCESSORY_FILE_p =    new DataSetPropertyCopy("MICROSCOPY_ACCESSORY_FILE","MICROSCOPY_ACCESSORY_FILE_NAME",      "$NAME");

        List<EntityPropertyCopy> propertyCopiesMigrationConfig = Arrays.asList(LSR_FORTESSA_PLATE_p,
                LSR_FORTESSA_TUBE_p,
                LSR_FORTESSA_WELL_p,
                FACS_ARIA_TUBE_p,
                INFLUX_TUBE_p,
                MOFLO_XDP_TUBE_p,
                S3E_TUBE_p,
                MICROSCOPY_SAMPLE_TYPE_p,
                MICROSCOPY_ACCESSORY_FILE_p);

        for (EntityPropertyCopy config:propertyCopiesMigrationConfig) {
            config.copy(sessionToken, v3);
            // Delete old copied name property
            config.getEntityPropertyDelete().deleteOldPropertyType(sessionToken, v3);
        }
    }
}
