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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.entitytype.EntityKind;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.sql.mappers.EntityMapper;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DAOFactory;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.AUTHORIZATION_GROUP_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.AUTHORIZATION_GROUP_ID_GRANTEE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_GRANTEE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERSON_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PROJECT_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SPACE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.AUTHORIZATION_GROUP_PERSONS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PERSONS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.ROLE_ASSIGNMENTS_TABLE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_ALL_TABLE;

public class PostgresSearchDAO implements ISQLSearchDAO
{

    public Session currentSession()
    {
        final DAOFactory daoFactory = CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY, DAOFactory.class);
        return daoFactory.getSessionFactory().getCurrentSession();
    }

    public Set<Long> queryDBWithNonRecursiveCriteria(final EntityKind entityKind, final List<ISearchCriteria> criteria,
            final SearchOperator operator)
    {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SpaceProjectIDsVO findAuthorisedSpaceProjectIDs(final Long userId)
    {
        final Session session = currentSession();
        final NativeQuery query = session.createNativeQuery(
                "SELECT rap." + SPACE_COLUMN + ", rap." + PROJECT_ID_COLUMN + ", rag." + SPACE_COLUMN + ", rag." + PROJECT_ID_COLUMN + "\n" +
                "FROM " + PERSONS_TABLE + " p\n" +
                "LEFT JOIN " + AUTHORIZATION_GROUP_PERSONS_TABLE + " ag ON (p." + ID_COLUMN + " = ag." + PERSON_ID_COLUMN + ")\n" +
                "LEFT JOIN " + ROLE_ASSIGNMENTS_TABLE + " rap ON (p." + ID_COLUMN + " = rap." + PERSON_GRANTEE_COLUMN + ")\n" +
                "LEFT JOIN " + ROLE_ASSIGNMENTS_TABLE + " rag ON (ag." + AUTHORIZATION_GROUP_ID_COLUMN + " = rag." +
                        AUTHORIZATION_GROUP_ID_GRANTEE_COLUMN + ")\n" +
                "WHERE p." + ID_COLUMN + " = :userId");
        query.setParameter("userId", userId);
        final List<Object[]> queryResultList = query.getResultList();

        final SpaceProjectIDsVO result = new SpaceProjectIDsVO();
        for (final Object[] resultRow : queryResultList)
        {
            final Object spaceIdPerson = resultRow[0];
            final Object projectIdPerson = resultRow[1];
            final Object spaceIdGroup = resultRow[2];
            final Object projectIdGroup = resultRow[3];
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
        }
        return result;
    }

    @Override
    public Set<Long> filterSampleIDsBySpaceAndProjectIDs(final Set<Long> ids,
            final SpaceProjectIDsVO authorizedSpaceProjectIds)
    {
        final Session session = currentSession();
        final NativeQuery<Long> query = session.createNativeQuery(
                "SELECT DISTINCT " + ID_COLUMN + "\n" +
                "FROM " + SAMPLES_ALL_TABLE + "\n" +
                "WHERE " + ID_COLUMN + " IN (:ids) AND (" + SPACE_COLUMN + " IN (:spaceIds) " +
                "OR " + PROJECT_COLUMN + " IN (:projectIds))",
                Long.class);
        query.setParameter("ids", ids);
        query.setParameter("spaceIds", authorizedSpaceProjectIds.getSpaceIds());
        query.setParameter("projectIds", authorizedSpaceProjectIds.getProjectIds());
        return new HashSet<>(query.getResultList());
    }

    @Override
    public Set<Long> findChildIDs(final EntityKind entityKind, final Set<Long> parentIdSet)
    {
        final EntityMapper dbEntityKind = EntityMapper.toDBEntityKind(entityKind);
        final Session session = currentSession();
        final NativeQuery<Long> query = session.createNativeQuery(
                "SELECT DISTINCT " + dbEntityKind.getRelationshipsTableChildIdField() + "\n" +
                "FROM " + dbEntityKind.getRelationshipsTable() + "\n" +
                "WHERE " + dbEntityKind.getRelationshipsTableParentIdField() + " IN (:parentIds)", Long.class);
        query.setParameter("parentIds", parentIdSet);
        return new HashSet<>(query.getResultList());
    }

    @Override
    public Set<Long> findParentIDs(final EntityKind entityKind, final Set<Long> childIdSet)
    {
        final EntityMapper dbEntityKind = EntityMapper.toDBEntityKind(entityKind);
        final Session session = currentSession();
        final NativeQuery<Long> query = session.createNativeQuery(
                "SELECT DISTINCT " + dbEntityKind.getRelationshipsTableParentIdField() + "\n" +
                "FROM " + dbEntityKind.getRelationshipsTable() + "\n" +
                "WHERE " + dbEntityKind.getRelationshipsTableChildIdField() + " IN (:childIds)", Long.class);
        query.setParameter("childIds", childIdSet);
        return new HashSet<>(query.getResultList());
    }

}
