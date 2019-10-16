package ch.systemsx.cisd.openbis.generic.server.hotfix;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.update.ListUpdateValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.EntityTypePermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.id.IEntityTypeId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentTypeFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentTypeUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyAssignment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.PropertyType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.property.create.PropertyAssignmentCreation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.math.BigInteger;
import java.util.*;


public class ELNCollectionTypeMigration {

    private static final String COLLECTION = "COLLECTION";

    private static final String[] experimentsOfTypeCollection = new String[]{
            // ELN-LIMS TYPES
        "STORAGES_COLLECTION",
        "GENERAL_PROTOCOLS",
        "PRODUCT_COLLECTION",
        "SUPPLIER_COLLECTION",
        "REQUEST_COLLECTION",
        "ORDER_COLLECTION",
        "TEMPLATES_COLLECTION",
        "PUBLICATIONS_COLLECTION",
            // ELN-LIMS LIFE SCIENCES TYPES
        "ANTIBODY_COLLECTION",
        "CHEMICAL_COLLECTION",
        "ENZYME_COLLECTION",
        "MEDIA_COLLECTION",
        "SOLUTION_BUFFER_COLLECTION",
        "BACTERIA_COLLECTION",
        "CELL_LINE_COLLECTION",
        "FLY_COLLECTION",
        "YEAST_COLLECTION",
        "PLASMID_COLLECTION",
        "OLIGO_COLLECTION",
        "RNA_COLLECTION",
        "PCR_PROTOCOLS",
        "WESTERN_BLOTTING_PROTOCOLS",
        "PLANT_COLLECTION"
    };

    private static Set<ExperimentType> getExperimentTypes(String[] experimentCodes) {
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = api.loginAsSystem();

        ExperimentSearchCriteria esc = new ExperimentSearchCriteria();
        esc.withOrOperator();
        for (String experimentOfTypeCollection:experimentCodes) {
            esc.withCode().thatEquals(experimentOfTypeCollection);
        }

        ExperimentFetchOptions efo = new ExperimentFetchOptions();
        efo.withType().withPropertyAssignments().withPropertyType();

        SearchResult<Experiment> experimentSearchResult = api.searchExperiments(sessionToken, esc, efo);

        Set<ExperimentType> experimentTypes = new HashSet<>();
        for (Experiment experiment:experimentSearchResult.getObjects()) {
            experimentTypes.add(experiment.getType());
        }

        return experimentTypes;
    }

    private static ExperimentType getExperimentType(String experimentTypeCode) {
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = api.loginAsSystem();

        ExperimentTypeFetchOptions etfo = new ExperimentTypeFetchOptions();
        etfo.withPropertyAssignments().withPropertyType();

        EntityTypePermId COLLECTION = new EntityTypePermId(experimentTypeCode);
        Map<IEntityTypeId, ExperimentType> collectionType = api.getExperimentTypes(sessionToken, Arrays.asList(COLLECTION), etfo);
        ExperimentType experimentTypeCOLLECTION = collectionType.get(COLLECTION);
        return experimentTypeCOLLECTION;
    }

    private static Set<PropertyType> getPropertyTypes(ExperimentType type) {
        Set<PropertyType> propertyTypes = new HashSet<>();
        for (PropertyAssignment propertyAssignment:type.getPropertyAssignments()) {
            propertyTypes.add(propertyAssignment.getPropertyType());
        }
        return propertyTypes;
    }

    private static Set<String> getPropertyTypesCodes(ExperimentType type) {
        Set<String> propertyTypes = new HashSet<>();
        for (PropertyAssignment propertyAssignment:type.getPropertyAssignments()) {
            propertyTypes.add(propertyAssignment.getPropertyType().getCode());
        }
        return propertyTypes;
    }

    private static void addMissingProperties(String experimentTypeCode, Set<PropertyType> missingPropertyTypes) {
        IApplicationServerInternalApi api = CommonServiceProvider.getApplicationServerApi();
        String sessionToken = api.loginAsSystem();

        List<PropertyAssignmentCreation> propertiesToAdd = new ArrayList<>();
        for (PropertyType propertyType:missingPropertyTypes) {
            PropertyAssignmentCreation pac = new PropertyAssignmentCreation();
            pac.setPropertyTypeId(propertyType.getPermId());
            propertiesToAdd.add(pac);
        }

        EntityTypePermId experimentPermId = new EntityTypePermId(experimentTypeCode);
        ExperimentTypeUpdate experimentTypeUpdate = new ExperimentTypeUpdate();
        experimentTypeUpdate.setTypeId(experimentPermId);
        ListUpdateValue.ListUpdateActionAdd luaa = new ListUpdateValue.ListUpdateActionAdd();
        luaa.setItems(propertiesToAdd);

        experimentTypeUpdate.setPropertyAssignmentActions(Arrays.asList(luaa));

        List<ExperimentTypeUpdate> experimentTypeUpdates = new ArrayList<>();
        experimentTypeUpdates.add(experimentTypeUpdate);
        if (!experimentTypeUpdates.isEmpty()) {
            api.updateExperimentTypes(sessionToken, experimentTypeUpdates);
        }
    }

