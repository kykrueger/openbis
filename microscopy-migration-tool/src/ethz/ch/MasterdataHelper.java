package ethz.ch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.DataType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.fetchoptions.PropertyTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.id.PropertyTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.search.PropertyTypeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.SampleType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.create.SampleTypeCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleTypeFetchOptions;

public class MasterdataHelper
{
    //
    // ELN properties and types
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
        propertyTypeSearchCriteria.withCodes().setFieldValue(Arrays.asList("$NAME", "$DESCRIPTION", "NOTES", "$DEFAULT_OBJECT_TYPE", "$XMLCOMMENTS", "$ANNOTATIONS_STATE"));
        propertyTypeSearchCriteria.withOrOperator();
        
        List<PropertyType> propertyTypes = v3.searchPropertyTypes(sessionToken, propertyTypeSearchCriteria, new PropertyTypeFetchOptions()).getObjects();
        boolean name = false;
        boolean description = false;
        boolean notes = false;
        boolean default_object_type = false;
        boolean xmlcomments = false;
        boolean annotations_state = false;
        
        for(PropertyType propertyType:propertyTypes) {
            if(propertyType.getCode().equals("$NAME")) {
                name = true;
            }
            if(propertyType.getCode().equals("$DESCRIPTION")) {
                description = true;
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
        
        if(!description) {
            PropertyTypeCreation DESCRIPTION = new PropertyTypeCreation();
            DESCRIPTION.setCode("$DESCRIPTION");
            DESCRIPTION.setLabel("Description");
            DESCRIPTION.setDescription("Description");
            DESCRIPTION.setDataType(DataType.VARCHAR);
            DESCRIPTION.setInternalNameSpace(true);
            toCreate.add(DESCRIPTION);
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
        
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("$NAME"));
        
        PropertyAssignmentCreation DEFAULT_OBJECT_TYPE = new PropertyAssignmentCreation();
        DEFAULT_OBJECT_TYPE.setPropertyTypeId(new PropertyTypePermId("$DEFAULT_OBJECT_TYPE"));
        
        ExperimentTypeCreation creation = new ExperimentTypeCreation();
        creation.setCode("COLLECTION");
        creation.setDescription("Used as a folder for things.");
        creation.setPropertyAssignments(Arrays.asList(NAME,  DEFAULT_OBJECT_TYPE));
        
        createExperimentTypeIfMissing(sessionToken, v3, creation);
    }
    
    //
    // Type to translate Attachments to DataSets
    //
    
    public static DataSetTypeCreation getDataSetTypeATTACHMENT() {
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("$NAME"));
        
        PropertyAssignmentCreation DESCRIPTION = new PropertyAssignmentCreation();
        DESCRIPTION.setPropertyTypeId(new PropertyTypePermId("$DESCRIPTION"));
        
        PropertyAssignmentCreation NOTES = new PropertyAssignmentCreation();
        NOTES.setPropertyTypeId(new PropertyTypePermId("NOTES"));
        
        PropertyAssignmentCreation XMLCOMMENTS = new PropertyAssignmentCreation();
        XMLCOMMENTS.setPropertyTypeId(new PropertyTypePermId("$XMLCOMMENTS"));
        XMLCOMMENTS.setShowInEditView(Boolean.FALSE);
        
        
        DataSetTypeCreation creation = new DataSetTypeCreation();
        creation.setCode("ATTACHMENT");
        creation.setDescription("Used to attach files to entities.");
        creation.setPropertyAssignments(Arrays.asList(NAME, DESCRIPTION, NOTES, XMLCOMMENTS));
        
        return creation;
    }
    
    //
    // Type to translate Tags to Samples
    //
    
    public static SampleTypeCreation getSampleTypeORGANIZATION_UNIT() {
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("$NAME"));
        
        PropertyAssignmentCreation DESCRIPTION = new PropertyAssignmentCreation();
        DESCRIPTION.setPropertyTypeId(new PropertyTypePermId("$DESCRIPTION"));
        
        PropertyAssignmentCreation XMLCOMMENTS = new PropertyAssignmentCreation();
        XMLCOMMENTS.setPropertyTypeId(new PropertyTypePermId("$XMLCOMMENTS"));
        XMLCOMMENTS.setShowInEditView(Boolean.FALSE);
        
