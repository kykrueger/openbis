/*
 * Copyright 2011 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

import java.util.*;
import java.util.stream.Collectors;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;

public class PostgresAuthorisationInformationProviderDAO implements ISQLAuthorisationInformationProviderDAO
{

    private ISQLExecutor executor;

    public PostgresAuthorisationInformationProviderDAO(ISQLExecutor executor)
    {
        this.executor = executor;
    }

    /**
     * Finds IDs of spaces and projects to which the current user has access.
     *
     * @param userId the ID of the user.
     * @return set of IDs of spaces authorized for the current user.
     */
    @Override @SuppressWarnings("unchecked")
    public AuthorisationInformation findAuthorisedSpaceProjectIDs(final Long userId)
    {
        final String p = "p";
        final String g = "g";
        final String ag = "ag";
        final String per = "per";
        final String pRoleCode = p + ".role_code";
        final String gRoleCode = g + ".role_code";
        final String pSpaceId = p + ".space_id";
        final String gSpaceId = g + ".space_id";
        final String pProjectId = p + ".project_id";
        final String gProjectId = g + ".project_id";
        final String query = SELECT + SP + p + PERIOD + ROLE_COLUMN + SP + DQ + pRoleCode + DQ + COMMA + SP +
                g + PERIOD + ROLE_COLUMN + SP + DQ + gRoleCode + DQ + COMMA + SP +
                p + PERIOD + SPACE_COLUMN + SP + DQ + pSpaceId + DQ + COMMA + SP +
                p + PERIOD + PROJECT_ID_COLUMN + SP + DQ + pProjectId + DQ + COMMA + SP +
                g + PERIOD + SPACE_COLUMN + SP + DQ + gSpaceId + DQ + SP + COMMA + SP +
                g + PERIOD + PROJECT_ID_COLUMN + SP + DQ + gProjectId + DQ + NL +
                FROM + SP + PERSONS_TABLE + SP + per + NL +
                LEFT_JOIN + SP + AUTHORIZATION_GROUP_PERSONS_TABLE + SP + ag + SP +
                ON + SP + per + PERIOD + ID_COLUMN + SP + EQ + SP + ag + PERIOD + PERSON_ID_COLUMN + NL +
                LEFT_JOIN + SP + ROLE_ASSIGNMENTS_TABLE + SP + p + SP +
                ON + SP + per + PERIOD + ID_COLUMN + SP + EQ + SP + p + PERIOD + PERSON_GRANTEE_COLUMN + NL +
                LEFT_JOIN + SP + ROLE_ASSIGNMENTS_TABLE + SP + g + SP +
                ON + SP + ag + PERIOD + AUTHORIZATION_GROUP_ID_COLUMN + SP + EQ + SP + g + PERIOD +
                AUTHORIZATION_GROUP_ID_GRANTEE_COLUMN + NL +
                WHERE + SP + per + PERIOD + ID_COLUMN + SP + EQ + SP + QU;
        final List<Object> args = Collections.singletonList(userId);
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        final AuthorisationInformation result = new AuthorisationInformation(new HashSet<>(), new HashSet<>(), new HashSet<>());
        queryResultList.forEach(resultRow ->
        {
            final Object roleIdPerson = resultRow.get(p + PERIOD + ROLE_COLUMN);
            final Object spaceIdPerson = resultRow.get(p + PERIOD + SPACE_COLUMN);
            final Object projectIdPerson = resultRow.get(p + PERIOD + PROJECT_ID_COLUMN);
            final Object roleIdGroup = resultRow.get(g + PERIOD + ROLE_COLUMN);
            final Object spaceIdGroup = resultRow.get(g + PERIOD + SPACE_COLUMN);
            final Object projectIdGroup = resultRow.get(g + PERIOD + PROJECT_ID_COLUMN);

            final boolean isInstanceRole = spaceIdPerson == null && projectIdPerson == null &&
                    spaceIdGroup == null && projectIdGroup == null;
            if (isInstanceRole)
            {
                if (roleIdPerson != null)
                {
                    result.getInstanceRoles().add(Role.valueOf((String) roleIdPerson));
                }
                if (roleIdGroup != null)
                {
                    result.getInstanceRoles().add(Role.valueOf((String) roleIdGroup));
                }
            }

            if (spaceIdPerson != null)
            {
                result.getSpaceIds().add((long) spaceIdPerson);
            }
            if (projectIdPerson != null)
            {
                result.getProjectIds().add((long) projectIdPerson);
            }
            if (spaceIdGroup != null)
            {
                result.getSpaceIds().add((long) spaceIdGroup);
            }
            if (projectIdGroup != null)
            {
                result.getProjectIds().add((long) projectIdGroup);
            }
        });
        return result;
    }

    @Override
    public Set<Long> getAuthorisedSamples(final Set<Long> requestedIDs, final AuthorisationInformation authInfo)
    {
        final String query = SELECT + SP + DISTINCT + SP + ID_COLUMN + NL +
                FROM + SP + TableMapper.SAMPLE.getEntitiesTable() + NL +
                WHERE + SP + ID_COLUMN + SP + IN + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + AND + NL +
                LP + SPACE_COLUMN + SP + IN + SP + LP +
                SELECT + SP + UNNEST + LP + QU + RP + RP + SP + OR + NL +
                PROJECT_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + OR + SP +
                EXPERIMENT_COLUMN + SP + IN + SP + LP +
                        SELECT + SP + ID_COLUMN + SP +
                        FROM + SP + TableMapper.EXPERIMENT.getEntitiesTable() + SP +
                        WHERE + SP + PROJECT_COLUMN + SP + IN + SP + SELECT_UNNEST + RP + OR + NL +
                SPACE_COLUMN + SP + IS_NULL + SP + AND + SP + PROJECT_COLUMN + SP + IS_NULL + RP;
        final Long[] projectIds = authInfo.getProjectIds().toArray(new Long[0]);
        final List<Object> args = Arrays.asList(requestedIDs.toArray(new Long[0]), authInfo.getSpaceIds().toArray(new Long[0]),
                projectIds, projectIds);
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        return collectIDs(queryResultList);
    }

    @Override
    public Set<Long> getAuthorisedExperiments(final Set<Long> requestedIDs, final AuthorisationInformation authInfo)
    {
        final String e = "e";
        final String p = "p";
        final String query = SELECT + SP + DISTINCT + SP + e + PERIOD + ID_COLUMN + NL +
                FROM + SP + TableMapper.EXPERIMENT.getEntitiesTable() + SP + e + NL +
                INNER_JOIN + SP + PROJECTS_TABLE + SP + p + SP + ON + SP + p + PERIOD + ID_COLUMN + SP + EQ + SP + e + PERIOD + PROJECT_COLUMN + SP +
                WHERE + SP + e + PERIOD + ID_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + AND +
                SP + LP + p + PERIOD + SPACE_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + SP + OR +
                SP + e + PERIOD + PROJECT_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP + RP;

        final List<Object> args = Arrays.asList(requestedIDs.toArray(new Long[0]), authInfo.getSpaceIds().toArray(new Long[0]),
                authInfo.getProjectIds().toArray(new Long[0]));
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        return collectIDs(queryResultList);
    }

    @Override
    public Set<Long> getTagsOfUser(final Set<Long> requestedIDs, final Long userID)
    {
        final String query = SELECT + SP + ID_COLUMN + SP + NL +
                FROM + SP + METAPROJECTS_TABLE + NL +
                WHERE + SP + OWNER_COLUMN + SP + EQ + SP + QU + SP +
                AND + SP + ID_COLUMN + SP + IN + LP + SELECT + SP + UNNEST + LP + QU + RP + RP;
        final List<Object> args = Arrays.asList(userID, requestedIDs.toArray(new Long[0]));
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);
        return collectIDs(queryResultList);
    }

    private Set<Long> collectIDs(final List<Map<String, Object>> queryResultList)
    {
        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(ID_COLUMN)).collect(Collectors.toSet());
    }

}
