package ch.ethz.sis.benchmark.impl.jdbc;

import java.sql.Connection;
import java.util.*;

public class SQLQueries {

    //
    // Help Query methods
    //

    public static Map<String, Long> getIds(Connection connection, String QUERY, Collection<String> codes) {
        Map<String, Long> idsByCode = new HashMap<>();

        ArrayList arg = new ArrayList();
        arg.add(codes.toArray(new String[0]));

        List<Map<String, Object>> idsWithCodes = SQLExecutor.executeQuery(connection, QUERY, arg);
        for (Map<String, Object> idsWithCode:idsWithCodes) {
            idsByCode.put((String) idsWithCode.get("code"), (Long) idsWithCode.get("id"));
        }

        return idsByCode;
    }

    //
    // Query methods
    //

    private static final String SELECT_SPACES = "SELECT id, code FROM spaces WHERE code IN(SELECT * FROM unnest(?))";

    public static Map<String, Long> getSpaceIds(Connection connection, Collection<String> spaceCodes) {
        return getIds(connection, SELECT_SPACES, spaceCodes);
    }

    private static final String SELECT_PROJECTS = "SELECT id, code, space_id FROM projects WHERE FALSE";

    public static Map<String, Long> getProjectIds(Connection connection, Collection<String> projectIdentifiers, Map<String, Long> spaceIdsByCode) {
        Map<Long, String> spaceCodeByIds = revertMap(spaceIdsByCode);
        Map<String, Long> projectIdsByIdentifier = new HashMap<>();

        StringBuilder SELECT_PROJECTS_WITH_ORS = new StringBuilder(SELECT_PROJECTS);
        for (String projectIdentifier:projectIdentifiers) {
            Long space_id = spaceIdsByCode.get(projectIdentifier.split("/")[1]);
            String code = projectIdentifier.split("/")[2];
            SELECT_PROJECTS_WITH_ORS.append(" OR space_id = " + space_id + " AND code = '" + code + "'");
        }
        List<Map<String, Object>> projectIdsWithCodes = SQLExecutor.executeQuery(connection, SELECT_PROJECTS_WITH_ORS.toString(), Arrays.asList());
        for (Map<String, Object> projectIdsWithCode:projectIdsWithCodes) {
            projectIdsByIdentifier.put("/" + spaceCodeByIds.get(projectIdsWithCode.get("space_id"))  + "/" + projectIdsWithCode.get("code"), (Long) projectIdsWithCode.get("id"));
        }

        return projectIdsByIdentifier;
    }

    private static final String SELECT_EXPERIMENTS = "SELECT id, code, proj_id FROM experiments_all WHERE FALSE";

    public static Map<String, Long> getExperimentIds(Connection connection, Collection<String> experimentIdentifiers, Map<String, Long> projectIdsByIdentifier) {
        Map<Long, String> projectIdentifiersByIds = revertMap(projectIdsByIdentifier);
        Map<String, Long> experimentIdsByIdentifier = new HashMap<>();

        StringBuilder SELECT_EXPERIMENTS_WITH_ORS = new StringBuilder(SELECT_EXPERIMENTS);
        for (String experimentIdentifier:experimentIdentifiers) {
            String projectIdentifier = "/" + experimentIdentifier.split("/")[1] + "/" + experimentIdentifier.split("/")[2];
            Long proj_id = projectIdsByIdentifier.get(projectIdentifier);
            String code = experimentIdentifier.split("/")[3];
            SELECT_EXPERIMENTS_WITH_ORS.append(" OR (proj_id = " + proj_id + " AND code = '" + code + "')");
        }
        List<Map<String, Object>> experimentIdsWithCodes = SQLExecutor.executeQuery(connection, SELECT_EXPERIMENTS_WITH_ORS.toString(), Arrays.asList());
        for (Map<String, Object> experimentIdsWithCode:experimentIdsWithCodes) {
            experimentIdsByIdentifier.put(projectIdentifiersByIds.get(experimentIdsWithCode.get("proj_id"))  + "/" + experimentIdsWithCode.get("code"), (Long) experimentIdsWithCode.get("id"));
        }

        return experimentIdsByIdentifier;
    }

    private static final String SELECT_TYPES = "SELECT id, code FROM sample_types WHERE code IN(SELECT * FROM unnest(?))";

    public static Map<String, Long> getTypeIds(Connection connection, Collection<String> sampleTypeCodes) {
        return getIds(connection, SELECT_TYPES, sampleTypeCodes);
    }

    private static final String SELECT_PROPERTY_TYPES = "SELECT id, code FROM property_types WHERE code IN(SELECT * FROM unnest(?))";

    public static Map<String, Long> getPropertyTypeIds(Connection connection, Collection<String> propertyTypeCodes) {
        return getIds(connection, SELECT_PROPERTY_TYPES, propertyTypeCodes);
    }