    private static List executeQuery(String SQL, String key, Object value) {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
        NativeQuery nativeQuery = currentSession.createNativeQuery(SQL);
        nativeQuery.setParameter(key, value);
        List<Object[]> result = nativeQuery.getResultList();
        return result;
    }

    private static void executeUpdate(String SQL, String ukey, Object uValue, String ckey, Object cValue) {
        DAOFactory daoFactory = (DAOFactory) CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY);
        Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
        NativeQuery nativeQuery = currentSession.createNativeQuery(SQL);
        nativeQuery.setParameter(ukey, uValue);
        nativeQuery.setParameter(ckey, cValue);
        nativeQuery.executeUpdate();
    }

    private static Map<BigInteger, BigInteger> getMap(List<Object[]> prty_id_AND_etpt_id) {
        Map<BigInteger, BigInteger> prty_id_2_etpt_id = new HashMap();
        for (Object[] row:prty_id_AND_etpt_id) {
            prty_id_2_etpt_id.put((BigInteger)row[0], (BigInteger)row[1]);
        }
        return prty_id_2_etpt_id;
    }

    public static void migrate() {
        // Obtain property types used by experiments that should be of type COLLECTION
        for (String experimentCode:experimentsOfTypeCollection) {
            Set<ExperimentType> experimentTypes = getExperimentTypes(new String[]{experimentCode});
            if (!experimentTypes.isEmpty()) {
                ExperimentType experimentType = getExperimentTypes(new String[]{experimentCode}).iterator().next();
                if (!experimentType.getCode().equals(COLLECTION)) {
                    // Property Type used
                    Set<PropertyType> propertyTypes = getPropertyTypes(experimentType);

                    // Property Type used in collection
                    Set<String> collectionPropertyTypes = getPropertyTypesCodes(getExperimentType(COLLECTION));

                    // Property Type NOT used in collection
                    Set<PropertyType> missingInCollection = new HashSet<>();
                    for (PropertyType propertyType:propertyTypes) {
                        if (!collectionPropertyTypes.contains(propertyType.getCode())) {
                            missingInCollection.add(propertyType);
                        }
                    }

                    // Add Property Type missing to collection
                    addMissingProperties(COLLECTION, missingInCollection);

                    //
                    // Swap experiment property types assignments that NOW should also exist in COLLECTION
                    //

                    // Current type id
                    final String COLLECTION_TYPE_ID = "SELECT id FROM experiment_types WHERE code = :code";
                    BigInteger collectionTypeTechId = (BigInteger) executeQuery(COLLECTION_TYPE_ID, "code", COLLECTION).get(0);
                    final String EXPERIMENT_TYPE_ID = "SELECT exty_id FROM experiments_all WHERE code = :code";
                    BigInteger experimentTypeTechId = (BigInteger) executeQuery(EXPERIMENT_TYPE_ID, "code", experimentCode).get(0);

                    // Current type properties
                    final String EXPERIMENT_PROPERTY_TYPE_IDS = "SELECT etpt.prty_id, etpt.id FROM experiment_type_property_types etpt WHERE etpt.exty_id = :exty_id";
                    Map<BigInteger, BigInteger> collection_prty_id_2_etpt_id = getMap(executeQuery(EXPERIMENT_PROPERTY_TYPE_IDS, "exty_id", collectionTypeTechId));
                    Map<BigInteger, BigInteger> experiment_prty_id_2_etpt_id = getMap(executeQuery(EXPERIMENT_PROPERTY_TYPE_IDS, "exty_id", experimentTypeTechId));

                    // Update properties
                    for (BigInteger propertyTechId:experiment_prty_id_2_etpt_id.keySet()) {
                        BigInteger oldAssignment = experiment_prty_id_2_etpt_id.get(propertyTechId);
                        BigInteger newAssignment = collection_prty_id_2_etpt_id.get(propertyTechId);
                        final String UPDATE_PROPERTY_ASSIGNMENT = "UPDATE experiment_properties SET etpt_id = :new_etpt_id WHERE etpt_id = :old_etpt_id";
                        System.out.println("ELNCollectionTypeMigration - Swap for property tech id : " + propertyTechId + " : " + oldAssignment + " <> " + newAssignment);
                        executeUpdate(UPDATE_PROPERTY_ASSIGNMENT, "old_etpt_id", oldAssignment, "new_etpt_id", newAssignment);
                    }

                    // Update type
                    final String UPDATE_TYPE = "UPDATE experiments_all SET exty_id = :exty_id WHERE code = :code";
                    executeUpdate(UPDATE_TYPE, "exty_id", collectionTypeTechId, "code", experimentCode);
                    System.out.println("ELNCollectionTypeMigration -  Update for : " + experimentCode + " : exty_id : " + collectionTypeTechId);

                }
            }
        }
    }
}
