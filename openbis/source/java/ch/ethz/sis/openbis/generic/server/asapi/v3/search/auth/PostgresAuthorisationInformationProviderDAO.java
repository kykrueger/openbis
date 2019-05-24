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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.roleassignment.Role;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.ISQLExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.AUTHORIZATION_GROUP_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.AUTHORIZATION_GROUP_ID_GRANTEE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_GRANTEE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ROLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.AUTHORIZATION_GROUP_PERSONS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PERSONS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.ROLE_ASSIGNMENTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;

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
        final String query = "SELECT " + p + "." + ROLE_COLUMN + " as \"p.role_code\", " + g + "." + ROLE_COLUMN + " as \"g.role_code\"," +
                p + "." + SPACE_COLUMN + " as \"p.space_id\", " + p + "." + PROJECT_ID_COLUMN + " as \"p.project_id\", " +
                g + "." + SPACE_COLUMN + " as \"g.space_id\", " + g + "." + PROJECT_ID_COLUMN + " as \"g.project_id\"\n" +
                "FROM " + PERSONS_TABLE + " per\n" +
                "LEFT JOIN " + AUTHORIZATION_GROUP_PERSONS_TABLE + " ag ON per." + ID_COLUMN + " = ag." + PERSON_ID_COLUMN + "\n" +
                "LEFT JOIN " + ROLE_ASSIGNMENTS_TABLE + " " + p + " ON per." + ID_COLUMN + " = " + p + "." + PERSON_GRANTEE_COLUMN + "\n" +
                "LEFT JOIN " + ROLE_ASSIGNMENTS_TABLE + " " + g + " ON ag." + AUTHORIZATION_GROUP_ID_COLUMN + " = " + g + "." +
                AUTHORIZATION_GROUP_ID_GRANTEE_COLUMN + "\n" +
                "WHERE per." + ID_COLUMN + " = ?";
        final List<Object> args = Collections.singletonList(userId);
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        final AuthorisationInformation result = new AuthorisationInformation(new HashSet<>(), new HashSet<>(), new HashSet<>());
        queryResultList.forEach(resultRow ->
        {
            final Object roleIdPerson = resultRow.get(p + "." + ROLE_COLUMN);
            final Object spaceIdPerson = resultRow.get(p + "." + SPACE_COLUMN);
            final Object projectIdPerson = resultRow.get(p + "." + PROJECT_ID_COLUMN);
            final Object roleIdGroup = resultRow.get(g + "." + ROLE_COLUMN);
            final Object spaceIdGroup = resultRow.get(g + "." + SPACE_COLUMN);
            final Object projectIdGroup = resultRow.get(g + "." + PROJECT_ID_COLUMN);

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

    /**
     * Filters sample IDs based on their relations to space and projects.
     *
     * @param requestedIDs the IDs to be filtered
     * @param authInfo value object that contains space and project IDs, which should be related to the
     *     resulting IDs.
     * @return the subset of IDs which are related either to one of the specified projects or spaces.
     */
    @Override public Set<Long> getAuthorisedSamples(final Set<Long> requestedIDs,
            final AuthorisationInformation authInfo)
    {
        final String query = "SELECT DISTINCT " + ID_COLUMN + "\n" +
                "FROM " + SAMPLES_ALL_TABLE + "\n" +
                "WHERE " + ID_COLUMN + " IN (SELECT unnest(?)) AND (" + SPACE_COLUMN + " IN (SELECT unnest(?)) " +
                "OR " + PROJECT_COLUMN + " IN (SELECT unnest(?)))";
        final List<Object> args = Arrays
                .asList(requestedIDs.toArray(), authInfo.getSpaceIds().toArray(), authInfo.getProjectIds().toArray());
        final List<Map<String, Object>> queryResultList = executor.execute(query, args);

        return queryResultList.stream().map(stringObjectMap -> (Long) stringObjectMap.get(ID_COLUMN)).collect(Collectors.toSet());
    }

}