    private static final String SELECT_PERSON = "SELECT id FROM persons WHERE user_id = ?";

    public static Long getPersonId(Connection connection, String user_id) {
        Map<String, Object> personId = SQLExecutor.executeQuery(connection, SELECT_PERSON, Arrays.asList(user_id)).get(0);
        return (Long) personId.get("id");
    }

//    /*
//     * samples_all table has several CONSTRAINTS that get executed on INSERT that chain more inserts that ara impossible due to a missing FK
//     * Because of that, all this contraints are actually doing nothing more than forcing the system to first insert the row and then do an update
//     *
//     * Example:
//     * INSERT INTO samples_all(id, perm_id, code, proj_id, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, space_id) VALUES(nextval('sample_id_seq'), 'fd4e9cec-ed92-455a-accd-8ff41ed83c6b', 'SAMPLE_lcd_nascar_adams', 17, 17, 2, NOW(), NOW(), 2, 2, 14) was aborted: ERROR: insert or update on table "experiment_relationships_history" violates foreign key constraint "exrelh_samp_fk"
//     *  Detail: Key (samp_id)=(6) is not present in table "samples_all".  Call getNextException to see other errors in the batch.
//     */
//
//    private static final String SAMPLE_INITIAL_INSERT = "INSERT INTO samples_all(id, perm_id, code, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier) VALUES(?, ?, ?, ?, NOW(), NOW(), ?, ?)";
//
//    private static final String SAMPLE_UPDATE_INSERT = "UPDATE samples_all SET proj_id = ?, expe_id = ?, space_id = ? WHERE id = ?";
//
//    // private static final String SAMPLE_INSERT = "INSERT INTO samples_all(id, perm_id, code, proj_id, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, space_id) VALUES(?, ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, ?)";
//
//    public static int insertSamplesOLD(Connection connection, List<List<Object>> samplesInsertArgs) {
//        List<List<Object>> SAMPLE_INITIAL_INSERT_ARGS = new ArrayList<>();
//        List<List<Object>> SAMPLE_UPDATE_INSERT_ARGS = new ArrayList<>();
//
//        // perm_id, code, proj_id, expe_id, saty_id, pers_id_registerer, pers_id_modifier, space_id
//        for (List<Object> sampleInsertArgs:samplesInsertArgs) {
//            // perm_id, code, saty_id, pers_id_registerer, pers_id_modifier
//            List<Object> SAMPLE_INITIAL_INSERT_ARG = new ArrayList<>();
//            SAMPLE_INITIAL_INSERT_ARG.add(sampleInsertArgs.get(0));
//            SAMPLE_INITIAL_INSERT_ARG.add(sampleInsertArgs.get(1));
//            SAMPLE_INITIAL_INSERT_ARG.add(sampleInsertArgs.get(2));
//            SAMPLE_INITIAL_INSERT_ARG.add(sampleInsertArgs.get(5));
//            SAMPLE_INITIAL_INSERT_ARG.add(sampleInsertArgs.get(6));
//            SAMPLE_INITIAL_INSERT_ARG.add(sampleInsertArgs.get(7));
//            SAMPLE_INITIAL_INSERT_ARGS.add(SAMPLE_INITIAL_INSERT_ARG);
//
//            // proj_id = ?, expe_id, = ?, space_id = ? WHERE permId = ?
//            List<Object> SAMPLE_UPDATE_INSERT_ARG = new ArrayList<>();
//            SAMPLE_UPDATE_INSERT_ARG.add(sampleInsertArgs.get(3));
//            SAMPLE_UPDATE_INSERT_ARG.add(sampleInsertArgs.get(4));
//            SAMPLE_UPDATE_INSERT_ARG.add(sampleInsertArgs.get(8));
//            SAMPLE_UPDATE_INSERT_ARG.add(sampleInsertArgs.get(0));
//            SAMPLE_UPDATE_INSERT_ARGS.add(SAMPLE_UPDATE_INSERT_ARG);
//        }
//        try {
//            connection.setAutoCommit(false);
//            SQLExecutor.executeUpdate(connection, SAMPLE_INITIAL_INSERT, SAMPLE_INITIAL_INSERT_ARGS);
//            SQLExecutor.executeUpdate(connection, SAMPLE_UPDATE_INSERT, SAMPLE_UPDATE_INSERT_ARGS);
//            connection.commit();
//        } catch (Exception ex) {
//            try {
//                connection.rollback();
//            } catch (Exception ex2) {}
//        } finally {
//            try {
//                connection.setAutoCommit(true);
//            } catch (Exception ex3) {}
//        }
//
//        return samplesInsertArgs.size();
//    }