        PropertyAssignmentCreation ANNOTATIONS_STATE = new PropertyAssignmentCreation();
        ANNOTATIONS_STATE.setPropertyTypeId(new PropertyTypePermId("$ANNOTATIONS_STATE"));
        ANNOTATIONS_STATE.setShowInEditView(Boolean.FALSE);
        
        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode("ORGANIZATION_UNIT");
        creation.setDescription("Used to create different organisations for samples since they can't belong to more than one experiment.");
        creation.setPropertyAssignments(Arrays.asList(NAME, DESCRIPTION,
                XMLCOMMENTS, 
                ANNOTATIONS_STATE));
        creation.setGeneratedCodePrefix("OU.");
        creation.setAutoGeneratedCode(true);
        return creation;
    }
    
    //
    // Exists
    //
    
    public static boolean doDataSetTypeExist(String sessionToken, IApplicationServerApi v3, String dataSetTypeCode) {
        Collection<DataSetType> c = v3.getDataSetTypes(sessionToken, Arrays.asList(new EntityTypePermId(dataSetTypeCode)), new DataSetTypeFetchOptions()).values();
        return !c.isEmpty();
    }
    
    public static boolean doSampleTypeExist(String sessionToken, IApplicationServerApi v3, String sampleTypeCode) {
        Collection<SampleType> c = v3.getSampleTypes(sessionToken, Arrays.asList(new EntityTypePermId(sampleTypeCode)), new SampleTypeFetchOptions()).values();
        return !c.isEmpty();
    }
    
    public static boolean doExperimentTypeExist(String sessionToken, IApplicationServerApi v3, String experimentTypeCode) {
        Collection<ExperimentType> c = v3.getExperimentTypes(sessionToken, Arrays.asList(new EntityTypePermId(experimentTypeCode)), new ExperimentTypeFetchOptions()).values();
        return !c.isEmpty();
    }
    
    //
    // Create if missing
    //
    
    public static void createSampleTypeIfMissing(String sessionToken, IApplicationServerApi v3, SampleTypeCreation toCreate) {
        if(!doSampleTypeExist(sessionToken, v3, toCreate.getCode())) {
            v3.createSampleTypes(sessionToken, Arrays.asList(toCreate));
        }
    }
    
    public static void createExperimentTypeIfMissing(String sessionToken, IApplicationServerApi v3, ExperimentTypeCreation toCreate) {
        if(!doExperimentTypeExist(sessionToken, v3, toCreate.getCode())) {
            v3.createExperimentTypes(sessionToken, Arrays.asList(toCreate));
        }
    }
    
    //
    // Experiment Type to Sample Type translator
    //
    
    public static void createDefaultSampleType(String sessionToken, IApplicationServerApi v3, String sampleTypeCode) {
        SampleTypeCreation stc = getDefaultSampleType(sampleTypeCode, "N/A", true);
        createSampleTypeIfMissing(sessionToken, v3, stc);
    }
    
    public static void createSampleTypesFromExperimentTypes(String sessionToken, IApplicationServerApi v3, List<String> experimentTypeIds) {
        List<EntityTypePermId> ids = new ArrayList<EntityTypePermId>();
        for(String experimentTypeId:experimentTypeIds) {
            ids.add(new EntityTypePermId(experimentTypeId));
        }
        ExperimentTypeFetchOptions fo = new ExperimentTypeFetchOptions();
        fo.withPropertyAssignments().withPropertyType();
        
        Collection<ExperimentType>  experimentTypes = v3.getExperimentTypes(sessionToken, ids, fo).values();
        for(ExperimentType experimentType:experimentTypes) {
            SampleTypeCreation stc = getSampleTypeFromExperimentType(experimentType);
            createSampleTypeIfMissing(sessionToken, v3, stc);
        }
    }
    
    public static SampleTypeCreation getDefaultSampleType(String sampleTypeCode, String sampleTypeDescription, boolean autoGeneratedCode) {
        SampleTypeCreation creation = new SampleTypeCreation();
        creation.setCode(sampleTypeCode);
        creation.setDescription(sampleTypeDescription);
        creation.setAutoGeneratedCode(autoGeneratedCode);
        creation.setGeneratedCodePrefix(sampleTypeCode + ".");
        
        List<PropertyAssignmentCreation> propertyAssignmentCreations = new ArrayList<PropertyAssignmentCreation>();
        
        PropertyAssignmentCreation NAME = new PropertyAssignmentCreation();
        NAME.setPropertyTypeId(new PropertyTypePermId("$NAME"));
        NAME.setSection("General Info");
        NAME.setShowInEditView(Boolean.TRUE);
        
        PropertyAssignmentCreation ANNOTATIONS_STATE = new PropertyAssignmentCreation();
        ANNOTATIONS_STATE.setPropertyTypeId(new PropertyTypePermId("$ANNOTATIONS_STATE"));
        ANNOTATIONS_STATE.setSection("General Info");
        ANNOTATIONS_STATE.setShowInEditView(Boolean.FALSE);
        
        propertyAssignmentCreations.add(NAME);
        propertyAssignmentCreations.add(ANNOTATIONS_STATE);
        
        creation.setPropertyAssignments(propertyAssignmentCreations);
        
        return creation;
    }
    
    public static SampleTypeCreation getSampleTypeFromExperimentType(ExperimentType experimentType) {
        SampleTypeCreation creation = getDefaultSampleType(experimentType.getCode(), experimentType.getDescription(), false);
        
        for(PropertyAssignment propertyAssignment:experimentType.getPropertyAssignments()) {
            PropertyAssignmentCreation pac = new PropertyAssignmentCreation();
            pac.setPropertyTypeId(propertyAssignment.getPropertyType().getPermId());
            pac.setShowInEditView(propertyAssignment.isShowInEditView());
            pac.setSection(propertyAssignment.getSection());
            creation.getPropertyAssignments().add(pac);
        }
        
        return creation;
    }
}
