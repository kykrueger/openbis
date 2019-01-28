package ethz.ch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;

public class MigrationMasterdataHelper
{
    //
    // ELN TYPES
    //
    
    public static void installELNTypes(String sessionToken, IApplicationServerApi v3, boolean COMMIT_CHANGES_TO_OPENBIS) {
        System.out.println("Installing Missing ELN Types.");
        if(COMMIT_CHANGES_TO_OPENBIS) {
            createPropertiesIfMissing(sessionToken, v3);
            createExperimentTypesIfMissing(sessionToken, v3);
        }
        System.out.println("ELN Types installed.");
    }
    
    public static void createPropertiesIfMissing(String sessionToken, IApplicationServerApi v3) {
        PropertyTypeSearchCriteria propertyTypeSearchCriteria = new PropertyTypeSearchCriteria();
        propertyTypeSearchCriteria.withCodes().setFieldValue(Arrays.asList("$NAME", "NOTES", "$DEFAULT_OBJECT_TYPE", "$XMLCOMMENTS", "$ANNOTATIONS_STATE"));
        propertyTypeSearchCriteria.withOrOperator();
        
        List<PropertyType> propertyTypes = v3.searchPropertyTypes(sessionToken, propertyTypeSearchCriteria, new PropertyTypeFetchOptions()).getObjects();
        boolean name = false;
        boolean notes = false;
        boolean default_object_type = false;
        boolean xmlcomments = false;
        boolean annotations_state = false;
        
        for(PropertyType propertyType:propertyTypes) {
            if(propertyType.getCode().equals("$NAME")) {
                name = true;
            }
            if(propertyType.getCode().equals("NOTES")) {
                notes = true;
            }
            if(propertyType.getCode().equals("$DEFAULT_OBJECT_TYPE")) {
                default_object_type = true;
            }
            if(propertyType.getCode().equals("$XMLCOMMENTS")) {
                xmlcomments = true;
            }
            if(propertyType.getCode().equals("$ANNOTATIONS_STATE")) {
                annotations_state = true;
            }
        }
        
        List<PropertyTypeCreation> toCreate = new ArrayList<>();
        
        if(!name) {
            PropertyTypeCreation NAME = new PropertyTypeCreation();
            NAME.setCode("$NAME");
            NAME.setLabel("Name");
            NAME.setDescription("Name");
            NAME.setDataType(DataType.VARCHAR);
            NAME.setInternalNameSpace(true);
            toCreate.add(NAME);
        }
        
        if(!notes) {
            PropertyTypeCreation NOTES = new PropertyTypeCreation();
            NOTES.setCode("NOTES");
            NOTES.setLabel("Notes");
            NOTES.setDescription("Notes");
            NOTES.setDataType(DataType.VARCHAR);
            toCreate.add(NOTES);
        }
        
        if(!default_object_type) {
            PropertyTypeCreation DEFAULT_OBJECT_TYPE = new PropertyTypeCreation();
            DEFAULT_OBJECT_TYPE.setCode("$DEFAULT_OBJECT_TYPE");
            DEFAULT_OBJECT_TYPE.setLabel("Default Object Type");
            DEFAULT_OBJECT_TYPE.setDescription("Default Object Type");
            DEFAULT_OBJECT_TYPE.setDataType(DataType.VARCHAR);
            DEFAULT_OBJECT_TYPE.setInternalNameSpace(true);
            toCreate.add(DEFAULT_OBJECT_TYPE);
        }
        
        if(!xmlcomments) {
            PropertyTypeCreation XMLCOMMENTS = new PropertyTypeCreation();
            XMLCOMMENTS.setCode("$XMLCOMMENTS");
            XMLCOMMENTS.setLabel("XML Comments");
            XMLCOMMENTS.setDescription("XML Comments");
            XMLCOMMENTS.setDataType(DataType.XML);
            XMLCOMMENTS.setInternalNameSpace(true);
            toCreate.add(XMLCOMMENTS);
        }
        
        if(!annotations_state) {
            PropertyTypeCreation ANNOTATIONS_STATE = new PropertyTypeCreation();
            ANNOTATIONS_STATE.setCode("$ANNOTATIONS_STATE");
            ANNOTATIONS_STATE.setLabel("Annotations State");
            ANNOTATIONS_STATE.setDescription("Annotations State");
            ANNOTATIONS_STATE.setDataType(DataType.XML);
            ANNOTATIONS_STATE.setInternalNameSpace(true);
            toCreate.add(ANNOTATIONS_STATE);
        }
        
        if(!toCreate.isEmpty()) {
            v3.createPropertyTypes(sessionToken, toCreate);
        }
    }
    
