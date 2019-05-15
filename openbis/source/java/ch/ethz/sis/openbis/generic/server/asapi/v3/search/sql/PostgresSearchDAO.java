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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.AbstractDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class PostgresSearchDAO extends AbstractDAO implements ISQLSearchDAO
{

    protected PostgresSearchDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    /*
     *
     */
    public Set<Long> queryDBWithNonRecursiveCriteria(final EntityKind entityKind, final List<ISearchCriteria> criteria,
            final SearchOperator operator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getResultSetSizeLimit()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public SpaceProjectIDsVO getAuthorisedSpaceProjectIds(final Long userId) {
        final Session session = currentSession();
        final NativeQuery query = session.createNativeQuery(
                "SELECT rap.space_id, rap.project_id, rag.space_id, rag.project_id\n" +
                "FROM " + PERSONS_TABLE + " p\n" +
                "LEFT JOIN " + AUTHORIZATION_GROUP_PERSONS_TABLE + " ag ON (p.id = ag.pers_id)\n" +
                "LEFT JOIN " + ROLE_ASSIGNMENTS_TABLE + " rap ON (p.id = rap.pers_id_grantee)\n" +
                "LEFT JOIN " + ROLE_ASSIGNMENTS_TABLE + " rag ON (ag.ag_id = rag.ag_id_grantee)\n" +
                "WHERE p.id = :userId");
        query.setParameter("userId", userId);
        final List<Object[]> queryResultList = query.getResultList();

        final SpaceProjectIDsVO result = new SpaceProjectIDsVO();
        for (final Object[] resultRow : queryResultList) {
            final Object spaceIdPerson = resultRow[0];
            final Object projectIdPerson = resultRow[1];
            final Object spaceIdGroup = resultRow[2];
            final Object projectIdGroup = resultRow[3];
            if (spaceIdPerson != null) {
                result.getSpaceIds().add((long) spaceIdPerson);
            }
            if (projectIdPerson != null) {
                result.getProjectIds().add((long) projectIdPerson);
            }
            if (spaceIdGroup != null) {
                result.getSpaceIds().add((long) spaceIdGroup);
            }
            if (projectIdGroup != null) {
                result.getProjectIds().add((long) projectIdGroup);
            }
        }
        return result;
    }

    @Override
    public Set<Long> filterSampleIDsBySpaceAndProjectIDs(final Set<Long> ids,
            final SpaceProjectIDsVO authorizedSpaceProjectIds) {
        final Session session = currentSession();
        final NativeQuery query = session.createNativeQuery(
                "SELECT DISTINCT id\n" +
                "FROM " + SAMPLES_ALL_TABLE + "\n" +
                "WHERE id IN (:ids) AND (space_id IN (:spaceIds) OR proj_id IN (:projectIds))");
        query.setParameter("ids", ids);
        query.setParameter("spaceIds", authorizedSpaceProjectIds.getSpaceIds());
        query.setParameter("projectIds", authorizedSpaceProjectIds.getProjectIds());

//        final IDAOFactory daoFactory = CommonServiceProvider.getApplicationContext().getBean(ComponentNames.DAO_FACTORY,
//                IDAOFactory.class);
//        final Session currentSession = daoFactory.getSessionFactory().getCurrentSession();
//        querySampleTypeId = currentSession.createSQLQuery("SELECT id from sample_types WHERE code = :sampleTypeCode");
//        querySampleTypeId.setParameter("sampleTypeCode", sampleTypeCode);
//        sampleTypeId = querySampleTypeId.uniqueResult();
        return new HashSet<>(query.getResultList());
    }

}
