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

    private static final String SAMPLE_INSERT = "INSERT INTO samples_all(id, perm_id, code, proj_id, expe_id, saty_id, registration_timestamp, modification_timestamp, pers_id_registerer, pers_id_modifier, space_id) VALUES(nextval('sample_id_seq'), ?, ?, ?, ?, ?, NOW(), NOW(), ?, ?, ?)";

    public static int insertSamples(Connection connection, List<List<Object>> sampleInsertArgs) {
        return SQLExecutor.executeUpdate(connection, SAMPLE_INSERT, sampleInsertArgs);
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