    public static void createExperimentTypesIfMissing(String sessionToken, IApplicationServerApi v3) {
        ExperimentTypeSearchCriteria experimentTypeSearchCriteria = new ExperimentTypeSearchCriteria();
        experimentTypeSearchCriteria.withCode().thatEquals("COLLECTION");
        
        List<ExperimentType> experimentType = v3.searchExperimentTypes(sessionToken, experimentTypeSearchCriteria, new ExperimentTypeFetchOptions()).getObjects();
        boolean collection = !experimentType.isEmpty();
        
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("$NAME"));
        
        PropertyAssignmentCreation DEFAULT_OBJECT_TYPE = new PropertyAssignmentCreation();
        DEFAULT_OBJECT_TYPE.setPropertyTypeId(new PropertyTypePermId("$DEFAULT_OBJECT_TYPE"));
        
        ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode("COLLECTION");
        creation.setDescription("Used as a folder for things.");
        creation.setPropertyAssignments(Arrays.asList(NAME,  DEFAULT_OBJECT_TYPE));
        
        if(!collection) {
            v3.createExperimentTypes(sessionToken, Arrays.asList(creation));
        }
    }
    
    //
    // NEW TYPE TO MIGRATE ATTACHMENTS
    //
    
    public static DataSetTypeCreation getDataSetTypeATTACHMENT() {
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("$NAME"));
        
        PropertyAssignmentCreation NOTES = new PropertyAssignmentCreation();
        NOTES.setPropertyTypeId(new PropertyTypePermId("NOTES"));
        
        PropertyAssignmentCreation XMLCOMMENTS = new PropertyAssignmentCreation();
        XMLCOMMENTS.setPropertyTypeId(new PropertyTypePermId("$XMLCOMMENTS"));
        XMLCOMMENTS.setShowInEditView(Boolean.FALSE);
        
        
        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode("ATTACHMENT");
        creation.setDescription("Used to attach files to entities.");
        creation.setPropertyAssignments(Arrays.asList(NAME, NOTES, XMLCOMMENTS));
        
        return creation;
    }
    
    //
    // NEW TYPE TO MIGRATE TAGS
    //
    
    public static SampleTypeCreation getSampleTypeORGANIZATION_UNIT() {
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("$NAME"));
        
        PropertyAssignmentCreation XMLCOMMENTS = new PropertyAssignmentCreation();
        XMLCOMMENTS.setPropertyTypeId(new PropertyTypePermId("$XMLCOMMENTS"));
        XMLCOMMENTS.setShowInEditView(Boolean.FALSE);
        
        PropertyAssignmentCreation ANNOTATIONS_STATE = new PropertyAssignmentCreation();
        ANNOTATIONS_STATE.setPropertyTypeId(new PropertyTypePermId("$ANNOTATIONS_STATE"));
        ANNOTATIONS_STATE.setShowInEditView(Boolean.FALSE);
        
        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode("ORGANIZATION_UNIT");
        creation.setDescription("Used to create different organisations for samples since they can't belong to more than one experiment.");
        creation.setPropertyAssignments(Arrays.asList(NAME, 
                XMLCOMMENTS, 
                ANNOTATIONS_STATE));
        creation.setGeneratedCodePrefix("OU.");
        creation.setAutoGeneratedCode(true);
        return creation;
    }
    
    //
    // NEW MICROSCOPY_EXPERIMENT SAMPLE TYPE TO MIGRATE EXPERIMENT TYPE TO MIGRATE TAGS
    //
    
    public static SampleTypeCreation getSampleTypeMICROSCOPY_EXPERIMENT() {
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("$NAME"));
        
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
        ANNOTATIONS_STATE.setPropertyTypeId(new PropertyTypePermId("$ANNOTATIONS_STATE"));
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
}