    private static final String SAMPLE_INSERT_HIBERNATE = "insert into samples (code, samp_id_part_of, cont_frozen, del_id, expe_frozen, expe_id, frozen, frozen_for_children, frozen_for_comp, frozen_for_data, frozen_for_parents, modification_timestamp, pers_id_modifier, orig_del, perm_id, proj_id, proj_frozen, pers_id_registerer, saty_id, space_id, space_frozen, version, id) values (?, NULL, FALSE, NULL, FALSE, ?, FALSE, FALSE, FALSE, FALSE, FALSE, NOW(), ?, NULL, ?, ?, FALSE, ?, ?, ?, FALSE, 1, ?)";

    // code, expe_id, pers_id_modifier, perm_id, proj_id, pers_id_registerer, saty_id, space_id, version, id

    public static int insertSamples(Connection connection, List<List<Object>> samplesInsertArgs) {
        List<List<Object>> SAMPLE_HIBERNATE_INSERT_ARGS = new ArrayList<>();
        for (List<Object> sampleInsertArgs:samplesInsertArgs) {
            List<Object> SAMPLE_HIBERNATE_INSERT_ARG = new ArrayList<>();
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(2));
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(4));
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(6));
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(1));
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(3));
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(6));
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(5));
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(8));
            SAMPLE_HIBERNATE_INSERT_ARG.add(sampleInsertArgs.get(0));
            SAMPLE_HIBERNATE_INSERT_ARGS.add(SAMPLE_HIBERNATE_INSERT_ARG);
        }
        SQLExecutor.executeUpdate(connection, SAMPLE_INSERT_HIBERNATE, SAMPLE_HIBERNATE_INSERT_ARGS);

        return samplesInsertArgs.size();
    }

    private static final String NEXT_SAMPLE_IDS = "SELECT setval('sample_id_seq', nextval('sample_id_seq') + 5000, true)";

    public static List<Long> nextSampleIds(Connection connection, int number) {
        List<Map<String, Object>> lastRes = SQLExecutor.executeQuery(connection, NEXT_SAMPLE_IDS, Arrays.asList());
        long last = (Long) lastRes.get(0).get("setval");
        long first = last - number;
        List<Long> results = new ArrayList<>(number);
        for (int i = 0; i < number; i++) {
            results.add(first + 1 + i);
        }
        return results;
    }

    private static final String SELECT_STPT = "SELECT id, saty_id, prty_id  FROM sample_type_property_types WHERE FALSE";

    public static Map<String, Long> getSampleTypePropertyTypeIds(Connection connection, Collection<String> sampleType_propertyTypes) {
        Map<String, Long> sampleTypePropertyTypeIdsByIdentifier = new HashMap<>();

        StringBuilder SELECT_STPT_WITH_ORS = new StringBuilder(SELECT_STPT);
        for (String sampleType_propertyType:sampleType_propertyTypes) {
            SELECT_STPT_WITH_ORS.append(" OR (saty_id = " + sampleType_propertyType.split(":")[0] + " AND prty_id = " + sampleType_propertyType.split(":")[1] + ")");
        }

        List<Map<String, Object>> sampleTypePropertyTypeIdsWithReferences = SQLExecutor.executeQuery(connection, SELECT_STPT_WITH_ORS.toString(), Arrays.asList());
        for (Map<String, Object> sampleTypePropertyTypeIdsWithReference:sampleTypePropertyTypeIdsWithReferences) {
            sampleTypePropertyTypeIdsByIdentifier.put(
                    sampleTypePropertyTypeIdsWithReference.get("saty_id") + ":" + sampleTypePropertyTypeIdsWithReference.get("prty_id")
                    ,
                    (Long) sampleTypePropertyTypeIdsWithReference.get("id")
            );
        }

        return sampleTypePropertyTypeIdsByIdentifier;
    }

    private static final String SAMPLE_PROPERTIES_INSERT = "INSERT INTO sample_properties(id, samp_id, stpt_id, value, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_author) VALUES(nextval('sample_property_id_seq'), ?, ?, ?, NOW(), NOW(), ?, ?)";

    public static int insertSamplesProperties(Connection connection, List<List<Object>> samplesPropertiesArgs) {
        try {
            connection.setAutoCommit(false);
            SQLExecutor.executeUpdate(connection, SAMPLE_PROPERTIES_INSERT, samplesPropertiesArgs);
            connection.commit();
        } catch (Exception ex) {
            try {
                connection.rollback();
            } catch (Exception ex2) {}
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (Exception ex3) {}
        }
        return samplesPropertiesArgs.size();
    }

    //
    // Utility methods
    //

    private static <VALUE, KEY> Map<VALUE,KEY> revertMap(Map<KEY, VALUE> map) {
        Map<VALUE, KEY> rMap = new HashMap<>(map.size());
        for (KEY key:map.keySet()) {
            rMap.put(map.get(key), key);
        }
        return rMap;
    }

}
